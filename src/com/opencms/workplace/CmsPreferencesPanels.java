/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsPreferencesPanels.java,v $
 * Date   : $Date: 2000/03/13 15:54:51 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the preference panels screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/03/13 15:54:51 $
 */
public class CmsPreferencesPanels extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
           
     /** Datablock value for checked */    
	 private static final String C_CHECKED = "CHECKED";
    
     /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the preferences panels template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The preferences panels template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        
       
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        
        String template="";
        String panel;
        String oldPanel;
        
        int explorerSettingsValue;
        
        // test values
        Enumeration keys = parameters.keys();
        String key;
        System.err.println("#####");
        while(keys.hasMoreElements()) {
	        key = (String) keys.nextElement();
	        System.err.print(key);
	        System.err.print(":");
	        System.err.println(parameters.get(key));
        }
        System.err.println("°°°°°");
        // test values end
                
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        //CmsXmlLanguageFile lang=new CmsXmlLanguageFile(cms);
        
        // check if the submit or ok button is selected. If so, update all values
        if ((parameters.get("SUBMIT") != null) || (parameters.get("OK") != null) ){
            // set settings for explorer panel
            int explorerSettings=getExplorerSettings(parameters);   
            session.putValue("EXPLORERSETTINGS",new Integer(explorerSettings).toString());
            cms.getRequestContext().currentUser().setAdditionalInfo(C_ADDITIONAL_INFO_EXPLORERSETTINGS,new Integer(explorerSettings).toString());
            
            // write the user data to the database
            cms.writeUser(cms.getRequestContext().currentUser());
        }
        
        // if the OK or cancel buttons are pressed return to the explorer and clear
        // the data in the session.
        if ((parameters.get("OK") != null) || (parameters.get("CANCEL") != null) ){
        session.removeValue("EXPLORERSETTINGS");
        try {
               cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_RELOAD);
            } catch (Exception e) {
                  throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_RELOAD,CmsException.C_UNKNOWN_EXCEPTION,e);
            }     
        }
                        
        // get the active panel value. This indicates which panel to be displayed.
        panel=(String)parameters.get("panel");
        if (panel != null) {
            template=panel;
            // this is the panel for setting the explorer panels
            if (panel.equals("explorer")) {              
                //get the actual user settings  
                // first try to read them from the session
                String explorerSettings=(String)session.getValue("EXPLORERSETTINGS");
                // if this fails, get the settings from the user obeject
                if (explorerSettings== null) {
                    explorerSettings=(String)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_EXPLORERSETTINGS);
                }
                //check if the default button was selected.
                // if so, reset the values to the default settings
                if (parameters.get("DEFAULT") !=null) {
                    explorerSettings=null;
                }
                
                // set them to default
                if (explorerSettings!= null) {
                    explorerSettingsValue=new Integer(explorerSettings).intValue();
                } else {
                    explorerSettingsValue=C_FILELIST_TITLE+C_FILELIST_TYPE+C_FILELIST_CHANGED;
                }
                             
                
                // now update the data in the template
                if ((explorerSettingsValue & C_FILELIST_TITLE) >0) {
                    xmlTemplateDocument.setXmlData("CHECKTITLE",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKTITLE"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_TYPE) >0) {
                    xmlTemplateDocument.setXmlData("CHECKTYPE",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKTYPE"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_CHANGED) >0) {
                    xmlTemplateDocument.setXmlData("CHECKCHANGED",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKCHANGED"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_SIZE) >0) {
                    xmlTemplateDocument.setXmlData("CHECKSIZE",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKSIZE"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_STATE) >0) {
                    xmlTemplateDocument.setXmlData("CHECKSTATE",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKSTATE"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_OWNER) >0) {
                    xmlTemplateDocument.setXmlData("CHECKOWNER",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKOWNER"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_GROUP) >0) {
                    xmlTemplateDocument.setXmlData("CHECKGROUP",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKGROUP"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_ACCESS) >0) {
                    xmlTemplateDocument.setXmlData("CHECKACCESS",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKACCESS"," ");
                }
                if ((explorerSettingsValue & C_FILELIST_LOCKED) >0) {
                    xmlTemplateDocument.setXmlData("CHECKLOCKEDBY",C_CHECKED);
                } else {
                    xmlTemplateDocument.setXmlData("CHECKLOCKEDBY"," ");
                }
            } else {
                // the explorer panel was not selected, so check if the explorer panel
                // data has to be stored
                oldPanel=(String)session.getValue(C_PARA_OLDPANEL);
                if (oldPanel != null) {
                    // the previous panel was the explorer panel, save all the date form there
                    if (oldPanel.equals("explorer")) {
                        int explorerSettings=getExplorerSettings(parameters);                      
                        session.putValue("EXPLORERSETTINGS",new Integer(explorerSettings).toString());
                    }
                }
            }
            
          session.putValue(C_PARA_OLDPANEL,panel);
        }
        
       
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    }
	
    /**
     * Calcualtes the settings for the explorer filelist form the data submitted in
     * the preference explorer panel
     * @param parameters Hashtable containing all request parameters
     * @return Explorer filelist flags.
     */
    private int getExplorerSettings(Hashtable parameters) {
        int explorerSettings=0;
        if (parameters.get("CBTITLE")!= null) {
            explorerSettings+=C_FILELIST_TITLE;
        }
        if (parameters.get("CBTYPE")!= null) {
            explorerSettings+=C_FILELIST_TYPE;
        }
        if (parameters.get("CBCHANGED")!= null) {
            explorerSettings+=C_FILELIST_CHANGED;
        }
        if (parameters.get("CBSIZE")!= null) {
            explorerSettings+=C_FILELIST_SIZE;
        }
        if (parameters.get("CBSTATE")!= null) {
            explorerSettings+=C_FILELIST_STATE;
        }
        if (parameters.get("CBOWNER")!= null) {
            explorerSettings+=C_FILELIST_OWNER;
        }
        if (parameters.get("CBGROUP")!= null) {
            explorerSettings+=C_FILELIST_GROUP;
        }
        if (parameters.get("CBACCESS")!= null) {
            explorerSettings+=C_FILELIST_ACCESS;
        }
        if (parameters.get("CBLOCKEDBY")!= null) {
            explorerSettings+=C_FILELIST_LOCKED;
        }
        return explorerSettings;
    }
    
     /**
     * User method to get the actual panel of the PReferences dialog.
     * <P>
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @exception CmsException
     * @see #commonPicsUrl
     */    
    public Object setPanel(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
        throws CmsException {
        
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        String panel=(String)session.getValue(C_PARA_OLDPANEL);
      
        return panel;
    }
    
}