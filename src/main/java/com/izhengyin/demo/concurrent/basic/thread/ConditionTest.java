package com.izhengyin.demo.concurrent.basic.thread;

import com.izhengyin.demo.concurrent.Sleep;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-10 14:15
 */
public class ConditionTest {

    private static Lock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();

    public static void main(String[] args) {
        Thread thread = new Thread(new Wait());
        thread.start();
        Sleep.second(5);
        lock.lock();
        try {
            condition.signal();
        }finally {
            lock.unlock();
        }

    }

    public static class Wait implements Runnable {
        @Override
        public void run() {
            lock.lock();
            try {
                System.out.println("running ... ");
                condition.await();
                System.out.println("end ... ");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }

}
