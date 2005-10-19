/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsAvailability.java,v $
 * Date   : $Date: 2005/10/19 09:55:34 $
 * Version: $Revision: 1.1.2.2 $
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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the availability/notification dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/availability.jsp
 * </ul>
 * <p>
 *
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAvailability extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "availability";
    
    /** Request parameter name for the activation of the notifciation. */
    public static final String PARAM_ENABLE_NOTIFICATION = "enablenotification";

    /** Request parameter name for the expiredate. */
    public static final String PARAM_EXPIREDATE = "expiredate";
    
    /** Request parameter name for the recursive flag. */
    public static final String PARAM_MODIFY_SIBLINGS = "modifysiblings";
    
    /** Request parameter name for the activation of the notifciation. */
    public static final String PARAM_NOTIFICATION_INTERVAL = "notificationinterval";

    /** Request parameter name for the recursive flag. */
    public static final String PARAM_RECURSIVE = "recursive";

    /** Request parameter name for the releasedate. */
    public static final String PARAM_RELEASEDATE = "releasedate";
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAvailability.class);
   
    
    private String m_paramEnablenotification;
    private String m_paramExpiredate;
    private String m_paramModifysiblings;
    private String m_paramNotificationinterval;
    private String m_paramRecursive;
    private String m_paramReleasedate;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAvailability(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAvailability(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns a localized String for "Group", if the flag of a group ACE, and the localization for "User" otherwise.<p>
     * 
     * @param locale the locale to return the result in
     * @param flags the flags of the ACE
     * 
     * @return localization for "Group", if the flag belongs to a group ACE
     */
    protected static String getLocalizedType(Locale locale, int flags) {
        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_USER) > 0) {
            return Messages.get().key(locale, Messages.GUI_LABEL_USER_0);
        } else {
            return Messages.get().key(locale, Messages.GUI_LABEL_GROUP_0);
        }
    }
    
    /**
     * Performs the resource touching, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUpdate() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        
        try {
            if (performUpdateOperation()) {
                // if no exception is caused and "true" is returned the touch operation was successful          
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            includeErrorpage(this, e);
        }
    }
    
    /**
     * Creates an the checkbox to enable content notification for a resource.<p>
     *  
     * @return HTML code for the enable_notification checkbox.
     */
    public String buildCheckboxEnableNotification() {

        String propVal = null;
        try {
            propVal = getCms().readPropertyObject(getParamResource(), CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION, false).getValue();
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        if (CmsStringUtil.isEmpty(propVal)) {
            propVal = CmsStringUtil.FALSE;
        }
        StringBuffer result = new StringBuffer(512);
        result.append("<input type=\"checkbox\" style=\"text-align:left\" name=\"");
        result.append(PARAM_ENABLE_NOTIFICATION);
        if (Boolean.valueOf(propVal).booleanValue()) {
            result.append("\" checked=\"checked");  
        }    
        result.append("\" value=\"true\">");
        return result.toString();
    }
    
    /**
     * Creates an the checkbox to modify all siblings.<p>
     *  
     * If no siblings exist for this resource, an empty string will be returned.
     * 
     * @return HTML code for the modify siblings checkbox.
     */
    public String buildCheckboxModifySiblings() {

        StringBuffer result = new StringBuffer(254);
        try {
            if (getCms().readSiblings(getParamResource(), CmsResourceFilter.ALL).size() > 1) {
                result.append("<tr>\n<td style=\"white-space:nowrap;\">");
                result.append(key(Messages.GUI_AVAILABILITY_MODIFY_SIBLINGS_0));
                result.append("</td>\n<td class=\"maxwidth\" style=\"padding-left: 5px;\">\n");
                result.append("<input type=\"checkbox\" name=\"");
                result.append(PARAM_MODIFY_SIBLINGS);
                result.append("\" value=\"true\" style=\"text-align:left\">\n</td>\n<td>&nbsp</td></tr>");
            }
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e);   
            }    
        }
        return result.toString();
    }
    
    /**
     * Creates the "recursive" checkbox for touching subresources of folders.<p>
     *  
     * @return the String with the checkbox input field or an empty String for folders.
     */
    public String buildCheckRecursive() {

        StringBuffer retValue = new StringBuffer(256);

        CmsResource res = null;
        try {
            res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
            return "";
        }

        // show the checkbox only for folders
        if (res.isFolder()) {
            retValue.append("<tr>\n\t<td style=\"white-space:nowrap;\">");
            retValue.append(key(Messages.GUI_TOUCH_MODIFY_SUBRESOURCES_0));
            retValue.append("</td><td class=\"maxwidth\" style=\"padding-left: 5px;\"><input type=\"checkbox\" style=\"text-align:left\" name=\"");
            retValue.append(PARAM_RECURSIVE);
            retValue.append("\" value=\"true\">&nbsp;</td>\n<td>&nbsp</td></tr>\n");
        }
        return retValue.toString();
    }
    
    /**
     * Creates an input field for the notification interval.<p>
     *  
     * @return HTML code for the notification interval input field.
     */
    public String buildInputNotificationInterval() {

        String propVal = null;
        try {
            propVal = getCms().readPropertyObject(getParamResource(),
                CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL, false).getValue();
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        if (CmsStringUtil.isEmpty(propVal)) {
            propVal = "";
        }
        StringBuffer result = new StringBuffer();

        result.append("<input class=\"maxwidth\" type=\"text\" name=\"");
        result.append(CmsAvailability.PARAM_NOTIFICATION_INTERVAL);
        result.append("\" value=\"");
        result.append(propVal);
        result.append("\">");
        return result.toString();
    }
    
    /**
     * Builds a String with HTML code to display the responsibles of a resource.<p>
     * 
     * @return HTML code for the responsibles of the current resource
     */
    public String buildResponsibleList() {

        List parentResources = new ArrayList();
        Map responsibles = new HashMap();
        CmsObject cms = getCms();
        String resourceSitePath = cms.getRequestContext().removeSiteRoot(getParamResource());
        try {
            // get all parent folders of the current file
            parentResources = cms.readPath(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        Iterator i = parentResources.iterator();
        while (i.hasNext()) {
            CmsResource resource = (CmsResource)i.next();
            try {
                String sitePath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
                Iterator entries = cms.getAccessControlEntries(sitePath, false).iterator();
                while (entries.hasNext()) {
                    CmsAccessControlEntry ace = (CmsAccessControlEntry)entries.next();
                    if (ace.isResponsible()) {
                        I_CmsPrincipal principal = cms.lookupPrincipal(ace.getPrincipal());
                        responsibles.put(principal, resourceSitePath.equals(sitePath) ? null: sitePath);
                    }
                }
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }    
        }    
  
        if (responsibles.size() == 0) {
            
            return key(Messages.GUI_AVAILABILITY_NO_RESPONSIBLES_0);
        }    
        StringBuffer result = new StringBuffer(512);
        result.append(dialogToggleStart(key(Messages.GUI_AVAILABILITY_RESPONSIBLES_0), "responsibles", false));
        Collection parentFolders = new ArrayList(responsibles.values());
        parentFolders.remove(null);
        if (parentFolders.size() > 0) {
            result.append("<table border=\"0\">\n<tr>\n\t<td>");
            result.append(key(Messages.GUI_PERMISSION_SELECT_VIEW_0));
            result.append("</td>\n<td><input type=\"button\" onclick=\"toggleInheritInfo();\" value=\"");
            result.append(key(Messages.GUI_LABEL_DETAILS_0));
            result.append("\" id=\"button\"/></td></tr></table>");
        }
        result.append(dialogWhiteBoxStart());
        i = responsibles.keySet().iterator();
        for (int j = 0; i.hasNext(); j++) {
            I_CmsPrincipal principal = (I_CmsPrincipal)i.next();
            String image = "user.png";
            String localizedType = getLocalizedType(getLocale(), CmsAccessControlEntry.ACCESS_FLAGS_USER);
            if (principal instanceof CmsGroup) {
                image = "group.png";
                localizedType = getLocalizedType(getLocale(), CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
            }
            result.append("<div class=\"dialogrow\"><img src=\"");
            result.append(getSkinUri());
            result.append("commons/");
            result.append(image);
            result.append("\" class=\"noborder\" width=\"16\" height=\"16\" alt=\"");
            result.append(localizedType);
            result.append("\" title=\"");
            result.append(localizedType);
            result.append("\">&nbsp;<span class=\"textbold\">");
            result.append(principal.getName());
            result.append("</span><div class=\"hide\" id=\"inheritinfo");
            result.append(j);
            result.append("\"><div class=\"dialogpermissioninherit\">");
            String resourceName = ((String)responsibles.get(principal));
            if (CmsStringUtil.isNotEmpty(resourceName)) {
                result.append(key(Messages.GUI_PERMISSION_INHERITED_FROM_1, new Object[] {resourceName}));
            }
            result.append("</div></div></div>\n");
        }
        result.append(dialogWhiteBoxEnd());
        result.append("</div>\n");
        return result.toString();
    }
    
    
    /**
     * Returns the current date and time as String formatted in localized pattern.<p>
     * 
     * @return the current date and time as String formatted in localized pattern
     */
    public String getCurrentDateTime() {

        // get the current date & time 
        return getCalendarLocalizedTime(System.currentTimeMillis());
    }

    /**
     * Returns the current expiredate as String formatted in localized pattern.<p>
     * 
     * @return the current expiredate as String formatted in localized pattern
     */
    public String getCurrentExpireDate() {

        // get the expirationdate
        try {
            CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
            if (res.getDateExpired() == CmsResource.DATE_EXPIRED_DEFAULT) {
                return CmsTouch.DEFAULT_DATE_STRING;
            } else {
                return getCalendarLocalizedTime(res.getDateExpired());
            }
        } catch (CmsException e) {
            return getCalendarLocalizedTime(System.currentTimeMillis());
        }
    }

    /**
     * Returns the current releasedate as String formatted in localized pattern.<p>
     * 
     * @return the current releasedate as String formatted in localized pattern
     */
    public String getCurrentReleaseDate() {

        // get the releasedate
        try {
            CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
            if (res.getDateReleased() == CmsResource.DATE_RELEASED_DEFAULT) {
                return CmsTouch.DEFAULT_DATE_STRING;
            } else {
                return getCalendarLocalizedTime(res.getDateReleased());
            }
        } catch (CmsException e) {
            return getCalendarLocalizedTime(System.currentTimeMillis());
        }
    }

    /**
     * Returns the value of the enable_notification parameter.<p>
     * 
     * The enable_notification parameter if content notification is enabled for this resource.<p>
     * 
     * @return the value of the enable_notification parameter
     */
    public String getParamEnablenotification() {

        return m_paramEnablenotification;
    }
    
    /**
     * Returns the value of the new expiredate parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * @return the value of the new expiredate parameter
     */
    public String getParamExpiredate() {

        return m_paramExpiredate;
    }
    
    /**
     * Returns the value of the modify siblings parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * @return the value of the modify siblings
     */
    public String getParamModifysiblings() {

        return m_paramModifysiblings;
    }

    /**
     * Returns the value of the notification interval parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * @return the value of the notification interval parameter
     */
    public String getParamNotificationinterval() {
        
        return m_paramNotificationinterval;
    }
    
    /**
     * Returns the value of the recursive parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The recursive parameter on folders decides if all subresources
     * of the folder should be touched, too.<p>
     * 
     * @return the value of the recursive parameter
     */
    public String getParamRecursive() {

        return m_paramRecursive;
    }
    
    /**
     * Returns the value of the new releasedate parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The releasedate parameter stores the new releasedate as String.<p>
     * 
     * @return the value of the new releasedate parameter
     */
    public String getParamReleasedate() {

        return m_paramReleasedate;
    }
    
    /**
     * Sets the value of the enable notification parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamEnablenotification(String value) {

        m_paramEnablenotification = value; 
    }
    
    /**
     * Sets the value of the expire date.<p>
     * 
     * @param value the value to set
     */
    public void setParamExpiredate(String value) {

        m_paramExpiredate = value;
    }

    /**
     * Sets the value of the modify siblings parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamModifysiblings(String value) {
        
        m_paramModifysiblings = value;
    }

    /**
     * Sets the value of the new timestamp parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNotificationinterval(String value) {

        m_paramNotificationinterval = value;
    }
    
    /**
     * Sets the value of the recursive parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamRecursive(String value) {

        m_paramRecursive = value;
    }

    /**
     * Sets the value of the releasedate parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamReleasedate(String value) {

        m_paramReleasedate = value;
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // check the required permissions to touch the resource       
        if (! checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }
        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for touch dialog     
            setParamTitle(key(Messages.GUI_AVAILABILITY_NOTIFICATION_SETTINGS_1, new Object[] {getParamResource()}));
        }
    }

    /**
     * Writes a property value for a resource.<p>
     * 
     * @param resourcePath the path of the resource
     * @param propertyName the name of the property
     * @param propertyValue the new value of the property
     * @throws CmsException if something goes wrong
     */
    protected void writeProperty(String resourcePath, String propertyName, String propertyValue) throws CmsException {

        if (CmsStringUtil.isEmpty(propertyValue)) {
            propertyValue = CmsProperty.DELETE_VALUE;
        }

        CmsProperty newProp = new CmsProperty();
        newProp.setName(propertyName);
        CmsProperty oldProp = getCms().readPropertyObject(resourcePath, propertyName, false);
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
            getCms().writePropertyObject(resourcePath, newProp);
        }

    }
    
    /**
     * Modifies the release and expire date of a resource, and changes the notification interval. <p>
     * 
     * @return true, if the modified was touched, otherwise false
     * @throws CmsException if modification is not successful
     */
    private boolean performUpdateOperation() throws CmsException {

        // on folder copy display "please wait" screen, not for simple file copy
        CmsResource sourceRes = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
        if (sourceRes.isFolder() && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the current resource name
        String filename = getParamResource();

        // get the new releasedate for the resource(s) from request parameter
        long releasedate = CmsResource.DATE_RELEASED_DEFAULT;
        try {
            if ((getParamReleasedate() != null) && (!getParamReleasedate().startsWith(CmsTouch.DEFAULT_DATE_STRING))) {
                releasedate = getCalendarDate(getParamReleasedate(), true);
            }
        } catch (ParseException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_PARSE_RELEASEDATE_1, getParamReleasedate()), e);
        }

        // get the new expire for the resource(s) from request parameter
        long expiredate = CmsResource.DATE_EXPIRED_DEFAULT;
        try {
            if ((getParamExpiredate() != null) && (!getParamExpiredate().startsWith(CmsTouch.DEFAULT_DATE_STRING))) {
                expiredate = getCalendarDate(getParamExpiredate(), true);
            }
        } catch (ParseException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_PARSE_EXPIREDATE_1, getParamExpiredate()), e);
        }

        // get the flag if the touch is recursive from request parameter
        boolean modifyRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();

        List resources = new ArrayList();
        if (Boolean.valueOf(getParamModifysiblings()).booleanValue()) {
            resources = getCms().readSiblings(filename, CmsResourceFilter.IGNORE_EXPIRATION);
        } else {
            resources.add(getCms().readResource(filename, CmsResourceFilter.IGNORE_EXPIRATION));
        } 
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource resource = (CmsResource)i.next();
            // modify release and expire date of the resource
            // lock resource if autolock is enabled
            String resourcePath = getCms().getRequestContext().removeSiteRoot(resource.getRootPath());
            checkLock(resourcePath);
            getCms().setDateReleased(resourcePath, releasedate, modifyRecursive);
            getCms().setDateExpired(resourcePath, expiredate, modifyRecursive);
            
            writeProperty(resourcePath, CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL, getParamNotificationinterval());
            writeProperty(resourcePath, CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION, getParamEnablenotification());
        }
        return true;
    }
    
    /**
     * 
     * @see org.opencms.workplace.CmsDialog#actionCloseDialog()
     */
    public void actionCloseDialog() throws JspException {
        // so that the explorer will be shown, if dialog is opened from e-mail
        getSettings().getFrameUris().put("body", "/system/workplace/views/explorer/explorer_fs.jsp"); 
        super.actionCloseDialog();   
    }    
}