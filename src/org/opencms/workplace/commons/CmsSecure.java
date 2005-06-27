/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsSecure.java,v $
 * Date   : $Date: 2005/06/27 23:22:16 $
 * Version: $Revision: 1.22 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building the security and export settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/secure.jsp
 * </ul>
 * <p>
 *
 * @author  Jan Baudisch 
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSecure extends CmsDialog {

    /** Value for the action: change the security and export setting. */
    public static final int ACTION_CHSECEXP = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "secure";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecure.class);

    /** Export parameter. */
    private String m_paramExport;

    /** Exportname parameter. */
    private String m_paramExportname;

    /** Intern parameter. */
    private String m_paramIntern;

    /** Secure parameter. */
    private String m_paramSecure;

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
     * Performs the Security and Export Change.<p>
     * 
     * @throws JspException if including a JSP subelement is not successful
     */
    public void actionChangeSecureExport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);

        String filename = getParamResource();

        try {
            // lock resource if autolock is enabled
            checkLock(getParamResource());

            // write the properties
            writeProperty(CmsPropertyDefinition.PROPERTY_EXPORT, getParamExport());
            writeProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME, getParamExportname());
            writeProperty(CmsPropertyDefinition.PROPERTY_SECURE, getParamSecure());

            // change the flag of the resource so that it is internal            
            CmsResource resource = getCms().readResource(filename);
            if (resource.isInternal() && !Boolean.valueOf(getParamIntern()).booleanValue()) {
                getCms().chflags(filename, resource.getFlags() & (~CmsResource.FLAG_INTERNAL));
            } else if (!resource.isInternal() && Boolean.valueOf(getParamIntern()).booleanValue()) {
                getCms().chflags(filename, resource.getFlags() | CmsResource.FLAG_INTERNAL);
            }

            actionCloseDialog();
        } catch (Throwable e) {
            // error during change of secure settings, show error dialog
            includeErrorpage(this, e);
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
        result.append("<td><input type=\"radio\" value=\"true\" onClick=\"checkNoIntern()\" name=\"").append(propName).append(
            "\" ").append("true".equals(propVal) ? "checked=\"checked\"" : "").append("/></td><td id=\"tablelabel\">").append(
            key("label.true")).append("</td>");
        result.append("<td><input type=\"radio\" value=\"false\" onClick=\"checkNoIntern()\" name=\"").append(propName).append(
            "\" ").append("false".equals(propVal) ? "checked=\"checked\"" : "").append("/></td><td id=\"tablelabel\">").append(
            key("label.false")).append("</td>");
        result.append("<td><input type=\"radio\" value=\"\" onClick=\"checkNoIntern()\" name=\"").append(propName).append(
            "\" ").append(CmsStringUtil.isEmpty(propVal) ? "checked=\"checked\"" : "").append(
            "/></td><td id=\"tablelabel\">").append(getPropertyInheritanceInfo(propName)).append("</td></tr></table>");
        return result.toString();
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
     * Returns the value of the exportname parameter.<p>
     * 
     * @return the value of the exportname parameter
     */
    public String getParamExportname() {

        return m_paramExportname;
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
     * Returns the value of the secure parameter.<p>
     * 
     * @return the value of the secure parameter
     */
    public String getParamSecure() {

        return m_paramSecure;
    }

    /**
     * Returns the information from which the property is inherited.<p>
     * 
     * @param propName the name of the property
     * @return a String containing the information from which the property is inherited and inherited value
     * @throws CmsException if the reading of the Property fails 
     */
    public String getPropertyInheritanceInfo(String propName) throws CmsException {

        String folderName = CmsResource.getParentFolder(getParamResource());
        String folderPropVal = null;
        while (CmsStringUtil.isNotEmpty(folderName)) {
            CmsProperty prop = getCms().readPropertyObject(folderName, propName, false);
            folderPropVal = prop.getValue();
            if (CmsStringUtil.isNotEmpty(folderPropVal)) {
                break;
            }
            folderName = CmsResource.getParentFolder(folderName);
        }

        if (CmsStringUtil.isNotEmpty(folderPropVal)) {
            return new StringBuffer(key("label.inherit")).append(' ').append(folderPropVal).append(' ').append(
                key("label.from")).append(' ').append(folderName).toString();
        } else {
            return key("label.notset");
        }
    }

    /**
     * Returns the path under which the resource is accessable.
     *  
     * @return the path under which the resource is accessable
     */
    public String getResourceUrl() {

        CmsObject cms = getCms();
        String uri = "";
        String serverPrefix = "";
        String vfsName = CmsLinkManager.getAbsoluteUri(getParamResource(), cms.getRequestContext().getUri());
        String secureResource = "";
        String exportedResource = "";
        try {
            if (resourceIsFolder()) {
                vfsName = vfsName.concat("/");
            }
            secureResource = getCms().readPropertyObject(
                getParamResource(),
                CmsPropertyDefinition.PROPERTY_SECURE,
                true).getValue();
            exportedResource = getCms().readPropertyObject(
                getParamResource(),
                CmsPropertyDefinition.PROPERTY_EXPORT,
                true).getValue();
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        if (Boolean.valueOf(exportedResource).booleanValue()) {
            uri = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
        } else {
            uri = OpenCms.getStaticExportManager().getVfsPrefix().concat(vfsName);
        }
        CmsSite currentSite = CmsSiteManager.getCurrentSite(getCms());
        if (currentSite == OpenCms.getSiteManager().getDefaultSite()) {
            serverPrefix = OpenCms.getSiteManager().getWorkplaceServer();
        } else {
            if (Boolean.valueOf(secureResource).booleanValue()) {
                serverPrefix = currentSite.getSecureUrl();
            } else {
                serverPrefix = currentSite.getUrl();
            }
        }
        return serverPrefix.concat(uri);

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
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        return String.valueOf(internProp);
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
            propVal = getCms().readPropertyObject(getParamResource(), propertyName, false).getValue();
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        if (CmsStringUtil.isEmpty(propVal)) {
            propVal = "";
        }
        return propVal;
    }

    /**
     * returns if the resource to be changed is a folder.
     * @return true if the resource is a folder
     * @throws CmsException if the reading of the resource fails
     */
    public boolean resourceIsFolder() throws CmsException {

        return getCms().readResource(getParamResource()).isFolder();
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
     * Sets the value of the intern parameter.<p>
     * 
     * @param value for the intern parameter
     */
    public void setParamIntern(String value) {

        m_paramIntern = value;
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
        
        // check the required permissions to change the resource properties      
        if (! checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }
        
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
     * Writes a property value for a resource.<p>
     * 
     * @param propertyName the name of the property
     * @param propertyValue the new value of the property
     * @throws CmsException if something goes wrong
     */
    protected void writeProperty(String propertyName, String propertyValue) throws CmsException {

        if (CmsStringUtil.isEmpty(propertyValue)) {
            propertyValue = CmsProperty.DELETE_VALUE;
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

        String oldStructureValue = oldProp.getStructureValue();
        String newStructureValue = newProp.getStructureValue();
        if (CmsStringUtil.isEmpty(oldStructureValue)) {
            oldStructureValue = CmsProperty.DELETE_VALUE;
        }
        if (CmsStringUtil.isEmpty(newStructureValue)) {
            newStructureValue = CmsProperty.DELETE_VALUE;
        }

        String oldResourceValue = oldProp.getResourceValue();
        String newResourceValue = newProp.getResourceValue();
        if (CmsStringUtil.isEmpty(oldResourceValue)) {
            oldResourceValue = CmsProperty.DELETE_VALUE;
        }
        if (CmsStringUtil.isEmpty(newResourceValue)) {
            newResourceValue = CmsProperty.DELETE_VALUE;
        }

        // change property only if it has been changed            
        if (!oldResourceValue.equals(newResourceValue) || !oldStructureValue.equals(newStructureValue)) {
            getCms().writePropertyObject(getParamResource(), newProp);
        }

    }
}