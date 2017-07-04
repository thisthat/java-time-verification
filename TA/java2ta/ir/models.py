import abc
from java2ta.ir.client import RestfulAPIClient

class Project(object):

    DEFAULT_URL = "localhost:9000"

    STATUS_CHOICES = {
        "closed": "closed",
        "opening": "opening",
        "open": "open",
    }

    MAX_ATTEMPTS = 100

    def __init__(self, name, path=None, api_url=None):
        
        self.name = name
        self.path = path
        self.status = Project.STATUS_CHOICES["closed"]

        api_url = api_url or Project.DEFAULT_URL

        self.client = RestfulAPIClient(api_url)


    def set_status(self, label):
        """
        Set the current status to the one corresponding the passed label
        """
    
        if label not in Project.STATUS_CHOICES:
            raise ValueError("Expected label in ('%s'). Passed: '%s'" % (",".join(Project.STATUS_CHOICES.keys()), label))

        self.status = Project.STATUS_CHOICES[label]


    def is_status(self, label):
        """
        Return True iff the current status corresponds to the passed label
        """
    
        if label not in Project.STATUS_CHOICES:
            raise ValueError("Expected label in ('%s'). Passed: '%s'" % (",".join(Project.STATUS_CHOICES.keys()), label))

        return self.status == Project.STATUS_CHOICES[label]


    def open(self, sync=False):
        
        data = {
            "name": self.name,
            "path": self.path,
        }

        self.client.post("/openProject", data)
        self.set_status("opening")


        if sync:
            is_open = False
            attempts = 1

            while not is_open and attempts < Project.MAX_ATTEMPTS:
                # wait till the project is in a final status (open or closed)
                to_wait = (attempts/2)**2
                log.debug("Wait %s seconds and check the project is open ..." % to_wait)
 
                sleep(to_wait)
                is_open = self.is_open()
                attempts = attempts + 1
                
            if not is_open:
                raise ValueError("The project cannot be opened after %s attempts" % attempts)

            

    def is_open(self):
    
        if self.is_status("open"):
            return True
        elif self.is_status("closed"):
            return False
        else:
            curr_status = self.client.post("/getStatus", { "name":self.name })

            assert "status" in curr_status, "Wrong status information."

            if curr_status["status"] == "error":
                description = curr_status["description"]
                raise Exception("Error when getting project status: %s" % description)

            self.status = curr_status["status"]

            return self.is_status("open")


    def clean(self):
        data = {
            "name": self.name,
        }

        self.client.post("/clean", data)
        self.set_status("closed")


    def get_threads(self, name=None):
    
        found_threads = []
    
        if self.is_open():
    
            data = { "name": self.name }
            all_threads = self.client.post("/getThreads", data)

            if name is not None:
                found_threads = filter(lambda t: t["className"] == name, 
                                        all_threads)
            else:
                found_threads = all_threads

        return found_threads


    def get_files(self, type=None):

        files = []

        if self.is_open():
            data = { "name": self.name }
            url = "/getAllFiles"

            if type:    
                data["type"] = type
                url = "/getFilesByType"
            else:
                data["skipTest"] = 0
                
            files = self.client.post(url, data)

        return files

    def get_file(self, path):

        file = None

        if not path.startswith("file://"):
            path = "file://" + path

        if self.is_open():
            data = { "name": self.name, "filePath": path }
            file = self.client.post("/getFile", data)

        if file is not None and isinstance(file, dict):
            file = [ file ]

        return file

    def get_class(self, name, path):
        files = self.get_file(path)

        found = None

        for curr in files:
            if curr["name"] == name:
                found = curr
                break

        return found

    def get_mains(self):
        
        mains = []

        if self.is_open():
            data = { "name": self.name }
            mains = self.client.post("/getMains", data)

        return mains


class ASTNode(object):
    """
    This is an abstract class to represent relevant nodes of the source AST.
    Since the AST is the Intermediate Representation obtained invoking the 
    webservice, we cache the AST once obtained and try to minimize the 
    service invokations.
    """
    __metaclass__ = abc.ABCMeta

    def __init__(self, name, project, parent=None):
        self.name = name
        self.project = project
        self._ast = None
        self.parent = parent
        self.children = []

        if self.parent is not None:
            self.parent.add_child(self)

    def add_child(self, child):
        assert isinstance(child, ASTNode)

        self.children.append(child)

    @property
    def ast(self):
        if not self._ast:
            self._ast = self.get_ast()

            assert isinstance(self._ast, dict)

        return self._ast

    @abc.abstractmethod
    def get_ast(self):
        """
        This is the method where the extending class should put the logic for
        invoking the service and finding the desired node in the Intermediate
        Representation.
        """
        pass


class Klass(ASTNode):

    def __init__(self, name, package_name, path, project):

        # at the moment, Klasses are root elements
        super(Klass, self).__init__(name, project)

        self.name = name
        self.path = path
        self.package_name = package_name

    def get_ast(self):
        classes = self.project.get_file(self.path)
        
        found = None

        for curr in classes:
            assert isinstance(curr, dict)
            assert "name" in curr
            assert "packageName" in curr

            if curr["name"] == self.name and \
                    curr["packageName"] == self.package_name:
                found = curr

        if not found:
            full_name = self.name
            if self.package_name:
                full_name = "%s.%s" % (self.package_name, self.full_name)

            raise Exception("It was not possible to import class '%s' from file '%s'" % (full_name, self.path))

        return found


class Thread(Klass):
    """
    This is a Klass extending the java.lang.Runnable interface 
    TODO other interfaces to consider?
    TODO add a check that the wrapped class indeed extends the Runnable interface
    """
    def __init__(self, name, package_name, project):

        super(Thread, self).__init__(name, package_name, "", project)


    def get_ast(self):
        threads = self.project.get_threads(self.name)

        assert len(threads) > 0

        # TODO at the moment consider only there is only one possible thread
        # with the given name. FIX ASAP
        thread = threads[0]

        assert isinstance(thread, dict)
        assert "className" in thread
        assert "packageName" in thread
        assert "path" in thread
      

        #print "curr thread: %s" % thread 
        classes_ir = self.project.get_file(thread["path"])
        #print "classes: %s" % classes_ir        

        found = filter(lambda ir: ir["name"] == self.name and ir["packageName"] == self.package_name, classes_ir)
    
        assert len(found) <= 1
        
        if len(found) == 0:
            full_name = self.name
            if self.package_name:
                full_name = "%s.%s" % (self.package_name, self.full_name)

            raise Exception("It was not possible to import class '%s' from file '%s'" % (full_name, self.path))

        self.path = found[0]["path"]

        return found[0]




class Method(ASTNode):

    def __init__(self, name, klass):
 
        assert isinstance(name, basestring)       
        assert isinstance(klass, Klass)

        super(Method, self).__init__(name, klass.project, parent=klass)
        self.klass = klass

    def get_ast(self):
        methods = self.klass.ast["methods"]

        found = filter(lambda m: m["name"] == self.name, methods)

        # TODO at the moment we only take the first method with the given name
        # (due to method overloading, the language may have more than one).
        # Fix this ASAP
        return found[0]
