import re
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

new_contract_check_type("is_type", Type)

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

    @contract(name="string", type="string|is_type")
    def __init__(self, name, type, value=None):

        self.name = name

        if isinstance(type, str):
            if type == "int":
                type = Int()
            elif type == "bool":
                type = Bool()
            else:
                raise ValueError("At the moment I can only accept the following type names: int, bool. Support for bounded integer must be added. Passed: %s" % type)

        self.type = type
        self.value = value

    def __repr__(self):
        var_repr = None
        try:
            value = ""
            if self.value:
                value = " = %s" % self.value
            var_repr = "%s : %s%s" % (self.type, self.name, value)
        except Exception:
            # there could be an exception in case of broken contracts in constructor __init__:
            # in such case no attribute name or type exists
            var_repr = super(Variable,self).__repr__()

        return var_repr

    def __str__(self):
        value = ""
        if self.value:
            value = " = %s" % self.value
 
        return "%s : %s%s" % (self.type, self.name, value)

    def __unicode__(self):
        value = ""
        if self.value:
            value = u" = %s" % self.value
 
        return u"%s : %s%s" % (self.type, self.name, value)

 

new_contract_check_type("is_variable", Variable)

class ClockVariable(Variable):

    @contract(name="string")
    def __init__(self, name):
    
        super(ClockVariable, self).__init__(name, ClockType())

new_contract_check_type("is_clock_variable", ClockVariable)

class ClockCondition(object):

    OP_NEG = { 
        "<" : ">=", 
        "<=": ">",
        ">" : "<=",
        ">=": "<",
        "==": "!=",
        "!=": "=="
    }    

    @contract(clock_var="string", op="string", exp="string")
    def __init__(self, clock_var, op, exp):

        if op not in ClockCondition.OP_NEG:
            raise ValueError("Operator %s is not upported for clock expressions" % op)

        self.clock_var = clock_var
        self.op = op
        self.exp = exp

    def __str__(self):
        return "%s %s %s" % (self.clock_var, self.op, self.exp)

    def __unicode__(self):
        return u"%s %s %s" % (self.clock_var, self.op, self.exp)
    
    def negate(self):
 
        op_neg = ClockCondition.OP_NEG[self.op]

        return ClockCondition(self.clock_var, op_neg, self.exp)       

new_contract_check_type("is_clock_condition", ClockCondition)

class Location(object):

    @contract(name="string", is_initial="bool", is_urgent="bool")
    def __init__(self, name, is_initial=False, is_urgent=False, is_committed=False, configuration=None, predicate=None, pc=None):

        self.name = name
        self.invariant = None
        self._incoming = set([])
        self._outgoing = set([])
        self._configuration = configuration
        self._predicate = predicate
        self._pc = pc
        self._ta_template = None

        self._is_initial = is_initial
        self._is_urgent = is_urgent
        self.is_committed = is_committed

    @property
    def ta_template(self):
        return self._ta_template

    def set_ta_template(self, ta_template):
        self._ta_template = ta_template

    @property
    def configuration(self):
        return self._configuration

    @property
    def predicate(self):
        return self._predicate
    
    @property
    def pc(self):
        return self._pc

    @property
    def incoming(self):
        return self._incoming

    @property
    def outgoing(self):
        return self._outgoing

    @contract(returns="list")
    def select_outgoing(self, target):
        res = []
        for edge in self._outgoing:
            if edge.target.name == target.name:
                res.append(edge)

        return res

    def set_configuration(self, configuration):
        self._configuration = configuration

    def set_predicate(self, predicate):
        self._predicate = predicate

    def set_pc(self, pc):           
        self._pc = pc

    def add_outgoing(self, edge):
        if not edge.source or edge.source != self:
            raise ValueError("Error adding outgoing: expected same source state, received: %s" % edge.source)
        
        self._outgoing.add(edge)

    def add_incoming(self, edge):
        if not edge.target or edge.target != self:
            raise ValueError("Error adding incoming: expected same target state, received: %s" % edge.target)
        self._incoming.add(edge)

    def equals_modulo_pc(self, location):
        """
        TODO not sure this is the right place for this
        check; after all, a TA knows nothing of PC's.
        Perhaps we should move the entire minimization step
        before creating the TA
        """
        curr_name = re.sub(r"\@[0-9\.]+$", "", self.name)
        loc_name = re.sub(r"\@[0-9\.]+$", "", location.name)
        return curr_name == loc_name

    @contract(cexp="string")
    def set_invariant(self, cexp):
        
