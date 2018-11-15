from java2ta.commons.utility import new_contract_check_type, unique
from java2ta.ta.models import TA # allow to import contract "is_ta"

from contracts import contract
import logging

log = logging.getLogger()

class PathFormula(object):

    def __init__(self):
        self.args = []
        self._ss_env = None

    def set_ss_env(self, ss_env):
        self._ss_env = ss_env

    def __str__(self):
        return "%s(%s)" % (self.__class__.__name__, self.args)

    __repr__ = __str__

    @property
    def ss_env(self):
        return self._ss_env

    @property
    def process_names(self):
        return self._ss_env.keys()

    @staticmethod
    def formula_to_uppaal(*args, **ctx):

        @contract(formula="is_path_formula")
        def aux(formula):
            uppaal_repr = formula.to_uppaal(*args, **ctx)
            if len(uppaal_repr) > 0 and uppaal_repr not in ["true", "false"]:
                uppaal_repr = "(%s)" % uppaal_repr
            return uppaal_repr

        return aux

new_contract_check_type("is_path_formula", PathFormula)
        
            
class And(PathFormula):

    def __init__(self, *argv):
        super(And, self).__init__()
 
        if len(argv) < 2:
            raise ValueError("An And formula should receive at least two arguments. Received: %s" % len(argv))
      
        for arg in argv:
            self.args.append(arg)
   

    def to_uppaal(self, *args, **ctx):

        aux = PathFormula.formula_to_uppaal(*args, **ctx)
        pre = map(aux, self.args)
        sub_formulas = map(aux, self.args)

        # we can simply ignore the 'true' sub-formulas, since they do not contribute 
        sub_formulas = filter(lambda f: f != "true", sub_formulas)

        sub_formulas = unique(sub_formulas)

        if len(sub_formulas) == 0:
            # all sub-formulas were true, thus And(true, ..., true) = true
            res = "true"
        elif "false" in sub_formulas:
            # one 'false' sub-formula is enough to falsify the entire And
            res = "false" 
        else:
            res =  u" and ".join(sub_formulas)

        return res


 
class Imply(PathFormula):

    def __init__(self, *argv):
        super(Imply, self).__init__()
    
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        aux = PathFormula.formula_to_uppaal(*args, **ctx)
        pre = map(aux, self.args)
        return u" -> ".join(map(aux, self.args))

        
class Iff(PathFormula):

    def __init__(self, *argv):
        super(Iff, self).__init__()
    
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        """
        TODO fix this
        """
        aux = PathFormula.formula_to_uppaal(*args, **ctx)
        pre = map(aux, self.args)
        return u" <-> ".join(map(aux, self.args))

         
class Or(PathFormula):

    def __init__(self, *argv):
        super(Or, self).__init__()
 
        if len(argv) < 2:
            raise ValueError("An Or formula should receive at least two arguments. Received: %s" % len(argv))
      
        for arg in argv:
            self.args.append(arg)
    
    def to_uppaal(self, *args, **ctx):
        aux = PathFormula.formula_to_uppaal(*args, **ctx)

        sub_formulas = map(aux, self.args)

        sub_formulas = filter(lambda f: f != "false", sub_formulas)

        sub_formulas = unique(sub_formulas)

        if len(sub_formulas) == 0:
            # all sub-formula were false, thus the Or(false,..., false) = false
            res = "false"
        elif "true" in sub_formulas:
            # one 'true' sub-formula is enough to satisfy the entire Or
            res = "true" 
        else:
            res = u" or ".join(sub_formulas)

        return res

class Not(PathFormula):

    def __init__(self, arg):
        super(Not, self).__init__()

        if not argv:
            raise ValueError("A Not formula should receive a non-null argument.")

        self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        # TODO generalize this check, perhaps using a common ancestor type for the accepted state formulas
        if not isinstance(subformula, And) and not isinstance(subformula, Or) and not isinstance(subformula, Proposition) and not isinstance(subformula, Not):
            raise ValueError("The AllPaths formula accepts only And/Or/Not/Proposition subformula. Got: %s" % (type(subformula)))
        return u"not %s" % (subformula.to_uppaal(*args, **ctx),)



        
