/*
 * [The "BSD license"]
 *  Copyright (c) 2014 Terence Parr
 *  Copyright (c) 2014 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * A Java 8 parser.grammar for ANTLR 4 derived from the Java Language Specification
 * chapter 19.
 *
 * NOTE: This parser.grammar results in a generated parser that is much slower
 *       than the Java 7 parser.grammar in the grammars-v4/java directory. This
 *     one is, however, extremely close to the spec.
 *
 * You can test with
 *
 *  $ antlr4 Java8.g4
 *  $ javac *.java
 *  $ grun Java8 compilationUnit *.java
 *
 * Or,
~/antlr/code/grammars-v4/java8 $ java Test .
/Users/parrt/antlr/code/grammars-v4/java8/./Java8BaseListener.java
/Users/parrt/antlr/code/grammars-v4/java8/./Java8Lexer.java
/Users/parrt/antlr/code/grammars-v4/java8/./Java8Listener.java
/Users/parrt/antlr/code/grammars-v4/java8/./Java8Parser.java
/Users/parrt/antlr/code/grammars-v4/java8/./Test.java
Total lexer+parser time 30844ms.
 */

grammar Java8CommentSupported;

/*
 * Productions from §3 (Lexical Structure)
 */

literal
	:	IntegerLiteral
	|	FloatingPointLiteral
	|	BooleanLiteral
	|	CharacterLiteral
	|	StringLiteral
	|	NullLiteral
	;

/*
 * Productions from §4 (Types, Values, and Variables)
 */

type
	:	primitiveType
	|	referenceType
	;

primitiveType
	:	annotation* numericType
	|	annotation* 'boolean'
	;

numericType
	:	integralType
	|	floatingPointType
	;

integralType
	:	'byte'
	|	'short'
	|	'int'
	|	'long'
	|	'char'
	;

floatingPointType
	:	'float'
	|	'double'
	;

referenceType
	:	classOrInterfaceType
	|	typeVariable
	|	arrayType
	;

classOrInterfaceType
	:	(	classType_lfno_classOrInterfaceType
		|	interfaceType_lfno_classOrInterfaceType
		)
		(	classType_lf_classOrInterfaceType
		|	interfaceType_lf_classOrInterfaceType
		)*
	;

classType
	:	comment* annotation* comment* Identifier comment* typeArguments? comment*
	|	comment* classOrInterfaceType comment* '.' comment* annotation* comment* Identifier comment* typeArguments? comment*
	;

classType_lf_classOrInterfaceType
	:	'.' annotation* Identifier typeArguments?
	;

classType_lfno_classOrInterfaceType
	:	annotation* Identifier typeArguments?
	;

interfaceType
	:	classType
	;

interfaceType_lf_classOrInterfaceType
	:	classType_lf_classOrInterfaceType
	;

interfaceType_lfno_classOrInterfaceType
	:	classType_lfno_classOrInterfaceType
	;

typeVariable
	:	annotation* Identifier
	;

arrayType
	:	primitiveType dims
	|	classOrInterfaceType dims
	|	typeVariable dims
	;

dims
	:	annotation* '[' ']' (annotation* '[' ']')*
	;

typeParameter
	:	typeParameterModifier* Identifier typeBound?
	;

typeParameterModifier
	:	annotation
	;

typeBound
	:	'extends' typeVariable
	|	'extends' classOrInterfaceType additionalBound*
	;

additionalBound
	:	'&' interfaceType
	;

typeArguments
	:	'<' typeArgumentList '>'
	;

typeArgumentList
	:	typeArgument (',' typeArgument)*
	;

typeArgument
	:	referenceType
	|	wildcard
	;

wildcard
	:	annotation* '?' wildcardBounds?
	;

wildcardBounds
	:	'extends' referenceType
	|	'super' referenceType
	;

/*
 * Productions from §6 (Names)
 */

packageName
	:	Identifier
	|	packageName '.' Identifier
	;

typeName
	:	Identifier
	|	packageOrTypeName '.' Identifier
	;

packageOrTypeName
	:	Identifier
	|	packageOrTypeName '.' Identifier
	;

expressionName
	:	Identifier
	|	ambiguousName '.' Identifier
	;

methodName
	:	Identifier
	;

ambiguousName
	:	Identifier
	|	ambiguousName '.' Identifier
	;

/*
 * Productions from §7 (Packages)
 */

compilationUnit
	:	comment* packageDeclaration? comment* importDeclaration* typeDeclaration* comment* EOF
	;

packageDeclaration
	:	packageModifier* 'package' Identifier ('.' Identifier)* ';'
	;

packageModifier
	:	annotation
	;

importDeclaration
	:	singleTypeImportDeclaration comment*
	|	typeImportOnDemandDeclaration comment*
	|	singleStaticImportDeclaration comment*
	|	staticImportOnDemandDeclaration comment*
	;

singleTypeImportDeclaration
	:	'import' typeName ';'
	;

typeImportOnDemandDeclaration
	:	'import' packageOrTypeName '.' '*' ';'
	;

singleStaticImportDeclaration
	:	'import' 'static' typeName '.' Identifier ';'
	;

