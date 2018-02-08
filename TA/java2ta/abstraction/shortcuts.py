import abc
import re
import itertools

from java2ta.commons.utility import partial_format, pairwise_iter
from java2ta.abstraction.models import DataType, DataTypeUnion, Predicate, And, Between, GT, LT, Eq, NotEq, EqItself, Dummy, Integer, Object, Iterator, Real, Z3String, AbsString, Natural, Boolean, Collection, BoundedCollection, Dummy, Domain, SymbolTable
from contracts import contract, check
import logging

log = logging.getLogger("main")

@contract(value="string|int", var="string|None", returns="is_predicate")
def noteq_predicate(value, var=None):
    res = None

    if var:
        res = NotEq(lhs=var, rhs=value)
    else:
        res = NotEq(rhs=value)
    return res

@contract(value="string|int", var="string|None", returns="is_predicate")
def eq_predicate(value, var=None):
    res = None
    if var: 
        res = Eq(lhs=var, rhs=value)
    else:
        res = Eq(rhs=value)
    return res

@contract(value_list="list[M](string|int)", returns="list[N](is_predicate),N<=M+1,N>=M")
def split_enum(value_list, case_else=False, var=None):

    predicates = []

    for value in value_list:
        predicates.append(eq_predicate(value, var))

    if case_else:
        value = " ".join(map(str, value_list))
        predicates.append(noteq_predicate(value, var))

    return predicates

@contract(returns="list[M](is_predicate),M=2")
def split_eq_value(value, var=None):
    return split_enum([value], case_else=True, var=var)

#@contract(split_values="list[N],N>0", lt_min=bool, gt_max=bool, returns="list[M],M>0")
def split_numeric_domain(split_values, lt_min=True, gt_max=True):
    
    predicates = []
 
    if len(split_values) > 0:

        # ignore duplicates and process the items in order
        split_values = sorted(set(split_values))

        if lt_min:
            first = split_values[0]
#            lt_min_pred = LT({"value":first})
            lt_min_pred = LT(rhs=first)
            predicates.append(lt_min_pred)

        for (value,succ) in pairwise_iter(split_values):
#            predicates.append(Eq({"value":curr}))
#            predicates.append(Eq(value=curr))
            predicates.append(eq_predicate(value))

    
            if succ != None:
#                predicates.append(Between({"min":curr,"max":succ}))
                predicates.append(Between(min=value,max=succ))
  
        if gt_max:
            last = split_values[-1]
#            gt_max = GT({"value":last})
            gt_max = GT(rhs=last)
            predicates.append(gt_max)
    
    return predicates

#@contract(field_name="string", predicates="list(is_predicate)")
#def split_field_domain(field_name, predicates):

@contract(predicates="list(is_predicate)", var_fields="dict(string:string)")
def split_field_domain(predicates, **var_fields):
    """
    This method takes as input a list of Predicate's for a generic {var} and make it 
    a list of predicates over ({field_name} {var}). This is the way to encode the
    access to a field named "field_name" of a record/object "var". 

    In this implementation we assume that all predicates have a free parameter, in their
    assertions, named {var}.
    """
    field_predicates = []
    for pred in predicates:
        check("is_predicate", pred)
    
        fp = pred.copy().wrap_fields(**var_fields)
        field_predicates.append(fp)
        
    return field_predicates


class DataTypeRegistry(dict):
    """
    Keeps a mapping between the fully-qualified name of the data-type in the 
    programming languages and the class describing the corresponding SMT 
    data-type.
    """

    _the_registry = None

    @staticmethod
    def the_registry():

        if DataTypeRegistry._the_registry == None:
            DataTypeRegistry._the_registry = DataTypeRegistry()
        
        return DataTypeRegistry._the_registry


@contract(name="string", projectors="dict(string:string)", returns="string")
def smt_declare_rec_datatype(name, projectors):

    dt_projectors = []
    for (attr_name, attr_dt) in projectors.iteritems():
        dt_projectors.append("(%(attr_name)s %(attr_type)s)" % { "attr_name":attr_name, "attr_type": attr_dt })

    smt_declaration = "(declare-datatypes () ((%(name)s (init-%(name)s %(attributes)s))))" % { "name": name, "attributes": " ".join(dt_projectors) }

    return smt_declaration



