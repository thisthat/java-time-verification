from nose.tools import *

from java2ta.engine.rules import RuleSet, Rule, Engine
from java2ta.engine.context import Context
from java2ta.engine.exceptions import EmptyContext, ObjectDoesNotExist
from java2ta.commons.timeout import timeout, TimeoutError

class FakeRuleNoMatch(Rule):

    def match(self):
        return False


class FakeRuleMatchNoWhere(Rule):

    def match(self):
        return True

    def where(self):
        return False


class FakeRuleMatch(Rule):

    def match(self):
        return True


class FakeRuleMatchAndWhere(Rule):

    def match(self):
        return True

    def where(self):
        return True



def setup():
    pass

def teardown():
    pass

def test_context_pop_empty():

    ctx = Context()

    try:
        ctx.pop()
        assert False, "It should raise an exception when popping an empty context"
    except EmptyContext:
        # this is expected
        pass

def test_context_push_pop():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    pop1 = ctx.pop()
    pop2 = ctx.pop()

    assert pop1 == env2
    assert pop2 == env1

def test_context_get_simple():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    assert ctx.get("a") == 1
    assert ctx.get("b") == 2
    assert ctx.get("c") == 3
    assert ctx.get("d") == 4


def test_context_get_name_override():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "a":3, "c": 3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    assert ctx.get("a") == 3
    assert ctx.get("b") == 2
    assert ctx.get("c") == 3
    assert ctx.get("d") == 4


def test_context_get_unknown():
 
    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "a":3, "c": 3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    try:
        ctx.get("x")
        assert False, "The name 'x' should not be present in the context"
    except ObjectDoesNotExist:
        # this is expected
        pass

 
def test_ruleset_pick_simple():

    rs = RuleSet()
    
    rule1 = Rule()
    rule2 = Rule()

    rs.add_rule(rule1)
    rs.add_rule(rule2)

    pick1 = rs.pick()

    assert pick1 == rule1 or pick1 == rule2


def test_ruleset_pick_multiple_times():
 
    rs = RuleSet()
    
    rule1 = Rule()
    rule2 = Rule()

    rs.add_rule(rule1)
    rs.add_rule(rule2)

    pick1 = rs.pick()
    pick2 = rs.pick()
    pick3 = rs.pick()

    # by picking 3 times in a set with 2 rules, there must be
    # some rule picked more than once

    assert pick1 == pick2 or pick2 == pick3 or pick1 == pick3

def test_ruleset_pick_with_disabled_rules():
 
    rs = RuleSet()
    
    rule1 = Rule()
    rule2 = Rule()

    rs.add_rule(rule1)
    rs.add_rule(rule2)

    rs.disable(rule1)

    pick1 = rs.pick()
   
    for i in range(10):
        # pick1 always equals rule2, since rule1 has been disabled
        assert pick1 == rule2


def test_ruleset_pick_from_all_disabled():

    rs = RuleSet()
    
    rule1 = Rule()
    rule2 = Rule()

    rs.add_rule(rule1)
    rs.add_rule(rule2)

    rs.disable(rule1)
    rs.disable(rule2)

    try:
        rs.pick()
        assert False, "An exception is expected when picking from a ruleset with all disabled rules"
    except ObjectDoesNotExist:
        # this was expected
        pass
   
 


def test_ruleset_pick_from_empty_ruleset():

    rs = RuleSet()

    try:
        rs.pick()
        assert False, "An exception should be raised when picking from an empty ruleset"
    except ObjectDoesNotExist:
        # this was expected
        pass


def test_engine_trivial():

    re = Engine()

    rule1 = Rule()
    
    re.add_rule(rule1)

    asts_pre = {} # at the moment pretty much every object can be an ASTS

    asts_post = re.run(asts_pre)

    assert asts_pre == asts_post
    assert re.num_applications == 0


def test_engine_no_enabled_rules():

    re = Engine()

    rule1 = FakeRuleNoMatch()
    rule2 = FakeRuleMatchNoWhere()
    
    re.add_rule(rule1)
    re.add_rule(rule2)

    asts_pre = {} # at the moment pretty much every object can be an ASTS

    # the execution is supposed to terminate because the two rules either
    # do not match anything, or they return False in the where() condition
    asts_post = re.run(asts_pre)

    assert asts_pre == asts_post
    assert re.num_applications == 0


def test_engine_divergent():

    @timeout(2)
    def start_engine(engine, asts):
        # the execution of the run(...) method is supposed not to terminate, for
        # this reason we wrap it in a funtion with a @timeout decorator that
        # expires after 2 seconds
        return engine.run(asts)

    re = Engine()

    rule1 = FakeRuleMatch()
    rule2 = FakeRuleMatchAndWhere()
    
    re.add_rule(rule1)
    re.add_rule(rule2)

    asts_pre = {} # at the moment pretty much every object can be an ASTS

    try:
        start_engine(re, asts_pre)
        assert False, "The execution was expected to loop forever"
    except TimeoutError:
        # the exception was expected
        pass

    assert re.num_applications > 0
