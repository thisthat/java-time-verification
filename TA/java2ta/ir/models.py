import abc
from collections import deque
from contracts import contract
from time import sleep

import json
from java2ta.commons.utility import new_contract_check_type
from java2ta.ir.client import RestfulAPIClient

import logging

log = logging.getLogger("main")

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

            assert isinstance(self._ast, dict), "%s - %s" % (type(self), self._ast)


        return self._ast

    @abc.abstractmethod
    def get_ast(self):
        """
        This is the method where the extending class should put the logic for
        invoking the service and finding the desired node in the Intermediate
        Representation.
        """
        pass

    @property
    @contract(returns="bool")
    def exists(self):
        """
        This property forces to load the ast (it is likely that loading the current object
        AST we force also the ancestor ASTs to be loaded)
        """
        return self.ast != None


new_contract_check_type("is_ast_node", ASTNode)


class Klass(ASTNode):

    def __init__(self, name, package_name, path, project):

        # at the moment, Klasses are root elements
        super(Klass, self).__init__(name, project)

        self.name = name
        self.path = path
        self.package_name = package_name

    @property
    def fqname(self):
        return "%s.%s" % (self.package_name, self.name)

    def get_ast(self):
        classes = self.project.get_file(self.path)
        
        if not classes:
            raise ValueError("No class found in file (%s)'" % (self.path,))

        found = None

        for curr in classes:
            assert isinstance(curr, dict)
            assert "name" in curr
            assert "packageName" in curr

            log.debug("Check: class name=%s, class package name=%s, looked name=%s, looked package name=%s" % (curr["name"], curr["packageName"], self.name, self.package_name))
            #print "check class, package name: %s, %s" % (curr["name"], curr["packageName"])

            if curr["name"] == self.name and \
                    curr["packageName"] == self.package_name:
                found = curr

        if not found:
            full_name = self.name
            if self.package_name:
                full_name = "%s.%s" % (self.package_name, full_name)

            raise Exception("Unable to load class '%s' from file '%s'" % (full_name, self.path))

        return found

new_contract_check_type("is_klass", Klass)


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
                full_name = "%s.%s" % (self.package_name, full_name)

            raise Exception("It was not possible to import class '%s' from file '%s'" % (full_name, self.path))

        self.path = found[0]["path"]

        return found[0]

class Method(ASTNode):

    @contract(name="string", klass="is_klass") # buttare il seguito: parent="is_klass|is_ir_varaible") # TODO we cannot specify contract is_ir_variable because there is a circular dependency b/w is_ir_variable and is_method; fix function "new_contract_check_type" to take also strings with fully qualified names of types in place of taking directly the class
    def __init__(self, name, klass):
 
        assert isinstance(name, basestring)       

        super(Method, self).__init__(name, klass.project, parent=klass)


    @property
    @contract(returns="string")
    def fqname(self):
        assert hasattr(self.parent, "fqname")
        return "%s.%s" % (self.parent.fqname, self.name)

    @contract(returns="dict")
    def get_ast(self):

        assert isinstance(self.parent, Klass)

        methods = []

        methods = self.parent.ast["methods"]

        log.debug("Current method: %s" % self.name)
        log.debug("Found %s methods: %s" % (len(methods), map(lambda m: m["name"], methods)))

        found = filter(lambda m: m["name"] == self.name, methods)

        # TODO at the moment we only take the first method with the given name
        # (due to method overloading, the language may have more than one).
        # Fix this ASAP
        res = None
        if len(found) > 0:
            res = found[0]
        return res

    @property
    @contract(returns="list(dict)")
    def instructions(self):
        return self.ast["stms"]

    @property
    @contract(returns="bool")
    def has_instructions(self):
        return len(self.instructions) > 0
    

new_contract_check_type("is_method", Method)


