import com.aitrade.AiTradeApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AiTradeApplication.class)
@Slf4j
public class StockTest {

    @Test
    public void testStock() {
        log.info("testStock");
    }
}