staticImportOnDemandDeclaration
	:	'import' 'static' typeName '.' '*' ';'
	;

typeDeclaration
	:	classDeclaration comment*
	|	interfaceDeclaration comment*
	|	';'
	;

/*
 * Productions from §8 (Classes)
 */

classDeclaration
	:	normalClassDeclaration
	|	enumDeclaration
	;

normalClassDeclaration
	:	comment* classModifier* comment* 'class' comment*  Identifier comment*  typeParameters? comment* superclass? comment* superinterfaces? comment* classBody comment*
	;

classModifier
	:	annotation
	|	'public'
	|	'protected'
	|	'private'
	|	'abstract'
	|	'static'
	|	'final'
	|	'strictfp'
	;

typeParameters
	:	'<' comment* typeParameterList '>'
	;

typeParameterList
	:	typeParameter  comment* (',' comment*  typeParameter comment* )*
	;

superclass
	:	'extends' comment* classType comment*
	;

superinterfaces
	:	'implements' comment* interfaceTypeList
	;

interfaceTypeList
	:	interfaceType comment* (','  comment* interfaceType  comment* )*
	;

classBody
	:	'{' comment* classBodyDeclaration* comment* '}'
	;

classBodyDeclaration
	:	classMemberDeclaration
	|	comment* instanceInitializer comment*
	|	comment* staticInitializer comment*
	|	comment* constructorDeclaration comment*
	;

classMemberDeclaration
	:	comment* fieldDeclaration comment*
	|	comment* methodDeclaration comment*
	|	comment* classDeclaration comment*
	|	comment* interfaceDeclaration comment*
	|	';'
	;

fieldDeclaration
	:	fieldModifier* unannType variableDeclaratorList ';'
	;

fieldModifier
	:	annotation
	|	'public'
	|	'protected'
	|	'private'
	|	'static'
	|	'final'
	|	'transient'
	|	'volatile'
	;

variableDeclaratorList
	:	variableDeclarator comment* (','  comment* variableDeclarator comment* )*
	;

variableDeclarator
	:	variableDeclaratorId  comment* ('='  comment* variableInitializer comment* )?
	;

variableDeclaratorId
	:	Identifier  comment* dims?
	;

variableInitializer
	:	expression
	|	arrayInitializer
	;

unannType
	:	unannPrimitiveType
	|	unannReferenceType
	;

unannPrimitiveType
	:	numericType
	|	'boolean'
	;

unannReferenceType
	:	unannClassOrInterfaceType
	|	unannTypeVariable
	|	unannArrayType
	;

unannClassOrInterfaceType
	:	(	unannClassType_lfno_unannClassOrInterfaceType
		|	unannInterfaceType_lfno_unannClassOrInterfaceType
		)
		(	unannClassType_lf_unannClassOrInterfaceType
		|	unannInterfaceType_lf_unannClassOrInterfaceType
		)*
	;

unannClassType
	:	Identifier typeArguments?
	|	unannClassOrInterfaceType '.' annotation* Identifier typeArguments?
	;

unannClassType_lf_unannClassOrInterfaceType
	:	'.' annotation* Identifier typeArguments?
	;

unannClassType_lfno_unannClassOrInterfaceType
	:	Identifier typeArguments?
	;

unannInterfaceType
	:	unannClassType
	;

unannInterfaceType_lf_unannClassOrInterfaceType
	:	unannClassType_lf_unannClassOrInterfaceType
	;

unannInterfaceType_lfno_unannClassOrInterfaceType
	:	unannClassType_lfno_unannClassOrInterfaceType
	;

unannTypeVariable
	:	Identifier
	;

unannArrayType
	:	unannPrimitiveType dims
	|	unannClassOrInterfaceType dims
	|	unannTypeVariable dims
	;

methodDeclaration
	:	comment* methodModifier* methodHeader methodBody comment*
	;

methodModifier
	:	annotation
	|	'public'
	|	'protected'
	|	'private'
	|	'abstract'
	|	'static'
	|	'final'
	|	'synchronized'
	|	'native'
	|	'strictfp'
	;

methodHeader
	:	result  comment* methodDeclarator  comment* throws_? comment*
	|	typeParameters  comment* annotation*  comment* result  comment* methodDeclarator comment*  throws_? comment*
	;

result
	:	unannType
	|	'void'
	;

methodDeclarator
	:	Identifier '(' formalParameterList? ')' dims?
	;

formalParameterList
	:	formalParameters ',' lastFormalParameter
	|	lastFormalParameter
	;

formalParameters
	:	formalParameter (',' formalParameter)*
	|	receiverParameter (',' formalParameter)*
	;

formalParameter
	:	comment* variableModifier* comment* unannType comment* variableDeclaratorId comment*
	;

variableModifier
	:	annotation
	|	'final'
	;

lastFormalParameter
	:	threeDotParameter
	|	formalParameter
	;

threeDotParameter: comment* variableModifier* comment* unannType comment* annotation* '...' comment* variableDeclaratorId comment* ;

receiverParameter
	:	annotation* unannType (Identifier '.')? 'this'
	;

throws_
	:	comment* 'throws' comment* exceptionTypeList comment*
	;