class Variable(ASTNode):

    @contract(name="string", method="is_method")
    def __init__(self, name, method):
        super(Variable, self).__init__(name, method.project, parent=method)
        self.method = method

    @property
    def fqname(self):
        return "%s.%s" % (self.method.fqname, self.name)

    def get_ast(self):
 
        # 1. get the root of the method node

        # 2. walk through the dictionary structure looking for the first occurrence of a variable declaration with the given name (there may be more than one, this is unpleasant) => TODO actually we don't look for the first occurrence of variable declaration, but for the first variable declaration/assignment that has a 'hiddenClass' within the 'expr' component FIX THIS!!!

        # 3. if a node for the variable name is found, it is the ast we were searching for, otherwise raise an exception
        
        method_node = self.method.get_ast()

        var_declarations = []

        # use the visitor pattern
        def check_var_declaration(node):
            """
            Handler for ASTVariableDeclaration
            """
            assert node["nodeType"] == "ASTVariableDeclaration"

            if node["name"]["nodeType"] == "ASTIdentifier" and node["name"]["value"] == self.name and \
                    "expr" in node and "hiddenClass" in node["expr"]:
                return node
        
            return None

        def check_var_assignment(node):
            """
            Handler for ASTAssignment
            """
            assert node["nodeType"] == "ASTAssignment"
            assert "left" in node

            if node["left"]["value"] == self.name and "hiddenClass" in node["right"]:
                return node

            return None
 
        visitor = ASTVisitor(method_node)
        visitor.add_handler("ASTVariableDeclaration", check_var_declaration)
        visitor.add_handler("ASTAssignment", check_var_assignment)
        var_ast = visitor.search()
           

        if not var_ast:
            raise ValueError("Cannot find variable '%s' in method %s. Method AST: %s" % (self.name, self.method.name, self.method.get_ast()))

#        assert "hiddenClass" in var_ast, var_ast

        return var_ast
            

new_contract_check_type("is_ir_variable", Variable)


class InnerKlass(Klass):

    @contract(v="is_ir_variable")
    def __init__(self, v):
    
        c_outer = v.parent.parent
        package_name = c_outer.fqname
        project = c_outer.project

        super(InnerKlass, self).__init__("",package_name,c_outer.path,v.project)

        self.parent = v

    def get_ast(self):
        var_ast = self.parent.ast

        class_ast = None
        if "expr" in var_ast:
            # case: variable declaration
            class_ast = var_ast["expr"]["hiddenClass"]
        elif "right" in var_ast:
            # variable assignment
            class_ast = var_ast["right"]["hiddenClass"]
        else:
            desc = "(%s,%s)" % (var_ast["nodeType"], var_ast["code"] if "code" in var_ast else "???")
            raise ValueError("Unable to extract the hidden class AST in this case: %s" % desc)

        return class_ast


class Project(object):

    DEFAULT_URL = "localhost:9000"

    STATUS_CHOICES = {
        "closed": "closed",
        "opening": "opening",
        "open": "open",
    }

    MAX_ATTEMPTS = 100

    def __init__(self, name, path=None, url=None, api_url=None):
        
        self.name = name
        self.path = path
        self.status = Project.STATUS_CHOICES["closed"]
        self.url = url 

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
     
#        print "open (path=%s)" % self.path
        if self.path:
            # when opening a project with a path, clean any existing
            # project with the same name on the server
            self.clean()
   
        data = {
            "name": self.name,
            "path": self.path,
        }

        self.client.post("/openProject", data)
#        print "set project as opening .."
        self.set_status("opening")

        if sync:
            is_open = False
            attempts = 1

            while self.status == "opening" and attempts < Project.MAX_ATTEMPTS:
                # wait till the project is in a final status (open or closed)
                to_wait = (attempts/2)**2
                log.debug("Wait %s seconds and check the project is open ..." % to_wait)
 
                sleep(to_wait)
#                is_open = self.is_open()   
                self.is_open()
                attempts = attempts + 1
                
#            if not is_open:
#                raise ValueError("The project cannot be opened after %s attempts" % attempts)

            if self.status == "closed":
                raise ValueError("The server cannot open the project after %s attempts" % attempts)

            assert self.status == "open"
                       

    def is_open(self):
    
        if self.is_status("open"):
            return True
        elif self.is_status("closed"):
            return False
        else:
            curr_status = self.client.post("/getStatus", { "name":self.name })
            assert "status" in curr_status, "Wrong status information."

