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
        ByteBuffer buffer = ByteBuffer.allocate(256);
        Scanner scanner = new Scanner(System.in);

        THREAD_POOL.execute(() -> {
            try {
                while (true) {
                    String message = scanner.nextLine();
                    if (channel.isOpen()) {
                        ByteBuffer outBuffer = ByteBuffer.wrap(message.getBytes());
                        channel.write(outBuffer);
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
                    if (channel.read(buffer) < 0) {
                        channel.close();
                        System.out.println("Socket closed");
                    } else {
                        buffer.flip();
                        byte[] byteArr = new byte[buffer.remaining()];
                        buffer.get(byteArr);
                        buffer.clear();
                        System.out.println(new String(byteArr));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}