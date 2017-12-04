from contracts import contract, check
import logging

from java2ta.commons.utility import new_contract_check_type
from java2ta.abstraction.models import AbstractAttribute, Predicate
from java2ta.ta.models import ClockVariable, Variable, Int

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

        self.__pc = initial.strip(".")

    @property
    def pc(self):
        return self.__pc

    def __str__(self):
        return "@%s" % self.__pc

    def __repr__(self):
        return "@%s" % self.__pc

    def __add__(self, other):
        
        assert isinstance(other, int)
        assert other >= 0

        new = PC(initial=self.__pc)
        new.inc(other)
        
        return new

    def __eq__(self, other):
        return other and self.__pc == other.pc

    def __hash__(self):
        return hash(self.__pc)

    def is_prefix(self, other):
        return other.pc.startswith("%s." % self.__pc)

    def inc(self, to_add=1):
        assert self.__pc != None
        assert isinstance(to_add, int)
        assert to_add >= 0

        prefix = ""
        last = ""
    
        parts = self.__pc.rsplit(".", 1)
    
        if len(parts) == 1:
            last = parts[0]
        else:
            prefix, last = parts
    
        res = str(int(last) + to_add)
        if len(prefix) > 0:
            res = "%s.%s" % (prefix, res)

        self.__pc = res
    
    def push(self, new):
        assert self.__pc != None
        
        self.__pc = "%s.%s" % (self.__pc, new)
        return self
    
    def pop(self):
        assert self.__pc != None

        parts = self.__pc.rsplit(".", 1)
    
        if len(parts) <= 1:
            raise ValueError("Cannot pop pc value: %s" % self.__pc)
    
        self.__pc = parts[0] 
        return parts[1]
 

new_contract_check_type("is_pc", PC)  

class ReachabilityResult(object):

    @contract(configurations="set(is_configuration)|list(is_configuration)", final_locations="list(is_location)", external_locations="list(is_location)", edges="list(is_edge)", variables="set(is_variable)")
    def __init__(self, configurations, final_locations, external_locations, edges, variables):
        self._configurations = set(configurations)
        self.final_locations = final_locations
        self.external_locations = external_locations
        self.edges = edges 
        self.variables = variables

        self.locations = set([])
        for e in edges:
            self.locations.add(e.source)
            self.locations.add(e.target)

    def __str__(self):
        return "Configurations: {%s}, Final locations: {%s}, External locations: {%s}, Edges: {%s}" % (self.configurations, self.final_locations, self.external_locations, self.edges)

    @property
    @contract(returns="list(is_configuration)")
    def configurations(self):
        return sorted(self._configurations)


class AttributePredicate(object):

    @contract(attribute="is_abstract_attribute", predicate="is_predicate")
    def __init__(self, attribute, predicate):

        self._attribute = attribute
        self._predicate = predicate
        self._ctx = self._get_context()

    @property
    @contract(returns="is_abstract_attribute")
    def attribute(self):
        return self._attribute

    @property
    @contract(returns="is_predicate")
    def predicate(self):
        return self._predicate

    @property
    @contract(returns="list(string)")
    def variables(self):
        return self.attribute.variables

    def _get_context(self):
        check("is_predicate", self.predicate)
        assert len(self.predicate.var_names) == 0 or len(self.predicate.var_names) == len(self.attribute.variables), "%s vs %s" % (self.predicate, self.attribute)

        ctx = {}
        for (pred_var, attr_var) in zip(self.predicate.var_names, self.attribute.variables):
            check("string", pred_var)
            check("string", attr_var)
            pred_var_name = pred_var.strip("{}")
            ctx[pred_var_name] = attr_var

        return ctx


    @contract(returns="string")
    def smt_assert(self):
        check("is_predicate", self.predicate)
        #ctx = self.get_context()

        return self.predicate.smt_assert(**(self._ctx))
 
    @contract(returns="string")
    def label(self):
        check("is_predicate", self_predicate)
        return self.predicate.label(**(self._ctx))

    def __str__(self):

        res = str(self.predicate)
        for pred_var, attr_val in self._ctx.iteritems():
            res = res.replace("{%s}" % pred_var, attr_val)

        return res

    def __unicode__(self):
        return str(self)

    def __repr__(self):
        return str(self)


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

    @property
    @contract(returns="string")
    def code(self):
        return self.node["code"]

    def __str__(self):
        return "%s" % self.child

