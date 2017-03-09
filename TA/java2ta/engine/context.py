from java2ta.engine.exceptions import ObjectDoesNotExist, EmptyContext

class Context(object):
    """
    A context is a stack of environments, each mapping variables to values.
    """

   
    def __init__(self, initial_env=None):
    
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

    def get(self, name, default=None):

        found = False
        value = default

        for env in reversed(self._context):
            if name in env:
                found = True
                value = env[name]
                break

        return value

##        if found:
##            return value
##        else:
##            raise ObjectDoesNotExist

    def top(self):

        if len(self._context) == 0:
            raise EmptyContext("An empty context has no top environment")

        return next(reversed(self._context))

    def update(self, name, value):
        """
        Update the environment on top of the context adding/updating name 
        """

        top = self.top()
        top[name] = value
        
    
