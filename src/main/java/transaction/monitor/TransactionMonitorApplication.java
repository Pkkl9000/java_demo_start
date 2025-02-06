package transaction.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.t1.java.demo.T1JavaDemoApplication;

@SpringBootApplication
@Slf4j
public class TransactionMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionMonitorApplication.class, args);
    }
}
