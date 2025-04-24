package com.zxz.like.manager.cache;


/**
 * 记录类是一种特殊的类，用于封装不可变的数据。
 * @param key   用于存储某个元素的键
 * @param count 用于存储该元素对应的计数
 */
public record Item(String key, int count) {}