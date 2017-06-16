from time import sleep
from contracts import contract, new_contract

import os
import tempfile
import subprocess
import fcntl

from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.translator.rules import ExtractMethodStateSpace, AddStates
from java2ta.translator import PC
from java2ta.ir.models import Project, Method, Klass
from java2ta.ta.models import TA, Location, Edge, is_location, is_edge
from java2ta.abstraction import StateSpace, AbstractAttribute
from java2ta.abstraction.domains import Domain, Predicate

import sys
import logging

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
@contract(project=Project, class_fqn=basestring, class_path=basestring, method_name=basestring, domains=dict, returns=TA)
def translate_method_to_automaton(project, class_fqn, class_path, method_name, domains):

##    assert isinstance(project, Project)
##    assert isinstance(class_fqn, basestring)
##    assert isinstance(method_name, basestring)
##    assert isinstance(domains, dict)

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

@new_contract
def is_configuration_repr(text):
    if not isinstance(text,basestring):
        raise ValueError("A predicate representation must be of type 'basestring'")
     # remove enclosing parenthesis
    text = text.strip("()")
    
    for item in text.strip().split(","):
        try:
            int(item) # force casting of string to int; if not possible, raise an exception
        except ValueError:
            raise ValueError("A predicate can only contain predicate codes (of type int)")
 
    return True  

@new_contract
def is_predicate(pred):

    if not isinstance(pred, Predicate):
        raise ValueError("Instance of class Predicate expected")

    return True

@new_contract
def is_attribute_predicate(pred):

    if not isinstance(pred, AttributePredicate):
        raise ValueError("Instance of class AttributePredicate expected")

    return True



@new_contract
def is_configuration(pred):

    if not isinstance(pred, tuple):
        raise ValueError("A configuration is a tuple of int")
    
    for curr in pred:
        if not isinstance(curr, int):
            raise ValueError("A configuration can only contain int")

    return True



@contract(pred="is_configuration|is_configuration_repr", pc=PC, returns=Location)
def build_loc(pred, pc):

    loc_name = "%s%s" % (pred, pc)

    loc = Location(loc_name)

    return loc

@contract(source_conf="list(is_configuration)", pc_source=PC, instr=dict, state_space=StateSpace, returns="tuple(list(is_configuration),list(is_edge),list(is_location))")
def compute_reachable(source_conf, pc_source, instr, state_space, preconditions=None):
    """
    INPUT:
    - source_conf : list of abstract configuration
    - pc_source : PC
    - instr : representation of instruction
    - state_space : StateSpace

    OUTPUT:
    - reachable : list of abstract configurations
    - edges : list of Edge
    - final : list of Location that have incoming Edge in edges, but no outgoing Edge
    """
#    assert isinstance(source_conf, list)
#    assert isinstance(pc_source, PC)
#    assert isinstance(instr, dict)
    assert preconditions is None or isinstance(preconditions, list)

    edges = []
    reachable = []
    final = []

    for source in source_conf:
        # TODO in principle each invokaction of check(...) is independent from the others
        (new_reachable, new_edges, new_final) = check(source, pc_source, instr, state_space, preconditions=preconditions)

        assert (len(new_reachable) == 0 and len(new_edges) == 0 and len(new_final) == 0) or (len(new_reachable) > 0 and len(new_edges) > 0 and len(new_final) > 0)

        edges.extend(new_edges)
        reachable.extend(new_reachable)
        final.extend(new_final)
            
    assert (len(reachable) == 0 and len(edges) == 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0)
    return (reachable, edges, final)


@contract(name=basestring, instructions="list(dict)", state_space=StateSpace, returns=TA)
def transform(name, instructions, state_space):
#    assert isinstance(name, basestring)
#    assert isinstance(instructions, list)
#    assert isinstance(state_space, StateSpace), state_space

    # TODO at the moment we call TA the class for the FSA ... this is not a big issue, but I leave
    # this note just to keep track of this "oddity"
    fsa = TA(name)

    log.info("State space attributes:")
    for attr in state_space.attributes:
        log.info("%s -> %s" % (attr.name, attr.domain))

    reach = dict()
    pc_source = PC(initial="0")
    reach[pc_source] = state_space.initial_configurations
    

    for instr in instructions:
        pc_target = pc_source + 1

        source_conf = reach[pc_source]

        if not source_conf:
            log.warning("Exit for no reachable states at PC: %s" % pc_source)
            break

        (reachable, edges, final) = compute_reachable(source_conf, pc_source, instr, state_space)
        assert (len(reachable) == 0 and len(edges) == 0) or (len(reachable) > 0 or len(edges) > 0)
        reach[pc_target] = reachable

        if len(edges) > 0:
            for e in edges:
                assert isinstance(e, Edge), e
    
                fsa.get_or_add_edge(e)

