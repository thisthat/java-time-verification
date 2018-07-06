/**
 * Created by Francesco Spegni
 */
public class MethodCall {

//    String election = "election";
//    String leader = "leader";

    public static int main(String[] args) {
        return MethodCall.foo("ciao", 5, 10);
    }

    public static int foo(String tag, int number, int j) {
    
//        int j = MethodCall.getMessageInt(5); // get the number

        if (tag.equals("election")) {
            if (j > number) {
                System.out.println("foo");
            }
            else if (j == number) // I won! 
            {
                System.out.println("fie");      
            }
            else if (j < number) 
            {   
                System.out.println("fix!");
            }
        } else if (tag.equals("leader")) {
            System.out.println("fuz");
        }

        return number + j;
    }

    public static int getMessageInt(int i) {
        return i + 1;
    }

}
