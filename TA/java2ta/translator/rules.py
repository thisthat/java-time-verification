import abc
from java2ta.abstraction import AbstractAttribute, StateSpace
from java2ta.abstraction.domains import Domain
from java2ta.engine.rules import Rule
from java2ta.ta.models import TA, Location, Edge
from java2ta.ir.models import Klass, Thread, Method
    

class ExtractStateSpace(Rule):
    """
    This rule assumes the context has a dictionary called 'abs_domains'
    associating every attribute/variable name with a list of admitted abstract
    values. A state-space is created where all the combinations of 
    attributes/variables abstract values are enumerated.

    If the rule is applied, it adds an entry 'state_space' of type StateSpace
    to the context.
    """

    __metaclass__ = abc.ABCMeta

    def match(self):
        return self.ctx.get("state_space") is None and self.ctx.get("abs_domains") is not None

    def let(self):
 
        attributes = self.get_attributes()   
        domains = self.ctx.get("abs_domains")
        assert isinstance(domains, dict), domains

        ss = StateSpace()

        # 1. find attributes with an associated abstract domain
        for attr in attributes:
            name = attr["name"]
            if name in domains:
                dom = domains[name]

                assert isinstance(dom, Domain)
                
                abs_att = AbstractAttribute(name, dom.values, dom.default)
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
    """
    This rule builds the StateSpace of a class instance, 
    examining the combinations of all from its attributes.
    """

    def get_attributes(self):
        assert isinstance(self.asts_in, Klass)
        assert "attributes" in self.asts_in.ast, self.asts_in.ast.keys()
        attributes = self.asts_in.ast["attributes"]

        return attributes

class ExtractMethodStateSpace(ExtractClassStateSpace):
    """
    This rule builds the StateSpace of a method, by collecting the relevant
    variables and attributes to which the method has access:
    - class attributes
    - method formal parameters
    - method local variables (TODO)
    """

    def get_attributes(self):
        assert isinstance(self.asts_in, Method)

        klass = self.asts_in.parent
        method_name = self.asts_in.name
        
        class_attributes = klass.ast["attributes"]
        method_parameters = self.asts_in.ast["parameters"]
 
        return class_attributes + method_parameters       


class AddStates(Rule):
    """
    This rule takes a StateSpace and create a number of locations in the 
    output Timed Automaton.
    """

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



class TranslateMethod(Rule):
    """
    This rule picks a method to be translated to a Timed Automaton, from a list
    of methods specified in the context.
    """

    def match(self):
        """
        There is something to do iff:
        - there is a list of methods to be translated
        - there is not a method under translation, currently
        - not all the methods have been translated
        """
        methods_todo = self.ctx.get("translate_methods")
        methods_done = self.ctx.get("methods_translated")
        curr_method = self.ctx.get("method_curr")

        return isinstance(methods_todo,list) and curr_method is None and (len(set(methods_todo) - set(methods(done))) > 0)


    def do_update_context(self, let_ctx):
        """
        Pick a method among those to be translated that have not been
        processed, so far. Save the method in the 'method_curr' variable in
        the context. Please, note that so far the rule does not make any 
        assumption about what a method is (it may be a single name, 
        a tuple with extra-information or an instance of some class)
        """
        assert self.ctx.get("method_curr") is None

        methods_todo = self.ctx.get("translate_methods")
        methods_done = self.ctx.get("methods_translated")
 
        missing = set(methods_todo) - set(methods_done) 

        pick_method = next(iter(missing))

        self.ctx.update("method_curr", pick_method)      


