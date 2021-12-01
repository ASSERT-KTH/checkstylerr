package org.roda.core.storage.protocol;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.data.utils.URLUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class FileProtocolManager implements ProtocolManager {
  private final URI connectionString;
  private final Path path;

  public FileProtocolManager(URI connectionString) {
    this.connectionString = connectionString;
    this.path = Paths.get(URLUtils.decode(connectionString.getPath()));
  }

  @Override
  public InputStream getInputStream() {

    try {
      return new FileInputStream(path.toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Boolean isAvailable() {
    return true;
  }

  @Override
  public void downloadResource(Path target) {
    Path output = target.resolve(FilenameUtils.getName(path.toString()));
    try {
      Files.copy(path, output, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
