## java   I/O流

### BIO

* #### BIO面向流的一种同步阻塞流(jdk1.4以前的唯一选择)，它相关的类和接口在java.io中

  一个客户端对应服务器端的一个线程，线程是有开销的，对于客服端而言当不传输数据时，则会把当前线程阻塞。

  ![BIO模型图](C:\Users\王松祥\AppData\Roaming\Typora\typora-user-images\image-20201116084757736.png)
  优点：可以及时收到客户端的请求，并进行相应处理。
  缺点：太耗费服务器端的性能，浪费资源，因为它的连接在不使用时，并不会断开，仍然是占有的状态，并保持阻塞的。
  适用创景:
  适用于连接数目小、且固定的架构中，这是由它的优缺点所决定的

  ```java
  //服务器端
  //声明一个服务器端以及监听的端口号
  ServerSocket serverSocket = new ServerSocket(5050);
  //创建一个线程池用以对接客户端
          ExecutorService pool = Executors.newCachedThreadPool();
  //循环判断，是否有客户端连接进来
          while (true){
              System.out.println("有人连接我吗？");
  //一旦监听到客户端，就从线程池中取一个线程用以对接,然后主线程再继续判断是否有客户端前来连接
              Socket socket = serverSocket.accept();
              System.out.println("客宾一位楼上请.....");
           //***************************
              pool.execute(new Runnable() {
                  @Override
                  public void run() {
                      try {
                          InputStream inputStream = socket.getInputStream();
                          byte[] bytes = new byte[1024];
                          int len=inputStream.read(bytes);
                          while(len!=-1){
                              System.out.println(Thread.currentThread().getName()+"----->"+new String(bytes,0,len));
                              len=inputStream.read(bytes);
                          }
                          System.out.println(Thread.currentThread().getName()+"撤离了.....");
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  }
              });
        //**************************
          }
  ```

  ```java
  //客户端 
  try {
      //创建一个socket客户端，在其中填入要访问的域名以及端口号
              Socket socket = new Socket("127.0.0.1", 5050);
              Scanner input = new Scanner(System.in);
              while (true){
        //循环判断是否有数据发送，一旦有数据发送就调用它本身的outputStream方法用以发送数据给服务器端          
                  System.out.println("请输入待发送信息:");
                  String next = input.next();
                  if (next!=null){
                      OutputStream outputStream = socket.getOutputStream();
                      outputStream.write(next.getBytes());
                  }else{
                      break;
                  }
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
  ```

  

  可以看到：每一个客户端对应于服务器端的一条线程，当不客户端不发送数据时，服务器端就阻塞在读入或写入的那个地方，造成浪费资源。

  基于事件驱动的----有则干活无则等待!

### NIO

  * #### NIO面向缓存区的一种同步非阻塞流(jdk1.4引入的一种新IO流)

  ![BIO模型图](C:\Users\王松祥\AppData\Roaming\Typora\typora-user-images\image-20201116120040624.png)

  一个线程对应多个客户端，服务器端开启一个线程轮询访问客户端，有信息传输时，就把这个信息拿走，没有就下一个，如果这个线程服务的所有客户端都没有信息传输，就直接返回，它的核心是不管有没有信息，我只走一遍，有就带走，没有就拉倒

---

  **selector、channel、buffer关系：**

  * 一个线程对应一个selector
  * 服务器端可以有多个selector，一个selector对应多个channel，一个channel对应一个buffer，一个buffer对应个客户端
  * selector根据事件，去获取具体的channel，基于事件驱动的一个关系
  * channel是双向通信的，可以反映底层操作系统的情况
  * buffer是双向的可以读、也可以写，不过需要flip来显示的进行切换

-------

**buffer定义了四个属性**

**capacity：**当前buffer所能容纳的最大数据个数，在buffer初始化的时候就定下来了，且是*不可修改的*，是buffer缓冲区的容量长度。

**limit：**表示缓冲区的当前的终点，因为buffer的读取或者存储是自增的/自减的，设立这个值以后，即保证不能超出buffer的最大用量，在这里的这个容量是指，当前buffer中数据的个数，可以把limit理解为当前buffer的数据长度，防止越界，但是这个**limit值是可以修改的**

**position：**下一次读取或写入元素时的索引位置。可以修改

**mark：**一个标志位，用来标志当前的position位置，在我们需要时可以通过reset方法来让position回到mark标记的地方，即允许有一个返回的过程

