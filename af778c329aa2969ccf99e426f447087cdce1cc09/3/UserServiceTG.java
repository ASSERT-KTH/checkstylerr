package com.ciandt.techgallery.service;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import com.ciandt.techgallery.persistence.model.TechGalleryUser;
import com.ciandt.techgallery.service.model.Response;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Services for Users.
 * 
 * @author felipers
 *
 */
public interface UserServiceTG {

  /**
   * Service for getting all users.
   * 
   * @return users info or message error.
   * @throws NotFoundException in case the information is not found
   */
  Response getUsers() throws NotFoundException;

  /**
   * Service for getting one user.
   * 
   * @param id entity id.
   * @return user info or message error.
   * @throws NotFoundException
   */
  TechGalleryUser getUser(final Long id) throws NotFoundException;

  /**
   * Service for adding a user.
   * 
   * @param user json with user info.
   * @return user info or message error.
   * @throws BadRequestException in case a request with problem were made.
   */
  TechGalleryUser addUser(final TechGalleryUser user) throws BadRequestException;

  /**
   * Service for updating a user.
   * 
   * @param user json with user info.
   * @return user info or message error.
   * @throws BadRequestException in case a request with problem were made.
   */
  TechGalleryUser updateUser(final TechGalleryUser user) throws BadRequestException;

  /**
   * Service for getting an User by its Login.
   * 
   * @param id entity id.
   * @return
   * @throws NotFoundException
   */
  TechGalleryUser getUserByLogin(final String user) throws NotFoundException;

  /**
   * Service for getting an User from an external provider by its Login.
   * 
   * @param id entity id.
   * @return user
   * @throws InternalServerErrorException in case something goes wrong
   * @throws NotFoundException in case the information are not found
   * @throws BadRequestException in case a request with problem were made.
   */
  TechGalleryUser getUserFromProvider(final String user)
      throws NotFoundException, BadRequestException, InternalServerErrorException;

  /**
   * Service for getting an User from a external provider by googleId.
   * 
   * @param googleId User´s google id
   * @return user
   * @throws InternalServerErrorException in case something goes wrong
   * @throws NotFoundException in case the information are not found
   * @throws BadRequestException in case a request with problem were made.
   */
  TechGalleryUser getUserByGoogleId(final String googleId)
      throws NotFoundException, BadRequestException, InternalServerErrorException;

  /**
   * Finds a TechGalleryUser by his/her email.
   * 
   * @param email the user's email
   * @throws NotFoundException if the user is not found
   */
  TechGalleryUser getUserByEmail(String email) throws NotFoundException;

  /**
   * Checks if user exists on provider, syncs with tech gallery's datastore. If user exists, adds to
   * TG's datastore (if not there). Returns the user.
   * 
   * @param userLogin userLogin
   * @return the user saved on the datastore
   * @throws BadRequestException 
   */
  TechGalleryUser getUserSyncedWithProvider(String userLogin)
      throws NotFoundException, InternalServerErrorException, BadRequestException;

  /**
   * This method should be executed whenever a user logs in It check whether the user exists on TG's
   * datastore and create them, if not. It also checks if the user's email has been changed and
   * update it, in case it was changed.
   * 
   * @param user A Google AppEngine API user
   * @return A response with the user data as it is on TG datastore
   */
  TechGalleryUser handleLogin(User user, HttpServletRequest req) throws NotFoundException,
      BadRequestException, InternalServerErrorException, IOException, OAuthRequestException;

}
