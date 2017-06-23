import abc
import re

from java2ta.commons.utility import partial_format, pairwise_iter

from contracts import contract, new_contract
import logging

log = logging.getLogger("main")

class DataType(object): 
    """
    A DataType is the representation of the data-type as insed
    in a programming language.

    We assume that programming language data-types can be declared
    as Z3/SMTLib2 data-types. Indeed in our framework we apply the 
    following mapping, roughly:

    - byte/short/int/long/Integer/Long -> Int
    - float/double/java.math.BigDecimal -> Real
    - classes -> recursive structures
    """
 
    def __init__(self, name, smt_declaration=None, smt_axioms=[]):
        self._name = name
        self._smt_declaration = smt_declaration or ""
        self._smt_axioms = list(smt_axioms)

    def __str__(self):
        return self.name

    def __repr__(self):
        curr_repr = self.smt_declaration or self.name

        return curr_repr

    @property
    def smt_declaration(self):
        declaration = self._smt_declaration

        if self._smt_axioms:
            declaration = declaration + "\n" + "\n".join(self._smt_axioms)

        return declaration or ""

    @property
    def smt_axioms(self):
        return self._smt_axioms

    def set_smt_declaration(self, smt_declaration):
        self._smt_declaration = smt_declaration

    def add_axiom(self, axiom):
        self._smt_axioms.append(axiom)

    @property
    def name(self):
        return self._name

    def __str__(self):
        return self._name


class Predicate(object):

    __metaclass__ = abc.ABCMeta

#    _smt_assert = ""
    _smt_condition = ""
    _label = ""

    def __init__(self, ctx, smt_condition=None, label=None): #smt_assert=None, label=None):

        self.ctx = dict(ctx)

        label = label or self._label
        self._label = partial_format(label, ctx)


#        smt_assert = smt_assert or self._smt_assert
#        self._smt_assert = partial_format(smt_assert, ctx)
        smt_condition = smt_condition or self._smt_condition
        self._smt_condition = partial_format(smt_condition, ctx)


    def label(self, **kwargs):
        """
        When invoking this method, the user should pass through **kwargs all the 
        missing parameters appearing in the predicate label.

        If some of the parameters are still missing, an exception is raised. 
        """
        ctx = dict(**self.ctx)
        ctx.update(kwargs)

        try:
            res = self._label.format(**ctx)
        except KeyError, e:
            raise ValueError("You should pass the following parameters to the method: %s" % ",".join(e.args))

        return res

    
    def smt_assert(self, **kwargs):

        res = "(assert %s)" % self.smt_condition(**kwargs)

        return res


    def smt_condition(self, **kwargs):
        """
        When invoking this method, the user should pass through **kwargs all the 
        missing parameters appearing in the smt assertion.

        If some of the parameters are still missing, an exception is raised.
        """
        ctx = dict(**self.ctx)
        ctx.update(**kwargs)

        try:
            res = self._smt_condition.format(**ctx) #self._smt_assert.format(**ctx)
        except KeyError, e:
            raise ValueError("You should pass the following parameters to the method: %s" % ",".join(e.args))

        return res
    
    def __repr__(self):
        return "(assert %s)" % self._smt_condition #self._smt_assert
    
    def __str__(self):
        return self._label

    @contract(var_names="None|list", suffix="None|str")
    def primed(self, var_names=None, suffix="_1"):
#        assert var_names==None or isinstance(var_names, list) 

        new_label = self._label
        new_condition = self._smt_condition # self._smt_assert

        if not var_names:
            # by default, prime every "dangling" reference in the context
            var_names = re.findall("\{[a-zA-Z]+\}", self._smt_condition) #self._smt_assert) # TODO this is a hack, find a better way to handle it
            # findall may return twice the same name, if 2 occurrences are there; don't consider duplicates
            var_names = set(var_names)
#        log.debug("Var names to be primed: %s" % var_names)
        for var in var_names:
            var = var.strip("{}") # remove initial and trailing curly brackets, if present

            assert isinstance(var, basestring)
            new_condition = new_condition.replace("{%s}" % var, "{%s}%s" % (var, suffix))
            new_label = new_label.replace("{%s}" % var, "{%s}%s" % (var, suffix))

        new = Predicate(ctx=self.ctx, smt_condition=new_condition, label=new_label)

        return new


