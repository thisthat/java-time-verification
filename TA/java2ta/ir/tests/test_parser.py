from nose.tools import *
import java2ta
import pkg_resources
import os

from java2ta.ir.parser import Parser

def setup():
    print "SETUP!"

def teardown():
    print "TEAR DOWN!"

def test_basic():

    base_path = pkg_resources.resource_filename(__name__, "examples")

    test1_path = os.path.join(base_path, "Philosopher.json")
    test2_path = os.path.join(base_path, "DiningPhilosopher.json")
    fake_path = os.path.join(base_path, "foofie.json")

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

