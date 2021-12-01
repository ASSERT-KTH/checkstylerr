/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.activity.log.strategies;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IdentityLogStrategy extends ActivityLogStrategy {

  public IdentityLogStrategy() {
    super();
  }

  @Override
  public ActivityLogWrapper apply(ActivityLogWrapper wrapper) {
    return wrapper;
  }
}
