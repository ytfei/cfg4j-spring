package cm.cfg4j.spring;

import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基于 cfg4j 的属性占位符解析器
 * <p>
 * Created by Yang Tengfei on 9/23/16.
 */
public class Cfg4jPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean, EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(Cfg4jPropertyPlaceholderConfigurer.class);

    public static final AtomicReference<Properties> __propertiesHolder = new AtomicReference<>();

    private Environment environment;

    private ConfigurationProvider configurationProvider;

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Properties properties = configurationProvider.allConfigurationAsProperties();
        __propertiesHolder.set(properties);

        // 刷新后不需要再更新 placeholder 的属性值, 因为该值只在启动时才用到
        super.setProperties(properties);

        // 配置environment
        configEnvironment();
    }

    private void configEnvironment() {
        if (environment != null && environment instanceof AbstractEnvironment) {
            InternalPropertySource propertySource = new InternalPropertySource("internalPropertySourceForConfigurationProvider", __propertiesHolder);
            ((AbstractEnvironment) environment).getPropertySources().addFirst(propertySource);
        } else {
            log.warn("cannot update spring environment with values from ConfigurationProvider");
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 使用最新的属性源来获取属性, 因为资源在 reload 后,会重置属性源. 为了保证 Environment 能够拿到最新的值, 故重写
     */
    private class InternalPropertySource extends PropertySource<AtomicReference<Properties>> {

        InternalPropertySource(String name, AtomicReference<Properties> source) {
            super(name, source);
        }

        @Override
        public Object getProperty(String name) {
            return __propertiesHolder.get().getProperty(name);
        }
    }
}
