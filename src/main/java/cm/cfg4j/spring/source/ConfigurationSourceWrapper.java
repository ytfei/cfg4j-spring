package cm.cfg4j.spring.source;

import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;

import java.util.Properties;

/**
 * Created by Yang Tengfei on 9/23/16.
 */
public class ConfigurationSourceWrapper implements ConfigurationSource {

    private final ConfigurationSource wrapped;

    public ConfigurationSourceWrapper(ConfigurationSource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Properties getConfiguration(Environment environment) {
        return wrapped.getConfiguration(environment);
    }

    @Override
    public void init() {
        wrapped.init();
    }

    @Override
    public void reload() {
        wrapped.reload();

        afterReload();
    }

    protected void afterReload() {

    }
}
