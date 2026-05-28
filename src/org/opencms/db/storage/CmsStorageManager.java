/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsStoragePolicyConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.storage.policy.CmsNoExternalStoragePolicy;
import org.opencms.db.storage.policy.CmsStoragePolicyContext;
import org.opencms.db.storage.policy.I_CmsStoragePolicy;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.file.CmsDataAccessException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Manager class to coordinate binary content storage between database tables
 * and external storage providers.<p>
 *
 * It evaluates storage policies, handles deduplication via SHA-512 hashing, and
 * ensures referential integrity before deleting content from external storage.<p>
 */
public class CmsStorageManager implements AutoCloseable {

    /**
     * Container for storage evaluation results, used by VFS drivers to determine
     * which database columns to update.<p>
     *
     * Note: This result follows a "mutual exclusivity" principle:
     * <ul>
     * <li>If the content is stored locally, {@link #getFileContent()} contains the
     * data, while {@link #getHash()} and {@link #getStorage()} are
     * <code>null</code>.</li>
     * <li>If the content is offloaded, {@link #getFileContent()} is empty
     * (zero-length array), while {@link #getHash()} and {@link #getStorage()}
     * contain the reference details.</li>
     * </ul>
     */
    public static class StorageResult {

        /** Value for the FILE_CONTENT column. */
        private final byte[] m_fileContent;

        /** Value for the STORAGE column. */
        private final String m_storage;

        /** Value for the HASH column. */
        private final String m_hash;

        /**
         * Constructor for local storage.<p>
         *
         * @param fileContent the raw bytes to be stored in the content table
         */
        public StorageResult(byte[] fileContent) {

            m_fileContent = fileContent;
            m_storage = null;
            m_hash = null;
        }

        /**
         * Constructor for external storage.<p>
         *
         * @param storage the stable storage identifier stored in the STORAGE column
         * @param hash the SHA-512 content hash stored in the HASH column
         */
        public StorageResult(String storage, String hash) {

            m_fileContent = new byte[0];
            m_storage = storage;
            m_hash = hash;
        }

        /**
         * Returns the content for the local FILE_CONTENT column.
         * <p>
         *
         * @return the bytes
         */
        public byte[] getFileContent() {

            return m_fileContent;
        }

        /**
         * Returns the hash for the HASH column.<p>
         *
         * @return the hash string
         */
        public String getHash() {

            return m_hash;
        }

        /**
         * Returns the stable storage identifier for the STORAGE column.<p>
         *
         * @return the storage identifier
         */
        public String getStorage() {

            return m_storage;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStorageManager.class);

    /** Prefix for storage properties. */
    private static final String PARAM_STORAGE_ACTIVE = "storage.active";

    /** Prefix for the legacy storage list. */
    private static final String PARAM_STORAGE_LEGACY = "storage.legacy";

    /** Prefix for backend-specific configuration. */
    private static final String PARAM_STORAGE_BACKEND_PREFIX = "storage.backend.";

    /** Property name for backend type. */
    private static final String PARAM_TYPE = "type";

    /** Property name for backend implementation class. */
    private static final String PARAM_CLASS = "class";

    /** Property name for the S3 bucket. */
    private static final String PARAM_BUCKET = "bucket";

    /** Property name for the S3 endpoint. */
    private static final String PARAM_ENDPOINT = "endpoint";

    /** Property name for the S3 access key. */
    private static final String PARAM_ACCESS_KEY = "accessKey";

    /** Property name for the S3 secret key. */
    private static final String PARAM_SECRET_KEY = "secretKey";

    /** Property name for path-style access. */
    private static final String PARAM_PATH_STYLE = "pathStyle";

    /** Property name for the S3 region. */
    private static final String PARAM_REGION = "region";

    /** Property name for the S3 complete API call timeout. */
    private static final String PARAM_API_CALL_TIMEOUT = "apiCallTimeout";

    /** Property name for the S3 single API call attempt timeout. */
    private static final String PARAM_API_CALL_ATTEMPT_TIMEOUT = "apiCallAttemptTimeout";

    /** Property name for the S3 connection timeout. */
    private static final String PARAM_CONNECTION_TIMEOUT = "connectionTimeout";

    /** Property name for the S3 maximum number of retries. */
    private static final String PARAM_MAX_RETRIES = "maxRetries";

    /** Property name for the S3 socket timeout. */
    private static final String PARAM_SOCKET_TIMEOUT = "socketTimeout";

