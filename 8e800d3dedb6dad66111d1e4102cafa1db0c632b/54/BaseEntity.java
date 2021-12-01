package com.ciandt.techgallery.persistence.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Unindex;

/**
 * BaseEntity entity.
 * 
 * @author Felipe Goncalves de Castro
 *
 */
public abstract class BaseEntity<ID> {

  @Unindex
  Date inactivatedDate;

  public abstract ID getId();

  public abstract void setId(ID id);

  public Date getInactivatedDate() {
    return inactivatedDate;
  }

  public void setInactivatedDate(Date inactivatedDate) {
    this.inactivatedDate = inactivatedDate;
  }

}