class DataTypeFactory(object):
    
    INTEGER_TYPES = [ "byte", "short", "int", "long", "java.lang.AtomicInteger", "java.lang.AtomicLong", "java.lang.BigInteger", "java.lang.Byte", "java.lang.Integer", "java.lang.Long", "java.lang.Short" ]
    REAL_TYPES = [ "float", "double", "java.lang.BigDecimal", "java.lang.Double", "java.lang.Float", ]
    STRING_TYPES = [ "java.lang.String", ]
    BOOL_TYPES = [ "boolean", ]
    OBJECT_TYPES = [ "java.lang.Object", ]
    ITERATOR_TYPES = [ "java.util.Iterator", ]

    # this is used to store the singleton instance of this data-type factory
    _the_factory = None

    @staticmethod
    def the_factory():

        if DataTypeFactory._the_factory == None:
            
            reg = DataTypeRegistry.the_registry()
            DataTypeFactory._the_factory = DataTypeFactory(reg)

        return DataTypeFactory._the_factory


    @contract(registry=DataTypeRegistry)   
    def __init__(self, registry):
        self.registry = registry


    @contract(fqn="string", returns="is_data_type")
    def from_fqn(self, fqn, *args, **kwargs):

        dt = None
        if fqn in self.registry:
            dt = self.registry[fqn]
        else:
            if fqn in self.INTEGER_TYPES:
                dt = Integer()
            elif fqn in self.BOOL_TYPES:
                dt = Boolean()
            elif fqn in self.REAL_TYPES:
                dt = Real()
            elif fqn in self.STRING_TYPES:
                dt = AbsString()
            elif fqn in self.ITERATOR_TYPES:
                dt = Iterator()
            elif fqn in self.OBJECT_TYPES:
                dt = Object()
            else:
                dt = DataType(name=fqn)

            self.registry[fqn] = dt

        return dt

    @contract(fqn="string", attributes="dict", returns="is_data_type")
    def from_class(self, fqn, attributes):
        """
        Assume 'fqn' is a string denoting the full-qualified name of the class, 
        and 'attributes' 
        is a dictionary describing all the attributes of the class itself.
        The latter maps each attribute name to its type.
        """

        if fqn not in self.registry:
            dt = DataType(name=fqn)
            self.registry[fqn] = dt

        dt = self.registry[fqn]

        if not dt.smt_declaration:

            attributes_dt = {}
            for (attr_name, attr_fq_type) in attributes.iteritems():
                attributes_dt[attr_name] = self.from_fqn(attr_fq_type)

            smt_declaration = smt_declare_rec_datatype(fqn, attributes_dt)   
            dt.set_smt_declaration(smt_declaration)

        return dt

## TODO restore the following code
INTEGERS = Domain(Integer(), split_numeric_domain([0,]), default=eq_predicate(0))
POS_INTEGERS = Domain(Integer(), split_numeric_domain([0,], lt_min=False), default=eq_predicate(0))
NATURALS = Domain(Natural(), split_numeric_domain([0,], lt_min=False), default=eq_predicate(0))
BOOLEANS = Domain(Boolean(), split_enum([ "true", "false" ]), default=eq_predicate("false"))
COLLECTIONS = Domain(Collection(), split_field_domain(split_numeric_domain([0,], lt_min=False ),var="size"))
OBJECTS = Domain(Object(), split_field_domain(split_enum(["true","false"]), var="is_null"))
ITERATORS = Domain(Iterator(), split_field_domain(split_numeric_domain([0,], lt_min=False ), var="n_items") + split_field_domain(split_numeric_domain([0,], lt_min=False ), var="n_visited"))

# TODO not use default Z3 strings because it looks like they "slow down" the sat/unsat query enormously (check why and what's a better abstraction; perhaps leave STRINGS as it is, and introduce a MY_STRINGS domain, also useful to compare the results in the two cases)
Z3_STRINGS = Domain(Z3String(), split_field_domain(split_numeric_domain([0,], lt_min=False),var="str.len")) 
STRINGS = Domain(AbsString(), split_field_domain(split_numeric_domain([0,],lt_min=False),var="size"))

