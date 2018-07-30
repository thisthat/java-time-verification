import re
import copy
import abc
from contracts import contract, new_contract, check
import logging
import  itertools

#from java2ta.abstraction.shortcuts import smt_declare_rec_datatype
from java2ta.abstraction import formulas
from java2ta.smt.models import SMTSolver
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
 
    @contract(name="string", smt_declaration="string|None", smt_var_axioms="list(string)")
    def __init__(self, name, smt_declaration=None, smt_var_axioms=[]):
        """
        smt_axioms should contain lists of SMT assertions with a reference to 
        {var}, the variable of the current type. The {var} placeholder will be
        replaced with the variable name, for each variable of the current type.
        """
        self._name = name
        self._smt_declaration = smt_declaration or ""
#        self._smt_var_axioms = list(smt_var_axioms)
        self._smt_var_axioms = []

        for ax in smt_var_axioms:
            self.add_smt_var_axiom(ax)

    def __str__(self):
        return self.name

    def __repr__(self):
        curr_repr = self.smt_declaration or self.name

        return curr_repr

    @property
    @contract(returns="string")
    def smt_declaration(self):
#        declaration = self._smt_declaration
#
#        if self._smt_var_axioms:
#            declaration = declaration + "\n" + "\n".join(self._smt_var_axioms)
#
#        return declaration or ""
        return self._smt_declaration or ""

#    @property
    @contract(var_name="string", returns="string")
    def smt_var_axioms(self, var_name):
        """
        Replace every occurrence of {var} with the variable name
        """
        var_axioms = map(lambda a: a.replace("{var}", var_name), self._smt_var_axioms)

        return "\n".join(var_axioms)

    def set_smt_declaration(self, smt_declaration):
        self._smt_declaration = smt_declaration

    @contract(axiom="string")
    def add_smt_var_axiom(self, axiom):
        check("list(string)", self._smt_var_axioms)

        if "{var}" not in axiom:
            raise ValueError("An SMT axiom should contain a placeholder {var} in it")
        self._smt_var_axioms.append(axiom)

    @property
    def name(self):
        return self._name

    def __str__(self):
        return self._name


new_contract_check_type("is_data_type", DataType)



class Z3String(DataType):
    """
    A String is a pair with:
    - value (abstracted to an integer value)
    - size (an integer value)
    """
    def __init__(self):
        super(Z3String,self).__init__(name="String") 


class AbsString(DataType):

    def __init__(self):
        from java2ta.abstraction.shortcuts import smt_declare_rec_datatype

        smt_declaration = smt_declare_rec_datatype("AbsString", {"size":"Int","value":"Int"}) 
        super(AbsString, self).__init__("AbsString", smt_declaration=smt_declaration, smt_var_axioms=["(assert (>= (size {var}) 0))"])

    @contract(var="string", returns="string")
    def smt_var_axioms(self, var):
        assertions = super(AbsString, self).smt_var_axioms(var)

        for lit_code in SymbolTable.get_literals():
            value = SymbolTable.decode_literal(lit_code)
            if SymbolTable.get_literal_type(value) == "String":
                size = len(value) - 2
                curr = "(assert (implies (= (value %(var)s) %(code)s) (= (size %(var)s) %(size)s)))" % { "var":var, "code":lit_code, "size":size }
                assertions = "%s\n%s" % (assertions, curr)

        return assertions


class Object(DataType):

    def __init__(self):
        from java2ta.abstraction.shortcuts import smt_declare_rec_datatype

        smt_declaration = "%s\n(declare-const null Object)" % smt_declare_rec_datatype("Object", {"isnull":"Bool",}) 
        super(Object, self).__init__("Object", smt_declaration=smt_declaration)


class Iterator(DataType):
    
    def __init__(self):    
        from java2ta.abstraction.shortcuts import smt_declare_rec_datatype

        smt_declaration = smt_declare_rec_datatype("Iterator", { "n_items":"Int", "n_visited":"Int",})

        # below we add the following constraints, for any X of type Iterator:
        # (n_items X) >= (n_visited X) >= 0
        super(Iterator, self).__init__("Iterator", smt_declaration=smt_declaration, smt_var_axioms=["(assert (>= (n_visited {var}) 0))", "(assert (>= (n_items {var}) (n_visited {var})))", ])


class Collection(DataType):
    
    def __init__(self): 
        from java2ta.abstraction.shortcuts import smt_declare_rec_datatype
        super(Collection, self).__init__("Collection", smt_declaration=smt_declare_rec_datatype("Collection", {"size":"Int"}))



class DataTypeUnion(DataType):

    @contract(datatypes="tuple")
    def __init__(self, *datatypes):
        self.datatypes = list(datatypes)

        check("list(is_data_type)", self.datatypes)

        name = "" #"_".join(map(lambda dt: dt.name, datatypes))
        union_smt_var_axioms = []
        smt_declaration = "" #"\n".join(map(lambda dt: dt.smt_declaration, datatypes)) # TODO this should be replaced by a tuple
 
        # TODO here we introduce duplicated datatypes declarations       
        for dt in datatypes:
            if len(name) == 0:
                name = dt.name
            else:
                name = "%s_%s" % (name, dt.name)

            union_smt_var_axioms.extend(dt._smt_var_axioms)

            if len(smt_declaration) == 0:
                smt_declaration = dt.smt_declaration
            else:
                smt_declaration = "%s\n%s".strip() % (smt_declaration, dt.smt_declaration)

        super(DataTypeUnion, self).__init__(name, smt_declaration, union_smt_var_axioms)


