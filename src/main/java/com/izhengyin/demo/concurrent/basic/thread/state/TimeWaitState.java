package com.izhengyin.demo.concurrent.basic.thread.state;

import com.izhengyin.demo.concurrent.Sleep;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 14:51
 */
public class TimeWaitState {
    /**
     *  "com.izhengyin.demo.concurrent.basic.thread.state.TimeWaitState" #10 prio=5 os_prio=31 tid=0x00007fa2b6868000 nid=0x5503 waiting on condition [0x0000700010b79000]
     *    java.lang.Thread.State: TIMED_WAITING (sleeping)
     * 	at java.lang.Thread.sleep(Native Method)
     * 	at java.lang.Thread.sleep(Thread.java:340)
     * 	at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:386)
     * 	at com.izhengyin.demo.concurrent.Sleep.second(Sleep.java:12)
     * 	at com.izhengyin.demo.concurrent.basic.thread.state.TimeWaitState$TimeWaiting.run(TimeWaitState.java:22)
     * 	at java.lang.Thread.run(Thread.java:745)
     * @param args
     */
    public static void main(String[] args) {
        new Thread(new TimeWaiting(), TimeWaitState.class.getName()).start();
    }

    static class TimeWaiting implements Runnable{
        @Override
        public void run() {
            while (true){
                Sleep.second(1);
            }
        }
    }
}
