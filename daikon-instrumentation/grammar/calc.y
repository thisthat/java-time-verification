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

import daikon.parser.structure.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
%}

%token SEP /* a text */
%token <sval> TEXT /* a text */
%token <sval> MOD /* a text */
%token ENTER /* a text */
%token <sval> EXIT /* a text */
%token CLASS /* a text */
%token OBJECT /* a text */



%token SIZE /* a text */
%token ORIG /* a text */


/* OPs */
%token COMMA /* a text */
%token IF /* a text */
%token EQUAL /* a text */
%token NEQUAL /* a text */
%token L /* a text */
%token LEQ /* a text */
%token GEQ /* a text */
%token G /* a text */
%token ONEOF /* a text */
%token ELEMS /* a text */

%left '-' '+'
%left '*' '/'

%type <obj> status
%type <obj> single
%type <obj> pars
%type <obj> arrays
%type <obj> op

%%

start : /* nothing */ {}
    | SEP first invs
    ; SEP first invs start

first: TEXT '(' pars ')' status {
    MethodInvariants m = new MethodInvariants(isClass, isObj, isEnter, isExit, exitLine, $1, (List)$3);
    lm.add(m);
    this.lastInv = m;
}
     | TEXT status {
        MethodInvariants m = new MethodInvariants(isClass, isObj, isEnter, isExit, exitLine, $1, new ArrayList<>());
        lm.add(m);
        this.lastInv = m;
}

status: ENTER  {  isClass = false;
                  isObj   = false;
                  isEnter = true;
                  isExit  = false;
                  exitLine = 0;
               }
      | EXIT   {  isClass = false;
                  isObj   = false;
                  isEnter = false;
                  isExit  = true;
                  exitLine = 0;
                  String s = $1;
                  s = s.substring(7);
                  if(s.length()>0)
                    exitLine = Integer.parseInt(s);
              }
      | CLASS  {  isClass = true;
                  isObj   = false;
                  isEnter = false;
                  isExit  = false;
                  exitLine = 0;
              }
      | OBJECT {  isClass = false;
                  isObj   = true;
                  isEnter = false;
                  isExit  = false;
                  exitLine = 0;
              }

pars: /* nothing */     { $$ = new ArrayList<>(); }
    | TEXT              {
                            List<String> out = new ArrayList<>();
                            out.add($1);
                            $$ = out;
                        }
    | TEXT COMMA pars   {
                            List<String> out = new ArrayList<>();
                            out.add($1);
                            out.addAll((List)$3);
                            $$ = out;
                        }

invs: /* nothing */ {  }
    | single invs  { this.lastInv.add((Invariant) $1); }

single: TEXT op TEXT                { $$ = new Check($1, $3, (Invariant.OP)$2); }
      | TEXT ONEOF arrays           { $$ = new OneOf($1, (List) $3); }
      | ORIG '(' TEXT ')' op TEXT   { $$ = new LOrig($3, (Invariant.OP)$5, $6); }
      | TEXT op ORIG '(' TEXT ')'   { $$ = new ROrig($1, (Invariant.OP)$2, $5); }
      | TEXT EQUAL MOD              { $$ = new Mod($1, $3); }
      | '(' single ')' IF '(' single ')'    { $$ = new Implication((Invariant)$2, (Invariant)$6); }
      | SIZE '(' TEXT ')' op TEXT           { $$ = new Size($3, $6, (Invariant.OP)$5); }
      | TEXT ELEMS op TEXT                  { /* ignore */ }
      | SIZE '(' TEXT ')' ONEOF arrays      { /* ignore */ }
      | TEXT ELEMS ONEOF arrays             { $$ = new ArrayVals($1,(List) $4); }
      | ORIG '(' TEXT ')' ONEOF arrays      { $$ = new LOrigArrayVals($3, (List) $6); }

arrays: '{' pars '}' { $$ = $2; }

op: L       { $$ = Invariant.OP.L; }
  | LEQ     { $$ = Invariant.OP.LE; }
  | GEQ     { $$ = Invariant.OP.GE; }
  | G       { $$ = Invariant.OP.G; }
  | EQUAL   { $$ = Invariant.OP.EQ; }
  | NEQUAL  { $$ = Invariant.OP.NEQ; }


%%


  private boolean isClass = false;
  private boolean isObj   = false;
  private boolean isEnter = false;
  private boolean isExit  = false;
  private int exitLine = 0;
  private MethodInvariants lastInv;

  private List<MethodInvariants> lm = new ArrayList<>();

  private Yylex lexer;

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
    String buffer = lexer.getBuffer(5);
    String msg = String.format("[%d] Error: %s\nBuffer:\n%s", lexer.line, error, buffer);
    throw new RuntimeException(msg);
  }


  public Parser(Reader r) {
      lexer = new Yylex(r, this);
  }

  public Parser(Reader r, boolean debugMe) {
        yydebug=debugMe;
        lexer = new Yylex(r, this);
  }

  public List<MethodInvariants> calculate(String input){
    InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    lexer = new Yylex(new InputStreamReader(stream), this);
    lm.clear();
    this.run();
    return lm;
  }

  public List<MethodInvariants> calculate(Path file) throws IOException {
      List<String> lines = Files.readAllLines(file);
      StringBuilder cnt = new StringBuilder();
      for(String line : lines) {
          if(line.contains("()") && !line.contains("():::"))
              cnt.append("\n");
          else if(line.contains("has only one value"))
              cnt.append("\n");
          else if(line.contains(":::EXIT") && line.contains(";condition="))
              cnt.append(line.substring(0, line.indexOf(";"))).append("\n");
          else
              cnt.append(line).append("\n");
      }
      return this.calculate(cnt.toString());
  }