/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * @since 8.0.0
 */
public class CmsPublishActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.publish";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = "publish";

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsPublishActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(getRequest()));
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();

        CmsPublishData initData = CmsPublishService.prefetch(getRequest());
        String prefetchedData = exportDictionary(
            CmsPublishData.DICT_NAME,
            I_CmsPublishService.class.getMethod("getInitData", java.util.HashMap.class),
            initData);
        sb.append(prefetchedData);
        sb.append(super.export());
        sb.append(export());
        sb.append(createNoCacheScript(
            GWT_MODULE_NAME,
            OpenCms.getModuleManager().getModule(CMS_MODULE_NAME).getVersion().toString()));
        return sb.toString();
    }

    /**
     * Returns the dialog title.<p>
     * 
     * @return the dialog title
     */
    public String getTitle() {

        return "Publish";
    }

}
