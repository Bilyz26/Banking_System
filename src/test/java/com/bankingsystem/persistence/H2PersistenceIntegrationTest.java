package com.bankingsystem.persistence;

import com.bankingsystem.bootstrap.BankingSystemApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = BankingSystemApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:banking;MODE=PostgreSQL;"
                        + "DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.flyway.enabled=true"
        })
@ActiveProfiles("postgresql")
class H2PersistenceIntegrationTest extends AbstractPersistenceIntegrationTest {
}
