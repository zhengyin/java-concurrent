package com.izhengyin.demo.concurrent.basic.thread.state;

import com.izhengyin.demo.concurrent.Sleep;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 15:29
 */
public class TerminatedState {
    public static void main(String[] args) {
        TimeWaiting timeWaiting = new TimeWaiting();
        Thread thread = new Thread(timeWaiting, TimeWaitState.class.getName());
        thread.start();
        Sleep.second(3);
        /**
         * 抛出异常
         * Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity. Occasionally a method may wish to test whether the current thread has been interrupted, and if so, to immediately throw this exception.
         */
        thread.interrupt();
        System.out.println(thread.getName()+" interrupted "+thread.isInterrupted());

        Sleep.second(3);
        //通过开关来控制进程的执行
        timeWaiting.setbStop(true);
    }

    static class TimeWaiting implements Runnable{
        private boolean bStop = false;
        @Override
        public void run() {
            while (!bStop){
                Sleep.second(1);
                System.out.println(System.currentTimeMillis() +" TimeWaiting ...");
            }

            System.out.println(Thread.currentThread().getName()+" exit!");
        }

        public void setbStop(boolean bStop) {
            this.bStop = bStop;
        }
    }
}
