/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.fluo.integration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.ConditionalWriter;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.ConditionalMutation;
import org.apache.fluo.core.impl.Environment;

public class FaultyConfig extends Environment {

  /**
   * A writer that will sometimes return unknown. When it returns unknown the condition may or may
   * not have been written.
   *
   * <p>
   * The following code was copied from Accumulo in order to avoid depending on accumulo test
   * module.
   */
  private static class FaultyConditionalWriter implements ConditionalWriter {

    private ConditionalWriter cw;
    private double up;
    private Random rand;
    private double wp;

    FaultyConditionalWriter(ConditionalWriter cw, double unknownProbability,
        double writeProbability) {
      this.cw = cw;
      this.up = unknownProbability;
      this.wp = writeProbability;
      this.rand = new Random();
    }

    @Override
    public Iterator<Result> write(Iterator<ConditionalMutation> mutations) {
      ArrayList<Result> resultList = new ArrayList<>();
      ArrayList<ConditionalMutation> writes = new ArrayList<>();

      while (mutations.hasNext()) {
        ConditionalMutation cm = mutations.next();
        if (rand.nextDouble() <= up && rand.nextDouble() > wp) {
          resultList.add(new Result(Status.UNKNOWN, cm, null));
        } else {
          writes.add(cm);
        }
      }

      if (!writes.isEmpty()) {
        Iterator<Result> results = cw.write(writes.iterator());

        while (results.hasNext()) {
          Result result = results.next();

          if (rand.nextDouble() <= up && rand.nextDouble() <= wp) {
            result = new Result(Status.UNKNOWN, result.getMutation(), result.getTabletServer());
          }
          resultList.add(result);
        }
      }
      return resultList.iterator();
    }

    @Override
    public Result write(ConditionalMutation mutation) {
      return write(Collections.singleton(mutation).iterator()).next();
    }

    @Override
    public void close() {
      cw.close();
    }
  }

  private double up;
  private double wp;

  FaultyConfig(Environment env, double up, double wp) throws Exception {
    super(env);
    this.up = up;
    this.wp = wp;
  }

  @Override
  public AccumuloClient getAccumuloClient() {
    return super.getAccumuloClient();
  }

  public ConditionalWriter createConditionalWriter() throws TableNotFoundException {
    return new FaultyConditionalWriter(super.getSharedResources().getConditionalWriter(), up, wp);
  }
}
