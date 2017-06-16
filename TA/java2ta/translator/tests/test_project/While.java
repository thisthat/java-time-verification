/**
 * Created by Francesco Spegni
 */
public class While {

    public static void main(String[] args) {
        foo(-5, 5);
    }

    public static int foo(int initial_i, int initial_j) {
    
        int i = initial_i + 1;
        int j = initial_j - 2;

        i++;
        while (j > 3) {
            j--;
        }
        j = i;
        return j;
    }
}
