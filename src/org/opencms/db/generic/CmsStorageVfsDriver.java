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

package org.opencms.db.generic;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.storage.CmsStorageException;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.CmsStorageManager.StorageResult;
import org.opencms.db.storage.policy.CmsStoragePolicyContext;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsInitException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CmsStorageVfsDriver extends CmsVfsDriver {

    /** The filename/path of the storage SQL query properties. */
    private static final String STORAGE_QUERY_PROPERTIES = "org/opencms/db/generic/storage.properties";

    protected CmsStorageManager m_storageManager;

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(CmsDbContext, CmsUUID, CmsResource, byte[])
     */
    public void createContent(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, byte[] content)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            StorageResult storageResult = m_storageManager.prepareContent(
                dbc,
                new CmsStoragePolicyContext(content, resource));
            byte[] fileContent = storageResult.getFileContent();
            String storage = storageResult.getStorage();
            String hash = storageResult.getHash();
            conn = m_sqlManager.getConnection(dbc);
            // create new offline content
            stmt = m_sqlManager.getPreparedStatement(conn, "C_OFFLINE_CONTENTS_WRITE");
            stmt.setString(1, resource.getResourceId().toString());
            if (fileContent.length < 2000) {
                stmt.setBytes(2, fileContent);
            } else {
                stmt.setBinaryStream(2, new ByteArrayInputStream(fileContent), fileContent.length);
            }
            stmt.setString(3, storage);
            stmt.setString(4, hash);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(CmsDbContext, java.sql.ResultSet, CmsUUID)
     */
    public CmsFile createFile(CmsDbContext dbc, ResultSet res, CmsUUID projectId)
    throws CmsDataAccessException, SQLException {

        // use the super class to read all standard properties
        CmsFile file = super.createFile(dbc, res, projectId);
        // read the storage information
        String storage = res.getString("STORAGE");
        // file content is stored locally -> return the file
        if (CmsStringUtil.isEmpty(storage)) {
            return file;
        }
        // load the file content from the storage
        String hash = res.getString("HASH");
        byte[] finalContent;
        try {
            finalContent = m_storageManager.loadContent(dbc, file.getContents(), storage, hash);
        } catch (CmsStorageException e) {
            throw createStorageReadException(storage, hash, e);
        }
        // copy the file but use the file content from the storage
        if (finalContent != file.getContents()) {
            return new CmsFile(
                file.getStructureId(),
                file.getResourceId(),
                file.getRootPath(),
                file.getTypeId(),
                file.getFlags(),
                projectId,
                file.getState(),
                file.getDateCreated(),
                file.getUserCreated(),
                file.getDateLastModified(),
                file.getUserLastModified(),
                file.getDateReleased(),
                file.getDateExpired(),
                file.getSiblingCount(),
                file.getLength(),
                file.getDateContent(),
                file.getVersion(),
                finalContent);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(CmsDbContext, java.sql.ResultSet, CmsUUID, boolean)
     */
    public CmsFile createFile(CmsDbContext dbc, ResultSet res, CmsUUID projectId, boolean hasFileContentInResultSet)
    throws CmsDataAccessException, SQLException {

        CmsFile file = super.createFile(dbc, res, projectId, hasFileContentInResultSet);
        if (!hasFileContentInResultSet) { // content is not relevant
            return file;
        }
        String storage = res.getString("STORAGE");
        if (CmsStringUtil.isEmpty(storage)) { // content is stored locally, already read by standard VFS driver
            return file;
        }
        // load the content from the storage
        String hash = res.getString("HASH");
        byte[] finalContent;
        try {
            finalContent = m_storageManager.loadContent(dbc, file.getContents(), storage, hash);
        } catch (CmsStorageException e) {
            throw createStorageReadException(storage, hash, e);
        }
        if (finalContent != file.getContents()) {
            return new CmsFile(
                file.getStructureId(),
                file.getResourceId(),
                file.getRootPath(),
                file.getTypeId(),
                file.getFlags(),
                file.getProjectLastModified(),
                file.getState(),
                file.getDateCreated(),
                file.getUserCreated(),
                file.getDateLastModified(),
                file.getUserLastModified(),
                file.getDateReleased(),
                file.getDateExpired(),
                file.getSiblingCount(),
                file.getLength(),
                file.getDateContent(),
                file.getVersion(),
                finalContent);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createOnlineContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[], int, boolean, boolean)
     */
    public void createOnlineContent(
        CmsDbContext dbc,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag,
        boolean keepOnline,
        boolean needToUpdateContent)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        String oldOnlineStorage = null;
        String oldOnlineHash = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            boolean dbcHasProjectId = (dbc.getProjectId() != null) && !dbc.getProjectId().isNullUUID();
            if (needToUpdateContent || dbcHasProjectId) {
                // read the hash of the actual online content
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_FILES_CONTENT");
                stmt.setString(1, resourceId.toString());
                res = stmt.executeQuery();
                if (res.next()) {
                    oldOnlineStorage = res.getString("STORAGE");
                    oldOnlineHash = res.getString("HASH");
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
                // read the actual offline content to take over the actual storage policy if needed
                String storage = null;
                String hash = null;
                stmt = m_sqlManager.getPreparedStatement(conn, "C_OFFLINE_FILES_CONTENT");
                stmt.setString(1, resourceId.toString());
                res = stmt.executeQuery();
                if (res.next()) {
                    storage = res.getString("STORAGE");
                    hash = res.getString("HASH");
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
                // move the content to the history or delete it as does the standard VFS driver
                if (dbcHasProjectId || !OpenCms.getSystemInfo().isHistoryEnabled()) {
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_DELETE");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                } else {
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
                // write the new online content taking over the offline storage policy
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_WRITE");
                stmt.setString(1, resourceId.toString());
                // handle the local binary blob (the external blob was already created in the offline table)
                if ((contents != null) && (contents.length > 0)) {
                    if (CmsStringUtil.isEmpty(hash)) {
                        if (contents.length < 2000) {
                            stmt.setBytes(2, contents);
                        } else {
                            stmt.setBinaryStream(2, new ByteArrayInputStream(contents), contents.length);
                        }
                    } else {
                        stmt.setBytes(2, new byte[0]);
                    }
                } else {
                    stmt.setBytes(2, new byte[0]);
                }
                stmt.setString(3, storage);
                stmt.setString(4, hash);
                stmt.setInt(5, publishTag);
                stmt.setInt(6, publishTag);
                stmt.setInt(7, keepOnline ? 1 : 0);
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
                // delete the old storage entry if necessary
                if (CmsStringUtil.isNotEmpty(oldOnlineHash)) {
                    m_storageManager.deleteContent(dbc, oldOnlineStorage, oldOnlineHash);
                }
            } else {
                // standard update in case the content blob is not relevant
                super.createOnlineContent(dbc, resourceId, contents, publishTag, keepOnline, needToUpdateContent);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteHistoryContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, int)
     */
    @Override
    public void deleteHistoryContent(CmsDbContext dbc, CmsUUID resourceId, int publishTagToKeep)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<String[]> storageReferences = new ArrayList<String[]>();
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_CONTENT_HISTORY_STORAGE_READ");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, publishTagToKeep);
            res = stmt.executeQuery();
            while (res.next()) {
                String storage = res.getString("STORAGE");
                String hash = res.getString("HASH");
                if (CmsStringUtil.isNotEmpty(storage) && CmsStringUtil.isNotEmpty(hash)) {
                    storageReferences.add(new String[] {storage, hash});
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        super.deleteHistoryContent(dbc, resourceId, publishTagToKeep);

        for (String[] storageReference : storageReferences) {
            m_storageManager.deleteContent(dbc, storageReference[0], storageReference[1]);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#destroy()
     */
    public void destroy() throws Throwable {

        Throwable failure = null;
        try {
            if (m_storageManager != null) {
                m_storageManager.close();
            }
        } catch (Throwable e) {
            failure = e;
        } finally {
            m_storageManager = null;
            try {
                super.destroy();
            } catch (Throwable e) {
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
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List<String> successiveDrivers,
        CmsDriverManager driverManager) {

        super.init(dbc, configurationManager, successiveDrivers, driverManager);
        try {
            CmsParameterConfiguration configuration = configurationManager.getConfiguration();
            CmsVfsConfiguration vfsConfiguration = (CmsVfsConfiguration)configurationManager.getConfiguration(
                CmsVfsConfiguration.class);
            m_storageManager = new CmsStorageManager(
                m_sqlManager,
                configuration,
                vfsConfiguration.getStoragePolicyConfiguration());
            m_storageManager.validateStorages(dbc);
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_INITIALIZING_VFS_DRIVER_0), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initSqlManager(String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.generic.CmsVfsDriver#initSqlManager(String, List)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(
        String classname,
        List<String> additionalQueryProperties) {

        return CmsSqlManager.getInstance(classname, additionalQueryProperties);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readContent(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public byte[] readContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        byte[] byteRes = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_ONLINE_FILES_CONTENT");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_OFFLINE_FILES_CONTENT");
            }
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                byte[] localContent = null;
                String storage = res.getString("STORAGE");
                String hash = res.getString("HASH");
                localContent = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
                try {
                    byteRes = m_storageManager.loadContent(dbc, localContent, storage, hash);
                } catch (CmsStorageException e) {
                    throw createStorageReadException(storage, hash, e);
                }
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(
                    Messages.get().container(
                        Messages.ERR_READ_CONTENT_WITH_RESOURCE_ID_2,
                        resourceId,
                        Boolean.valueOf(projectId.equals(CmsProject.ONLINE_PROJECT_ID))));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return byteRes;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readHistoryContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, int)
     */
    public byte[] readHistoryContent(CmsDbContext dbc, CmsUUID resourceId, int publishTag)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        byte[] content = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_READ_CONTENT");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, publishTag);
            stmt.setInt(3, publishTag);
            res = stmt.executeQuery();

            if (res.next()) {
                byte[] localContent = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
                String storage = res.getString("STORAGE");
                String hash = res.getString("HASH");
                try {
                    content = m_storageManager.loadContent(dbc, localContent, storage, hash);
                } catch (CmsStorageException e) {
                    throw createStorageReadException(storage, hash, e);
                }
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return content;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource)
     */
    public void removeFile(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        CmsUUID resourceId = resource.getResourceId();
        try {
            conn = m_sqlManager.getConnection(dbc);
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_ONLINE_FILES_CONTENT");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_OFFLINE_FILES_CONTENT");
            }
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                String storage = res.getString("STORAGE");
                String hash = res.getString("HASH");
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
                super.removeFile(dbc, projectId, resource);
                // remove the file if in storage
                if (CmsStringUtil.isNotEmpty(storage)) {
                    m_storageManager.deleteContent(dbc, storage, hash);
                }

            }
            super.removeFile(dbc, projectId, resource);
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, byte[])
     */
    public void writeContent(CmsDbContext dbc, CmsResource resource, byte[] content) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        String oldStorage = null;
        String oldHash = null;
        try {
            // read the actual offline content
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_OFFLINE_FILES_CONTENT");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                oldStorage = res.getString("STORAGE");
                oldHash = res.getString("HASH");
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);
            // prepare to store the content blob
            StorageResult storageResult = m_storageManager.prepareContent(
                dbc,
                new CmsStoragePolicyContext(content, resource));
            stmt = m_sqlManager.getPreparedStatement(conn, "C_OFFLINE_CONTENTS_UPDATE");
            // update the contents table
            byte[] fileContent = storageResult.getFileContent();
            if (fileContent.length < 2000) {
                stmt.setBytes(1, fileContent);
            } else {
                stmt.setBinaryStream(1, new ByteArrayInputStream(fileContent), fileContent.length);
            }
            stmt.setString(2, storageResult.getStorage());
            stmt.setString(3, storageResult.getHash());
            stmt.setString(4, resource.getResourceId().toString());
            stmt.executeUpdate();
            // delete the old content blob if not used any more
            if (CmsStringUtil.isNotEmpty(oldHash)) {
                m_storageManager.deleteContent(dbc, oldStorage, oldHash);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.generic.CmsVfsDriver#getAdditionalSqlQueryProperties(CmsParameterConfiguration)
     */
    @Override
    protected List<String> getAdditionalSqlQueryProperties(CmsParameterConfiguration configuration) {

        List<String> result = new ArrayList<String>();
        result.add(STORAGE_QUERY_PROPERTIES);
        return result;
    }

    /**
     * Creates the data access exception for storage read failures.<p>
     *
     * @param storage the storage identifier
     * @param hash the content hash
     * @param cause the storage exception
     * @return the data access exception
     */
    private CmsDataAccessException createStorageReadException(String storage, String hash, CmsStorageException cause) {

        return new CmsDataAccessException(
            org.opencms.db.storage.Messages.get().container(
                org.opencms.db.storage.Messages.ERR_STORAGE_BLOB_LOAD_FAILED_2,
                storage,
                hash),
            cause);
    }
}
