/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.reindex;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexDisposalConfirmationPlugin extends ReindexRodaEntityPlugin<DisposalConfirmation> {

  @Override
  public String getName() {
    return "Rebuild disposal confirmation index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<DisposalConfirmation> cloneMe() {
    return new ReindexDisposalConfirmationPlugin();
  }

  @Override
  public List<Class<DisposalConfirmation>> getObjectClasses() {
    return Collections.singletonList(DisposalConfirmation.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    // do nothing
  }

}
