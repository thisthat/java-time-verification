from nose.tools import *
from java2ta.engine.context import Context

def setup():
    pass

def teardown():
    pass

def test_pop_empty():

    ctx = Context()

    try:
        ctx.pop()
        assert False, "It should raise an exception when popping an empty context"
    except Context.EmptyContext:
        # this is expected
        pass

def test_push_pop():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    pop1 = ctx.pop()
    pop2 = ctx.pop()

    assert pop1 == env2
    assert pop2 == env1

def test_get_simple():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    assert ctx.get("a") == 1
    assert ctx.get("b") == 2
    assert ctx.get("c") == 3
    assert ctx.get("d") == 4


def test_get_name_override():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "a":3, "c": 3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    assert ctx.get("a") == 3
    assert ctx.get("b") == 2
    assert ctx.get("c") == 3
    assert ctx.get("d") == 4


def test_get_unknown():
 
    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "a":3, "c": 3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    try:
        ctx.get("x")
        assert False, "The name 'x' should not be present in the context"
    except Context.ObjectDoesNotExist:
        # this is expected
        pass

  
