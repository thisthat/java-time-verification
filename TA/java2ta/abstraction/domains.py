import abc

from java2ta.commons.utility import partial_format, pairwise_iter

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
 
    def __init__(self, name, smt_declaration=None):
        self._name = name
        self._smt_declaration = smt_declaration

    @property
    def smt_declaration(self):
        return self._smt_declaration

    def set_smt_declaration(self, smt_declaration):
        self._smt_declaration = smt_declaration

    @property
    def name(self):
        return self._name

    def __unicode__(self):
        return self._name


class Predicate(object):

    __metaclass__ = abc.ABCMeta

    _smt_assert = ""
    _label = ""

    def __init__(self, ctx, smt_assert=None, label=None):

        self.ctx = ctx

        label = label or self._label
        self._label = partial_format(label, ctx)


        smt_assert = smt_assert or self._smt_assert
        self._smt_assert = partial_format(smt_assert, ctx)


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
        """
        When invoking this method, the user should pass through **kwargs all the 
        missing parameters appearing in the smt assertion.

        If some of the parameters are still missing, an exception is raised.
        """
        ctx = dict(**self.ctx)
        ctx.update(**kwargs)

        try:
            res = self._smt_assert.format(**ctx)
        except KeyError, e:
            raise ValueError("You should pass the following parameters to the method: %s" % ",".join(e.args))

        return res
    

class BinaryPredicate(Predicate):

    __metaclass__ = abc.ABCMeta

    _smt_name = "..." # the name of the predicate in SMTlib
    _smt_assert = "(assert ({name} {var} {value}))"
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


class Between(Predicate):
    
    _smt_assert = "(assert (and (< {min} {var}) (< {var} {max})))"
    _label = "{min} < {var} < {max}"


def split_numeric_domain(split_values):
    
    predicates = []
 
    if len(split_values) > 0:

        # ignore duplicates and process the items in order
        split_values = sorted(set(split_values))
    
        first = split_values[0]
        last = split_values[-1]
    
        lt_min = LT({"value":first})
        gt_max = GT({"value":last})
    
        predicates.append(lt_min)
    
        for (curr,succ) in pairwise_iter(split_values):
            predicates.append(Eq({"value":curr}))
    
            if succ != None:
                predicates.append(Between({"min":curr,"max":succ}))
        
        predicates.append(gt_max)
    
    return predicates


class Domain(object):
    """
    A Domain is basically a pair containing:
    - DataType: specifies the Z3/SMTLib2 equivalent of the programming language 
            data-type we are abstracting
    - Predicates: specifies a list of predicates that can be applied at concrete
            values in order to obtain the desired abstract values. Note that each
            concrete value is supposed to satisfy *exactly* one of the predicates,
            at each time. In this way, each predicate can be treated as an
            *abstract value* of the domain
    """
    def __init__(self, datatype, predicates):
        self.datatype = datatype
        self._default = predicates[0]
        self.predicates = set(predicates)

    @property
    def size(self):
        return len(self.predicates)

    @property
    def default(self):
        return self._default

    @property
    def smt_declaration(self):
        smt_declaration = "<No DataType>"

        if self.datatype:
            smt_declaration = self.datatype.smt_declaration

        return smt_declaration
    
    @property    
    def name(self):
        name = "<No DataType>"

        if self.datatype:
            name = self.datatype.name

        return name


class Integer(DataType):
    def __init__(self):
        super(Integer,self).__init__(name="Int")

class Real(DataType):
    def __init__(self):
        super(Real,self).__init__(name="Real")

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

    # this is used to store the singleton instance of this data-type factory
    _the_factory = None

    @staticmethod
    def the_factory():

        if DataTypeFactory._the_factory == None:
            
            reg = DataTypeRegistry.the_registry()
            DataTypeFactory._the_factory = DataTypeFactory(reg)

        return DataTypeFactory._the_factory

   
    def __init__(self, registry):
        assert isinstance(registry, DataTypeRegistry)
        self.registry = registry


    def from_fqn(self, fqn, *args, **kwargs):

        dt = None
        if fqn in self.registry:
            dt = self.registry[fqn]
        else:
            if fqn in self.INTEGER_TYPES:
                dt = Integer()
            elif fqn in self.REAL_TYPES:
                dt = Real()
            else:
                dt = DataType(name=fqn)

            self.registry[fqn] = dt

        return dt

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
    
            attr_projectors = []
            for (attr_name, attr_type) in attributes.iteritems():

                attr_dt = self.from_fqn(attr_type)
                attr_dt_name = attr_dt.name if attr_dt else "???"

                attr_projectors.append("(%(attr_name)s %(attr_type)s)" % { "attr_name":attr_name, "attr_type": attr_dt_name })

            smt_declaration = "(declare-datatypes () ((%(fqn)s (init-%(fqn)s %(attributes)s))))" % { "fqn": fqn, "attributes": " ".join(attr_projectors) }
            dt.set_smt_declaration(smt_declaration)

        return dt



##INTEGERS = Domain(["null", "< 0", "== 0", "> 0", "max"])
##POS_INTEGERS = Domain(["null", "== 0", "> 0", "max"])
##
##STRING = Domain(["null", "not_null"])
##
##COLLECTION = Domain(["null", "empty", "some_elements" ])
##BOUNDED_COLLECTION = Domain(["null", "empty", "some_elements", "full" ])
##
##BOOLEANS = Domain([ "true", "false" ])
