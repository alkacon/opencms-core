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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;

/**
 * The skeleton for the sitemap entry editor handlers.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsSitemapEntryEditorHandler implements I_CmsPropertyEditorHandler {

    /** The sitemap controller for this mode handler. */
    protected CmsSitemapController m_controller;

    /** The sitemap entry for this mode handler. */
    protected CmsClientSitemapEntry m_entry;

    /**
     * Creates a new instance of this class.<p>
     *
     * @param controller the sitemap controller for this mode
     * @param entry the sitemap entry for this mode
     */
    public A_CmsSitemapEntryEditorHandler(CmsSitemapController controller, CmsClientSitemapEntry entry) {

        m_controller = controller;
        m_entry = entry;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getName()
     */
    public String getName() {

        return m_entry.getName();
    }

}
