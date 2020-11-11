package com.izhengyin.demo.concurrent.basic.thread;
import com.izhengyin.demo.concurrent.Sleep;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 14:30
 */
public class DaemonThread {
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis()+" Main started!");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Sleep.second(1);
                System.out.println(System.currentTimeMillis()+" daemonTest end!");
            }
        };
        Thread thread = new Thread(runnable,"daemonTest");
        thread.setDaemon(true);
        thread.start();
        System.out.println(System.currentTimeMillis()+" Main exit!");
    }
}
