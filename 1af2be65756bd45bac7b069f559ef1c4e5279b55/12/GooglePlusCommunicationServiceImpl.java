package com.ciandt.techgallery.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ciandt.techgallery.service.GooglePlusCommunicationService;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Acl;
import com.google.api.services.plusDomains.model.Activity;
import com.google.api.services.plusDomains.model.PlusDomainsAclentryResource;
import com.google.appengine.api.users.User;

/**
 * Services for Google Plus communications Endpoint requests.
 *
 * @author Thulio Ribeiro
 *
 */
public class GooglePlusCommunicationServiceImpl implements GooglePlusCommunicationService {

  private static GooglePlusCommunicationServiceImpl instance;

  private GooglePlusCommunicationServiceImpl() {
  }

  /**
   * Singleton method for the service.
   *
   * @author <a href="mailto:tribeiro@ciandt.com"> Thulio Soares Ribeiro </a>
   * @since 23/10/2015
   *
   * @return GooglePlusCommunicationServiceImpl instance.
   */
  public static GooglePlusCommunicationServiceImpl getInstance() {
    if (instance == null) {
      instance = new GooglePlusCommunicationServiceImpl();
    }
    return instance;
  }

  /**
   * Create a post in the users google plus.
   *
   * @author <a href="mailto:tribeiro@ciandt.com"> Thulio Soares Ribeiro </a>
   * @since 23/10/2015
   *
   * @param content
   *          string with the content of the post.
   * @param user
   *          user thats adding a post.
   * @param req
   *          current servlet request.
   *
   */
  @Override
  public void postGooglePlus(String content, User user, HttpServletRequest req)
      throws InternalServerErrorException, BadRequestException, NotFoundException, IOException {

    // Create a list of ACL entries
    PlusDomainsAclentryResource resource = new PlusDomainsAclentryResource();
    resource.setType("domain");

    List<PlusDomainsAclentryResource> aclEntries = new ArrayList<PlusDomainsAclentryResource>();
    aclEntries.add(resource);

    Acl acl = new Acl();
    acl.setItems(aclEntries);
    acl.setDomainRestricted(true); // Required, this does the domain restriction

    // Create a new activity object to be executed
    Activity activity = new Activity().setObject(new Activity.PlusDomainsObject().setOriginalContent(content))
        .setAccess(acl);

    // Creating a google credential in base of the header authorization
    String header = req.getHeader("Authorization");
    String accesstoken = header.substring(header.indexOf(' ')).trim();
    GoogleCredential credential = new GoogleCredential().setAccessToken(accesstoken);

    // Create a new authorized API client according the credential
    PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), credential).build();

    // Execute the API request, which calls `activities.insert` for the logged
    // in user
    activity = plusDomains.activities().insert("me", activity).execute();
  }

}
