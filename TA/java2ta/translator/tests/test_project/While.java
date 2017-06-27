/**
 * Created by Francesco Spegni
 */
public class While {

    int initial_i = 4;

    public static void main(String[] args) {
        self.initial_i = -5;
        foo(5);
    }

    public static int foo(int initial_j) {
    
        int i = initial_i + 1;
        int j = initial_j - 2;

        i++;

        while (j > 3) {
            j--;
            
            if (i < 5) {
                break;
                i += 4000;
            }
        }

        outer:
        while (j > 0) {
            j = j * 2; 
            while (j < 2) {
                break outer;
                j = j * 2;
            }
            j = i;
        }

        j = i + 1;
        return j;
    }
}
