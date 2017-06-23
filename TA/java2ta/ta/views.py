from jinja2 import Environment, PackageLoader, select_autoescape

from contracts import contract

from java2ta.abstraction.domains import is_domain

class GraphViz(object):

    template = "finite_state_automaton.gv"

    def __init__(self, ta):
        self.ta = ta

    @contract(attributes="dict(str:is_domain)")
    def render(self, attributes):
        env = Environment(
            loader=PackageLoader("java2ta.ta", "templates"),
        )
        template = env.get_template(self.template)

        ctx = {
            "ta": self.ta,  
            "attributes": attributes, 
        }
        return template.render(**ctx)