class Predicate(object):

    __metaclass__ = abc.ABCMeta

    _smt_name = "..." # the name of the predicate in SMTlib
    _smt_condition = "({name} {arguments})"
    _label = "{name}({arguments})"

    def __init__(self, *arguments):
        self._arguments = arguments
        label_arguments = ",".join(map(lambda v: v.label() if isinstance(v, Predicate) else str(v), arguments))
        self._label = partial_format(self._label, { "name": self._smt_name, "arguments": label_arguments })
    
        smt_arguments = " ".join(map(lambda v: v.smt_condition() if isinstance(v, Predicate) else str(v), arguments))
        self._smt_condition = partial_format(self._smt_condition, { "name": self._smt_name, "arguments": smt_arguments })

    def __eq__(self, other):
        return not predicates_differ(self, other)

    def __ne__(self, other):
        return predicates_differ(self, other)


    def __hash__(self):
        return hash(str(self))

    def copy(self):
        import copy
        return copy.deepcopy(self)

    @contract(returns="string")
    def label(self, **kwargs):
        """
        When invoking this method, the user should pass through **kwargs all the 
        missing parameters appearing in the predicate label.

        If some of the parameters are still missing, an exception is raised. 
        """
        ctx = dict(**kwargs)
        return partial_format(self._label, ctx)

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
        ctx = dict(**kwargs)
        return partial_format(self._smt_condition, ctx)
    
    def __repr__(self):
        return self._label
    
    def __str__(self):
        return self._label


    def wrap_fields(self, **var_fields):
        ctx_smt = {}
        ctx_label = {}

        for var,field in var_fields.iteritems():
            ctx_smt[var] = "(%s {%s})" % (field,var)
            ctx_label[var] = "{%s}.%s" % (var,field)

        self._smt_condition = partial_format(self._smt_condition, ctx_smt)
        self._label = partial_format(self._label, ctx_label)
    
        return self

    @property
    @contract(returns="set(string)")
    def var_names(self):
        var_names = re.findall("\{[a-zA-Z\_0-9]+\}", self._smt_condition) # TODO this is a bit hack-ish, find a better way to handle it

        var_names_stripped = map(lambda v: v.strip("{}"), var_names)
        return set(var_names_stripped)

    @contract(rename="dict(string:string)")
    def rename(self, **rename): #var_old, var_new):

        old_vars = self.var_names

        for var_old, var_new in rename.iteritems():
#            var_old = "{%s}" % var_old.strip("{}")            
#            var_new = "{%s}" % var_new.strip("{}")

            if var_old not in old_vars:
                raise ValueError("Variable to be renamed (%s) not present in variables (%s)" % (var_old, old_vars))

            self._smt_condition = self._smt_condition.replace(var_old, var_new)
            self._label = self._label.replace(var_old, var_new)

            assert var_old not in self.var_names, "%s vs %s" % (var_old, self.var_names)

        return self

    @contract(var_names="None|list(string)", suffix="string")
    def primed(self, var_names=None, suffix="_1"):
        """
        Return a copy of itself, but with primed variables.
        var_names = the set of variable names to be primed (if none is specified all will be primed)
        suffix = the suffix to add to represent a primed variable
        """
        new_label = self._label
        new_condition = self._smt_condition # self._smt_assert

        if not var_names:
            var_names = self.var_names
        
        for var in var_names:
            var = var.strip("{}") # remove initial and trailing curly brackets, if present

            assert isinstance(var, basestring)
            new_condition = new_condition.replace("{%s}" % var, "{%s}%s" % (var, suffix))
            new_label = new_label.replace("{%s}" % var, "{%s}%s" % (var, suffix))

        new = self.copy()
        new._smt_condition = new_condition
        new._label = new_label

        return new





new_contract_check_type("is_predicate", Predicate)

@contract(returns="bool")
def predicates_differ(left, right):
    """
    Return True iff the left and right predicates differ. Note that we should not assume that left or
    right are Predicate's. If they are not instances of Predicate, then they differ.
    """

    differ = False

    if (left is None and right is not None) or (left is not None and right is None):
        differ = True
    elif not isinstance(right, Predicate) or not isinstance(left, Predicate) or left.__class__ != right.__class__:
        differ = True
    else:
        for key,value_left in left.__dict__.iteritems():
            value_right = getattr(right, key)
            if value_left != value_right:
                differ = True
                break

    return differ

