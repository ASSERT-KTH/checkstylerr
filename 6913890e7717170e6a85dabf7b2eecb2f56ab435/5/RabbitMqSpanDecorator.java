/**
 * Copyright 2017-2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.spring.rabbitmq;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.amqp.core.MessageProperties;

/**
 * @author Gilles Robert
 */
public class RabbitMqSpanDecorator {

  public void onSend(MessageProperties messageProperties, String exchange, String routingKey, Span span) {
    Tags.COMPONENT.set(span, RabbitMqTracingTags.RABBITMQ.getKey());
    RabbitMqTracingTags.EXCHANGE.set(span, exchange);
    RabbitMqTracingTags.MESSAGE_ID.set(span, messageProperties.getMessageId());
    RabbitMqTracingTags.ROUTING_KEY.set(span, routingKey);
  }

  public void onReceive(MessageProperties messageProperties, Span span) {
    Tags.COMPONENT.set(span, RabbitMqTracingTags.RABBITMQ.getKey());
    RabbitMqTracingTags.EXCHANGE.set(span, messageProperties.getReceivedExchange());
    RabbitMqTracingTags.MESSAGE_ID.set(span, messageProperties.getMessageId());
    RabbitMqTracingTags.ROUTING_KEY.set(span, messageProperties.getReceivedRoutingKey());
    RabbitMqTracingTags.CONSUMER_QUEUE.set(span, messageProperties.getConsumerQueue());
  }

  /**
   * Note, new span isn't created for reply messages.
   * This extension point allows for example marking AMQP message consumer span with error for example based custom headers
   */
  public void onSendReply(MessageProperties replyMessageProperties, String replyExchange, String replyRoutingKey, Span span) {
  }

  public void onError(Exception ex, Span span) {
    Map<String, Object> exceptionLogs = new LinkedHashMap<>(2);
    exceptionLogs.put("event", Tags.ERROR.getKey());
    exceptionLogs.put("error.object", ex);
    span.log(exceptionLogs);
    Tags.ERROR.set(span, true);
  }
}
