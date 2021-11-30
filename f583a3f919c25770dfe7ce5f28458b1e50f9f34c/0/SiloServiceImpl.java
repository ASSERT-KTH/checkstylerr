package org.benetech.servicenet.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.benetech.servicenet.domain.UserProfile;
import org.benetech.servicenet.service.SiloService;
import org.benetech.servicenet.domain.Silo;
import org.benetech.servicenet.repository.SiloRepository;
import org.benetech.servicenet.service.dto.SiloDTO;
import org.benetech.servicenet.service.dto.provider.SiloWithLogoDTO;
import org.benetech.servicenet.service.mapper.SiloMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Silo}.
 */
@Service
@Transactional
public class SiloServiceImpl implements SiloService {

    private final Logger log = LoggerFactory.getLogger(SiloServiceImpl.class);

    private final SiloRepository siloRepository;

    private final SiloMapper siloMapper;

    public SiloServiceImpl(SiloRepository siloRepository, SiloMapper siloMapper) {
        this.siloRepository = siloRepository;
        this.siloMapper = siloMapper;
    }

    /**
     * Save a silo.
     *
     * @param siloDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public SiloDTO save(SiloWithLogoDTO siloDTO) {
        log.debug("Request to save Silo : {}", siloDTO);
        Silo silo = siloMapper.toEntity(siloDTO);
        silo = siloRepository.save(silo);
        return siloMapper.toDto(silo);
    }

    /**
     * Get all the silos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SiloDTO> findPagedAll(Pageable pageable) {
        log.debug("Request to get all Silos");
        return siloRepository.findAll(pageable)
            .map(siloMapper::toDto);
    }

    /**
     * Get all the silos.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SiloDTO> findAll() {
        log.debug("Request to get all Silos");
        return siloRepository.findAll().stream()
            .map(siloMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one silo by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<SiloWithLogoDTO> findOne(UUID id) {
        log.debug("Request to get Silo : {}", id);
        return siloRepository.findById(id)
            .map(this::toSiloWithLogo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SiloDTO> findOneByName(String name) {
        log.debug("Request to get Silo : {}", name);
        return siloRepository.getByName(name)
            .map(siloMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Optional<SiloWithLogoDTO> findOneByNameOrId(String nameOrId) {
        log.debug("Request to get Silo : {}", nameOrId);
        UUID id = null;
        try {
            id = UUID.fromString(nameOrId);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return siloRepository.getByNameOrId(nameOrId, id)
            .map(this::toSiloWithLogo);
    }

    private SiloWithLogoDTO toSiloWithLogo(Silo silo) {
        SiloWithLogoDTO siloWithLogoDTO = new SiloWithLogoDTO();
        siloWithLogoDTO.setLogoBase64(silo.getLogoBase64());
        siloWithLogoDTO.setId(silo.getId());
        siloWithLogoDTO.setName(silo.getName());
        siloWithLogoDTO.setPublic(silo.isPublic());
        siloWithLogoDTO.setReferralEnabled(silo.isReferralEnabled());
        return siloWithLogoDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Silo> getOneByName(String name) {
        log.debug("Request to get Silo : {}", name);
        return siloRepository.getByName(name);
    }

    /**
     * Delete the silo by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(UUID id) {
        log.debug("Request to delete Silo : {}", id);
        Set<UserProfile> userProfileSet = siloRepository.getOne(id).getUserProfiles();
        for (UserProfile userProfile : userProfileSet) {
            userProfile.setSilo(null);
        }
        siloRepository.deleteById(id);
    }
}