##class And(Predicate):
##
##        if len(predicates) < 2:
##            raise ValueError("The AND predicate requires at least two sub-predicates")
##
##        self.predicates = predicates
##
##        ctx = {}
##
##        # the following iteration leaves in ctx only the common pairs (key,value) of the
##        # respective ctx's
##        for pred in predicates:
##
##            if not isinstance(pred, Predicate):
##                raise ValueError("Arguments of predicate And should be instances of Predicate. Passed: %s (%s)" % (type(pred), pred))
##
##            for (key, val) in pred.ctx.iteritems():
##                if key not in ctx:
##                    ctx[key] = val
##                elif ctx[key] != val:
##                    del ctx[key]
##
##        self.ctx = ctx
##
##
##    def __repr__(self):
##        res = " and ".join(map(lambda p: repr(p) or "", self.predicates))
##        return res
##
##
##    def __str__(self):
##        res = " and ".join(map(lambda p: str(p), self.predicates))
##        return res
##
##
##    @contract(kwargs="dict(string:string)", returns="string")
##    def label(self, **kwargs):
##
##        label = ") and (".join(map(lambda p: p.label(**kwargs), self.predicates))
##
##        return "(%s)" % label
##
##    @contract(kwargs="dict(string:string)", returns="string")
##    def smt_condition(self, **kwargs):
##
##        smt_condition = ""
##
##        for pred in self.predicates:
##            if len(smt_condition) == 0:
##                smt_condition = pred.smt_condition(**kwargs)
##            else:
##                smt_condition = "(and %s %s)" % (pred.smt_condition(**kwargs), smt_condition)
##
##        return smt_condition
##
##
##    @property
##    @contract(returns="set(string)")
##    def var_names(self):
##
##        var_names = set([])
##
##        for pred in self.predicates:
##            var_names = var_names | pred.var_names
##
##        return var_names
## 
##
##    @contract(var_names="None|list", suffix="None|string", returns="is_predicate")
##    def primed(self, var_names=None, suffix="_1"):
##        
###        check("list(is_predicate)", self.predicates)
##        check("tuple", self.predicates)
##        primed_predicates = []
##
##        for pred in self.predicates:
##            primed_predicates.append(pred.primed(var_names=var_names, suffix=suffix))
##
##        return And(*primed_predicates)
##

class BinaryPredicate(Predicate):

    __metaclass__ = abc.ABCMeta

    def __init__(self, lhs=None, rhs=None):
        lhs = lhs if lhs is not None else "{lhs}"
        rhs = rhs if rhs is not None else "{rhs}"
        arguments = [ lhs, rhs ]

        super(BinaryPredicate, self).__init__(*arguments)
    

class Or(Predicate):
    _smt_name = "or"

    def __init__(self, *arguments):
        if len(arguments) < 2:
            raise ValueError("The OR predicate requires at least two sub-predicates. Received: %s" % arguments)

        super(Or, self).__init__(*arguments)

class And(Predicate):
    _smt_name = "and"

    def __init__(self, *arguments):
        if len(arguments) < 2:
            raise ValueError("The AND predicate requires at least two sub-predicates. Received: %s" % arguments)

        super(And, self).__init__(*arguments)

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
    _label = "!=({arguments})"


#class Between(Predicate):    
class Between(And):
#    _smt_assert = "(assert (and (< {min} {var}) (< {var} {max})))"
#    _smt_condition = "(and (< {min} {var}) (< {var} {max}))"
#    _label = "in({arguments})"

    def __init__(self, var=None, min=None, max=None):
        var = var if var is not None else "{var}"
        min = min if min is not None else "{min}"
        max = max if max is not None else "{max}"
#        arguments = ( var, min, max )
        arguments = ( LT(lhs=min, rhs=var), LT(lhs=var, rhs=max) )
#        print "arguments: %s" % (arguments,)
        super(Between, self).__init__(*arguments)


class EqItself(Predicate):
#    _smt_assert = "(assert (= {var} {var}))"
    _smt_condition = "(= {var} {var})"
    _label = "{var} = {var}"

#    def __init__(self, **ctx):
#        super(EqItself,self).__init__(**ctx)


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
    @contract(datatype="is_data_type", predicates="list[N](is_predicate),N>0", default="is_predicate|None")
    def __init__(self, datatype, predicates, default=None):

        self.datatype = datatype
 
        if default:
            # store the predicate equivalent to the one
            # specified as default (this is important
            # because we want to ensure that this.default
            # is one of the instances in this.predicates) 
            found_default = None

            for pred in predicates:
                if pred == default: 
                    found_default = pred
                    break

            if found_default:
                default = found_default
            else:
                raise ValueError("The default domain value should be one of the passed predicates")
        else:
            default = predicates[0]

        self._default = default
        self.predicates = predicates #list(set(predicates))

    def primed(self):
        res = copy.deepcopy(self)

        ctx_rename = {}
        for v in self.variables:
            ctx_rename[v] = "%s_1" % v
    
        for p in res.predicates:
            p.rename(**ctx_rename)           

        return res


    @property
    @contract(returns="set(string)")
    def variables(self):
        res = set([])
        for p in self.predicates:
            res = res | p.var_names
        return res

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
    @contract(returns="list(string)")
    def smt_declarations(self):
        res = []

        if self.datatype:
            res = [ self.datatype.smt_declaration.strip() ]

        return res
 

    @property
    @contract(returns="list(string)")
    def smt_type_names(self):
        res = []

        if self.datatype:
            res = [ self.datatype.name ]

        return res

  
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

    @contract(datatypes="list(is_data_type)", predicates="list(is_predicate)", default="None|is_predicate")
    def __init__(self, datatypes, predicates, default=None):


        datatype = DataTypeUnion(*datatypes)
    
        default = default or predicates[0] #UNKNOWN # predicates[0] 

        super(CompareVariables, self).__init__(predicates=predicates, datatype=datatype, default=default)

    @property
    @contract(returns="list(is_data_type)")
    def datatypes(self):
        return self.datatype.datatypes
    

    @property
    def values(self):
        return self.predicates


    @property
    @contract(returns="list(string)")
    def smt_type_names(self):
        res = []
        for dt in self.datatypes:
            res.append(dt.name)

        return res

    @property
    @contract(returns="list(string)")
    def smt_declarations(self):

        res = []
        for dt in self.datatypes:
            res.append(dt.smt_declaration)

        return res

 
