package com.ciandt.techgallery.service.impl;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Ref;

import com.ciandt.techgallery.persistence.dao.EndorsementDAO;
import com.ciandt.techgallery.persistence.dao.impl.EndorsementDAOImpl;
import com.ciandt.techgallery.persistence.model.Endorsement;
import com.ciandt.techgallery.persistence.model.TechGalleryUser;
import com.ciandt.techgallery.persistence.model.Technology;
import com.ciandt.techgallery.service.EndorsementService;
import com.ciandt.techgallery.service.SkillService;
import com.ciandt.techgallery.service.TechnologyService;
import com.ciandt.techgallery.service.UserServiceTG;
import com.ciandt.techgallery.service.model.EndorsementEntityResponse;
import com.ciandt.techgallery.service.model.EndorsementResponse;
import com.ciandt.techgallery.service.model.EndorsementsGroupedByEndorsedTransient;
import com.ciandt.techgallery.service.model.EndorsementsResponse;
import com.ciandt.techgallery.service.model.Response;
import com.ciandt.techgallery.service.model.ShowEndorsementsResponse;
import com.ciandt.techgallery.service.model.SkillResponse;
import com.ciandt.techgallery.utils.i18n.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Services for Endorsement Endpoint requests.
 *
 * @author felipers
 *
 */
public class EndorsementServiceImpl implements EndorsementService {

  /*
   * Constants --------------------------------------------
   */
  private static final I18n i18n = I18n.getInstance();

  /*
   * Attributes --------------------------------------------
   */
  private static EndorsementServiceImpl instance;

  /** tech gallery user service. */
  UserServiceTG userService = UserServiceTGImpl.getInstance();
  /** endorsement dao. */
  EndorsementDAO endorsementDAO = EndorsementDAOImpl.getInstance();
  /** skill service. */
  SkillService skillService = SkillServiceImpl.getInstance();
  /** Technology service. */
  TechnologyService techService = TechnologyServiceImpl.getInstance();

  /*
   * Constructors --------------------------------------------
   */
  private EndorsementServiceImpl() {}

  /**
   * Singleton method for the service.
   *
   * @author <a href="mailto:joaom@ciandt.com"> João Felipe de Medeiros Moreira </a>
   * @since 07/10/2015
   *
   * @return EndorsementServiceImpl instance.
   */
  public static EndorsementServiceImpl getInstance() {
    if (instance == null) {
      instance = new EndorsementServiceImpl();
    }
    return instance;
  }

  /*
   * Methods --------------------------------------------
   */
  /**
   * POST for adding a endorsement. TODO: Refactor - Extract Method
   *
   * @throws InternalServerErrorException in case something goes wrong
   * @throws OAuthRequestException in case of authentication problem
   * @throws NotFoundException in case the information are not founded
   * @throws BadRequestException in case a request with problem were made.
   */
  @Override
  public Response addOrUpdateEndorsement(EndorsementResponse endorsement, User user)
      throws InternalServerErrorException, BadRequestException, NotFoundException,
      OAuthRequestException {
    // endorser user google id
    String googleId;
    // endorser user from techgallery datastore
    TechGalleryUser tgEndorserUser;
    // endorsed user from techgallery datastore
    TechGalleryUser tgEndorsedUser;
    // endorsed email
    String endorsedEmail;
    // technology from techgallery datastore
    Technology technology;

    // User from endpoint (endorser) can't be null
    if (user == null) {
      throw new OAuthRequestException(i18n.t("OAuth error, null user reference!"));
    } else {
      googleId = user.getUserId();
    }

    // TechGalleryUser can't be null and must exists on datastore
    if (googleId == null || googleId.equals("")) {
      throw new NotFoundException(i18n.t("Current user was not found!"));
    } else {
      // get the TechGalleryUser from datastore
      tgEndorserUser = userService.getUserByGoogleId(googleId);
      if (tgEndorserUser == null) {
        throw new BadRequestException(i18n.t("Endorser user do not exists on datastore!"));
      }
      tgEndorserUser.setGoogleId(googleId);
    }

    // endorsed email can't be null.
    endorsedEmail = endorsement.getEndorsed();
    if (endorsedEmail == null || endorsedEmail.equals("")) {
      throw new BadRequestException(i18n.t("Endorsed email was not especified!"));
    } else {
      // get user from PEOPLE
      tgEndorsedUser = userService.getUserSyncedWithProvider(endorsedEmail);
      if (tgEndorsedUser == null) {
        throw new BadRequestException(i18n.t("Endorsed email was not found on PEOPLE!"));
      }
    }

    // technology id can't be null and must exists on datastore
    final String technologyId = endorsement.getTechnology();
    if (technologyId == null || technologyId.equals("")) {
      throw new BadRequestException(i18n.t("Technology was not especified!"));
    } else {
      technology = techService.getTechnologyById(technologyId);
      if (technology == null) {
        throw new BadRequestException(i18n.t("Technology do not exists!"));
      }
    }

    // final checks and persist
    // user cannot endorse itself
    if (tgEndorserUser.getId().equals(tgEndorsedUser.getId())) {
      throw new BadRequestException(i18n.t("You cannot endorse yourself!"));
    }
    // user cannot endorse the same people twice
    if (endorsementDAO.findActivesByUsers(tgEndorserUser, tgEndorsedUser, technology).size() > 0) {
      throw new BadRequestException(i18n.t("You already endorsed this user for this technology"));
    }
    // create endorsement and save it
    final Endorsement entity = new Endorsement();
    entity.setEndorser(Ref.create(tgEndorserUser));
    entity.setEndorsed(Ref.create(tgEndorsedUser));
    entity.setTimestamp(new Date());
    entity.setTechnology(Ref.create(technology));
    entity.setActive(true);
    endorsementDAO.add(entity);
    // return the added entity
    return getEndorsement(entity.getId());
  }

