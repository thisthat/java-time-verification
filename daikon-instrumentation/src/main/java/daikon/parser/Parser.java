//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package daikon.parser;



//#line 15 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"

import daikon.parser.structure.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
//#line 27 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 1000;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short SEP=257;
public final static short TEXT=258;
public final static short MOD=259;
public final static short ENTER=260;
public final static short EXIT=261;
public final static short CLASS=262;
public final static short OBJECT=263;
public final static short SIZE=264;
public final static short ORIG=265;
public final static short COMMA=266;
public final static short IF=267;
public final static short EQUAL=268;
public final static short NEQUAL=269;
public final static short L=270;
public final static short LEQ=271;
public final static short GEQ=272;
public final static short G=273;
public final static short ONEOF=274;
public final static short ELEMS=275;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    0,    6,    6,    1,    1,    1,    1,    3,    3,
    3,    7,    7,    2,    2,    2,    2,    2,    2,    2,
    2,    2,    2,    2,    4,    5,    5,    5,    5,    5,
    5,
};
final static short yylen[] = {                            2,
    0,    7,    5,    2,    1,    1,    1,    1,    0,    1,
    3,    0,    2,    3,    3,    6,    6,    3,    7,    6,
    4,    6,    4,    6,    3,    1,    1,    1,    1,    1,
    1,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    0,    5,    6,    7,    8,    0,    4,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   31,
   26,   27,   28,   29,    0,    0,    0,    0,    0,    0,
   13,    0,    0,    0,   18,    0,   15,   30,    0,    0,
   14,    0,    0,    0,    0,    0,   11,    3,    0,   23,
   21,    0,    0,    0,    0,    0,   25,    0,    0,    0,
    0,    0,    0,    2,   17,   22,   20,   24,   16,    0,
   19,
};
final static short yydgoto[] = {                          2,
   10,   15,   18,   37,   27,    4,   16,
};
final static short yysindex[] = {                      -237,
 -235,    0,  -34,  -40,    0,    0,    0,    0, -232,    0,
 -259,  -19,  -13,  -40,  -40, -228, -205,   21, -196,    0,
    0,    0,    0,    0,  -92, -229, -241, -194, -193,   25,
    0, -235, -232, -226,    0, -232,    0,    0,  -92, -191,
    0,   28,   29,   30, -198,  -40,    0,    0,  -53,    0,
    0, -185, -222, -214,   34, -237,    0,   35,  -92, -183,
  -92, -181,  -40,    0,    0,    0,    0,    0,    0,   37,
    0,
};
final static short yyrindex[] = {                        79,
    0,    0,    0, -177,    0,    0,    0,    0,   40,    0,
    0,    0,    0,    0,    1,    0,  -39,    0, -240,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  -38,    0,    0,  -43,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    1,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   79,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,
};
final static short yygindex[] = {                        27,
   50,  -10,  -14,  -31,  -21,   53,   -8,
};
final static int YYTABLESIZE=258;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         14,
   12,   10,    9,   30,   40,    9,   31,   50,   19,   20,
   21,   22,   23,   24,   25,   26,   41,   30,   47,    1,
   28,   49,    3,   42,   30,   17,   29,   66,   32,   68,
   36,   60,   62,    5,    6,    7,    8,   56,   38,   20,
   21,   22,   23,   24,   39,   38,   20,   21,   22,   23,
   24,   59,   70,   38,   20,   21,   22,   23,   24,   61,
   33,   34,   35,   43,   44,   45,   51,   52,   55,   53,
   54,   57,   58,   63,   67,   65,   69,   71,    1,   12,
    9,    9,   64,   48,   46,   10,    9,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   11,    0,    0,
    0,    0,    0,   12,   13,    5,    6,    7,    8,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   12,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         40,
    0,   41,   41,   14,   26,   40,   15,   39,  268,  269,
  270,  271,  272,  273,  274,  275,  258,  258,   33,  257,
   40,   36,  258,  265,  265,  258,   40,   59,  257,   61,
  123,   53,   54,  260,  261,  262,  263,   46,  268,  269,
  270,  271,  272,  273,  274,  268,  269,  270,  271,  272,
  273,  274,   63,  268,  269,  270,  271,  272,  273,  274,
  266,   41,  259,  258,  258,   41,  258,   40,  267,   41,
   41,  125,  258,   40,  258,   41,  258,   41,    0,  257,
   41,  125,   56,   34,   32,  125,  125,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  258,   -1,   -1,
   -1,   -1,   -1,  264,  265,  260,  261,  262,  263,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  257,
};
}
final static short YYFINAL=2;
final static short YYMAXTOKEN=275;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'",null,
"'-'",null,"'/'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
"'{'",null,"'}'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,"SEP","TEXT","MOD","ENTER","EXIT","CLASS",
"OBJECT","SIZE","ORIG","COMMA","IF","EQUAL","NEQUAL","L","LEQ","GEQ","G",
"ONEOF","ELEMS",
};
final static String yyrule[] = {
"$accept : start",
"start :",
"start : SEP first invs SEP first invs start",
"first : TEXT '(' pars ')' status",
"first : TEXT status",
"status : ENTER",
"status : EXIT",
"status : CLASS",
"status : OBJECT",
"pars :",
"pars : TEXT",
"pars : TEXT COMMA pars",
"invs :",
"invs : single invs",
"single : TEXT op TEXT",
"single : TEXT ONEOF arrays",
"single : ORIG '(' TEXT ')' op TEXT",
"single : TEXT op ORIG '(' TEXT ')'",
"single : TEXT EQUAL MOD",
"single : '(' single ')' IF '(' single ')'",
"single : SIZE '(' TEXT ')' op TEXT",
"single : TEXT ELEMS op TEXT",
"single : SIZE '(' TEXT ')' ONEOF arrays",
"single : TEXT ELEMS ONEOF arrays",
"single : ORIG '(' TEXT ')' ONEOF arrays",
"arrays : '{' pars '}'",
"op : L",
"op : LEQ",
"op : GEQ",
"op : G",
"op : EQUAL",
"op : NEQUAL",
};

