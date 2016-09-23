package cm.cfg4j.spring;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.git.GitConfigurationSourceBuilder;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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

    private static final String KEY_CONFIG_REPO_PATH = "project";

    private static final String KEY_CONFIG_BRANCH = "profile";

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String configKey = element.getAttribute(KEY);
        if (configKey == null || configKey.length() == 0)
            configKey = KEY_DEFAULT_VALUE;

        String configFile = System.getProperty(configKey, DEFAULT_CONFIG_FILE_NAME);

        Properties prop = loadProperties(configFile);

        ConfigurationProvider provider = newConfigurationProvider(prop);
        ConfigurationProviderFactoryBean.providerHolder.set(provider);

        // 将 ConfigurationProvider 注册到容器
        registerConfigurationProvider(parserContext);

        // 设置 Cfg4jPropertyPlaceholderConfigurer 相关属性
        builder.addPropertyValue("configurationProvider", provider);

        // 用于当配置的占位符，没有给到属性值时报警
        builder.addPropertyValue("ignoreUnresolvablePlaceholders",
            Boolean.valueOf(element.getAttribute("ignore-unresolvable")));
    }

    private void registerConfigurationProvider(ParserContext parserContext) {
        BeanDefinition configurationProviderFactoryBean = BeanDefinitionBuilder.rootBeanDefinition(ConfigurationProviderFactoryBean.class).getBeanDefinition();
        parserContext.getRegistry().registerBeanDefinition("configurationProviderFactoryBean", configurationProviderFactoryBean);

//        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
//        genericBeanDefinition.setBeanClass(ConfigurationProvider.class);
//        genericBeanDefinition.setFactoryBeanName("configurationProviderFactoryBean");
//        genericBeanDefinition.setLazyInit(false);
//        parserContext.getRegistry().registerBeanDefinition("configurationProvider", genericBeanDefinition);
    }

    private ConfigurationProvider newConfigurationProvider(Properties prop) {
        log.info("create provider according to : {}", prop);
        String configRepoPath = prop.getProperty(KEY_CONFIG_REPO_PATH);
        String configBranch = prop.getProperty(KEY_CONFIG_BRANCH);

        log.info("load system configuration from {}:{}", configRepoPath, configBranch);
        ConfigFilesProvider configFilesProvider = () -> Arrays.asList(/*Paths.get("application.properties"), */Paths.get("configuration.yaml"));

        ConfigurationSource source = new GitConfigurationSourceBuilder()
            .withRepositoryURI(configRepoPath)
            .withConfigFilesProvider(configFilesProvider)
            .build();

        Environment environment = new ImmutableEnvironment(configBranch);

        ProviderAwareConfigurationSource sourceWrapper = new ProviderAwareConfigurationSource(source);

        ConfigurationProvider provider = new ConfigurationProviderBuilder()
            .withConfigurationSource(sourceWrapper)
            .withEnvironment(environment)
            .withReloadStrategy(new PeriodicalReloadStrategy(5, TimeUnit.SECONDS))
            .build();

        sourceWrapper.setConfigurationProvider(provider);

        return provider;
    }

    private Properties loadProperties(String configFile) {
        Properties prop = new Properties();
        try {
            prop.load(ConfigurationBeanDefinitionParser.class.getClassLoader().getResourceAsStream(configFile));
        } catch (IOException e) {
            log.error("failed to load origin properties", e);
            throw new IllegalArgumentException("failed to load origin properties", e);
        }

        return prop;
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return Cfg4jPropertyPlaceholderConfigurer.class;
    }

    /**
     * 将 配置源 与 配置接口绑定
     */
    private class ProviderAwareConfigurationSource extends ConfigurationSourceWrapper {

        private ConfigurationProvider configurationProvider;

        ProviderAwareConfigurationSource(ConfigurationSource wrapped) {
            super(wrapped);
        }

        public ConfigurationProvider getConfigurationProvider() {
            return configurationProvider;
        }

        public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
            this.configurationProvider = configurationProvider;
        }

        @Override
        protected void afterReload() {
            log.info("configuration is reloaded");
            Cfg4jPropertyPlaceholderConfigurer.propertiesHolder.set(configurationProvider.allConfigurationAsProperties());
        }
    }
}
