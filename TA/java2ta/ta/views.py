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
from java2ta.ta.models import Location, ReactUpdateEdge, NotifyUpdateEdge
import subprocess

import logging

log = logging.getLogger("main")

##class TADisplay(object):
##
##    @contract(ta_template="is_ta_template")
##    def __init__(self, ta_template):
##        self.ta_template = ta_template
##
##    @contract(returns="dict(string:*)")
##    def get_filters(self):
##        # override to return view-specific filters; return a dictionary
##        # name |-> callable
##        return {}
##
##    @contract(ta_template="is_ta_template")
##    def get_context(self):
##        ctx = {
##            "ta_template": self.ta_template, 
##        }
##
##        return ctx
##       
##
##    @contract(ta="is_ta_template")
##    def render(self, ta_template):
##
##        if not self.rendering_template:
##            raise ValueError("You must specify a rendering template to use in order to render the timed automaton")
## 
##        env = Environment(
##            loader=PackageLoader("java2ta.ta", "templates"),
##        )
##
##        custom_filters = self.get_filters(ta_template)
##    
##        if not isinstance(custom_filters, dict):
##            raise ValueError("The returned custom filters are not a dict")
##
##        for name,filter_def in custom_filters.iteritems():
##            env.filters[name] = filter_def
##        
##        rendering_template = env.get_template(self.rendering_template)
##
##        ctx = self.get_context(ta_template)
##
##        return rendering_template.render(**ctx)
##
##
##    def save(self, path):
##        self.path = path
##        render = self.render()
##        # save the graph text on file
##        with open(path, "w+") as out_f:
##            out_f.write(render)
##
##new_contract_check_type("is_ta_display", TADisplay)
##

##class NTARenderer(object):
##
##    @contract(nta="is_nta", ta_display="is_ta_display")
##    def __init__(self, nta, ta_display):
##        self.nta = nta
##        self.ta_display = ta_display
##
##    @contract(ta_template="is_ta_template")
##    def get_context(self):
##        ctx = {
##            "nta": self.nta, 
##        }
##
##        return ctx
## 
##    @contract(returns="dict(string:object)")
##    def render(self):
##        res = {}
##
##        rendered_templates = {}
##
##        for ta in self.nta.tas:
##            if ta.template.name not in rendered_templates:
##                renderer = TARenderer(ta.template)
##                rendered_templates[ta.template.name] = renderer.render()
##
##            res[ta.name] = rendered_templates[ta.template.name]
##
##        return res
##

class TADisplay(object):

    DOT_ENGINE_CHOICES = [ "dot", "neato", "spdf", "circo", ]

    def __init__(self, ta_template, legend=None, show_pc=True, type="pdf", engine="dot", name=None):
        self.ta_template = ta_template
        self.show_pc = show_pc
        self.type = type
        self.name = name or ta_template.name

        if engine not in TADisplay.DOT_ENGINE_CHOICES:   
            raise ValueError("Engine '%s' is not allowed. Choose one among: %s" % (engine, TADisplay.DOT_ENGINE_CHOICES))

        self.engine = engine
        self.legend = legend
        self._positions = {} # this is populated by method _load_positions
 
    @contract(loc="is_location", returns="string")
    def get_loc_name(self, loc):
        loc_name = loc.name
        if not self.show_pc:
            loc_name = re.sub(r'\@[0-9\.]+$', "", loc_name)

        return loc_name
        
    def group_locations(self,loc):
        con=[]
        lista=[]
        for ta_lo in loc:
               if ta_lo.is_initial:
                   continue
               if con.count(ta_lo.name.split("@")[1])==0:    
                   con.append(ta_lo.name.split("@")[1])
             #  print "ecco con prima %s" % (con)

        while True:
           listaii=[]
           #con=str(cont)
           
           cona=con.pop()    
           for ta_location in loc:
               if ta_location.is_initial:
                   continue
               if ta_location.name.split("@")[1]==cona:
                   listaii.append(ta_location)
            
           listai=(cona,sorted(listaii)) 
           lista.append(listai) 
           if len(con)==0:
               break  
            
        return lista
        
    def display(self):
    
        g = Digraph(self.name, engine=self.engine)
        g.attr(rankdir="TB")

        node_attrs = { "shape": "rect", "style": "rounded" }
        g.attr(maxiter="10")

        for loc in self.ta_template.locations:
            if loc.is_initial:
                loc_name = self.get_loc_name(loc) 
                node_attrs["peripheries"] = "2"
                g.node(loc.name, label=loc_name, **node_attrs)
        node_attrs["peripheries"] = "1"                
        for cluster_id, (pc, locations) in enumerate(sorted(self.group_locations(self.ta_template.locations))):
            with g.subgraph(name=str(cluster_id)) as c:
                c.attr(rankdir="TB")
                c.attr(style="invis")
                for loc in locations:
                    loc_name = self.get_loc_name(loc) 
                    c.node(loc.name, label=loc_name, **node_attrs)

        for e in self.ta_template.edges:  
            edge_style = "solid"
            if isinstance(e, ReactUpdateEdge):
                edge_style = "dashed"
            elif isinstance(e, NotifyUpdateEdge):
                edge_style =  "dotted"

            g.edge(e.source.name, e.target.name, label=e.formatted_label, style=edge_style)

        if self.legend:
            l = Digraph("legend", node_attr={"shape":"plaintext"})
            legend_text = "<<table border='1px' cellpadding='2' cellspacing='0' cellborder='0'><tr><td align='center'>position</td><td>val_1 : pred_1 ... val_N : pred_N</td></tr>"
            for label, desc in self.legend:
                legend_text += "<tr><td align='center'>%s</td><td align='left'>%s</td></tr>" % (escape(label), escape(desc))
            legend_text += "</table>>"
            l.node("legend", legend_text)
            g.subgraph(l)
 
        self._load_positions(g)

        return g

    def _load_positions(self, g=None):

        temp_display_file = tempfile.NamedTemporaryFile(delete=False, suffix=".gv")
        coordinates_path = self.save(temp_display_file.name, g)

        positions_map = {}

        log.debug("Coordinates path: %s" % coordinates_path)

        with open(coordinates_path, "r") as coordinates_file:
            for line in coordinates_file:
                # shlex split by spaces, if not appearing in quotes
                parts = shlex.split(line)
            
                if parts[0] == "node":
                    positions_map[parts[1]] = (parts[2], parts[3])

                # TODO at the moment we ignore the edge coordinates, 
                # that could be useful to make a better looking uppaal
                # automaton

        self._positions = positions_map


    @contract(location="is_location", returns="tuple(float,float)")
    def get_location_coordinates(self, location):
        if location.name not in self._positions.keys():
            raise ValueError("Locations not loaded. Invoke the display method before loading location coordinates")
     
        log.debug("Positions: %s" % (self._positions,))
       
        x_text, y_text = self._positions[location.name]
        return (float(x_text), float(y_text))

    @contract(location="is_location", returns="float")
    def get_location_x(self, location):
        (loc_x, loc_y) = self.get_location_coordinates(location)
        return loc_x

    @contract(location="is_location", returns="float")
    def get_location_y(self, location):
        (loc_x, loc_y) = self.get_location_coordinates(location)
        return loc_y


    def open(self):

        log.debug("Before rendering graph ...")
        g = self.display()
        log.debug("Graph source: %s" % g.source)
        g.format = self.type #"pdf"
        log.debug("Before viewing graph ...")
        g.view()


    def save(self, path, graph=None):
    
        if graph is None:
            graph = self.display()
            
        graph.format = "plain"

        plain_path = graph.render(path, view=False, cleanup=True)

        return plain_path

