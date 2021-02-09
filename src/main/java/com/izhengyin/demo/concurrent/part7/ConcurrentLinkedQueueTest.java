package com.izhengyin.demo.concurrent.part7;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-10 15:41
 */
public class ConcurrentLinkedQueueTest {
    private final static ConcurrentLinkedQueue<Integer> QUEUE = new ConcurrentLinkedQueue<>();
    public static void main(String[] args) {

        QUEUE.add(1);
        QUEUE.add(2);
        QUEUE.add(3);
        QUEUE.add(0);

        System.out.println(QUEUE);


    //    removeTest();

    }


    private static void removeTest(){
        IntStream.range(1,10000000)
                .forEach(QUEUE::add);

        IntStream.range(9000000,9000010)
                .forEach(i -> {
                    Executors.newFixedThreadPool(10)
                            .execute(() -> remove(i));
                });
    }

    private static void remove(int v){
        long t = System.currentTimeMillis();
        QUEUE.remove(v);
        System.out.println(System.currentTimeMillis()+" remove "+v+" consume "+(System.currentTimeMillis()-t));
    }
}
