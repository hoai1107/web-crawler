import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class WebCrawler implements Runnable {
    public static final int MAX_DEPTH = 2;
    public static final int MAX_CRAWL = 10;
    public static final int MAX_WORKER = 15;

    public static final ExecutorService workers = Executors.newFixedThreadPool(MAX_WORKER);

    WebCrawler(){
    }
}
