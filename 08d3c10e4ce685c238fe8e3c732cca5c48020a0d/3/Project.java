package com.ciandt.techgallery.persistence.model;

import com.ciandt.techgallery.service.transformer.ProjectTransformer;
import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Project entity.
 *
 * @author moizes
 *
 */
@Entity
@ApiTransformer(ProjectTransformer.class)
public class Project extends BaseEntity<Long> {

  /*
   * Constants --------------------------------------------
   */
  public static final String ID = "id";
  public static final String NAME = "name";

  @Id
  private Long id;

    @Index
    private String name;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Compare the Project entity by Name
     *
     * @param project entity.
     */
    public int compareTo(Project project){
        if(project != null) {
            return this.id.compareTo(project.getId()) == 0 ? 0 : 1;
        }
        return 1;
    }
}
