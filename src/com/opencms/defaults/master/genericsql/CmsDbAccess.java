/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/genericsql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2003/05/20 16:36:24 $
* Version: $Revision: 1.36 $
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

import com.opencms.boot.CmsBase;
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.dbpool.CmsDbcp;
import com.opencms.dbpool.CmsIdGenerator;
import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.CmsMasterMedia;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.Utils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * This class provides methods to access the database in a generic way.
 */
public class CmsDbAccess {

    public static final String C_COS_PREFIX = "/" + I_CmsConstants.C_ROOTNAME_COS;

    /** The pool to access offline ressources */
    protected String m_poolName;

    /** The pool to access the online ressources */
    protected String m_onlinePoolName;

    /** The pool to access the backup ressources */
    protected String m_backupPoolName;

    /** The query properties for this accessmodule */
    protected Properties m_queries;

    /** The root channel of the module */
    protected String m_rootChannel = "/";
    
    /** TODO: delete this after successful change of dbpool */
    private String m_interimOfflinePoolUrl;

    /**
     * Public empty constructor, call "init()" on this class afterwards.
     * This allows more flexible custom module development.
     * FLEX: Made the constructor public!
     */
    public CmsDbAccess() {
    }

    /**
     * Constructs a new DbAccessObject.
     * @param poolName the pool to access offline ressources.
     * @param onlinePoolName the pool to access the online ressources.
     * @param backupPoolName the pool to access the backup ressources.
     */
    public CmsDbAccess(String poolName, String onlinePoolName, String backupPoolName) {
        init(poolName, onlinePoolName, backupPoolName);
    }
    
    /**
     * Initializes the DBAccessObject.
     */
    public void init(String offline, String online, String backup) {
        m_interimOfflinePoolUrl = offline;
        m_poolName = CmsDbcp.C_DBCP_JDBC_URL_PREFIX+ offline;
        m_onlinePoolName = CmsDbcp.C_DBCP_JDBC_URL_PREFIX+online;
        m_backupPoolName = CmsDbcp.C_DBCP_JDBC_URL_PREFIX+backup;
        m_queries = new Properties();
        // collect all query.properties in all packages of superclasses
        loadQueries(getClass());
        combineQueries();        
    }

    /**
     * Set the root channel
     * @param newRootChannel the new value for the rootChannel
     */
    public void setRootChannel(String newRootChannel) {
        m_rootChannel = newRootChannel;
    }

    /**
     * Get the root channel
     */
    public String getRootChannel() {
        return m_rootChannel;
    }