#        print "Found reachable configurations at PC = %s: %s" % (pc_target, reach[pc_target])
        pc_source = pc_target
    return fsa


class AttributePredicate(object):

    @contract(attribute=AbstractAttribute, predicate=Predicate)
    def __init__(self, attribute, predicate):

        self.attribute = attribute
        self.predicate = predicate


    def smt_assert(self):
        # TODO here we assume that all predicates have a variable {var} in them
        return self.predicate.smt_assert(var=self.attribute.name)
 
    def label(self):
        # TODO here we assume that all predicates have a variable {var} in them
        return self.predicate.label(var=self.attribute.name)       

    def __str__(self):
        # TODO here we assume that all predicates have a variable {var} in them
        return str(self.predicate).replace("{var}", self.attribute.name)


    @contract(self="type(t)",returns="type(w),w=t")
    def primed(self):
        assert isinstance(self.predicate, Predicate)

        primed_pred = self.predicate.primed(suffix="_1")
        new_attr_pred = AttributePredicate(self.attribute, primed_pred)

        return new_attr_pred


class Cache(object):

    @contract(name=basestring)
    def __init__(self, name):
        self._values = {}
        self.n_hits = 0
        self.n_tot = 0
        self.name

    @property
    @contract(returns="int,>=0")
    def n_misses(self):
        return self.n_tot - self.n_hits    

    @property
    @contract(returns="int,>=-1")
    def ratio(self):
        r = -1
        if self.n_tot > 0:
            r = self.n_hits / self.n_tot
        return r
       
    @contract(key=basestring)
    def lookup(self, key):
        res = None
        self.n_tot = self.n_tot + 1
        if key in self._values:
            res = self._values[key]
            self.n_hit = self.n_hits + 1
            log.debug("Cache %s hit: %s => %s" % (self.name, key, res))
        else:
            log.debug("Cache %s miss: %s" % (self.name, key))

        return res

    @contract(key=basestring)
    def store(self, key, new_value):
        self._values[key] = new_value

class SMTProb(object):

    OP_DECODE = { "plus": "+", "minus": "-", "greater": ">", "less": "<"}

    _SMT_CACHE = Cache()
    _NODE_TO_SMT_CACHE = Cache()

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
    @contract(node="dict", smt=basestring)
    def node_cache_store(node, smt):
        node_key = node["code"].strip()
        SMTProb._NODE_TO_SMT_CACHE.store(node_key, smt)
        
    
    @staticmethod
    @contract(source_conf="is_configuration", instr="dict", target_conf="is_configuration", returns="None|bool")
    def cache_lookup(source_conf, instr, target_conf):
        source_key = str(source_conf)
        instr_key = instr["code"].replace("\n","")
        target_key = str(target_conf)        

        key = "%s-%s-%s" % (source_key, instr_key, target_key)
        res = SMTProb._SMT_CACHE.lookup(key)
        return res

    @staticmethod
    @contract(source_pred="is_configuration", instr="dict", target_pred="is_configuration", res=bool)
    def cache_store(source_pred, instr, target_pred, res):
        source_key = str(source_pred)
        instr_key = instr["code"].replace("\n","")
        target_key = str(target_pred)        

        key = "%s-%s-%s" % (source_key, instr_key, target_key)
        SMTProb._SMT_CACHE.store(key, res)

    @contract(node="dict", returns=basestring)
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
                        if curr.name != var:
                            others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
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
                others = []
                for curr in self.attributes:
                    assert isinstance(curr, AbstractAttribute)
                    if curr.name != var:
                        others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
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
                        if curr.name != var:
                            others.append("(= %s_1 %s)" % (curr.name, curr.name)) # TODO primed name
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
            assert isinstance(pred, AttributePredicate)
        
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
            attr_declaration = "(declare-const %s %s)" % (attr.name, type_name) #attr.domain.name)
            smt_code.append(attr_declaration) 
 
            if primed:       
                # declare one constant for the primed attribute
                primed_attr = attr.primed()

                primed_attr_declaration = "(declare-const %s %s)" % (primed_attr.name, type_name) # NB type of attr and primed_attr is the same #primed_attr.domain.name)
                smt_code.append(primed_attr_declaration)
 
        return smt_code


    @contract(source_pred="list(is_attribute_predicate)", instr="None|dict", target_pred="None|list(is_attribute_predicate)", returns=basestring)
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
        except IOError:
            # there was no error to read
            pass

        return err_msg

    def _get_output(self):
        return self._cmd.stdout.readline()

    def _log_smt(self, commands):

        if isinstance(commands, basestring):
            commands = commands.split("\n")

        for line in commands:
            if line:
                #print "SMT> %s" % line
                log.debug("SMT> %s" % line)


    def _check_error(self, line, default=None):
        
        if line.startswith("(error "):
            raise ValueError(line)
        elif default:
            raise ValueError(default)
    
    @contract(commands=basestring, returns=basestring)
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

    def check_sat_guard(self, source_conf, guard, preconditions=None):
        assert isinstance(source_conf, tuple) # it is a tuple of Predicate's
        assert isinstance(self.attributes, list)   
        assert isinstance(guard, Precondition)
        assert preconditions is None or isinstance(preconditions, list)
     

        log.debug("In check_sat_guard ...")  
        source_pred = conf_to_attribute_predicate(source_conf, self.attributes)

        log.debug("Source pred: %s" % source_pred)
        new_preconditions = [ guard ]
        if preconditions:
            new_preconditions.extend(preconditions)
        commands = self.to_smt_problem(source_pred, preconditions=new_preconditions) + "\n" + "(check-sat)\n"

        log.debug("SMT run: %s" % commands)

        answer = self.get_tool_answer(commands)
        smt_res = False
        if answer.strip() == "sat":
            smt_res = True
        elif answer.strip() == "unsat":
            smt_res = False
        else:
            self._check_error(answer, default="Unknown value")

        return smt_res




