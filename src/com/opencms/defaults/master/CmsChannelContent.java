/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsChannelContent.java,v $
* Date   : $Date: 2004/02/22 13:52:27 $
* Version: $Revision: 1.65 $
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
// TODO: remove group/user
package com.opencms.defaults.master;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceTypeFolder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.A_CmsContentDefinition;
import com.opencms.defaults.CmsFilterMethod;
import com.opencms.defaults.I_CmsExtendedContentDefinition;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * This class is the master of several Modules. It carries a lot of generic
 * data-fileds which can be used for a special Module.
 *
 * The module creates a set of methods to support project-integration, history
 * and import - export.
 *
 * @author E. Falkenhan $
 * $Revision: 1.65 $
 * $Date: 2004/02/22 13:52:27 $
 */
public class CmsChannelContent extends A_CmsContentDefinition implements I_CmsExtendedContentDefinition{

    // definition of the error codes used by this content defintion
    private static String C_CHANNELNAME_ERRFIELD="channelname";
    private static String C_PARENT_ERRFIELD="channelparent";
    //error code for empty inputs
    private static String C_ERRCODE_EMPTY="empty";
    //error code for no text input

    /** The cms-object to get access to the cms-ressources */
    protected CmsObject m_cms = null;

    /**
     * The channel ID
     */
    private String m_channelId;

    /**
     * The resource object that contains the information for the channel
     */
    private CmsResource m_channel;

    /**
     * The name of the channel
     */
    private String m_channelname;

    /**
     * The name of the parent channel
     */
    private String m_parentchannel;

    /**
     * The properties of the channel
     */
    private Map m_properties;

    /**
     * The groupid of the channel
     */
    // private CmsUUID m_GroupId;

    /**
     * The userid of the channel
     */
    // private CmsUUID m_UserId;

    /**
     * The accessflags of the channel
     */
    private int m_accessflags;

    /**
     * Constructor to create a new contentdefinition. You can set data with your
     * set-Methods. After you have called the write-method this definition gets
     * a unique id.
     */
    public CmsChannelContent(CmsObject cms) {
        m_cms = cms;
        initValues();
    }

