/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.modules.siard.out.write;

import java.io.OutputStream;

import com.databasepreservation.common.io.providers.InputStreamProvider;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;

/**
 * Defines the behaviour for outputting data
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface WriteStrategy {
  /**
   * Creates a stream through which data can be written to the output format
   *
   * @param container
   *          The container where the data will be written
   * @param path
   *          The path (relative to the container) to the file where the data from
   *          the stream should be written to, String digestAlgorithm, String fontCase
   * @return an OutputStream that is able to write to the specified location in a
   *         way specific to the WriteStrategy, this stream should be closed after
   *         use by calling the close() method
   */
  OutputStream createOutputStream(SIARDArchiveContainer container, String path) throws ModuleException;

  void writeTo(InputStreamProvider provider, String path);

  /**
   * @return true if the WriteStrategy supports writing a to a new file before
   *         closing the previous one
   */
  boolean isSimultaneousWritingSupported();

  /**
   * Handles closing of the underlying structure used by this WriteStrategy object
   *
   * @throws ModuleException
   */
  void finish(SIARDArchiveContainer container) throws ModuleException;

  /**
   * Handles setting up the underlying structure used by this WriteStrategy object
   *
   * @throws ModuleException
   */
  void setup(SIARDArchiveContainer container) throws ModuleException;
}
