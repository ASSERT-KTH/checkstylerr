/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.parsers.topology;

import java.io.Serializable;
import org.apache.metron.parsers.bolt.WriterHandler;
import org.apache.metron.parsers.interfaces.MessageFilter;
import org.apache.metron.parsers.interfaces.MessageParser;
import org.json.simple.JSONObject;

public class ParserComponents implements Serializable {
  private static final long serialVersionUID = 7880346740026374665L;

  private MessageParser<JSONObject> messageParser;
  private MessageFilter<JSONObject> filter;
  private WriterHandler writer;

  public ParserComponents(
      MessageParser<JSONObject> messageParser,
      MessageFilter<JSONObject> filter,
      WriterHandler writer) {
    this.messageParser = messageParser;
    this.filter = filter;
    this.writer = writer;
  }

  public MessageParser<JSONObject> getMessageParser() {
    return messageParser;
  }

  public MessageFilter<JSONObject> getFilter() {
    return filter;
  }

  public WriterHandler getWriter() {
    return writer;
  }

  public void setMessageParser(
      MessageParser<JSONObject> messageParser) {
    this.messageParser = messageParser;
  }

  public void setFilter(
      MessageFilter<JSONObject> filter) {
    this.filter = filter;
  }

  public void setWriter(WriterHandler writer) {
    this.writer = writer;
  }
}