class BinaryPredicate(Predicate):

    __metaclass__ = abc.ABCMeta

    _smt_name = "..." # the name of the predicate in SMTlib
#    _smt_assert = "(assert ({name} {var} {value}))"
    _smt_condition = "({name} {var} {value})"
    _label = "{var} {name} {value}"

    def __init__(self, ctx):

        ctx["name"] = self._smt_name
        super(BinaryPredicate, self).__init__(ctx)
    

class GT(BinaryPredicate):
    _smt_name = ">"


class GTE(BinaryPredicate):
    _smt_name = ">="


class LT(BinaryPredicate):
    _smt_name = "<"


class LTE(BinaryPredicate):
    _smt_name = "<="


class Eq(BinaryPredicate):
    _smt_name = "="


class NotEq(BinaryPredicate):
    _smt_name = "distinct"
    _label = "{var} != {value}"

class Between(Predicate):    
#    _smt_assert = "(assert (and (< {min} {var}) (< {var} {max})))"
    _smt_condition = "(and (< {min} {var}) (< {var} {max}))"
    _label = "{min} < {var} < {max}"


class EqItself(Predicate):
#    _smt_assert = "(assert (= {var} {var}))"
    _smt_condition = "(= {var} {var})"
    _label = "{var} = {var}"


@contract(returns="list[M],M=2")
def split_eq_value(value):
    
    predicates = [ Eq({"value":value}), NotEq({"value":value}) ]

    return predicates


def split_enum(value_list):

    predicates = []

    for curr in value_list:
        predicates.append(Eq({"value":curr}))

    return predicates

@contract(split_values="list[N],N>0", lt_min=bool, gt_max=bool, returns="list[M],M>0")
def split_numeric_domain(split_values, lt_min=True, gt_max=True):
    
    predicates = []
 
    if len(split_values) > 0:

        # ignore duplicates and process the items in order
        split_values = sorted(set(split_values))

        if lt_min:
            first = split_values[0]
            lt_min_pred = LT({"value":first})
            predicates.append(lt_min_pred)

        for (curr,succ) in pairwise_iter(split_values):
            predicates.append(Eq({"value":curr}))
    
            if succ != None:
                predicates.append(Between({"min":curr,"max":succ}))
  
        if gt_max:
            last = split_values[-1]
            gt_max = GT({"value":last})
            predicates.append(gt_max)
    
    return predicates


def split_field_domain(field_name, predicates):
    """
    This method takes as input a list of Predicate's for a generic {var} and make it 
    a list of predicates over ({field_name} {var}). This is the way to encode the
    access to a field named "field_name" of a record/object "var". 

    In this implementation we assume that all predicates have a free parameter, in their
    assertions, named {var}.
    """
    assert isinstance(field_name, basestring)
    assert isinstance(predicates, list)

    field_predicates = []
    for pred in predicates:
        assert isinstance(pred, Predicate)
    
        smt_condition = pred.smt_condition(var="(%s {var})" % field_name)
        label = pred.label(var="{var}.%s" % field_name)
        fp = Predicate(pred.ctx, smt_condition=smt_condition, label=label)
        field_predicates.append(fp)
        
    return field_predicates

@new_contract
def is_domain(obj):
    if not isinstance(obj, Domain):
        raise ValueError("Expected object (%s) of type Domain." % type(obj))

