import requests

class APIError(Exception):
    pass

class RestfulAPIClient(object):

    def __init__(self, base_url, protocol="http"):
    
        self.base_url = base_url
        self.protocol = protocol

    def ping(self):
    
        url = "%s://%s/" % (self.protocol, self.base_url)

        resp = requests.get(url)

        if resp.status_code != 200:
            raise APIError("API service not available")

    def get(self, url, **data):

        if url.startswith("/"):
            url = url[1:]

        full_url = "%s://%s/%s" % (self.protocol, self.base_url, url)

        if data:
            querystring = []

            for (var,val) in data.iteritems():
                querystring.append("%s=%s" % (var,val))

            full_url = "%s?%s" % (full_url, "&".join(querystring))

        resp = requests.get(full_url)

        if resp.status_code != 200:
            raise ApiError("GET /{}/ {}".format(url, resp.status_code))
            
        return resp.json()



    def post(self, url, data):
        
        if url.startswith("/"):
            url = url[1:]

        full_url = "%s://%s/%s" % (self.protocol, self.base_url, url)
        resp = requests.post(full_url, data)

        if resp.status_code != 200:
            raise ApiError("POST /{}/ {}".format(url, resp.status_code))

        print "returned text:\n%s" % resp.text
            
        return resp.json()

    
    def delete(self, url, **data):
        raise ValueError("Method not supported, yet")


    def put(self, url, **data):
        raise ValueError("Method not supported, yet")

        
