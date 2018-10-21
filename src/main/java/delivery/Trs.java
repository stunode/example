package delivery;

import java.io.IOException;
import java.util.Scanner;

/**
 * 类名称: Trs
 * 功能描述:
 * 日期:  2018/10/20 18:43
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class Trs {
    public static String BLOCK_INPUT = "E:\\code\\example\\src\\main\\java\\delivery\\BlockInput%s.txt";

    public static void main(String[] args) throws Exception {

        new Thread ( ()->{
            try {
                charTest ();
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }).start ();
        while (true){
            System.out.println ("do something");
            Thread.sleep (5000);
        }
    }


    public static void charTest() throws Exception {
        while (true) {
            Scanner ourInput = new Scanner (System.in);
            String stringIn = ourInput.nextLine ();
            System.out.println (stringIn);
        }
    }



}
