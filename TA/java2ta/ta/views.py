from jinja2 import Environment, PackageLoader, select_autoescape

from contracts import contract


class GraphViz(object):

    template = "finite_state_automaton.gv"

    def __init__(self, ta):
        self.ta = ta

    @contract(legend="None|list(tuple(string,string))")
    def render(self, legend):
        env = Environment(
            loader=PackageLoader("java2ta.ta", "templates"),
        )
        template = env.get_template(self.template)

        ctx = {
            "ta": self.ta,  
            "legend": legend,
        }
        return template.render(**ctx)
