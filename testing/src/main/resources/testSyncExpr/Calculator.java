package mi;
import java.io.IOException;

/**
 * Created by giovanni (@thisthatDC) on 18/03/16.
 */
public class Calculator {

    interface IntegerMath {
        int operation(int a, int b);
    }

    public int operateBinary(int a, int b, IntegerMath op) {
        return op.operation(a, b);
    }

    public static void main(String... args) throws IOException {
        //single line comment
        Calculator myApp = new Calculator();
        IntegerMath addition = (a, b) -> a + b;
        IntegerMath subtraction = (a, b) -> a - b;
        System.out.println("40 + 2 = " +
                myApp.operateBinary(40, 2, addition));
        /* mul
        tiasdi
         */


        synchronized (this){
            System.out.println("20 - 10 = " +
                    myApp.operateBinary(20, 10, subtraction));
        }

    }
}

