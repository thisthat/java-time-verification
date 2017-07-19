import pkg_resources
import os
import subprocess
import pydot
import logging
import logging.config

from contracts import contract

from java2ta.ir.models import *
from java2ta.translator.shortcuts import *
from java2ta.abstraction.shortcuts import *
from java2ta.abstraction.models import *
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

log = logging.getLogger("main")

@contract(state_space="is_state_space")
def build_legend(state_space):

    legend = []

    log.debug("Build legend. Attributes: %s" % state_space.attributes)

    for (idx,attr) in enumerate(state_space.attributes):
        check("is_abstract_attribute", attr)
        #variables = ",".join(attr.variables)
        pred_labels = []
        for idx_attr,pred in enumerate(attr.values):    
            check("is_predicate", pred)
            ctx = {}
            for p_var,a_var in zip(pred.var_names, attr.variables):
                key = p_var.strip("{}")
                ctx[key] = a_var

            pred_labels.append("%s : { %s }" % (idx_attr, pred.label(**ctx)))

        desc = ", ".join(pred_labels)

        log.debug("Legend: (%s,%s) : %s" % (idx,attr,desc)) 
        legend.append((str(idx),desc))

    return legend


proj_name = "dist-progs" # test_project
proj_path =  os.path.abspath(pkg_resources.resource_filename("java2ta.ir.tests", proj_name))
#proj_path =  os.path.abspath(pkg_resources.resource_filename("java2ta.translator.tests", proj_name))
class_name = "RingLeader" # DoWhile, While, Short, SequentialCode
file_name = "RingLeader.java" # DoWhile.java, While.java, Short.java, SequentialCode.java
method_name = "handleMsg"

i_lt_j = LT().rename(var="var_1",value="var_2")
i_gt_j = GT().rename(var="var_1",value="var_2")
i_eq_j = Eq().rename(var="var_1",value="var_2")
i_neq_j = NotEq().rename(var="var_1",value="var_2")

# each tuple in the domain has the following structure:
# (variable names, domain, is_local)
domains = [
#    "i": INTEGERS,
#    "j": INTEGERS,
#    (["i","j"], DomainProduct(var_1=INTEGERS, var_2=INTEGERS)),
#    (["i","j"], CompareVariables(datatypes=[Integer(), Integer()], predicates=[i_eq_j, i_lt_j, i_gt_j]), True),
#    ("initial_i", INTEGERS, False),
#    ("initial_j", INTEGERS, False),
#    ("number", INTEGERS, False),
    ("leaderId", INTEGERS, False),
    ("next", INTEGERS, False),
    ("awake", BOOLEANS, False),
    ("src", INTEGERS, False),
    ("tag", STRINGS, False),
#    ("j", INTEGERS, True),
    (["j","number"], CompareVariables(datatypes=[Integer(), Integer()], predicates=[i_eq_j, i_neq_j]), False),
]


# start code

p = Project(proj_name, "file://%s" % proj_path, "localhost:9000")

p.open()
m = get_method(p, class_name, file_name, method_name)

ss = get_state_space_from_method(m, domains)

fsa = translate_method_to_fsa(m, ss)

legend = build_legend(ss)

log.debug("Final legend: %s" % legend)

print "FSA: %d locations, %d edges" % (len(fsa.locations), len(fsa.edges))

gv = GraphViz(fsa)
with open("testfoo.gv", "w+") as out_f:
    out_f.write(gv.render(legend))

(graph,) = pydot.graph_from_dot_file('testfoo.gv')
graph.write_pdf('testfoo.pdf')
graph.write_svg('testfoo.svg')


subprocess.call(["xdg-open", "testfoo.svg"])
