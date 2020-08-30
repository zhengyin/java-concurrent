package com.izhengyin.demo.concurrent.part3;
import com.izhengyin.demo.concurrent.SleepUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/8/30 1:32 下午
 */
public class SynchronizedTest {
    public static void main(String[] args){
        Map<String,Long> timeCounter = new HashMap<>(100);
        CountDownLatch countDownLatch = new CountDownLatch(100);
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        //执行100次
        IntStream.rangeClosed(0,100)
                .forEach(i -> {
                    final String executeKey = "execute-"+i;
                    timeCounter.put(executeKey,System.currentTimeMillis());
                    executorService.execute(() -> {
                        //每次执行都模拟100MS延迟
                        calculate(100);
                        //记录每一次执行的耗时
                        System.out.println(executeKey+" -> "+(System.currentTimeMillis() - timeCounter.get(executeKey))+" ms");
                        countDownLatch.countDown();
                    });
                });
        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    private synchronized static void calculate(long delay){
        SleepUtils.sleep(delay);
    }
}