exceptionTypeList
	:	exceptionType (',' exceptionType)*
	;

exceptionType
	:	classType
	|	typeVariable
	;

methodBody
	:	block
	|	';'
	;

instanceInitializer
	:	block
	;

staticInitializer
	:	'static' block
	;

constructorDeclaration
	:	constructorModifier*  comment* constructorDeclarator  comment* throws_?  comment* constructorBody
	;

constructorModifier
	:	annotation
	|	'public'
	|	'protected'
	|	'private'
	;

constructorDeclarator
	:	typeParameters? simpleTypeName '(' formalParameterList? ')'
	;

simpleTypeName
	:	Identifier
	;

constructorBody
	:	'{' comment* explicitConstructorInvocation? comment* blockStatements? comment* '}'
	;

explicitConstructorInvocation
	:	typeArguments? comment*  'this'  comment* '('  comment* argumentList?  comment* ')'  comment* ';'
	|	typeArguments?  comment* 'super'  comment* '(' comment* argumentList?  comment* ')'  comment* ';'
	|	expressionName '.' comment* typeArguments?  comment* 'super' comment*  '('  comment* argumentList?  comment* ')'  comment* ';'
	|	primary '.' comment* typeArguments? 'super'  comment* '('  comment* argumentList? comment*  ')'  comment* ';'
	;

enumDeclaration
	:	classModifier* comment* 'enum' comment* Identifier comment* superinterfaces? comment* enumBody
	;

enumBody
	:	'{' comment* enumConstantList? ','?  comment* enumBodyDeclarations? comment* '}' comment*
	;

enumConstantList
	:	enumConstant comment* (',' comment* enumConstant comment* )*
	;

enumConstant
	:	enumConstantModifier* comment* Identifier comment* ('(' comment* argumentList? comment* ')')? comment* classBody? comment*
	;

enumConstantModifier
	:	annotation
	;

enumBodyDeclarations
	:	';' classBodyDeclaration*
	;

/*
 * Productions from §9 (Interfaces)
 */

interfaceDeclaration
	:	normalInterfaceDeclaration
	|	annotationTypeDeclaration
	;

normalInterfaceDeclaration
	:	comment* interfaceModifier* 'interface' comment* Identifier comment* typeParameters? comment* extendsInterfaces? comment* interfaceBody comment*
	;

interfaceModifier
	:	annotation
	|	'public'
	|	'protected'
	|	'private'
	|	'abstract'
	|	'static'
	|	'strictfp'
	;

extendsInterfaces
	:	'extends' interfaceTypeList
	;

interfaceBody
	:	'{'  comment* interfaceMemberDeclaration*  comment* '}'
	;

interfaceMemberDeclaration
	:	comment* constantDeclaration comment*
	|	comment* interfaceMethodDeclaration comment*
	|	comment* classDeclaration comment*
	|	comment* interfaceDeclaration comment*
	|	';'
	;

constantDeclaration
	:	constantModifier* comment* unannType comment* variableDeclaratorList comment* ';'
	;

constantModifier
	:	annotation
	|	'public'
	|	'static'
	|	'final'
	;

interfaceMethodDeclaration
	:	interfaceMethodModifier* comment* methodHeader comment* methodBody
	;

interfaceMethodModifier
	:	annotation
	|	'public'
	|	'abstract'
	|	'default'
	|	'static'
	|	'strictfp'
	;

annotationTypeDeclaration
	:	interfaceModifier* comment* '@' 'interface' comment* Identifier comment* annotationTypeBody
	;

annotationTypeBody
	:	'{' comment* annotationTypeMemberDeclaration* comment* '}'
	;

annotationTypeMemberDeclaration
	:	annotationTypeElementDeclaration
	|	constantDeclaration
	|	classDeclaration
	|	interfaceDeclaration
	|	';'
	;

annotationTypeElementDeclaration
	:	annotationTypeElementModifier*  comment* unannType comment*  Identifier comment*  '('  comment* ')' comment*  dims?  comment* defaultValue?  comment* ';'
	;

annotationTypeElementModifier
	:	annotation
	|	'public'
	|	'abstract'
	;

defaultValue
	:	'default' elementValue
	;

annotation
	:	normalAnnotation comment*
	|	markerAnnotation comment*
	|	singleElementAnnotation comment*
	;

normalAnnotation
	:	'@' typeName comment* '(' comment* elementValuePairList? comment*  ')'
	;

elementValuePairList
	:	elementValuePair (',' elementValuePair)*
	;

elementValuePair
	:	Identifier '=' elementValue
	;

elementValue
	:	conditionalExpression
	|	elementValueArrayInitializer
	|	annotation
	;

elementValueArrayInitializer
	:	 comment* '{'  comment* elementValueList?  comment* ','?  comment* '}'  comment*
	;

elementValueList
	:	 comment* elementValue  comment* (',' comment* elementValue comment* )*
	;

markerAnnotation
	:	'@' typeName
	;

singleElementAnnotation
	:	'@' typeName comment* '(' comment* elementValue comment* ')'
	;

/*
 * Productions from §10 (Arrays)
 */

arrayInitializer
	:	 comment* '{' comment* variableInitializerList? comment* ','?  comment* '}' comment*
	;

