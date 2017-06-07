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

package org.opencms.workplace;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsMultiException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * The base class to build a dialog capable of multiple file operations.<p>
 *
 * Extend this class for workplace dialogs that can perform operations on more than one
 * VFS resource like copy, move, touch etc.<p>
 *
 * Provides methods to determine if a multi-resource operation has to be done and helper methods,
 * e.g. to get the list of resources to work with.<p>
 *
 * @since 6.2.0
 */
public abstract class CmsMultiDialog extends CmsDialog {

    /** The delimiter that is used in the resource list request parameter. */
    public static final String DELIMITER_RESOURCES = "|";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMultiDialog.class);

    /** Collects all eventually thrown exceptions during a multi operation. */
    private CmsMultiException m_multiOperationException;

    /** The resource list parameter value. */
    private String m_paramResourcelist;

    /** The list of resource names for the multi operation. */
    private List<String> m_resourceList;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsMultiDialog(CmsJspActionElement jsp) {

        super(jsp);
        m_multiOperationException = new CmsMultiException();
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsMultiDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Adds an exception thrown during a multi resource operation to the multi exception.<p>
     *
     * After iterating the dialog resources, use {@link #checkMultiOperationException(I_CmsMessageBundle, String)} to
     * display the multi exception depending on collected exceptions.<p>
     *
     * @param exc the exception that was thrown
     */
    public void addMultiOperationException(CmsException exc) {

        m_multiOperationException.addException(exc);
    }

    /**
     * @see org.opencms.workplace.CmsDialog#buildLockHeaderBox()
     */
    @Override
    public String buildLockHeaderBox() throws CmsException {

        if (!isMultiOperation()) {
            return super.buildLockHeaderBox();
        }
        StringBuffer html = new StringBuffer(1024);
        // include multi resource list
        html.append(dialogBlockStart(key(org.opencms.workplace.commons.Messages.GUI_MULTI_RESOURCELIST_TITLE_0)));
        html.append(buildResourceList());
        html.append(dialogBlockEnd());
        return html.toString();
    }

    /**
     * Builds the HTML for the resource list that is affected by the multi operation.<p>
     *
     * @return the HTML for the resource list that is affected by the multi operation
     */
    public String buildResourceList() {

        // check how many resources are selected to decide using a div or not
        boolean scroll = (getResourceList().size() > 6);

        StringBuffer result = new StringBuffer(1024);

        result.append(dialogWhiteBoxStart());

        // if the output to long, wrap it in a div
        if (scroll) {
            result.append("<div style='width: 100%; height:100px; overflow: auto;'>\n");
        }

        result.append("<table border=\"0\">\n");
        Iterator<String> i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            result.append("\t<tr>\n");
            result.append("\t\t<td class='textbold' style=\"vertical-align:top;\">");
            result.append(CmsResource.getName(resName));
            result.append("&nbsp;</td>\n\t\t<td style=\"vertical-align:top;\">");
            String title = null;
            try {
                // get the title property value
                title = getCms().readPropertyObject(resName, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                    null);
            } catch (CmsException e) {
                // ignore this exception, title not found
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
                // show the title information
                result.append(title);
            }
            result.append("</td>\n\t</tr>\n");
        }
        result.append("</table>");

        // close the div if needed
        if (scroll) {
            result.append("</div>\n");
        }
        result.append(dialogWhiteBoxEnd());
        return result.toString();
    }

    /**
     * Checks if an exception occurred during a multi resource operation, and throws a new exception if necessary.<p>
     *
     * @param messages the message bundle to use for the exception message generation
     * @param key the key for the exception to throw with one parameter
     * @throws CmsException the exception that is thrown when the multi operation was not successful
     */
    public void checkMultiOperationException(I_CmsMessageBundle messages, String key) throws CmsException {

        if (m_multiOperationException.hasExceptions()) {
            m_multiOperationException.setMessage(
                new CmsMessageContainer(messages, key, new Object[] {m_multiOperationException}));
            throw m_multiOperationException;
        }
    }

    /**
     * Returns the value of the resource list parameter, or null if the parameter is not provided.<p>
     *
     * This parameter selects the resources to perform operations on.<p>
     *
     * @return the value of the resource list parameter or null, if the parameter is not provided
     */
    public String getParamResourcelist() {

        if (CmsStringUtil.isNotEmpty(m_paramResourcelist) && !"null".equals(m_paramResourcelist)) {
            return m_paramResourcelist;
        } else {
            return null;
        }
    }

    /**
     * Returns the resources that are defined for the dialog operation.<p>
     *
     * For single resource operations, the list contains one item: the resource name found
     * in the request parameter value of the "resource" parameter.<p>
     *
     * @return the resources that are defined for the dialog operation
     */
    public List<String> getResourceList() {

        if (m_resourceList == null) {
            // use lazy initializing
            if (getParamResourcelist() != null) {
                // found the resourcelist parameter
                m_resourceList = CmsStringUtil.splitAsList(getParamResourcelist(), DELIMITER_RESOURCES, true);
                Collections.sort(m_resourceList);
            } else {
                // this is a single resource operation, create list containing the resource name
                m_resourceList = new ArrayList<String>(1);
                m_resourceList.add(getParamResource());
            }
        }
        return m_resourceList;
    }

    /**
     * Returns the value of the resourcelist parameter in form of a String separated
     * with {@link #DELIMITER_RESOURCES}, or the value of the  resource parameter if the
     * first parameter is not provided (no multiple choice has been done.<p>
     *
     * This may be used for jsps as value for the parameter for resources {@link #PARAM_RESOURCELIST}.<p>
     *
     * @return the value of the resourcelist parameter or null, if the parameter is not provided
     */
    public String getResourceListAsParam() {

        String result = getParamResourcelist();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = getParamResource();
        }
        return result;
    }

    /**
     * Returns true if the dialog operation has to be performed on multiple resources.<p>
     *
     * @return true if the dialog operation has to be performed on multiple resources, otherwise false
     */
    public boolean isMultiOperation() {

        return (getResourceList().size() > 1);
    }

    /**
     * Sets the title of the dialog depending on the operation type, multiple or single operation.<p>
     *
     * @param singleKey the key for the single operation
     * @param multiKey the key for the multiple operation
     */
    public void setDialogTitle(String singleKey, String multiKey) {

        if (isMultiOperation()) {
            // generate title with number of selected resources and parent folder parameters
            String resCount = String.valueOf(getResourceList().size());
            String currentFolder = CmsResource.getFolderPath(getSettings().getExplorerResource());
            currentFolder = CmsStringUtil.formatResourceName(currentFolder, 40);
            Object[] params = new Object[] {resCount, currentFolder};
            setParamTitle(key(multiKey, params));
        } else {
            // generate title using the resource name as parameter for the key
            String resourceName = CmsStringUtil.formatResourceName(getParamResource(), 50);
            setParamTitle(key(singleKey, new Object[] {resourceName}));
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#setParamResource(java.lang.String)
     */
    @Override
    public void setParamResource(String value) {

        super.setParamResource(value);
        m_resourceList = null;
    }

    /**
     * Sets the value of the resourcelist parameter.<p>
     *
     * @param paramResourcelist the value of the resourcelist parameter
     */
    public void setParamResourcelist(String paramResourcelist) {

        m_paramResourcelist = paramResourcelist;
        m_resourceList = null;
    }

    /**
     * Checks if the permissions of the current user on the single resource to use in the dialog are sufficient.<p>
     *
     * For a multi resource operation, this returns always true, checks only for single resource operations.<p>
     *
     * @see CmsDialog#checkResourcePermissions(CmsPermissionSet, boolean)
     *
     * @param required the required permissions for the dialog
     * @param neededForFolder if true, the permissions are required for the parent folder of the resource (e.g. for editors)
     * @return true if the permissions are sufficient, otherwise false
     */
    @Override
    protected boolean checkResourcePermissions(CmsPermissionSet required, boolean neededForFolder) {

        if (isMultiOperation()) {
            // for multi resource operation, return always true
            return true;
        } else {
            // check for single resource operation
            return super.checkResourcePermissions(required, neededForFolder);
        }
    }

    /**
     * Checks if the resource operation is an operation on at least one folder.<p>
     *
     * @return true if the operation an operation on at least one folder, otherwise false
     */
    protected boolean isOperationOnFolder() {

        Iterator<String> i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = i.next();
            try {
                CmsResource curRes = getCms().readResource(resName, CmsResourceFilter.ALL);
                if (curRes.isFolder()) {
                    // found a folder
                    return true;
                }
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }
        return false;
    }

    /**
     * Performs the dialog operation for the selected resources.<p>
     *
     * @return true, if the operation was successful, otherwise false
     *
     * @throws CmsException if operation was not successful
     */
    protected abstract boolean performDialogOperation() throws CmsException;

}