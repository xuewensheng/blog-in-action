package com.spring.boot.blog.initializrstart.thread;

import java.util.*;

public class MyThread  extends  Thread{


    @Override
    public void run(){
        System.out.println("Hello, I am the defined thread created by extends Thread");
    }
    public static void main(String[] agrs){

        Thread myThread = new MyThread();
        myThread.start();

        List<Integer>  list = Arrays.asList(1,2,3,4,5);
        Integer a=0;
       System.out.println(list.stream().reduce(a,Integer::sum));

       //Supplier 没有接受数据只有返回
       //Consumer 只接受数据，不返回数据
       list.stream().forEach(System.out::println);
       list.forEach(value -> {
           System.out.println(value);
       });
       //Predicate -> 判断（挑选偶数）
       list.stream().filter(value -> value%2==0).forEach(System.out::println);
       //Function ->转换(Integer->Integer*2)
        list.stream().filter(value -> value%2==0).map(value -> value*2);

        List<String> helloWord = Arrays.asList("a,b","c,d,e");

        helloWord.forEach(value -> {
            System.out.println(value);
        });
        helloWord.stream().map(value -> value.split(","))
                //map 一维变二维 ["a,b","c,d,e"] ->[[a,b],[c,d,e]]
                .flatMap(values ->Arrays.stream(values))
                //map 二维变一维 [[a,b],[c,d,e]]->["a","b","c","d","e"]
              .forEach(System.out::println);


    }
}