  /**
   * POST for adding a endorsement. TODO: Refactor - Extract Method. Same in
   * {@link #addOrUpdateEndorsement(EndorsementResponse, User)}
   *
   * @throws InternalServerErrorException
   * @throws BadRequestException
   * @throws NotFoundException
   * @throws OAuthRequestException
   */
  @Override
  public Response addOrUpdateEndorsementPlusOne(EndorsementResponse endorsement, User user)
      throws InternalServerErrorException, BadRequestException, NotFoundException,
      OAuthRequestException {

    // endorser user google id
    String googleId;
    // User from endpoint (endorser) can't be null
    if (user == null) {
      throw new OAuthRequestException(i18n.t("OAuth error, null user reference!"));
    } else {
      googleId = user.getUserId();
    }

    // TechGalleryUser can't be null and must exists on datastore
    // endorser user from techgallery datastore
    TechGalleryUser tgEndorserUser;
    if (googleId == null || googleId.equals("")) {
      throw new NotFoundException(i18n.t("Current user was not found!"));
    } else {
      // get the TechGalleryUser from datastore
      tgEndorserUser = userService.getUserByGoogleId(googleId);
      if (tgEndorserUser == null) {
        throw new BadRequestException(i18n.t("Endorser user do not exists on datastore!"));
      }
      tgEndorserUser.setGoogleId(googleId);
    }

    // endorsed user from techgallery datastore
    TechGalleryUser tgEndorsedUser;
    // endorsed email can't be null.
    final String endorsedEmail = endorsement.getEndorsed();
    if (endorsedEmail == null || endorsedEmail.equals("")) {
      throw new BadRequestException(i18n.t("Endorsed email was not especified!"));
    } else {
      // get user from PEOPLE
      tgEndorsedUser = userService.getUserSyncedWithProvider(endorsedEmail);
      if (tgEndorsedUser == null) {
        throw new BadRequestException(i18n.t("Endorsed email was not found on PEOPLE!"));
      }
    }

    // technology from techgallery datastore
    Technology technology;
    // technology id can't be null and must exists on datastore
    final String technologyId = endorsement.getTechnology();
    if (technologyId == null || technologyId.equals("")) {
      throw new BadRequestException(i18n.t("Technology was not especified!"));
    } else {
      technology = techService.getTechnologyById(technologyId);
      if (technology == null) {
        throw new BadRequestException(i18n.t("Technology do not exists!"));
      }
    }

    // final checks and persist
    // user cannot endorse itself
    if (tgEndorserUser.getId().equals(tgEndorsedUser.getId())) {
      throw new BadRequestException(i18n.t("You cannot endorse yourself!"));
    }

    // should exist only one active endorsement per
    // endorser/endorsed/technology. the others are
    // saved for history purpose. if already exist one active endorsement, set
    // to inactive.
    // if not, add a new one as active
    final List<Endorsement> endorsements =
        endorsementDAO.findActivesByUsers(tgEndorserUser, tgEndorsedUser, technology);
    if (endorsements.size() == 1) {
      endorsements.get(0).setInactivatedDate(new Date());
      endorsements.get(0).setActive(false);
      endorsementDAO.update(endorsements.get(0));
      return getEndorsement(endorsements.get(0).getId());
    } else if (endorsements.size() > 1) {
      throw new BadRequestException(
          i18n.t("More than one active endorserment for the same endorser/endorsed/technology!"));
    }

    // create endorsement and save it
    final Endorsement entity = new Endorsement();
    entity.setEndorser(Ref.create(tgEndorserUser));
    entity.setEndorsed(Ref.create(tgEndorsedUser));
    entity.setTimestamp(new Date());
    entity.setTechnology(Ref.create(technology));
    entity.setActive(true);
    endorsementDAO.add(entity);
    // return the added entity
    return getEndorsement(entity.getId());
  }

