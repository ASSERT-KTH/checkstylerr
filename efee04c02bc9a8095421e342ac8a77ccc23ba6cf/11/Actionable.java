/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface Actionable<T extends IsIndexed> {

  interface Action<T> {
    List<String> getMethods();
  }

  enum ActionImpact {
    NONE, UPDATED, DESTROYED
  }

  // Generic, implemented in AbstractActionable

  boolean canAct(Action<T> action, ActionableObject<T> object);

  void act(Action<T> action, ActionableObject<T> object);

  void act(Action<T> action, ActionableObject<T> object, AsyncCallback<ActionImpact> callback);

  // NO OBJECT

  boolean canAct(Action<T> action);

  void act(Action<T> action);

  void act(Action<T> action, AsyncCallback<ActionImpact> callback);

  // SINGLE OBJECT

  boolean canAct(Action<T> action, T object);

  void act(Action<T> action, T object);

  void act(Action<T> action, T object, AsyncCallback<ActionImpact> callback);

  // MULTIPLE OBJECTS

  boolean canAct(Action<T> action, SelectedItems<T> objects);

  void act(Action<T> action, SelectedItems<T> objects);

  void act(Action<T> action, SelectedItems<T> objects, AsyncCallback<ActionImpact> callback);

  // Layout
  ActionableBundle<T> createActionsBundle();

  Action<T> actionForName(String name);
}
