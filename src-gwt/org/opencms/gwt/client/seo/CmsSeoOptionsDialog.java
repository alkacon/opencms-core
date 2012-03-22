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

package org.opencms.gwt.client.seo;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.shared.CmsAliasBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * This is a dialog widget which wraps a {@link CmsAliasList}.
 */
public class CmsSeoOptionsDialog extends CmsPopup {

    /** The alias messages. */
    protected static CmsAliasMessages aliasMessages = new CmsAliasMessages();

    /** The inner alias list. */
    protected CmsAliasList m_aliasList;

    /** The root panel for this dialog. */
    protected FlowPanel m_panel;

    /** The structure id of the resource whose aliases are being edited. */
    protected CmsUUID m_structureId;

    /**
     * Creates a new dialog instance.<p>
     * 
     * @param structureId the structure id of the resource whose aliases are being edited
     * @param infoBean a bean containing the information to display in the resource info box 
     * @param aliases the existing aliases of the resource 
     */
    public CmsSeoOptionsDialog(CmsUUID structureId, CmsListInfoBean infoBean, List<CmsAliasBean> aliases) {

        super(aliasMessages.seoOptions()); //$NON-NLS-1$
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(true);
        setWidth(590);
        m_structureId = structureId;
        m_panel = new FlowPanel();
        CmsFieldSet aliasFieldset = new CmsFieldSet();
        aliasFieldset.setLegend(aliasMessages.aliases()); //$NON-NLS-1$
        m_aliasList = new CmsAliasList(structureId, aliases);
        CmsListItemWidget liWidget = new CmsListItemWidget(infoBean);
        liWidget.setStateIcon(StateIcon.standard);
        m_panel.add(liWidget);
        aliasFieldset.getElement().getStyle().setMarginTop(10, Unit.PX);
        aliasFieldset.addContent(m_aliasList);
        m_panel.add(aliasFieldset);
        Style style = m_aliasList.getElement().getStyle();
        style.setProperty("minHeight", "300px"); //$NON-NLS-1$ //$NON-NLS-2$
        style.setProperty("maxHeight", "450px"); //$NON-NLS-1$ //$NON-NLS-2$

        style.setOverflowY(Overflow.AUTO);
        setMainContent(m_panel);
        addButton(createCancelButton());
        addButton(saveButton());
    }

    /**
     * Loads the aliases for a given page.<p>
     * 
     * @param structureId the structure id of the page 
     * @param callback the callback for the loaded aliases 
     */
    public static void loadAliases(final CmsUUID structureId, final AsyncCallback<List<CmsAliasBean>> callback) {

        final CmsRpcAction<List<CmsAliasBean>> action = new CmsRpcAction<List<CmsAliasBean>>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().getAliasesForPage(structureId, this);
            }

            @Override
            protected void onResponse(List<CmsAliasBean> result) {

                stop(false);
                callback.onSuccess(result);
            }
        };
        action.execute();
    }

    /**
     * Saves the aliases for a given page.<p>
     *  
     * @param uuid the page structure id 
     * @param aliases the aliases to save
     */
    public void saveAliases(final CmsUUID uuid, final List<CmsAliasBean> aliases) {

        final CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().saveAliases(uuid, aliases, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            public void onResponse(Void result) {

                stop(false);
            }

        };
        action.execute();
    }

    /**
     * The method which is called when the user clicks the save button of the dialog.<p>
     */
    protected void onClickSave() {

        Timer timer = new Timer() {

            @Override
            public void run() {

                m_aliasList.clearValidationErrors();
                m_aliasList.validate(new Runnable() {

                    public void run() {

                        if (!m_aliasList.hasValidationErrors()) {
                            List<CmsAliasBean> aliases = m_aliasList.getAliases();
                            saveAliases(m_structureId, aliases);
                            hide();
                        }
                    }
                });
            }
        };
        // slight delay so that the validation doesn't interfere with validations triggered by the change event 
        timer.schedule(20);
    }

    /**
     * Creates the cancel button.<p>
     * 
     * @return the cancel button
     */
    private CmsPushButton createCancelButton() {

        addDialogClose(null);
        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsSeoOptionsDialog.this.hide();
            }
        });

        return button;
    }

    /**
     * Creates the OK button.<p>
     * 
     * @return the OK button
     */
    private CmsPushButton saveButton() {

        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_SAVE_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsSeoOptionsDialog.this.onClickSave();
            }
        });
        return button;
    }

}
