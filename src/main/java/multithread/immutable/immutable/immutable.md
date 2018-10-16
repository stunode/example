## 不变类：
   不变的,不可改变的类
   不需要使用Sychronized进行保护(提高性能)，多个线程同时执行类中的方法也不会出现线程安全性问题
## final
    final字段：一旦赋值不可改变
    final方法：不可被子类覆盖
    final变量和final参数：局部变量和 方法的参数也可以声明final（final参数不可以被赋值）
## jdk中的immutable类： string bigInteger Pattern