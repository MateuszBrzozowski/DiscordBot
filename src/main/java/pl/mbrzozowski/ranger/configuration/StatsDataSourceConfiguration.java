package pl.mbrzozowski.ranger.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import pl.mbrzozowski.ranger.stats.model.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(basePackages = "pl.mbrzozowski.ranger.repository.stats",
        entityManagerFactoryRef = "statsEntityManagerFactory",
        transactionManagerRef = "statsTransactionManager")
public class StatsDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("app.datasource.stats")
    public DataSourceProperties statsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.stats.configuration")
    public DataSource statsDataSource() {
        return statsDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "statsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean statsEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        return builder
                .dataSource(statsDataSource())
                .packages(
                        Deaths.class,
                        DiscordUser.class,
                        Revives.class,
                        Players.class,
                        Wounds.class)
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager statsTransactionManager(
            final @Qualifier("statsEntityManagerFactory") LocalContainerEntityManagerFactoryBean statsEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(statsEntityManagerFactory.getObject()));
    }
}
