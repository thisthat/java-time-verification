import abc
from java2ta.engine.exceptions import ObjectDoesNotExist
from java2ta.engine.context import Context

class Rule(object):
    """
    Abstract class for a rule
    """
    __metaclass__ = abc.ABCMeta
    
    def __init__(self, *args, **kwargs):
        pass

    def setup(self, asts_in, asts_out, ctx):

        if not isinstance(ctx, Context):
            raise ValueError("Expected context of type Context")

        self.asts_in = asts_in
        self.asts_out = asts_out
        self.ctx = ctx

    @abc.abstractmethod
    def match(self):
        """
        Return True iff the rule matches with the passed set of
        abstract syntax trees. The rule can be applied only if the
        this method returns True.
        """
        return False

    def let(self):
        """
        Return an instance of context, possibly including bindings obtained
        from subterms of the input term. The bindings introduced by this
        method will not be propagated to the next rules, unless the
        do_update_method() does not do it explicitly.
        """
        return None

    def where(self, let_ctx=None):
        """
        Allow to specify an additional boolean constraint on the matching terms.
        Return True iff the additional condition is satisfied. The rule can
        be applied only if this method returns True. The default behavior is to
        return True because actual rules may not want to set a where condition.
        """
        return True

    def do_rewrite_asts(self, let_ctx=None):
        """
        Implement the actual transformation of the input abstract syntax trees.
        The resulting term can make use of:
        - the original term
        - the bindings in the original context
        - the bindings in the additional context produced by the let() method
        """

        if let_ctx is not None and not isinstance(let_ctx, dict):
            raise ValueError("Expected no context or context of type dict")

        return self.asts_out

    def do_update_context(self, let_ctx=None):
        """
        Modify the passed context to propagate some global information on the
        next chosen rules. The context can be updated with information from:
        - the original input term
        - the rewritten term
        - the original context
        - the context returned by the let() method
        """

        if let_ctx is not None and not isinstance(let_ctx, dict):
            raise ValueError("Expected no context or context of type dict")


class RuleSet(object):

    def __init__(self):
    
        self._rules = set()
        self._enabled_rules = set()

    def add_rule(self, rule):
        """
        Add a rule to the ruleset. When added, a rule is also enabled.
        """
    
        if not isinstance(rule, Rule):
            raise ValueError("Expected rule of type Rule")

        self._rules.add(rule)
        self._enabled_rules.add(rule)

    def enable_all(self):
 
        for rule in self._rules:
            self._enabled_rules.add(rule)       

    def disable(self, rule):
        self._enabled_rules.remove(rule)

    def pick(self):
        
        assert isinstance(self._rules, set)

        if len(self._enabled_rules) == 0:
            raise ObjectDoesNotExist("Cannot pick a rule from an empty rule-set")

        rule = next(iter(self._enabled_rules))
        return rule


class Engine(object):

    def __init__(self, ruleset=None):
    
        assert ruleset is None or isinstance(ruleset, RuleSet)

        if ruleset is None:
            ruleset = RuleSet()
   
        self._ruleset = ruleset
        self._num_applications = 0

    @property
    def ruleset(self):
        return self._ruleset

    @property
    def num_applications(self):
        return self._num_applications

    def add_rule(self, rule):
        assert isinstance(self._ruleset, RuleSet)
        assert isinstance(rule, Rule)

        self._ruleset.add_rule(rule)

    def run(self, asts_in, asts_out, ctx=None):

        # start with an empty environment on top of the context
        if not ctx:
            ctx = Context()
        
        ctx.push({})

        self._ruleset.enable_all()

        while True:
      
            rule = None
            try:
                rule = self._ruleset.pick()
            except ObjectDoesNotExist:
                break

            assert isinstance(rule, Rule)

            rule.setup(asts_in, asts_out, ctx)
    
            if rule.match():
                let_ctx = rule.let()
                assert let_ctx is None or isinstance(let_ctx, dict), "The let() method should return None or a dictionary of assignments: { var1:val1, ..., varN, valN }. Returned: %s" % let_ctx

                if rule.where(let_ctx):

                    # overwrite old reference to asts_out
                    asts_out = rule.do_rewrite_asts(let_ctx)
        
                    assert asts_out is not None, "Perhaps you forgot to return the output AST from do_rewrite_asts?"

                    # update context for next rules
                    rule.do_update_context(let_ctx)
        
                    self._num_applications = self._num_applications + 1

                    self._ruleset.enable_all()

                else:
                    self._ruleset.disable(rule)

            else:
                self._ruleset.disable(rule)

        return (asts_out,ctx)
