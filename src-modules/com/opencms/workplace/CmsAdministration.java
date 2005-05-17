/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdministration.java,v $
* Date   : $Date: 2005/05/17 13:47:28 $
* Version: $Revision: 1.1 $
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


package com.opencms.workplace;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsTemplateClassManager;
import com.opencms.template.CmsXmlTemplateFile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class is used to display the administration view.
 *
 * Creation date: (09.08.00 14:01:21)
 * @author Hanjo Riege
 * @version $Name:  $ $Revision: 1.1 $ $Date: 2005/05/17 13:47:28 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsAdministration extends CmsWorkplaceDefault {

    /**
     * Returns the complete Icon.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateDocument contains the icondefinition.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @param picName the basic name of the picture.
     * @param sender the name of the icon incl. path.
     * @param languageKey the the key for the languagefile.
     * @param iconActiveMethod the method for decision if icon is active, or null.
     * @param iconVisibleMethod the method for decision if icon is visible,  or null.
     */

    private String generateIcon(CmsObject cms, CmsXmlTemplateFile templateDocument, Hashtable parameters,
            CmsXmlLanguageFile lang, String picName, String sender, String languageKey,
                    String iconActiveMethod, String iconVisibleMethod, String accessVisible) throws CmsException {

        boolean hasAccessVisible = (new Boolean(accessVisible)).booleanValue();
        String iconPicPath = (String)resourcesUri(cms, "", null, null);
        // change the iconPicPath if the point is from a module
        if(sender.startsWith(C_VFS_PATH_SYSTEM + "modules")) {
            if (picName.startsWith("/")) {
                iconPicPath = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl() + sender.substring(0, sender.indexOf("/administration/"));
            }
            else {
                iconPicPath = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl() + sender.substring(0, sender.indexOf("administration/")) + "pics/";
            }
        }

        // call the method for activation decision
        boolean activate = true;
        if(iconActiveMethod != null && !"".equals(iconActiveMethod)) {
            String className = iconActiveMethod.substring(0, iconActiveMethod.lastIndexOf("."));
            iconActiveMethod = iconActiveMethod.substring(iconActiveMethod.lastIndexOf(".") + 1);
            Method groupsMethod = null;
            try {
                Object o = CmsTemplateClassManager.getClassInstance(className);
                groupsMethod = o.getClass().getMethod(iconActiveMethod, new Class[] {
                    CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class
                });
                activate = ((Boolean)groupsMethod.invoke(o, new Object[] {
                    cms, lang, parameters
                })).booleanValue();

            }
            catch(NoSuchMethodException exc) {

                // The requested method was not found.
                throwException("Could not find icon activation method " + iconActiveMethod
                        + " in calling class " + className + " for generating icon.", CmsLegacyException.C_NOT_FOUND);
            }
            catch(InvocationTargetException targetEx) {

                // the method could be invoked, but throwed a exception
                // itself. Get this exception and throw it again.
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {

                    throwException("Icon activation method " + iconActiveMethod + " in calling class "
                            + className + " throwed an exception. " + e, CmsLegacyException.C_UNKNOWN_EXCEPTION);
                }
                else {

                    // This is a CmsException
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }
            catch(Exception exc2) {
                throwException("Icon activation method " + iconActiveMethod + " in calling class "
                        + className + " was found but could not be invoked. " + exc2, CmsLegacyException.C_UNKNOWN_EXCEPTION);
            }
        }

        // call the method for the visibility decision
        boolean visible = true;
        if(iconVisibleMethod != null && !"".equals(iconVisibleMethod)) {
            String className = iconVisibleMethod.substring(0, iconVisibleMethod.lastIndexOf("."));
            iconVisibleMethod = iconVisibleMethod.substring(iconVisibleMethod.lastIndexOf(".") + 1);
            Method groupsMethod = null;
            try {
                Object o = CmsTemplateClassManager.getClassInstance(className);
                groupsMethod = o.getClass().getMethod(iconVisibleMethod, new Class[] {
                    CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class
                });
                visible = ((Boolean)groupsMethod.invoke(o, new Object[] {
                    cms, lang, parameters
                })).booleanValue();
            }
            catch(NoSuchMethodException exc) {

                // The requested method was not found.
                throwException("Could not find icon activation method " + iconVisibleMethod
                        + " in calling class " + className + " for generating icon.", CmsException.C_NOT_FOUND);
            }
            catch(InvocationTargetException targetEx) {

                // the method could be invoked, but throwed a exception

                // itself. Get this exception and throw it again.
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {

                    throwException("Icon activation method " + iconVisibleMethod + " in calling class "
                            + className + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
                }
                else {

                    // This is a CmsException

                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }
            catch(Exception exc2) {
                throwException("Icon activation method " + iconVisibleMethod + " in calling class "
                        + className + " was found but could not be invoked. " + exc2, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        templateDocument.setData("linkTo", CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl() + C_VFS_PATH_WORKPLACE 
                + "action/administration_content_top.html?sender=" + sender);
        StringBuffer iconLabelBuffer = new StringBuffer(lang.getLanguageValue(languageKey));

        // Insert a html-break, if needed
        if(iconLabelBuffer.toString().indexOf("- ") != -1) {
            iconLabelBuffer.insert(iconLabelBuffer.toString().indexOf("- ") + 1, "<BR>");
        }
        templateDocument.setData("linkName", iconLabelBuffer.toString());
        if(visible && hasAccessVisible) {
            if(activate) {
                templateDocument.setData("picture", iconPicPath + picName + ".gif");
                return templateDocument.getProcessedDataValue("defaulticon");
            }
            else {
                templateDocument.setData("picture", iconPicPath + picName + "_in.gif");
                return templateDocument.getProcessedDataValue("deactivatedicon");
            }
        }
        else {
            return templateDocument.getProcessedDataValue("noicon");
        }
    } // of generateIcon

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {       
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsXmlWpTemplateFile templateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = templateDocument.getLanguageFile();
        String navPos = (String)session.getValue(C_SESSION_ADMIN_POS);
        templateDocument.setData("emptyPic", (String)resourcesUri(cms, "empty.gif", null, null));
        CmsXmlWpConfigFile confFile = new CmsXmlWpConfigFile(cms);
        String sentBy = (String)parameters.get("sender");      
        
        if(sentBy == null) {
            if(navPos == null) {
                sentBy = confFile.getWorkplaceAdministrationPath();
            }
            else {
                if(!navPos.endsWith("/")) {
                    navPos = navPos.substring(0, navPos.indexOf("/") + 1);
                }
                sentBy = navPos;
            }
        }
        
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element "
                    + ((elementName == null) ? "<root>" : elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: "
                    + ((templateSelector == null) ? "<default>" : templateSelector));
            OpenCms.getLog(this).debug("SentBy: " + sentBy );                    
        }          
        
        List iconVector = (List) new ArrayList();
        if(sentBy.endsWith("/administration/")) {

            // we must serch for administrationPoints in AdminPath and in system/modules/..
            sentBy = confFile.getWorkplaceAdministrationPath();
            iconVector = cms.getSubFolders(sentBy);
            List modules = (List) new ArrayList();

            modules = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_MODULES);

            for(int i = 0;i < modules.size();i++) {
                List moduleAdminPoints = (List) new ArrayList();                
                String dummy = cms.getSitePath((CmsFolder)modules.get(i));
                dummy += "administration/";
                try {
                    moduleAdminPoints = cms.getSubFolders(dummy);
                } catch (CmsVfsResourceNotFoundException e) {
                    // folder does not exists
                    continue;
                } catch (CmsSecurityException e1) {
                    // no access to this adminstration point, skip it
                }
                for(int j = 0;j < moduleAdminPoints.size();j++) {
                    CmsFolder currentModuleAdminFolder = (CmsFolder) moduleAdminPoints.get(j);
                    iconVector.add(currentModuleAdminFolder);
                    //System.err.println( currentModuleAdminFolder.getResourceName() );
                }
            }
        }
        else {
            iconVector = cms.getSubFolders(sentBy);
        }
        session.putValue(C_SESSION_ADMIN_POS, sentBy);
        List iconVector2 = cms.getFilesInFolder(sentBy);
        int numFolders = iconVector.size();
        if(numFolders > 0) {
            String iconNames[] = new String[numFolders];
            int index[] = new int[numFolders];
            String folderTitles[] = new String[numFolders];
            String folderLangKeys[] = new String[numFolders];
            String folderPos[] = new String[numFolders];
            String folderVisible[] = new String[numFolders];
            String folderActiv[] = new String[numFolders];
            String accessVisible[] = new String[numFolders];
            for(int i = 0;i < numFolders;i++) {
                CmsResource aktIcon = (CmsResource)iconVector.get(i);
                try {
                    Map propertyinfos = cms.readProperties(cms.getSitePath(aktIcon));
                    iconNames[i] = cms.getSitePath(aktIcon);
                    index[i] = i;
                    folderLangKeys[i] = getStringValue((String)propertyinfos.get(C_PROPERTY_NAVTEXT));
                    folderTitles[i] = getStringValue((String)propertyinfos.get(C_PROPERTY_TITLE));
                    folderPos[i] = getStringValue((String)propertyinfos.get(C_PROPERTY_NAVPOS));
                    if(folderPos[i].equals("")) {
                        folderPos[i] = "101";
                    }
                    folderVisible[i] = getStringValue((String)propertyinfos.get(C_PROPERTY_VISIBLE));
                    folderActiv[i] = getStringValue((String)propertyinfos.get(C_PROPERTY_ACTIV));
                    accessVisible[i] = new Boolean(checkVisible(cms, aktIcon)).toString();
                } catch(CmsSecurityException e) {
                    // ignore all "access denied" type exceptions
                } catch(CmsException e) {
                    throw e;
                } catch(Throwable t) {
                    throw new CmsException("[" + this.getClass().getName() + "] "
                            + t.getMessage(), CmsException.C_SQL_ERROR, t);
                }
            } // end of for
            sort(iconNames, index, folderPos, numFolders);
            String completeTable = "";
            int element = 0;
            int zeile = 0;
            while(element < numFolders) {
                String completeRow = "";
                //while((element < numFolders) && (element < (zeile + 1) * C_ELEMENT_PER_ROW)) {
                while((element < numFolders)) {
                    int pos = index[element];
                    if(iconNames[element] != null){
                        completeRow += generateIcon(cms, templateDocument, parameters, lang, folderTitles[pos],
                            iconNames[element], folderLangKeys[pos], folderActiv[pos], folderVisible[pos],
                            accessVisible[pos]);
                    }
                    element++;
                }
                templateDocument.setData("entrys", completeRow);
                completeTable += templateDocument.getProcessedDataValue("list_row");
                zeile++;
            } // of while
            templateDocument.setData("iconTable", completeTable);
        }
        else {

            // no Folders, just a real page
            try {
                CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).sendCmsRedirect(sentBy
                        + "index.html?initial=true");
            }
            catch(Exception e) {
                throw new CmsException("Redirect fails :" + cms.getSitePath((CmsFile)iconVector2.get(0)),
                        CmsException.C_UNKNOWN_EXCEPTION, e);
            }
            return null;
        }
        return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
    }

    /**
     * returns the String or "" if it is null.
     * Creation date: (29.10.00 16:05:38)
     * @return java.lang.String
     * @param param java.lang.String
     */

    private String getStringValue(String param) {
        if(param == null) {
            return "";
        }
        return param;
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Sorts a set of arrays containing navigation information depending on
     * their navigation positions.
     * @param filenames Array of filenames
     * @param index Array of associate Strings
     * @param positions Array of navpostions
     */

    private void sort(String[] filenames, int[] index, String[] positions, int max) {

        // Sorting algorithm
        // This method uses an bubble sort, so replace this with something more
        // efficient
        try {
            for(int i = max - 1;i > 0;i--) {
                for(int j = 0;j < i;j++) {
                    float a = new Float(positions[j]).floatValue();
                    float b = new Float(positions[j + 1]).floatValue();
                    if(a > b) {
                        String tempfilename = filenames[j];
                        int tempindex = index[j];
                        String tempposition = positions[j];
                        filenames[j] = filenames[j + 1];
                        index[j] = index[j + 1];
                        positions[j] = positions[j + 1];
                        filenames[j + 1] = tempfilename;
                        index[j + 1] = tempindex;
                        positions[j + 1] = tempposition;
                    }
                }
            }
        }
        catch(Exception e) {
             if(OpenCms.getLog(this).isWarnEnabled()){
                 OpenCms.getLog(this).warn("Adminpoints unsorted cause I cant get a valid float value", e);
             }
        }
    } // of sort

    /**
     * Check if this resource should be displayed in the administrationview.
     * @param cms The CmsObject
     * @param resource The resource to be checked.
     * @return True or false.
     * @throws CmsException if something goes wrong.
     */

    private boolean checkVisible(CmsObject cms, CmsResource resource) throws CmsException {
    	return cms.hasPermissions(resource, CmsPermissionSet.ACCESS_VIEW);
    }
}