#            print "curr status: %s" % curr_status
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

    @contract(name="None|string", returns="list(dict)")
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


    @contract(type="None|string", returns="list(string)")
    def get_files(self, type=None):

        files = []

        if self.is_open():
            data = { "name": self.name, }
            url = "/getAllFiles"

            if type:    
                # if type is specified, the ws returns a list of 
                # dictionaries,  
                # only take the path information
                data["type"] = type
                url = "/getFilesByType"
                files_dict = self.client.post(url, data)
                files = map(lambda f: f["path"], files_dict)
            else:
                # if no type is specified, the ws returns a list of 
                # strings
                data["skipTest"] = 0

                files = self.client.post(url, data)

        return files

    @contract(path="string", returns="list(dict)")
    def get_file(self, path):
        """
        Returns a list of class definitions contained in the file.
        TODO is it always 1 or can it be longer than 1?
        """    

        file = None

        if not path.startswith("file://"):
            path = "file://" + path

        if not self.is_open():
            raise ValueError("You need to open the project first")

        #data = { "name": self.name, "filePath": path }
        data = { "name": self.name, "path": path }
        file = self.client.post("/getFile", data)

        if file is not None and isinstance(file, dict):
            file = [ file ]

        return file

    @contract(name="string", path="string", returns="dict")
    def get_class(self, name, path):
        files = self.get_file(path)

        found = None

        for curr in files:
            if curr["name"] == name:
                found = curr
                break

        return found

    @contract(returns="list(dict)")
    def get_classes(self):
        """
        In order not to waste memory space limitation, we do not return 
        all the classes ASTs. Instead, we return only a dictionary 
        containing the following keys:
        'className', 'name', 'package'
        With this information is possible to invoke the get_class
        method, for the desired classes.
        """
        classes = []
    
        if self.is_open():
            data = { "name": self.name }
            url = "/getFilesByType"
    
            # all the classes extend java.lang.Object
            data["type"] = "Object"
            classes = self.client.post(url, data)
    
        return classes


    def get_mains(self):
        
        mains = []

        if self.is_open():
            data = { "name": self.name }
            mains = self.client.post("/getMains", data)

        return mains

    @contract(class_fqn="string", class_path="string", method_fqn="string", returns="is_method")
    def get_method(self, class_fqn, class_path, method_fqn):
        class_name = ""
        package_name = ""
    
        fqn_parts = class_fqn.rsplit(".", 1)
    
        if len(fqn_parts) == 1:
            # the fully qualified name contains no package name
            class_name = fqn_parts[0]
        else:
            # the package is part of the fully qualified name
            package_name = fqn_parts[0]
            class_name = fqn_parts[1]

        method_fqn_parts = method_fqn.split(".")
        outer_method_name = None
        outer_variable_name = None
        method_name = None

        klass = Klass(class_name, package_name, "file://%s" % class_path, self)
        m = None

        if len(method_fqn_parts) == 1:
            # receive the name of the method
            method_name = method_fqn_parts[0]
            m = Method(method_name, klass)
        elif len(method_fqn_parts) == 2:
            # receive the name of the actual method within an anonymous class assigned to a class attribute
            attribute_name = method_fqn_parts[0]
            method_name = method_fqn_parts[1]
    
            raise ValueError("Trying to access method '%s' of anonymous class assigned to attribute '%s' of class %s not supported. Please, come back later" % (method_name, attribute_name, class_fqn))
        elif len(method_fqn_parts) == 3:
            # receive the name of the actual method within an inner class inside another method
            outer_method_name = method_fqn_parts[0]
            outer_variable_name = method_fqn_parts[1]
            method_name = method_fqn_parts[2]

            outer_m = Method(outer_method_name, klass)
            outer_v = Variable(outer_variable_name, outer_m)
            inner_c = InnerKlass(outer_v)
            m = Method(method_name, inner_c)
        else:
            raise ValueError("Unexpected format for method_name (%s). Accepted formats: <method_name>|<outer_method_name>.<variable_name>.<method_name>. Passed: %s" % (method_fqn, method_fqn_parts,))
   
        return m

new_contract_check_type("is_project", Project)


class JSONFileClient(RestfulAPIClient):

    def __init__(self, path):
        self.path = path.replace("file://","")

    def post(self, url, *args, **kwargs):    
        """
        Whatever request it receives, it will return the content
        of the file itself.
        """
        data = None

        if url == "/getFile":
            with open(self.path) as json_file:
                data = json.load(json_file)
        elif url == "/getStatus":
            try:
                with open(self.path) as json_file:
                    # I can read, consider the project open
                    data = { "status":"open" }    
            except Exception, e:
                # error reading, consider the project closed
                #print "Error opening file: %s" % e
                data = { "status":"closed" }

        return data