new_contract_check_type("is_precondition", Precondition)

class Negate(Precondition):
    pass

class FreshNames(object):

    _COUNTER = 1000
    clock_variables = {}

    @staticmethod
    @contract(prefix="string", returns="string")
    def get_name(prefix):
        return "%s%s" % (prefix, FreshNames.get_id())

    @staticmethod
    @contract(returns="int")
    def get_id():
        curr_id = FreshNames._COUNTER
        FreshNames._COUNTER = FreshNames._COUNTER + 1
        return curr_id

    @staticmethod
    @contract(pc="is_pc", prefix="string")
    def enter_clock_variable(pc, prefix=""):
#        cv_name = "%s%s" % (prefix, pc)
        cv_name = FreshNames.get_name(prefix)
        cv = ClockVariable(cv_name)

        lower = Variable("%s_lo" % cv.name, Int())
        upper = Variable("%s_up" % cv.name, Int())

        FreshNames.clock_variables[pc] = (cv, lower, upper)

        return (cv, lower, upper)

    @staticmethod
    @contract(pc="is_pc", prefix="string")
    def get_clock_variable(pc, prefix=""):

        (cv, lower, upper) = FreshNames.clock_variables.get(pc, (None,None,None))

        if not cv:
            (cv, lower, upper) = FreshNames.enter_clock_variable(pc, prefix=prefix)

        return cv

    @staticmethod
    @contract(pc="is_pc", prefix="string")
    def get_clock_bounds(pc, prefix=""):

        (cv, lower, upper) = FreshNames.clock_variables.get(pc, (None,None,None))

        if not cv:
            (cv, lower, upper) = FreshNames.enter_clock_variable(pc, prefix=prefix)

        return (lower, upper)



class KnowledgeBase(object):

    KB = {}
 
    @staticmethod   
    @contract(class_name="string", method_name="string", knowledge="tuple(list(string),string,is_data_type)")
    def add_method(class_name, method_name, knowledge):

        if class_name not in KnowledgeBase.KB:
            KnowledgeBase.KB[class_name] = {}

        if method_name in KnowledgeBase.KB[class_name]:
            raise ValueError("You already provided an interpretation for method (%s,%s)" % (class_name, method_name))

        KnowledgeBase.KB[class_name][method_name] = knowledge

        
    @staticmethod
    @contract(class_name="string", method_name="string", returns="bool")
    def has_method(class_name, method_name):
        res = (class_name in KnowledgeBase.KB and method_name in KnowledgeBase.KB[class_name])
        return res

    @staticmethod
    @contract(class_name="string", method_name="string", res_var="string", params="dict(string:string)", lhs_var="string", returns="tuple(list(string),string,is_data_type)")
    def get_method(class_name, method_name, res_var, params, lhs_var):

        assert KnowledgeBase.has_method(class_name, method_name)

        check("tuple(list(string),string,is_data_type)", KnowledgeBase.KB[class_name][method_name])

        (kb_smt_declarations, kb_smt_assertion,dt) = KnowledgeBase.KB[class_name][method_name]

        ctx = dict(params)
        ctx["res"] = res_var
        ctx["lhs"] = lhs_var
    
        log.debug("Context: %s" % ctx)
        fun_replace = lambda x,y: x.replace("{%s}" % y, ctx[y])

        smt_declarations = []
        smt_assertion = reduce(fun_replace, ctx, kb_smt_assertion) #kb_smt_assertion.replace("{res}", res_var)

        for curr in kb_smt_declarations:
            curr_smt_declaration = reduce(fun_replace, ctx, curr)
            smt_declarations.append(curr_smt_declaration)

        return smt_declarations, smt_assertion, dt


