/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class While {

    public void simpleWhile(){
        while( i < 10){
            System.out.print("ciao");
            System.out.print("ciao");
        }
    }

    public void singleWhile(){
        while( i < 10)
            System.out.print("ciao");
        System.out.print("ciao");
    }

    public void combinedTest(){
        for(int i = 0; i < 10; i++){
            while(true){
                if(i == x)
                    return false;
                x = 10;
                return 30;
            }
        }
    }
}