#    @contract(source_pred="is_predicate", instr=dict, target_pred="is_predicate", returns=bool)
    def check_sat_instr(self, source_conf, instr, target_conf, preconditions=None):

        # TODO at the moment we don't use the precondition
        assert isinstance(source_conf, tuple) # it is a tuple of Predicate's
        assert isinstance(target_conf, tuple) # it is a tuple of Predicate's
        assert isinstance(self.attributes, list)   
        assert preconditions is None or isinstance(preconditions, list)

#        self.instr = instr
    
        source_pred = conf_to_attribute_predicate(source_conf, self.attributes)
        target_pred = conf_to_attribute_predicate(target_conf, self.attributes)

        commands = self.to_smt_problem(source_pred, instr, target_pred) + "\n" + "(check-sat)\n"

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

class Precondition(object):

    def __init__(self, node):
        assert isinstance(node, dict) or isinstance(node, Precondition)
        self.child = node

    @property
    @contract(returns="dict")
    def node(self):
        if isinstance(self.child, Precondition):
            node = self.child.node
        else:
            node = self.child

        return node

class Negate(Precondition):
    pass

@contract(source="is_configuration", pc_source=PC, instr=dict, state_space=StateSpace, returns="tuple(list,list,list)")
def check(source, pc_source, instr, state_space, preconditions=None, postconditions=None):
#    assert isinstance(source, tuple)
#    assert isinstance(pc_source, PC)
#    assert isinstance(state_space, StateSpace)
    assert preconditions is None or isinstance(preconditions, list)

