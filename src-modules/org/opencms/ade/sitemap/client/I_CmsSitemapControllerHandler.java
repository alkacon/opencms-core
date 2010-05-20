/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/I_CmsSitemapControllerHandler.java,v $
 * Date   : $Date: 2010/05/20 09:17:29 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

/**
 * Sitemap controller handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapController
 */
public interface I_CmsSitemapControllerHandler {

    /**
     * Will be triggered when something has changed.<p>
     * 
     * @param change the specific change
     */
    void onChange(I_CmsClientSitemapChange change);

    /**
     * Will be triggered when the undo list is cleared.<p> 
     */
    void onClearUndo();

    /**
     * Will be triggered when starting to undo.<p>
     */
    void onFirstUndo();

    /**
     * Will be triggered when an entry gets its children.<p>
     * 
     * @param entry the entry that got its children
     * @param originalParent the original path, in case of moved or renamed
     */
    void onGetChildren(CmsClientSitemapEntry entry, String originalParent);

    /**
     * Will be triggered when redoing the last possible action.<p>
     */
    void onLastRedo();

    /**
     * Will be triggered when undoing the last possible action.<p>
     */
    void onLastUndo();

    /**
     * Will be triggered on reset.<p>
     */
    void onReset();

    /**
     * Will be triggered when the sitemap is changed in anyway for the first time.<p> 
     */
    void onStartEdit();
}
