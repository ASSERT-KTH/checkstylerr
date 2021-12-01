/* DigiDoc4J library
 *
 * This software is released under either the GNU Library General Public
 * License (see LICENSE.LGPL).
 *
 * Note that the only valid version of the LGPL license as far as this
 * project is concerned is the original GNU Library General Public License
 * Version 2.1, February 1999
 */

package org.digidoc4j.impl.asic.xades.validation;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import eu.europa.esig.dss.enumerations.RevocationType;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.DSSRevocationUtils;
import eu.europa.esig.dss.validation.reports.Reports;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.digidoc4j.Configuration;
import org.digidoc4j.X509Cert;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.digidoc4j.exceptions.SignedWithExpiredCertificateException;
import org.digidoc4j.exceptions.TimestampAfterOCSPResponseTimeException;
import org.digidoc4j.exceptions.TimestampAndOcspResponseTimeDeltaTooLargeException;
import org.digidoc4j.exceptions.UntrustedRevocationSourceException;
import org.digidoc4j.impl.asic.xades.XadesSignature;
import org.digidoc4j.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.validation.timestamp.TimestampToken;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.xades.validation.XAdESSignature;

public class TimestampSignatureValidator extends XadesSignatureValidator {

  private static final Logger log = LoggerFactory.getLogger(TimestampSignatureValidator.class);

  public TimestampSignatureValidator(XadesSignature signature, Configuration configuration) {
    super(signature, configuration);
  }

  public TimestampSignatureValidator(XadesSignature signature) {
    super(signature, Configuration.getInstance());
  }

  @Override
  protected void populateValidationErrors() {
    super.populateValidationErrors();
    this.addCertificateExpirationError();
    this.addSigningTimeErrors();
    this.addRevocationErrors();
  }

  private void addSigningTimeErrors() {
    XAdESSignature signature = this.getDssSignature();
    List<TimestampToken> signatureTimestamps = signature.getSignatureTimestamps();
    if (signatureTimestamps == null || signatureTimestamps.isEmpty()) {
      return;
    }
    Date timestamp = signatureTimestamps.stream()
            .map(TimestampToken::getGenerationTime)
            .filter(t -> t != null)
            .sorted()
            .findFirst()
            .orElse(null);
    if (timestamp == null) {
      return;
    }
    List<BasicOCSPResp> signingCertificateOcspResponses = getOcspResponsesForSigningCertificate(signature);
    if (signingCertificateOcspResponses == null || signingCertificateOcspResponses.isEmpty()) {
      return;
    }
    Date ocspTime = signingCertificateOcspResponses.stream()
            .map(BasicOCSPResp::getProducedAt)
            .filter(t -> DateUtils.compareAtSamePrecision(timestamp, t) <= 0)
            .sorted()
            .findFirst()
            .orElse(null);
    if (ocspTime == null) {
      this.log.error("OCSP response production time is before timestamp time");
      addValidationError(new TimestampAfterOCSPResponseTimeException());
      return;
    }
    int deltaLimit = this.configuration.getRevocationAndTimestampDeltaInMinutes();
    long differenceInMinutes = DateUtils.differenceInMinutes(timestamp, ocspTime);
    this.log.debug("Difference in minutes: <{}>", differenceInMinutes);
    if (!DateUtils.isInRangeMinutes(timestamp, ocspTime, deltaLimit)) {
      this.log.error("The difference between the OCSP response production time and the signature timestamp is too large <{} minutes>", differenceInMinutes);
      this.addValidationError(new TimestampAndOcspResponseTimeDeltaTooLargeException());
    } else if (this.configuration.getAllowedTimestampAndOCSPResponseDeltaInMinutes() < differenceInMinutes && differenceInMinutes < deltaLimit) {
      this.log.warn("The difference (in minutes) between the OCSP response production time and the signature timestamp is in allowable range (<{}>, allowed maximum <{}>)", differenceInMinutes, deltaLimit);
      this.addValidationWarning(new DigiDoc4JException("The difference between the OCSP response time and the signature timestamp is in allowable range"));
    }
  }

  private void addCertificateExpirationError() {
    Date signingTime = this.signature.getTrustedSigningTime();
    if (signingTime == null) {
      return;
    }
    X509Certificate signerCert = this.signature.getSigningCertificate().getX509Certificate();
    boolean isCertValid = signingTime.compareTo(signerCert.getNotBefore()) >= 0
            && signingTime.compareTo(signerCert.getNotAfter()) <= 0;
    if (!isCertValid) {
      this.log.error("Signature has been created with expired certificate");
      this.addValidationError(new SignedWithExpiredCertificateException());
    }
  }

  private void addRevocationErrors() {
    Reports reports = this.signature.validate().getReports();
    DiagnosticData diagnosticData = reports.getDiagnosticData();
    if (diagnosticData == null) {
      return;
    }
    SimpleReport simpleReport = reports.getSimpleReport();
    String signatureId = simpleReport.getFirstSignatureId();
    if (signatureId == null) {
      return;
    }
    String certificateId = diagnosticData.getSigningCertificateId(signatureId);
    if (certificateId == null) {
      return;
    }
    RevocationType certificateRevocationSource = diagnosticData.getCertificateRevocationSource(certificateId);
    this.log.debug("Revocation source is <{}>", certificateRevocationSource);
    if (RevocationType.CRL.equals(certificateRevocationSource)) {
      this.log.error("Signing certificate revocation source is CRL instead of OCSP");
      this.addValidationError(new UntrustedRevocationSourceException());
    }
  }

  private List<BasicOCSPResp> getOcspResponsesForSigningCertificate(XAdESSignature signature) {
    X509Cert signingCertificate = this.signature.getSigningCertificate();
    if (signingCertificate == null) {
      return null;
    }
    BigInteger certificateSerialNumber = signingCertificate.getX509Certificate().getSerialNumber();
    return signature.getOCSPSource().getAllRevocationBinaries()
            .stream()
            .map(o -> {
              try {
                return DSSRevocationUtils.loadOCSPFromBinaries(o.getBinaries());
              } catch (IOException e) {
                throw new IllegalArgumentException("Invalid ocsp binary");
              }
            })
            .filter(r -> isOcspResponseForCertificate(r, certificateSerialNumber))
            .collect(Collectors.toList());
  }

  private static boolean isOcspResponseForCertificate(BasicOCSPResp ocspResponse, BigInteger certificateSerialNumber) {
    return Arrays.stream(ocspResponse.getResponses())
            .anyMatch(r -> certificateSerialNumber.equals(r.getCertID().getSerialNumber()));
  }

}
