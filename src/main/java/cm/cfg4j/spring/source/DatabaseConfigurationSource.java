package cm.cfg4j.spring.source;

import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基于数据库的配置源
 * <p>
 * Created by Yang Tengfei on 9/25/16.
 */
public class DatabaseConfigurationSource implements ConfigurationSource {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfigurationSource.class);

    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final String tableName;

    private final String project;

    private final AtomicReference<Map<String, Properties>> propertiesHolder = new AtomicReference<>();

    private static final String SQL = "SELECT `profile`, `key`, `value` " +
        " FROM %s where project = ?";

    public DatabaseConfigurationSource(String driver, String url, String user, String password, String tableName, String project) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.tableName = tableName;
        this.project = project;
    }

    @Override
    public Properties getConfiguration(Environment environment) {
        Map<String, Properties> prop = propertiesHolder.get();
        if (prop == null) {
            synchronized (propertiesHolder) {
                propertiesHolder.set(loadFromDatabase());
            }
        }

        return propertiesHolder.get().get(environment.getName());
    }

    private Map<String, Properties> loadFromDatabase() {
        Map<String, Properties> propMap = new ConcurrentHashMap<>();

        String sql = String.format(SQL, tableName);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                addToMap(propMap, rs.getString("profile"), rs.getString("key"), rs.getString("value"));
            }
            rs.close();
        } catch (SQLException e) {
            throw new IllegalStateException("failed to execute sql", e);
        }

        propertiesHolder.set(propMap);
        return propMap;
    }

    private void addToMap(Map<String, Properties> propMap, String profile, String key, String value) {
        Properties prop = propMap.get(profile);
        if (prop == null) {
            prop = new Properties();
            propMap.put(profile, prop);
        }

        prop.setProperty(key, value);
    }

    @Override
    public void init() {
        // NOP
    }

    @Override
    public void reload() {
        loadFromDatabase();
    }

    private Connection getConnection() {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalArgumentException("please check properties from config file", e);
        }
    }
}