class BoundedCollection(Domain):
    
    def __init__(self, max_val):
        super(BoundedCollection,self).__init__(Collection(), split_field_domain(split_numeric_domain([0,max_val], lt_min=False, gt_max=False),var="size"))


class Dummy(Domain):
    @contract(datatype=DataType)
    def __init__(self, datatype):
        super(Dummy,self).__init__(datatype=datatype, predicates=[EqItself()]) #{})])



class AbstractAttribute(object):

    @contract(variables="list(string)|string", domain="is_domain", is_local="list(bool)|bool")
    def __init__(self, variables, domain, is_local): #, is_local):
        """
        An AbstractAttribute has a name, a domain and is either a local attribute (i.e. declared within the
        analysed code) or is global (i.e. from another component or the environment)
        """

        if isinstance(variables, basestring):
            variables = [ variables ]

        if set(variables) != domain.variables:
            raise ValueError("The variables of the AbstractAttribute should match those of the domain predicates. Passed variables: %s. Domain predicate variables: %s" % (variables, domain.variables))

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
        if not self.domain:
            raise ValueError("You must set a domain first")

        return self.domain.datatypes
 
    @property
    def predicates(self):
        if not self.domain:
            raise ValueError("You must set a domain first")

        return self.domain.predicates
   
    @property
    @contract(returns="list(int)")
    def encoded_values(self):
        return sorted(self.enc_values.keys())

    @contract(returns=int)
    def encoded_value(self, value):
        if value not in self.rev_domain:
            raise ValueError("The passed value ('%s') is not one of the allowed ones . Accepted values: %s" % (value, ",".join(map(lambda v: str(v), self.values))))
    
        return self.rev_domain[value]

    @contract(self="type(t)",returns="type(w),t=w")
    def primed(self):       
#        primed_name = "%s_1" % self.name
        primed_variables = map(lambda v: "%s_1" % v, self.variables)
        primed_domain = self.domain.primed()
        log.debug("Domain: %s => primed domain: %s" % (self.domain, primed_domain))
        new_attr = AbstractAttribute(primed_variables, primed_domain, self.is_local)

        log.debug("(%s)' => %s" % (self, new_attr))
        return new_attr


    @property
    @contract(returns="list(is_predicate)")
    def values(self):
#        return sorted(self.rev_domain.keys())  

        return self.domain.values


    @contract(encoding="int",returns="is_predicate")
    def value(self, encoding):
        if encoding not in self.enc_values:
            raise ValueError("The passed encoding ('%s') is not one of the allowed ones. Accepted encodings: %s" % (encoding, ",".join(map(str,self.encoded_values))))

        return self.enc_values[encoding]

    
    @property
    @contract(returns="list(string)") #None|string")
    def smt_declarations(self):
        res = [] #None
        if self.domain:
#            assert isinstance(self.domain, Domain)
            check("is_domain", self.domain)
            res = self.domain.smt_declarations

#            # add constraints by the domain predicates for this attribute
#            res = res + "\n" + self.domain.smt_predicate_abstraction 

        return res


    @property
    @contract(returns="list(string)") #string")
    def smt_type_names(self):
        res = [] # None
        if self.domain:
#            assert isinstance(self.domain, Domain)
            check("is_domain", self.domain)
            res = self.domain.smt_type_names #name

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

    @contract(returns="list(is_abstract_attribute)")
    def get_attributes(self, var_name=None):
        """
        Return the attributes in the state space that contain the specified variable.
        If no variable name is given, return all the attributes of the state space.
        """
 
        res = []
        if var_name:
            for attr in self._attributes:
                if var_name in attr.variables:
                    res.append(attr)
        else:
            res = self._attributes.values()   

        check("list(is_abstract_attribute)", res)

        sort_by_name = lambda att: att.name
        return sorted(res, key=sort_by_name) 
 

    @property
    @contract(returns="list(is_abstract_attribute)")
    def attributes(self):
        """
        Return the attributes that make the state space, in alphabetical order.
        """
#        sort_by_name = lambda att: att.name

