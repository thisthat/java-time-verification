import re

from jinja2 import Environment, PackageLoader, select_autoescape

from contracts import contract


class TARenderer(object):

    template = None # override in subclasses, if useful

    @contract(ta="is_ta", legend="None|list(tuple(string,string))", template="None|string")
    def __init__(self, ta, legend=None, template=None):

        self.legend = legend
        self.template = template or self.template

        if not self.template:
            raise ValueError("You must specify a template to use in order to render the timed automaton")
    
        self.ta = ta

    @contract(returns="dict(string:*)")
    def get_filters(self):
        # override to return view-specific filters; return a dictionary
        # name |-> callable
        return {}

    def render(self):
        env = Environment(
            loader=PackageLoader("java2ta.ta", "templates"),
        )

        custom_filters = self.get_filters()
    
        if not isinstance(custom_filters, dict):
            raise ValueError("The returned custom filters are not a dict")

        for name,filter_def in custom_filters.iteritems():
            env.filters[name] = filter_def
        
        template = env.get_template(self.template)

        ctx = {
            "ta": self.ta,
            "legend": self.legend, 
        }

        return template.render(**ctx)

    def save(self, path):
    
        with open(path, "w+") as out_f:
            out_f.write(self.render())


class GraphViz(TARenderer):
    template = "finite_state_automaton.gv"

def loc_name(name):
    """
    This will be used as filter for converting location names to valid
    Uppaal location identifiers/names
    """
    return unicode(name).replace("(","loc_").replace(")@", "_at_").replace(", ","_").replace(".","_")

def var_name(name):
    return re.sub("(|)","", re.sub("@|,|\.","_", unicode(name)))

class Uppaal(TARenderer):
    template = "uppaal.xml"

    def get_filters(self):
        return { "loc_name": loc_name, "var_name": var_name }
