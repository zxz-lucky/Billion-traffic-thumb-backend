package com.zxz.like.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface TopK {

    AddResult add(String key, int increment);

    List<Item> list();  //返回当前TopK元素的列表

    BlockingQueue<Item> expelled(); //获取被挤出TopK的元素的队列

    void fading();  //对所有计数进行衰减

    long total();
}
