package com.zxz.like.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;


/**
 * 定义了一系列用于维护和操作 top-k 数据结构的方法
 * top-k 数据结构通常用于找出一组数据中出现频率最高的前 k 个元素。
 */
public interface TopK {

    //添加元素并更新TopK结构
    AddResult add(String key, int increment);

    //返回当前TopK元素的列表
    List<Item> list();

    //获取被挤出TopK的元素的队列
    BlockingQueue<Item> expelled();

    //对所有计数进行衰减
    void fading();

    long total();
}
