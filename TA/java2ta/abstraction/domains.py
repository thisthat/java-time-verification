class Domain(object):
    
    def __init__(self, values):
        self._default = values[0]
        self.values = set(values)

    @property
    def size(self):
        return len(self.values)

    @property
    def default(self):
        return self._default

INTEGERS = Domain(["null", "< 0", "== 0", "> 0", "max"])
POS_INTEGERS = Domain(["null", "== 0", "> 0", "max"])

STRING = Domain(["null", "not_null"])

COLLECTION = Domain(["null", "empty", "some_elements" ])
BOUNDED_COLLECTION = Domain(["null", "empty", "some_elements", "full" ])

BOOLEANS = Domain([ "true", "false" ])