#        assert isinstance(cexp, ClockCondition)

        self.invariant = cexp

    def add_invariant(self, cexp):
        if self.invariant is not None and len(self.invariant) > 0:
    
            cexp = "(%s) and (%s)" % (self.invariant, cexp)

        self.invariant = cexp


    @property
    def is_initial(self):
        return self._is_initial

    @contract(value="bool")
    def set_initial(self, value=True):

        self._is_initial = value

    @property
    def is_urgent(self):
        return self._is_urgent

    @contract(value="bool")
    def set_urgent(self, value=True):
    
        self._is_urgent = value


    def __str__(self):
        return self.name


    def __repr__(self):
        return self.name


    @contract(returns="string")
    def uppaal_name(self, ta=None):
        from java2ta.ta.views import uppaal_loc_name

        assert ta is None or isinstance(ta, TA)

        uppaal_name = None

        if ta is None:
            uppaal_name = uppaal_loc_name(self.name)
        else:
            uppaal_name = "%s.%s" % (ta.name, uppaal_loc_name(self.name))

        return uppaal_name

    @property
    def neighbors(self):
        neighbors_list = map(lambda e: e.target, self.outgoing)
        return set(neighbors_list)
    
new_contract_check_type("is_location", Location) 
 

class Edge(object):

    @contract(source="is_location", target="is_location", label="None|string", guard="None|string")
    def __init__(self, source, target, label=None, guard=None, reset=None, clock_variables=None, variables=None, synchronization=None):

        assert isinstance(source, Location)
        assert isinstance(target, Location)

        self.source = source
        self.target = target

        self.label = label
        self.guard = guard
        self.reset = reset or set([])

        self.clock_variables = clock_variables or set([])
        self.variables = variables or set([])

        self.synchronization = synchronization

    @property
    def formatted_label(self):
        label_parts = []
        if self.guard:
            label_parts.append("[%s]" % self.guard)

        if self.label:
            label_parts.append(self.label)

        for r in self.reset:
            label_parts.append("%s := 0" % r)
            

        label = ""
        if label_parts:
            label = "|".join(label_parts)

        return label

    @contract(name="string")
    def add_reset(self, name):
        """
        Since self.reset is a set, we are not afraid of duplicates
        """
        self.reset.add(name)

    def add_constraint(self, condition):
        guard = condition

        if self.guard:
            guard = "(%s) and (%s)" % (self.guard, condition)

        self.guard = guard
            

    def __str__(self):
        label = self.formatted_label
        if label:
            label = "[%s]" % label
        return "%s -%s-> %s" % (self.source, label, self.target)

    __repr__ = __str__

    def __unicode__(self):
        label = self.formatted_label
        if label:
            label = "[%s]" % label
        return "%s -%s-> %s" % (self.source, label, self.target)

    def get_kwargs(self):
        kwargs = {
            "source": self.source, 
            "target": self.target, 
            "label": self.label, 
            "guard": self.guard, 
            "reset": self.reset, 
            "clock_variables": self.clock_variables, 
            "variables": self.variables,
            "synchronization": self.synchronization
        }

        return kwargs


new_contract_check_type("is_edge", Edge)

class TimeEdge(Edge):

    def __init__(self, source, target, sleep_time, label=None, *args, **kwargs):
        self.sleep_time = sleep_time
        super(TimeEdge, self).__init__(source, target, label=label)

    def __str__(self):
        label = self.formatted_label
        if label:
            label = "[%s]" % label
        return "%s ~%s~> %s" % (self.source, label, self.target)

new_contract_check_type("is_time_edge", TimeEdge)