#        return sorted(self._attributes.values(), key=sort_by_name) 
        return self.get_attributes()

    @property
    @contract(returns="set(string)")
    def variables(self):
        assert isinstance(self._attributes, dict)

        res = set([])
        for attr in self._attributes.values():
            res = res | set(attr.variables)

        return res
     
       
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
        check("list(is_abstract_attribute)", self.attributes)

        if len(configuration) != len(self.attributes):
            raise Exception("Statespace has %s attributes, thus it can decode configurations with %s elements. Got configuration with %s elements" % (len(self.attributes), len(self.attributes), len(configuration)))

        decoded = ()
        for (att,conf_val) in zip(self.attributes, configuration):
            decoded += (att.value(conf_val), )

        log.debug("Decode configuration: %s vs attributes %s. Result: %s" % (configuration, self.attributes, decoded))

        return decoded

    @contract(values="tuple|list", returns="is_configuration")
    def configuration(self, values):
        """
        Given a tuple (or list) of values (the i-th item is interpreted as the value of the i-th attribute),
        produce a configuration, i.e. a tuple of encoded values.

        Use this method to **encode** the attribute values into a configuration
        """
        check("list(is_abstract_attribute)", self.attributes)

        if len(values) != len(self.attributes):
            raise Exception("Statespace has %s attributes, thus it can encode lists of values with %s elements. Got list of values %s elements" % (len(self.attributes), len(self.attributes), len(values)))

        conf = ()
        for (att,val) in zip(self.attributes, values):
            check("is_abstract_attribute", att)
            conf += (att.encoded_value(val), )

        return conf

new_contract_check_type("is_state_space", StateSpace)


##class DomainProduct(CompareVariables):
##
##    @contract(var_domains="dict(str:is_domain)")
##    def __init__(self, **var_domains):
##        all_predicates = []
##        var_datatypes = {}
##
##        for var,dom in var_domains.iteritems():
##            all_predicates.append(dom.predicates)
##            var_datatypes[var] = dom.datatype
##
##        predicates = self._get_product_predicates(all_predicates)
##        super(DomainProduct, self).__init__(predicates, *var_datatypes)
##
##    @contract(all_predicates="list(list(is_predicate))", returns="list(is_predicate)")
##    def _get_product_predicates(self, all_predicates):
##
##        predicates = []
##        for curr_tuple in itertools.product(all_predicates):
##            check("tuple", curr_tuple)
##            predicates.append(And(*curr_tuple))
##        return predicates
## 

class Integer(DataType):
    def __init__(self):
        super(Integer,self).__init__(name="Int")


#class Natural(DataType):
class Natural(Integer):
    """
    This tries to implement the Natural numbers as a subclass of the
    Integer numbers, adding a constraint.
    This has not been tested with SMT. #TODO
    """
    def __init__(self):
#        super(Natural, self).__init__(name="Nat", smt_declaration="(declare-datatypes () ((Nat (mk-natural (val Int)))))", smt_axioms=["(assert (forall ((x Nat)) (>= (val x) 0)))"])

        super(Natural, self).__init__()

        self.add_smt_var_axiom("(assert (>= {var} 0))")

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


class SymbolTable(object):

    RE_LITERAL = "^(null)$|^(true|false)$|^(\-?[0-9]+)$|^(\-?[0-9]+(?:\.[0-9]+)?)$|^(\"\w+\"|'\w+')$" # was: "\w+"

    LITERALS = {}
    REV_LITERALS = {}

    _COUNTER = 1


    @staticmethod
    def reset():
        SymbolTable.LITERALS = {}
        SymbolTable.REV_LITERALS = {}
        SymbolTable._COUNTER = 1

    @staticmethod
    @contract(value="string", returns="tuple(string,string)")
    def add_literal(value):

        lit_type = SymbolTable.get_literal_type(value)
    
        if lit_type == "object":
            if value == "null":
                lit_code = value
            else:
                raise ValueError("For type 'object' only the 'null' literal is supported. Passed: %s" % value)
        elif lit_type == "String":
            # normalize the string literals to use ' as delimiter
            value = "'%s'" % (value[1:-1],)
            lit_code = SymbolTable.LITERALS.get(value, None)

            if lit_code is None:
                lit_code = str(SymbolTable._COUNTER)
                SymbolTable.LITERALS[value] = lit_code 
                SymbolTable.REV_LITERALS[lit_code] = value

                SymbolTable._COUNTER = SymbolTable._COUNTER + 1
                log.debug("Update symbol table: %s => %s" % (value, SymbolTable.LITERALS[value]))

            lit_code = SymbolTable.LITERALS[value]
        else:
            # other literal (bool, int, float): don't add to the SymbolTable
            lit_code = value

        return (lit_code, lit_type)


    @staticmethod
    @contract(literal="string", returns="string")
    def get_literal_type(literal):
        m = re.match(SymbolTable.RE_LITERAL, literal)

        if m:
            g = m.groups()
            if g[0] is not None:
                return "object"
            elif g[1] is not None:
                return "bool"
            elif g[2] is not None:
                return "int"
            elif g[3] is not None:
                return "float"
            elif g[4] is not None:
                return "String"
        
        raise ValueError("Cannot recognize literal: %s" % literal)

    
    @staticmethod
    @contract(returns="list(string)")
    def get_literals():
        literals = SymbolTable.LITERALS.values()

        return literals


    @staticmethod
    @contract(value="string", returns="string")
    def get_literal(value):
        code = SymbolTable.LITERALS.get(value, None)

        if code is None:
            raise ValueError("Literal '%s' does not exist")

        return code

    @staticmethod
    @contract(code="string", returns="string")
    def decode_literal(code):

        lit = SymbolTable.REV_LITERALS.get(code, None)

        if lit is None:
            raise ValueError("No literal has cthe passed code: %s" % code)
    
        return lit


