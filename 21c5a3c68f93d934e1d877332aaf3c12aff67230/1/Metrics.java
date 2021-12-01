package org.roda.core.data.v2.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "metrics")
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metrics {
  private Map<String, String> metrics;

  public Metrics() {
    metrics = new HashMap<>();
  }

  public Map<String, String> getMetrics() {
    return metrics;
  }

  public void addMetric(String metricName, String metricValue) {
    metrics.put(metricName, metricValue);
  }

}
