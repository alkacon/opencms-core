/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/genericsql/Attic/CmsDbAccess.java,v $
 * Author : $Author: e.falkenhan $
 * Date   : $Date: 2001/11/08 15:12:41 $
 * Version: $Revision: 1.7 $
 * Release: $Name:  $
 *
 * Copyright (c) 2000 Framfab Deutschland ag.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Framfab.
 * In order to use this source code, you need written permission from Framfab.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * FRAMFAB MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. FRAMFAB SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.opencms.defaults.master.genericsql;

import java.sql.*;
import java.util.*;
import java.io.IOException;
import java.lang.reflect.*;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.boot.CmsBase;
import com.opencms.file.*;
import com.opencms.dbpool.CmsIdGenerator;
import com.opencms.defaults.master.*;
import com.opencms.boot.*;

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

    /**
     * Make this constructor private, so noone cann call the default constructor.
     */
    private CmsDbAccess() {
    }

    /**
     * Constructs a new DbAccessObject.
     * @param poolName the pool to access offline ressources.
     * @param onlinePoolName the pool to access the online ressources.
     * @param backupPoolName the pool to access the backup ressources.
     */
    public CmsDbAccess(String poolName, String onlinePoolName, String backupPoolName) {
        m_poolName = poolName;
        m_onlinePoolName = onlinePoolName;
        m_backupPoolName = backupPoolName;
        m_queries = new Properties();
        // collect all query.properties in all packages of superclasses
        loadQueries(getClass());
        combineQueries();
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
        int newMasterId = CmsIdGenerator.nextId(m_poolName, "CMS_MODULE_MASTER");
        int projectId = cms.getRequestContext().currentProject().getId();
        int currentUserId = cms.getRequestContext().currentUser().getId();
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

        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "insert_offline");
            sqlFillValues(stmnt, content.getSubId(), dataset);
            stmnt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, dataset.m_mediaToAdd, new Vector(), new Vector());
            updateChannels(cms, dataset.m_masterId, dataset.m_channelToAdd, dataset.m_channelToDelete);
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * Updates the lockstet in the database.
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
        if(dataset.m_lockedBy <= -1) {
            // unlock the cd
            dataset.m_lockedBy = -1;
        } else {
            // lock the ressource into the current project
            dataset.m_lockedInProject = cms.getRequestContext().currentProject().getId();
        }

        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "update_lockstate_offline");
            stmnt.setInt(1, dataset.m_lockedBy);
            stmnt.setInt(2, dataset.m_lockedInProject);
            stmnt.setInt(3, dataset.m_masterId);
            stmnt.setInt(4, content.getSubId());
            stmnt.executeUpdate();
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
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
        if((dataset.m_lockedBy != cms.getRequestContext().currentUser().getId())) {
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
        int currentUserId = cms.getRequestContext().currentUser().getId();
        // updateing some values for updated dataset
        if(dataset.m_state != I_CmsConstants.C_STATE_NEW) {
            // if the state is not new then set the state to changed
            dataset.m_state = I_CmsConstants.C_STATE_CHANGED;
        }
        dataset.m_lastModifiedBy = currentUserId;
        dataset.m_dateLastModified = currentTime;

        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "update_offline");
            int rowcounter = sqlFillValues(stmnt, content.getSubId(), dataset);
            stmnt.setInt(rowcounter++, dataset.m_masterId);
            stmnt.setInt(rowcounter++, content.getSubId());
            stmnt.executeUpdate();
            // after inserting the row, we have to update media and channel tables
            updateMedia(dataset.m_masterId, dataset.m_mediaToAdd, dataset.m_mediaToUpdate, dataset.m_mediaToDelete);
            updateChannels(cms, dataset.m_masterId, dataset.m_channelToAdd, dataset.m_channelToDelete);
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     */
    public void read(CmsObject cms, CmsMasterContent content, CmsMasterDataSet dataset, int masterId)
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

        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(poolToUse);
            stmnt = sqlPrepare(con, statement_key);
            stmnt.setInt(1, masterId);
            stmnt.setInt(2, content.getSubId());
            res = stmnt.executeQuery();
            if(res.next()) {
                sqlFillValues(res, cms, dataset);
            } else {
                throw new CmsException("Row not found: " + masterId + " " + content.getSubId(), CmsException.C_NOT_FOUND);
            }
            if(!checkAccess(cms, content, false)) {
                throw new CmsException("Not readable", CmsException.C_NO_ACCESS);
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }
    }

    /**
     * Read the lockstate from the database.
     * We nned this, because of someone has maybe stolen the lock.
     * @param dataset the dataset to read the lockstate into.
     * @param subId the subId of this cd
     */
    protected void readLockstate(CmsMasterDataSet dataset, int subId) throws CmsException {
        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "read_lockstate_offline");
            stmnt.setInt(1, dataset.m_masterId);
            stmnt.setInt(2, subId);
            res = stmnt.executeQuery();
            if(res.next()) {
                // update the values
                dataset.m_lockedInProject = res.getInt(1);
                dataset.m_lockedBy = res.getInt(2);
            } else {
                // no values found - this is a new row
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }
    }

    /**
     * Reads all media from the database.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @returns a Vector of media objects.
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

        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(poolToUse);
            stmnt = sqlPrepare(con, statement_key);
            stmnt.setInt(1, content.getId());
            res = stmnt.executeQuery();
            while(res.next()) {
                int i = 1;
                retValue.add(new CmsMasterMedia (
                    res.getInt(i++),
                    res.getInt(i++),
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
            sqlClose(con, stmnt, res);
        }
        return retValue;
    }

    /**
     * Reads all channels from the database.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @returns a Vector of channel names.
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

        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(poolToUse);
            stmnt = sqlPrepare(con, statement_key);
            stmnt.setInt(1, content.getId());
            res = stmnt.executeQuery();
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
                    retValue.add(((CmsResource)resources.get(0)).getAbsolutePath());
                }
            }
        } catch(SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, res);
        }
        return retValue;
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
        if((dataset.m_lockedBy != cms.getRequestContext().currentUser().getId())) {
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
            PreparedStatement stmnt = null;
            Connection con = null;
            try {
                con = DriverManager.getConnection(m_poolName);
                stmnt = sqlPrepare(con, statement_key);
                stmnt.setInt(1, dataset.m_masterId);
                stmnt.setInt(2, content.getSubId());
                if(stmnt.executeUpdate() != 1) {
                    // no line deleted - row wasn't found
                    throw new CmsException("Row not found: " + dataset.m_masterId + " " + content.getSubId(), CmsException.C_NOT_FOUND);
                }
                // after deleting the row, we have to delete media and channel rows
                deleteAllMedia(dataset.m_masterId);
                deleteAllChannels(dataset.m_masterId);
            } catch(SQLException exc) {
                throw new CmsException(CmsException.C_SQL_ERROR, exc);
            } finally {
                sqlClose(con, stmnt, null);
            }
        } else {
            // set state to deleted and update the line
            String statement_key = "update_offline";
            dataset.m_state = I_CmsConstants.C_STATE_DELETED;
            dataset.m_lockedBy = I_CmsConstants.C_UNKNOWN_ID;
            PreparedStatement stmnt = null;
            Connection con = null;
            try {
                con = DriverManager.getConnection(m_poolName);
                stmnt = sqlPrepare(con, "update_offline");
                int rowcounter = sqlFillValues(stmnt, content.getSubId(), dataset);
                stmnt.setInt(rowcounter++, dataset.m_masterId);
                stmnt.setInt(rowcounter++, content.getSubId());
                stmnt.executeUpdate();
            } catch(SQLException exc) {
                throw new CmsException(CmsException.C_SQL_ERROR, exc);
            } finally {
                sqlClose(con, stmnt, null);
            }
        }
    }

    /**
     * Returns a string representation of this instance.
     * This can be used for debugging.
     * @returns the string representation of this instance.
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
        Class superClass = getClass().getSuperclass();
        if((!superClass.equals(currentClass)) && (superClass != java.lang.Object.class)) {
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
     * @returns if in this run replacements are done.
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
     * Closes all sql ressources.
     * @param con the Connection to close.
     * @param stmnt the SqlStatement to close.
     * @param resr the ResultSet to close.
     */
    protected void sqlClose(Connection con, Statement stmnt, ResultSet res) {
        try {
            res.close();
        } catch(Exception exc) {
            // ignore the exception
            // this was null or already closed
        }
        try {
            stmnt.close();
        } catch(Exception exc) {
            // ignore the exception
            // this was null or already closed
        }
        try {
        con.close();
        } catch(Exception exc) {
            // ignore the exception
            // this was null or already closed
        }
    }

    /**
     * Inserts all values to the statement for insert and update.
     * @param stmnt the Statement to fill the values to.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param subId the subid of this module.
     * @param dataset the set of data for this contentdefinition.
     * @returns the actual rowcounter.
     */
    protected int sqlFillValues(PreparedStatement stmnt, int subId, CmsMasterDataSet dataset)
        throws SQLException {
        // columncounter
        int i = 1;
        //// COREDATA ////
        stmnt.setInt(i++,dataset.m_masterId);
        stmnt.setInt(i++,subId);
        stmnt.setInt(i++,dataset.m_userId);
        stmnt.setInt(i++,dataset.m_groupId);
        stmnt.setInt(i++,dataset.m_lockedInProject);
        stmnt.setInt(i++,dataset.m_accessFlags);
        stmnt.setInt(i++,dataset.m_state);
        stmnt.setInt(i++,dataset.m_lockedBy);
        stmnt.setInt(i++,dataset.m_lastModifiedBy);
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
        return i;
    }

    /**
     * Inserts all values to the statement for insert and update.
     * @param res the Resultset read the values from.
     * @param cms the CmsObject to get access to cms-ressources.
     * @param content the CmsMasterContent to write to the database.
     * @param dataset the set of data for this contentdefinition.
     * @returns the actual rowcounter.
     */
    protected int sqlFillValues(ResultSet res, CmsObject cms, CmsMasterDataSet dataset)
        throws SQLException {
        // columncounter
        int i = 1;
        //// COREDATA ////
        dataset.m_masterId = res.getInt(i++);
        res.getInt(i++); // we don't have to store the sub-id
        dataset.m_userId = res.getInt(i++);
        dataset.m_groupId = res.getInt(i++);
        dataset.m_lockedInProject = res.getInt(i++);
        // compute project based on the current project and the channels
        dataset.m_projectId = computeProjectId(cms, dataset);
        dataset.m_accessFlags = res.getInt(i++);
        dataset.m_state = res.getInt(i++);
        dataset.m_lockedBy =res.getInt(i++);
        dataset.m_lastModifiedBy = res.getInt(i++);
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
                if( (cms.getRequestContext().currentUser().getId() == dataset.m_userId) ||
                     cms.isAdmin()) {
                     return offlineProjectId;
                }
            } catch(CmsException exc) {
                // ignore the exception -> we are not admin
            }

            String statement_key = "read_channel_offline";
            String poolToUse = m_poolName;

            PreparedStatement stmnt = null;
            ResultSet res = null;
            Connection con = null;
            try {
                cms.setContextToCos();
                con = DriverManager.getConnection(poolToUse);
                stmnt = sqlPrepare(con, statement_key);
                stmnt.setInt(1, dataset.m_masterId);
                res = stmnt.executeQuery();
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
                sqlClose(con, stmnt, res);
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
     * @returns the increased columnscounter;
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
     * @returns the increased columnscounter;
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
     * @returns the increased columnscounter;
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
     * @returns the increased columnscounter;
     */
    protected int sqlSetIntArray(ResultSet res, int[] array, int columnscounter)
        throws SQLException {
        for(int j = 0; j < array.length; j++) {
            array[j] = res.getInt(columnscounter++);
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
                if(checkAccess(cms, content, viewonly)) {
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
    protected boolean checkAccess(CmsObject cms, CmsMasterContent content, boolean viewonly) {
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
     * @returns true, if this is the onlineproject, else returns false
     */
    protected boolean isOnlineProject(CmsObject cms) {
        boolean retValue = false;
        try {
             if(cms.getRequestContext().currentProject().equals(cms.onlineProject())) {
                // yes, this is the onlineproject!
                retValue = true;
             }
        } catch(CmsException exc) {
            // ignore the exception
        }
        return retValue;
    }

    /**
     * Deletes all media lines for one master.
     * @param masterId - the masterId to delete the media for
     * @throws SQLException if an sql-error occur
     */
    protected void deleteAllMedia(int masterId) throws SQLException {
        String statement_key = "delete_all_media_offline";
        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, statement_key);
            stmnt.setInt(1, masterId);
            stmnt.executeUpdate();
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * Deletes all channel lines for one master.
     * @param masterId - the masterId to delete the media for
     * @throws SQLException if an sql-error occur
     */
    protected void deleteAllChannels(int masterId) throws SQLException {
        String statement_key = "delete_all_channel_offline";
        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, statement_key);
            stmnt.setInt(1, masterId);
            stmnt.executeUpdate();
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * TODO: write header here
     */
    protected void updateMedia(int masterId, Vector mediaToAdd,
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
                media.setId(CmsIdGenerator.nextId(m_poolName, "CMS_MODULE_MEDIA"));
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
                stmnt.setInt(rowCounter++, masterId);
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
                stmnt.setInt(2, masterId);
                stmnt.executeUpdate();
            }
        } finally {
            sqlClose(con, stmnt, null);
        }
    }

    /**
     * TODO: write header here
     */
    protected void updateChannels(CmsObject cms, int masterId, Vector channelToAdd,
        Vector channelToDelete) throws SQLException {
        // add new channel
        PreparedStatement stmnt = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            stmnt = sqlPrepare(con, "insert_channel_offline");
            for(int i = 0; i < channelToAdd.size(); i++) {
                try {
                    stmnt.setInt(1, masterId);
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
                    stmnt.setInt(1, masterId);
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
     * TODO: write header here
     */
    protected int sqlFillValues(PreparedStatement stmnt, CmsMasterMedia media)
        throws SQLException {
        int i = 1;
        stmnt.setInt(i++, media.getId());
        stmnt.setInt(i++, media.getMasterId());
        stmnt.setInt(i++, media.getPosition());
        stmnt.setInt(i++, media.getWidth());
        stmnt.setInt(i++, media.getHeight());
        stmnt.setInt(i++, media.getSize());
        stmnt.setString(i++, media.getMimetype());
        stmnt.setInt(i++, media.getType());
        stmnt.setString(i++, media.getTitle());
        stmnt.setString(i++, media.getName());
        stmnt.setString(i++, media.getDescription());
        stmnt.setBytes(i++, media.getMedia());
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
    public Vector getHistory(CmsObject cms, Class contentDefinitionClass, int masterId, int subId) throws CmsException{
        Vector retVector = new Vector();
        Vector allBackup = new Vector();
        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "read_all_backup");
            stmnt.setInt(1, masterId);
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
                                                  int masterId, int subId, int versionId) throws CmsException{
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
    public CmsMasterDataSet getVersionFromHistory(CmsObject cms, int masterId, int subId, int versionId) throws CmsException{
        CmsMasterDataSet dataset = new CmsMasterDataSet();
        PreparedStatement stmnt = null;
        ResultSet res = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "read_backup");
            stmnt.setInt(1, masterId);
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
        int groupId = 0;
        try {
            groupId = cms.readGroup(backup.m_groupId).getId();
        } catch (CmsException exc){
            groupId = dataset.m_groupId;
        }
        dataset.m_groupId = groupId;
        // check if the user exists
        int userId = 0;
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
            stmnt.setInt(1, dataset.m_masterId);
            stmnt.setInt(2, versionId);
            res = stmnt.executeQuery();
            while (res.next()){
                int i = 1;
                CmsMasterMedia media = new CmsMasterMedia (
                                            res.getInt(i++),
                                            res.getInt(i++),
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

        // backup the data
        if(enableHistory) {
            publishBackupData(cms, dataset, subId, versionId, publishingDate);
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
            stmnt.setInt(2, I_CmsConstants.C_UNKNOWN_ID);
            stmnt.setInt(3, dataset.m_masterId);
            stmnt.setInt(4, subId);
            stmnt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsException(CmsException.C_SQL_ERROR, exc);
        } finally {
            sqlClose(con, stmnt, null);
        }

        // update changedModuleData Vector
        changedModuleData.add(cms.getSiteName() + cms.C_ROOTNAME_COS + "/"+
            contentDefinitionName +"/"+dataset.m_masterId);
    }

    /**
     * @todo: add description here
     */
    protected void publishDeleteData(int masterId, int subId, String table) throws CmsException {
        PreparedStatement stmnt = null;
        Connection con = null;
        // delete channel relation
        try {
            con = DriverManager.getConnection(m_onlinePoolName);
            stmnt = sqlPrepare(con, "delete_all_channel_" + table);
            stmnt.setInt(1, masterId);
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
            stmnt.setInt(1, masterId);
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
            stmnt.setInt(1, masterId);
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
        int masterId = dataset.m_masterId;
        // copy the row
        try {
            stmnt = null;
            con = DriverManager.getConnection(m_onlinePoolName);
            stmnt = sqlPrepare(con, "insert_online");
            // correct the data in the dataset
            dataset.m_projectId = I_CmsConstants.C_PROJECT_ONLINE_ID;
            dataset.m_lockedInProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
            dataset.m_state = I_CmsConstants.C_STATE_UNCHANGED;
            dataset.m_lockedBy = I_CmsConstants.C_UNKNOWN_ID;
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
            stmnt.setInt(1, masterId);
            res = stmnt.executeQuery();
            while(res.next()) {
                // create a new dataset to fill the values
                int i = 1;
                CmsMasterMedia mediaset = new CmsMasterMedia (
                                                res.getInt(i++),
                                                res.getInt(i++),
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
            stmnt.setInt(1, masterId);
            res = stmnt.executeQuery();
            while (res.next()){
                // insert all channel relations for master into online
                try {
                    stmnt2 = null;
                    con2 = null;
                    con2 = DriverManager.getConnection(m_onlinePoolName);
                    stmnt2 = sqlPrepare(con2, "insert_channel_online");
                    stmnt2.setInt(1, masterId);
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
        int masterId = dataset.m_masterId;
        // copy the row
        try {
            stmnt = null;
            con = null;
            con = DriverManager.getConnection(m_backupPoolName);
            stmnt = sqlPrepare(con, "insert_backup");
            // correct the data in the dataset
            dataset.m_lockedBy = I_CmsConstants.C_UNKNOWN_ID;
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
            stmnt.setInt(1, masterId);
            res = stmnt.executeQuery();
            while(res.next()) {
                // create a new dataset to fill the values
                int i = 1;
                CmsMasterMedia mediaset = new CmsMasterMedia (
                                                res.getInt(i++),
                                                res.getInt(i++),
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
}