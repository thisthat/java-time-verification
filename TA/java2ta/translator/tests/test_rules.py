from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.ta.models import TA
from java2ta.translator.rules import ExtractStateSpace, AddStates
from java2ta.abstraction import StateSpace
from java2ta.ir.models import Project, Thread
from java2ta.ir.tests.test_models import check_is_open

import pkg_resources

def test_extract_state_space():


    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    thread_desc = p.get_thread("Producer")

    thread = Thread(thread_desc, p)


    r1 = ExtractStateSpace()
    ta = TA(thread.name)
    e = Engine()
    e.add_rule(r1)
    
    ctx = Context()
    ctx.push({})
    predicates = {
        "b": [ "null", "empty", "some_elements", "full", ]
    }
    ctx.update("abs_predicates", predicates)
    
    (ta_post, ctx_post) = e.run(thread, ta, ctx)

    assert e.num_applications == 1
    
    assert len(ta_post.locations) == 0
    assert len(ta_post.edges) == 0

    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    assert len(ctx.get("state_space").enumerate) == 4


def test_add_states():


    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    thread_desc = p.get_thread("Producer")

    thread = Thread(thread_desc, p)


    r1 = ExtractStateSpace()
    r2 = AddStates()
    ta = TA(thread.name)
    e = Engine()
    e.add_rule(r1)
    e.add_rule(r2)
    
    ctx = Context()
    ctx.push({})
    predicates = {
        "b": [ "null", "empty", "some_elements", "full", ]
    }
    ctx.update("abs_predicates", predicates)
    
    (ta_post, ctx_post) = e.run(thread, ta, ctx)

    assert e.num_applications == 2, e.num_applications
    
    assert len(ta_post.locations) == 4
    assert len(ta_post.edges) == 0


