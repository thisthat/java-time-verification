from java2ta.abstraction.models import AbstractAttribute, StateSpace, Domain, Integer, LT, GT, Eq
from java2ta.abstraction.shortcuts import INTEGERS, split_numeric_domain

def test_non_local_abstract_attribute():

    predicates = [ Eq("{lhs}",0), LT("{lhs}",0), GT("{lhs}",0) ]
    dom = Domain(Integer(), predicates)

    aa = AbstractAttribute("foo", dom, is_local=False)

    assert aa.name == "foo"
    assert aa.variables == [ "foo", ]
    assert aa.is_local == False
    assert aa.values == predicates
    assert len(aa.encoded_values) == len(predicates)
    assert len(aa.initial) == len(predicates)
    assert aa.initial == aa.encoded_values

def test_local_abstract_attribute():

    predicates = [ Eq("{lhs}",0), LT("{lhs}",0), GT("{lhs}",0) ]
    dom = Domain(Integer(), predicates) # the first predicate is the default value for the domain

    aa = AbstractAttribute("foo", dom, is_local=True)

    assert aa.name == "foo"
    assert aa.variables == [ "foo", ]
    assert aa.is_local == True
    assert aa.values == predicates
    assert len(aa.encoded_values) == len(predicates)
    assert len(aa.initial) == 1

    eq = predicates[0]
    assert eq in aa.rev_domain
    assert aa.initial == [ aa.rev_domain[eq] ]


def test_statespace():

    foo = AbstractAttribute("foo", INTEGERS, is_local=True) #values_foo, initial_foo)
    fie = AbstractAttribute("fie", INTEGERS, is_local=True) #values_fie, initial_fie)

    ss = StateSpace()
    ss.add_attribute(foo)
    ss.add_attribute(fie)

    # check attributes are returned sorted by name
    assert ss.attributes == [ fie, foo ]
    # check proper attributes are returned
    assert ss.get_attribute("foo") == foo
    assert ss.get_attribute("fie") == fie

    assert isinstance(ss.enumerate, list), "Expected list. Got: %s" % ss.enumerate

    assert len(ss.enumerate) == len(INTEGERS.values) * len(INTEGERS.values) #len(values_foo) * len(values_fie)
#    assert (fie.encoded_value("A"), foo.encoded_value("> 0")) in ss.enumerate, ss.enumerate
    assert (fie.encoded_value(LT(rhs=0)), foo.encoded_value(GT(rhs=0))) in ss.enumerate, ss.enumerate
    assert (fie.encoded_value(Eq(rhs=0)), foo.encoded_value(Eq(rhs=0))) in ss.enumerate
    assert (LT(rhs=0), GT(rhs=0)) not in ss.enumerate
    assert (Eq(rhs=0), Eq(rhs=0)) not in ss.enumerate

    assert ss.value((0,0)) == (LT(rhs=0), LT(rhs=0)), ss.value((0,0))
    assert ss.value((0,1)) == (LT(rhs=0), Eq(rhs=0))
    assert ss.value((1,2)) == (Eq(rhs=0), GT(rhs=0))

    assert ss.configuration([LT(rhs=0), LT(rhs=0)]) == (0,0)
    assert ss.configuration([LT(rhs=0), Eq(rhs=0)]) == (0,1)
    assert ss.configuration([LT(rhs=0), GT(rhs=0)]) == (0,2)

    assert ss.configuration([GT(rhs=0), LT(rhs=0)]) == (2,0)
    assert ss.configuration([GT(rhs=0), Eq(rhs=0)]) == (2,1)
    assert ss.configuration([GT(rhs=0), GT(rhs=0)]) == (2,2)


