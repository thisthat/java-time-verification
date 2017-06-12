

class PC(object):
    """
    This class encapsulates the operation of manipulating a program counter
    in our framework.
    """

    def __init__(self, initial="0"):
        assert isinstance(initial, int) or isinstance(initial, basestring)

        if isinstance(initial, int):
            initial = str(initial)        

        self.pc = initial


    def __str__(self):
        return "@%s" % self.pc


    def __repr__(self):
        return "@%s" % self.pc

    def __add__(self, other):
        
        assert isinstance(other, int)
        assert other >= 0

        new = PC(initial=self.pc)
        new.inc(other)
        
        return new


    def inc(self, to_add=1):
        assert self.pc != None
        assert isinstance(to_add, int)
        assert to_add >= 0

        prefix = ""
        last = ""
    
        parts = self.pc.rsplit(".", 1)
    
        if len(parts) == 1:
            last = parts[0]
        else:
            prefix, last = parts
    
        res = str(int(last) + to_add)
        if len(prefix) > 0:
            res = "%s.%s" % (prefix, res)

        self.pc = res
    
    def push(self, new):
        assert self.pc != None
        
        self.pc = "%s.%s" % (self.pc, new)
        return self
    
    def pop(self):
        assert self.pc != None

        parts = self.pc.rsplit(".", 1)
    
        if len(parts) <= 1:
            raise ValueError("Cannot pop pc value: %s" % self.pc)
    
        self.pc = parts[0] 
        return parts[1]
    
