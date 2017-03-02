from java2ta.ir.client import RestfulAPIClient

def test_simple_client():

    c = RestfulAPIClient("localhost:9000", protocol="http")

    try:
        c.ping()
    except Exception, e:
        # this was not expected
        assert False, "The API restful service is expected to run when testing. Details: %s" % e

def test_client_bad_url():

    c = RestfulAPIClient("fakehostaddress:9000", protocol="http")

    try:
        c.ping()
        assert False, "The client should raise an exception when trying to contact a bad url"

    except Exception, e:
        # this was expected because the passed API url is intendedly wrong
        pass


