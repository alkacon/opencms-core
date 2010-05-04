/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapToolbar.java,v $
 * Date   : $Date: 2010/05/04 06:54:27 $
 * Version: $Revision: 1.10 $
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
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap toolbar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapToolbar extends CmsToolbar {

    /** The add button. */
    private CmsToggleButton m_addButton;

    /** The clipboard button. */
    private CmsToggleButton m_clipboardButton;

    /** The publish button. */
    private CmsToggleButton m_publishButton;

    /** The redo button. */
    private CmsToggleButton m_redoButton;

    /** The reset button. */
    private CmsToggleButton m_resetButton;

    /** The save button. */
    private CmsToggleButton m_saveButton;

    /** The undo button. */
    private CmsToggleButton m_undoButton;

    /**
     * Constructor.<p>
     * 
     * @param controller the sitemap controller 
     */
    public CmsSitemapToolbar(CmsSitemapController controller) {

        m_saveButton = new CmsToggleButton(I_CmsButton.ButtonData.SAVE);
        m_saveButton.disable(Messages.get().key(Messages.GUI_DISABLED_SAVE_0));
        addLeft(m_saveButton);

        m_undoButton = new CmsToggleButton();
        m_undoButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().toolbarUndo());
        m_undoButton.setTitle(Messages.get().key(Messages.GUI_TOOLBAR_UNDO_0));
        m_undoButton.disable(Messages.get().key(Messages.GUI_DISABLED_UNDO_0));
        addLeft(m_undoButton);

        m_redoButton = new CmsToggleButton();
        m_redoButton.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().toolbarRedo());
        m_redoButton.setTitle(Messages.get().key(Messages.GUI_TOOLBAR_REDO_0));
        m_redoButton.disable(Messages.get().key(Messages.GUI_DISABLED_REDO_0));
        addLeft(m_redoButton);

        m_resetButton = new CmsToggleButton(I_CmsButton.ButtonData.RESET);
        m_resetButton.disable(Messages.get().key(Messages.GUI_DISABLED_RESET_0));
        addLeft(m_resetButton);

        m_addButton = new CmsToggleButton(I_CmsButton.ButtonData.ADD);
        if (!controller.isEditable()) {
            m_addButton.disable(controller.getData().getNoEditReason());
        }
        addLeft(m_addButton);

        m_clipboardButton = new CmsToggleButton(I_CmsButton.ButtonData.CLIPBOARD);
        if (!controller.isEditable()) {
            m_clipboardButton.disable(controller.getData().getNoEditReason());
        }
        addLeft(m_clipboardButton);

        m_publishButton = new CmsToggleButton(I_CmsButton.ButtonData.PUBLISH);
        addRight(m_publishButton);

        if (!controller.isEditable()) {
            CmsNotification.get().sendSticky(
                CmsNotification.Type.WARNING,
                Messages.get().key(Messages.GUI_NO_EDIT_NOTIFICATION_1, controller.getData().getNoEditReason()));
        }
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
                    getSaveButton().setDown(false);
                    handler.onSave();
                } else if (event.getSource().equals(getAddButton())) {
                    getAddButton().setDown(false);
                    handler.onAdd();
                } else if (event.getSource().equals(getClipboardButton())) {
                    getClipboardButton().setDown(false);
                    handler.onClipboard();
                } else if (event.getSource().equals(getPublishButton())) {
                    CmsDomUtil.ensureMouseOut(getPublishButton().getElement());
                    getPublishButton().setDown(false);
                    handler.onPublish();
                } else if (event.getSource().equals(getUndoButton())) {
                    getUndoButton().setDown(false);
                    handler.onUndo();
                } else if (event.getSource().equals(getRedoButton())) {
                    getRedoButton().setDown(false);
                    handler.onRedo();
                } else if (event.getSource().equals(getResetButton())) {
                    CmsDomUtil.ensureMouseOut(getResetButton().getElement());
                    getResetButton().setDown(false);
                    handler.onReset();
                }
            }
        };

        m_saveButton.addClickHandler(clickHandler);
        m_undoButton.addClickHandler(clickHandler);
        m_redoButton.addClickHandler(clickHandler);
        m_resetButton.addClickHandler(clickHandler);
        m_addButton.addClickHandler(clickHandler);
        m_clipboardButton.addClickHandler(clickHandler);
        m_publishButton.addClickHandler(clickHandler);
    }

    /**
     * Returns the add Button.<p>
     *
     * @return the add Button
     */
    public CmsToggleButton getAddButton() {

        return m_addButton;
    }

    /**
     * Returns the clipboard Button.<p>
     *
     * @return the clipboard Button
     */
    public CmsToggleButton getClipboardButton() {

        return m_clipboardButton;
    }

    /**
     * Returns the publish Button.<p>
     *
     * @return the publish Button
     */
    public CmsToggleButton getPublishButton() {

        return m_publishButton;
    }

    /**
     * Returns the redo Button.<p>
     *
     * @return the redo Button
     */
    public CmsToggleButton getRedoButton() {

        return m_redoButton;
    }

    /**
     * Returns the reset Button.<p>
     *
     * @return the reset Button
     */
    public CmsToggleButton getResetButton() {

        return m_resetButton;
    }

    /**
     * Returns the save Button.<p>
     *
     * @return the save Button
     */
    public CmsToggleButton getSaveButton() {

        return m_saveButton;
    }

    /**
     * Returns the undo Button.<p>
     *
     * @return the undo Button
     */
    public CmsToggleButton getUndoButton() {

        return m_undoButton;
    }
}
