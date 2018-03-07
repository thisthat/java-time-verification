from time import sleep

import re
import os
import tempfile
import subprocess
import fcntl

from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.translator.rules import ExtractMethodStateSpace, AddStates
from java2ta.translator.models import PC, ReachabilityResult, AttributePredicate, Cache, Precondition, Negate, KnowledgeBase, FreshNames
from java2ta.ir.models import Project, Method, Klass
from java2ta.ir.client import APIError
from java2ta.ir.shortcuts import get_timestamps, check_now_assignments, get_identifiers
from java2ta.ta.models import TA, Location, Edge, TimeEdge, ClockVariable
from java2ta.abstraction.models import StateSpace, AbstractAttribute, Domain, Predicate, SymbolTable, CompareVariables, Integer, LT, GT, Eq
from java2ta.abstraction.shortcuts import DataTypeFactory, INTEGERS

import itertools
import sys
import logging
from contracts import contract, new_contract, check

# this global variable is a dirty way to pass to all the methods in the workflow a 
# quick reference to the method currently under analysis; the code should be refactored
# so that the global variable is not needed any more
THE_METHOD = None

PROCESS_PRECONDITIONS = False # see the bug in processing preconditions, before re-enabling that code

log = logging.getLogger(__name__)
log_smt = logging.getLogger("smt")

class IdentifierAsLiteralException(Exception):
    
    def __init__(self, identifier, *args, **kwargs):
        self.identifier = identifier
        super(IdentifierAsLiteralException, self).__init__(*args, **kwargs)

class ForgottenVariableException(Exception):
        
    def __init__(self, var_name, *args, **kwargs):
        self.var_name = var_name
        super(ForgottenVariableException,self).__init__(*args, **kwargs)

    def __str__(self):
        return "Variable %s has been forgotten." % self.var_name

class ForgottenMethodException(Exception):

    def __init__(self, method_name, class_name, class_path, *args, **kwargs):
        self.method_name = method_name
        self.class_name = class_name
        self.class_path = class_path
        super(ForgottenMethodException,self).__init__(*args, **kwargs)

    def __str__(self):
        return "Method %s has been forgotten (class: %s, path: %s)" % (self.method_name, self.class_name, self.class_path)

class UnknownSMTInterpretationException(Exception):

    def __init__(self, node, *args, **kwargs):
        self.node = node
        msg = "Unknown SMT interpretation for code (%s): %s" % (node["nodeType"], node["code"], )
        super(UnknownSMTInterpretationException, self).__init__(msg, *args, **kwargs)


class TimeTransitionException(Exception):

    def __init__(self, class_fqn, method_name, var, *args, **kwargs):
        self.class_fqn = class_fqn
        self.method_name = method_name
        self.var = var
        super(TimeTransitionException, self).__init__(*args, **kwargs)


class UpdateTimestampException(Exception):

    def __init__(self, class_fqn, method_name, var, rhs_node, *args, **kwargs):
        self.class_fqn = class_fqn
        self.method_name = method_name
        self.var = var
        self.rhs_node = rhs_node
        super(UpdateTimestampException, self).__init__(*args, **kwargs)

@contract(attr_predicates="None|list(is_attribute_predicate)", returns="set(string)")
def pred_to_env(attr_predicates):

    res = set([])

    if attr_predicates is not None:
        for attr_pred in attr_predicates:
            res = res | set(attr_pred.variables)

    return res

##@contract(project=Project, class_fqn="string", class_path="string", method_name="string", domains="dict", returns=TA)
##def translate_method_to_automaton(project, class_fqn, class_path, method_name, domains):
##
##    # check this works (case 1: no dot, case 2: one or more dots)
##    class_name = ""
##    package_name = ""
##
##    fqn_parts = class_fqn.rsplit(".", 1)
##
##    if len(fqn_parts) == 1:
##        class_name = fqn_parts[0]
##    else:
##        package_name = fqn_parts[0]
##        class_name = fqn_parts[1]
##
##    klass = Klass(class_name, package_name, "file://%s" % class_path, project)
##    m = Method(method_name, klass)
##
##    r1 = ExtractMethodStateSpace()
##    r2 = AddStates()
##    # TODO add rules for adding edges b/w states by static analysis (SMT based)
##
##    e = Engine()
##    e.add_rule(r1)
##    e.add_rule(r2)
##
##    ctx = Context()
##    ctx.push({})
##    ctx.update("abs_domains", domains)
##    ta = TA(method_name)
##    (ta_post, ctx_post) = e.run(m, ta, ctx)
##
##    return ta_post
##

@contract(conf="is_configuration", pc="is_pc", returns="string")
def build_location_name(conf, pc):
    conf_string = ",".join(map(str, conf))
    loc_name = "(%s)%s" % (conf_string, pc)
    return loc_name

@contract(conf="is_configuration", pc="is_pc", returns="is_location")
def build_location(conf, pc):
    
    # convert the conf to a list of string, and join the items
    # using ","
    loc_name = build_location_name(conf, pc)
#    log.debug("conf: %s, loc name: %s" % (conf_string, loc_name))
    loc = Location(loc_name)

    return loc


@contract(source_conf="list(is_configuration)", pc_source=PC, instr="list(dict)", state_space="is_state_space", project="is_project", visited_locations="set(string)", preconditions="list(is_precondition)|None", returns=ReachabilityResult)
def compute_reachable(source_conf, pc_source, instr, state_space, project, visited_locations, preconditions=None, pc_jump_stack=None, deadlines=None):
    """
    INPUT:
    - source_conf : list of abstract configuration
    - pc_source : PC
    - instr : representation of instruction or list of instructions
    - state_space : StateSpace

    OUTPUT:
    - reachable : list of abstract configurations
    - edges : list of Edge
    - final : list of Location that have incoming Edge in edges, but no outgoing Edge
    """
    assert preconditions is None or isinstance(preconditions, list)

    log.debug("Compute reachable: source_conf=%s, pc_source=%s, state_space=%s, preconditions=%s, pc_jump_stack=%s, deadlines=%s" % (source_conf, pc_source, state_space, preconditions, pc_jump_stack, deadlines))

    reachable = []
    final = []
    external = []
    edges = []
    variables = set([])

    if deadlines is None:
        deadlines = []

    log.debug("Instructions: %s. PC: %s" % (instr, pc_source.pc))

    if len(instr) == 0:
        # empty block case
        reachable = set(source_conf)
        final = map(lambda c: build_location(c, pc_source), source_conf) # create a location for each configuration

    curr_pc = PC(pc_source.pc)
    for curr_instr in instr:

