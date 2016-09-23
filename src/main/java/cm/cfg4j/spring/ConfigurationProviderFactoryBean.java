package cm.cfg4j.spring;

import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Yang Tengfei on 9/23/16.
 */
public class ConfigurationProviderFactoryBean implements FactoryBean<ConfigurationProvider> {

    private final Logger log = LoggerFactory.getLogger(ConfigurationProviderFactoryBean.class);

    static final AtomicReference<ConfigurationProvider> providerHolder = new AtomicReference<>();

    @Override
    public ConfigurationProvider getObject() throws Exception {
        if (providerHolder.get() == null)
            throw new IllegalStateException("ConfigurationProvider is not init");
        return providerHolder.get();
    }

    @Override
    public Class<?> getObjectType() {
        return ConfigurationProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
