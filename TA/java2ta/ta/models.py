class Type(object):

    def __init__(self, name):
        self.name = name

class Int(Type):

    def __init__(self):

        super(Type, self).__init__("int")

class Bool(Type):

    def __init__(self):

        super(Type, self).__init__("bool")

class BoundedInt(Type):

    def __init__(self, min_val, max_val):

        assert isinstance(min_val, int)
        assert isinsatnce(max_val, int)

        self.min_val = min_val
        self.max_val = max_val
        super(Type, self).__init__("int[%s,%s]" % (min_val,max_val))

    def values(self):
        return xrange(self.min_val, self.max_val+1)


class ClockType(Type):
    
    def __init__(self):
        super(Type, self).__init__("clock")


class Variable(object):

    def __init__(self, name, type):

        assert isinstance(name, basestring)
        assert isinstance(type, Type)

        self.name = name
        self.type = type


class ClockVariable(Variable):

    def __init__(self, name):
    
        super(Variable, self).__init__(name, Clock)


class ClockExpression(object):

    def __init__(self, exp=""):
    
        self.exp = exp


class Location(object):

    def __init__(self, name, is_initial=False, is_urgent=False):

        assert isinstance(name, basestring)

        self.name = name
        self.invariant = None
        self.incoming = set([])
        self.outgoing = set([])

        self.is_initial = is_initial
        self.is_urgent = is_urgent


    def set_invariant(self, cexp):
        
        assert isinstance(cexp, ClockExpression)

        self.invariant = cexp


    def set_initial(self):

        self.is_initial = True

    
    def set_urgent(self):
    
        self.is_urgent = True


    def __str__(self):
        return self.name


class Edge(object):

    def __init__(self, source, target, label=None):

        assert isinstance(source, Location)
        assert isinstance(target, Location)

        self.source = source
        self.target = target

        source.outgoing.add(self)
        target.incoming.add(self)
        self.label = label

    def __str__(self):
        return "%s -> %s" % (self.source, self.target)


class TA(object):
    
    def __init__(self, name, locations=[], edges=[]):

        assert isinstance(name, basestring)
        assert isinstance(locations, set) or isinstance(locations, list)
        assert isinstance(edges, set) or isinstance(edges, list)

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
        self.variables = set([])


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

        source_loc = self.get_or_add_location(edge.source)
        target_loc = self.get_or_add_location(edge.target)

        found = None

        if source_loc in self._edges_lookup and target_loc in self._edges_lookup[source_loc]:
            found = self._edges_lookup[source_loc][target_loc]

        if not found:
            found = Edge(source_loc, target_loc)
            self.add_edge(found)

        # in any case, update the edge label
        found.label = edge.label

        return found
 
        
          

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
        

    def add_edge(self, edge):

        assert isinstance(edge, Edge)
        assert isinstance(self.edges, set)

        if edge.source not in self.locations or edge.target not in self.locations:
            raise ValueError("Before adding an edge, you must add its source and target locations to the TA")
    
        self.edges.add(edge)

        if not edge.source in self._edges_lookup:
            self._edges_lookup[edge.source] = {}
        self._edges_lookup[edge.source][edge.target] = edge

    def add_variable(self, var):

        assert isinstance(var, Variable)
    
        self.variables.add(var)


class NTA(object):

    def __init__(self):

        self.tas = set([])
        self.variables = set([])

    def add_ta(self, ta):
    
        assert isinstance(ta, TA)

        if not ta.initial_loc:
            raise ValueError("All the TAs must have an initial location")

        self.tas.add(ta)

    def add_variable(self, var):

        assert isinstance(var, Variable)
    
        self.variables.add(var)