class Domain(object):
    """
    A Domain is basically a tuple containing:
    - datatype: specifies the Z3/SMTLib2 equivalent of the programming language 
            data-type we are abstracting
    - predicates: specifies a list of predicates that can be applied at concrete
            values in order to obtain the desired abstract values. Note that each
            concrete value is supposed to satisfy *exactly* one of the predicates,
            at each time. In this way, each predicate can be treated as an
            *abstract value* of the domain
    - default: one predicate that is satisfied initially by the variable of the given
            datatype
    """
    @contract(datatype=DataType, predicates="list[N],N>0", default="None|bool")
    def __init__(self, datatype, predicates, default=None):
        self.datatype = datatype
    
        if not default:
            default = predicates[0]

        assert isinstance(default, Predicate), default

        self._default = default
        self.predicates = list(set(predicates))

    
    def __str__(self):
        return "(%s, %s)" % (str(self.datatype), str(self.predicates))

    def __repr__(self):
        return "(%s, %s)" % (repr(self.datatype), repr(self.predicates))

    @property
    def size(self):
        return len(self.predicates)

    @property
    def default(self):
        return self._default

    @property
    @contract(returns="str")
    def smt_predicate_abstraction(self):
        constraints = []

        for pred in self.values:
            assert isinstance(pred, Predicate)
            constraints.append(pred.smt_condition(var="x"))

        type_name = self.name
        res = "(assert (forall ((x %s)) (or %s)))" % (type_name, " ".join(constraints))

        return res
        

    @property
    @contract(returns="str")
    def smt_declaration(self):
        smt_declaration = "<No DataType>"

        if self.datatype:
            smt_declaration = self.datatype.smt_declaration

        smt_declaration = smt_declaration + "\n" + self.smt_predicate_abstraction

        return smt_declaration.strip()
    
    @property    
    def name(self):
        name = "<No DataType>"

        if self.datatype:
            name = self.datatype.name

        return name

    @property
    def values(self):
        return self.predicates

    def format_values(self, **ctx):
        res = map(lambda p: p.label(**ctx), self.predicates)

        return ",".join(res)


class Integer(DataType):
    def __init__(self):
        super(Integer,self).__init__(name="Int")


class Natural(DataType):
    def __init__(self):
        super(Natural, self).__init__(name="Nat", smt_declaration="(declare-datatypes () ((Nat (mk-natural (val Int)))))", smt_axioms=["(assert (forall ((x Nat)) (>= (val x) 0)))"])

class Real(DataType):
    def __init__(self):
        super(Real,self).__init__(name="Real")


class Boolean(DataType):
    def __init__(self):
        super(Boolean,self).__init__(name="Bool")



def smt_declare_scalar(name, values):
    """
    This function creates a valid Z3 scalar datatype only if the passed list contains all strings.
    It does not work in case it contains numbers or keyword.

    At the moment we don't check that values contains valid input values.
    """
    str_values = map(lambda v: str(v), values)

    smt_declaration = "(declare-datatypes () ((%(name)s %(values)s)))" % {
        "name": name, "values": " ".join(str_values)
    }

    return smt_declaration

def smt_declare_rec_datatype(name, projectors):

    dt_projectors = []
    for (attr_name, attr_dt) in projectors.iteritems():
        dt_projectors.append("(%(attr_name)s %(attr_type)s)" % { "attr_name":attr_name, "attr_type": attr_dt })

    smt_declaration = "(declare-datatypes () ((%(name)s (init-%(name)s %(attributes)s))))" % { "name": name, "attributes": " ".join(dt_projectors) }

    return smt_declaration


class String(DataType):
    """
    A String is a pair with:
    - value (abstracted to an integer value)
    - size (an integer value)
    """
    def __init__(self):
        super(String,self).__init__(name="String", smt_declaration=smt_declare_rec_datatype("String", {"value":"Int", "len":"Int"}))



class Collection(DataType):
    
    def __init__(self):
        super(Collection, self).__init__("Collection", smt_declaration=smt_declare_rec_datatype("Collection", {"size":"Int"}))


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


    @contract(fqn=basestring)
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

    @contract(fqn=basestring, attributes="dict")
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

class Dummy(Domain):
    @contract(datatype=DataType)
    def __init__(self, datatype):
        super(Dummy,self).__init__(datatype, [EqItself({})])


INTEGERS = Domain(Integer(), split_numeric_domain([0,]))
POS_INTEGERS = Domain(Integer(), split_numeric_domain([0,], lt_min=False))
NATURALS = Domain(Natural(), split_numeric_domain([0,], lt_min=False))
BOOLEANS = Domain(Boolean(), split_enum([ "true", "false" ]))
COLLECTIONS = Domain(Collection(), split_field_domain("size", split_numeric_domain([0,], lt_min=False )))
STRINGS = Domain(String(), split_field_domain("len", split_numeric_domain([0,], lt_min=False))) 

class BoundedCollection(Domain):
    
    def __init__(self, max_val):
        super(BoundedCollection,self).__init__(Collection(), split_field_domain("size", split_numeric_domain([0,max_val], lt_min=False, gt_max=False)))


