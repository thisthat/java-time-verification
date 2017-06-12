from time import sleep
import os
import tempfile
import subprocess
import fcntl

from java2ta.engine.rules import Engine
from java2ta.engine.context import Context
from java2ta.translator.rules import ExtractMethodStateSpace, AddStates
from java2ta.translator import PC
from java2ta.ir.models import Project, Method, Klass
from java2ta.ta.models import TA, Location, Edge
from java2ta.abstraction import StateSpace, AbstractAttribute
from java2ta.abstraction.domains import Domain, Predicate

import sys


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
def translate_method_to_automaton(project, class_fqn, class_path, method_name, domains):

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


def compute_reachable(source_conf, pc_source, instr, state_space, precondition=None):
    """
    INPUT:
    - source_conf : list of abstract configurations
    - pc_source : PC
    - instr : representation of instruction
    - state_space : StateSpace

    OUTPUT:
    - reachable : list of abstract configurations
    - edges : list of Edge
    - final : list of Location that have incoming Edge in edges, but no outgoing Edge
    """
    assert isinstance(source_conf, list)
    assert isinstance(pc_source, PC)
    assert isinstance(instr, dict)
    assert precondition is None or isinstance(precondition, Precondition)

    edges = []
    reachable = []
    final = []

    for source in source_conf:
        (new_reachable, new_edges, new_final) = check(source, pc_source, instr, state_space, precondition=precondition)

        assert (len(new_reachable) == 0 and len(new_edges) == 0 and len(new_final) == 0) or (len(new_reachable) > 0 and len(new_edges) > 0 and len(new_final) > 0)

        edges.extend(new_edges)
        reachable.extend(new_reachable)
        final.extend(new_final)
            
    assert (len(reachable) == 0 and len(edges) == 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0)
    return (reachable, edges, final)


def transform(name, instructions, state_space):
    assert isinstance(name, basestring)
    assert isinstance(instructions, list)
    assert isinstance(state_space, StateSpace), state_space

    # TODO at the moment we call TA the class for the FSA ... this is not a big issue, but I leave
    # this note just to keep track of this "oddity"
    fsa = TA(name)

    print "State space attributes:"
    for attr in state_space.attributes:
        print "%s -> %s" % (attr.name, attr.domain)
    print

    reach = dict()
    pc_source = PC(initial="0")
    reach[pc_source] = state_space.initial_configurations
    

    for instr in instructions:
        pc_target = pc_source + 1

        source_conf = reach[pc_source]

        if not source_conf:
            print "WARNING: Exit for no reachable states at PC: %s" % pc_source
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

    def __init__(self, attribute, predicate):
        assert isinstance(attribute, AbstractAttribute)
        assert isinstance(predicate, Predicate)

        self.attribute = attribute
        self.predicate = predicate


    def smt_assert(self):
        # TODO here we assume that all predicates have a variable {var} in them
        return self.predicate.smt_assert(var=self.attribute.name)
        

    def __str__(self):
        # TODO here we assume that all predicates have a variable {var} in them
        return str(self.predicate).replace("{var}", self.attribute.name)


    def primed(self):

        primed_pred = self.predicate.primed(suffix="_1")
        new_attr_pred = AttributePredicate(self.attribute, primed_pred)

        return new_attr_pred


class SMTProb(object):

    OP_DECODE = { "plus": "+", "minus": "-" }

#    def __init__(self, source_pred, instr, target_pred, attributes):
    def __init__(self, attributes):

        assert isinstance(attributes, list) # it is a list of Attribute's
 
        self.attributes = attributes
        self.source_pred = []
        self.target_pred = []
        self.instr = None
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

    def node_to_smt(self, node):
 
        assert isinstance(node, dict), node
        assert "nodeType" in node 

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
                    print "WARNING: Interpret PL post-op expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp)
                    smt = node_exp["code"]
            else:
                print "WARNING: Interpret PL expression (%s) as SMT code: %s ..." % (node_exp["code"], node_exp)
                smt = node_exp["code"]
        elif node_type == "ASTBinary":
            assert "op" in node
            assert "left" in node
            assert "right" in node
            
            if node["op"] in self.OP_DECODE:
                op = self.OP_DECODE[node["op"]]
            else:
                print "WARNING: Interpret PL operator (%s) as SMT operator..." % op
            left = node["left"]
            right = node["right"]

            smt = "(%s %s %s)" % (op, self.node_to_smt(left), self.node_to_smt(right))
        elif node_type == "ASTLiteral":
            assert "value" in node
            smt = node["value"]
        else:
            print "WARNING: Interpret PL code (%s) as SMT code: %s ..." % (node["code"], node)
            smt = node["code"]

        return smt

    def primed(self, pred_list):
        assert isinstance(pred_list, list)
 
        res = list()
      
        for pred in pred_list:
            assert isinstance(pred, AttributePredicate)
        
            pred_primed = pred.primed()
