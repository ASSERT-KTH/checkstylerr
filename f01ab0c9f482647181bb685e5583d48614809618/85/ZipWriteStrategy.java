package com.databasepreservation.modules.siard.out.write;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ZipWriteStrategy implements WriteStrategy {
  private static final Logger LOGGER = LoggerFactory.getLogger(ZipWriteStrategy.class);

  private final CompressionMethod compressionMethod;
  private ProtectedZipArchiveOutputStream zipOut;

  public ZipWriteStrategy(CompressionMethod compressionMethod) {
    this.compressionMethod = compressionMethod;
  }

  @Override
  public OutputStream createOutputStream(SIARDArchiveContainer container, String path) throws ModuleException {
    ArchiveEntry archiveEntry = new ZipArchiveEntry(path);
    try {
      zipOut.putArchiveEntry(archiveEntry);
    } catch (IOException e) {
      throw new ModuleException("Error creating new entry in zip file", e);
    }
    return zipOut;
  }

  @Override
  public boolean isSimultaneousWritingSupported() {
    return false;
  }

  @Override
  public void finish(SIARDArchiveContainer container) throws ModuleException {
    try {
      zipOut.closeArchiveEntry();
    } catch (IOException e) {
      if (!"No current entry to close".equals(e.getMessage())) {
        LOGGER.debug("the ArchiveEntry is already closed or the ZipArchiveOutputStream is already finished", e);
      }
    }

    try {
      zipOut.finish();
      zipOut.protectedClose();
    } catch (IOException e) {
      throw new ModuleException("Problem while finalizing zip output stream", e);
    }
  }

  @Override
  public void setup(SIARDArchiveContainer container) throws ModuleException {
    try {
      zipOut = new ProtectedZipArchiveOutputStream(container.getPath().toFile());

      zipOut.setUseZip64(Zip64Mode.Always);

      switch (compressionMethod) {
        case DEFLATE:
          zipOut.setMethod(ZipArchiveOutputStream.DEFLATED);
          break;
        case STORE:
          zipOut.setMethod(ZipArchiveOutputStream.STORED);
          break;
        default:
          throw new ModuleException("Invalid compression method: " + compressionMethod);
      }
    } catch (IOException e) {
      throw new ModuleException("Error creating SIARD archive file: " + compressionMethod, e);
    }
  }

  public enum CompressionMethod {
    DEFLATE, STORE
  }

  private class ProtectedZipArchiveOutputStream extends ZipArchiveOutputStream {
    public ProtectedZipArchiveOutputStream(File file) throws IOException {
      super(file);
    }

    @Override
    public void close() throws IOException {
      flush();
      zipOut.closeArchiveEntry();
    }

    private void protectedClose() throws IOException {
      super.close();
    }
  }
}
