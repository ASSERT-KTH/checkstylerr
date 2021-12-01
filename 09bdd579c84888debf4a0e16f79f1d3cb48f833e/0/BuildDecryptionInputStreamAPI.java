package name.neuhalfen.projects.crypto.bouncycastle.openpgp;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.decrypting.DecryptionStreamFactory;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.validation.SignatureValidationStrategies;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.validation.SignatureValidationStrategy;
import org.bouncycastle.openpgp.PGPException;

/**
 * This class implements the builder for decrypting Streams.
 */
@SuppressWarnings({"PMD.AtLeastOneConstructor","PMD.AccessorMethodGeneration"})
public final class BuildDecryptionInputStreamAPI {

  @Nonnull
  private KeyringConfig keyringConfig;
  @Nonnull
  private SignatureValidationStrategy signatureCheckingMode;

  /**
   * Start building by passing in the keyring config.
   *
   * @param keyringConfig Keyring
   * @return next build step
   */
  @Nonnull
  public Validation withConfig(@Nullable KeyringConfig keyringConfig) {
    if (keyringConfig == null) {
      throw new IllegalArgumentException("keyringConfig must not be null");
    }

    BuildDecryptionInputStreamAPI.this.keyringConfig = keyringConfig;
    return new Validation();
  }

  public interface Build {

    /**
     * Build the final decrypted input stream. . This method will start reading the cipherstream
     * until it finds the encrypted plaintext. . If the source data is NOT signed, but a signature
     * is REQUIRED, then this function might even throw. . Signatures are verified AFTER decryption
     * (reading the whole(!) plaintext stream).
     *
     * @param encryptedData An encrypted input stream. *Will not be closed.*
     * @return Plaintext stream. Signatures are checked the moment EOF is reached.
     * @throws IOException IO is dangerous. Also wraps several GPG exceptions.
     * @throws NoSuchProviderException BC provider is not registered
     */
    @Nonnull
    InputStream fromEncryptedInputStream(@Nullable InputStream encryptedData)
        throws IOException, NoSuchProviderException;
  }

  public final class Validation {

    /**
     * Decryption will enforce that the ciphertext has been signed by ALL of the public key ids
     * passed. . Key-ids are long values. For example with the following keyring . # gpg -k
     * --keyid-format=0xlong ... pub   2048R/0x3DF16BD7C3F280F3 2015-09-27 uid [ultimate] Rezi
     * Recipient (Pasword: recipient) &lt;recipient@example.com&gt; sub 2048R/0x54A3DB374F787AB7
     * 2015-09-27 ... . --&gt; . andRequireSignatureFromAllKeys(0x54A3DB374F787AB7L)
     *
     * @param publicKeyIds a valid signature from all of the passed keys is required. The keys MUST
     * exist in the public keyring.
     * @return the next build step
     */
    @Nonnull
    public Build andRequireSignatureFromAllKeys(@Nullable Long... publicKeyIds) {
      if (publicKeyIds == null || publicKeyIds.length == 0) {
        throw new IllegalArgumentException("publicKeyIds must not be null or empty");
      }

      BuildDecryptionInputStreamAPI.this.signatureCheckingMode = SignatureValidationStrategies
          .requireSignatureFromAllKeys(publicKeyIds);
      return new Builder();
    }


    /**
     * Decryption will enforce that the ciphertext has been signed by ALL of the public key ids
     * passed. . Key-ids are long values. For example with the following keyring . # gpg -k
     * --keyid-format=0xlong . pub   2048R/0x3DF16BD7C3F280F3 2015-09-27 uid [ultimate] Rezi
     * Recipient (Pasword: recipient) &lt;recipient@example.com&gt; sub 2048R/0x54A3DB374F787AB7
     * 2015-09-27 . . --&gt; . andRequireSignatureFromAllKeys("recipient@example.com")
     *
     * @param userIds a valid signature from all of the passed keys is required. The keys MUST exist
     * in the public keyring.
     * @return the next build step
     * @throws PGPException error extracting public keys from keyring
     * @throws IOException IO is dangerous
     */
    @Nonnull
    public Build andRequireSignatureFromAllKeys(@Nullable String... userIds)
        throws PGPException, IOException {

      if (userIds == null || userIds.length == 0) {
        throw new IllegalArgumentException("userIds must not be null or empty");
      }

      BuildDecryptionInputStreamAPI.this.signatureCheckingMode = SignatureValidationStrategies
          .requireSignatureFromAllKeys(keyringConfig.getPublicKeyRings(), userIds);
      return new Builder();
    }

    /**
     * Enforce a valid signature from *any* public key in the keyring. . Signatures of keys NOT
     * present in the keyring are IGNORED (treated as not existing).
     *
     * @return next build step
     */
    @Nonnull
    public Build andValidateSomeoneSigned() {
      BuildDecryptionInputStreamAPI.this.signatureCheckingMode = SignatureValidationStrategies
          .requireAnySignature();
      return new Builder();
    }

    /**
     * Ignore all, even invalid(!) signatures.
     *
     * @return next build step
     */
    @Nonnull
    public Build andIgnoreSignatures() {
      BuildDecryptionInputStreamAPI.this.signatureCheckingMode = SignatureValidationStrategies
          .ignoreSignatures();
      return new Builder();
    }


    public final class Builder implements Build {

      /**
       * Build the final decrypted input stream. . This method will start reading the cipherstream
       * until it finds the encrypted plaintext. . If the source data is NOT signed, but a signature
       * is REQUIRED, then this function might even throw. . Signatures are verified AFTER
       * decryption (reading the whole(!) plaintext stream).
       *
       * @param encryptedData An encrypted input stream. Will not be closed.
       * @return Plaintext stream. Signatures are checked the moment EOF is reached.
       * @throws IOException IO is dangerous. Also wraps several GPG exceptions.
       * @throws NoSuchProviderException BC provider is not registered
       */
      @Nonnull
      public InputStream fromEncryptedInputStream(@Nullable InputStream encryptedData)
          throws IOException, NoSuchProviderException {
        if (encryptedData == null) {
          throw new IllegalArgumentException("encryptedData must not be null");
        }

        final DecryptionStreamFactory pgpInputStreamFactory =
            DecryptionStreamFactory.create(
                BuildDecryptionInputStreamAPI.this.keyringConfig,
                BuildDecryptionInputStreamAPI.this.signatureCheckingMode);

        return pgpInputStreamFactory.wrapWithDecryptAndVerify(encryptedData);
      }
    }
  }
}
