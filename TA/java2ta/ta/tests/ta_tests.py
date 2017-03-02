from nose.tools import *

from java2ta.ta.models import ClockExpression, Type, Variable, ClockVariable, Location, Edge, TA, NTA

def test_connected_locations():

    l1 = Location("L1")
    l2 = Location("L2")
    l3 = Location("L3")
    
    e1 = Edge(l1,l2)
    e2 = Edge(l2,l3)
    e3 = Edge(l3,l1)

    # test 1st edge
    assert e1 in l1.outgoing
    assert e1 in l2.incoming

    assert len(l1.outgoing) == 1
    assert len(l2.incoming) == 1

    assert e1.source == l1
    assert e1.target == l2

    # test 2nd edge
    assert e2 in l2.outgoing
    assert e2 in l3.incoming

    assert len(l2.outgoing) == 1
    assert len(l3.incoming) == 1

    assert e2.source == l2
    assert e2.target == l3

    # test 3rd edge
    assert e3 in l3.outgoing
    assert e3 in l1.incoming

    assert len(l3.outgoing) == 1
    assert len(l1.incoming) == 1

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
