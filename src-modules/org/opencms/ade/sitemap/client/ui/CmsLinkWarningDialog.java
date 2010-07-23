/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsLinkWarningDialog.java,v $
 * Date   : $Date: 2010/07/23 11:38:25 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopupDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * A dialog which informs the user that deleting a sitemap item will break links
 * from other resources.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsLinkWarningDialog extends CmsPopupDialog {

    /** The content of the dialog. */
    protected CmsLinkWarningPanel m_content;

    /** The handler which will be executed when the user clicks OK. */
    protected I_CmsConfirmDialogHandler m_handler;

    /**
     * Constructor.<p>
     * 
     * @param handler the handler which should be executed when the user presses the OK button
     * @param brokenLinkBeans the beans representing the links which would be broken 
     */
    public CmsLinkWarningDialog(I_CmsConfirmDialogHandler handler, List<CmsSitemapBrokenLinkBean> brokenLinkBeans) {

        super(org.opencms.ade.sitemap.client.Messages.get().key(
            org.opencms.ade.sitemap.client.Messages.GUI_LINK_WARNING_TITLE_0), new CmsLinkWarningPanel());
        m_handler = handler;
        m_content = (CmsLinkWarningPanel)getContent();
        m_content.fill(brokenLinkBeans);
        setModal(true);
        CmsPushButton cancelButton = addButton(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                m_handler.onClose();
                hide();
            }
        });
        CmsPushButton okButton = addButton(Messages.get().key(Messages.GUI_OK_0));
        okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hide();
                m_handler.onOk();
            }
        });

    }

    /**
     * Returns the actual panel containing the link warnings.<p>
     * 
     * @return the panel containing the link warnings 
     */
    public CmsLinkWarningPanel getLinkWarningPanel() {

        return m_content;
    }

    /**
     * Adds a button with the given text to the dialog and returns it.<p>
     * 
     * @param text the text for the new button
     * 
     * @return the button which was added 
     */
    protected CmsPushButton addButton(String text) {

        CmsPushButton button = new CmsPushButton();
        button.setUseMinWidth(true);
        button.setText(text);
        addButton(button);
        return button;
    }
}
