import pkg_resources
import os
import subprocess
import pydot
import logging
import logging.config

from java2ta.ir.models import *
from java2ta.translator.shortcuts import *
from java2ta.abstraction.domains import *
from java2ta.ta.views import GraphViz

LOG_CONFIG = {
    "version": 1,
    "disable_existing_loggers": 0,
    "root": {
        "level": "DEBUG",
        "handlers": [
            "console",
            "file",
            "debugfile"
        ]
    },
    "loggers": {
        
    },
    "formatters": {
        "precise": {
            "format": "%(asctime)s:%(name)s:%(levelname)s:%(message)s"
        },
        "brief": {
            "format": "%(levelname)s:%(name)s:%(message)s"
        }
    },
    "handlers": {
        "debugfile": {
            "class": "logging.FileHandler",
            "formatter": "brief",
            "mode": "a",
            "level": "DEBUG",
            "filename": "testfoo.log"
        },
        "console": {
            "formatter": "precise",
            "class": "logging.StreamHandler",
            "stream": "ext://sys.stdout",
            "level": "WARNING"
        },
        "file": {
            "formatter": "precise",
            "backupCount": 3,
            "level": "WARNING",
            "maxBytes": 10240000,
            "class": "logging.handlers.RotatingFileHandler",
            "filename": "testfoo.log"
        }
    }
}

logging.config.dictConfig(LOG_CONFIG)

proj_path =  os.path.abspath(pkg_resources.resource_filename("java2ta.translator.tests", "test_project"))

proj_url = "file://%s" % proj_path

p = Project("test_project", proj_url, "localhost:9000")
p.open()

print "Wait 2 seconds for opening project ..."
sleep(2)

domains = {
#    "i": INTEGERS,
#    "j": INTEGERS,
    "i_j": CompareNumbers(["i","j"], Integer()),
    "initial_i": INTEGERS,
    "initial_j": INTEGERS,
}

(m, fsa) = translate_method_to_fsa(p, "While", "While.java", "foo", domains)
#(m, fsa) = translate_method_to_fsa(p, "SequentialCode", "SequentialCode.java", "foo", domains)
#(m, fsa) = translate_method_to_fsa(p, "Short", "Short.java", "foo", domains)

print "FSA: %d locations, %d edges" % (len(fsa.locations), len(fsa.edges))

gv = GraphViz(fsa)
with open("testfoo.gv", "w+") as out_f:
    out_f.write(gv.render(domains))

(graph,) = pydot.graph_from_dot_file('testfoo.gv')
graph.write_pdf('testfoo.pdf')
graph.write_svg('testfoo.svg')


subprocess.call(["xdg-open", "testfoo.svg"])
