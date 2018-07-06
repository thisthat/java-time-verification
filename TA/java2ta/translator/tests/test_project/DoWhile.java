/**
 * Created by Francesco Spegni
 */
public class DoWhile {

    int initial_i = 4;

    public static void main(String[] args) {
        self.initial_i = -5;
        foo(5);
    }

    public static int foo(int initial_j) {
    
        int i = initial_i + 1;
        int j = initial_j - 2;

        i++;

        outer:
        do {
            j = j * 2; 
            inner:
            do {
                break inner;
                j = j * 2;
            }
            while (j < 2);

            j = i;
        }
        while (j > 0);

        j = i + 1;
        return j;
    }
}
