package com.izhengyin.demo.concurrent.basic.thread.state;

/**
 *
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 14:51
 */
public class WaitState {
    /**
     * "com.izhengyin.demo.concurrent.basic.thread.state.WaitState" #10 prio=5 os_prio=31 tid=0x00007f8985841800 nid=0x5503 in Object.wait() [0x000070000716d000]
     *    java.lang.Thread.State: WAITING (on object monitor)
     * 	at java.lang.Object.wait(Native Method)
     * 	- waiting on <0x000000076abb3648> (a java.lang.Class for com.izhengyin.demo.concurrent.basic.thread.state.WaitState$Wait)
     * 	at java.lang.Object.wait(Object.java:502)
     * 	at com.izhengyin.demo.concurrent.basic.thread.state.WaitState$Wait.run(WaitState.java:18)
     * 	- locked <0x000000076abb3648> (a java.lang.Class for com.izhengyin.demo.concurrent.basic.thread.state.WaitState$Wait)
     * 	at java.lang.Thread.run(Thread.java:745)
     *  @param args
     */
    public static void main(String[] args) {
        new Thread(new Wait(), WaitState.class.getName()).start();
    }

    static class Wait implements Runnable{
        @Override
        public void run() {
            synchronized (Wait.class){
                try {
                    Wait.class.wait();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