#            print "pred: %s vs pred-primed: %s" % (pred, pred_primed)

            res.append(pred_primed)

        assert isinstance(res, list)
        return res

    def add_precondition(self, expression):
        assert isinstance(self._assert_preconditions, list)
        assert isinstance(expression, dict)

        self._assert_precoditions.append(self.node_to_smt(expression))

    @property
    def assertions(self):
    
        assert isinstance(self.source_pred, list)
        assert isinstance(self.target_pred, list)
        assert self.instr is None or self.instr == "goto" or isinstance(self.instr, dict)

        if self._assertions == None:
            self._assertions = []
            self._assertions.extend(self.source_pred)
            self._assertions.extend(self._assert_preconditions)
            
            # SMT assertions about the target predicates require that:
            # - if instruction does something, target predicates use primed vars
            # - if instroction does not do anything, target predicates use same vars as soure predicates   
            target_pred = self.target_pred

            if isinstance(self.instr, dict):
                smt_instr = self.node_to_smt(self.instr)

                if smt_instr:
                    instr_assert = "(assert %s)" % smt_instr
                    self._assertions.append(instr_assert)

                    target_pred = self.primed(target_pred)

            self._assertions.extend(target_pred)

        return self._assertions
 

    def to_smt(self):
        """
        This method returns a syntattically valid SMT problem corresponding to the
        collected assertions and instruction.

        TODO atm, this only covers Z3 syntax
        """

        smt_code = []
        
#        print "current attributes: %s" % self.attributes
        # add declaration of attributes
        for attr in self.attributes:
            assert isinstance(attr, AbstractAttribute), attr
       
            # declare the attribute data-type (if needed)
            dt_decl = attr.domain.smt_declaration
            if dt_decl:
                smt_code.append(attr.domain.dt_decl)
            
            # declare one constant for the (straight) attribute
            attr_declaration = "(declare-const %s %s)" % (attr.name, attr.domain.name)
            smt_code.append(attr_declaration) 
        
            # declare one constant for the primed attribute
            primed_attr = attr.primed()
            primed_attr_declaration = "(declare-const %s %s)" % (primed_attr.name, primed_attr.domain.name)
            smt_code.append(primed_attr_declaration)
       
        # add assertions

        for curr in self.assertions:
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
                print "SMT> %s" % line


    def _check_error(self, line, default=None):
        
        if line.startswith("(error "):
            raise ValueError(line)
        elif default:
            raise ValueError(default)
    
    def check_sat(self, source_pred, instr, target_pred, precondition=None):

        # TODO at the moment we don't use the precondition
        assert isinstance(source_pred, tuple) # it is a tuple of Predicate's
        assert isinstance(target_pred, tuple) # it is a tuple of Predicate's
        assert isinstance(self.attributes, list)   
        assert precondition is None or isinstance(precondition, Precondition)

        self.instr = instr

        self.source_pred = []
        for (attr, pred) in zip(self.attributes, source_pred):
            assert isinstance(attr, AbstractAttribute)
            assert isinstance(pred, Predicate)
            self.source_pred.append(AttributePredicate(attr, pred))

        self.target_pred = []
        for (attr, pred) in zip(self.attributes, target_pred):
            assert isinstance(attr, AbstractAttribute)
            assert isinstance(pred, Predicate)
            self.target_pred.append(AttributePredicate(attr, pred))

        commands =self.to_smt() + "\n" + "(check-sat)\n"

        self._log_smt(commands)

        self._cmd.stdin.write(commands)

        answer = self._get_output()
        err_msg = self._get_error()
        if err_msg:
            sys.stderr.write(err_msg)

        if answer.strip() == "sat":
            return True
        elif answer.strip() == "unsat":
            return False
        else:
            self._check_error(answer, default="Unknown value")


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
        self.node = node

class Negate(Precondition):
    pass

