/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryActionElement.java,v $
 * Date   : $Date: 2010/04/12 14:00:39 $
 * Version: $Revision: 1.4 $
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
 * Gallery action used to generate the gallery dialog.<p>
 * 
 * see jsp file <tt>/system/modules/org.opencms.ade.galleries/testVfs.jsp</tt>.<p>
 * 
 * @author Polina Smagina 
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsGalleryActionElement extends CmsJspActionElement {

    /** The current workplace locale. */
    private Locale m_wpLocale;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsGalleryActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        JSONObject coreData = CmsCoreProvider.get().getData(getRequest());
        m_wpLocale = CmsLocaleManager.getLocale(coreData.optString(I_CmsCoreProviderConstants.KEY_WP_LOCALE));
    }

    /**
     * Returns the needed server data for client-side usage.<p> 
     *
     * @return the needed server data for client-side usage
     */
    public String getData() {

        return CmsGalleryProvider.get().export(getRequest());
    }

    /**
     * Returns the editor title.<p>
     * 
     * @return the editor title
     */
    public String getTitle() {

        return Messages.get().getBundle(m_wpLocale).key(Messages.GUI_GALLERIES_TITLE_0);
    }
}
