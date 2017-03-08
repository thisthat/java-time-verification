from java2ta.ir.client import RestfulAPIClient

class Project(object):

    DEFAULT_URL = "localhost:9000"

    STATUS_CHOICES = {
        "closed": "closed",
        "opening": "opening",
        "open": "open",
    }


    def __init__(self, name, path, api_url=None):
        
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
            data = { "name": self.name, "skipTest": 0 }
            url = "/getAllFiles"

            if type:    
                data["type"] = type
                url = "/getFilesByType"
                
            files = self.client.post(url, data)

        return files

    def get_file(self, path):

        file = None

        if not path.startswith("file://"):
            path = "file://" + path

        if self.is_open():
            data = { "name": self.name, "filePath": path }
            file = self.client.post("/getFile", data)

        return file

    def get_mains(self):
        
        mains = []

        if self.is_open():
            data = { "name": self.name }
            mains = self.client.post("/getMains", data)

        return mains
