package com.softserve.edu.repository;

import com.softserve.edu.entity.device.Device;
import com.softserve.edu.entity.enumeration.verification.Status;
import com.softserve.edu.entity.organization.Organization;
import com.softserve.edu.entity.user.User;
import com.softserve.edu.entity.verification.Verification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface VerificationRepository extends PagingAndSortingRepository<Verification, String> {
    Page<Verification> findByProviderId(Long providerId, Pageable pageable);

    Page<Verification> findByCalibratorId(Long calibratorId, Pageable pageable);

    Page<Verification> findByTaskId(Long taskID, Pageable pageable);

    Verification[] findByTaskId(Long taskID);

    Verification[] findByTaskIdOrderByQueueAsc(Long id);

    Page<Verification> findByProviderIdAndStatusOrderByInitialDateDesc(Long providerId, Status status, Pageable pageable);

    Page<Verification> findByCalibratorIdAndStatusOrderByInitialDateDesc(Long calibratorId, Status status, Pageable pageable);

    Page<Verification> findByStateVerificatorIdAndStatusOrderByInitialDateDesc(Long stateVerificatorId, Status status, Pageable pageable);

    Page<Verification> findByStateVerificatorId(Long stateVerificatorId, Pageable pageable);

    Page<Verification> findByProviderIdAndStatus(Long providerId, Status status, Pageable pageable);

    Page<Verification> findByCalibratorIdAndStatus(Long calibratorId, Status status, Pageable pageable);

    Page<Verification> findByStateVerificatorIdAndStatus(Long stateVerificatorId, Status status, Pageable pageable);

    //search methods for calibrator
    Page<Verification> findByCalibratorIdAndStatusAndIdLikeIgnoreCase(Long calibratorId, Status status, String search, Pageable pageable);

    Page<Verification> findByCalibratorIdAndStatusAndInitialDateLike(Long calibratorId, Status status, Date date, Pageable pageable);

    Page<Verification> findByCalibratorIdAndStatusAndClientData_lastNameLikeIgnoreCase(Long calibratorId, Status status, String search, Pageable pageable);

    Page<Verification> findByCalibratorIdAndStatusAndClientDataClientAddressStreetLikeIgnoreCase(Long calibratorId, Status status, String search, Pageable pageable);

    // search methods for provider
    Page<Verification> findByProviderIdAndStatusAndIdLikeIgnoreCase(Long providerId, Status status, String search, Pageable pageable);

    Page<Verification> findByProviderIdAndStatusAndInitialDate(Long providerId, Status status, Date date, Pageable pageable);

    Page<Verification> findByProviderIdAndStatusAndClientData_lastNameLikeIgnoreCase(Long providerId, Status status, String search, Pageable pageable);

    Page<Verification> findByProviderIdAndStatusAndClientDataClientAddressStreetLikeIgnoreCase(Long providerId, Status status, String search, Pageable pageable);

    // search methods for verificator
    Page<Verification> findByStateVerificatorIdAndStatusAndIdLikeIgnoreCase(Long stateVerificatorId, Status status, String search, Pageable pageable);

    Page<Verification> findByStateVerificatorIdAndStatusAndInitialDateLike(Long stateVerificatorId, Status status, Date date, Pageable pageable);

    Page<Verification> findByStateVerificatorIdAndStatusAndClientData_lastNameLikeIgnoreCase(Long stateVerificatorId, Status status, String search, Pageable pageable);

    Page<Verification> findByStateVerificatorIdAndStatusAndClientDataClientAddressStreetLikeIgnoreCase(Long stateVerificatorId, Status status, String search, Pageable pageable);

    /**
     * This method serves for security purpose. When provider employee(or admin) makes GET request
     * for any verification he can only get it if id of organization and provider employee matches.
     * Otherwise(if returned null) AccessDeniedException will be thrown.
     *
     * @param id         Id of verification.
     * @param providerId Provider organization id.
     * @return Verification object that match provided query or null if no matches found.
     */
    Verification findByIdAndProviderId(String id, Long providerId);

    Verification findByIdAndCalibratorId(String id, Long providerId);

    Verification findByIdAndStateVerificatorId(String id, Long stateVerificatorId);

    Long countByProviderEmployeeUsernameAndStatus(String providerEmployeeUsername, Status status);

    Long countByCalibratorEmployeeUsernameAndStatus(String providerEmployeeUsername, Status status);

    Long countByStateVerificatorEmployeeUsernameAndStatus(String providerEmployee_username, Status status);

    Long countByProviderIdAndStatusAndReadStatus(Long providerId, Status status, Verification.ReadStatus readStatus);

    Long countByCalibratorIdAndStatusAndReadStatus(Long providerId, Status status, Verification.ReadStatus readStatus);

    Long countByStateVerificatorIdAndStatusAndReadStatus(Long stateVerificatorId, Status status, Verification.ReadStatus readStatus);

    Long countByCalibratorId(Long calibratorId);

    Long countByCalibratorIdAndStatus(Long calibratorId, Status status);

    Long countByProviderAndStatus(Organization provider, Status status);

    @Query("select u.providerEmployee from Verification u where u.id = :id")
    User getProviderEmployeeById(@Param("id") String id);

    List<Verification> findByProviderEmployeeUsernameAndStatus(String providerEmployee, Status status);

    List<Verification> findByCalibratorEmployeeUsernameAndStatus(String calibratorEmployee, Status status);

    List<Verification> findByStateVerificatorEmployeeUsernameAndStatus(String calibratorEmployee, Status status);

    List<Verification> findByProviderEmployeeIsNotNullAndProviderAndSentToCalibratorDateBetween(Organization organization, Date dateFrom, Date DateTo);

    List<Verification> findByCalibratorAndInitialDateBetween(Organization organization, Date dateFrom, Date DateTo);

    List<Verification> findByProvider(Organization organization);

    List<Verification> findByProviderAndInitialDateBetween(Organization organization, Date dateFrom, Date DateTo);

    List<Verification> findByStateVerificatorAndInitialDateBetween(Organization organization, Date dateFrom, Date DateTo);

    List<Verification> findByInitialDate(Date date);

    @Query("SELECT COUNT(u.id) FROM Verification u WHERE u.status = 'SENT' and u.provider = :provider")
    int getCountOfAllSentVerifications(@Param("provider") Organization provider);

    @Query("SELECT COUNT(u.id) FROM Verification u WHERE u.status = 'ACCEPTED' and u.provider = :provider")
    int getCountOfAllAcceptedVerifications(@Param("provider") Organization provider);

    @Query("SELECT COUNT(u.id) FROM Verification u WHERE u.status = 'IN_PROGRESS' and u.calibratorEmployee IS NULL and u.calibrator = :provider")
    int findCountOfAllCalibratorVerificationWithoutEmployee(@Param("provider") Organization provider);

    @Query("SELECT COUNT(u.id) FROM Verification u WHERE u.status IN ('IN_PROGRESS', 'PLANNING_TASK', 'TEST_PLACE_DETERMINED', 'SENT_TO_TEST_DEVICE', 'TEST_COMPLETED') and u.calibratorEmployee IS NOT NULL and u.calibrator = :provider")
    int findCountOfAllCalibratorVerificationWithEmployee(@Param("provider") Organization provider);

    @Query("SELECT COUNT(u.id) FROM Verification u WHERE u.status = 'SENT_TO_VERIFICATOR' and u.stateVerificatorEmployee IS NULL and u.stateVerificator = :provider")
    int findCountOfAllVerificatorVerificationWithoutEmployee(@Param("provider") Organization provider);

    @Query("SELECT COUNT(u.id) FROM Verification u WHERE (u.status = 'SENT_TO_VERIFICATOR' or u.status = 'TEST_OK' or u.status = 'TEST_NOK') and u.stateVerificatorEmployee IS NOT NULL and u.stateVerificator = :provider")
    int findCountOfAllVerificatorVerificationWithEmployee(@Param("provider") Organization provider);

    @Query("SELECT MIN(u.initialDate) FROM Verification u WHERE (u.status = 'ACCEPTED' or u.status = 'SENT') and u.provider = :provider")
    java.sql.Date getEarliestDateOfAllAcceptedOrSentVerificationsByProvider(@Param("provider") Organization provider);

    @Query("SELECT MIN(u.initialDate) FROM Verification u WHERE  u.status NOT IN ('ACCEPTED', 'SENT', 'SENT_TO_PROVIDER') and u.provider = :provider")
    java.sql.Date getEarliestDateOfArchivalVerificationsByProvider(@Param("provider") Organization provider);

    @Query("SELECT MIN(u.initialDate) FROM Verification u WHERE u.status IN ('IN_PROGRESS', 'TEST_PLACE_DETERMINED', 'SENT_TO_TEST_DEVICE', 'TEST_COMPLETED') and u.calibrator = :calibrator")
    java.sql.Date getEarliestDateOfAllNewVerificationsByCalibrator(@Param("calibrator") Organization calibrator);

    @Query("SELECT MIN(u.initialDate) FROM Verification u WHERE u.status = 'TEST_COMPLETED' and u.calibrator = :calibrator")
    java.sql.Date getEarliestDateOfDigitalVerificationProtocolsByCalibrator(@Param("calibrator") Organization calibrator);

    @Query("SELECT MIN(u.initialDate) FROM Verification u WHERE u.status IN ('SENT_TO_VERIFICATOR', 'TEST_NOK', 'TEST_OK') and u.stateVerificator = :verificator")
    java.sql.Date getEarliestDateOfDigitalVerificationProtocolsByVerificator(@Param("verificator") Organization verificator);

    @Query("SELECT MIN(u.sentToVerificatorDate) FROM Verification u WHERE u.status IN ('SENT_TO_VERIFICATOR', 'TEST_NOK', 'TEST_OK') and u.stateVerificator = :verificator")
    java.sql.Timestamp getEarliestDateOfSentToVerificator(@Param("verificator") Organization verificator);

    @Query("SELECT MIN(u.initialDate) FROM Verification u WHERE u.status NOT IN ('CREATED_BY_CALIBRATOR','SENT_TO_PROVIDER','ACCEPTED', 'SENT', 'IN_PROGRESS') and u.calibrator = :calibrator")
    java.sql.Date getEarliestDateOfArchivalVerificationsByCalibrator(@Param("calibrator") Organization calibrator);

    @Query("SELECT MIN(u.sentToCalibratorDate) FROM Verification u WHERE u.taskStatus IN ('PLANNING_TASK') " +
            "and u.calibrator = :calibrator")
    java.sql.Date getEarliestPlanningTaskDate(@Param("calibrator") Organization calibrator);

    List<Verification> findByCalibratorEmployeeUsernameAndTaskStatus(String userName, Status status);

    List<Verification> findByTaskStatusAndCalibratorId(Status status, Long id);

    List<Verification> findByIdIn( List<String> id);

    Page<Verification> findByTaskStatusAndCalibratorId(Status status, Long id, Pageable pageable);

    Page<Verification> findByCalibratorEmployeeUsernameAndTaskStatus(String userName, Status status, Pageable pageable);

    @Query("SELECT u FROM Verification u INNER JOIN u.device d WHERE d.id = u.device.id AND " +
            "d.deviceType= :deviceType AND u.initialDate = :initialDate")
    List<Verification> findVerificationByDateAndDeviceType(@Param("initialDate") Date initialDate,
                                                           @Param("deviceType") Device.DeviceType deviceType);

    @Query("SELECT COUNT(u.id) FROM Verification u INNER JOIN u.device d WHERE d.id = u.device.id AND " +
            "d.deviceType= :deviceType AND u.initialDate = :initialDate")
    long getCountOfAllVerificationsCreatedWithDeviceTypeToday(@Param("initialDate") Date initialDate,
                                                              @Param("deviceType") Device.DeviceType deviceType);

    @Modifying
    @Query("UPDATE Verification u SET u.queue = :queue WHERE u.id = :id ")
    void updateVerificationQueueById (@Param("queue") int queue , @Param("id") String id);
}


