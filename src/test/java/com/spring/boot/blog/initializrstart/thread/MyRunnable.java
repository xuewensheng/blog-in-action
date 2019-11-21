package com.spring.boot.blog.initializrstart.thread;

import java.util.HashMap;

public class MyRunnable  implements Runnable{


    @Override
    public void run() {
        System.out.println("Hello, I am the defined thread created by implements runnable");
    }

    public static void main(String[] agrs){

    MyRunnable  myRunnable = new MyRunnable();

    Thread thread = new Thread(myRunnable);
    thread.start();

    }
}
