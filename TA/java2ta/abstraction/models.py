import re
import abc
from contracts import contract, new_contract, check
import logging

from java2ta.commons.utility import new_contract_check_type, partial_format

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


new_contract_check_type("is_data_type", DataType)

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




class DataTypeUnion(DataType):

    @contract(datatypes="tuple")
    def __init__(self, *datatypes):
        self.datatypes = list(datatypes)

        check("list(is_data_type)", self.datatypes)

        name = "" #"_".join(map(lambda dt: dt.name, datatypes))
        smt_axioms = []
        smt_declaration = "" #"\n".join(map(lambda dt: dt.smt_declaration, datatypes)) # TODO this should be replaced by a tuple
        
        for dt in datatypes:
            if len(name) == 0:
                name = dt.name
            else:
                name = "%s_%s" % (name, dt.name)

            smt_axioms.extend(dt.smt_axioms)

            if len(smt_declaration) == 0:
                smt_declaration = dt.smt_declaration
            else:
                smt_declaration = "%s\n%s".strip() % (smt_declaration, dt.smt_declaration)

        super(DataTypeUnion, self).__init__(name, smt_declaration, smt_axioms)


class Predicate(object):

    __metaclass__ = abc.ABCMeta

#    _smt_assert = ""
    _smt_condition = ""
    _label = ""

    @contract(smt_condition="string|None", label="string|None")
    def __init__(self, smt_condition=None, label=None, **ctx):

        self.ctx = dict(**ctx)

        label = label or self._label
        self._label = partial_format(label, ctx)

        smt_condition = smt_condition or self._smt_condition
        self._smt_condition = partial_format(smt_condition, ctx)
    
    @contract(returns="string")
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

    
    @contract(returns="string")
    def smt_assert(self, **kwargs):

        res = "(assert %s)" % self.smt_condition(**kwargs)

        return res


    @contract(returns="string")
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

    @property
    @contract(returns="set(string)")
    def var_names(self):
        var_names = re.findall("\{[a-zA-Z\_0-9]+\}", self._smt_condition) # TODO this is a bit hack-ish, find a better way to handle it
        return set(var_names)

    @contract(rename="dict(string:string)")
    def rename(self, **rename): #var_old, var_new):

        old_vars = self.var_names

        for var_old, var_new in rename.iteritems():
            var_old = "{%s}" % var_old.strip("{}")            
            var_new = "{%s}" % var_new.strip("{}")

            if var_old not in old_vars:
                raise ValueError("Variable to be renamed (%s) not present in variables (%s)" % (var_old, old_vars))

            self._smt_condition = self._smt_condition.replace(var_old, var_new)
            self._label = self._label.replace(var_old, var_new)

            assert var_old not in self.var_names, "%s vs %s" % (var_old, self.var_names)

        return self

    @contract(var_names="None|list(string)", suffix="string")
    def primed(self, var_names=None, suffix="_1"):

        new_label = self._label
        new_condition = self._smt_condition # self._smt_assert

        if not var_names:
            var_names = self.var_names
        
        for var in var_names:
            var = var.strip("{}") # remove initial and trailing curly brackets, if present

            assert isinstance(var, basestring)
            new_condition = new_condition.replace("{%s}" % var, "{%s}%s" % (var, suffix))
            new_label = new_label.replace("{%s}" % var, "{%s}%s" % (var, suffix))

        new = Predicate(ctx=self.ctx, smt_condition=new_condition, label=new_label)

        return new

new_contract_check_type("is_predicate", Predicate)


class And(Predicate):
        
    @contract(predicates="tuple")
    def __init__(self, *predicates):
        self.predicates = predicates

        exclude = []
        ctx = {}

#        print "received (type:%s): %s" % (predicates, type(predicates))

        # the following iteration leaves in ctx only the common pairs (key,value) of the
        # respective ctx's
        for pred in predicates:
