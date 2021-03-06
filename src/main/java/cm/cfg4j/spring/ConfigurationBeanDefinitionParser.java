package cm.cfg4j.spring;

import cm.cfg4j.spring.config.ConfigurationProviderFactoryImpl;
import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 加载 配置系统 原始配置文件, 根据文件的内容构造 PropertiesPlaceHolderConfigure,
 * 并注册 ConfigurationProvider 到容器中
 * <p>
 * Created by Yang Tengfei on 9/23/16.
 */
public class ConfigurationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private final Logger log = LoggerFactory.getLogger(ConfigurationBeanDefinitionParser.class);

    private static final String KEY = "key";

    private static final String KEY_DEFAULT_VALUE = "configFile";

    private static final String DEFAULT_CONFIG_FILE_NAME = "app.properties";

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Properties prop = loadProperties(getConfigFile(element.getAttribute(KEY)));
        log.info("origin config: {}", prop);

        ConfigurationProvider provider = new ConfigurationProviderFactoryImpl().create(prop);

        // 将 ConfigurationProvider 注册到容器
        registerConfigurationProvider(parserContext, provider);

        // 设置 Cfg4jPropertyPlaceholderConfigurer 相关属性
        builder.addPropertyValue("configurationProvider", provider);
    }

    private String getConfigFile(String configKey) {
        if (configKey == null || configKey.length() == 0) {
            configKey = KEY_DEFAULT_VALUE;
            log.info("use default config key: {}", configKey);
        }

        String configFile = System.getProperty(configKey);
        if (configFile == null || configFile.length() == 0) {
            log.info("cannot file 'configFile' in system properties");
            configFile = System.getenv(configKey);

            if (configFile == null || configFile.length() == 0) {
                log.info("cannot file 'configFile' in system env, use default config file: {}", DEFAULT_CONFIG_FILE_NAME);
                configFile = DEFAULT_CONFIG_FILE_NAME;
            }
        }

        log.info("read origin config from file '{}' in classpath", configFile);
        return configFile;
    }

    private void registerConfigurationProvider(ParserContext parserContext, ConfigurationProvider provider) {
        log.info("register [cfg4jConfigurationProvider] to spring");

        // 通过工厂方法静态类,避开 Spring 无法直接注册一个对象实例的问题
        ConfigurationProviderFactoryBean.providerHolder.set(provider);

        BeanDefinition configurationProviderFactoryBean = BeanDefinitionBuilder.rootBeanDefinition(ConfigurationProviderFactoryBean.class).getBeanDefinition();
        parserContext.getRegistry().registerBeanDefinition("cfg4jConfigurationProvider", configurationProviderFactoryBean);
    }


    private Properties loadProperties(String configFile) {
        log.info("load origin config from file: {}", configFile);

        Properties prop = new Properties();
        try {
            InputStream in = ConfigurationBeanDefinitionParser.class.getClassLoader().getResourceAsStream(configFile);
            if (in == null)
                throw new IllegalArgumentException("cannot file origin config file: " + configFile);

            prop.load(in);
        } catch (IOException e) {
            log.error("failed to load origin properties", e);
            throw new IllegalArgumentException("failed to load origin properties: " + configFile, e);
        }

        return prop;
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return Cfg4jPropertyPlaceholderConfigurer.class;
    }


}
