/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.modules.siard.out.update;

import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;

import java.io.OutputStream;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface UpdateStrategy {
  /**
   * Creates a stream through which data can be written to the output format
   *
   * @return an OutputStream that is able to write to the specified location in a
   *         way specific to the UpdateStrategy, this stream should be closed after
   *         use by calling the close() method
   */
  OutputStream createOutputStream() throws ModuleException;

  /**
   * Updates the {@link SIARDArchiveContainer} with the updated file
   *
   * @param container
   *          The container where the data will be update
   * @param path
   *          The path (relative to the container) to the file where the data from
   *          the stream should be written to
   */
  void updateSIARDArchive(SIARDArchiveContainer container, String path) throws ModuleException;
}
