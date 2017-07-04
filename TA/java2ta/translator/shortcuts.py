from time import sleep

import os
import tempfile
import subprocess
import fcntl

from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.translator.rules import ExtractMethodStateSpace, AddStates
from java2ta.translator.models import PC, ReachabilityResult, AttributePredicate, Cache, Precondition, Negate
from java2ta.ir.models import Project, Method, Klass
from java2ta.ta.models import TA, Location, Edge
from java2ta.abstraction.models import StateSpace, AbstractAttribute, Domain, Predicate

import sys
import logging
from contracts import contract, new_contract, check

log = logging.getLogger("main")

"""
This file collects functions that encode the most common scenarios for our
translation approach.
"""

"""
Scenario 1.

INPUT
- project name
- project path
- class name
- method name
- a dictionary mapping variables to domains

OUTPUT
- a timed automaton
"""
@contract(project=Project, class_fqn="string", class_path="string", method_name="string", domains="dict", returns=TA)
def translate_method_to_automaton(project, class_fqn, class_path, method_name, domains):

    # check this works (case 1: no dot, case 2: one or more dots)
    class_name = ""
    package_name = ""

    fqn_parts = class_fqn.rsplit(".", 1)

    if len(fqn_parts) == 1:
        class_name = fqn_parts[0]
    else:
        package_name = fqn_parts[0]
        class_name = fqn_parts[1]

    klass = Klass(class_name, package_name, "file://%s" % class_path, project)
    m = Method(method_name, klass)

    r1 = ExtractMethodStateSpace()
    r2 = AddStates()
    # TODO add rules for adding edges b/w states by static analysis (SMT based)

    e = Engine()
    e.add_rule(r1)
    e.add_rule(r2)

    ctx = Context()
    ctx.push({})
    ctx.update("abs_domains", domains)
    ta = TA(method_name)
    (ta_post, ctx_post) = e.run(m, ta, ctx)

    return ta_post

@contract(pred="is_configuration|is_configuration_repr", pc=PC, returns=Location)
def build_loc(pred, pc):

    loc_name = "%s%s" % (pred, pc)

    loc = Location(loc_name)

    return loc

@contract(source_conf="list(is_configuration)", pc_source=PC, instr="list(dict)", state_space=StateSpace, returns=ReachabilityResult)
def compute_reachable(source_conf, pc_source, instr, state_space, preconditions=None, pc_break_stack=None):
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

    log.debug("Compute reachable: source_conf=%s, pc_source=%s, instr=%s, state_space=%s, preconditions=%s, pc_break_stack=%s" % (source_conf, pc_source, instr, state_space, preconditions, pc_break_stack))

    edges = []
    reachable = []
    final = []
    external = []

    curr_pc = PC(pc_source.pc)
    for curr_instr in instr:

#        log.debug("Compute reachable: %s[%s] ..." % (curr_pc, curr_instr["code"]))

        # reachable and final must contain only information that are reached by
        # the last instruction (NB on the contrary, edges contain all the edges 
        # added by all the instructions)
        reachable = set([])
        final = []

#        log.debug("Curr PC: %s => External: %s" % (curr_pc, filter(lambda loc: loc.name.split("@")[1] == curr_pc.pc, external)))
    
        # avoid duplicates
        source_conf = set(source_conf)

        if not source_conf:
            log.warning("Exit for non reachable states at PC: %s" % curr_pc)
            break

        for source in source_conf:
            # TODO in principle each invocation of check_reach(...) is independent from the others
            rr = check_reach(source, curr_pc, curr_instr, state_space, preconditions=preconditions, pc_break_stack=pc_break_stack)
    
            edges.extend(rr.edges) #new_edges)
            reachable = reachable | set(rr.configurations) # avoid duplicates #.extend(rr.configurations) #new_reachable)
            final.extend(rr.final_locations) #new_final)
            external.extend(rr.external_locations)

        # next iteration starts from reached configurations, and advance PC by 1
        log.debug("Compute reachable next iteration: (%s)%s -> (%s)%s" % (source_conf, curr_pc, reachable, curr_pc + 1))
        source_conf = reachable
        curr_pc = curr_pc + 1

