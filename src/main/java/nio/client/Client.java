package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        try {
            new Client().start();
        } finally {
            THREAD_POOL.shutdown();
        }
    }

    public void start() {
        System.out.println("New client started");

        THREAD_POOL.execute(() -> {
            try {
                SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9000));
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String message = scanner.nextLine();
                    channel.write(ByteBuffer.wrap(message.getBytes()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        THREAD_POOL.execute(() -> {
            try {
                SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9000));
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                while (true) {
                    channel.read(byteBuffer);
                    String message = new String(byteBuffer.array());
                    System.out.println("Echo: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}