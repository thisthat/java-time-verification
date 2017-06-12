/**
 * Created by Francesco Spegni
 */
public class SequentialCode {

    public static void main(String[] args) {
        foo(-5, 5);
    }

    public static int foo(int initial_i, int initial_j) {
    
        int i = initial_i + 1;
        int j = initial_j - 2;

        i++;
        if (i > 0) {
            j = j+1;
            i--;
        } else {
            j = j-1;
            i++;
        }

        if (j < 0) {
            j++;
            j--;
        }

        j = j+1;
        i = j+1;

        while (j > 3) {
            j--;
        }
        j = i;
        return j;
    }
}
