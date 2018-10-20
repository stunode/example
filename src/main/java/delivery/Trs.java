package delivery;

/**
 * 类名称: Trs
 * 功能描述:
 * 日期:  2018/10/20 18:43
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class Trs {
    public static String BLOCK_INPUT = "E:\\code\\example\\src\\main\\java\\delivery\\BlockInput%s.txt" ;

    public static void main(String[] args) {

        //初始化 blockinput filename
        BLOCK_INPUT = String.format (BLOCK_INPUT, 2);

        System.out.println (BLOCK_INPUT);

    }
}
