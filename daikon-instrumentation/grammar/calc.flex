package daikon.parser;

%%

%byaccj

%{
  private Parser yyparser;

  public int line = 1;

  public Yylex(java.io.Reader r, Parser yyparser) {
    this(r);
    this.yyparser = yyparser;
  }
%}

NL  = \n | \r | \r\n
TEXT = [aA-zZ0-9\-][aA-zZ0-9\.\-]*
MOD = [0-9]+\s*\(mod\s+[0-9]+\)
SEP = "==========================================================================="

%%

"{" |
"}" |
"(" |
")" { return (int) yycharat(0); }

","  { return Parser.COMMA; }
"==" { return Parser.EQUAL; }
"!=" { return Parser.NEQUAL; }
">" { return Parser.G; }
">=" { return Parser.GEQ; }
"<=" { return Parser.LEQ; }
"<" { return Parser.L; }

":::ENTER" { return Parser.ENTER; }
":::EXIT"  { return Parser.EXIT; }


"one of" { return Parser.ONEOF; }

"size" { return Parser.SIZE; }
"orig" { return Parser.ORIG; }

/* newline */
{NL}   { line++; System.out.println("Line: " + line); }

{TEXT} {
        System.out.print("\n[TEXT] ");
        System.out.print(yytext());
        System.out.print("---\n");
        yyparser.yylval = new ParserVal(yytext());
        return Parser.TEXT;
}

{MOD} {
        System.out.print("\n[MOD] ");
        System.out.print(yytext());
        System.out.print("---\n");
        yyparser.yylval = new ParserVal(yytext());
        return Parser.MOD;
}

{SEP} {
        return Parser.SEP;
}

/* whitespace */
[ \t]+ { }

/* error fallback */
[^]    { throw new RuntimeException("[" + line + "] Error: unexpected character '"+yytext()+"'");  }
