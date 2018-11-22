from time import sleep
from contracts import contract
from java2ta.ir.models import ASTVisitor

import logging

log = logging.getLogger(__name__)

def check_is_open(project, max_seconds=30):
    num_attempts = max_seconds

    while num_attempts > 0 and not project.is_open():
        assert project.status in [ "open", "opening" ], "Expected project in 'opening' status. Got: '%s'" % project.status
        # repeat after a short time ...
        sleep(1)
        num_attempts = num_attempts - 1

    assert project.is_open(), "Process '%s' (path='%s',status='%s') took more that %s seconds to open, something is probably wrong ..." % (project.name, project.path, project.status, max_seconds)


@contract(returns="tuple(set(string), set(string))")
def analyze_assignment(node):
    """
    Given an ast node from the intermediate representation, return a pair of sets:  
    - the first set contains the identifiers referred to within the expression
    - the second set contains any method calls appearing withing the expression
    The passed node is the rhs of the variable declaration/assignment
    """
    assert "nodeType" in node

    found_method_calls = set([])
    found_identifiers = set([]) 

    def handle_identifier(matching_node):
        assert "code" in matching_node

        curr_identifier = matching_node["code"]
        found_identifiers.add(curr_identifier)

    def handle_method_calls(matching_node):
        assert "classPointed" in matching_node
        assert "methodName" in matching_node

        class_pointed = matching_node["classPointed"]
        method_name = matching_node["methodName"]
        found_method_calls.add("%s.%s" % (class_pointed, method_name))

    visitor = ASTVisitor(node)
    visitor.add_handler("ASTIdentifier", handle_identifier)
    visitor.add_handler("ASTMethodCall", handle_method_calls)
    visitor.visit()

    return (found_identifiers, found_method_calls)

def get_fingerprint(var, node):
    return (var, node["line"], node["start"], node["end"])

def get_fingerprints(timestamps):

    fingerprints = []
    for var, node_list in timestmaps:
        for entry in node_list:
            fingerprints.add(get_fingerprint(var, node))
    return fingerprints

@contract(node="is_ast_node|dict", returns="dict(string:string)")
def get_time_variables(node):

    time_variables = {}

    def handle_expression(curr_node):
        assert "nodeType" in curr_node
        assert curr_node["nodeType"] == "ASTRE"
        assert "expression" in curr_node
        assert "expressionName" in curr_node            
        assert "env" in curr_node
    
        for var in curr_node["env"]:
            assert "timeType" in var
            var_name = var["name"]
            if var["timeType"] != "null":
                curr_type = time_variables.get(var_name, None)

                if curr_type is not None and curr_type != var["timeType"]:
                    raise ValueError("Unexpected error: variable '%s' already present with different time type ('%s' and '%s')" % (var_name, var["timeType"], curr_type))
                else:
                    time_variables[var_name] = var["timeType"]

        
    visitor = ASTVisitor(node)
    visitor.add_handler("ASTRE", handle_expression)

    visitor.visit()

    log.debug("Time variables: %s" % time_variables)

    return time_variables



