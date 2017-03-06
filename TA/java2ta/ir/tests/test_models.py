from time import sleep

from java2ta.ir.models import Project

def test_open_project():

    p = Project("fooproject", "file:///home/spegni/git/safe-rtse/java-xal/TA/java2ta/ir/tests/javaproject/", "localhost:9000")

    assert p.is_status("closed")
    p.open()

    sleep(2)

    assert p.is_status("opening") or p.is_status("open")


