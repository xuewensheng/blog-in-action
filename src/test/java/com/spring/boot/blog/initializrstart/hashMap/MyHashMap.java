package com.spring.boot.blog.initializrstart.hashMap;

import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyHashMap<K,V>  implements MyMap{

    //定义默认数组的大小2^n
    private static  int defaultLength = 1<<4;

    //扩容标准所使用的useSize/数组长度>0.75
    private static double defaultAddSizeFactory = 0.75;
    //使用数组的位置
    private int useSize;

    private Entry<K,V>[] table = null;

    //spring 门面模式运用
    public MyHashMap(){
        this(defaultLength,defaultAddSizeFactory);
    }

    public MyHashMap(int defaultLength, double defaultAddSizeFactory ){

        this.defaultLength = defaultLength;
        this.defaultAddSizeFactory = defaultAddSizeFactory;
        table = new Entry[defaultLength];
    }

    @Override
    public V put(Object k, Object v) {
        //判断是否需要扩容
        if(useSize>defaultLength*defaultAddSizeFactory){
         upSize();
        }
        int index =  getIndex((K) k,table.length);
        Entry<K,V> entry = table[index];
        if(entry == null){
            table[index] = new Entry(k,v,null);
            useSize++;
        }else if(entry != null){
            table[index] = new Entry(k,v,entry);
        }
        return table[index].getValue();
    }

    private int getIndex(K k, int length){

        int m = length-1;
        int index = hash(k.hashCode())&m;
        return index;
    }

    private int hash(int hashCode){
     hashCode = hashCode^((hashCode>>>20)^(hashCode>>>12));
     return hashCode^((hashCode>>>7)^(hashCode>>>4));
    }

    private void upSize(){
     Entry<K,V>[] newTable = new Entry[2*defaultLength];
     againHash(newTable);
    }

    private void againHash(Entry<K,V>[] newTable){

        List<Entry<K,V>> entryList = new ArrayList<Entry<K,V>>();
        for(int i=0 ; i<table.length; i++){
            if(table[i] == null){
                continue;
            }
            foundEntryByNext(table[i] ,entryList);
        }
        if(entryList.size()>0){
            useSize = 0;
            defaultLength = 2*defaultLength;
            table = newTable;
            for(Entry<K,V> entry: entryList){
                if(entry.next != null){
                    entry.next = null;
                }
                put(entry.getKey(),entry.getValue());
            }
        }
    }

    private void foundEntryByNext(Entry<K,V> entry, List<Entry<K,V>> entryList ){
        if(entry !=null && entry.next != null){
            entryList.add(entry);
            foundEntryByNext(entry.next,entryList);
        }else{
            entryList.add(entry);
        }
    }
    @Override
    public V get(Object k) {
        int index = getIndex((K)k,table.length);
        if(table[index] == null){
            throw new NullPointerException();
        }
        return findValueByEqualKey((K)k,table[index]);
    }
    public V findValueByEqualKey(K k, Entry<K,V> entry){
        if(k == entry.getKey() || k.equals(entry.getKey())){
            return entry.getValue();
        }else if(entry.next != null){
           return findValueByEqualKey(k,entry.next);
        }
        return null;
    }



    class Entry<K,V> implements MyMap.Entry<K,V>{
        K k;
        V v;
        Entry<K,V> next;
        public  Entry(K k,V v,Entry<K,V> next){
            this.k = k;
            this.v = v;
            this.next = next;
        }
        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }
    }

    public static void main(String[] agrs){
        MyHashMap<String,String> myMap = new MyHashMap<String,String>();
        for(int i=0;i<1000;i++){
            myMap.put("key"+i,"value"+i);
            System.out.println(myMap.get("key"+i));
        }
    }
}
