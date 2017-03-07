from nose.tools import *

from java2ta.ta.models import TA, NTA, Location, Edge
from java2ta.engine.context import Context
from java2ta.engine.exceptions import ObjectDoesNotExist
from java2ta.engine.rules import Rule, RuleSet, Engine

class RuleCreateLocations(Rule):

    def match(self):
        return True

    def where(self, let_ctx):
        return let_ctx["num_locations"] > 0

    def do_rewrite_asts(self, let_ctx):
    
        assert isinstance(self.asts_out, TA)
        assert let_ctx["num_locations"] > 0

        num_locations = let_ctx["num_locations"]

        l = Location(name="location_%s" % num_locations)

        self.asts_out.add_location(l)

        return self.asts_out
    
    def let(self):
        # take the number of locations to create from the ctx or,
        # in the case of empty context, from the input tree

        num_locations = self.asts_in

        try:
            num_locations = self.ctx.get("num_locations")
        except ObjectDoesNotExist:
            # first time we execute the rule, use the asts input value
            pass

        let_ctx = {
            "num_locations": num_locations,
        }

        return let_ctx

    def do_update_context(self, let_ctx=None):
        num_locations = let_ctx["num_locations"]

        self.ctx.update("num_locations", num_locations-1)


class RuleCreateChain(Rule):

    def match(self):
        return True

    def where(self, let_ctx):
        return let_ctx["num_locations"] > 0

    def do_rewrite_asts(self, let_ctx):
    
        assert isinstance(self.asts_out, TA)
        assert let_ctx["num_locations"] > 0

        num_locations = let_ctx["num_locations"]
        new_loc = let_ctx["new_loc"]
        prev_loc = let_ctx["prev_loc"]

        assert isinstance(new_loc, Location)
        assert prev_loc is None or isinstance(prev_loc, Location)

        self.asts_out.add_location(new_loc)

        if prev_loc is not None:
            e = Edge(source=prev_loc, target=new_loc)
            self.asts_out.add_edge(e)

        return self.asts_out
    
    def let(self):
        # take the number of locations to create from the ctx or,
        # in the case of empty context, from the input tree

        num_locations = self.asts_in
        prev_loc = None

        try:
            num_locations = self.ctx.get("num_locations")
            prev_loc = self.ctx.get("new_loc")
        except ObjectDoesNotExist:
            # first time we execute the rule, use the asts input value
            pass

        new_loc = Location(name="location_%s" % num_locations)

        let_ctx = {
            "num_locations": num_locations,
            "new_loc": new_loc,
            "prev_loc": prev_loc, 
        }

        return let_ctx

    def do_update_context(self, let_ctx=None):
        num_locations = let_ctx["num_locations"]
        new_loc = let_ctx["new_loc"]
        prev_loc = let_ctx["prev_loc"]

        self.ctx.update("num_locations", num_locations-1)
        self.ctx.update("new_loc", new_loc)
        self.ctx.update("prev_loc", prev_loc)
 
    

def test_engine_create_locations():

    re = Engine()

    r = RuleCreateLocations()
    re.add_rule(r)

    asts_in_pre = 3 # the input AST is just a number specifying the number of locations to be created
    asts_out_pre = TA(name="foo")

    (asts_out_post, ctx_post) = re.run(asts_in_pre, asts_out_pre)

    assert re.num_applications == asts_in_pre
    assert isinstance(asts_out_post, TA)
    assert asts_out_post.name == asts_out_pre.name
    assert len(asts_out_post.locations) == asts_in_pre
    assert len(asts_out_post.edges) == 0

    for loc in asts_out_post.locations:
        assert len(loc.outgoing) == 0
        assert len(loc.incoming) == 0
    

def test_engine_create_edges():

    re = Engine()

    r = RuleCreateChain()
    re.add_rule(r)

    asts_in_pre = 3 # the input AST is just a number specifying the number of locations to be created
    asts_out_pre = TA(name="foo")

    (asts_out_post, ctx_post) = re.run(asts_in_pre, asts_out_pre)

    assert re.num_applications == asts_in_pre
    assert isinstance(asts_out_post, TA)
    assert asts_out_post.name == asts_out_pre.name
    assert len(asts_out_post.locations) == asts_in_pre
    assert len(asts_out_post.edges) == asts_in_pre - 1

    for loc in asts_out_post.locations:
        assert len(loc.outgoing) >=0
        assert len(loc.outgoing) <= 1
        assert len(loc.incoming) >= 0
        assert len(loc.incoming) <= 1
    


