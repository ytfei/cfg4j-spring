package cm.cfg4j.spring.source;

/**
 * 将 配置源 与 配置接口绑定
 * <p>
 * Created by Yang Tengfei on 9/24/16.
 */

import cm.cfg4j.spring.Cfg4jPropertyPlaceholderConfigurer;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.source.ConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderAwareConfigurationSource extends ConfigurationSourceWrapper {

    private final Logger log = LoggerFactory.getLogger(ProviderAwareConfigurationSource.class);

    private ConfigurationProvider configurationProvider;

    public ProviderAwareConfigurationSource(ConfigurationSource wrapped) {
        super(wrapped);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected void afterReload() {
        log.info("configuration is reloaded");
        Cfg4jPropertyPlaceholderConfigurer.__propertiesHolder.set(configurationProvider.allConfigurationAsProperties());
    }
}
