package com.sexycode.simplerpc.provider.app;

import com.sexycode.simplerpc.provider.service.Calculator;
import com.sexycode.simplerpc.provider.service.CalculatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * <p>
 *
 * 服务暴露
 */
public class ProviderApp {
    private static Logger logger=LoggerFactory.getLogger(ProviderApp.class);


    public static void main(String[] args) throws Exception {
        //把服务暴露出来

        Calculator calculator =new CalculatorImpl();
        export(calculator,9090);
    }

    public static void export(final Object service, int port) throws Exception{
        if(service==null){
            throw new IllegalArgumentException("service instance == null");
        }
        if(port<=0 ||port>65535){
            throw new IllegalArgumentException("Invalid port " + port);
        }

        ServerSocket serverSocket =new ServerSocket(port);
        while(true) {
            try {
                // 监听Socket请求
                final Socket socket = serverSocket.accept();
                //将收到的请求进行反序列化，反序列化可以通过自带的方法，或者jackson等外部包
                //BIO最简单的
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            try {
                                //把对象写入流之中，再到socket里面拿
                                ObjectInputStream input = new ObjectInputStream(
                                        socket.getInputStream());
                                try {

                                    System.out.println("\nServer解析请求 ： ");
                                    String methodName = input.readUTF();
                                    System.out.println("methodName : " + methodName);
                                    // 泛型与数组是不兼容的，除了通配符作泛型参数以外
                                    Class<?>[] parameterTypes = (Class<?>[])input.readObject();
                                    System.out.println(
                                            "parameterTypes : " + Arrays.toString(parameterTypes));
                                    Object[] arguments = (Object[])input.readObject();
                                    System.out.println("arguments : " + Arrays.toString(arguments));


                                    /* Server 处理请求，进行响应*/
                                    ObjectOutputStream output = new ObjectOutputStream(
                                            socket.getOutputStream());
                                    try {
                                        // service类型为Object的(可以发布任何服务)，故只能通过反射调用处理请求
                                        // 反射调用，处理请求
                                        Method method = service.getClass().getMethod(methodName,
                                                parameterTypes);
                                        Object result = method.invoke(service, arguments);
                                        System.out.println("\nServer 处理并生成响应 ：");
                                        System.out.println("result : " + result);
                                        output.writeObject(result);
                                    } catch (Throwable t) {
                                        output.writeObject(t);
                                    } finally {
                                        output.close();
                                    }
                                } finally {
                                    input.close();
                                }
                            } finally {
                                socket.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();//用一个线程来解决这个问题，如果今后要拓展，那就用一个线程池来提升，
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
