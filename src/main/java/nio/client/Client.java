package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        try {
            new Client().start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            THREAD_POOL.shutdown();
        }
    }

    public void start() throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9000));
        System.out.println("New client started");

        THREAD_POOL.execute(() -> {
            try {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String message = scanner.nextLine();
                    if (channel.isOpen()) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
                        channel.write(byteBuffer);
                    } else {
                        System.out.println("Socket closed");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        THREAD_POOL.execute(() -> {
            try {
                while (true) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                    channel.read(byteBuffer);
                    String message = new String(byteBuffer.array());
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}