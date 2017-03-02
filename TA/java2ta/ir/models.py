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
            raise ValueError("Expected label in ('%s')" % ",".join(Project.STATUS_CHOICES.keys()))

        self.status = Project.STATUS_CHOICES[label]


    def is_status(self, label):
        """
        Return True iff the current status corresponds to the passed label
        """
    
        if label not in Project.STATUS_CHOICES:
            raise ValueError("Expected label in ('%s')" % ",".join(Project.STATUS_CHOICES.keys()))

        return self.status == Project.STATUS_CHOICES[label]


    def open_project(self):
        
        data = {
            "name": self.name,
            "path": self.path,
        }

        self.client.post("/openProject", **data)
        self.set_status("opening")
    
        return self.status == Project.STATUS_CHOICES["open"]

    def is_open(self):
    
        if self.get_status("open"):
            return True
        elif self.get_status("close"):
            return False
        else:
            curr_status = self.client.post("/isProjectOpen")

            if curr_status == "1":
                self.set_status("open")
            else:
                self.set_status("opening")


    def get_threads(self):
    
        return []

    def get_files(self, type=None):

        return []

    def get_file(self, path):

        return None
