/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/A_CmsTab.java,v $
 * Date   : $Date: 2010/05/25 12:36:33 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;

import com.google.gwt.user.client.ui.Composite;

/**
 * A tab for the gallery dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsTab extends Composite {

    private GalleryTabId m_tabId;

    /**
     * Constructor.<p>
     * 
     * @param tabId the tab id
     */
    protected A_CmsTab(GalleryTabId tabId) {

        m_tabId = tabId;
    }

    /**
     * Returns the tab id.<p>
     * 
     * @return the tab id
     */
    public GalleryTabId getTabId() {

        return m_tabId;
    }

    /**
     * Will be triggered when a tab is selected.<p>
     */
    public void onSelection() {

        getTabHandler().onSelection();
    }

    /**
     * Returns the tab handler.<p>
     *
     * @return the tab handler
     */
    protected abstract A_CmsTabHandler getTabHandler();

}
