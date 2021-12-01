/* DigiDoc4J library
 *
 * This software is released under either the GNU Library General Public
 * License (see LICENSE.LGPL).
 *
 * Note that the only valid version of the LGPL license as far as this
 * project is concerned is the original GNU Library General Public License
 * Version 2.1, February 1999
 */

package org.digidoc4j.impl;

import eu.europa.esig.dss.model.x509.CertificateToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.digidoc4j.Configuration;
import org.digidoc4j.ServiceType;
import org.digidoc4j.TSLCertificateSource;
import org.digidoc4j.exceptions.CertificateValidationException;
import org.digidoc4j.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Created by Janar Rahumeel (CGI Estonia)
 */
public class CommonOCSPSource extends SKOnlineOCSPSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonOCSPSource.class);

  private boolean useNonce;
  private boolean useAiaOCSP = false;

  /**
   * @param configuration configuration
   */
  public CommonOCSPSource(Configuration configuration) {
    super(configuration);
    useNonce = configuration.isOcspNonceUsed();
  }

  @Override
  public String getAccessLocation(X509Certificate certificate) {
    if (getConfiguration().isAiaOcspPreferred()) {
      LOGGER.info("Trying to find AIA OCSP url for certificate");
      String aiaOcspFromCertificate = getAccessLocationFromCertificate(certificate);
      if (!StringUtils.isEmpty(aiaOcspFromCertificate)) {
        LOGGER.info("Found AIA OCSP url from certificate");
        setAiaOCspParams(certificate);
        return aiaOcspFromCertificate;
      } else {
        LOGGER.info("Could not find OCSP url from certificate. Trying to Retrieve it from configuration");
        String issuerCommonName = getCN(certificate.getIssuerX500Principal());
        String aiaOcspFromConfiguration = getConfiguration().getAiaOcspSourceByCN(issuerCommonName);
        if (!StringUtils.isEmpty(aiaOcspFromConfiguration)) {
          LOGGER.info("Found AIA OCSP url from configuration");
          setAiaOCspParams(certificate);
          return aiaOcspFromConfiguration;
        }
        LOGGER.info("Could not find OCSP url configuration. Using default OCSP source");
      }
    }

    setAndUsePayedOcspParams();
    return super.getAccessLocation(certificate);
  }

  @Override
  protected ServiceType getOCSPType() {
    return useAiaOCSP ? ServiceType.AIA_OCSP : ServiceType.OCSP;
  }

  @Override
  public Extension createNonce(X509Certificate certificate) {
    if (!useNonce) {
      LOGGER.info("Skipping creating nonce..");
      return null;
    }
    LOGGER.debug("Creating default OCSP nonce ...");
    return new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false, new DEROctetString(Helper.generateRandomBytes(32)));
  }

  @Override
  protected void verifyOcspResponderCertificate(CertificateToken token, Date producedAt) {
    verifyValidityDate(token, producedAt);
    TSLCertificateSource certificateSource = getConfiguration().getTSL();

    if (!certificateSource.isTrusted(token)
            && CollectionUtils.isEmpty(certificateSource.getBySubject(token.getIssuer()))) {
      throw CertificateValidationException.of(CertificateValidationException.CertificateValidationStatus.UNTRUSTED,
              String.format("OCSP response certificate <%s> match is not found in TSL", token.getDSSIdAsString()));
    }
    try {
      if (!token.getCertificate().getExtendedKeyUsage().contains(OID_OCSP_SIGNING)) {
        throw CertificateValidationException.of(CertificateValidationException.CertificateValidationStatus.TECHNICAL,
                String.format("OCSP response certificate <%s> does not have 'OCSPSigning' extended key usage", token.getDSSIdAsString()));
      }
    } catch (CertificateParsingException e) {
      throw CertificateValidationException.of(CertificateValidationException.CertificateValidationStatus.TECHNICAL,
              String.format("Error on verifying 'OCSPSigning' extended key usage for OCSP response certificate <%s>", token.getDSSIdAsString()), e);
    }
  }

  @Override
  protected void checkNonce(BasicOCSPResp response, Extension expectedNonceExtension) {
    if (!useNonce) {
      return;
    }
    super.checkNonce(response, expectedNonceExtension);
  }

  private String getAccessLocationFromCertificate(X509Certificate certificate) {
    LOGGER.info("Trying to retrieve OCSP url from the certificate");
    try {
      byte[] encodedAiaBytes = certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
      if (encodedAiaBytes != null) {
        AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(JcaX509ExtensionUtils.parseExtensionValue(encodedAiaBytes));
        AccessDescription[] descriptions = aia.getAccessDescriptions();
        for (AccessDescription description : descriptions) {
          if (OCSPObjectIdentifiers.id_pkix_ocsp.getId().equals(description.getAccessMethod().getId())) {
            return description.getAccessLocation().getName().toString();
          }
        }
      }
    } catch (IOException e) {
      LOGGER.warn("Error reading ocsp location from certificate");
    }
    return null;
  }

  private String getCN(X500Principal x500Principal) {
    X500Name x500name = new X500Name(x500Principal.getName() );
    RDN cn = x500name.getRDNs(BCStyle.CN)[0];
    return IETFUtils.valueToString(cn.getFirst().getValue());
  }

  private void setAiaOCspParams(X509Certificate certificate) {
    useAiaOCSP = true;
    useNonce = getConfiguration().getUseNonceForAiaOcspByCN(getCN(certificate.getIssuerX500Principal()));
    if (getDataLoader() instanceof SkOCSPDataLoader) {
      ((SkOCSPDataLoader) getDataLoader()).setAsAiaOcsp(true);
    }
  }

  private void setAndUsePayedOcspParams() {
    useAiaOCSP = false;
    useNonce = getConfiguration().isOcspNonceUsed();
    if (getDataLoader() instanceof SkOCSPDataLoader) {
      ((SkOCSPDataLoader) getDataLoader()).setAsAiaOcsp(false);
    }
  }
}
