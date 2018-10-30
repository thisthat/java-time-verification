from java2ta.commons.utility import *

def test_total_dict():
    d = TotalDict()
    d["p"] = TotalDict(__prefix__="p")
    d["q"] = TotalDict(__prefix__="q")
    
    assert len(d) == 2
    assert "p" in d
    assert "q" in d

    d["p"]["foo"] = 3
    assert len(d["p"]) == 1
    assert "foo" in d["p"]

    res = partial_format("ciao", d) 
    assert res == "ciao"
    res = partial_format("ciao {foo}", d) 
    assert res == "ciao {foo}", "Got: %s" % res

    res = partial_format("ciao {p.foo}", d) 
    assert res == "ciao 3", "Got: %s" % res

    res = partial_format("ciao {q.fie}", d) 
    assert res == "ciao {q.fie}", "Got: %s" % res


    assert "z" not in d

    try:
        res = partial_format("ciao {z.moo}", d)
        assert False, "Expected exception for namespace 'z' not present in TotalDict: %s" % d
    except ValueError:
        assert True
