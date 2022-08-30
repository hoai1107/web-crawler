import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LogRocketCrawler extends WebCrawler {

    private final String LG_BASE_URL = "https://blog.logrocket.com/";
    private static final Set<String> linkSet = ConcurrentHashMap.newKeySet();
    private static final LinkedBlockingQueue<Pair<String, Integer>> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public void crawl(String URL, int currentDepth) {
        try {
            if (!linkSet.contains(URL) && currentDepth < MAX_DEPTH && counter.get() < MAX_CRAWL) {
                linkSet.add(URL);
                counter.getAndIncrement();

                Document document = Jsoup.connect(URL).get();
                Elements relatedPosts = document.select("div.listrelated > div.card-block > a[href]");
                System.out.printf("LogRocket - %s(%s)\n", document.title(), URL);

                for (Element post : relatedPosts) {
                    String nextURL = post.attr("abs:href");
                    queue.offer(Pair.of(nextURL, currentDepth + 1));
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + URL + " - " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try{
            Document document = Jsoup.connect(LG_BASE_URL).get();
            Elements recentPosts = document.select("h2.card-title > a");

            for(Element post : recentPosts){
                String link = post.attr("abs:href");
                queue.offer(Pair.of(link, 0));
            }

            while (counter.intValue() < MAX_CRAWL) {
                Pair<String, Integer> item = queue.poll(5, TimeUnit.SECONDS);
                if (item != null) {
                    workers.execute(() -> crawl(item.getKey(), item.getValue()));
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
