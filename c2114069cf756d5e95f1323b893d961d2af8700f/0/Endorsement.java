package com.ciandt.techgallery.persistence.model;

import com.google.api.server.spi.config.ApiTransformer;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Unindex;

import com.ciandt.techgallery.service.util.EndorsementTransformer;

import java.util.Date;

/**
 * Endorsement entity.
 * 
 * @author felipers
 *
 */
@Entity
@ApiTransformer(EndorsementTransformer.class)
public class Endorsement extends BaseEntity<Long> {

  /*
   * Constants --------------------------------------------
   */
  public static final String ID = "id";
  public static final String ENDORSER = "endorser";
  public static final String ENDORSED = "endorsed";
  public static final String TIMESTAMP = "timestamp";
  public static final String ACTIVE = "active";
  public static final String TECHNOLOGY = "technology";

  @Id
  private Long id;

  @Index
  @Load
  private Ref<TechGalleryUser> endorser;

  @Index
  @Load
  private Ref<TechGalleryUser> endorsed;

  @Unindex
  private Date timestamp;

  @Index
  private boolean active;

  @Index
  @Load
  private Ref<Technology> technology;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  public Ref<TechGalleryUser> getEndorser() {
    return endorser;
  }

  public void setEndorser(Ref<TechGalleryUser> endorser) {
    this.endorser = endorser;
  }

  public Ref<TechGalleryUser> getEndorsed() {
    return endorsed;
  }

  public void setEndorsed(Ref<TechGalleryUser> endorsed) {
    this.endorsed = endorsed;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public Ref<Technology> getTechnology() {
    return technology;
  }

  public void setTechnology(Ref<Technology> technology) {
    this.technology = technology;
  }

  public TechGalleryUser getEndorserEntity() {
    if (endorser != null) {
      return endorser.get();
    }

    return null;
  }

  public TechGalleryUser getEndorsedEntity() {
    if (endorsed != null) {
      return endorsed.get();
    }

    return null;
  }


  public Technology getTechnologyEntity() {
    if (technology != null) {
      return technology.get();
    }

    return null;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
