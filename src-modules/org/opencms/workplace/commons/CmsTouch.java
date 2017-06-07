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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the touch resource(s) dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/touch.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsTouch extends CmsMultiDialog {

    /** Value for the action: touch. */
    public static final int ACTION_TOUCH = 100;

    /** Default value for date last modified, the release and expire date. */
    public static final String DEFAULT_DATE_STRING = "-";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "touch";

    /** Request parameter name for the content flag. */
    public static final String PARAM_CONTENT = "content";

    /** Request parameter name for timestamp. */
    public static final String PARAM_NEWTIMESTAMP = "newtimestamp";

    /** Request parameter name for the recursive flag. */
    public static final String PARAM_RECURSIVE = "recursive";

    /** Content parameter. */
    private String m_paramContent;

    /** Timestamp parameter. */
    private String m_paramNewtimestamp;

    /** Recursive parameter. */
    private String m_paramRecursive;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsTouch(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsTouch(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs a touch operation for a single resource.<p>
     *
     * @param resourceName the resource name of the resource to touch
     * @param timeStamp the new time stamp
     * @param recursive the flag if the touch operation is recursive
     * @param correctDate the flag if the new time stamp is a correct date
     * @param touchContent if the content has to be rewritten
     *
     * @throws CmsException if touching the resource fails
     */
    public static void touchSingleResource(
        CmsObject cms,
        String resourceName,
        long timeStamp,
        boolean recursive,
        boolean correctDate,
        boolean touchContent) throws CmsException {

        CmsResource sourceRes = cms.readResource(resourceName, CmsResourceFilter.ALL);
        if (!correctDate) {
            // no date value entered, use current resource modification date
            timeStamp = sourceRes.getDateLastModified();
        }
        cms.setDateLastModified(resourceName, timeStamp, recursive);

        if (touchContent) {
            if (sourceRes.isFile()) {
                hardTouch(cms, sourceRes);
            } else if (recursive) {
                Iterator<CmsResource> it = cms.readResources(resourceName, CmsResourceFilter.ALL, true).iterator();
                while (it.hasNext()) {
                    CmsResource subRes = it.next();
                    if (subRes.isFile()) {
                        hardTouch(cms, subRes);
                    }
                }
            }
        }
    }

    /**
     * Rewrites the content of the given file.<p>
     *
     * @param resource the resource to rewrite the content for
     *
     * @throws CmsException if something goes wrong
     */
    private static void hardTouch(CmsObject cms, CmsResource resource) throws CmsException {

        CmsFile file = cms.readFile(resource);
        file.setContents(file.getContents());
        cms.writeFile(file);
    }

    /**
     * Performs the resource touching, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionTouch() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            if (performDialogOperation()) {
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
     * Creates the "rewrite content" checkbox.<p>
     *
     * @return the String with the checkbox input field
     */
    public String buildCheckContent() {

        StringBuffer retValue = new StringBuffer(256);

        retValue.append("<tr>\n\t<td colspan=\"3\" style=\"white-space: nowrap;\" unselectable=\"on\">");
        retValue.append("<input type=\"checkbox\" name=\"");
        retValue.append(PARAM_CONTENT);
        retValue.append("\" value=\"true\">&nbsp;");
        retValue.append(key(Messages.GUI_TOUCH_MODIFY_CONTENT_0));
        retValue.append("</td>\n</tr>\n");
        return retValue.toString();
    }

    /**
     * Creates the "recursive" checkbox for touching subresources of folders.<p>
     *
     * @return the String with the checkbox input field or an empty String for folders.
     */
    public String buildCheckRecursive() {

        StringBuffer retValue = new StringBuffer(256);

        // show the checkbox only for folders
        if (isOperationOnFolder()) {
            retValue.append("<tr>\n\t<td colspan=\"3\" style=\"white-space: nowrap;\" unselectable=\"on\">");
            retValue.append("<input type=\"checkbox\" name=\"");
            retValue.append(PARAM_RECURSIVE);
            retValue.append("\" value=\"true\">&nbsp;");
            retValue.append(key(Messages.GUI_TOUCH_MODIFY_SUBRESOURCES_0));
            retValue.append("</td>\n</tr>\n");
        }
        return retValue.toString();
    }

    /**
     * Creates the HTML JavaScript and stylesheet includes required by the calendar for the head of the page.<p>
     *
     * @return the necessary HTML code for the js and stylesheet includes
     *
     * @deprecated use {@link CmsCalendarWidget#calendarIncludes(java.util.Locale)}, this is just here so that old JSP still work
     */
    @Deprecated
    public String calendarIncludes() {

        return CmsCalendarWidget.calendarIncludes(getLocale());
    }

    /**
     * Generates the HTML to initialize the JavaScript calendar element on the end of a page.<p>
     *
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param dateStatusFunc name of the function which determines if/how a date should be disabled
     * @param showTime true if the time selector should be shown, otherwise false
     * @return the HTML code to initialize a calendar poup element
     *
     * @deprecated use {@link CmsCalendarWidget#calendarInit(org.opencms.i18n.CmsMessages, String, String, String, boolean, boolean, boolean, String, boolean)}, this is just here so that old JSP still work
     */
    @Deprecated
    public String calendarInit(
        String inputFieldId,
        String triggerButtonId,
        String align,
        boolean singleClick,
        boolean weekNumbers,
        boolean mondayFirst,
        String dateStatusFunc,
        boolean showTime) {

        return CmsCalendarWidget.calendarInit(
            getMessages(),
            inputFieldId,
            triggerButtonId,
            align,
            singleClick,
            weekNumbers,
            mondayFirst,
            dateStatusFunc,
            showTime);
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
     * Returns the value of the content parameter,
     * or null if this parameter was not provided.<p>
     *
     * The content parameter on files decides if also the content is rewritten.<p>
     *
     * @return the value of the content parameter
     */
    public String getParamContent() {

        return m_paramContent;
    }

    /**
     * Returns the value of the new timestamp parameter,
     * or null if this parameter was not provided.<p>
     *
     * The timestamp parameter stores the new timestamp as String.<p>
     *
     * @return the value of the new timestamp parameter
     */
    public String getParamNewtimestamp() {

        return m_paramNewtimestamp;
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
     * Sets the value of the content parameter.<p>
     *
     * @param value the value to set
     */
    public void setParamContent(String value) {

        m_paramContent = value;
    }

    /**
     * Sets the value of the new timestamp parameter.<p>
     *
     * @param value the value to set
     */
    public void setParamNewtimestamp(String value) {

        m_paramNewtimestamp = value;
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to touch the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_TOUCH);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for touch dialog
            setDialogTitle(Messages.GUI_TOUCH_RESOURCE_1, Messages.GUI_TOUCH_MULTI_2);
        }
    }

    /**
     * Performs the resource touching.<p>
     *
     * @return true, if the resource was touched, otherwise false
     * @throws CmsException if touching is not successful
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        // on folder touch or multi resource operation display "please wait" screen, not for simple file copy
        if (!DIALOG_WAIT.equals(getParamAction())) {
            // check if the "please wait" screen has to be shown
            if (isMultiOperation()) {
                // show please wait for every multi resource operation
                return false;
            } else {
                // check if the single resource is a folder
                CmsResource sourceRes = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
                if (sourceRes.isFolder()) {
                    return false;
                }
            }
        }

        // get the new timestamp for the resource(s) from request parameter
        long timeStamp = 0;
        boolean correctDate = false;
        try {
            if (CmsStringUtil.isNotEmpty(getParamNewtimestamp())) {
                timeStamp = CmsCalendarWidget.getCalendarDate(getMessages(), getParamNewtimestamp(), true);
                correctDate = true;
            }
        } catch (ParseException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_PARSE_TIMESTAMP_1, getParamNewtimestamp()), e);
        }

        // get the flag if the touch is recursive from request parameter
        boolean touchRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();
        // get the flag to the touch the content from request parameter
        boolean touchContent = Boolean.valueOf(getParamContent()).booleanValue();

        // now touch the resource(s)
        Iterator<String> i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            try {
                // lock resource if autolock is enabled
                checkLock(resName);
                touchSingleResource(getCms(), resName, timeStamp, touchRecursive, correctDate, touchContent);
            } catch (CmsException e) {
                // collect exceptions to create a detailed output
                addMultiOperationException(e);
            }
        }
        checkMultiOperationException(Messages.get(), Messages.ERR_TOUCH_MULTI_0);

        return true;
    }
}