variableInitializerList
	:	 comment* variableInitializer comment* (',' variableInitializer comment* )* comment*
	;

/*
 * Productions from §14 (Blocks and Statements)
 */

block
	:	comment* '{'  comment* blockStatements?  comment* '}' comment*
	;

blockStatements
	:	blockStatement blockStatement*
	;

blockStatement
	:	localVariableDeclarationStatement  comment*
	|	classDeclaration  comment*
	|	statement
	;

localVariableDeclarationStatement
	:	localVariableDeclaration ';'
	;

localVariableDeclaration
	:	variableModifier* unannType variableDeclaratorList
	;

statement
	:	comment* statementWithoutTrailingSubstatement  comment*
	|	comment* labeledStatement  comment*
	|	comment* ifThenStatement  comment*
	|	comment* ifThenElseStatement  comment*
	|	comment* whileStatement  comment*
	|	comment* forStatement  comment*
	;

statementNoShortIf
	:	statementWithoutTrailingSubstatement comment*
	|	labeledStatementNoShortIf comment*
	|	ifThenElseStatementNoShortIf comment*
	|	whileStatementNoShortIf comment*
	|	forStatementNoShortIf comment*
	;

statementWithoutTrailingSubstatement
	:	block
	|	emptyStatement
	|	expressionStatement
	|	assertStatement  comment*
	|	switchStatement
	|	doStatement
	|	breakStatement
	|	continueStatement
	|	returnStatement
	|	synchronizedStatement
	|	throwStatement
	|	tryStatement
	;

emptyStatement
	:	';'
	;

labeledStatement
	:	Identifier ':' statement
	;

labeledStatementNoShortIf
	:	Identifier ':' statementNoShortIf
	;

expressionStatement
	:	comment* statementExpression ';'
	;

statementExpression
	:	assignment
	|	preIncrementExpression
	|	preDecrementExpression
	|	postIncrementExpression
	|	postDecrementExpression
	|	methodInvocation
	|	classInstanceCreationExpression
	;

ifThenStatement
	:	'if'  comment* '('  comment* expression  comment* ')'  comment* statement
	;

ifThenElseStatement
	:	'if'  comment* '('  comment* expression  comment* ')' comment* statementNoShortIf comment* 'else' comment* statement
	;

ifThenElseStatementNoShortIf
	:	'if'  comment* '('  comment* expression comment*  ')' comment* statementNoShortIf comment* 'else' comment* statementNoShortIf
	;

assertStatement
	:	'assert' expression ';'
	|	'assert' expression ':' expression ';'
	;

switchStatement
	:	'switch'  comment* '(' comment*  expression  comment* ')'  comment* switchBlock
	;

switchBlock
	:	'{'  comment* switchBlockStatementGroup*  comment* switchLabel*  comment* '}'
	;

switchBlockStatementGroup
	:	switchLabels  comment* blockStatements
	;

switchLabels
	:	switchLabel  comment* switchLabel*
	;

switchLabel
	:	'case'  comment* constantExpression  comment* ':' comment*
	|	'case'  comment* enumConstantName  comment* ':' comment*
	|	'default' comment* ':' comment*
	;

enumConstantName
	:	Identifier
	;

whileStatement
	:	'while'  comment* '('  comment* expression comment*  ')'  comment* statement
	;

whileStatementNoShortIf
	:	'while'  comment* '('  comment* expression  comment* ')'  comment* statementNoShortIf
	;

doStatement
	:	'do'  comment* statement comment* 'while'  comment* '('  comment* expression  comment* ')'  comment* ';'
	;

forStatement
	:	basicForStatement
	|	enhancedForStatement
	;

forStatementNoShortIf
	:	basicForStatementNoShortIf
	|	enhancedForStatementNoShortIf
	;

basicForStatement
	:	'for'  comment* '('  comment* forInit?  comment* ';'  comment* expression?  comment* ';' comment*  forUpdate?  comment* ')' comment*  statement
	;

basicForStatementNoShortIf
	:	'for'  comment* '(' forInit? comment*  ';'  comment* expression?  comment* ';'  comment* forUpdate? comment*  ')'  comment* statementNoShortIf
	;

forInit
	:	statementExpressionList
	|	localVariableDeclaration
	;

forUpdate
	:	statementExpressionList
	;

statementExpressionList
	:	statementExpression comment*  (',' comment*  statementExpression comment* )*
	;

enhancedForStatement
	:	'for'  comment* '(' comment* variableModifier*  comment* unannType  comment* variableDeclaratorId  comment* ':'  comment* expression  comment* ')'  comment* statement
	;

enhancedForStatementNoShortIf
	:	'for' comment*  '('  comment* variableModifier*  comment* unannType  comment* variableDeclaratorId  comment* ':'  comment* expression comment*  ')' comment*  statementNoShortIf
	;

breakStatement
	:	'break'  comment* Identifier? comment*  ';'
	;

continueStatement
	:	'continue'  comment* Identifier?  comment* ';'
	;

returnStatement
	:	'return' comment*  expression?  comment* ';'
	;

throwStatement
	:	'throw'  comment* expression  comment* ';'
	;

