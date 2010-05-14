/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryActionElement.java,v $
 * Date   : $Date: 2010/05/14 13:36:29 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.ReqParam;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.gwt.CmsGwtActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Gallery action used to generate the gallery dialog.<p>
 * 
 * see jsp file <tt>/system/modules/org.opencms.ade.galleries/testVfs.jsp</tt>.<p>
 * 
 * @author Polina Smagina 
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsGalleryActionElement extends CmsGwtActionElement {

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsGalleryActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        GalleryMode mode = null;
        try {
            mode = GalleryMode.valueOf(getRequest().getParameter(ReqParam.dialogmode.name()).trim());
        } catch (Exception e) {
            mode = GalleryMode.view;
        }
        return export(mode);
    }

    /**
     * Returns the serialized initial data for gallery dialog depending on the given mode.<p>
     * 
     * @param galleryMode the gallery mode
     * 
     * @return the data
     * 
     * @throws Exception if something goes wrong
     */
    private String export(GalleryMode galleryMode) throws Exception {

        CmsGalleryService galleryService = CmsGalleryService.newInstance(getRequest());
        CmsGalleryDataBean data = galleryService.getInitialSettings(galleryMode);
        CmsGallerySearchBean search = null;
        if (GalleryTabId.cms_tab_results.equals(data.getStartTab())) {
            search = galleryService.getSearch(data);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(getRequest()));
        sb.append(CmsGalleryDataBean.DICT_NAME).append("='");
        sb.append(serialize(I_CmsGalleryService.class.getMethod("getInitialSettings", GalleryMode.class), data));
        sb.append("';");
        sb.append(CmsGallerySearchBean.DICT_NAME).append("='").append(
            serialize(I_CmsGalleryService.class.getMethod("getSearch", CmsGalleryDataBean.class), search));
        sb.append("';");
        return sb.toString();
    }

    /**
     * Returns the serialized initial data for gallery dialog within the sitmap editor.<p>
     * 
     * @return the data
     * 
     * @throws Exception if something goes wrong
     */
    public String exportForSitemap() throws Exception {

        return export(GalleryMode.sitemap);
    }

    /**
     * Returns the serialized initial data for gallery dialog within the container-page editor.<p>
     * 
     * @return the data
     * 
     * @throws Exception if something goes wrong
     */
    public String exportForContainerpage() throws Exception {

        return export(GalleryMode.ade);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export());
        sb.append(export());
        return sb.toString();
    }

    /**
     * Returns the editor title.<p>
     * 
     * @return the editor title
     */
    public String getTitle() {

        return Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_GALLERIES_TITLE_0);
    }

}
