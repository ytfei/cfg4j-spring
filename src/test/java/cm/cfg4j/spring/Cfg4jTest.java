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

    @Test(enabled = false) // disable because require db config
    public void testWithDatabaseBackend() throws Exception {
        System.setProperty("configFile", "app-database.properties"); // use app-file.properties as origin config

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-config-database.xml");
        ConfigurationProvider provider = context.getBean(ConfigurationProvider.class);
        assertNotNull(provider);

        final String key = "sample.key";
        final String v = provider.getProperty(key, String.class);
        assertEquals(v, "sample.value");

        assertEquals(context.getEnvironment().getProperty(key), v);

        Dummy dummy = context.getBean(Dummy.class);
        assertEquals(dummy.getName(), v);
    }
}
