package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress("localhost", 9000));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started");

        while (true) {
            selector.select();
            System.out.println("New selector event");
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    System.out.println("New selector acceptable event");
                    register(selector, serverSocket);
                }

                if (selectionKey.isReadable()) {
                    System.out.println("New selector readable event");
                    String message = readMessage(selectionKey);
                    if (selectionKey.channel().isOpen()) {
                        writeMessage(selectionKey, message);
                    }
                }
                iterator.remove();
            }
        }
    }

    private void writeMessage(SelectionKey selectionKey, String message) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        String out = "echo: " + message;
        ByteBuffer byteBuffer = ByteBuffer.wrap(out.getBytes());
        client.write(byteBuffer);
    }

    public void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client is connected");
    }

    public String readMessage(SelectionKey selectionKey) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        String message;
        if (client.read(byteBuffer) < 0) {
            selectionKey.cancel();
            client.close();
            message = "Socket closed";
            System.out.println(message);
        } else {
            message = new String(byteBuffer.array());
            System.out.println("New message: " + message);
        }
        return message;
    }
}