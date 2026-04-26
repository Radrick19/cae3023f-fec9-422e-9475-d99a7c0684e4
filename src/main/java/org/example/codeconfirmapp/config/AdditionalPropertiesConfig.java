package org.example.codeconfirmapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@PropertySource(value = "classpath:email.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:sms.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:telegram.properties", ignoreResourceNotFound = true)
public class AdditionalPropertiesConfig {
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        var filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("HTTP-запрос: ");
        return filter;
    }
}
