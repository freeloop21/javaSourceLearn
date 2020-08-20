package test.geekTime.lesson23;

/**
 * 分别提供了普通静态变量、静态常量，常量又考虑到原始类型和引用类型可能有区别。
 *
 *
 * Javac CLPreparation.java
 * Javap –v CLPreparation.class
 *
 */
public class CLPreparation {
    public int b = 200;
    public String c = "字符串";
    public static int a = 100;//普通原始类型静态变量和引用类型（即使是常量），是需要额外调用 putstatic 等 JVM 指令的,这些是在显式初始化阶段执行，而不是准备阶段调用
    public static final int INT_CONSTANT = 1000;//原始类型常量，则不需要putstatic这样的步骤。
    public static final Integer INTEGER_CONSTANT = Integer.valueOf(10000);//普通原始类型静态变量和引用类型（即使是常量），是需要额外调用 putstatic 等 JVM 指令的,这些是在显式初始化阶段执行，而不是准备阶段调用
    public static Integer d = Integer.valueOf(20000);
}