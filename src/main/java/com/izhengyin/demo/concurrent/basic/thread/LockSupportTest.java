package com.izhengyin.demo.concurrent.basic.thread;

import com.izhengyin.demo.concurrent.Sleep;
import java.util.concurrent.locks.LockSupport;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-10 14:00
 */
public class LockSupportTest {
    private static Object object = new Object();
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            System.out.println("running ~ ");
            LockSupport.park(object);
            System.out.println("end ~ ");
        });
        thread.start();
        Sleep.second(5);
        LockSupport.unpark(thread);
    }
}