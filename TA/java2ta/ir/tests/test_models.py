from time import sleep

from java2ta.ir.models import Project

import pkg_resources

def test_open_project():

    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("fooproject", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    sleep(1)

    assert p.is_status("opening") or p.is_status("open")
    is_open = p.is_open()

    if not is_open:
        sleep(1)
        assert p.is_open(), "Expected opened project. Got: %s" % p.status


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

    sleep(1)

    assert p.is_status("opening") or p.is_status("open")
   
    p.clean()

    assert p.is_status("closed")


def test_get_files():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("foo", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    sleep(1)

    assert p.is_open(), "Expected open project. Project status: %s" % p.status

    files = p.get_files()

    assert len(files) == 1, "Expected list containing one file. Got: %s" % files

    assert files[0] == "HelloWorld.java"
 

def test_get_mains():
 
    test_proj_path = pkg_resources.resource_filename(__name__, "helloworld")

    p = Project("foo", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    sleep(1)

    assert p.is_open(), "Expected open project. Project status: %s" % p.status

    mains = p.get_mains()

    assert len(mains) == 1, "Expected list containing one class with main method. Got: %s" % mains
    assert mains[0]["className"] == "HelloWorld"
    assert mains[0]["path"] == "HelloWorld.java"
    assert mains[0]["packageName"] == ""