synchronizedStatement
	:	'synchronized'  comment* '('  comment* expression  comment* ')'  comment* block
	;

tryStatement
	:	'try'  comment* block  comment* catches
	|	'try'  comment* block  comment* catches? comment*  finally_
	|	tryWithResourcesStatement
	;

catches
	:	catchClause catchClause*
	;

catchClause
	:	'catch' comment*  '(' comment*  catchFormalParameter  comment* ')'  comment* block
	;

catchFormalParameter
	:	variableModifier*  comment* catchType  comment* variableDeclaratorId
	;

catchType
	:	unannClassType  comment* ('|'  comment* classType  comment* )*
	;

finally_
	:	'finally'  comment* block
	;

tryWithResourcesStatement
	:	'try'  comment* resourceSpecification  comment* block  comment* catches?  comment* finally_?
	;

resourceSpecification
	:	'(' comment*  resourceList  comment* ';'? ')'
	;

resourceList
	:	resource  comment* (';'  comment* resource  comment* )*
	;

resource
	:	variableModifier* comment*  unannType  comment* variableDeclaratorId  comment* '='  comment* expression
	;

/*
 * Productions from §15 (Expressions)
 */

primary
	:	 comment* (primaryNoNewArray_lfno_primary | arrayCreationExpression) comment*  (primaryNoNewArray_lf_primary comment* )*  comment*
	;

primaryNoNewArray
	:	literal
	|	typeName ('[' ']')* '.' 'class'
	|	'void' '.' 'class'
	|	'this'
	|	typeName '.' 'this'
	|	'(' expression ')'
	|	classInstanceCreationExpression
	|	fieldAccess
	|	arrayAccess
	|	methodInvocation
	|	methodReference
	;

primaryNoNewArray_lf_arrayAccess
	:
	;

primaryNoNewArray_lfno_arrayAccess
	:	literal
	|	typeName ('[' ']')* '.' 'class'
	|	'void' '.' 'class'
	|	'this'
	|	typeName '.' 'this'
	|	'(' expression ')'
	|	classInstanceCreationExpression
	|	fieldAccess
	|	methodInvocation
	|	methodReference
	;

primaryNoNewArray_lf_primary
	:	classInstanceCreationExpression_lf_primary
	|	fieldAccess_lf_primary
	|	arrayAccess_lf_primary
	|	methodInvocation_lf_primary
	|	methodReference_lf_primary
	;

primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary
	:
	;

primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary
	:	classInstanceCreationExpression_lf_primary
	|	fieldAccess_lf_primary
	|	methodInvocation_lf_primary
	|	methodReference_lf_primary
	;

primaryNoNewArray_lfno_primary
	:	literal
	|	typeName ('[' ']')* '.' 'class'
	|	unannPrimitiveType ('[' ']')* '.' 'class'
	|	'void' '.' 'class'
	|	'this'
	|	typeName '.' 'this'
	|	'(' expression ')'
	|	classInstanceCreationExpression_lfno_primary
	|	fieldAccess_lfno_primary
	|	arrayAccess_lfno_primary
	|	methodInvocation_lfno_primary
	|	methodReference_lfno_primary
	;

primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary
	:
	;

primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary
	:	literal
	|	typeName ('[' ']')* '.' 'class'
	|	unannPrimitiveType ('[' ']')* '.' 'class'
	|	'void' '.' 'class'
	|	'this'
	|	typeName '.' 'this'
	|	'(' expression ')'
	|	classInstanceCreationExpression_lfno_primary
	|	fieldAccess_lfno_primary
	|	methodInvocation_lfno_primary
	|	methodReference_lfno_primary
	;

classInstanceCreationExpression
	:	'new' typeArguments? comment* annotation* comment* Identifier comment* ('.' comment* annotation* comment* Identifier comment*)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody? comment*
	|	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody? comment*
	|	primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody? comment*
	;

classInstanceCreationExpression_lf_primary
	:	'.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody? comment*
	;

classInstanceCreationExpression_lfno_primary
	:	'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody? comment*
	|	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody? comment*
	;

typeArgumentsOrDiamond
	:	typeArguments
	|	'<' '>'
	;

fieldAccess
	:	primary '.'  comment* Identifier
	|	'super' '.'  comment* Identifier
	|	typeName '.'  comment* 'super' '.'  comment* Identifier
	;

fieldAccess_lf_primary
	:	'.'  comment* Identifier
	;

fieldAccess_lfno_primary
	:	'super'  comment* '.'  comment* Identifier
	|	typeName  comment* '.'  comment* 'super' '.'  comment* Identifier
	;

arrayAccess
	:	(	expressionName '[' expression ']'
		|	primaryNoNewArray_lfno_arrayAccess '[' expression ']'
		)
		(	primaryNoNewArray_lf_arrayAccess '[' expression ']'
		)*
	;

arrayAccess_lf_primary
	:	(	primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary '[' expression ']'
		)
		(	primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary '[' expression ']'
		)*
	;

arrayAccess_lfno_primary
	:	(	expressionName '[' expression ']'
		|	primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary '[' expression ']'
		)
		(	primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary '[' expression ']'
		)*
	;

