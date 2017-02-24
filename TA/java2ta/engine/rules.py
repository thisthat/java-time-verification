class Rule(object):
    """
    Abstract class for a rule
    """
    
    def __init__(self, asts, ctx):
        self.asts = asts
        self.ctx = ctx

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
        return {}

    def where(self):
        """
        Allow to specify an additional boolean constraint on the matching terms.
        Return True iff the additional condition is satisfied. The rule can
        be applied only if this method returns True.
        """
        return False

    def do_rewrite_asts(self):
        """
        Implement the actual transformation of the input abstract syntax trees.
        The resulting term can make use of:
        - the original term
        - the bindings in the original context
        - the bindings in the additional context produced by the let() method
        """
        return self.asts

    def do_update_context(self):
        """
        Modify the passed context to propagate some global information on the
        next chosen rules. The context can be updated with information from:
        - the original input term
        - the rewritten term
        - the original context
        - the context returned by the let() method
        """
        return self.ctx