class DummyProject(Project):

    def __init__(self, *args, **kwargs):
        super(DummyProject, self).__init__(*args, **kwargs)

        self.client = JSONFileClient(self.path)

class ASTVisitor(object):

    @contract(node="is_ast_node|dict")
    def __init__(self, node):
        self.handlers = {}

        if isinstance(node, ASTNode):
            node = node.ast

        self.node = node

    def reset_handlers(self):
        self.handlers = {}

    @contract(node_type="string")
    def add_handler(self, node_type, handler):
        existing = self.handlers.get(node_type, [])
        existing.append(handler)

        self.handlers[node_type] = existing
        #print "handlers: %s -> %s" % (node_type, existing)

    @contract(node_type="string", node="dict", return_first="bool", accumulate="bool", returns="list")
    def _handle(self, node_type, node, return_first, accumulate):
        """ 
        Invoke all the handlers of the given node_type. Each handler take the 
        node as argument. 
        Return True if the node has been handled by at least one handler, False 
        otherwise.
        """
        node_handlers = self.handlers.get(node_type, [])
        node_results = []
        
#        print "node handlers (%s): %s" % (node_type, node_handlers)

#        print "check handler: %s" % node_type
        for h in node_handlers:
#            print "found handler: %s -> %s" % (node_type, h)
            curr_res = h(node)
#            print "curr handler result: %s" % curr_res
            if curr_res is not None and (accumulate or return_first):
                node_results.append(curr_res)
                if return_first:
                    break

#        return len(node_handlers) > 0

        # if "return_first" is set, the returned list of results is at most 1 long
        assert return_first == False or len(node_results) <= 1
        return node_results

    def visit(self):
        self._visit_recursively(return_first=False, accumulate=False)

    
    def search(self):
        return self._visit_recursively(return_first=True, accumulate=False)

    @contract(returns="list")
    def filter(self):
        return self._visit_recursively(return_first=False, accumulate=True)

    @contract(return_first="bool", accumulate="bool")
    def _visit_recursively(self, return_first, accumulate):
        """
        This internal method implements the skeleton of a recursive visit. The visit can be used
        for several purposes, e.g.:
        - search : pass return_first=True; it returns the first result returned by a handler that is not None
        - filter : pass accumulate=True; it returns all the results (!= None) of the handlers
        - visit : pass return_first=False, accumulate=False; the handler can set their global variables and do computation with side-effects
        """
        if return_first and accumulate:
            raise ValueError("You cannot set both 'return_first' and 'accumulate'")

        to_visit = deque([ self.node ])

        results = []

        while len(to_visit) > 0:

            # pop from the left side of the queue
            curr_node = to_visit.popleft()

            assert isinstance(curr_node, dict), "%s vs %s" % (curr_node, to_visit)

            node_type = curr_node.get("nodeType", None)

            if node_type is not None:
#                is_handled = self.handle(node_type, curr_node)
                handler_res = self._handle(node_type, curr_node, return_first, accumulate)

#                print "handler result: %s" % handler_res
                if len(handler_res) > 0:
                    # for this first implementation, don't go recursive after handling the node
                    results.extend(handler_res)

                    if return_first:
                        # only the first result is important, stop recursion
                        break
            
            # otherwise, visit sub-nodes recursively
            for key,value in curr_node.iteritems():

                if isinstance(value, list):
                    # extend on the rightside of the queue
#                    print "found list child: %s" % value
                    for curr in value:
                        assert not isinstance(curr, list) # we don't expect nested lists
                
                        if isinstance(curr, dict):
                            # the only way to encode a nested data structure is a dict within a list
                            to_visit.extend(value)
                elif isinstance(value, dict):
                    # append on the right size of the queue
#                    print "found dictionary child: %s" % value
                    to_visit.append(value)

        if return_first and len(results) > 0:
            return results[0]
        elif accumulate:
            return results
        else:
            return None
                
       
new_contract_check_type("is_ast_visitor", ASTVisitor)

