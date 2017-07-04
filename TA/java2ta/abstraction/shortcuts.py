import abc
import re
import itertools

from java2ta.commons.utility import partial_format, pairwise_iter
from java2ta.abstraction.models import DataType, DataTypeUnion, Predicate, And, GT, LT, Eq, NotEq, EqItself, Dummy, Integer, Real, String, Natural, Boolean, Collection, BoundedCollection, Dummy, Domain
from contracts import contract, new_contract
import logging

log = logging.getLogger("main")

@contract(returns="list[M],M=2")
def split_eq_value(value, var=None):
    
    ctx={"value":value}
    if var:
        ctx["var"] = var
    predicates = [ Eq(**ctx), NotEq(**ctx) ]

    return predicates


def split_enum(value_list):

    predicates = []

    for curr in value_list:
#        predicates.append(Eq({"value":curr}))
        predicates.append(Eq(value=curr))


    return predicates

#@contract(split_values="list[N],N>0", lt_min=bool, gt_max=bool, returns="list[M],M>0")
def split_numeric_domain(split_values, lt_min=True, gt_max=True):
    
    predicates = []
 
    if len(split_values) > 0:

        # ignore duplicates and process the items in order
        split_values = sorted(set(split_values))

        if lt_min:
            first = split_values[0]
#            lt_min_pred = LT({"value":first})
            lt_min_pred = LT(value=first)
            predicates.append(lt_min_pred)

        for (curr,succ) in pairwise_iter(split_values):
#            predicates.append(Eq({"value":curr}))
            predicates.append(Eq(value=curr))

    
            if succ != None:
#                predicates.append(Between({"min":curr,"max":succ}))
                predicates.append(Between(min=curr,max=succ))
  
        if gt_max:
            last = split_values[-1]
#            gt_max = GT({"value":last})
            gt_max = GT(value=last)
            predicates.append(gt_max)
    
    return predicates

@contract(field_name="string", predicates="list(is_predicate)")
def split_field_domain(field_name, predicates):
    """
    This method takes as input a list of Predicate's for a generic {var} and make it 
    a list of predicates over ({field_name} {var}). This is the way to encode the
    access to a field named "field_name" of a record/object "var". 

    In this implementation we assume that all predicates have a free parameter, in their
    assertions, named {var}.
    """
#    assert isinstance(field_name, basestring)
#    assert isinstance(predicates, list)

    field_predicates = []
    for pred in predicates:
        assert isinstance(pred, Predicate)
    
        assert len(pred.var_names) == 1, "Expected predicate with single variable. Got: %s" % pred

        var_name = list(pred.var_names)[0].strip("{}")
        ctx_smt = { var_name:"(%s {%s})" % (field_name, var_name) }
        ctx_label = { var_name: "{%s}.%s" % (var_name, field_name) }
        smt_condition = pred.smt_condition(**ctx_smt) #var="(%s {var})" % field_name)
        label = pred.label(**ctx_label) #var="{var}.%s" % field_name)

#        print "pred context: %s" % pred.ctx
        fp = Predicate(smt_condition=smt_condition, label=label, **pred.ctx)
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


class DataTypeFactory(object):
    
    INTEGER_TYPES = [ "byte", "short", "int", "long", "java.lang.AtomicInteger", "java.lang.AtomicLong", "java.lang.BigInteger", "java.lang.Byte", "java.lang.Integer", "java.lang.Long", "java.lang.Short" ]
    REAL_TYPES = [ "float", "double", "java.lang.BigDecimal", "java.lang.Double", "java.lang.Float", ]
    STRING_TYPES = [ "java.lang.String", ]

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
            elif fqn in self.REAL_TYPES:
                dt = Real()
            elif fqn in self.STRING_TYPES:
                dt = String()
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
INTEGERS = Domain(Integer(), split_numeric_domain([0,]))
POS_INTEGERS = Domain(Integer(), split_numeric_domain([0,], lt_min=False))
NATURALS = Domain(Natural(), split_numeric_domain([0,], lt_min=False))
BOOLEANS = Domain(Boolean(), split_enum([ "true", "false" ]))
COLLECTIONS = Domain(Collection(), split_field_domain("size", split_numeric_domain([0,], lt_min=False )))
STRINGS = Domain(String(), split_field_domain("len", split_numeric_domain([0,], lt_min=False))) 