def check(source, pc_source, instr, state_space, precondition=None, postcondition=None):
    assert isinstance(source, tuple)
    assert isinstance(pc_source, PC)
    assert isinstance(state_space, StateSpace)
    assert precondition is None or isinstance(precondition, Precondition)

    print instr

    # produce a label out of a code block
    rows = instr["code"].split("\n")
    stripped_rows = map(lambda r: r.strip(), rows)
    instr_label = " ".join(stripped_rows)

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
        for target in state_space.enumerate: #configurations:
            assert isinstance(target, tuple)

            pc_target = pc_source + 1
            print "check: (%s,%s) -[%s]?-> (%s,%s)" % (source,pc_source,instr_label,target,pc_target)

            target_pred = state_space.value(target)
            with SMTProb(state_space.attributes) as smt_prob:
                smt_prob.push()
                is_sat = smt_prob.check_sat(source_pred, instr, target_pred, precondition=precondition)
            #res[2].append(end_if_loc)
                print "SMT solver result: %s" % is_sat
                smt_prob.pop()
        
                if is_sat:
                    source_loc_label = "%s%s" % (source, pc_source)
                    target_loc_label = "%s%s" % (target, pc_target)
        
                    source_loc = Location(source_loc_label)
                    target_loc = Location(target_loc_label)
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
        
        guard = instr["guard"]
        source_confs = [ source ]
        pc_source_then = PC(pc_source.pc).push(0).push(0)
        pc_source_else = PC(pc_source.pc).push(1).push(0)

        stms_then = instr["ifBranch"]["stms"]
        stms_else = None

        if instr["elseBranch"]: 
            assert "stms" in instr["elseBranch"]
            stms_else = instr["elseBranch"]["stms"]

        final_then = final_else = []

        curr_pc = PC(pc_source_then.pc)
        for instr_then in stms_then:
            (reachable_then, edges_then, final_then) = compute_reachable(source_confs, curr_pc, instr_then, state_space, precondition=Precondition(guard))
            curr_pc.inc()

            # add reachable configuration and edges to the results
            reachable.extend(reachable_then)
            edges.extend(edges_then)    

        # add ed edge from source to the begin of the "then" branch
        source_loc_label = "%s%s" % (source, pc_source)
        then_loc_label = "%s%s" % (source, pc_source_then)
        
        source_loc = Location(source_loc_label)
        then_loc = Location(then_loc_label)
        edge_then = Edge(source_loc, then_loc, "then")
 
        # add an edge to the set of resulting edges       
        edges.append(edge_then)

        if stms_else:
            # the else statement is optional

            curr_pc = PC(pc_source_else.pc)
            for instr_else in stms_else:
                (reachable_else, edges_else, final_else) = compute_reachable(source_confs, curr_pc, instr_else, state_space, precondition=Negate(guard))
                curr_pc.inc()
    
                # add reachable conf and edges to the results
                reachable.extend(reachable_else)
                edges.extend(edges_else)    

    
            # add en edge from source to the begin of the "else" branch
            else_loc_label = "%s%s" % (source, pc_source_else)
            
            else_loc = Location(else_loc_label)
            edge_else = Edge(source_loc, else_loc, "else")
            
            # add an edge to the results
            edges.append(edge_else)
    
        # add an edge from each one of the reachable state to the same
        # state with a "popped" pc

        pc_target = pc_source + 1
        for loc in final_then + final_else:
            (state_pred, pc) = loc.name.split("@") # TODO this relies on how PC builds labels

            # the end-if location has the same predicate of the reached final state, but the pc of the line after the if-statement
            end_if_loc_name = "%s%s" % (state_pred, pc_target)
            end_if_loc = Location(end_if_loc_name)

            # add edges and final locations to the results
            edges.append(Edge(loc, end_if_loc, "endif"))
            final.append(end_if_loc)


    elif instr_type == "ASTWhile":
        #print instr
        assert "expression" in instr
        assert "stms" in instr

        stms_while = instr["stms"]
        guard = instr["expression"]
        pc_source_while = PC(pc_source.pc).push(0)

        curr_pc = PC(pc_source_while.pc)
        for instr_while in stms_while:
            (reachable_while, edges_while, final_while) = compute_reachable(source_confs, curr_pc, instr_while, state_space, precondition=Precondition(guard))
            curr_pc.inc()

            # add reachable configuration and edges to the results
            reachable.extend(reachable_while)
            edges.extend(edges_while)    


            

    elif instr_type == "ASTReturn":
        # ignore
        pass
    else:
        #raise ValueError("Instruction type not covered: %s" % instr)
        print "WARNING: instruction ignored: %s" % instr

#    assert  isinstance(res,tuple) and len(res) == 3
#    return res

    assert (len(reachable) == 0 and len(edges) == 0 and len(final) == 0) or (len(reachable) > 0 and len(edges) > 0 and len(final) > 0)
    return (reachable, edges, final)

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
