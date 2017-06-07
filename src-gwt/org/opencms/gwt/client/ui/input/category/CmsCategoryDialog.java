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

package org.opencms.gwt.client.ui.input.category;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsResourceCategoryInfo;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Dialog to display and change resource categories.<p>
 */
public class CmsCategoryDialog extends CmsPopup {

    /** The category tree widget. */
    CmsCategoryTree m_categoryTree;

    /** The on save command. Called when categories have been changed. */
    Command m_onSave;

    /** The resource structure id. */
    CmsUUID m_structureId;

    /** The cancel button. */
    private CmsPushButton m_cancelButton;

    /** The is initialized flag. */
    private boolean m_initialized;

    /** The is initializing flag. */
    private boolean m_initializing;

    /** The main content panel. */
    private FlowPanel m_main;

    /** The save button. */
    private CmsPushButton m_saveButton;

    /**
     * Constructor.<p>
     *
     * @param structureId the resource structure id
     * @param onSave the on save command, called when categories have been changed
     */
    public CmsCategoryDialog(CmsUUID structureId, Command onSave) {

        super(Messages.get().key(Messages.GUI_DIALOG_CATEGORIES_TITLE_0));
        m_structureId = structureId;
        m_onSave = onSave;
        setGlassEnabled(true);
        catchNotifications();
        addDialogClose(null);
        m_main = new FlowPanel();
        setMainContent(m_main);
        m_cancelButton = new CmsPushButton();
        m_cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        m_cancelButton.setUseMinWidth(true);
        m_cancelButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        m_cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hide();
            }
        });
        addButton(m_cancelButton);
        m_saveButton = new CmsPushButton();
        m_saveButton.setText(Messages.get().key(Messages.GUI_SAVE_0));
        m_saveButton.setUseMinWidth(true);
        m_saveButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_saveButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                saveCategories();
            }
        });
        addButton(m_saveButton);
        m_saveButton.disable(Messages.get().key(Messages.GUI_LOADING_0));
    }

    /**
     * Initializes the dialog content.<p>
     *
     * @param categoryInfo the resource category info
     */
    public void initialize(CmsResourceCategoryInfo categoryInfo) {

        m_initializing = false;
        m_initialized = true;
        m_main.add(new CmsListItemWidget(categoryInfo.getResourceInfo()));
        m_categoryTree = new CmsCategoryTree(
            categoryInfo.getCurrentCategories(),
            300,
            false,
            categoryInfo.getCategoryTree());
        m_categoryTree.addValueChangeHandler(new ValueChangeHandler<List<String>>() {

            public void onValueChange(ValueChangeEvent<List<String>> event) {

                setChanged();
            }
        });
        m_main.add(m_categoryTree);
        m_categoryTree.truncate("CATEGORIES", DEFAULT_WIDTH - 20);
        LockIcon lock = categoryInfo.getResourceInfo().getLockIcon();
        if ((lock == null)
            || lock.equals(LockIcon.NONE)
            || lock.equals(LockIcon.OPEN)
            || lock.equals(LockIcon.SHARED_OPEN)) {
            m_saveButton.disable(Messages.get().key(Messages.GUI_NOTHING_CHANGED_0));
        } else {
            m_categoryTree.disable(Messages.get().key(Messages.GUI_RESOURCE_LOCKED_0));
            m_saveButton.disable(Messages.get().key(Messages.GUI_RESOURCE_LOCKED_0));
        }
        setWidth(DEFAULT_WIDTH);
        if (isShowing()) {
            center();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
        if (!m_initialized && !m_initializing) {
            m_initializing = true;
            CmsRpcAction<CmsResourceCategoryInfo> action = new CmsRpcAction<CmsResourceCategoryInfo>() {

                @Override
                public void execute() {

                    setHeight(450);
                    start(0, true);
                    CmsCoreProvider.getService().getCategoryInfo(m_structureId, this);
                }

                @Override
                protected void onResponse(CmsResourceCategoryInfo result) {

                    stop(false);
                    setHeight(-1);
                    initialize(result);
                }
            };
            action.execute();
        }
    }

    /**
     * Saves the selected categories and hides the dialog.<p>
     */
    protected void saveCategories() {

        final List<String> categories = m_categoryTree.getAllSelected();
        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getService().setResourceCategories(m_structureId, categories, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                hide();
                if (m_onSave != null) {
                    m_onSave.execute();
                }
            }
        };
        action.execute();
    }

    /**
     * Sets the dialog state to changed, enabling the save button.<p>
     */
    void setChanged() {

        m_saveButton.enable();
    }
}
