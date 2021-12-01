package com.ctrip.apollo.client;

import com.ctrip.apollo.client.loader.ConfigLoaderFactory;
import com.ctrip.apollo.client.loader.ConfigLoaderManager;
import com.ctrip.apollo.client.model.PropertyChange;
import com.ctrip.apollo.client.model.PropertySourceReloadResult;
import com.ctrip.apollo.client.util.ConfigUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client side config manager
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigManager
    implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware {
  private static final Logger logger = LoggerFactory.getLogger(ApolloConfigManager.class);
  private static AtomicReference<ApolloConfigManager>
      singletonProtector =
      new AtomicReference<ApolloConfigManager>();

  private ConfigLoaderManager configLoaderManager;
  private ConfigurableApplicationContext applicationContext;
  private ConfigUtil configUtil;
  private ScheduledExecutorService executorService;
  private AtomicLong counter;
  private RefreshScope scope;

  public ApolloConfigManager() {
    if (!singletonProtector.compareAndSet(null, this)) {
      throw new IllegalStateException("There should be only one ApolloConfigManager instance!");
    }
    this.configLoaderManager = ConfigLoaderFactory.getInstance().getConfigLoaderManager();
    this.configUtil = ConfigUtil.getInstance();
    this.counter = new AtomicLong();
    executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "ApolloConfigManager-" + counter.incrementAndGet());
        return thread;
      }
    });
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if (!(applicationContext instanceof ConfigurableApplicationContext)) {
      throw new RuntimeException(
          String.format(
              "ApplicationContext must implement ConfigurableApplicationContext, but found: %s",
              applicationContext.getClass().getName()));
    }
    this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    this.configUtil.setApplicationContext(applicationContext);
  }

  /**
   * This is the first method invoked, so we could prepare the property sources here.
   * Specifically we need to finish preparing property source before PropertySourcesPlaceholderConfigurer
   * so that configurations could be injected correctly
   */
  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    registerDependentBeans(registry);
    initializePropertySource();
    schedulePeriodicRefresh();
  }

  /**
   * Register beans needed for Apollo Config Client
   * <li>
   * - RefreshScope: used to refresh beans when configurations changes
   * </li>
   * <li>
   * - PropertySourcesPlaceholderConfigurer: used to support placeholder configuration injection
   * </li>
   */
  private void registerDependentBeans(BeanDefinitionRegistry registry) {
    BeanDefinition
        refreshScope =
        BeanDefinitionBuilder.genericBeanDefinition(RefreshScope.class).getBeanDefinition();
    registry.registerBeanDefinition("refreshScope", refreshScope);
    BeanDefinition
        propertySourcesPlaceholderConfigurer =
        BeanDefinitionBuilder.genericBeanDefinition(PropertySourcesPlaceholderConfigurer.class)
            .getBeanDefinition();
    registry.registerBeanDefinition("propertySourcesPlaceholderConfigurer",
        propertySourcesPlaceholderConfigurer);
  }

  /**
   * This is executed after postProcessBeanDefinitionRegistry
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
  }

  /**
   * Make sure this bean is called before other beans
   */
  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  /**
   * Initialize property sources
   */
  void initializePropertySource() {
    //TODO stop application from starting when config cannot be loaded?
    CompositePropertySource result = this.configLoaderManager.loadPropertySource();

    updateEnvironmentPropertySource(result);
  }

  private void updateEnvironmentPropertySource(CompositePropertySource currentPropertySource) {
    MutablePropertySources
        currentPropertySources =
        applicationContext.getEnvironment().getPropertySources();
    if (currentPropertySources.contains(currentPropertySource.getName())) {
      currentPropertySources.replace(currentPropertySource.getName(), currentPropertySource);
      return;
    }
    currentPropertySources.addFirst(currentPropertySource);
  }

  void schedulePeriodicRefresh() {
    executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              updatePropertySource();
            } catch (Throwable ex) {
              logger.error("Refreshing config failed", ex);
            }
          }
        }, configUtil.getRefreshInterval(), configUtil.getRefreshInterval(),
        configUtil.getRefreshTimeUnit());
  }

  public List<PropertyChange> updatePropertySource() {
    PropertySourceReloadResult result = this.configLoaderManager.reloadPropertySource();
    if (result.hasChanges()) {
      logger.info("Found changes, refresh environment and refreshscope beans.");
      updateEnvironmentPropertySource(result.getPropertySource());
      refreshBeans();
    }
    return result.getChanges();
  }

  private void refreshBeans() {
    if (this.scope == null) {
      this.scope = applicationContext.getBean("refreshScope", RefreshScope.class);
    }

    if (this.scope == null) {
      logger.error("Could not get refresh scope object, skip refresh beans");
      return;
    }

    this.scope.refreshAll();
  }
}
