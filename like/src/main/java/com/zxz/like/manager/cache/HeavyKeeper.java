package com.zxz.like.manager.cache;

import cn.hutool.core.util.HashUtil;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HeavyKeeper implements TopK {
    private static final int LOOKUP_TABLE_SIZE = 256;  //一个常量，用于定义查找表的大小，值为 256
    private final int k;  //表示要找出的前 k 个元素
    private final int width;  //用于定义二维数组 buckets 的宽度和深度
    private final int depth;  
    private final double[] lookupTable;  //double 类型的数组，作为查找表，用于存储衰减因子的幂次值
    private final Bucket[][] buckets;  //二维数组，元素类型为 Bucket，用于存储元素的指纹和计数信息
    private final PriorityQueue<Node> minHeap;  //优先队列（最小堆），用于维护当前的前 k 个元素，按照计数从小到大排序
    private final BlockingQueue<Item> expelledQueue;  //阻塞队列，用于存储被挤出前 k 的元素
    private final Random random;    //随机数生成器
    private long total;  //记录所有元素的总计数
    private final int minCount;  //最小计数阈值，在添加元素时可能会用到
  
    public HeavyKeeper(int k, int width, int depth, double decay, int minCount) {  
        this.k = k;  
        this.width = width;  
        this.depth = depth;  
        this.minCount = minCount;  

        //初始化查找表 lookupTable，存储衰减因子的幂次值
        this.lookupTable = new double[LOOKUP_TABLE_SIZE];
        for (int i = 0; i < LOOKUP_TABLE_SIZE; i++) {  
            lookupTable[i] = Math.pow(decay, i);  
        }  

        //初始化二维数组 buckets，为每个位置创建一个 Bucket 对象
        this.buckets = new Bucket[depth][width];  
        for (int i = 0; i < depth; i++) {  
            for (int j = 0; j < width; j++) {  
                buckets[i][j] = new Bucket();  
            }  
        }  

        //初始化最小堆 minHeap，使用 Comparator.comparingInt(n -> n.count) 按照元素的计数从小到大排序
        this.minHeap = new PriorityQueue<>(Comparator.comparingInt(n -> n.count));
        this.expelledQueue = new LinkedBlockingQueue<>();   //初始化阻塞队列 expelledQueue
        this.random = new Random();  //初始化随机数生成器 random
        this.total = 0;  //将总计数 total 初始化为 0
    }


    /**
     * 该方法返回当前的前 k 个元素列表
     * @return
     */
    @Override
    public List<Item> list() {
        synchronized (minHeap) {  //使用 synchronized 块确保线程安全，因为在多线程环境下可能会同时访问和修改 minHeap
            List<Item> result = new ArrayList<>(minHeap.size());
            for (Node node : minHeap) {     //遍历 minHeap，将每个 Node 对象转换为 Item 对象并添加到结果列表中
                result.add(new Item(node.key, node.count));  
            }  
            result.sort((a, b) -> Integer.compare(b.count(), a.count()));  //对结果列表进行排序，按照计数从大到小排列
            return result;  
        }  
    }

    /**
     * 该方法返回存储 被挤出前 k 元素的阻塞队列 expelledQueue
     * @return
     */
    @Override  
    public BlockingQueue<Item> expelled() {  
        return expelledQueue;  
    }

    /**
     * 该方法用于对所有元素的计数进行衰减操作
     */
    @Override  
    public void fading() {  
        for (Bucket[] row : buckets) {  //遍历二维数组 buckets
            for (Bucket bucket : row) {  
                synchronized (bucket) {  //使用 synchronized 块确保线程安全
                    bucket.count = bucket.count >> 1;  //对每个 Bucket 对象的计数进行右移一位操作（相当于除以 2）
                }  
            }  
        }  
          
        synchronized (minHeap) {  
            PriorityQueue<Node> newHeap = new PriorityQueue<>(Comparator.comparingInt(n -> n.count));  
            for (Node node : minHeap) {  //遍历最小堆 minHeap
                newHeap.add(new Node(node.key, node.count >> 1));  //将每个 Node 对象的计数右移一位
            }               //创建新的 Node 对象添加到新的优先队列 newHeap 中
            minHeap.clear();  //清空原最小堆 minHeap
            minHeap.addAll(newHeap);  //并将新的优先队列 newHeap 中的元素添加到 minHeap 中
        }  
          
        total = total >> 1;  //将总计数 total 右移一位（相当于除以 2）
    }

    /**
     * 该方法返回所有元素的总计数 total
     * @return
     */
    @Override  
    public long total() {  
        return total;  
    }


    /**
     * Bucket 类用于存储元素的指纹（fingerprint）和计数（count）信息
     */
    private static class Bucket {  
        long fingerprint;  
        int count;  
    }

    /**
     * Node 类用于存储元素的键（key）和计数（count）信息，在最小堆 minHeap 中使用
     */
    private static class Node {  
        final String key;  
        final int count;  
          
        Node(String key, int count) {  
            this.key = key;  
            this.count = count;  
        }  
    }

    /**
     * hash 方法用于对字节数组 data 进行哈希计算，调用 HashUtil 类的 murmur32 方法实现
     * @param data
     * @return
     */
    private static int hash(byte[] data) {  
        return HashUtil.murmur32(data);
    }



    /**
     * add方法是 HeavyKeeper 算法的核心，主要作用是添加元素并更新 TopK 结构。
     * @param key
     * @param increment
     * @return
     */
    // 实现了向 HeavyKeeper 数据结构中添加一个键值对（键为 key，增量为 increment）
    // 并根据添加后的结果更新内部的状态，包括 buckets、minHeap 和 expelledQueue 等
    @Override
    public AddResult add(String key, int increment) {

        byte[] keyBytes = key.getBytes();   //首先将传入的 key 转换为字节数组 keyBytes
        long itemFingerprint = hash(keyBytes);  //计算 key 的指纹 itemFingerprint，通过调用 hash 方法对字节数组进行哈希处理
        int maxCount = 0;   //初始化 maxCount 为 0，用于记录在遍历 buckets 过程中遇到的最大计数

        for (int i = 0; i < depth; i++) {   //外层循环遍历 buckets 的每一行（由 depth 决定）
            int bucketNumber = Math.abs(hash(keyBytes)) % width;    //计算当前行中对应的桶的编号 bucketNumber，通过对 key 的哈希值取绝对值后对 width 取模得到
            Bucket bucket = buckets[i][bucketNumber];   //获取对应的 Bucket 对象

            synchronized (bucket) {
                //检查当前 Bucket 的计数 bucket.count
                if (bucket.count == 0) {    //如果 bucket.count 为 0，说明该桶为空
                    bucket.fingerprint = itemFingerprint;
                    bucket.count = increment;
                    maxCount = Math.max(maxCount, increment);
                } else if (bucket.fingerprint == itemFingerprint) { //如果相等，说明该桶中存储的是要添加的键
                    bucket.count += increment;
                    maxCount = Math.max(maxCount, bucket.count);
                } else {    //如果以上两种情况都不满足，说明该桶中存储的是其他键
                    for (int j = 0; j < increment; j++) {   //通过内层循环，根据衰减因子进行概率性的计数减少操作
                        double decay = bucket.count < LOOKUP_TABLE_SIZE ?
                                lookupTable[bucket.count] :
                                lookupTable[LOOKUP_TABLE_SIZE - 1];
                        if (random.nextDouble() < decay) {
                            bucket.count--;
                            if (bucket.count == 0) {
                                bucket.fingerprint = itemFingerprint;
                                bucket.count = increment - j;
                                maxCount = Math.max(maxCount, bucket.count);
                                break;
                            }
                        }
                    }
                }
            }
        }

        total += increment; //将 increment 加到 total 中，更新总计数

        if (maxCount < minCount) {  //说明添加的键值对的计数未达到最小计数阈值
            return new AddResult(null, false, null);
        }

        synchronized (minHeap) {
            boolean isHot = false;  //初始化 isHot 为 false，表示当前键是否为热门键
            String expelled = null; //初始化 expelled 为 null，表示被挤出的键

            //使用流操作在 minHeap 中查找是否存在与 key 相等的 Node
            Optional<Node> existing = minHeap.stream()
                    .filter(n -> n.key.equals(key))
                    .findFirst();

            //如果存在，从 minHeap 中移除该 Node，并添加一个新的 Node，其键为 key，计数为 maxCount，并将 isHot 设置为 true
            if (existing.isPresent()) {
                minHeap.remove(existing.get());
                minHeap.add(new Node(key, maxCount));
                isHot = true;
            } else {    //如果不存在
                //检查 minHeap 的大小是否小于 k 或者 maxCount 是否大于等于 minHeap 中最小计数的 Node 的计数
                if (minHeap.size() < k || maxCount >= Objects.requireNonNull(minHeap.peek()).count) {
                    Node newNode = new Node(key, maxCount); //如果满足条件，创建一个新的 Node，其键为 key，计数为 maxCount
                    if (minHeap.size() >= k) {  //如果 minHeap 的大小已经达到 k，则从 minHeap 中移除最小计数的 Node
                        expelled = minHeap.poll().key;  //将其键赋值给 expelled
                        expelledQueue.offer(new Item(expelled, maxCount));  //并将该键值对添加到 expelledQueue 中
                    }
                    minHeap.add(newNode);   //然后将新的 Node 添加到 minHeap 中
                    isHot = true;   //并将 isHot 设置为 true
                }
            }

            return new AddResult(expelled, isHot, key);
        }
    }


 }




