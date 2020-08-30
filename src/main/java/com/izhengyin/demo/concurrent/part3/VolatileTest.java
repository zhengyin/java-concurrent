package com.izhengyin.demo.concurrent.part3;

import com.izhengyin.demo.concurrent.SleepUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/8/30 2:44 下午
 */
public class VolatileTest {
    private static volatile String v;
    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        long sTime = System.currentTimeMillis();
        int loop = 1000000;
        IntStream.rangeClosed(0,loop)
                .forEach(i -> {
                    executorService.execute(() -> {
                        //构建一个字符串，赋值给 volatile 变量
                        int size = i % 1000;
                        StringBuffer sb = new StringBuffer(size);
                        IntStream.rangeClosed(0,size).forEach(sb::append);
                        v = sb.toString();
                        //打印耗时
                        if(i == loop){
                            System.out.println((System.currentTimeMillis() - sTime) +"ms");
                        }
                    });
                });
        SleepUtils.sleep(5000);
        executorService.shutdown();
    }
}
