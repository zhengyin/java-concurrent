package com.izhengyin.demo.concurrent.basic.thread.pool;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 16:29
 */
public class MyHttpClientPool {
    private final static OkHttpClient okHttpClient = new OkHttpClient();
    private int initSize;
    private LinkedList<Connection> connections = new LinkedList<>();
    public MyHttpClientPool(int initSize){
        this.initSize = initSize;
        createConnections();
    }

    /**
     * 执行请求
     * @param request
     * @return
     */
    public Response call(Request request, long timeout , long fetchConnectionTimeout){
        Connection connection = getConnection(fetchConnectionTimeout);
        try {
            connection.setRequest(request);
            Thread thread = new Thread(connection);
            thread.start();
            long timeoutMills = System.currentTimeMillis() + timeout;
            try {
                thread.join(timeout);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(System.currentTimeMillis() >= timeoutMills){
                throw new RuntimeException("执行超时!");
            }
            return connection.getResponse();
        }finally {
            releaseConnection(connection);
        }

    }

    /**
     * 获取连接
     * @return
     */
    private Connection getConnection(long mills){
        Assert.isTrue(mills > 0,"获取连接超时参数 [mills] 必须大于0");
        long timeoutMills = System.currentTimeMillis() + mills;
        synchronized (connections){
            while (true){
                if(connections.isEmpty() && timeoutMills > System.currentTimeMillis()){
                    try {
                        connections.wait();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }else if(timeoutMills < System.currentTimeMillis()){
                    throw new RuntimeException("获取连接超时");
                }else{
                    Connection connection = connections.removeFirst();
                    if(Objects.isNull(connection)){
                        throw new RuntimeException("没有可用的连接");
                    }
                    return connection;
                }
            }
        }
    }

    /**
     * 释放连接
     * @return
     */
    private void releaseConnection(Connection connection){
        synchronized (connections){
            if(connections.add(connection)){
                connections.notifyAll();
            }
        }
    }

    private void createConnections(){
        for (int i = 0; i < initSize; i++){
            connections.add(new Connection(i));
        }
    }


    private static class Connection implements Runnable{
        private int id;
        private Request request;
        private Response response;
        public Connection(int id){
            this.id = id;
        }
        @Override
        public void run() {
            Objects.requireNonNull(request,"request cannot be null!");
            System.out.println(Connection.class.getName()+"["+this.id+"] , "+request.toString());
            try ( Response response = okHttpClient.newCall(this.request).execute()){
                this.response = response;
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                request = null;
            }
        }

        public void setRequest(Request request) {
            this.request = request;
        }

        public Response getResponse() {
            try {
                return response;
            }finally {
                response = null;
            }
        }
    }
}
