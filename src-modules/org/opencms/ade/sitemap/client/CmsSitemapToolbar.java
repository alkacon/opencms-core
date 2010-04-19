/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapToolbar.java,v $
 * Date   : $Date: 2010/04/19 11:48:12 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarButton;

/**
 * Sitemap toolbar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapToolbar extends CmsToolbar {

    /** The add button. */
    private CmsToolbarButton m_addButton;

    /** The clipboard button. */
    private CmsToolbarButton m_clipboardButton;

    /** The new button. */
    private CmsToolbarButton m_newButton;

    /** The publish button. */
    private CmsToolbarButton m_publishButton;

    /** The reset button. */
    private CmsToolbarButton m_resetButton;

    /** The save button. */
    private CmsToolbarButton m_saveButton;

    /** The subsitemap button. */
    private CmsToolbarButton m_subsitemapButton;

    /**
     * Constructor.<p>
     * 
     * @param handler the handler
     */
    public CmsSitemapToolbar(CmsSitemapToolbarHandler handler) {

        boolean isEditable = CmsSitemapProvider.get().isEditable();
        handler.setToolbar(this);

        m_saveButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.SAVE);
        m_saveButton.setEnabled(false);
        m_saveButton.addClickHandler(handler);
        addLeft(m_saveButton);

        m_subsitemapButton = new CmsToolbarButton(
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSubsitemap(),
            Messages.get().key(Messages.GUI_TOOLBAR_SUBSITEMAP_0));
        m_subsitemapButton.setEnabled(isEditable);
        m_subsitemapButton.addClickHandler(handler);
        addLeft(m_subsitemapButton);

        m_addButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.ADD);
        m_addButton.setEnabled(isEditable);
        m_addButton.addClickHandler(handler);
        addLeft(m_addButton);

        m_newButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.NEW);
        m_newButton.setEnabled(isEditable);
        m_newButton.addClickHandler(handler);
        addLeft(m_newButton);

        m_clipboardButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.CLIPBOARD);
        m_clipboardButton.setEnabled(isEditable);
        m_clipboardButton.addClickHandler(handler);
        addLeft(m_clipboardButton);

        m_publishButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.PUBLISH);
        m_publishButton.addClickHandler(handler);
        addRight(m_publishButton);

        m_resetButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.RESET);
        m_resetButton.setEnabled(false);
        m_resetButton.addClickHandler(handler);
        addRight(m_resetButton);
    }

    /**
     * Returns the add Button.<p>
     *
     * @return the add Button
     */
    public CmsToolbarButton getAddButton() {

        return m_addButton;
    }

    /**
     * Returns the clipboard Button.<p>
     *
     * @return the clipboard Button
     */
    public CmsToolbarButton getClipboardButton() {

        return m_clipboardButton;
    }

    /**
     * Returns the new Button.<p>
     *
     * @return the new Button
     */
    public CmsToolbarButton getNewButton() {

        return m_newButton;
    }

    /**
     * Returns the publish Button.<p>
     *
     * @return the publish Button
     */
    public CmsToolbarButton getPublishButton() {

        return m_publishButton;
    }

    /**
     * Returns the reset Button.<p>
     *
     * @return the reset Button
     */
    public CmsToolbarButton getResetButton() {

        return m_resetButton;
    }

    /**
     * Returns the save Button.<p>
     *
     * @return the save Button
     */
    public CmsToolbarButton getSaveButton() {

        return m_saveButton;
    }

    /**
     * Returns the subsitemap Button.<p>
     *
     * @return the subsitemap Button
     */
    public CmsToolbarButton getSubsitemapButton() {

        return m_subsitemapButton;
    }
}
