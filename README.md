# Type System

Currently we defined two different types:
* `Timestamp`
* `Duration`
* `Unknown`
* `Warning` -> Only used to return error

Based on these types, we define a set of rules to process Java-like expressions to infer those types from normal 
integer variables.

## Timestamp
The type `Timestamp` defines that a variable hold a time value that refers to a specific point in time.

## Duration
The type `Duration` is used to define that a variable holds a time value that specifies a quantum (amount) of time.

## Unknown
The type `Unkown` is used as base type for parameter. Analyzing the source code we can derive the correct type of it.

## Warning
The type `Warning` is used to stop the processing and return a warning to developers because they are doing smth wrong.

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
Every parameter is initially defined as `Unknown`. Analyzing the source code we can 
instantiate to its correct type.

These rules are correct under the assumption that developers do not hard-encode timestamp 
values in source code but only duration values. They rely on Java APIs to determine timestamps.


# Inductive Case

We use the following notation in the equations:
1. `Timestamp`: T 
2. `Duration`: D
2. `Warning`: W
We assume that each rule is composed of expression of which we have already resolved the type.
The proof of convergence it is a trivial proof on the size of expression.

The possible cases are (we exclude the symmetric cases):
* $`T + T \prec W`$
* $`T - T \prec D`$
* $`T \times T \prec W`$
* $`T \div T \prec W`$

* $`T + D \prec T`$
* $`T - D \prec T`$
* $`T \times D \prec W`$
* $`T \div D \prec W`$

* $`D + D \prec D`$
* $`D - D \prec D`$
* $`D \times D \prec D`$
* $`D \div D \prec D`$

* $`max(T,T) \prec T`$
* $`max(T,D) \prec W`$
* $`max(D,D) \prec D`$
* $`min(T,T) \prec T`$
* $`min(T,D) \prec W`$
* $`min(D,D) \prec D`$


Here the rules for instantiating the unknown. We can **NOT** assume the symmetric property but we assume it is always the correct operation:
 
* $`T + U       \Rightarrow U \prec D`$
* $`T - U       \Rightarrow U \prec D`$
* $`T \times U  \Rightarrow U \prec W`$
* $`T \div U    \Rightarrow U \prec W`$

* $`D + U       \Rightarrow U \prec T`$
* $`D - U       \Rightarrow U \prec D`$
* $`D \times U  \Rightarrow U \prec D`$
* $`D \div U    \Rightarrow U \prec D`$

* $`U + T       \Rightarrow U \prec D`$ 
* $`U - T       \Rightarrow U \prec T`$
* $`U \times T  \Rightarrow U \prec W`$
* $`U \div T    \Rightarrow U \prec W`$

* $`U + D       \Rightarrow U \prec U`$
* $`U - D       \Rightarrow U \prec U`$
* $`U \times D  \Rightarrow U \prec D`$
* $`U \div D    \Rightarrow U \prec D`$


* $`max(T,U) \Rightarrow U \prec T`$
* $`max(D,U) \Rightarrow U \prec D`$
* $`min(T,U) \Rightarrow U \prec T`$
* $`min(D,U) \Rightarrow U \prec D`$

Method Calls of **ET** methods [1], we say that the parameter type **MUST** be of type $`K`$ based on our manual analysis,
where $`K`$ is either $`T`$ or $`D`$.





# Reference
[1] Giovanni Liva, Muhammad Taimoor Khan, and Martin Pinzger. 2017. Extracting timed automata from Java methods. In Proceedings of the 17th International Working Conference on Source Code Analysis and Manipulation (SCAM). IEEE, 91–100.
[2] Liva, G., Khan, M.T., Spegni, F., Spalazzi, L., Bollin, A., Pinzger, M.: Modeling time in java programs for automatic error detection. In Proceedings of the IEEE/ACM Conference on Formal Methods in Software Engineering (FormaliSE 2018). IEEE Press (2018)