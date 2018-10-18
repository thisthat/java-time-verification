import string
import itertools
from contracts import contract, new_contract

def new_contract_check_type(contract_name, my_type):
    def contract_check_type(obj):
        if not isinstance(obj, my_type):
            raise ValueError("Expected object of type '%s'. Got: '%s'" % (my_type, type(obj)))

    return new_contract(contract_name, contract_check_type)

class TotalDict(dict):
    """
    A usual python dictionary is like a "partial" function, returning a value only for 
    the defined keys. This is a total extension of a usual dictionary, returning:
    - the value, if the key is defined
    - the key, otherwise

    This dictionary is used to partially format strings when not all parameters
    are available at the same time.

    credits:
    http://stackoverflow.com/a/11284026/374430
    """

    def __missing__(self, key):
        return "{" + key + "}"

    # the following, allow to access a key either as foo.key or as foo["key"]
    # this is useful for the dotted notation used in abstraction predicates
    __getattr__= dict.__getitem__
    __setattr__= dict.__setitem__
    __delattr__= dict.__delitem__



def partial_format(toformat, ctx):
    pctx = TotalDict(**ctx)
    formatter = string.Formatter()

    return formatter.vformat(toformat, (), pctx)

def pairwise_iter(iterable):
    """
    Iterates a sequence s returning at each step two items:
    - current item
    - next item
    The successive invokation of next, the next item becomes the 
    current one.
    Example:

    s  -> (s0,s1), (s1,s2), (s2, s3), ...

    Credits:
    http://stackoverflow.com/a/5434936/374430
    """
    a, b = itertools.tee(iterable)
    next(b, None)
    return itertools.izip_longest(a, b)

