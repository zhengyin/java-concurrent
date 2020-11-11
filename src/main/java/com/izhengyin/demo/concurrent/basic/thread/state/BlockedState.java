package com.izhengyin.demo.concurrent.basic.thread.state;

import com.izhengyin.demo.concurrent.Sleep;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 15:11
 */
public class BlockedState {
    public static void main(String[] args) {
        /**
         *
         * "Blocked1" #10 prio=5 os_prio=31 tid=0x00007fa3e583a800 nid=0x5503 waiting on condition [0x0000700011181000]
         *    java.lang.Thread.State: TIMED_WAITING (sleeping)
         * 	at java.lang.Thread.sleep(Native Method)
         * 	at java.lang.Thread.sleep(Thread.java:340)
         * 	at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:386)
         * 	at com.izhengyin.demo.concurrent.Sleep.second(Sleep.java:12)
         * 	at com.izhengyin.demo.concurrent.basic.thread.state.BlockedState$Blocked.run(BlockedState.java:21)
         * 	- locked <0x000000076abae6a8> (a java.lang.Class for com.izhengyin.demo.concurrent.basic.thread.state.BlockedState)
         * 	at java.lang.Thread.run(Thread.java:745)
         */
        new Thread(new Blocked(),"Blocked1").start();
        /**
         * "Blocked2" #11 prio=5 os_prio=31 tid=0x00007fa3e4004800 nid=0xa903 waiting for monitor entry [0x0000700011284000]
         *    java.lang.Thread.State: BLOCKED (on object monitor)
         * 	at com.izhengyin.demo.concurrent.basic.thread.state.BlockedState$Blocked.run(BlockedState.java:21)
         * 	- waiting to lock <0x000000076abae6a8> (a java.lang.Class for com.izhengyin.demo.concurrent.basic.thread.state.BlockedState)
         * 	at java.lang.Thread.run(Thread.java:745)
         *    Locked ownable synchronizers:
         * 	- None
         */
        new Thread(new Blocked(),"Blocked2").start();
    }

    static class Blocked implements Runnable{
        @Override
        public void run() {
            synchronized (BlockedState.class){
                //不释放锁
                while (true){
                    Sleep.second(1);
                }
            }
        }
    }
}
