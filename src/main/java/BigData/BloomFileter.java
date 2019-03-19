package BigData;

import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public class BloomFileter {private static final long serialVersionUID = -5221305273707291280L;
    private final int[] seeds;
    private final int size;
    private final MisjudgmentRate rate;
    private final AtomicInteger useCount = new AtomicInteger(0);
    private final Double autoClearRate;
    private Jedis client;
    private final String key;

    /**
     * 默认中等程序的误判率：MisjudgmentRate.MIDDLE 以及不自动清空数据（性能会有少许提升）
     *
     * @param dataCount
     *            预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     */
    public BloomFileter(Jedis client,int dataCount,String key) {
        // 中等程序的误判率 MisjudgmentRate.MIDDLE

        this(client,MisjudgmentRate.MIDDLE, dataCount, null,key);

    }
    /**
     *
     * @param rate
     *            一个枚举类型的误判率
     * @param dataCount
     *            预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     * @param autoClearRate
     *            自动清空过滤器内部信息的使用比率，传null则表示不会自动清理，
     *            当过滤器使用率达到100%时，则无论传入什么数据，都会认为在数据已经存在了
     *            当希望过滤器使用率达到80%时自动清空重新使用，则传入0.8
     */
    public BloomFileter(Jedis jedis,MisjudgmentRate rate, int dataCount, Double autoClearRate,String key) {
        long bitSize = rate.seeds.length * dataCount;

        if (bitSize < 0 || bitSize > Integer.MAX_VALUE) {
            throw new RuntimeException("位数太大溢出了，请降低误判率或者降低数据大小");
        }
        client = jedis;
        this.key = key;
        this.rate = rate;
        seeds = rate.seeds;
        size = (int) bitSize;
        client.setbit(key, size, false);
        this.autoClearRate = autoClearRate;
    }

    public void add(String data) {
        // 检查是否超过使用率
        checkNeedClear();
        for (int i = 0; i < seeds.length; i++) {
            int index = hash(data, seeds[i]);
            setTrue(index);
        }
    }

    public boolean addIfNotExist(String data) {
        checkNeedClear();
        int[] indexs = new int[seeds.length];
        // 先假定存在
        boolean exist = true;
        int index;

        for (int i = 0; i < seeds.length; i++) {
            indexs[i] = index = hash(data, seeds[i]);

            if (exist) {

                if (!client.getbit(key,index)) {
                    // 只要有一个不存在，就可以认为整个字符串都是第一次出现的
                    exist = false;
                    // 补充之前的信息
                    for (int j = 0; j <= i; j++) {
                        setTrue(indexs[j]);
                    }
                }
            } else {
                setTrue(index);
            }
        }

        return exist;
    }

    private int hash(String data, int seeds) {
        // 转换成为Array
        char[] value = data.toCharArray();
        int hash = 0;
        // 得到hashCode
        if (value.length > 0) {
            for (int i = 0; i < value.length; i++) {
                hash = i * hash + value[i];
            }
        }
        // 散列
        hash = hash * seeds % size;
        // 避免负
        return Math.abs(hash);
    }

    private void setTrue(int index) {
//        System.out.println(index);
        // 原子类 i++并且获取值
        useCount.incrementAndGet();
        // 设置指定index 为true
        client.setbit(key, index, true);
    }
    //
    private void checkNeedClear() {
        if (autoClearRate != null) {
            // 使用率 进行比较
            if (getUseRate() >= autoClearRate) {
                synchronized (this) {
                    if (getUseRate() >= autoClearRate) {
                        // 清理notebook
//                        notebook.clear();
                        client.del(key);
                        // 使用数量设置为0 设置为0
                        useCount.set(0);
                    }
                }
            }
        }
    }

    //获取使用率
    public double getUseRate() {
        return (double) useCount.intValue() / (double) size;
    }


    /**
     * 分配的位数越多，误判率越低但是越占内存
     *
     * 4个位误判率大概是0.14689159766308
     *
     * 8个位误判率大概是0.02157714146322
     *
     * 16个位误判率大概是0.00046557303372
     *
     * 32个位误判率大概是0.00000021167340
     *
     * @author lianghaohui
     *
     */

    public enum MisjudgmentRate {
        // 这里要选取质数，能很好的降低错误率
        /**
         * 每个字符串分配4个位
         */
        VERY_SMALL(new int[] { 2, 3, 5, 7 }),
        /**
         * 每个字符串分配8个位
         */
        SMALL(new int[] { 2, 3, 5, 7, 11, 13, 17, 19 }), //
        /**
         * 每个字符串分配16个位
         */
        MIDDLE(new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53 }), //
        /**
         * 每个字符串分配32个位
         */
        HIGH(new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
                101, 103, 107, 109, 113, 127, 131 });

        private int[] seeds;

        private MisjudgmentRate(int[] seeds) {
            this.seeds = seeds;
        }

        public int[] getSeeds() {
            return seeds;
        }

        public void setSeeds(int[] seeds) {
            this.seeds = seeds;
        }

    }

    public static void main(String[] args) {
        Jedis client = new Jedis("localhost",6379);
        client.auth("123456");
        BloomFileter fileter = new BloomFileter(client,3,"aa");
        System.out.println(fileter.addIfNotExist("7"));
        System.out.println(fileter.addIfNotExist("7"));
        client.close();


    }

}

