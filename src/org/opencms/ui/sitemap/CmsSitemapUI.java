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

package org.opencms.ui.sitemap;

import org.opencms.ui.A_CmsUI;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;

/**
 * UI class for Vaadin dialogs in the sitemap editor.<p>
 */
public class CmsSitemapUI extends A_CmsUI {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The content. */
    private CssLayout m_content = new CssLayout();

    /** The sitemap extension installed on this UI instance. */
    private CmsSitemapExtension m_sitemapExtension;

    /**
     * @see com.vaadin.ui.AbstractSingleComponentContainer#getContent()
     */
    @Override
    public CssLayout getContent() {

        return (CssLayout)super.getContent();
    }

    /**
     * @see org.opencms.ui.A_CmsUI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    protected void init(VaadinRequest request) {

        super.init(request);
        m_sitemapExtension = new CmsSitemapExtension(this);
        setContent(m_content);
    }

    /**
     * Gets the sitemap extension instance.<p>
     *
     * @return the sitemap extension instance
     */
    CmsSitemapExtension getSitemapExtension() {

        return m_sitemapExtension;
    }

}
