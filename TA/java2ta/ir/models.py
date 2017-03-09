from java2ta.ir.client import RestfulAPIClient

class Project(object):

    DEFAULT_URL = "localhost:9000"

    STATUS_CHOICES = {
        "closed": "closed",
        "opening": "opening",
        "open": "open",
    }


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


    def open(self):
        
        data = {
            "name": self.name,
            "path": self.path,
        }

        self.client.post("/openProject", data)
        self.set_status("opening")

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

   


    def get_threads(self):
    
        threads = []
    
        if self.is_open():
    
            data = { "name": self.name }
            threads = self.client.post("/getThreads", data)

        return threads

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

    def get_mains(self):
        
        mains = []

        if self.is_open():
            data = { "name": self.name }
            mains = self.client.post("/getMains", data)

        return mains


class Thread(object):

    def __init__(self, ir, project):
        assert isinstance(ir, dict)
        assert isinstance(project, Project)

        assert project.is_open(), "Expected open project. Got project in status: '%s'" % project.status

        self.name = ir["className"]  
        self.path = ir["path"]
        self.package = ir["packageName"]
        self._project = project
        self._imported = False
        self._klass = None

    @property
    def klass(self):
    
        if not self._imported:
            classes = self._project.get_file(self.path)
            
            for curr in classes:
                if curr["name"] == self.name:
                    self._klass = curr
                    self._imported = True

            if not self._klass:
                raise Exception("It was not possible to import class '%s' from file '%s'" % (self.name, self.path))

        assert self._klass is not None
        return self._klass
    

