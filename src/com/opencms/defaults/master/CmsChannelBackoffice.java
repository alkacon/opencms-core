/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsChannelBackoffice.java,v $
* Date   : $Date: 2003/07/22 00:29:23 $
* Version: $Revision: 1.19 $
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

package com.opencms.defaults.master;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.defaults.A_CmsBackoffice;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Encoder;
import com.opencms.workplace.CmsXmlLanguageFile;
import com.opencms.workplace.CmsXmlWpTemplateFile;

import java.util.Hashtable;
import java.util.Vector;


public class CmsChannelBackoffice extends A_CmsBackoffice{
    
    /**
     * The COS channel.
     */
    private CmsChannelContent m_channel;
    
    /**
     * The ID of the channel which is set as a property on the folder representing the channel.
     */
    private String m_channelId = "0";
    
    /**
     * The resource ID of the folder representing the channel.
     */
    private CmsUUID m_folderId;

    public Class getContentDefinitionClass() {
        return CmsChannelContent.class;
    }

    public String getCreateUrl(CmsObject cms, String tagcontent,
                             A_CmsXmlContent doc, Object userObject) throws Exception{
        return "system/workplace/administration/channels/EditBackoffice.html";
    }

    public String getBackofficeUrl(CmsObject cms, String tagcontent,
                                 A_CmsXmlContent doc, Object userObject) throws Exception {
        return "system/workplace/administration/channels/Backoffice.html";
    }

    public String getEditUrl(CmsObject cms, String tagcontent,
                           A_CmsXmlContent doc, Object userObject) throws Exception {
        return "system/workplace/administration/channels/EditBackoffice.html";
    }

    public String getPublishUrl(CmsObject cms, String tagcontent,
                                 A_CmsXmlContent doc, Object userObject) throws Exception {
        return "system/workplace/administration/channels/Backoffice.html";
    }

    // removed for testing
    public String getSetupUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws Exception {
        return "";
    }

