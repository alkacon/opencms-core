/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsDisplayResource.java,v $
 * Date   : $Date: 2005/06/16 16:56:21 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Shows a preview of the selected resource in the Explorer view.<p>
 * 
 * This is required to get correct previews of statically exported pages
 * in the Online project.<p>
 * 
 * The following file uses this class:
 * <ul>
 * <li>/commons/displayresource.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class CmsDisplayResource extends CmsDialog {

    /** Request parameter name for versionid. */
    public static final String PARAM_VERSIONID = "versionid";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDisplayResource.class);

    /** The controller. */
    private CmsFlexController m_controller;

    /** The version id parameter. */
    private String m_paramVersionid;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDisplayResource(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDisplayResource(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        m_controller = CmsFlexController.getController(req);
    }

    /**
     * Returns the content of a backup resource.<p>
     * 
     * @param cms a CmsObject
     * @param resource the name of the backup resource
     * @param versionId the version id of the backup resource
     * @return the content of a backup resource
     */
    protected static byte[] getBackupResourceContent(CmsObject cms, String resource, String versionId) {

        if (CmsStringUtil.isNotEmpty(resource) && CmsStringUtil.isNotEmpty(versionId)) {

            // try to load the backup resource
            CmsBackupResource res = null;
            try {
                res = cms.readBackupFile(resource, Integer.parseInt(versionId));
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
                return "".getBytes();
            }
            byte[] backupResourceContent = res.getContents();
            backupResourceContent = CmsEncoder.changeEncoding(
                backupResourceContent,
                OpenCms.getSystemInfo().getDefaultEncoding(),
                cms.getRequestContext().getEncoding());
            return backupResourceContent;
        }

        return "".getBytes();
    }

    /**
     * Redirects to the specified file or shows backup resource.<p>
     * 
     * @throws Exception if redirection fails
     */
    public void actionShow() throws Exception {

        String url = getJsp().link(getParamResource());
        // try to load the backup resource
        if (CmsStringUtil.isNotEmpty(getParamVersionid())) {
            byte[] result = getBackupResourceContent(getCms(), getParamResource(), getParamVersionid());
            if (result != null) {
                // get the top level ressponse to change the content type
                m_controller.getTopResponse().setContentType(
                    OpenCms.getResourceManager().getMimeType(
                        getParamResource(),
                        getCms().getRequestContext().getEncoding()));
                m_controller.getTopResponse().setContentLength(result.length);

                try {
                    getJsp().getResponse().getOutputStream().write(result);
                    getJsp().getResponse().getOutputStream().flush();
                } catch (IOException e) {
                    // can usually be ignored
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e.getLocalizedMessage());
                    }
                    return;
                }
            }
        } else {
            if (getCms().existsResource(getParamResource(), CmsResourceFilter.DEFAULT)) {
                getJsp().getResponse().sendRedirect(url);
            } else {
                // resource is outside time window, show error message
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1,
                    getParamResource()));
            }
        }
    }

    /**
     * Returns the paramVersionid.<p>
     *
     * @return the paramVersionid
     */
    public String getParamVersionid() {

        return m_paramVersionid;
    }

    /**
     * Sets the paramVersionid.<p>
     *
     * @param paramVersionid the paramVersionid to set
     */
    public void setParamVersionid(String paramVersionid) {

        m_paramVersionid = paramVersionid;
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
    }
}