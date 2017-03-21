from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.ta.models import TA
from java2ta.translator.rules import ExtractClassStateSpace, ExtractMethodStateSpace, AddStates
from java2ta.abstraction import StateSpace
from java2ta.abstraction.domains import *
from java2ta.ir.models import Project, Thread, Klass, Method
from java2ta.ir.tests.test_models import check_is_open

import pkg_resources

def test_extract_class_state_space():

    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    thread = Thread("Producer", "", p)


    r1 = ExtractClassStateSpace()
    ta = TA(thread.name)
    e = Engine()
    e.add_rule(r1)
    
    ctx = Context()
    ctx.push({})
    domains = {
        "b": BOUNDED_COLLECTION,
    }
    ctx.update("abs_domains", domains)
    
    (ta_post, ctx_post) = e.run(thread, ta, ctx)

    assert e.num_applications == 1
    
    assert len(ta_post.locations) == 0
    assert len(ta_post.edges) == 0

    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    assert len(ctx.get("state_space").enumerate) == BOUNDED_COLLECTION.size


def test_add_states_from_class_state_space():

    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    thread = Thread("Producer", "", p)

    r1 = ExtractClassStateSpace()
    r2 = AddStates()
    ta = TA(thread.name)
    e = Engine()
    e.add_rule(r1)
    e.add_rule(r2)
    
    ctx = Context()
    ctx.push({})
    domains = {
        "b": BOUNDED_COLLECTION,
    }
    ctx.update("abs_domains", domains)
    
    (ta_post, ctx_post) = e.run(thread, ta, ctx)

    assert e.num_applications == 2, e.num_applications
    
    assert len(ta_post.locations) == BOUNDED_COLLECTION.size
    assert len(ta_post.edges) == 0


def test_extract_method_state_space():

    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    klass = Klass("SemaphoreLock", "", "file://SemaphoreLock.java", p)

    m = Method("requestCS", klass)

    r1 = ExtractMethodStateSpace()
    ta = TA(klass.name)
    e = Engine()
    e.add_rule(r1)
    
    ctx = Context()
    ctx.push({})
    domains = {
        "mutex": BOOLEANS,
        "i": INTEGERS,
    }
    ctx.update("abs_domains", domains)
    
    (ta_post, ctx_post) = e.run(m, ta, ctx)

    assert e.num_applications == 1
    
    assert len(ta_post.locations) == 0
    assert len(ta_post.edges) == 0

    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    assert ctx.get("state_space").size == domains["mutex"].size * domains["i"].size


