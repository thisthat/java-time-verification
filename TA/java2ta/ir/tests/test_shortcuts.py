import pkg_resources
from java2ta.ir.models import Project, Method, Klass
from java2ta.ir.shortcuts import *

def test_analyze_assignment():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    t = Klass("HelloWorld","","HelloWorld.java", project=p)
    run_method = Method("time_method", t)

#    (found_identifiers, found_method_calls) = analyze_assignment(run_method.ast)
    now_methods = set([ "java.lang.System.currentTimeMillis" ])
    var_timestamps = get_timestamps(run_method, now_methods)

#    assert isinstance(found_identifiers, set), found_identifiers
#    assert found_identifiers == set([ "a", "b", "c", "x", "System", "Thread" ]), found_identifiers

    # the method has also a variable "a" and a parameter "x" which are not timestamps, but they
    # are used to determine the value of the timestamps "b" and "c"
    assert set(var_timestamps.keys()) == set(["b","c"]), var_timestamps
    assert len(var_timestamps["b"]) == 2, var_timestamps["b"] # b is a timestamp appearing in two assignments/declarations
    assert len(var_timestamps["c"]) == 1, var_timestamps["c"] # c is a timestamp appearing in one assignment

    assert var_timestamps["b"][0]["code"] == "System.currentTimeMillis()", var_timestamps["b"][0]
    assert var_timestamps["b"][0]["line"] in [ 42, 47 ], var_timestamps["b"][0]
    assert var_timestamps["b"][1]["code"] == "System.currentTimeMillis()", var_timestamps["b"][1]
    assert var_timestamps["b"][1]["line"] in [ 42, 47 ], var_timestamps["b"][1]
    assert var_timestamps["b"][0]["line"] != var_timestamps["b"][1]["line"], var_timestamps["b"]

    assert var_timestamps["c"][0]["code"] == "b + a", var_timestamps["c"][0]
    assert var_timestamps["c"][0]["line"] in [ 43 ], var_timestamps["c"][0]


def test_check_now_assignments():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    t = Klass("HelloWorld","","HelloWorld.java", project=p)
    run_method = Method("time_method", t)

#    (found_identifiers, found_method_calls) = analyze_assignment(run_method.ast)
    now_methods = set([ "java.lang.System.currentTimeMillis" ])
    var_timestamps = get_timestamps(run_method, now_methods)

    assert "b" in var_timestamps
    assert "c" in var_timestamps

    res_b = check_now_assignments(var_timestamps["b"], now_methods)

    assert res_b == True

    res_c = check_now_assignments(var_timestamps["c"], now_methods)
    
    assert res_c == False

    # this is expected to return False, because the list of now_methods is empty
    res_b_wrong = check_now_assignments(var_timestamps["b"], set([]))
    assert res_b_wrong == False 


    # this is expected to return False, because the list of nodes is empty
    res_empty = check_now_assignments([], now_methods)
    assert res_empty == False
