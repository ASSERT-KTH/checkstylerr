/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.reindex;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexFormatPlugin extends ReindexRodaEntityPlugin<Format> {

  @Override
  public String getName() {
    return "Rebuild format index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Format> cloneMe() {
    return new ReindexFormatPlugin();
  }

  @Override
  public List<Class<Format>> getObjectClasses() {
    return Arrays.asList(Format.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    // do nothing
  }

}