methodInvocation
	:	methodName '(' argumentList? ')'
	|	typeName '.' comment* typeArguments? Identifier '(' argumentList? ')'
	|	expressionName '.' comment* typeArguments? Identifier '(' argumentList? ')'
	|	primary '.' comment* typeArguments? Identifier '(' argumentList? ')'
	|	'super' '.' comment* typeArguments? Identifier '(' argumentList? ')'
	|	typeName '.' comment* 'super' '.' comment* typeArguments? Identifier '(' argumentList? ')'
	;

methodInvocation_lf_primary
	:	'.'  comment* typeArguments? Identifier '(' argumentList? ')'
	;

methodInvocation_lfno_primary
	:	methodName '(' argumentList? ')'
	|	typeName '.'  comment* comment*  typeArguments? Identifier '(' argumentList? ')'
	|	expressionName '.' comment*  comment*  typeArguments? Identifier '(' argumentList? ')'
	|	'super' '.'  comment*  comment*  typeArguments? Identifier '(' argumentList? ')'
	|	typeName '.' comment*   comment*  'super' '.'  comment*  comment*  typeArguments? Identifier '(' argumentList? ')'
	;

argumentList
	:	expression comment* (',' comment* expression comment* )*
	;

methodReference
	:	expressionName '::' typeArguments? Identifier
	|	referenceType '::' typeArguments? Identifier
	|	primary '::' typeArguments? Identifier
	|	'super' '::' typeArguments? Identifier
	|	typeName '.' 'super' '::' typeArguments? Identifier
	|	classType '::' typeArguments? 'new'
	|	arrayType '::' 'new'
	;

methodReference_lf_primary
	:	'::' typeArguments? Identifier
	;

methodReference_lfno_primary
	:	expressionName '::' typeArguments? Identifier
	|	referenceType '::' typeArguments? Identifier
	|	'super' '::' typeArguments? Identifier
	|	typeName '.' 'super' '::' typeArguments? Identifier
	|	classType '::' typeArguments? 'new'
	|	arrayType '::' 'new'
	;

arrayCreationExpression
	:	'new' comment* primitiveType comment* dimExprs comment* dims? comment*
	|	'new' comment* classOrInterfaceType comment* dimExprs comment* dims? comment*
	|	'new' comment* primitiveType comment* dims comment* arrayInitializer comment*
	|	'new' comment* classOrInterfaceType comment* dims comment* arrayInitializer comment*
	;

dimExprs
	:	dimExpr dimExpr*
	;

dimExpr
	:	annotation* '[' expression ']'
	;

constantExpression
	:	expression
	;

expression
	:	comment* lambdaExpression comment*
	|	comment* assignmentExpression comment*
	;

lambdaExpression
	:	comment* lambdaParameters '->' comment* lambdaBody
	;

lambdaParameters
	:	Identifier
	|	'(' comment* formalParameterList? comment*  ')'
	|	'(' comment* inferredFormalParameterList comment* ')'
	;

inferredFormalParameterList
	:	Identifier comment* (',' comment* Identifier comment* )*
	;

lambdaBody
	:	expression
	|	block
	;

assignmentExpression
	:	conditionalExpression
	|	assignment
	;

assignment
	:	leftHandSide comment* assignmentOperator comment* expression comment*
	;

leftHandSide
	:	expressionName
	|	fieldAccess
	|	arrayAccess
	;

assignmentOperator
	:	'='
	|	'*='
	|	'/='
	|	'%='
	|	'+='
	|	'-='
	|	'<<='
	|	'>>='
	|	'>>>='
	|	'&='
	|	'^='
	|	'|='
	;

conditionalExpression
	:	conditionalOrExpression
	|	conditionalOrExpression comment* '?' comment* expression comment* ':' comment* conditionalExpression
	;

conditionalOrExpression
	:	conditionalAndExpression
	|	conditionalOrExpression  comment* '||' comment*  conditionalAndExpression comment*
	;

conditionalAndExpression
	:	inclusiveOrExpression
	|	conditionalAndExpression  comment* '&&'  comment* inclusiveOrExpression comment*
	;

inclusiveOrExpression
	:	exclusiveOrExpression
	|	inclusiveOrExpression  comment* '|'  comment* exclusiveOrExpression comment*
	;

exclusiveOrExpression
	:	andExpression
	|	exclusiveOrExpression  comment* '^'  comment* andExpression comment*
	;

andExpression
	:	equalityExpression
	|	andExpression  comment* '&'  comment* equalityExpression comment*
	;

equalityExpression
	:	relationalExpression
	|	equalityExpression comment*  '=='  comment* relationalExpression comment*
	|	equalityExpression  comment* '!='  comment* relationalExpression comment*
	;

relationalExpression
	:	shiftExpression
	|	relationalExpression  comment* '<'  comment* shiftExpression comment*
	|	relationalExpression  comment* '>'  comment* shiftExpression comment*
	|	relationalExpression  comment* '<='  comment* shiftExpression comment*
	|	relationalExpression  comment* '>='  comment* shiftExpression comment*
	|	relationalExpression  comment* 'instanceof'  comment* referenceType comment*
	;

