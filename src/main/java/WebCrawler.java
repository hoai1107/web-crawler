import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;

import redis.clients.jedis.Jedis;

public abstract class WebCrawler implements Runnable {
    public static final int MAX_DEPTH = 2;
    public static final int MAX_CRAWL = 20;
    public static final int MAX_WORKER = 15;

    private final int REDIS_DB_INDEX;

    public static final ExecutorService workers = Executors.newFixedThreadPool(MAX_WORKER);

    WebCrawler(int index) {
        REDIS_DB_INDEX = index;
    }

    public void addURLToCrawl(String URL, int depth) {
        try (Jedis jedis = RedisClient.getPool().getResource()) {
            jedis.select(REDIS_DB_INDEX);

            boolean exists = jedis.sismember("visited_url", URL);

            if (!exists) {
                jedis.sadd("visited_url", URL);
                jedis.lpush("next_url", String.format("%d:%s", depth, URL));
            }
        }
    }

    public Pair<String, Integer> getURLToCrawl() {
        try (Jedis jedis = RedisClient.getPool().getResource()) {
            jedis.select(REDIS_DB_INDEX);

            String value = jedis.rpop("next_url");

            if (value == null) {
                return null;
            }

            String[] arr = value.split(":", 2); // Split from the first ":"

            int depth = Integer.valueOf(arr[0]);
            String url = arr[1];

            return Pair.of(url, depth);
        }
    }

    public boolean isURLExists(String URL) {
        try (Jedis jedis = RedisClient.getPool().getResource()) {
            jedis.select(REDIS_DB_INDEX);

            return jedis.sismember("visited_url", URL);
        }
    }
}
