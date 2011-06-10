/*
 * File   : $Source: /alkacon/cvs/opencms/src-gwt/org/opencms/ade/sitemap/client/model/I_CmsClientSitemapChange.java,v $
 * Date   : $Date: 2011/06/10 06:57:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;

/**
 * Stores one change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsClientSitemapChange {

    /**
     * Applies the current change to the clipboard view.<p>
     * 
     * @param view the clipboard view
     */
    void applyToClipboardView(CmsToolbarClipboardView view);

    /**
     * Applies the current change to the model.<p>
     * 
     * @param controller the controller
     */
    void applyToModel(CmsSitemapController controller);

    /**
     * Applies the current change to the view.<p>
     * 
     * @param view the view
     */
    void applyToView(CmsSitemapView view);

    /**
     * Returns this change for commit.<p>
     * 
     * @return this change for commit
     */
    CmsSitemapChange getChangeForCommit();

    /**
     * Updates the sitemap entry with in the change data.<p>
     * 
     * @param entry the updated entry
     */
    void updateEntry(CmsClientSitemapEntry entry);
}