shiftExpression
	:	additiveExpression
	|	shiftExpression  comment* '<' '<'  comment* additiveExpression comment*
	|	shiftExpression  comment* '>' '>'  comment* additiveExpression comment*
	|	shiftExpression  comment* '>' '>' '>'  comment* additiveExpression comment*
	;

additiveExpression
	:	multiplicativeExpression
	|	additiveExpression  comment* '+'  comment* multiplicativeExpression comment*
	|	additiveExpression  comment* '-'  comment* multiplicativeExpression comment*
	;

multiplicativeExpression
	:	unaryExpression
	|	multiplicativeExpression  comment* '*'  comment* unaryExpression comment*
	|	multiplicativeExpression  comment* '/'  comment* unaryExpression comment*
	|	multiplicativeExpression  comment* '%'  comment* unaryExpression comment*
	;

unaryExpression
	:	preIncrementExpression comment*
	|	preDecrementExpression comment*
	|	'+' unaryExpression comment*
	|	'-' unaryExpression comment*
	|	unaryExpressionNotPlusMinus comment*
	;

preIncrementExpression
	:	'++' unaryExpression comment*
	;

preDecrementExpression
	:	'--' unaryExpression comment*
	;

unaryExpressionNotPlusMinus
	:	postfixExpression comment*
	|	'~' unaryExpression comment*
	|	'!' unaryExpression comment*
	|	castExpression comment*
	;

postfixExpression
	:	(	primary
		|	expressionName
		)
		(	postIncrementExpression_lf_postfixExpression
		|	postDecrementExpression_lf_postfixExpression
		)*
	;

postIncrementExpression
	:	postfixExpression '++'
	;

postIncrementExpression_lf_postfixExpression
	:	'++'
	;

postDecrementExpression
	:	postfixExpression '--'
	;

postDecrementExpression_lf_postfixExpression
	:	'--'
	;

castExpression
	:	'(' primitiveType ')'  comment* unaryExpression
	|	'(' referenceType additionalBound* ')' comment* unaryExpressionNotPlusMinus
	|	'(' referenceType additionalBound* ')' comment*  lambdaExpression
	;

// LEXER

// §3.9 Keywords

ABSTRACT : 'abstract';
ASSERT : 'assert';
BOOLEAN : 'boolean';
BREAK : 'break';
BYTE : 'byte';
CASE : 'case';
CATCH : 'catch';
CHAR : 'char';
CLASS : 'class';
CONST : 'const';
CONTINUE : 'continue';
DEFAULT : 'default';
DO : 'do';
DOUBLE : 'double';
ELSE : 'else';
ENUM : 'enum';
EXTENDS : 'extends';
FINAL : 'final';
FINALLY : 'finally';
FLOAT : 'float';
FOR : 'for';
IF : 'if';
GOTO : 'goto';
IMPLEMENTS : 'implements';
IMPORT : 'import';
INSTANCEOF : 'instanceof';
INT : 'int';
INTERFACE : 'interface';
LONG : 'long';
NATIVE : 'native';
NEW : 'new';
PACKAGE : 'package';
PRIVATE : 'private';
PROTECTED : 'protected';
PUBLIC : 'public';
RETURN : 'return';
SHORT : 'short';
STATIC : 'static';
STRICTFP : 'strictfp';
SUPER : 'super';
SWITCH : 'switch';
SYNCHRONIZED : 'synchronized';
THIS : 'this';
THROW : 'throw';
THROWS : 'throws';
TRANSIENT : 'transient';
TRY : 'try';
VOID : 'void';
VOLATILE : 'volatile';
WHILE : 'while';

// §3.10.1 Integer Literals

IntegerLiteral
	:	DecimalIntegerLiteral
	|	HexIntegerLiteral
	|	OctalIntegerLiteral
	|	BinaryIntegerLiteral
	;

fragment
DecimalIntegerLiteral
	:	DecimalNumeral IntegerTypeSuffix?
	;

fragment
HexIntegerLiteral
	:	HexNumeral IntegerTypeSuffix?
	;

fragment
OctalIntegerLiteral
	:	OctalNumeral IntegerTypeSuffix?
	;

fragment
BinaryIntegerLiteral
	:	BinaryNumeral IntegerTypeSuffix?
	;

fragment
IntegerTypeSuffix
	:	[lL]
	;

fragment
DecimalNumeral
	:	'0'
	|	NonZeroDigit (Digits? | Underscores Digits)
	;

fragment
Digits
	:	Digit (DigitsAndUnderscores? Digit)?
	;

fragment
Digit
	:	'0'
	|	NonZeroDigit
	;

fragment
NonZeroDigit
	:	[1-9]
	;

fragment
DigitsAndUnderscores
	:	DigitOrUnderscore+
	;

fragment
DigitOrUnderscore
	:	Digit
	|	'_'
	;

fragment
Underscores
	:	'_'+
	;

fragment
HexNumeral
	:	'0' [xX] HexDigits
	;

fragment
HexDigits
	:	HexDigit (HexDigitsAndUnderscores? HexDigit)?
	;

fragment
HexDigit
	:	[0-9a-fA-F]
	;