    /**
     * Gets the content of a given template file.
     * <P>
     *
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName not used here
     * @param parameters get the parameters action for the button activity
     *                   and id for the used content definition instance object
     *                   and the author, title, text content for setting the new/changed data
     * @param templateSelector template section that should be processed.
     * @return Processed content of the given template file.
     * @throws CmsException
     */
    public byte[] getContentEdit(CmsObject cms, CmsXmlWpTemplateFile template, String elementName,
                             Hashtable parameters, String templateSelector) throws CmsException {
        //get the Languagedata
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        //create appropriate class name with underscores for labels
        String moduleName = "";
        moduleName = getClass().toString(); //get name
        moduleName = moduleName.substring(5); //remove 'class' substring at the beginning
        moduleName = moduleName.trim();
        moduleName = moduleName.replace('.', '_'); //replace dots with underscores
        //session
        I_CmsSession session = cms.getRequestContext().getSession(true);
        //default error
        String error="";
        //get values of all fields
        //gets the parameter action from the hashtable.
        String action = (String) parameters.get("action");
        if (action == null){
            action="";
        } else if (action.equals("exit")){
            clearSession(session);
            templateSelector= "done";
            return startProcessing(cms, template, "", parameters, templateSelector);
        }

        // Get the parameter values

        // get value of id
        String id = (String) parameters.get("id");        
        String channelId = (String) parameters.get("channelId");
        String resourceId = (String) parameters.get("resourceid");
        String nullUUIDStr = CmsUUID.getNullUUID().toString();
        
        // get value of parentId
        String parentName = (String) parameters.get("parentName");
        if(parentName == null || parentName.equals("")){
            parentName="/";
        }
        
        // channelname
        String channelname = (String) parameters.get("channelName");
        if(channelname == null){
            channelname = "";
        }
        
        // plausibility check
        if(!action.equals("") && channelname.trim().equals("")){
            error = lang.getLanguageValue(moduleName+".error.message2");
        }
        
        // title of channel
        String title = (String) parameters.get("title");

        String owner=(String) parameters.get("owner");
        owner=(owner!=null?owner.trim():"");
        String group=(String) parameters.get("groupId");
        group=(group!=null?group.trim():"");

        int accessFlags = this.getAccessValue(parameters);

        //new channel
        if (id == null || id.equals("")){
            id = "new";
        }
        if(channelId==null || channelId.trim().equals("")){
            channelId="-1";
        }
        if(resourceId==null || resourceId.trim().equals("")){
            resourceId=nullUUIDStr;
        }
        if(id.equals("new")){
            if(resourceId.equals(nullUUIDStr)){
                m_channel=null;
            }else{
                id=resourceId;
            }
        }
        // for the first call check if the id exist, i.e. if the category is to edit
        // and it is not created new one then read the information of category
        if (action.equals("") && !id.equals("new")) {            
//            try {
//                Integer.parseInt(id);
//            } catch (Exception err) {
//                throw new CmsException(err.getMessage());
//            }
            // gets the already existing ouputchannel's content definition.
            m_channel = new CmsChannelContent(cms,new CmsUUID(id));
            parentName=m_channel.getParentName();
            m_channelId=m_channel.getChannelId();
            m_folderId = m_channel.getId();
            channelname=m_channel.getChannelName();
            title=m_channel.getTitle();
            owner=m_channel.getOwner().toString();
            group=m_channel.getGroupId().toString();
            accessFlags=m_channel.getAccessFlags();
        }else if(m_channel==null){            
            //create a new ouputchannels content definition.
            m_channel = new CmsChannelContent(cms);
            m_channelId=C_UNKNOWN_ID+"";
            m_folderId = CmsUUID.getNullUUID();
        }
        //put parentId in session for user method
        session.putValue("parentName",parentName);
        session.putValue("id",m_folderId.toString());
        //set data in CD when data is correct
        if(error.equals("") && !action.equals("")) {           
            m_channel.setChannelId(m_channelId);
            m_channel.setParentName(parentName);
            m_channel.setChannelName(channelname);
            m_channel.setTitle(title);
            m_channel.setGroup(new CmsUUID(group));
            m_channel.setOwner(new CmsUUID(owner));
            m_channel.setAccessFlags(accessFlags);
        }
        //get values of the user for new entry
        String defaultGroup=cms.getRequestContext().currentGroup().getName();
        String defaultUser=cms.getRequestContext().currentUser().getName();
        //get all groups
        Vector cmsGroups=cms.getGroups();
        // select box of group
        String groupOptions="";
        for (int i=0; i<cmsGroups.size(); i++) {
            String groupName=((CmsGroup)cmsGroups.elementAt(i)).getName();
            CmsUUID groupId=((CmsGroup)cmsGroups.elementAt(i)).getId();
            template.setData("name",groupName);
            template.setData("value",groupId.toString());
            if (!group.equals("") && (cms.readGroup(new CmsUUID(group)).getName()).equals(groupName)) {
                template.setData("check","selected");
            }else if(m_channelId.equals(C_UNKNOWN_ID+"") && groupName.equals(defaultGroup)){
                template.setData("check","selected");
            }else{
                template.setData("check","");
            }
            groupOptions=groupOptions+template.getProcessedDataValue("selectoption",this);
        }
        template.setData("groups",groupOptions);
        // select box of owner
        String userOptions="";
        Vector cmsUsers=cms.getUsers();
        for (int i=0;i<cmsUsers.size();i++) {
            String userName=((CmsUser)cmsUsers.elementAt(i)).getName();
            CmsUUID userId=((CmsUser)cmsUsers.elementAt(i)).getId();
            template.setData("name",userName);
            template.setData("value",userId.toString());
            if (!owner.equals("") && (cms.readUser(new CmsUUID(owner)).getName()).equals(userName)) {
                template.setData("check","selected");
            }else if(m_channelId.equals(C_UNKNOWN_ID+"") && userName.equals(defaultUser)){
                template.setData("check","selected");
            }else{
                template.setData("check","");
            }
            userOptions=userOptions+template.getProcessedDataValue("selectoption",this);
        }
        template.setData("users",userOptions);
        // set the access flags
        this.setAccessValue(template, m_channel.getAccessFlags());
        //set data in template
        template.setData("channelId", ""+m_channel.getChannelId());
        template.setData("resourceid", m_channel.getId().toString());
        template.setData("channelName", m_channel.getChannelName());
        template.setData("title", Encoder.escape(m_channel.getTitle(),
            cms.getRequestContext().getEncoding()));
        template.setData("parentName", m_channel.getParentName());
        template.setData("error", error);

        //if the saveexit-button has been clicked.
        if(action.equals("saveexit") && error.equals("")){
            try{
                m_channel.setChannelId(channelId);
                //write/save
                m_channel.write(cms);
                //exit
                clearSession(session);
                templateSelector = "done";
                return startProcessing(cms, template, "", parameters, templateSelector);
            }catch (CmsException exc){
                template.setData("channelId", channelId);
                template.setData("resourceid", m_channel.getId().toString());
                template.setData("channelName", channelname);
                template.setData("title", Encoder.escape(title,
                    cms.getRequestContext().getEncoding()));
                template.setData("parentName", parentName);
                error = lang.getLanguageValue(moduleName+".error.message3")+" "+exc.getShortException();
                template.setData("error", error);
                //log
                exc.printStackTrace(System.err);
            }catch (Exception e){
                template.setData("channelId", channelId);
                template.setData("resourceid", m_channel.getId().toString());
                template.setData("channelName", channelname);
                template.setData("title", Encoder.escape(title,
                    cms.getRequestContext().getEncoding()));
                template.setData("parentName", parentName);
                error = lang.getLanguageValue(moduleName+".error.message3")+" "+e.getMessage();
                template.setData("error", error);
                //log
                e.printStackTrace(System.err);
            }
        }

        //if the "save"-button has been clicked.
        if(action.equals("save") && error.equals("")){
            try{
                m_channel.setChannelId(channelId);                
                //write
                m_channel.write(cms);
                m_channelId=m_channel.getChannelId();
                template.setData("channelId", m_channelId);
                template.setData("resourceid", m_channel.getId().toString());
                //indicator for saved new entrys - the new id
                template.setData("newChannelId", m_channelId);
            }catch (CmsException exc){
                template.setData("channelId", channelId);
                template.setData("resourceid", m_channel.getId().toString());
                template.setData("channelName", channelname);
                template.setData("title", Encoder.escape(title,
                    cms.getRequestContext().getEncoding()));
                template.setData("parentName", parentName);
                error = lang.getLanguageValue(moduleName+".error.message3")+" "+exc.getShortException();
                template.setData("error", error);
                //log
                exc.printStackTrace(System.err);
            }catch (Exception e){
                e.printStackTrace(System.err);
                template.setData("channelId",  channelId);
                template.setData("resourceid", m_channel.getId().toString());
                template.setData("channelName", channelname);
                template.setData("title", Encoder.escape(title,
                    cms.getRequestContext().getEncoding()));
                template.setData("parentName", parentName);
                error = lang.getLanguageValue(moduleName+".error.message3")+" "+e.getMessage();
                template.setData("error", error);
            }
        }
        //finally start the processing
        return startProcessing(cms, template, elementName, parameters, templateSelector);
    }

