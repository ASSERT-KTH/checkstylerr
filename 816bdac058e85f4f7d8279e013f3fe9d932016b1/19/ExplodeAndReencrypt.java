package name.neuhalfen.projects.crypto.bouncycastle.openpgp.reencryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BuildEncryptionOutputStreamAPI;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.util.io.Streams;


final class ExplodeAndReencrypt {

  private final ZipEntityStrategy entityHandlingStrategy;

  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
      .getLogger(ExplodeAndReencrypt.class);

  private final InputStream is;
  private final BuildEncryptionOutputStreamAPI.Build encryptionFactory;


  ExplodeAndReencrypt(InputStream is, ZipEntityStrategy entityHandlingStrategy,
      BuildEncryptionOutputStreamAPI.Build encryptionFactory) {
    this.is = is;
    this.entityHandlingStrategy = entityHandlingStrategy;
    this.encryptionFactory = encryptionFactory;
  }


  void explodeAndReencrypt()
      throws IOException, SignatureException, NoSuchAlgorithmException, PGPException, NoSuchProviderException {
    boolean zipDataFound = false;
    final ZipInputStream zis = new ZipInputStream(is);

    ZipEntry entry;

    int numDirs = 0;
    int numFiles = 0;
    while ((entry = zis.getNextEntry()) != null) {

      final String sanitizedFileName = entityHandlingStrategy.rewriteName(entry.getName());

      if (!entry.getName().equals(sanitizedFileName)) {
        LOGGER.trace("Rewriting '{}' to '{}'", entry.getName(), sanitizedFileName);
      }

      if (!zipDataFound) {
        // Inform the logger that this is indeed a ZIP file
        zipDataFound = true;
        LOGGER.trace("Found ZIP Data");
      }

      if (entry.isDirectory()) {
        numDirs++;
        LOGGER.debug("found directory '{}'", entry.getName());

        entityHandlingStrategy.handleDirectory(sanitizedFileName);
      } else {
        numFiles++;

        LOGGER.debug("found file '{}'", entry.getName());

        try (
            final OutputStream outputStream = entityHandlingStrategy
                .createOutputStream(sanitizedFileName)
        ) {
          if (outputStream != null) {
            final OutputStream encryptedSmallFromZIP = encryptionFactory.andWriteTo(outputStream);
            Streams.pipeAll(zis, encryptedSmallFromZIP);
            encryptedSmallFromZIP.flush();
            encryptedSmallFromZIP.close();
          } else {
            LOGGER.trace("Ignore {}", entry.getName());
          }
        }
      }
    }
    if (zipDataFound) {
      LOGGER.debug("ZIP input stream closed. Created {} directories, and {} files.", numDirs,
          numFiles);
    } else {
      LOGGER.info("ZIP input stream closed. No ZIP data found.");
    }

  }
}