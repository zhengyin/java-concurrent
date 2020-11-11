package com.izhengyin.demo.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 14:31
 */
public class Sleep {
    public static void second(int second){
        try {
            TimeUnit.SECONDS.sleep(second);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}
