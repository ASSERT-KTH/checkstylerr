package org.benetech.servicenet.repository;

import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import org.benetech.servicenet.domain.GeocodingResult;
import org.benetech.servicenet.domain.Silo;
import org.benetech.servicenet.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the GeocodingResult entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GeocodingResultRepository extends JpaRepository<GeocodingResult, UUID> {

    String ADDRESS_CACHE = "addressCache";

    List<GeocodingResult> findAllByAddress(String address);

    @Override
    <S extends GeocodingResult> S save(S var1);

    @Override
    <S extends GeocodingResult> List<S> saveAll(Iterable<S> var1);

    @Override
    <S extends GeocodingResult> S saveAndFlush(S var1);

    @Override
    void deleteInBatch(Iterable<GeocodingResult> var1);

    @Override
    void deleteAllInBatch();

    Page<GeocodingResult> findAll(Pageable pageable);

    @Query("SELECT DISTINCT gr.postalCode FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "WHERE gr.postalCode != ''"
        + "ORDER BY gr.postalCode")
    SortedSet<String> getDistinctPostalCodesFromGeoResults();

    @Query("SELECT DISTINCT gr.administrativeAreaLevel2 FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "WHERE gr.administrativeAreaLevel2 != ''"
        + "ORDER BY gr.administrativeAreaLevel2")
    SortedSet<String> getDistinctRegionsFromGeoResults();

    @Query("SELECT DISTINCT gr.locality FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "WHERE gr.locality != ''"
        + "ORDER BY gr.locality")
    SortedSet<String> getDistinctCityFromGeoResults();

    @Query("SELECT DISTINCT gr.postalCode FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "JOIN l.organization org "
        + "JOIN org.userProfiles userProfile "
        + "WHERE gr.postalCode != '' "
        + "AND userProfile.id != :#{#currentUserProfile.id} "
        + "AND userProfile.silo = :#{#currentUserProfile.silo} "
        + "AND userProfile.systemAccount.name = 'ServiceProvider' "
        + "ORDER BY gr.postalCode")
    SortedSet<String> getDistinctPostalCodesFromGeoResultsForServiceProviders(@Param("currentUserProfile")
        UserProfile currentUserProfile);

    @Query("SELECT DISTINCT gr.postalCode FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "JOIN l.organization org "
        + "JOIN org.userProfiles userProfile "
        + "WHERE gr.postalCode != '' "
        + "AND userProfile.silo = :silo "
        + "AND userProfile.systemAccount.name = 'ServiceProvider' "
        + "ORDER BY gr.postalCode")
    SortedSet<String> getDistinctPostalCodesFromGeoResultsForServiceProviders(@Param("silo")
        Silo silo);

    @Query("SELECT DISTINCT gr.administrativeAreaLevel2 FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "JOIN l.organization org "
        + "JOIN org.userProfiles userProfile "
        + "WHERE gr.administrativeAreaLevel2 != '' "
        + "AND userProfile.id != :#{#currentUserProfile.id} "
        + "AND userProfile.silo = :#{#currentUserProfile.silo} "
        + "AND userProfile.systemAccount.name = 'ServiceProvider' "
        + "ORDER BY gr.administrativeAreaLevel2")
    SortedSet<String> getDistinctRegionsFromGeoResultsForServiceProviders(@Param("currentUserProfile")
        UserProfile currentUserProfile);

    @Query("SELECT DISTINCT gr.administrativeAreaLevel2 FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "JOIN l.organization org "
        + "JOIN org.userProfiles userProfile "
        + "WHERE gr.administrativeAreaLevel2 != '' "
        + "AND userProfile.silo = :silo "
        + "AND userProfile.systemAccount.name = 'ServiceProvider' "
        + "ORDER BY gr.administrativeAreaLevel2")
    SortedSet<String> getDistinctRegionsFromGeoResultsForServiceProviders(@Param("silo")
        Silo silo);

    @Query("SELECT DISTINCT gr.locality FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "JOIN l.organization org "
        + "JOIN org.userProfiles userProfile "
        + "WHERE gr.locality != '' "
        + "AND userProfile.id != :#{#currentUserProfile.id} "
        + "AND userProfile.silo = :#{#currentUserProfile.silo} "
        + "AND userProfile.systemAccount.name = 'ServiceProvider' "
        + "ORDER BY gr.locality")
    SortedSet<String> getDistinctCityFromGeoResultsForServiceProviders(@Param("currentUserProfile")
        UserProfile currentUserProfile);

    @Query("SELECT DISTINCT gr.locality FROM Location l "
        + "JOIN l.geocodingResults gr "
        + "JOIN l.organization org "
        + "JOIN org.userProfiles userProfile "
        + "WHERE gr.locality != '' "
        + "AND userProfile.silo = :silo "
        + "AND userProfile.systemAccount.name = 'ServiceProvider' "
        + "ORDER BY gr.locality")
    SortedSet<String> getDistinctCityFromGeoResultsForServiceProviders(@Param("silo")
        Silo silo);

    List<GeocodingResult> findByFormattedAddressIsNullOrLocalityIsNullAndAddressIsNotNull();
}