def uppaal_loc_name(name):
    """
    This will be used as filter for converting location names to valid
    Uppaal location identifiers/names
    """
    return unicode(name).replace("(","loc_").replace(")@", "_at_").replace(",","_").replace(".","_").replace(" ","")


def uppaal_var_name(name):
    return re.sub("(|)","", re.sub("@|,|\.","_", unicode(name)))

def uppaal_channel_name(name):
    return re.sub("(|)","", re.sub("@|,|\.","_", unicode(name)))
   

##def uppaal_get_loc_position(position_map):
##
##    # take the maximum value of y
##    y_values = map(lambda pos: float(pos[1]), position_map.values())
##    y_max = int(max(y_values) * 100)
##    y_min = int(min(y_values) * 100)
##    
##    def get_x(loc):
##        loc_name = loc
##        if isinstance(loc, Location):
##            loc_name = loc.name
##        return int(float(position_map[loc_name][0]) * 100)
##
##    def get_y(loc):
##        loc_name = loc
##        if isinstance(loc, Location):
##            loc_name = loc.name
##        y_curr = int(float(position_map[loc_name][1]) * 100)
##
##        return y_max - y_curr
##
##    return (get_x, get_y)

class Uppaal(object):
    rendering_template = "uppaal.xml"

    @contract(nta="is_nta")
    def __init__(self, nta):
        self.nta = nta
        self.path = None


    def save(self, path):
        self.path = path
        render = self.render()
        # save the graph text on file
        with open(path, "w+") as out_f:
            out_f.write(render)

    @contract(returns="dict")
    def get_filters(self):

        # first we need to display all the timed automata and to keep a reference
        # to each display (one for each TA template)
        ta_template_displays = {}

        for ta in self.nta.tas:
            if ta.template.name not in ta_template_displays:
                d = TADisplay(ta.template)
                ta_template_displays[ta.template.name] = d
                d.display()

        # next we create two functions that, from the TA template name and the
        # location name, can determine the location coordinates
        uppaal_loc_x = lambda loc: int(100 * ta_template_displays[loc.ta_template.name].get_location_x(loc)) 
        uppaal_loc_y = lambda loc: int(-100 * ta_template_displays[loc.ta_template.name].get_location_y(loc) )
    
        return {
            "loc_name": uppaal_loc_name,
            "var_name": uppaal_var_name,
            "channel_name": uppaal_channel_name,
            "loc_x": uppaal_loc_x,
            "loc_y": uppaal_loc_y,
        }

    def open(self):

        if not self.path:
            raise ValueError("You must save it before")

        # launch uppaal in background
        subprocess.Popen(["uppaal",self.path])


    @contract(returns="dict")
    def get_context(self):
        ctx = {
            "nta": self.nta, 
        }

        return ctx


    def render(self):

        env = Environment(
            loader=PackageLoader("java2ta.ta", "templates"),
        )

        custom_filters = self.get_filters()
    
        assert isinstance(custom_filters, dict)

        for name,filter_def in custom_filters.iteritems():
            env.filters[name] = filter_def
        
        rendering_template = env.get_template(self.rendering_template)

        ctx = self.get_context()

        return rendering_template.render(**ctx)

