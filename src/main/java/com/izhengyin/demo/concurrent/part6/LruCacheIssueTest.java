package com.izhengyin.demo.concurrent.part6;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2021-02-09 10:49
 */
public class LruCacheIssueTest {
    public static void main(String[] args) {
        int loop = 10000;
        int maxSize = 64;
        int sample = 65;
        if (args.length > 0){
            loop = Integer.parseInt(args[0]);
        }
        //maxSize , Sample 一同配置
        if (args.length > 2){
            maxSize = Integer.parseInt(args[1]);
            sample = Integer.parseInt(args[2]);
        }
        lruCacheIssue(loop,maxSize,sample);
    }
    private static void lruCacheIssue(int loop,int maxSize,int sample){
        List<Long> metrics1 = Collections.synchronizedList(new ArrayList<>(loop * sample));
        LruCache<String,Object> concurrentLruCache = new ConcurrentLruCache<String, Object>(maxSize, v -> v.hashCode(),metrics1);
        System.out.println("有问题的 ConcurrentLruCache 版本总耗时 ： "+benchmarkLruCache(loop,sample,concurrentLruCache).stream()
                //     .peek(System.out::println)
                .mapToLong(v -> v)
                .summaryStatistics());

        System.out.println("有问题的 ConcurrentLruCache 写次数与耗时 ： "+metrics1.stream().mapToLong(v -> v).summaryStatistics());

        List<Long> metrics2 = Collections.synchronizedList(new ArrayList<>(loop * sample));
        LruCache<String,Object> optimizeConcurrentLruCache = new OptimizeConcurrentLruCache<String, Object>(maxSize, v -> v.hashCode(),metrics2);
        System.out.println("优化后的 ConcurrentLruCache 版本总耗时 ： "+benchmarkLruCache(loop,sample,optimizeConcurrentLruCache).stream()
                //     .peek(System.out::println)
                .mapToLong(v -> v)
                .summaryStatistics());

        System.out.println("优化后的 ConcurrentLruCache 写次数与耗时 ： "+metrics2.stream().mapToLong(v -> v).summaryStatistics());
    }

    /**
     *
     * @param loop 循环次数
     * @param sample 模拟的VALUE不同种类数
     * @param lruCache 测试的Lru实现
     * @return
     */
    private static List<Long> benchmarkLruCache(int loop , int sample , LruCache<String,Object> lruCache){
        List<Long> times = Collections.synchronizedList(new ArrayList<>(loop));
        CountDownLatch countDownLatch = new CountDownLatch(loop);
        ExecutorService executor = Executors.newFixedThreadPool(100);
        IntStream.rangeClosed(1,loop)
                .forEach(i ->{
                    executor.execute(() -> {
                        long s = System.currentTimeMillis();
                        IntStream.rangeClosed(1,sample)
                                .forEach(n -> {
                                    String v = n+"";
                                    if(!lruCache.get(v).equals(v .hashCode())){
                                        throw new RuntimeException("结果错误");
                                    }
                                    times.add((System.currentTimeMillis() - s));
                                });
                        countDownLatch.countDown();
                    });
                });
        executor.shutdown();
        try {
            countDownLatch.await();
        }catch (InterruptedException e){}
        return times;
    }

    /**
     * 并发的Lur Cache
     * @param <K>
     * @param <V>
     */
    private static class ConcurrentLruCache<K, V> implements LruCache<K, V>{

        private final int maxSize;

        private final ConcurrentLinkedQueue<K> queue = new ConcurrentLinkedQueue<>();

        private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        private final Function<K, V> generator;

        private final List<Long> metrics;

        public ConcurrentLruCache(int maxSize, Function<K, V> generator , List<Long> metrics) {
            Assert.isTrue(maxSize > 0, "LRU max size should be positive");
            Assert.notNull(generator, "Generator function should not be null");
            this.maxSize = maxSize;
            this.generator = generator;
            this.metrics = metrics;
        }

        @Override
        public V get(K key) {
            this.lock.readLock().lock();
            try {
                if (this.queue.size() < this.maxSize / 2) {
                    V cached = this.cache.get(key);
                    if (cached != null) {
                        return cached;
                    }
                }
                else if (this.queue.remove(key)) {
                    this.queue.add(key);
                    return this.cache.get(key);
                }
            }
            finally {
                this.lock.readLock().unlock();
            }



            this.lock.writeLock().lock();
            long s = System.currentTimeMillis();
            try {


                // retrying in case of concurrent reads on the same key
                if (this.queue.remove(key)) {
                    this.queue.add(key);
                    return this.cache.get(key);
                }
                if (this.queue.size() == this.maxSize) {
                    K leastUsed = this.queue.poll();
                    if (leastUsed != null) {
                        this.cache.remove(leastUsed);
                    }
                }
                V value = this.generator.apply(key);
                this.queue.add(key);
                this.cache.put(key, value);
                return value;
            }
            finally {
                metrics.add(System.currentTimeMillis()-s);
                this.lock.writeLock().unlock();
            }
        }
    }


    private static class OptimizeConcurrentLruCache<K, V> implements LruCache<K, V>{

        private final int maxSize;

        private final ConcurrentLinkedDeque<K> queue = new ConcurrentLinkedDeque<>();

        private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

        private final ReadWriteLock lock;

        private final Function<K, V> generator;

        private volatile int size = 0;

        private final List<Long> metrics;

        public OptimizeConcurrentLruCache(int maxSize, Function<K, V> generator ,List<Long> metrics ) {
            Assert.isTrue(maxSize > 0, "LRU max size should be positive");
            Assert.notNull(generator, "Generator function should not be null");
            this.maxSize = maxSize;
            this.generator = generator;
            this.lock = new ReentrantReadWriteLock();
            this.metrics = metrics;
        }

        @Override
        public V get(K key) {
            V cached = this.cache.get(key);
            if (cached != null) {
                if (this.size < this.maxSize) {
                    return cached;
                }
                this.lock.readLock().lock();
                try {
                    if (this.queue.removeLastOccurrence(key)) {
                        this.queue.offer(key);
                    }
                    return cached;
                }
                finally {
                    this.lock.readLock().unlock();
                }
            }
            this.lock.writeLock().lock();
            long s = System.currentTimeMillis();
            try {
                // Retrying in case of concurrent reads on the same key
                cached = this.cache.get(key);
                if (cached  != null) {
                    if (this.queue.removeLastOccurrence(key)) {
                        this.queue.offer(key);
                    }
                    return cached;
                }
                // Generate value first, to prevent size inconsistency
                V value = this.generator.apply(key);
                int cacheSize = this.size;
                if (cacheSize == this.maxSize) {
                    K leastUsed = this.queue.poll();
                    if (leastUsed != null) {
                        this.cache.remove(leastUsed);
                        cacheSize--;
                    }
                }
                this.queue.offer(key);
                this.cache.put(key, value);
                this.size = cacheSize + 1;
                return value;
            }
            finally {
                metrics.add(System.currentTimeMillis() - s);
                this.lock.writeLock().unlock();
            }
        }
    }

    private interface LruCache<K,V> {
        V get(K key);
    }
}
