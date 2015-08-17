/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.alias;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The alias editor.<p>
 */
public class CmsAliasEditor {

    /** The interval used for upating the editing status on the server. */
    public static final int STATUS_UPDATE_INTERVAL = 10000;

    /** Flag to indicate that the alias editor is not being used anymore. */
    boolean m_finished;

    /** The controller. */
    private CmsAliasTableController m_controller;

    /** The view. */
    private CmsAliasView m_view;

    /**
     * Creates a new instance.<p>
     */
    public CmsAliasEditor() {

        m_controller = new CmsAliasTableController();
        m_view = new CmsAliasView(m_controller);
        m_controller.setView(m_view);
    }

    /**
     * Checks whether the alias editor is finished.<p>
     *
     * @return true if the alias editor is finished
     */
    public boolean isFinished() {

        return m_finished;
    }

    /**
     * Opens the alias editor.<p>
     *
     * In addition to displaying the alias editor, this also sets up a timer which regularly informs the server
     * that the alias table for the current site is being edited by the current user. This timer is deactivated
     * when the dialog is closed.<p>
     */
    public void show() {

        final CmsPopup popup = new CmsPopup(CmsAliasMessages.messageTitleAliasEditor());
        popup.setGlassEnabled(true);
        popup.setModal(true);

        final RepeatingCommand updateCommand = new RepeatingCommand() {

            public boolean execute() {

                if (!isFinished() && popup.isVisible() && popup.isAttached()) {
                    updateAliasEditorStatus(true);
                    return true;
                } else {
                    return false;
                }
            }
        };

        popup.setMainContent(m_view);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                setFinished(true);
                updateAliasEditorStatus(false);
            }
        });
        for (CmsPushButton button : m_view.getButtonBar()) {
            popup.addButton(button);
        }
        m_view.setPopup(popup);
        popup.setWidth(946);
        popup.addDialogClose(null);
        m_controller.load(new Runnable() {

            public void run() {

                popup.centerHorizontally(80);
                Scheduler.get().scheduleFixedDelay(updateCommand, STATUS_UPDATE_INTERVAL);
            }
        });

    }

    /**
     * Sets the 'finished' flag.<p>
     *
     * @param finished the new value of the 'finished' flag
     */
    protected void setFinished(boolean finished) {

        m_finished = finished;
    }

    /**
     * Asynchronously updates the alias editor status.<p>
     *
     * @param editing the status we want to set
     */
    protected void updateAliasEditorStatus(boolean editing) {

        CmsSitemapView.getInstance().getController().getService().updateAliasEditorStatus(
            editing,
            new AsyncCallback<Void>() {

                public void onFailure(Throwable caught) {

                    // do nothing
                }

                public void onSuccess(Void result) {

                    // do nothing
                }

            });
    }
}
