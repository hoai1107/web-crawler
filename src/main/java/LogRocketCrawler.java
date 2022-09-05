import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class LogRocketCrawler extends WebCrawler {

    private final String LG_BASE_URL = "https://blog.logrocket.com/";
    private final AtomicInteger counter = new AtomicInteger(0);

    public LogRocketCrawler() {
        super(1);
    }

    public void crawl(String URL, int currentDepth) {
        try {
            if (currentDepth < MAX_DEPTH && counter.get() < MAX_CRAWL) {
                counter.getAndIncrement();

                Document document = Jsoup.connect(URL).get();
                Elements relatedPosts = document.select("div.listrelated > div.card-block > a[href]");
                System.out.printf("LogRocket - %s(%s)\n", document.title(), URL);

                for (Element post : relatedPosts) {
                    String nextURL = post.attr("abs:href");
                    addURLToCrawl(nextURL, currentDepth + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + URL + " - " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            Document document = Jsoup.connect(LG_BASE_URL).get();
            Elements recentPosts = document.select("h2.card-title > a");

            for (Element post : recentPosts) {
                String link = post.attr("abs:href");
                addURLToCrawl(link, 0);
            }

            while (counter.intValue() < MAX_CRAWL) {
                Pair<String, Integer> item = getURLToCrawl();
                if (item != null) {
                    workers.execute(() -> crawl(item.getKey(), item.getValue()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
