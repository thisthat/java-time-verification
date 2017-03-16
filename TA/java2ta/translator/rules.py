import abc
from java2ta.abstraction import AbstractAttribute, StateSpace
from java2ta.engine.rules import Rule
from java2ta.ta.models import TA, Location, Edge
from java2ta.ir.models import Klass, Thread
    

class ExtractStateSpace(Rule):
    """
    This rule assumes the context has a dictionary called 'abs_predicates'
    associating every attribute name with a list of admitted values.

    If the rule is applied, it adds an entry 'state_space' of type StateSpace
    to the context.
    """

    __metaclass__ = abc.ABCMeta

    def match(self):
        return self.ctx.get("state_space") is None and self.ctx.get("abs_predicates") is not None

    def let(self):
 
        attributes = self.get_attributes()   
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

    @abc.abstractmethod
    def get_attributes(self):
        return None
       

    def do_update_context(self, let_ctx):
        self.ctx.update("state_space", let_ctx["state_space"])


class ExtractClassStateSpace(ExtractStateSpace):

    def get_attributes(self):
        assert isinstance(self.asts_in, Klass)
        attributes = self.asts_in.ast["attributes"]

        return attributes

class ExtractMethodStateSpace(ExtractClassStateSpace):

    def get_attributes(self):
        assert isinstance(self.asts_in, tuple)
        assert isinstance(self.asts_in[0], Klass)
        assert isinstance(self.asts_in[1], basestring)

        klass, method_name = self.asts_in
        
        # TODO in case of method overloading, now we pick the first; 
        # find a better way to specify exactly which method is the desired one
        method = klass.get_methods(method_name)[0]        

        class_attributes = klass.ast["attributes"]
        method_parameters = method["parameters"]
 
        return class_attributes + method_parameters       


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

        

        
