/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class For {

    //
    public void simpleFor(){
        for(int i = 0; i < 10; i++){
            System.out.print("asd");
        }
    }

    public void simpleForNoInit(){
        for(; i < 10; i++){
            System.out.print("asd");
        }
    }

    public void simpleForNoInitNoCheck(){
        for(;; i++){
            System.out.print("asd");
        }
    }

    public void simpleForNoInitNoCheckNoUp(){
        for(;;){
            System.out.print("asd");
        }
    }

    public void simpleForNoCheck(){
        for(int i = 0;; i++){
            System.out.print("asd");
        }
    }

    public void simpleForNoInitNoUp(){
        for(; i<10; ){
            System.out.print("asd");
        }
    }

    public void simpleForNoUp(){
        for(int i = 0; i < 10; ){
            System.out.print("asd");
        }
    }

    public void simpleForNoCheckNoUp(){
        for(int i=0;;){
            System.out.print("asd");
        }
    }

    public void complexFor(){
        for(int i = 0; i < 10; i++)
            System.out.print("asd");

    }

    public void complexForNoInit(){
        for(; i < 10; i++)
            System.out.print("asd");

    }

    public void complexForNoInitNoCheck(){
        for(;; i++)
            System.out.print("asd");

    }

    public void complexForNoInitNoCheckNoUp(){
        for(;;)
            System.out.print("asd");

    }

    public void complexForNoCheck(){
        for(int i = 0;; i++)
            System.out.print("asd");

    }

    public void complexForNoInitNoUp(){
        for(; i<10; )
            System.out.print("asd");

    }

    public void complexForNoUp(){
        for(int i = 0; i < 10; )
            System.out.print("asd");

    }

    public void complexForNoCheckNoUp(){
        for(int i=0;;)
            System.out.print("asd");

    }
}
