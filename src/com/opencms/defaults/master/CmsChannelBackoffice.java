/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsChannelBackoffice.java,v $
 * Author : $Author: a.schouten $
 * Date   : $Date: 2001/11/20 16:41:07 $
 * Version: $Revision: 1.5 $
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

package com.opencms.defaults.master;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;
import com.opencms.defaults.*;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.*;


public abstract class CmsChannelBackoffice extends A_CmsBackoffice{

    /** Default value of permission*/
    protected final static int C_DEFAULT_PERMISSIONS=383;
    // possible accessflags
    protected final static String[] C_ACCESS_FLAGS = {"1","2","4","8","16","32","64","128","256"};
    //CmsChannelContent
    private CmsChannelContent cd = null;
    //int/Integer id
    private String idvalue = "0";
    /** UNNKNOWN ID */
    private static final int C_UNKNOWN_ID=-1;

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
     * 					 and id for the used content definition instance object
     *					 and the author, title, text content for setting the new/changed data
     * @param templateSelector template section that should be processed.
     * @return Processed content of the given template file.
     * @exception CmsException
     */
    public byte[] getContentEdit(CmsObject cms, CmsXmlWpTemplateFile template, String elementName,
							 Hashtable parameters, String templateSelector) throws CmsException {
        //get the Languagedata
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        //create appropriate class name with underscores for labels
        String moduleName = "";
        moduleName = (String) getClass().toString(); //get name
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

        //get value of id
        String id = (String) parameters.get("id");
        String channelid = (String) parameters.get("channelId");
        //get value of parentId
        String parentName = (String) parameters.get("parentName");
        if(parentName == null || parentName.equals("")){
            parentName="/";
        }
        //channelname
        String channelname = (String) parameters.get("channelName");
        if(channelname == null){
		    channelname = "";
        }
        //plausibility check
        if(!action.equals("") && channelname.trim().equals("")){
            error = lang.getLanguageValue(moduleName+".error.message2");
        }
        String owner=(String) parameters.get("owner");
        owner=(owner!=null?owner.trim():"");
        String group=(String) parameters.get("groupId");
        group=(group!=null?group.trim():"");

        int accessFlags = this.getAccessValue(parameters);

        //new channel
        if (id == null || id.equals("")){
            id = "new";
        }
        if(channelid==null || channelid.trim().equals("")){
            channelid="-1";
        }
        if(id.equals("new")){
            if(channelid.equals("-1")){
                cd=null;
            }else{
                id=channelid;
            }
        }
        // for the first call check if the id exist, i.e. if the category is to edit
        // and it is not created new one then read the information of category
        if (action.equals("") && !id.equals("new")) {
            try {
                Integer.parseInt(id);
            } catch (Exception err) {
                throw new CmsException(err.getMessage());
            }
            // gets the already existing ouputchannel's content definition.
            cd = new CmsChannelContent(cms,new Integer(id));
            parentName=cd.getParentName();
            idvalue=cd.getChannelId();
            channelname=cd.getChannelName();
            owner=new Integer(cd.getOwner()).toString();
            group=new Integer(cd.getGroupId()).toString();
            accessFlags=cd.getAccessFlags();
        }else if(cd==null){
            //create a new ouputchannels content definition.
            cd = new CmsChannelContent(cms);
            idvalue=C_UNKNOWN_ID+"";
        }
        //put parentId in session for user method
        session.putValue("parentName",parentName);
        session.putValue("id",idvalue+"");
        //set data in CD when data is correct
        if(error.equals("") && !action.equals("")){
            cd.setChannelId(idvalue);
            cd.setParentName(parentName);
            cd.setChannelName(channelname);
            cd.setGroup(new Integer(group).intValue());
            cd.setOwner(new Integer(owner).intValue());
            cd.setAccessFlags(accessFlags);
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
            int groupId=((CmsGroup)cmsGroups.elementAt(i)).getId();
            template.setData("name",groupName);
            template.setData("value",(new Integer(groupId)).toString());
            if (!group.equals("") && (cms.readGroup(Integer.parseInt(group)).getName()).equals(groupName)) {
                template.setData("check","selected");
            }else if(idvalue.equals(C_UNKNOWN_ID+"") && groupName.equals(defaultGroup)){
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
            int userId=((CmsUser)cmsUsers.elementAt(i)).getId();
            template.setData("name",userName);
            template.setData("value",(new Integer(userId)).toString());
            if (!owner.equals("") && (cms.readUser(Integer.parseInt(owner)).getName()).equals(userName)) {
                template.setData("check","selected");
            }else if(idvalue.equals(C_UNKNOWN_ID+"") && userName.equals(defaultUser)){
                template.setData("check","selected");
            }else{
                template.setData("check","");
            }
            userOptions=userOptions+template.getProcessedDataValue("selectoption",this);
        }
        template.setData("users",userOptions);
        // set the access flags
        this.setAccessValue(template, cd.getAccessFlags());
        //set data in template
        template.setData("channelId", ""+cd.getChannelId());
        template.setData("channelName", cd.getChannelName());
        template.setData("parentName", cd.getParentName());
        template.setData("error", error);

	    //if the saveexit-button has been clicked.
	    if(action.equals("saveexit") && error.equals("")){
            try{
                //write/save
                cd.write(cms);
                //exit
                clearSession(session);
                templateSelector = "done";
                return startProcessing(cms, template, "", parameters, templateSelector);
            }catch (CmsException exc){
                template.setData("channelId", idvalue);
                template.setData("channelName", channelname);
                template.setData("parentName", parentName);
                error = lang.getLanguageValue(moduleName+".error.message3")+" "+exc.getShortException();
                template.setData("error", error);
                //log
                exc.printStackTrace(System.err);
            }catch (Exception e){
                template.setData("channelId", idvalue);
                template.setData("channelName", channelname);
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
                //write
                cd.write(cms);
                idvalue=cd.getChannelId();
                template.setData("channelId", idvalue);
                //indicator for saved new entrys - the new id
                template.setData("newChannelId", idvalue);
            }catch (CmsException exc){
                template.setData("channelId", idvalue);
                template.setData("channelName", channelname);
                template.setData("parentName", parentName);
                error = lang.getLanguageValue(moduleName+".error.message3")+" "+exc.getShortException();
                template.setData("error", error);
                //log
                exc.printStackTrace(System.err);
            }catch (Exception e){
                e.printStackTrace(System.err);
                template.setData("channelId",  idvalue);
                template.setData("channelName", channelname);
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
                    accessFlag += new Integer(this.C_ACCESS_FLAGS[i]).intValue();
                }
            }
        }
        if(accessFlag == 0){
            accessFlag = this.C_DEFAULT_PERMISSIONS;
        }
        return accessFlag;
    }

    /**
     * Set the accessFlags in the template
     */
     private void setAccessValue(CmsXmlWpTemplateFile template, int accessFlags){
        // permissions check boxes
        for(int i=0; i<=8; i++) {
            int accessValueAtI = new Integer(this.C_ACCESS_FLAGS[i]).intValue();
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