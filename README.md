# Type System

Currently we defined two different types:
* `Timestamp`
* `Duration`

Based on these types, we define a set of rules to process Java-like expressions to infer those types from normal 
integer variables.

## Timestamp
The type `Timestamp` defines that a variable hold a time value that refers to a specific point in time.

## Duration
The type `Duration` is used to define that a variable holds a time value that specifies a quantum (amount) of time.


# Type System

## Definitions

Every Java statement that alter a value of a variable is in the form:

$` variable_name = expression `$

We call this form **assignment** and every Java statements that alter a variable value is mapped in this form: *e.g.* assignment and variable initialization.
The LHS of an assignment is _**always**_ a variable name. In the RHS, we use the term **value** to refer to the value associated to every operand of the expression, 

We process the `expression` (RHS) following the Java semantics of expression resolution (recursively on subexpression) and we determine its time type. 
We then mark the `variable_name` with the time type of the expression.

## Base Case
We define how determine `Timestamp` value using the time semantics defined in [1].
When we encounter a method call to an **RT** method, we say it is a `Timestamp` value.
When in the analysis we encounter a scalar value, we mark it as `Duration` type.

These rules are correct under the assumption that developers do not hard-encode timestamp values in source code but only duration values.
They rely on Java APIs to determine timestamps.


# Inductive Case

The possible cases are (we exclude the symmetric cases):
* $``Timestamp` \bigodot `Timestamp` \prec `Timestamp``$
* $``Timestamp` \bigodot `Duration`  \prec `Timestamp``$


# Reference
[1] Giovanni Liva, Muhammad Taimoor Khan, and Martin Pinzger. 2017. Extracting timed automata from Java methods. In Proceedings of the 17th International Working Conference on Source Code Analysis and Manipulation (SCAM). IEEE, 91–100.
[2] Liva, G., Khan, M.T., Spegni, F., Spalazzi, L., Bollin, A., Pinzger, M.: Modeling time in java programs for automatic error detection. In Proceedings of the IEEE/ACM Conference on Formal Methods in Software Engineering (FormaliSE 2018). IEEE Press (2018)