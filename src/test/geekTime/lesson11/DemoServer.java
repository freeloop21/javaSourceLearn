package test.geekTime.lesson11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 举个收快递的例子不知道理解是否正确。
 *
 * BIO，快递员通知你有一份快递会在今天送到某某地方，你需要在某某地方一致等待快递员的到来。
 *
 * NIO，快递员通知你有一份快递会送到你公司的前台，你需要每隔一段时间去前台询问是否有你的快递。
 *
 * AIO，快递员通知你有一份快递会送到你公司的前台，并且前台收到后会给你打电话通知你过来取。
 */

/**
 * 由于nio实际上是同步非阻塞io，是一个线程在同步的进行事件处理，当一组事channel处理完毕以后，去检查有没有又可以处理的channel。
 * 这也就是同步+非阻塞。同步，指每个准备好的channel处理是依次进行的，非阻塞，是指线程不会傻傻的等待读。
 * 只有当channel准备好后，才会进行。那么就会有这样一个问题，当每个channel所进行的都是耗时操作时，
 * 由于是同步操作，就会积压很多channel任务，从而完成影响。那么就需要对nio进行类似负载均衡的操作，
 * 如用线程池去进行管理读写，将channel分给其他的线程去执行，这样既充分利用了每一个线程，又不至于都堆积在一个线程中，等待执行。
 * 不知道上述理解是否正确？
 */

/**
 * java核心技术 11讲
 * # 同步、阻塞式 API, 所以需要多线程以实现多任务处理。
 *
 * 其实现要点是：服务器端启动 ServerSocket，端口 0 表示自动绑定一个空闲端口。
 * 调用 accept 方法，阻塞等待客户端连接。
 * 利用 Socket 模拟了一个简单的客户端，只进行连接、读取、打印。
 * 当连接建立后，启动一个单独线程负责回复客户端请求。
 *
 * 这样，一个简单的 Socket 服务器就被实现出来了。
 *
 *
 * 如果连接数并不是非常多，只有最多几百个连接的普通应用，这种模式往往可以工作的很好。
 * 但是，如果连接数量急剧上升，这种实现方式就无法很好地工作了，因为线程上下文切换开销会在高并发时变得很明显，
 * 这是同步阻塞方式的低扩展性劣势
 */
public class DemoServer extends Thread {
    private ServerSocket serverSocket;

    private CountDownLatch latch;

    private ExecutorService executorService;

    private DemoServer(){

    }

    public DemoServer(CountDownLatch latch, ExecutorService executorService) {
        this.latch = latch;
        this.executorService = executorService;
    }


    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(0);
            latch.countDown();

            while (true) {
                Socket socket = serverSocket.accept();//一个accept对应一个请求连接
                RequestHandler requestHandler = new RequestHandler(socket);
                executorService.submit(requestHandler);
            }
        } catch (IOException  e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        DemoServer server = new DemoServer(lock, executorService);
        server.start();

        lock.await();

        for (int i = 0; i < 10; i++) {
            try (Socket client = new Socket(InetAddress.getLocalHost(), server.getPort())) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                bufferedReader.lines().forEach(s -> System.out.println(s));
            }
        }
    }
}

// 简化实现，不做读取，直接发送字符串
class RequestHandler extends Thread {
    private Socket socket;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream());) {
            out.println("Hello world!");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//----------------------------------------------------------------------------

/**
 * NIO 引入的多路复用机制
 *
 * 1.首先，通过 Selector.open() 创建一个 Selector，作为类似调度员的角色。
 * 2.然后，创建一个 ServerSocketChannel，并且向 Selector 注册，通过指定 SelectionKey.OP_ACCEPT，告诉调度员，它关注的是新的连接请求。
 * 注意，为什么我们要明确配置非阻塞模式呢？这是因为阻塞模式下，注册操作是不允许的，会抛出 IllegalBlockingModeException 异常。
 * 3.Selector 阻塞在 select 操作，当有 Channel 发生接入请求，就会被唤醒。
 * 4.在 sayHelloWorld 方法中，通过 SocketChannel 和 Buffer 进行数据操作，在本例中是发送了一段字符串。
 *
 *
 * NIO 则是利用了单线程轮询事件的机制，通过高效地定位就绪的 Channel，来决定做什么，
 * 仅仅 select 阶段是阻塞的，可以有效避免大量客户端连接时，频繁线程切换带来的问题，应用的扩展能力有了非常大的提高。
 */
class NIOServer extends Thread {
    public void run() {
        //try括号内的资源会在try语句结束后自动释放，前提是这些可关闭的资源必须实现 java.lang.AutoCloseable 接口。
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open();) {// 创建Selector和Channel
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8888));
            serverSocket.configureBlocking(false);
            // 注册到Selector，并说明关注点
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();// 阻塞等待就绪的Channel，这是关键点之一
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                //单线程轮询事件的机制
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    // 生产系统中一般会额外进行就绪状态检查
                    sayHelloWorld((ServerSocketChannel) key.channel());
                    iter.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sayHelloWorld(ServerSocketChannel server) throws IOException {
        try (SocketChannel client = server.accept();) {
            client.write(Charset.defaultCharset().encode("Hello world!"));
        }
    }

    // 省略了与前面类似的main
}


/**
 * NIO2
 *
 * 基本抽象很相似，AsynchronousServerSocketChannel 对应于上面例子中的 ServerSocketChannel；
 * AsynchronousSocketChannel 则对应 SocketChannel。
 *
 * 业务逻辑的关键在于，通过指定 CompletionHandler 回调接口，在 accept/read/write 等关键节点，
 * 通过事件机制调用，这是非常不同的一种编程思路。
 */
/*AsynchronousServerSocketChannel serverSock = AsynchronousServerSocketChannel.open().bind(sockAddr);
serverSock.accept(serverSock, new CompletionHandler<>() { //为异步操作指定CompletionHandler回调函数
    @Override
    public void completed(AsynchronousSocketChannel sockChannel, AsynchronousServerSocketChannel serverSock) {
        serverSock.accept(serverSock, this);
        // 另外一个 write（sock，CompletionHandler{}）
        sayHelloWorld(sockChannel, Charset.defaultCharset().encode
                ("Hello World!"));
    }
    // 省略其他路径处理方法...
});*/