    /**
     * get the accessFlags from the template
     */
    private int getAccessValue(Hashtable parameters){
        int accessFlag = 0;
        for(int i=0; i<=8; i++){
            String permissionsAtI=(String)parameters.get("permissions"+i);
            if(permissionsAtI != null) {
                if(permissionsAtI.equals("on")) {
                    accessFlag += new Integer(C_ACCESS_FLAGS[i]).intValue();
                }
            }
        }
        if(accessFlag == 0){
            accessFlag = C_DEFAULT_PERMISSIONS;
        }
        return accessFlag;
    }

    /**
     * Set the accessFlags in the template
     */
     private void setAccessValue(CmsXmlWpTemplateFile template, int accessFlags){
        // permissions check boxes
        for(int i=0; i<=8; i++) {
            int accessValueAtI = new Integer(C_ACCESS_FLAGS[i]).intValue();
            if ((accessFlags & accessValueAtI) > 0) {
                template.setData("permissions_check_"+i,"checked");
            } else {
                template.setData("permissions_check_"+i,"");
            }
        }
     }
    /**
     * remove all session values used by outputchannel Backoffice
     */
    private void clearSession(I_CmsSession session){
        session.removeValue("parentName");
        session.removeValue("id");
    }
    /**
     *
     */
    public boolean isExtendedList(){
        return true;
    }
}