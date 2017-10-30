try:
    # python 2.x
    from cgi import escape
except ImportError:
    # python 3.x
    from html import escape
import re
#import pydot
import shlex
import tempfile
from jinja2 import Environment, PackageLoader, select_autoescape
from graphviz import Digraph, Graph
from contracts import contract
from java2ta.ta.models import Location

import logging

log = logging.getLogger("main")

class TARenderer(object):

    template = None # override in subclasses, if useful

    @contract(ta="is_ta", legend="None|list(tuple(string,string))", template="None|string")
    def __init__(self, ta, legend=None, template=None):

        self.legend = legend
        self.template = template or self.template

   
        self.ta = ta
        self.path = None

    @contract(returns="dict(string:*)")
    def get_filters(self):
        # override to return view-specific filters; return a dictionary
        # name |-> callable
        return {}

    def get_context(self):
        ctx = {
            "ta": self.ta,
            "legend": self.legend, 
        }

        return ctx
       

    def render(self):

        if not self.template:
            raise ValueError("You must specify a template to use in order to render the timed automaton")
 
        env = Environment(
            loader=PackageLoader("java2ta.ta", "templates"),
        )

        custom_filters = self.get_filters()
    
        if not isinstance(custom_filters, dict):
            raise ValueError("The returned custom filters are not a dict")

        for name,filter_def in custom_filters.iteritems():
            env.filters[name] = filter_def
        
        template = env.get_template(self.template)

        ctx = self.get_context()

        return template.render(**ctx)


    def save(self, path):
        self.path = path
        render = self.render()
        # save the graph text on file
        with open(path, "w+") as out_f:
            out_f.write(render)


##class GraphViz(TARenderer):
##    template = "finite_state_automaton.gv"
##
##    def __init__(self, *args, **kwargs):
##
##        self._graph = None
##        super(GraphViz, self).__init__(*args, **kwargs)
##
##    def get_or_create_graph(self):
##
##        if not self._graph:
##            if not self.path:
##                raise ValueError("Cannot create graph until you render it and save it on a file")        
##
##            (self._graph, ) = pydot.graph_from_dot_file(self.path)
##
##        return self._graph
##
##    def write_pdf(self, path):
##        graph = self.get_or_create_graph()
##        graph.write_pdf(path)
##
##    def write_svg(self, path):
##        graph = self.get_or_create_graph()
##        graph.write_svg(path)
##

class GraphViz(TARenderer):

    def __init__(self, ta, legend=None, show_pc=True):
        self.show_pc = show_pc
        super(GraphViz, self).__init__(ta, legend)
 
    @contract(loc="is_location", returns="string")
    def get_loc_name(self, loc):
        loc_name = loc.name
        if not self.show_pc:
            loc_name = re.sub(r'\@[0-9\.]+$', "", loc_name)

        return loc_name
   
    def render(self, *args, **kwargs):
        ta = self.ta
    
        g = Digraph(ta.name)
        g.attr(rankdir="TD")

        for loc in ta.locations:
            node_attrs = { "shape": "rect", "style": "rounded" }
            if loc.is_initial:
                node_attrs["peripheries"] = "2"

            loc_name = self.get_loc_name(loc)
            g.node(loc.name, label=loc_name, **node_attrs)

        for e in ta.edges:
            g.edge(e.source.name, e.target.name, label=e.label)

        if self.legend:
            l = Digraph("legend", node_attr={"shape":"plaintext"})
            legend_text = "<<table border='1px' cellpadding='2' cellspacing='0' cellborder='0'><tr><td align='center'>position</td><td>val_1 : pred_1 ... val_N : pred_N</td></tr>"
            for label, desc in self.legend:
                legend_text += "<tr><td align='center'>%s</td><td align='left'>%s</td></tr>" % (escape(label), escape(desc))
            legend_text += "</table>>"
            l.node("legend", legend_text)
            g.subgraph(l)
 
        return g


    def save(self, path):

        g = self.render()
        g.view()

    def save_plain(self, path):
    
        g = self.render()
        g.format = "plain"
        plain_path = g.render(path, view=False, cleanup=True)
        log.debug("Saved: %s -> %s" % (path, plain_path))
        return plain_path

def uppaal_loc_name(name):
    """
    This will be used as filter for converting location names to valid
    Uppaal location identifiers/names
    """
    return unicode(name).replace("(","loc_").replace(")@", "_at_").replace(", ","_").replace(".","_")


def uppaal_var_name(name):
    return re.sub("(|)","", re.sub("@|,|\.","_", unicode(name)))


def uppaal_get_loc_position(position_map):
    def get_x(loc):
        loc_name = loc
        if isinstance(loc, Location):
            loc_name = loc.name
        return int(float(position_map[loc_name][0]) * 100)

    def get_y(loc):
        loc_name = loc
        if isinstance(loc, Location):
            loc_name = loc.name
        return int(float(position_map[loc_name][1]) * 100)

    return (get_x, get_y)

class Uppaal(TARenderer):
    template = "uppaal.xml"

    def get_position_map(self):

        temp_gv_file = tempfile.NamedTemporaryFile(delete=False, suffix=".gv")
        gv = GraphViz(self.ta)
        temp_plain_path = gv.save_plain(temp_gv_file.name)

        position_map = {}

        with open(temp_plain_path, "r") as temp_plain_file:
            for line in temp_plain_file:
                # shlex split by spaces, if not appearing in quotes
                log.debug("Line: %s" % line)
                parts = shlex.split(line)
                log.debug("Parts: %s" % parts)
            
                if parts[0] == "node":
                    position_map[parts[1]] = (parts[2], parts[3])

                # TODO at the moment we ignore the edge coordinates, 
                # that could be useful to make a better looking uppaal
                # automaton

        return position_map


    def get_filters(self):

        position_map = self.get_position_map()
        log.debug("Position map: %s" % position_map)
        (loc_x, loc_y) = uppaal_get_loc_position(position_map)
        return { "loc_name": uppaal_loc_name, "var_name": uppaal_var_name, "loc_x": loc_x, "loc_y": loc_y }