#            assert isinstance(pred, Predicate), "type: %s + %s" % (type(pred),pred)
            check("is_predicate", pred)

            for (key, val) in pred.ctx.iteritems():
                if key not in exclude:
                    if key not in ctx:
                        ctx[key] = val
                    elif ctx[key] != val:
                        del ctx[key]

        self.ctx = ctx

    def __repr__(self):
        res = " and ".join(map(lambda p: repr(p) or "", self.predicates))
        return res


    def __str__(self):
        res = " and ".join(map(lambda p: str(p), self.predicates))
        return res


    @contract(kwargs="dict(string:string)", returns="string")
    def label(self, **kwargs):

        label = ") and (".join(map(lambda p: p.label(**kwargs), self.predicates))

        return "(%s)" % label

    @contract(kwargs="dict(string:string)", returns="string")
    def smt_condition(self, **kwargs):

        smt_condition = ""

        for pred in self.predicates:
            if len(smt_condition) == 0:
                smt_condition = pred.smt_condition(**kwargs)
            else:
                smt_condition = "(and %s %s)" % (pred.smt_condition(**kwargs), smt_condition)

        return smt_condition


    @property
    @contract(returns="set(string)")
    def var_names(self):

        var_names = set([])

        for pred in self.predicates:
            var_names = var_names | pred.var_names

        return var_names
 

    @contract(var_names="None|list", suffix="None|string", returns="is_predicate")
    def primed(self, var_names=None, suffix="_1"):
        
        check("list(is_predicate)", self.predicates)
        primed_predicates = []

        for pred in self.predicates:
            primed_predicates.append(pred.primed(var_names=var_names, suffix=suffix))

        return And(*primed_predicates)

        


class BinaryPredicate(Predicate):

    __metaclass__ = abc.ABCMeta

    _smt_name = "..." # the name of the predicate in SMTlib
#    _smt_assert = "(assert ({name} {var} {value}))"
    _smt_condition = "({name} {var} {value})"
    _label = "{var} {name} {value}"

    def __init__(self, **ctx):
        assert "name" not in ctx, "A BinaryPredicate cannot specify a 'name' argument"
        ctx["name"] = self._smt_name
        super(BinaryPredicate, self).__init__(**ctx)
    

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




#UNKNOWN = Eq(var="1", value="1")


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
    @contract(datatype="is_data_type", predicates="list[N](is_predicate),N>0")
    def __init__(self, datatype, predicates, default=None):

        self.datatype = datatype
    
        default = default or predicates[0]

        self._default = default
        self.predicates = list(set(predicates))

    @property
    @contract(returns="list(is_data_type)")
    def datatypes(self):
        return [ self.datatype ]
    
    def __str__(self):
        return "(%s, %s)" % (str(self.datatype), str(self.predicates))

    def __repr__(self):
        return "(%s, %s)" % (repr(self.datatype), repr(self.predicates))

    @property   
    @contract(returns="int")
    def size(self):
        check("list(is_predicate)", self.predicates)
        return len(self.predicates)

    @property
    @contract(returns="is_predicate")
    def default(self):
        return self._default

    @property
    @contract(returns="string")
    def smt_predicate_abstraction(self):
        constraints = []

        ctx = {}
 
        check("list(is_predicate)", self.predicates)   
        for pred in self.predicates:

            assert len(pred.var_names) == 1, "Expected predicates with single variable. Got: %s" % pred
            for v in pred.var_names:
                var_name = v.strip("{}")
                ctx[var_name] = var_name


        for pred in self.predicates:
            constraints.append(pred.smt_condition(**ctx))

        assert len(ctx.keys()) == 1, "All predicates should use the same variable. Got: %s" % ctx
        type_name = self.name
        res = "(assert (forall ((%s %s)) (or %s)))" % (ctx.keys()[0], type_name, " ".join(constraints))

        return res
        

    @property
    @contract(returns="string")
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


new_contract_check_type("is_domain", Domain)