##@contract(node="is_ast_node|dict", returns="set(string)") #returns="dict(string:list(dict))")
##def get_time_variables(node):
##
##    time_variables = set([])
####    timestamp_fingerprints = []
####
####    @contract(curr_var="string")
####    def check_var_is_timestamp(curr_var, rhs_node):
####        """
####        This function is a common task in the analysis of variable declarations and assignments.
####        """
####        curr_fingerprint = get_fingerprint(curr_var, rhs_node)
####
####        if curr_fingerprint not in timestamp_fingerprints:
####            is_timestamp = rhs_node.get("timeCritical", False)
####
####            print "check var %s is timestamp: %s" % (curr_var, is_timestamp)
####    
####            if is_timestamp:
####                print "var %s is timestamp" % curr_var
####                existing = time_variables.get(curr_var, None)
####                if existing is None:
####                    existing = []
####                existing.append(rhs_node)
####                time_variables[curr_var] = existing
####                timestamp_fingerprints.append(curr_fingerprint)
####
####    
####    def handle_var_declaration(curr_node):
####        assert "name" in curr_node, curr_node
####        assert "nodeType" in curr_node["name"]
####        assert curr_node["name"]["nodeType"] == "ASTIdentifier", "Expected node with ASTIdentifier. Got: %s" % (curr_node["name"], )
####        assert "expr" in curr_node, curr_node
####
####        curr_var = curr_node["name"]["value"]
####        log.debug("Check var declaration: %s" % curr_var)
####        rhs_node = curr_node["expr"]
####
####        log.debug("Check curr var %s is timestamp ..." % curr_var)
####        check_var_is_timestamp(curr_var, rhs_node) #curr_node)
####
####    def handle_var_assignment(curr_node):
####        assert "nodeType" in curr_node
####        assert curr_node["nodeType"] == "ASTAssignment"
####        assert "left" in curr_node, curr_node
####        assert "right" in curr_node, curr_node
####        curr_var = None
####
####        log.debug("Check var assignment: %s" % curr_node["left"])
####
####        if curr_node["left"]["nodeType"] == "ASTIdentifier":
####            curr_var = curr_node["left"]["value"]
####            rhs_node = curr_node["right"]
####
####            #print "lhs is variable: %s" % curr_var
####            log.debug("Check curr var %s is timestamp ..." % curr_var)
####            check_var_is_timestamp(curr_var, rhs_node) #curr_node)
#### 
##       
####    def handle_identifier(curr_node):
####        assert "nodeType" in curr_node
####        assert curr_node["nodeType"] == "ASTIdentifier"
####        assert "timeCritical" in curr_node
####        assert "code" in curr_node
####
####        if curr_node["timeCritical"]:
####            time_variables.add(curr_node["code"])
##
##    visitor = ASTVisitor(node)
###    visitor.add_handler("ASTAssignment", handle_var_assignment)
###    visitor.add_handler("ASTVariableDeclaration", handle_var_declaration)
###    visitor.add_handler("ASTRE", handle_astre)
##    visitor.add_handler("ASTIdentifier", handle_identifier)
##    
##    visitor.visit()
##
##    log.debug("Time variables: %s" % time_variables)
##
##    return time_variables
##

@contract(node="dict", returns="bool")
def check_sleep_invocation(node):
    log.debug("Check sleep invocation from node: %s" % node)
    return "nodeType" in node and node["nodeType"] == "ASTRE" and\
            "expressionName" in node and node["expressionName"] == "call_to_sleep"

@contract(nodes="list(dict)", now_methods="set(string)", returns="bool")
def check_now_assignments(nodes, now_methods):
    """
    Check whether the passed list of nodes are now-assignment. A now-assignment is
    defined as the result of a method call, such that the invoked method is in the
    list of now_methods.

    In other words:
    - if a node in the list is not a method call, the entire list does not contain only now-assignments
    - if a node in the list is a method call to a method not in the list of now_methods, the entire list does not contain only now-assignments
    - if the current list of assignment is empty, we don't consider it as a list of now-assignments
    """
    res = len(nodes) > 0

    log.debug("Check now assignments : now methods = %s ..." % (now_methods, ))
    for curr in nodes:
        if curr["nodeType"] != "ASTMethodCall":
            res = False
            break

        called_method = "%s.%s" % (curr["classPointed"], curr["methodName"])
        log.debug("Check now assignment: called method = %s" % called_method)
        if called_method not in now_methods:
            res = False     
            break
     
    log.debug("Check now assignments : result = %s ..." % res)   
    return res
        

def get_identifiers(node):
        identifiers = []

        def save_identifier(curr_node):
            identifiers.append(curr_node["value"])

        visitor = ASTVisitor(node)
        visitor.add_handler("ASTIdentifier", save_identifier)
        visitor.visit()
    
        return identifiers


def get_instr_text(instr, max_len=None): 
    rows = instr["code"].split("\n")
    stripped_rows = map(lambda r: r.strip(), rows)
    instr_text = " ".join(stripped_rows)

    if max_len > 0:
        len_text = len(instr_text)
        if len_text > max_len:
            cut_len = max_len/2
            instr_tetx = instr_text[:cut_len].strip() + "..." + instr_text[len_text-cut_len:].strip()

    return instr_text
