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

import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import org.digidoc4j.Configuration;
import org.digidoc4j.ExternalConnectionType;
import org.digidoc4j.ServiceType;
import org.digidoc4j.impl.asic.DataLoaderDecorator;

public class SkOCSPDataLoader extends SkDataLoader {

  private boolean isAiaOcsp = false;
  protected static final String OCSP_CONTENT_TYPE = "application/ocsp-request";

  public SkOCSPDataLoader(Configuration configuration) {
    DataLoaderDecorator.decorateWithProxySettingsFor(ExternalConnectionType.OCSP, this, configuration);
    DataLoaderDecorator.decorateWithSslSettingsFor(ExternalConnectionType.OCSP, this, configuration);
    contentType = OCSP_CONTENT_TYPE;
  }

  public void setAsAiaOcsp(boolean isAiaOcsp) {
    this.isAiaOcsp = isAiaOcsp;
  }

  @Override
  protected ServiceType getServiceType() {
    return isAiaOcsp ? ServiceType.AIA_OCSP : ServiceType.OCSP;
  }
}
