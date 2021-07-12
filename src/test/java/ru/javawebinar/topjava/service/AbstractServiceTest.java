package ru.javawebinar.topjava.service;

import org.junit.*;
import org.junit.rules.ExternalResource;
import org.junit.rules.Stopwatch;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.ActiveDbProfileResolver;
import ru.javawebinar.topjava.TimingRules;

import javax.annotation.Resource;

import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.util.ValidationUtil.getRootCause;


@ActiveProfiles(resolver = ActiveDbProfileResolver.class)
@RunWith(SpringRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml"
})
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public abstract class AbstractServiceTest {
    @ClassRule
    public static ExternalResource summary = TimingRules.SUMMARY;

    @Resource
    public ApplicationContext context;

    @Rule
    public Stopwatch stopwatch = TimingRules.STOPWATCH;

    public boolean isJdbc() {
        for(String profile : context.getEnvironment().getActiveProfiles()){
            if ("JDBC".equalsIgnoreCase(profile)) {
                return false;
            }
        }
        return true;
    }

    //  Check root cause in JUnit: https://github.com/junit-team/junit4/pull/778
    protected <T extends Throwable> void validateRootCause(Class<T> rootExceptionClass, Runnable runnable) {
        assertThrows(rootExceptionClass, () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw getRootCause(e);
            }
        });
    }
}