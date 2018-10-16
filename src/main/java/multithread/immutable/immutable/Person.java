package multithread.immutable.immutable;

/**
 * @Author Ryan
 * @Date 2018/10/15 23:42
 * @Function
 */
public class Person {

    private final String name; // final一旦赋值，就不可改变

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        Person p = new Person("ryan");
    }
}
