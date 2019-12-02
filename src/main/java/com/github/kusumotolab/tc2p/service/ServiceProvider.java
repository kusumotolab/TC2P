package com.github.kusumotolab.tc2p.service;

import java.util.HashMap;
import java.util.Map;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import com.github.kusumotolab.tc2p.framework.Controller;

public class ServiceProvider {

  private static final ServiceProvider instance = new ServiceProvider();

  private final Map<String, ServiceGraph> graphs = new HashMap<>();

  private ServiceProvider() {
    final ConfigurationBuilder configuration = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forClass(Services.class))
        .addScanners(new FieldAnnotationsScanner());
    final Reflections reflections = new Reflections(configuration);

    reflections.getFieldsAnnotatedWith(Service.class)
        .forEach(field -> {
          try {
            field.setAccessible(true);
            final Service service = field.getAnnotation(Service.class);
            final ServiceGraph serviceGraph = (ServiceGraph) field.get(Services.getInstance());
            graphs.put(service.name(), serviceGraph);
          } catch (final IllegalAccessException e) {
            e.printStackTrace();
          }
        });
  }

  public static ServiceProvider getInstance() {
    return instance;
  }

  public Controller resolve(final String key) {
    final ServiceGraph graph = graphs.get(key);
    if (graph == null) {
      return null;
    }
    return graph.resolve();
  }
}

