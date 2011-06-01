/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsNewMenuEntry.java,v $
 * Date   : $Date: 2011/06/01 13:06:32 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;

/**
 * Sitemap context menu new entry.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsNewMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsNewMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setImageClass(I_CmsImageBundle.INSTANCE.contextMenuIcons().newElement());
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_NEW_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsSitemapController controller = getHoverbar().getController();
        CmsClientSitemapEntry entry = controller.getEntry(getHoverbar().getSitePath());
        CmsClientSitemapEntry root = controller.getData().getRoot();
        String rootRelativePath = entry.getSitePath().substring(root.getSitePath().length());
        int numSlashes = rootRelativePath.replaceAll("[^/]", "").length();
        if (numSlashes < controller.getData().getMaxDepth() - 1) {
            controller.createSubEntry(entry);
        } else {
            String title = Messages.get().key(Messages.GUI_SITEMAP_TOO_DEEP_TITLE_0);
            String message = Messages.get().key(Messages.GUI_SITEMAP_TOO_DEEP_MESSAGE_0);
            (new CmsAlertDialog(title, message)).center();
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
     */
    @Override
    public void onShow(CmsHoverbarShowEvent event) {

        if (CmsSitemapView.getInstance().isNavigationMode()) {
            String sitePath = getHoverbar().getSitePath();
            CmsSitemapController controller = getHoverbar().getController();
            CmsClientSitemapEntry entry = controller.getEntry(sitePath);
            boolean show = (controller.getData().getDefaultNewElementInfo() != null)
                && (entry != null)
                && entry.isInNavigation()
                && entry.isFolderType()
                && !entry.hasForeignFolderLock();
            setVisible(show);
        } else {
            setVisible(false);
        }
    }
}