  /**
   * GET for getting all endorsements.
   */
  @Override
  public Response getEndorsements() throws InternalServerErrorException, NotFoundException {
    final List<Endorsement> endrsEntities = endorsementDAO.findAll();
    // if list is null, return a not found exception
    if (endrsEntities == null) {
      throw new NotFoundException(i18n.t("No endorsement was found."));
    } else {
      final EndorsementsResponse response = new EndorsementsResponse();
      final List<EndorsementEntityResponse> internList = new ArrayList<EndorsementEntityResponse>();

      for (int i = 0; i < endrsEntities.size(); i++) {
        final Endorsement entity = endrsEntities.get(i);
        final EndorsementEntityResponse endrsResponseItem = new EndorsementEntityResponse();
        endrsResponseItem.setId(entity.getId());
        endrsResponseItem.setEndorser(entity.getEndorserEntity());
        endrsResponseItem.setEndorsed(entity.getEndorsedEntity());
        endrsResponseItem.setTimestamp(entity.getTimestamp());
        endrsResponseItem.setTechnology(entity.getTechnologyEntity());
        internList.add(endrsResponseItem);
      }
      response.setEndorsements(internList);
      return response;
    }
  }

  /**
   * GET for getting one endorsement.
   */
  @Override
  public Response getEndorsement(Long id) throws NotFoundException {
    final Endorsement endorseEntity = endorsementDAO.findById(id);
    // if technology is null, return a not found exception
    if (endorseEntity == null) {
      throw new NotFoundException(i18n.t("No endorsement was found."));
    } else {
      final EndorsementEntityResponse response = new EndorsementEntityResponse();
      response.setId(endorseEntity.getId());
      response.setId(endorseEntity.getId());
      response.setEndorser(endorseEntity.getEndorserEntity());
      response.setEndorsed(endorseEntity.getEndorsedEntity());
      response.setTimestamp(endorseEntity.getTimestamp());
      response.setTechnology(endorseEntity.getTechnologyEntity());
      response.setActive(endorseEntity.isActive());
      return response;
    }
  }

  /**
   * GET for getting one endorsement.
   *
   * @throws InternalServerErrorException in case something goes wrong
   * @throws OAuthRequestException in case of authentication problem
   * @throws NotFoundException in case the information are not founded
   * @throws BadRequestException in case a request with problem were made.
   */
  @Override
  public Response getEndorsementsByTech(String techId, User user)
      throws InternalServerErrorException, BadRequestException, NotFoundException,
      OAuthRequestException {
    final List<Endorsement> endorsementsByTech = endorsementDAO.findAllActivesByTechnology(techId);
    final List<EndorsementsGroupedByEndorsedTransient> grouped =
        groupEndorsementByEndorsed(endorsementsByTech, techId);
    Collections.sort(grouped, new EndorsementsGroupedByEndorsedTransient());

    final Technology technology = techService.getTechnologyById(techId);
    techService.updateEdorsedsCounter(technology, grouped.size());

    final ShowEndorsementsResponse response = new ShowEndorsementsResponse();
    response.setEndorsements(grouped);
    return response;
  }

  @Override
  public List<EndorsementsGroupedByEndorsedTransient> groupEndorsementByEndorsed(
      List<Endorsement> endorsements, String techId) throws BadRequestException, NotFoundException,
          InternalServerErrorException, OAuthRequestException {

    final Map<TechGalleryUser, List<TechGalleryUser>> mapUsersGrouped =
        new HashMap<TechGalleryUser, List<TechGalleryUser>>();

    for (final Endorsement endorsement : endorsements) {
      final TechGalleryUser endorsed = endorsement.getEndorsedEntity();

      if (mapUsersGrouped.containsKey(endorsed)) {
        final List<TechGalleryUser> endorsersList = mapUsersGrouped.get(endorsed);
        endorsersList.add(endorsement.getEndorserEntity());
        mapUsersGrouped.put(endorsed, endorsersList);
      } else {
        final List<TechGalleryUser> endorsersList = new ArrayList<TechGalleryUser>();
        endorsersList.add(endorsement.getEndorserEntity());
        mapUsersGrouped.put(endorsed, endorsersList);
      }
    }
    return transformGroupedUserMapIntoList(mapUsersGrouped, techId);
  }

  private List<EndorsementsGroupedByEndorsedTransient> transformGroupedUserMapIntoList(
      Map<TechGalleryUser, List<TechGalleryUser>> mapUsersGrouped, String techId)
          throws BadRequestException, NotFoundException, InternalServerErrorException,
          OAuthRequestException {
    final List<EndorsementsGroupedByEndorsedTransient> groupedList =
        new ArrayList<EndorsementsGroupedByEndorsedTransient>();

    for (final Map.Entry<TechGalleryUser, List<TechGalleryUser>> entry : mapUsersGrouped
        .entrySet()) {
      final EndorsementsGroupedByEndorsedTransient grouped =
          new EndorsementsGroupedByEndorsedTransient();
      grouped.setEndorsed(entry.getKey());
      final SkillResponse response =
          (SkillResponse) skillService.getUserSkill(techId, entry.getKey());
      if (response != null) {
        grouped.setEndorsedSkill(response.getValue());
      } else {
        grouped.setEndorsedSkill(0);
      }
      grouped.setEndorsers(entry.getValue());
      groupedList.add(grouped);
    }
    return groupedList;
  }
}
