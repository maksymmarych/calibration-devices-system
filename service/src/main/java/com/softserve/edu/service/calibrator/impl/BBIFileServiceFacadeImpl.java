package com.softserve.edu.service.calibrator.impl;

import com.softserve.edu.common.Constants;
import com.softserve.edu.device.test.data.DeviceTestData;
import com.softserve.edu.entity.Address;
import com.softserve.edu.entity.device.CalibrationModule;
import com.softserve.edu.entity.device.Counter;
import com.softserve.edu.entity.device.CounterType;
import com.softserve.edu.entity.device.Device;
import com.softserve.edu.entity.enumeration.verification.Status;
import com.softserve.edu.entity.organization.Organization;
import com.softserve.edu.entity.user.User;
import com.softserve.edu.entity.verification.ClientData;
import com.softserve.edu.entity.verification.Verification;
import com.softserve.edu.repository.CalibrationModuleRepository;
import com.softserve.edu.service.admin.CounterTypeService;
import com.softserve.edu.service.admin.OrganizationService;
import com.softserve.edu.service.calibrator.BBIFileServiceFacade;
import com.softserve.edu.service.calibrator.BbiFileService;
import com.softserve.edu.service.calibrator.CalibratorService;
import com.softserve.edu.service.calibrator.data.test.CalibrationTestService;
import com.softserve.edu.service.catalogue.DistrictService;
import com.softserve.edu.service.catalogue.LocalityService;
import com.softserve.edu.service.catalogue.RegionService;
import com.softserve.edu.service.catalogue.StreetService;
import com.softserve.edu.service.exceptions.*;
import com.softserve.edu.service.tool.DeviceService;
import com.softserve.edu.service.utils.BBIOutcomeDTO;
import com.softserve.edu.service.verification.VerificationService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Service
public class BBIFileServiceFacadeImpl implements BBIFileServiceFacade {
    private static final String[] bbiExtensions = {"bbi", "BBI"};
    private static final String[] dbfExtensions = {"db", "dbf", "DB", "DBF"};
    private static InputStream inStream;
    private static BufferedInputStream bufferedInputStream;

    private final Logger logger = Logger.getLogger(BBIFileServiceFacadeImpl.class.getSimpleName());

    @Autowired
    private BbiFileService bbiFileService;

    @Autowired
    private CalibratorService calibratorService;

    @Autowired
    private CalibrationTestService calibrationTestService;

    @Autowired
    private CalibrationModuleRepository calibrationModuleRepository;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private CounterTypeService counterTypeService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private LocalityService localityService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private StreetService streetService;

    @Autowired
    private RegionService regionService;

    @Transactional
    public DeviceTestData parseBBIFile(File BBIFile, String originalFileName) throws IOException, DecoderException, InvalidImageInBbiException {
        inStream = FileUtils.openInputStream(BBIFile);
        bufferedInputStream = new BufferedInputStream(inStream);
        bufferedInputStream.mark(inStream.available());
        DeviceTestData deviceTestData = bbiFileService.parseBbiFile(bufferedInputStream, originalFileName);
        bufferedInputStream.reset();
        return deviceTestData;
    }

    public void saveBBIFile(DeviceTestData deviceTestData, String verificationID, String originalFileName) throws IOException {
        calibratorService.uploadBbi(bufferedInputStream, verificationID, originalFileName);
        calibrationTestService.createNewTest(deviceTestData, verificationID);
        bufferedInputStream.close();
        inStream.close();
    }

    @Override
    public DeviceTestData parseAndSaveBBIFile(File BBIfile, String verificationID, String originalFileName)
            throws IOException, DecoderException, InvalidImageInBbiException {
        DeviceTestData deviceTestData;
        try (InputStream inputStream = FileUtils.openInputStream(BBIfile)) {
            deviceTestData = parseAndSaveBBIFile(inputStream, verificationID, originalFileName);
            calibrationTestService.createNewTest(deviceTestData, verificationID);
        } catch (DecoderException e) {
            logger.error("error ", e);
        }
        return null;
    }

