/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsPublishProject.java,v $
 * Date   : $Date: 2006/10/26 11:27:19 $
 * Version: $Revision: 1.27.4.5 $
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

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.threads.CmsPublishThread;
import org.opencms.workplace.threads.CmsRelationsValidatorThread;

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
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.27.4.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPublishProject extends CmsReport {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "publishproject";

    /** Request parameter name for the publishsiblings parameter. */
    public static final String PARAM_PUBLISHSIBLINGS = "publishsiblings";

    /** Request parameter name for the subresources parameter. */
    public static final String PARAM_SUBRESOURCES = "subresources";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishProject.class);

    private String m_paramDirectpublish;
    private String m_paramProjectid;
    private String m_paramProjectname;
    private String m_paramPublishsiblings;
    private String m_paramSubresources;

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
     * Performs the publish report, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
                actionCloseDialog();
                break;
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(FILE_REPORT_OUTPUT);

                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            default:
                try {
                    boolean directPublish = Boolean.valueOf(getParamDirectpublish()).booleanValue();
                    boolean publishSubResources = Boolean.valueOf(getParamSubresources()).booleanValue();

                    CmsPublishList publishList = null;
                    if (directPublish) {
                        // get the offline resource(s) in direct publish mode
                        List publishResources = new ArrayList(getResourceList().size());
                        Iterator i = getResourceList().iterator();
                        while (i.hasNext()) {
                            String resName = (String)i.next();
                            try {
                                CmsResource res = getCms().readResource(resName, CmsResourceFilter.ALL);
                                publishResources.add(res);
                                // check if the resource is locked                   
                                CmsLock lock = getCms().getLock(resName);
                                if (!lock.isNullLock()) {
                                    // resource is locked, so unlock it
                                    getCms().unlockResource(resName);
                                } else {
                                    // if resource is unlocked
                                    if (res.isFolder() && publishSubResources) {
                                        // force unlock all subresources
                                        String folderName = resName;
                                        if (!folderName.endsWith("/")) {
                                            folderName += "/";
                                        }
                                        getCms().lockResource(folderName);
                                        getCms().unlockResource(folderName);
                                    }
                                }
                            } catch (CmsException e) {
                                addMultiOperationException(e);
                            }
                        }

                        // create publish list for direct publish
                        publishList = getCms().getPublishList(
                            publishResources,
                            Boolean.valueOf(getParamPublishsiblings()).booleanValue(),
                            publishSubResources);
                        // check permissions
                        getCms().checkPublishPermissions(publishList);

                        // for error(s) unlocking resource(s), throw exception
                        checkMultiOperationException(Messages.get(), Messages.ERR_PUBLISH_MULTI_UNLOCK_0);
                    } else {
                        if (getCms().getRequestContext().currentProject().getType() == CmsProject.PROJECT_TYPE_TEMPORARY) {
                            // set the flag that this is a temporary project
                            setParamRefreshWorkplace(CmsStringUtil.TRUE);
                        }
                        // unlock all project resources
                        getCms().unlockProject(Integer.parseInt(getParamProjectid()));
                    }

                    // start the link validation thread before publishing
                    CmsRelationsValidatorThread thread = new CmsRelationsValidatorThread(
                        getCms(),
                        publishList,
                        getSettings());
                    setParamAction(REPORT_BEGIN);
                    setParamThread(thread.getUUID().toString());

                    // set the flag that another thread is following
                    setParamThreadHasNext(CmsStringUtil.TRUE);
                    // set the key name for the continue checkbox
                    setParamReportContinueKey(Messages.GUI_PUBLISH_CONTINUE_BROKEN_LINKS_0);
                    getJsp().include(FILE_REPORT_OUTPUT);
                } catch (Throwable e) {
                    // error while unlocking resources, show error screen
                    includeErrorpage(this, e);
                }
        }
    }

    /**
     * Override to display additional options in the lock dialog.<p>
     * 
     * @return html code to display additional options
     */
    public String buildLockAdditionalOptions() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamProjectid())) {
            // publish project
            return "<br>\n" + key(Messages.GUI_PUBLISH_PROJECT_CONFIRMATION_1, new Object[] {getProjectname()});
        }
        // show only for direct publish actions
        StringBuffer result = new StringBuffer(128);
        CmsResource res = null;
        if (!isMultiOperation()) {
            try {
                res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // res will be null
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }
        boolean showSiblingCheckBox = false;
        if (isMultiOperation()
            || ((res != null) && res.isFile() && (res.getSiblingCount() > 1))
            || ((res != null) && res.isFolder())) {
            // resource is file and has siblings, so create checkbox

            result.append(dialogSpacer());
            result.append("<input type=\"checkbox\" name=\"");
            result.append(PARAM_PUBLISHSIBLINGS);
            result.append("\" value=\"true\"");
            // set the checkbox state to the default value defined in the opencms.properties
            if (getSettings().getUserSettings().getDialogPublishSiblings()) {
                result.append(" checked=\"checked\"");
            }
            result.append(">&nbsp;");
            result.append(key(Messages.GUI_PUBLISH_ALLSIBLINGS_0));
            showSiblingCheckBox = true;
        }
        if (isOperationOnFolder()) {
            // at least one folder is selected, show "publish subresources" checkbox
            if (showSiblingCheckBox) {
                result.append("<br>");
            }
            result.append("<input type=\"checkbox\" name=\"");
            result.append(PARAM_SUBRESOURCES);
            result.append("\" value=\"true\" checked=\"checked\">&nbsp;");
            if (isMultiOperation()) {
                result.append(key(Messages.GUI_PUBLISH_MULTI_SUBRESOURCES_0));
            } else {
                result.append(key(Messages.GUI_PUBLISH_SUBRESOURCES_0));
            }
        }
        result.append("<br>\n<br>\n");
        result.append(key(Messages.GUI_PUBLISH_CONFIRMATION_0));
        return result.toString();
    }

    /**
     * Returns the html code to build the confirmation messages.<p>
     * 
     * @return html code
     */
    public String buildLockConfirmationMessageJS() {

        StringBuffer html = new StringBuffer(512);
        html.append("<script type='text/javascript'><!--\n");
        html.append("function setConfirmationMessage(locks, blockinglocks) {\n");
        html.append("\tvar confMsg = document.getElementById('conf-msg');\n");
        html.append("\tif (locks > -1) {\n");
        html.append("\t\tdocument.getElementById('butClose').className = 'hide';\n");
        html.append("\t\tdocument.getElementById('butContinue').className = '';\n");
        html.append("\t\tif (locks > 0) {\n");
        html.append("\t\t\tconfMsg.innerHTML = '");
        html.append(key(Messages.GUI_PUBLISH_UNLOCK_CONFIRMATION_0));
        html.append("';\n");
        html.append("\t\t} else {\n");
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

        CmsLockFilter nonBlockingFilter = CmsLockFilter.FILTER_NON_INHERITED;
        nonBlockingFilter = nonBlockingFilter.filterIncludedUserId(getCms().getRequestContext().currentUser().getId());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamProjectid())) {
            nonBlockingFilter = CmsLockFilter.FILTER_ALL.filterProject(Integer.parseInt(getParamProjectid()));
        }
        CmsLockFilter blockingFilter = CmsLockFilter.FILTER_INHERITED;
        return buildLockDialog(nonBlockingFilter, blockingFilter, 0);
    }

    /**
     * @see org.opencms.workplace.CmsMultiDialog#buildLockHeaderBox()
     */
    public String buildLockHeaderBox() throws CmsException {

        if (Boolean.valueOf(getParamDirectpublish()).booleanValue()) {
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
     * Returns if a resource will be directly published.<p>
     * 
     * @return <code>"true"</code> if a resource will be directly published
     */
    public String getParamDirectpublish() {

        return m_paramDirectpublish;
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
     * Returns the value of the subresources parameter.<p>
     * 
     * @return the value of the subresources parameter
     */
    public String getParamSubresources() {

        return m_paramSubresources;
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
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set to refresh the folder tree
        setParamRefreshWorkplace("true");

        // set the publishing type: publish project or direct publish
        if (CmsStringUtil.isNotEmpty(getParamResource()) || isMultiOperation()) {
            setParamDirectpublish(CmsStringUtil.TRUE);
        }
        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            // skip unlock confirmation dialog
            setAction(ACTION_CONFIRMED);
        } else if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (REPORT_END.equals(getParamAction())) {
            if (Boolean.valueOf(getParamThreadHasNext()).booleanValue()) {
                // after the link check start the publish thread
                startPublishThread();

                setParamAction(REPORT_UPDATE);
                setAction(ACTION_REPORT_UPDATE);
            } else {
                // ends the publish thread
                setAction(ACTION_REPORT_END);
            }
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // set parameters depending on publishing type
            if (Boolean.valueOf(getParamDirectpublish()).booleanValue()) {
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
        }
    }

    /**
     * Determine the right project id and name if no request parameter "projectid" is given.<p>
     */
    private void computePublishProject() {

        String projectId = getParamProjectid();
        int id;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(projectId)) {
            // projectid not found in request parameter,
            id = getCms().getRequestContext().currentProject().getId();
            setParamProjectname(getCms().getRequestContext().currentProject().getName());
            setParamProjectid("" + id);
        } else {
            id = Integer.parseInt(projectId);
            try {
                setParamProjectname(getCms().readProject(id).getName());
            } catch (CmsException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_SET_PROJECT_NAME_FAILED_0), e);
            }
        }
    }

    /**
     * Returns the project name.<p>
     * 
     * @return the project name
     */
    private String getProjectname() {

        int id = Integer.parseInt(getParamProjectid());
        try {
            return getCms().readProject(id).getName();
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_SET_PROJECT_NAME_FAILED_0), e);
        }
        return "-error-";
    }

    /**
     * Starts the publish thread for the project or a resource.<p>
     * 
     * The type of publish thread is determined by the value of the "directpublish" parameter.<p>
     */
    private void startPublishThread() {

        // create a publish thread from the current publish list
        CmsPublishList publishList = getSettings().getPublishList();
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)getJsp().getRequest().getSession().getAttribute(
            CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        CmsPublishThread thread = new CmsPublishThread(getCms(), publishList, settings);

        // set the new thread id and flag that no thread is following
        setParamThread(thread.getUUID().toString());
        setParamThreadHasNext(CmsStringUtil.FALSE);

        // start the publish thread
        thread.start();
    }
}
