from java2ta.abstraction.models import SymbolTable

def test_empty():

    SymbolTable.reset()
    assert len(SymbolTable.get_literals()) == 0

def test_non_encoded_literals():
    """
    The are literals that do not need encoding,  (because they can be directly 
    translated to SMT, e.g. 
    integers and booleans) and others that do need encoding because there is no 
    direct translation to SMT or we don't want to use the corresponding SMT 
    """

    SymbolTable.reset()
    m = len(SymbolTable.get_literals())
    code, type = SymbolTable.add_literal("3")
    assert len(SymbolTable.get_literals()) == m
    assert code == "3"
    assert type == "int"
    
    code, type = SymbolTable.add_literal("true")
    assert len(SymbolTable.get_literals()) == m
    assert code == "true"
    assert type == "bool"

def test_encoded_strings():

    SymbolTable.reset()
    m = len(SymbolTable.get_literals())
    code_1, type = SymbolTable.add_literal("'foo'")
    assert len(SymbolTable.get_literals()) == m + 1
    assert int(code_1) > 0
    assert type == "String"

    code_2, type = SymbolTable.add_literal("'foo'")
    assert len(SymbolTable.get_literals()) == m + 1
    assert int(code_2) > 0
    assert code_1 == code_2
    assert type == "String"


def test_normalise_string_delimiters():

    SymbolTable.reset()
    m = len(SymbolTable.get_literals())
    code_1, type_1 = SymbolTable.add_literal("'foo'")
    assert len(SymbolTable.get_literals()) == m + 1
    assert int(code_1) > 0
    assert type_1 == "String"

    # the second insertion of the same string (with different delimiters) is
    # identified as a replica of the first occurrence
    code_2, type_2 = SymbolTable.add_literal('"foo"')
    assert len(SymbolTable.get_literals()) == m + 1
    assert int(code_2) > 0
    assert (code_1, type_1) == (code_2, type_2)