    public DeviceTestData parseAndSaveBBIFile(MultipartFile BBIfile, String verificationID, String originalFileName)
            throws IOException, NoSuchElementException, DecoderException {
        try {
            return parseAndSaveBBIFile(BBIfile.getInputStream(), verificationID, originalFileName);
        } catch (Exception e) {
            logger.error("Error " + e);
        }
        return null;
    }

    @Transactional
    public DeviceTestData parseAndSaveBBIFile(InputStream inputStream, String verificationID, String originalFileName)
            throws IOException, DecoderException, InvalidImageInBbiException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        bufferedInputStream.mark(inputStream.available());
        DeviceTestData deviceTestData = bbiFileService.parseBbiFile(bufferedInputStream, originalFileName);
        bufferedInputStream.reset();
        calibratorService.uploadBbi(bufferedInputStream, verificationID, originalFileName);
        return deviceTestData;
    }

    public List<BBIOutcomeDTO> parseAndSaveArchiveOfBBIfiles(File archive, String originalFileName,
                                                             User calibratorEmployee)
            throws IOException, ZipException, SQLException, ClassNotFoundException, ParseException, InvalidImageInBbiException {
        try (InputStream inputStream = FileUtils.openInputStream(archive)) {
            return parseAndSaveArchiveOfBBIfiles(inputStream, originalFileName, calibratorEmployee);
        }
    }


    public List<BBIOutcomeDTO> parseAndSaveArchiveOfBBIfiles(MultipartFile archiveFile, String originalFileName,
                                                             User calibratorEmployee) throws IOException, ZipException,
            SQLException, ClassNotFoundException, ParseException, InvalidImageInBbiException {
        return parseAndSaveArchiveOfBBIfiles(archiveFile.getInputStream(), originalFileName, calibratorEmployee);
    }

    @Transactional
    public List<BBIOutcomeDTO> parseAndSaveArchiveOfBBIfiles(InputStream archiveStream, String originalFileName,
                                                             User calibratorEmployee) throws IOException,
            ZipException, SQLException, ClassNotFoundException, ParseException, InvalidImageInBbiException {
        File directoryWithUnpackedFiles = unpackArchive(archiveStream, originalFileName);
        Map<String, Map<String, String>> bbiFileNamesToVerificationMap = getVerificationMapFromUnpackedFiles(
                directoryWithUnpackedFiles);
        List<File> listOfBBIfiles = new ArrayList<>(FileUtils.listFiles(directoryWithUnpackedFiles, bbiExtensions, true));
        List<BBIOutcomeDTO> resultsOfBBIProcessing = processListOfBBIFiles(bbiFileNamesToVerificationMap, listOfBBIfiles,
                calibratorEmployee);
        FileUtils.forceDelete(directoryWithUnpackedFiles);
        return resultsOfBBIProcessing;
    }

    /**
     * @param verificationMapFromUnpackedFiles Map of BBI files names to their corresponding verifications
     * @param listOfBBIfiles                   List with BBI files extracted from the archive
     * @return List of DTOs containing BBI filename, verification id, outcome of parsing (true/false), and reason of
     * rejection (if the bbi file was rejected)
     */
    private List<BBIOutcomeDTO> processListOfBBIFiles(Map<String, Map<String, String>> verificationMapFromUnpackedFiles,
                                                      List<File> listOfBBIfiles, User calibratorEmployee) throws
            ParseException, IOException, InvalidImageInBbiException {
        List<BBIOutcomeDTO> resultsOfBBIProcessing = new ArrayList<>();

        for (File bbiFile : listOfBBIfiles) {
            Map<String, String> correspondingVerificationMap = verificationMapFromUnpackedFiles.get(bbiFile.getName());
            String correspondingVerification = null;
            BBIOutcomeDTO.ReasonOfRejection reasonOfRejection = null;
            DeviceTestData deviceTestData;
            try {
                if (correspondingVerificationMap == null) {
                    throw new MismatchBbiFilesNamesException();
                }
                if (bbiFileService.findByFileNameAndDate(bbiFile.getName(), correspondingVerificationMap.get(Constants.DATE))) {
                    throw new FileAlreadyExistsException(bbiFile.getName());
                }
                correspondingVerification = correspondingVerificationMap.get(Constants.VERIFICATION_ID);
                if (correspondingVerification == null) {
                    deviceTestData = parseBBIFile(bbiFile, bbiFile.getName());
                    CalibrationModule calibrationModule = calibrationModuleRepository.findBySerialNumber(deviceTestData.getInstallmentNumber());
                    if (calibrationModule == null) {
                        throw new InvalidModuleIdException();
                    }
                    correspondingVerification = createNewVerificationFromMap(correspondingVerificationMap,
                            calibratorEmployee, deviceTestData);

                    saveBBIFile(deviceTestData, correspondingVerification, bbiFile.getName());
                    Verification verification = verificationService.findById(correspondingVerification);
                    verification.setCalibrationModule(calibrationModule);
                    verification.setStatus(Status.CREATED_BY_CALIBRATOR);
                    verificationService.saveVerification(verification);
                } else {
                    deviceTestData = parseBBIFile(bbiFile, bbiFile.getName());
                    updateVerificationFromMap(correspondingVerificationMap, correspondingVerification, deviceTestData);
                    saveBBIFile(deviceTestData, correspondingVerification, bbiFile.getName());
                }
            } catch (MismatchBbiFilesNamesException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.NAME_OF_BBI_FILE_DOES_NOT_MATCH;
                logger.error("Mismatch of bbi file names");
            } catch (FileAlreadyExistsException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.BBI_FILE_IS_ALREADY_IN_DATABASE;
                logger.error("BBI file is already in database");
            } catch (IOException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.BBI_IS_NOT_VALID;
                logger.error("BBI is not valid");
            } catch (InvalidImageInBbiException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.INVALID_IMAGE_IN_BBI;
                logger.error("Wrong image in BBI file");
            } catch (InvalidModuleIdException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.INVALID_MODULE_ID;
                logger.error("Wrong module serial number in BBI file");
            } catch (InvalidDeviceTypeIdException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.INVALID_DEVICE_TYPE_ID;
                logger.error("Wrong device type id in BBI file");
            } catch (InvalidVerificationCodeException e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.INVALID_VERIFICATION_CODE;
                logger.error("Invalid verification code");
            } catch (Exception e) {
                reasonOfRejection = BBIOutcomeDTO.ReasonOfRejection.UNKNOWN_REASON_OF_REJECTION;
                logger.error("Unknown reason of rejection");
            } finally {
                if (inStream != null) {
                    bufferedInputStream.close();
                    inStream.close();
                }
            }
            if (reasonOfRejection == null) {
                resultsOfBBIProcessing.add(BBIOutcomeDTO.accept(bbiFile.getName(), correspondingVerification));
            } else {
                resultsOfBBIProcessing.add(BBIOutcomeDTO.reject(bbiFile.getName(), correspondingVerification,
                        reasonOfRejection));
            }
        }
        return resultsOfBBIProcessing;
    }

    /**
     * Unpacks file into temporary directory
     *
     * @param inputStream      InputStream representing archive file
     * @param originalFileName Name of the archive
     * @return Directory to which the archive was unpacked
     * @throws IOException
     * @throws ZipException
     */

    private File unpackArchive(InputStream inputStream, String originalFileName) throws IOException, ZipException {
        String randomDirectoryName = RandomStringUtils.randomAlphanumeric(8);
        File directoryForUnpacking = FileUtils.getFile(FileUtils.getTempDirectoryPath(), randomDirectoryName);
        FileUtils.forceMkdir(directoryForUnpacking);
        File zipFileDownloaded = FileUtils.getFile(FileUtils.getTempDirectoryPath(), originalFileName);

        try (OutputStream os = new FileOutputStream(zipFileDownloaded)) {
            IOUtils.copy(inputStream, os);
        }

        ZipFile zipFile = new ZipFile(zipFileDownloaded);
        zipFile.extractAll(directoryForUnpacking.toString());
        FileUtils.forceDelete(zipFileDownloaded);
        return directoryForUnpacking;
    }

    /**
     * @param directoryWithUnpackedFiles Directory with unpacked files (should include BBIs and DBF)
     * @return Map of BBI files names to their corresponding verifications
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @implNote Uses sqlite to open DBF
     */
    private Map<String, Map<String, String>> getVerificationMapFromUnpackedFiles(File directoryWithUnpackedFiles)
            throws SQLException, ClassNotFoundException, FileNotFoundException {

        Map<String, Map<String, String>> bbiFilesToVerification = new LinkedHashMap<>();
        Map<String, String> verificationMap;
        Optional<File> foundDBFile = FileUtils.listFiles(directoryWithUnpackedFiles,
                dbfExtensions, true).stream().findFirst();
        File dbFile = foundDBFile.orElseThrow(() -> new FileNotFoundException("DBF not found"));
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Results");
            while (resultSet.next()) {
                verificationMap = new LinkedHashMap<>();
                verificationMap.put(Constants.VERIFICATION_ID, resultSet.getString("Id_pc"));
                verificationMap.put(Constants.LAST_NAME, resultSet.getString("Surname"));
                verificationMap.put(Constants.FIRST_NAME, resultSet.getString("Name"));
                verificationMap.put(Constants.MIDDLE_NAME, resultSet.getString("Middlename"));
                verificationMap.put(Constants.CITY, resultSet.getString("City"));
                verificationMap.put(Constants.REGION, resultSet.getString("District"));
                verificationMap.put(Constants.STREET, resultSet.getString("Street"));
                verificationMap.put(Constants.BUILDING, resultSet.getString("Building"));
                verificationMap.put(Constants.FLAT, resultSet.getString("Apartment"));
                verificationMap.put(Constants.STAMP, resultSet.getString("Account"));
                verificationMap.put(Constants.COUNTER_NUMBER, resultSet.getString("CounterNumber"));
                verificationMap.put(Constants.COUNTER_SIZE_AND_SYMBOL, resultSet.getString("Type"));
                verificationMap.put(Constants.YEAR, resultSet.getString("Year"));
                verificationMap.put(Constants.PHONE_NUMBER, resultSet.getString("TelNumber"));
                verificationMap.put(Constants.PROVIDER, resultSet.getString("Customer"));
                verificationMap.put(Constants.DATE, resultSet.getString("Date"));

                try {
                    verificationMap.put(Constants.CITY_ID, resultSet.getString("CityID"));
                    verificationMap.put(Constants.DISTRICT_ID, resultSet.getString("DistrictID"));
                    verificationMap.put(Constants.STREET_ID, resultSet.getString("StreetID"));
                    verificationMap.put(Constants.CUSTOMER_ID, resultSet.getString("CustomerID"));

                    verificationMap.put(Constants.COMMENT, resultSet.getString("Note"));
                } catch (SQLException e) {
                    logger.warn("User was trying to upload old archive format ");
                }

                bbiFilesToVerification.put(resultSet.getString("FileNumber"), verificationMap);
            }
        }
        return bbiFilesToVerification;
    }

    private String createNewVerificationFromMap(Map<String, String> verificationData, User calibratorEmployee, DeviceTestData deviceTestData)
            throws ParseException, InvalidDeviceTypeIdException {

        String cityName = null;
        String regionName = null;
        String streetName = null;
        ClientData clientData;

        try {
            Long cityIdLong = Long.parseLong(verificationData.get(Constants.CITY_ID));
            cityName = localityService.findById(cityIdLong).getDesignation();

            Long districtIdLong = Long.parseLong(verificationData.get(Constants.DISTRICT_ID));
            regionName = districtService.findDistrictById(districtIdLong).getDesignation();

            Long streetIdLong = Long.parseLong(verificationData.get(Constants.STREET_ID));
            streetName = streetService.findStreetById(streetIdLong).getDesignation();
        } catch (NumberFormatException e) {
            logger.info("Old *.db format", e);
        }

        if (cityName != null && regionName != null && streetName != null) {
            Address address = new Address(regionName, cityName,
                    streetName, verificationData.get(Constants.BUILDING), verificationData.get(Constants.FLAT));
            clientData = new ClientData(verificationData.get(Constants.FIRST_NAME),
                    verificationData.get(Constants.LAST_NAME), verificationData.get(Constants.MIDDLE_NAME),
                    verificationData.get(Constants.PHONE_NUMBER), address);
        } else {
            Address address = new Address(verificationData.get(Constants.REGION), verificationData.get(Constants.CITY),
                    verificationData.get(Constants.STREET), verificationData.get(Constants.BUILDING),
                    verificationData.get(Constants.FLAT));
            clientData = new ClientData(verificationData.get(Constants.FIRST_NAME),
                    verificationData.get(Constants.LAST_NAME), verificationData.get(Constants.MIDDLE_NAME),
                    verificationData.get(Constants.PHONE_NUMBER), address);
        }

        Long calibratorOrganisationId = calibratorEmployee.getOrganization().getId();
        Organization calibrator = organizationService.getOrganizationById(calibratorOrganisationId);
        Counter counter = getCounterFromVerificationData(verificationData, deviceTestData);
        Date date = new SimpleDateFormat(Constants.FULL_DATE).parse(verificationData.get(Constants.DATE));
        Organization providerFromBBI = organizationService.getOrganizationById(Long.parseLong(verificationData.get(Constants.CUSTOMER_ID)));

        Verification verification = new Verification(date, clientData, Status.CREATED_BY_CALIBRATOR, calibrator,
                providerFromBBI, calibratorEmployee, counter, null, verificationData.get(Constants.COMMENT),
                verificationData.get(Constants.DATE));

        List<String> listWithOneId = verificationService.saveVerificationCustom(verification, Constants.ONE_VERIFICATION,
                counter.getCounterType().getDevice().getDeviceType());

        return listWithOneId.get(Constants.FIRST_INDEX_IN_ARRAY);
    }

    private void updateVerificationFromMap(Map<String, String> verificationData, String verificationId, DeviceTestData deviceTestData)
            throws Exception {

        Verification verification = verificationService.findById(verificationId);
        if (verification == null) {
            throw new InvalidVerificationCodeException();
        }
        Long deviceId;
        deviceId = getDeviceIdByDeviceTypeId(deviceTestData.getDeviceTypeId());
        Device device = deviceService.getById(deviceId);
        verification.setDevice(device);
        verification.setVerificationTime(verificationData.get(Constants.DATE));
        Counter counter = getCounterFromVerificationData(verificationData, deviceTestData);
        verification.setCounter(counter);
        verificationService.saveVerification(verification);

    }

    private Long getDeviceIdByDeviceTypeId(int deviceTypeId) throws InvalidDeviceTypeIdException {
        String deviceType;
        switch (deviceTypeId) {
            case 2:
                deviceType = "THERMAL";
                break;
            default:
                deviceType = "WATER";
                break;
        }
        return deviceService.getByDeviceTypeAndDefaultDevice(deviceType, Constants.DEFAULT_DEVICE).getId();
    }

    private Counter getCounterFromVerificationData(Map<String, String> verificationData, DeviceTestData deviceTestData) throws InvalidDeviceTypeIdException {
        String sizeAndSymbol = verificationData.get(Constants.COUNTER_SIZE_AND_SYMBOL);
        String[] parts = sizeAndSymbol.split(" ");
        String standardSize = parts[0] + " " + parts[1];
        String symbol = parts[2];
        if (parts.length > Constants.MIN_LENGTH) {
            for (int i = Constants.MIN_LENGTH; i < parts.length; i++) {
                symbol += " " + parts[i];
            }
        }
        CounterType counterType = counterTypeService.findOneBySymbolAndStandardSize(symbol, standardSize);
        /**
         * If there is no such counterType, a new one will be created. After that super admin under his login
         * should complete all necessary data about this device, otherwise there will be no full data about this device
         * and NullPointerException will be generated
         */
        if (counterType == null) {
            Long deviceId = getDeviceIdByDeviceTypeId(deviceTestData.getDeviceTypeId());
            String deviceName = deviceService.getById(deviceId).getDeviceName();
            counterTypeService.addCounterType(deviceName, symbol, standardSize, null, null, null, null, deviceId);
            counterType = counterTypeService.findOneBySymbolAndStandardSize(symbol, standardSize);
            logger.info("Device with id=" + counterType.getId() + " was created");

        }
        return new Counter(verificationData.get(Constants.YEAR),
                verificationData.get(Constants.COUNTER_NUMBER), counterType, verificationData.get(Constants.STAMP));
    }
}