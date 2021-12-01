package name.neuhalfen.projects.crypto.bouncycastle.openpgp.validation;


import java.security.SignatureException;
import java.util.Map;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;

final class IgnoreSignaturesValidationStrategy implements SignatureValidationStrategy {

  @Override
  public void validateSignatures(PGPObjectFactory factory,
      Map<Long, PGPOnePassSignature> onePassSignatures) throws SignatureException, PGPException {
    // Ignore
  }


  @Override
  public boolean isRequireSignatureCheck() {
    return false;
  }
}
