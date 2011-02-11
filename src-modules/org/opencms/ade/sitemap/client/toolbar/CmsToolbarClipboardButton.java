/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarClipboardButton.java,v $
 * Date   : $Date: 2011/02/11 14:35:17 $
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.ui.I_CmsButton;

/**
 * Sitemap toolbar clipboard button.<p>
 * 
 * @author Michael Moossen
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardButton extends A_CmsToolbarListMenuButton {

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarClipboardButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        super(
            I_CmsButton.ButtonData.CLIPBOARD.getTitle(),
            I_CmsButton.ButtonData.CLIPBOARD.getIconClass(),
            toolbar,
            controller);
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.A_CmsToolbarListMenuButton#initContent()
     */
    @Override
    protected void initContent() {

        CmsToolbarClipboardView view = new CmsToolbarClipboardView(this, getController());
        createTab(
            Messages.get().key(Messages.GUI_CLIPBOARD_MODIFIED_TITLE_0),
            Messages.get().key(Messages.GUI_CLIPBOARD_MODIFIED_DESC_0),
            view.getModified());
        // TODO: handle deletions
        //        createTab(
        //            Messages.get().key(Messages.GUI_CLIPBOARD_DELETED_TITLE_0),
        //            Messages.get().key(Messages.GUI_CLIPBOARD_DELETED_DESC_0),
        //            view.getDeleted());
    }
}
