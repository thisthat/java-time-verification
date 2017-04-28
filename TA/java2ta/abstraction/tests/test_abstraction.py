from java2ta.abstraction import AbstractAttribute, StateSpace
from java2ta.abstraction.domains import GT, LT, Eq, LTE, GTE, Between, \
                                split_numeric_domain

def test_abstract_attribute():

    values = [ "< 0", "== 0", "> 0" ]
    initial = "== 0"

    aa = AbstractAttribute("foo", values, initial)

    assert len(aa.encoded_values) == len(values)
    assert aa.values == sorted(values), "%s vs %s" % (aa.values, values)
    assert aa.initial != initial
    assert aa.initial in aa.encoded_values

def test_statespace():

    values_foo = [ "< 0", "== 0", "> 0" ]
    initial_foo = "== 0"

    foo = AbstractAttribute("foo", values_foo, initial_foo)

    values_fie = [ "A", "B", "C", "D", "[E-Z]" ]
    initial_fie = "A"

    fie = AbstractAttribute("fie", values_fie, initial_fie)

    ss = StateSpace()
    ss.add_attribute(foo)
    ss.add_attribute(fie)

    # check attributes are returned sorted by name
    assert ss.attributes == [ fie, foo ]
    # check proper attributes are returned
    assert ss.get_attribute("foo") == foo
    assert ss.get_attribute("fie") == fie

    assert isinstance(ss.enumerate, list), "Expected list. Got: %s" % ss.enumerate

    assert len(ss.enumerate) == len(values_foo) * len(values_fie)
    assert (fie.encoded_value("A"), foo.encoded_value("> 0")) in ss.enumerate, ss.enumerate
    assert (fie.encoded_value("A"), foo.encoded_value("== 0")) in ss.enumerate
    assert ("A", "> 0") not in ss.enumerate
    assert ("A", "== 0") not in ss.enumerate
    assert ("A", "B") not in ss.enumerate
    assert ("> 0", "== 0") not in ss.enumerate

    assert ss.value((0,0)) == ("A", "< 0"), ss.value((0,0))
    assert ss.value((0,1)) == ("A", "== 0")
    assert ss.value((1,2)) == ("B", "> 0")

    assert ss.configuration(["A", "< 0"]) == (0,0)
    assert ss.configuration(["A", "== 0"]) == (0,1)
    assert ss.configuration(["A", "> 0"]) == (0,2)

    assert ss.configuration(["C", "< 0"]) == (2,0)
    assert ss.configuration(["C", "== 0"]) == (2,1)
    assert ss.configuration(["C", "> 0"]) == (2,2)

def test_pred_gt():

    gt = GT({"value":0})
    label = gt.label(var="foo")
    assert label == "foo > 0"
        
    smt_assert = gt.smt_assert(var="fie")
    assert smt_assert == "(assert (> fie 0))"
 
def test_pred_gte():

    gte = GTE({"value":0})
    label = gte.label(var="foo")
    assert label == "foo >= 0"
        
    smt_assert = gte.smt_assert(var="fie")
    assert smt_assert == "(assert (>= fie 0))"
 
def test_pred_lt():

    lt = LT({"value":0})
    label = lt.label(var="foo")
    assert label == "foo < 0"
        
    smt_assert = lt.smt_assert(var="fie")
    assert smt_assert == "(assert (< fie 0))"
 
def test_pred_lte():

    lte = LTE({"value":0})
    label = lte.label(var="foo")
    assert label == "foo <= 0", label
        
    smt_assert = lte.smt_assert(var="fie")
    assert smt_assert == "(assert (<= fie 0))", smt_assert
 
def test_pred_eq():

    eq = Eq({"value":0})
    label = eq.label(var="foo")
    assert label == "foo = 0", label
        
    smt_assert = eq.smt_assert(var="fie")
    assert smt_assert == "(assert (= fie 0))", smt_assert
 
 
def test_pred_between():

    eq = Between({"min":0, "max":100})
    label = eq.label(var="foo")
    assert label == "0 < foo < 100", label
        
    smt_assert = eq.smt_assert(var="fie")
    assert smt_assert == "(assert (and (< 0 fie) (< fie 100)))", smt_assert
 

def test_split_numeric_domain():
    """
    When passing X distinct and sorted numbers to split the entire
    [-inf,+inf] numeric interval, we expect to get 2*X+1 predicates:
    - less than the minimum
    - greater than the maximum
    - equal to each item of X
    - between x and y, where x and y are two adjacent items of X
    """
    values = [-5,0,1,2,10]
    pred = split_numeric_domain(values)

    assert len(pred) == 11
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo < -5" in labels
    assert "foo = -5" in labels
    assert "-5 < foo < 0" in labels
    assert "foo = 0" in labels
    assert "0 < foo < 1" in labels
    assert "foo = 1" in labels
    assert "1 < foo < 2" in labels
    assert "foo = 2" in labels
    assert "2 < foo < 10" in labels
    assert "foo = 10" in labels
    assert "foo > 10" in labels


def test_split_numeric_domain_duplicates():
    """
    When passing a list of numbers with duplicates, we expect to obtain
    the same predicates as when passing the list with only distinct
    numbers.
    """
    values = [-5,0,1,1,2,10]
    pred = split_numeric_domain(values)

    assert len(pred) == 11
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo < -5" in labels
    assert "foo = -5" in labels
    assert "-5 < foo < 0" in labels
    assert "foo = 0" in labels
    assert "0 < foo < 1" in labels
    assert "foo = 1" in labels
    assert "1 < foo < 2" in labels
    assert "foo = 2" in labels
    assert "2 < foo < 10" in labels
    assert "foo = 10" in labels
    assert "foo > 10" in labels



def test_split_numeric_domain_unsorted():
    """
    If we pass a list of unsorted numbers to split the numeric
    interval, we expect to obtain the same predicates as the
    ones obtained when the same numbers are passed in order.
    """
    values = [10,-5,2,0,1,]
    pred = split_numeric_domain(values)

    assert len(pred) == 11
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo < -5" in labels
    assert "foo = -5" in labels
    assert "-5 < foo < 0" in labels
    assert "foo = 0" in labels
    assert "0 < foo < 1" in labels
    assert "foo = 1" in labels
    assert "1 < foo < 2" in labels
    assert "foo = 2" in labels
    assert "2 < foo < 10" in labels
    assert "foo = 10" in labels
    assert "foo > 10" in labels


def test_split_numeric_domain_small():
    """
    If we split the numeric domain using only one
    value, we expect to get 3 predicates:
    - less than the specified number
    - equal to the specified number
    - greater than the specified number
    """
    values = [0]
    pred = split_numeric_domain(values)

    assert len(pred) == 3
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo < 0" in labels
    assert "foo = 0" in labels
    assert "foo > 0" in labels


def test_split_numeric_domain_no_value():
    """
    If we try to split a numeric interval without specifying
    any number, simply no predicate is generated.
    """
    pred = split_numeric_domain([])

    assert len(pred) == 0
