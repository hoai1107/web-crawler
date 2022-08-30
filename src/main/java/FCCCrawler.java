import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FCCCrawler extends WebCrawler {
    // FCC = FreeCodeCamp
    private final String FCC_BASE_URL = "https://www.freecodecamp.org";
    private static final Set<String> linkSet = ConcurrentHashMap.newKeySet();
    private static final LinkedBlockingQueue<Pair<String, Integer>> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger counter = new AtomicInteger();

    FCCCrawler() {
        super();
    }

    public void crawl(String URL, int currentDepth) {
        try {
            if (!linkSet.contains(URL) && currentDepth < MAX_DEPTH && counter.get() <= MAX_CRAWL) {
                linkSet.add(URL);
                counter.getAndIncrement();

                Document document = Jsoup.connect(URL).get();
                System.out.printf("FreeCodeCamp - %s (%s)\n", document.title(), URL);

                Element postContent = document.select("section.post-content").first();
                if (postContent == null) {
                    return;
                }
                Elements links = postContent.select("a[href]");

                for (Element el : links) {
                    String nextURL = el.attr("abs:href");

                    if (!nextURL.startsWith(FCC_BASE_URL)) {
                        return;
                    }
                    queue.offer(Pair.of(nextURL, currentDepth + 1));
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + URL + " - " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            Document document = Jsoup.connect(FCC_BASE_URL + "/news").get();
            Elements posts = document.select("h2.post-card-title > a");

            for (Element post : posts) {
                String link = post.attr("abs:href");
                queue.add(Pair.of(link, 0));
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