    /**
     * Constructor to read a existing contentdefinition from the database. The
     * data read from the database will be filled into the member-variables.
     * You can read them with the get- and modify them with the set-methods.
     * Changes you have made must be written back to the database by calling
     * the write-method.
     * @param cms the cms-object for access to cms-resources.
     * @param resourceid the resource id of the channel to read.
     * @throws CmsException if the data couldn't be read from the database.
     */
    public CmsChannelContent(CmsObject cms, String channelId) throws CmsException {
        new CmsChannelContent(cms, new CmsUUID(channelId));
    }
    /**
     * Constructor to read a existing contentdefinition from the database. The
     * data read from the database will be filled into the member-variables.
     * You can read them with the get- and modify them with the set-methods.
     * Changes you have made must be written back to the database by calling
     * the write-method.
     * @param cms the cms-object for access to cms-resources.
     * @param channelname the name of the channel to read.
     * @throws CmsException if the data couldn't be read from the database.
     */
    public CmsChannelContent(CmsObject cms, CmsUUID channelId) throws CmsException {
        m_cms = cms;
        initValues();
        m_cms.getRequestContext().saveSiteRoot();
        m_cms.setContextToCos();

        try {
            m_channel = m_cms.readFolder(channelId, true);            
            m_channelname = m_channel.getName();
            m_parentchannel = CmsResource.getParentFolder(cms.readAbsolutePath(m_channel));
            // m_GroupId = m_channel.getGroupId();
            // m_UserId = m_channel.getOwnerId();
            // m_accessflags = m_channel.getAccessFlags();
            m_properties = m_cms.readProperties(cms.readAbsolutePath(m_channel));
            m_channelId = (String) m_properties.get(I_CmsConstants.C_PROPERTY_CHANNELID);
        } catch (CmsException exc) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Could not get channel " + channelId, exc);
            }
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Constructor to create a new contentdefinition. You can set data with your
     * set-Methods. After you have called the write-method this definition gets
     * a unique id.
     */
    public CmsChannelContent(CmsObject cms, CmsResource resource) {
        String channelId = I_CmsConstants.C_UNKNOWN_ID+"";
        String fullName = cms.readAbsolutePath(resource);
        m_cms = cms;
        m_channel = resource;
        m_channelname = resource.getName();
        m_parentchannel = CmsResource.getParentFolder(cms.readAbsolutePath(resource));
        // m_GroupId = resource.getGroupId();
        // m_UserId = resource.getOwnerId();
        // m_accessflags = resource.getAccessFlags();
        try{
            m_properties = cms.readProperties(fullName);
            channelId = (String)m_properties.get(I_CmsConstants.C_PROPERTY_CHANNELID);
        } catch (CmsException exc){
            m_properties = new Hashtable();
            m_properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, I_CmsConstants.C_UNKNOWN_ID+"");
        } finally {
            if(channelId == null || "".equals(channelId)){
                channelId = I_CmsConstants.C_UNKNOWN_ID+"";
            }
            m_channelId = channelId;
        }
    }

    /**
     * This method initialises all needed members with default-values.
     */
    protected void initValues() {
        m_channelId = I_CmsConstants.C_UNKNOWN_ID+"";
        m_channelname = "";
        m_parentchannel = "";
        m_accessflags = I_CmsConstants.C_ACCESS_DEFAULT_FLAGS;
        // create the resource object for the channel:
        m_channel = new CmsResource(CmsUUID.getNullUUID(), CmsUUID.getNullUUID(),
                                     CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), "", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
                                     0,
                                     m_cms.getRequestContext().currentProject().getId(), 1,
                                     I_CmsConstants.C_UNKNOWN_ID,
                                     System.currentTimeMillis(), m_cms.getRequestContext().currentUser().getId(),
                                     System.currentTimeMillis(), m_cms.getRequestContext().currentUser().getId(),
                                     0, 1);
        m_properties = new Hashtable();
    }

    /**
     * delete method
     * for delete instance of content definition
     * @param cms the CmsObject to use.
     */
    public void delete(CmsObject cms) throws Exception {
        cms.getRequestContext().saveSiteRoot();
        cms.setContextToCos();
        try{
            cms.deleteResource(cms.readAbsolutePath(m_channel), I_CmsConstants.C_DELETE_OPTION_IGNORE_VFS_LINKS);
        } catch (CmsException exc){
            throw exc;
            /*
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsChannelContent] Could not delete channel "+cms.readPath(m_channel));
            }
            */
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * undelete method
     * for undelete instance of content definition
     * @param cms the CmsObject to use.
     */
    public void undelete(CmsObject cms) throws Exception {
        cms.getRequestContext().saveSiteRoot();
        cms.setContextToCos();
        try{
            cms.undeleteResource(cms.readAbsolutePath(m_channel));
        } catch (CmsException exc){
            if (OpenCms.getLog(this).isErrorEnabled() ) {
                OpenCms.getLog(this).error("Could not undelete channel " + cms.readAbsolutePath(m_channel), exc);
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * publish method
     * for publish instance of content definition
     * @param cms the CmsObject to use.
     */
    public void publishResource(CmsObject cms) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Channels can't be published directly");
        }
    }

    /**
     * restore method
     * for restore instance of content definition from history
     * @param cms the CmsObject to use.
     * @param versionId The id of the version to restore
     */
    public void restore(CmsObject cms, int versionId) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Channels can't be restored from history");
        }
    }

    /**
     * Change owner method
     * for changing permissions of content definition
     * @param cms the CmsObject to use.
     * @param owner the new owner of the cd.
     */
    public void chown(CmsObject cms, CmsUUID owner) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Permissions of Channels can be changed only in EditBackoffice");
        }
    }

    /**
     * Change group method
     * for changing permissions of content definition
     * @param cms the CmsObject to use.
     * @param group the new group of the cd.
     */
    public void chgrp(CmsObject cms, CmsUUID group) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Permissions of Channels can be changed only in EditBackoffice");
        }
    }

    /**
     * Change access flags method
     * for changing permissions of content definition
     * @param cms the CmsObject to use.
     * @param accessflags the new access flags of the cd.
     */
    public void chmod(CmsObject cms, int accessflags) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Permissions of Channels can be changed only in EditBackoffice");
        }
    }
    /**
     * Copy method
     * for copying content definition
     * @param cms the CmsObject to use.
     * @return int The id of the new content definition
     */
    public CmsUUID copy(CmsObject cms) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Channels can't be copied!");
        }
        return CmsUUID.getNullUUID();
    }

    /**
     * write method
     * to write the current content of the content definition to the database.
     * @param cms the CmsObject to use.
     */
    public void write(CmsObject cms) throws Exception {
        CmsResource newChannel = null;
        CmsLock lock = null;
        // is this a new row or an existing row?
        cms.getRequestContext().saveSiteRoot();
        cms.setContextToCos();
        try{
            if((I_CmsConstants.C_UNKNOWN_ID+"").equals(m_channelId)) {
                // this is a new row - call the create statement
                // first set the new channelId
                setNewChannelId();
                newChannel = cms.createResource(m_parentchannel, m_channelname, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, m_properties);
                cms.lockResource(cms.readAbsolutePath(newChannel), true);
            } else {
                if (!"".equals(m_channel.getName())) {
                    newChannel = cms.readFolder(cms.readAbsolutePath(m_channel));
                }
                
                if (newChannel!=null && !cms.readAbsolutePath(newChannel).equals(m_parentchannel+m_channelname+"/")){
                    // the parent and/or the channelname has changed,
                    // so move or rename the channel
                    String parent = CmsResource.getParentFolder(cms.readAbsolutePath(newChannel));
                    if(! parent.equals(m_parentchannel)){
                        // move the channel to the new parent channel
                        cms.moveResource(cms.readAbsolutePath(newChannel), m_parentchannel+m_channelname);
                    } else if (!newChannel.getName().equals(m_channelname)){
                        // only rename the channel, the parent has not changed
                        cms.renameResource(cms.readAbsolutePath(newChannel), m_channelname);
                    }
                }
                // read the changed channel
                newChannel =  cms.readFolder(m_parentchannel+m_channelname+"/");
                lock = cms.getLock(newChannel);
                // update the title of the channel
                String propTitle = cms.readProperty(cms.readAbsolutePath(newChannel), I_CmsConstants.C_PROPERTY_TITLE);
                if (propTitle == null){
                    propTitle = "";
                }
                if (!propTitle.equals(this.getTitle())){
                    cms.writeProperty(cms.readAbsolutePath(newChannel), I_CmsConstants.C_PROPERTY_TITLE, this.getTitle());
                }
                // check if the lockstate has changed
                if(!lock.getUserId().equals(this.getLockstate()) ||
                    lock.getProjectId() != cms.getRequestContext().currentProject().getId()){
                    if(this.getLockstate().isNullUUID()){
                        // unlock the channel
                        cms.unlockResource(cms.readAbsolutePath(newChannel), false);
                    } else {
                        // lock the channel
                        cms.lockResource(cms.readAbsolutePath(newChannel), true);
                    }
                }
            }
            // TODO: ACL's for COS
//            // check if the owner has changed
//            if(!newChannel.getOwnerId().equals(this.getOwner())){
//                cms.chown(cms.readAbsolutePath(newChannel), this.getOwnerName());
//            }
//            // check if the group has changed
//            if(!newChannel.getGroupId().equals(this.getGroupId())){
//                cms.chgrp(cms.readAbsolutePath(newChannel), this.getGroup());
//            }
//            // check if the accessflags has changed
//            if(newChannel.getAccessFlags() != this.getAccessFlags()){
//                cms.chmod(cms.readAbsolutePath(newChannel), this.getAccessFlags());
//            }
            m_channel = cms.readFolder(cms.readAbsolutePath(newChannel));
        } catch (CmsException exc){
            throw exc;
            /*
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsChannelContent] Could not write channel "+cms.readPath(m_channel));
            }
            */
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * gets the unique Id of a content definition instance
     * @param cms the CmsObject to use.
     * @return a string with the Id
     */
    public String getUniqueId(CmsObject cms) {
        return m_channel.getStructureId().toString();
    }

    /**
     * gets the unique Id of a content definition instance
     *
     * @return int The unique id
     */
    public CmsUUID getId() {
        return m_channel.getStructureId();
    }

    /**
     * Gets the projectId where the CD belongs to.
     * This method is required for the use of the abstract backoffice.
     * @return int with the projectId.
     */
    public int getLockedInProject() {
        try {
            return m_cms.getLock(m_channel).getProjectId();
        } catch (CmsException e) {
            return I_CmsConstants.C_UNKNOWN_ID;
        }
    }

    /**
     * Gets the state of a CD.
     * This method is required for the use of the abstract backoffice.
     * @return int with the state.
     */
    public int getState() {
        return m_channel.getState();
    }

    /**
     * Gets the projectId of a CD.
     * This method is required for the use of the abstract backoffice.
     * @return int with the projectId.
     */
    public int getProjectId() {
        return m_channel.getProjectLastModified();
    }

    /**
     * gets the unique channel Id of a content definition instance
     * @return a string with the Id
     */
    public String getChannelId() {
        return m_channelId;
    }

    /**
     * sets the unique channel Id of a content definition instance
     */
    public void setChannelId(String id) {
        m_properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, id);
        m_channelId = id;
    }

    /**
     * Gets the title of the channel
     */
    public String getTitle(){
        String title = (String)m_properties.get(I_CmsConstants.C_PROPERTY_TITLE);
        if (title == null){
            title = "";
        }
        return title;
    }

    /**
     * sets the title of a content definition instance
     */
    public void setTitle(String title) {
        m_properties.put(I_CmsConstants.C_PROPERTY_TITLE, title);
    }

    /**
     * Gets the lockstate.
     * @return a int with the user who has locked the ressource.
     */
    public CmsUUID getLockstate() {
        try {
            return m_cms.getLock(m_channel).getUserId();
        } catch (CmsException e) {
            return CmsUUID.getNullUUID();
        }
    }

    /**
     * Sets the lockstates
     * @param the lockstate for the actual entry.
     */
    public void setLockstate(CmsUUID lockstate) {
        m_channel.setLocked(lockstate);
    }

    /**
     * Gets the ownername of this contentdefinition.
     */
    public String getOwnerName() {
        String ownername = "";
        try{
            ownername = m_cms.readUser(getOwner()).getName();
        } catch (CmsException exc){
            //nothing to do, return empty string
        }
        return ownername;
    }

    /**
     * Gets the owner of this contentdefinition.
     */
    public CmsUUID getOwner() {
        return CmsUUID.getNullUUID();
        // return m_UserId;
    }

    /**
     * Sets the owner of this contentdefinition.
     */
    public void setOwner(CmsUUID id) {
        // m_UserId = id;
    }

    /**
     * Gets the groupname
     */
    public String getGroup() {
        String groupname = "*NOT USED*";
        // try{
        //     groupname = m_cms.readGroup(this.getGroupId()).getName();
        // } catch (CmsException exc){
            // nothing to do, return empty string
        // }
        return groupname;
    }

    /**
     * Gets the groupid
     */
    public CmsUUID getGroupId() {
        return CmsUUID.getNullUUID();
        // return m_GroupId;
    }

    /**
     * Sets the group.
     */
    public void setGroup(CmsUUID groupId) {
        // m_GroupId = groupId;
    }

    /**
     * Gets the channelname
     */
    public String getChannelPath() {
        return m_cms.readAbsolutePath(m_channel);
    }

    /**
     * Gets the channelname
     */
    public String getChannelName() {
        return m_channelname;
    }

    /**
     * Sets the channelname.
     */
    public void setChannelName(String name) {
        m_channelname = name;
    }

    /**
     * Sets the parentname of the channel.
     */
    public void setParentName(String name) {
        m_parentchannel = name;
    }

    /**
     * Gets the name of the parent channel
     */
    public String getParentName() {
        return m_parentchannel;
    }

    /**
     * Sets the accessflags of the channel.
     */
    public void setAccessFlags(int flags) {
        m_accessflags = flags;
    }

    /**
     * Gets the accessflags of the channel
     */
    public int getAccessFlags() {
        return m_accessflags;
    }

    /**
     * Gets the date of the last modification of the channel
     */
    public long getDateLastModified(){
        return m_channel.getDateLastModified();
    }

    /**
     * Gets the date of the creation of the channel
     */
    public long getDateCreated(){
        return m_channel.getDateCreated();
    }

    /**
     * Gets the id of the user who has modified the channel
     */
    public CmsUUID getLastModifiedBy(){
        return m_channel.getUserLastModified();
    }

    /**
     * Gets the name of the user who has modified the channel
     */
    public String getLastModifiedByName(){
        return "";
    }

    /**
     * Gets the version id of version the channel
     */
    public int getVersionId(){
        return I_CmsConstants.C_UNKNOWN_ID;
    }

    /**
     * Gets all groups, that may work for a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */

    public Integer getGroups(CmsObject cms, Vector names, Vector values) throws CmsException {

        // get all groups
        Vector groups = cms.getGroups();
        int retValue = -1;
        String defaultGroup = OpenCms.getDefaultUsers().getGroupUsers();
        // make sure the user has a session
        CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String enteredGroup = this.getGroup();
        if(enteredGroup != null && !enteredGroup.equals("")) {

            // if an error has occurred before, take the previous entry of the user
            defaultGroup = enteredGroup;
        }

        // fill the names and values
        int n = 0;
        for(int z = 0;z < groups.size();z++) {
            if(((CmsGroup)groups.elementAt(z)).getProjectCoWorker()) {
                String name = ((CmsGroup)groups.elementAt(z)).getName();
                if(defaultGroup.equals(name)) {
                    retValue = n;
                }
                names.addElement(name);
                values.addElement(name);
                n++; // count the number of ProjectCoWorkers
            }
        }
        return new Integer(retValue);
    }

    /**
     * history method
     * returns the history of content definition
     * @param cms the CmsObject to use.
     * @return Vector The history of a cd
     */
    public Vector getHistory(CmsObject cms) {
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Channels have no history");
        }
        return null;
    }

    /**
     * History method
     * returns the cd of the version with the given versionId
     *
     * @param cms The CmsObject
     * @param versionId The version id
     * @return Object The object with the version of the cd
     */
    public Object getVersionFromHistory(CmsObject cms, int versionId){
        if (OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn("Channels have no history");
        }
        return null;
    }
    /**
     * returns true if the CD is readable for the current user
     * @return true
     */
    public boolean isReadable() {
        m_cms.getRequestContext().saveSiteRoot();
        m_cms.setContextToCos();
        try {
            return m_cms.hasPermissions(m_cms.readAbsolutePath(m_channel), I_CmsConstants.C_READ_ACCESS); 
            // TODO: remove this later
            // m_cms.accessRead();
        } catch(CmsException exc) {
            // there was a cms-exception - no read-access!
            return false;
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * returns true if the CD is writeable for the current user
     * @return true
     */
    public boolean isWriteable() {
        m_cms.getRequestContext().saveSiteRoot();
        m_cms.setContextToCos();
        try {
            // return m_cms.accessWrite(cms.readPath(m_channel));
            return m_cms.hasPermissions(m_cms.readAbsolutePath(m_channel), I_CmsConstants.C_WRITE_ACCESS);
        } catch(CmsException exc) {
            // there was a cms-exception - no write-access!
            return false;
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Returns the sub-id of this contentdefinition. You have to implement this
     * method so it returns a unique sunb-id that describes the type of the
     * contentdefinition. (E.g. article: sub-id=1; table: sub-id=2).
     */
    //abstract public int getSubId();

    /**
     * Returns a String representation of this instance.
     * This can be used for debugging purposes.
     */
    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append(this.getClass().getName() + "{");
        returnValue.append("ChannelId=" + getChannelId() + ";");
        returnValue.append("ChannelName=" + getChannelPath() + ";");
        returnValue.append("Lockstate=" + getLockstate() + ";");
        returnValue.append("AccessFlags=" + getAccessFlagsAsString() + ";");
        returnValue.append(m_channel.toString() + "}");
        return returnValue.toString();
    }

    /**
     * Convenience method to get the access-Flags as String representation.
     * @return String of access rights
     */
    public String getAccessFlagsAsString() {
//        int accessFlags = m_channel.getAccessFlags();
        String str = "NOT YET";
        // TODO: reimplement using acl
        /*
        str += ((accessFlags & I_CmsConstants.C_PERMISSION_READ)>0?"r":"-");
        str += ((accessFlags & I_CmsConstants.C_PERMISSION_WRITE)>0?"w":"-");
        str += ((accessFlags & I_CmsConstants.C_PERMISSION_VIEW)>0?"v":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_GROUP_READ)>0?"r":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_GROUP_WRITE)>0?"w":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_GROUP_VISIBLE)>0?"v":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_PUBLIC_READ)>0?"r":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_PUBLIC_WRITE)>0?"w":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_PUBLIC_VISIBLE)>0?"v":"-");
        str += ((accessFlags & I_CmsConstants.C_ACCESS_INTERNAL_READ)>0?"i":"-");
        */
        return str;
    }

    /**
     * Get the permissions of Channel
     */
    public void setAccessFlagsAsString(String permissions){
        //change direction
        String perm=permissions;
        permissions="";
        for(int x=9;x >= 0;x--) {
            char temp=perm.charAt(x);
            permissions+=temp;
        }
        setAccessFlags(Integer.parseInt(permissions,2));
    }

    /**
     * Sets the channelId of a new channel
     */
    private void setNewChannelId() throws CmsException{
        int newChannelId = org.opencms.db.CmsDbUtil.nextId(I_CmsConstants.C_TABLE_CHANNELID);
        m_properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, newChannelId+"");
        m_channelId = newChannelId+"";
    }

    /**
     * This content definition is lockable. This class overwrited the isLockable method of the abstract
     * backoffice to flag that this content definition uses the lock feature of the backoffice.  *
     */
    public static boolean isLockable() {
        return true;
    }

    /**
     * Gets the names of the table columns displayed in the backoffice filelist.
     * This method is needed for the abstract backoffice class only.
     * @return Vector containing the columnnames.
     */
    public static Vector getFieldNames(CmsObject cms) {
        Vector names = new Vector();
        names.addElement("channelId");
        names.addElement("channelPath");
        names.addElement("title");
        names.addElement("ownerName");
        names.addElement("group");
        names.addElement("accessFlagsAsString");
        return names;
    }

    /**
     * Gets the methods used to display the dava values in the backoffice filelist.
     * This method is needed for the abstract backoffice class only.
     * @return Vector with the nescessary get methods
     */
    public static Vector getFieldMethods(CmsObject cms) {
        Vector methods = new Vector();
        try {
            methods.addElement(CmsChannelContent.class.getMethod("getChannelId", new Class[0]));
            methods.addElement(CmsChannelContent.class.getMethod("getChannelPath", new Class[0]));
            methods.addElement(CmsChannelContent.class.getMethod("getTitle", new Class[0]));
            methods.addElement(CmsChannelContent.class.getMethod("getOwnerName", new Class[0]));
            methods.addElement(CmsChannelContent.class.getMethod("getGroup", new Class[0]));
            methods.addElement(CmsChannelContent.class.getMethod("getAccessFlagsAsString", new Class[0]));
        } catch(NoSuchMethodException exc) {
            // this exception should never occur. You know your own methods in this cd
        }
        return methods;
    }

    /**
     * Gets the filter methods
     * This method is needed for the abstract backoffice class only.
     * @return a method array containing the methods
     */
    public static Vector getFilterMethods(CmsObject cms) {
        Vector filterMethods = new Vector();
        try {
            CmsFilterMethod filterUp = new CmsFilterMethod("All Channels",
                                      CmsChannelContent.class.getMethod("getChannelList",
                                      new Class[] {CmsObject.class} ) , new Object[0]);
            filterMethods.addElement(filterUp);
        } catch (NoSuchMethodException nsm) {
            // this exception should never occur because you know your filter methods.
        }
        return filterMethods;
    }

    /**
     * Returns a vector of CD objects, sorted descending by channelname.
     * This method is needed for the abstract backoffice class only.
     * @param cms The CmsObject.
     */
    public static Vector getChannelList(CmsObject cms) throws CmsException {
        Vector content = new Vector();
        cms.getRequestContext().saveSiteRoot();
        cms.setContextToCos();
        try {
            getAllResources(cms, "/", content);
        } catch(CmsException e) {
            // ignore the exception
            if (OpenCms.getLog(CmsChannelContent.class).isWarnEnabled()) {
                OpenCms.getLog(CmsChannelContent.class).warn("Error while reading subfolders of cos root", e);
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
        return content;
    }
    /**
     * Helper for method getChannelList
     */
    private static void getAllResources(CmsObject cms, String rootFolder, Vector allFolders) throws CmsException {
        // get folders of this rootFolder
        Vector subFolders = new Vector();
        try{
            subFolders = cms.getResourcesInFolder(rootFolder);
        } catch (CmsException e){
            // if the folder could not be found it might be deleted, so don't throw this exception
            if(e.getType() != CmsException.C_NOT_FOUND){
                throw e;
            }
        }
        //copy the values into the allFolders Vector
        for(int i = 0;i < subFolders.size();i++) {
            CmsResource curFolder = (CmsResource)subFolders.elementAt(i);
            CmsChannelContent curChannel = new CmsChannelContent(cms, curFolder);
            allFolders.addElement(curChannel);
            getAllResources(cms, cms.readAbsolutePath(curFolder), allFolders);
        }
    }
    /**
     * Plauzibilization method.
     * This method checks if all inputfields contain correct input data.
     * If an input field has no correct data, a CmsPlausibilizationException is thrown.
     * @throws Throws CmsPlausibilizationException containing a vector of error-codes.
     */
    public void check() throws CmsPlausibilizationException {
        // define the vector which will hold all error codes
        Vector errorCodes = new Vector();
        //check the channelname
        if (m_channelname == null || "".equals(m_channelname)) {
            errorCodes.addElement(C_CHANNELNAME_ERRFIELD+I_CmsConstants.C_ERRSPERATOR+C_ERRCODE_EMPTY);
        }
        //check the parentchannel
        if (m_parentchannel == null || "".equals(m_parentchannel)) {
            errorCodes.addElement(C_PARENT_ERRFIELD+I_CmsConstants.C_ERRSPERATOR+C_ERRCODE_EMPTY);
        }
        // now test if there was an error message and throw an exception
        if (errorCodes.size()>0) {
            throw new CmsPlausibilizationException(errorCodes);
        }
    }
}