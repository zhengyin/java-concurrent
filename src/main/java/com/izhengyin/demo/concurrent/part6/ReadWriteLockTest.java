package com.izhengyin.demo.concurrent.part6;

import com.izhengyin.demo.concurrent.SleepUtils;
import org.springframework.util.Assert;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-18 08:17
 */
public class ReadWriteLockTest {
    public static void main(String[] args){
    //    readLock();
    //   writeLock();
        lruCacheIssue();
    }

    /**
     * 读锁可以被多个线程获取,但当写锁被占有时，读锁的获取都将被阻塞。
     */
    private static void readLock(){
        ReentrantReadWriteLock wrl = new ReentrantReadWriteLock();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        IntStream.rangeClosed(1,10)
                .forEach(i -> executorService.execute(() -> {
                    wrl.readLock().lock();
                    try {
                        System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" Get Read Lock");
                        //mock delay
                        SleepUtils.sleep(100);
                    }finally {
                        wrl.readLock().unlock();

                        if(i  == 5){
                            wrl.writeLock().lock();
                            try {
                                System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" Get Write Lock");
                                SleepUtils.sleep(3000);
                            }finally {
                                wrl.writeLock().unlock();
                            }
                        }

                    }
                }));
        executorService.shutdown();
    }

    /**
     * 写锁的获取是独占的,可重入的。除此之外，如果当前线程已获得读锁，写锁的获取同样也会被阻塞。
     */
    private static void writeLock(){
        ReentrantReadWriteLock wrl = new ReentrantReadWriteLock();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        IntStream.rangeClosed(1,10)
                .forEach(i -> executorService.execute(() -> {
                    wrl.writeLock().lock();
                    try {
                        System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" Get Write Lock");
                        //mock delay
                        SleepUtils.sleep(100);
                    }finally {
                        wrl.writeLock().unlock();
                    }
                }));
        executorService.shutdown();

        SleepUtils.sleep(1000);


        wrl.readLock().lock();
        try {
            System.out.println("Main Get Read Lock");
        }finally {
            //注释以后将被阻塞
            wrl.readLock().unlock();
        }

        wrl.writeLock().lock();
        try {
            System.out.println("Main Get Write Lock");
        }finally {
            wrl.writeLock().unlock();
        }

    }



    private static void lruCacheIssue(){
        LruCache<String,Object> concurrentLruCache = new ConcurrentLruCache<String, Object>(64, v -> v.hashCode());
        System.out.println("ConcurrentLruCache 总耗时 ： "+benchmarkLruCache(1000,75,concurrentLruCache).stream()
           //     .peek(System.out::println)
                .mapToLong(v -> v)
                .sum());

        LruCache<String,Object> optimizeConcurrentLruCache = new OptimizeConcurrentLruCache<String, Object>(64, v -> v.hashCode());
        System.out.println("OptimizeConcurrentLruCache 总耗时 ： "+benchmarkLruCache(1000,75,optimizeConcurrentLruCache).stream()
           //     .peek(System.out::println)
                .mapToLong(v -> v)
                .sum());
    }


    private static List<Long> benchmarkLruCache(int loop , int sample , LruCache<String,Object> lruCache){
        List<Long> times = new ArrayList<>(loop + 1);
        CountDownLatch countDownLatch = new CountDownLatch(loop);
        ExecutorService executor = Executors.newFixedThreadPool(100);
        IntStream.rangeClosed(1,loop)
                .forEach(i ->
                        executor.execute(() -> {
                            long s = System.currentTimeMillis();
                            IntStream.rangeClosed(1,sample)
                                    .forEach(n -> lruCache.get(n+""));
                            times.add((System.currentTimeMillis() - s));
                            countDownLatch.countDown();
                        })
                );
        executor.shutdown();
        try {
            countDownLatch.await();
        }catch (InterruptedException e){}
        return times;
    }

    /**
     * cache
     * 通过对读写使用不同的所策略，兼顾读的并发性，也兼顾写操作对应读操作的可见性。
     */
    private final static class ConcurrentCache {
        private final Map<String,Object> map = new HashMap<>();
        private final ReentrantReadWriteLock wrl = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock;
        private final ReentrantReadWriteLock.WriteLock writeLock;

        public ConcurrentCache(){
            this.readLock = wrl.readLock();
            this.writeLock = wrl.writeLock();
        }

        /**
         * 读的时候加读锁，多线程读不阻塞
         * @param key
         * @return
         */
        public Object get(String key){
            readLock.lock();
            try {
                return map.get(key);
            }finally {
                readLock.unlock();
            }
        }

        /**
         * 写的时候加写锁，写锁同时阻塞读锁，这样保证写完后的最新数据会第一时间被读到
         * @param key
         * @param supplier
         * @return
         */
        public Object set(String key , Supplier<Object> supplier){
            writeLock.lock();
            try {
                return map.put(key, supplier.get());
            }finally {
                writeLock.unlock();
            }
        }

        /**
         * 同 set
         */
        public void  clear(){
            writeLock.lock();
            try {
                map.clear();
            }finally {
                writeLock.unlock();
            }

        }
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

        public ConcurrentLruCache(int maxSize, Function<K, V> generator) {
            Assert.isTrue(maxSize > 0, "LRU max size should be positive");
            Assert.notNull(generator, "Generator function should not be null");
            this.maxSize = maxSize;
            this.generator = generator;
        }

        @Override
        public V get(K key) {

            this.lock.readLock().lock();
            try {
                //大于一半时，
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

        public OptimizeConcurrentLruCache(int maxSize, Function<K, V> generator) {
            Assert.isTrue(maxSize > 0, "LRU max size should be positive");
            Assert.notNull(generator, "Generator function should not be null");
            this.maxSize = maxSize;
            this.generator = generator;
            this.lock = new ReentrantReadWriteLock();
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
                this.lock.writeLock().unlock();
            }
        }
    }

    private interface LruCache<K,V> {
        V get(K key);
    }
}