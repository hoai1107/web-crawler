import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<WebCrawler> crawlers = new ArrayList<>();

        crawlers.add(new FCCCrawler());
        crawlers.add(new MediumCrawler());
        crawlers.add(new LogRocketCrawler());

        CompletableFuture<?>[] futures = crawlers.stream()
                .map(crawler -> CompletableFuture.runAsync(crawler, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        // Shutdown Main executor
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
            System.out.println("Main executor shutdown.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Shutdown executor of WebCrawler
        WebCrawler.workers.shutdown();
        try {
            if (!WebCrawler.workers.awaitTermination(60, TimeUnit.SECONDS)) {
                WebCrawler.workers.shutdownNow();
            }
            System.out.println("WebCrawler executor has been shutdown.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Finish crawling.");

        RedisClient.shutdownPool();
        System.out.println("Clear all Redis DB.");
    }
}