class Future(PathFormula):

    def __init__(self, arg):
        super(Future, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        # TODO generalize this check, perhaps using a common ancestor type for the accepted state formulas
        if not isinstance(subformula, And) and not isinstance(subformula, Or) and not isinstance(subformula, Proposition) and not isinstance(subformula, Not):
            raise ValueError("The AllPaths formula accepts only And/Or/Not/Proposition subformula. Got: %s" % (type(subformula)))
        return u"<> %s" % (subformula.to_uppaal(*args, **ctx),)


        
class Next(PathFormula):

    def __init__(self, arg):
        super(Next, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        raise ValueError("The Next formula is not supported in Uppaal")

class Globally(PathFormula):

    def __init__(self, arg):
        super(Globally, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        log.debug("Globally.to_uppaal: %s, %s" % (args, ctx))

        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        # TODO generalize this check, perhaps using a common ancestor type for the accepted state formulas
        if not isinstance(subformula, And) and not isinstance(subformula, Or) and not isinstance(subformula, Proposition) and not isinstance(subformula, Not):
            raise ValueError("The AllPaths formula accepts only And/Or/Not/Proposition subformula. Got: %s" % (type(subformula)))

        # this is a hack (because the "initial" location is auxiliary, and we assume it should not 
        # falsify a Globally formula)
        initial_locations = []
        for curr_arg in args:
            if isinstance(curr_arg, dict):
                # assume it's a dict of processes
                for name, proc in iter(curr_arg.items()):
                    assert isinstance(proc, TA)
                    if not proc.template.initial_loc:
                        raise ValueError("Cannot accept in the context processes that do not have an initial location set") 
                    if proc.template.initial_loc.name == "initial":
                        initial_locations.append("%s.initial" % (proc.name,))

        log.debug("Hack G formula: initial locations = %s" % (initial_locations))

        if len(initial_locations) > 0:
            initial_locations = ") or (".join(initial_locations)
            initial_locations = "(%s) or " % initial_locations
        else:
            initial_locations = ""

        return u"[] %s%s" % (initial_locations, subformula.to_uppaal(*args, **ctx),)

        
class StateFormula(PathFormula):

    def __init__(self):
        super(StateFormula, self).__init__()
        
class AllPaths(StateFormula):

    def __init__(self, arg):
        super(AllPaths, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
    
        log.debug("AllPaths.to_uppaal: %s, %s" % (args, ctx))
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        if not isinstance(subformula, Globally) and not isinstance(subformula, Future):
            raise ValueError("The AllPaths formula accepts only Globally or Future as subformula. Got: %s" % (type(subformula)))
        return u"A%s" % (subformula.to_uppaal(*args, **ctx),)



class SomePaths(StateFormula):

    def __init__(self, arg):
        super(SomePaths, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, *args, **ctx):
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        if not isinstance(subformula, Globally) and not isinstance(subformula, Future):
            raise ValueError("The SomePaths formula accepts only Globally or Future as subformula. Got: %s" % (type(subformula)))
        return u"E%s" % (subformula.to_uppaal(*args, **ctx),)


            
class Proposition(StateFormula):
    
    @contract(proc_name="string")
    def __init__(self, proc_name, arg):
        super(Proposition, self).__init__()
        self.proc_name = proc_name
        self.args.append(arg) # arg is a configuration


    def __str__(self):
        return "%s : %s" % (self.proc_name, self.args[0])

    __repr__ = __str__

    @contract(formula_env="dict(string:is_ta)")
    def to_uppaal(self, formula_env, **ctx):
        """
        The Uppaal interpretation of a single proposition is an or-formula conjuncting all the 
        locations that match the passed configuration (i.e. one for each reachable PC with the passed
        configuration)
        """
        from java2ta.translator.models import build_location_name, PC
        from java2ta.ta.models import TA, TATemplate
        from java2ta.ta.views import uppaal_loc_name

        #log.debug("Formula env: %s. Process name: %s" % (formula_env, self.proc_name))
        ta = formula_env.get(self.proc_name, None)

        if not ta:
            raise ValueError("Cannot find TA for process with name '%s' in the proposition" % self.proc_name)

#        assert len(processes) == 1, "At the moment we only handle the case of one process. Proposition: %s. Processes: %s, %s" % (self, processes,ctx)
#        ta = processes[0]

        if not isinstance(ta, TA):
            raise ValueError("Expected argument of type TATemplate. Passed: %s" % type(ta))

        assert len(self.args) == 1, "Expected exactly 1 argument. Got: %s" % self.args
        conf = self.args[0]

        locations = ta.template.conf_to_locations(conf)

        # extract the uppaal name of locations that match the passed configuration, and create an
        # or-formula in the uppaal specification language
        uppaal_locations = map(lambda loc: loc.uppaal_name(ta), ta.template.conf_to_locations(conf))

        uppaal_formula = "false"
        if len(uppaal_locations) > 0:
            uppaal_formula = "(%s)" % (" or ".join(uppaal_locations), )
        return uppaal_formula
        