class SleepEdge(Edge):

    def __init__(self, source, target, sleep_time, label=None, *args, **kwargs):
        self.sleep_time = sleep_time
        super(SleepEdge, self).__init__(source, target, label=label)


    def get_kwargs(self):
        kwargs = super(SleepEdge, self).get_kwargs()
        kwargs["sleep_time"] = self.sleep_time
        return kwargs

    def __str__(self):
        label = self.formatted_label
        if label:
            label = "[%s]" % label
        return "%s ~=%s=~> %s" % (self.source, label, self.target)

new_contract_check_type("is_sleep_edge", SleepEdge)

   

class NotifyUpdateEdge(Edge):

    def __str__(self):
        label = self.formatted_label
        if label:
            label = "[%s]" % label

        label = "%s{%s}" % (label, self.synchronization)
        return "%s -%s-> %s" % (self.source, label, self.target)

    def __unicode__(self):
        label = self.formatted_label
        if label:
            label = u"[%s]" % label

        label = u"%s{%s}" % (label, self.synchronization)
        return u"%s -%s-> %s" % (self.source, label, self.target)

class ReactUpdateEdge(Edge):

    def __str__(self):
        label = "{%s}" % self.synchronization
        return "%s -%s-> %s" % (self.source, label, self.target)

    def __unicode__(self):
        label = u"{%s}" % self.synchronization
        return u"%s -%s-> %s" % (self.source, label, self.target)



class TATemplate(object):
 
    @contract(name="string", locations="set(is_location)|list(is_location)", edges="set(is_edge)|list(is_edge)", broadcast_channels="list(string)")
    def __init__(self, name, locations=[], edges=[], broadcast_channels=[]):

        self.name = name
        self.locations = set([])
        self._location_names = dict()
        self.broadcast_channels = []
    
        for loc in locations:
            self.add_location(loc)

        self.edges = set([])
        self._edges_lookup = {} 

        for curr_edge in edges:
            self.add_edge(curr_edge)

        self.initial_loc = None
        self._variables = dict()
        self._clock_variables = dict() 
 
    def __str__(self):

        return "%s (%s states, %s transitions)" % (self.name, len(self.locations), len(self.edges))

    __repr__ = __str__
   
    @property
    def final_locations(self):
        for loc in self.locations:
            if len(loc.outgoing) == 0:
                yield loc

    @property
    def variables(self):
        return self._variables.values()

    def has_variable(self, name):
        return name in self._variables

    @property
    def clock_variables(self):
        return self._clock_variables.values()

    @contract(returns="list(is_location)")
    def conf_to_locations(self, conf):
        match_conf = lambda loc: loc.configuration == conf
        return filter(match_conf, self.locations)

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
#        assert isinstance(loc, basestring) or isinstance(loc, Location)
        assert isinstance(loc, Location)

#        name = loc
#        if isinstance(loc, Location):
#            name = loc.name
        name = loc.name

        found = self.get_location(name)

        if not found:
#            new_loc = loc# Location(name)
#            if isinstance(loc, basestring):
#                new_loc = Location(name)
#
#            self.add_location(new_loc)
#            found = new_loc
            self.add_location(loc)
            found = loc
        else:   
            # TODO this is a bit hack-ish; should we consider the passed location (loc) an updated
            # version of the existing one (and thus merge the new received information) or are they
            # supposed to be identical? At the moment I only look at the 'is_initial' flag, and update
            # it if the found location is not initial, but the passed loc is
            if loc.is_initial and not found.is_initial:
                found.set_initial()
            

        assert isinstance(found, Location)
        return found
 
   
    @contract(edge="is_edge", returns="is_edge")
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
            # we create an instance of edge.__class__
            # in this way, we support creating copies of the same type as the argument edge
            kwargs = edge.get_kwargs()
            kwargs["source"] = source_loc
            kwargs["target"] = target_loc
