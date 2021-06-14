package com.sexycode.simplerpc.comsumer.app;

import com.sexycode.simplerpc.provider.service.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.Arrays;

/**
 * <p>
 *
 *
 */
public class ComsumerApp {
    private static Logger log = LoggerFactory.getLogger(ComsumerApp.class);

    public static void main(String[] args) throws Exception {
        Calculator calculator=refer(Calculator.class,"127.0.0.1",9090);
        String name="China";
        int adderA=1;
        int adderB=2;
        System.out.println(calculator.add(adderA,adderB));
        System.out.println(calculator.hello(name));
    }

    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws  Exception{
        //接口类型 interfaClass
        if(interfaceClass==null){
            throw new IllegalArgumentException("interfaceClass ==null");
        }
        if(!interfaceClass.isInterface()){
            //JDK实现动态代理只能实现对接口的代理，不然就用CGLib
            throw new IllegalArgumentException("interface Class must be interface class");
        }
        if(host==null ||host.length() ==0){
            throw new IllegalArgumentException("host is illegal");

        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port " + port);
        }
        System.out.println(
                "Get remote service " + interfaceClass.getName() + " from server " + host + ":" + port);

        //动态代理
        T proxy =(T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket=new Socket(host, port);
                try{
                    ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
                    try{
                        //序列化发送请求
                        System.out.println("client 发送请求/n");
                        objectOutputStream.writeUTF(method.getName());
                        System.out.println("methodName : " + method.getName());
                        objectOutputStream.writeObject(method.getParameterTypes());
                        System.out.println("parameterTypes : " + Arrays.toString(method
                                .getParameterTypes()));
                        objectOutputStream.writeObject(args);
                        System.out.println("arguments : " + Arrays.toString(args));

                        //处理server返回的数据
                        ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
                        try{
                            Object result=objectInputStream.readObject();
                            if(result instanceof Throwable){
                                throw (Throwable)result;
                            }
                            System.out.println("\nClient收到响应 ： ");
                            System.out.println("result : " + result);
                            return result;
                        }finally {
                            objectInputStream.close();
                        }
                    }finally {
                        objectOutputStream.close();
                    }
                }finally {
                    socket.close();
                }
            }
        });
        return  proxy;
    }
}
