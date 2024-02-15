package pl.mbrzozowski.ranger.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.Player;
import pl.mbrzozowski.ranger.event.reminder.UsersReminder;
import pl.mbrzozowski.ranger.games.birthday.Birthday;
import pl.mbrzozowski.ranger.games.giveaway.Giveaway;
import pl.mbrzozowski.ranger.games.giveaway.GiveawayUser;
import pl.mbrzozowski.ranger.games.giveaway.Prize;
import pl.mbrzozowski.ranger.games.reputation.Reputation;
import pl.mbrzozowski.ranger.members.InOutGuildMembers;
import pl.mbrzozowski.ranger.members.clan.ClanMember;
import pl.mbrzozowski.ranger.members.clan.rank.Rank;
import pl.mbrzozowski.ranger.recruit.Recruit;
import pl.mbrzozowski.ranger.recruit.RecruitBlackList;
import pl.mbrzozowski.ranger.recruit.WaitingRecruit;
import pl.mbrzozowski.ranger.role.Role;
import pl.mbrzozowski.ranger.server.seed.call.Message;
import pl.mbrzozowski.ranger.server.service.Client;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "pl.mbrzozowski.ranger.repository.main",
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager")
public class MainDataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.main")
    public DataSourceProperties mainDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.main.configuration")
    public DataSource mainDataSource() {
        return mainDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(mainDataSource())
                .packages(
                        Event.class,
                        Player.class,
                        Recruit.class,
                        RecruitBlackList.class,
                        Client.class,
                        UsersReminder.class,
                        Role.class,
                        InOutGuildMembers.class,
                        Giveaway.class,
                        GiveawayUser.class,
                        Prize.class,
                        WaitingRecruit.class,
                        ClanMember.class,
                        Rank.class,
                        Message.class,
                        Reputation.class,
                        Birthday.class)
                .properties(properties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager mainTransactionManager(
            final @Qualifier("mainEntityManagerFactory") LocalContainerEntityManagerFactoryBean mainEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(mainEntityManagerFactory.getObject()));
    }
}
