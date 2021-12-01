/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

public class StreamResponse implements EntityResponse {
  private String filename;
  private String mediaType;
  private long fileSize = -1;
  private ConsumesOutputStream stream;

  public StreamResponse(String filename, String mediaType, ConsumesOutputStream stream) {
    super();
    this.filename = filename;
    this.mediaType = mediaType;
    this.stream = stream;
  }

  public StreamResponse(String filename, String mediaType, long fileSize, ConsumesOutputStream stream) {
    super();
    this.filename = filename;
    this.mediaType = mediaType;
    this.fileSize = fileSize;
    this.stream = stream;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }

  @Override
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public ConsumesOutputStream getStream() {
    return stream;
  }

  public void setStream(ConsumesOutputStream stream) {
    this.stream = stream;
  }


}
