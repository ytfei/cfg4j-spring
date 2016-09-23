package cm.cfg4j.spring;

import org.cfg4j.provider.ConfigurationProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by Yang Tengfei on 9/23/16.
 */
public class Cfg4jTest {

    @Test
    public void testWithFileBackend() throws Exception {
        System.setProperty("configFile", "app-file.properties"); // use app-file.properties as origin config

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-config-file.xml");
        ConfigurationProvider provider = context.getBean(ConfigurationProvider.class);
        assertNotNull(provider);

        final String key = "databasePool.url";
        assertEquals(context.getEnvironment().getProperty(key), provider.getProperty(key, String.class));

        Dummy dummy = context.getBean(Dummy.class);
        assertEquals(dummy.getName(), "hello");
    }

    @Test
    public void testWithGitBackend() throws Exception {
        System.setProperty("configFile", "app-git.properties"); // use app-file.properties as origin config

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-config-git.xml");
        ConfigurationProvider provider = context.getBean(ConfigurationProvider.class);
        assertNotNull(provider);

        final String key = "databasePool.url";
        assertEquals(context.getEnvironment().getProperty(key), provider.getProperty(key, String.class));

        Dummy dummy = context.getBean(Dummy.class);
        assertEquals(dummy.getName(), context.getEnvironment().getProperty(key));
    }
}
