from contracts import contract
import logging

log = logging.getLogger()

class PathFormula(object):

    def __init__(self):
        self.args = []
        
##    def from_predicate(self, ss, pred): #argv):
##    
##        # translate a predicate onto a list of configurations
##        conf_list = predicate_to_existential_abstraction(ss, pred)
##
##        # define a formula for converting a configuration onto a Proposition
##        conf_to_prop = lambda c: Proposition(c)
##
##        # translate a list of configurations onto a list of Proposition's
##        formulas = map(conf_to_prop, conf_list)
##
##        # create an Or among all the Proposition's in the list
##        return Or(*formulas)
##
            
class And(PathFormula):

    def __init__(self, *argv):
        super(And, self).__init__()
 
        if len(argv) < 2:
            raise ValueError("An And formula should receive at least two arguments. Received: %s" % len(argv))
      
        for arg in argv:
            self.args.append(arg)
   
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self, ta):

        form_to_upp = lambda f: "(%s)" % f.to_uppaal(ta)

        pre = map(form_to_upp, self.args)

        return u" and ".join(map(form_to_upp, self.args))

 
class Imply(PathFormula):

    def __init__(self, *argv):
        super(Imply, self).__init__()
    
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self, ta):
        """
        TODO fix this
        """
        form_to_upp = lambda f: "(%s)" % f.to_uppaal(ta)

        pre = map(form_to_upp, self.args)

        return u" -> ".join(map(form_to_upp, self.args))
        
class Iff(PathFormula):

    def __init__(self, *argv):
        super(Iff, self).__init__()
    
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self, ta):
        """
        TODO fix this
        """
        form_to_upp = lambda f: "(%s)" % f.to_uppaal(ta)

        pre = map(form_to_upp, self.args)

        return u" <-> ".join(map(form_to_upp, self.args))
         
class Or(PathFormula):

    def __init__(self, *argv):
        super(Or, self).__init__()
 
        if len(argv) < 2:
            raise ValueError("An Or formula should receive at least two arguments. Received: %s" % len(argv))
      
        for arg in argv:
            self.args.append(arg)
    
    def to_uppaal(self, ta):

        form_to_upp = lambda f: "(%s)" % f.to_uppaal(ta)

        log.debug("Or arguments: %s" % self.args)
        pre = map(form_to_upp, self.args)
        log.debug("Or arguments formatted: %s" % pre)

        # TODO here we should refer (and print) the name of the locations *encoding* the propositions
        #...

        return u" or ".join(pre)
       
class Not(PathFormula):

    def __init__(self, arg):
        super(Not, self).__init__()

        if not argv:
            raise ValueError("A Not formula should receive a non-null argument.")

        self.args.append(arg)

    def to_uppaal(self, ta):
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        # TODO generalize this check, perhaps using a common ancestor type for the accepted state formulas
        if not isinstance(subformula, And) and not isinstance(subformula, Or) and not isinstance(subformula, Proposition) and not isinstance(subformula, Not):
            raise ValueError("The AllPaths formula accepts only And/Or/Not/Proposition subformula. Got: %s" % (type(subformula)))
        return u"not %s" % (subformula.to_uppaal(ta),)



        
class Future(PathFormula):

    def __init__(self, arg):
        super(Future, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, ta):
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        # TODO generalize this check, perhaps using a common ancestor type for the accepted state formulas
        if not isinstance(subformula, And) and not isinstance(subformula, Or) and not isinstance(subformula, Proposition) and not isinstance(subformula, Not):
            raise ValueError("The AllPaths formula accepts only And/Or/Not/Proposition subformula. Got: %s" % (type(subformula)))
        return u"<> %s" % (subformula.to_uppaal(ta),)


        
class Next(PathFormula):

    def __init__(self, arg):
        super(Next, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, ta):
        raise ValueError("The Next formula is not supported in Uppaal")

class Globally(PathFormula):

    def __init__(self, arg):
        super(Globally, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, ta):

        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        # TODO generalize this check, perhaps using a common ancestor type for the accepted state formulas
        if not isinstance(subformula, And) and not isinstance(subformula, Or) and not isinstance(subformula, Proposition) and not isinstance(subformula, Not):
            raise ValueError("The AllPaths formula accepts only And/Or/Not/Proposition subformula. Got: %s" % (type(subformula)))
        return u"[] %s" % (subformula.to_uppaal(ta),)



        
class StateFormula(PathFormula):

    def __init__(self):
        super(StateFormula, self).__init__()
        
class AllPaths(StateFormula):

    def __init__(self, arg):
        super(AllPaths, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, ta):
    
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        if not isinstance(subformula, Globally) and not isinstance(subformula, Future):
            raise ValueError("The AllPaths formula accepts only Globally or Future as subformula. Got: %s" % (type(subformula)))
        return u"A%s" % (subformula.to_uppaal(ta),)



class SomePaths(StateFormula):

    def __init__(self, arg):
        super(SomePaths, self).__init__()

        self.args.append(arg)

    def to_uppaal(self, ta):
        num_subformulas = len(self.args)
        if num_subformulas != 1:
            raise ValueError("Expected exactly one sub-formula. Got %s sub-formulas" % num_subformulas)

        subformula = self.args[0]
        if not isinstance(subformula, Globally) and not isinstance(subformula, Future):
            raise ValueError("The SomePaths formula accepts only Globally or Future as subformula. Got: %s" % (type(subformula)))
        return u"E%s" % (subformula.to_uppaal(ta),)


            
class Proposition(StateFormula):
    
    def __init__(self, arg):
        super(Proposition, self).__init__()
        self.args.append(arg) # arg is a configuration

    def to_uppaal(self, ta):
        """
        The Uppaal interpretation of a single proposition is an or-formula conjuncting all the 
        locations that match the passed configuration (i.e. one for each reachable PC with the passed
        configuration)
        """
        from java2ta.translator.models import build_location_name, PC
        from java2ta.ta.models import TA, TATemplate
        from java2ta.ta.views import uppaal_loc_name

        if not isinstance(ta, TATemplate):
            raise ValueError("Expected argument of type TATemplate. Passed: %s" % type(ta))

        assert len(self.args) == 1, "Expected exactly 1 argument. Got: %s" % self.args
        conf = self.args[0]

        locations = ta.conf_to_locations(conf)

        # extract the uppaal name of locations that match the passed configuration, and create an
        # or-formula in the uppaal specification language
        uppaal_locations = map(lambda loc: loc.uppaal_name(ta), ta.conf_to_locations(conf))
    
        uppaal_formula = "(%s)" % (" or ".join(uppaal_locations), )
        return uppaal_formula
        
