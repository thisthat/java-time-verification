import requests

import logging

log = logging.getLogger(__name__)

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

    def get(self, url, data={}):

        if url.startswith("/"):
            url = url[1:]

        full_url = "%s://%s/%s" % (self.protocol, self.base_url, url)

        if data:
            querystring = []

            for (var,val) in data.iteritems():
                querystring.append("%s=%s" % (var,val))

            full_url = "%s?%s" % (full_url, "&".join(querystring))

        log.info("GET: url=%s" % (full_url,))

        resp = requests.get(full_url)

        debug_text = resp.text
        if len(debug_text) > 100:
            debug_text = debug_text[:100] + "..."

        log.info("Response: (%s) %s" % (resp.status_code, debug_text))

        if resp.status_code != 200:
            raise APIError("GET /{}/ {}".format(url, resp.status_code))
    
        log.debug("JSON: %s" % (resp.json(),))        
        return resp.json()

    def post(self, url, data={}):
        
        if url.startswith("/"):
            url = url[1:]

        full_url = "%s://%s/%s" % (self.protocol, self.base_url, url)

        log.debug("POST: url=%s, data=%s" % (full_url,data))
        resp = requests.post(full_url, data)

        debug_text = resp.text
#        if len(debug_text) > 100:
#            debug_text = debug_text[:100] + "..."
        log.debug("Response: (%s) %s" % (resp.status_code, debug_text))

        if resp.status_code != 200:
            raise APIError("POST {} {} : {}".format(full_url, resp.status_code, data))

#        print "returned text:\n%s" % resp.text
        log.debug("JSON: %s" % (resp.json(),))        
        return resp.json()

    
    def delete(self, url, data={}):
        raise ValueError("Method not supported, yet")


    def put(self, url, data={}):
        raise ValueError("Method not supported, yet")

        
