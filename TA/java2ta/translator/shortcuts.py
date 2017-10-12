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
from java2ta.ta.models import TA, Location, Edge
from java2ta.abstraction.models import StateSpace, AbstractAttribute, Domain, Predicate, SymbolTable
from java2ta.abstraction.shortcuts import DataTypeFactory

import sys
import logging
from contracts import contract, new_contract, check

log = logging.getLogger("main")

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

#@contract(pred="is_configuration|is_configuration_repr", pc=PC, returns=Location)
#@contract(pred="list(is_predicate)", pc="is_pc", returns="is_location")
@contract(conf="is_configuration", pc="is_pc", state_space="is_state_space", returns="is_location")
def build_loc(conf, pc, state_space):

    
    #if not isinstance(pred, Predicate):
    #    pred = state_space.value(pred)

    #check("is_predicate", pred)

#    conf = state_space.configuration(pred)    

    loc_name = "%s%s" % (conf, pc) #(pred, pc)

#    loc_name = "%s%s" % (",".join(map(lambda p: str(p),pred)), pc)
    loc = Location(loc_name)

    return loc

@contract(source_conf="list(is_configuration)", pc_source=PC, instr="list(dict)", state_space="is_state_space", project="is_project", preconditions="list(is_precondition)|None", returns=ReachabilityResult)
def compute_reachable(source_conf, pc_source, instr, state_space, project, preconditions=None, pc_jump_stack=None, deadlines=None):
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

    if deadlines is None:
        deadlines = []

    curr_pc = PC(pc_source.pc)
    for curr_instr in instr:

        # reachable and final must contain only information that are reached by
        # the last instruction (NB on the contrary, edges contain all the edges 
        # added by all the instructions)

        reachable = set([])
        final = []

        # use sets in order to avoid duplicates
        source_conf = set(source_conf)

        if not source_conf:
            log.warning("Exit for non reachable states at PC: %s" % curr_pc)
            break

        for source in source_conf:
            # TODO in principle each invocation of check_reach(...) is independent from the others
            rr = check_reach(source, curr_pc, curr_instr, state_space, project, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
    
            edges.extend(rr.edges)
            reachable = reachable | set(rr.configurations) # use sets and set unions to avoid duplicates
            final.extend(rr.final_locations) 
            external.extend(rr.external_locations)

        
        log.debug("Check reachable at %s: %s from %s" % (curr_pc, reachable, source_conf))
        if len(reachable) == 0:
            # in this case the current instruction interrupts the flow of instructions; thus, we
            # must ignore the following instructions
            break

        # next iteration starts from reached configurations, and advance PC by 1
        log.debug("Compute reachable next iteration: (%s)%s -> (%s)%s" % (source_conf, curr_pc, reachable, curr_pc + 1))
        source_conf = reachable
        curr_pc = curr_pc + 1

#    assert (len(reachable) >= 0 and len(edges) >= 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0)

    return ReachabilityResult(configurations=reachable, final_locations=final, external_locations=external, edges=edges)


@contract(name="string", instructions="list(dict)", state_space="is_state_space", project="is_project", returns=TA)
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

    rr = compute_reachable(source_conf, pc_source, instructions, state_space, project)

    log.info("reachable: %s, final: %s, external: %s" % (rr.configurations, rr.final_locations, rr.external_locations))

    if len(rr.edges) > 0:
        for e in rr.edges:
            assert isinstance(e, Edge), e

            ta.get_or_add_edge(e)

    return ta



class SMTProb(object):

    OP_DECODE = { "plus": "+", "minus": "-", "greater": ">", "less": "<", "mul": "*", "equality": "=" }

    _SMT_CACHE = Cache("smt")
    _NODE_TO_SMT_CACHE = Cache("node2smt")

    @contract(attributes="list", project="is_project")
    def __init__(self, attributes, project):

        self.attributes = attributes
        self._assert_preconditions = []
        self._assertions = None
        self._project = project

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
    @contract(node="dict", returns="None|*")
    def node_cache_lookup(node): 

        node_key = node["code"].strip()
        res = SMTProb._NODE_TO_SMT_CACHE.lookup(node_key)
        return res

    @staticmethod
    @contract(node="dict", obj="*")
    def node_cache_store(node, obj):
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

    @contract(node="dict", returns="tuple(list(string),string)")
    def node_to_smt(self, node):
        """
        This method returns a pair of strings: the first contains auxiliary declarations (e.g. constants,
        datatypes, ...) and the second contains the BODY of the actual assertion.

        TODO perhaps we can simplify this to just return a list(string); at the moment I don't see a reason
        of "separating" the final assertion (second element of the tuple) from the previous declarations (first
        element of the tuple)
        """
        assert "code" in node

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
            #smt = None      

            log.debug("Found ASTRE node (%s)" % node_exp_type)

            # go by induction on the sub-node
            if node_exp_type == "ASTVariableDeclaration":
                assert "name" in node_exp
                assert "value" in node_exp["name"]
                assert "expr" in node_exp

                var = node_exp["name"]["value"] # TODO check that the LHS is always obtained in this way 
                rhs = node_exp["expr"]

                if rhs is not None:
                    rhs_smt_declarations, rhs_smt_assertion = self.node_to_smt(rhs)
                    smt_declarations.extend(rhs_smt_declarations)
                    smt_assertion = "(= %s_1 %s)" % (var, rhs_smt_assertion) # TODO primed name

                    # force other primed vars to be equal to non-primed vars
                    #others = []
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
#                        if curr.name != var:
#                            others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
                        for attr_var in curr.variables:
                            if attr_var != var:
                                #others.append("(= %s_1 %s)" % (attr_var, attr_var)) # TODO primed name
                                smt_declarations.append("(assert (= %s_1 %s))" % (attr_var, attr_var)) # TODO primed names

#                    if len(others) > 0:
#                        smt_assertion = "(and %s %s)" % (smt_assertion, " ".join(others))
    
            elif node_exp_type == "ASTAssignment":
                assert "right" in node_exp
                assert "left" in node_exp
                assert "nodeType" in node_exp["left"]
                assert node_exp["left"]["nodeType"] == "ASTLiteral"

                var = node_exp["left"]["value"]
                rhs = node_exp["right"]
    
                rhs_smt_declarations,rhs_smt_assertion = self.node_to_smt(rhs)
                smt_declarations.extend(rhs_smt_declarations)
                smt_assertion = "(= %s_1 %s)" % (var, rhs_smt_assertion) # TODO primed name

                # force other primed vars to be equal to non-primed vars
#                log.debug("Processing attributes: %s" % self.attributes)
                #others = []
                for curr in self.attributes:
                    assert isinstance(curr, AbstractAttribute)

#                    if curr.name != var:
#                        others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
#                    log.debug("Var: %s -> Attribute: %s -> Variables: %s" % (var, curr, curr.variables))
                    for attr_var in curr.variables:
                        if attr_var != var:
                            #others.append("(= %s_1 %s)" % (attr_var, attr_var)) # TODO primed name
                            smt_declarations.append("(assert (= %s_1 %s))" % (attr_var, attr_var)) # TODO primed names
#                if len(others) > 0:
#                    smt_assertion = "(and %s %s)" % (smt_assertion, " ".join(others))

            elif node_exp_type == "ASTPostOp":
                assert "code" in node_exp

                # var is all but the last 2 chars
                var = node_exp["code"][:-2] 
                # op is given by the last 2 chars
                op = node_exp["code"][-2:]
    
                if op in [ "++", "--" ]:
                    if op == "++":
                        smt_assertion = "(= %s_1 (+ %s 1))" % (var, var) # TODO primed name
                    else:
                        # op == "--":
                        smt_assertion = "(= %s_1 (- %s 1))" % (var, var) # TODO primed name

                    # force other primed vars to be equal to non-primed vars
                    #others = []
                    for curr in self.attributes:
                        assert isinstance(curr, AbstractAttribute)
#                        if curr.name != var:
#                            others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
                        for attr_var in curr.variables:
                            if attr_var != var:
                                #others.append("(= %s_1 %s)" % (attr_var, attr_var)) # TODO primed name
                                smt_declarations.append("(assert (= %s_1 %s))" % (attr_var, attr_var)) # TODO primed names

                    if len(others) > 0:
                        smt_assertion = "(and %s %s)" % (smt, " ".join(others))
    

                else:
                    log.warning("Interpret PL post-op expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp))
                    smt_assertion = node_exp["code"]
            elif node_exp_type == "ASTMethodCall":
                smt_declarations, smt_assertion = self.node_to_smt(node_exp)

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

            lhs_smt_declarations, lhs_smt_assertion = self.node_to_smt(left)
            rhs_smt_declarations, rhs_smt_assertion = self.node_to_smt(right)

            smt_declarations.extend(lhs_smt_declarations)
            smt_declarations.extend(rhs_smt_declarations)
            smt_assertion = "(%s %s %s)" % (op, lhs_smt_assertion, rhs_smt_assertion)
        elif node_type == "ASTLiteral":
            # at the moment an ASTLiberal can be an actual literal
            # or an identifier; I hope this will be fixed in the
            # future, by the IR
            assert "value" in node
            log.debug("Dump literal: %s" % node)
            lit_value = literal_to_smt(node["value"])
            smt_assertion = lit_value
        elif node_type == "ASTMethodCall":
            log.debug("Node: %s" % node)
#            log.debug("Method call attributes: %s" % node.keys())
#            log.debug("Callee: %s" % node["exprCallee"])
            check("is_project", self._project)
            method_name = node["methodName"] 
            class_name = node["classPointed"] or "example.TestNeighbors" # HACK # TODO I guess this is a heuristic and the actual class of the object can only be determined at run-time, thus the user should be able to specify this information with some input
 
            par_ctx = {}   
            for (par_id,par) in enumerate(node["parameters"]):
                (par_smt_declarations, par_smt_assert) = self.node_to_smt(par)
                smt_declarations.extend(par_smt_declarations)
                #smt_declarations.append(par_smt_assert)
                par_name = "par_%s" % par_id
                par_ctx[par_name] = par_smt_assert

            # TODO check: if is a method we handle directly, do it; otherwise, look for its
            # IR by inquiring the service           
            log.debug("Check knowledge base: (%s,%s)" % (class_name, method_name))
            has_direct_method = KnowledgeBase.has_method(class_name, method_name)

            # resolve lhs of method call (i.e. the callee)
            lhs_smt_declarations,lhs_smt_assertion = self.node_to_smt(node["exprCallee"])
            smt_declarations.extend(lhs_smt_declarations)
 
            tmp_var_name = FreshNames.get_name(prefix=method_name)
      
            method_smt_declarations = []
            if has_direct_method:
                (method_smt_declarations, method_smt_assertion, smt_dt) = KnowledgeBase.get_method(class_name,method_name,tmp_var_name,par_ctx,lhs_smt_assertion)
            else:
                class_path = "%s.java" % class_name # TODO this in general could be different
    #            class_fqn = "" # TODO this could be different
                method = self._project.get_method(class_name, class_path, method_name)
                method_ast = method.get_ast()
                assert "returnType" in method_ast
    
                method_type = method_ast["returnType"]
    
                dtfactory = DataTypeFactory.the_factory()
                smt_dt = dtfactory.from_fqn(method_type)
    
                log.debug("Invoke method %s:%s (class name: %s, class path: %s) => SMT type: %s" % (method_name, method_type, class_name, class_path, smt_dt))
                log.debug(method_ast.keys())

            # add frame condition: all variables are left untouched (NB this is true only for read-only methods; in general methods could change the status of some of the state variables)

            check("list(is_abstract_attribute)", self.attributes)
            for curr in self.attributes:
                check("is_abstract_attribute", curr)
#                assert isinstance(curr, AbstractAttribute)

#                    if curr.name != var:
#                        others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
#                    log.debug("Var: %s -> Attribute: %s -> Variables: %s" % (var, curr, curr.variables))
                for attr_var in curr.variables:
                    smt_declarations.append("(assert (= %s_1 %s))" % (attr_var, attr_var)) # TODO primed name
       

            # add the auxiliary declarations
            smt_declarations.append( "(declare-const %s %s)" % (tmp_var_name, smt_dt))
            smt_declarations.extend(method_smt_declarations)

            # store the returned value for this case
            smt_assertion = tmp_var_name
 
        else:
            log.warning("Interpret PL code (%s) as SMT code: %s ..." % (node["code"], node))
            smt_assertion = node["code"]

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

    @contract(expression="dict")
    def add_precondition(self, expression):
#        assert isinstance(self._assert_preconditions, list)
#        assert isinstance(expression, dict)

        check("list(string,string)", self._assert_preconditions)

        self._assert_precoditions.extend(self.node_to_smt(expression))


    @contract(pre="is_precondition", returns="tuple(list(string),string)")
    def precondition_to_smt(self, pre):
        """
        Returns the same output of node_to_smt(...) for the passed Precondition.

        TODO perhaps we can simplify this to just return a list(string); at the moment I don't see a reason
        of "separating" the final assertion (second element of the tuple) from the previous declarations (first
        element of the tuple). Decide together with the node_to_smt method
        """
    
        smt_declarations = [] #None
        smt_assertion = "" #None
        if isinstance(pre, Negate):
            pre_smt_declarations, pre_smt_assertion = self.node_to_smt(pre.node)
            smt_assertion = "(assert (not %s))" % pre_smt_assertion
        else:
            pre_smt_declarations, pre_smt_assertion = self.node_to_smt(pre.node)
            smt_assertion = "(assert %s)" % pre_smt_assertion

        assert(len(smt_assertion)>0)

        smt_declarations.extend(pre_smt_declarations)

        log.debug("Pre: %s => (%s,%s)" % (pre, smt_declarations, smt_assertion))
        return smt_declarations, smt_assertion
 

    @contract(source_pred="list(is_attribute_predicate)", instr="dict|None", target_pred="list(is_attribute_predicate)|None", preconditions="None|list(is_precondition)")
    def assertions(self, source_pred, instr=None, target_pred=None, preconditions=None):
    
        assertions = []
        
        assertions.extend(source_pred)

        check("list(string,string)", self._assert_preconditions)
#        assertions.extend(self._assert_preconditions)
        for pre_smt_declaration, pre_smt_assertion in self._assert_preconditions:
            assertions.append(pre_smt_declaration)
            assertions.append(pre_smt_assertion)
  
        if preconditions:
       
#            log.debug("Before preconditions: %s" % assertions)
   
            for pre in preconditions:
#                assert isinstance(pre, Precondition)

##                smt_pre = None
##                if isinstance(pre, Negate):
##                    pre_node = self.node_to_smt(pre.node)
##                    smt_pre = "(assert (not %s))" % pre_node
##                else:
##                    pre_node = self.node_to_smt(pre.node)
##                    smt_pre = "(assert %s)" % pre_node
##
##                log.debug("Pre: %s => %s" % (pre, smt_pre))
    
                smt_declarations,smt_pre = self.precondition_to_smt(pre)
                assertions.extend(smt_declarations)
                assertions.append(smt_pre)
        
#            log.debug("After preconditions: %s" % assertions)

        # SMT assertions about the target predicates require that:
        # - if instruction does something, target predicates use primed vars
        # - if instroction does not do anything, target predicates use same vars as soure predicates   
#        target_pred = self.target_pred

        if instr is not None:
            smt_instr_declarations, smt_instr_assertion = self.node_to_smt(instr)

            if smt_instr_declarations:
                assertions.extend(smt_instr_declarations)

            if smt_instr_assertion: # is not None: # also discard the case of empty strings (should never happen, though)
                instr_assert = "(assert %s)" % smt_instr_assertion
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
        #do_primed = (target_pred is not None)
        do_primed = True
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
#    @contract(source_conf="is_configuration", guard=Precondition, preconditions="None|list(is_precondition)", returns="bool")
    def check_sat_guard(self, source_conf, guard, preconditions=None):
 
#        log.debug("Received source pred (%s): %s" % (type(source_conf), source_conf)) 

        check("list(is_abstract_attribute)", self.attributes)       
#        log.debug("In check_sat_guard ...")  
        source_pred = conf_to_attribute_predicate(source_conf, self.attributes)

#        log.debug("Source pred: %s" % source_pred)
        new_preconditions = [ guard ]
        if preconditions:
            new_preconditions.extend(preconditions)
        commands = self.to_smt_problem(source_pred, preconditions=new_preconditions) + "\n" + "(check-sat)\n"


        answer = self.get_tool_answer(commands)

        log.debug("check guard: %s vs %s, given: %s ? %s" % (source_pred,guard,preconditions, answer))

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

@contract(source="is_configuration", pc_source=PC, instr="dict", state_space="is_state_space", project="is_project", preconditions="list(is_precondition)|None", postconditions="list(is_precondition)|None", deadlines="list(is_pc)|None", returns=ReachabilityResult)
def check_reach(source, pc_source, instr, state_space, project, preconditions=None, postconditions=None, pc_jump_stack=None, deadlines=None):
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
    source_loc = build_loc(source, pc_source, state_space) #build_loc(source, pc_source)

    # begin 
    # res contains a list of reachable configurations, a list of Edge's, and a list of final Location's
    reachable = []
    edges = []
    final = []
    external = []
 
    pc_target = None
   
    if instr_type == "ASTRE":

#        log.info("Check ASTRE: '%s' %s ..." % (instr_text, pc_source))

        node_exp_type = instr["expression"]["nodeType"]
        log.debug("Found ASTRE instruction (%s)" % node_exp_type)


        pc_target = pc_source + 1

        for target in state_space.enumerate:
            assert isinstance(target, tuple)

            log.debug("check: (%s,%s) -[%s]?-> (%s,%s)" % (source,pc_source,instr_label,target,pc_target))


            target_pred = state_space.value(target)
            with SMTProb(state_space.attributes, project) as smt_prob:
                cache_found = SMTProb.smt_cache_lookup(source, instr, target)

                is_sat = False
                if cache_found is not None:
                    is_sat = cache_found
                else:
                    smt_prob.push()
                    is_sat = smt_prob.check_sat_instr(source_pred, instr, target_pred, preconditions=preconditions)
                    smt_prob.pop()
                    SMTProb.smt_cache_store(source, instr, target, is_sat)
        
                log.debug("Instruction SAT: %s" % is_sat)
                if is_sat:
                    target_loc = build_loc(target, pc_target, state_space) #(target, pc_target)
                    edge_label = instr["code"]
                    edge = Edge(source_loc, target_loc, edge_label)
     
                    reachable.append(target)
                    edges.append(edge) 
                    final.append(target_loc)

        log.debug("ASTRE final: %s" % final)
        log.debug("ASTRE reachable: %s" % reachable)
        log.debug("ASTRE edges: %s" % edges)
        
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
            rr_then = compute_reachable(reachable_then, pc_source_then, stms_then, state_space, project, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            preconditions.pop()    

            final_then = rr_then.final_locations
            edges.extend(rr_then.edges) #edges_then)

            # add ed edge from source to the begin of the "then" branch
            then_loc = build_loc(source, pc_source_then, state_space) #(source, pc_source_then)
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
            rr_else = compute_reachable(reachable_else, pc_source_else, stms_else, state_space, project, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            preconditions.pop()

            final_else = rr_else.final_locations
       
            edges.extend(rr_else.edges) #edges_else)
            external.extend(rr_else.external_locations)
    
            # add en edge from source to the begin of the "else" branch
            else_loc = build_loc(source, pc_source_else, state_space) #(source, pc_source_else)
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
            end_if_loc = build_loc(state_final_conf, pc_target, state_space)            

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

        # determine whether the guard and/or its negation are satisfiable
        with SMTProb(state_space.attributes, project) as smt_prob:

            smt_prob.push()
            is_while_reachable = smt_prob.check_sat_guard(source_pred, guard=Precondition(guard["expression"]))
            smt_prob.pop()
    
            smt_prob.push()
            is_not_while_reachable = smt_prob.check_sat_guard(source_pred, guard=Negate(guard["expression"]))
            smt_prob.pop()

        # the guard is satisfiable: enter the block
        if is_while_reachable:
    
            pc_source_while = PC(pc_source.pc).push(0)
            reachable_while = [ source ]
            
            preconditions.append(Precondition(guard["expression"]))
            pc_jump_stack.append((while_identifier, pc_source_while, pc_target))
            
            rr_while = compute_reachable(reachable_while, pc_source_while, stms_while, state_space, project, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
            
            pc_jump_stack.pop()
            preconditions.pop()

#            log.debug("While reachable result: %s" % rr_while)

            edges.extend(rr_while.edges)
            external.extend(rr_while.external_locations)
    
            # add edge from source to the begin of the while
            # TODO at the moment we don't take advantage of the while-guard as precondition
            while_loc = build_loc(source, pc_source_while, state_space)# (source, pc_source_while)
            while_edge = Edge(source_loc, while_loc, "while (%s)" % guard["code"])
            edges.append(while_edge)

            # if entered the while loop, for each final location of while, create an edge out of the while loop and an edge at the beginning of the while itself
            # TODO this is an overapproximation (every final state can repeat the loop and/or exit); invoke the compute_reachable(...) procedure to "unfold" the loop (beware of endless loops)
            for loc in rr_while.final_locations: #final_while:
                assert isinstance(loc, Location)
                (state_final_conf, pc_final) = parse_location(loc)

                loc_out = build_loc(state_final_conf, pc_target, state_space)
                edge_out = Edge(loc, loc_out, "end while")
                edges.append(edge_out)
                final.append(loc_out)
                reachable.append(state_final_conf)
    
                loc_restart = build_loc(state_final_conf, pc_source, state_space)
                edge_restart = Edge(loc, loc_restart)
                edges.append(edge_restart)
    
        # the negation of the guard is satisfiable: jump over the block
        if is_not_while_reachable:

            # add edge from source to the end of the while
            while_neg_loc = build_loc(source, pc_target, state_space) #(source, pc_target)
            while_neg_edge = Edge(source_loc, while_neg_loc, "not (%s)" % guard["code"])
            edges.append(while_neg_edge)
            final.append(while_neg_loc)
            reachable.append(source)

        check_closure(pc_target, reachable, final, external)

    elif instr_type == "ASTDoWhile":
        assert "expr" in instr, instr.keys()
        assert "stms" in instr
        assert "identifier" in instr, "ASTWhile: %s" % instr

        while_identifier = instr["identifier"]
        stms_while = instr["stms"]
        guard = instr["expr"]
        assert "code" in guard
        assert "expression" in guard
        pc_target = pc_source + 1

        # always enter the while block
        pc_source_while = PC(pc_source.pc).push("0")

        pc_jump_stack.append((while_identifier, pc_source_while, pc_target))
        rr_while = compute_reachable([source], pc_source_while, stms_while, state_space, project, preconditions, pc_jump_stack, deadlines=deadlines)
        pc_jump_stack.pop()

        edges.extend(rr_while.edges)
        external.extend(rr_while.external_locations)

        # add an edge that "falls" inside the while block, from outside
        do_loc = build_loc(source, pc_source_while, state_space)
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
                    rr_while_back = compute_reachable([conf_final], pc_source_while, stms_while, state_space, project, preconditions=preconditions, pc_jump_stack=pc_jump_stack, deadlines=deadlines)
                    pc_jump_stack.pop()
                    preconditions.pop()
        
                    edges.extend(rr_while_back.edges)
                    external.extend(rr_while_back.external_locations)
            
                    # add edge from current location to the begin of the while
                    # TODO at the moment we don't take advantage of the while-guard as precondition
                    while_loc = build_loc(conf_final, pc_source_while, state_space)# (source, pc_source_while)
                    while_edge = Edge(loc_final, while_loc, "while (%s)" % guard["code"])
                    edges.append(while_edge)
        
                    # if entered the while loop, for each final location of while, create an edge out of the while loop and an edge at the beginning of the while itself
                    # TODO this is an overapproximation (every final state can repeat the loop and/or exit); invoke the compute_reachable(...) procedure to "unfold" the loop (beware of endless loops)
                    for loc in rr_while_back.final_locations:
                        assert isinstance(loc, Location)
                        (state_final_conf, pc_final) = parse_location(loc)
        
                        loc_out = build_loc(state_final_conf, pc_target, state_space)
                        edge_out = Edge(loc, loc_out, "end while")
                        edges.append(edge_out)
                        final.append(loc_out)
                        reachable.append(state_final_conf)
            
                        loc_restart = build_loc(state_final_conf, pc_source, state_space)
                        edge_restart = Edge(loc, loc_restart)
                        edges.append(edge_restart)
            
            # the negation of the guard is satisfiable: jump over the while
            if is_not_while_reachable:

                # add edge from current location to the end of the while
                while_neg_loc = build_loc(conf_final, pc_target, state_space)
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

        loc_out = build_loc(source, pc_target, state_space) #(source, pc_target)
        # new edge: from source location to final location
        edge_break = Edge(source_loc, loc_out, edge_label)
        edges.append(edge_break)  
        external.append(loc_out)

        # no final locations
        # (ASTBreak and ASTContinue break the compositionality approach)
    elif instr_type == "ASTReturn":
        # do nothing    
        log.debug("Return: %s" % instr)
        # begin BIG-FRAGILE-HACK
        res = instr["expr"]["code"] 
        (state_loc,pc_loc) = parse_location(source_loc)
        if res == "true":
            state_loc = tuple_replace(state_loc, 2, 0) # 2 is the position of "res", 0 encodes true
        else:
            state_loc = tuple_replace(state_loc, 2, 1) # 2 is the position of "res, 1 encodes false
        # end BIG-FRAGILE-HACK
        pc_target = pc_source + 1
        return_loc = build_loc(source, pc_target, state_space)
        edges.append(Edge(source_loc,return_loc))
        reachable.append(state_loc)
#        final.append(source_loc)

#        check_closure(pc_target, reachable, final, external)

    elif instr_type == "ASTTry":
        assert "tryBranch" in instr
        assert "catchBranch" in instr
        assert "finallyBranch" in instr

        pc_target = pc_source + 1

        # recursive call on the try branch
        stms_try = instr["tryBranch"]["stms"]
        pc_source_try = PC(pc_source.pc).push("0").push("0")
        rr_try = compute_reachable([source], pc_source_try, stms_try, state_space, project, preconditions, pc_jump_stack, deadlines=deadlines)
        edges.extend(rr_try.edges)
        external.extend(rr_try.external_locations)
    
        # add edge from source to the begin of the try
        try_loc = build_loc(source, pc_source_try, state_space)
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
            if int(loc_conf[1]) == 1:
                found_exception_locs.append(loc)
                found_exception_confs.append(loc_conf)
        # end BIG-HACK-...

        log.debug("Found exception states: %s" % found_exception_locs)
        stms_catch = instr["catchBranch"][0]["stms"] # HACK assume there is exactly 1 catchBranch
        pc_source_catch = PC(pc_source.pc).push("1").push("0")
        log.debug("Compute reachable in catch ...")
        rr_catch = compute_reachable(found_exception_confs, pc_source_catch, stms_catch, state_space, project, preconditions, pc_jump_stack, deadlines=deadlines)

        # add edge from exception states in the try block, to the begin of the catch block
        for excp_loc in found_exception_locs:   
            (excp_conf, excp_pc) = parse_location(excp_loc)
            catch_excp_loc = build_loc(excp_conf, pc_source_catch, state_space)
            edges.append(Edge(excp_loc, catch_excp_loc, "catch"))

        final_catch = rr_catch.final_locations


        for loc in final_try + final_catch:
            log.debug("Sleep check final loc: %s" % loc)

            assert isinstance(loc, Location)
            (state_loc, pc_loc) = parse_location(loc)
            end_try_loc = build_loc(state_loc, pc_target, state_space)
            edges.append(Edge(loc, end_try_loc))       
            final.append(end_try_loc)
            reachable.append(state_loc)

        check_closure(pc_target, reachable, final, external)

    elif instr_type == "ASTDeadline":

        assert "stms" in instr

        stms_deadline = instr["stms"]
        pc_source_deadline = PC(pc_source.pc).push("0")
        deadlines.append(pc_source) 
        rr_deadline = compute_reachable([source],pc_source_deadline, stms_deadline, state_space, project, preconditions, pc_jump_stack, deadlines=deadlines)
        deadlines.pop()
        edges.extend(rr_deadline.edges)
    
        # add edges from source to the begin of the deadline
        deadline_loc = build_loc(source, pc_source_deadline, state_space)
        deadline_edge = Edge(source_loc, deadline_loc, "")
        edges.append(deadline_edge)

        final_deadline = rr_deadline.final_locations

        pc_target = pc_source + 1

        for loc in rr_deadline.final_locations:
            (loc_conf, loc_pc) = parse_location(loc)
            post_loc = build_loc(loc_conf, pc_target)
            e = Edge(loc, post_loc)
            edges.append(e)

        # add edges from intermediate locations to exception state
#        exception_conf = tuple_replace(source, 1, 1) # first 1 is the position of "exception", second 1 encodes true
#        exception_loc = build_loc(exception_conf, pc_source, state_space)

#        for curr_edge in rr_deadline.edges: #final_locations: #configurations:
#            loc = curr_edge.target
##        for loc in rr_deadline.locations:
##            #assert isinstance(state_conf, Configuration)
##            #loc = build_loc(state_conf, pc_source, state_space)
##            (loc_conf, loc_pc) = parse_location(loc)
##
##            clock = FreshNames.get_clock_variable(pc_source, prefix="deadline")
##            (clock_lower, clock_upper) = FreshNames.get_clock_bound(pc_source)
##
##            # add an invariant 
##            invariant = "%s <= %s" % (clock.name, clock_upper.name)
##            loc.set_invariant(ClockExpression(invariant))
##
##            # add a transition to an exception location, if the deadline is passed
##            exception_conf = tuple_replace(loc_conf, 1, 1)
##            exception_loc = build_loc(exception_conf, loc_pc, state_space)
##
##            
##            guard = "%s >= %s" % (clock.name, clock_upper.name)
##            edges.append(Edge(loc, exception_loc, label=guard, guard=ClockExpression(guard), clock_variables=[clock], variables=[clock_lower,clock_upper]))
##            external.append(exception_loc)
##            reachable.append(exception_conf)
##
        log.debug("Final deadline locations: %s" % final_deadline)
        for loc in final_deadline:
            log.debug("Deadline check final loc: %s" % loc)
            assert isinstance(loc, Location)
            (state_loc, pc_loc) = parse_location(loc)
            end_deadline_loc = build_loc(state_loc, pc_target, state_space)
            edges.append(Edge(loc, end_deadline_loc))       
            final.append(end_deadline_loc)
            reachable.append(state_loc)

        check_closure(pc_target, reachable, final, external)

    else:
        #raise ValueError("Instruction type not covered: %s" % instr)
        log.warning("Instruction ignored (%s): %s" % (instr_type, instr))

   
    assert (len(reachable) >= 0 and len(edges) >= 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0), "# reachable: %s, # edges: %s, # final: %s. Instruction: %s" % (len(reachable), len(edges), len(final), instr)
    rr = ReachabilityResult(configurations=reachable, final_locations=final, external_locations=external, edges=edges)

    # handle stack of deadlines
    log.debug("Curr deadlines: %s" % deadlines)

    curr_edges = list(rr.edges)
    for pc in deadlines:
        log.debug("Curr locations: %s" % rr.locations)
        cv = FreshNames.get_clock_variable(pc, "deadline")
        (lower, upper) = FreshNames.get_clock_bound(pc, "deadline")

        for loc in rr.locations:
            # add invariant for states 
            inv = "%s <= %s" % (cv.name, upper.name)
            if loc.invariant:
                inv = "(%s) and (%s)" % (loc.invariant, inv)
            loc.set_invariant(inv)

            # add exception location and edge to it
            (loc_conf, loc_pc) = parse_location(loc)
            exc_conf = tuple_replace(loc_conf, 1, 1)
            exc_loc = build_loc(exc_conf, loc_pc, state_space)
            
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
 

@contract(conf="tuple", attributes="list(is_abstract_attribute)", returns="list(is_attribute_predicate)")
def conf_to_attribute_predicate(conf, attributes):
    res = []
    for (attr, pred) in zip(attributes, conf):
        check("is_abstract_attribute", attr)
        check("is_predicate", pred)
        res.append(AttributePredicate(attr, pred))

    return res


@contract(loc="is_location", returns="tuple(is_configuration,is_pc)")
def parse_location(loc):
    loc_label, pc_label = loc.name.split("@") # TODO this depends on how the location name is built
    conf = tuple(map(int, loc_label.strip("()").split(",")))
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

@contract(method=Method, state_space="is_state_space", returns=TA)
def translate_method_to_ta(method, state_space):

    instructions = method.ast["stms"]

    ta = transform(method.name, instructions, state_space, method.project)

    return ta


@contract(lit_value="string", returns="string")
def literal_to_smt(lit_value):

    STR_MARKERS = [ '"', "'" ]
    res = lit_value
    if lit_value in [ "True", "False" ]:
        # this match Java boolean values
        res = lit_value.lower()
    elif re.match("^[0-9]+(\.[0-9]+)$|^\.[0-9]+$", lit_value): #lit_value.isdigit():
        # this matches Java integer values and float values
        res = lit_value
    elif lit_value == "null": 
        # this matches a null pointer or a string/char constant
        res = "%s" % SymbolTable.add_literal(lit_value)
    elif lit_value[0] in STR_MARKERS and lit_value[-1] == lit_value[0]:
        res = "%s" % SymbolTable.add_literal(lit_value[1:-1])
    elif re.match("^[a-zA-Z0-9_]+$", lit_value):
        # this match an identifier, not really a literal
        res = lit_value
    else:
        # don't know what literal is
        log.warning("Don't know how to handle literal '%s'" % lit_value)

    return res


