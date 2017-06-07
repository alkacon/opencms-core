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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Sitemap toolbar publish button.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarPublishButton extends CmsPushButton {

    /** The content editor handler. */
    private I_CmsContentEditorHandler m_editorHandler;

    /**
     * Constructor.<p>
     *
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller
     */
    public CmsToolbarPublishButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        m_editorHandler = toolbar.getToolbarHandler().getEditorHandler();
        setImageClass(I_CmsButton.ButtonData.PUBLISH_BUTTON.getIconClass());
        setTitle(I_CmsButton.ButtonData.PUBLISH_BUTTON.getTitle());
        setButtonStyle(ButtonStyle.FONT_ICON, null);
        setSize(Size.big);

        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                toolbar.onButtonActivation(CmsToolbarPublishButton.this);
                setEnabled(false);
                openPublish();
            }
        });
    }

    /**
     * Opens the publish dialog without changes check.<p>
     */
    protected void openPublish() {

        CmsPublishDialog.showPublishDialog(new HashMap<String, String>(), new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> closeEvent) {

                setEnabled(true);
            }
        }, new Runnable() {

            public void run() {

                openPublish();
            }

        }, m_editorHandler);
    }
}
