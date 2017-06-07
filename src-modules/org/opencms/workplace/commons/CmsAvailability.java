/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the resource availability/notification dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/availability.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsAvailability extends CmsMultiDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "availability";

    /** Request parameter name for the activation of the notification. */
    public static final String PARAM_ENABLE_NOTIFICATION = "enablenotification";

    /** Request parameter name for the expire date. */
    public static final String PARAM_EXPIREDATE = "expiredate";

    /** Request parameter name for the leave expire. */
    public static final String PARAM_LEAVEEXPIRE = "leaveexpire";

    /** Request parameter name for the leave release. */
    public static final String PARAM_LEAVERELEASE = "leaverelease";

    /** Request parameter name for the include siblings flag. */
    public static final String PARAM_MODIFY_SIBLINGS = "modifysiblings";

    /** Request parameter name for the activation of the notification. */
    public static final String PARAM_NOTIFICATION_INTERVAL = "notificationinterval";

    /** Request parameter name for the recursive flag. */
    public static final String PARAM_RECURSIVE = "recursive";

    /** Request parameter name for the release date. */
    public static final String PARAM_RELEASEDATE = "releasedate";

    /** Request parameter name for the reset expire. */
    public static final String PARAM_RESETEXPIRE = "resetexpire";

    /** Request parameter name for the reset release. */
    public static final String PARAM_RESETRELEASE = "resetrelease";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAvailability.class);

    /** A parameter of this dialog. */
    private String m_paramEnablenotification;

    /** A parameter of this dialog. */
    private String m_paramExpiredate;

    /** A parameter of this dialog. */
    private String m_paramLeaveexpire;

    /** A parameter of this dialog. */
    private String m_paramModifysiblings;

    /** A parameter of this dialog. */
    private String m_paramNotificationinterval;

    /** A parameter of this dialog. */
    private String m_paramRecursive;

    /** A parameter of this dialog. */
    private String m_paramReleasedate;

    /** A parameter of this dialog. */
    private String m_paramResetexpire;

    /** A parameter of this dialog. */
    private String m_paramResetrelease;

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
     * Performs the notification operations on a single resource.<p>
     *
     * @param cms the CMS context
     * @param resName the VFS path of the resource
     * @param enableNotification if the notification is activated
     * @param notificationInterval the notification interval in days
     * @param modifySiblings flag indicating to include resource siblings
     *
     * @throws CmsException if the availability and notification operations fail
     */
    public static void performSingleResourceNotification(
        CmsObject cms,
        String resName,
        boolean enableNotification,
        int notificationInterval,
        boolean modifySiblings) throws CmsException {

        List<CmsResource> resources = new ArrayList<CmsResource>();
        if (modifySiblings) {
            // modify all siblings of a resource
            resources = cms.readSiblings(resName, CmsResourceFilter.IGNORE_EXPIRATION);
        } else {
            // modify only resource without siblings
            resources.add(cms.readResource(resName, CmsResourceFilter.IGNORE_EXPIRATION));
        }
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {
            CmsResource resource = i.next();
            String resourcePath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
            // lock resource if auto lock is enabled
            CmsLockActionRecord lockRecord = CmsLockUtil.ensureLock(cms, resource);
            try {
                // write notification settings
                writeProperty(
                    cms,
                    resourcePath,
                    CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,
                    String.valueOf(notificationInterval));
                writeProperty(
                    cms,
                    resourcePath,
                    CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                    String.valueOf(enableNotification));
            } finally {
                if (lockRecord.getChange() == LockChange.locked) {
                    cms.unlockResource(resource);
                }
            }
        }
    }

    /**
     * Writes a property value for a resource.<p>
     *
     * @param cms the cms context
     * @param resourcePath the path of the resource
     * @param propertyName the name of the property
     * @param propertyValue the new value of the property
     *
     * @throws CmsException if something goes wrong
     */
    public static void writeProperty(CmsObject cms, String resourcePath, String propertyName, String propertyValue)
    throws CmsException {

        if (CmsStringUtil.isEmpty(propertyValue)) {
            propertyValue = CmsProperty.DELETE_VALUE;
        }

        CmsProperty newProp = new CmsProperty();
        newProp.setName(propertyName);
        CmsProperty oldProp = cms.readPropertyObject(resourcePath, propertyName, false);
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
            cms.writePropertyObject(resourcePath, newProp);
        }
    }

    /**
     *
     * @see org.opencms.workplace.CmsDialog#actionCloseDialog()
     */
    @Override
    public void actionCloseDialog() throws JspException {

        // so that the explorer will be shown, if dialog is opened from e-mail
        getSettings().getFrameUris().put("body", CmsWorkplace.VFS_PATH_VIEWS + "explorer/explorer_fs.jsp");
        super.actionCloseDialog();
    }

    /**
     * Performs the resource operation, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUpdate() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);

        try {
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned the dialog operation was successful
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
     * Creates the check box to enable content notification for a resource.<p>
     *
     * @return HTML code for the enable_notification check box.
     */
    public String buildCheckboxEnableNotification() {

        String propVal = null;
        if (!isMultiOperation()) {
            // get current settings for single resource dialog
            try {
                propVal = getCms().readPropertyObject(
                    getParamResource(),
                    CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                    false).getValue();
            } catch (CmsException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
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
     * Creates an the check box to modify all siblings.<p>
     *
     * If no siblings exist for this resource, an empty string will be returned.
     *
     * @return HTML code for the modify siblings check box.
     */
    public String buildCheckboxModifySiblings() {

        StringBuffer result = new StringBuffer(254);
        try {
            if (isMultiOperation() || (getCms().readSiblings(getParamResource(), CmsResourceFilter.ALL).size() > 1)) {
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
     * Creates the "recursive" check box for touching subresources of folders.<p>
     *
     * @return the String with the check box input field or an empty String for folders.
     */
    public String buildCheckRecursive() {

        StringBuffer result = new StringBuffer(256);

        // show the check box only for operation(s) on folder(s)
        if (isOperationOnFolder()) {
            result.append(dialogBlockStart(key(Messages.GUI_AVAILABILITY_NOTIFICATION_SUBRES_0)));
            result.append("<table border=\"0\">");
            result.append("<tr>\n\t<td style=\"white-space:nowrap;\">");
            result.append(key(Messages.GUI_TOUCH_MODIFY_SUBRESOURCES_0));
            result.append(
                "</td><td class=\"maxwidth\" style=\"padding-left: 5px;\"><input type=\"checkbox\" style=\"text-align:left\" name=\"");
            result.append(PARAM_RECURSIVE);
            result.append("\" value=\"true\">&nbsp;</td>\n<td>&nbsp</td></tr>\n");
            result.append("</table>");
            result.append(dialogBlockEnd());
            result.append(dialogSpacer());
        }
        return result.toString();
    }

    /**
     * Creates an input field for the notification interval.<p>
     *
     * @return HTML code for the notification interval input field.
     */
    public String buildInputNotificationInterval() {

        String propVal = null;
        if (!isMultiOperation()) {
            try {
                propVal = getCms().readPropertyObject(
                    getParamResource(),
                    CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,
                    false).getValue();
            } catch (CmsException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
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

        if (isMultiOperation()) {
            // show no responsibles list for multi operation
            return "";
        } else {
            // single resource operation, create list of responsibles
            StringBuffer result = new StringBuffer(512);
            result.append("<tr><td colspan=\"3\">");
            List<CmsResource> parentResources = new ArrayList<CmsResource>();
            Map<I_CmsPrincipal, String> responsibles = new HashMap<I_CmsPrincipal, String>();
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
            Iterator<CmsResource> i = parentResources.iterator();
            while (i.hasNext()) {
                CmsResource resource = i.next();
                try {
                    String sitePath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
                    Iterator<CmsAccessControlEntry> entries = cms.getAccessControlEntries(sitePath, false).iterator();
                    while (entries.hasNext()) {
                        CmsAccessControlEntry ace = entries.next();
                        if (ace.isResponsible()) {
                            I_CmsPrincipal principal = cms.lookupPrincipal(ace.getPrincipal());
                            if (principal != null) {
                                responsibles.put(principal, resourceSitePath.equals(sitePath) ? null : sitePath);
                            }
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
                // no responsibles found
                result.append(key(Messages.GUI_AVAILABILITY_NO_RESPONSIBLES_0));
            } else {
                // found responsibles, create list
                result.append(dialogToggleStart(key(Messages.GUI_AVAILABILITY_RESPONSIBLES_0), "responsibles", false));
                Collection<String> parentFolders = new ArrayList<String>(responsibles.values());
                parentFolders.remove(null);
                if (parentFolders.size() > 0) {
                    result.append("<table border=\"0\">\n<tr>\n\t<td>");
                    result.append(key(Messages.GUI_PERMISSION_SELECT_VIEW_0));
                    result.append("</td>\n<td><input type=\"button\" onclick=\"toggleInheritInfo();\" value=\"");
                    result.append(key(Messages.GUI_LABEL_DETAILS_0));
                    result.append("\" id=\"button\"/></td></tr></table>");
                }
                result.append(dialogWhiteBoxStart());
                Iterator<Map.Entry<I_CmsPrincipal, String>> it = responsibles.entrySet().iterator();
                for (int j = 0; it.hasNext(); j++) {
                    Map.Entry<I_CmsPrincipal, String> entry = it.next();
                    I_CmsPrincipal principal = entry.getKey();
                    String image = "user.png";
                    String localizedType = getLocalizedType(CmsAccessControlEntry.ACCESS_FLAGS_USER);
                    if (principal instanceof CmsGroup) {
                        image = "group.png";
                        localizedType = getLocalizedType(CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
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
                    String resourceName = entry.getValue();
                    if (CmsStringUtil.isNotEmpty(resourceName)) {
                        result.append(key(Messages.GUI_PERMISSION_INHERITED_FROM_1, new Object[] {resourceName}));
                    }
                    result.append("</div></div></div>\n");
                }
                result.append(dialogWhiteBoxEnd());
                result.append("</div>\n");
                result.append("</td></tr>");
            }
            return result.toString();
        }
    }

    /**
     * Returns the current date and time as String formatted in localized pattern.<p>
     *
     * @return the current date and time as String formatted in localized pattern
     */
    public String getCurrentDateTime() {

        // get the current date & time
        return CmsCalendarWidget.getCalendarLocalizedTime(getLocale(), getMessages(), System.currentTimeMillis());
    }

    /**
     * Returns the current expire date as String formatted in localized pattern.<p>
     *
     * @return the current expire date as String formatted in localized pattern
     */
    public String getCurrentExpireDate() {

        // get the expiration date
        if (isMultiOperation()) {
            return CmsTouch.DEFAULT_DATE_STRING;
        } else {
            try {
                CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
                if (res.getDateExpired() == CmsResource.DATE_EXPIRED_DEFAULT) {
                    return CmsTouch.DEFAULT_DATE_STRING;
                } else {
                    return CmsCalendarWidget.getCalendarLocalizedTime(getLocale(), getMessages(), res.getDateExpired());
                }
            } catch (CmsException e) {
                return CmsCalendarWidget.getCalendarLocalizedTime(
                    getLocale(),
                    getMessages(),
                    System.currentTimeMillis());
            }
        }
    }

    /**
     * Returns the current release date as String formatted in localized pattern.<p>
     *
     * @return the current release date as String formatted in localized pattern
     */
    public String getCurrentReleaseDate() {

        // get the release date
        if (isMultiOperation()) {
            return CmsTouch.DEFAULT_DATE_STRING;
        } else {
            try {
                CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
                if (res.getDateReleased() == CmsResource.DATE_RELEASED_DEFAULT) {
                    return CmsTouch.DEFAULT_DATE_STRING;
                } else {
                    return CmsCalendarWidget.getCalendarLocalizedTime(
                        getLocale(),
                        getMessages(),
                        res.getDateReleased());
                }
            } catch (CmsException e) {
                return CmsCalendarWidget.getCalendarLocalizedTime(
                    getLocale(),
                    getMessages(),
                    System.currentTimeMillis());
            }
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
     * Returns the value of the new expire date parameter,
     * or null if this parameter was not provided.<p>
     *
     * @return the value of the new expire date parameter
     */
    public String getParamExpiredate() {

        return m_paramExpiredate;
    }

    /**
     * Returns the value of the leave expire parameter.<p>
     *
     * @return the value of the leave expire parameter
     */
    public String getParamLeaveexpire() {

        return m_paramLeaveexpire;
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
     * Returns the value of the new release date parameter,
     * or null if this parameter was not provided.<p>
     *
     * The release date parameter stores the new release date as String.<p>
     *
     * @return the value of the new release date parameter
     */
    public String getParamReleasedate() {

        return m_paramReleasedate;
    }

    /**
     * Returns the value of the reset expire parameter.<p>
     *
     * @return the value of the reset expire parameter
     */
    public String getParamResetexpire() {

        return m_paramResetexpire;
    }

    /**
     * Returns the value of the reset release parameter.<p>
     *
     * @return the value of the reset release parameter
     */
    public String getParamResetrelease() {

        return m_paramResetrelease;
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
     * Sets the value of the leave expire parameter.<p>
     *
     * @param paramLeaveexpire the value of the leave expire parameter
     */
    public void setParamLeaveexpire(String paramLeaveexpire) {

        m_paramLeaveexpire = paramLeaveexpire;
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
     * Sets the value of the new time stamp parameter.<p>
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
     * Sets the value of the release date parameter.<p>
     *
     * @param value the value to set
     */
    public void setParamReleasedate(String value) {

        m_paramReleasedate = value;
    }

    /**
     * Sets the value of the reset expire parameter.<p>
     *
     * @param paramResetexpire the value of the reset expire parameter
     */
    public void setParamResetexpire(String paramResetexpire) {

        m_paramResetexpire = paramResetexpire;
    }

    /**
     * Sets the value of the reset release parameter.<p>
     *
     * @param paramResetrelease the value of the reset release parameter
     */
    public void setParamResetrelease(String paramResetrelease) {

        m_paramResetrelease = paramResetrelease;
    }

    /**
     * Returns a localized String for "Group", if the flag of a group ACE, and the localization for "User" otherwise.<p>
     *
     * @param flags the flags of the ACE
     *
     * @return localization for "Group", if the flag belongs to a group ACE
     */
    protected String getLocalizedType(int flags) {

        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_USER) > 0) {
            return key(Messages.GUI_LABEL_USER_0);
        } else {
            return key(Messages.GUI_LABEL_GROUP_0);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to modify the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
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
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for dialog
            setDialogTitle(
                Messages.GUI_AVAILABILITY_NOTIFICATION_SETTINGS_1,
                Messages.GUI_AVAILABILITY_NOTIFICATION_MULTI_2);
        }
    }

    /**
     * Modifies the release and expire date of a resource, and changes the notification interval. <p>
     *
     * @return true, if the operation was performed, otherwise false
     * @throws CmsException if modification is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // check if the current resource is a folder for single operation
        boolean isFolder = isOperationOnFolder();
        // on folder deletion or multi operation display "please wait" screen, not for simple file deletion
        if ((isMultiOperation() || isFolder) && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the new release date for the resource(s) from request parameter
        long releaseDate = CmsResource.DATE_RELEASED_DEFAULT;
        boolean resetReleaseDate = Boolean.valueOf(getParamResetrelease()).booleanValue();
        boolean leaveReleaseDate = false;
        if (!resetReleaseDate) {
            try {
                if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamReleasedate()))
                    && (!getParamReleasedate().startsWith(CmsTouch.DEFAULT_DATE_STRING))) {
                    releaseDate = CmsCalendarWidget.getCalendarDate(getMessages(), getParamReleasedate(), true);
                } else {
                    leaveReleaseDate = true;
                }
            } catch (ParseException e) {
                throw new CmsException(
                    Messages.get().container(Messages.ERR_PARSE_RELEASEDATE_1, getParamReleasedate()),
                    e);
            }
        }

        // get the new expire date for the resource(s) from request parameter
        long expireDate = CmsResource.DATE_EXPIRED_DEFAULT;
        boolean resetExpireDate = Boolean.valueOf(getParamResetexpire()).booleanValue();
        boolean leaveExpireDate = false;
        if (!resetExpireDate) {
            try {
                if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamExpiredate()))
                    && (!getParamExpiredate().startsWith(CmsTouch.DEFAULT_DATE_STRING))) {
                    expireDate = CmsCalendarWidget.getCalendarDate(getMessages(), getParamExpiredate(), true);
                } else {
                    leaveExpireDate = true;
                }
            } catch (ParseException e) {
                throw new CmsException(
                    Messages.get().container(Messages.ERR_PARSE_EXPIREDATE_1, getParamExpiredate()),
                    e);
            }
        }

        // get the flag if the operation is recursive from request parameter
        boolean modifyRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();

        // now iterate the resource(s)
        Iterator<String> i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            try {
                performSingleResourceAvailability(
                    resName,
                    releaseDate,
                    expireDate,
                    leaveReleaseDate,
                    leaveExpireDate,
                    modifyRecursive);
            } catch (CmsException e) {
                // collect exceptions to create a detailed output
                addMultiOperationException(e);
            }
        }

        boolean notificationEnabled = Boolean.valueOf(getParamEnablenotification()).booleanValue();
        int notificationInterval = 0;
        try {
            notificationInterval = Integer.parseInt(getParamNotificationinterval());
        } catch (Throwable e) {
            // ignore
        }

        // get the flag if the operation should be executed on resource siblings, too
        boolean modifySiblings = Boolean.valueOf(getParamModifysiblings()).booleanValue();

        // now iterate the resource(s)
        i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            try {
                performSingleResourceNotification(
                    getCms(),
                    resName,
                    notificationEnabled,
                    notificationInterval,
                    modifySiblings);
            } catch (CmsException e) {
                // collect exceptions to create a detailed output
                addMultiOperationException(e);
            }
        }
        checkMultiOperationException(Messages.get(), Messages.ERR_AVAILABILITY_MULTI_0);

        return true;
    }

    /**
     * Performs the availability operations on a single resource.<p>
     *
     * @param resName the VFS path of the resource
     * @param releaseDate the new release date
     * @param expireDate the new expiration date
     * @param leaveRelease if the release date should be left untouched
     * @param leaveExpire if the expiration date should be left untouched
     * @param modifyRecursive flag indicating if the operation is recursive for folders
     *
     * @throws CmsException if the availability and notification operations fail
     */
    protected void performSingleResourceAvailability(
        String resName,
        long releaseDate,
        long expireDate,
        boolean leaveRelease,
        boolean leaveExpire,
        boolean modifyRecursive) throws CmsException {

        CmsResource resource = getCms().readResource(resName, CmsResourceFilter.IGNORE_EXPIRATION);
        if (leaveRelease) {
            releaseDate = resource.getDateReleased();
        }
        if (leaveExpire) {
            expireDate = resource.getDateExpired();
        }
        if (expireDate < releaseDate) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_AVAILABILITY_BAD_TIMEWINDOW_0));
        }
        String resourcePath = getCms().getSitePath(resource);
        // lock resource if auto lock is enabled
        checkLock(resourcePath);
        // modify release and expire date of the resource if needed
        if (!leaveRelease) {
            getCms().setDateReleased(resourcePath, releaseDate, modifyRecursive);
        }
        if (!leaveExpire) {
            getCms().setDateExpired(resourcePath, expireDate, modifyRecursive);
        }
    }
}