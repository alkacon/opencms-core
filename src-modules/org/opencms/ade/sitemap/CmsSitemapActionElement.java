/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsSitemapActionElement.java,v $
 * Date   : $Date: 2010/03/15 15:12:54 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.sitemap;

import org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants;
import org.opencms.gwt.CmsCoreProvider;
import org.opencms.gwt.shared.I_CmsCoreProviderConstants;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Sitemap action used to generate the sitemap editor.<p>
 * 
 * see jsp file <tt>/system/modules/org.opencms.ade.sitemap/sitemap.jsp</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapActionElement extends CmsJspActionElement {

    /** The current sitemap URI. */
    private String m_uri;

    /** The current workplace locale. */
    private Locale m_wpLocale;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsSitemapActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        JSONObject coreData = CmsCoreProvider.get().getData(getCmsObject());
        m_wpLocale = CmsLocaleManager.getLocale(coreData.optString(I_CmsCoreProviderConstants.KEY_WP_LOCALE));
        JSONObject sitemapData = CmsSitemapProvider.get().getData(getCmsObject(), getRequest());
        m_uri = sitemapData.optString(I_CmsSitemapProviderConstants.KEY_URI_SITEMAP);
    }

    /**
     * Returns the needed server data for client-side usage.<p> 
     *
     * @return the needed server data for client-side usage
     */
    public String getData() {

        return CmsSitemapProvider.get().export(ClientMessages.get(), getRequest());
    }

    /**
     * Returns the editor title.<p>
     * 
     * @return the editor title
     */
    public String getTitle() {

        return Messages.get().getBundle(m_wpLocale).key(Messages.GUI_EDITOR_TITLE_1, m_uri);
    }
}
