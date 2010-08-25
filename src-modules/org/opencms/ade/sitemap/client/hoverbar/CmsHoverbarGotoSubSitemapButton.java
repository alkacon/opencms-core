/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsHoverbarGotoSubSitemapButton.java,v $
 * Date   : $Date: 2010/08/25 14:40:14 $
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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.xml.sitemap.CmsSitemapManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

/**
 * A button for navigating to a sub-sitemap.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsHoverbarGotoSubSitemapButton extends CmsPushButton {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsHoverbarGotoSubSitemapButton(final CmsSitemapHoverbar hoverbar) {

        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarGotoSub());
        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_GOTO_SUB_0));
        setShowBorder(false);
        setVisible(false);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hoverbar.deattach();

                CmsSitemapController controller = hoverbar.getController();
                String sitePath = hoverbar.getSitePath();
                CmsClientSitemapEntry entry = controller.getEntry(sitePath);
                String sitemapLocation = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
                Window.Location.assign(CmsCoreProvider.get().link(sitemapLocation));
            }
        });
        hoverbar.addAttachHandler(new I_CmsHoverbarAttachHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsHoverbarAttachHandler#onAttach(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarAttachEvent)
             */
            public void onAttach(CmsHoverbarAttachEvent event) {

                CmsSitemapController controller = hoverbar.getController();
                String sitePath = hoverbar.getSitePath();
                CmsClientSitemapEntry entry = controller.getEntry(sitePath);
                String sitemapLocation = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
                setVisible((sitemapLocation != null) && !controller.isRoot(sitePath));
            }
        });
    }

}
