package cm.cfg4j.spring.config;

import cm.cfg4j.spring.source.ProviderAwareConfigurationSource;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.git.GitConfigurationSourceBuilder;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 根据原始配置文件,创建相应的 ConfigurationProvider
 * <p>
 * Created by Yang Tengfei on 9/23/16.
 */
public class ConfigurationProviderFactoryImpl implements ConfigurationProviderFactory {

    private final Logger log = LoggerFactory.getLogger(ConfigurationProviderFactoryImpl.class);

    /**
     * 项目名称
     */
    private static final String KEY_CONFIG_PROJECT = "project";

    /**
     * 环境配置
     */
    private static final String KEY_CONFIG_PROFILE = "profile";

    /**
     * 后端配置源的类型
     */
    private static final String KEY_CONFIG_TYPE = "type";

    /**
     * 提供配置服务(源)的文件名, 只有在 git 和 file 的情况下有效
     */
    private static final String KEY_CONFIG_FILES = "files";


    private static final String DEFAULT_CONFIG_FILES = "config.properties";

    /**
     * 数据库配置源 所需要的 配置项
     */
    private static final String KEY_CONFIG_DB_DRIVER = "db.driver";

    private static final String KEY_CONFIG_DB_URL = "db.url";

    private static final String KEY_CONFIG_DB_USER = "db.user";

    private static final String KEY_CONFIG_DB_PASSWPRD = "db.password";

    private enum ConfigType {
        git, consul, file, database
    }

    @Override
    public ConfigurationProvider create(Properties prop) {
        log.info("create provider according to : {}", prop);

        final ConfigType type = ConfigType.valueOf(prop.getProperty(KEY_CONFIG_TYPE));
        switch (type) {
            case git:
                return newGitConfigurationProvider(prop);
            case consul:
                throw new UnsupportedOperationException("consul backend is not supported yet");
            case file:
                return newLocalFileConfigurationProvider(prop);
            case database:
                return newDatabaseConfigurationProvider(prop);
            default:
                throw new UnsupportedOperationException(type + " backend is not supported yet");
        }
    }

    private ConfigurationProvider newDatabaseConfigurationProvider(Properties prop) {
        return null;
    }

    /**
     * 创建基于本地配置文件(在类路径中)的配置源
     *
     * @param prop
     * @return
     */
    private ConfigurationProvider newLocalFileConfigurationProvider(Properties prop) {
        log.info("create file backend configuration provider");

        String project = prop.getProperty(KEY_CONFIG_PROJECT, "");
        String profile = prop.getProperty(KEY_CONFIG_PROFILE, "");
        String files = prop.getProperty(KEY_CONFIG_FILES, DEFAULT_CONFIG_FILES);

        if (files == null || files.trim().length() == 0)
            throw new IllegalArgumentException("config item 'files' cannot be empty");

        log.info("load file configuration from {}:{}, files '{}'", project, profile, files);
        ConfigFilesProvider configFilesProvider = () -> Arrays.stream(files.split("[,;]")).map(Paths::get).collect(Collectors.toList());

        ConfigurationSource source = new ClasspathConfigurationSource(configFilesProvider);

        String envStr = "";
        if (project != null && project.length() > 0)
            envStr += project;

        if (profile != null && profile.length() > 0)
            envStr += "/" + profile;

        Environment environment = new ImmutableEnvironment(envStr);

        ProviderAwareConfigurationSource sourceWrapper = new ProviderAwareConfigurationSource(source);

        ConfigurationProvider provider = new ConfigurationProviderBuilder()
            .withConfigurationSource(sourceWrapper)
            .withEnvironment(environment)
            .withReloadStrategy(new PeriodicalReloadStrategy(15, TimeUnit.SECONDS))
            .build();

        sourceWrapper.setConfigurationProvider(provider);

        return provider;
    }

    private ConfigurationProvider newGitConfigurationProvider(Properties prop) {
        log.info("create git backend configuration provider");

        String configRepoPath = prop.getProperty(KEY_CONFIG_PROJECT);
        String configBranch = prop.getProperty(KEY_CONFIG_PROFILE);
        String files = prop.getProperty(KEY_CONFIG_FILES, DEFAULT_CONFIG_FILES);

        if (files == null || files.trim().length() == 0)
            throw new IllegalArgumentException("config item 'files' cannot be empty");

        log.info("load git configuration from {}:{}, files '{}'", configRepoPath, configBranch, files);
        ConfigFilesProvider configFilesProvider = () -> Arrays.stream(files.split("[,;]")).map(Paths::get).collect(Collectors.toList());

        ConfigurationSource source = new GitConfigurationSourceBuilder()
            .withRepositoryURI(configRepoPath)
            .withConfigFilesProvider(configFilesProvider)
            .build();

        Environment environment = new ImmutableEnvironment(configBranch);

        ProviderAwareConfigurationSource sourceWrapper = new ProviderAwareConfigurationSource(source);

        ConfigurationProvider provider = new ConfigurationProviderBuilder()
            .withConfigurationSource(sourceWrapper)
            .withEnvironment(environment)
            .withReloadStrategy(new PeriodicalReloadStrategy(15, TimeUnit.SECONDS))
            .build();

        sourceWrapper.setConfigurationProvider(provider);

        return provider;
    }


}
