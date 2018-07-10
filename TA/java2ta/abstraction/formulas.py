class PathFormula(object):

    def __init__(self):
        self.args = []
        
    def from_predicate(self, ss, pred): #argv):
##        pro=""
##        cont=1
##        for arg in argv:
##            
##            pro=pro+"Proposition(%s)" % (str(arg))
##            if cont<len(argv):
##                pro=pro+","  
##            cont=cont+1    
##        g="Or(%s)" % (pro)  
##         
##        return eval(g).to_uppaal()
    
        # translate a predicate onto a list of configurations
        conf_list = predicate_to_existential_abstraction(ss, pred)

        # define a formula for converting a configuration onto a Proposition
        conf_to_prop = lambda c: Proposition(c)

        # translate a list of configurations onto a list of Proposition's
        formulas = map(conf_to_prop, conf_list)

        # create an Or among all the Proposition's in the list
        return Or(*formulas)
            
class And(PathFormula):

    def __init__(self, *argv):
        super(And, self).__init__()
    
        for arg in argv:
            self.args.append(arg)

    def to_uppaal(self):

        form_to_upp = lambda f: "(%s)" % f.to_uppaal()

        pre = map(form_to_upp, self.args)

        return u" and ".join(map(form_to_upp, self.args))
 

class Or(PathFormula):

    def __init__(self, *argv):
        super(Or, self).__init__()
       
        for arg in argv:
            self.args.append(arg)
    
    def to_uppaal(self):
##        res="(1!=1)"   
##        for arg in self.args:
##            res="(%s) or (%s)" % (res,arg.to_uppaal())
##        return res

        form_to_upp = lambda f: "(%s)" % f.to_uppaal()

        pre = map(form_to_upp, self.args)

        return u" or ".join(map(form_to_upp, self.args))
       
class Not(PathFormula):

    def __init__(self, arg):
        super(Not, self).__init__()

        self.args.append(arg)

    def to_uppaal(self):
        return u"not (%s)" % (self.args[0].to_uppaal(),)
        
class Future(PathFormula):

    def __init__(self, arg):
        super(Future, self).__init__()

        self.args.append(arg)

    def to_uppaal(self):
        return u"Future (%s)" % (self.args[0].to_uppaal(),)
        
class Next(PathFormula):

    def __init__(self, arg):
        super(Next, self).__init__()

        self.args.append(arg)

    def to_uppaal(self):
        return u"Next (%s)" % (self.args[0].to_uppaal(),)

class Globally(PathFormula):

    def __init__(self, arg):
        super(Globally, self).__init__()

        self.args.append(arg)

    def to_uppaal(self):
        return u"Globally (%s)" % (self.args[0].to_uppaal(),)
        
class StateFormula(PathFormula):

    def __init__(self):
        super(StateFormula, self).__init__()
        
class AllPaths(StateFormula):

    def __init__(self, arg):
        super(AllPaths, self).__init__()

        self.args.append(arg)

    def to_uppaal(self):
        return u"All Paths (%s)" % (self.args[0].to_uppaal(),)

class SomePaths(StateFormula):

    def __init__(self, arg):
        super(SomePaths, self).__init__()

        self.args.append(arg)

    def to_uppaal(self):
        return u"Some Paths (%s)" % (self.args[0].to_uppaal(),)
            
class Proposition(StateFormula):
    
    def __init__(self, arg):
        super(Proposition, self).__init__()
        self.args.append(arg)

    def to_uppaal(self):
        assert len(self.args) == 1, "Expected exactly 1 argument. Got: %s" % self.args
        return u"%s" % (self.args[0],)
        
