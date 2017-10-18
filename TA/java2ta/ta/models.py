from contracts import contract, new_contract

from java2ta.commons.utility import new_contract_check_type

import logging

log = logging.getLogger("main")

class Type(object):

    def __init__(self, name):
        self.name = name

    def __repr__(self):
        return self.name

    def __str__(self):
        return self.name

    def __unicode__(self):
        return unicode(self.name)

class Int(Type):

    def __init__(self):

        super(Int, self).__init__("int")

class Bool(Type):

    def __init__(self):

        super(Bool, self).__init__("bool")

class BoundedInt(Type):

    def __init__(self, min_val, max_val):

        assert isinstance(min_val, int)
        assert isinsatnce(max_val, int)

        self.min_val = min_val
        self.max_val = max_val
        super(BoundedInt, self).__init__("int[%s,%s]" % (min_val,max_val))

    def values(self):
        return xrange(self.min_val, self.max_val+1)


class ClockType(Type):
    
    def __init__(self):
        super(ClockType, self).__init__("clock")


class Variable(object):

    @contract(name="string", type=Type)
    def __init__(self, name, type):

        self.name = name
        self.type = type

    def __repr__(self):
        return "%s : %s" % (self.name, self.type)

    def __str__(self):
        return "%s : %s" % (self.name, self.type)

    def __unicode__(self):
        return u"%s : %s" % (self.name, self.type)

new_contract_check_type("is_variable", Variable)

class ClockVariable(Variable):

    @contract(name="string")
    def __init__(self, name):
    
        super(ClockVariable, self).__init__(name, ClockType())

new_contract_check_type("is_clock_variable", ClockVariable)

##class ClockExpression(object):
##
##    def __init__(self, exp=""):
##    
##        self.exp = exp
##
##    def __str__(self):
##        self.exp
##
##    def __unicode__(self):
##        return unicode(self.exp)
##

class Location(object):

    @contract(name="string", is_initial="bool", is_urgent="bool")
    def __init__(self, name, is_initial=False, is_urgent=False):

        self.name = name
        self.invariant = None
        self.incoming = set([])
        self.outgoing = set([])

        self.is_initial = is_initial
        self.is_urgent = is_urgent


    @contract(cexp="string")
    def set_invariant(self, cexp):
        
#        assert isinstance(cexp, ClockExpression)

        self.invariant = cexp


    def set_initial(self):

        self.is_initial = True

    
    def set_urgent(self):
    
        self.is_urgent = True


    def __str__(self):
        return self.name


    def __repr__(self):
        return self.name

new_contract_check_type("is_location", Location) 
       

class Edge(object):

    def __init__(self, source, target, label=None, guard=None, reset=None, clock_variables=None, variables=None):

        assert isinstance(source, Location)
        assert isinstance(target, Location)

        self.source = source
        self.target = target

        source.outgoing.add(self)
        target.incoming.add(self)
        self.label = label
        self.guard = guard
        self.reset = reset

        self.clock_variables = clock_variables or set([])
        self.variables = variables or set([])

    def __str__(self):
        return "%s -> %s" % (self.source, self.target)

new_contract_check_type("is_edge", Edge)

