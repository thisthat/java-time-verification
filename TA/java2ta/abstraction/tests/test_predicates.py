from java2ta.abstraction.models import PredicateParser, LT, GT, Eq, And

def test_predicate_parser():

    pp = PredicateParser()
    pred = pp.parse("(< 3 2)")   

    assert isinstance(pred, LT)
    assert pred.label() == "<(3,2)"
    assert pred.smt_assert() == "(assert (< 3 2))"
    assert pred.var_names == set([])

    pred1 = pp.parse("(> {lhs} 0)")
    assert isinstance(pred1, GT)
    assert pred1.label() == ">({lhs},0)"
    assert pred1.smt_assert() == "(assert (> {lhs} 0))"
    assert pred1.var_names == set(["lhs"]) #set(["{lhs}"])

    assert pred1.label(lhs="foo") == ">(foo,0)"
    assert pred1.smt_assert(lhs="foo") == "(assert (> foo 0))"

def test_parse_complex_predicate():

    pp = PredicateParser()
    pred = pp.parse("(and (< {foo} 3) (> {fie} 2)")   

    assert isinstance(pred, And)
    assert pred.label() == "and(<({foo},3),>({fie},2))"
    assert pred.smt_assert() == "(assert (and (< {foo} 3) (> {fie} 2)))", pred.smt_assert()
    assert pred.var_names == set(["foo", "fie"])  #set(["{foo}","{fie}"])

    assert pred.label(foo="a", fie="b") == "and(<(a,3),>(b,2))"
    assert pred.smt_assert(foo="a",fie="b") == "(assert (and (< a 3) (> b 2)))"

    pred.rename(foo="var")
    assert pred.smt_assert() == "(assert (and (< {var} 3) (> {fie} 2)))", pred.smt_assert()
    assert pred.var_names == set(["var","fie"]) #set(["{var}","{fie}"])

    assert pred.label(foo="a", fie="b") == "and(<({var},3),>(b,2))" # do not instantiate {var}, but {foo}
    assert pred.smt_assert(foo="a",fie="b") == "(assert (and (< {var} 3) (> b 2)))"
  
 
