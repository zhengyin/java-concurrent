package com.izhengyin.demo.concurrent.basic.thread.pool;

import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 16:58
 */
public class TestMyHttpClientPool {
    public static void main(String[] args) {
        MyHttpClientPool myHttpClientPool  = new MyHttpClientPool(10);
        ExecutorService executorService = Executors.newFixedThreadPool(11);
        IntStream.range(0,15)
                .forEach(i -> {
                    executorService.execute(() -> {
                        Response response = myHttpClientPool.call(new Request.Builder().url("http://httpbin.org/delay/1?id="+i).build(),3000,100);
                        System.out.println(System.currentTimeMillis()+" ["+i+"] -> "+response.toString());
                    });
                });
    }
}
