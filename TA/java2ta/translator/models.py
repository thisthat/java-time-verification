from contracts import contract, check
import logging

from java2ta.commons.utility import new_contract_check_type
from java2ta.abstraction.models import AbstractAttribute, Predicate


log = logging.getLogger("main")

class PC(object):
    """
    This class encapsulates the operation of manipulating a program counter
    in our framework.
    """

    @contract(initial="int|string")
    def __init__(self, initial="0"):
        if isinstance(initial, int):
            initial = str(initial)        

        self.pc = initial.strip(".")


    def __str__(self):
        return "@%s" % self.pc


    def __repr__(self):
        return "@%s" % self.pc

    def __add__(self, other):
        
        assert isinstance(other, int)
        assert other >= 0

        new = PC(initial=self.pc)
        new.inc(other)
        
        return new

    def __eq__(self, other):
        return other and self.pc == other.pc


    def is_prefix(self, other):
        return other.pc.startswith("%s." % self.pc)

    def inc(self, to_add=1):
        assert self.pc != None
        assert isinstance(to_add, int)
        assert to_add >= 0

        prefix = ""
        last = ""
    
        parts = self.pc.rsplit(".", 1)
    
        if len(parts) == 1:
            last = parts[0]
        else:
            prefix, last = parts
    
        res = str(int(last) + to_add)
        if len(prefix) > 0:
            res = "%s.%s" % (prefix, res)

        self.pc = res
    
    def push(self, new):
        assert self.pc != None
        
        self.pc = "%s.%s" % (self.pc, new)
        return self
    
    def pop(self):
        assert self.pc != None

        parts = self.pc.rsplit(".", 1)
    
        if len(parts) <= 1:
            raise ValueError("Cannot pop pc value: %s" % self.pc)
    
        self.pc = parts[0] 
        return parts[1]
    
class ReachabilityResult(object):

    @contract(configurations="set(is_configuration)|list(is_configuration)", final_locations="list(is_location)", external_locations="list(is_location)", edges="list(is_edge)")
    def __init__(self, configurations, final_locations, external_locations, edges):
        self._configurations = set(configurations)
        self.final_locations = final_locations
        self.external_locations = external_locations
        self.edges = edges 

    def __str__(self):
        return "Configurations: {%s}, Final locations: {%s}, External locations: {%s}, Edges: {%s}" % (self.configurations, self.final_locations, self.external_locations, self.edges)

    @property
    @contract(returns="list(is_configuration)")
    def configurations(self):
        return sorted(self._configurations)


class AttributePredicate(object):

    @contract(attribute="is_abstract_attribute", predicate="is_predicate")
    def __init__(self, attribute, predicate):

        self.attribute = attribute
        self.predicate = predicate


    def get_context(self):

        assert len(self.predicate.var_names) == 0 or len(self.predicate.var_names) == len(self.attribute.variables), "%s vs %s" % (self.predicate, self.attribute)

        ctx = {}
        for (pred_var, attr_var) in zip(self.predicate.var_names, self.attribute.variables):
            pred_var_name = pred_var.strip("{}")
            ctx[pred_var_name] = attr_var

#        log.debug("Predicate: %s. Attribute: %s. Context: %s." % (self.predicate, self.attribute, ctx))

        return ctx


    def smt_assert(self):
        # TODO here we assume that all predicates have a variable {var} in them
#        assert len(self.predicate.var_names) <= 1, self.predicate

        ctx = self.get_context()

#        ctx = {}
#        if len(self.predicate.var_names) == 1:
#            var_name = list(self.predicate.var_names)[0].strip("{}")
#            ctx = { var_name: self.attribute.name }
 
        return self.predicate.smt_assert(**ctx) #var=self.attribute.name)
 
    def label(self):
        # TODO here we assume that all predicates have a variable {var} in them
##        assert len(self.predicate.var_names) <= 1, self.predicate
##        ctx = {}
##        if len(self.predicate.var_names) == 1:
##            var_name = list(self.predicate.var_names)[0].strip("{}")
##            ctx = { var_name: self.attribute.name }

        ctx = self.get_context()
        return self.predicate.label(**ctx) #var=self.attribute.name)       

    def __str__(self):
        # TODO here we assume that all predicates have a variable {var} in them
##        assert len(self.predicate.var_names) <= 1, self.predicate
##        res = str(self.predicate)
##
##        if len(self.predicate.var_names) == 1:
##            var_name = list(self.predicate.var_names)[0]
##            res = res.replace(var_name, self.attribute.name) #"{var}", self.attribute.name)
        ctx = self.get_context()

        res = str(self.predicate)
        for pred_var, attr_val in ctx.iteritems():
            res = res.replace("{%s}" % pred_var, attr_var)

        return res


    @contract(self="type(t)",returns="type(w),w=t")
    def primed(self):
        check("is_predicate", self.predicate)

        primed_pred = self.predicate.primed(suffix="_1")
        new_attr_pred = AttributePredicate(self.attribute, primed_pred)

        return new_attr_pred

new_contract_check_type("is_attribute_predicate", AttributePredicate)


class Cache(object):

    @contract(name="string")
    def __init__(self, name):
        self._values = {}
        self.n_hits = 0
        self.n_tot = 0
        self.name = name

    @property
    @contract(returns="int,>=0")
    def n_misses(self):
        return self.n_tot - self.n_hits    

    @property
    @contract(returns="int,>=-1")
    def ratio(self):
        r = -1
        if self.n_tot > 0:
            r = self.n_hits / self.n_tot
        return r
       
    @contract(key="string")
    def lookup(self, key):
        res = None
        self.n_tot = self.n_tot + 1
        if key in self._values:
            res = self._values[key]
            self.n_hit = self.n_hits + 1
            log.debug("Cache %s hit: %s => %s" % (self.name, key, res))
        else:
            log.debug("Cache %s miss: %s" % (self.name, key))

        return res

    @contract(key="string")
    def store(self, key, new_value):
        self._values[key] = new_value



##@new_contract
##def is_precondition(obj):
##    if not isinstance(obj, Precondition):
##        raise ValueError("Expected Precondition, not %s" % type(obj))
##

class Precondition(object):

    def __init__(self, node):
        assert isinstance(node, dict) or isinstance(node, Precondition)
        self.child = node

    @property
    @contract(returns="dict")
    def node(self):
        if isinstance(self.child, Precondition):
            node = self.child.node
        else:
            node = self.child

        return node

new_contract_check_type("is_precondition", Precondition)

class Negate(Precondition):
    pass