在写buffer的时候，buffer会跟踪写入了多少数据，需要读buffer的时候，需要调用flip()来将buffer从写模式切换成读模式，**读模式中只能读取写入的数据，而非整个buffer。**
当数据都读完了，你需要清空buffer以供下次使用，可以有2种方法来操作：**调用clear() **或者**调用compact()**。
区别：clear方法清空整个buffer，compact方法只清除你已经读取的数据，未读取的数据会被移到buffer的开头，此时写入数据会从当前数据的末尾开始。

-----

**channel**

* channel是一个接口

* 常用的四个实现类：FileChannel、DataGramChannel、ServerSocketChannel、SocketChannel

  1）、FileChannel：向文件当中读写数据；

  ​		常用方法：read、write、transFerFrom、transFerTo

  2）、DatagramChannel：通过UDP协议向网络读写数据；
  3）、SocketChannel：通过TCP协议向网络读写数据；
  4）、ServerSocketChannel：以一个web服务器的形式，监听到来的TCP连接，对每个连接建立一个SocketChannel。

  ```java
  //四种channel获取方式
      ServerSocketChannel channel = new ServerSocket().getChannel();
      SocketChannel channel1 = new Socket().getChannel();
      FileChannel fileChannel = new FileOutputStream().getChannel();
      DatagramChannel datagramChannel = new DatagramSocket().getChannel();
  ```

  ```java
  //filechannel向文件中写入数据		
      String str="hello world !";
      FileChannel fileChannel = new FileOutputStream("d:\\file01.txt").getChannel();
      ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
      byteBuffer.put(str.getBytes());
      byteBuffer.flip();
      fileChannel.write(byteBuffer);
  ```

  ```java
  //filechannel从文件中读取数据
      File file = new File("d:\\file01.txt");
      FileChannel fileChannel = new FileInputStream(file).getChannel();
      ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.length());
      fileChannel.read(byteBuffer);
      byteBuffer.flip();
      System.out.println(new String(byteBuffer.array()));
  ```

  ```java
  //从一个channel中读取数据，再放到另一个channel中，即实现一个复制
  //在这要注意一点的就是，从一个读即被读的那个channel必须是---可输入类型的channel--- 即可以被读的一个channel
  //被写的那个channel必须是一个可以被写的channel，否则会报异常NonWriteableChannel  ----> NonWritableChannelException
      File file = new File("file01");
      FileChannel channel = new FileInputStream(file).getChannel();
      FileChannel channel1 = new FileOutputStream("file02").getChannel();
      ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
      channel.read(byteBuffer);
      byteBuffer.flip();
      channel1.write(byteBuffer);
  ```

   **把buffer转为只读buffer，--->buffer.asReadOnlyBuffer();**

   **MappedByteBuffer**，可以在堆外内存直接对数据进行修改，系统不需要再单独拷贝一份进行修改。

  * 参数解读：

    1、这个map的权限：有读写、只读、private（写时拷贝-->Mode for a private (copy-on-write) mapping.）

    2.开始修改的位置

    3.允许修改的最大长度


**selector**

* 一个线程对应一个selector，一个selector可以对应多个channel(通道)，且是轮询访问，channel通过事件驱动的方式，来请求selector进行处理，一旦某个channel有响应事件到达了selector中，则selector就会去处理这个channel的数据，若是当前所有的selector下的channel都没有数据进行传输，则seletor还可以去做其他事情，这就是非阻塞的核心要义，不再是每一个channel一个线程，在这里用了多路复用器，即多个channel共享一个线程，有数据传输，就调用，没有也无需阻塞等待，这在很大程度上，降低了服务器端的性能损耗，同时也降低了频繁的线程切换，在很大程度上提升了数据传输的效率。

* **selector是一个抽象类**    <u>每一个channel对应一个selectionKey，当channel触发事件时则把其对应的selectionKey返回</u>

  ​	①selector轮询，当有事件触发时，则会返回一个selectionKeys的set集合

  ```java
  Set<SelectionKey> selectionKeys = selector.selectedKeys();
  ```

  ​	②然后可以遍历这个selectionKeys的set集合获取每一个selectionKey，再获取key中的具体SocketChannel和其对应的服务器端的buffer缓冲区

  * select(long timeout)方法，等待timeout的时间若是没有，channel进行数据传输，则直接返回，若有,则把触发事件的channel加入到selet集合并返回，
  * selectKeys（）从内部集合中得到所有注册的selectionKey
  * selectNow轮训一遍就立马返回，不再等待
  * wakeup唤醒某个阻塞的selector
  * select（）轮询一遍后若没有selectionKey加入，则阻塞