#        log.debug("Check instruction: %s" % curr_instr)

        # reachable and final must contain only information that are reached by
        # the last instruction (NB on the contrary, edges contain all the edges 
        # added by all the instructions)

        reachable = set([])
        final = []

        # use sets in order to avoid duplicates
        source_conf = sorted(set(source_conf))

        if not source_conf:
            log.debug("Exit for non reachable states at PC: %s" % curr_pc)
            break

        for source in source_conf:

            loc_name = build_location_name(source, curr_pc)
            log.debug("Check location already visited: %s vs %s" % (loc_name, visited_locations))
            if loc_name in visited_locations:
                # location already visited, skip it
                continue

            log.debug("Check reach problem: source=%s%s, instr=%s, preconditions=%s, visited locations: %s" % (source, curr_pc, curr_instr["code"], preconditions, visited_locations))
            # TODO in principle each invocation of check_reach(...) is independent from the others
            rr = check_reach(source, curr_pc, curr_instr, state_space, project, visited_locations, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            log.debug("Check reach: source=%s, result=%s" % (source, rr))    

            edges.extend(rr.edges)
            reachable = reachable | set(rr.configurations) # use sets and set unions to avoid duplicates
            final.extend(rr.final_locations) 
            external.extend(rr.external_locations)
            variables = variables | rr.variables
        
#        log.debug("Check reachable at %s: %s from %s" % (curr_pc, reachable, source_conf))
        if len(reachable) == 0:
            # in this case the current instruction interrupts the flow of instructions; thus, we
            # must ignore the following instructions
            log.debug("Instruction interrupts flow: %s. Source conf: %s. PC: %s" % (curr_instr["code"], source_conf, curr_pc))
            break

        # next iteration starts from reached configurations, and advance PC by 1
        log.debug("Compute reachable next iteration: (%s)%s -> (%s)%s" % (source_conf, curr_pc, reachable, curr_pc + 1))
        source_conf = reachable
        curr_pc = curr_pc + 1

#    assert (len(reachable) >= 0 and len(edges) >= 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0)

    return ReachabilityResult(configurations=reachable, final_locations=final, external_locations=external, edges=edges, variables=variables)


@contract(name="string", instructions="list[N](dict),N>0", state_space="is_state_space", project="is_project", returns="is_ta")
def transform(name, instructions, state_space, project):

    # TODO at the moment we call TA the class for the FSA ... this is not a big issue, but I leave
    # this note just to keep track of this "oddity"
    ta = TA(name)

    log.info("State space attributes:")
    for attr in state_space.attributes:
        log.info("%s -> %s" % (attr.name, attr.domain))

    reach = dict()
    pc_source = PC(initial="0")
    source_conf = state_space.initial_configurations
    visited_locations = set([]) # initially, no location is visited

    log.debug("Initial configurations: %s" % sorted(set(source_conf)))

    rr = compute_reachable(source_conf, pc_source, instructions, state_space, project, visited_locations)

    log.info("reachable: %s, final: %s, external: %s" % (rr.configurations, rr.final_locations, rr.external_locations))
    log.info("visited locations: %s" % visited_locations)

    # add variables and clock variables to the automaton
    for var in rr.variables:
        if isinstance(var, ClockVariable):
            if not ta.has_clock_variable(var):
                ta.add_clock_variable(var)
            else:
                log.warning("Skip adding clock variable for the second time: %s" % var)
        else:
            if not ta.has_variable(var):
                ta.add_variable(var)
            else:
                log.warning("Skip adding variable for the second time: %s" % var)

    log.debug("all variables added to automaton ...")

    # add edges to the automaton; when adding edges, it also add
    # the locations
    if len(rr.edges) > 0:
        for e in rr.edges:
            assert isinstance(e, Edge), e

            ta.get_or_add_edge(e)

    log.debug("all edges added to automaton ...")

    # determine whether there exists an initial location, or create one
    ta.close()

    log.debug("automaton closed")

    return ta


def node_to_deadline_exp(node, now_timestamps):
    """
    This is a very naive interpretation, where expressions like:

    now + timeout

    is translated to

    0 + timeout
        
    if "now" is one of the now_timestamps
    """
    exp = node["code"]
    for curr in now_timestamps:
        exp = exp.replace(curr, "0")
 
    log.debug("Node: %s -> Exp: %s" % (node["code"], exp))
    return exp


class SMTProb(object):

    OP_DECODE = { "plus": "+", "minus": "-", "greater": ">", "less": "<", "mul": "*", "equality": "=", "and": "and", "or": "or", "notEqual": "distinct", "lessEqual": "<=", "greaterEqual": ">=", "mod": "mod" }

    _SMT_CACHE = Cache("smt")
    _NODE_TO_SMT_CACHE = Cache("node2smt")
    _DISABLE_NODE_CACHE = True # now it seems that there are some bugs; add this flag to easily come back (also useful for comparing execution with and without cache)

    _engine = None

    @staticmethod
    def init_engine(*args, **kwargs):
        SMTProb._engine = SMTProb(*args, **kwargs)

    @staticmethod
    def the_engine():
        return SMTProb._engine

    @contract(attributes="list", project="is_project")
    def __init__(self, attributes, project):

        self._line_num = 0
        self.attributes = attributes
        self._project = project

        self._cmd = subprocess.Popen(["z3", "-in"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)

        # set stderr as non-blocking
        flags = fcntl.fcntl(self._cmd.stderr, fcntl.F_GETFL) # get current p.stdout flags
        fcntl.fcntl(self._cmd.stderr, fcntl.F_SETFL, flags | os.O_NONBLOCK)

    def __del__(self):
        cmd = getattr(self, "_cmd", None)
        if cmd:
            cmd.terminate()


    def __enter__(self):
        """
        interface for calling instance using "with", e.g.

        with SMTProb(...) as prob:  
            ... do something ...
        """
        return self

    
    def __exit__(self, *args, **kwargs):
        """
        interface for calling instance using "with" (see __enter__)
        """
        self._cmd.terminate()


    @staticmethod
    @contract(node="dict", returns="None|*")
    def node_cache_lookup(node): 

        if SMTProb._DISABLE_NODE_CACHE:
            return None

        node_key = node["code"].strip()
        res = SMTProb._NODE_TO_SMT_CACHE.lookup(node_key)
        return res

    @staticmethod
    @contract(node="dict", obj="*")
    def node_cache_store(node, obj):
        if SMTProb._DISABLE_NODE_CACHE:
            return

        node_key = node["code"].strip()
        SMTProb._NODE_TO_SMT_CACHE.store(node_key, obj)
        
    
    @staticmethod
    @contract(source_conf="is_configuration", instr="dict", target_conf="is_configuration", returns="None|bool")
    def smt_cache_lookup(source_conf, instr, target_conf):
        source_key = str(source_conf)
        instr_key = instr["code"].replace("\n","")
        target_key = str(target_conf)        

        key = "%s-%s-%s" % (source_key, instr_key, target_key)
        res = SMTProb._SMT_CACHE.lookup(key)
        return res

    @staticmethod
    @contract(source_pred="is_configuration", instr="dict", target_pred="is_configuration", res=bool)
    def smt_cache_store(source_pred, instr, target_pred, res):
        source_key = str(source_pred)
        instr_key = instr["code"].replace("\n","")
        target_key = str(target_pred)        

        key = "%s-%s-%s" % (source_key, instr_key, target_key)
        SMTProb._SMT_CACHE.store(key, res)

    @contract(node="dict", env="set(string)", returns="tuple(list(string),string)")
    def node_to_smt(self, node, env, frame=None):
        """
        This method returns a pair of strings: the first contains auxiliary declarations (e.g. constants,
        datatypes, ...) and the second contains the BODY of the actual assertion.

        The exclude_frame is a list of program variables that should be
        excluded when computing the local frame condition (e.g. because
        they are affected by the statement represented by the node)

        TODO perhaps we can simplify this to just return a list(string); at the moment I don't see a reason
        of "separating" the final assertion (second element of the tuple) from the previous declarations (first
        element of the tuple)
        """
        assert "code" in node

        log.debug("Node to smt: %s. Frame: %s" % (node["code"], frame))

        if not frame:
            frame = []

        cache_found = SMTProb.node_cache_lookup(node)

        if cache_found:
            return cache_found

        node_type = node["nodeType"]

        smt_declarations = [] #""
        smt_assertion = ""

        if node_type == "ASTRE":
 
            assert "expression" in node, node
            assert "nodeType" in node["expression"]

            node_exp = node["expression"]
            node_exp_type = node["expression"]["nodeType"]

            log.debug("Found ASTRE node (%s)" % node_exp_type)

            # go by induction on the sub-node
            if node_exp_type == "ASTVariableDeclaration":
                assert "name" in node_exp
                assert "value" in node_exp["name"]
                assert "expr" in node_exp

                var = node_exp["name"]["value"] # TODO check that the LHS is always obtained in this way
                rhs = node_exp["expr"]

#                if var not in env:
#                    raise ForgottenVariableException(var)
                curr_method = get_current_method()
                assert curr_method != None, "Expected a non-null current method under analysis"
                class_fqn = curr_method.parent.fqname
                method_name = curr_method.name
#                now_timestamps = KnowledgeBase.get_now_timestamps(class_fqn, method_name)
                is_now_timestamp = KnowledgeBase.is_now_timestamp(class_fqn, method_name, var)
                is_timestamp = KnowledgeBase.is_timestamp(class_fqn, method_name, var)

#                print "check '%s.%s.%s' is now timestamp: %s" % (class_fqn, method_name, var, is_now_timestamp)
                if is_now_timestamp:
                    raise TimeTransitionException(class_fqn, method_name, var)
                elif is_timestamp:
                    raise UpdateTimestampException(class_fqn, method_name, var, rhs)
##                    if not is_now_timestamp:
##                        deadline_exp = node_to_deadline_exp(rhs, now_timestamps)
##                        KnowledgeBase.set_deadline_exp(class_fqn, method_name, var, deadline_exp)
## 
##                    # force the state not to change (all variables of all attributes keep their value)
##                    for curr in self.attributes:
##                        assert isinstance(curr, AbstractAttribute)
##                        for attr_var in curr.variables:
##                            assert not is_now_timestamp or attr_var != var # at the moment we don't allow now-timestamps to be in the abstract state space; other timestamp variables can
###                            if attr_var != var and attr_var in frame:
##                            smt_declarations.append("(assert (= %s_1 %s)) ; ASTVariableDeclaration frame condition (with now timestamp)" % (attr_var, attr_var)) # TODO primed names
                elif rhs is not None:
                    new_frame = list(frame)
                    try:
                        # remove var if present in frame/new_frame
                        new_frame.remove(var) 
                    except ValueError:
                        # do nothing
                        pass
                    rhs_smt_declarations, rhs_smt_assertion = self.node_to_smt(rhs, env, frame=new_frame)
                    if rhs_smt_assertion:
                        smt_declarations.extend(rhs_smt_declarations)
                        if var in env:
                            smt_assertion = "(= %s_1 %s)" % (var, rhs_smt_assertion) # TODO primed name
                        # NB: if var not in env, the variable declaration does not modify the abstract
                        # environment (unless the rhs has side-effects or it raises a ForgottenVariable/
                        # ForgottenMethod exception; the previous recursive call to node_to_smt(rhs, ...)
                        # checks exactly that)

                    # force other primed vars to be equal to non-primed vars
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
                        for attr_var in curr.variables:
                            if attr_var != var and attr_var in frame:
                                smt_declarations.append("(assert (= %s_1 %s)) ; ASTVariableDeclaration frame condition" % (attr_var, attr_var)) # TODO primed names

            elif node_exp_type == "ASTAssignment":
                assert "right" in node_exp
                assert "left" in node_exp
                assert "nodeType" in node_exp["left"]
#                assert node_exp["left"]["nodeType"] == "ASTLiteral", "Expected ASTLiteral, found: %s" % node_exp["left"]["nodeType"]
                assert node_exp["left"]["nodeType"] == "ASTIdentifier"

                var = node_exp["left"]["value"]
                rhs = node_exp["right"]

                curr_method = get_current_method()
                assert curr_method != None, "Expected a non-null current method under analysis"
                class_fqn = curr_method.parent.fqname
                method_name = curr_method.name
#                now_timestamps = KnowledgeBase.get_now_timestamps(class_fqn, method_name)
                is_now_timestamp = KnowledgeBase.is_now_timestamp(class_fqn, method_name, var)
                is_timestamp = KnowledgeBase.is_timestamp(class_fqn, method_name, var)

                if is_now_timestamp:
                    raise TimeTransitionException(class_fqn, method_name, var)
                elif is_timestamp:
                    raise UpdateTimestampException(class_fqn, method_name, var, rhs)

##                if is_now_timestamp or is_timestamp:
##
##                    if not is_now_timestamp:
##                        deadline_exp = node_to_deadline_exp(rhs, now_timestamps)
##                        KnowledgeBase.set_deadline_exp(class_fqn, method_name, var, deadline_exp)
##
##                    # force the state not to change (all variables of all attributes keep their value)
##                    for curr in self.attributes:
##                        assert isinstance(curr, AbstractAttribute)
##                        for attr_var in curr.variables:
##                            assert attr_var != var
###                            if attr_var != var and attr_var in frame:
##                            smt_declarations.append("(assert (= %s_1 %s)) ; ASTAssignment frame condition (with now timestamp)" % (attr_var, attr_var)) # TODO primed names
                elif var not in env:
                    # if var not in the environment, it means we are abstracting from it completely
                    # (i.e. we are forgetting it)
                    log.debug("Variable %s not in env: %s" % (var, env))
                    raise ForgottenVariableException(var)
                else:
                    new_frame = list(frame)
                    try:
                        # remove var if present in frame/new_frame
                        new_frame.remove(var)
                    except ValueError:
                        # do nothing
                        pass
    
                    rhs_smt_declarations,rhs_smt_assertion = self.node_to_smt(rhs, env, frame=new_frame)
                    if rhs_smt_assertion:
                        smt_declarations.extend(rhs_smt_declarations)
                        smt_assertion = "(= %s_1 %s)" % (var, rhs_smt_assertion) # TODO primed name
    
                    # force other primed vars to be equal to non-primed vars
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
    
                        for attr_var in curr.variables:
                            if attr_var != var and attr_var in frame:
                                smt_declarations.append("(assert (= %s_1 %s)) ; ASTAssignment frame condition" % (attr_var, attr_var)) # TODO primed names

            elif node_exp_type == "ASTPostOp":
                assert "code" in node_exp

                # var is all but the last 2 chars
                var = node_exp["code"][:-2] 
                # op is given by the last 2 chars
                op = node_exp["code"][-2:]

                if var not in env:
                    raise ForgottenVariableException(var)
    
                if op in [ "++", "--" ]:
                    if op == "++":
                        smt_assertion = "(= %s_1 (+ %s 1))" % (var, var) # TODO primed name
                    else:
                        # op == "--":
                        smt_assertion = "(= %s_1 (- %s 1))" % (var, var) # TODO primed name

                    # force other primed vars to be equal to non-primed vars
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
                        for attr_var in curr.variables:
                            if attr_var != var:
                                smt_declarations.append("(assert (= %s_1 %s))" % (attr_var, attr_var)) # TODO primed names

                else:
                    log.warning("Interpret PL post-op expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp))
                    smt_assertion = node_exp["code"]
            elif node_exp_type == "ASTMethodCall":

                mc_smt_declarations, mc_smt_assertion = self.node_to_smt(node_exp, env, frame=frame)
                if mc_smt_declarations:
                    smt_declarations.extend(mc_smt_declarations)
                assert len(mc_smt_assertion) > 0
                smt_assertion = mc_smt_assertion
#                del env["__self__"]
            else:
                log.warning("Interpret PL expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp))
                smt_assertion = node_exp["code"]
        elif node_type == "ASTBinary":

            assert "op" in node
            assert "left" in node
            assert "right" in node

            op = node["op"]
            if op in self.OP_DECODE:
                op = self.OP_DECODE[op]
            else:
                log.warning("Interpret PL operator (%s) as SMT operator..." % op)
            left = node["left"]
            right = node["right"]

            lhs_smt_declarations, lhs_smt_assertion = self.node_to_smt(left, env, frame=frame)
            rhs_smt_declarations, rhs_smt_assertion = self.node_to_smt(right, env, frame=frame)

            log.debug("left: %s, frame: %s, left declarations: %s, left smt: %s" % (left, frame, lhs_smt_declarations, lhs_smt_assertion))
            log.debug("right: %s, frame: %s, right declarations: %s, right smt: %s" % (right, frame, rhs_smt_declarations, rhs_smt_assertion))

            if lhs_smt_declarations: # and rhs_smt_declarations:
                smt_declarations.extend(lhs_smt_declarations)

            if rhs_smt_declarations:
                smt_declarations.extend(rhs_smt_declarations)
                
            smt_assertion = "(%s %s %s)" % (op, lhs_smt_assertion, rhs_smt_assertion)
        elif node_type == "ASTIdentifier":
            assert "value" in node

            var = node["value"]
    
            if var not in env:
                raise ForgottenVariableException(var)

            smt_assertion = var
        elif node_type == "ASTLiteral":
            assert "value" in node
            #log.debug("Dump literal: %s" % node)
            try:
                lit_value = literal_to_smt(node)
                log.debug("Node value: %s. SMT literal: %s" % (node["value"], lit_value))
            except IdentifierAsLiteralException as e:
                # this is a tail of bug described in issue #57 ;
                # leave a warning log to ease the debugging
                lit_value = e.identifier
                log.warning("Passed identifier as literal. This should not happen: %s. Line: %s. Start: %s. End: %s" % (lit_value, node["line"], node["start"], node["end"]))                
                if lit_value not in env:
                    raise ForgottenVariableException(lit_value)


            smt_assertion = lit_value
        elif node_type == "ASTMethodCall":
            log.debug("Node: %s" % node)
            method_name = node["methodName"] 
            class_name = node["classPointed"] or "-" # or "example.TestNeighbors" # HACK # TODO I guess this is a heuristic and the actual class of the object can only be determined at run-time, thus the user should be able to specify this information with some input
 
            log.debug("Check knowledge base: (%s,%s)" % (class_name, method_name))

            try:
                (smt_dt, parameters, method_env, smt_interpretation) = KnowledgeBase.get_method(class_name,method_name)
            except Exception, e:
                log.warning("Error interpreting method: %s. Node: %s" % (e, node))
                raise ForgottenMethodException(method_name, class_name, class_path="?")

            if smt_interpretation:
                log.debug("Found interpretation for method %s: %s, %s, %s, %s" % (method_name, smt_dt, parameters, method_env, smt_interpretation))
    
                tmp_var_name = FreshNames.get_name(prefix=("%s_" % method_name))
                assert node is not None

                interpretation_context = { 
                    "__return__": tmp_var_name,
                }

                callee_node = node.get("exprCallee", {})

                if callee_node:
                    callee_type = callee_node.get("nodeType", None)
                    if callee_type == "ASTIdentifier":
#                        self_var = callee_node["code"]
                        interpretation_context["__self__"] = callee_node["code"]
                    else:
                        log.warning("Callee of unknown type (%s). Cannot use the __self__var in method interpretation. Callee node: %s" % (callee_type, callee_node))
    

                smt_declarations = []

                for par_id,par in enumerate(node["parameters"]):
    
                    par_value = None
                    if par["nodeType"] == "ASTLiteral":
                        # passing a literal (after encoding it through the SymbolTable)
                        raw_value = par["value"]
    
                        par_value, par_type = SymbolTable.add_literal(raw_value)
    
#                        if raw_value[0] in [ '"', "'" ] and raw_value[-1] == raw_value[0]:
#                            par_value = SymbolTable.add_string(raw_value[1:-1])
                        if par_type == "String":
                            # it is a string; return our custom data type for strings
                            par_value = "(init-AbsString %s %s)" % (par_value, len(raw_value) - 2) #
#                        else:
#                            par_value = SymbolTable.add_literal(raw_value)

                    elif par["nodeType"] == "ASTIdentifier":
                        # passing an identifier, directly as the variable name

                        if par["code"] not in env:
                            # it may be that the variable passed as argument is not one of the 
                            # variables in the abstract state space
                            raise ForgottenVariableException(par["code"])

                        par_value = par["code"]
                    else:
#                        raise ValueError("Sorry dude. At the moment we cannot handle method call parameters that are neither literals nor identifiers. Passed: %s" % par)
                        log.debug("Method parameter is an expression: %s" % par["code"])
                        smt_declarations_arg, smt_assertion_arg = self.node_to_smt(par, env, frame)

                        log.debug("SMT declarations for method parameter: %s" % smt_declarations_arg)
                        log.debug("SMT assertion for method parameter: %s" % smt_assertion_arg)

                        par_aux_var = FreshNames.get_name(prefix="par_%s_" % par_id)  

                        # below there is a hack; in order to generalise this code, take advantage of
                        # the variable "parameters" that we get earlier but we don't use
                        smt_declarations.append("(declare-const %s Int) ; this is a hack: I assume the parameter expression computes an integer" % par_aux_var)

                        if smt_assertion_arg:
                            par_assertion = "(assert (= %s %s))" % (par_aux_var, smt_assertion_arg)
                            smt_declarations_arg.append(par_assertion)
    
                        if smt_declarations_arg:
                            smt_declarations.append("; begin encoding method call parameter")
                            smt_declarations.extend(smt_declarations_arg)
                            smt_declarations.append("; end encoding method call parameter")

                        par_value = par_aux_var
    
                    interpretation_context["par_%s" % par_id] = par_value
    
                log.debug("Method call aux var: %s => %s" % (method_name, tmp_var_name))
                smt_declarations.append( "(declare-const %s %s)" % (tmp_var_name, smt_dt))
                smt_declarations.append(smt_interpretation.smt_assert(**interpretation_context))
    
                log.debug("Method call smt interpretation: %s" % smt_interpretation)
                log.debug("Method call frame: %s" % frame)
                new_frame = [x for x in frame if x not in method_env] 
                # force primed vars in frame to be equal to non-primed vars
                for curr in self.attributes:
                    assert isinstance(curr, AbstractAttribute)
    
                    for attr_var in curr.variables:
                        if attr_var in frame:
                            smt_declarations.append("(assert (= %s_1 %s)) ; ASTAssignment frame condition" % (attr_var, attr_var)) # TODO primed names
    
    
                # store the returned value for this case
#                smt_assertion = tmp_var_name   
                smt_assertion = tmp_var_name

                log.debug("Method smt declarations: %s" % smt_declarations)
                log.debug("Method smt assertion: %s" % smt_assertion)
            else:
                log.debug("Cannot find interpretation for method %s: %s, %s, %s, %s" % (method_name, smt_dt, parameters, method_env, smt_interpretation))
                smt_assertion = ""

        elif node_type == "ASTNewObject":
            smt_assertion = ""
        elif node_type == "ASTCast":
            smt_assertion = ""   
        else:
#            log.warning("Interpret PL code (%s) as SMT code: %s ..." % (node["code"], node))
#            smt_assertion = node["code"]
            raise UnknownSMTInterpretationException(node) #"Do not know the SMT interpretation of code (%s): %s" % (node["code"], node))
#            smt_assertion = ""

        SMTProb.node_cache_store(node, (smt_declarations,smt_assertion))

        return (smt_declarations,smt_assertion)

    @contract(pred_list="list(is_attribute_predicate)", returns="list(is_attribute_predicate)")
    def primed(self, pred_list):
        assert isinstance(pred_list, list)
 
        res = list()
      
        for pred in pred_list:
            check("is_attribute_predicate", pred)
        
            pred_primed = pred.primed()

            res.append(pred_primed)

        return res


    @contract(pre="is_precondition", env="set(string)", returns="tuple(list(string),string)")
    def precondition_to_smt(self, pre, env):
        """
        Returns the same output of node_to_smt(...) for the passed Precondition.

        TODO perhaps we can simplify this to just return a list(string); at the moment I don't see a reason
        of "separating" the final assertion (second element of the tuple) from the previous declarations (first
        element of the tuple). Decide together with the node_to_smt method
        """
    
        smt_declarations = [] #None
        smt_assertion = "" #None

#        try:
        pre_smt_declarations, pre_smt_assertion = self.node_to_smt(pre.node, env)

        log.debug("pre node: %s, declarations: %s, smt: %s" % (pre.node, pre_smt_declarations, pre_smt_assertion))

        if pre_smt_assertion:
            if isinstance(pre, Negate):
                pre_smt_assertion = "(not %s)" % pre_smt_assertion

#        assert(len(smt_assertion)>0)
            smt_assertion = "(assert %s)" % pre_smt_assertion
            smt_declarations.extend(pre_smt_declarations)
##        except ForgottenVariableException, e:
##            log.warning("Skip precondition because of forgotten variable: %s" % e.var_name)
##            smt_assertion = ""
##            smt_declarations = []
##
##        log.debug("Pre: %s => (%s,%s)" % (pre, smt_declarations, smt_assertion))
        return smt_declarations, smt_assertion
 

    @contract(source_pred="list(is_attribute_predicate)", instr="dict|None", target_pred="list(is_attribute_predicate)|None", preconditions="None|list(is_precondition)", guard="None|is_precondition")
    def combine_assertions(self, source_pred, instr=None, target_pred=None, preconditions=None, guard=None):
        """
        Given a predicate on the source, an instruction, a predicate on
        the target, and a list of preconditions, build a list of SMT
        assertions corresponding to the problem we will submit to the   
        solver.
        """
    
        assertions = []
        
        assertions.append("; begin encoding source state")
        assertions.extend(source_pred)
        assertions.append("; end encoding source state")

        env = pred_to_env(source_pred) | pred_to_env(target_pred)

        if PROCESS_PRECONDITIONS and preconditions:
            """
            This piece of code adds a set of preconditions to the 
            predicates of the source state. This is buggy, though, 
            because does not handle the case where the precondition 
            is an expression on some variable, say foo, and the variable
            foo has been assigned to some other value before the current
            instruction. This may lead to inconsistencies (i.e. unsat
            constraints) between the source state predicate and the 
            precondition predicates.

            TODO fix this before setting PROCESS_PRECONDITIONS to True
            """
       
            assertions.append("; begin encoding precondition")
            for pre in preconditions:
    
                try:
                    smt_declarations,smt_pre = self.precondition_to_smt(pre, env)
                    assertions.extend(smt_declarations)
                    assertions.append(smt_pre)
                    log.debug("pre: %s, assertions: %s, smt: %s" % (pre, smt_declarations, smt_pre))
                except ForgottenVariableException, e:
                    assertions.append("; skip precondition because of forgotten variable (%s)" % pre.node["code"])
            assertions.append("; end encoding precondition")
 

        # distinguish between guard and preconditions; the preconditions
        # are the "past" guards, in the same block; the guard is the 
        # "current" precondition; if guard is set, we are checking 
        # whether the source state satisfies it
        if guard is not None:
            assertions.append("; begin encoding guard: %s" % guard.node["code"])
            try:
                smt_declarations,smt_guard = self.precondition_to_smt(guard, env)
                assertions.extend(smt_declarations)
                assertions.append(smt_guard)
                log.debug("guard: %s, assertions: %s, smt: %s" % (guard, smt_declarations, smt_guard))
            except ForgottenVariableException, e:
                assertions.append("; skip guard because of forgotten variable (%s)" % guard.node["code"])
            assertions.append("; end encoding guard")
       
        # SMT assertions about the target predicates require that:
        # - if instruction does something, target predicates use primed vars
        # - if instruction does not do anything, target predicates use same vars as source predicates   

        if instr is not None:
            # pass the entire environment as the frame of variables to
            # be preserved; it is a responsibility of node_to_smt to 
            # remove some variables, if they are modified by (part of)
            # the processed instruction
            frame_variables = []
            for attr in source_pred:    
                log.debug("Instruction: %s. Predicate: %s. Variables: %s." % (instr["code"], attr.predicate, attr.variables))
                frame_variables.extend(attr.variables)

            assertions.append("; begin encoding instruction: %s" % instr["code"])

            smt_instr_declarations = None
            smt_instr_assertion = None
            try:
                smt_instr_declarations, smt_instr_assertion = self.node_to_smt(instr, env, frame=frame_variables)
            except ForgottenVariableException, e:
                assertions.append("; cannot compute SMT model for instruction because one variable has been forgotten: %s" % e.var_name)
            except ForgottenMethodException, e:
                assertions.append("; cannot compute SMT model for instruction because one method has been forgotten: (name:%s,class:%s,path:%s)" % (e.method_name, e.class_name, e.class_path))


            if smt_instr_declarations:
                assertions.extend(smt_instr_declarations)

            if smt_instr_assertion: # is not None: # also discard the case of empty strings (should never happen, though)
                instr_assert = "(assert %s)" % smt_instr_assertion
                assertions.append(instr_assert)

            assertions.append("; end encoding instruction: %s" % instr["code"])

            if target_pred is not None:
                target_pred_primed = self.primed(target_pred)

                assertions.append("; begin encoding target state")
                assertions.extend(target_pred_primed)
                assertions.append("; end encoding target state")

        return assertions
 

    @contract(primed=bool, returns="list")
    def get_smt_attribute_declaration(self, primed=True):
        """
        Get the SMT declaration of each attribute. If primed==True, add also
        a declaration for the primed version of the attribute itself.
        """
        smt_code = []
        declared_types = set([])
        declared_vars = []

#        log.debug("Attributes: %s" % self.attributes)
    
        for attr in self.attributes:
            assert isinstance(attr, AbstractAttribute), attr
            #assert isinstance(attr.domain, Domain)
 
            # declare the attribute data-type (if needed)
            #dt_decl = attr.domain.smt_declaration
#            dt_decl = attr.smt_declaration
#            type_name = attr.smt_type_name
            for dt_decl,type_name in zip(attr.smt_declarations,attr.smt_type_names):

#                log.debug("Check declared types: %s vs %s" % (type_name, declared_types))
                if dt_decl and type_name not in declared_types:
                    smt_code.append(dt_decl) #was: attr.domain.dt_decl)
                    declared_types.add(type_name)
    
            # declare one constant for the (straight) attribute
#            attr_declaration = "(declare-const %s %s)" % (attr.name, type_name) #attr.domain.name)

            attr_declarations = [] #= ""
            for (var, dt) in zip(attr.variables, attr.datatypes):

                if var in declared_vars:
                    continue

                attr_declarations.append("(declare-const %s %s)" % (var, dt.name))
                attr_axioms = dt.smt_var_axioms(var)
                if attr_axioms:
                    attr_declarations.append(attr_axioms)

                declared_vars.append(var)

            smt_code.append("\n".join(attr_declarations)) 
 
#            log.debug("Check primed flag: %s" % primed) 
            if primed:       
                # declare one constant for the primed attribute
                primed_attr = attr.primed()
#                log.debug("primed attribute: %s -> %s" % (primed_attr, primed_attr.variables))
                primed_attr_declarations = [] #""
                for (primed_var, primed_dt) in zip(primed_attr.variables, primed_attr.datatypes):

                    if primed_var in declared_vars:
                        continue

                    primed_attr_declarations.append("(declare-const %s %s)" % (primed_var, primed_dt, )) # NB type of attr and primed_attr is the same #primed_attr.domain.name)
                    primed_attr_axioms = primed_dt.smt_var_axioms(primed_var)

                    if primed_attr_axioms:
                        primed_attr_declarations.append(primed_attr_axioms)

                    declared_vars.append(primed_var)

                smt_code.append("\n".join(primed_attr_declarations))
 
        return smt_code


    @contract(source_pred="list(is_attribute_predicate)", instr="None|dict", target_pred="None|list(is_attribute_predicate)", guard="None|is_precondition", returns="list(string)")
    def to_smt_problem(self, source_pred, instr=None, target_pred=None, preconditions=None, guard=None):
        """
        This method returns a syntattically valid SMT problem corresponding to the
        collected assertions and instruction.

        TODO atm, this only covers Z3 syntax
        """
        assert (instr is None and target_pred is None and guard is not None) or (instr is not None and target_pred is not None and guard is None)
        smt_code = []
        
        smt_code.append("; begin declarations of attributes of source and target states")

        # add declaration of attributes and their types
        do_primed = (target_pred is not None)
        #do_primed = True
        smt_code.extend(self.get_smt_attribute_declaration(primed=do_primed))     

        smt_code.append("; end declarations of attributes of source and target states")

#        env = pred_to_env(source_pred) or pred_to_env(target_pred) # take the union of the two environments

        # add problem assertions derived from source state, instruction, target state
        for curr in self.combine_assertions(source_pred, instr=instr, target_pred=target_pred, preconditions=preconditions, guard=guard):
            if isinstance(curr, AttributePredicate):
                smt_code.append(curr.smt_assert())
            elif isinstance(curr, basestring):
                smt_code.append(curr)
            else:
                raise ValueError("Don't know how to handle assertion (%s): %s" % (type(curr), curr))

        smt_code.append("(check-sat)")

#        return "\n".join(smt_code)
        return smt_code

    
    def _get_error(self):
        err_msg = ""

        try:
            err_msg = " ".join(self._cmd.stderr.readlines())
            self._log_smt(err_msg, is_input=False)
        except IOError:
            # there was no error to read
            pass

        return err_msg

    def _get_output(self):
        out = self._cmd.stdout.readline()
        self._log_smt(out, is_input=False)
        return out

    @contract(commands="list(string)|string", is_input="bool")
    def _log_smt(self, commands, is_input=True):

        line_format = "**<< {line}"
        if is_input:
            line_format = "{line_num}>> {line}"

        if isinstance(commands, basestring):
            commands = commands.split("\n")

        for curr_command in commands:
            if curr_command:
                if is_input:
                    self._line_num = self._line_num + 1
                line = line_format.format(line=curr_command,line_num=self._line_num)
                log_smt.debug(line)


    def _check_error(self, line, default=None):
        
        if line.startswith("(error "):
            raise ValueError(line)
        elif default:
            raise ValueError(default)
    
    @contract(commands="list(string)", returns="string")
    def get_tool_answer(self, commands):
        """
        Receives a list of commands to be executed by the tool.
        Returns the text output of the tool.
        """ 

        self._log_smt(commands)

        commands = "\n".join(commands)
        self._cmd.stdin.write(commands + "\n")

        answer = self._get_output()

        err_msg = self._get_error()
        if err_msg:
            sys.stderr.write(err_msg)

        return answer

    @contract(source_pred="tuple", guard=Precondition, preconditions="None|list(is_precondition)", returns="bool")
    def check_sat_guard(self, source_pred, guard, preconditions=None):
 
        check("list(is_abstract_attribute)", self.attributes)       
        source_attpred = conf_to_attribute_predicate(source_pred, self.attributes)
        
#        new_preconditions = [ guard ]
#        if preconditions:
#            new_preconditions.extend(preconditions)

        try:
            commands = self.to_smt_problem(source_attpred, guard=guard) #, preconditions=new_preconditions)

            answer = self.get_tool_answer(commands)

            log.debug("check guard: %s vs %s, given: %s ? %s" % (source_attpred,guard.code,preconditions, answer))
    
            smt_res = False
            if answer.strip() == "sat":
                smt_res = True
            elif answer.strip() == "unsat":
                smt_res = False
            else:
                self._check_error(answer, default="Unknown value")

        except (ForgottenVariableException, ForgottenMethodException) as e:
            # conservative abstraction: assume that if a required 
            # variable has been abstracted, then the formula is 
            # satisfiable
            log.debug("Unable to verify guard because of a forgotten variable or method: %s. Details: %s. Let us assume the guard is satisfied ..." % (guard.code, e))
            smt_res = True

        return smt_res


    @contract(source_pred="tuple", instr="dict", target_pred="tuple", preconditions="None|list(is_precondition)", returns="bool")
    def check_sat_instr(self, source_pred, instr, target_pred, preconditions):

        check("list(is_abstract_attribute)", self.attributes)

        source_attpred = conf_to_attribute_predicate(source_pred, self.attributes)
        target_attpred = conf_to_attribute_predicate(target_pred, self.attributes)

        check("list(is_attribute_predicate)", source_attpred)
        check("list(is_attribute_predicate)", target_attpred)

        try:
            commands = self.to_smt_problem(source_attpred, instr, target_attpred, preconditions=preconditions)
            answer = self.get_tool_answer(commands)

            smt_res = False
            if answer.strip() == "sat":
                smt_res = True
            elif answer.strip() == "unsat":
                smt_res = False
            else:
                self._check_error(answer, default="Unknown value")
        except (ForgottenVariableException, ForgottenMethodException) as e:
            # conservative abstraction: if a needed variable has been
            # abstracted, assume the problem is satisfiable
            smt_res = True


        return smt_res

    def push(self):
        cmd = "(push)\n"
        self._log_smt(cmd)
        self._cmd.stdin.write(cmd)

        
    def pop(self):
        cmd = "(pop)\n"
        self._log_smt(cmd)
        self._cmd.stdin.write(cmd)


    def get_model(self):
        # TODO run the (get-model) command
        pass


    def get_unsat_core(self):
        # TODO run the (get-unsat-core) command
        pass

@contract(instr_type="string", pc_jump_stack="list[N](tuple(string|None,is_pc,is_pc)),N>0", target="string|None", returns="is_pc")
def find_break_target(instr_type, pc_jump_stack, target):

    (identifier,pc_curr_continue,pc_curr_break) = pc_jump_stack[-1]

    if target:
        found = False
        pos = len(pc_jump_stack)-1
        while not found and pos >= 0:
            (identifier, pc_curr_continue, pc_curr_break) = pc_jump_stack[pos]
            if identifier == target:
                found = True
            pos = pos - 1

        if not found:
            raise ValueError("Cannot break to unknown label '%s'" % target)


    if instr_type == "ASTBreak":
        pc_res = pc_curr_break
    else:
        pc_res = pc_curr_continue

    return pc_res

@contract(curr_tuple="tuple", pos="int", value="*")
def tuple_replace(curr_tuple, pos, value):
    new_values = list(curr_tuple)
    new_values[pos] = value
    return tuple(new_values)

def set_current_method(method):
    global THE_METHOD
    THE_METHOD = method

def get_current_method():
    global THE_METHOD
    return THE_METHOD

@contract(node="dict", returns="tuple(set(string)|None,string|None)")
def parse_clock_condition(node):
    assert "expression" in node, "Expected node containing the expression of the guard"

    clock_variables = set([])
    clock_condition = ""

    curr_method = get_current_method()
    class_fqname = curr_method.parent.fqname
    method_name = curr_method.name

    identifiers = get_identifiers(node)

    is_clock_condition = len(identifiers) > 0

    if is_clock_condition:
        # in our simplified setting, in order to be a clock conditon, all the identifiers in the 
        # guard must be timestamps;
        for curr_identifier in identifiers:
            if not KnowledgeBase.is_timestamp(class_fqname, method_name, curr_identifier):
                is_clock_condition = False
                break

        if is_clock_condition:
            # this is a simplified assumption as well: I may have only timestamps among the 
            # identifiers, but used for method calls or other fancy computations; the clock conditions
            # should also check for "basic" arithmetic operators + comparison operators
            clock_variables = set(identifiers)
            clock_condition = node["code"] # TODO this is temporary, and must be processed

    return clock_variables, clock_condition


@contract(source="is_configuration", pc_source=PC, instr="dict", state_space="is_state_space", project="is_project", visited_locations="set(string)", preconditions="list(is_precondition)|None", postconditions="list(is_precondition)|None", deadlines="list(is_pc)|None", returns=ReachabilityResult)
def check_reach(source, pc_source, instr, state_space, project, visited_locations, preconditions=None, postconditions=None, pc_jump_stack=None, deadlines=None):
    assert preconditions is None or isinstance(preconditions, list)
    assert pc_jump_stack is None or isinstance(pc_jump_stack, list)
    assert deadlines is None or isinstance(deadlines, list)

    if pc_jump_stack is None:
        pc_jump_stack = []

    if preconditions is None:
        preconditions = []

    if postconditions is None:
        postconditions = []

    if deadlines is None:
        deadlines = []

    # produce a label out of a code block
    rows = instr["code"].split("\n")
    stripped_rows = map(lambda r: r.strip(), rows)
    instr_text = " ".join(stripped_rows)

#    print "domains: %s" % domains
    instr_label = instr_text
    len_label = len(instr_label)
    if len_label > 50:
        instr_label = instr_label[:15].strip() + "..." + instr_label[len_label-15:].strip()

    instr_type = instr["nodeType"]
    source_pred = state_space.value(source)
    source_loc = build_location(source, pc_source)

    log.debug("Add source to visited locations: %s. Previous visited locations: %s. Num visited locations: %s." % (source_loc, visited_locations, len(visited_locations)))
    assert source_loc.name not in visited_locations, "Location %s already visited" % source_loc.name
    visited_locations.add(source_loc.name)

    # begin 
    # res contains a list of reachable configurations, a list of Edge's, a list of final Location's, and a list of variables
    reachable = []
    edges = []
    final = []
    external = []
    variables = set([])
 
    pc_target = None
   
    if instr_type == "ASTRE":
        """
        This is the case of "simple" statements
        """

#        log.info("Check ASTRE: '%s' %s ..." % (instr_text, pc_source))
##
        node_exp_type = instr["expression"]["nodeType"]
        log.debug("Found ASTRE statement (%s)" % node_exp_type)

        pc_target = pc_source + 1

        for target in state_space.enumerate:
            assert isinstance(target, tuple)

            log.debug("check statement: (%s,%s) -[%s]?-> (%s,%s)" % (source,pc_source,instr_label,target,pc_target))


            target_pred = state_space.value(target)
            with SMTProb(state_space.attributes, project) as smt_prob:
                cache_found = SMTProb.smt_cache_lookup(source, instr, target)

                is_time_transition = False
                is_sat = False
                if cache_found is not None:
                    is_sat = cache_found
                else:
                    smt_prob.push()
                    try:
                        is_sat = smt_prob.check_sat_instr(source_pred, instr, target_pred, preconditions)
                    except TimeTransitionException, e:
                        is_sat = (source_pred == target_pred)
                        is_time_transition = True
                    except UpdateTimestampException, e:
                        is_sat = (source_pred == target_pred)

                        if is_sat:
                            now_timestamps = KnowledgeBase.get_now_timestamps(e.class_fqn, e.method_name)
                            deadline_exp = node_to_deadline_exp(e.rhs_node, now_timestamps)
                            KnowledgeBase.set_deadline_exp(e.class_fqn, e.method_name, e.var, deadline_exp)


                    smt_prob.pop()
                    SMTProb.smt_cache_store(source, instr, target, is_sat)
        
                log.debug("Statement SAT: %s" % is_sat)
                if is_sat:
                    target_loc = build_location(target, pc_target)
                    edge_label = instr["code"]

                    if is_time_transition:
                        edge = TimeEdge(source_loc, target_loc, edge_label)
                        log.debug("Add time edge: %s" % edge)
                    else:
                        edge = Edge(source_loc, target_loc, edge_label)
     
                    reachable.append(target)
                    edges.append(edge) 
                    final.append(target_loc)
        
    elif instr_type == "ASTIf":
        assert "guard" in instr
        assert "ifBranch" in instr
        assert "elseBranch" in instr
        assert "stms" in instr["ifBranch"]
        
        log.info("Check ASTIf: '%s' %s..." % (instr_text,pc_source))

        guard = instr["guard"]
        assert "code" in guard
        assert "expression" in guard
        pc_source_then = PC(pc_source.pc).push(0).push(0)
        pc_source_else = PC(pc_source.pc).push(1).push(0)

        stms_then = instr["ifBranch"]["stms"]
        stms_else = None

        if instr["elseBranch"]: 
            assert "stms" in instr["elseBranch"]
            stms_else = instr["elseBranch"]["stms"]

        final_then = final_else = []
        pc_target = pc_source + 1

        clock_variables, clock_condition = parse_clock_condition(guard)
        is_clock_condition = len(clock_variables) > 0

        print "check is clock condition (ASTIf): %s |-> %s" % (guard["code"], is_clock_condition)
        # initialize those variables
        is_then_reachable = is_else_reachable = is_clock_condition
        
        if not is_clock_condition:

            with SMTProb(state_space.attributes, project) as smt_prob:
    
                assert "expression" in guard
    
                smt_prob.push()
                is_then_reachable = smt_prob.check_sat_guard(source_pred, guard=Precondition(guard["expression"]))
                smt_prob.pop()
    
                smt_prob.push()
                is_else_reachable = smt_prob.check_sat_guard(source_pred, guard=Negate(guard["expression"])) 
                smt_prob.pop()
    
        if (is_then_reachable):
            reachable_then = [ source ]

            preconditions.append(Precondition(guard["expression"]))
            rr_then = compute_reachable(reachable_then, pc_source_then, stms_then, state_space, project, visited_locations, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            preconditions.pop()    

            variables |= rr_then.variables
            final_then = rr_then.final_locations
            edges.extend(rr_then.edges) #edges_then)

            # add ed edge from source to the begin of the "then" branch
            then_loc = build_location(source, pc_source_then)

            if is_clock_condition:
                edge_then = Edge(source_loc, then_loc, guard=clock_condition, clock_variables=clock_variables)
            else:
                edge_then = Edge(source_loc, then_loc, "if (%s)" % guard["code"])
 
            # add an edge to the set of resulting edges       
            edges.append(edge_then) 
            external.extend(rr_then.external_locations)

        if stms_else and is_else_reachable:
            # the else statement is optional: here it is present and reachable
            assert "expression" in guard

            #curr_pc = PC(pc_source_else.pc)
            reachable_else = [ source ]

            preconditions.append(Negate(guard["expression"]))
            rr_else = compute_reachable(reachable_else, pc_source_else, stms_else, state_space, project, visited_locations, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            preconditions.pop()

            variables |= rr_else.variables
            final_else = rr_else.final_locations
       
            edges.extend(rr_else.edges) #edges_else)
            external.extend(rr_else.external_locations)
    
            # add en edge from source to the begin of the "else" branch
            else_loc = build_location(source, pc_source_else)
            if is_clock_condition:
                edge_else = Edge(source_loc, else_loc, guard="not (%s)" % clock_condition, clock_variables=clock_variables)
            else:
                edge_else = Edge(source_loc, else_loc, "else (not(%s))" % guard["code"])
            # add an edge to the results
            edges.append(edge_else)
        elif not stms_else and is_else_reachable:
            final_else = [ source_loc ] 


        # add an edge from each one of the reachable state to the same
        # state with a "popped" pc

        for loc in final_then + final_else:
            assert isinstance(loc, Location)
            (state_final_conf, pc_final) = parse_location(loc)

            # the end-if location has the same predicate of the reached final state, but the pc of the line after the if-statement
            end_if_loc = build_location(state_final_conf, pc_target)            

            # add edges and final locations to the results
            edges.append(Edge(loc, end_if_loc, "endif"))
            final.append(end_if_loc)
            reachable.append(state_final_conf)

        check_closure(pc_target, reachable, final, external)
 
    elif instr_type == "ASTWhile":

        assert "expr" in instr, instr.keys()
        assert "stms" in instr
        assert "identifier" in instr, "ASTWhile: %s" % instr

        while_identifier = instr["identifier"]
        stms_while = instr["stms"]
        guard = instr["expr"]
        assert "code" in guard
        assert "expression" in guard
        pc_target = pc_source + 1

        tovisit_sources = [ source, ]
#        visited_sources = []

        pc_source_while = PC(pc_source.pc).push(0)

        clock_variables, clock_condition = parse_clock_condition(guard)
        is_clock_condition = len(clock_variables) > 0

        print "check is clock condition (ASTWhile): %s |-> %s" % (guard["code"], is_clock_condition)
        # initialize those variables
        is_while_reachable = is_clock_condition
        is_not_while_reachable = is_clock_condition

        while len(tovisit_sources) > 0:
            curr_source_conf = tovisit_sources.pop()
            curr_source_loc = build_location(curr_source_conf, pc_source)

            assert curr_source_loc not in visited_locations, "Location in ASTWhile already visited: %s" % curr_source_loc.name

#            visited_sources.append(curr_source_conf)
            #visited_locations.add(curr_source_loc)
            curr_source_pred = state_space.value(curr_source_conf)

            if not is_clock_condition:
                # determine whether the guard and/or its negation are satisfiable
                with SMTProb(state_space.attributes, project) as smt_prob:
        
                    smt_prob.push()
                    is_while_reachable = smt_prob.check_sat_guard(curr_source_pred, guard=Precondition(guard["expression"]))
                    smt_prob.pop()
            
                    smt_prob.push()
                    is_not_while_reachable = smt_prob.check_sat_guard(curr_source_pred, guard=Negate(guard["expression"]))
                    smt_prob.pop()
    
            assert not is_clock_condition or (is_while_reachable and is_not_while_reachable)

            # the guard is satisfiable: enter the block
            if is_while_reachable:
        
                preconditions.append(Precondition(guard["expression"]))
                pc_jump_stack.append((while_identifier, pc_source_while, pc_target))
                
                rr_while = compute_reachable([ curr_source_conf, ], pc_source_while, stms_while, state_space, project, visited_locations, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
                
                variables |= rr_while.variables
                pc_jump_stack.pop()
                preconditions.pop()
    
    #            log.debug("While reachable result: %s" % rr_while)
    
                edges.extend(rr_while.edges)
                external.extend(rr_while.external_locations)
        
                # add edge from source to the begin of the while
                # TODO at the moment we don't take advantage of the while-guard as precondition
                while_loc = build_location(curr_source_conf, pc_source_while)

                if is_clock_condition:  
                    while_edge = Edge(curr_source_loc, while_loc, guard=clock_condition, clock_variables=clock_variables)
                else:
                    while_edge = Edge(curr_source_loc, while_loc, "while (%s)" % guard["code"])
                    
                edges.append(while_edge)
 
                # every final state is used as starting point for a new iteration of the 
                # saturation algorithm of the while block   
                for loc in rr_while.final_locations: 

                    assert isinstance(loc, Location)
                    (state_final_conf, pc_final) = parse_location(loc)
    
                    # go back to a new evaluation of the while condition (with the current configuration)
                    loc_back = build_location(state_final_conf, pc_source)
                    edge_back = Edge(loc, loc_back, "end while")
                    edges.append(edge_back)
    
#                    log.debug("Curr while final: %s vs %s" % (state_final_conf, visited_sources))
    
                    # in case the configuration has not been visited before
                    # by an iteration of the while, then visit it
#                    if state_final_conf not in visited_sources:
                    if loc.name not in visited_locations:
                        # unroll a new (abstract) while loop
                        tovisit_sources.append(state_final_conf)
                       

            # the negation of the guard is satisfiable: jump over the block
            if is_not_while_reachable:
    
                # add edge from source to the end of the while
                curr_source_neg_loc = build_location(curr_source_conf, pc_target)
                if is_clock_condition:  
                    while_exit_edge = Edge(curr_source_loc, curr_source_neg_loc, guard="not (%s)" % clock_condition, clock_variables=clock_variables)
                else:
                    while_exit_edge = Edge(curr_source_loc, curr_source_neg_loc, "not (%s)" % guard["code"])
                edges.append(while_exit_edge)
                final.append(curr_source_neg_loc)
                reachable.append(curr_source_conf)
    
        check_closure(pc_target, reachable, final, external)

    elif instr_type == "ASTDoWhile":
        assert "expr" in instr, instr.keys()
        assert "stms" in instr
        assert "identifier" in instr, "ASTDoWhile: %s" % instr

        while_identifier = instr["identifier"]
        stms_while = instr["stms"]
        guard = instr["expr"]
        assert "code" in guard
        assert "expression" in guard
        pc_target = pc_source + 1

        # always enter the while block
        pc_source_while = PC(pc_source.pc).push("0")

        pc_jump_stack.append((while_identifier, pc_source_while, pc_target))
        rr_while = compute_reachable([source], pc_source_while, stms_while, state_space, project, visited_locations, preconditions, pc_jump_stack, deadlines=deadlines)
        pc_jump_stack.pop()

        variables |= rr_while.variables
        edges.extend(rr_while.edges)
        external.extend(rr_while.external_locations)

        # add an edge that "falls" inside the while block, from outside
        do_loc = build_location(source, pc_source_while)
        do_edge = Edge(source_loc, do_loc, "do")
        edges.append(do_edge)

        
        if len(rr_while.final_locations) == 0:
            reachable = [ ] #source ]
            final = [ ] #source_loc ]
        else:

            # for each reachable configuration, determine whether the guard and/or its negation are satisfiable
            for loc_final in rr_while.final_locations:
    
                conf_final, pc_final = parse_location(loc_final)
                pred_final = state_space.value(conf_final)

                with SMTProb(state_space.attributes, project) as smt_prob:
        
                    guard_exp = guard["expression"]
                    smt_prob.push()
                    is_while_reachable = smt_prob.check_sat_guard(pred_final, guard=Precondition(guard_exp))
                    smt_prob.pop()
            
                    smt_prob.push()
                    is_not_while_reachable = smt_prob.check_sat_guard(pred_final, guard=Negate(guard_exp))
                    smt_prob.pop()
        
                # the guard is satisfiable: jump back to the statement block
                if is_while_reachable:
                    
                    preconditions.append(Precondition(guard["expression"]))
                    pc_jump_stack.append((while_identifier, pc_source_while, pc_target))
                    rr_while_back = compute_reachable([conf_final], pc_source_while, stms_while, state_space, project, visited_locations, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
                    pc_jump_stack.pop()
                    preconditions.pop()
        
                    variables |= rr_while_back.variables
                    edges.extend(rr_while_back.edges)
                    external.extend(rr_while_back.external_locations)
            
                    # add edge from current location to the begin of the while
                    # TODO at the moment we don't take advantage of the while-guard as precondition
                    while_loc = build_location(conf_final, pc_source_while)
                    while_edge = Edge(loc_final, while_loc, "while (%s)" % guard["code"])
                    edges.append(while_edge)
        
                    # if entered the while loop, for each final location of while, create an edge out of the while loop and an edge at the beginning of the while itself
                    # TODO this is an overapproximation (every final state can repeat the loop and/or exit); invoke the compute_reachable(...) procedure to "unfold" the loop (beware of endless loops)
                    for loc in rr_while_back.final_locations:
                        assert isinstance(loc, Location)
                        (state_final_conf, pc_final) = parse_location(loc)
        
                        loc_out = build_location(state_final_conf, pc_target)
                        edge_out = Edge(loc, loc_out, "end while")
                        edges.append(edge_out)
                        final.append(loc_out)
                        reachable.append(state_final_conf)
            
                        loc_restart = build_location(state_final_conf, pc_source)
                        edge_restart = Edge(loc, loc_restart)
                        edges.append(edge_restart)
            
            # the negation of the guard is satisfiable: jump over the while
            if is_not_while_reachable:

                # add edge from current location to the end of the while
                while_neg_loc = build_location(conf_final, pc_target)
                while_neg_edge = Edge(loc_final, while_neg_loc, "not (%s)" % guard["code"])
                edges.append(while_neg_edge)
                final.append(while_neg_loc)
                reachable.append(conf_final)

        check_closure(pc_target, reachable, final, external)

    elif instr_type in [ "ASTBreak", "ASTContinue" ]:
        # the main difference b/w ASTContinue and ASTBreak is that 
        # the former goes back to the pc of the referred statement, while the
        # latter goes *past* the pc of the referred statement 

        assert "target" in instr

        target_identifier = instr["target"]
        pc_target = find_break_target(instr_type, pc_jump_stack, target_identifier)
        assert isinstance(pc_target, PC)

        edge_label = "break" if instr_type == "ASTBreak" else "continue"

        if target_identifier:
            edge_label = "%s %s" % (edge_label, target_identifier)


        log.debug("Found pc target (%s): %s. PC break stack: %s" % (target_identifier, pc_target, pc_jump_stack))

        loc_out = build_location(source, pc_target)
        # new edge: from source location to final location
        edge_break = Edge(source_loc, loc_out, edge_label)
        edges.append(edge_break)  
        external.append(loc_out)

        # no final locations
        # (ASTBreak and ASTContinue break the compositionality approach)
##    elif instr_type == "ASTReturn":
##        # do nothing    
##        log.debug("Return: %s" % instr)
##        # begin BIG-FRAGILE-HACK
##        res = instr["expr"]["code"] 
##        (state_loc,pc_loc) = parse_location(source_loc)
##        if res == "true":
##            state_loc = tuple_replace(state_loc, 2, 0) # 2 is the position of "res", 0 encodes true
##        else:
##            state_loc = tuple_replace(state_loc, 2, 1) # 2 is the position of "res, 1 encodes false
##        # end BIG-FRAGILE-HACK
##        pc_target = pc_source + 1
##        return_loc = build_location(source, pc_target)
##        edges.append(Edge(source_loc,return_loc))
##        reachable.append(state_loc)
###        final.append(source_loc)
##
###        check_closure(pc_target, reachable, final, external)
##
    elif instr_type == "ASTTry":
        assert "tryBranch" in instr
        assert "catchBranch" in instr
        assert "finallyBranch" in instr

        pc_target = pc_source + 1

        # recursive call on the try branch
        stms_try = instr["tryBranch"]["stms"]
        pc_source_try = PC(pc_source.pc).push("0").push("0")
        rr_try = compute_reachable([source], pc_source_try, stms_try, state_space, project, visited_locations, preconditions, pc_jump_stack, deadlines=deadlines)

        variables |= rr_try.variables
        edges.extend(rr_try.edges)
        external.extend(rr_try.external_locations)
    
        # add edge from source to the begin of the try
        try_loc = build_location(source, pc_source_try)
        try_edge = Edge(source_loc, try_loc, "")
        edges.append(try_edge)

        final_try = rr_try.final_locations
    
        # recursive call on the catch branch
    
        # begin BIG-HACK-FOR-DETECTING-EXCEPTION-LOCATIONS
        found_exception_locs = [] 
        found_exception_confs = []
        
#        for curr_edge in rr_try.edges:
#            log.debug("Check edge goes to exception: %s" % curr_edge)
#            (edge_source_conf,edge_source_pc) = parse_location(curr_edge.target)
        for loc in rr_try.locations:
            (loc_conf, loc_pc) = parse_location(loc)
            if len(loc_conf) > 2 and int(loc_conf[1]) == 1:
                found_exception_locs.append(loc)
                found_exception_confs.append(loc_conf)
        # end BIG-HACK-...

        log.debug("Found exception states: %s" % found_exception_locs)
        stms_catch = instr["catchBranch"][0]["stms"] # HACK assume there is exactly 1 catchBranch
        pc_source_catch = PC(pc_source.pc).push("1").push("0")
        log.debug("Compute reachable in catch ...")
        rr_catch = compute_reachable(found_exception_confs, pc_source_catch, stms_catch, state_space, project, visited_locations, preconditions, pc_jump_stack, deadlines=deadlines)

        variables |= rr_catch.variables
        edges.extend(rr_catch.edges) # TODO check this

        # add edge from exception states in the try block, to the begin of the catch block
        for excp_loc in found_exception_locs:   
            (excp_conf, excp_pc) = parse_location(excp_loc)
            catch_excp_loc = build_location(excp_conf, pc_source_catch)
            edges.append(Edge(excp_loc, catch_excp_loc, "catch"))

        final_catch = rr_catch.final_locations


        for loc in final_try + final_catch:
            log.debug("Sleep check final loc: %s" % loc)

            assert isinstance(loc, Location)
            (state_loc, pc_loc) = parse_location(loc)
            end_try_loc = build_location(state_loc, pc_target)
            edges.append(Edge(loc, end_try_loc))       
            final.append(end_try_loc)
            reachable.append(state_loc)

        check_closure(pc_target, reachable, final, external)

###    elif instr_type == "ASTDeadline":
###
###        assert "stms" in instr
###
###        # add a clock variable, its lower bound, and its upper bound 
###        cv = FreshNames.get_clock_variable(pc_source, prefix="dl")
###        (lower, upper) = FreshNames.get_clock_bounds(pc_source, prefix="dl")
###        variables = variables | set([ cv, lower, upper ])
###
###        # explore recursively the reachable states
###        stms_deadline = instr["stms"]
###        pc_source_deadline = PC(pc_source.pc).push("0")
###        deadlines.append(pc_source) 
###        rr_deadline = compute_reachable([source],pc_source_deadline, stms_deadline, state_space, project, visited_locations, preconditions, pc_jump_stack, deadlines=deadlines)
###
###        deadlines.pop()
###        variables |= rr_deadline.variables
###        edges.extend(rr_deadline.edges)
###    
###        # add edges from source to the begin of the deadline
###        deadline_loc = build_location(source, pc_source_deadline)
###        deadline_edge = Edge(source_loc, deadline_loc, "")
###        edges.append(deadline_edge)
###
###        final_deadline = rr_deadline.final_locations
###
###        pc_target = pc_source + 1
###
###        for loc in rr_deadline.final_locations:
###            (loc_conf, loc_pc) = parse_location(loc)
###            post_loc = build_location(loc_conf, pc_target)
###            e = Edge(loc, post_loc)
###            edges.append(e)
###
###        log.debug("Final deadline locations: %s" % final_deadline)
###        for loc in final_deadline:
###            log.debug("Deadline check final loc: %s" % loc)
###            assert isinstance(loc, Location)
###            (state_loc, pc_loc) = parse_location(loc)
###            end_deadline_loc = build_location(state_loc, pc_target)
###            edges.append(Edge(loc, end_deadline_loc))       
###            final.append(end_deadline_loc)
###            reachable.append(state_loc)
###
###        check_closure(pc_target, reachable, final, external)
###
    elif instr_type == "ASTForEach" or instr_type == "ASTFor":

        assert "identifier" in instr
        assert "stms" in instr

        foreach_identifier = instr["identifier"]
        stms_foreach = instr["stms"]
        pc_target = pc_source + 1

        pc_source_foreach = PC(pc_source.pc).push(0)

        tovisit_sources = [ source, ]
#        visited_sources = []

        while len(tovisit_sources) > 0:

            curr_source_conf = tovisit_sources.pop()
            curr_source_loc = build_location(curr_source_conf, pc_source)

            assert curr_source_loc not in visited_locations, "Location in ASTFor/ASTForEach already visited: %s" % curr_source_loc.name


#            visited_sources.append(curr_source_conf)
            #visited_locations.add(curr_source_loc)
        
            reachable_foreach = [ curr_source_conf ]    
            pc_jump_stack.append((foreach_identifier, pc_source_foreach, pc_target))
            
            rr_foreach = compute_reachable(reachable_foreach, pc_source_foreach, stms_foreach, state_space, project, visited_locations, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            
            variables |= rr_foreach.variables
            pc_jump_stack.pop()

            begin_foreach_loc = build_location(source, pc_source_foreach)

            edges.extend(rr_foreach.edges)
            edges.append(Edge(source_loc, begin_foreach_loc, "for each"))

            for loc in rr_foreach.final_locations:
                # each final location should either quit the loop, or restart
                assert isinstance(loc, Location)
                (state_final_conf, pc_final) = parse_location(loc)

                loc_out = build_location(state_final_conf, pc_target)
                edge_out = Edge(loc, loc_out, "end foreach")
                edges.append(edge_out)
                final.append(loc_out)
                reachable.append(state_final_conf)

#                log.debug("Curr foreach final: %s vs %s" % (state_final_conf, visited_sources))

                # add an edge restarting the foreach loop
                loc_restart = build_location(state_final_conf, pc_source_foreach)
                edge_restart = Edge(loc, loc_restart, "for each")
                edges.append(edge_restart)

                # in case the configuration has not been visited before
                # by an iteration of the foreach, then visit it
#                if state_final_conf not in visited_sources:
                if loc.name not in visited_locations:
                    tovisit_sources.append(state_final_conf)

        check_closure(pc_target, reachable, final, external)

    else:
        #raise ValueError("Instruction type not covered: %s" % instr)
        log.warning("Instruction ignored (%s): %s" % (instr_type, instr))

   
    assert (len(reachable) >= 0 and len(edges) >= 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0), "# reachable: %s, # edges: %s, # final: %s. Instruction: %s" % (len(reachable), len(edges), len(final), instr)
    rr = ReachabilityResult(configurations=reachable, final_locations=final, external_locations=external, edges=edges, variables=variables)

    # handle stack of deadlines
    log.debug("Curr deadlines: %s" % deadlines)

    curr_edges = list(rr.edges)
    for pc in deadlines:
#        log.debug("Curr locations: %s" % rr.locations)
        cv = FreshNames.get_clock_variable(pc, "deadline")
        (lower, upper) = FreshNames.get_clock_bounds(pc, "deadline")

        for loc in rr.locations:
            # add invariant for states 
            inv = "%s <= %s" % (cv.name, upper.name)
            if loc.invariant:
                inv = "(%s) and (%s)" % (loc.invariant, inv)
            loc.set_invariant(inv)

            # add exception location and edge to it
            (loc_conf, loc_pc) = parse_location(loc)
            exc_conf = tuple_replace(loc_conf, 1, 1)
            exc_loc = build_location(exc_conf, loc_pc)
            
            guard = "%s >= %s" % (cv.name, upper.name)
            e = Edge(loc, exc_loc, guard=guard, label=guard)
    
            rr.edges.append(e)

        for e in curr_edges: #rr.edges:
            # add temporal guards in edge
            guard = "(%s >= %s) and (%s <= %s)" % (cv.name, lower.name, cv.name, upper.name)
            label = guard
            if e.guard:
                guard = "(%s) and (%s)" % (guard, e.guard)
            if e.label:
                label = "(%s) and (%s)" % (label, e.label)
            e.guard = guard
            e.label = label
            
            
    return rr

@contract(pc_target='is_pc',reachable='list(is_configuration)',final='list(is_location)',external='list(is_location)')
def check_closure(pc_target,reachable,final,external):

    # closure operation: any jump to an external location that is at the pc_target, become a jump 
    # to a final location check whether it has been reached via some break
    for loc in external:
        assert isinstance(loc, Location)
        (state_ext_conf, pc_ext) = parse_location(loc)
    
        log.debug("Check external (%s) reaches target (%s) ..." % (pc_ext, pc_target))

        if pc_ext.pc == pc_target.pc:
            # at least one external location goes to the pc_target
            # position, thus the negation of while is reachable
            final.append(loc)
            reachable.append(state_ext_conf)
 

@contract(conf_predicates="tuple", attributes="list(is_abstract_attribute)", returns="list(is_attribute_predicate)")
def conf_to_attribute_predicate(conf_predicates, attributes): #conf, attributes):
    res = []
#    for (attr, pred) in zip(attributes, conf):
    for (pred, attr) in zip(conf_predicates, attributes):
        check("is_abstract_attribute", attr)
        check("is_predicate", pred)
 
        res.append(AttributePredicate(attr, pred))

    return res


@contract(loc="is_location", returns="tuple(is_configuration,is_pc)")
def parse_location(loc):
    loc_label, pc_label = loc.name.split("@") # TODO this depends on how the location name is built

    loc_label = loc_label.strip("(),")
    conf = tuple()
    if len(loc_label) > 0:
        conf = tuple(map(int, loc_label.strip("(),").split(","))) # NB the parameter "()," means: remove any opening/closing parenthesis and comma, to handle the case of single element tuples, e.g. (0,) 
    pc = PC(pc_label)
    return (conf, pc)

##@contract(loc_list="list", new_loc_list="list", returns=bool)
##def while_fixpoint(loc_list, new_loc_list):
##    """
##    Return true iff the new_loc_list is entireley contained in loc_list
##    """
###    assert isinstance(loc_list, list)
###    assert isinstance(new_loc_list, list)
##
##    res = False
##
##    if len(loc_list) >= len(new_loc_list):
##        res = True
##        get_loc_name = lambda l: l.name
##        name_list = map(get_loc_name, loc_list)
##
##        for new_loc in new_loc_list:
##            assert isinstance(new_loc, Location)
##            if new_loc.name not in loc_list:
##                res = False
##                break
##
##    return res

@contract(klass="is_klass")
def get_class_attributes(klass):
#    assert isinstance(klass, Klass)
    assert "attributes" in klass.ast

    return klass.ast["attributes"]

@contract(method="is_method", returns="list(string)")
def get_method_attributes(method):
#    assert isinstance(method, Method)
    assert "parameters" in method.ast
    assert "declaredVar" in method.ast

    klass = method.parent
    method_name = method.name

    klass_attributes = get_class_attributes(klass)
    method_parameters = method.ast["parameters"]
    method_vars = method.ast["declaredVar"]

#    return klass_attributes + method_parameters + method_vars
    attributes = []

    for curr in klass_attributes + method_parameters:
#        attributes.append((curr["name"], False))
        attributes.append(curr["name"])
    
    for curr in method_vars:
#        attributes.append((curr["name"], True))
        attributes.append(curr["name"])
    
    return attributes


@contract(method=Method, domains="list(tuple(list(string)|string, is_domain, bool))", returns="is_state_space")
def get_state_space_from_method(method, domains):

    # 1. get method attributes
    attributes = get_method_attributes(method)

    # 2. initialize empty state-space
    ss = StateSpace()

    for (var_names, domain, is_local) in domains:
        check('list(string)|string', var_names)
        check('is_domain', domain)
        check('bool', is_local)
        
        abs_att = AbstractAttribute(var_names, domain, is_local)
        ss.add_attribute(abs_att)

    return ss


##@contract(class_fqn="string", class_path="string", method_name="string", project=Project, returns=Method)
##def get_method(project, class_fqn, class_path, method_name):
##    # check this works (case 1: no dot, case 2: one or more dots)
##    class_name = ""
##    package_name = ""
##
##    fqn_parts = class_fqn.rsplit(".", 1)
##
##    if len(fqn_parts) == 1:
##        class_name = fqn_parts[0]
##    else:
##        package_name = fqn_parts[0]
##        class_name = fqn_parts[1]
##
##    klass = Klass(class_name, package_name, "file://%s" % class_path, project)
##    m = Method(method_name, klass)
##
##    return m

@contract(method="is_method", state_space="is_state_space", returns="is_ta")
def translate_method_to_ta(method, state_space):

    set_current_method(method)

    assert get_current_method() != None

    #instructions = method.ast["stms"]
    instructions = method.instructions

    if len(instructions) == 0:
        raise ValueError("The passed method has no instructions. This is not allowed.")

    # pre-analysis of variable timestamps
#    now_methods = KnowledgeBase.get_now_methods()
#    timestamps = get_timestamps(method) #, now_methods)    
#    only_now_assignments = {}
#    for var, nodes in timestamps.iteritems():
#        is_now_variable = check_now_assignments(nodes, now_methods)
#        only_now_assignments[var] = is_now_variable
#
#        # exploit the analysis of timestamps in order to add attributes to the state-space
#        if is_now_variable:
#            try:
#                attr = state_space.get_attribute(var)
#            except ValueError, e:
#                # attribute for the now variable does not exist, add one
#                attr = AbstractAttribute(variables=[var,],domain=INTEGERS,is_local=False)    
#                assert attr.name == var
#                state_space.add_attribute(attr)

    class_fqn = method.parent.fqname
    compare_absolute = itertools.combinations(KnowledgeBase.get_absolute_timestamps(class_fqn, method.name), 2)
    compare_relative = itertools.combinations(KnowledgeBase.get_relative_timestamps(class_fqn, method.name), 2)

    for left,right in list(compare_absolute) + list(compare_relative):
        attr = AbstractAttribute(variables=[left,right], domain=CompareVariables(datatypes=[Integer(), Integer()], predicates=[LT(left,right), Eq(left,right), GT(left,right)]), is_local=False)
        state_space.add_attribute(attr)

    ta = transform(method.name, instructions, state_space, method.project)

    return ta


@contract(node="dict", returns="string")
def literal_to_smt(node):
    assert "code" in node
    lit_value = node["code"]
#    lit_type = node["type"]
    STR_MARKERS = [ '"', "'" ]
    res = lit_value
##    if lit_value in [ "true", "false" ]:
##        # this match Java boolean values
##        res = lit_value.lower()
##    elif re.match("^[0-9]+(\.[0-9]+)?$|^\.[0-9]+$", lit_value): #lit_value.isdigit():
##        # this matches Java integer values and float values
##        res = lit_value
##    elif lit_value == "null": 
##        # this matches a null pointer or a string/char constant
##        res = "%s" % SymbolTable.add_literal(lit_value)
##    elif lit_value[0] in STR_MARKERS and lit_value[-1] == lit_value[0]:
###        res = "%s" % SymbolTable.add_literal(lit_value[1:-1])  
##        lit_code = SymbolTable.add_literal(lit_value[1:-1])
##        res = "(init-AbsString %s %s)" % (lit_code, len(lit_value) - 2)
##    elif re.match("^[a-zA-Z0-9_]+$", lit_value):
##        # this match an identifier, not really a literal
###        res = lit_value
###        log.warning("Passed an identifier as literal, this should not happen: %s" % lit_value)
##        raise IdentifierAsLiteralException(lit_value)
##    else:
##        # don't know what literal is
##        log.warning("Don't know how to handle literal '%s'" % lit_value)
##

    lit_code,lit_type = SymbolTable.add_literal(lit_value)

    if lit_type == "String":
        res = "(init-AbsString %s %s)" % (lit_code, len(lit_value) - 2)
    elif lit_type in [ "bool", "int", "long", "char", ]:
        res = lit_code
    elif re.match("^[a-zA-Z0-9_]+$", lit_value):
        # this match an identifier, not really a literal
        raise IdentifierAsLiteralException(lit_value)
    else:
        # don't know what literal is
        log.warning("Don't know how to handle literal '%s'" % lit_value)

    return res

