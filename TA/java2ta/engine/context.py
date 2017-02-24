from java2ta.engine.exceptions import ObjectDoesNotExist, EmptyContext

class Context(object):
    """
    A context is a stack of environments, each mapping variables to values.
    """

   
    def __init__(self):

        self._context = []

    def push(self, ctx):
        
        self._context.append(ctx)

    def pop(self):

        res = None
        try:
            res = self._context.pop()
        except IndexError:
            raise EmptyContext("Cannot pop from empty context")

        return res

    def get(self, name):

        found = False
        value = None

        for env in reversed(self._context):
            if name in env:
                found = True
                value = env[name]
                break

        if found:
            return value
        else:
            raise ObjectDoesNotExist
        
    