class CompareVariables(Domain):

    @contract(predicates="list(is_predicate)", datatypes="list(is_data_type)", default="None|is_predicate")
    def __init__(self, predicates, datatypes, default=None):
        
        datatype = DataTypeUnion(*datatypes)
    
        default = default or predicates[0] #UNKNOWN # predicates[0] 

        super(CompareVariables, self).__init__(predicates=predicates, datatype=datatype, default=default)

    @property
    @contract(returns="list(is_data_type)")
    def datatypes(self):
        return self.datatype.datatypes
    
    @property
    @contract(returns="string")
    def smt_predicate_abstraction(self):
        check("list(is_predicate)", self.predicates)

        constraints = []

        # prepare a dummy context (replace every variable with its own name)
        ctx = {}
        for pred in self.predicates: #values:
            assert isinstance(pred, Predicate)
#            print "pred: %s ; var names: %s" % (pred, pred.var_names)
            for v in pred.var_names:
                var_name = v.strip("{}")
                ctx[var_name] = var_name

        for pred in self.values:
            assert isinstance(pred, Predicate)
#            print "curr pred: %s" % pred
            constraints.append(pred.smt_condition(**ctx))

        # quantified variables
        qv = []
#        print "ctx = %s, predicates = %s" % (ctx, self.predicates)
        for idx,dt in enumerate(self.datatypes):
            var_name = "var_%s" % (idx+1)
            qv.append("(%s %s)" % (ctx[var_name], dt))

#        type_name = self.name
#        res = "(assert (forall ((x %s)) (or %s)))" % (type_name, " ".join(constraints))
        res = "(assert (forall (%s) (or %s)))" % (" ".join(qv), " ".join(constraints))

        return res

    @property
    def values(self):
        return self.predicates


 
class BoundedCollection(Domain):
    
    def __init__(self, max_val):
        super(BoundedCollection,self).__init__(Collection(), split_field_domain("size", split_numeric_domain([0,max_val], lt_min=False, gt_max=False)))


class Dummy(Domain):
    @contract(datatype=DataType)
    def __init__(self, datatype):
        super(Dummy,self).__init__(datatype, [EqItself({})])



class AbstractAttribute(object):

    @contract(variables="list(string)|string", domain="is_domain", is_local="list(bool)|bool")
    def __init__(self, variables, domain, is_local): #, is_local):
        """
        An AbstractAttribute has a name, a domain and is either a local attribute (i.e. declared within the
        analysed code) or is global (i.e. from another component or the environment)
        """

        if isinstance(variables, basestring):
            variables = [ variables ]

        self.variables = variables
        self.name = "__".join(self.variables)

        self.domain = domain
        values = domain.values
        initial = []

        if is_local:
            initial = [ domain.default ]
        else:
            initial = domain.values

        self.is_local = is_local

#        self.initial = self.get_initial(is_local)
    
        assert hasattr(values, "__iter__"), "Expected iterable. Passed '%s'" % values

        self.enc_values = {}
        self.rev_domain = {}

        # encode the passed values to form an integer domain; at the same
        # time keep track of the mapping between domain values and abstract value
        for (pos,item) in enumerate(values):
            self.enc_values[pos] = item

            if item in self.rev_domain:
                raise Exception("You passed a list of values containing twice '%s'. Duplicates are not allowed." % item)

            self.rev_domain[item] = pos

        for curr in initial:
            assert curr in values #or curr == UNKNOWN, "Expected value in '%s' or UNKNOWN. Passed: '%s'" % (",".join(map(lambda v: str(v), values)), curr)

        # store the encoded values of the passed initial values
        self.initial = map(lambda val: self.rev_domain[val], initial)

    @property
    def datatypes(self):
        return self.domain.datatypes
    
    @property
    @contract(returns="list(int)")
    def encoded_values(self):
        return sorted(self.enc_values.keys())

    @contract(returns=int)
    def encoded_value(self, value):
        if value not in self.rev_domain:
            raise ValueError("The passed value ('%s') is not one of the allowed ones . Accepted values: %s" % (value, ",".join(self.values)))
    
        return self.rev_domain[value]

    @contract(self="type(t)",returns="type(w),t=w")
    def primed(self):       