new_contract("is_ast", lambda s: True) # TODO implement this check

class LeftLinearParser(object):

    RE_VAR = "{\w+}" #r"({\w+})\s?(.*)"
    RE_NODE_NAME = "[^\s(){}]+" # r"([^\s(){}]+)\s?(.*)"
    RE_LITERAL = None # to be defined in your parser
    SYMBOL_TO_CLASS = {} # a mapping from the first matched symbol to the class to be instantiated


    @contract(text="string")
    def parse(self, text):
        ast, remaining = self._text_to_ast(text.strip())
        if len(remaining) > 0:
            raise ValueError("Cannot parse text: %s" % remaining)    

        pred = self._ast_to_object(ast)
        return pred

    @contract(token="string", text="string", returns="tuple(string,string)")
    def _match(self, token, text):
        reg_exp = r"(%s)\s?(.*)" % token
        g = re.match(reg_exp, text)

        if not g:
            raise ValueError("Cannot parse text. Expected: %s. Passed: %s" % (reg_exp, text))

        groups = g.groups()

        token, remaining = groups[0], groups[1] if len(groups) > 1 and groups[1] is not None else ""

        return token, remaining

    @contract(ast="is_ast")
    def _ast_to_object(self, ast): 
        raise ValueError("You must override this")
 
    @contract(text="string", returns="tuple(is_ast, string)")
    def _text_to_ast(self, text):
        """
        Given some text, it returns a tuple representing its abstract
        syntax tree, and a string containing the non-parsed text. 
        """
        raise ValueError("You must override this")

    @contract(symbol="string")
    def _get_class(self, symbol):
        found_class = self.SYMBOL_TO_CLASS.get(symbol, None)

        #if not found_class:
            #logger.warning("No class found when parsing symbol: %s" % symbol)

        return found_class

    @contract(ast="tuple(string, list)|string", returns="string")
    def _walk(self, ast):
        # walk the ast and serialize it as a string

        res = None
        if isinstance(ast, basestring):
            res = ast
        elif isinstance(ast, tuple):
            arguments = []
            for a in ast[1]:
                arguments.append(self._walk(a))
    
            res = "(%s %s)" % (ast[0], " ".join(arguments))
        else:   
            raise ValueError("The walk procedure does not expect this format")

        return res

    def _is_node(self, ast):
        return isinstance(ast, tuple) and len(ast) == 2 and isinstance(ast[1], list) and re.match(self.RE_NODE_NAME, ast[0])

    def _is_var(self, ast):
        return isinstance(ast, basestring) and re.match(self.RE_VAR, ast)

    def _is_literal(self, ast):
        return isinstance(ast, basestring) and re.match(self.RE_LITERAL, ast)


