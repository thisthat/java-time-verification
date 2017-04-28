package test.library.juppaal;

import uppaal.*;

/**
 * Created by giovanni on 19/04/2017.
 */
public class Main {
    public static void main(String[] args) {
        NTA doc = new NTA();
        Automaton aut1 = new Automaton("test_1");
        Automaton aut2 = new Automaton("test_2");
        Location s0 = new Location(aut1, new Name("s0"), Location.LocationType.NORMAL, 0, 0);
        //aut1.addLocation(s0);
        Location s1 = new Location(aut1, new Name("s1"), Location.LocationType.NORMAL, 0, 0);
        //aut1.addLocation(s1);
        Location s2 = new Location(aut1, new Name("s2"), Location.LocationType.NORMAL, 0, 0);
        //aut1.addLocation(s2);
        Location s3 = new Location(aut1, new Name("s3"), Location.LocationType.NORMAL, 0, 0);
        //aut1.addLocation(s3);
        Location s4 = new Location(aut1, new Name("s4"), Location.LocationType.NORMAL, 0, 0);
        //aut1.addLocation(s4);
        Location s5 = new Location(aut1, new Name("s5"), Location.LocationType.NORMAL, 0, 0);
        //aut1.addLocation(s5);
        new Transition(aut1, s0, s1);
        new Transition(aut1, s0, s2);
        new Transition(aut1, s0, s5);
        new Transition(aut1, s1, s3);
        new Transition(aut1, s2, s4);
        doc.addAutomaton(aut1);
        doc.addAutomaton(aut2);
        doc.setAutoPositioned(true);
        doc.writePrettyLayoutModelToFile("test.xml");
    }
}
