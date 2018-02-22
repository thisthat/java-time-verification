from java2ta.translator.models import KnowledgeBase

def test_knowledge_base_is_timestamp():
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp")
    assert KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp")

    assert not KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "not_a_timestamp")


def test_knowledge_base_get_timestamps():
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp")
    KnowledgeBase.add_timestamp("foo.fie.SecondClassName", "anothermethod", "another_timestamp")

    assert KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp")
    assert KnowledgeBase.is_timestamp("foo.fie.SecondClassName", "anothermethod", "another_timestamp")

    assert not KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "another_timestamp")
    assert not KnowledgeBase.is_timestamp("foo.fie.SecondClassName", "anothermethod", "var_timestamp")


    assert len(KnowledgeBase.get_timestamps("foo.fie.ClassName", "mymethod")) == 1
    assert len(KnowledgeBase.get_timestamps("foo.fie.SecondClassName", "anothermethod")) == 1