class PredicateParser(LeftLinearParser):

    RE_LITERAL = "null|true|false|\-?[0-9]+(?:\.[0-9]+)?|\"\w+\"|'\w+'"
    SYMBOL_TO_CLASS = {
        ">"     : GT,
        ">="    : GTE,
        "<"     : LT,
        "<="    : LTE,
        "="     : Eq,
        "!="    : NotEq,
        "and"   : And,
        "or"    : Or, # to be added
#        "not"   : Not, # to be added
#       "->"    : Imply, # to be added
#       "<->"   : Iff, # to be added
    }

    VAR_DELIM_BEGIN = "{"
    VAR_DELIM_END = "}"

    @contract(text="string", returns="tuple(is_ast, string)")
    def _text_to_ast(self, text):
        """
        Given some text, it returns a tuple representing its abstract
        syntax tree, and a string containing the non-parsed text. 
        """

        remaining = ""

        if text[0] == "(":
            # we are in a new nested node; the first component is the
            # node name, which cannot contain neither spaces nor 
            # parenthesis, nor curly braces
            name, remaining = self._match(LeftLinearParser.RE_NODE_NAME, text[1:])

            arguments = []
            while len(remaining) > 0 and not remaining.startswith(")"):
                ast, remaining = self._text_to_ast(remaining.strip())
                arguments.append(ast)

            if len(remaining) > 0:
                # then: remaining.startswith(")")
                remaining = remaining[1:]

            result = (name, arguments)
        elif text[0] == self.VAR_DELIM_BEGIN: #"{":
            # it is a variable name, enclosed in curly brackets
            var_name, remaining = self._match(self.RE_VAR, text)
            result = ("%s" % var_name)
        else:
            # a token that ends at the first space (if any)  
            name, remaining = self._match(self.RE_LITERAL, text)

            # if literal name is a string, add it to the symbol table and replace
            # the literal with its encoded value
            lit_code, lit_type = SymbolTable.add_literal(name)
            if lit_type == "String":
                name = "(init-AbsString %s %s)" % (lit_code, len(name) - 2) # TODO this work because I assume to encode literals as objects of type AbsString

            result = name

        # in case of normal exit, I should have consumed some input
        # (otherwise it means it was not possible to parse it, thus I 
        # should have raised an exception earlier)
        assert len(remaining) < len(text)
        return result, remaining


    @contract(ast="is_ast", returns="is_predicate|string|int")
    def _ast_to_object(self, ast): 
        
        if self._is_node(ast):
            """
            case 1: it is a predicate that we have to parse
            case 2: it is a piece of smt code (injected by the user or to encode a literal)
            """
            name = ast[0]

            # get the class of predicate corresponding to the node name
            pred_class = self._get_class(name) #self.SYMBOL_TO_CLASS.get(name, None)
            if pred_class is not None:
                # case 1: a user predicate, continue to parse
                # recursively obtain the predicate/var/literal of each
                # argument ast
                arguments = []
                for a in ast[1]:
                    arguments.append(self._ast_to_object(a))
                # instantiate the predicate
                p = pred_class(*arguments)
            else:
                # case 2: a piece of SMT code, take it as text
                p = self._walk(ast)
                
            return p

        elif self._is_var(ast):
            # this is a variable
            return ast
    
        elif self._is_literal(ast):
            # this is a literal
            return ast

        elif isinstance(ast, int):
            # this is the encoded value of a string literal
            return ast
    
        else:
            #raise ValueError("The passed ast contains an unexpected node: %s" % ast)
            log.warning("Unknown predicate content. Assume it's a piece of SMT code: %s" % ast)
            return ast

    @staticmethod
    @contract(ast="tuple(string, list)|string", returns="string")
    def _walk(ast):
        # walk the ast and serialize it as a string

        res = None
        if isinstance(ast, basestring):
            res = ast
        elif isinstance(ast, tuple):
            arguments = []
            for a in ast[1]:
                arguments.append(PredicateParser._walk(a))
    
            res = "(%s %s)" % (ast[0], " ".join(arguments))
        else:   
            raise ValueError("The walk procedure does not expect this format")

#        log.debug("Walk IN: %s OUT: %s" % (ast, res))
        return res

    @staticmethod
    def _is_node(ast):
        return isinstance(ast, tuple) and len(ast) == 2 and isinstance(ast[1], list) and re.match(PredicateParser.RE_NODE_NAME, ast[0])

    @staticmethod
    def _is_var(ast):
        return isinstance(ast, basestring) and re.match(PredicateParser.RE_VAR, ast)

    @staticmethod
    def _is_literal(ast):
        return isinstance(ast, basestring) and re.match(PredicateParser.RE_LITERAL, ast)
 
class FormulaParser(LeftLinearParser):

    RE_LITERAL = "true|false|\[[^\[\]]+\]" # TODO this reg-ex should refer to PROP_DELIM_BEGIN e PROP_DELIM_END
    SYMBOL_TO_CLASS = {
        ">"     : GT,
        ">="    : GTE,
        "<"     : LT,
        "<="    : LTE,
        "="     : Eq,
        "!="    : NotEq,
        "and"   : formulas.And,
        "or"    : formulas.Or,
        "not"   : formulas.Not,
#        "P" : formulas.Proposition,
        "G" : formulas.Globally,
        "S" : formulas.SomePaths,
        "A" : formulas.AllPaths,
        "N" : formulas.Next,
        "F" : formulas.Future,
        "->"    : formulas.Imply, # to be added
        "<->"   : formulas.Iff # to be added
    }

    PROP_DELIM_BEGIN = "["
    PROP_DELIM_END = "]"

    @contract(ss="is_state_space")
    def __init__(self,ss):
        super(FormulaParser,self).__init__()
        self.ss=ss
    
    def predicate_to_existential_abstraction(self,ss, pred):
   
        solver = SMTSolver()

        existential_abstraction = []

        for conf in ss.enumerate: # filter the configurations that are compatible with pred

            conf_preds = ss.value(conf)
 
            ctx = {}
            for var in ss.variables:
                ctx[var] = var

  
            curr_problem = []
            for (curr_attr, curr_pred) in zip(ss.attributes, conf_preds):
                # TODO this is a dirty solution; find a better way
                datatypes = [ curr_attr.domain.datatype ]
                if hasattr(datatypes[0], "datatypes"):
                    datatypes = datatypes[0].datatypes
                # end of the dirty solution

                assert len(curr_attr.variables) == len(datatypes), "Expected one datatype per variable. Got %s variables vs %s datatypes" % (len(curr_attr.variables), len(datatypes))

                for (var, datatype) in zip(curr_attr.variables, datatypes):
                    assert isinstance(datatype, DataType)
                    dt_declaration = datatype.smt_declaration
                    log.debug("Datatype: %s -> Declaration: %s" % (datatype, dt_declaration))
                    dt_var_axioms = datatype.smt_var_axioms(var)
                    log.debug("Var: %s:%s -> Axioms: %s" % (var, datatype, dt_var_axioms))

                    if len(dt_declaration) > 0:
                        curr_problem.append(dt_declaration)
                    if len(dt_var_axioms) > 0:
                        curr_problem.append(dt_var_axioms)
                    curr_problem.append("(declare-const %s %s)" % (var, datatype))