#    print instr

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

    # begin 
    # res contains a list of reachable configurations, a list of Edge's, and a list of final Location's
    reachable = []
    edges = []
    final = []


    if instr_type == "ASTRE":

        log.info("Check ASTRE: '%s' %s ..." % (instr_text, pc_source))

        for target in state_space.enumerate: #configurations:
            assert isinstance(target, tuple)

            pc_target = pc_source + 1
            #print "check: (%s,%s) -[%s]?-> (%s,%s)" % (source,pc_source,instr_label,target,pc_target)
            log.debug("check: (%s,%s) -[%s]?-> (%s,%s)" % (source,pc_source,instr_label,target,pc_target))


            target_pred = state_space.value(target)
            with SMTProb(state_space.attributes) as smt_prob:
                cache_found = SMTProb.cache_lookup(source, instr, target)

                is_sat = False
                if cache_found is not None:
                    is_sat = cache_found
                else:
                    smt_prob.push()
                    is_sat = smt_prob.check_sat_instr(source_pred, instr, target_pred, preconditions=preconditions)
            #res[2].append(end_if_loc)
                #print "SMT solver result: %s" % is_sat 
                    log.debug("SMT solver result: %s" % is_sat)
                    smt_prob.pop()
                    SMTProb.cache_store(source, instr, target, is_sat)
        
                if is_sat:
                    #source_loc_label = "%s%s" % (source, pc_source)
                    #target_loc_label = "%s%s" % (target, pc_target)
        
                    #source_loc = Location(source_loc_label) # TODO every case define it's own source_loc at the same way
                    #target_loc = Location(target_loc_label)
                    source_loc = build_loc(source, pc_source) # TODO do not repeat
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
#        source_confs = [ source ]
        pc_source_then = PC(pc_source.pc).push(0).push(0)
        pc_source_else = PC(pc_source.pc).push(1).push(0)

        stms_then = instr["ifBranch"]["stms"]
        stms_else = None

        if instr["elseBranch"]: 
            assert "stms" in instr["elseBranch"]
            stms_else = instr["elseBranch"]["stms"]

        final_then = final_else = []
        pc_target = pc_source + 1
        source_loc = build_loc(source, pc_source)

        with SMTProb(state_space.attributes) as smt_prob:

            assert "expression" in guard

            smt_prob.push()

            is_then_reachable = smt_prob.check_sat_guard(source_pred, guard=Precondition(guard["expression"]))
            log.debug("SMT solver result: %s" % is_then_reachable)
            smt_prob.pop()

            smt_prob.push()
            is_else_reachable = smt_prob.check_sat_guard(source_pred, guard=Negate(guard["expression"]))
            log.debug("SMT solver result: %s" % is_else_reachable)
            smt_prob.pop()

    
        if (is_then_reachable):
            curr_pc = PC(pc_source_then.pc)
            reachable_then = [ source ]
            for instr_then in stms_then:
                (reachable_then, edges_then, final_then) = compute_reachable(reachable_then, curr_pc, instr_then, state_space, preconditions=[Precondition(guard["expression"])])
                curr_pc.inc()
    
                # add reachable configuration and edges to the results
    #            reachable.extend(reachable_then)
                edges.extend(edges_then)
    
            # add ed edge from source to the begin of the "then" branch
            then_loc = build_loc(source, pc_source_then)
            edge_then = Edge(source_loc, then_loc, "if (%s)" % guard["code"])
 
            # add an edge to the set of resulting edges       
            edges.append(edge_then)

        if stms_else:
            # the else statement is optional: here it is present
            assert "expression" in guard

            if is_else_reachable:

                reachable_else = [ source ]
                curr_pc = PC(pc_source_else.pc)
                for instr_else in stms_else:
                    (reachable_else, edges_else, final_else) = compute_reachable(reachable_else, curr_pc, instr_else, state_space, preconditions=[Negate(guard["expression"])])
                    curr_pc.inc()
        
                    # add reachable conf and edges to the results
                    edges.extend(edges_else)    
        
                # add en edge from source to the begin of the "else" branch
                else_loc = build_loc(source, pc_source_else)
                edge_else = Edge(source_loc, else_loc, "else (not(%s))" % guard["code"])
                # add an edge to the results
                edges.append(edge_else)
        else:
            # the else branch is optional: here it is not present
    
            if is_else_reachable:
                final_else = [ source_loc ] # in the following we will add an edge from source_loc to end_if_loc

        # add an edge from each one of the reachable state to the same
        # state with a "popped" pc

        for loc in final_then + final_else:
            assert isinstance(loc, Location)
            (state_pred_label, pc) = loc.name.split("@") # TODO this relies on how PC builds labels
            state_pred = parse_configuration(state_pred_label)

            # the end-if location has the same predicate of the reached final state, but the pc of the line after the if-statement
            #end_if_loc_name = "%s%s" % (state_pred, pc_target)
            #end_if_loc = Location(end_if_loc_name)
            end_if_loc = build_loc(state_pred, pc_target)            

            # add edges and final locations to the results
            edges.append(Edge(loc, end_if_loc, "endif"))
            final.append(end_if_loc)
            reachable.append(state_pred)
 