    /**
     * Inserts a new row in the database with the dataset.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @param mediaToAdd a Vector of media to add.
     */
    public void insert(CmsObject cms, CmsMasterContent content,
                       CmsMasterDataSet dataset)
        throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't write to the online project", CmsException.C_NO_ACCESS);
        }
        //int newMasterId = CmsIdGenerator.nextId(m_poolName, "CMS_MODULE_MASTER");
        CmsUUID newMasterId = new CmsUUID();
        int projectId = cms.getRequestContext().currentProject().getId();
        CmsUUID currentUserId = cms.getRequestContext().currentUser().getId();
        long currentTime = new java.util.Date().getTime();
        // filling some default-values for new dataset's
        dataset.m_masterId = newMasterId;
        dataset.m_userId = currentUserId;
        dataset.m_groupId = cms.getRequestContext().currentGroup().getId();
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
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "insert_offline");
            sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, dataset.m_mediaToAdd, new Vector(), new Vector());
            updateChannels(cms, dataset.m_masterId, dataset.m_channelToAdd, dataset.m_channelToDelete);
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * Inserts a new row in the database with the copied dataset.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @param mediaToAdd a Vector of media to add.
     * @param channelToAdd a Vector of channels to add.
     * @return int The id of the new content definition
     */
    public CmsUUID copy(CmsObject cms, CmsMasterContent content,
                       CmsMasterDataSet dataset, Vector mediaToAdd, Vector channelToAdd)
        throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't write to the online project", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't write it!
            throw new CmsException("Can't update a cd with a backup cd ", CmsException.C_NO_ACCESS);
        }
        if(!content.isWriteable()) {
            // no write access
            throw new CmsException("Not writeable", CmsException.C_NO_ACCESS);
        }
        //int newMasterId = CmsIdGenerator.nextId(m_poolName, "CMS_MODULE_MASTER");
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
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "insert_offline");
            sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, mediaToAdd, new Vector(), new Vector());
            updateChannels(cms, dataset.m_masterId, channelToAdd, new Vector());
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, null);
        }
        return newMasterId;
    }

    /**
     * Updates the lockstate in the database.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void writeLockstate(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset)
        throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't lock in the online project", CmsException.C_NO_ACCESS);
        }
        if(!content.isWriteable()) {
            // no write access
            throw new CmsException("Not writeable", CmsException.C_NO_ACCESS);
        }
        /*
        if(dataset.m_lockedBy <= -1) {
            // unlock the cd
            dataset.m_lockedBy = -1;
        } else {
        */
        if (!dataset.m_lockedBy.isNullUUID()) {
            // lock the resource into the current project
            dataset.m_lockedInProject = cms.getRequestContext().currentProject().getId();
        }

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "update_lockstate_offline");
            stmt.setString(1, dataset.m_lockedBy.toString());
            stmt.setInt(2, dataset.m_lockedInProject);
            stmt.setString(3, dataset.m_masterId.toString());
            stmt.setInt(4, content.getSubId());
            stmt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void write(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset)
        throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't write to the online project", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't write it!
            throw new CmsException("Can't update a cd with a backup cd ", CmsException.C_NO_ACCESS);
        }
        // read the lockstate
        readLockstate(dataset, content.getSubId());
        if(!dataset.m_lockedBy.equals(cms.getRequestContext().currentUser().getId())) {
            // is not locked by this user
            throw new CmsException("Not locked by this user", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_lockedInProject != dataset.m_projectId) {
            // not locked in this project
            throw new CmsException("Not locked in this project", CmsException.C_NO_ACCESS);
        }
        if(!content.isWriteable()) {
            // no write access
            throw new CmsException("Not writeable", CmsException.C_NO_ACCESS);
        }

        long currentTime = new java.util.Date().getTime();
        CmsUUID currentUserId = cms.getRequestContext().currentUser().getId();
        // updateing some values for updated dataset
        if(dataset.m_state != I_CmsConstants.C_STATE_NEW) {
            // if the state is not new then set the state to changed
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        dataset.m_lastModifiedBy = currentUserId;
        dataset.m_dateLastModified = currentTime;

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "update_offline");
            int rowcounter = sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.setString(rowcounter++, dataset.m_masterId.toString());
            stmt.setInt(rowcounter++, content.getSubId());
            stmt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, dataset.m_mediaToAdd, dataset.m_mediaToUpdate, dataset.m_mediaToDelete);
            updateChannels(cms, dataset.m_masterId, dataset.m_channelToAdd, dataset.m_channelToDelete);
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void read(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset, CmsUUID contentId)
        throws CmsException {
        if(!content.isReadable()) {
            // no read access
            throw new CmsException("Not readable", CmsException.C_NO_ACCESS);
        }
        String statement_key = "read_offline";
        String poolToUse = m_poolName;
        if(isOnlineProject(cms)) {
            statement_key = "read_online";
            poolToUse = m_onlinePoolName;
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(poolToUse);
            stmt = sqlPrepare(conn, statement_key);
            stmt.setString(1, contentId.toString());
            stmt.setInt(2, content.getSubId());
            res = stmt.executeQuery();
            if(res.next()) {
                sqlFillValues(res, cms, dataset);
            } else {
                throw new CmsException( "[" + this.getClass().getName() + ".read] no content found for CID:" + contentId + ", SID: " + content.getSubId() + ", statement: " + statement_key, CmsException.C_NOT_FOUND);
            }
            if(!checkAccess(content, false)) {
                throw new CmsException("Not readable", CmsException.C_NO_ACCESS);
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, res);
        }
    }

    /**
     * Read the lockstate from the database.
     * We nned this, because of someone has maybe stolen the lock.
     * @param dataset the dataset to read the lockstate into.
     * @param subId the subId of this cd
     */
    protected void readLockstate(CmsMasterDataSet dataset, int subId) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "read_lockstate_offline");
            stmt.setString(1, dataset.m_masterId.toString());
            stmt.setInt(2, subId);
            res = stmt.executeQuery();
            if(res.next()) {
                // update the values
                dataset.m_lockedInProject = res.getInt(1);
                dataset.m_lockedBy = new CmsUUID( res.getString(2) );
            } else {
                // no values found - this is a new row
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, res);
        }
    }

    /**
     * Reads all media from the database.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @return a Vector of media objects.
     */
    public Vector readMedia(CmsObject cms, CmsMasterContent content)
        throws CmsException {
        if(!content.isReadable()) {
            // no read access
            throw new CmsException("Not readable", CmsException.C_NO_ACCESS);
        }
        Vector retValue = new Vector();
        String statement_key = "read_media_offline";
        String poolToUse = m_poolName;
        if(isOnlineProject(cms)) {
            statement_key = "read_media_online";
            poolToUse = m_onlinePoolName;
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(poolToUse);
            stmt = sqlPrepare(conn, statement_key);
            stmt.setString(1, content.getId().toString());
            res = stmt.executeQuery();
            while(res.next()) {
                int i = 1;
                retValue.add(new CmsMasterMedia (
                    res.getInt(i++),
                    new CmsUUID( res.getString(i++) ),
                    res.getInt(i++),
                    res.getInt(i++),
                    res.getInt(i++),
                    res.getInt(i++),
                    res.getString(i++),
                    res.getInt(i++),
                    res.getString(i++),
                    res.getString(i++),
                    res.getString(i++),
                    res.getBytes(i++)
                ));
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, res);
        }
        return retValue;
    }

    /**
     * Reads all channels from the database.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @return a Vector of channel names.
     */
    public Vector readChannels(CmsObject cms, CmsMasterContent content)
        throws CmsException {
        if(!content.isReadable()) {
            // no read access
            throw new CmsException("Not readable", CmsException.C_NO_ACCESS);
        }
        Vector retValue = new Vector();
        String statement_key = "read_channel_offline";
        String poolToUse = m_poolName;
        if(isOnlineProject(cms)) {
            statement_key = "read_channel_online";
            poolToUse = m_onlinePoolName;
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(poolToUse);
            stmt = sqlPrepare(conn, statement_key);
            stmt.setString(1, content.getId().toString());
            res = stmt.executeQuery();
            while(res.next()) {
                // get the channel id
                int channeldId = res.getInt(1);
                // read the resource by property "channelid"
                cms.setContextToCos();
                Vector resources = new Vector();
                try {
                    resources = cms.getResourcesWithProperty(I_CmsConstants.C_PROPERTY_CHANNELID, channeldId+"", I_CmsConstants.C_TYPE_FOLDER);
                } catch(CmsException exc) {
                    // ignore the exception - switch to next channel
                }
                cms.setContextToVfs();
                if(resources.size() >= 1) {
                    // add the name of the channel to the ret-value
                    CmsResource resource = (CmsResource)resources.get(0);
                    if (resource.getState() != CmsResource.C_STATE_DELETED) {
                        retValue.add(resource.getAbsolutePath());
                    }
                }
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, res);
        }
        return retValue;
    }

    /**
     * Reads all content definitions of a given channel
     * @param cms the CmsObject to get access to cms-ressources.
     * @param channelId the id of the channel.
     * @return Vector The datasets of the contentdefinitions in the channel
     */
    public Vector readAllByChannel(CmsObject cms, int channelId, int subId)
        throws CmsException {
        Vector theDataSets = new Vector();
        String statement_key = "readallbychannel_offline";
        String poolToUse = m_poolName;
        if(isOnlineProject(cms)) {
            statement_key = "readallbychannel_online";
            poolToUse = m_onlinePoolName;
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(poolToUse);
            stmt = sqlPrepare(conn, statement_key);
            stmt.setInt(1, subId);
            stmt.setInt(2, channelId);
            res = stmt.executeQuery();
            while(res.next()) {
                CmsMasterDataSet dataset = new CmsMasterDataSet();
                sqlFillValues(res, cms, dataset);
                theDataSets.add(dataset);
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, res);
        }
        return theDataSets;
    }

    /**
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void delete(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset)
        throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't delete from the online project", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't delete it!
            throw new CmsException("Can't delete a backup cd ", CmsException.C_NO_ACCESS);
        }
        // read the lockstate
        readLockstate(dataset, content.getSubId());
        if((!dataset.m_lockedBy.equals(cms.getRequestContext().currentUser().getId()))) {
            // is not locked by this user
            throw new CmsException("Not locked by this user", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_lockedInProject != dataset.m_projectId) {
            // not locked in this project
            throw new CmsException("Not locked in this project", CmsException.C_NO_ACCESS);
        }
        if(!content.isWriteable()) {
            // no write access
            throw new CmsException("Not writeable", CmsException.C_NO_ACCESS);
        }

        if(dataset.m_state == I_CmsConstants.C_STATE_NEW) {
            // this is a new line in this project and can be deleted
            String statement_key = "delete_offline";
            PreparedStatement stmt = null;
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(m_poolName);
                stmt = sqlPrepare(conn, statement_key);
                stmt.setString(1, dataset.m_masterId.toString());
                stmt.setInt(2, content.getSubId());
                if(stmt.executeUpdate() != 1) {
                    // no line deleted - row wasn't found
                    throw new CmsException("Row not found: " + dataset.m_masterId + " " + content.getSubId(), CmsException.C_NOT_FOUND);
                }
                // after deleting the row, we have to delete media and channel rows
                deleteAllMedia(dataset.m_masterId);
                deleteAllChannels(dataset.m_masterId);
            } catch(SQLException exc) {
                throw new CmsException(CmsException.C_SQL_ERROR, exc);
            } finally {
                sqlClose(conn, stmt, null);
            }
        } else {
            // set state to deleted and update the line
            dataset.m_state = I_CmsConstants.C_STATE_DELETED;
            dataset.m_lockedBy = CmsUUID.getNullUUID();
            PreparedStatement stmt = null;
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(m_poolName);
                stmt = sqlPrepare(conn, "update_offline");
                int rowcounter = sqlFillValues(stmt, content.getSubId(), dataset);
                stmt.setString(rowcounter++, dataset.m_masterId.toString());
                stmt.setInt(rowcounter++, content.getSubId());
                stmt.executeUpdate();
            } catch(SQLException exc) {
                throw new CmsException(CmsException.C_SQL_ERROR, exc);
            } finally {
                sqlClose(conn, stmt, null);
            }
        }
    }

    /**
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void undelete(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset)
        throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't undelete from the online project", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't delete it!
            throw new CmsException("Can't undelete a backup cd ", CmsException.C_NO_ACCESS);
        }
        if(!content.isWriteable()) {
            // no write access
            throw new CmsException("Not writeable", CmsException.C_NO_ACCESS);
        }
        // set state to deleted and update the line
        dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        dataset.m_lockedBy = cms.getRequestContext().currentUser().getId();
        dataset.m_lockedInProject = cms.getRequestContext().currentProject().getId();
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "update_offline");
            int rowcounter = sqlFillValues(stmt, content.getSubId(), dataset);
            stmt.setString(rowcounter++, dataset.m_masterId.toString());
            stmt.setInt(rowcounter++, content.getSubId());
            stmt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * Changes the perrmissions of the Master
     *
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void changePermissions(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset) throws CmsException {
        if(isOnlineProject(cms)) {
            // this is the onlineproject - don't write into this project directly
            throw new CmsException("Can't change permissions in online project", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_versionId != I_CmsConstants.C_UNKNOWN_ID) {
            // this is not the online row - it was read from history
            // don't delete it!
            throw new CmsException("Can't change permissions of a backup cd ", CmsException.C_NO_ACCESS);
        }
        // read the lockstate
        readLockstate(dataset, content.getSubId());
        if (!dataset.m_lockedBy.equals(cms.getRequestContext().currentUser().getId())) {
            // is not locked by this user
            throw new CmsException("Not locked by this user", CmsException.C_NO_ACCESS);
        }
        if(dataset.m_lockedInProject != dataset.m_projectId) {
            // not locked in this project
            throw new CmsException("Not locked in this project", CmsException.C_NO_ACCESS);
        }
        if(!content.isWriteable()) {
            // no write access
            throw new CmsException("Not writeable", CmsException.C_NO_ACCESS);
        }
        if (dataset.m_state != I_CmsConstants.C_STATE_NEW){
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        dataset.m_dateLastModified = System.currentTimeMillis();
        dataset.m_lastModifiedBy = cms.getRequestContext().currentUser().getId();
        // update the line
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, "update_permissions_offline");
            stmt.setString(1, dataset.m_userId.toString());
            stmt.setString(2, dataset.m_groupId.toString());
            stmt.setInt(3, dataset.m_accessFlags);
            stmt.setInt(4, dataset.m_state);
            stmt.setString(5, dataset.m_lastModifiedBy.toString());
            stmt.setTimestamp(6, new Timestamp(dataset.m_dateLastModified));
            stmt.setString(7, dataset.m_masterId.toString());
            stmt.setInt(8, content.getSubId());
            stmt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * Returns a string representation of this instance.
     * This can be used for debugging.
     * @return the string representation of this instance.
     */
    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append(this.getClass().getName() + "{");
        returnValue.append("m_poolName="+m_poolName+";");
        returnValue.append("m_backupPoolName="+m_backupPoolName+";");
        returnValue.append("m_onlinePoolName="+m_onlinePoolName+";");
        returnValue.append("m_queries="+m_queries + "}");
        return returnValue.toString();
    }

    /**
     * Loads recursively all query.properties from all packages of the
     * superclasses. This method calls recuresively itself with the superclass
     * (if exists) as parameter.
     *
     * @param the currentClass of the dbaccess module.
     */
    private void loadQueries(Class currentClass) {
        // creates the queryFilenam from the packagename and
        // filename query.properties
        String className = currentClass.getName();
        String queryFilename = className.substring(0, className.lastIndexOf('.'));
        queryFilename = queryFilename.replace('.','/') + "/query.properties";
        // gets the superclass and calls this method recursively
        Class superClass = currentClass.getSuperclass();
        if(superClass != java.lang.Object.class) {
            loadQueries(superClass);
        }
        try {
            // load the queries. Entries of the most recent class will overwrite
            // entries of superclasses.
            m_queries.load(getClass().getClassLoader().getResourceAsStream(queryFilename));
        } catch(Exception exc) {
            // no query.properties found - write to logstream.
            if(CmsBase.isLogging()) {
                CmsBase.log(CmsBase.C_MODULE_DEBUG, "[CmsDbAccess] Couldn't load " + queryFilename + " errormessage: " + exc.getMessage());
            }
        }
    }

    /**
     * Combines the queries in the properties to complete quereis. Therefor a
     * replacement is needed: The follwing Strings will be replaces
     * automatically by the corresponding property-entrys:
     * ${property_key}
     */
    private void combineQueries() {
        Enumeration keys = m_queries.keys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            // replace while there has benn repacements performaend
            while(replace(key));
        }
    }

    /**
     * Computes one run of the replacement for one query.
     * Stores the new value into m_queries.
     * @param key the key for the query to compute.
     * @return if in this run replacements are done.
     */
    private boolean replace(String key) {
        boolean retValue = false;
        String value = m_queries.getProperty(key);
        String newValue = new String();
        int index = 0;
        int lastIndex = 0;
        // run as long as there are "${" strings found
        while(index != -1) {
            index = value.indexOf("${", lastIndex);
            if(index != -1) {
                retValue = true;
                int nextIndex = value.indexOf('}', index);
                if(nextIndex != -1) {
                    // get the replacer key
                    String replacer = value.substring(index+2, nextIndex);
                    // copy the first part of the query
                    newValue += value.substring(lastIndex, index);
                    // copy the replcement-value
                    newValue += m_queries.getProperty(replacer, "");
                    // set up lastindex
                    lastIndex = nextIndex+1;
                } else {
                    // no key found, just copy the query-part
                    newValue += value.substring(lastIndex, index+2);
                    // set up lastindex
                    lastIndex = index+2;
                }
            } else {
                // end of the string, copy the tail into new value
                newValue += value.substring(lastIndex);
            }
        }
        // put back the new query to the queries
        m_queries.put(key, newValue);
        // returns true, if replacements were made in this run
        return retValue;
    }

    /**
     * Creates a new connection and prepares a stement.
     * @param con the Connection to use.
     * @param queryKey the key for the query to use. The query will be get
     * by m_queries.getParameter(key)
     */
    protected PreparedStatement sqlPrepare(Connection con, String queryKey) throws SQLException {
        return con.prepareStatement(m_queries.getProperty(queryKey, ""));
    }

    /**
     * Creates a new connection and prepares a stement.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param con the Connection to use.
     * @param queryKey the key for the query to use. The query will be get
     * by m_queries.getParameter(key)
     */
    protected PreparedStatement sqlPrepare(CmsObject cms, Connection con, String queryKey) throws SQLException {
        return this.sqlPrepare(cms, con, queryKey, null);
    }
    
    /**
     * Replaces in a SQL statement $XXX tokens by strings and returns a prepared statement.
     * 
     * @param cms the current user's CmsObject instance
     * @param con the JDBC connection
     * @param queryKey the name of the SQL statement (in query.properties)
     * @param optionalSqlTokens a HashMap with optional SQL tokens to be replaced in the SQL statement
     * @return a new PreparedStatement
     * @throws SQLException
     */
    protected PreparedStatement sqlPrepare(CmsObject cms, Connection con, String queryKey, HashMap optionalSqlTokens) throws SQLException {
        String statement = null;
        String moduleMaster = null;
        String channelRel = null;
        String media = null;

        // get the string of the SQL statement
        statement = m_queries.getProperty(queryKey, "");

        // choose the right tables depending on the online/offline project
        if (isOnlineProject(cms)) {
            moduleMaster = "CMS_MODULE_ONLINE_MASTER";
            channelRel = "CMS_MODULE_ONLINE_CHANNEL_REL";
            media = "CMS_MODULE_ONLINE_MEDIA";
        } else {
            moduleMaster = "CMS_MODULE_MASTER";
            channelRel = "CMS_MODULE_CHANNEL_REL";
            media = "CMS_MODULE_MEDIA";
        }

        // replace in the SQL statement the table names
        statement = Utils.replace(statement, "$CMS_MODULE_MASTER", moduleMaster);
        statement = Utils.replace(statement, "$CMS_MODULE_CHANNEL_REL", channelRel);
        statement = Utils.replace(statement, "$CMS_MODULE_MEDIA", media);

        // replace in the SQL statement further optional SQL tokens
        if (optionalSqlTokens != null) {
            Iterator optionalSqlKeys = optionalSqlTokens.keySet().iterator();
            while (optionalSqlKeys.hasNext()) {
                String currentKey = (String) optionalSqlKeys.next();
                String currentValue = (String) optionalSqlTokens.get(currentKey);
                statement = Utils.replace(statement, currentKey, currentValue);
            }
        }

        //System.err.println(statement);

        return con.prepareStatement(statement);
    }

    /**
     * Closes all sql ressources.
     * @param con the Connection to close.
     * @param stmnt the SqlStatement to close.
     * @param resr the ResultSet to close.
     */
    protected void sqlClose(Connection con, Statement stmnt, ResultSet res) {
        try {
            res.close();
        } catch (Exception e) {
            // ignore the exception
        } finally {
            res = null;
        }

        try {
            stmnt.close();
        } catch (Exception e) {
            // ignore the exception
        } finally {
            stmnt = null;
        }

        try {
            con.close();
        } catch (Exception e) {
            // ignore the exception
        } finally {
            con = null;
        }
    }

    /**
     * Inserts all values to the statement for insert and update.
     * @param stmnt the Statement to fill the values to.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param subId the subid of this module.
     * @param dataset the set of data for this contentdefinition.
     * @return the actual rowcounter.
     */
    protected int sqlFillValues(PreparedStatement stmnt, int subId, CmsMasterDataSet dataset)
        throws SQLException {
        // columncounter
        int i = 1;
        //// COREDATA ////
        stmnt.setString(i++,dataset.m_masterId.toString());
        stmnt.setInt(i++,subId);
        stmnt.setString(i++,dataset.m_userId.toString());
        stmnt.setString(i++,dataset.m_groupId.toString());
        stmnt.setInt(i++,dataset.m_lockedInProject);
        stmnt.setInt(i++,dataset.m_accessFlags);
        stmnt.setInt(i++,dataset.m_state);
        stmnt.setString(i++,dataset.m_lockedBy.toString());
        stmnt.setString(i++,dataset.m_lastModifiedBy.toString());
        stmnt.setTimestamp(i++,new Timestamp(dataset.m_dateCreated));
        stmnt.setTimestamp(i++,new Timestamp(dataset.m_dateLastModified));
        //// USERDATA ////
        stmnt.setTimestamp(i++,new Timestamp(dataset.m_publicationDate));
        stmnt.setTimestamp(i++,new Timestamp(dataset.m_purgeDate));
        stmnt.setInt(i++,dataset.m_flags);
        stmnt.setInt(i++,dataset.m_feedId);
        stmnt.setInt(i++,dataset.m_feedReference);
        if(dataset.m_feedFilename == null){
            stmnt.setNull(i++,Types.VARCHAR);
        } else {
            stmnt.setString(i++,dataset.m_feedFilename);
        }
        if(dataset.m_title == null){
            stmnt.setNull(i++,Types.VARCHAR);
        } else {
            stmnt.setString(i++,dataset.m_title);
        }
        //// GENERIC DATA ////
        i = sqlSetTextArray(stmnt, dataset.m_dataBig, i);
        i = sqlSetTextArray(stmnt, dataset.m_dataMedium, i);
        i = sqlSetTextArray(stmnt, dataset.m_dataSmall, i);
        i = sqlSetIntArray(stmnt, dataset.m_dataInt, i);
        i = sqlSetIntArray(stmnt, dataset.m_dataReference, i);
        i = sqlSetDateArray(stmnt, dataset.m_dataDate, i);
        return i;
    }

    /**
     * Inserts all values to the statement for insert and update.
     * @param res the Resultset read the values from.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @return the actual rowcounter.
     */
    protected int sqlFillValues(ResultSet res, CmsObject cms, CmsMasterDataSet dataset)
        throws SQLException {
        // columncounter
        int i = 1;
        //// COREDATA ////
        dataset.m_masterId = new CmsUUID( res.getString(i++) );
        res.getInt(i++); // we don't have to store the sub-id
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
        dataset.m_title = res.getString(i++);;
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
     * Computes the correct project id based on the channels.
     */
    protected int computeProjectId(CmsObject cms, CmsMasterDataSet dataset) throws SQLException  {
        int onlineProjectId = I_CmsConstants.C_UNKNOWN_ID;
        int offlineProjectId = I_CmsConstants.C_UNKNOWN_ID;

        try {
            offlineProjectId = cms.getRequestContext().currentProject().getId();
            onlineProjectId = cms.onlineProject().getId();
        } catch(CmsException exc) {
            // ignore the exception
        }

        if(!isOnlineProject(cms)) {
            // this is a offline project -> compute if we have to return the
            // online project id or the offline project id

            // the owner and the administrtor has always access
            try {
                if( (cms.getRequestContext().currentUser().getId().equals(dataset.m_userId)) ||
                     cms.isAdmin()) {
                     return offlineProjectId;
                }
            } catch(CmsException exc) {
                // ignore the exception -> we are not admin
            }

            String statement_key = "read_channel_offline";
            String poolToUse = m_poolName;

            PreparedStatement stmt = null;
            ResultSet res = null;
            Connection conn = null;
            try {
                cms.setContextToCos();
                conn = DriverManager.getConnection(poolToUse);
                stmt = sqlPrepare(conn, statement_key);
                stmt.setString(1, dataset.m_masterId.toString());
                res = stmt.executeQuery();
                while(res.next()) {
                    // get the channel id
                    int channeldId = res.getInt(1);
                    // read the resource by property "channelid"
                    Vector resources = new Vector();
                    try {
                        resources = cms.getResourcesWithProperty(I_CmsConstants.C_PROPERTY_CHANNELID, channeldId+"", I_CmsConstants.C_TYPE_FOLDER);
                    } catch(CmsException exc) {
                        // ignore the exception - read the next one
                    }
                    if(resources.size() >= 1) {
                        int resProjectId = ((CmsResource)resources.get(0)).getProjectId();
                        if(resProjectId == offlineProjectId) {
                            // yes - we have found a chanel that belongs to
                            // the current offlineproject -> we can return the
                            // offline project id as computed project id
                            return offlineProjectId;
                        }
                    }
                }
            } finally {
                cms.setContextToVfs();
                sqlClose(conn, stmt, res);
            }
        }
        // no channel found, that belongs to the offlineproject ->
        // return the online project id.
        return onlineProjectId;
    }

    /**
     * Sets an array of strings into the stmnt.
     * @param stmnt the PreparedStatement to set the values into.
     * @param array the array of strings to set.
     * @param the columnscounter for the stmnt.
     * @return the increased columnscounter;
     */
    protected int sqlSetTextArray(PreparedStatement stmnt, String[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            if(array[j] == null) {
                stmnt.setNull(columnscounter++,Types.LONGVARCHAR);
            } else {
                stmnt.setString(columnscounter++,array[j]);
            }
        }
        return columnscounter;
    }

    /**
     * Sets an array of strings from the resultset.
     * @param res the ResultSet to get the values from.
     * @param array the array of strings to set.
     * @param the columnscounter for the res.
     * @return the increased columnscounter;
     */
    protected int sqlSetTextArray(ResultSet res, String[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            array[j] = res.getString(columnscounter++);
        }
        return columnscounter;
    }

    /**
     * Sets an array of ints into the stmnt.
     * @param stmnt the PreparedStatement to set the values into.
     * @param array the array of ints to set.
     * @param the columnscounter for the stmnt.
     * @return the increased columnscounter;
     */
    protected int sqlSetIntArray(PreparedStatement stmnt, int[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            stmnt.setInt(columnscounter++,array[j]);
        }
        return columnscounter;
    }

    /**
     * Sets an array of ints from the resultset.
     * @param res the ResultSet to get the values from.
     * @param array the array of ints to set.
     * @param the columnscounter for the res.
     * @return the increased columnscounter;
     */
    protected int sqlSetIntArray(ResultSet res, int[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            array[j] = res.getInt(columnscounter++);
        }
        return columnscounter;
    }

    /**
     * Sets an array of ints into the stmnt.
     * @param stmnt the PreparedStatement to set the values into.
     * @param array the array of longs to set.
     * @param the columnscounter for the stmnt.
     * @return the increased columnscounter;
     */
    protected int sqlSetDateArray(PreparedStatement stmnt, long[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            stmnt.setTimestamp(columnscounter++,new Timestamp(array[j]));
        }
        return columnscounter;
    }

    /**
     * Sets an array of ints from the resultset.
     * @param res the ResultSet to get the values from.
     * @param array the array of longs to set.
     * @param the columnscounter for the res.
     * @return the increased columnscounter;
     */
    protected int sqlSetDateArray(ResultSet res, long[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            array[j] = res.getTimestamp(columnscounter++).getTime();
        }
        return columnscounter;
    }

    /**
     * Returns a vector of contentdefinitions based on the sql resultset.
     * Never mind about the visible flag.
     * @param res - the ResultSet to get data-lines from.
     * @param contentDefinitionClass - the class of the cd to create new instances.
     * @param cms - the CmsObject to get access to cms-ressources.
     * @throws SqlException if nothing could be read from the resultset.
     */
    protected Vector createVectorOfCd(ResultSet res, Class contentDefinitionClass, CmsObject cms)
        throws SQLException {
        return createVectorOfCd(res, contentDefinitionClass, cms, false);
    }

    /**
     * Returns a vector of contentdefinitions based on the sql resultset.
     * @param res - the ResultSet to get data-lines from.
     * @param contentDefinitionClass - the class of the cd to create new instances.
     * @param cms - the CmsObject to get access to cms-ressources.
     * @param viewonly - decides, if only the ones that are visible should be returned
     * @throws SqlException if nothing could be read from the resultset.
     */
    protected Vector createVectorOfCd(ResultSet res, Class contentDefinitionClass, CmsObject cms, boolean viewonly)
        throws SQLException {
        Constructor constructor;
        Vector retValue = new Vector();
        try { // to get the constructor to create an empty contentDefinition
            constructor = contentDefinitionClass.getConstructor(new Class[]{CmsObject.class, CmsMasterDataSet.class});
        } catch(NoSuchMethodException exc) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                CmsBase.log(I_CmsLogChannels.C_MODULE_DEBUG, "[CmsDbAccess] Cannot locate constructor: " + exc.getMessage());
            }
            // canno't fill the vector - missing constructor
            return retValue;
        }
        while(res.next()) { // while there is data in the resultset
            CmsMasterDataSet dataset = new CmsMasterDataSet();
            try { // to invoce the constructor to get a new empty instance
                CmsMasterContent content = (CmsMasterContent)constructor.newInstance(new Object[]{cms, dataset});
                sqlFillValues(res, cms, dataset);
                // add the cd only if read (and visible) permissions are granted.
                // the visible-permissens will be checked, if viewonly is set to true
                // viewonly=true is needed for the backoffice
                if(checkAccess(content, viewonly)) {
                    retValue.add(content);
                }
            } catch(Exception exc) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                    CmsBase.log(I_CmsLogChannels.C_MODULE_DEBUG, "[CmsDbAccess] Cannot invoce constructor: " + exc.getMessage());
                }
            }
        }
        return retValue;
    }

    /**
     * Returns a vector of contentdefinitions based on the sql resultset.
     * @param datasets - the vector with the datasets.
     * @param contentDefinitionClass - the class of the cd to create new instances.
     * @param cms - the CmsObject to get access to cms-ressources.
     * @throws SqlException if nothing could be read from the resultset.
     */
    protected Vector createVectorOfCd(Vector datasets, Class contentDefinitionClass, CmsObject cms)
        throws SQLException {
        Constructor constructor;
        Vector retValue = new Vector();
        try { // to get the constructor to create an empty contentDefinition
            constructor = contentDefinitionClass.getConstructor(new Class[]{CmsObject.class, CmsMasterDataSet.class});
        } catch(NoSuchMethodException exc) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                CmsBase.log(I_CmsLogChannels.C_MODULE_DEBUG, "[CmsDbAccess] Cannot locate constructor: " + exc.getMessage());
            }
            // canno't fill the vector - missing constructor
            return retValue;
        }
        // create content definition for each dataset
        for(int i=0; i < datasets.size(); i++) {
            CmsMasterDataSet dataset = (CmsMasterDataSet)datasets.elementAt(i);
            try { // to invoce the constructor to get a new empty instance
                CmsMasterContent content = (CmsMasterContent)constructor.newInstance(new Object[]{cms, dataset});
                retValue.add(content);
            } catch(Exception exc) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                    CmsBase.log(I_CmsLogChannels.C_MODULE_DEBUG, "[CmsDbAccess] Cannot invoce constructor: " + exc.getMessage());
                }
            }
        }
        return retValue;
    }

    /**
     * Checks if read (and visible) permissions are granted.
     * the visible-permissens will be checked, if viewonly is set to true
     * viewonly=true is needed for the backoffice
     * @param cms - the CmsObject to get access to cms-ressources.
     * @param content - the cd to check.
     * @param viewonly - if set to true the v-Flag will be checked, too
     */
    protected boolean checkAccess(CmsMasterContent content, boolean viewonly) {
        if(!content.isReadable()) {
            // was not readable
            return false;
        } else if(viewonly) {
            // additional check for v-Flags
            return content.isVisible();
        } else {
            // was readable - return true
            return true;
        }
    }

    /**
     * Returns true, if this is the onlineproject
     * @param cms - the CmsObject to get access to cms-ressources.
     * @return true, if this is the onlineproject, else returns false
     */
    protected boolean isOnlineProject(CmsObject cms) {
        return cms.getRequestContext().currentProject().isOnlineProject();
    }

    /**
     * Deletes all media lines for one master.
     * @param masterId - the masterId to delete the media for
     * @throws SQLException if an sql-error occur
     */
    protected void deleteAllMedia(CmsUUID masterId) throws SQLException {
        String statement_key = "delete_all_media_offline";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, statement_key);
            stmt.setString(1, masterId.toString());
            stmt.executeUpdate();
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * Deletes all channel lines for one master.
     * @param masterId - the masterId to delete the media for
     * @throws SQLException if an sql-error occur
     */
    protected void deleteAllChannels(CmsUUID masterId) throws SQLException {
        String statement_key = "delete_all_channel_offline";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(m_poolName);
            stmt = sqlPrepare(conn, statement_key);
            stmt.setString(1, masterId.toString());
            stmt.executeUpdate();
        } finally {
            sqlClose(conn, stmt, null);
        }
    }

    /**
     * Updates the media object of a content definition.
     * 
     * @param masterId the content definition master id
     * @param mediaToAdd vector of media objects to add
     * @param mediaToUpdate vector of media objects to update 
     * @param mediaToDelete vector of media objects to delete
     * @throws SQLException
     * @throws CmsException
     */
    protected void updateMedia(CmsUUID masterId, Vector mediaToAdd,
                               Vector mediaToUpdate, Vector mediaToDelete)
        throws SQLException, CmsException {
        // add new media
        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "insert_media_offline");
            for(int i = 0; i < mediaToAdd.size(); i++) {
                CmsMasterMedia media = (CmsMasterMedia) mediaToAdd.get(i);
                media.setId(CmsIdGenerator.nextId(m_interimOfflinePoolUrl, "CMS_MODULE_MEDIA"));
                media.setMasterId(masterId);
                sqlFillValues(stmnt, media);
                stmnt.executeUpdate();
            }
        } finally {
            sqlClose(con, stmnt, null);
        }

        // update existing media
        stmnt = null;
        con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "update_media_offline");
            for(int i = 0; i < mediaToUpdate.size(); i++) {
                CmsMasterMedia media = (CmsMasterMedia) mediaToUpdate.get(i);
                media.setMasterId(masterId);
                int rowCounter = sqlFillValues(stmnt, media);
                stmnt.setInt(rowCounter++, media.getId());
                stmnt.setString(rowCounter++, masterId.toString());
                stmnt.executeUpdate();
            }
        } finally {
            sqlClose(con, stmnt, null);
        }
        // delete unneeded media
        stmnt = null;
        con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "delete_media_offline");
            for(int i = 0; i < mediaToDelete.size(); i++) {
                CmsMasterMedia media = (CmsMasterMedia) mediaToDelete.get(i);
                stmnt.setInt(1, media.getId());
                stmnt.setString(2, masterId.toString());
                stmnt.executeUpdate();
            }
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * Updates the channels of a content definition.
     * 
     * @param cms the current context object
     * @param masterId the content definition master id
     * @param channelToAdd vector of channels to add 
     * @param channelToDelete vector of channels to delete
     * @throws SQLException
     */
    protected void updateChannels(CmsObject cms, CmsUUID masterId, Vector channelToAdd,
        Vector channelToDelete) throws SQLException {
        // add new channel
        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "insert_channel_offline");
            for(int i = 0; i < channelToAdd.size(); i++) {
                try {
                    stmnt.setString(1, masterId.toString());
                    cms.setContextToCos();
                    stmnt.setInt(2, Integer.parseInt(cms.readProperty(channelToAdd.get(i)+"",
                        I_CmsConstants.C_PROPERTY_CHANNELID)));
                    cms.setContextToVfs();
                    // stmnt.setInt(2, Integer.parseInt(cms.readProperty(C_COS_PREFIX + channelToAdd.get(i),
                    //    I_CmsConstants.C_PROPERTY_CHANNELID)));
                    stmnt.executeUpdate();
                } catch(CmsException exc) {
                    // no channel found - write to logfile
                    if(CmsBase.isLogging()) {
                        CmsBase.log(CmsBase.C_MODULE_DEBUG, "[CmsDbAccess] Couldn't find channel " + channelToAdd.get(i) + " errormessage: " + exc.getMessage());
                    }
                }
            }
        } finally {
            sqlClose(con, stmnt, null);
        }

        // delete unneeded channel
        stmnt = null;
        con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "delete_channel_offline");
            for(int i = 0; i < channelToDelete.size(); i++) {
                try {
                    stmnt.setString(1, masterId.toString());
                    cms.setContextToCos();
                    stmnt.setInt(2, Integer.parseInt(cms.readProperty(channelToDelete.get(i)+"",
                         I_CmsConstants.C_PROPERTY_CHANNELID)));
                    cms.setContextToVfs();
                    // stmnt.setInt(2, Integer.parseInt(cms.readProperty(C_COS_PREFIX + channelToDelete.get(i),
                    //     I_CmsConstants.C_PROPERTY_CHANNELID)));
                    stmnt.executeUpdate();
                } catch(CmsException exc) {
                    // no channel found - write to logfile
                    if(CmsBase.isLogging()) {
                        CmsBase.log(CmsBase.C_MODULE_DEBUG, "[CmsDbAccess] Couldn't find channel " + channelToAdd.get(i) + " errormessage: " + exc.getMessage());
                    }
                }
            }
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * Fills a prepared statement with media values.
     * 
     * @param stmnt the statement to fill
     * @param media the data to fill the statement with
     * @return int the number of values set in the statement
     * @throws SQLException if data could not be set in statement
     */
    protected int sqlFillValues(PreparedStatement stmnt, CmsMasterMedia media)
        throws SQLException {
        int i = 1;
        stmnt.setInt(i++, media.getId());
        stmnt.setString(i++, media.getMasterId().toString());
        stmnt.setInt(i++, media.getPosition());
        stmnt.setInt(i++, media.getWidth());
        stmnt.setInt(i++, media.getHeight());
        stmnt.setInt(i++, media.getSize());
        stmnt.setString(i++, media.getMimetype());
        stmnt.setInt(i++, media.getType());
        stmnt.setString(i++, media.getTitle());
        stmnt.setString(i++, media.getName());
        stmnt.setString(i++, media.getDescription());
        stmnt.setBinaryStream(i++, new ByteArrayInputStream(media.getMedia()), media.getMedia().length);
        //stmnt.setBytes(i++, media.getMedia());
        return i;
    }

    /**
     * Returns a vector with all version of a master in the backup
     *
     * @param cms The CmsObject
     * @param masterId The id of the master
     * @param subId The sub_id
     * @return Vector A vector with all versions of the master
     */
    public Vector getHistory(CmsObject cms, Class contentDefinitionClass, CmsUUID masterId, int subId) throws CmsException{
        Vector retVector = new Vector();
        Vector allBackup = new Vector();
        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "read_all_backup");
            stmnt.setString(1, masterId.toString());
            stmnt.setInt(2, subId);
            // gets all versions of the master in the backup table
            res = stmnt.executeQuery();
            while(res.next()) {
                CmsMasterDataSet dataset = new CmsMasterDataSet();
                sqlFillValues(res, cms, dataset);
                dataset.m_versionId = res.getInt("VERSION_ID");
                dataset.m_userName = res.getString("USER_NAME");
                dataset.m_groupName = res.getString("GROUP_NAME");
                dataset.m_lastModifiedByName = res.getString("LASTMODIFIED_BY_NAME");
                allBackup.add(dataset);
            }
            retVector = createVectorOfCd(allBackup, contentDefinitionClass, cms);
        } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);
        } finally {
            sqlClose(con, stmnt, res);
        }
        return retVector;
    }

    /**
     * Returns the version of a master in the backup
     *
     * @param cms The CmsObject
     * @param contentDefinitionClass The class of the content definition
     * @param masterId The id of the master
     * @param subId The sub_id
     * @param versionId The version id
     * @return CmsMasterContent A content definition of the version
     */
    public CmsMasterContent getVersionFromHistory(CmsObject cms, Class contentDefinitionClass,
                                                  CmsUUID masterId, int subId, int versionId) throws CmsException{
        CmsMasterContent content = null;
        CmsMasterDataSet dataset = this.getVersionFromHistory(cms, masterId, subId, versionId);
        Constructor constructor;
        try { // to get the constructor to create an empty contentDefinition
            constructor = contentDefinitionClass.getConstructor(new Class[]{CmsObject.class, CmsMasterDataSet.class});
        } catch(NoSuchMethodException exc) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                CmsBase.log(I_CmsLogChannels.C_MODULE_DEBUG, "[CmsDbAccess] Cannot locate constructor: " + exc.getMessage());
            }
            // canno't fill the vector - missing constructor
            return content;
        }
        // create content definition for each dataset
        if (dataset != null){
            try { // to invoce the constructor to get a new empty instance
                content = (CmsMasterContent)constructor.newInstance(new Object[]{cms, dataset});
            } catch(Exception exc) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                    CmsBase.log(I_CmsLogChannels.C_MODULE_DEBUG, "[CmsDbAccess] Cannot invoce constructor: " + exc.getMessage());
                }
            }
        }
        return content;
    }

    /**
     * Returns the version of a master in the backup
     *
     * @param cms The CmsObject
     * @param masterId The id of the master
     * @param subId The sub_id
     * @param versionId The version id
     * @return Vector A vector with all versions of the master
     */
    public CmsMasterDataSet getVersionFromHistory(CmsObject cms, CmsUUID masterId, int subId, int versionId) throws CmsException{
        CmsMasterDataSet dataset = new CmsMasterDataSet();
        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "read_backup");
            stmnt.setString(1, masterId.toString());
            stmnt.setInt(2, subId);
            stmnt.setInt(3, versionId);
            // gets the master in the backup table with the given versionid
            res = stmnt.executeQuery();
            if(res.next()) {
                sqlFillValues(res, cms, dataset);
                dataset.m_versionId = res.getInt("VERSION_ID");
                dataset.m_userName = res.getString("USER_NAME");
                dataset.m_groupName = res.getString("GROUP_NAME");
                dataset.m_lastModifiedByName = res.getString("LASTMODIFIED_BY_NAME");
            } else {
                throw new CmsException("Row not found: " + masterId + " " + subId + " version " + versionId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);
        } finally {
            sqlClose(con, stmnt, res);
        }
        return dataset;
    }

    /**
     * Restores a version of the master and media from backup
     *
     * @param cms The CmsObject
     * @param content The master content
     * @param dataset The dataset of the master
     * @param versionId The version id of the master and media to restore
     */
     public void restore(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset, int versionId) throws CmsException{
        Connection con = null;
        Connection con2 = null;
        PreparedStatement stmnt = null;
        PreparedStatement stmnt2 = null;
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
        if (dataset.m_state != I_CmsConstants.C_STATE_NEW){
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        // check if the group exists
        CmsUUID groupId = CmsUUID.getNullUUID();
        try {
            groupId = cms.readGroup(backup.m_groupId).getId();
        } catch (CmsException exc){
            groupId = dataset.m_groupId;
        }
        dataset.m_groupId = groupId;
        // check if the user exists
        CmsUUID userId = CmsUUID.getNullUUID();
        try {
            userId = cms.readUser(backup.m_userId).getId();
        } catch (CmsException exc){
            userId = dataset.m_userId;
        }
        dataset.m_userId = userId;
        // write the master
        this.write(cms, content, dataset);
        // delete the media
        try {
            deleteAllMedia(dataset.m_masterId);
        } catch (SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        }
        // copy the media from backup
        try {
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "read_media_backup");
            stmnt.setString(1, dataset.m_masterId.toString());
            stmnt.setInt(2, versionId);
            res = stmnt.executeQuery();
            while (res.next()){
                int i = 1;
                CmsMasterMedia media = new CmsMasterMedia (
                                            res.getInt(i++),
                                            new CmsUUID( res.getString(i++) ),
                                            res.getInt(i++),
                                            res.getInt(i++),
                                            res.getInt(i++),
                                            res.getInt(i++),
                                            res.getString(i++),
                                            res.getInt(i++),
                                            res.getString(i++),
                                            res.getString(i++),
                                            res.getString(i++),
                                            res.getBytes(i++));
                // store the data in offline table
                try {
                    stmnt2 = null;
                    con2 = null;
                    con2 = DriverManager.getConnection(m_poolName);
                    stmnt2 = sqlPrepare(con2, "insert_media_offline");
                    sqlFillValues(stmnt2, media);
                    stmnt2.executeUpdate();
                } catch (SQLException ex){
                    throw new CmsException(CmsException.C_SQL_ERROR, ex);
                } finally {
                    sqlClose(con2, stmnt2, null);
                }
            }
        } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);
        } finally {
            sqlClose(con, stmnt, res);
        }
     }

    /**
     * Publishes a single content definition
     *
     * @param cms The CmsObject
     * @param dataset the dataset to publish.
     * @param subId the subId to publish cd's for.
     * @param contentDefinitionName the name of the contentdefinition.
     * @param enableHistory set to true if backup tables should be filled.
     * @param versionId the versionId to save in the backup tables.
     * @param publishingDate the date and time of this publishing process.
     */
    public void publishResource(CmsObject cms, CmsMasterDataSet dataset, int subId, String contentDefinitionName,
                                boolean enableHistory, int versionId, long publishingDate, Vector changedResources,
                                Vector changedModuleData) throws CmsException{
        this.publishOneLine(cms, dataset, subId, contentDefinitionName, enableHistory, versionId,
        publishingDate, changedResources,changedModuleData);
    }
    /**
     * Publishes all ressources for this project
     * Publishes all modified content definitions for this project.
     * @param cms The CmsObject
     * @param enableHistory set to true if backup tables should be filled.
     * @param projectId the Project that should be published.
     * @param versionId the versionId to save in the backup tables.
     * @param publishingDate the date and time of this publishing process.
     * @param subId the subId to publish cd's for.
     * @param contentDefinitionName the name of the contentdefinition.
     * @param changedRessources a Vector of Ressources that were changed by this
     * publishing process.
     * @param changedModuleData a Vector of Ressource that were changed by this
     * publishing process. New published data will be add to this Vector to
     * return it.
     */
    public void publishProject(CmsObject cms, boolean enableHistory,
        int projectId, int versionId, long publishingDate, int subId,
        String contentDefinitionName, Vector changedRessources,
        Vector changedModuleData) throws CmsException {

        String statement_key = "read_all_for_publish";
        String poolToUse = m_poolName;

        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(poolToUse);
            stmnt = sqlPrepare(con, statement_key);
            stmnt.setInt(1, subId);
            stmnt.setInt(2, projectId);
            stmnt.setInt(3, I_CmsConstants.C_STATE_UNCHANGED);
            // gets all ressources that are changed int this project
            // and that belongs to this subId
            res = stmnt.executeQuery();
            while(res.next()) {
                // create a new dataset to fill the values
                CmsMasterDataSet dataset = new CmsMasterDataSet();
                // fill the values to the dataset
                sqlFillValues(res, cms, dataset);
                publishOneLine(cms, dataset, subId, contentDefinitionName,
                    enableHistory, versionId, publishingDate, changedRessources,
                    changedModuleData);
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }
    }

    /**
     * @param cms The CmsObject
     * @param dataset the dataset to publish.
     * @param subId the subId to publish cd's for.
     * @param contentDefinitionName the name of the contentdefinition.
     * @param enableHistory set to true if backup tables should be filled.
     * @param versionId the versionId to save in the backup tables.
     * @param publishingDate the date and time of this publishing process.
     * @param changedRessources a Vector of Ressources that were changed by this
     * publishing process.
     * @param changedModuleData a Vector of Ressource that were changed by this
     * publishing process. New published data will be add to this Vector to
     * return it.
     */
    protected void publishOneLine(CmsObject cms, CmsMasterDataSet dataset,
        int subId, String contentDefinitionName, boolean enableHistory,
        int versionId, long publishingDate, Vector changedRessources,
        Vector changedModuleData) throws CmsException {
        
        try {
           Class.forName(contentDefinitionName).getMethod("beforePublish",
                new Class[] {CmsObject.class, Boolean.class, Integer.class, Integer.class,
                Long.class, Vector.class, Vector.class, CmsMasterDataSet.class}).invoke(null, new Object[] {cms,
                new Boolean(enableHistory), new Integer(subId), new Integer(versionId), new Long(publishingDate),
                changedRessources, changedModuleData, dataset});   
        }
        catch (Exception e) {
            CmsBase.log(CmsBase.C_MODULE_DEBUG, "[CmsDbAccess] error calling method beforePublish in class " + contentDefinitionName );
        }

        // backup the data
        if(enableHistory) {
            // stroe the creationdate, because it will be set to publishingdate
            // with this method.
            long backupCreationDate = dataset.m_dateCreated;
            publishBackupData(cms, dataset, subId, versionId, publishingDate);
            // restore the creationdate to the correct one
            dataset.m_dateCreated = backupCreationDate;
        }

        // delete the online data
        publishDeleteData(dataset.m_masterId, subId, "online");

        if(dataset.m_state == I_CmsConstants.C_STATE_DELETED) {
            // delete the data from offline
            // the state was DELETED
            publishDeleteData(dataset.m_masterId, subId, "offline");
        } else {
            // copy the data from offline to online
            // the state was NEW or CHANGED
            publishCopyData(dataset, subId);
        }

        // now update state, lockstate and projectId in offline
        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "update_state_offline");
            stmnt.setInt(1, I_CmsConstants.C_STATE_UNCHANGED);
            stmnt.setString(2, CmsUUID.getNullUUID().toString());
            stmnt.setString(3, dataset.m_masterId.toString());
            stmnt.setInt(4, subId);
            stmnt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }

        // update changedModuleData Vector
        changedModuleData.add(cms.getSiteName() + CmsObject.C_ROOTNAME_COS + "/"+
            contentDefinitionName +"/"+dataset.m_masterId);
    }

    /**
     * @todo: add description here
     */
    protected void publishDeleteData(CmsUUID masterId, int subId, String table) throws CmsException {
        PreparedStatement stmnt = null;
        Connection con = null;
        // delete channel relation
        try {
            con = DriverManager.getConnection(m_onlinePoolName);
            stmnt = sqlPrepare(con, "delete_all_channel_" + table);
            stmnt.setString(1, masterId.toString());
            stmnt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
        // delete media
        try {
            con = null;
            stmnt = null;
            con = DriverManager.getConnection(m_onlinePoolName);
            stmnt = sqlPrepare(con, "delete_all_media_" + table);
            stmnt.setString(1, masterId.toString());
            stmnt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
        // delete the row
        try {
            stmnt = null;
            con = null;
            con = DriverManager.getConnection(m_onlinePoolName);
            stmnt = sqlPrepare(con, "delete_" + table);
            stmnt.setString(1, masterId.toString());
            stmnt.setInt(2, subId);
            stmnt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
   }

    /**
     * @todo: add description here
     */
    protected void publishCopyData(CmsMasterDataSet dataset, int subId ) throws CmsException {
        PreparedStatement stmnt = null;
        PreparedStatement stmnt2 = null;
        ResultSet res = null;
        Connection con = null;
        Connection con2 = null;
        CmsUUID masterId = dataset.m_masterId;
        
        // copy the row
        try {
            stmnt = null;
            con = DriverManager.getConnection(m_onlinePoolName);
            stmnt = sqlPrepare(con, "insert_online");
            // correct the data in the dataset
            dataset.m_projectId = I_CmsConstants.C_PROJECT_ONLINE_ID;
            dataset.m_lockedInProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
            dataset.m_state = I_CmsConstants.C_STATE_UNCHANGED;
            dataset.m_lockedBy = CmsUUID.getNullUUID();
            sqlFillValues(stmnt, subId, dataset);
            stmnt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
        // copy media
        try {
            // read all media of master from offline
            stmnt = null;
            res = null;
            con = null;
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "read_media_offline");
            stmnt.setString(1, masterId.toString());
            res = stmnt.executeQuery();
            while(res.next()) {
                // create a new dataset to fill the values
                int i = 1;
                CmsMasterMedia mediaset = new CmsMasterMedia (
                                                res.getInt(i++),
                                                new CmsUUID( res.getString(i++) ),
                                                res.getInt(i++),
                                                res.getInt(i++),
                                                res.getInt(i++),
                                                res.getInt(i++),
                                                res.getString(i++),
                                                res.getInt(i++),
                                                res.getString(i++),
                                                res.getString(i++),
                                                res.getString(i++),
                                                res.getBytes(i++));
                // insert media of master into online
                try {
                    stmnt2 = null;
                    con2 = null;
                    con2 = DriverManager.getConnection(m_onlinePoolName);
                    stmnt2 = sqlPrepare(con2, "insert_media_online");
                    sqlFillValues(stmnt2, mediaset);
                    stmnt2.executeUpdate();
                } catch(SQLException ex){
                    throw ex;
                } finally {
                    sqlClose(con2, stmnt2, null);
                }
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }

        // copy channel relation
        try {
            stmnt = null;
            res = null;
            con = null;
            con = DriverManager.getConnection(m_poolName);
            // read all channel relations for master from offline
            stmnt = sqlPrepare(con, "read_channel_offline");
            stmnt.setString(1, masterId.toString());
            res = stmnt.executeQuery();
            while (res.next()){
                // insert all channel relations for master into online
                try {
                    stmnt2 = null;
                    con2 = null;
                    con2 = DriverManager.getConnection(m_onlinePoolName);
                    stmnt2 = sqlPrepare(con2, "insert_channel_online");
                    stmnt2.setString(1, masterId.toString());
                    stmnt2.setInt(2, res.getInt(1));
                    stmnt2.executeUpdate();
                } catch (SQLException ex){
                    throw ex;
                } finally {
                    sqlClose(con2, stmnt2, null);
                }
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }
    }

    /**
     * @todo: add description here
     */
    protected void publishBackupData(CmsObject cms, CmsMasterDataSet dataset, int subId,
                                     int versionId, long publishDate ) throws CmsException {
        PreparedStatement stmnt = null;
        PreparedStatement stmnt2 = null;
        ResultSet res = null;
        Connection con = null;
        Connection con2 = null;
        CmsUUID masterId = dataset.m_masterId;
        
        // copy the row
        try {
            stmnt = null;
            con = null;
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "insert_backup");
            // correct the data in the dataset
            dataset.m_lockedBy = CmsUUID.getNullUUID();
            dataset.m_dateCreated = publishDate;
            // get the name of the owner
            String ownerName = "";
            try {
                CmsUser owner = cms.readUser(dataset.m_userId);
                ownerName = owner.getName()+" "+owner.getFirstname()+" "+owner.getLastname();
            } catch (CmsException ex){
                ownerName = "";
            }
            // get the name of the group
            String groupName = "";
            try {
                CmsGroup group = cms.readGroup(dataset.m_groupId);
                groupName = group.getName();
            } catch (CmsException ex){
                groupName = "";
            }
            // get the name of the user who has modified the resource
            String userName = "";
            try {
                CmsUser user = cms.readUser(dataset.m_lastModifiedBy);
                userName = user.getName()+" "+user.getFirstname()+" "+user.getLastname();
            } catch (CmsException ex){
                userName = "";
            }
            int lastId = sqlFillValues(stmnt, subId, dataset);
            // set version
            stmnt.setInt(lastId++, versionId);
            stmnt.setString(lastId++, ownerName);
            stmnt.setString(lastId++, groupName);
            stmnt.setString(lastId++, userName);
            stmnt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
        // copy media
        try {
            // read all media of master from offline
            stmnt = null;
            res = null;
            con = null;
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "read_media_offline");
            stmnt.setString(1, masterId.toString());
            res = stmnt.executeQuery();
            while(res.next()) {
                // create a new dataset to fill the values
                int i = 1;
                CmsMasterMedia mediaset = new CmsMasterMedia (
                                                res.getInt(i++),
                                                new CmsUUID( res.getString(i++) ),
                                                res.getInt(i++),
                                                res.getInt(i++),
                                                res.getInt(i++),
                                                res.getInt(i++),
                                                res.getString(i++),
                                                res.getInt(i++),
                                                res.getString(i++),
                                                res.getString(i++),
                                                res.getString(i++),
                                                res.getBytes(i++));
                // insert media of master into backup
                try {
                    con2 = null;
                    stmnt2 = null;
                    con2 = DriverManager.getConnection(m_backupPoolName);
                    stmnt2 = sqlPrepare(con2, "insert_media_backup");
                    int lastId = sqlFillValues(stmnt2, mediaset);
                    stmnt2.setInt(lastId, versionId);
                    stmnt2.executeUpdate();
                } catch(SQLException ex){
                    throw ex;
                } finally {
                    sqlClose(con2, stmnt2, null);
                }
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }
    }

    /**
     * Returns the correct connection. If this is the onlineproject it returns
     * the online connection, else it returns the offline connection.
     * @param cms the CmsObject to get access to cms-ressources.
     * @return the SQLConnection.
     * @throws SQLException if the connection can not be returned.
     */
    public Connection getConnection(CmsObject cms) throws SQLException {
        String poolToUse;
        if(isOnlineProject(cms)) {
            poolToUse = m_onlinePoolName;
        } else {
            poolToUse = m_poolName;
        }
        return DriverManager.getConnection(poolToUse);
    }
}