/**
 * Copyright (C) 2015 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client.java.query;

import com.couchbase.client.java.CouchbaseAsyncBucket;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.element.Element;
import com.couchbase.client.java.query.dsl.path.AbstractPath;
import com.couchbase.client.java.query.dsl.path.DefaultDeleteUsePath;
import com.couchbase.client.java.query.dsl.path.DeleteUsePath;

import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.x;

public class Delete {

  private Delete() {}

  public static DeleteUsePath deleteFrom(String bucket) {
    return deleteFrom(i(bucket));
  }

  public static DeleteUsePath deleteFrom(Expression bucket) {
    return new DefaultDeleteUsePath(new DeletePath(bucket));
  }

  public static DeleteUsePath deleteFromCurrentBucket() {
    return deleteFrom(x(CouchbaseAsyncBucket.CURRENT_BUCKET_IDENTIFIER));
  }

  private static class DeletePath extends AbstractPath {
    public DeletePath(final Expression bucket) {
      super(null);
      element(new Element() {
        @Override
        public String export() {
          return "DELETE FROM " + bucket.toString();
        }
      });
    }
  }

}
