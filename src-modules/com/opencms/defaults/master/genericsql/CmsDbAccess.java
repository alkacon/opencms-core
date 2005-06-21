/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/defaults/master/genericsql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2005/06/21 15:50:00 $
* Version: $Revision: 1.6 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults.master.genericsql;

import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.CmsMasterMedia;
import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsLegacySecurityException;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * This class provides methods to access the database in a generic way.
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsDbAccess {

    /** The root channel of the module */
    protected String m_rootChannel = "/";

    /** TODO: delete this after successful change of dbpool */
    private String m_poolUrl;

    /** 'Constants' file. */
    protected com.opencms.defaults.master.genericsql.CmsSqlManager m_sqlManager;

    /**
     * Public empty constructor, call "init(String)" on this class afterwards.
     * This allows more flexible custom module development.<p>
     */
    public CmsDbAccess() {}

    /**
     * Constructs a new DbAccessObject and calls init(String) with the given String.<p>
     * @param dbPool the pool to access resources.
     */
    public CmsDbAccess(String dbPool) {
        init(dbPool);
    }

    /**
     * Initializes the SqlManager with the used pool.<p>
     */
    public com.opencms.defaults.master.genericsql.CmsSqlManager init(String dbPool) {
        m_sqlManager = initQueries(dbPool, getClass());
        m_poolUrl = dbPool;
        
        return m_sqlManager;
    }

    /**
     * Retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.<p>
     */
    public com.opencms.defaults.master.genericsql.CmsSqlManager initQueries(String dbPoolUrl, Class currentClass) {
        return new com.opencms.defaults.master.genericsql.CmsSqlManager(dbPoolUrl, currentClass);
    }

    /**
     * Checks if the master module table is available by performing
     * a simple query on it.<p>
     * 
     * @return true if the query was successfully performed, false otherwise
     */
    public boolean checkTables() {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "check_module_master");
            stmt.executeQuery();
            return true;                
                
        } catch (SQLException exc) {
            return false;
            
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Performs an update script on a database.<p>
     * 
     * @param updateScript the script
     * @param replacers parameter/values to replace within the script
     * @throws CmsException if something goes wrong
     */
    public void updateDatabase(String updateScript, Map replacers) throws CmsException {
        CmsSetupDb setup = new CmsSetupDb(""); /* TODO: add base path, even if not needed */
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection();
            setup.setConnection(conn);
            setup.updateDatabase(updateScript, replacers, true);
            
            Vector errors = setup.getErrors();
            if (!errors.isEmpty()) {
                StringBuffer errorMessages = new StringBuffer();
                for (Iterator i = errors.iterator(); i.hasNext();) {
                    errorMessages.append((String)i.next());
                    errorMessages.append("\n");
                }
                throw new CmsLegacyException(errorMessages.toString(), CmsLegacyException.C_SQL_ERROR);
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, null, null);
        }
    }
    
    /**
     * Set the root channel of the content.<p>
     * 
     * @param newRootChannel the new value for the rootChannel.
     */
    public void setRootChannel(String newRootChannel) {
        m_rootChannel = newRootChannel;
    }

    /**
     * Get the root channel of the content.<p>
     * 
     * @return String the root channel.
     */
    public String getRootChannel() {
        return m_rootChannel;
    }

    /**
     * Inserts a new single row in the database with the dataset.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @throws CmsException if somethong goes wrong
     */
    public void insert(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
       
        if (dataset.m_masterId == null || CmsUUID.getNullUUID().equals(dataset.m_masterId)) {
            // create a new master ID
            dataset.m_masterId = new CmsUUID();
        }
        
        int projectId = cms.getRequestContext().currentProject().getId();
        long currentTime = new java.util.Date().getTime();
        
        CmsUUID currentUserId = cms.getRequestContext().currentUser().getId();        
        dataset.m_userId = currentUserId;

        CmsUUID defaultGroupId = CmsUUID.getNullUUID();
        String defaultGroupName = null;
        
        try {
            defaultGroupName = (String)cms.getRequestContext().currentUser().getAdditionalInfo(
                CmsUserSettings.ADDITIONAL_INFO_DEFAULTGROUP);
            
            if (defaultGroupName == null || "".equalsIgnoreCase(defaultGroupName)) {
                if (CmsLog.getLog(this).isWarnEnabled()) {
                    CmsLog.getLog(this).warn(
                        "Error reading default group of user "
                        + cms.getRequestContext().currentUser().getName()
                        + ", using group "
                        + OpenCms.getDefaultUsers().getGroupUsers()
                        + " instead");
                }
                
                defaultGroupName = OpenCms.getDefaultUsers().getGroupUsers();
            }
            
            CmsGroup defaultGroup = cms.readGroup(defaultGroupName);
            defaultGroupId = defaultGroup.getId();
        } catch (CmsException e) {
            if (CmsLog.getLog(this).isErrorEnabled()) {
                CmsLog.getLog(this).error(
                    "Error reading default group "
                        + defaultGroupName
                        + " of user "
                        + cms.getRequestContext().currentUser().getName(),
                    e);
            }

            defaultGroupId = CmsUUID.getNullUUID();
        }
        
        dataset.m_groupId = defaultGroupId;

        dataset.m_projectId = projectId;
        dataset.m_lockedInProject = projectId;
        dataset.m_state = I_CmsConstants.C_STATE_NEW;
        dataset.m_lockedBy = currentUserId;
        dataset.m_lastModifiedBy = currentUserId;
        dataset.m_dateCreated = currentTime;
        dataset.m_dateLastModified = currentTime;

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "insert_offline");
            sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, dataset.m_mediaToAdd, new Vector(), new Vector());
            updateChannels(cms, dataset.m_masterId, dataset.m_channelToAdd, dataset.m_channelToDelete);
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Inserts a new row in the database with the copied dataset.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @param mediaToAdd a Vector of media to add.
     * @param channelToAdd a Vector of channels to add.
     * @return CmsUUID The uuid of the new content definition
     * @throws CmsException in case something goes wrong
     */
    public CmsUUID copy(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset, Vector mediaToAdd, Vector channelToAdd) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
        if (dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't write it!
            throw new CmsLegacySecurityException("Can't update a cd with a backup cd ", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (!content.isWriteable()) {
            // no write access
            throw new CmsLegacySecurityException("Not writeable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        CmsUUID newMasterId = new CmsUUID();
        int projectId = cms.getRequestContext().currentProject().getId();
        CmsUUID currentUserId = cms.getRequestContext().currentUser().getId();
        long currentTime = new java.util.Date().getTime();
        // filling some default-values for new dataset's
        dataset.m_masterId = newMasterId;
        dataset.m_projectId = projectId;
        dataset.m_lockedInProject = projectId;
        dataset.m_state = I_CmsConstants.C_STATE_NEW;
        dataset.m_lockedBy = currentUserId;
        dataset.m_lastModifiedBy = currentUserId;
        dataset.m_dateCreated = currentTime;
        dataset.m_dateLastModified = currentTime;

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "insert_offline");
            sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, mediaToAdd, new Vector(), new Vector());
            updateChannels(cms, dataset.m_masterId, channelToAdd, new Vector());
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        return newMasterId;
    }

    /**
     * Updates the lockstate in the database.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void writeLockstate(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
        if (!content.isWriteable()) {
            // no write access
            throw new CmsLegacySecurityException("Not writeable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (!dataset.m_lockedBy.isNullUUID()) {
            // lock the resource into the current project
            dataset.m_lockedInProject = cms.getRequestContext().currentProject().getId();
        }

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "update_lockstate_offline");
            stmt.setString(1, dataset.m_lockedBy.toString());
            stmt.setInt(2, dataset.m_lockedInProject);
            stmt.setString(3, dataset.m_masterId.toString());
            stmt.setInt(4, content.getSubId());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /** 
     * Write the dataset to the database.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void write(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
        if (dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't write it!
            throw new CmsLegacySecurityException("Can't update a cd with a backup cd", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        // read the lockstate
        readLockstate(dataset, content.getSubId());
        if (!dataset.m_lockedBy.equals(cms.getRequestContext().currentUser().getId())) {
            // is not locked by this user
            throw new CmsLegacySecurityException("Not locked by this user", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (dataset.m_lockedInProject != dataset.m_projectId) {
            // not locked in this project
            throw new CmsLegacySecurityException("Not locked in this project", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (!content.isWriteable()) {
            // no write access
            throw new CmsLegacySecurityException("Not writeable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        long currentTime = new java.util.Date().getTime();
        CmsUUID currentUserId = cms.getRequestContext().currentUser().getId();
        // updateing some values for updated dataset
        if (dataset.m_state != I_CmsConstants.C_STATE_NEW) {
            // if the state is not new then set the state to changed
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        dataset.m_lastModifiedBy = currentUserId;
        dataset.m_dateLastModified = currentTime;

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "update_offline");
            int rowcounter = sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.setString(rowcounter++, dataset.m_masterId.toString());
            stmt.setInt(rowcounter++, content.getSubId());
            stmt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, dataset.m_mediaToAdd, dataset.m_mediaToUpdate, dataset.m_mediaToDelete);
            updateChannels(cms, dataset.m_masterId, dataset.m_channelToAdd, dataset.m_channelToDelete);
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Read the dataset with the given UUID from the database.<p>
     * 
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @param contentId the UUID of the contentdefinition.
     */
    public void read(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset, CmsUUID contentId) throws CmsException {
        if (!content.isReadable()) {
            // no read access
            throw new CmsLegacySecurityException("Not readable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        String statement_key = "read_offline";
        if (isOnlineProject(cms)) {
            statement_key = "read_online";
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setString(1, contentId.toString());
            stmt.setInt(2, content.getSubId());
            res = stmt.executeQuery();
            if (res.next()) {
                sqlFillValues(res, cms, dataset);
            } else {
                throw new CmsLegacyException("[" + this.getClass().getName() + ".read] no content found for CID:" + contentId + ", SID: " + content.getSubId() + ", statement: " + statement_key, CmsLegacyException.C_NOT_FOUND);
            }
            if (!checkAccess(content, false)) {
                throw new CmsLegacySecurityException("Not readable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }

    /**
     * Read the lockstate from the database.
     * We need this because of someone has maybe stolen the lock.<p>
     * 
     * @param dataset the dataset to read the lockstate into.
     * @param subId the subId of this cd
     */
    protected void readLockstate(CmsMasterDataSet dataset, int subId) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "read_lockstate_offline");
            stmt.setString(1, dataset.m_masterId.toString());
            stmt.setInt(2, subId);
            res = stmt.executeQuery();
            if (res.next()) {
                // update the values
                dataset.m_lockedInProject = res.getInt(1);
                dataset.m_lockedBy = new CmsUUID(res.getString(2));
            } else {
                // no values found - this is a new row
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }

    /**
     * Reads all media contents from the database.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @return a Vector of media objects.
     */
    public Vector readMedia(CmsObject cms, CmsMasterContent content) throws CmsException {
        if (!content.isReadable()) {
            // no read access
            throw new CmsLegacySecurityException("Not readable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        Vector retValue = new Vector();
        String statement_key = "read_media_offline";
        if (isOnlineProject(cms)) {
            statement_key = "read_media_online";
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setString(1, content.getId().toString());
            res = stmt.executeQuery();
            while (res.next()) {
                int i = 1;
                retValue.add(new CmsMasterMedia(res.getInt(i++), new CmsUUID(res.getString(i++)), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getString(i++), res.getInt(i++), res.getString(i++), res.getString(i++), res.getString(i++), res.getBytes(i++)));
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return retValue;
    }

    /**
     * Reads all channels from the database.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @return a Vector of channel names.
     */
    public Vector readChannels(CmsObject cms, CmsMasterContent content) throws CmsException {
        if (!content.isReadable()) {
            // no read access
            throw new CmsLegacySecurityException("Not readable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        Vector retValue = new Vector();
        String statement_key = "read_channel_names_offline";
        if (isOnlineProject(cms)) {
            statement_key = "read_channel_names_online";
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setString(1, content.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                // get the channel id
                String channeldName = res.getString(1);
                retValue.add(channeldName );
            }
            
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return retValue;
    }

    /**
     * Reads all content definitions of a given channel.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param channelId the id of the channel.
     * @param subId the sub ID of the contentdefinition.
     * @return Vector the datasets of the contentdefinitions in the channel.
     */
    public Vector readAllByChannel(CmsObject cms, String channelId, int subId) throws CmsException {
        Vector theDataSets = new Vector();
        String statement_key = "readallbychannel_offline";
        if (isOnlineProject(cms)) {
            statement_key = "readallbychannel_online";
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setInt(1, subId);
            stmt.setString(2, channelId);
            res = stmt.executeQuery();
            while (res.next()) {
                CmsMasterDataSet dataset = new CmsMasterDataSet();
                sqlFillValues(res, cms, dataset);
                theDataSets.add(dataset);
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return theDataSets;
    }

    /**
     * Delete a dataset from the offline table.<p>
     * 
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void delete(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
        if (dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't delete it!
            throw new CmsLegacySecurityException("Can't delete a backup cd", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        // read the lockstate
        readLockstate(dataset, content.getSubId());
        if ((!dataset.m_lockedBy.equals(cms.getRequestContext().currentUser().getId()))) {
            // is not locked by this user
            throw new CmsLegacySecurityException("Not locked by this user", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (dataset.m_lockedInProject != dataset.m_projectId) {
            // not locked in this project
            throw new CmsLegacySecurityException("Not locked in this project", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (!content.isWriteable()) {
            // no write access
            throw new CmsLegacySecurityException("Not writeable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        if (dataset.m_state == I_CmsConstants.C_STATE_NEW) {
            // this is a new line in this project and can be deleted
            String statement_key = "delete_offline";
            PreparedStatement stmt = null;
            Connection conn = null;
            try {
                conn = m_sqlManager.getConnection();
                stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
                stmt.setString(1, dataset.m_masterId.toString());
                stmt.setInt(2, content.getSubId());
                if (stmt.executeUpdate() != 1) {
                    // no line deleted - row wasn't found
                    throw new CmsLegacyException("Row not found: " + dataset.m_masterId + " " + content.getSubId(), CmsLegacyException.C_NOT_FOUND);
                }
                // after deleting the row, we have to delete media and channel rows
                deleteAllMedia(dataset.m_masterId);
                deleteAllChannels(dataset.m_masterId);
            } catch (SQLException exc) {
                throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
            } finally {
                m_sqlManager.closeAll(null, conn, stmt, null);
            }
        } else {
            // set state to deleted and update the line
            dataset.m_state = I_CmsConstants.C_STATE_DELETED;
            dataset.m_lockedBy = CmsUUID.getNullUUID();
            PreparedStatement stmt = null;
            Connection conn = null;
            try {
                conn = m_sqlManager.getConnection();
                stmt = m_sqlManager.getPreparedStatement(conn, "update_offline");
                int rowcounter = sqlFillValues(stmt, content.getSubId(), dataset);
                stmt.setString(rowcounter++, dataset.m_masterId.toString());
                stmt.setInt(rowcounter++, content.getSubId());
                stmt.executeUpdate();
            } catch (SQLException exc) {
                throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
            } finally {
                m_sqlManager.closeAll(null, conn, stmt, null);
            }
        }
    }

    /**
     * Undelete a prevoiusly deleted contentdefinition.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void undelete(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
        if (dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't delete it!
            throw new CmsLegacySecurityException("Can't undelete a backup cd ", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (!content.isWriteable()) {
            // no write access
            throw new CmsLegacySecurityException("Not writeable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        // set state to deleted and update the line
        dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        dataset.m_lockedBy = cms.getRequestContext().currentUser().getId();
        dataset.m_lockedInProject = cms.getRequestContext().currentProject().getId();
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "update_offline");
            int rowcounter = sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.setString(rowcounter++, dataset.m_masterId.toString());
            stmt.setInt(rowcounter++, content.getSubId());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Changes the permissions of the content.<p>
     *
     * @param cms the CmsObject to get access to cms resources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void changePermissions(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if (isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT_0));
        }
        if (dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't delete it!
            throw new CmsLegacySecurityException("Can't change permissions of a backup cd ", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        // read the lockstate
        readLockstate(dataset, content.getSubId());
        if (!dataset.m_lockedBy.equals(cms.getRequestContext().currentUser().getId())) {
            // is not locked by this user
            throw new CmsLegacySecurityException("Not locked by this user", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (dataset.m_lockedInProject != dataset.m_projectId) {
            // not locked in this project
            throw new CmsLegacySecurityException("Not locked in this project", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (!content.isWriteable()) {
            // no write access
            throw new CmsLegacySecurityException("Not writeable", CmsLegacySecurityException.C_SECURITY_NO_PERMISSIONS);
        }
        if (dataset.m_state != I_CmsConstants.C_STATE_NEW) {
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        dataset.m_dateLastModified = System.currentTimeMillis();
        dataset.m_lastModifiedBy = cms.getRequestContext().currentUser().getId();
        // update the line
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "update_permissions_offline");
            stmt.setString(1, dataset.m_userId.toString());
            stmt.setString(2, dataset.m_groupId.toString());
            stmt.setInt(3, dataset.m_accessFlags);
            stmt.setInt(4, dataset.m_state);
            stmt.setString(5, dataset.m_lastModifiedBy.toString());
            stmt.setTimestamp(6, new Timestamp(dataset.m_dateLastModified));
            stmt.setString(7, dataset.m_masterId.toString());
            stmt.setInt(8, content.getSubId());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Returns a string representation of this instance.
     * This can be used for debugging.<p>
     * 
     * @return the string representation of this instance.
     */
    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append(this.getClass().getName() + "{");
        returnValue.append("Used db pool=" + m_poolUrl + ";");
        returnValue.append("}");
        return returnValue.toString();
    }

    /**
     * Inserts all values to the statement for insertion and update.<p>
     * 
     * @param stmt the Statement to fill the values to.
     * @param subId the subid of this module.
     * @param dataset the set of data for this contentdefinition.
     * @return the current rowcounter.
     */
    protected int sqlFillValues(PreparedStatement stmt, int subId, CmsMasterDataSet dataset) throws SQLException {
        // columncounter
        int i = 1;
        //// COREDATA ////
        stmt.setString(i++, dataset.m_masterId.toString());
        stmt.setInt(i++, subId);
        stmt.setString(i++, dataset.m_userId.toString());
        stmt.setString(i++, dataset.m_groupId.toString());
        stmt.setInt(i++, dataset.m_lockedInProject);
        stmt.setInt(i++, dataset.m_accessFlags);
        stmt.setInt(i++, dataset.m_state);
        stmt.setString(i++, dataset.m_lockedBy.toString());
        stmt.setString(i++, dataset.m_lastModifiedBy.toString());
        stmt.setTimestamp(i++, new Timestamp(dataset.m_dateCreated));
        stmt.setTimestamp(i++, new Timestamp(dataset.m_dateLastModified));
        //// USERDATA ////
        stmt.setTimestamp(i++, new Timestamp(dataset.m_publicationDate));
        stmt.setTimestamp(i++, new Timestamp(dataset.m_purgeDate));
        stmt.setInt(i++, dataset.m_flags);
        stmt.setInt(i++, dataset.m_feedId);
        stmt.setInt(i++, dataset.m_feedReference);
        if (dataset.m_feedFilename == null) {
            stmt.setNull(i++, Types.VARCHAR);
        } else {
            stmt.setString(i++, dataset.m_feedFilename);
        }
        if (dataset.m_title == null) {
            stmt.setNull(i++, Types.VARCHAR);
        } else {
            stmt.setString(i++, dataset.m_title);
        }
        //// GENERIC DATA ////
        i = sqlSetTextArray(stmt, dataset.m_dataBig, i);
        i = sqlSetTextArray(stmt, dataset.m_dataMedium, i);
        i = sqlSetTextArray(stmt, dataset.m_dataSmall, i);
        i = sqlSetIntArray(stmt, dataset.m_dataInt, i);
        i = sqlSetIntArray(stmt, dataset.m_dataReference, i);
        i = sqlSetDateArray(stmt, dataset.m_dataDate, i);
        return i;
    }

    /**
     * Inserts all values to the statement for insert and update.<p>
     * 
     * @param res the Resultset read the values from.
     * @param cms the CmsObject to get access to cms resources.
     * @param dataset the set of data for this contentdefinition.
     * @return the current rowcounter.
     */
    protected int sqlFillValues(ResultSet res, CmsObject cms, CmsMasterDataSet dataset) throws SQLException {
        // columncounter
        int i = 1;
        //// COREDATA ////
        dataset.m_masterId = new CmsUUID(res.getString(i++));
        // cw/12.02.2004 - subId was not read, but was already defined in data set - need it in search
        dataset.m_subId = res.getInt(i++);
        dataset.m_userId = new CmsUUID(res.getString(i++));
        dataset.m_groupId = new CmsUUID(res.getString(i++));
        dataset.m_lockedInProject = res.getInt(i++);
        // compute project based on the current project and the channels
        dataset.m_projectId = computeProjectId(cms, dataset);
        dataset.m_accessFlags = res.getInt(i++);
        dataset.m_state = res.getInt(i++);
        dataset.m_lockedBy = new CmsUUID(res.getString(i++));
        dataset.m_lastModifiedBy = new CmsUUID(res.getString(i++));
        dataset.m_dateCreated = res.getTimestamp(i++).getTime();
        dataset.m_dateLastModified = res.getTimestamp(i++).getTime();
        //// USERDATA ////
        dataset.m_publicationDate = res.getTimestamp(i++).getTime();
        dataset.m_purgeDate = res.getTimestamp(i++).getTime();
        dataset.m_flags = res.getInt(i++);
        dataset.m_feedId = res.getInt(i++);
        dataset.m_feedReference = res.getInt(i++);
        dataset.m_feedFilename = res.getString(i++);
        dataset.m_title = res.getString(i++);
        //// GENERIC DATA ////
        i = sqlSetTextArray(res, dataset.m_dataBig, i);
        i = sqlSetTextArray(res, dataset.m_dataMedium, i);
        i = sqlSetTextArray(res, dataset.m_dataSmall, i);
        i = sqlSetIntArray(res, dataset.m_dataInt, i);
        i = sqlSetIntArray(res, dataset.m_dataReference, i);
        i = sqlSetDateArray(res, dataset.m_dataDate, i);
        return i;
    }

    /**
     * Computes the correct project id based on the current user and the channels.<p>
     * 
     * @param cms the CmsObject
     * @param dataset the dataSet
     * @return int the project id
     * @throws SQLException if something goes wrong
     */
    protected int computeProjectId(CmsObject cms, CmsMasterDataSet dataset) throws SQLException {
        //int onlineProjectId = I_CmsConstants.C_UNKNOWN_ID;
        int offlineProjectId = I_CmsConstants.C_UNKNOWN_ID;

        offlineProjectId = cms.getRequestContext().currentProject().getId();
        //onlineProjectId = I_CmsConstants.C_PROJECT_ONLINE_ID;

        if (!isOnlineProject(cms)) {
            // this is an offline project -> compute if we have to return the
            // online project id or the offline project id

            // the owner and the administrtor has always access
            if ((cms.getRequestContext().currentUser().getId().equals(dataset.m_userId)) || cms.isAdmin()) {
                return offlineProjectId;
            }
            
            return offlineProjectId;

            // DISABLED: always return current offline project ID!
            //            String statement_key = "read_channel_offline";
            //
            //            PreparedStatement stmt = null;
            //            ResultSet res = null;
            //            Connection conn = null;
            //            cms.getRequestContext().saveSiteRoot();
            //            try {
            //                cms.setContextToCos();
            //                conn = m_sqlManager.getConnection();
            //                stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            //                stmt.setString(1, dataset.m_masterId.toString());
            //                res = stmt.executeQuery();
            //                while (res.next()) {
            //                    // get the channel id
            //                     String channelId = res.getString(1);
            //                    // read the resource by property "channelid"
            //                    CmsFolder channelFolder = null;
            //                    try {
            //                        channelFolder = cms.readFolder(new CmsUUID(channelId), false);
            //                        //resources = cms.getResourcesWithPropertyDefintion(I_CmsConstants.C_PROPERTY_CHANNELID, channelId + "", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
            //                    } catch (CmsException exc) {
            //                        // ignore the exception - read the next one
            //                    }
            //                    if (channelFolder != null) {
            //                        int resProjectId = channelFolder.getProjectLastModified();
            //                        if (resProjectId == offlineProjectId) {
            //                            // yes - we have found a channel that belongs to
            //                            // the current offlineproject -> we can return the
            //                            // offline project id as computed project id
            //                            return offlineProjectId;
            //                        }
            //                    }
            //                }
            //            } finally {
            //                cms.getRequestContext().restoreSiteRoot();
            //                m_sqlManager.closeAll(conn, stmt, res);
            //            }
        }
        // no channel found that belongs to the offlineproject ->
        // return the online project id.
        return offlineProjectId;
    }

    /**
     * Sets an array of strings into the statement.<p>
     * 
     * @param stmt the PreparedStatement to set the values into.
     * @param array the array of strings to set.
     * @param the columnscounter for the statement.
     * @return the increased columnscounter;
     */
    protected int sqlSetTextArray(PreparedStatement stmt, String[] array, int columnscounter) throws SQLException {
        for (int j = 0; j < array.length; j++) {
            if (array[j] == null) {
                stmt.setNull(columnscounter++, Types.LONGVARCHAR);
            } else {
                stmt.setString(columnscounter++, array[j]);
            }
        }
        return columnscounter;
    }

    /**
     * Sets an array of strings from the resultset.<p>
     * 
     * @param res the ResultSet to get the values from.
     * @param array the array of strings to set.
     * @param the columnscounter for the res.
     * @return the increased columnscounter;
     */
    protected int sqlSetTextArray(ResultSet res, String[] array, int columnscounter) throws SQLException {
        for (int j = 0; j < array.length; j++) {
            array[j] = res.getString(columnscounter++);
        }
        return columnscounter;
    }

    /**
     * Sets an array of ints into the statement.<p>
     * 
     * @param stmt the PreparedStatement to set the values into.
     * @param array the array of ints to set.
     * @param the columnscounter for the stmnt.
     * @return the increased columnscounter;
     */
    protected int sqlSetIntArray(PreparedStatement stmt, int[] array, int columnscounter) throws SQLException {
        for (int j = 0; j < array.length; j++) {
            stmt.setInt(columnscounter++, array[j]);
        }
        return columnscounter;
    }

    /**
     * Sets an array of ints from the resultset.<p>
     * 
     * @param res the ResultSet to get the values from.
     * @param array the array of ints to set.
     * @param the columnscounter for the res.
     * @return the increased columnscounter;
     */
    protected int sqlSetIntArray(ResultSet res, int[] array, int columnscounter) throws SQLException {
        for (int j = 0; j < array.length; j++) {
            array[j] = res.getInt(columnscounter++);
        }
        return columnscounter;
    }

    /**
     * Sets an array of longs (dates) into the statement.<p>
     * 
     * @param stmt the PreparedStatement to set the values into.
     * @param array the array of longs to set.
     * @param the columnscounter for the stmnt.
     * @return the increased columnscounter;
     */
    protected int sqlSetDateArray(PreparedStatement stmt, long[] array, int columnscounter) throws SQLException {
        for (int j = 0; j < array.length; j++) {
            stmt.setTimestamp(columnscounter++, new Timestamp(array[j]));
        }
        return columnscounter;
    }

    /**
     * Sets an array of longs (dates) from the resultset.<p>
     * 
     * @param res the ResultSet to get the values from.
     * @param array the array of longs to set.
     * @param the columnscounter for the res.
     * @return the increased columnscounter;
     */
    protected int sqlSetDateArray(ResultSet res, long[] array, int columnscounter) throws SQLException {
        for (int j = 0; j < array.length; j++) {
            array[j] = res.getTimestamp(columnscounter++).getTime();
        }
        return columnscounter;
    }

    /**
     * Returns a vector of contentdefinitions based on the sql resultset.
     * Never mind about the visible flag.<p>
     * 
     * @param res the ResultSet to get data lines from.
     * @param contentDefinitionClass the class of the cd to create new instances.
     * @param cms the CmsObject to get access to cms resources.
     * @throws SqlException if nothing could be read from the resultset.
     */
    protected Vector createVectorOfCd(ResultSet res, Class contentDefinitionClass, CmsObject cms) throws SQLException {
        return createVectorOfCd(res, contentDefinitionClass, cms, false);
    }

    /**
     * Returns a vector of contentdefinitions based on the sql resultset.<p>
     * 
     * @param res the ResultSet to get data-lines from.
     * @param contentDefinitionClass the class of the cd to create new instances.
     * @param cms the CmsObject to get access to cms resources.
     * @param viewonly  decides, if only the ones that are visible should be returned
     * @throws SqlException if nothing could be read from the resultset.
     */
    protected Vector createVectorOfCd(ResultSet res, Class contentDefinitionClass, CmsObject cms, boolean viewonly) throws SQLException {
        Constructor constructor;
        Vector retValue = new Vector();
       
        try { // to get the constructor to create an empty contentDefinition
            constructor = contentDefinitionClass.getConstructor(new Class[] { CmsObject.class, CmsMasterDataSet.class });
        } catch (NoSuchMethodException exc) {
            
            if (CmsLog.getLog(this).isWarnEnabled()) {
                CmsLog.getLog(this).warn("Cannot locate constructor", exc);
            }
            // canno't fill the vector - missing constructor
            return retValue;
        }
        while (res.next()) { // while there is data in the resultset
            CmsMasterDataSet dataset = new CmsMasterDataSet();
            try { // to invoce the constructor to get a new empty instance
                CmsMasterContent content = (CmsMasterContent)constructor.newInstance(new Object[] { cms, dataset });              
                
                sqlFillValues(res, cms, dataset);
                // add the cd only if read (and visible) permissions are granted.
                // the visible-permissens will be checked, if viewonly is set to true
                // viewonly=true is needed for the backoffice
                if (checkAccess(content, viewonly)) {
                    retValue.add(content);
                }
            } catch (Exception exc) {
                if (CmsLog.getLog(this).isWarnEnabled()) {
                    CmsLog.getLog(this).warn("Cannot invoce constructor", exc);
                }
            }
        }
        return retValue;
    }

    /**
     * Returns a vector of contentdefinitions based on the sql resultset.<p>
     * 
     * @param datasets the vector with the datasets.
     * @param contentDefinitionClass the class of the cd to create new instances.
     * @param cms the CmsObject to get access to cms-ressources.
     * @throws SqlException if nothing could be read from the resultset.
     */
    protected Vector createVectorOfCd(Vector datasets, Class contentDefinitionClass, CmsObject cms) throws SQLException {
        Constructor constructor;
        Vector retValue = new Vector();
        try { // to get the constructor to create an empty contentDefinition
            constructor = contentDefinitionClass.getConstructor(new Class[] { CmsObject.class, CmsMasterDataSet.class });
        } catch (NoSuchMethodException exc) {
            if (CmsLog.getLog(this).isWarnEnabled()) {
                CmsLog.getLog(this).warn("Cannot locate constructor", exc);
            }
            // canno't fill the vector - missing constructor
            return retValue;
        }
        // create content definition for each dataset
        for (int i = 0; i < datasets.size(); i++) {
            CmsMasterDataSet dataset = (CmsMasterDataSet)datasets.elementAt(i);
            try { // to invoce the constructor to get a new empty instance
                CmsMasterContent content = (CmsMasterContent)constructor.newInstance(new Object[] { cms, dataset });
                retValue.add(content);
            } catch (Exception exc) {
                if (CmsLog.getLog(this).isWarnEnabled()) {
                    CmsLog.getLog(this).warn("Cannot invoce constructor", exc);
                }
            }
        }
        return retValue;
    }

    /**
     * Checks if read (and visible) permissions are granted.
     * the visible permissions will be checked, if viewonly is set to true
     * viewonly=true is needed for the backoffice.<p>
     * 
     * @param content the cd to check.
     * @param viewonly if set to true the v-Flag will be checked, too.
     */
    protected boolean checkAccess(CmsMasterContent content, boolean viewonly) {
        if (!content.isReadable()) {
            // was not readable
            return false;
        } else if (viewonly) {
            // additional check for v-Flags
            return content.isVisible();
        } else {
            // was readable - return true
            return true;
        }
    }

    /**
     * Returns true, if the current project is the online project.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @return true, if this is the onlineproject, else returns false
     */
    protected boolean isOnlineProject(CmsObject cms) {
        return cms.getRequestContext().currentProject().isOnlineProject();
    }

    /**
     * Deletes all media lines for a master content definition.<p>
     * 
     * @param masterId - the masterId to delete the media for.
     * @throws SQLException if an sql error occurs.
     */
    protected void deleteAllMedia(CmsUUID masterId) throws SQLException {
        String statement_key = "delete_all_media_offline";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setString(1, masterId.toString());
            stmt.executeUpdate();
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Deletes all channel lines for one master.<p>
     * 
     * @param masterId - the masterId to delete the channels for.
     * @throws SQLException if an sql error occurs.
     */
    protected void deleteAllChannels(CmsUUID masterId) throws SQLException {
        String statement_key = "delete_all_channel_offline";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setString(1, masterId.toString());
            stmt.executeUpdate();
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Updates the media object of a content definition.<p>
     * 
     * @param masterId the content definition master id.
     * @param mediaToAdd vector of media objects to add.
     * @param mediaToUpdate vector of media objects to update.
     * @param mediaToDelete vector of media objects to delete.
     * @throws SQLException if an sql error occurs.
     * @throws CmsException if an error occurs.
     */
    protected void updateMedia(CmsUUID masterId, Vector mediaToAdd, Vector mediaToUpdate, Vector mediaToDelete) throws SQLException, CmsException {
        // add new media
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "insert_media_offline");
            for (int i = 0; i < mediaToAdd.size(); i++) {
                CmsMasterMedia media = (CmsMasterMedia)mediaToAdd.get(i);
                media.setId(CmsDbUtil.nextId(m_poolUrl, "CMS_MODULE_MEDIA"));
                media.setMasterId(masterId);
                sqlFillValues(stmt, media);
                stmt.executeUpdate();
            }
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }

        // update existing media
        stmt = null;
        conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "update_media_offline");
            for (int i = 0; i < mediaToUpdate.size(); i++) {
                CmsMasterMedia media = (CmsMasterMedia)mediaToUpdate.get(i);
                media.setMasterId(masterId);
                int rowCounter = sqlFillValues(stmt, media);
                stmt.setInt(rowCounter++, media.getId());
                stmt.setString(rowCounter++, masterId.toString());
                stmt.executeUpdate();
            }
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        // delete unneeded media
        stmt = null;
        conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "delete_media_offline");
            for (int i = 0; i < mediaToDelete.size(); i++) {
                CmsMasterMedia media = (CmsMasterMedia)mediaToDelete.get(i);
                stmt.setInt(1, media.getId());
                stmt.setString(2, masterId.toString());
                stmt.executeUpdate();
            }
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Updates the channels of a content definition.<p>
     * 
     * @param cms the current context object.
     * @param masterId the content definition master id.
     * @param channelToAdd vector of channels to add. 
     * @param channelToDelete vector of channels to delete.
     * @throws SQLException if an sql error occurs.
     */
    protected void updateChannels(CmsObject cms, CmsUUID masterId, Vector channelToAdd, Vector channelToDelete) throws SQLException {
        // add new channel
        PreparedStatement stmt = null;
        Connection conn = null;
        cms.getRequestContext().saveSiteRoot();
        cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);
           conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "insert_channel_offline");
            for (int i = 0; i < channelToAdd.size(); i++) {
                try {
                    stmt.setString(1, masterId.toString());


                    //int id=Integer.parseInt(cms.readProperty(channelToAdd.get(i) + "", I_CmsConstants.C_PROPERTY_CHANNELID));
                    //stmt.setInt(2, id);
                    CmsResource channel=cms.readFolder((String)channelToAdd.get(i));
                    String id=channel.getResourceId().toString();
                   
                    stmt.setString(2, id);  
                    stmt.setString(3,(String)channelToAdd.get(i));
                    //    I_CmsConstants.C_PROPERTY_CHANNELID)));
                    stmt.executeUpdate();
                } catch (CmsException exc) {
                    // no channel found - write to logfile
                    if (CmsLog.getLog(this).isWarnEnabled()) {
                        CmsLog.getLog(this).warn("Couldn't find channel " + channelToAdd.get(i), exc);
                    }
                }
            }
            m_sqlManager.closeAll(null, conn, stmt, null);
            
        // delete unneeded channel
        stmt = null;
        conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "delete_channel_offline");
            for (int i = 0; i < channelToDelete.size(); i++) {
                //try {
                    stmt.setString(1, masterId.toString());
                    stmt.setString(2, (String)channelToDelete.get(i) );
                    // stmnt.setInt(2, Integer.parseInt(cms.readProperty(C_COS_PREFIX + channelToDelete.get(i),
                    //     I_CmsConstants.C_PROPERTY_CHANNELID)));
                    stmt.executeUpdate();
                /*} catch (CmsException exc) {
                    // no channel found - write to logfile
                    if (CmsLog.getLog(this).isWarnEnabled()) {
                        CmsLog.getLog(this).warn("Couldn't find channel " + channelToAdd.get(i), exc);
                    }
                }*/
            }
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Fills a prepared statement with media values.<p>
     * 
     * @param stmt the statement to fill.
     * @param media the data to fill the statement with.
     * @return int the number of values set in the statement.
     * @throws SQLException if data could not be set in statement.
     */
    protected int sqlFillValues(PreparedStatement stmt, CmsMasterMedia media) throws SQLException {
        int i = 1;
        stmt.setInt(i++, media.getId());
        stmt.setString(i++, media.getMasterId().toString());
        stmt.setInt(i++, media.getPosition());
        stmt.setInt(i++, media.getWidth());
        stmt.setInt(i++, media.getHeight());
        stmt.setInt(i++, media.getSize());
        stmt.setString(i++, media.getMimetype());
        stmt.setInt(i++, media.getType());
        stmt.setString(i++, media.getTitle());
        stmt.setString(i++, media.getName());
        stmt.setString(i++, media.getDescription());
        stmt.setBinaryStream(i++, new ByteArrayInputStream(media.getMedia()), media.getMedia().length);
        //stmnt.setBytes(i++, media.getMedia());
        return i;
    }

    /**
     * Returns a vector with all version of a master in the backup table.<p>
     *
     * @param cms the CmsObject.
     * @param contentDefinitionClass the Class of the current master.
     * @param masterId the id of the master.
     * @param subId the sub id of the master.
     * @return Vector a vector with all versions of the master.
     */
    public Vector getHistory(CmsObject cms, Class contentDefinitionClass, CmsUUID masterId, int subId) throws CmsException {
        Vector retVector = new Vector();
        Vector allBackup = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "read_all_backup");
            stmt.setString(1, masterId.toString());
            stmt.setInt(2, subId);
            // gets all versions of the master in the backup table
            res = stmt.executeQuery();
            while (res.next()) {
                CmsMasterDataSet dataset = new CmsMasterDataSet();
                sqlFillValues(res, cms, dataset);
                dataset.m_versionId = res.getInt("TAG_ID");
                dataset.m_userName = res.getString("USER_NAME");
                dataset.m_groupName = res.getString("GROUP_NAME");
                dataset.m_lastModifiedByName = res.getString("LASTMODIFIED_BY_NAME");
                allBackup.add(dataset);
            }
            retVector = createVectorOfCd(allBackup, contentDefinitionClass, cms);
        } catch (SQLException e) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return retVector;
    }

    /**
     * Returns the version of a master in the backup.<p>
     *
     * @param cms The CmsObject.
     * @param contentDefinitionClass The class of the content definition.
     * @param masterId the id of the master.
     * @param subId the sub id.
     * @param versionId the version id.
     * @return CmsMasterContent a content definition of the version.
     */
    public CmsMasterContent getVersionFromHistory(CmsObject cms, Class contentDefinitionClass, CmsUUID masterId, int subId, int versionId) throws CmsException {
        CmsMasterContent content = null;
        CmsMasterDataSet dataset = this.getVersionFromHistory(cms, masterId, subId, versionId);
        Constructor constructor;
        try { // to get the constructor to create an empty contentDefinition
            constructor = contentDefinitionClass.getConstructor(new Class[] { CmsObject.class, CmsMasterDataSet.class });
        } catch (NoSuchMethodException exc) {
            if (CmsLog.getLog(this).isWarnEnabled()) {
                CmsLog.getLog(this).warn("Cannot locate constructor", exc);
            }
            // canno't fill the vector - missing constructor
            return content;
        }
        // create content definition for each dataset
        if (dataset != null) {
            try { // to invoce the constructor to get a new empty instance
                content = (CmsMasterContent)constructor.newInstance(new Object[] { cms, dataset });
            } catch (Exception exc) {
                if (CmsLog.getLog(this).isWarnEnabled()) {
                    CmsLog.getLog(this).warn("Cannot invoke constructor", exc);
                }
            }
        }
        return content;
    }

    /**
     * Returns the version of a master from the backup.<p>
     *
     * @param cms the CmsObject.
     * @param masterId the id of the master.
     * @param subId the sub id.
     * @param versionId the version id.
     * @return Vector A vector with all versions of the master
     */
    public CmsMasterDataSet getVersionFromHistory(CmsObject cms, CmsUUID masterId, int subId, int versionId) throws CmsException {
        CmsMasterDataSet dataset = new CmsMasterDataSet();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "read_backup");
            stmt.setString(1, masterId.toString());
            stmt.setInt(2, subId);
            stmt.setInt(3, versionId);
            // gets the master in the backup table with the given versionid
            res = stmt.executeQuery();
            if (res.next()) {
                sqlFillValues(res, cms, dataset);
                dataset.m_versionId = res.getInt("TAG_ID");
                dataset.m_userName = res.getString("USER_NAME");
                dataset.m_groupName = res.getString("GROUP_NAME");
                dataset.m_lastModifiedByName = res.getString("LASTMODIFIED_BY_NAME");
            } else {
                throw new CmsLegacyException("Row not found: " + masterId + " " + subId + " version " + versionId, CmsLegacyException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return dataset;
    }

    /**
     * Restores a version of the master and media from backup.<p>
     *
     * @param cms the CmsObject.
     * @param content the master content.
     * @param dataset the dataset of the master.
     * @param versionId the version id of the master and media to restore.
     */
    public void restore(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset, int versionId) throws CmsException {
        Connection conn = null;
        Connection conn2 = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet res = null;
        // first read the version from backup
        CmsMasterDataSet backup = getVersionFromHistory(cms, dataset.m_masterId, content.getSubId(), versionId);
        // update the dataset
        dataset.m_accessFlags = backup.m_accessFlags;
        dataset.m_dataBig = backup.m_dataBig;
        dataset.m_dataInt = backup.m_dataInt;
        dataset.m_dataMedium = backup.m_dataMedium;
        dataset.m_dataReference = backup.m_dataReference;
        dataset.m_dataDate = backup.m_dataDate;
        dataset.m_dataSmall = backup.m_dataSmall;
        dataset.m_feedFilename = backup.m_feedFilename;
        dataset.m_feedId = backup.m_feedId;
        dataset.m_feedReference = backup.m_feedReference;
        dataset.m_flags = backup.m_flags;
        dataset.m_title = backup.m_title;
        dataset.m_publicationDate = backup.m_publicationDate;
        dataset.m_purgeDate = backup.m_purgeDate;
        dataset.m_channel = new Vector();
        dataset.m_channelToAdd = new Vector();
        dataset.m_channelToDelete = new Vector();
        dataset.m_media = new Vector();
        dataset.m_mediaToAdd = new Vector();
        dataset.m_mediaToUpdate = new Vector();
        dataset.m_mediaToDelete = new Vector();
        dataset.m_lastModifiedBy = cms.getRequestContext().currentUser().getId();
        if (dataset.m_state != I_CmsConstants.C_STATE_NEW) {
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        // check if the group exists
        CmsUUID groupId = CmsUUID.getNullUUID();
        try {
            groupId = cms.readGroup(backup.m_groupId).getId();
        } catch (CmsException exc) {
            groupId = dataset.m_groupId;
        }
        dataset.m_groupId = groupId;
        // check if the user exists
        CmsUUID userId = CmsUUID.getNullUUID();
        try {
            userId = cms.readUser(backup.m_userId).getId();
        } catch (CmsException exc) {
            userId = dataset.m_userId;
        }
        dataset.m_userId = userId;
        // write the master
        this.write(cms, content, dataset);
        // delete the media
        try {
            deleteAllMedia(dataset.m_masterId);
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        }
        // copy the media from backup
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "read_media_backup");
            stmt.setString(1, dataset.m_masterId.toString());
            stmt.setInt(2, versionId);
            res = stmt.executeQuery();
            while (res.next()) {
                int i = 1;
                CmsMasterMedia media = new CmsMasterMedia(res.getInt(i++), new CmsUUID(res.getString(i++)), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getString(i++), res.getInt(i++), res.getString(i++), res.getString(i++), res.getString(i++), res.getBytes(i++));
                // store the data in offline table
                try {
                    stmt2 = null;
                    conn2 = null;
                    conn2 = m_sqlManager.getConnection();
                    stmt2 = m_sqlManager.getPreparedStatement(conn2, "insert_media_offline");
                    sqlFillValues(stmt2, media);
                    stmt2.executeUpdate();
                } catch (SQLException ex) {
                    throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, ex);
                } finally {
                    m_sqlManager.closeAll(null, conn2, stmt2, null);
                }
            }
        } catch (SQLException e) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }

    /**
     * Publishes a single content definition.<p>
     * 
     * @param cms The CmsObject
     * @param publishHistoryId the ID of the current publish task
     * @param dataset the dataset to publish.
     * @param subId the subId to publish cd's for.
     * @param contentDefinitionName the name of the contentdefinition.
     * @param enableHistory set to true if backup tables should be filled.
     * @param versionId the versionId to save in the backup tables.
     * @param publishingDate the date and time of this publishing process.
     * @param changedResources a Vector of changed resources.
     * @param changedModuleData a Vector of changed moduledata.
     */
    public void publishResource(CmsObject cms, CmsUUID publishHistoryId, CmsMasterDataSet dataset, int subId, String contentDefinitionName, boolean enableHistory, int versionId, long publishingDate, Vector changedResources, Vector changedModuleData) throws CmsException {
        this.publishOneLine(cms, publishHistoryId, dataset, subId, contentDefinitionName, enableHistory, versionId, publishingDate, changedResources, changedModuleData);
    }
    /**
     * Publishes all resources for this project.
     * Publishes all modified content definitions for this project.<p>
     * 
     * @param cms The CmsObject
     * @param publishHistoryId the ID of the current publish task
     * @param enableHistory set to true if backup tables should be filled.
     * @param projectId the Project that should be published.
     * @param versionId the versionId to save in the backup tables.
     * @param publishingDate the date and time of this publishing process.
     * @param subId the subId to publish cd's for.
     * @param contentDefinitionName the name of the contentdefinition.
     * @param changedRessources a Vector of Resources that were changed by this publishing process.
     * @param changedModuleData a Vector of Resources that were changed by this publishing process. 
     * New published data will be added to this Vector to return it.
     */
    public void publishProject(CmsObject cms, CmsUUID publishHistoryId, boolean enableHistory, int projectId, int versionId, long publishingDate, int subId, String contentDefinitionName, Vector changedRessources, Vector changedModuleData) throws CmsException {

        String statement_key = "read_all_for_publish";

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, statement_key);
            stmt.setInt(1, subId);
            stmt.setInt(2, projectId);
            stmt.setInt(3, I_CmsConstants.C_STATE_UNCHANGED);
            // gets all ressources that are changed int this project
            // and that belongs to this subId
            res = stmt.executeQuery();
            while (res.next()) {
                // create a new dataset to fill the values
                CmsMasterDataSet dataset = new CmsMasterDataSet();
                // fill the values to the dataset
                sqlFillValues(res, cms, dataset);
                publishOneLine(cms, publishHistoryId, dataset, subId, contentDefinitionName, enableHistory, versionId, publishingDate, changedRessources, changedModuleData);
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }

    /**
     * Publish one content definition.<p>
     * 
     * @param cms The CmsObject
     * @param publishHistoryId the ID of the current publish task
     * @param dataset the dataset to publish.
     * @param subId the subId to publish cd's for.
     * @param contentDefinitionName the name of the contentdefinition.
     * @param enableHistory set to true if backup tables should be filled.
     * @param versionId the versionId to save in the backup tables.
     * @param publishingDate the date and time of this publishing process.
     * @param changedRessources a Vector of Resources that were changed by this publishing process.
     * @param changedModuleData a Vector of Ressource that were changed by this publishing process. 
     * New published data will be add to this Vector to return it.
     */
    protected void publishOneLine(CmsObject cms, CmsUUID publishHistoryId, CmsMasterDataSet dataset, int subId, String contentDefinitionName, boolean enableHistory, int versionId, long publishingDate, Vector changedRessources, Vector changedModuleData) throws CmsException {
        int state = dataset.m_state;

        try {
            Class.forName(contentDefinitionName).getMethod("beforePublish", new Class[] { CmsObject.class, Boolean.class, Integer.class, Integer.class, Long.class, Vector.class, Vector.class, CmsMasterDataSet.class }).invoke(null, new Object[] { cms, new Boolean(enableHistory), new Integer(subId), new Integer(versionId), new Long(publishingDate), changedRessources, changedModuleData, dataset });
        } catch (Exception e) {
            CmsLog.getLog(this).warn("Error calling method beforePublish() in class " + contentDefinitionName, e);
        }

        // backup the data
        if (enableHistory) {
            // store the creationdate, because it will be set to publishingdate
            // with this method.
            long backupCreationDate = dataset.m_dateCreated;
            publishBackupData(cms, dataset, subId, versionId, publishingDate);
            // restore the creationdate to the correct one
            dataset.m_dateCreated = backupCreationDate;
        }

        // delete the online data
        publishDeleteData(dataset.m_masterId, subId, "online");

        if (state == I_CmsConstants.C_STATE_DELETED) {
            // delete the data from offline
            // the state was DELETED
            publishDeleteData(dataset.m_masterId, subId, "offline");
        } else {
            // copy the data from offline to online
            // the state was NEW or CHANGED
            publishCopyData(dataset, subId);
        }

        // now update state, lockstate and projectId in offline
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "update_state_offline");
            stmt.setInt(1, I_CmsConstants.C_STATE_UNCHANGED);
            stmt.setString(2, CmsUUID.getNullUUID().toString());
            stmt.setString(3, dataset.m_masterId.toString());
            stmt.setInt(4, subId);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        
        // add an entry to the publish history
        writePublishHistory(
            cms.getRequestContext().currentProject(),
            publishHistoryId,
            versionId,
            contentDefinitionName,
            dataset.m_masterId,
            subId,
            state);

        // update changedModuleData Vector
        changedModuleData.add(
            new CmsPublishedResource(
                CmsUUID.getNullUUID(),
                dataset.m_masterId, 
                I_CmsConstants.C_UNKNOWN_ID,
                contentDefinitionName,
                subId, 
                false,
                state, 
                1));
    }

    /**
     * Publish all deletions.<p>
     * @param masterId the id of the content definition.
     * @param subId the sub id of the cd.
     * @param table the used table.
     * @throws CmsException if sql or other errors occur.
     */
    protected void publishDeleteData(CmsUUID masterId, int subId, String table) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        String deleteChannel = "delete_all_channel_" + table;
        // delete channel relation
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, deleteChannel);
            stmt.setString(1, masterId.toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        // delete media
        try {
            conn = null;
            stmt = null;
            String deleteMedia = "delete_all_media_" + table;
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, deleteMedia);
            stmt.setString(1, masterId.toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        // delete the row
        try {
            stmt = null;
            conn = null;
            String delete = "delete_" + table;
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, delete);
            stmt.setString(1, masterId.toString());
            stmt.setInt(2, subId);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * Publishes a copied row of a content definition.<p>
     * 
     * @param dataset the dataset.
     * @param subId the used sub id.
     * @throws CmsException if sql or other errors occur.
     */
    protected void publishCopyData(CmsMasterDataSet dataset, int subId) throws CmsException {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet res = null;
        Connection conn = null;
        Connection conn2 = null;
        CmsUUID masterId = dataset.m_masterId;

        // copy the row
        try {
            stmt = null;
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "insert_online");
            // correct the data in the dataset
            dataset.m_projectId = I_CmsConstants.C_PROJECT_ONLINE_ID;
            dataset.m_lockedInProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
            dataset.m_state = I_CmsConstants.C_STATE_UNCHANGED;
            dataset.m_lockedBy = CmsUUID.getNullUUID();
            sqlFillValues(stmt, subId, dataset);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        // copy media
        try {
            // read all media of master from offline
            stmt = null;
            res = null;
            conn = null;
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "read_media_offline");
            stmt.setString(1, masterId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                // create a new dataset to fill the values
                int i = 1;
                CmsMasterMedia mediaset = new CmsMasterMedia(res.getInt(i++), new CmsUUID(res.getString(i++)), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getString(i++), res.getInt(i++), res.getString(i++), res.getString(i++), res.getString(i++), res.getBytes(i++));
                // insert media of master into online
                try {
                    stmt2 = null;
                    conn2 = null;
                    conn2 = m_sqlManager.getConnection();
                    stmt2 = m_sqlManager.getPreparedStatement(conn2, "insert_media_online");
                    sqlFillValues(stmt2, mediaset);
                    stmt2.executeUpdate();
                } catch (SQLException ex) {
                    throw ex;
                } finally {
                    m_sqlManager.closeAll(null, conn2, stmt2, null);
                }
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        // copy channel relation
        try {
            stmt = null;
            res = null;
            conn = null;
            conn = m_sqlManager.getConnection();
            // read all channel relations for master from offline
            stmt = m_sqlManager.getPreparedStatement(conn, "read_channel_offline");
            stmt.setString(1, masterId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                // insert all channel relations for master into online
                try {
                    stmt2 = null;
                    conn2 = null;
                    conn2 = m_sqlManager.getConnection();
                    stmt2 = m_sqlManager.getPreparedStatement(conn2, "insert_channel_online");
                    stmt2.setString(1, masterId.toString());
                    stmt2.setString(2, res.getString(1));
                    stmt2.setString(3, res.getString(2));
                    stmt2.executeUpdate();
                } catch (SQLException ex) {
                    throw ex;
                } finally {
                    m_sqlManager.closeAll(null, conn2, stmt2, null);
                }
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }

    /**
     * Insert content definitions in backup table<p>
     * @param cms the CmsObject.
     * @param dataset the current data set.
     * @param subId the used sub id.
     * @param versionId the version of the backup.
     * @param publishDate the publishing date.
     * @throws CmsException if sql or other errors occur.
     */
    protected void publishBackupData(CmsObject cms, CmsMasterDataSet dataset, int subId, int versionId, long publishDate) throws CmsException {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet res = null;
        Connection conn = null;
        Connection conn2 = null;
        CmsUUID masterId = dataset.m_masterId;

        // copy the row
        try {
            stmt = null;
            conn = null;
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "insert_backup");
            // correct the data in the dataset
            dataset.m_lockedBy = CmsUUID.getNullUUID();
            dataset.m_dateCreated = publishDate;
            // get the name of the owner
            String ownerName = "";
            try {
                CmsUser owner = cms.readUser(dataset.m_userId);
                ownerName = owner.getName() + " " + owner.getFirstname() + " " + owner.getLastname();
            } catch (CmsException ex) {
                ownerName = "";
            }
            // get the name of the group
            String groupName = "";
            try {
                CmsGroup group = cms.readGroup(dataset.m_groupId);
                groupName = group.getName();
            } catch (CmsException ex) {
                groupName = "";
            }
            // get the name of the user who has modified the resource
            String userName = "";
            try {
                CmsUser user = cms.readUser(dataset.m_lastModifiedBy);
                userName = user.getName() + " " + user.getFirstname() + " " + user.getLastname();
            } catch (CmsException ex) {
                userName = "";
            }
            int lastId = sqlFillValues(stmt, subId, dataset);
            // set version
            stmt.setInt(lastId++, versionId);
            stmt.setString(lastId++, ownerName);
            stmt.setString(lastId++, groupName);
            stmt.setString(lastId++, userName);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
        // copy media
        try {
            // read all media of master from offline
            stmt = null;
            res = null;
            conn = null;
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "read_media_offline");
            stmt.setString(1, masterId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                // create a new dataset to fill the values
                int i = 1;
                CmsMasterMedia mediaset = new CmsMasterMedia(res.getInt(i++), new CmsUUID(res.getString(i++)), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getInt(i++), res.getString(i++), res.getInt(i++), res.getString(i++), res.getString(i++), res.getString(i++), res.getBytes(i++));
                // insert media of master into backup
                try {
                    conn2 = null;
                    stmt2 = null;
                    conn2 = m_sqlManager.getConnection();
                    stmt2 = m_sqlManager.getPreparedStatement(conn2, "insert_media_backup");
                    int lastId = sqlFillValues(stmt2, mediaset);
                    stmt2.setInt(lastId, versionId);
                    stmt2.executeUpdate();
                } catch (SQLException ex) {
                    throw ex;
                } finally {
                    m_sqlManager.closeAll(null, conn2, stmt2, null);
                }
            }
        } catch (SQLException exc) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }
    
    /**
     * Inserts an entry in the publish history for a published COS resource.<p>
     * 
     * @param project the current project
     * @param publishId the ID of the current publishing process
     * @param tagId the current backup ID
     * @param contentDefinitionName the package/class name of the content definition 
     * @param masterId the content ID of the published module data
     * @param subId the module ID of the published module data
     * @param state the state of the resource *before* it was published
     * 
     * @throws CmsException if something goes wrong
     */   
    public void writePublishHistory(CmsProject project, CmsUUID publishId, int tagId, String contentDefinitionName, CmsUUID masterId, int subId, int state) throws CmsException {
        
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, tagId);
            // structure id is null
            stmt.setString(2, CmsUUID.getNullUUID().toString());
            // master ID
            stmt.setString(3, masterId.toString());
            // content definition name
            stmt.setString(4, contentDefinitionName);
            // state
            stmt.setInt(5, state);
            // sub ID
            stmt.setInt(6, subId);
            stmt.setString(7, publishId.toString());
            stmt.setInt(8, 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsLegacyException(CmsLegacyException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
    }    

}