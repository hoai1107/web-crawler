import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MediumCrawler extends WebCrawler {
    private final String MEDIUM_BASE_URL = "https://medium.com/";
    private final String[] TAGS = {"technology", "programming", "software-development", "java"};
    private static AtomicInteger counter;

    private static final Set<String> linkSet = ConcurrentHashMap.newKeySet();

    public MediumCrawler() {
        super();
        counter = new AtomicInteger();
    }

    public void crawl(String URL) {
        String category = URL.substring(23);
        try {
            if (!linkSet.contains(URL) && counter.get() <= MAX_CRAWL) {
                linkSet.add(URL);

                Document document = Jsoup.connect(URL).get();
                Elements freeArticles = document.select("article:not(.meteredContent)");

                for (Element article : freeArticles) {
                    if (counter.getAndIncrement() <= MAX_CRAWL) {
                        Element el = article.select("a[aria-label='Post Preview Title']").first();
                        assert el != null;

                        Element titleEl = el.select("h2").first();
                        assert titleEl != null;

                        String title = titleEl.text();
                        String link = el.attr("abs:href");

                        System.out.printf("Medium(%s) - %s (%s)\n", StringUtils.capitalize(category), title, link);

                        Thread.sleep(Duration.ofSeconds(2).toMillis());
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + URL + " - " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        for (String tag : TAGS) {
            workers.execute(() -> crawl(MEDIUM_BASE_URL + "tag/" + tag));
        }
    }
}
