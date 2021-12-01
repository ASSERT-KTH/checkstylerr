package com.ciandt.techgallery.service.endpoint.profile;

import com.ciandt.techgallery.Constants;
import com.ciandt.techgallery.persistence.model.Technology;
import com.ciandt.techgallery.persistence.model.profile.UserProfile;
import com.ciandt.techgallery.service.impl.profile.UserProfileServiceImpl;
import com.ciandt.techgallery.service.profile.UserProfileService;
import com.ciandt.techgallery.service.endpoint.TechGalleryApiDefinition;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiReference;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.AuthLevel;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

@ApiReference(TechGalleryApiDefinition.class)
public class UserProfileEndpoint {

  private UserProfileService service = UserProfileServiceImpl.getInstance();

  @ApiMethod(name = "profile.get", path = "profile", httpMethod = "get")
  public UserProfile getUserProfileByEmail(@Named("email") String email, User user) throws NotFoundException {
    return service.findUserProfileByEmail(email);
  }

  @ApiMethod(name = "profile.addItem", path = "profile", httpMethod = "post")
  public UserProfile addItemToProfile(Technology technology, User user)
      throws NotFoundException, BadRequestException, InternalServerErrorException {
    return service.addItem(technology, user);
  }

}
