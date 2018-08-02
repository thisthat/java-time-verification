
from java2ta.ir.models import Project, Thread, Klass, InnerKlass, Method, Variable, ASTVisitor
from java2ta.ir.shortcuts import check_is_open

import pkg_resources


def test_open_project():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    assert p.is_status("opening") or p.is_status("open")

    check_is_open(p)

def test_close_wrong_project():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("foofiedoesnotexist", "file:///%s" % test_proj_path, "localhost:9000")

    try:
        p.clean()
        assert False, "An exception was expected because it does not exist such a project"
    except Exception:
        # this was expected
        pass


def test_close_open_project():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    assert p.is_status("opening") or p.is_status("open")
   
    p.clean()

    assert p.is_status("closed")



def test_get_files():
 
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    files = p.get_files()

    assert len(files) == 1, "Expected list containing one file. Got: %s" % files

    assert files[0] == "HelloWorld.java", files



def test_get_mains():
 
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    p.open()

    check_is_open(p)

    mains = p.get_mains()

    assert len(mains) == 1, "Expected list containing one class with main method. Got: %s" % mains
    assert mains[0]["className"] == "HelloWorld", mains
    assert mains[0]["path"] == "HelloWorld.java", mains
    assert mains[0]["packageName"] == "", main


def test_open_project_bigger_concurrent():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
 
    files = p.get_files()
    assert len(files) == 51

    # test presence of some of the present files
    assert "Philosopher.java" in files
    assert "BoundedBuffer2.java" in files
    assert "LockTest.java" in files
    assert "LLSC.java" in files



