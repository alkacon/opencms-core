/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleAdminEdit.java,v $
* Date   : $Date: 2003/09/17 14:30:13 $
* Version: $Revision: 1.21 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the conflicting Files for a new Module.
 * Creation date: (06.09.00 09:30:25)
 * @author Hanjo Riege
 */
public class CmsAdminModuleAdminEdit extends CmsWorkplaceDefault {

    /**
     *  Checks if the name is correct.
     *  @param name the name to check..
     */
    private boolean checkName(String name) {
        if(name == null || name.length() == 0 || name.trim().length() == 0) {
            return false;
        }
        for(int i = 0;i < name.length();i++) {
            char c = name.charAt(i);
            if(((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9')) && ((c < 'A') || (c > 'Z')) && (c != '-') && (c != '.') && (c != '_') && (c != '~')) {
                return false;
            }
        }
        return true;
    }

    /**
     *  Checks if the type of the value is correct and returns the converted value or null.
     *  @param type the type that the value should have..
     *  @param value the value to check.
     */
    private String checkType(String type, String value) {
        type = type.toLowerCase();
        try {
            if("string".equals(type)) {
                if((value != null) && (value.indexOf("\"") < 0)) {
                    return value;
                }
                else {
                    return null;
                }
            }
            else {
                if("int".equals(type) || "integer".equals(type)) {
                    value = "" + Integer.parseInt(value);
                    return value;
                }
                else {
                    if("float".equals(type)) {
                        value = "" + Float.valueOf(value);
                        return value;
                    }
                    else {
                        if("boolean".equals(type)) {
                            value = "" + Boolean.valueOf(value);
                            return value;
                        }
                        else {
                            if("long".equals(type)) {
                                value = "" + Long.valueOf(value);
                                return value;
                            }
                            else {
                                if("double".equals(type)) {
                                    value = "" + Double.valueOf(value);
                                    return value;
                                }
                                else {
                                    if("byte".equals(type)) {
                                        value = "" + Byte.valueOf(value);
                                        return value;
                                    }
                                    else {

                                        // the type dosen't exist
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception exc) {

            // the type of the value was wrong
            return null;
        }
    }

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
        if(OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).debug("Template file is: " + templateFile);
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        Hashtable sessionData = (Hashtable)session.getValue(C_SESSION_MODULE_ADMIN_DATA);
        String module = (String)sessionData.get(C_MODULE_PACKETNAME);
        xmlTemplateDocument.setData("packetname", module);
        Vector paraNames = (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_NAMES);
        Vector paraDescr = (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_DESCR);
        Vector paraTyp = (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_TYP);
        Vector paraVal = (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_VAL);
        String prop = (String)parameters.get("prop");
        String delete = (String)parameters.get("delete");
        String ok = (String)parameters.get("ok");
        String step = (String)parameters.get("step");
        if((prop == null) || ("".equals(prop))) {

            // new property
            if((ok != null) && (!"".equals(ok))) {

                // read new prop
                String name = getStringValue((String)parameters.get("NAME"));
                String description = getStringValue((String)parameters.get("BESCHREIBUNG"));
                String type = (String)parameters.get("TYP");
                String value = (String)parameters.get("WERT");

                //  check if all fields are filled out and if the value is correct
                String newValue = checkType(type, value);
                if((checkName(name)) && (newValue != null)) {
                    paraNames.addElement(name);
                    paraDescr.addElement(description);
                    paraTyp.addElement(type);
                    paraVal.addElement(newValue);
                    templateSelector = "done";
                }
                else {
                    session.putValue("parametername", name);
                    session.putValue("description", description);
                    session.putValue("parametertype", type);
                    session.putValue("parametervalue", value);
                    templateSelector = "errornew";
                }
            }
            else {
                if((step == null) || ("".equals(step))) {
                    xmlTemplateDocument.setData("paraname", "");
                    xmlTemplateDocument.setData("paranameok", "");
                    xmlTemplateDocument.setData("value", "");
                    xmlTemplateDocument.setData("description", "");
                    xmlTemplateDocument.setData("delybutton", " ");
                }
                else {

                    // from Errorpage
                    xmlTemplateDocument.setData("paraname", "");
                    xmlTemplateDocument.setData("paranameok", (String)session.getValue("parametername"));
                    xmlTemplateDocument.setData("value", (String)session.getValue("parametervalue"));
                    xmlTemplateDocument.setData((String)session.getValue("parametertype"), "selected");
                    xmlTemplateDocument.setData("description", (String)session.getValue("description"));
                    xmlTemplateDocument.setData("delybutton", " ");
                    session.removeValue("packagename");
                    session.removeValue("parametervalue");
                    session.removeValue("parametertype");
                    session.removeValue("description");
                }
            }
        }else {
            if((ok != null) && (!"".equals(ok))) {

                // set property
                String type = getStringValue((String)parameters.get("TYP"));
                String value = getStringValue((String)parameters.get("WERT"));
                String newValue = checkType(type, value);
                if(newValue != null) {
                    int i = paraNames.indexOf(prop);
                    paraNames.removeElementAt(i);
                    paraDescr.removeElementAt(i);
                    paraTyp.removeElementAt(i);
                    paraVal.removeElementAt(i);
                    paraNames.addElement(prop);
                    paraDescr.addElement(getStringValue((String)parameters.get("BESCHREIBUNG")));
                    paraTyp.addElement(type);
                    paraVal.addElement(newValue);
                    templateSelector = "done";
                }else {
                    session.putValue("parametername", prop);
                    session.putValue("description", getStringValue((String)parameters.get("BESCHREIBUNG")));
                    session.putValue("parametertype", type);
                    session.putValue("parametervalue", value);
                    templateSelector = "errorold";
                }
            }else {
                if((delete != null) && (!"".equals(delete))) {

                    // delete property
                    int i = paraNames.indexOf(prop);
                    paraNames.removeElementAt(i);
                    paraDescr.removeElementAt(i);
                    paraTyp.removeElementAt(i);
                    paraVal.removeElementAt(i);
                    templateSelector = "done";
                }else {

                    // prepare for change property
                    if((step == null) || ("".equals(step))) {
                        int i = paraNames.indexOf(prop);
                        xmlTemplateDocument.setData("paraname", prop);
                        xmlTemplateDocument.setData("nameentry", prop);
                        xmlTemplateDocument.setData("value", (String)paraVal.elementAt(i));
                        xmlTemplateDocument.setData("description", (String)paraDescr.elementAt(i));
                        xmlTemplateDocument.setData((String)paraTyp.elementAt(i), "selected");
                        xmlTemplateDocument.setData("delybutton", xmlTemplateDocument.getProcessedDataValue("deletebutton"));
                    }else {

                        // from errorpage errorold
                        prop = (String)session.getValue("parametername");
                        xmlTemplateDocument.setData("paraname", prop);
                        xmlTemplateDocument.setData("nameentry", prop);
                        xmlTemplateDocument.setData("value", (String)session.getValue("parametervalue"));
                        xmlTemplateDocument.setData("description", (String)session.getValue("description"));
                        xmlTemplateDocument.setData((String)session.getValue("parametertype"), "selected");
                        xmlTemplateDocument.setData("delybutton", xmlTemplateDocument.getProcessedDataValue("deletebutton"));
                        session.removeValue("packagename");
                        session.removeValue("parametervalue");
                        session.removeValue("parametertype");
                        session.removeValue("description");
                    }
                }
            }
        }
        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
}
