/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsSitemapToolbar.java,v $
 * Date   : $Date: 2010/06/24 09:05:26 $
 * Version: $Revision: 1.3 $
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

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.ui.CmsToolbar;

import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap toolbar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapToolbar extends CmsToolbar {

    /**
     * Constructor.<p>
     * 
     * @param controller the sitemap controller 
     */
    public CmsSitemapToolbar(CmsSitemapController controller) {

        addLeft(new CmsToolbarSaveButton(this, controller));
        addLeft(new CmsToolbarUndoButton(this, controller));
        addLeft(new CmsToolbarRedoButton(this, controller));
        addLeft(new CmsToolbarResetButton(this, controller));
        addLeft(new CmsToolbarAddButton(this, controller));
        addLeft(new CmsToolbarClipboardButton(this, controller));

        addRight(new CmsToolbarPublishButton(this, controller));
    }

    /**
     * Should be executed by every widget when starting an action.<p>
     * 
     * @param widget the widget that got activated
     */
    public void onButtonActivation(Widget widget) {

        for (Widget w : getAll()) {
            if (!(w instanceof I_CmsToolbarActivable)) {
                continue;
            }
            ((I_CmsToolbarActivable)w).onActivation(widget);
        }
    }
}
