package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * 类名称: StreamTest
 * 功能描述:
 * 日期:  2018/10/22 9:42
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class StreamTest {

    public static void main(String[] args) {

        Stream<Integer> stream = Arrays.stream(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});

        //求集合元素只和
        Integer result = stream.reduce(0, Integer::sum);
        System.out.println(result);

        stream = Arrays.stream(new Integer[]{1, 2, 3, 4, 5, 6, 7});

        //求和
        stream.reduce((i, j) -> i + j).ifPresent(System.out::println);

        List<Person> personList = new ArrayList<> ();
        personList.add (new Person ("xiao", 2));
        personList.add (new Person ("da", 22));
        personList.add (new Person ("xiao", 21));
        personList.add (new Person ("xiao", 19));
        Integer sumResult =  personList.stream ().map ((p) -> p.getAge ()).reduce ((x, y) -> x + y).get ();
        System.out.println ("Personlist sum result : " +sumResult);


    }
}

class Person{
    String name;
    Integer age;

    public Person(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
