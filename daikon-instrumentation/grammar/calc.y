/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2001 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * This is a modified version of the example from                          *
 *   http://www.lincom-asg.com/~rjamison/byacc/                            *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%{
import java.io.*;
import java.nio.charset.StandardCharsets;
%}

%token SEP /* a text */
%token <sval> TEXT /* a text */
%token <sval> MOD /* a text */
%token ENTER /* a text */
%token EXIT /* a text */



%token SIZE /* a text */
%token ORIG /* a text */


/* OPs */
%token COMMA /* a text */
%token EQUAL /* a text */
%token NEQUAL /* a text */
%token L /* a text */
%token LEQ /* a text */
%token GEQ /* a text */
%token G /* a text */
%token ONEOF /* a text */

%left '-' '+'
%left '*' '/'

%type <obj> single

%%

start : /* nothing */ {this.result = false;}
    | SEP first invs
    ; SEP first invs start

first: TEXT '(' pars ')' status { System.out.println("first"); }

status: ENTER
      | EXIT

pars: TEXT
    | TEXT COMMA pars

invs: /* nothing */ { System.out.println("nothing"); }
    | single invs  { System.out.println("single"); }

single: TEXT op TEXT        {}
      | function op TEXT    {}
      | TEXT ONEOF array    {}
      | TEXT op ORIG '(' TEXT ')' {}
      | TEXT EQUAL MOD {}



array: '{' pars '}' {}


op: L       { }
  | LEQ     { }
  | GEQ     { }
  | G       { }
  | EQUAL   { }
  | NEQUAL  { }

function: SIZE '(' TEXT ')'

%%

  private Yylex lexer;

  private Boolean result = false;

  private int yylex () {
    int yyl_return = -1;
    try {
      yylval = new ParserVal(0);
      yyl_return = lexer.yylex();
    }
    catch (IOException e) {
      System.err.println("IO error :"+e);
    }
    return yyl_return;
  }

  public void yyerror (String error) {
    throw new RuntimeException("Error: " + error);
  }


  public Parser(Reader r) {
      lexer = new Yylex(r, this);
  }

  public Parser(Reader r, boolean debugMe) {
        yydebug=debugMe;
        lexer = new Yylex(r, this);
  }

  public void calculate(String input){
    InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    lexer = new Yylex(new InputStreamReader(stream), this);
    this.run();
  }

