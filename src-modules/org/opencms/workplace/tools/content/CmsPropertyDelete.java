/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsPropertyDelete.java,v $
 * Date   : $Date: 2005/06/23 11:11:29 $
 * Version: $Revision: 1.11 $
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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the delete property definition dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/administration/properties/delete/index.html
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * @author  Armen Markarian 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPropertyDelete extends CmsDialog {

    /** Value for the action: delete cascade. */
    public static final int ACTION_DELETE_CASCADE = 100;

    /** Request parameter value for the action: delete cascade. */
    public static final String DIALOG_DELETE_CASCADE = "deletecascade";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "propertydelete";

    /** Request parameter name for the property name. */
    public static final String PARAM_PROPERTYNAME = "propertyname";

    private String m_paramPropertyName;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyDelete(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPropertyDelete(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Deletes the property definition.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionDelete() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            getCms().deletePropertyDefinition(getParamPropertyName());
            // close the dialog
            actionCloseDialog();
        } catch (Throwable e) {
            // error while deleting property definition, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Deletes the property definition by cascading the properties on resources.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionDeleteCascade() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            // list of all resources containing this propertydefinition
            List resourcesWithProperty = getCms().readResourcesWithProperty(getParamPropertyName());
            // list of all resources locked by another user, containing this propertydefinition
            List resourcesLockedByOtherUser = getResourcesLockedByOtherUser(resourcesWithProperty);
            // do the following operations only if all of the resources are not locked by another user
            if (resourcesLockedByOtherUser.isEmpty()) {
                // save the site root
                getCms().getRequestContext().saveSiteRoot();
                // change to the root site
                getCms().getRequestContext().setSiteRoot("/");
                try {
                    Iterator i = resourcesWithProperty.iterator();
                    while (i.hasNext()) {
                        CmsResource resource = (CmsResource)i.next();
                        // read the property object
                        CmsProperty property = getCms().readPropertyObject(
                            resource.getRootPath(),
                            getParamPropertyName(),
                            false);
                        CmsLock lock = getCms().getLock(resource);
                        if (lock.getType() == CmsLock.C_TYPE_UNLOCKED) {
                            // lock the resource for the current (Admin) user
                            getCms().lockResource(resource.getRootPath());
                        }
                        property.setStructureValue(CmsProperty.C_DELETE_VALUE);
                        property.setResourceValue(CmsProperty.C_DELETE_VALUE);
                        // write the property with the null value to the resource and cascade it from the definition
                        getCms().writePropertyObject(resource.getRootPath(), property);
                        // unlock the resource
                        getCms().unlockResource(resource.getRootPath());
                    }
                    // delete the property definition at last
                    getCms().deletePropertyDefinition(getParamPropertyName());
                } finally {
                    // restore the siteroot
                    getCms().getRequestContext().restoreSiteRoot();
                    // close the dialog
                    actionCloseDialog();
                }
            } else {

                StringBuffer reason = new StringBuffer();
                reason.append(dialogWhiteBoxStart());
                reason.append(buildResourceList(resourcesLockedByOtherUser, true));
                reason.append(dialogWhiteBoxEnd());
                throw new CmsVfsException(Messages.get().container(
                    Messages.ERR_DEL_PROP_RESOURCES_LOCKED_1,
                    reason.toString()));
            }
        } catch (Throwable e) {
            // error while deleting property definition, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Builds a HTML list of Resources that use the specified property.<p>
     *  
     * @throws CmsException if operation was not successful
     * 
     * @return the HTML String for the Resource list
     */
    public String buildResourceList() throws CmsException {

        List resourcesWithProperty = getCms().readResourcesWithProperty(getParamPropertyName());

        return buildResourceList(resourcesWithProperty, false);
    }

    /**
     * Builds a HTML list of Resources.<p>
     * 
     * Columns: Type, Name, Uri, Value of the property, locked by(optional).<p>
     *  
     * @param resourceList a list of resources
     * @param lockInfo a boolean to decide if the locked info should be shown or not
     * @throws CmsException if operation was not successful
     * 
     * @return the HTML String for the Resource list
     */
    public String buildResourceList(List resourceList, boolean lockInfo) throws CmsException {

        // reverse the resource list
        Collections.reverse(resourceList);

        StringBuffer result = new StringBuffer();
        result.append("<table border=\"0\" width=\"100%\" cellpadding=\"1\" cellspacing=\"1\">\n");
        result.append("<tr>\n");
        // Type        
        result.append("\t<td style=\"width:5%;\" class=\"textbold\">");
        result.append(key("input.type"));
        result.append("</td>\n");
        // Uri
        result.append("\t<td style=\"width:40%;\" class=\"textbold\">");
        result.append(key("input.adress"));
        result.append("</td>\n");
        // Name
        result.append("\t<td style=\"width:25%;\" class=\"textbold\">");
        result.append(key("input.title"));
        result.append("</td>\n");
        if (!lockInfo) {
            // Property value
            result.append("\t<td style=\"width:30%;\" class=\"textbold\">");
            result.append(key("input.propertyvalue"));
            result.append("</td>\n");
        }
        if (lockInfo) {
            // Property value
            result.append("\t<td style=\"width:30%;\" class=\"textbold\">");
            result.append(key("explorer.lockedby"));
            result.append("</td>\n");
            result.append("</tr>\n");
        }
        result.append("</tr>\n");
        result.append("<tr><td colspan=\"4\"><span style=\"height: 6px;\">&nbsp;</span></td></tr>\n");

        getCms().getRequestContext().saveSiteRoot();
        getCms().getRequestContext().setSiteRoot("/");
        try {
            Iterator i = resourceList.iterator();
            while (i.hasNext()) {
                CmsResource resource = (CmsResource)i.next();
                String filetype = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName();
                result.append("<tr>\n");
                // file type
                result.append("\t<td>");
                result.append("<img src=\"");
                result.append(getSkinUri());
                result.append("filetypes/");
                result.append(filetype);
                result.append(".gif\">");
                result.append("</td>\n");
                // file address
                result.append("\t<td>");
                result.append(resource.getRootPath());
                result.append("</td>\n");
                // title
                result.append("\t<td>");
                result.append(getJsp().property(CmsPropertyDefinition.PROPERTY_TITLE, resource.getRootPath(), ""));
                result.append("</td>\n");
                // current value of the property
                if (!lockInfo) {
                    result.append("\t<td>");
                    result.append(getJsp().property(getParamPropertyName(), resource.getRootPath()));
                    result.append("</td>\n");
                }
                // locked by user
                if (lockInfo) {
                    CmsLock lock = getCms().getLock(resource);
                    result.append("\t<td>");
                    result.append(getCms().readUser(lock.getUserId()).getName());
                    result.append("</td>\n");
                }
                result.append("</tr>\n");
            }
            result.append("</table>\n");
        } finally {
            getCms().getRequestContext().restoreSiteRoot();
        }

        return result.toString();
    }

    /**
     * Builds the html for the property definition select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the property definition select box
     */
    public String buildSelectProperty(String attributes) {

        return CmsPropertyChange.buildSelectProperty(getCms(), key("please.select"), attributes);
    }

    /**
     * Returns the value of the propertyname parameter.<p>
     *
     * @return the value of the propertyname parameter
     */
    public String getParamPropertyName() {

        return m_paramPropertyName;
    }

    /**
     * Sets the value of the propertyname parameter.<p>
     *
     * @param paramPropertyName the value of the propertyname parameter
     */
    public void setParamPropertyName(String paramPropertyName) {

        m_paramPropertyName = paramPropertyName;
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
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
            setParamTitle(key("title.propertydelete") + ": " + getParamPropertyName());
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_DELETE_CASCADE.equals(getParamAction())) {
            setAction(ACTION_DELETE_CASCADE);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for change property value dialog     
            setParamTitle(key("title.propertydelete"));
        }
    }

    /**
     * Returns a list of resources that are locked by another user as the current user.<p>
     * 
     * @param resourceList the list of all (mixed) resources
     * 
     * @return a list of resources that are locked by another user as the current user
     * @throws CmsException if the getLock operation fails
     */
    private List getResourcesLockedByOtherUser(List resourceList) throws CmsException {

        List lockedResourcesByOtherUser = new ArrayList();
        Iterator i = resourceList.iterator();
        while (i.hasNext()) {
            CmsResource resource = (CmsResource)i.next();
            // get the lock state for the resource
            CmsLock lock = getCms().getLock(resource);
            // add this resource to the list if this is locked by another user
            if (lock.getType() != CmsLock.C_TYPE_UNLOCKED
                && !lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
                lockedResourcesByOtherUser.add(resource);
            }
        }

        return lockedResourcesByOtherUser;
    }
}
