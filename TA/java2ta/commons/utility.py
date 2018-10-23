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
    the defined keys. This is a different object that behaves like a total dictionary, i.e. it 
    returns a value for every key that it is asked. The returned value is:
    - the value, if the key is defined
    - the key, otherwise

    This dictionary is used to partially format strings when not all parameters
    are available at the same time.

    credits:
    http://stackoverflow.com/a/11284026/374430
    """

    def __init__(self, *args, **kwargs):
        self.__prefix__ = kwargs.pop("__prefix__","")
        super(TotalDict, self).__init__(*args, **kwargs)

    def __missing__(self, key):
        res = "{%s}" % key
        if self.__prefix__:
            res = "{%s.%s}" % (self.__prefix__, key)

        return res

    def __getattr__(self, key, *args, **kwargs):
        return self.get(key, self.__missing__(key))



def partial_format(toformat, ctx):
    pctx = TotalDict(**ctx)
    formatter = string.Formatter()

    try:
        res = formatter.vformat(toformat, (), pctx) 
        return res
    except AttributeError, e:
        raise ValueError("The string to format ('%s') probably contains a reference to a namespace not present in the context (%s)" % (toformat, ctx))


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

