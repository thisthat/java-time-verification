from time import sleep
from contracts import contract
from java2ta.ir.models import ASTVisitor


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

@contract(node="is_ast_node|dict", now_methods="set(string)")
def get_timestamps(node, now_methods):

    timestamp_variables = {}
    timestamp_fingerprints = []
    prev_timestamp_variables = {}
    prev_timestamp_fingerprints = [] 

    @contract(curr_var="string")
    def check_var_is_timestamp(curr_var, rhs_node): #curr_node):
        """
        This function is a common task in the analysis of variable declarations and assignments.
        """
        curr_fingerprint = get_fingerprint(curr_var, rhs_node)

#        if curr_var not in timestamp_variables:  
        if curr_fingerprint not in timestamp_fingerprints:
            (found_identifiers, found_method_calls) = analyze_assignment(rhs_node) #curr_node) 
 
            is_timestamp = len(found_identifiers & set(timestamp_variables.keys())) > 0 or \
                len(found_method_calls & now_methods) > 0

#            is_timestamp = (len(found_identifiers & timestamp_variables) > 0) or \
#                (len(found_method_calls & now_methods) > 0)
#            print "curr node: %s" % rhs_node
#            print "check is timestamp (identifiers=%s, timestamps=%s, method_calls=%s, now_methods=%s): %s" % (found_identifiers, timestamp_variables, found_method_calls, now_methods, is_timestamp)
   
            if is_timestamp:
#                timestamp_variables.add(curr_var)
                existing = timestamp_variables.get(curr_var, None)
                if existing is None:
                    existing = []
                existing.append(rhs_node)
                timestamp_variables[curr_var] = existing
                timestamp_fingerprints.append(curr_fingerprint)

    
    def handle_var_declaration(curr_node):
        assert "name" in curr_node, curr_node
        assert "nodeType" in curr_node["name"]
        assert curr_node["name"]["nodeType"] == "ASTIdentifier"
        assert "expr" in curr_node, curr_node

        curr_var = curr_node["name"]["value"]
        #print "declare variable: %s" % curr_var
        rhs_node = curr_node["expr"]

        check_var_is_timestamp(curr_var, rhs_node) #curr_node)

    def handle_var_assignment(curr_node):
        assert "nodeType" in curr_node
        assert curr_node["nodeType"] == "ASTAssignment"
        assert "left" in curr_node, curr_node
        assert "right" in curr_node, curr_node
        curr_var = None

        if curr_node["left"]["nodeType"] == "ASTIdentifier":
            curr_var = curr_node["left"]["value"]
            rhs_node = curr_node["right"]

            #print "lhs is variable: %s" % curr_var
            check_var_is_timestamp(curr_var, rhs_node) #curr_node)
    
    while True:
        """
        the following loop is guaranteed to terminate, because:
        - monotonicity: prev_timestamp_variables is contained or equal to timestamp_variables
        - upper-bounded: timestamp_variables can contain at most all variable assignments in the node
        """
        visitor = ASTVisitor(node)
        visitor.add_handler("ASTAssignment", handle_var_assignment)
        visitor.add_handler("ASTVariableDeclaration", handle_var_declaration)
        visitor.visit()

        # check that prev_timestamp_variables is contained or equal to timestamp_variables
        
#        assert prev_timestamp_variables & timestamp_variables == prev_timestamp_variables
        ts_vars = set(timestamp_variables.keys())
        prev_ts_vars = set(prev_timestamp_variables.keys())
        assert ts_vars & prev_ts_vars == prev_ts_vars

        is_upper_bound = (prev_ts_vars == ts_vars)
        if is_upper_bound:
            # if here we have discovered the same timestamp variables than before; in order to check
            # whether we reached the upper-bound of this loop, we have to see if the list of assignments
            # of each timestamp remained the same; if yes, we reached the upper-bound, otherwise we have
            # to continue looping
            for var in prev_ts_vars:
                if len(prev_timestamp_variables[var]) != len(timestamp_variables[var]):
                    # if here, we have discovered a new assignment to the existing timestamp: it's
                    # not the upper-bound of the procedure
                    is_upper_bound = False

        if is_upper_bound:
            break

        prev_timestamp_variables = timestamp_variables
        prev_timestamp_fingerprints = timestamp_fingerprints

        timestamp_variables = {}
        timestamp_fingerprints = []
    
    return timestamp_variables


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

    for curr in nodes:
        if curr["nodeType"] != "ASTMethodCall":
            res = False
            break

        called_method = "%s.%s" % (curr["classPointed"], curr["methodName"])
        if called_method not in now_methods:
            res = False     
            break
        
    return res
        
        
