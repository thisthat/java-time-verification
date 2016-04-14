/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class If {


    public void singleNoBracket(){
        if(x == 1)
            x = 1;
    }

    public void elseNoBracket(){
        if(x == 2)
            x = 1;
        else
            x = 2;
    }

    public void singleBracket(){
        if(x == 1) {
            x = 1;
        }
    }

    public void elseBracket(){
        if(x == 2) {
            x = 1;
        }
        else {
            x = 2;
        }
    }


    public void ifInsideIf(){
        if(y > 0) {
            if(x > 1){
                return 2;
            }
        }
    }

    public void ifInsideIfAndElse(){
        if(y > 0) {
            if(x > 1){
                return 1;
            }
        }
        else
            if(x > 3){
                return 2;
            }
    }

    public void ifInsideIfAndElseWOutParentesis(){
        if(y > 0)
            if(x > 1)
                return 1;
        else
            if(x > 3){
                return 2;
            }
    }
    public void ifInsideIfAndElseWOutParentesis(){
        if(y > 0)
            if(x > 1)
                return 2;
        else
            if(x > 3)
                return 4;
    }
}
