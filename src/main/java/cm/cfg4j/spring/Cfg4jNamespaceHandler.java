package cm.cfg4j.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * XML Namespace 解析
 * <p>
 * Created by Yang Tengfei on 9/23/16.
 */
public class Cfg4jNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
        registerBeanDefinitionParser("config", new ConfigurationBeanDefinitionParser());
    }
}
