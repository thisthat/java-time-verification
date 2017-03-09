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
    
    assert ta_post.locations == ta.locations
    assert ta_post.edges == ta.edges

    assert ctx_post.get("state_space") is not None
    assert isinstance(ctx_post.get("state_space"), StateSpace)

    assert len(ctx.get("state_space").enumerate) == 4
