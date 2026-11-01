package com.bnagritech.tradesphere.security.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    // Store BigDecimal as Decimal128 (a number) instead of String,
    // so $sum, $avg and sorting work in reports.
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(c ->
                c.bigDecimal(MongoCustomConversions.BigDecimalRepresentation.DECIMAL128));
    }
}
