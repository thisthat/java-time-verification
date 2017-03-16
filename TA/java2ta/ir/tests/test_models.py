from time import sleep

from java2ta.ir.models import Project, Thread

import pkg_resources

def check_is_open(project, max_seconds=30):
    num_attempts = max_seconds

    while num_attempts > 0 and not project.is_open():
        assert project.status in [ "open", "opening" ], "Expected project in 'opening' status. Got: '%s'" % project.status
        # repeat after a short time ...
        sleep(1)
        num_attempts = num_attempts - 1

    assert project.is_open(), "Process '%s' (path='%s',status='%s') took more that %s seconds to open, something is probably wrong ..." % (project.name, project.path, project.status, max_seconds)


def test_open_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    assert p.is_status("opening") or p.is_status("open")

    check_is_open(p)

def test_close_wrong_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("foofiedoesnotexist", "file:///%s" % test_proj_path, "localhost:9000")

    try:
        p.clean()
        assert False, "An exception was expected because it does not exist such a project"
    except Exception:
        # this was expected
        pass

def test_close_open_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    assert p.is_status("opening") or p.is_status("open")
   
    p.clean()

    assert p.is_status("closed")


def test_get_files():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    files = p.get_files()

    assert len(files) == 1, "Expected list containing one file. Got: %s" % files

    assert files[0] == "HelloWorld.java", files
 

def test_get_mains():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    mains = p.get_mains()

    assert len(mains) == 1, "Expected list containing one class with main method. Got: %s" % mains
    assert mains[0]["className"] == "HelloWorld", mains
    assert mains[0]["path"] == "HelloWorld.java", mains
    assert mains[0]["packageName"] == "", main

def test_open_project_bigger_concurrent():

    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
 
    files = p.get_files()
    assert len(files) == 51

    # test presence of some of the present files
    assert "Philosopher.java" in files
    assert "BoundedBuffer2.java" in files
    assert "LockTest.java" in files
    assert "LLSC.java" in files


def test_open_project_bigger_distributed():

    test_proj_path = pkg_resources.resource_filename(__name__, "dist-progs")

    p = Project("dist-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)

    files = p.get_files()
    assert len(files) == 90

    # test presence of some of the files
    assert "ShortestPath.java" in files, files
    assert "ElectionTester.java" in files, files
    assert "SeqMessage.java" in files, files
    assert "StableMutexTester.java" in files, files
    assert "MsgList.java" in files, files
    assert "Lock.java" in files, files

def check_file(file):

    assert "code" in file
    assert "methods" in file
    assert "imports" in file
    assert "abstract" in file
    assert "staticInit" in file
    assert "accessRight" in file
    assert "start" in file
    assert "end" in file
    assert file["end"] >= file["start"]
    assert "interface" in file  
    assert "implmentsInterfaces" in file # TODO: fix name implementsInterfaces
    assert "lineEnd" in file
    assert "importsAsString" in file
    assert "nodeType" in file
    assert "parent" in file
    assert "realPackageName" in file
    assert "path" in file
    assert "line" in file
    assert "allMethods" in file
    assert "name" in file
    assert "constraint" in file
    assert "packageName" in file
    assert "extendClass" in file
    assert "timeCritical" in file
    assert "attributes" in file

def test_get_files_dist():

    test_proj_path = pkg_resources.resource_filename(__name__, "dist-progs")

    p = Project("dist-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
 
    all_files_lock = p.get_file("Lock.java")

    assert isinstance(all_files_lock, list)

    file_lock = all_files_lock[0]

    check_file(file_lock)
    assert not file_lock["abstract"] 
    assert file_lock["interface"]
    assert file_lock["name"] == "Lock"
    assert file_lock["path"].endswith("Lock.java")
    assert len(file_lock["allMethods"]) == 2

    assert file_lock["allMethods"][0]["name"] in ["requestCS", "releaseCS"]
    assert file_lock["allMethods"][1]["name"] in ["requestCS", "releaseCS"]

    assert file_lock["implmentsInterfaces"] == [ "MsgHandler" ] # TODO fix name
    assert file_lock["extendClass"] == "Object"

    all_files_leader = p.get_file("RingLeader.java")
    assert isinstance(all_files_leader, list)
    file_leader = all_files_leader[0]
    check_file(file_leader)
    assert file_leader["implmentsInterfaces"] == [ "Election" ] # TODO fix name
    assert len(file_leader["attributes"]) == 4
    assert file_leader["attributes"][0]["name"] in [ "number", "leaderId", "next", "awake", ]
    assert file_leader["attributes"][1]["name"] in [ "number", "leaderId", "next", "awake", ]
    assert file_leader["attributes"][2]["name"] in [ "number", "leaderId", "next", "awake", ]
    assert file_leader["attributes"][3]["name"] in [ "number", "leaderId", "next", "awake", ]

    assert file_leader["extendClass"] == "Process"
    assert len(file_leader["allMethods"]) == 4
    assert file_leader["allMethods"][0]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"]
    assert file_leader["allMethods"][1]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"]
    assert file_leader["allMethods"][2]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"]
    assert file_leader["allMethods"][3]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"]


def test_get_files_by_type():

    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    files = p.get_files(type="Lock")

    assert len(files) == 5
    
    expected_classes = [ "SemaphoreLock", "Dekker", "Bakery", "HWMutex", "PetersonAlgorithm" ]
    expected_paths = [ "SemaphoreLock.java", "Dekker.java", "Bakery.java", "HWMutex.java", "PetersonAlgorithm.java" ]
    
    assert files[0]["className"] in expected_classes
    assert files[1]["className"] in expected_classes
    assert files[2]["className"] in expected_classes
    assert files[3]["className"] in expected_classes
    assert files[4]["className"] in expected_classes

    assert files[0]["path"] in expected_paths
    assert files[1]["path"] in expected_paths
    assert files[2]["path"] in expected_paths
    assert files[3]["path"] in expected_paths
    assert files[4]["path"] in expected_paths


def test_thread():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    threads = p.get_threads() 

    for ir in threads:

        t = Thread(ir["className"], ir["packageName"], project=p) 

        assert t.name == ir["className"]
        assert t.path == ir["path"]
        assert t.package_name == ir["packageName"]

        ast = t.ast
    
        assert isinstance(ast, dict), ast 
        assert ast["name"] == t.name
        assert ast["path"].endswith(t.path)
        assert ast["packageName"] == t.package_name


def test_methods():

    test_proj_path = pkg_resources.resource_filename(__name__, "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    threads = p.get_threads() 

    for ir in threads:

        t = Thread(ir["className"], ir["packageName"], project=p) 

        run_methods = t.get_methods("run")
    
        assert len(run_methods) == 1, "Every thread is supposed to have a run() method"

        constructors = t.get_methods(t.name)

        assert len(constructors) <= 1, "Every thread has at most one constructor, in this project"

        all_methods = t.methods
        assert len(all_methods) >= len(run_methods) + len(constructors)

    
