/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.sites;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.administration.A_CmsImportFromHttp;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Uploads and stores a favicon.<p>
 */
public class CmsSitesFaviconUpload extends A_CmsImportFromHttp {

    /** The name for the favicon. */
    public static final String ICON_NAME = "favicon.ico";

    /** The URI for this dialog. */
    private static final String DIALOG_URI = PATH_WORKPLACE + "admin/sites/favicon.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitesFaviconUpload.class);

    /** The sites parameter. */
    private String m_paramSites;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesFaviconUpload(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesFaviconUpload(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        String site = getParamSites();
        try {
            // check if the site stored in the sites parameter exists and is readable
            CmsObject cms = OpenCms.initCmsObject(getCms());
            cms.getRequestContext().setSiteRoot("");
            cms.existsResource(site + "/");

            // copy the file to the server
            copyFileToServer(OpenCms.getSystemInfo().getPackagesRfsPath());
            if ((getParamImportfile() == null) || !getParamImportfile().equals(ICON_NAME)) {
                // file null or name not valid
                throw new CmsException(Messages.get().container(
                    Messages.ERR_INVALID_FAVICON_FILE_1,
                    getParamImportfile()));
            }

            // get the uploaded file content
            String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
            importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importpath + getParamImportfile());
            RandomAccessFile f = new RandomAccessFile(importpath, "r");
            byte[] content = new byte[(int)f.length()];
            f.read(content);
            f.close();

            // check the existence of favicon
            String favCreatePath = site + "/" + ICON_NAME;
            int imageResId = CmsResourceTypeImage.getStaticTypeId();
            if (cms.existsResource(favCreatePath)) {
                // replace the existing favicon
                cms.replaceResource(favCreatePath, imageResId, content, new ArrayList<CmsProperty>());
            } else {
                // create the new favicon
                cms.createResource(favCreatePath, imageResId, content, new ArrayList<CmsProperty>());
            }

            // set the dialog parameters
            String title = OpenCms.getSiteManager().getSiteForSiteRoot(site).getTitle();
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsSitesList.PARAM_SITES, new String[] {getParamSites()});
            params.put(CmsSitesList.PARAM_SITE_TITLE, new String[] {title});
            params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});

            // forward the request
            getToolManager().jspForwardTool(this, "/sites/detail", params);

        } catch (CmsException e) {
            // error copying the file to the OpenCms server
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(getLocale()), e);
            }
            setException(e);
            return;
        }
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getDialogReturnUri()
     */
    @Override
    public String getDialogReturnUri() {

        return DIALOG_URI;
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getImportMessage()
     */
    @Override
    public String getImportMessage() {

        return key(Messages.GUI_SITES_IMPORT_FAV_0);
    }

    /**
     * Returns the paramSites.<p>
     *
     * @return the paramSites
     */
    public String getParamSites() {

        return m_paramSites;
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getStarttext()
     */
    @Override
    public String getStarttext() {

        return key(Messages.GUI_SITES_IMPORT_FAV_BLOCK_0);
    }

    /**
     * Sets the paramSites.<p>
     *
     * @param paramSites the paramSites to set
     */
    public void setParamSites(String paramSites) {

        m_paramSites = paramSites;
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#defaultActionHtml()
     */
    @Override
    protected String defaultActionHtml() {

        return super.defaultActionHtml().replaceAll("application/zip", "image/x-icon");
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.Messages.get().getBundleName());

    }
}
