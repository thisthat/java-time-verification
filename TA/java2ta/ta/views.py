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
import subprocess

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


class GraphViz(TARenderer):

    ENGINE_CHOICES = [ "dot", "neato", "spdf", "circo", ]

    def __init__(self, ta, legend=None, show_pc=True, type="pdf", engine="dot"):
        self.show_pc = show_pc
        self.type = type

        if engine not in GraphViz.ENGINE_CHOICES:   
            raise ValueError("Engine '%s' is not allowed. Choose one among: %s" % (engine, GraphViz.ENGINE_CHOICES))

        self.engine = engine
        super(GraphViz, self).__init__(ta, legend)
 
    @contract(loc="is_location", returns="string")
    def get_loc_name(self, loc):
        loc_name = loc.name
        if not self.show_pc:
            loc_name = re.sub(r'\@[0-9\.]+$', "", loc_name)

        return loc_name
        
    def group_locations(self,loc):
     con=[]
    # cont=0
     lista=[]
     #while len(loc)!=0:
     for ta_lo in loc:
            if ta_lo.is_initial:
                continue
            if con.count(ta_lo.name.split("@")[1])==0:    
                con.append(ta_lo.name.split("@")[1])
            print "ecco con prima %s" % (con)
     while 1:
        listaii=[]
        #con=str(cont)
        
        cona=con.pop()    
        for ta_location in loc:
            print "ecco con dopo %s" % (con)
           # if str(ta_location)=="initial":
       # loc.remove("initial")
              #  continue
           # if str(ta_location)=="ending":
       # loc.remove("ending")
              #  continue
            #if cont==0:
             #   cont+=1
              #  con=str(ta_location)[1]
            if ta_location.is_initial:
                continue
            if ta_location.name.split("@")[1]==cona:
                
       # listai.append(loca)
                listaii.append(ta_location)
                #loc.remove(ta_location)
        #cont+=1
         
        #listaii.sort()
        listai=(cona,sorted(listaii)) 
        lista.append(listai) 
        print "ecco la lista %s" % (lista)
        if len(con)==0:
            break  
         
        #del listaii[:]
     return lista
     
    def render(self, *args, **kwargs):
        ta = self.ta
    
        g = Digraph(ta.name, engine=self.engine)
        g.attr(rankdir="TB")

      #  for (pc, locations) in sorted(self.group_locations(ta.locations)):
      #     sg = g.subgraph(name=pc)
      #     sg.attr(rankdir='LR')
      #     for loc in locations:
      #         sg.node(loc.name, label=loc_name, **node_attrs)
        
        
        node_attrs = { "shape": "rect", "style": "rounded" }
        g.attr(maxiter="10")

        for loc in ta.locations:
                if loc.is_initial:
                  #  print str(loc)
                    loc_name = self.get_loc_name(loc) 
                    node_attrs["peripheries"] = "2"
                    g.node(loc.name, label=loc_name, **node_attrs)
        node_attrs["peripheries"] = "1"                
        for cluster_id, (pc, locations) in enumerate(sorted(self.group_locations(ta.locations))):
###            print "\necco la lista restituita %s,%s" % (pc,locations)
          #  c=Digraph(pc)       
          #  c.attr(rankdir="TB")
            
           # node_attrs = { "shape": "oval", "style": "rounded" }
            
           
           # sg = g.subgraph(name=pc)
           # sg.attr(rankdir='LR')
            
           # for loc in locations:
            with g.subgraph(name=str(cluster_id)) as c:
                c.attr(rankdir="TB")
                c.attr(style="invis")
                for loc in locations:
                        
                        loc_name = self.get_loc_name(loc) 
                     #   print "ecco l'elemento %s e il nome %s" % (loc,loc_name)
                        #sg.node(loc.name, label=loc_name, **node_attrs)        
                        c.node(loc.name, label=loc_name, **node_attrs)
              #  g.node(loc.name, label=loc_name, **node_attrs)
          #  g.subgraph(c)  

        for e in ta.edges:
            g.edge(e.source.name, e.target.name, label=e.formatted_label)

        if self.legend:
            l = Digraph("legend", node_attr={"shape":"plaintext"})
            legend_text = "<<table border='1px' cellpadding='2' cellspacing='0' cellborder='0'><tr><td align='center'>position</td><td>val_1 : pred_1 ... val_N : pred_N</td></tr>"
            for label, desc in self.legend:
                legend_text += "<tr><td align='center'>%s</td><td align='left'>%s</td></tr>" % (escape(label), escape(desc))
            legend_text += "</table>>"
            l.node("legend", legend_text)
            g.subgraph(l)
 
        return g


    def open(self):

        log.debug("Before rendering graph ...")
        g = self.render()
        log.debug("Graph source: %s" % g.source)
        g.format = self.type #"pdf"
        log.debug("Before viewing graph ...")
        g.view()

##    def save_plain(self, path):
##        log.warning("deprecated: use save instead ...")
##        return self.save(path)

    def save(self, path):
    
        g = self.render()
        g.format = "plain"
        plain_path = g.render(path, view=False, cleanup=True)
        return plain_path

def uppaal_loc_name(name):
    """
    This will be used as filter for converting location names to valid
    Uppaal location identifiers/names
    """
    return unicode(name).replace("(","loc_").replace(")@", "_at_").replace(",","_").replace(".","_").replace(" ","")


def uppaal_var_name(name):
    return re.sub("(|)","", re.sub("@|,|\.","_", unicode(name)))


def uppaal_get_loc_position(position_map):

    # take the maximum value of y
    y_values = map(lambda pos: float(pos[1]), position_map.values())
    y_max = int(max(y_values) * 100)
    y_min = int(min(y_values) * 100)
    
    def get_x(loc):
        loc_name = loc
        if isinstance(loc, Location):
            loc_name = loc.name
        return int(float(position_map[loc_name][0]) * 100)

    def get_y(loc):
        loc_name = loc
        if isinstance(loc, Location):
            loc_name = loc.name
        y_curr = int(float(position_map[loc_name][1]) * 100)

        return y_max - y_curr

    return (get_x, get_y)

class Uppaal(TARenderer):
    template = "uppaal.xml"

    def get_position_map(self):

        temp_gv_file = tempfile.NamedTemporaryFile(delete=False, suffix=".gv")
        gv = GraphViz(self.ta)
        temp_plain_path = gv.save(temp_gv_file.name)

        position_map = {}

        with open(temp_plain_path, "r") as temp_plain_file:
            for line in temp_plain_file:
                # shlex split by spaces, if not appearing in quotes
                parts = shlex.split(line)
            
                if parts[0] == "node":
                    position_map[parts[1]] = (parts[2], parts[3])

                # TODO at the moment we ignore the edge coordinates, 
                # that could be useful to make a better looking uppaal
                # automaton

        return position_map


    def get_filters(self):

        position_map = self.get_position_map()
        (loc_x, loc_y) = uppaal_get_loc_position(position_map)
        return { "loc_name": uppaal_loc_name, "var_name": uppaal_var_name, "loc_x": loc_x, "loc_y": loc_y }

    def open(self):

        if not self.path:
            raise ValueError("You must save it before")

        # launch uppaal in background
        subprocess.Popen(["uppaal",self.path])

