/**
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

package com.opencms.defaults;

import com.opencms.defaults.master.*;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.core.*;
import org.opencms.file.*;
import org.opencms.main.*;

import com.opencms.workplace.*;
import com.opencms.template.*;

import java.util.*;

/**
 * Backoffice class with channel functionality
 * @author Michael Dernen
 */
public abstract class A_CmsChannelBackoffice extends A_CmsBackoffice {

    /**
     * UserMethod creates a selectBox for selected channels
     *
     * @param cms CmsObject Object for accessing system resources
     * @param lang CmsXmlLanguageFile the language file
     * @param values Vector for the values in the selectBox
     * @param names Vector for the names in the selectBox
     * @param parameters Hashtable with all template class parameters.
     * @return Integer selected Index Value of selectBox
     * @throws CmsException in case of unrecoverable errors
     */
    public Integer getSelectedChannels(CmsObject cms, CmsXmlLanguageFile lang, Vector values, Vector names, Hashtable parameters)
                    throws CmsException {
        int retValue = -1;
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);        
        CmsMasterContent cd = (CmsMasterContent)session.getValue(getContentDefinitionClass().getName());
        Vector channels = cd.getSelectedChannels();
        int size = channels.size();
        for (int i=0; i < size; i++) {
            values.add(channels.elementAt(i));
            names.add(channels.elementAt(i));
        }
        return new Integer(retValue);
    }

    /**
     * UserMethod creates a selectBox for available channels
     *
     * @param cms CmsObject Object for accessing system resources
     * @param lang CmsXmlLanguageFile
     * @param values Vector for the values in the selectBox
     * @param names Vector for the names in the selectBox
     * @param parameters Hashtable with all template class parameters.
     * @return Integer selected Index Value of selectBox
     * @throws org.opencms.main.CmsException in case of unrecoverable errors
     */
    public Integer getAvailableChannels(CmsObject cms, CmsXmlLanguageFile lang, Vector values, Vector names, Hashtable parameters)
                    throws CmsException {
        int retValue = -1;
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsMasterContent cd = (CmsMasterContent)session.getValue(getContentDefinitionClass().getName());
        Vector channels = cd.getAvailableChannels(cms);
        int size = channels.size();
        for (int i=0; i < size; i++) {
            values.add(channels.elementAt(i));
            names.add(channels.elementAt(i));
        }
        return new Integer(retValue);
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
     * @throws org.opencms.main.CmsException in case of unrecoverable errors
     */
    public Integer getMediaPosition(CmsObject cms, CmsXmlLanguageFile lang, Vector values, Vector names, Hashtable parameters)
                    throws CmsException {
        //get session
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        //selected media content definition
        //media_position
        String media_position = null;
        int media_positionInt=0;
        //content of the select box
        String selectBoxContent="";
        //flag
        boolean flag=false;
        try {
            //get the content
            selectBoxContent=selectBoxContent(cms);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        StringTokenizer t = new StringTokenizer(selectBoxContent, ";");
        while (t.hasMoreElements()) {
            String s=t.nextToken();
            if (s!=null && !s.equals("")) {
                int z=s.indexOf(":");
                values.add(s.substring(0, z));
                String name=s.substring(z+1);
                names.add(name);
                //if there is a 0 as position don't
                if (name.equals("0")) {
                  flag=true;
                }
            }
        }
        //try to get the medias from session
        try {
                media_position=(String)session.getValue("media_position");
                media_positionInt=Integer.parseInt(media_position);
                if (!flag) {
                    media_positionInt=Integer.parseInt(media_position)-1;
                }
        } catch (Exception e) {
                media_positionInt=0;
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
         * @param userObject Hashtable with parameters.
         * @return String or byte[] with the content of this subelement.
         * @throws CmsException if something goes wrong
         */
        public Object getContentMedia(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
            //return value
            String content="";
            String row="";
            //get the template
            CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile) doc;
            I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
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
            media_title = org.opencms.i18n.CmsEncoder.unescape(media_title,
                cms.getRequestContext().getEncoding());
            // get the pos
            String pos = (String) parameters.get("pos");
            pos=(pos!=null?pos.trim():"");
            if (pos.equals("")) {
                //if your are in the edit modus get another pos
                pos = (String) parameters.get("posEdit");
                pos=(pos!=null?pos.trim():"");
            }
            int posInt=I_CmsConstants.C_UNKNOWN_ID;
            if (!pos.equals("")) {
                try {
                    posInt=Integer.parseInt(pos);
                } catch (Exception e) {
                    posInt=I_CmsConstants.C_UNKNOWN_ID;
                }
            }
            // get the media_position
            String media_position = (String) parameters.get("media_position");
            media_position=(media_position!=null?media_position.trim():"");
            int media_positionInt=0;
            if (!media_position.equals("")) {
                try {
                    media_positionInt=Integer.parseInt(media_position);
                } catch (Exception e) {
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
            try {
                    media=(Vector)session.getValue("media");
            } catch (Exception e) {
                    e.printStackTrace(System.err);
            }
            //get the selected CmsMasterMedia
            if (dbmedia!=null) {
            //if you are here for the fist time - get the medias from db
                if (media==null) {
                //copy medias in your vector
                    media=new Vector();
                    //media=(Vector)dbmedia.clone();
                    for (int i=0; i<dbmedia.size(); i++) {
                        CmsMasterMedia cd = (CmsMasterMedia)dbmedia.elementAt(i);
                        //if the type is == paragraph1
                        if (cd.getType()>=0) {
                            media.add(cd);
                        }
                    }
                }
                for (int i=0; i<media.size(); i++) {
                    if (posInt==i) {
                        selectedmediaCD=(CmsMasterMedia)media.elementAt(i);
                    }
                }
            }
            //delete media
            if (media_action.equals("delPicture")) {
                if (selectedmediaCD!=null) {
                    masterCD.deleteMedia(selectedmediaCD);
                    //remove CD from vector
                    media.removeElementAt(posInt);
                }
            } else if (media_action.equals("addPicture") && !media_name.equals("unknown")) {
                    // get the picture file and/or file from the RequestContext if there is any
                    CmsRequestContext reqCont = cms.getRequestContext();
                    Enumeration enum = CmsXmlTemplateLoader.getRequest(reqCont).getFileNames();
                    String filename = "";
                    byte[] mediafile = null;
                    //get the file
                    while (enum.hasMoreElements()) {
                        filename = (String) enum.nextElement();
                        if (!filename.equals("unknown")) {
                            mediafile = CmsXmlTemplateLoader.getRequest(reqCont).getFile(filename);
                            //set the media_mimetype
                            media_mimetype=CmsMasterMedia.computeMimetype(cms, filename);
                            //set the type
                            if (!media_mimetype.startsWith("image")) {
                                media_type=1;
                            }
                        }
                    }
                    if (mediafile!=null) {
                        //new media
                        mediaCD=new CmsMasterMedia(media_positionInt, 0, 0, mediafile.length, media_mimetype, media_type, media_title, media_name, "default", mediafile);
                        if (mediaCD!=null) {
                            //add media
                            masterCD.addMedia(mediaCD);
                            //add Cd to vector
                            media.addElement(mediaCD);
                            //set media_position back to default
                            media_position="";
                        }
                    } else {
                        //set new values
                        selectedmediaCD.setName(media_name);
                        selectedmediaCD.setTitle(media_title);
                        selectedmediaCD.setPosition(media_positionInt);
                        //set media_position back to default
                        media_position="";
                        //update media
                        masterCD.updateMedia(selectedmediaCD);
                        //update the vector
                        media.insertElementAt(selectedmediaCD, posInt);
                        //remove old CD
                        media.removeElementAt(posInt+1);
                    }
            }
            //empty the input fields and select box
            if (media_action.equals("clear")) {
                //set media_position back to default
                media_position="";
            }
            //open window on reload
            if (media_action.equals("prevPicture") && (selectedmediaCD != null)) {
                //set the url
                templateFile.setData("sid", "" + masterCD.getSubId());
                templateFile.setData("cid", "" + selectedmediaCD.getMasterId());
                templateFile.setData("mid", "" + selectedmediaCD.getId());
                templateFile.setData("file", "" + selectedmediaCD.getName());

                templateFile.setData("preview", templateFile.getProcessedDataValue("media_preview", this));
            } else {
                //set the url to default
                templateFile.setData("preview", "");
            }
            //special template for edit
            if (media_action.equals("editPicture") && (selectedmediaCD!=null)) {
                //set media_position back to default
                media_position=""+selectedmediaCD.getPosition();
                //put the media_position in session
                session.putValue("media_position", media_position);
                //fill the template
                templateFile.setData("media_name", selectedmediaCD.getName());
                templateFile.setData("posEdit", pos);
                String title=selectedmediaCD.getTitle();
                title=org.opencms.i18n.CmsEncoder.unescape(title,
                    cms.getRequestContext().getEncoding());
                templateFile.setData("media_alt_text", title);
                templateFile.setData("media_file", templateFile.getProcessedDataValue("media_edit", this));
            } else {
                //put the media_position in session
                session.putValue("media_position", media_position);
                //no edit - build normal template
                templateFile.setData("media_file", templateFile.getProcessedDataValue("media_upload", this));
            }
            //fill the list
            if (media!=null) {
                //content of the position select box
                String selectBoxContent="";
                try {
                    //get the content
                    selectBoxContent=selectBoxContent(cms);
                } catch (Exception e) {
                }
                for (int i=0; i<media.size(); i++) {
                    mediaCD= (CmsMasterMedia)media.elementAt(i);
                    templateFile.setData("pos", ""+i);
                    templateFile.setData("media_row_name", mediaCD.getName());
                    templateFile.setData("media_title", mediaCD.getTitle());
                    //get the name to the value
                    String mediapos=""+mediaCD.getPosition();
                    StringTokenizer t = new StringTokenizer(selectBoxContent, ";");
                    while (t.hasMoreElements()) {
                        String s=t.nextToken();
                        if (s!=null && !s.equals("")) {
                            int z=s.indexOf(":");
                            String value=s.substring(0, z);
                            String name=s.substring(z+1);
                            if (mediapos.equals(name)) {
                                mediapos=value;
                            }
                        }
                    }
                    templateFile.setData("media_position", ""+mediapos);
                    templateFile.setData("media_size", ""+mediaCD.getSize());
                    templateFile.setData("media_type", ""+mediaCD.getType());
                    //set String
                    row += templateFile.getProcessedDataValue("media_row", this);
                    //set back
                    mediapos="";
                }
                templateFile.setData("media_line", row);
            }
            //build the content
            content=templateFile.getProcessedDataValue("media_content", this);
            //put the vector in session
            if (media!=null) {
                session.putValue("media", media);
            }
            //put the vector in session
            if (selectedmediaCD!=null) {
                session.putValue("selectedmediaCD", selectedmediaCD);
            }
            //return
            return content;
    }

    /**
     * Method to return option values for media-position selectbox
     * has to be overriden to provide values and names for the selectbox
     * 
     * @param cms CmsObject to access system resources
     * @return the option values
     */
    protected String selectBoxContent(CmsObject cms) {
        return "";
    }
}