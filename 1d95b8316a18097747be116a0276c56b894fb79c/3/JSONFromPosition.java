/**
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
package org.apache.metron.common.message;

import org.apache.commons.io.Charsets;
import org.apache.storm.tuple.Tuple;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONFromPosition implements MessageGetStrategy {

  private int position = 0;

  private ThreadLocal<JSONParser> parser = new ThreadLocal<JSONParser>() {
    @Override
    protected JSONParser initialValue() {
      return new JSONParser();
    }
  };

  public JSONFromPosition() {};

  public JSONFromPosition(Integer position) {
    this.position = position == null?0:position;
  }

  @Override
  public JSONObject get(Tuple tuple) {
    String s = null;
    try {
      s =  new String(tuple.getBinary(position), Charsets.UTF_8);
      return (JSONObject) parser.get().parse(s);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to parse " + s + " due to " + e.getMessage(), e);
    }
  }
}
