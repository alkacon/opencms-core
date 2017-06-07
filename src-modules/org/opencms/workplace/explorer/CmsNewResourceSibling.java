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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * The new resource sibling dialog handles the creation of a new sibling in the VFS.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_sibling.html
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsNewResourceSibling extends CmsNewResourcePointer {

    /** Request parameter name for the keep properties flag. */
    public static final String PARAM_KEEPPROPERTIES = "keepproperties";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResourceSibling.class);

    /** The keep properties parameter. */
    private String m_paramKeepProperties;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceSibling(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceSibling(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates the new sibling of a resource.<p>
     *
     * @throws JspException if inclusion of error dialog fails
     */
    @Override
    public void actionCreateResource() throws JspException {

        try {
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            String newResourceParam = fullResourceName;
            // the target
            String targetName = getParamLinkTarget();
            if (targetName == null) {
                targetName = "";
            }

            // create the sibling
            String storedSiteRoot = null;
            try {
                if (OpenCms.getSiteManager().getSiteRoot(targetName) != null) {
                    // add site root to new resource path
                    String siteRootFolder = getCms().getRequestContext().getSiteRoot();
                    if (siteRootFolder.endsWith("/")) {
                        siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length() - 1);
                    }
                    fullResourceName = siteRootFolder + fullResourceName;
                    storedSiteRoot = getCms().getRequestContext().getSiteRoot();
                    getCms().getRequestContext().setSiteRoot("/");
                }

                // check if the link target is a file or a folder
                boolean isFolder = false;
                CmsResource targetRes = getCms().readResource(targetName);
                isFolder = targetRes.isFolder();

                if (isFolder) {
                    // link URL is a folder, so copy the folder with all sub resources as siblings
                    if (targetName.endsWith("/")) {
                        targetName = targetName.substring(0, targetName.length() - 1);
                    }
                    // copy the folder
                    getCms().copyResource(targetName, fullResourceName, CmsResource.COPY_AS_SIBLING);
                } else {
                    // link URL is a file, so create sibling of the link target
                    List<CmsProperty> targetProperties = null;
                    boolean keepProperties = Boolean.valueOf(getParamKeepProperties()).booleanValue();
                    if (keepProperties) {
                        // keep the individual properties of the original file
                        try {
                            targetProperties = getCms().readPropertyObjects(targetName, false);
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                    getCms().createSibling(targetName, fullResourceName, targetProperties);
                }

            } finally {
                // restore the site root
                if (storedSiteRoot != null) {
                    getCms().getRequestContext().setSiteRoot(storedSiteRoot);
                }
            }

            // set resource parameter to new resource name for property dialog
            setParamResource(newResourceParam);
            setResourceCreated(true);
        } catch (Throwable e) {
            // error creating pointer, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_CREATE_LINK_0));
            includeErrorpage(this, e);
        }

    }

    /**
     * Forwards to the property dialog if the resourceeditprops parameter is true.<p>
     *
     * If the parameter is not true, the dialog will be closed.<p>
     * If the sibling of the new resource is locked, the paramter will be ignored as properties
     * cannot be created in this case.<p>
     *
     * @throws IOException if forwarding to the property dialog fails
     * @throws ServletException if forwarding to the property dialog fails
     * @throws JspException if an inclusion fails
     */
    @Override
    public void actionEditProperties() throws IOException, JspException, ServletException {

        boolean editProps = Boolean.valueOf(getParamNewResourceEditProps()).booleanValue();
        // get the sibling name
        String newRes = getParamResource();
        // check if this sibling is locked
        try {
            CmsLock lock = getCms().getLock(newRes);
            // if the new resource has no exclusive lock, set the editProps flag to false
            if (!lock.isDirectlyOwnedInProjectBy(getCms())) {
                editProps = false;
            }
        } catch (CmsException e) {
            throw new JspException(e);
        }
        if (editProps) {
            // edit properties checkbox checked, forward to property dialog
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_RESOURCE, new String[] {getParamResource()});
            params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, new String[] {CmsPropertyAdvanced.MODE_WIZARD});
            sendForward(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER, params);
        } else {
            // edit properties not checked, close the dialog
            actionCloseDialog();
        }
    }

    /**
     * Returns the current explorer path for use in Javascript of new sibling dialog.<p>
     *
     * @return the current explorer path
     */
    public String getCurrentPath() {

        String path = getSettings().getExplorerResource();
        if (path == null) {
            path = "/";
        }
        return CmsResource.getFolderPath(path);
    }

    /**
     * Returns the keep properties request parameter value.<p>
     *
     * @return the keep properties request parameter value
     */
    public String getParamKeepProperties() {

        return m_paramKeepProperties;
    }

    /**
     * Sets the keep properties request parameter value.<p>
     *
     * @param keepProperties the keep properties request parameter value
     */
    public void setParamKeepProperties(String keepProperties) {

        m_paramKeepProperties = keepProperties;
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
        // set the action for the JSP switch
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog
            setParamTitle(key(Messages.GUI_NEWRESOURCE_SIBLING_0));
        }
    }

}