/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsSecure.java,v $
 * Date   : $Date: 2005/03/31 10:08:46 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.commons;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for building the security and export settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/secure.jsp
 * </ul>
 *
 * @author  Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.5 $
 * 
 * @since 6.0
 */
public class CmsSecure extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "secure";

    /** Value for the action: change the security and export setting. */
    public static final int ACTION_CHSECEXP = 100;

    /** Export parameter. */
    private String m_paramExport;

    /** Secure parameter. */
    private String m_paramSecure;

    /** Intern parameter. */
    private String m_paramIntern;

    /** Exportname parameter. */
    private String m_paramExportname;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSecure(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSecure(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the value of the secure parameter.<p>
     * 
     * @return the value of the secure parameter
     */
    public String getParamSecure() {

        return m_paramSecure;
    }

    /**
     * Returns the value of the export parameter.<p>
     * 
     * @return the value of the export parameter
     */
    public String getParamExport() {

        return m_paramExport;
    }

    /**
     * Returns the value of the intern parameter.<p>
     * 
     * @return the value of the intern parameter
     */
    public String getParamIntern() {

        return m_paramIntern;
    }

    /**
     * Returns the value of the exportname parameter.<p>
     * 
     * @return the value of the exportname parameter
     */
    public String getParamExportname() {

        return m_paramExportname;
    }

    /**
     * Sets the value of the export parameter.<p>
     * 
     * @param value for the export parameter
     */
    public void setParamExport(String value) {

        m_paramExport = value;
    }

    /**
     * Sets the value of the exportname parameter.<p>
     * 
     * @param value for the exportname parameter
     */
    public void setParamExportname(String value) {

        m_paramExportname = value;
    }

    /**
     * Sets the value of the secure parameter.<p>
     * 
     * @param value for the secure parameter
     */
    public void setParamSecure(String value) {

        m_paramSecure = value;
    }

    /**
     * Sets the value of the intern parameter.<p>
     * 
     * @param value for the intern parameter
     */
    public void setParamIntern(String value) {

        m_paramIntern = value;
    }

    /**
     * Determines whether to show the export settings dialog depending on the users settings.<p>
     * 
     * @return true if dialogs should be shown, otherwise false
     */
    public boolean showExportSettings() {
        return getSettings().getUserSettings().getDialogShowExportSettings();
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_CHSECEXP);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for chnav dialog    
            setParamTitle(key("title.secureexport") + ": " + CmsResource.getName(getParamResource()));
        }
    }

    /**
     * Returns value of the property of the resource.
     * 
     * @param propertyName the name of the property to read 
     * @return the value of the secure property of the resource
     */
    public String readProperty(String propertyName) {

        String propVal = null;
        try {
            propVal = getCms().readPropertyObject(getParamResource(), propertyName, true).getValue();
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        if (CmsStringUtil.isEmpty(propVal)) {
            propVal = "";
        }
        return propVal;
    }

    /**
     * Returns value of the the intern property of the resource.
     *  
     * @return the value of the intern property of the resource
     */
    public String readInternProp() {

        boolean internProp = false;
        try {
            internProp = getCms().readResource(getParamResource()).isInternal();
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        return String.valueOf(internProp);
    }

    /**
     * Returns the path under which the resource is accessable.
     *  
     * @return the path under which the resource is accessable
     */
    public String getResourceUrl() {

        boolean secure = Boolean.valueOf(readProperty(I_CmsConstants.C_PROPERTY_SECURE)).booleanValue();
        StringBuffer result = new StringBuffer();
        CmsSite currentSite = CmsSiteManager.getCurrentSite(getCms());
        if (currentSite == OpenCms.getSiteManager().getDefaultSite()) {
            result.append(OpenCms.getSiteManager().getWorkplaceServer());
        } else {
            if (secure) {
                result.append(currentSite.getSecureUrl());
            } else {
                result.append(currentSite.getUrl());
            }
        }
        result.append(OpenCms.getLinkManager().substituteLink(getCms(), getParamResource()));

        return result.toString();
    }

    /**
     * Performs the Security and Export Change.<p>
     * 
     * @throws JspException if including a JSP subelement is not successful
     */
    public void actionChangeSecureExport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);

        String filename = getParamResource();

        try {
            // lock resource if autolock is enabled
            checkLock(getParamResource());

            // write the properties
            writeProperty(I_CmsConstants.C_PROPERTY_EXPORT, getParamExport());
            writeProperty(I_CmsConstants.C_PROPERTY_EXPORTNAME, getParamExportname());
            writeProperty(I_CmsConstants.C_PROPERTY_SECURE, getParamSecure());

            // change the flag of the resource so that it is internal            
            CmsResource resource = getCms().readResource(filename);
            if ("true".equals(getParamIntern())) {
                getCms().chflags(filename, resource.getFlags() | I_CmsConstants.C_RESOURCEFLAG_INTERNAL);
            } else {
                getCms().chflags(filename, resource.getFlags() & (~I_CmsConstants.C_RESOURCEFLAG_INTERNAL));
            }

            actionCloseDialog();
        } catch (CmsException e) {
            // error during change of settings, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message." + getParamDialogtype()));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);

        }

    }

    /**
     * Returns the parent folder name where the property is inherited from.<p>
     * 
     * @param propName the name of the property
     * @param propVal the value of the property
     * @return the name of the parent folderwhere the property is inherited from or null 
     * if the property is directly attached to the file or not inherited
     * @throws CmsException if the reading of the Property fails 
     */
    public String propertyInheritedFrom(String propName, String propVal) throws CmsException {

        // property is set for the resource and therefore not inherited
        if (!getCms().readPropertyObject(getParamResource(), propName, false).isNullProperty()) {
            return null;
        }
        String folderName = CmsResource.getParentFolder(getParamResource());
        while (CmsStringUtil.isNotEmpty(folderName)) {
            CmsProperty prop = getCms().readPropertyObject(folderName, propName, false);
            String folderPropVal = prop.getValue();
            if (propVal.equals(folderPropVal)) {
                return folderName;
            }
            folderName = CmsResource.getParentFolder(folderName);
        }
        // property is not inherited
        return null;
    }

    /**
     * Returns the information from which the property is inherited or an empty String if the property is not inherited.<p>
     * 
     * @param propName the name of the property
     * @param propVal the value of the property
     * @return the name of the parent folderwhere the property is inherited from or null if the
     * @throws CmsException if the reading of the Property fails 
     */
    public String getPropertyInheritanceInfo(String propName, String propVal) throws CmsException {

        String folderName = propertyInheritedFrom(propName, propVal);
        if (CmsStringUtil.isNotEmpty(folderName)) {
            return new StringBuffer(key("label.inherit")).append(' ').append(propVal).append(' ').append(
                key("label.from")).append(' ').append(folderName).toString();
        } else {
            return key("label.notset");
        }
    }

    /**
     * Writes a property value for a resource.<p>
     * 
     * @param propertyName the name of the property
     * @param propertyValue the new value of the property
     * @throws JspException  if including a JSP subelement is not successful
     */
    protected void writeProperty(String propertyName, String propertyValue) throws JspException {

        try {
            if (CmsStringUtil.isEmpty(propertyValue)) {
                propertyValue = CmsProperty.C_DELETE_VALUE;
            }

            CmsProperty newProp = new CmsProperty();
            newProp.setName(propertyName);
            CmsProperty oldProp = getCms().readPropertyObject(getParamResource(), propertyName, false);
            if (oldProp.isNullProperty()) {
                // property value was not already set
                if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                    newProp.setStructureValue(propertyValue);
                } else {
                    newProp.setResourceValue(propertyValue);
                }
            } else {
                if (oldProp.getStructureValue() != null) {
                    newProp.setStructureValue(propertyValue);
                    newProp.setResourceValue(oldProp.getResourceValue());
                } else {
                    newProp.setResourceValue(propertyValue);
                }
            }
            newProp.setAutoCreatePropertyDefinition(true);
            getCms().writePropertyObject(getParamResource(), newProp);

        } catch (CmsException e) {
            // error during chnav, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message." + getParamDialogtype()));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);

        }
    }

    /**
     * Builds the radio input to set the export and secure property.
     *
     * @param propName the name of the property to build the radio input for
     * @return html for the radio input
     * @throws CmsException if the reading of a property fails
     */
    public String buildRadio(String propName) throws CmsException {

        String propVal = readProperty(propName);
        StringBuffer result = new StringBuffer("<table border=\"0\"><tr>");
        result.append("<td><input type=\"radio\" value=\"true\" onClick=\"checkNoIntern()\" name=\"").append(propName).append("\" ").append(
            "true".equals(propVal) ? "checked=\"checked\"" : "").append("/></td><td id=\"tablelabel\">").append(key("label.true")).append("</td>");
        result.append("<td><input type=\"radio\" value=\"false\" onClick=\"checkNoIntern()\" name=\"").append(propName).append("\" ").append(
            "false".equals(propVal) ? "checked=\"checked\"" : "").append("/></td><td id=\"tablelabel\">").append(key("label.false")).append("</td>");
        result.append("<td><input type=\"radio\" value=\"\" onClick=\"checkNoIntern()\" name=\"").append(propName).append("\" ").append(
            CmsStringUtil.isEmpty(propVal) ? "checked=\"checked\"" : "").append("/></td><td id=\"tablelabel\">").append(
            getPropertyInheritanceInfo(propName, propVal)).append("</td></tr></table>");
        return result.toString();
    }
}