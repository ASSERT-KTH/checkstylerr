/*
 * Copyright 2013-2017 Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.iu.harp.resource;

import java.io.DataOutput;
import java.io.IOException;

/*******************************************************
 * The abstract class of transferable data
 * structures.
 ******************************************************/
public abstract class Transferable {

  /**
   * Get the number of Bytes of encoded data.
   * 
   * @return number of bytes
   */
  public abstract int getNumEnocdeBytes();

  /**
   * Encode the data as DataOutPut
   * 
   * @param out
   * @throws IOException
   */
  public abstract void encode(DataOutput out)
    throws IOException;

  /**
   * Release the data
   */
  public abstract void release();

  /**
   * Free the data
   */
  public abstract void free();
}
