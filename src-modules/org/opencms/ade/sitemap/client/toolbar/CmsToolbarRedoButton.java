/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarRedoButton.java,v $
 * Date   : $Date: 2010/06/24 09:05:26 $
 * Version: $Revision: 1.2 $
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
import org.opencms.ade.sitemap.client.control.CmsSitemapClearUndoEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapFirstUndoEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapLastRedoEvent;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapClearUndoHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapFirstUndoHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapLastRedoHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.CmsToggleButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap toolbar redo button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsToolbarRedoButton extends CmsToggleButton {

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarRedoButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().toolbarRedo());
        setTitle(Messages.get().key(Messages.GUI_TOOLBAR_REDO_0));
        disable(Messages.get().key(Messages.GUI_DISABLED_REDO_0));
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                toolbar.onButtonActivation(CmsToolbarRedoButton.this);
                setDown(false);
                controller.redo();
            }
        });
        controller.addClearUndoHandler(new I_CmsSitemapClearUndoHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapClearUndoHandler#onClearUndo(org.opencms.ade.sitemap.client.control.CmsSitemapClearUndoEvent)
             */
            public void onClearUndo(CmsSitemapClearUndoEvent event) {

                disable(Messages.get().key(Messages.GUI_DISABLED_REDO_0));
            }
        });
        controller.addFirstUndoHandler(new I_CmsSitemapFirstUndoHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapFirstUndoHandler#onFirstUndo(org.opencms.ade.sitemap.client.control.CmsSitemapFirstUndoEvent)
             */
            public void onFirstUndo(CmsSitemapFirstUndoEvent event) {

                enable();
            }
        });
        controller.addLastRedoHandler(new I_CmsSitemapLastRedoHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapLastRedoHandler#onLastRedo(org.opencms.ade.sitemap.client.control.CmsSitemapLastRedoEvent)
             */
            public void onLastRedo(CmsSitemapLastRedoEvent event) {

                disable(Messages.get().key(Messages.GUI_DISABLED_REDO_0));
            }
        });
    }
}
