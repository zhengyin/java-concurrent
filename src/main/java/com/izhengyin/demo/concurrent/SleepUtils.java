package com.izhengyin.demo.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin
 */
public class SleepUtils {
    public static void sleep(long timeout){
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