#    assert (len(reachable) >= 0 and len(edges) >= 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0)
    return ReachabilityResult(configurations=reachable, final_locations=final, external_locations=external, edges=edges)


@contract(name="string", instructions="list(dict)", state_space=StateSpace, returns=TA)
def transform(name, instructions, state_space):

    # TODO at the moment we call TA the class for the FSA ... this is not a big issue, but I leave
    # this note just to keep track of this "oddity"
    fsa = TA(name)

    log.info("State space attributes:")
    for attr in state_space.attributes:
        log.info("%s -> %s" % (attr.name, attr.domain))

    reach = dict()
    pc_source = PC(initial="0")
    source_conf = state_space.initial_configurations

    rr = compute_reachable(source_conf, pc_source, instructions, state_space)

    log.info("reachable: %s, final: %s, external: %s" % (rr.configurations, rr.final_locations, rr.external_locations))

    if len(rr.edges) > 0:
        for e in rr.edges:
            assert isinstance(e, Edge), e

            fsa.get_or_add_edge(e)

    return fsa


class SMTProb(object):

    OP_DECODE = { "plus": "+", "minus": "-", "greater": ">", "less": "<", "mul": "*" }

    _SMT_CACHE = Cache("smt")
    _NODE_TO_SMT_CACHE = Cache("node2smt")

    @contract(attributes="list")
    def __init__(self, attributes):

        self.attributes = attributes
        self._assert_preconditions = []
        self._assertions = None

        self._cmd = subprocess.Popen(["z3", "-in"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)

        # set stderr as non-blocking
        flags = fcntl.fcntl(self._cmd.stderr, fcntl.F_GETFL) # get current p.stdout flags
        fcntl.fcntl(self._cmd.stderr, fcntl.F_SETFL, flags | os.O_NONBLOCK)


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
    @contract(node="dict", returns="None|string")
    def node_cache_lookup(node): 

        node_key = node["code"].strip()
        res = SMTProb._NODE_TO_SMT_CACHE.lookup(node_key)
        return res

    @staticmethod
    @contract(node="dict", smt="string")
    def node_cache_store(node, smt):
        node_key = node["code"].strip()
        SMTProb._NODE_TO_SMT_CACHE.store(node_key, smt)
        
    
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

    @contract(node="dict", returns="string")
    def node_to_smt(self, node):
        assert "code" in node

        cache_found = SMTProb.node_cache_lookup(node)

        if cache_found:
            return cache_found

        node_type = node["nodeType"]

        smt = None

        if node_type == "ASTRE":
 
            assert "expression" in node, node
            assert "nodeType" in node["expression"]

            node_exp = node["expression"]
    
            node_exp_type = node["expression"]["nodeType"]
            #smt = None      

            # go by induction on the sub-node
            if node_exp_type == "ASTVariableDeclaration":
                assert "name" in node_exp
                assert "value" in node_exp["name"]
                assert "expr" in node_exp

                var = node_exp["name"]["value"] # TODO check that the LHS is always obtained in this way 
                rhs = node_exp["expr"]

                if rhs is not None:
                    smt = "(= %s_1 %s)" % (var, self.node_to_smt(rhs)) # TODO primed name

                    # force other primed vars to be equal to non-primed vars
                    others = []
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
#                        if curr.name != var:
#                            others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
                        for attr_var in curr.variables:
                            if attr_var != var:
                                others.append("(= %s_1 %s)" % (attr_var, attr_var)) # TODO primed name

                    if len(others) > 0:
                        smt = "(and %s %s)" % (smt, " ".join(others))
    
            elif node_exp_type == "ASTAssignment":
                assert "right" in node_exp
                assert "left" in node_exp
                assert "nodeType" in node_exp["left"]
                assert node_exp["left"]["nodeType"] == "ASTLiteral"

                var = node_exp["left"]["value"]
                rhs = node_exp["right"]
                smt = "(= %s_1 %s)" % (var, self.node_to_smt(rhs)) # TODO primed name

                # force other primed vars to be equal to non-primed vars
#                log.debug("Processing attributes: %s" % self.attributes)
                others = []
                for curr in self.attributes:
                    assert isinstance(curr, AbstractAttribute)

#                    if curr.name != var:
#                        others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
#                    log.debug("Var: %s -> Attribute: %s -> Variables: %s" % (var, curr, curr.variables))
                    for attr_var in curr.variables:
                        if attr_var != var:
                            others.append("(= %s_1 %s)" % (attr_var, attr_var)) # TODO primed name
                if len(others) > 0:
                    smt = "(and %s %s)" % (smt, " ".join(others))

            elif node_exp_type == "ASTPostOp":
                assert "code" in node_exp

                # var is all but the last 2 chars
                var = node_exp["code"][:-2] 
                # op is given by the last 2 chars
                op = node_exp["code"][-2:]
    
                if op in [ "++", "--" ]:
                    if op == "++":
                        smt = "(= %s_1 (+ %s 1))" % (var, var) # TODO primed name
                    else:
                        # op == "--":
                        smt = "(= %s_1 (- %s 1))" % (var, var) # TODO primed name

                    # force other primed vars to be equal to non-primed vars
                    others = []
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
#                        if curr.name != var:
#                            others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
                        for attr_var in curr.variables:
                            if attr_var != var:
                                others.append("(= %s_1 %s)" % (attr_var, attr_var)) # TODO primed name

                    if len(others) > 0:
                        smt = "(and %s %s)" % (smt, " ".join(others))
    
                else:
                    log.warning("Interpret PL post-op expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp))
                    smt = node_exp["code"]
            else:
                log.warning("Interpret PL expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp))
                smt = node_exp["code"]
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

            smt = "(%s %s %s)" % (op, self.node_to_smt(left), self.node_to_smt(right))
        elif node_type == "ASTLiteral":
            assert "value" in node
            smt = node["value"]
        else:
            log.warning("Interpret PL code (%s) as SMT code: %s ..." % (node["code"], node))
            smt = node["code"]

        SMTProb.node_cache_store(node, smt)

        return smt

    @contract(pred_list="list(is_attribute_predicate)", returns="list(is_attribute_predicate)")
    def primed(self, pred_list):
        assert isinstance(pred_list, list)
 
        res = list()
      
        for pred in pred_list:
            check("is_attribute_predicate", pred)
        
            pred_primed = pred.primed()

            res.append(pred_primed)

        return res

    def add_precondition(self, expression):
        assert isinstance(self._assert_preconditions, list)
        assert isinstance(expression, dict)

        self._assert_precoditions.append(self.node_to_smt(expression))


    def assertions(self, source_pred, instr=None, target_pred=None, preconditions=None):
    
#        assert isinstance(self.source_pred, list)
#        assert isinstance(self.target_pred, list)
#        assert self.instr is None or self.instr == "goto" or isinstance(self.instr, dict)

        assertions = []
        
        assertions.extend(source_pred)
        assertions.extend(self._assert_preconditions)
            
        if preconditions:
            for pre in preconditions:
                assert isinstance(pre, Precondition)

                smt_pre = None
                if isinstance(pre, Negate):
                    pre_node = self.node_to_smt(pre.node)
                    smt_pre = "(assert (not %s))" % pre_node
                else:
                    pre_node = self.node_to_smt(pre.node)
                    smt_pre = "(assert %s)" % pre_node

                log.debug("Pre: %s => %s" % (pre, smt_pre))
                assertions.append(smt_pre)

        # SMT assertions about the target predicates require that:
        # - if instruction does something, target predicates use primed vars
        # - if instroction does not do anything, target predicates use same vars as soure predicates   
#        target_pred = self.target_pred

        if instr is not None:
            smt_instr = self.node_to_smt(instr)

            if smt_instr is not None:
                instr_assert = "(assert %s)" % smt_instr
                assertions.append(instr_assert)

            if target_pred is not None:
                target_pred_primed = self.primed(target_pred)

                assertions.extend(target_pred_primed)

        return assertions
 

    @contract(primed=bool, returns="list")
    def get_smt_attribute_declaration(self, primed=True):
        """
        Get the SMT declaration of each attribute. If primed==True, add also
        a declaration for the primed version of the attribute itself.
        """
        smt_code = []
        declared_types = set([])

#        log.debug("Attributes: %s" % self.attributes)
    
        for attr in self.attributes:
            assert isinstance(attr, AbstractAttribute), attr
            #assert isinstance(attr.domain, Domain)
 
            # declare the attribute data-type (if needed)
            #dt_decl = attr.domain.smt_declaration
            dt_decl = attr.smt_declaration
            type_name = attr.smt_type_name
            if dt_decl and type_name not in declared_types:
                smt_code.append(dt_decl) #was: attr.domain.dt_decl)
                declared_types.add(type_name)
    
            # declare one constant for the (straight) attribute
#            attr_declaration = "(declare-const %s %s)" % (attr.name, type_name) #attr.domain.name)
            attr_declaration = ""
            for (var, dt) in zip(attr.variables, attr.datatypes):
                attr_declaration = "(declare-const %s %s)\n%s" % (var, dt.name, attr_declaration)

            smt_code.append(attr_declaration) 
 
#            log.debug("Check primed flag: %s" % primed) 
            if primed:       
                # declare one constant for the primed attribute
                primed_attr = attr.primed()
#                log.debug("primed attribute: %s -> %s" % (primed_attr, primed_attr.variables))
                primed_attr_declaration = ""
                for (primed_var, primed_dt) in zip(primed_attr.variables, primed_attr.datatypes):
                    primed_attr_declaration = "(declare-const %s %s)\n%s" % (primed_var, primed_dt, primed_attr_declaration) # NB type of attr and primed_attr is the same #primed_attr.domain.name)
                smt_code.append(primed_attr_declaration)
 
        return smt_code


    @contract(source_pred="list(is_attribute_predicate)", instr="None|dict", target_pred="None|list(is_attribute_predicate)", returns="string")
    def to_smt_problem(self, source_pred, instr=None, target_pred=None, preconditions=None):
        """
        This method returns a syntattically valid SMT problem corresponding to the
        collected assertions and instruction.

        TODO atm, this only covers Z3 syntax
        """
        assert (instr is None and target_pred is None) or (instr is not None and target_pred is not None)
        smt_code = []
        
        # add declaration of attributes and their types
        do_primed = (target_pred is not None)
        smt_code.extend(self.get_smt_attribute_declaration(primed=do_primed))     

        # add problem assertions derived from source state, instruction, target state
        for curr in self.assertions(source_pred, instr=instr, target_pred=target_pred, preconditions=preconditions):
            if isinstance(curr, AttributePredicate):
                smt_code.append(curr.smt_assert())
            elif isinstance(curr, basestring):
                smt_code.append(curr)
            else:
                raise ValueError("Don't know how to handle assertion (%s): %s" % (type(curr), curr))

        return "\n".join(smt_code)

    
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

        head = "SMT<<"
        if not is_input:
            head = "SMT>>"

        if isinstance(commands, basestring):
            commands = commands.split("\n")

        for line in commands:
            if line:
                log.debug("%s %s" % (head,line))


    def _check_error(self, line, default=None):
        
        if line.startswith("(error "):
            raise ValueError(line)
        elif default:
            raise ValueError(default)
    
    @contract(commands="string", returns="string")
    def get_tool_answer(self, commands):
        """
        Receives a string containing the commands to be executed by the tool.
        Returns the text output of the tool.
        """ 
        self._log_smt(commands)

        self._cmd.stdin.write(commands)

        answer = self._get_output()
        err_msg = self._get_error()
        if err_msg:
            sys.stderr.write(err_msg)

        return answer

    @contract(source_conf="tuple", guard=Precondition, preconditions="None|list(is_precondition)", returns="bool")
    def check_sat_guard(self, source_conf, guard, preconditions=None):
 
        log.debug("Received source pred (%s): %s" % (type(source_conf), source_conf)) 

        check("list(is_abstract_attribute)", self.attributes)       

#        log.debug("In check_sat_guard ...")  
        source_pred = conf_to_attribute_predicate(source_conf, self.attributes)

#        log.debug("Source pred: %s" % source_pred)
        new_preconditions = [ guard ]
        if preconditions:
            new_preconditions.extend(preconditions)
        commands = self.to_smt_problem(source_pred, preconditions=new_preconditions) + "\n" + "(check-sat)\n"


        answer = self.get_tool_answer(commands)
        smt_res = False
        if answer.strip() == "sat":
            smt_res = True
        elif answer.strip() == "unsat":
            smt_res = False
        else:
            self._check_error(answer, default="Unknown value")

        return smt_res


    @contract(source_conf="tuple", instr="dict", target_conf="tuple", preconditions="None|list(is_precondition)", returns="bool")
    def check_sat_instr(self, source_conf, instr, target_conf, preconditions=None):
#        assert isinstance(source_conf, tuple) # it is a tuple of Predicate's
#        assert isinstance(target_conf, tuple) # it is a tuple of Predicate's
#        assert isinstance(self.attributes, list)   
#        assert preconditions is None or isinstance(preconditions, list)

        check("list(is_abstract_attribute)", self.attributes)

        source_pred = conf_to_attribute_predicate(source_conf, self.attributes)
        target_pred = conf_to_attribute_predicate(target_conf, self.attributes)

        check("list(is_attribute_predicate)", source_pred)
        check("list(is_attribute_predicate)", target_pred)

        commands = self.to_smt_problem(source_pred, instr, target_pred, preconditions=preconditions) + "\n" + "(check-sat)\n"

        answer = self.get_tool_answer(commands)
        smt_res = False
        if answer.strip() == "sat":
            smt_res = True
        elif answer.strip() == "unsat":
            smt_res = False
        else:
            self._check_error(answer, default="Unknown value")

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

@contract(pc_break_stack="list[N](is_pc),N>0", break_target="string|None", returns="is_pc")
def find_break_target(pc_break_stack, break_target):

    pc_res = pc_break_stack[-1]

    if break_target:
        pc_res = None
        pos = len(pc_break_stack)-1
        while not pc_res and pos >= 0:
            (identifier, pc_curr) = pc_break_stack[pos]
            if identifier == break_target:
                pc_res = pc_curr

        if not pc_res:
            raise ValueError("Cannot break to unknown label '%s'" % break_target)

    return pc_res

@contract(source="is_configuration", pc_source=PC, instr="dict", state_space=StateSpace, returns=ReachabilityResult)
def check_reach(source, pc_source, instr, state_space, preconditions=None, postconditions=None, pc_break_stack=None):
    assert preconditions is None or isinstance(preconditions, list)
    assert pc_break_stack is None or isinstance(pc_break_stack, list)

    if pc_break_stack is None:
        pc_break_stack = []

    if preconditions is None:
        preconditions = []

    if postconditions is None:
        postconditions = []

    # produce a label out of a code block
    rows = instr["code"].split("\n")
    stripped_rows = map(lambda r: r.strip(), rows)
    instr_text = " ".join(stripped_rows)

    instr_label = instr_text
    len_label = len(instr_label)
    if len_label > 50:
        instr_label = instr_label[:15].strip() + "..." + instr_label[len_label-15:].strip()

    instr_type = instr["nodeType"]
    source_pred = state_space.value(source)
    source_loc = build_loc(source, pc_source)

    # begin 
    # res contains a list of reachable configurations, a list of Edge's, and a list of final Location's
    reachable = []
    edges = []
    final = []
    external = []
    
    if instr_type == "ASTRE":

        log.info("Check ASTRE: '%s' %s ..." % (instr_text, pc_source))

        for target in state_space.enumerate:
            assert isinstance(target, tuple)

            pc_target = pc_source + 1
            log.debug("check: (%s,%s) -[%s]?-> (%s,%s)" % (source,pc_source,instr_label,target,pc_target))


            target_pred = state_space.value(target)
            with SMTProb(state_space.attributes) as smt_prob:
                cache_found = SMTProb.smt_cache_lookup(source, instr, target)

                is_sat = False
                if cache_found is not None:
                    is_sat = cache_found
                else:
                    smt_prob.push()
                    is_sat = smt_prob.check_sat_instr(source_pred, instr, target_pred, preconditions=preconditions)
                    smt_prob.pop()
                    SMTProb.smt_cache_store(source, instr, target, is_sat)
        
                if is_sat:
                    target_loc = build_loc(target, pc_target)
                    edge_label = instr["code"]
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
        pc_source_then = PC(pc_source.pc).push(0).push(0)
        pc_source_else = PC(pc_source.pc).push(1).push(0)

        stms_then = instr["ifBranch"]["stms"]
        stms_else = None

        if instr["elseBranch"]: 
            assert "stms" in instr["elseBranch"]
            stms_else = instr["elseBranch"]["stms"]

        final_then = final_else = []
        pc_target = pc_source + 1

        with SMTProb(state_space.attributes) as smt_prob:

            assert "expression" in guard

            smt_prob.push()

            is_then_reachable = smt_prob.check_sat_guard(source_pred, guard=Precondition(guard["expression"]))
            smt_prob.pop()

            smt_prob.push()
            is_else_reachable = smt_prob.check_sat_guard(source_pred, guard=Negate(guard["expression"]))
            smt_prob.pop()

    
        if (is_then_reachable):
#            curr_pc = PC(pc_source_then.pc)
            reachable_then = [ source ]

            preconditions.append(Precondition(guard["expression"]))
#            (reachable_then, edges_then, final_then) = compute_reachable(reachable_then, curr_pc, stms_then, state_space, preconditions=preconditions, pc_break_stack=pc_break_stack)
            rr_then = compute_reachable(reachable_then, pc_source_then, stms_then, state_space, preconditions=preconditions, pc_break_stack=pc_break_stack)
    
            final_then = rr_then.final_locations
            edges.extend(rr_then.edges) #edges_then)

            # add ed edge from source to the begin of the "then" branch
            then_loc = build_loc(source, pc_source_then)
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
#            (reachable_else, edges_else, final_else) = compute_reachable(reachable_else, curr_pc, stms_else, state_space, preconditions=preconditions, pc_break_stack=pc_break_stack)
            rr_else = compute_reachable(reachable_else, pc_source_else, stms_else, state_space, preconditions=preconditions, pc_break_stack=pc_break_stack)

            final_else = rr.final_locations
       
            edges.extend(rr_else.edges) #edges_else)
            external.extend(rr_else.external_locations)
    
            # add en edge from source to the begin of the "else" branch
            else_loc = build_loc(source, pc_source_else)
            edge_else = Edge(source_loc, else_loc, "else (not(%s))" % guard["code"])
            # add an edge to the results
            edges.append(edge_else)
        elif not stms_else and is_else_reachable:
            final_else = [ source_loc ] 


        # check external locations contain locations reached via a break
        for loc in external:
            assert isinstance(loc, Location)
            pred, pc = loc.name.split("@")
            conf = parse_configuration(pred)

            if pc == pc_target.pc:
                # at least one external location goes to the pc_target
                # position, thus the negation of while is reachable
                final.append(loc)
                reachable.append(conf)
 

        # add an edge from each one of the reachable state to the same
        # state with a "popped" pc

        for loc in final_then + final_else:
            assert isinstance(loc, Location)
            (state_pred_label, pc) = loc.name.split("@") # TODO this relies on how PC builds labels
            state_pred = parse_configuration(state_pred_label)

            # the end-if location has the same predicate of the reached final state, but the pc of the line after the if-statement
            end_if_loc = build_loc(state_pred, pc_target)            

            # add edges and final locations to the results
            edges.append(Edge(loc, end_if_loc, "endif"))
            final.append(end_if_loc)
            reachable.append(state_pred)
 
    elif instr_type == "ASTWhile":

#        log.debug("ASTWhile: %s ..." % instr)

        assert "expr" in instr, instr.keys()
        assert "stms" in instr
        assert "identifier" in instr

#        log.info("Check ASTWhile: '%s' %s ..." % (instr_text, pc_source))

        while_identifier = instr["identifier"]
        stms_while = instr["stms"]
        guard = instr["expr"]
        assert "code" in guard
        assert "expression" in guard
        pc_target = pc_source + 1

        with SMTProb(state_space.attributes) as smt_prob:

            smt_prob.push()
            is_while_reachable = smt_prob.check_sat_guard(source_pred, guard=Precondition(guard["expression"]))
            smt_prob.pop()
    
            smt_prob.push()
            is_not_while_reachable = smt_prob.check_sat_guard(source_pred, guard=Negate(guard["expression"]))
            smt_prob.pop()

        if is_while_reachable:
    
            pc_source_while = PC(pc_source.pc).push(0)
            reachable_while = [ source ]
            
            preconditions.append(Precondition(guard["expression"]))

            pc_break_stack.append((while_identifier, pc_target))
            rr_while = compute_reachable(reachable_while, pc_source_while, stms_while, state_space, preconditions=preconditions, pc_break_stack=pc_break_stack)
            pc_break_stack.pop()

#            log.debug("While reachable result: %s" % rr_while)

            edges.extend(rr_while.edges)
            external.extend(rr_while.external_locations)
    
            # add edge from source to the begin of the while
            # TODO at the moment we don't take advantage of the while-guard as precondition
            while_loc = build_loc(source, pc_source_while)
            while_edge = Edge(source_loc, while_loc, "while (%s)" % guard["code"])
            edges.append(while_edge)

            # if entered the while loop, for each final location of while, create an edge out of the while loop and an edge at the beginning of the while itself
            for loc in rr_while.final_locations: #final_while:
                assert isinstance(loc, Location)
                (pred_label, pc_label) = loc.name.split("@") # TODO we rely on the way we represent location names
                pred = parse_configuration(pred_label)
                loc_out = build_loc(pred, pc_target)
                edge_out = Edge(loc, loc_out, "end while")
                edges.append(edge_out)
                final.append(loc_out)
                reachable.append(pred)
    
                loc_restart = build_loc(pred, pc_source)
                edge_restart = Edge(loc, loc_restart)
                edges.append(edge_restart)
    
            # check whether it has been reached via some break
#            log.debug("External in while: %s" % external)
            for loc in external:
                assert isinstance(loc, Location)
                pred, pc = loc.name.split("@")
                conf = parse_configuration(pred)

                if pc == pc_target.pc:
                    # at least one external location goes to the pc_target
                    # position, thus the negation of while is reachable
                    final.append(loc)
                    reachable.append(conf)
        
        if is_not_while_reachable:

            # add edge from source to the end of the while
            while_neg_loc = build_loc(source, pc_target)
            while_neg_edge = Edge(source_loc, while_neg_loc, "not (%s)" % guard["code"])
            edges.append(while_neg_edge)
            final.append(while_neg_loc)
            reachable.append(source)


    elif instr_type == "ASTBreak":

        assert "target" in instr

#        log.debug("ASTBreak: %s ..." % instr)
        # do nothing
#        log.info("Check ASTBreak: '%s' %s ..." % (instr_text, pc_source))
#        pc_target = pc_break_stack[-1] # get the item pushed last TODO handle the label to go with the break  #pc_break_stack.pop()

        break_identifier = instr["target"]
        pc_target = find_break_target(pc_break_stack, break_identifier)

        assert isinstance(pc_target, PC)

        # new reached configuration: the current configuration, 
#        reachable.append(source)
        # final location: source configuration with PC successive to the current block
        loc_out = build_loc(source, pc_target)
        #final.append(loc_out)
        # new edge: from source location to final location
        edge_break = Edge(source_loc, loc_out, "break")
        edges.append(edge_break)  
#        reachable.append(source)
#        final.append(source)
        external.append(loc_out)

        # no final locations
        # (ASTBreak breaks the compositionality approach)


#        log.debug("%s%s =[break]=> %s%s" % (source, pc_source, reachable, pc_target))



    elif instr_type == "ASTReturn":

#        log.info("Check ASTReturn: '%s' %s ..." % (instr_text, pc_source))
    
        # do nothing
        pass
    else:
        #raise ValueError("Instruction type not covered: %s" % instr)
        log.warning("Instruction ignored: %s" % instr)

#    assert  isinstance(res,tuple) and len(res) == 3
#    return res

    assert (len(reachable) >= 0 and len(edges) >= 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0), "# reachable: %s, # edges: %s, # final: %s. Instruction: %s" % (len(reachable), len(edges), len(final), instr)
#    return (reachable, edges, final)
    return ReachabilityResult(configurations=reachable, final_locations=final, external_locations=external, edges=edges)

@contract(conf="tuple", attributes="list(is_abstract_attribute)", returns="list(is_attribute_predicate)")
def conf_to_attribute_predicate(conf, attributes):
    res = []
    for (attr, pred) in zip(attributes, conf):
        check("is_abstract_attribute", attr)
        check("is_predicate", pred)
        res.append(AttributePredicate(attr, pred))

    return res


@contract(pred_label="is_configuration_repr", returns="is_configuration")
def parse_configuration(pred_label):

    values = map(int, pred_label.strip("()").split(","))

    return tuple(values)

@contract(loc_list="list", new_loc_list="list", returns=bool)
def while_fixpoint(loc_list, new_loc_list):
    """
    Return true iff the new_loc_list is entireley contained in loc_list
    """
#    assert isinstance(loc_list, list)
#    assert isinstance(new_loc_list, list)

    res = False

    if len(loc_list) >= len(new_loc_list):
        res = True
        get_loc_name = lambda l: l.name
        name_list = map(get_loc_name, loc_list)

        for new_loc in new_loc_list:
            assert isinstance(new_loc, Location)
            if new_loc.name not in loc_list:
                res = False
                break

    return res
def get_class_attributes(klass):
    assert isinstance(klass, Klass)
    assert "attributes" in klass.ast

    return klass.ast["attributes"]

@contract(method=Method, returns="list(string)")
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


@contract(method=Method, domains="list(tuple(list(string)|string,is_domain,bool))")
def get_state_space_from_method(method, domains):

    # 1. get method attributes
    attributes = get_method_attributes(method)
##    
##    attributes_dict = {}
##    for (attr, is_local) in attributes:
##        attributes_dict[attr] = is_local
##    
#    print "all attributes: %s" % attributes
#    print "domains: %s" % domains
    # 2. initialize empty state-space
    ss = StateSpace()
##
##    # 3. find attributes with an associated abstract domain
##    for (attr,is_local) in attributes:
##        assert isinstance(attr, basestring)
##        assert isinstance(is_local, bool)
##    
##        if attr in domains:
##            dom = domains[attr]
##
##            assert isinstance(dom, Domain)
##
##            abs_att = AbstractAttribute(attr, dom, is_local)# dom.values, dom.default)
##            ss.add_attribute(abs_att)

    for (var_names, domain, is_local) in domains: #.iteritems():
        #assert isinstance(var_names, list) or isinstance(var_names, basestring), var_names
        #assert isinstance(domain, Domain)
        #assert isinstance(is_local, bool)
        check('list(string)|string', var_names)
        check('is_domain', domain)
        check('bool', is_local)
        
#        assert isinstance(dom_spec, tuple)
#        assert len(dom_spec) == 2
#        assert isinstance(dom_spec[0], list)
#        assert isinstance(dom_spec[1], Domain)

        abs_att = AbstractAttribute(var_names, domain, is_local)
        ss.add_attribute(abs_att)

    return ss


@contract(class_fqn="string", class_path="string", method_name="string", project=Project, returns=Method)
def get_method(project, class_fqn, class_path, method_name):
    # check this works (case 1: no dot, case 2: one or more dots)
    class_name = ""
    package_name = ""

    fqn_parts = class_fqn.rsplit(".", 1)

    if len(fqn_parts) == 1:
        class_name = fqn_parts[0]
    else:
        package_name = fqn_parts[0]
        class_name = fqn_parts[1]

    klass = Klass(class_name, package_name, "file://%s" % class_path, project)
    m = Method(method_name, klass)

    return m

@contract(method=Method, state_space=StateSpace, returns=TA)
def translate_method_to_fsa(method, state_space):

    instructions = method.ast["stms"]

    fsa = transform(method.name, instructions, state_space)

    return fsa
