/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.configuration.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SaveButtonObserver {
  private List<ISaveButtonObserver> observerList = new ArrayList<>();

  public void addObserver(ISaveButtonObserver observer) {
    if (!this.observerList.contains(observer)) {
      this.observerList.add(observer);
    }
  }

  public void removeObserver(ISaveButtonObserver observer) {
    this.observerList.remove(observer);
  }

  public void setEnabled(String databaseUUID, boolean enabled) {
    for (ISaveButtonObserver observer : this.observerList) {
      observer.update(databaseUUID, enabled);
    }
  }
}
