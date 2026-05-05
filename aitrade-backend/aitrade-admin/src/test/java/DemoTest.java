import com.aitrade.AiTradeApplication;
import com.aitrade.news.domain.vo.CryptoNews;
import com.aitrade.news.service.ICryptoNewsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AiTradeApplication.class)
public class DemoTest {

    @Autowired
    private ICryptoNewsService cryptoNewsRssService;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void test() {
        System.out.println("hello world");
//        List<CryptoNews> news = cryptoNewsRssService.fetchJinse(20);
        List<CryptoNews> news = cryptoNewsRssService.fetchCoinTelegraph(20);

        for (CryptoNews cryptoNews : news) {
            System.out.println(cryptoNews);
        }

        System.out.println("done");

    }

    @Test
    public void testCoinDeskConnection() {
        try {
            String result = restTemplate.getForObject("https://www.coindesk.com/arc/outboundfeeds/rss/", String.class);
            System.out.println("CoinDesk Connection: " + result);
        } catch (Exception e) {
            System.out.println("CoinDesk Connection Error: " + e.getMessage());
        }
    }
}