#        primed_name = "%s_1" % self.name
        primed_variables = map(lambda v: "%s_1" % v, self.variables)
        new_attr = AbstractAttribute(primed_variables, self.domain, self.is_local)

        log.debug("(%s)' => %s" % (self, new_attr))
        return new_attr


    @property
    def values(self):
        return sorted(self.rev_domain.keys())


    def value(self, encoding):
        if encoding not in self.enc_values:
            raise ValueError("The passed encoding ('%s') is not one of the allowed ones. Accepted encodings: %s" % (encoding, ",".join(self.encoded_values)))

        return self.enc_values[encoding]

    
    @property
    @contract(returns="None|string")
    def smt_declaration(self):
        res = None
        if self.domain:
#            assert isinstance(self.domain, Domain)
            check("is_domain", self.domain)
            res = self.domain.smt_declaration

#            # add constraints by the domain predicates for this attribute
#            res = res + "\n" + self.domain.smt_predicate_abstraction 

        return res


    @property
    @contract(returns="string")
    def smt_type_name(self):
        res = None
        if self.domain:
#            assert isinstance(self.domain, Domain)
            check("is_domain", self.domain)
            res = self.domain.name

        return res

    def __str__(self):
        return "%s : %s" % (self.name, self.domain)

    def __repr__(self):
        return str(self)

new_contract_check_type("is_abstract_attribute", AbstractAttribute)

@new_contract
def is_configuration(pred):

    if not isinstance(pred, tuple):
        raise ValueError("A configuration is a tuple of int")
    
    for curr in pred:
        if not isinstance(curr, int):
            raise ValueError("A configuration can only contain int")

@new_contract
def is_configuration_repr(text):
    if not isinstance(text,basestring):
        raise ValueError("A predicate representation must be of type 'basestring'")
     # remove enclosing parenthesis
    text = text.strip("()")
    
    for item in text.strip().split(","):
        try:
            int(item) # force casting of string to int; if not possible, raise an exception
        except ValueError:
            raise ValueError("A predicate can only contain predicate codes (of type int)")
 
    return True  


class StateSpace(object):

    def __init__(self):
        self._attributes = {}
        self._configurations = [] #set([])
        self._initial_configurations = [] #set([])

    @contract(attribute="is_abstract_attribute")
    def add_attribute(self, attribute):
        """
        Add an instance of AbstractAttribute to the state spacae.
        """
#        assert isinstance(attribute, AbstractAttribute)
        self._attributes[attribute.name] = attribute
        # invalidate cached configurations
        self._configurations = [] #set([])
        self._initial_configurations = [] #set([])

    @contract(name="string", returns="is_abstract_attribute")
    def get_attribute(self, name):
        """
        Return the AbstractAttribute with the given name.
        """

        if name not in self._attributes:
            raise ValueError("The attribute '%s' is not present" % name)

        res = self._attributes[name]

#        assert isinstance(res, AbstractAttribute)
        return res

    @property
    @contract(returns="list(is_abstract_attribute)")
    def attributes(self):
        """
        Return the attributes that make the state space, in alphabetical order.
        """
        sort_by_name = lambda att: att.name

        return sorted(self._attributes.values(), key=sort_by_name)
 
    @property   
    @contract(returns="list(is_configuration)")
    def enumerate(self):
        """
        A configuration is a tuple describing the product of all attribute values. The i-th item of the
        tuple is the value of the i-th attribute. Attributes are considered in the alphabetical order.
        """
        if not self._configurations:
            # enumerate configurations and store for later use
            self._configurations = [()] # start with a single empty configuration
            for attr in self.attributes:
                
                self._configurations = self._do_multiply(self._configurations, attr)

        return self._configurations

    @property
    @contract(returns="list(is_configuration)")
    def initial_configurations(self):
        """
        Filter the enumerated configurations and return only the initial ones, i.e. where
        all the attributes have the initial value.
        """
        if not self._initial_configurations:
            self._initial_configurations = [] #set([])

            for conf in self.enumerate:
                check("is_configuration", conf)