#            found = edge.__class__(source_loc, target_loc, label=edge.label, guard=edge.guard, clock_variables=edge.clock_variables, variables=edge.variables, synchronization=edge.synchronization)
            found = edge.__class__(**kwargs)
            self.add_edge(found)

        # in any case, update the edge label
        found.label = edge.label

        return found
 
        
    def add_location(self, loc):

        assert isinstance(loc, Location)
        assert isinstance(self.locations, set)

# TODO do not prevent TATemplate from having more than one initial location. If more then one initial
# location is present, the TATemplate does not point to any of them
        if loc.is_initial and self.initial_loc != loc:
            self.initial_loc = None

#        if loc.is_initial:
#            if self.initial_loc:
#                raise ValueError("You are adding a location tagged as initial to a TATemplate that already contains one initial location")
#            self.initial_loc = loc

        if loc.name not in self._location_names:
            self.locations.add(loc)
            self._location_names[loc.name] = loc
 
        loc.set_ta_template(self)

    
    @contract(loc="is_location")
    def del_location(self, loc):
        if loc.is_initial:
            if self.initial_loc == loc:
                self.initial_loc = None
            else:
                raise ValueError("Location is initial, but is not the TATemplate initial location")

        self.locations.discard(loc)
        self._location_names.pop(loc.name, None)
 
  
    @contract(edge="is_edge")
    def add_edge(self, edge):

        assert isinstance(edge, Edge)
        assert isinstance(self.edges, set)

        if edge.source not in self.locations or edge.target not in self.locations:
            raise ValueError("Before adding an edge, you must add its source and target locations to the TATemplate")
   
#        edge.source.outgoing.add(edge)
        edge.source.add_outgoing(edge)
#        edge.target.incoming.add(edge)
        edge.target.add_incoming(edge)

        self.edges.add(edge)

        if not edge.source in self._edges_lookup:
            self._edges_lookup[edge.source] = {}
        self._edges_lookup[edge.source][edge.target] = edge

        

    @contract(edge="is_edge")
    def del_edge(self, edge):
        
        if edge.source in self._edges_lookup:
            if edge.target in self._edges_lookup[edge.source]:
                self._edges_lookup[edge.source].pop(edge.target)
            if len(self._edges_lookup[edge.source]) == 0:
                self._edges_lookup.pop(edge.source)

        log.debug("Edge source outgoing: %s" % edge.source.outgoing)
        log.debug("Edge target incoming: %s" % edge.target.incoming)
        edge.source.outgoing.discard(edge)
        edge.target.incoming.discard(edge)

        self.edges.discard(edge)

    def add_variable(self, var):
        assert isinstance(var, Variable)

        log.debug(u"Add variable: %s" % unicode(var))

        if var.name not in self._variables:
            self._variables[var.name] = var

 
    @contract(var="string|is_clock_variable")
    def add_clock_variable(self, var):

        if isinstance(var, basestring):
            var = ClockVariable(var)

        assert isinstance(var, ClockVariable)

        log.debug(u"Add clock: %s" % unicode(var))

        # do not insert duplicate clock variables
        if var.name not in self._clock_variables:
            self._clock_variables[var.name] = var


    def close(self):
        """
        An "open" TATemplate is a template where multiple locations are labeled as initial locations,
        but none of them is referred to as the (unique) initial location of the template. To close
        the template, one has to create a new location (called "initial"), labeled it as the unique
        initial location, add edges towards the locations previously labeled as initial, and mark the
        latter as no more initial.

        One final constraint is that the unique initial location should be named "initial"
        """

        from java2ta.translator.models import PC
 
        pseudo_initial = filter(lambda loc: loc.is_initial, self.locations)

        log.debug("Pseudo initial locations: %s" % pseudo_initial)

        # if there are one or more locations labeled as initial, the template should have no pointer
        # to any initial location (since it's impossible to determine which one is the real initial loc)
        assert len(pseudo_initial) == 0 or self.initial_loc is None
        # if the initial location is set, then the list of pseudo-initial locations should contain 
        # exactly that location
        assert self.initial_loc is None or (len(pseudo_initial) == 1 and pseudo_initial[0] == self.initial_loc)

        if self.initial_loc != None and self.initial_loc.name == "initial":
            # the template is already "closed"
            return    

