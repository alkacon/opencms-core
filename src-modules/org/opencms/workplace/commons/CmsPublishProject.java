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

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsHtmlReport;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Creates the dialogs for publishing a project or a resource.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/publishproject.jsp
 * <li>/commons/publishresource.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsPublishProject extends CmsMultiDialog {

    /** Value for the action: delete the resource. */
    public static final int ACTION_PUBLISH = 110;

    /** Value for the action: resources confirmed. */
    public static final int ACTION_RESOURCES_CONFIRMED = 111;

    /** Request parameter value for the action: dialog resources confirmed. */
    public static final String DIALOG_RESOURCES_CONFIRMED = "resourcesconfirmed";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "publishproject";

    /** Request parameter name for the directpublish parameter. */
    public static final String PARAM_DIRECTPUBLISH = "directpublish";

    /** Request parameter name for the publishsiblings parameter. */
    public static final String PARAM_PUBLISHSIBLINGS = "publishsiblings";

    /** Request parameter name for the relatedresources parameter. */
    public static final String PARAM_RELATEDRESOURCES = "relatedresources";

    /** Request parameter name for the subresources parameter. */
    public static final String PARAM_SUBRESOURCES = "subresources";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishProject.class);

    /** Parameter value for the direct publish flag. */
    private String m_paramDirectpublish;

    /** Parameter value for the progress key. */
    private String m_paramProgresskey;

    /** Parameter value for the project id. */
    private String m_paramProjectid;

    /** Parameter value for the project name. */
    private String m_paramProjectname;

    /** Parameter value for the publish siblings flag. */
    private String m_paramPublishsiblings;

    /** Parameter value for the publish related resources flag. */
    private String m_paramRelatedresources;

    /** Parameter value for the publish subresources flag. */
    private String m_paramSubresources;

    /** The progress bar for the dialog. */
    private CmsProgressWidget m_progress;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPublishProject(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishProject(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsDialog#actionCloseDialog()
     */
    @Override
    public void actionCloseDialog() throws JspException {

        CmsProgressThread thread = CmsProgressWidget.getProgressThread(getParamProgresskey());
        if (thread != null) {
            thread.interrupt();
            CmsProgressWidget.removeProgressThread(thread.getKey());
        }

        super.actionCloseDialog();
    }

    /**
     * Performs the publish action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionPublish() throws JspException {

        try {
            boolean isFolder = false;
            if (!isMultiOperation()) {
                if (isDirectPublish()) {
                    isFolder = getCms().readResource(getParamResource(), CmsResourceFilter.ALL).isFolder();
                }
            }
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned publish operation was successful
                if (isMultiOperation() || isFolder) {
                    // set request attribute to reload the explorer tree view
                    List<String> folderList = new ArrayList<String>();
                    folderList.add(CmsResource.getParentFolder(getResourceList().get(0)));
                    Iterator<String> it = getResourceList().iterator();
                    while (it.hasNext()) {
                        String res = it.next();
                        if (CmsResource.isFolder(res)) {
                            folderList.add(res);
                        }
                    }
                    getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // prepare common message part
            includeErrorpage(this, e);
        }
    }

    /**
     * Returns the html for the confirmation message.<p>
     *
     * @return the html for the confirmation message
     */
    public String buildConfirmation() {

        StringBuffer result = new StringBuffer(512);

        result.append("<p><div id='conf-msg'>\n");
        if (!isDirectPublish()) {
            result.append(key(Messages.GUI_PUBLISH_PROJECT_CONFIRMATION_1, new Object[] {getProjectname()}));
        } else {
            boolean isFolder = false;
            if (!isMultiOperation()) {
                try {
                    isFolder = getCms().readResource(getParamResource(), CmsResourceFilter.ALL).isFolder();
                } catch (CmsException e) {
                    // ignore
                }
            }
            if (isMultiOperation() || isFolder || (hasSiblings() && hasCorrectLockstate())) {
                result.append(key(Messages.GUI_PUBLISH_MULTI_CONFIRMATION_0));
            } else {
                result.append(key(Messages.GUI_PUBLISH_CONFIRMATION_0));
            }
        }
        result.append("\n</div></p>\n");
        return result.toString();
    }

    /**
     * Returns the html code to build the confirmation messages.<p>
     *
     * @return html code
     */
    @Override
    public String buildLockConfirmationMessageJS() {

        StringBuffer html = new StringBuffer(512);
        html.append("<script type='text/javascript'><!--\n");
        html.append("function setConfirmationMessage(locks, blockinglocks) {\n");
        html.append("\tvar confMsg = document.getElementById('conf-msg');\n");
        html.append("\tif (locks > -1) {\n");
        html.append("\t\tdocument.getElementById('butClose').className = 'hide';\n");
        html.append("\t\tdocument.getElementById('butContinue').className = '';\n");
        html.append("\t\tif (locks > 0) {\n");
        html.append("\t\t\tshowAjaxReportContent();\n");
        html.append("\t\t\tconfMsg.innerHTML = '");
        html.append(key(Messages.GUI_PUBLISH_UNLOCK_CONFIRMATION_0));
        html.append("';\n");
        html.append("\t\t} else {\n");
        html.append("\t\tshowAjaxOk();\n");
        html.append("\t\t\tconfMsg.innerHTML = '");
        html.append(key(Messages.GUI_PUBLISH_NO_LOCKS_CONFIRMATION_0));
        html.append("';\n");
        html.append("\t\t}\n");
        html.append("\t} else {\n");
        html.append("\t\tdocument.getElementById('butClose').className = '';\n");
        html.append("\t\tdocument.getElementById('butContinue').className = 'hide';\n");
        html.append("\t\tconfMsg.innerHTML = '");
        html.append(key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0));
        html.append("';\n");
        html.append("\t}\n");
        html.append("}\n");
        html.append("// -->\n");
        html.append("</script>\n");
        return html.toString();
    }

    /**
     * Returns the html code to build the lock dialog.<p>
     *
     * @return html code
     *
     * @throws CmsException if something goes wrong
     */
    public String buildLockDialog() throws CmsException {

        CmsLockFilter nonBlockingFilter = CmsLockFilter.FILTER_ALL;
        nonBlockingFilter = nonBlockingFilter.filterLockableByUser(getCms().getRequestContext().getCurrentUser());
        nonBlockingFilter = nonBlockingFilter.filterSharedExclusive();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamProjectid())) {
            nonBlockingFilter = nonBlockingFilter.filterProject(new CmsUUID(getParamProjectid()));
        }
        return org.opencms.workplace.commons.CmsLock.buildLockDialog(
            this,
            nonBlockingFilter,
            getBlockingFilter(),
            0,
            true);
    }

    /**
     * @see org.opencms.workplace.CmsMultiDialog#buildLockHeaderBox()
     */
    @Override
    public String buildLockHeaderBox() throws CmsException {

        if (isDirectPublish()) {
            return super.buildLockHeaderBox();
        }
        StringBuffer html = new StringBuffer(512);
        // include resource info
        html.append(dialogBlockStart(null));
        html.append(key(org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0));
        html.append(": ");
        html.append(getProjectname());
        html.append(dialogBlockEnd());
        return html.toString();
    }

    /**
     * Override to display additional options in the lock dialog.<p>
     *
     * @return html code to display additional options
     */
    public String buildPublishOptions() {

        // show only for direct publish actions
        StringBuffer result = new StringBuffer(128);
        boolean showOptionSiblings = (isMultiOperation()
            || isOperationOnFolder()
            || (isDirectPublish() && hasSiblings() && hasCorrectLockstate()));
        boolean showOptionSubresources = (isMultiOperation() || isOperationOnFolder());

        result.append("<p>");
        if (showOptionSiblings) {
            // show only for multi resource operation or if resource has siblings and correct lock state
            if (!isMultiOperation() && !isOperationOnFolder()) {
                result.append(key(Messages.GUI_DELETE_WARNING_SIBLINGS_0));
                result.append("<br>");
            }
            result.append("<input type='checkbox' name='");
            result.append(PARAM_PUBLISHSIBLINGS);
            result.append("' value='true' onclick=\"reloadReport();\"");
            if (Boolean.valueOf(getParamPublishsiblings()).booleanValue()) {
                result.append(" checked='checked'");
            }
            result.append(">&nbsp;");
            result.append(key(Messages.GUI_PUBLISH_ALLSIBLINGS_0));
            result.append("<br>\n");
        } else {
            result.append("<input type='hidden' name='");
            result.append(PARAM_PUBLISHSIBLINGS);
            result.append("' value='");
            result.append(Boolean.valueOf(getParamPublishsiblings()));
            result.append("'");
            if (Boolean.valueOf(getParamPublishsiblings()).booleanValue()) {
                result.append(" checked='checked'");
            }
            result.append(">\n");
        }
        if (showOptionSubresources) {
            // at least one folder is selected, show "publish subresources" checkbox
            result.append("<input type='checkbox' name='");
            result.append(PARAM_SUBRESOURCES);
            result.append("' value='true' onclick=\"reloadReport();\"");
            if (Boolean.valueOf(getParamSubresources()).booleanValue()) {
                result.append(" checked='checked'");
            }
            result.append(">&nbsp;");
            if (isMultiOperation()) {
                result.append(key(Messages.GUI_PUBLISH_MULTI_SUBRESOURCES_0));
            } else {
                result.append(key(Messages.GUI_PUBLISH_SUBRESOURCES_0));
            }
            result.append("<br>\n");
        } else {
            result.append("<input type='hidden' name='");
            result.append(PARAM_SUBRESOURCES);
            result.append("' value='");
            result.append(Boolean.valueOf(getParamSubresources()));
            result.append("'");
            if (Boolean.valueOf(getParamSubresources()).booleanValue()) {
                result.append(" checked='checked'");
            }
            result.append(">\n");
        }
        // code for the 'publish related resources' button
        boolean disabled = false;
        if ((OpenCms.getWorkplaceManager().getDefaultUserSettings().getPublishRelatedResources() == CmsDefaultUserSettings.PUBLISH_RELATED_RESOURCES_MODE_FORCE)
            && !OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER)) {
            disabled = true;
        }
        result.append("<input type='checkbox' name='");
        result.append(PARAM_RELATEDRESOURCES);
        result.append("' value='true' onclick=\"reloadReport();\"");
        if (Boolean.valueOf(getParamRelatedresources()).booleanValue()) {
            result.append(" checked='checked'");
        }
        if (disabled) {
            result.append(" disabled='disabled'");
        }
        result.append(">&nbsp;");
        result.append(key(Messages.GUI_PUBLISH_RELATED_RESOURCES_0));
        result.append("<br>\n");
        result.append("</p>\n");
        return result.toString();
    }

    /**
     * Returns the list of the resources to publish with broken relations.<p>
     *
     * @return the list of the resources to publish with broken relations
     */
    public CmsPublishBrokenRelationsList getBrokenRelationsList() {

        return new CmsPublishBrokenRelationsList(getJsp(), getParentFolder());
    }

    /**
     * Returns if a resource will be directly published.<p>
     *
     * @return <code>"true"</code> if a resource will be directly published
     */
    public String getParamDirectpublish() {

        return m_paramDirectpublish;
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getParamFramename()
     */
    @Override
    public String getParamFramename() {

        String fn = super.getParamFramename();
        // to correctly return after publish project
        if ((fn == null) && !isDirectPublish()) {
            fn = "body";
        }
        return fn;
    }

    /**
     * Returns the value for the progress key.<p>
     *
     * @return the value for the progress key
     */
    public String getParamProgresskey() {

        return m_paramProgresskey;
    }

    /**
     * Returns the value of the project id which will be published.<p>
     *
     * @return the String value of the project id
     */
    public String getParamProjectid() {

        return m_paramProjectid;
    }

    /**
     * Returns the value of the project name which will be published.<p>
     *
     * @return the String value of the project name
     */
    public String getParamProjectname() {

        return m_paramProjectname;
    }

    /**
     * Returns if siblings of the resource should be published.<p>
     *
     * @return <code>"true"</code> (String) if siblings of the resource should be published
     */
    public String getParamPublishsiblings() {

        return m_paramPublishsiblings;
    }

    /**
     * Returns the value of the related resources parameter.<p>
     *
     * @return the value of the related resources parameter
     */
    public String getParamRelatedresources() {

        return m_paramRelatedresources;
    }

    /**
     * Returns the value of the subresources parameter.<p>
     *
     * @return the value of the sub resources parameter
     */
    public String getParamSubresources() {

        return m_paramSubresources;
    }

    /**
     * Returns the progress bar for the dialog.<p>
     *
     * @return the progress bar for the dialog
     */
    public CmsProgressWidget getProgress() {

        return m_progress;
    }

    /**
     * Unlocks all selected resources, will be called by the JSP page.<p>
     *
     * @return <code>true</code> if everything went ok
     *
     * @throws JspException if there is some problem including the error page
     */
    public CmsPublishList getPublishList() throws JspException {

        CmsPublishList publishList = null;
        if (isDirectPublish()) {
            // get the offline resource(s) in direct publish mode
            List<CmsResource> publishResources = new ArrayList<CmsResource>(getResourceList().size());
            Iterator<String> i = getResourceList().iterator();
            while (i.hasNext()) {
                String resName = i.next();
                try {
                    publishResources.add(getCms().readResource(resName, CmsResourceFilter.ALL));
                } catch (CmsException e) {
                    addMultiOperationException(e);
                }
            }
            try {
                boolean publishSubResources = Boolean.valueOf(getParamSubresources()).booleanValue();
                boolean publishSiblings = Boolean.valueOf(getParamPublishsiblings()).booleanValue();
                // create publish list for direct publish
                publishList = OpenCms.getPublishManager().getPublishList(
                    getCms(),
                    publishResources,
                    publishSiblings,
                    publishSubResources);
            } catch (CmsException e) {
                addMultiOperationException(e);
            }
        } else {
            try {
                // be careful #getParamProjectid() is always the current project
                publishList = OpenCms.getPublishManager().getPublishList(getCms());
            } catch (CmsException e) {
                addMultiOperationException(e);
            }
        }
        try {
            // throw exception for errors unlocking resources
            checkMultiOperationException(Messages.get(), Messages.ERR_PUBLISH_LIST_CREATION_0);
        } catch (Throwable e) {
            publishList = null;
            // error while unlocking resources, show error screen
            includeErrorpage(this, e);
        }
        getSettings().setPublishList(publishList);
        return publishList;
    }

    /**
     * Returns the list with the resources to publish.<p>
     *
     * @return the list with the resources to publish
     *
     * @throws JspException if creation of publish list fails
     */
    public CmsPublishResourcesList getPublishResourcesList() throws JspException {

        if (getPublishList() != null) {
            return new CmsPublishResourcesList(
                getJsp(),
                getParentFolder(),
                Boolean.valueOf(getParamRelatedresources()).booleanValue());
        }

        return null;
    }

    /**
     * Returns <code>true</code> if the resources to be published will generate broken links.<p>
     *
     * @return <code>true</code> if the resources to be published will generate broken links
     */
    public boolean hasBrokenLinks() {

        //        CmsPublishBrokenRelationsList list = new CmsPublishBrokenRelationsList(getJsp(), getParentFolder());
        //        list.refreshList();

        return (getBrokenRelationsList().getList().getTotalSize() > 0);
    }

    /**
     * Returns <code>true</code> if the current user is allowed
     * to publish the selected resources.<p>
     *
     * @return <code>true</code> if the current user is allowed
     *          to publish the selected resources
     */
    public boolean isCanPublish() {

        return OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER);
    }

    /**
     * Returns <code>true</code> if the selection has blocking locks.<p>
     *
     * @return <code>true</code> if the selection has blocking locks
     */
    public boolean isLockStateOk() {

        org.opencms.workplace.commons.CmsLock lockDialog = new org.opencms.workplace.commons.CmsLock(getJsp());
        lockDialog.setParamIncluderelated(CmsStringUtil.TRUE);
        lockDialog.setBlockingFilter(getBlockingFilter());
        if (!isDirectPublish()) {
            lockDialog.setParamResource("/");
        }
        if (!lockDialog.getBlockingLockedResources().isEmpty()) {
            // blocking locks found, so show them
            return false;
        }
        if (!isDirectPublish()) {
            // is publish project operation no resource iteration possible
            return true;
        }

        // flag to indicate that all resources are exclusive locked
        boolean locked = true;
        // flag to indicate that all resources are unlocked
        boolean unlocked = true;

        Iterator<String> i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            try {
                CmsLock lock = getCms().getLock(getCms().readResource(resName, CmsResourceFilter.ALL));
                if (!lock.isUnlocked()) {
                    unlocked = false;
                    if (locked
                        && !lock.isOwnedInProjectBy(
                            getCms().getRequestContext().getCurrentUser(),
                            getCms().getRequestContext().getCurrentProject())) {
                        // locks of another users or locked in another project are blocking
                        locked = false;
                    }
                }
            } catch (CmsException e) {
                // error reading a resource, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return locked || unlocked;
    }

    /**
     * Sets if a resource will be directly published.<p>
     *
     * @param value <code>"true"</code> (String) if a resource will be directly published
     */
    public void setParamDirectpublish(String value) {

        m_paramDirectpublish = value;
    }

    /**
     * Sets the value for the progress key.<p>
     *
     * @param value the value for the progress key to set
     */
    public void setParamProgresskey(String value) {

        m_paramProgresskey = value;
    }

    /**
     * Sets the value of the project id which will be published.<p>
     *
     * @param value the String value of the project id
     */
    public void setParamProjectid(String value) {

        m_paramProjectid = value;
    }

    /**
     * Sets the value of the project name which will be published.<p>
     *
     * @param value the String value of the project name
     */
    public void setParamProjectname(String value) {

        m_paramProjectname = value;
    }

    /**
     * Sets if siblings of the resource should be published.<p>
     *
     * @param value <code>"true"</code> (String) if siblings of the resource should be published
     */
    public void setParamPublishsiblings(String value) {

        m_paramPublishsiblings = value;
    }

    /**
     * Sets the value of the related resources parameter.<p>
     *
     * @param relatedResources the value of the related resources parameter
     */
    public void setParamRelatedresources(String relatedResources) {

        m_paramRelatedresources = relatedResources;
    }

    /**
     * Sets the value of the subresources parameter.<p>
     *
     * @param paramSubresources the value of the subresources parameter
     */
    public void setParamSubresources(String paramSubresources) {

        m_paramSubresources = paramSubresources;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        m_progress = new CmsProgressWidget(getJsp());
        m_progress.setWidth("300px");

        // set the publishing type: publish project or direct publish
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamResource()) || isMultiOperation()) {
            setParamDirectpublish(CmsStringUtil.TRUE);
        }
        // set default options
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamAction()) || DIALOG_INITIAL.equals(getParamAction())) {
            // siblings option. set to the default value defined in the opencms-workplace.xml
            setParamPublishsiblings(String.valueOf(getSettings().getUserSettings().getDialogPublishSiblings()));
            // sub resources option. default value is true
            setParamSubresources(Boolean.TRUE.toString());
            // related resources option.
            String defValue = CmsStringUtil.TRUE;
            if (OpenCms.getWorkplaceManager().getDefaultUserSettings().getPublishRelatedResources() == CmsDefaultUserSettings.PUBLISH_RELATED_RESOURCES_MODE_FALSE) {
                defValue = CmsStringUtil.FALSE;
            }
            setParamRelatedresources(defValue);
        }

        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_PUBLISH);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_RESOURCES_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_RESOURCES_CONFIRMED);

            // merge publish list with related resources if needed
            CmsPublishList publishList = getSettings().getPublishList();
            if (publishList == null) { // this may happen if the user has not publish permissions (with multi selection)
                // no publish permissions for the resource, set cancel action to close dialog
                setAction(ACTION_CANCEL);
                return;
            }
            if (Boolean.valueOf(getParamRelatedresources()).booleanValue() && publishList.isDirectPublish()) {
                try {
                    // try to find the publish list with related related resources in the progress thread
                    CmsProgressThread thread = CmsProgressWidget.getProgressThread(getParamProgresskey());
                    CmsPublishList storedList = null;
                    if (thread != null) {
                        storedList = ((CmsPublishResourcesList)thread.getList()).getPublishList();
                    }

                    if (storedList == null) {
                        CmsPublishList relResources = OpenCms.getPublishManager().getRelatedResourcesToPublish(
                            getCms(),
                            publishList);
                        publishList = OpenCms.getPublishManager().mergePublishLists(
                            getCms(),
                            publishList,
                            relResources);
                    } else {
                        publishList = storedList;
                    }

                    getSettings().setPublishList(publishList);
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }

            // start the progress
            CmsProgressWidget.removeProgressThread(getProgress().getKey());
            getProgress().startProgress(getBrokenRelationsList());

            // wait to see if already finished
            synchronized (this) {
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            CmsProgressThread thread = CmsProgressWidget.getProgressThread(getProgress().getKey());
            if ((!thread.isAlive()) && (thread.getList().getList().getTotalSize() == 0)) {
                // skip broken links confirmation screen
                setAction(ACTION_PUBLISH);
            }

        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);

            // set parameters depending on publishing type
            if (isDirectPublish()) {
                // check the required permissions to publish the resource directly
                if (!getCms().isManagerOfProject()
                    && !checkResourcePermissions(CmsPermissionSet.ACCESS_DIRECT_PUBLISH, false)) {
                    // no publish permissions for the single resource, set cancel action to close dialog
                    setAction(ACTION_CANCEL);
                    return;
                }
                // add the title for the direct publish dialog
                setDialogTitle(Messages.GUI_PUBLISH_RESOURCE_1, Messages.GUI_PUBLISH_MULTI_2);
            } else {
                // add the title for the publish project dialog
                setParamTitle(key(Messages.GUI_PUBLISH_PROJECT_0));
                // determine the project id and name for publishing
                computePublishProject();
                // determine target to close the report
            }
            // if lock state if not as expected
            if (isLockStateOk()) { // this may take a while :(
                // skip lock confirmation screen
                setAction(ACTION_LOCKS_CONFIRMED);
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsMultiDialog#performDialogOperation()
     */
    @Override
    protected boolean performDialogOperation() throws CmsException {

        CmsPublishList publishList = getSettings().getPublishList();
        if (publishList == null) {
            throw new CmsException(
                Messages.get().container(org.opencms.db.Messages.ERR_GET_PUBLISH_LIST_PROJECT_1, getProjectname()));
        }
        OpenCms.getPublishManager().publishProject(
            getCms(),
            new CmsHtmlReport(getLocale(), getCms().getRequestContext().getSiteRoot()),
            publishList);
        // wait 2 seconds, may be it finishes fast
        OpenCms.getPublishManager().waitWhileRunning(1500);
        return true;
    }

    /**
     * Determine the right project id and name if no request parameter "projectid" is given.<p>
     */
    private void computePublishProject() {

        String projectId = getParamProjectid();
        CmsUUID id;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(projectId)) {
            // projectid not found in request parameter,
            id = getCms().getRequestContext().getCurrentProject().getUuid();
            setParamProjectname(getCms().getRequestContext().getCurrentProject().getName());
            setParamProjectid("" + id);
        } else {
            id = new CmsUUID(projectId);
            try {
                setParamProjectname(getCms().readProject(id).getName());
            } catch (CmsException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_SET_PROJECT_NAME_FAILED_0), e);
            }
        }
    }

    /**
     * Returns the filter to identify blocking locks.<p>
     *
     * @return the filter to identify blocking locks
     */
    private CmsLockFilter getBlockingFilter() {

        CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
        blockingFilter = blockingFilter.filterNotLockableByUser(getCms().getRequestContext().getCurrentUser());
        if (!isDirectPublish()) {
            blockingFilter = blockingFilter.filterProject(new CmsUUID(getParamProjectid()));
        }
        return blockingFilter;
    }

    /**
     * Returns the parent folder for the publish process.<p>
     *
     * @return the parent folder for the publish process
     */
    private String getParentFolder() {

        String relativeTo;
        if (isDirectPublish()) {
            relativeTo = CmsResource.getParentFolder(getResourceList().get(0));
        } else {
            relativeTo = getCms().getRequestContext().getSiteRoot() + "/";
        }
        return relativeTo;
    }

    /**
     * Returns the project name.<p>
     *
     * @return the project name
     */
    private String getProjectname() {

        CmsUUID id = new CmsUUID(getParamProjectid());
        try {
            return getCms().readProject(id).getName();
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_SET_PROJECT_NAME_FAILED_0), e);
        }
        return "-error-";
    }

    /**
     * Returns <code>false</code> if this is a publish project operation.<p>
     *
     * @return <code>true</code> if this is a direct publish operation
     */
    private boolean isDirectPublish() {

        if (getParamDirectpublish() != null) {
            return Boolean.valueOf(getParamDirectpublish()).booleanValue();
        }
        return getDialogUri().endsWith("publishresource.jsp");
    }
}