#                curr_problem.append(curr_pred.smt_assert(lhs=curr_attr.variables[0]))
                curr_problem.append(curr_pred.smt_assert(**ctx))


        curr_problem.append(pred.smt_assert(**ctx))
        log.debug("Existential abstraction SMT problem: %s. Context: %s" % (curr_problem, ctx))

        # pass the problem to the smt solver; if it is satisfiable, take the conf
        # otherwise skip it
        solver.push()
        res = solver.check_sat(curr_problem)
        if res not in ["sat", "unsat"]:
            raise ValueError("Error computing the existential abstraction of the formula: %s" % res)

        if res == "sat":
            existential_abstraction.append(conf)
        solver.pop()

        return existential_abstraction
    
    def from_predicate(self, ss, pred): #argv):
    
        # translate a predicate onto a list of configurations
        conf_list = self.predicate_to_existential_abstraction(ss, pred)
    
        # define a formula for converting a configuration onto a Proposition
        conf_to_prop = lambda c: formulas.Proposition(c)

        # translate a list of configurations onto a list of Proposition's
        prop_list = map(conf_to_prop, conf_list)

        # create an Or among all the Proposition's in the list
        return formulas.Or(*prop_list)        
    
    @contract(text="string", returns="tuple(is_ast, string)")
    
    def _text_to_ast(self, text):
        """
        Given some text, it returns a tuple representing its abstract
        syntax tree, and a string containing the non-parsed text. 
        """

        remaining = ""

        if text[0] == "(":
            # we are in a new nested node; the first component is the
            # node name, which cannot contain neither spaces nor 
            # parenthesis, nor curly braces
            name, remaining = self._match(LeftLinearParser.RE_NODE_NAME, text[1:])

            arguments = []
            while len(remaining) > 0 and not remaining.startswith(")"):
                ast, remaining = self._text_to_ast(remaining.strip())
                arguments.append(ast)

            if len(remaining) > 0:
                # then: remaining.startswith(")")
                remaining = remaining[1:]

            result = (name, arguments)
       
        else:
            # a token that ends at the first space (if any)  
            #name, remaining = self._match(self.RE_LITERAL, text)
            
            literal, remaining = self._match(self.RE_LITERAL, text)
            if literal[0]==self.PROP_DELIM_BEGIN and literal[-1]==self.PROP_DELIM_END:
                predicate=literal[1:-1]
                pp=PredicateParser()
                pre=pp.parse(predicate)
                assert isinstance(pre, Predicate)
                #print pre
                result=self.from_predicate(self.ss,pre)
            else:
                raise ValueError("not handled: literal = %s, remaining = %s" % (literal, remaining))    

        # in case of normal exit, I should have consumed some input
        # (otherwise it means it was not possible to parse it, thus I 
        # should have raised an exception earlier)
        assert len(remaining) < len(text)

        return result, remaining

    #@contract(ast="is_ast", returns="is_predicate|string|int")
    def _ast_to_object(self, ast): 

        if isinstance(ast, tuple):
            
            assert isinstance(ast[0], basestring), ast
            assert isinstance(ast[1], list)

            arguments = []
            name = ast[0]
            klass = self.SYMBOL_TO_CLASS[name]

            arguments = []
            for item in ast[1]:
                arguments.append(self._ast_to_object(item))

            return klass(*arguments)

        elif isinstance(ast,formulas.Or):
            return (ast)                    
        else:
            raise ValueError("Value not expected")
                    

    @staticmethod
    @contract(ast="tuple(string, list)|string", returns="string")
    def _walk(ast):
        # walk the ast and serialize it as a string

        res = None
        if isinstance(ast, basestring):
            res = ast
        elif isinstance(ast, tuple):
            arguments = []
            for a in ast[1]:
                arguments.append(PredicateParser._walk(a))
    
            res = "(%s %s)" % (ast[0], " ".join(arguments))
        else:   
            raise ValueError("The walk procedure does not expect this format")

#        log.debug("Walk IN: %s OUT: %s" % (ast, res))
        return res

    @staticmethod
    def _is_node(ast):
        return isinstance(ast, tuple) and len(ast) == 2 and isinstance(ast[1], list) and re.match(PredicateParser.RE_NODE_NAME, ast[0])

    @staticmethod
    def _is_var(ast):
        return isinstance(ast, basestring) and re.match(PredicateParser.RE_VAR, ast)

    @staticmethod
    def _is_literal(ast):
        return isinstance(ast, basestring) and re.match(PredicateParser.RE_LITERAL, ast)
       

