/**
 * Created by Francesco Spegni
 */
public class Switch {

    int initial_i = 4;
    int initial_j = 5;

    public static void main(String[] args) {
        self.initial_i = -5;
        foo(5);
    }

    public static int foo(int initial_j) {
    
        int i = self.initial_i + 1;
        int j = self.initial_j - 2;

        bau:
        i++;

        ciao:
        miao:
        while (i < 10) {

            i = i - j;
            foofie:
            switch(i % 5) {
                case 0:
                case 3:
                    j = i/2;
                    break;
                case 1:
                    j = j++;
                case 2:
                case 4:
                    j = j--;
                    break;
            }
            i++;
        }

        j = i + 1;
        return j;
    }
}