#                assert isinstance(conf, tuple)

                # the configuration is considerd initial *if-and-only-if* all the attributes
                # have the initial value
                is_initial = True
                for (attr,val) in zip(self.attributes, conf):
                    assert isinstance(attr, AbstractAttribute)
                    if val not in attr.initial:
                        is_initial = False
                        break

                if is_initial:
                    # cache only initial configurations
                    self._initial_configurations.append(conf)
     
        assert isinstance(self._initial_configurations, list)       
        return self._initial_configurations


    @property
    def size(self):
        """
        Return the number of configurations that constitute the state space. This equals the
        product of the sizes of the domains of the attributes in the state space itself.
        """
        return len(self.enumerate)

    @contract(configurations="list(is_configuration)", attribute="is_abstract_attribute", returns="list(is_configuration)")
    def _do_multiply(self, configurations, attribute):
        """     
        Take as input a tuple describing a partial configuration, and produce a list of tuples
        obtained by combining the passed tuple with each possible value of the domain of the
        passed attribute. 

        By calling this method for each attribute of the state space, starting with the empty 
        tuple, and collecting the configuration tuples produced by the last call, one obtains 
        all the possible configurations of attributes in the state space.
        """

#        assert isinstance(configurations, list)
#        assert isinstance(attribute, AbstractAttribute)

        new_configurations = []
        for conf in configurations:

            for val in attribute.encoded_values:
                new_tuple = conf + (val,)
                new_configurations.append(new_tuple)

        return new_configurations

    @contract(configuration="is_configuration", returns="tuple")
    def value(self, configuration):
        """
        Given a particular configuration of values of the state space attributes, explicit the value of 
        each attribute making the configuration.

        Use this method to **decode** the attributes values from a given configuration.
        """

#        assert isinstance(configuration, tuple)

        check("list(is_abstract_attribute)", self.attributes)
        attributes = self.attributes

        if len(configuration) != len(attributes):
            raise Exception("Statespace has %s attributes, thus it can decode configurations with %s elements. Got configuration with %s elements" % (len(attributes), len(attributes), len(configuration)))

        decoded = ()
        for (att,conf_val) in zip(attributes, configuration):
            decoded += (att.value(conf_val), )

        return decoded

    @contract(values="tuple", returns="is_configuration")
    def configuration(self, values):
        """
        Given a tuple of values (the i-th item is interpreted as the value of the i-th attribute),
        produce a configuration, i.e. a tuple of encoded values.

        Use this method to **encode** the attribute values into a configuration
        """
        check("list(is_abstract_attribute)", self.attributes)
#        attributes = self.attributes

        if len(values) != len(self.attributes):
            raise Exception("Statespace has %s attributes, thus it can encode lists of values with %s elements. Got list of values %s elements" % (len(self.attributes), len(self.attributes), len(values)))

        conf = ()
        for (att,val) in zip(self.attributes, values):
            check("is_abstract_attribute", att)
            conf += (att.encoded_value(val), )

        return conf




class DomainProduct(CompareVariables):

    @contract(var_domains="dict(str:is_domain)")
    def __init__(self, **var_domains):
        all_predicates = []
        var_datatypes = {}

        for var,dom in var_domains.iteritems():
            all_predicates.append(dom.predicates)
            var_datatypes[var] = dom.datatype

        predicates = self._get_product_predicates(all_predicates)
        super(DomainProduct, self).__init__(predicates, *var_datatypes)

    @contract(all_predicates="list(list(is_predicate))", returns="list(is_predicate)")
    def _get_product_predicates(self, all_predicates):

        predicates = []
        for curr_tuple in itertools.product(all_predicates):
            check("tuple", curr_tuple)
            predicates.append(And(*curr_tuple))
        return predicates
    
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


@contract(values="list(*)", returns="string")
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

