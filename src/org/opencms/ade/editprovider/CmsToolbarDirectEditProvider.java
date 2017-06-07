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

package org.opencms.ade.editprovider;

import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * A Direct Edit provider class which also offers some limited ADE functionality,
 * like for example the Publish dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarDirectEditProvider extends CmsAdvancedDirectEditProvider {

    /** The module name. */
    public static final String MODULE_NAME = "editprovider";

    /**
     * Creates a new instance of this direct edit provider.<p>
     */
    public CmsToolbarDirectEditProvider() {

        // ensure that the generated data elements get an id
        m_useIds = true;
    }

    /**
     * Returns the direct edit include HTML to insert in the page beginning.<p>
     *
     * @param context the page context
     * @param params the parameters for the direct edit includes
     *
     * @return the direct edit include HTML to insert in the page beginning
     *
     * @throws Exception if something goes wrong
     */
    public String getIncludes(PageContext context, CmsDirectEditParams params) throws Exception {

        return new CmsEditProviderActionElement(
            context,
            (HttpServletRequest)context.getRequest(),
            (HttpServletResponse)context.getResponse()).exportAll();
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditIncludes(javax.servlet.jsp.PageContext, org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    @Override
    public void insertDirectEditIncludes(PageContext context, CmsDirectEditParams params) throws JspException {

        JspException error = null;
        String includeData = "";

        try {
            includeData = getIncludes(context, params);
        } catch (JspException e) {
            error = e;
        } catch (Exception e) {
            error = new JspException(e);
        }
        if (error != null) {
            throw error;
        }
        print(context, includeData);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#newInstance()
     */
    @Override
    public I_CmsDirectEditProvider newInstance() {

        CmsToolbarDirectEditProvider result = new CmsToolbarDirectEditProvider();
        result.m_configurationParameters = m_configurationParameters;
        return result;
    }

}
