/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class For {
    public void simpleFor(){
        for(int i = 0; i < 10; i++){
            System.out.print("asd");
        }
    }

    public void ForNoInit(){
        for(; i < 10; i++){
            System.out.print("asd");
        }
    }

    public void ForNoInitNoCheck(){
        for(;; i++){
            System.out.print("asd");
        }
    }

    public void ForNoInitNoCheckNoUp(){
        for(;;){
            System.out.print("asd");
        }
    }

    public void ForNoCheck(){
        for(int i = 0;; i++){
            System.out.print("asd");
        }
    }

    public void ForNoInitNoUp(){
        for(; i<10; ){
            System.out.print("asd");
        }
    }

    public void ForNoUp(){
        for(int i = 0; i < 10; ){
            System.out.print("asd");
        }
    }

    public void ForNoCheckNoUp(){
        for(int i=0;;){
            System.out.print("asd");
        }
    }
}