//#line 148 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"


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
//#line 352 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 63 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{}
break;
case 3:
//#line 67 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{
    MethodInvariants m = new MethodInvariants(isClass, isObj, isEnter, isExit, exitLine, val_peek(4).sval, (List)val_peek(2).obj);
    m.setFileLine(lexer.line);
    lm.add(m);
    this.lastInv = m;
}
break;
case 4:
//#line 73 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{
        MethodInvariants m = new MethodInvariants(isClass, isObj, isEnter, isExit, exitLine, val_peek(1).sval, new ArrayList<>());
        m.setFileLine(lexer.line);
        lm.add(m);
        this.lastInv = m;
}
break;
case 5:
//#line 80 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{  isClass = false;
                  isObj   = false;
                  isEnter = true;
                  isExit  = false;
                  exitLine = 0;
               }
break;
case 6:
//#line 86 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{  isClass = false;
                  isObj   = false;
                  isEnter = false;
                  isExit  = true;
                  exitLine = 0;
                  String s = val_peek(0).sval;
                  s = s.substring(7);
                  if(s.length()>0)
                    exitLine = Integer.parseInt(s);
              }
break;
case 7:
//#line 96 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{  isClass = true;
                  isObj   = false;
                  isEnter = false;
                  isExit  = false;
                  exitLine = 0;
              }
break;
case 8:
//#line 102 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{  isClass = false;
                  isObj   = true;
                  isEnter = false;
                  isExit  = false;
                  exitLine = 0;
              }
break;
case 9:
//#line 109 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new ArrayList<>(); }
break;
case 10:
//#line 110 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{
                            List<String> out = new ArrayList<>();
                            out.add(val_peek(0).sval);
                            yyval.obj = out;
                        }
break;
case 11:
//#line 115 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{
                            List<String> out = new ArrayList<>();
                            out.add(val_peek(2).sval);
                            out.addAll((List)val_peek(0).obj);
                            yyval.obj = out;
                        }
break;
case 12:
//#line 122 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{  }
break;
case 13:
//#line 123 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ this.lastInv.add((Invariant) val_peek(1).obj); }
break;
case 14:
//#line 125 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new Check(val_peek(2).sval, val_peek(0).sval, (Invariant.OP)val_peek(1).obj); }
break;
case 15:
//#line 126 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new OneOf(val_peek(2).sval, (List) val_peek(0).obj); }
break;
case 16:
//#line 127 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new LOrig(val_peek(3).sval, (Invariant.OP)val_peek(1).obj, val_peek(0).sval); }
break;
case 17:
//#line 128 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new ROrig(val_peek(5).sval, (Invariant.OP)val_peek(4).obj, val_peek(1).sval); }
break;
case 18:
//#line 129 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new Mod(val_peek(2).sval, val_peek(0).sval); }
break;
case 19:
//#line 130 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new Implication((Invariant)val_peek(5).obj, (Invariant)val_peek(1).obj); }
break;
case 20:
//#line 131 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new Size(val_peek(3).sval, val_peek(0).sval, (Invariant.OP)val_peek(1).obj); }
break;
case 21:
//#line 132 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ /* ignore */ }
break;
case 22:
//#line 133 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ /* ignore */ }
break;
case 23:
//#line 134 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new ArrayVals(val_peek(3).sval,(List) val_peek(0).obj); }
break;
case 24:
//#line 135 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = new LOrigArrayVals(val_peek(3).sval, (List) val_peek(0).obj); }
break;
case 25:
//#line 137 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 26:
//#line 139 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = Invariant.OP.L; }
break;
case 27:
//#line 140 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = Invariant.OP.LE; }
break;
case 28:
//#line 141 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = Invariant.OP.GE; }
break;
case 29:
//#line 142 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = Invariant.OP.G; }
break;
case 30:
//#line 143 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = Invariant.OP.EQ; }
break;
case 31:
//#line 144 "/Users/giovanni/repository/java-xal/daikon-instrumentation/grammar/calc.y"
{ yyval.obj = Invariant.OP.NEQ; }
break;
//#line 664 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
