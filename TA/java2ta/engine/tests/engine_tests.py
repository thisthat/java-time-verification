from nose.tools import *

from java2ta.engine.rules import RuleSet, Rule, Engine
from java2ta.engine.context import Context
from java2ta.engine.exceptions import EmptyContext, ObjectDoesNotExist
from java2ta.commons.timeout import timeout, TimeoutError

class FakeRuleNoMatch(Rule):

    def match(self):
        return False


class FakeRuleMatch(Rule):

    def match(self):
        return True


class FakeRuleMatchNoWhere(FakeRuleMatch):

    def where(self, let_ctx):
        return False


class FakeRuleMatchAndWhere(FakeRuleMatch):

    def where(self, let_ctx):
        return True


class FakeRuleMatchAndLet(FakeRuleMatch):

    def match(self):
    
        match_res = True

        try:
            match_res = self.ctx.get("a") != 1
        except ObjectDoesNotExist:
            pass

        return match_res
        

    def let(self):
        return {
            "a":1,
            "b":2,
            "c":3,
        }

    def do_update_context(self, let_ctx):

        self.ctx.update("a", let_ctx["a"])
        self.ctx.update("b", let_ctx["b"])


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


def test_context_top_empty():

    ctx = Context()

    try:
        top = ctx.top()
        assert False, "Looking for the top element of an empty context should raise an exception"
    except EmptyContext:
        # this is expected
        pass


def test_context_push_and_top():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    top1 = ctx.top()
    top2 = ctx.top()

    assert top1 == top2
    assert top1 == env1


def test_context_push_and_top():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    top1 = ctx.top()

    pop1 = ctx.pop()

    top2 = ctx.top()

    assert top1 != top2
    assert top1 == env2
    assert top2 == env1


def test_context_push_pop_and_top():

    ctx = Context()

    env1 = { "a":1, "b": 2 }
    env2 = { "c":3, "d": 4 }

    ctx.push(env1)
    ctx.push(env2)

    pop1 = ctx.pop()
    pop2 = ctx.pop()

    try:
        top1 = ctx.top()
        assert False, "At this point the context should be empty and the top() raise an exception"
    except EmptyContext:
        # this was expected
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

    assert ctx.get("x") is None, "The name 'x' should not be present in the context"
 
def test_ruleset_pick_simple():

    rs = RuleSet()
    
    rule1 = FakeRuleNoMatch()
    rule2 = FakeRuleMatch()

    rs.add_rule(rule1)
    rs.add_rule(rule2)

    pick1 = rs.pick()

    assert pick1 == rule1 or pick1 == rule2


def test_ruleset_pick_multiple_times():
 
    rs = RuleSet()
    
    rule1 = FakeRuleNoMatch()
    rule2 = FakeRuleMatch()

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
    
    rule1 = FakeRuleNoMatch()
    rule2 = FakeRuleMatch()

    rs.add_rule(rule1)
    rs.add_rule(rule2)

    rs.disable(rule1)

    pick1 = rs.pick()
   
    for i in range(10):
        # pick1 always equals rule2, since rule1 has been disabled
        assert pick1 == rule2


def test_ruleset_pick_from_all_disabled():

    rs = RuleSet()
    
    rule1 = FakeRuleNoMatch()
    rule2 = FakeRuleMatch()

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

    rule1 = FakeRuleNoMatch()
    
    re.add_rule(rule1)

    asts_in_pre = {} # at the moment pretty much every object can be an ASTS
    asts_out_pre = {} 

    (asts_out_post,ctx_post) = re.run(asts_in_pre, asts_out_pre)

    assert asts_in_pre == asts_out_post
    assert re.num_applications == 0


def test_engine_no_enabled_rules():

    re = Engine()

    rule1 = FakeRuleNoMatch()
    rule2 = FakeRuleMatchNoWhere()
    
    re.add_rule(rule1)
    re.add_rule(rule2)

    asts_in_pre = {} # at the moment pretty much every object can be an ASTS
    asts_out_pre = {}

    # the execution is supposed to terminate because the two rules either
    # do not match anything, or they return False in the where() condition
    (asts_out_post,ctx_post) = re.run(asts_in_pre, asts_out_pre)

    assert asts_out_pre == asts_out_post
    assert re.num_applications == 0


def test_engine_divergent():
    """
    Note: this test uses @timeout decorator, which in turn uses signal library
    and the SIGALARM. The latter is only supported in the Unix world.
    """

    @timeout(2)
    def start_engine(engine, asts_in, asts_out):
        # the execution of the run(...) method is supposed not to terminate, for
        # this reason we wrap it in a funtion with a @timeout decorator that
        # expires after 2 seconds
        return engine.run(asts_in, asts_out)

    re = Engine()

    rule1 = FakeRuleMatch()
    rule2 = FakeRuleMatchAndWhere()
    
    re.add_rule(rule1)
    re.add_rule(rule2)

    asts_in_pre = {} # at the moment pretty much every object can be an ASTS
    asts_out_pre = {}

    try:
        (asts_out_post, ctx_post) = start_engine(re, asts_in_pre, asts_out_pre)
        assert False, "The execution was expected to loop forever"
    except TimeoutError:
        # the exception was expected
        pass

    assert re.num_applications > 0

def test_engine_rule_match_once():

    re = Engine()

    rule1 = FakeRuleMatchAndLet()
    
    re.add_rule(rule1)

    asts_in_pre = {} # at the moment pretty much every object can be an ASTS
    asts_out_pre = {}

    # the execution is supposed to terminate because the two rules either
    # do not match anything, or they return False in the where() condition
    (asts_out_post, ctx_post) = re.run(asts_in_pre, asts_out_pre)

    assert asts_out_pre == asts_out_post
    assert re.num_applications == 1
    assert ctx_post.get("a") == 1
    assert ctx_post.get("b") == 2

    ctx_post.get("c") is None, "The rule FakeRuleMatchAndLet does not set any 'c' variable in the do_update_context(...) method"

