from java2ta.abstraction.domains import GT, LT, Eq, NotEq, LTE, GTE, Between, \
                                split_numeric_domain, split_eq_value, split_enum, split_field_domain, \
                                Integer, Real, DataTypeFactory

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
 
def test_pred_not_eq():

    neq = NotEq({"value":0})
    label = neq.label(var="foo")
    assert label == "foo != 0", label
        
    smt_assert = neq.smt_assert(var="fie")
    assert smt_assert == "(assert (distinct fie 0))", smt_assert
 
 
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


def test_split_numeric_domain_upper_bounded():
    """
    In an upper-bounded numeric domain, we don't have values that are greater
    than the greatest value passed
    """
    values = [-5, 0, 10]
    pred = split_numeric_domain(values, gt_max=False)

    assert len(pred) == 6
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo < -5" in labels
    assert "foo = -5" in labels
    assert "-5 < foo < 0" in labels
    assert "foo = 0" in labels
    assert "0 < foo < 10" in labels
    assert "foo = 10" in labels

    assert "(assert (< foo -5))" in smt_asserts
    assert "(assert (= foo -5))" in smt_asserts
    assert "(assert (and (< -5 foo) (< foo 0)))" in smt_asserts
    assert "(assert (= foo 0))" in smt_asserts
    assert "(assert (and (< 0 foo) (< foo 10)))" in smt_asserts
    assert "(assert (= foo 10))" in smt_asserts


def test_split_numeric_domain_lower_bounded():
    """
    In a lower-bounded numeric domain, we don't have values that are smaller
    than the smallest value passed
    """
    values = [-5, 0, 10]
    pred = split_numeric_domain(values, lt_min=False)

    assert len(pred) == 6
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo = -5" in labels
    assert "-5 < foo < 0" in labels
    assert "foo = 0" in labels
    assert "0 < foo < 10" in labels
    assert "foo = 10" in labels
    assert "foo > 10" in labels

    assert "(assert (= foo -5))" in smt_asserts
    assert "(assert (and (< -5 foo) (< foo 0)))" in smt_asserts
    assert "(assert (= foo 0))" in smt_asserts
    assert "(assert (and (< 0 foo) (< foo 10)))" in smt_asserts
    assert "(assert (= foo 10))" in smt_asserts
    assert "(assert (> foo 10))" in smt_asserts


def test_split_numeric_domain_lower_uppser_bounded():
    """
    In a lower-/upper- bounded numeric domain, we don't have values that are smaller
    (resp. greater) than the smallest (resp. greatest) value passed
    """
    values = [-5, 0, 10]
    pred = split_numeric_domain(values, lt_min=False, gt_max=False)

    assert len(pred) == 5
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo = -5" in labels
    assert "-5 < foo < 0" in labels
    assert "foo = 0" in labels
    assert "0 < foo < 10" in labels
    assert "foo = 10" in labels

    assert "(assert (= foo -5))" in smt_asserts
    assert "(assert (and (< -5 foo) (< foo 0)))" in smt_asserts
    assert "(assert (= foo 0))" in smt_asserts
    assert "(assert (and (< 0 foo) (< foo 10)))" in smt_asserts
    assert "(assert (= foo 10))" in smt_asserts


def test_split_value_equality():

    pred = split_eq_value(10)

    assert len(pred) == 2
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo = 10" in labels
    assert "foo != 10" in labels

    assert "(assert (= foo 10))" in smt_asserts
    assert "(assert (distinct foo 10))" in smt_asserts

def test_split_value_equality_enumeration():

    values = [-5,0,100]
    pred = split_enum(values)

    assert len(pred) == len(values)
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    assert "foo = -5" in labels
    assert "foo = 0" in labels
    assert "foo = 100" in labels

    assert "(assert (= foo -5))" in smt_asserts, smt_asserts
    assert "(assert (= foo 0))" in smt_asserts, smt_asserts
    assert "(assert (= foo 100))" in smt_asserts, smt_asserts



def test_split_field_domain():
    values = [-5,0,100]
    pred = split_enum(values)

    assert len(pred) == len(values)
    
    labels = map(lambda p: p.label(var="foo"), pred)
    smt_asserts = map(lambda p: p.smt_assert(var="foo"), pred)

    field_pred = split_field_domain("myfield", pred)

    assert len(pred) == len(field_pred)
    field_labels = map(lambda p: p.label(var="fie"), field_pred)
    field_smt_asserts = map(lambda p: p.smt_assert(var="fie"), field_pred)

    assert "fie.myfield = -5" in field_labels, field_labels
    assert "fie.myfield = 0" in field_labels
    assert "fie.myfield = 100" in field_labels

    assert "(assert (= (myfield fie) -5))" in field_smt_asserts, field_smt_asserts
    assert "(assert (= (myfield fie) 0))" in field_smt_asserts, field_smt_asserts
    assert "(assert (= (myfield fie) 100))" in field_smt_asserts, field_smt_asserts


def test_split_numeric_domain_no_value():
    """
    If we try to split a numeric interval without specifying
    any number, simply no predicate is generated.
    """
    pred = split_numeric_domain([])

    assert len(pred) == 0

def test_integer_types():

    fac = DataTypeFactory.the_factory()

    int1 = fac.from_fqn("java.lang.Integer")
    int2 = fac.from_fqn("int")
    int3 = fac.from_fqn("long")
    int4 = fac.from_fqn("java.lang.Long")

    # all integer types are mapped to the same smt type
    assert int1.__class__ == int2.__class__
    assert int2.__class__ == int3.__class__, "%s vs %s" % (int2, int3)
    assert int3.__class__ == int4.__class__

    assert isinstance(int3, Integer)


def test_data_type_factory_consistency():

    fac = DataTypeFactory.the_factory()

    real1 = fac.from_fqn("java.lang.Double")
    real2 = fac.from_fqn("java.lang.Double")

    # when the same fully-qualified type name is enquired, the
    # same DataType instance is returned
    assert real1 == real2


def test_data_type_factory_distinction():

    fac = DataTypeFactory.the_factory()

    real1 = fac.from_fqn("java.lang.Double")
    real2 = fac.from_fqn("java.lang.Float")

    int1 = fac.from_fqn("java.lang.Integer")
    int2 = fac.from_fqn("int")

    # different fully-qualified data-type names correspond to different
    # DataType instances, even though the latter may share the same
    # type
    assert int1.__class__ == int2.__class__
    assert int1 != int2

    assert real1.__class__ == real2.__class__
    assert real1 != real2
