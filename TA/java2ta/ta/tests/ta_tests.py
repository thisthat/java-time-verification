from nose.tools import *

from java2ta.ta.models import Type, Variable, ClockVariable, Location, Edge, TA, NTA

def test_connected_locations():

    l1 = Location("L1")
    l2 = Location("L2")
    l3 = Location("L3")
    
    e1 = Edge(l1,l2)
    e2 = Edge(l2,l3)
    e3 = Edge(l3,l1)

    # test 1st edge: creating the edge does not modify the outcoing/incoming edges
    assert e1 not in l1.outgoing
    assert e1 not in l2.incoming

    assert len(l1.outgoing) == 0
    assert len(l2.incoming) == 0

    assert e1.source == l1
    assert e1.target == l2

    # test 2nd edge: same as before
    assert e2 not in l2.outgoing
    assert e2 not in l3.incoming

    assert len(l2.outgoing) == 0
    assert len(l3.incoming) == 0

    assert e2.source == l2
    assert e2.target == l3

    # test 3rd edge: ditto
    assert e3 not in l3.outgoing
    assert e3 not in l1.incoming

    assert len(l3.outgoing) == 0
    assert len(l1.incoming) == 0

    assert e3.source == l3
    assert e3.target == l1


def test_simple_ta():

    l1 = Location("L1")
    l2 = Location("L2")
    l3 = Location("L3")
    
    e1 = Edge(l1,l2)
    e2 = Edge(l2,l3)
    e3 = Edge(l3,l1)

    ta = TA("foo")
    ta.add_location(l1)
    ta.add_location(l2)
    ta.add_location(l3)

    # all locations are distinct
    assert len(ta.locations) == 3

    # inserting a duplicated location is ignored
    ta.add_location(l1)
    assert len(ta.locations) == 3

    ta.add_edge(e1)
    ta.add_edge(e2)
    ta.add_edge(e3)

    assert len(ta.edges) == 3

    # adding a duplicate edge is ignored
    ta.add_edge(e1)
    assert len(ta.edges) == 3

    # adding an edge modifies the set of incoming/outgoing edges to/from locations
    assert e1 in l1.outgoing
    assert e1 in l2.incoming
    assert e2 in l2.outgoing
    assert e2 in l3.incoming
    assert e3 in l3.outgoing
    assert e3 in l1.incoming


def test_ta_initial_loc():

    l1 = Location("L1", is_initial=True)

    assert l1.is_initial
    
    l2 = Location("L2")
    l3 = Location("L3")
    l3.is_initial = True
    
    e1 = Edge(l1,l2)

    ta = TA("foo")
    ta.add_location(l1)
    ta.add_location(l2)
    
    ta.add_edge(e1)

    assert ta.initial_loc == l1, "%s vs %s" % (ta.initial_loc, l1)
        
    try:
        ta.add_location(l3)
        assert False, "An exception was expected due to inserting a second initial state"
    except ValueError:
        # exception was expected
        pass

 
def test_dangling_edges():

    l1 = Location("L1")
    l2 = Location("L2")
    l3 = Location("L3")
    
    e1 = Edge(l1,l2)
    e2 = Edge(l2,l3)
    e3 = Edge(l3,l1)

    ta = TA("foo")
    ta.add_location(l1)
    ta.add_location(l2)

    ta.add_edge(e1)

    try:
        ta.add_edge(e2)
        assert False, "Expected exception because e2 target state is not in TA"
    except ValueError:
        # this was expected, because e2 target state is not in ta
        pass

    try:
        ta.add_edge(e3)
        assert False, "Expected exception because e3 source state is not in TA"
    except ValueError:
        # this was expected, because e3 source state is not in ta
        pass


def test_simple_nta():

    l1 = Location("L1", is_initial=True)
    l2 = Location("L2")
    l3 = Location("L3")
    
    e1 = Edge(l1,l2)
    e2 = Edge(l2,l3)
    e3 = Edge(l3,l1)

    ta = TA("foo")
    ta.add_location(l1)
    ta.add_location(l2)
    ta.add_location(l3)

    ta.add_edge(e1)
    ta.add_edge(e2)
    ta.add_edge(e3)

    nta = NTA()

    nta.add_ta(ta)

    assert len(nta.tas) == 1

    # adding twice the same TA is ignored
    nta.add_ta(ta)
    assert len(nta.tas) == 1
 

def test_nta_missing_initial():

    # a first timed automaton
    l1 = Location("L1", is_initial=True)
    assert l1.is_initial == True

    l2 = Location("L2")
    l3 = Location("L3")
    
    e1 = Edge(l1,l2)
    e2 = Edge(l2,l3)
    e3 = Edge(l3,l1)

    ta = TA("foo")
    ta.add_location(l1)
    ta.add_location(l2)
    ta.add_location(l3)

    ta.add_edge(e1)
    ta.add_edge(e2)
    ta.add_edge(e3)

    # a second timed automaton
    l12 = Location("L12")
    assert l12.is_initial == False
    
    l22 = Location("L22")
    l32 = Location("L32")
    
    e12 = Edge(l12,l22)
    e22 = Edge(l22,l32)
    e32 = Edge(l32,l12)

    ta2 = TA("foo 2")
    ta2.add_location(l12)
    ta2.add_location(l22)
    ta2.add_location(l32)

    ta2.add_edge(e12)
    ta2.add_edge(e22)
    ta2.add_edge(e32)
    
    # the network of TAs
    nta = NTA()

    nta.add_ta(ta)

    try:
        nta.add_ta(ta2)
        assert False, "An exception was expected because the added TA does not have an initial location"
    except ValueError:
        # this was excpected, because ta2 does not have an initia location
        pass
  
