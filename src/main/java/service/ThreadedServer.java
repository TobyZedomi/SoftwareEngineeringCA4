package service;

import model.OrderManager;
import model.UserManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;




public class ThreadedServer {
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final int QUEUE_CAPACITY = 50;


    public static void main(String[] args) {
        ExecutorService clientHandlerPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try (ServerSocket connectionSocket = new ServerSocket(UserUtilities.PORT)){
            UserManager userManager = new UserManager();
            OrderManager orderManager = new OrderManager();

            boolean validServerSession = true;
            while(validServerSession){
                Socket clientDataSocket = connectionSocket.accept();
                TCPVideoGameServer clientHandler = new TCPVideoGameServer(clientDataSocket, userManager, orderManager);
                clientHandlerPool.submit(clientHandler);
            }
        }catch (IOException e){
            System.out.println("Connection socket cannot be established");
        }finally {
            clientHandlerPool.shutdown();
        }
    }
}