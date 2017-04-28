import string
import itertools

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