* **channel注册到seletor的过程**

  * 客户端连接到服务器端后，服务器端会生成它对应的socketChannel。
  * 服务器端调用register方法，将其注册到selector
  * 注册完成以后，会返回一个selectionKey用以对接socketChannel
  * 之后selector调用select方法进行轮询判断那个socketChannel有数据传输，一旦有数据传输，则把该channel对应的selectionKey返回
  * 服务器端，通过selectionKey反向获取socketChannel，进而进行数据传输。

  ```java
  //nio简单示例
  //创建一个serveSocketChannel服务端
  ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
  //创建一个selector选择器
          Selector selector = Selector.open();
  //声明服务端监听的端口号
          InetSocketAddress inetSocketAddress = new InetSocketAddress(5050);
  //将服务器端绑定到该端口
          serverSocketChannel.socket().bind(inetSocketAddress);
  //将服务端置为非阻塞的模式
          serverSocketChannel.configureBlocking(false);
  //将服务端注册到selector选择器中
          serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
          while (true){
              //selector开启轮询，查看是否有客户端前来连接
              int i = selector.selectNow();
              if (i==0){
                  //用以模拟服务器端做其他事情
                  System.out.println("没有连接，我做其他的事情去!");
                  try {
                      Thread.sleep(3000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  continue;
              }else{
                  //一旦selector发下有客户端的事件触发
                  Set<SelectionKey> selectionKeys = selector.selectedKeys();
                  Iterator<SelectionKey> iterator = selectionKeys.iterator();
                //就开始遍历，  
                  while (iterator.hasNext()){
                      SelectionKey selectionKey = iterator.next();
                      //若是连接请求，则进行连接
                      if (selectionKey.isAcceptable()){
                          System.out.println("有人来连接了诶");
                          //通过服务器端创建与之对应的客户端socket
                          SocketChannel socketChannel = serverSocketChannel.accept();
                          System.out.println(socketChannel.hashCode()+"--->"+Thread.currentThread().getName());
                          socketChannel.configureBlocking(false);
                          //将客户端注册到selector选择器中
                          socketChannel.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                      }
                      //若是读请求，则开始读操作
                      if (selectionKey.isReadable()){
                          SocketChannel channel = (SocketChannel) selectionKey.channel();
                          ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                          channel.read(byteBuffer);
                          System.out.println(new String(byteBuffer.array()));
                          byteBuffer.clear();
                      }
                      iterator.remove();
                  }
              }
  
  
          }
  ```

  ```java
      //客户端
  
  //创建客户端的socket
      SocketChannel socketChannel = SocketChannel.open();
  //声明将要建立连接的inet地址
          InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 5050);
  //将socket设置为非阻塞的， 读写完之后立马就返回，不阻塞
          socketChannel.configureBlocking(false);
          if (!socketChannel.connect(inetSocketAddress)){
              while (!socketChannel.finishConnect()){
                  System.out.println("客户端做自己的事情");
              }
          }
          if (socketChannel.isConnected()){
              ByteBuffer byteBuffer = ByteBuffer.wrap("hello world".getBytes());
              socketChannel.write(byteBuffer);
          }
          System.in.read();
  ```

  

----

  NIO是面向**缓冲区**的，数据读取到一个它稍后处理的**缓冲区中**，需要时可以**在缓冲区中前后移动**，这就增加了它的灵活性，使用它可以提供非阻塞式的高伸缩性网络

------

  优点：很大程度上降低了对服务器的性能要求，可以做到一条线程处理多个客户端的连接，同时它是非阻塞的

  缺点：不能保证及时处理客户端的请求，且因为它是面向数据块的，当数据块不完整时，不能被处理（读取、写入），还需要额外的为每一个块进行信息标记，若是不完整的则不允许被处理。

---

### AIO

* #### AIO异步非阻塞

 与NIO不同，当进行读写操作时，只须直接调用API的read或write方法即可。这两种方法均为异步的，对于读操作而言，当有流可读取时，操作系统会将可读的流传入read方法的缓冲区，并通知应用程序；对于写操作而言，当操作系统将write方法传递的流写入完毕时，操作系统主动通知应用程序。即可以理解为，read/write方法都是异步的，完成后会主动调用回调函数。在JDK1.7中，这部分内容被称作NIO.2，主要在java.nio.channels包下增加了下面四个异步通道：AsynchronousSocketChannel、AsynchronousServerSocketChannel、AsynchronousFileChannel、AsynchronousDatagramChannel

  

  

