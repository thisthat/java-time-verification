from java2ta.engine.context import Context
from java2ta.ta.models import TA

from java2ta.translator.shortcuts import translate_method_to_ta

from java2ta.abstraction.models import StateSpace, AbstractAttribute, LT, Eq, GT, CompareVariables, Integer
from java2ta.abstraction.shortcuts import INTEGERS
from java2ta.ir.models import Project, Thread, Klass, Method, Variable, InnerKlass
from java2ta.ir.tests.test_models import check_is_open

import pkg_resources

def test_translate_statements():

    # get a reference to a thread
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    klass = Klass("HelloWorld", "", "file://HelloWorld.java", p)

    m = Method("test", klass)

    ss = StateSpace()
    ss.add_attribute(AbstractAttribute(["x"], INTEGERS, False))
    ss.add_attribute(AbstractAttribute(["y"], INTEGERS, False))
#    ss.add_attribute(AbstractAttribute(["z"], INTEGERS, False))
#    ss.add_attribute(AbstractAttribute(["w"], INTEGERS, False))

    ta = translate_method_to_ta(m, ss)

    assert isinstance(ta, TA)

    assert 28 == len(ta.locations), len(ta.locations)
    assert 99 == len(ta.edges), len(ta.edges)
    assert 0 == len(ta.variables), len(ta.variables)
    assert 0 == len(ta.clock_variables), len(ta.clock_variables)
    

def test_translate_while():

    # get a reference to a thread
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    klass = Klass("HelloWorld", "", "file://HelloWorld.java", p)

    m = Method("time_method", klass)

    ss = StateSpace()
    ss.add_attribute(AbstractAttribute(["x"], INTEGERS, False))
    ss.add_attribute(AbstractAttribute(["a"], INTEGERS, False))
    ss.add_attribute(AbstractAttribute(["b","c"], domain=CompareVariables(datatypes=[Integer(),Integer()], predicates=[LT("b","c"),Eq("b","c"), GT("b","c")]), is_local=False))

    ta = translate_method_to_ta(m, ss)

    assert isinstance(ta, TA)

    assert 142 == len(ta.locations), len(ta.locations)
    assert 432 == len(ta.edges), len(ta.edges)
    assert 0 == len(ta.variables), len(ta.variables)
    assert 0 == len(ta.clock_variables), len(ta.clock_variables)

    
def test_inner_method_with_statements():

    # get a reference to a thread
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    klass = Klass("HelloWorld", "", "file://HelloWorld.java", p)

    m = Method("fie", klass)

    # maxseq is a local variable of the method "doSwap"
    v = Variable("varfoo", m)

    c_inner = InnerKlass(v)
    m_inner = Method("mymethod", c_inner)

    ss = StateSpace()
    ss.add_attribute(AbstractAttribute(["i"], INTEGERS, False))

    ta = translate_method_to_ta(m_inner, ss)

    assert isinstance(ta, TA)

    assert 7 == len(ta.locations), len(ta.locations)
    assert 6 == len(ta.edges), len(ta.edges)
    assert 0 == len(ta.variables), len(ta.variables)
    assert 0 == len(ta.clock_variables), len(ta.clock_variables)
    

