package com.skyding.dubbo.autoconfigure;

import com.skyding.util.ClassUtil;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * to register dubbo reference or service automatically
 * <p>
 * created at 2018/10/15
 *
 * @author weichunhe
 */
public class DubboPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private static Logger LOG = LoggerFactory.getLogger(DubboPostProcessor.class);

    private String encoding = "UTF-8";

    private Configuration freemarkerCfg = initFreemarker();

    private BeanDefinitionRegistry definitionRegistry;

    /**
     * the freemarker template path relative to classpath.You can access all the interfaces that need to be registered to Spring through `interfaces`.
     * The `interfaces` is a `ArrayList<Class>`.
     */
    private String freemarkerRelativePath;

    /**
     * where to scan interfaces ,use comma to split multiple packages.
     * Notice: It will not scan the subpackages.
     */
    private String interfacePackages;

    /**
     * It's true when configured in service side ,false in consumer side.
     */
    private Boolean serviceSide;

    /**
     * Modify the application context's internal bean definition registry after its
     * standard initialization. All regular bean definitions will have been loaded,
     * but no beans will have been instantiated yet. This allows for adding further
     * bean definitions before the next post-processing phase kicks in.
     *
     * @param registry the bean definition registry used by the application context
     * @throws BeansException in case of errors
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        definitionRegistry = registry;
    }

    /**
     * Modify the application context's internal bean factory after its standard
     * initialization. All bean definitions will have been loaded, but no beans
     * will have been instantiated yet. This allows for overriding or adding
     * properties even to eager-initializing beans.
     *
     * @param beanFactory the bean factory used by the application context
     * @throws BeansException in case of errors
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] packages = interfacePackages.split(",");
        List<Class<?>> classes = new ArrayList<>();
        for (String pkg : packages) {
            classes.addAll(ClassUtil.getClasses(pkg, false));
        }
        List<Class> needToRegisters = filterInterfaces(classes, beanFactory);
        registerBean(definitionRegistry, needToRegisters);
    }

    /**
     * filter the interfaces which need to be registered into Spring container
     *
     * @param interfaces
     * @param beanFactory
     * @return
     */
    protected List<Class> filterInterfaces(List<Class<?>> interfaces, ConfigurableListableBeanFactory beanFactory) {
        List<Class> needToRegisters = new ArrayList<>();
        for (Class<?> clazz : interfaces) {
            if (!clazz.isInterface()) {
                continue;
            }
            if (isServiceSide()) {
                try {
                    beanFactory.getBeanDefinition(clazz.getName());
                } catch (NoSuchBeanDefinitionException e) {
                    needToRegisters.add(clazz);
                }
            } else {
                String[] beanNames = beanFactory.getBeanNamesForType(clazz);
                if (beanNames == null || beanNames.length == 0) {
                    needToRegisters.add(clazz);
                }
            }
        }
        return needToRegisters;
    }

    /**
     * generate BeanDefinition for {@code interfaces} argument base on the {@code freemarkerRelativePath} property,
     * and then register to Spring container
     *
     * @param registry
     * @param interfaces
     */
    protected void registerBean(BeanDefinitionRegistry registry, List<Class> interfaces) {
        LOG.info("dubbo start to register for {} automatically", interfaces);
        XmlBeanDefinitionReader definitionReader = new XmlBeanDefinitionReader(registry);
        Writer writer = new StringWriter();
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("interfaces", interfaces);
        try {
            freemarkerCfg.getTemplate(freemarkerRelativePath).process(dataModel, writer);
            writer.close();
        } catch (Exception e) {
            LOG.error("dubbo has a error while loading freemarker.", e);
            return;
        }
        String xml = writer.toString();
        LOG.info("dubbo generate XML automatically:{}", xml);
        ByteArrayResource resource = new ByteArrayResource(xml.getBytes(Charset.forName(encoding)));
        int count = definitionReader.loadBeanDefinitions(resource);
        LOG.info("dubbo should register {} beans automatically, actually registered {} beans ", interfaces.size(), count);
    }

    protected Configuration initFreemarker() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        cfg.setDefaultEncoding(encoding);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        return cfg;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getFreemarkerRelativePath() {
        return freemarkerRelativePath;
    }

    @Required
    public void setFreemarkerRelativePath(String freemarkerRelativePath) {
        this.freemarkerRelativePath = freemarkerRelativePath;
    }

    public String getInterfacePackages() {
        return interfacePackages;
    }

    @Required
    public void setInterfacePackages(String interfacePackages) {
        this.interfacePackages = interfacePackages;
    }

    public boolean isServiceSide() {
        return serviceSide;
    }

    @Required
    public void setServiceSide(boolean serviceSide) {
        this.serviceSide = serviceSide;
    }

}
