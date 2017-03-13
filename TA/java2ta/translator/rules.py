from java2ta.abstraction import AbstractAttribute, StateSpace
from java2ta.engine.rules import Rule
from java2ta.ta.models import TA, Location, Edge
    

class ExtractStateSpace(Rule):
    """
    This rule assumes the context has a dictionary called 'abs_predicates'
    associating every attribute name with a list of admitted values.

    If the rule is applied, it adds an entry 'state_space' of type StateSpace
    to the context.
    """

    def match(self):
        return self.ctx.get("state_space") is None and self.ctx.get("abs_predicates") is not None

    def let(self):
    
        attributes = self.asts_in.klass["attributes"]
        predicates = self.ctx.get("abs_predicates")
        assert isinstance(predicates, dict), predicates

        ss = StateSpace()

        # 1. find attributes with an abstraction predicate
        for attr in attributes:
            name = attr["name"]
            if name in predicates:
                values = predicates[name]
                
                abs_att = AbstractAttribute(name, values, values[0])
                ss.add_attribute(abs_att)

        let_ctx = {
            "state_space": ss
        }

        return let_ctx

    def do_update_context(self, let_ctx):
        self.ctx.update("state_space", let_ctx["state_space"])


class AddStates(Rule):

    def match(self):

        ss = self.ctx.get("state_space")

        return ss is not None and len(self.asts_out.locations) == 0

    def do_rewrite_asts(self, let_ctx):
    
        assert isinstance(self.asts_out, TA)
    
        ss = self.ctx.get("state_space")

        assert isinstance(ss, StateSpace)

        for conf in ss.enumerate:
            name = "_".join(map(lambda v: "%s" % v, conf))
            l = Location(name)

            self.asts_out.add_location(l)

        return self.asts_out

        

        