    /** Property name for the FS repository path. */
    private static final String PARAM_PATH = "path";

    /** The active write storage implementation. */
    private I_CmsStorage m_activeStorage;

    /** The active write storage identifier. */
    private String m_activeStorageId;

    /** The configured storage policy. */
    private I_CmsStoragePolicy m_storagePolicy;

    /** The configured storages keyed by their stable identifier. */
    private Map<String, I_CmsStorage> m_storages;

    /** The configured backend id for each storage identifier. */
    private Map<String, String> m_storageBackendIds;

    /** The SQL manager. */
    private final CmsSqlManager m_sqlManager;

    /**
     * Creates a new storage manager.<p>
     *
     * @param sqlManager the SQL manager
     * @param configuration the runtime property configuration
     */
    public CmsStorageManager(CmsSqlManager sqlManager, CmsParameterConfiguration configuration) {

        this(sqlManager, configuration, null);
    }

    /**
     * Creates a new storage manager.<p>
     *
     * @param sqlManager the SQL manager
     * @param configuration the runtime property configuration
     * @param storagePolicyConfiguration the storage policy configuration
     */
    public CmsStorageManager(
        CmsSqlManager sqlManager,
        CmsParameterConfiguration configuration,
        CmsStoragePolicyConfiguration storagePolicyConfiguration) {

        m_sqlManager = sqlManager;
        m_storagePolicy = createStoragePolicy(storagePolicyConfiguration);
        initStorages(configuration);
    }

