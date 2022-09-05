import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {

    private static final JedisPool POOL = new JedisPool(buildConfig(), "localhost", 6379);

    private static JedisPoolConfig buildConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);

        return poolConfig;

    }

    public static JedisPool getPool() {
        return POOL;
    }

    public static void shutdownPool() {
        try (Jedis jedis = POOL.getResource()) {
            jedis.flushAll();
        }

        POOL.close();
    }

    public static void main(String[] args) {
        try (Jedis jedis = getPool().getResource()) {

        }

        shutdownPool();
    }
}
