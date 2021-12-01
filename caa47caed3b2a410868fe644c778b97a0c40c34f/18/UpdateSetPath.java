package com.couchbase.client.java.query.dsl.path;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.dsl.Expression;

public interface UpdateSetPath extends InitialUpdateUnsetPath {

  UpdateSetPath set(String path, Expression value);
  UpdateSetPath set(String path, JsonObject value);
  UpdateSetPath set(String path, JsonArray value);
  UpdateSetPath set(String path, String value);
  UpdateSetPath set(String path, int value);
  UpdateSetPath set(String path, long value);
  UpdateSetPath set(String path, double value);
  UpdateSetPath set(String path, float value);
  UpdateSetPath set(String path, boolean value);

  UpdateSetPath set(String path, Expression value, Expression updateFor);
  UpdateSetPath set(String path, JsonObject value, Expression updateFor);
  UpdateSetPath set(String path, JsonArray value, Expression updateFor);
  UpdateSetPath set(String path, String value, Expression updateFor);
  UpdateSetPath set(String path, int value, Expression updateFor);
  UpdateSetPath set(String path, long value, Expression updateFor);
  UpdateSetPath set(String path, double value, Expression updateFor);
  UpdateSetPath set(String path, float value, Expression updateFor);
  UpdateSetPath set(String path, boolean value, Expression updateFor);

  UpdateSetPath set(Expression path, Expression value);
  UpdateSetPath set(Expression path, JsonObject value);
  UpdateSetPath set(Expression path, JsonArray value);
  UpdateSetPath set(Expression path, String value);
  UpdateSetPath set(Expression path, int value);
  UpdateSetPath set(Expression path, long value);
  UpdateSetPath set(Expression path, double value);
  UpdateSetPath set(Expression path, float value);
  UpdateSetPath set(Expression path, boolean value);

  UpdateSetPath set(Expression path, Expression value, Expression updateFor);
  UpdateSetPath set(Expression path, JsonObject value, Expression updateFor);
  UpdateSetPath set(Expression path, JsonArray value, Expression updateFor);
  UpdateSetPath set(Expression path, String value, Expression updateFor);
  UpdateSetPath set(Expression path, int value, Expression updateFor);
  UpdateSetPath set(Expression path, long value, Expression updateFor);
  UpdateSetPath set(Expression path, double value, Expression updateFor);
  UpdateSetPath set(Expression path, float value, Expression updateFor);
  UpdateSetPath set(Expression path, boolean value, Expression updateFor);

}
