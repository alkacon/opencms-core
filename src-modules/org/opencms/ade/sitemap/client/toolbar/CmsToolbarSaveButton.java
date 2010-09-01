/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarSaveButton.java,v $
 * Date   : $Date: 2010/09/01 10:15:19 $
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapLastUndoEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapResetEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapStartEditEvent;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapLastUndoHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapResetHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapStartEditHandler;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap toolbar save button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public class CmsToolbarSaveButton extends CmsToggleButton {

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarSaveButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        setImageClass(I_CmsButton.ButtonData.SAVE.getIconClass());
        setTitle(I_CmsButton.ButtonData.SAVE.getTitle());
        disable(Messages.get().key(Messages.GUI_DISABLED_SAVE_0));
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                toolbar.onButtonActivation(CmsToolbarSaveButton.this);
                setDown(false);
                controller.commit(false);
            }
        });
        controller.addLastUndoHandler(new I_CmsSitemapLastUndoHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapLastUndoHandler#onLastUndo(org.opencms.ade.sitemap.client.control.CmsSitemapLastUndoEvent)
             */
            public void onLastUndo(CmsSitemapLastUndoEvent event) {

                disable(Messages.get().key(Messages.GUI_DISABLED_SAVE_0));
            }
        });
        controller.addResetHandler(new I_CmsSitemapResetHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapResetHandler#onReset(org.opencms.ade.sitemap.client.control.CmsSitemapResetEvent)
             */
            public void onReset(CmsSitemapResetEvent event) {

                disable(Messages.get().key(Messages.GUI_DISABLED_SAVE_0));
            }
        });
        controller.addStartEditHandler(new I_CmsSitemapStartEditHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapStartEditHandler#onStartEdit(org.opencms.ade.sitemap.client.control.CmsSitemapStartEditEvent)
             */
            public void onStartEdit(CmsSitemapStartEditEvent event) {

                enable();
            }
        });
    }
}
