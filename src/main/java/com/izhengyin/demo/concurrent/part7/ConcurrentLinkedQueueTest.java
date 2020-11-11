package com.izhengyin.demo.concurrent.part7;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-10 15:41
 */
public class ConcurrentLinkedQueueTest {
    public static void main(String[] args) {
        ConcurrentLinkedQueue<String> connLinkedQueue = new ConcurrentLinkedQueue<>();
        connLinkedQueue.offer("a");

    }
}
