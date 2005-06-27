/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsChnav.java,v $
 * Date   : $Date: 2005/06/27 23:22:16 $
 * Version: $Revision: 1.21 $
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the change navigation dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/chnav.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.21 $ 
 * 
 * @since 6.0.0 
 */
public class CmsChnav extends CmsDialog {

    /** Value for the action: change the navigation. */
    public static final int ACTION_CHNAV = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "chnav";
    
    /** Request parameter name for the navigation position. */
    public static final String PARAM_NAVPOS = "navpos";

    /** Request parameter name for the navigation text. */
    public static final String PARAM_NAVTEXT = "navtext";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChnav.class);
    
    private String m_paramNavpos;

    private String m_paramNavtext;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsChnav(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsChnav(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds the HTML for the select box of the navigation position.<p>
     * 
     * @param cms the CmsObject
     * @param filename the current file
     * @param attributes optional attributes for the &lt;select&gt; tag, do not add the "name" atribute!
     * @param messages the localized workplace messages
     * 
     * @return the HTML for a navigation position select box
     */
    public static String buildNavPosSelector(CmsObject cms, String filename, String attributes, CmsMessages messages) {

        List navList = new ArrayList();
        List options = new ArrayList();
        List values = new ArrayList();

        // get current file navigation element
        CmsJspNavElement curNav = CmsJspNavBuilder.getNavigationForResource(cms, filename);

        // get the parent folder of the current file
        filename = CmsResource.getParentFolder(filename);

        // get navigation of the current folder
        navList = CmsJspNavBuilder.getNavigationForFolder(cms, filename);
        float maxValue = 0;
        float nextPos = 0;

        // calculate value for the first navigation position
        float firstValue = 1;
        if (navList.size() > 0) {
            try {
                CmsJspNavElement ne = (CmsJspNavElement)navList.get(0);
                maxValue = ne.getNavPosition();
            } catch (Exception e) {
                // should usually never happen
                LOG.error(e.getLocalizedMessage());
            }
        }

        if (maxValue != 0) {
            firstValue = maxValue / 2;
        }

        // add the first entry: before first element
        options.add(messages.key("input.firstelement"));
        values.add(firstValue + "");

        // show all present navigation elements in box
        for (int i = 0; i < navList.size(); i++) {
            CmsJspNavElement ne = (CmsJspNavElement)navList.get(i);
            String navText = ne.getNavText();
            float navPos = ne.getNavPosition();
            // get position of next nav element
            nextPos = navPos + 2;
            if ((i + 1) < navList.size()) {
                nextPos = ((CmsJspNavElement)navList.get(i + 1)).getNavPosition();
            }
            // calculate new position of current nav element
            float newPos = (navPos + nextPos) / 2;

            // check new maxValue of positions and increase it
            if (navPos > maxValue) {
                maxValue = navPos;
            }

            // if the element is the current file, mark it in selectbox
            if (curNav.getNavText().equals(navText) && curNav.getNavPosition() == navPos) {
                options.add(CmsEncoder.escapeHtml(messages.key("input.currentposition") + " [" + ne.getFileName() + "]"));
                values.add("-1");
            } else {
                options.add(CmsEncoder.escapeHtml(navText + " [" + ne.getFileName() + "]"));
                values.add(newPos + "");
            }
        }

        // add the entry: at the last position
        options.add(messages.key("input.lastelement"));
        values.add((maxValue + 1) + "");

        // add the entry: no change
        options.add(messages.key("input.nochange"));
        if (curNav.getNavPosition() == Float.MAX_VALUE) {
            // current resource has no valid position, use "last position"
            values.add((maxValue + 1) + "");
        } else {
            // current resource has valid position, use "-1" for no change
            values.add("-1");
        }

        if (attributes != null && !"".equals(attributes.trim())) {
            attributes = " " + attributes;
        } else {
            attributes = "";
        }
        return CmsWorkplace.buildSelect(
            "name=\"" + PARAM_NAVPOS + "\"" + attributes,
            options,
            values,
            values.size() - 1,
            true);
    }

    /**
     * Performs the navigation change.<p>
     * 
     * @throws JspException if including a JSP subelement is not successful
     */
    public void actionChangeNav() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);

        // get request parameters
        String filename = getParamResource();
        String newText = getParamNavtext();
        String selectedPosString = getParamNavpos();

        try {
            // lock resource if autolock is enabled
            checkLock(getParamResource());
            // save the new NavText if not null
            if (newText != null) {
                CmsProperty newNavText = new CmsProperty();
                newNavText.setName(CmsPropertyDefinition.PROPERTY_NAVTEXT);
                CmsProperty oldNavText = getCms().readPropertyObject(
                    filename,
                    CmsPropertyDefinition.PROPERTY_NAVTEXT,
                    false);
                if (oldNavText.isNullProperty()) {
                    // property value was not already set
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        newNavText.setStructureValue(newText);
                    } else {
                        newNavText.setResourceValue(newText);
                    }
                } else {
                    if (oldNavText.getStructureValue() != null) {
                        newNavText.setStructureValue(newText);
                        newNavText.setResourceValue(oldNavText.getResourceValue());
                    } else {
                        newNavText.setResourceValue(newText);
                    }
                }

                String oldStructureValue = oldNavText.getStructureValue();
                String newStructureValue = newNavText.getStructureValue();
                if (CmsStringUtil.isEmpty(oldStructureValue)) {
                    oldStructureValue = CmsProperty.DELETE_VALUE;
                }
                if (CmsStringUtil.isEmpty(newStructureValue)) {
                    newStructureValue = CmsProperty.DELETE_VALUE;
                }

                String oldResourceValue = oldNavText.getResourceValue();
                String newResourceValue = newNavText.getResourceValue();
                if (CmsStringUtil.isEmpty(oldResourceValue)) {
                    oldResourceValue = CmsProperty.DELETE_VALUE;
                }
                if (CmsStringUtil.isEmpty(newResourceValue)) {
                    newResourceValue = CmsProperty.DELETE_VALUE;
                }

                // change nav text only if it has been changed            
                if (!oldResourceValue.equals(newResourceValue) || !oldStructureValue.equals(newStructureValue)) {
                    getCms().writePropertyObject(getParamResource(), newNavText);
                }
            }

            // determine the selected position
            float selectedPos = -1;
            try {
                selectedPos = Float.parseFloat(selectedPosString);
            } catch (Exception e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }

            // only update the position if a change is requested
            if (selectedPos != -1) {
                CmsProperty newNavPos = new CmsProperty();
                newNavPos.setName(CmsPropertyDefinition.PROPERTY_NAVPOS);
                CmsProperty oldNavPos = getCms().readPropertyObject(
                    filename,
                    CmsPropertyDefinition.PROPERTY_NAVPOS,
                    false);
                if (oldNavPos.isNullProperty()) {
                    // property value was not already set
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        newNavPos.setStructureValue(selectedPosString);
                    } else {
                        newNavPos.setResourceValue(selectedPosString);
                    }
                } else {
                    if (oldNavPos.getStructureValue() != null) {
                        newNavPos.setStructureValue(selectedPosString);
                        newNavPos.setResourceValue(oldNavPos.getResourceValue());
                    } else {
                        newNavPos.setResourceValue(selectedPosString);
                    }
                }
                getCms().writePropertyObject(filename, newNavPos);
            }
        } catch (Throwable e) {
            // error during chnav, show error dialog
            includeErrorpage(this, e);
        }
        // chnav operation was successful, return to workplace
        actionCloseDialog();
    }

    /**
     * Builds the HTML for the select box of the navigation position.<p>
     * 
     * @return the HTML for a navigation position select box
     */
    public String buildNavPosSelector() {

        synchronized (this) {
            return buildNavPosSelector(getCms(), getParamResource(), null, getMessages());
        }
    }

    /**
     * Returns the escaped NavText property value of the current resource.<p>
     * 
     * @return the NavText property value of the current resource
     */
    public String getCurrentNavText() {

        try {
            String navText = getCms().readPropertyObject(
                getParamResource(),
                CmsPropertyDefinition.PROPERTY_NAVTEXT,
                false).getValue();
            if (navText == null) {
                navText = "";
            }
            return CmsEncoder.escapeXml(navText);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
            return "";
        }

    }

    /**
     * Returns the value of the navigation position parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The navigation position parameter defines the new value for 
     * the NavPos property.<p>
     * 
     * @return the value of the target parameter
     */
    public String getParamNavpos() {

        return m_paramNavpos;
    }

    /**
     * Returns the value of the navigation text parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The navigation text parameter defines the new value for 
     * the NavText property.<p>
     * 
     * @return the value of the target parameter
     */
    public String getParamNavtext() {

        return m_paramNavtext;
    }

    /**
     * Sets the value of the navigation position parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNavpos(String value) {

        m_paramNavpos = value;
    }

    /**
     * Sets the value of the navigation text parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNavtext(String value) {

        m_paramNavtext = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // check the required permissions to change navigation of the resource       
        if (! checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }
        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_CHNAV);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for chnav dialog     
            setParamTitle(key("explorer.context.chnav") + ": " + CmsResource.getName(getParamResource()));
        }
    }

}
