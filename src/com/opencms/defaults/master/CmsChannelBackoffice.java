/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsChannelBackoffice.java,v $
 * Author : $Author: a.schouten $
 * Date   : $Date: 2001/11/19 15:11:08 $
 * Version: $Revision: 1.4 $
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

    /**
     * UserMethod creates a selectBox for selected channels
     *
     * @param cms CmsObject Object for accessing system resources
     * @param lang CmsXmlLanguageFile
     * @param values Vector for the values in the selectBox
     * @param names Vector for the names in the selectBox
     * @param parameters Hashtable with all template class parameters.
     * @return Integer selected Index Value of selectBox
     * @throws com.opencms.core.CmsException in case of unrecoverable errors
     */
    public Integer getMediaPosition(CmsObject cms,CmsXmlLanguageFile lang,Vector values,Vector names,Hashtable parameters)
                    throws CmsException{
        //get session
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsMasterContent masterCD = (CmsMasterContent)session.getValue(getContentDefinitionClass().getName());
        //selected media content definition
        CmsMasterMedia selectedmediaCD=null;
        //media_position
        String media_position = null;
        int media_positionInt=0;
        //try to get the medias from session
        try{
                media_position=(String)session.getValue("media_position");
                media_positionInt=Integer.parseInt(media_position)-1;
        }catch(Exception e){
                media_positionInt=0;
        }
        String selectBoxContent="";
        try{
            //get the content
            selectBoxContent=selectBoxContent(cms);
        }catch(Exception e){
            e.printStackTrace(System.err);
        }
        StringTokenizer t = new StringTokenizer(selectBoxContent,";");
        while(t.hasMoreElements()){
            String s=t.nextToken();
            if(s!=null && !s.equals("")){
                int z=s.indexOf(":");
                values.add(s.substring(0,z));
                names.add(s.substring(z+1));
            }
        }
        return new Integer(media_positionInt);
    }

	 /**
	     * get the complete page to edit,delete and upload files and pictures
	     * <P>
	     * This method can be called using <code>&lt;METHOD name="getTitle"&gt;</code>
	     * in the template file.
	     *
	     * @param cms CmsObject Object for accessing system resources.
	     * @param tagcontent Unused in this special case of a user method. Can be ignored.
	     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
	     * @param userObj Hashtable with parameters.
	     * @return String or byte[] with the content of this subelement.
	     * @exception CmsException
	     */
	    public Object getContentMedia(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
	        //return value
	        String content="";
	        System.err.println("");
	        String row="";
	        //get the template
	        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile) doc;
	        I_CmsSession session = cms.getRequestContext().getSession(true);
	        CmsMasterContent masterCD = (CmsMasterContent)session.getValue(getContentDefinitionClass().getName());
	        //get the parameter
	        Hashtable parameters = (Hashtable)userObject;
	        // get the action
	        String media_action = (String) parameters.get("media_action");
	        media_action=(media_action!=null?media_action.trim():"");
	        // get the media_name
	        String media_name = (String) parameters.get("media_name");
	        media_name=(media_name!=null?media_name.trim():"");
	        // get the alt_text
	        String media_title = (String) parameters.get("media_alt_text");
	        media_title=(media_title!=null?media_title.trim():"");
	        // get the pos
	        String pos = (String) parameters.get("pos");
	        pos=(pos!=null?pos.trim():"");
	        if(pos.equals("")){
	            //if your are in the edit modus get another pos
	            pos = (String) parameters.get("posEdit");
	            pos=(pos!=null?pos.trim():"");
	        }
	        int posInt=I_CmsConstants.C_UNKNOWN_ID;
	        if(!pos.equals("")){
	            try{
	                posInt=Integer.parseInt(pos);
	            }catch(Exception e){
	                posInt=I_CmsConstants.C_UNKNOWN_ID;
	            }
	        }
	        // get the media_position
	        String media_position = (String) parameters.get("media_position");
	        media_position=(media_position!=null?media_position.trim():"");
	        int media_positionInt=0;
	        if(!media_position.equals("")){
	            try{
	                media_positionInt=Integer.parseInt(media_position);
	            }catch(Exception e){
	                media_positionInt=0;
	            }
	        }
	        //the media_type
	        int media_type=0;
	        //the media_mimetype
	        String media_mimetype="";
	        //media content definition
	        CmsMasterMedia mediaCD=null;
	        //selected media content definition
	        CmsMasterMedia selectedmediaCD=null;
	        //get all media files from this content definition
	        Vector dbmedia = masterCD.getMedia();
	        //Vector
	        Vector media = null;
	        //try to get the medias from session
	        try{
	                media=(Vector)session.getValue("media");
	        }catch(Exception e){
	                e.printStackTrace(System.err);
	        }
	        //get the selected CmsMasterMedia
	        if(dbmedia!=null){
	        //if you are here for the fist time - get the medias from db
	            if(media==null){
	            //copy medias in your vector
	                media=(Vector)dbmedia.clone();
	            }
	            for(int i=0;i<media.size();i++){
	                if(posInt==i){
	                    selectedmediaCD=(CmsMasterMedia)media.elementAt(i);
	                }
	            }
	        }
	        //delete media
	        if(media_action.equals("delPicture")){
	            if(selectedmediaCD!=null){
	                masterCD.deleteMedia(selectedmediaCD);
	                //remove CD from vector
	                media.removeElementAt(posInt);
	            }
	        }else if(media_action.equals("addPicture") && !media_name.equals("unknown")){
	                // get the picture file and/or file from the RequestContext if there is any
	                CmsRequestContext reqCont = cms.getRequestContext();
	                Enumeration enum = reqCont.getRequest().getFileNames();
	                String filename = "";
	                byte[] mediafile = null;
	                //get the file
	                while (enum.hasMoreElements()) {
	                    filename = (String) enum.nextElement();
	                    if (!filename.equals("unknown")) {
	                        mediafile = reqCont.getRequest().getFile(filename);
	                        //set the media_mimetype
	                        media_mimetype=CmsMasterMedia.computeMimetype(cms,filename);
	                        //set the type
	                        if(!media_mimetype.startsWith("image")){
	                            media_type=1;
	                        }
	                    }
	                }
	                if(mediafile!=null){
	                    //new media
	                    mediaCD=new CmsMasterMedia(media_positionInt,0,0,mediafile.length,media_mimetype,media_type,media_title,media_name,"default",mediafile);
	                    if(mediaCD!=null){
	                        //add media
	                        masterCD.addMedia(mediaCD);
	                        //add Cd to vector
	                        media.addElement(mediaCD);
	                        //set media_position back to default
	                        media_position="";
	                    }
	                }else{
	                    //set new values
	                    selectedmediaCD.setName(media_name);
	                    selectedmediaCD.setTitle(media_title);
	                    selectedmediaCD.setPosition(media_positionInt);
	                    //set media_position back to default
	                    media_position="";
	                    //update media
	                    masterCD.updateMedia(selectedmediaCD);
	                    //update the vector
	                    media.insertElementAt(selectedmediaCD,posInt);
	                    //remove old CD
	                    media.removeElementAt(posInt+1);
	                }
	        }
	        //empty the input fields and select box
	        if(media_action.equals("clear")){
	            //set media_position back to default
	            media_position="";
	        }
	        //open window on reload
	        if(media_action.equals("prevPicture")){
	            //set the url
	            templateFile.setData("preview",templateFile.getProcessedDataValue("media_preview",this));
	        }else{
	        System.err.println("prevPicture else");
	            //set the url to default
	            templateFile.setData("preview","");
	        }
	        //special template for edit
	        if(media_action.equals("editPicture")){
	            //set media_position back to default
	            media_position=""+selectedmediaCD.getPosition();
	            //put the media_position in session
	            session.putValue("media_position",media_position);
	            //fill the template
	            templateFile.setData("media_name",selectedmediaCD.getName());
	            templateFile.setData("posEdit",pos);
	            templateFile.setData("media_alt_text",selectedmediaCD.getTitle());
	            templateFile.setData("media_file",templateFile.getProcessedDataValue("media_edit",this));
	        }else{
	            //put the media_position in session
	            session.putValue("media_position",media_position);
	            //no edit - build normal template
	            templateFile.setData("media_file",templateFile.getProcessedDataValue("media_upload",this));
	        }
	        //fill the list
	        if(media!=null){
	            for(int i=0;i<media.size();i++){
	                mediaCD= (CmsMasterMedia)media.elementAt(i);
	                templateFile.setData("pos",""+i);
	                templateFile.setData("media_row_name",mediaCD.getName());
	                templateFile.setData("media_title",mediaCD.getTitle());
	                templateFile.setData("media_position",""+mediaCD.getPosition());
	                templateFile.setData("media_size",""+mediaCD.getSize());
	                templateFile.setData("media_type",""+mediaCD.getType());
	                //set String
	                row+=templateFile.getProcessedDataValue("media_row",this);
	            }
	            templateFile.setData("media_line",row);
	        }
	        //build the content
	        content=templateFile.getProcessedDataValue("media_content",this);
	        //put the vector in session
	        if(media!=null){
	        session.putValue("media",media);
	        }
	        //put the vector in session
	        if(selectedmediaCD!=null){
	            session.putValue("selectedmediaCD",selectedmediaCD);
	        }
	        //return
	        return content;
    }

    //abtract for selectBox
    protected abstract String selectBoxContent(CmsObject cms);

}