#        if not stms_else:
    elif instr_type == "ASTWhile":
        #print instr
        assert "expr" in instr, instr.keys()
        assert "stms" in instr

        log.info("Check ASTWhile: '%s' %s ..." % (instr_text, pc_source))

        source_loc = build_loc(source, pc_source) # TODO factorize
        stms_while = instr["stms"]
        guard = instr["expr"]
        assert "code" in guard
        assert "expression" in guard
        pc_target = pc_source + 1

        with SMTProb(state_space.attributes) as smt_prob:

            smt_prob.push()
            is_while_reachable = smt_prob.check_sat_guard(source_pred, guard=Precondition(guard["expression"]))
            log.debug("SMT solver result: %s" % is_while_reachable)
            smt_prob.pop()
    
            smt_prob.push()
            is_not_while_reachable = smt_prob.check_sat_guard(source_pred, guard=Negate(guard["expression"]))
            log.debug("SMT solver result: %s" % is_not_while_reachable)
            smt_prob.pop()

        final_while = []
        if is_while_reachable:
    
            pc_source_while = PC(pc_source.pc).push(0)
            reachable_while = [ source ]
            curr_pc = PC(pc_source_while.pc)
            for instr_while in stms_while:
                assert isinstance(instr_while, dict)
                (new_reachable_while, edges_while, final_while) = compute_reachable(reachable_while, curr_pc, instr_while, state_space, preconditions=[Precondition(guard["expression"])])
                log.debug("%s%s =[while(%s)]=> %s%s" % (reachable_while, curr_pc, guard["code"], new_reachable_while, curr_pc+1))
                reachable_while = new_reachable_while
                curr_pc.inc()
    
                # add reachable configuration and edges to the results
                edges.extend(edges_while)    
    
            # add edge from source to the begin of the while
            # TODO at the moment we don't take advantage of the while-guard as precondition
            while_loc = build_loc(source, pc_source_while)
            while_edge = Edge(source_loc, while_loc, "while (%s)" % guard["code"])
            edges.append(while_edge)
            
        if is_not_while_reachable:

            # add edge from source to the end of the while
            while_neg_loc = build_loc(source, pc_target)
            while_neg_edge = Edge(source_loc, while_neg_loc, "not (%s)" % guard["code"])
            edges.append(while_neg_edge)
            final.append(while_neg_loc)
            reachable.append(source)
    
        # if entered the while loop, for each final location of while, create an edge out of the while loop and an edge at the beginning of the while itself
        for loc in final_while:
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

    elif instr_type == "ASTReturn":

        log.info("Check ASTReturn: '%s' %s ..." % (instr_text, pc_source))
    
        # do nothing

    else:
        #raise ValueError("Instruction type not covered: %s" % instr)
        log.warning("Instruction ignored: %s" % instr)

#    assert  isinstance(res,tuple) and len(res) == 3
#    return res

    assert (len(reachable) == 0 and len(edges) == 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0), "# reachable: %s, # edges: %s, # final: %s" % (len(reachable), len(edges), len(final))
    return (reachable, edges, final)

@contract(conf="tuple", returns="list(is_attribute_predicate)")
def conf_to_attribute_predicate(conf, attributes):
    res = []
    for (attr, pred) in zip(attributes, conf):
        assert isinstance(attr, AbstractAttribute)
        assert isinstance(pred, Predicate)
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

def get_method_attributes(method):
    assert isinstance(method, Method)
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
        attributes.append((curr["name"], False))
    
    for curr in method_vars:
        attributes.append((curr["name"], True))
    
    return attributes


def get_state_space_from_method(method, domains):
    assert isinstance(method, Method)
    assert isinstance(domains, dict)

    # 1. get method attributes
    attributes = get_method_attributes(method)
    
#    print "all attributes: %s" % attributes
#    print "domains: %s" % domains
    # 2. initialize empty state-space
    ss = StateSpace()

    # 3. find attributes with an associated abstract domain
    for (attr,is_local) in attributes:
        assert isinstance(attr, basestring)
        assert isinstance(is_local, bool)
    
        if attr in domains:
            dom = domains[attr]

            assert isinstance(dom, Domain)

            abs_att = AbstractAttribute(attr, dom, is_local)# dom.values, dom.default)
            ss.add_attribute(abs_att)

    return ss


def translate_method_to_fsa(project, class_fqn, class_path, method_name, domains):

    assert isinstance(project, Project)
    assert isinstance(class_fqn, basestring)
    assert isinstance(method_name, basestring)
    assert isinstance(domains, dict)

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

    state_space = get_state_space_from_method(m, domains)

    instructions = m.ast["stms"]

    fsa = transform(method_name, instructions, state_space)

    return (m,fsa)
