import java.util.ArrayList;
import java.util.List;
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

        for (WebCrawler crawler : crawlers) {
            executor.execute(crawler);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
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
    }
}