fragment
HexDigitsAndUnderscores
	:	HexDigitOrUnderscore+
	;

fragment
HexDigitOrUnderscore
	:	HexDigit
	|	'_'
	;

fragment
OctalNumeral
	:	'0' Underscores? OctalDigits
	;

fragment
OctalDigits
	:	OctalDigit (OctalDigitsAndUnderscores? OctalDigit)?
	;

fragment
OctalDigit
	:	[0-7]
	;

fragment
OctalDigitsAndUnderscores
	:	OctalDigitOrUnderscore+
	;

fragment
OctalDigitOrUnderscore
	:	OctalDigit
	|	'_'
	;

fragment
BinaryNumeral
	:	'0' [bB] BinaryDigits
	;

fragment
BinaryDigits
	:	BinaryDigit (BinaryDigitsAndUnderscores? BinaryDigit)?
	;

fragment
BinaryDigit
	:	[01]
	;

fragment
BinaryDigitsAndUnderscores
	:	BinaryDigitOrUnderscore+
	;

fragment
BinaryDigitOrUnderscore
	:	BinaryDigit
	|	'_'
	;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
	:	DecimalFloatingPointLiteral
	|	HexadecimalFloatingPointLiteral
	;

fragment
DecimalFloatingPointLiteral
	:	Digits '.' Digits? ExponentPart? FloatTypeSuffix?
	|	'.' Digits ExponentPart? FloatTypeSuffix?
	|	Digits ExponentPart FloatTypeSuffix?
	|	Digits FloatTypeSuffix
	;

fragment
ExponentPart
	:	ExponentIndicator SignedInteger
	;

fragment
ExponentIndicator
	:	[eE]
	;

fragment
SignedInteger
	:	Sign? Digits
	;

fragment
Sign
	:	[+-]
	;

fragment
FloatTypeSuffix
	:	[fFdD]
	;

fragment
HexadecimalFloatingPointLiteral
	:	HexSignificand BinaryExponent FloatTypeSuffix?
	;

fragment
HexSignificand
	:	HexNumeral '.'?
	|	'0' [xX] HexDigits? '.' HexDigits
	;

fragment
BinaryExponent
	:	BinaryExponentIndicator SignedInteger
	;

fragment
BinaryExponentIndicator
	:	[pP]
	;

// §3.10.3 Boolean Literals

BooleanLiteral
	:	'true'
	|	'false'
	;

// §3.10.4 Character Literals

CharacterLiteral
	:	'\'' SingleCharacter '\''
	|	'\'' EscapeSequence '\''
	;

fragment
SingleCharacter
	:	~['\\]
	;

// §3.10.5 String Literals

StringLiteral
	:	'"' StringCharacters? '"'
	;

fragment
StringCharacters
	:	StringCharacter+
	;

fragment
StringCharacter
	:	~["\\]
	|	EscapeSequence
	;

// §3.10.6 Escape Sequences for Character and String Literals

fragment
EscapeSequence
	:	'\\' [btnfr"'\\]
	|	OctalEscape
    |   UnicodeEscape // This is not in the spec but prevents having to preprocess the input
	;

fragment
OctalEscape
	:	'\\' OctalDigit
	|	'\\' OctalDigit OctalDigit
	|	'\\' ZeroToThree OctalDigit OctalDigit
	;

fragment
ZeroToThree
	:	[0-3]
	;

// This is not in the spec but prevents having to preprocess the input
fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

// §3.10.7 The Null Literal

NullLiteral
	:	'null'
	;

// §3.11 Separators

LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
SEMI : ';';
COMMA : ',';
DOT : '.';

// §3.12 Operators

ASSIGN : '=';
GT : '>';
LT : '<';
BANG : '!';
TILDE : '~';
QUESTION : '?';
COLON : ':';
EQUAL : '==';
LE : '<=';
GE : '>=';
NOTEQUAL : '!=';
AND : '&&';
OR : '||';
INC : '++';
DEC : '--';
ADD : '+';
SUB : '-';
MUL : '*';
DIV : '/';
BITAND : '&';
BITOR : '|';
CARET : '^';
MOD : '%';
ARROW : '->';
COLONCOLON : '::';

ADD_ASSIGN : '+=';
SUB_ASSIGN : '-=';
MUL_ASSIGN : '*=';
DIV_ASSIGN : '/=';
AND_ASSIGN : '&=';
OR_ASSIGN : '|=';
XOR_ASSIGN : '^=';
MOD_ASSIGN : '%=';
LSHIFT_ASSIGN : '<<=';
RSHIFT_ASSIGN : '>>=';
URSHIFT_ASSIGN : '>>>=';

// §3.8 Identifiers (must appear after all keywords in the parser.grammar)

Identifier
	:	JavaLetter JavaLetterOrDigit*
	;

fragment
JavaLetter
	:	[a-zA-Z$_] // these are the "java letters" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment
JavaLetterOrDigit
	:	[a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

//
// Additional symbols not defined in the lexical specification
//

AT : '@';
ELLIPSIS : '...';

//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
    ;

comment : MULTICOMMENT | LINECOMMENT ;

MULTICOMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINECOMMENT
    :   '//' ~[\r\n]* -> skip
    ;
