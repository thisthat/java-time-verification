from time import sleep

from java2ta.ir.models import Project

import pkg_resources

def check_is_open(project):
    num_attempts = 30

    while num_attempts > 0 and not project.is_open():
        # repeat after a short time ...
        sleep(1)
        num_attempts = num_attempts - 1

    assert project.is_open(), "Process '%s' (path='%s',status='%s') took more that %s seconds to open, something is probably wrong ..." % (project.name, project.path, project.status, num_attempts)


def test_open_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("fooproject", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    assert p.is_status("opening") or p.is_status("open")

    check_is_open(p)

def test_close_wrong_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworlds")

    p = Project("foofiedoesnotexist", "file:///%s" % test_proj_path, "localhost:9000")

    try:
        p.clean()
        assert False, "An exception was expected because it does not exist such a project"
    except Exception:
        # this was expected
        pass

def test_close_open_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("fie", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    assert p.is_status("opening") or p.is_status("open")
   
    p.clean()

    assert p.is_status("closed")


def test_get_files():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("foo", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    files = p.get_files()

    assert len(files) == 1, "Expected list containing one file. Got: %s" % files

    assert files[0] == "HelloWorld.java", files
 

def test_get_mains():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("foo", "file://%s" % test_proj_path, "localhost:9000")

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


def test_get_file():

    test_proj_path = pkg_resources.resource_filename(__name__, "dist-progs")

    p = Project("dist-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
 
    file = p.get_file("Lock.java")
    assert isinstance(file, dict)
