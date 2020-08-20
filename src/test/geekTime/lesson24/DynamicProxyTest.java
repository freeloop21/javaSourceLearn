package test.geekTime.lesson24;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 第24讲 | 有哪些方法可以在运行时动态生成一个Java类？
 * 动态代理例子
 */
public class DynamicProxyTest {

    public static void main(String[] args) {
        MaotaiJiu maotaiJiu = new MaotaiJiu();
        // Guitai()中可以传入其他类型，此时要保证Proxy.newProxyInstance 时传的参数类型对应。所以可以生成很多类型的代理类，调用这些类的不同方法，
        // 但是因为使用的是同一个处理类（InvocationHandler），所以调用方法的前后逻辑都是该处理类（InvocationHandler）中的invoke方法。
        InvocationHandler guiTai = new GuiTai_Handler(maotaiJiu);
        SellWine sellWine = (SellWine) Proxy.newProxyInstance(MaotaiJiu.class.getClassLoader(), MaotaiJiu.class.getInterfaces(), guiTai);
        sellWine.maiJiu();
    }


}

// SellWine 是一个接口，你可以理解它为卖酒的许可证。
interface SellWine{
    void maiJiu();
}

class MaotaiJiu implements SellWine{
    public void maiJiu(){
        System.out.println("我卖得是茅台酒。");
    }
}

class GuiTai_Handler implements InvocationHandler{

    private MaotaiJiu maotaiJiu;

    public GuiTai_Handler(MaotaiJiu maotaiJiu) {
        this.maotaiJiu = maotaiJiu;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("销售开始");
        method.invoke(maotaiJiu, args);
        System.out.println("销售结束");
        return null;
    }
}