package com.spring.boot.blog.initializrstart.hashMap;

public interface MyMap<K,V> {
    public V put(K k, V v);

    public V get(K k);

    //定义个内部接口
    public interface Entry<K,V>{
        public K getKey();

        public V getValue();
    }
}
