/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.tencent.angel.ml.optimizer.sgd;


import com.tencent.angel.ml.math.vector.TDoubleVector;

public abstract class L2Loss implements Loss {

  protected double lamda;

  @Override
  public boolean isL1Reg() {
    return false;
  }

  @Override
  public boolean isL2Reg() {
    if (this.lamda > 1e-12)
      return true;
    else
      return false;
  }

  @Override
  public double getReg(TDoubleVector w) {
    double reg = 0.0;
    if (isL2Reg()) {
      for (int i = 0; i < w.getDimension(); i++) {
        reg += w.get(i) * w.get(i);
      }
    }
    return 0.5 * getRegParam() * reg;
  }

  @Override
  public double getRegParam() {
    return this.lamda;
  }

}
