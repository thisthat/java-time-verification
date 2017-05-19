from time import sleep

from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.translator.rules import ExtractMethodStateSpace, AddStates
from java2ta.ir.models import Project, Method, Klass
from java2ta.ta.models import TA
from java2ta.abstraction.domains import Domain

"""
This file collects functions that encode the most common scenarios for our
translation approach.
"""

"""
Scenario 1.

INPUT
- project name
- project path
- class name
- method name
- a dictionary mapping variables to domains

OUTPUT
- a timed automaton
"""
def translate_method_to_automaton(project, class_fqn, class_path, method_name, domains):

    assert isinstance(project, Project)
    assert isinstance(class_fqn, basestring)
    assert isinstance(method_name, basestring)
    assert isinstance(domains, dict)

    # check this works (case 1: no dot, case 2: one or more dots)
    package_name, class_name = class_fqn.split(".")

    klass = Klass(class_name, package_name, "file://%s" % class_path, project)
    m = Method(method_name, klass)

    r1 = ExtractMethodStateSpace()
    r2 = AddStates()
    # TODO add rules for adding edges b/w states by static analysis (SMT based)

    e = Engine()
    e.add_rule(r1)
    e.add_rule(r2)

    ctx = Context()
    ctx.push({})
    ctx.update("abs_domains", domains)
    ta = TA(klass.name)
    (ta_post, ctx_post) = e.run(m, ta, ctx)

    return ta_post
