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

package org.opencms.ade.sitemap;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Sitemap action used to generate the sitemap editor.<p>
 *
 * See jsp file <tt>/system/workplace/commons/sitemap.jsp</tt>.<p>
 *
 * @since 8.0.0
 */
public class CmsSitemapActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.sitemap";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = "sitemap";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapActionElement.class);

    /** The current sitemap data. */
    private CmsSitemapData m_sitemapData;

    /**
     * Constructor.<p>
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitemapActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        return "";
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export(".vfsMode"));
        sb.append(
            exportDictionary(
                CmsSitemapData.DICT_NAME,
                I_CmsSitemapService.class.getMethod("prefetch", String.class),
                getSitemapData()));
        sb.append(exportModuleScriptTag(GWT_MODULE_NAME));
        String vaadinBootstrap = CmsStringUtil.joinPaths(
            OpenCms.getSystemInfo().getContextPath(),
            "VAADIN/vaadinBootstrap.js");
        sb.append("  <script type=\"text/javascript\"\n" + "          src=\"" + vaadinBootstrap + "\"></script>");
        sb.append(
            "<script type='text/javascript'>    \n"
                + "function initVaadin() { "
                + CmsVaadinUtils.getBootstrapScript(getCmsObject(), "sitemap-ui", "workplace/sitemap/")
                + " } "
                + "</script>");
        return sb.toString();
    }

    /**
     * Returns the needed server data for client-side usage.<p>
     *
     * @return the needed server data for client-side usage
     */
    public CmsSitemapData getSitemapData() {

        if (m_sitemapData == null) {
            try {
                m_sitemapData = CmsVfsSitemapService.prefetch(
                    getRequest(),
                    getCmsObject().getRequestContext().getUri());
            } catch (CmsRpcException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_sitemapData;
    }

    /**
     * Returns the editor title.<p>
     *
     * @return the editor title
     */
    public String getTitle() {

        CmsSitemapData data = getSitemapData();
        String folderTitle = "";
        if (data != null) {
            folderTitle = getSitemapData().getOpenPath();
            CmsClientSitemapEntry root = getSitemapData().getRoot();
            if (root != null) {
                CmsClientProperty titleProp = root.getOwnProperties().get(CmsPropertyDefinition.PROPERTY_TITLE);
                if ((titleProp != null) && !titleProp.isEmpty()) {
                    folderTitle = root.getOwnProperties().get(CmsPropertyDefinition.PROPERTY_TITLE).getEffectiveValue();
                } else {
                    folderTitle = root.getName();
                }
            }
        }
        return Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_EDITOR_TITLE_1, folderTitle);
    }
}
