#!/usr/bin/env python
import pkg_resources
import os
import subprocess
import logging
import logging.config

from contracts import contract

from java2ta.ir.models import *
from java2ta.translator.shortcuts import *
from java2ta.translator.models import KnowledgeBase
from java2ta.abstraction.shortcuts import *
from java2ta.abstraction.models import *
from java2ta.ta.views import GraphViz, Uppaal

CURRFILE=os.path.basename(__file__)

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
        "client": { "level":"WARNING",},
        "requests.packages.urllib3.connectionpool": { "level":"WARNING"},     
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
            "filename": CURRFILE + ".log",
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
            "filename": CURRFILE + ".log",
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


# populate the knowledge base. the knowledge base is made of "abstraction operations" that compare value of 
# several variables among themselves, or against some constant value). the knowledge base also contains the
# "abstract interpretation" of standard java methods, for which we don't have the source code (e.g. the method
# "equals", "contains" and "println" of class java.lang.String)

# this "knowledge" of the String.equals method works only when composing an object of type String with a constant
# string (a literal)
string_equals = ([ "(assert (= {res} (= (value {lhs}) {par_0})))" ], "{res}", Boolean())
string_contains = ([ "(declare_const tmp Int)" , "(assert (= {res} (= (value {lhs}) (* (value {par_0}) tmp))))" ], "{res}", Boolean())
KnowledgeBase.add_method("java.lang.String", "equals", string_equals)
KnowledgeBase.add_method("java.lang.String", "contains", string_contains)
KnowledgeBase.add_method("java.io.PrintStream", "println", ([], "(assert (= 1 1)", Boolean()))
KnowledgeBase.add_method("java.lang.Thread", "sleep", ([], "(assert (= 1 1))", Boolean()))
KnowledgeBase.add_method("-", "send_icmp_request", ([], "(assert (= 1 1))", Boolean()))
KnowledgeBase.add_method("-", "receive_icmp_reply", ([], "(assert (= 1 1))", Boolean()))
KnowledgeBase.add_method("-", "setAlive", ([], "(assert (= 1 1))", Boolean()))


# start to analyse the code

i_lt_j = LT().rename(var="var_1",value="var_2")
i_gt_j = GT().rename(var="var_1",value="var_2")
i_eq_j = Eq().rename(var="var_1",value="var_2")
i_neq_j = NotEq().rename(var="var_1",value="var_2")

#i_streq_j = Eq().rename(var="var_1",value="var_2").wrap_fields(var_1="value",var_2="value")
#i_strneq_j = NotEq().rename(var="var_1",value="var_2").wrap_fields(var_1="value", var_2="value")
tag_values = split_enum([ literal_to_smt("'leader'"), literal_to_smt("'election'") ], case_else=True)


### here the user inputs begin. first we ask for the "coordinates" of the method to be analyzed

proj_name = "test_project" # "dist-progs" # test_project 
#proj_path =  os.path.abspath(pkg_resources.resource_filename("java2ta.ir.tests", proj_name))
proj_path =  "example_neighbors.json" #os.path.abspath(pkg_resources.resource_filename("java2ta.translator.tests", proj_name))
#class_name = "example.TestNeighbors" #"RingLeader" # DoWhile, While, Short, SequentialCode
file_name = "TestNeighbors.java"# "RingLeader.java" # DoWhile.java, While.java, Short.java, SequentialCode.java
#method_name = "is_alive" #"handleMsg"


# next we ask for a "list" of abstract predicates, that compose an "abstract domain". An abstract predicates P
# has this form:
#
# P : var1 ... varN -> Bool
#
# and is used to build the abstract state. Given predicates P_1 ... P_M the abstract state can be seen as
# a boolean configuration of them, e.g.: P_1 and not P_2 and ... and not P_M. Given M predicates, there exist
# 2^M abstract states.

method_names = [
#    "example.TestNeighbors.is_alive",
    "example.TestNeighbors.run",
]

# each tuple in the domain has the following structure:
# (variable names, domain, is_local)
domains = [
    ("address", Dummy(Integer()), False),
    ("timeout", Dummy(Integer()), False),
    ("res",BOOLEANS,True), # local variable for storing returned result
    ("exception_inv", BOOLEANS, True), # local variable for detecting excetion raised
    ("isAlive", BOOLEANS, True),
]

# here the tool "fetch" the specified method, and buils a finite state automaton (TA) using the passed list
# of predicates. here the tool does not need any further input.
# start code

p = DummyProject(proj_name, "file://%s" % proj_path, "localhost:9000")

p.open()

method_kb = {}

for curr in method_names:
    parts = curr.split(".")
    method_name = parts[-1] # the method name is the last component
    class_name = ".".join(parts[:-1]) # the FQN for the class is all the previous one
    log.debug("%s (%s) -> %s + %s" % (curr, parts, class_name, method_name))

    file_name = "%s.Java" % class_name # HACK TODO change this!

    m = p.get_method(class_name, file_name, method_name)

    ss = get_state_space_from_method(m, domains)

    ta = translate_method_to_ta(m, ss)
    
    method_kb[curr] = ta

    legend = build_legend(ss)

    log.debug("Method legend (%s): %s" % (curr, legend))

    print "TA for method %s: %d locations, %d edges" % (curr, len(ta.locations), len(ta.edges))

# here we pass the TA to a renderer (GraphViz) which produces a graphical representation of it. Some input
# may be required to the user (e.g. the type and name of output file)

    gv = GraphViz(ta, legend)
    gv.save(curr + ".gv")

#    log.debug("TA variables: %s" % map(lambda v: str(v), ta.variables))

#    log.debug("TA clock variables: %s" % map(lambda v: str(v), ta.clock_variables))

    uppaal = Uppaal(ta, legend)
    uppaal.save(curr + ".xml")