class TA(object):
    
    @contract(name="string", locations="set(is_location)|list(is_location)", edges="set(is_edge)|list(is_edge)")
    def __init__(self, name, locations=[], edges=[]):

        self.name = name
        self.locations = set([])
        self._location_names = dict()
    
        for loc in locations:
            self.add_location(loc)

        self.edges = set([])
        self._edges_lookup = {} 

        for curr_edge in edges:
            self.add_edge(curr_edge)

        self.initial_loc = None
        self._variables = dict()
        self._clock_variables = dict() 
    
    @property
    def variables(self):
        return self._variables.values()

    def has_variable(self, name):
        return name in self._variables

    @property
    def clock_variables(self):
        return self._clock_variables.values()

    def has_clock_variable(self, name):
        return name in self._clock_variables

    def has_location(self, name):
        assert isinstance(name, basestring)
        return name in self._location_names

    def get_location(self, name):
        assert isinstance(name, basestring)

        found = self._location_names.get(name, None)
        assert isinstance(found, Location) or found == None

        return found


    def get_or_add_location(self, loc):
        assert isinstance(loc, basestring) or isinstance(loc, Location)

        name = loc
        if isinstance(loc, Location):
            name = loc.name

        found = self.get_location(name)

        if not found:
            new_loc = loc# Location(name)
            if isinstance(loc, basestring):
                new_loc = Location(name)

            self.add_location(new_loc)
            found = new_loc

        assert isinstance(found, Location)
        return found
 
   
    def get_or_add_edge(self, edge):
        assert isinstance(edge, Edge)

        for cv in edge.clock_variables:
            self.add_clock_variable(cv)

        for v in edge.variables:
            self.add_variable(v)

        source_loc = self.get_or_add_location(edge.source)
        target_loc = self.get_or_add_location(edge.target)

        found = None

        if source_loc in self._edges_lookup and target_loc in self._edges_lookup[source_loc]:
            found = self._edges_lookup[source_loc][target_loc]

        if not found:
            found = Edge(source_loc, target_loc, label=edge.label, guard=edge.guard, clock_variables=edge.clock_variables, variables=edge.variables)
            self.add_edge(found)

        # in any case, update the edge label
        found.label = edge.label

        return found
 
        
          
    @contract(loc="is_location")
    def add_location(self, loc):

        assert isinstance(loc, Location)
        assert isinstance(self.locations, set)

        if loc.is_initial:
            if self.initial_loc:
                raise ValueError("You are adding a location tagged as initial to a TA that already contains one initial location")
            self.initial_loc = loc

        if loc.name not in self._location_names:
            self.locations.add(loc)
            self._location_names[loc.name] = loc
        
    @contract(edge="is_edge")
    def add_edge(self, edge):

        assert isinstance(edge, Edge)
        assert isinstance(self.edges, set)

        if edge.source not in self.locations or edge.target not in self.locations:
            raise ValueError("Before adding an edge, you must add its source and target locations to the TA")
    
        self.edges.add(edge)

        if not edge.source in self._edges_lookup:
            self._edges_lookup[edge.source] = {}
        self._edges_lookup[edge.source][edge.target] = edge

##    def get_or_add_variable(self, name, type):
##        
##        c = Variable(name, type)
##        self.add_clock_variable(name)
## 
    def add_variable(self, var):
        assert isinstance(var, Variable)

        log.debug(u"Add variable: %s" % unicode(var))

        if var.name not in self._variables:
            self._variables[var.name] = var

    def add_clock_variable(self, var):
        assert isinstance(var, ClockVariable)

        log.debug(u"Add clock: %s" % unicode(var))

        # do not insert duplicate clock variables
        if var.name not in self._clock_variables:
            self._clock_variables[var.name] = var


    def close(self):
        """
        The operation of closing a TA does the following:
        1. call pseudo-initial a location that has no entering transition
        2. if a single pseudo-initial location exists, makes it initial
        3. if multiple pseudo-initial locations exist, create an initial
            location and add a non-deterministic edge towards each of
            the pseudo-initial locations
        """

        if self.initial_loc != None:
            return    

        pseudo_initial = filter(lambda loc: len(loc.incoming) == 0, self.locations)

        initial = None

        if len(pseudo_initial) == 1:
            initial = pseudo_initial[0]
            initial.set_initial()
        elif len(pseudo_initial) == 0:
#           TODO what to do in this case?
            log.warning("The timed automaton has no pseudo-initial location. Not sure what to do ...")
#            raise ValueError("Cannot handle automaton with circular states")
        else:
            # len(pseudo_initial) > 0
            initial = Location("initial", is_initial=True)
            self.add_location(initial)
            
            for loc in pseudo_initial:
                e = Edge(initial, loc)
                self.add_edge(e)

        if initial:
            self.initial_loc = initial

new_contract_check_type("is_ta", TA)


class NTA(object):

    def __init__(self):

        self.tas = set([])
        self.variables = set([])

    @contract(ta="is_ta")
    def add_ta(self, ta):

        if not ta.initial_loc:
            raise ValueError("All the TAs must have an initial location")

        self.tas.add(ta)

    @contract(var="is_variable")
    def add_variable(self, var):
    
        self.variables.add(var)


new_contract_check_type("is_nta", NTA)