    /**
     * Closes all configured storage backends.<p>
     *
     * @throws Exception if closing one or more storage backends fails
     */
    public void close() throws Exception {

        Exception failure = null;
        if (m_storages == null) {
            return;
        }
        for (I_CmsStorage storage : m_storages.values()) {
            try {
                storage.close();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_STORAGE_CLOSE_FAILED_1,
                            storage.getStorageIdentifier()),
                        e);
                }
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Deletes content from external storage if it is no longer referenced in any
     * content table.<p>
     *
     * @param dbc the database context
     * @param storage the stable storage identifier stored in the STORAGE column
     * @param hash the hash of the content to delete
     */
    public void deleteContent(CmsDbContext dbc, String storage, String hash) {

        if (CmsStringUtil.isEmpty(hash) || CmsStringUtil.isEmpty(storage)) {
            return;
        }
        try {
            if (!isContentReferenced(dbc, storage, hash)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Blob with hash " + hash + " is not referenced in " + storage + " any more. Delete it.");
                }
                try {
                    I_CmsStorage targetStorage = m_storages.get(storage);
                    if (targetStorage != null) {
                        targetStorage.deleteContent(dbc, hash);
                    } else {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_STORAGE_DELETE_BACKEND_MISSING_1, storage));
                    }
                } catch (Exception e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_STORAGE_DELETE_FAILED_2, hash, storage), e);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Blob with hash " + hash + " is still referenced. Skipping deletion.");
                }
            }
        } catch (CmsDbSqlException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_STORAGE_REFERENCES_FAILED_1, hash), e);
        }
    }

    /**
     * Returns a configured storage backend by its stable storage identifier.<p>
     *
     * @param storage the stable storage identifier
     * @return the configured storage backend, or null if no such backend is configured
     */
    public I_CmsStorage getStorage(String storage) {

        return m_storages.get(storage);
    }

    /**
     * Loads the content either from the provided local bytes or from external
     * storage.<p>
     *
     * @param dbc the database context
     * @param contents the local bytes (from FILE_CONTENT column)
     * @param storage the stable storage identifier stored in the STORAGE column
     * @param hash the SHA-512 content hash stored in the HASH column
     * @return the raw bytes of the file
     * @throws CmsStorageException if external storage content can not be loaded
     */
    public byte[] loadContent(CmsDbContext dbc, byte[] contents, String storage, String hash)
    throws CmsStorageException {

        if (CmsStringUtil.isNotEmpty(storage) && CmsStringUtil.isNotEmpty(hash)) {
            try {
                I_CmsStorage configuredStorage = m_storages.get(storage);
                if (configuredStorage == null) {
                    String message = Messages.get().getBundle().key(Messages.ERR_STORAGE_UNCONFIGURED_1, storage);
                    LOG.error(message);
                    throw new CmsStorageException(message);
                }
                byte[] result = configuredStorage.loadContent(dbc, hash);
                if (result == null) {
                    throw new CmsStorageBlobNotFoundException(
                        Messages.get().getBundle().key(Messages.ERR_STORAGE_BLOB_MISSING_2, storage, hash));
                }
                return result;
            } catch (CmsStorageException e) {
                throw e;
            } catch (Exception e) {
                throw new CmsStorageException(
                    Messages.get().getBundle().key(Messages.ERR_STORAGE_BLOB_LOAD_FAILED_2, storage, hash),
                    e);
            }
        } else {
            return contents;
        }
    }

    /**
     * Prepares content for storage by checking the policy and offloading to
     * external storage if required.<p>
     *
     * @param dbc the database context
     * @param context the storage policy context
     * @return the storage result containing either the bytes or the hash
     * @throws CmsDataAccessException if storing required external content fails
     */
    public StorageResult prepareContent(CmsDbContext dbc, CmsStoragePolicyContext context)
    throws CmsDataAccessException {

        if (m_storagePolicy.isExternalStorageRequired(context)) {
            byte[] rawData = context.getContent();
            String hash = null;
            try {
                hash = calculateSha512(rawData);
                m_activeStorage.storeContent(dbc, hash, rawData);
                return new StorageResult(m_activeStorage.getStorageIdentifier(), hash);
            } catch (Exception e) {
                throw new CmsDataAccessException(
                    Messages.get().container(
                        Messages.ERR_STORAGE_EXTERNAL_WRITE_2,
                        hash,
                        m_activeStorage.getStorageIdentifier()),
                    e);
            }
        }
        return new StorageResult(context.getContent());
    }

    /**
     * Validates all configured storage backends.<p>
     *
     * @param dbc the database context
     * @throws Exception if a configured storage backend is not available
     */
    public void validateStorages(CmsDbContext dbc) throws Exception {

        for (I_CmsStorage storage : m_storages.values()) {
            String identifier = storage.getStorageIdentifier();
            String storageId = m_storageBackendIds.get(identifier);
            String role = getStorageRole(storageId);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_STORAGE_BACKEND_CHECKING_3,
                        storageId,
                        identifier,
                        role));
            }
            try {
                storage.validateAvailable(dbc);
            } catch (Exception e) {
                String reason = getFailureReason(e);
                if (CmsLog.INIT.isErrorEnabled()) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(
                            Messages.INIT_STORAGE_BACKEND_FAILED_4,
                            new Object[] {storageId, identifier, role, reason}),
                        e);
                }
                throw e;
            }
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.INIT_STORAGE_BACKEND_OK_3, storageId, identifier, role));
            }
        }
    }

    /**
     * Calculates the SHA-512 hash of the given content and returns it as a hex
     * string.<p>
     *
     * @param content the content to hash
     * @return the SHA-512 hex string
     * @throws NoSuchAlgorithmException if the hashing algorithm is not available
     */
    private String calculateSha512(byte[] content) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hash = md.digest(content);
        StringBuilder sb = new StringBuilder(128);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0'); // pad with leading zero
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Creates the database storage backend.<p>
     *
     * @param configuredClassName the configured implementation class, or null
     * @return the database storage backend
     */
    private I_CmsDbStorage createDbStorage(String configuredClassName) {

        String className = configuredClassName;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(className)) {
            className = getDefaultDbStorageClassName();
        }
        try {
            Class<?> storageClass = Class.forName(className);
            if (!I_CmsDbStorage.class.isAssignableFrom(storageClass)) {
                throw new IllegalArgumentException(
                    Messages.get().getBundle().key(
                        Messages.ERR_STORAGE_DB_CLASS_INVALID_2,
                        className,
                        I_CmsDbStorage.class.getName()));
            }
            Constructor<?> constructor = storageClass.getConstructor(CmsSqlManager.class);
            return (I_CmsDbStorage)constructor.newInstance(m_sqlManager);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_DB_CLASS_CREATE_1, className),
                e);
        }
    }

    /**
     * Creates a storage backend from the runtime property configuration.
     *
     * @param id the configured backend id
     * @param configuration the runtime property configuration
     * @return the storage backend
     */
    private I_CmsStorage createStorage(String id, CmsParameterConfiguration configuration) {

        String prefix = PARAM_STORAGE_BACKEND_PREFIX + id + ".";
        if (I_CmsDbStorage.STORAGE_TYPE.equals(id)) {
            return createDbStorage(configuration.getString(prefix + PARAM_CLASS, null));
        }
        String type = configuration.getString(prefix + PARAM_TYPE, null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(type)) {
            throw new IllegalArgumentException(Messages.get().getBundle().key(Messages.ERR_STORAGE_MISSING_TYPE_1, id));
        }
        if (I_CmsDbStorage.STORAGE_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_DB_RESERVED_ID_1, id));
        }
        if (CmsS3Storage.STORAGE_TYPE.equals(type)) {
            String endpoint = requireConfig(configuration, prefix + PARAM_ENDPOINT, id);
            String bucket = requireConfig(configuration, prefix + PARAM_BUCKET, id);
            String accessKey = requireConfig(configuration, prefix + PARAM_ACCESS_KEY, id);
            String secretKey = requireConfig(configuration, prefix + PARAM_SECRET_KEY, id);
            boolean pathStyle = configuration.getBoolean(prefix + PARAM_PATH_STYLE, true);
            int connectionTimeout = configuration.getInteger(
                prefix + PARAM_CONNECTION_TIMEOUT,
                CmsS3ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT);
            int socketTimeout = configuration.getInteger(
                prefix + PARAM_SOCKET_TIMEOUT,
                CmsS3ClientConfiguration.DEFAULT_SOCKET_TIMEOUT);
            int apiCallAttemptTimeout = configuration.getInteger(
                prefix + PARAM_API_CALL_ATTEMPT_TIMEOUT,
                CmsS3ClientConfiguration.DEFAULT_API_CALL_ATTEMPT_TIMEOUT);
            int apiCallTimeout = configuration.getInteger(
                prefix + PARAM_API_CALL_TIMEOUT,
                CmsS3ClientConfiguration.DEFAULT_API_CALL_TIMEOUT);
            int maxRetries = configuration.getInteger(
                prefix + PARAM_MAX_RETRIES,
                CmsS3ClientConfiguration.DEFAULT_MAX_RETRIES);
            String region = configuration.getString(prefix + PARAM_REGION, CmsS3ClientConfiguration.DEFAULT_REGION);
            return new CmsS3Storage(
                id,
                new CmsS3ClientConfiguration(
                    endpoint,
                    bucket,
                    accessKey,
                    secretKey,
                    pathStyle,
                    region,
                    connectionTimeout,
                    socketTimeout,
                    apiCallAttemptTimeout,
                    apiCallTimeout,
                    maxRetries));
        }
        if (CmsFsStorage.STORAGE_TYPE.equals(type)) {
            String path = requireConfig(configuration, prefix + PARAM_PATH, id);
            return new CmsFsStorage(id, path);
        }
        throw new IllegalArgumentException(
            Messages.get().getBundle().key(Messages.ERR_STORAGE_UNSUPPORTED_TYPE_2, type, id));
    }

    /**
     * Creates the storage policy from the VFS configuration.
     *
     * @param configuration the storage policy configuration
     * @return the storage policy
     */
    private I_CmsStoragePolicy createStoragePolicy(CmsStoragePolicyConfiguration configuration) {

        String className = CmsNoExternalStoragePolicy.class.getName();
        CmsParameterConfiguration parameters = null;
        if ((configuration != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getClassName())) {
            className = configuration.getClassName();
            parameters = configuration.getConfiguration();
        }
        try {
            Object instance = Class.forName(className).newInstance();
            if (!(instance instanceof I_CmsStoragePolicy)) {
                throw new IllegalArgumentException(
                    Messages.get().getBundle().key(
                        Messages.ERR_STORAGE_POLICY_INVALID_2,
                        className,
                        I_CmsStoragePolicy.class.getName()));
            }
            I_CmsStoragePolicy policy = (I_CmsStoragePolicy)instance;
            if (policy instanceof I_CmsConfigurationParameterHandler) {
                I_CmsConfigurationParameterHandler parameterHandler = (I_CmsConfigurationParameterHandler)policy;
                if (parameters != null) {
                    for (String key : parameters.keySet()) {
                        parameterHandler.addConfigurationParameter(key, parameters.get(key));
                    }
                }
                parameterHandler.initConfiguration();
            }
            return policy;
        } catch (CmsConfigurationException e) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_POLICY_INIT_1, className),
                e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_POLICY_CREATE_1, className),
                e);
        }
    }

    /**
     * Returns the default DB storage class name for the active database package.<p>
     *
     * @return the default DB storage class name
     */
    private String getDefaultDbStorageClassName() {

        Package sqlManagerPackage = m_sqlManager.getClass().getPackage();
        String packageName = sqlManagerPackage != null ? sqlManagerPackage.getName() : null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(packageName)) {
            String className = packageName + ".CmsDbStorage";
            try {
                Class.forName(className);
                return className;
            } catch (ClassNotFoundException e) {
                // use the generic implementation below
            }
        }
        return org.opencms.db.generic.CmsDbStorage.class.getName();
    }

    /**
     * Returns a short reason for a backend validation failure.<p>
     *
     * @param e the failure
     * @return the failure reason
     */
    private String getFailureReason(Exception e) {

        String message = e.getMessage();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            message = e.getClass().getName();
        }
        return message;
    }

    /**
     * Returns the configured role for a storage id.<p>
     *
     * @param storageId the storage id
     * @return the configured role
     */
    private String getStorageRole(String storageId) {

        return getStorageRole(storageId, m_activeStorageId);
    }

    /**
     * Returns the configured role for a storage id.<p>
     *
     * @param storageId the storage id
     * @param activeStorageId the active storage id
     * @return the configured role
     */
    private String getStorageRole(String storageId, String activeStorageId) {

        return activeStorageId.equals(storageId) ? "active" : "legacy";
    }

    /**
     * Initializes the configured storage backends.
     *
     * @param configuration the runtime property configuration
     */
    private void initStorages(CmsParameterConfiguration configuration) {

        m_storages = new LinkedHashMap<String, I_CmsStorage>();
        m_storageBackendIds = new LinkedHashMap<String, String>();

        String activeStorageId = configuration.getString(PARAM_STORAGE_ACTIVE, I_CmsDbStorage.STORAGE_TYPE).trim();
        List<String> referencedStorages = new ArrayList<String>();
        referencedStorages.add(activeStorageId);
        for (String legacyStorage : parseCommaSeparatedList(configuration.getString(PARAM_STORAGE_LEGACY, ""))) {
            if (legacyStorage.equals(activeStorageId)) {
                throw new IllegalArgumentException(
                    Messages.get().getBundle().key(Messages.ERR_STORAGE_LEGACY_CONTAINS_ACTIVE_1, activeStorageId));
            }
            if (!referencedStorages.contains(legacyStorage)) {
                referencedStorages.add(legacyStorage);
            }
        }
        I_CmsStorage activeStorageCandidate = null;
        for (String storageId : referencedStorages) {
            I_CmsStorage storage = createStorage(storageId, configuration);
            String identifier = storage.getStorageIdentifier();
            if (m_storages.containsKey(identifier)) {
                // if it's the DB storage, it's already in the map
                storage = m_storages.get(identifier);
            } else {
                m_storages.put(identifier, storage);
                m_storageBackendIds.put(identifier, storageId);
            }
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_STORAGE_BACKEND_CONFIGURED_3,
                        storageId,
                        identifier,
                        getStorageRole(storageId, activeStorageId)));
            }
            if (storageId.equals(activeStorageId)) {
                activeStorageCandidate = storage;
            }
        }
        m_activeStorageId = activeStorageId;
        m_activeStorage = activeStorageCandidate;
        if (m_activeStorage == null) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_ACTIVE_MISSING_1, activeStorageId));
        }
    }

    /**
     * Checks if a specific hash is still used in any online, history or offline
     * content table for the given storage identifier.<p>
     *
     * @param dbc the database context
     * @param storage the storage identifier
     * @param hash the hash to check
     * @return true if the hash is still in use for that storage
     * @throws CmsDbSqlException if database access fails
     */
    private boolean isContentReferenced(CmsDbContext dbc, String storage, String hash) throws CmsDbSqlException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STORAGE_CONTENT_INUSE");
            stmt.setString(1, hash);
            stmt.setString(2, storage);
            stmt.setString(3, hash);
            stmt.setString(4, storage);
            res = stmt.executeQuery();
            return res.next();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                org.opencms.db.generic.Messages.get().container(org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * Parses a comma-separated backend list.
     *
     * @param value the raw configuration value
     * @return the parsed backend identifiers
     */
    private List<String> parseCommaSeparatedList(String value) {

        List<String> result = new ArrayList<String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            return result;
        }
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && !result.contains(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Reads a required configuration value.
     *
     * @param configuration the runtime property configuration
     * @param key the property key
     * @param storageId the backend identifier
     *
     * @return the configured value
     */
    private String requireConfig(CmsParameterConfiguration configuration, String key, String storageId) {

        String value = configuration.getString(key, null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_STORAGE_MISSING_CONFIG_2, key, storageId));
        }
        return value.trim();
    }
}
