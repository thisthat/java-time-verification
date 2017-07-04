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

@contract(state_space=StateSpace)
def build_legend(state_space):

    legend = []

    for (idx,attr) in enumerate(state_space.attributes):
        check("is_abstract_attribute", attr)
        #variables = ",".join(attr.variables)
        pred_labels = []
        for pred in attr.values:    
            check("is_predicate", pred)
            ctx={ p_var.strip("{}"):a_var for (p_var,a_var) in zip(pred.var_names,attr.variables) }
            print "Legend: %s -> Ctx: %s" % (pred, ctx)
            pred_labels.append(pred.label(**ctx))

        desc = ",".join(pred_labels)

        legend.append((str(idx),desc))

    return legend


proj_path =  os.path.abspath(pkg_resources.resource_filename("java2ta.translator.tests", "test_project"))

proj_url = "file://%s" % proj_path

p = Project("test_project", proj_url, "localhost:9000")
p.open()

#print "Wait 2 seconds for opening project ..."
#sleep(2)

i_lt_j = LT().rename(var="var_1",value="var_2")
i_eq_j = Eq().rename(var="var_1",value="var_2")
i_gt_j = GT().rename(var="var_1",value="var_2")

# each tuple in the domain has the following structure:
# (variable names, domain, is_local)
domains = [
#    "i": INTEGERS,
#    "j": INTEGERS,
#    (["i","j"], DomainProduct(var_1=INTEGERS, var_2=INTEGERS)),
    (["i","j"], CompareVariables(datatypes=[Integer(), Integer()], predicates=[i_eq_j, i_lt_j, i_gt_j]), True),
    ("initial_i", INTEGERS, False),
    ("initial_j", INTEGERS, False),
]

m = get_method(p, "While", "While.java", "foo")

ss = get_state_space_from_method(m, domains)

fsa = translate_method_to_fsa(m, ss)

legend = build_legend(ss)

#(m, fsa) = translate_method_to_fsa(p, "While", "While.java", "foo", domains)
#(m, fsa) = translate_method_to_fsa(p, "SequentialCode", "SequentialCode.java", "foo", domains)
#(m, fsa) = translate_method_to_fsa(p, "Short", "Short.java", "foo", domains)

print "FSA: %d locations, %d edges" % (len(fsa.locations), len(fsa.edges))

gv = GraphViz(fsa)
with open("testfoo.gv", "w+") as out_f:
    out_f.write(gv.render(legend))

(graph,) = pydot.graph_from_dot_file('testfoo.gv')
graph.write_pdf('testfoo.pdf')
graph.write_svg('testfoo.svg')


subprocess.call(["xdg-open", "testfoo.svg"])
