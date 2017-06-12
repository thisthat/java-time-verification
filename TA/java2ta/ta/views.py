from jinja2 import Environment, PackageLoader, select_autoescape


class GraphViz(object):

    template = "finite_state_automaton.gv"

    def __init__(self, ta):
        self.ta = ta

    def render(self):
        env = Environment(
            loader=PackageLoader("java2ta.ta", "templates"),
        )
        template = env.get_template(self.template)

        ctx = {
            "ta": self.ta
        }
        return template.render(**ctx)
