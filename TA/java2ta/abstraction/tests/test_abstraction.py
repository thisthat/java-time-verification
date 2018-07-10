from java2ta.abstraction.models import AbstractAttribute, StateSpace, Domain, Integer, LT, GT, Eq
from java2ta.abstraction.shortcuts import INTEGERS, split_numeric_domain

def test_non_local_abstract_attribute():

    predicates = [ Eq("{foo}",0), LT("{foo}",0), GT("{foo}",0) ]
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

    predicates = [ Eq("{foo}",0), LT("{foo}",0), GT("{foo}",0) ]
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

    predicates_foo = [ Eq("{foo}",0), LT("{foo}",0), GT("{foo}",0) ]
    dom_foo = Domain(Integer(), predicates_foo) # the first predicate is the default value for the domain
    foo = AbstractAttribute("foo", dom_foo, is_local=True) #values_foo, initial_foo)

    predicates_fie = [ Eq("{fie}",0), LT("{fie}",0), GT("{fie}",0) ]
    dom_fie = Domain(Integer(), predicates_fie) # the first predicate is the default value for the domain
    fie = AbstractAttribute("fie", dom_fie, is_local=True) #values_fie, initial_fie)

    ss = StateSpace()
    ss.add_attribute(foo)
    ss.add_attribute(fie)

    # check attributes are returned sorted by name
    assert ss.attributes == [ fie, foo ]
    # check proper attributes are returned
    assert ss.get_attribute("foo") == foo
    assert ss.get_attribute("fie") == fie

    assert isinstance(ss.enumerate, list), "Expected list. Got: %s" % ss.enumerate

    assert len(ss.enumerate) == 3 * 3 # 3 predicates for foo, 3 predicates for fie; 

    assert (foo.encoded_value(LT("{foo}", 0)), fie.encoded_value(GT("{fie}", 0))) in ss.enumerate, ss.enumerate
    assert (foo.encoded_value(Eq("{foo}", 0)), fie.encoded_value(Eq("{fie}", 0))) in ss.enumerate
    assert (LT("{fie}", 0), GT("{fie}", 0)) not in ss.enumerate
    assert (Eq("{foo}", 0), Eq("{foo}", 0)) not in ss.enumerate

    assert ss.value((0,0)) == (Eq("{fie}", 0), Eq("{foo}", 0)), ss.value((0,0))
    assert ss.value((0,1)) == (Eq("{fie}", 0), LT("{foo}", 0))
    assert ss.value((1,2)) == (LT("{fie}", 0), GT("{foo}", 0))

    assert ss.configuration([Eq("{fie}",0), Eq("{foo}",0)]) == (0,0)
    assert ss.configuration([Eq("{fie}",0), LT("{foo}",0)]) == (0,1)
    assert ss.configuration([Eq("{fie}",0), GT("{foo}",0)]) == (0,2)

    assert ss.configuration([GT("{fie}",0), Eq("{foo}",0)]) == (2,0)
    assert ss.configuration([GT("{fie}",0), LT("{foo}",0)]) == (2,1)
    assert ss.configuration([GT("{fie}",0), GT("{foo}",0)]) == (2,2)


