import json
import os

class Parser(object):

    def parse(self, filename):

        if not os.path.exists(filename):
            raise IOError("The passed filename '%s' does not exist" % filename)

        with open(filename, "r") as in_f:
            obj = json.load(in_f)

        return obj
