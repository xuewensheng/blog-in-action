package com.spring.boot.blog.initializrstart.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MyCallable implements Callable<String> {

    @Override
    public String call() throws Exception {
        return "Hello, I am the defined thread created by implements Callable";
    }

    public static void main(String[] agrs){

        MyCallable myCallable = new MyCallable();

        FutureTask<String> futureTask = new FutureTask<>(myCallable);

        Thread thread = new Thread(futureTask);

        thread.start();

        String result = "";
        try{
            result = futureTask.get();
            System.out.println(result);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
