package com.marklogic.spring.batch.config;

import com.marklogic.client.helper.DatabaseClientConfig;
import com.marklogic.client.helper.DatabaseClientProvider;
import com.marklogic.client.spring.SimpleDatabaseClientProvider;
import com.marklogic.client.spring.SpringDatabaseClientConfig;
import com.marklogic.xcc.template.XccTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource(value = "classpath:job.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:job.properties", ignoreResourceNotFound = true)
@Import(value = { SpringDatabaseClientConfig.class } )
public class MarkLogicApplicationContext {

    @Value("${marklogic.name}")
    private String mlAppName;

    /**
     * Ensures that placeholders are replaced with property values
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    public DatabaseClientProvider databaseClientProvider(DatabaseClientConfig databaseClientConfig) {
        return new SimpleDatabaseClientProvider(databaseClientConfig);
    }

    @Bean
    public XccTemplate xccTemplate(DatabaseClientConfig databaseClientConfig) {
        return new XccTemplate(
                String.format("xcc://%s:%s@%s:8000/%s",
                        databaseClientConfig.getUsername(),
                        databaseClientConfig.getPassword(),
                        databaseClientConfig.getHost(),
                        mlAppName));
    }

}