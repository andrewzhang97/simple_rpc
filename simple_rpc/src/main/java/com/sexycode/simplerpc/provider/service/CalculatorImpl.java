package com.sexycode.simplerpc.provider.service;

/**
 * <p>
 *
 * 服务接口的实现
 */
public class CalculatorImpl implements Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public String hello(String name) {
        return "Hello"+name;
    }
}
