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

package org.opencms.ade.contenteditor;

import com.alkacon.acacia.shared.ContentDefinition;

import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.gwt.CmsGwtActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The content editor action element.<p>
 */
public class CmsContentEditorActionElement extends CmsGwtActionElement {

    /** The module name. */
    public static final String MODULE_NAME = "contenteditor";

    /**
     * Constructor.<p>
     * 
     * @param context the page context
     * @param req the servlet request
     * @param res the servlet response
     */
    public CmsContentEditorActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(getRequest()));
        wrapScript(sb);
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(getPrefetch());
        sb.append(super.export());
        sb.append(export());
        sb.append(createNoCacheScript(MODULE_NAME));
        return sb.toString();
    }

    /**
     * Returns the prefetch data include.<p>
     * 
     * @return the prefetch data include
     *  
     * @throws Exception if something goes wrong
     */
    private String getPrefetch() throws Exception {

        ContentDefinition definition = CmsContentService.newInstance(getRequest()).prefetch();
        StringBuffer sb = new StringBuffer();
        String prefetchedData = serializeForJavascript(I_CmsContentService.class.getMethod("prefetch"), definition);
        sb.append(I_CmsContentService.DICT_CONTENT_DEFINITION).append("='").append(prefetchedData).append("';");
        wrapScript(sb);
        return sb.toString();
    }
}