def test_open_project_bigger_distributed():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "dist-progs")

    p = Project("dist-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)

    files = p.get_files()
    assert len(files) == 90

    # test presence of some of the files
    assert "ShortestPath.java" in files, files
    assert "ElectionTester.java" in files, files
    assert "SeqMessage.java" in files, files
    assert "StableMutexTester.java" in files, files
    assert "MsgList.java" in files, files
    assert "Lock.java" in files, files


def check_file(file):

    assert "code" in file
    assert "methods" in file
    assert "imports" in file
    assert "abstract" in file
    assert "staticInit" in file
    assert "accessRight" in file
    assert "start" in file
    assert "end" in file
    assert file["end"] >= file["start"]
    assert "interface" in file  
    assert "implmentsInterfaces" in file # TODO: fix name implementsInterfaces
    assert "lineEnd" in file
    assert "importsAsString" in file
    assert "nodeType" in file
    assert "parent" in file
    assert "realPackageName" in file
    assert "path" in file
    assert "line" in file
    assert "methods" in file, sorted(file.keys())
    assert "name" in file
    assert "constraint" in file
    assert "packageName" in file
    assert "extendClass" in file
    assert "timeCritical" in file
    assert "attributes" in file


def test_get_files_dist():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "dist-progs")

    p = Project("dist-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
 
    all_files_lock = p.get_file("Lock.java")

    assert isinstance(all_files_lock, list)

    file_lock = all_files_lock[0]

    check_file(file_lock)
    assert not file_lock["abstract"] 
    assert file_lock["interface"]
    assert file_lock["name"] == "Lock"
    assert file_lock["path"] ==  "Lock.java", file_lock
    assert len(file_lock["methods"]) == 2 # was: allMethods

    assert file_lock["methods"][0]["name"] in ["requestCS", "releaseCS"] # was: allMethods
    assert file_lock["methods"][1]["name"] in ["requestCS", "releaseCS"] # was: allMethods

    assert file_lock["implmentsInterfaces"] == [ "MsgHandler" ] # TODO fix name
    assert file_lock["extendClass"] == "Object"

    all_files_leader = p.get_file("RingLeader.java")
    assert isinstance(all_files_leader, list)
    file_leader = all_files_leader[0]
    check_file(file_leader)
    assert file_leader["implmentsInterfaces"] == [ "Election" ] # TODO fix name
    assert len(file_leader["attributes"]) == 4
    assert file_leader["attributes"][0]["name"] in [ "number", "leaderId", "next", "awake", ]
    assert file_leader["attributes"][1]["name"] in [ "number", "leaderId", "next", "awake", ]
    assert file_leader["attributes"][2]["name"] in [ "number", "leaderId", "next", "awake", ]
    assert file_leader["attributes"][3]["name"] in [ "number", "leaderId", "next", "awake", ]

    assert file_leader["extendClass"] == "Process"
    assert len(file_leader["methods"]) == 4 # was: allMethods
    assert file_leader["methods"][0]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"] # was: allMethods
    assert file_leader["methods"][1]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"] # was: allMethods
    assert file_leader["methods"][2]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"] # was: allMethods
    assert file_leader["methods"][3]["name"] in [ "startElection", "handleMsg", "getLeader", "RingLeader"] # was: allMethods



def test_get_files_by_type():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    all_files = p.get_files()
    assert len(all_files) >= 5, all_files

    all_classes = p.get_classes()
    assert len(all_classes) >= 5, all_classes

    files = p.get_files(type_fqn="Lock")

    assert len(files) == 5, len(files)
    
#    expected_classes = [ "SemaphoreLock", "Dekker", "Bakery", "HWMutex", "PetersonAlgorithm" ]
    expected_paths = [ "SemaphoreLock.java", "Dekker.java", "Bakery.java", "HWMutex.java", "PetersonAlgorithm.java" ]
    
    for f in files:
        assert f in expected_paths



def test_thread():
 
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    threads = p.get_threads() 

    assert len(threads) > 0, len(threads)

    for ir in threads:

        t = Thread(ir["className"], ir["packageName"], project=p) 

        assert t.name == ir["className"]
        assert t.path == "" 
        assert t.package_name == ir["packageName"]

        ast = t.ast
    
        assert isinstance(ast, dict), ast 
        assert ast["name"] == t.name
        assert len(t.path) > 0
        assert ast["path"] == t.path
        assert ast["packageName"] == t.package_name


def test_methods():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")

    p = Project("conc-progs", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    t = Thread("MyThread","",project=p) #name=FooBar, package=.Foo
    run_method = Method("run", t)

    assert isinstance(run_method.ast, dict), "Every thread is supposed to have a run() method"

    assert "name" in run_method.ast
    assert "parameters" in run_method.ast
    assert "returnType" in run_method.ast, run_method.ast.keys()
    assert "code" in run_method.ast
    assert "stms" in run_method.ast
    assert "abstract" in run_method.ast
    assert "static" in run_method.ast
    assert "signature" in run_method.ast
    assert "exceptionsThrowed" in run_method.ast
    assert "synchronized" in run_method.ast, run_method.ast.keys()
    assert "declaredVar" in run_method.ast

    # the ast contains only a While, which is not a node of type ASTRE, thus it has no "env"
    assert run_method.ast["stms"][0]["nodeType"] == "ASTWhile"
    assert "env" not in run_method.ast["stms"][0] 

    # the expression of the while, though, is a node of type ASTRE and has an "env"
    assert run_method.ast["stms"][0]["expr"]["nodeType"] == "ASTRE"
    assert "env" in run_method.ast["stms"][0]["expr"]

    while_env = run_method.ast["stms"][0]["expr"]["env"]

    assert len(while_env) == 3 
    assert while_env[0]["name"] == "myId", while_env
    assert while_env[0]["type"] == "int"
    assert while_env[1]["name"] == "lock"
    assert while_env[1]["type"] == "Lock"
    assert while_env[2]["name"] == "r"
    assert while_env[2]["type"] == "java.util.Random", while_env 



def test_declared_vars_simple():
    """
    Access the BCell class in BCell.java file (project 'conc-progs') and check that there   
    are three methods, two of which without a declared variable, and one with a declared integer variable.
    """
    p_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")
    p = Project("conc-progs", "file://%s" % p_path, "localhost:9000")
    p.open()
    check_is_open(p)
    c = p.get_class("BCell", "BCell.java")
    assert isinstance(c, dict)
    
    assert c["name"] == "BCell"
    assert c["path" ] == "BCell.java"
    
    assert isinstance(c["methods"], list)
    assert len(c["methods"]) == 3
    
    # beware: in principle the list of methods could be rearranged, and this should not affect the code;
    # atm tests rely on the fact that methods are exported in the same order as they are declared within the class
    assert c["methods"][0]["name"] == "getValue"
    assert c["methods"][0]["declaredVar"] == [] # no var declared in the method getValue
    
    assert c["methods"][1]["name"] == "setValue"
    assert c["methods"][1]["declaredVar"] == [] # no var declared in the method setValue
    
    assert c["methods"][2]["name"] == "swap"
    decvars = c["methods"][2]["declaredVar"]
    assert len(decvars) == 1 # 1 var declared in the method swap
    assert decvars[0]["name"] == "temp" # var name is temp
    assert decvars[0]["type"] == "int" # var type is int
    


def test_declared_vars_complex():

    p_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")
    p = Project("conc-progs", "file://%s" % p_path, "localhost:9000")
    p.open()
    check_is_open(p)
    c = p.get_class("ConcQueue", "ConcQueue.java")
    assert isinstance(c, dict)
    
    assert c["name"] == "ConcQueue"
    assert c["path" ] == "ConcQueue.java", c["path"]
    
    assert isinstance(c["methods"], list)
    assert len(c["methods"]) == 3

    assert c["methods"][0]["name"] == "ConcQueue"
    assert c["methods"][0]["declaredVar"] == []

    assert c["methods"][1]["name"] == "Enqueue"
    dec_vars_enqueue = c["methods"][1]["declaredVar"]
    assert len(dec_vars_enqueue) == 3

    assert dec_vars_enqueue[0]["name"] == "ltail"
    assert dec_vars_enqueue[1]["name"] == "lnext"
    assert dec_vars_enqueue[2]["name"] == "node"

    assert c["methods"][2]["name"] == "Dequeue"
    dec_vars_dequeue = c["methods"][2]["declaredVar"]
    assert len(dec_vars_dequeue) == 4, dec_vars_dequeue

    assert dec_vars_dequeue[0]["name"] == "ltail"
    assert dec_vars_dequeue[1]["name"] == "lhead"
    assert dec_vars_dequeue[2]["name"] == "lnext"
    assert dec_vars_dequeue[3]["name"] == "return_val"



def test_statement_env_simple():
    p_path = pkg_resources.resource_filename("java2ta.ir.tests", "conc-progs")
    p = Project("conc-progs", "file://%s" % p_path, "localhost:9000")
    p.open()
    check_is_open(p)
    c = p.get_class("BCell", "BCell.java")
    assert isinstance(c, dict)
    
    assert c["name"] == "BCell"
    assert c["path"] == "BCell.java"
    
    assert isinstance(c["methods"], list)
    assert len(c["methods"]) == 3
    
    # beware: in principle the list of methods could be rearranged, and this should not affect the code;
    # atm tests rely on the fact that methods are exported in the same order as they are declared within the class

    # the getValue and setValue methods can only see the class attribute named "value" of type "int"
    assert c["methods"][0]["name"] == "getValue"
    assert len(c["methods"][0]["stms"]) > 0
    for smt in c["methods"][0]["stms"]:
        assert smt["nodeType"] != "ASTRE"
   
    assert c["methods"][1]["name"] == "setValue"
    assert len(c["methods"][1]["stms"]) > 0
    for smt in c["methods"][1]["stms"]:
        assert smt["nodeType"] != "ASTRE" or "env" in smt
        if "env" in smt:
            assert len(smt["env"]) == 2, "Expected env of size 2. Received: %s" % smt["env"]
            for var in smt["env"]:
                assert var["name"] in ["i","value"]
                assert var["type"] == "int"
    
    assert c["methods"][2]["name"] == "swap"
    assert len(c["methods"][2]["stms"]) > 0
    for smt in c["methods"][2]["stms"]:
        assert smt["nodeType"] != "ASTRE" or "env" in smt
        if "env" in smt:     
            assert len(smt["env"]) == 3
            for var in smt["env"]:
                assert var["name"] in ["temp","x","value"]
                assert var["type"] in ["BCell","int"]
                assert var["type"] != "BCell" or var["name"] == "x"
                assert var["type"] != "int" or var["name"] in ["temp","value"]

def test_classes():
    
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    classes = p.get_classes() 

    assert len(list(classes)) > 0, classes

    for c in classes:
        assert "className" in c
        assert "packageName" in c
        assert "path" in c
        class_path = "file://%s" % c["path"]
        klass = Klass(c["className"], c["packageName"], class_path, p)

        ast = klass.ast

        assert isinstance(ast, dict)
        assert "name" in ast
        assert "packageName" in ast
        assert ast["name"] == c["className"]
        assert ast["packageName"] == c["packageName"]
   
def test_variable():
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    c = Klass("HelloWorld","", "HelloWorld.java",project=p)
    m = Method("fie", c)

    # maxseq is a local variable of the method "doSwap"
    v = Variable("varfoo", m)
    assert v.fqname == "%s.%s" % (m.fqname, "varfoo"), "variable: %s, method: %s" % (v.fqname, m.fqname)

    var_ast = v.ast
    assert isinstance(var_ast, dict)
    assert "name" in var_ast, var_ast
    assert "type" in var_ast, var_ast

    assert isinstance(var_ast["name"], dict)
    assert "value" in var_ast["name"]
    assert var_ast["name"]["value"] == "varfoo", var_ast
    assert var_ast["type"] == "Foo", var_ast
    assert var_ast["typePointed"] == "Foo", var_ast 


def test_variable_with_inner_class():
 
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    c = Klass("HelloWorld","", "HelloWorld.java",project=p)
    m = Method("fie", c)

    # maxseq is a local variable of the method "doSwap"
    v = Variable("varfoo", m)
    assert v.fqname == "%s.%s" % (m.fqname, v.name), "variable: %s, method: %s" % (v.fqname, m.fqname)

    var_ast = v.ast
    assert isinstance(var_ast, dict)
    assert "name" in var_ast, var_ast
    assert "type" in var_ast, var_ast

    assert isinstance(var_ast["name"], dict)
    assert "value" in var_ast["name"]
    assert var_ast["name"]["value"] == "varfoo", var_ast
    assert var_ast["name"]["nodeType"] == "ASTIdentifier", var_ast 
    assert var_ast["type"] == "Foo", var_ast
    assert var_ast["typePointed"] == "Foo", var_ast 

    assert var_ast["nodeType"] == "ASTVariableDeclaration"
    assert "expr" in var_ast
    assert var_ast["expr"]["nodeType"] == "ASTNewObject"
    assert "hiddenClass" in var_ast["expr"]
    assert "methods" in var_ast["expr"]["hiddenClass"]
    assert len(var_ast["expr"]["hiddenClass"]["methods"]) == 1 # only the overridden/implemented methods are given
    assert "accessRight" in var_ast["expr"]["hiddenClass"]
    assert var_ast["expr"]["hiddenClass"]["accessRight"] == "HIDDEN"



def test_inner_method():

    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    c = Klass("HelloWorld","", "HelloWorld.java",project=p)
    m = Method("fie", c)

    # maxseq is a local variable of the method "doSwap"
    v = Variable("varfoo", m)
 
    c_inner = InnerKlass(v)
    m_inner = Method("mymethod", c_inner)

    assert m_inner.ast is not None
    assert m_inner.ast["nodeType"] == "ASTMethod"
    assert "stms" in m_inner.ast
    assert m_inner.ast["name"] == "mymethod"
    assert len(m_inner.ast["stms"]) == 2, "Expected 2 statements. Got: %s - %s" % (len(m_inner.ast["stms"]),)
    assert m_inner.ast["stms"][0]["nodeType"] == "ASTRE", m_inner.ast["stms"][0]["nodeType"]
    assert m_inner.ast["stms"][1]["nodeType"] == "ASTReturn"

def test_ast_visitor_simple():
    """
    just test that we can iterate the AST of a class, without giving any handler
    """
    
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    classes = p.get_classes() 

    assert len(list(classes)) > 0, classes

    klass = Klass("HelloWorld", "", "file://HelloWorld.java", p)
 
    visitor = ASTVisitor(klass)
    visitor.visit()

def test_ast_visitor_one_handler():
    """
    test we can visit a class ast specifying a single handler
    """
    
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    classes = p.get_classes() 

    assert len(list(classes)) > 0, classes

    klass = Klass("HelloWorld", "", "file://HelloWorld.java", p)
 
    identifier_names = set([])
    def collect_identifier_names(node):
        assert "value" in node
        identifier_names.add(node["value"])
    
    visitor = ASTVisitor(klass)
    visitor.add_handler("ASTIdentifier", collect_identifier_names)
    visitor.visit()

    assert identifier_names == set([ "i", "varfoo", "System", "a", "b", "c", "Thread", "w", "x", "y", ]), identifier_names

def test_ast_visitor_more_handlers():
    """
    test we can visit a class ast specifying more than one handler
    """
    
    test_proj_path = pkg_resources.resource_filename("java2ta.ir.tests", "helloworld")

    p = Project("helloworld", "file://%s" % test_proj_path, "localhost:9000")

    assert p.is_status("closed")
    p.open()

    check_is_open(p)
  
    classes = p.get_classes() 

    assert len(list(classes)) > 0, classes

    klass = Klass("HelloWorld", "", "file://HelloWorld.java", p)
 
    var_declarations = set([])
    def collect_variable_declarations(node):    
        assert "name" in node, node
        assert "code" in node["name"], node["name"]
        assert "type" in node, node
        var_declarations.add((node["name"]["code"],node["type"]))
    
    var_assignments = set([])
    def collect_variable_assignments(node):
        assert "left" in node, node
        assert "nodeType" in node["left"], node["left"]
        lhs_node_type = node["left"]["nodeType"]

        if lhs_node_type == "ASTIdentifier":
            var_assignments.add((node["left"]["code"], node["line"], node["start"], node["end"]))
    
    visitor = ASTVisitor(klass)
    visitor.add_handler("ASTVariableDeclaration", collect_variable_declarations)
    visitor.add_handler("ASTAssignment", collect_variable_assignments)
    visitor.visit()

    assert len(var_declarations) == 5, var_declarations
    assert ("varfoo", "Foo") in var_declarations, var_declarations
    assert ("i", "int") in var_declarations, var_declarations
    assert ("a", "int") in var_declarations
    assert ("b", "int") in var_declarations
    assert ("c", "int") in var_declarations

    assert len(var_assignments) == 4, var_assignments
    assert ("x", 23, 493, 502) in var_assignments
    assert ("y", 24, 512, 521) in var_assignments
    assert ("b", 48, 975, 1005) in var_assignments
    assert ("i", 32, 701, 710) in var_assignments


