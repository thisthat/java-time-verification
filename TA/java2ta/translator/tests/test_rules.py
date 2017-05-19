from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.ta.models import TA

from java2ta.translator.rules import ExtractClassStateSpace, ExtractMethodStateSpace, AddStates
from java2ta.translator.shortcuts import translate_method_to_automaton

from java2ta.abstraction import StateSpace
from java2ta.abstraction.domains import *
from java2ta.ir.models import Project, Thread, Klass, Method
from java2ta.ir.tests.test_models import check_is_open

import pkg_resources

def test_extract_class_state_space():

    # get a reference to a thread
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    thread = Thread("Producer", "", p)

    # extract the state-space of the thread class; the only rule in the
    # engine is the one creating the state-space
    r1 = ExtractClassStateSpace()
    ta = TA(thread.name)
    e = Engine()
    e.add_rule(r1)
    
    # assume attribute bc is a bounded collection of size 1000; this
    # is the only component of the abstract domain
    bc = BoundedCollection(1000)

    ctx = Context()
    ctx.push({})
    domains = {
        "b": bc,
    }
    ctx.update("abs_domains", domains)
    
    (ta_post, ctx_post) = e.run(thread, ta, ctx)

    assert e.num_applications == 1
    
    assert len(ta_post.locations) == 0
    assert len(ta_post.edges) == 0

    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    # check the state-space has 3 states (due to the abstraction of the single var bc)
    assert bc.size == 3
    assert len(ctx.get("state_space").enumerate) == bc.size


def test_add_states_from_class_state_space():

    # this state is an evolution of test_extract_class_state_space; we add to the
    # engine the rule that creates an automaton with a state for each state in the
    # state-space
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()
    
    check_is_open(p)

    thread = Thread("Producer", "", p)

    # notice that here we add one rule in addition to those of test_extract_class_state_space
    r1 = ExtractClassStateSpace()
    r2 = AddStates()
    ta = TA(thread.name)
    e = Engine()
    e.add_rule(r1)
    e.add_rule(r2)
    
    # we still abstract a single variable
    bc = BoundedCollection(1000)

    ctx = Context()
    ctx.push({})
    domains = {
        "b": bc, 
    }
    ctx.update("abs_domains", domains)
    
    (ta_post, ctx_post) = e.run(thread, ta, ctx)

    assert e.num_applications == 2, e.num_applications
 
    # check the final automaton has 3 states    
    assert bc.size == 3
    assert len(ta_post.locations) == bc.size
    assert len(ta_post.edges) == 0


def test_extract_method_state_space_simple():

    # this tests checks how to create an automaton from a method; the environment of the
    # method is given by the variables in the method itself, the passed parameters, the
    # instance attributes, and the class attributes
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    klass = Klass("SemaphoreLock", "", "file://SemaphoreLock.java", p)

    m = Method("requestCS", klass)

    r1 = ExtractMethodStateSpace()
    e = Engine()
    e.add_rule(r1)
    
    # the environment of the method requestCS contains an instance attribute
    # (mutex) and a passed parameter (i)
    ctx = Context()
    ctx.push({})
    domains = {
        "mutex": BOOLEANS,
        "i": INTEGERS,
    }
    ctx.update("abs_domains", domains)
 
    ta = TA(klass.name)
    (ta_post, ctx_post) = e.run(m, ta, ctx)

    assert e.num_applications == 1
    
    assert len(ta_post.locations) == 0
    assert len(ta_post.edges) == 0

    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    # check the final automaton has 6 (= 3 * 2) states
    assert domains["mutex"].size == 2
    assert domains["i"].size == 3
    assert ctx.get("state_space").size == domains["mutex"].size * domains["i"].size


def test_extract_method_space_complex():

    # this test extends test_extract_method_space_complex, by analysing a more complex
    # method, with local variables, some declared in the inner AST nodes

    # this tests checks how to create an automaton from a method; the environment of the
    # method is given by the variables in the method itself, the passed parameters, the
    # instance attributes, and the class attributes
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    klass = Klass("ConcQueue", "", "file://ConcQueue.java", p)

    m = Method("Dequeue", klass)

    r1 = ExtractMethodStateSpace()
    r2 = AddStates()

    e = Engine()
    e.add_rule(r1)
    e.add_rule(r2)
       
    # the environment of the method Dequeue contains two instance attributes
    # (head and tail) and four local variables (ltail, lhead, lnext, return_val), the latter
    # declared in a branch of a nested if-then-else; since the environment is complex
    # and involve several user defined types, we use a DataTypeFactory

    factory = DataTypeFactory.the_factory()
    dt_pointer = factory.from_class("Pointer", {"ptr":"java.lang.String", "count": "int" })
    dom_pointer = Domain(dt_pointer, split_field_domain("count", split_numeric_domain([0,], lt_min=False)))
    ctx = Context()
    ctx.push({})
    domains = {
        "head": dom_pointer,
        "tail": dom_pointer,
        "ltail": dom_pointer,
        "lhead": dom_pointer,
        "lnext": dom_pointer,
        "return_val": STRINGS,
    }
    ctx.update("abs_domains", domains)
 
    ta = TA(klass.name)
    (ta_post, ctx_post) = e.run(m, ta, ctx)

    assert e.num_applications == 2
    
    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    # check the final automaton has 6 (= 3 * 2) states
    assert domains["head"].size == 2
    assert domains["tail"].size == 2
    assert domains["lhead"].size == 2
    assert domains["ltail"].size == 2
    assert domains["lnext"].size == 2
    assert domains["return_val"].size == 2

    assert len(ta_post.locations) == (dom_pointer.size ** 5) * STRINGS.size, "%s vs %s" % (len(ta_post.locations), (dom_pointer.size ** 5) * STRINGS.size)
    assert len(ta_post.edges) == 0



def test_extract_method_space_shortcut():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    # create the domains dictionary

    factory = DataTypeFactory.the_factory()
    dt_pointer = factory.from_class("Pointer", {"ptr":"java.lang.String", "count": "int" })
    dom_pointer = Domain(dt_pointer, split_field_domain("count", split_numeric_domain([0,], lt_min=False)))

    domains = {
        "head": dom_pointer,
        "tail": dom_pointer,
        "ltail": dom_pointer,
        "lhead": dom_pointer,
        "lnext": dom_pointer,
        "return_val": STRINGS,
    }

    # get the automaton

    ta = translate_method_to_automaton(p, "ConcQueue", "ConcQueue.java", "Dequeue", domains)

    assert len(ta.locations) == (dom_pointer.size ** 5) * STRINGS.size, "%s vs %s" % (len(ta.locations), (dom_pointer.size ** 5) * STRINGS.size)
    assert len(ta.edges) == 0

