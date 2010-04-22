/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapToolbar.java,v $
 * Date   : $Date: 2010/04/22 08:18:40 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarButton;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

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

    /** The add button. */
    private CmsToolbarButton m_addButton;

    /** The clipboard button. */
    private CmsToolbarButton m_clipboardButton;

    /** The new button. */
    private CmsToolbarButton m_newButton;

    /** The publish button. */
    private CmsToolbarButton m_publishButton;

    /** The redo button. */
    private CmsToolbarButton m_redoButton;

    /** The reset button. */
    private CmsToolbarButton m_resetButton;

    /** The save button. */
    private CmsToolbarButton m_saveButton;

    /** The undo button. */
    private CmsToolbarButton m_undoButton;

    /**
     * Constructor.<p>
     */
    public CmsSitemapToolbar() {

        boolean isEditable = CmsSitemapProvider.get().isEditable();

        m_saveButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.SAVE);
        m_saveButton.setEnabled(false);
        addLeft(m_saveButton);

        m_undoButton = new CmsToolbarButton(I_CmsImageBundle.INSTANCE.buttonCss().toolbarUndo(), Messages.get().key(
            Messages.GUI_TOOLBAR_UNDO_0));
        m_undoButton.setEnabled(false);
        addLeft(m_undoButton);

        m_redoButton = new CmsToolbarButton(I_CmsImageBundle.INSTANCE.buttonCss().toolbarRedo(), Messages.get().key(
            Messages.GUI_TOOLBAR_REDO_0));
        m_redoButton.setEnabled(false);
        addLeft(m_redoButton);

        m_resetButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.RESET);
        m_resetButton.setEnabled(false);
        addLeft(m_resetButton);

        m_addButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.ADD);
        m_addButton.setEnabled(isEditable);
        addLeft(m_addButton);

        m_newButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.NEW);
        m_newButton.setEnabled(isEditable);
        addLeft(m_newButton);

        m_clipboardButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.CLIPBOARD);
        m_clipboardButton.setEnabled(isEditable);
        addLeft(m_clipboardButton);

        m_publishButton = new CmsToolbarButton(CmsToolbarButton.ButtonData.PUBLISH);
        addRight(m_publishButton);
    }

    /**
     * Sets the handler.<p>
     * 
     * @param handler the handler
     */
    public void setHandler(final CmsSitemapToolbarHandler handler) {

        ClickHandler clickHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (event.getSource().equals(getSaveButton())) {
                    CmsDomUtil.ensureMouseOut(getSaveButton().getElement());
                    handler.onSave();
                } else if (event.getSource().equals(getAddButton())) {
                    CmsDomUtil.ensureMouseOut(getAddButton().getElement());
                    handler.onAdd();
                } else if (event.getSource().equals(getClipboardButton())) {
                    CmsDomUtil.ensureMouseOut(getClipboardButton().getElement());
                    handler.onClipboard();
                } else if (event.getSource().equals(getPublishButton())) {
                    CmsDomUtil.ensureMouseOut(getPublishButton().getElement());
                    handler.onPublish();
                } else if (event.getSource().equals(getUndoButton())) {
                    CmsDomUtil.ensureMouseOut(getUndoButton().getElement());
                    handler.onUndo();
                } else if (event.getSource().equals(getRedoButton())) {
                    CmsDomUtil.ensureMouseOut(getRedoButton().getElement());
                    handler.onRedo();
                } else if (event.getSource().equals(getResetButton())) {
                    CmsDomUtil.ensureMouseOut(getResetButton().getElement());
                    handler.onReset();
                }
            }
        };

        m_saveButton.addClickHandler(clickHandler);
        m_undoButton.addClickHandler(clickHandler);
        m_redoButton.addClickHandler(clickHandler);
        m_resetButton.addClickHandler(clickHandler);
        m_addButton.addClickHandler(clickHandler);
        m_newButton.addClickHandler(clickHandler);
        m_clipboardButton.addClickHandler(clickHandler);
        m_publishButton.addClickHandler(clickHandler);
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
     * Returns the redo Button.<p>
     *
     * @return the redo Button
     */
    public CmsToolbarButton getRedoButton() {

        return m_redoButton;
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
     * Returns the undo Button.<p>
     *
     * @return the undo Button
     */
    public CmsToolbarButton getUndoButton() {

        return m_undoButton;
    }
}
