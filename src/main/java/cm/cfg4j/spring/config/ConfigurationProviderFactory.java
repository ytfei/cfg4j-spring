package cm.cfg4j.spring.config;

import org.cfg4j.provider.ConfigurationProvider;

import java.util.Properties;

/**
 * 工厂类
 * <p>
 * Created by Yang Tengfei on 9/23/16.
 */
public interface ConfigurationProviderFactory {
    ConfigurationProvider create(Properties properties);
}
