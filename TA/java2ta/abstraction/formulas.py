from java2ta.commons.utility import new_contract_check_type

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

    @staticmethod
    def formula_to_uppaal(*args, **ctx):

        @contract(formula="is_path_formula")
        def aux(formula):
            uppaal_repr = formula.to_uppaal(*args, **ctx)
            if len(uppaal_repr) > 0:
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
   
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self, *args, **ctx):

        aux = PathFormula.formula_to_uppaal(*args, **ctx)
        pre = map(aux, self.args)
        return u" and ".join(map(aux, self.args))


 
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
        log.debug("Or.to_uppaal: %s, %s" % (args, ctx))

        log.debug("Or arguments: %s" % self.args)
        aux = PathFormula.formula_to_uppaal(*args, **ctx)

        pre = map(aux, self.args)
        log.debug("Or arguments formatted: %s" % pre)

        return u" or ".join(pre)
       
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
        return u"[] %s" % (subformula.to_uppaal(*args, **ctx),)



        
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
    
    def __init__(self, arg):
        super(Proposition, self).__init__()
        self.args.append(arg) # arg is a configuration


    def to_uppaal(self, *processes, **ctx):
        """
        The Uppaal interpretation of a single proposition is an or-formula conjuncting all the 
        locations that match the passed configuration (i.e. one for each reachable PC with the passed
        configuration)
        """
        from java2ta.translator.models import build_location_name, PC
        from java2ta.ta.models import TA, TATemplate
        from java2ta.ta.views import uppaal_loc_name

        assert len(processes) == 1, "At the moment we only handle the case of one process. Passed: %s, %s" % (processes,ctx)
        ta = processes[0]
        
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
        
