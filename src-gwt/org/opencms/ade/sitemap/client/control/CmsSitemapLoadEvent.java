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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Sitemap load event.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.ade.sitemap.client.control.CmsSitemapController
 */
public class CmsSitemapLoadEvent extends GwtEvent<I_CmsSitemapLoadHandler> {

    /** Event type for sitemap change events. */
    private static final Type<I_CmsSitemapLoadHandler> TYPE = new Type<I_CmsSitemapLoadHandler>();

    /** The loaded sitemap entry. */
    private CmsClientSitemapEntry m_entry;

    /** Flag to indicate if the entry should be opened. */
    private boolean m_setOpen;

    /**
     * Constructor.<p>
     *
     * @param entry the entry that loaded its children
     * @param setOpen if the entry should be opened
     */
    public CmsSitemapLoadEvent(CmsClientSitemapEntry entry, boolean setOpen) {

        m_entry = entry;
        m_setOpen = setOpen;
    }

    /**
     * Gets the event type associated with change events.<p>
     *
     * @return the handler type
     */
    public static Type<I_CmsSitemapLoadHandler> getType() {

        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public final Type<I_CmsSitemapLoadHandler> getAssociatedType() {

        return TYPE;
    }

    /**
     * Returns the entry.<p>
     *
     * @return the entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * Returns if the entry should be opened.<p>
     *
     * @return <code>true</code> if the entry should be opened
     */
    public boolean isSetOpen() {

        return m_setOpen;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsSitemapLoadHandler handler) {

        handler.onLoad(this);
    }
}