##        pseudo_initial = filter(lambda loc: len(loc.incoming) == 0, self.locations)
##
##        if len(pseudo_initial) == 0:
##            pseudo_initial =  filter(lambda l: l.pc == PC(0), self.locations)
##
        if len(pseudo_initial) == 0:
            raise ValueError("Cannot detect pseudo-initial locations")


        initial = Location("initial", is_initial=True, is_committed=True)
        self.add_location(initial)

        for loc in pseudo_initial:
            e = Edge(initial, loc)
            self.add_edge(e)

        self.initial_loc = initial



    def sanity_check(self):
        for loc in self.locations:
            for e in loc.outgoing:
                if e.source != loc: 
                    raise ValueError("Location %s has outgoing edge with different source location" % (loc, e))

            for e in loc.incoming:  
                if e.target != loc:
                    raise ValueError("Location %s has incoming edge with different target location" % (loc, e))


new_contract_check_type("is_ta_template", TATemplate)

class TA(object):
    
    @contract(template="is_ta_template", name="string")
    def __init__(self, name, template):
        self.name = name
        self.template = template

    def __str__(self):
        return "%s : { %s }" % (self.name, self.template)

    __repr__ = __str__

new_contract_check_type("is_ta", TA)

class NTA(object):

    def __init__(self):

        self._ta_templates = {}
        self._tas = {} #set([])
        self.variables = set([])
        self.broadcast_channels = set([])

    @property
    @contract(returns="list(is_ta)")
    def tas(self):
        return self._tas.values()

    @property
    @contract(returns="list(is_ta_template)")
    def ta_templates(self):
        return self._ta_templates.values()

    @contract(ta="is_ta")
    def add_ta(self, ta):

        if not ta.template.initial_loc:
            raise ValueError("All the TAs must have an initial location")

#        self.tas.add(ta)
        self._tas[ta.name] = ta
        self._ta_templates[ta.template.name] = ta.template

        self.broadcast_channels.update(ta.template.broadcast_channels)

    @contract(ta_name="string", returns="is_ta")
    def get_ta(self, ta_name):
        ta = self._tas[ta_name]
        return ta

    @contract(var="is_variable")
    def add_variable(self, var):
    
        self.variables.add(var)


new_contract_check_type("is_nta", NTA)

class Trace(object):

    def __init__(self):
        self.steps = []

    def add_step(self, state):
        self.steps.append(state)


new_contract_check_type("is_trace", Trace)

class TraceState(object):

    @contract(id="string", proc_locations="dict(string:*)")
    def __init__(self, id, proc_locations):
        self.id = id
        self.proc_locations = dict(**proc_locations)

    def __str__(self):
        proc_locations = ["%s : %s" % (p,loc) for p,loc in self.proc_locations.iteritems() ]
        return "%s : { %s }" % (self.id, ", ".join(proc_locations))
        
    

@contract(tat="is_ta_template", returns="set(is_location)")
def filter_locations_by_pc(tat, first_pc, last_pc):

    locations = set([])
    for curr in tat.locations:
        if curr.pc >= first_pc and curr.pc <= last_pc:

            locations.add(curr)

    return locations


@contract(locations="set(is_location)", returns="set(is_edge)")
def get_incoming_edges(locations):
    """
    Return the list of edges that come from locations outside the current set of locations and targets
    some state inside the passed set of locations
    """
    loc_names = map(lambda curr: curr.name, locations)
   
    edges = set([])
    for loc in locations:
 
        for e in loc.incoming:  
            if e.source.name not in loc_names:
                edges.add(e)

    return edges

@contract(locations="set(is_location)", returns="set(is_edge)")
def get_outgoing_edges(locations):
    """
    Return the list of edges that come from locations within the current set of locations and targets
    some state outside the passed set of locations
    """
    
    loc_names = map(lambda curr: curr.name, locations)
   
    edges = set([])
    for loc in locations:
 
        for e in loc.outgoing:  
            if e.target.name not in loc_names:
                edges.add(e)

    return edges

   
