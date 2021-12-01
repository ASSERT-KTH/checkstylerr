/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.ip.Permissions;

public class DescriptiveMetadataVersionsBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private DescriptiveMetadataViewBundle descriptiveMetadata;
  private List<BinaryVersionBundle> versions;
  private Permissions permissions;

  public DescriptiveMetadataVersionsBundle() {
    super();
  }

  public DescriptiveMetadataVersionsBundle(DescriptiveMetadataViewBundle descriptiveMetadata,
    List<BinaryVersionBundle> versions, Permissions permissions) {
    super();
    this.descriptiveMetadata = descriptiveMetadata;
    this.versions = versions;
    this.permissions = permissions;
  }

  public DescriptiveMetadataViewBundle getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(DescriptiveMetadataViewBundle descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public List<BinaryVersionBundle> getVersions() {
    return versions;
  }

  public void setVersions(List<BinaryVersionBundle> versions) {
    this.versions = versions;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

}
