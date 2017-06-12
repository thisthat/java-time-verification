import pkg_resources
import os
import subprocess
import pydot

from java2ta.ir.models import *
from java2ta.translator.shortcuts import *
from java2ta.abstraction.domains import *
from java2ta.ta.views import GraphViz

proj_path =  os.path.abspath(pkg_resources.resource_filename("java2ta.translator.tests", "test_project"))

proj_url = "file://%s" % proj_path

p = Project("test_project", proj_url, "localhost:9000")
p.open()

print "Wait 2 seconds for opening project ..."
sleep(2)

domains = {
    "i": INTEGERS,
    "j": INTEGERS,
    "initial_i": INTEGERS,
    "initial_j": INTEGERS,
}

(m, fsa) = translate_method_to_fsa(p, "SequentialCode", "SequentialCode.java", "foo", domains)
#(m, fsa) = translate_method_to_fsa(p, "Short", "Short.java", "foo", domains)


print "FSA: %d locations, %d edges" % (len(fsa.locations), len(fsa.edges))

gv = GraphViz(fsa)
with open("testfoo.gv", "w+") as out_f:
    out_f.write(gv.render())

(graph,) = pydot.graph_from_dot_file('testfoo.gv')
graph.write_pdf('testfoo.pdf')

subprocess.call(["xdg-open", "testfoo.pdf"])
