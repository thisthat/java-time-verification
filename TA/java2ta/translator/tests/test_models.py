from java2ta.translator.models import KnowledgeBase

def test_knowledge_base_is_timestamp():
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp", False)
    assert KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp")

    assert not KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "not_a_timestamp")


def test_knowledge_base_get_timestamps():
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp", False)
    KnowledgeBase.add_timestamp("foo.fie.SecondClassName", "anothermethod", "another_timestamp", False)

    assert KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp")
    assert KnowledgeBase.is_timestamp("foo.fie.SecondClassName", "anothermethod", "another_timestamp")

    assert not KnowledgeBase.is_timestamp("foo.fie.ClassName", "mymethod", "another_timestamp")
    assert not KnowledgeBase.is_timestamp("foo.fie.SecondClassName", "anothermethod", "var_timestamp")


    assert len(KnowledgeBase.get_timestamps("foo.fie.ClassName", "mymethod")) == 1
    assert len(KnowledgeBase.get_timestamps("foo.fie.SecondClassName", "anothermethod")) == 1


def test_kb_absolute_vs_relative():
    # one relative timestamp
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "var_timestamp", True)

    # two absolute timestamps
    KnowledgeBase.add_timestamp("foo.fie.SecondClassName", "anothermethod", "another_timestamp", False)
    KnowledgeBase.add_timestamp("foo.fie.SecondClassName", "anothermethod", "third_timestamp", False)


    anothermethod_absolute = KnowledgeBase.get_absolute_timestamps("foo.fie.SecondClassName", "anothermethod")
    mymethod_relative = KnowledgeBase.get_relative_timestamps("foo.fie.ClassName", "mymethod")
    assert len(anothermethod_absolute) == 2
    assert len(mymethod_relative) == 1

    assert len(KnowledgeBase.get_relative_timestamps("foo.fie.SecondClassName", "anothermethod")) == 0
    assert len(KnowledgeBase.get_absolute_timestamps("foo.fie.ClassName", "mymethod")) == 0

    assert "var_timestamp" in mymethod_relative
    assert "another_timestamp" in anothermethod_absolute
    assert "third_timestamp" in anothermethod_absolute

def test_kb_now_is_absolute():

    # one relative timestamp
    try:
        KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "now_timestamp", True, is_now=True)
        assert False, "Exception expected when setting a timestamp as 'now' and 'relative' at the same time"
    except Exception:
        # expected behavior: cannot be relative and now-timestamp at the same time
        assert True

    # one now timestamp
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "now_timestamp", False, is_now=True)

    # one absolute timestamp
    KnowledgeBase.add_timestamp("foo.fie.ClassName", "mymethod", "abs_timestamp", False, is_now=False)

    # two relative timestamps
    KnowledgeBase.add_timestamp("foo.fie.SecondClassName", "anothermethod", "another_timestamp", True)
    KnowledgeBase.add_timestamp("foo.fie.SecondClassName", "anothermethod", "third_timestamp", True)

    mymethod_absolute = KnowledgeBase.get_absolute_timestamps("foo.fie.ClassName", "mymethod")
    mymethod_now = KnowledgeBase.get_now_timestamps("foo.fie.ClassName", "mymethod")
    anothermethod_relative = KnowledgeBase.get_relative_timestamps("foo.fie.SecondClassName", "anothermethod")
    assert len(anothermethod_relative) == 2
    assert len(mymethod_absolute) == 2
    assert len(mymethod_now) == 1

    assert "now_timestamp" in mymethod_absolute
    assert "now_timestamp" in mymethod_now
    assert "abs_timestamp" in mymethod_absolute
    
    assert "another_timestamp" in anothermethod_relative
    assert "third_timestamp" in anothermethod_relative
