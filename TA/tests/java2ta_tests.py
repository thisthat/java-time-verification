from nose.tools import *
import java2ta

from java2ta.ir.parser import Parser

def setup():
    print "SETUP!"

def teardown():
    print "TEAR DOWN!"

def test_basic():

    test1_path = "tests/examples/Philosopher.json"
    test2_path = "tests/examples/DiningPhilosopher.json"
    fake_path = "tests/examples/foofie.json"

    p = Parser()
    obj1 = p.parse(test1_path)
    assert obj1 is not None
    assert isinstance(obj1, dict)

    obj2 = p.parse(test2_path)
    assert obj2 is not None
    assert isinstance(obj2, dict)

    try:
        fake = p.parse(fake_path)
        assert False, "An exception was expected because the path does not exist"
    except IOError:
        # this was expected
        pass

