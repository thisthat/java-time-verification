package parser;

import daikon.parser.Parser;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ParserTest {

    @Test
    public void name() throws Exception {
        String file = ParserTest.class.getClassLoader().getResource("only_injected_invariants.txt").getFile();
        //Parser p = new Parser(new FileReader(file));
        //p.run();
        Parser p = new Parser();
        List<String> lines = Files.readAllLines(Paths.get(file));
        StringBuilder cnt = new StringBuilder();
        for(String line : lines) {
            if(line.contains("()"))
                cnt.append("\n");
            else
                cnt.append(line).append("\n");
        }
        p.calculate(cnt.toString());
    }
}
