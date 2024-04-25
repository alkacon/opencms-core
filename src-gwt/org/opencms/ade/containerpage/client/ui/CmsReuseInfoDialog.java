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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.shared.CmsReuseInfo;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsDialogContextMenuHandler;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsResourceListInfo;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog for showing the usages of a reused element before editing it.
 */
public class CmsReuseInfoDialog extends CmsPopup {

    /**
     * UiBinder interface for this dialog.<p>
     */
    interface I_UiBinder extends UiBinder<Panel, CmsReuseInfoDialog> {
        // empty uibinder interface
    }

    /** UiBinder instance for this dialog. */
    private static I_UiBinder uibinder = GWT.create(I_UiBinder.class);

    /** Container for the label. */
    @UiField
    protected FlowPanel m_labelBox;

    /** The container for the file info box. */
    @UiField
    protected Panel m_infoBoxContainer;

    /** The message label. */
    @UiField
    protected Label m_label;


    /** The list item container for the element usages. */
    @UiField
    protected CmsList<CmsListItem> m_listPanel;

    /** The ok button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** The cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;


    /** The callback for the dialog. */
    private Consumer<Boolean> m_callback;

    /**
     * Creates a new instance.
     *
     * @param reuseInfo the reuse info bean
     * @param callback the callback to call with the dialog result
     */
    public CmsReuseInfoDialog(CmsReuseInfo reuseInfo, Consumer<Boolean> callback) {

        super(reuseInfo.getTitle());
        setModal(true);
        setGlassEnabled(true);
        m_callback = callback;
        CmsListInfoBean elementInfo = reuseInfo.getElementInfo();
        Panel panel = uibinder.createAndBindUi(this);
        Widget warnIcon = FontOpenCms.WARNING.getWidget(20, I_CmsConstantsBundle.INSTANCE.css().colorWarning());
        m_labelBox.insert(warnIcon, 0);
        CmsListItemWidget infoBox = new CmsListItemWidget(elementInfo);
        m_infoBoxContainer.add(infoBox);
        setMainContent(panel);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        for (CmsPushButton button : getDialogButtons()) {
            addButton(button);
        }
        CmsDialogContextMenuHandler menuHandler = new CmsDialogContextMenuHandler();
        m_okButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_cancelButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        m_label.setText(reuseInfo.getMessage());

        for (CmsResourceListInfo bean : reuseInfo.getUsageInfos()) {
            CmsListItemWidget liw = new CmsListItemWidget(bean);
            CmsListItem li = new CmsListItem(liw);
            CmsContextMenuButton button = new CmsContextMenuButton(bean.getStructureId(), menuHandler, AdeContext.resourceinfo);
            liw.addButton(button);
            m_listPanel.add(li);
        }

        m_okButton.addClickHandler(event -> {
            CmsReuseInfoDialog.this.hide();
            m_callback.accept(Boolean.TRUE);
        });

        m_cancelButton.addClickHandler(event -> {
            CmsReuseInfoDialog.this.hide();
            m_callback.accept(Boolean.FALSE);
        });

    }

    /**
     * Gets the dialog buttons.
     *
     * @return the dialog buttons
     */
    private List<CmsPushButton> getDialogButtons() {

        return Arrays.asList(m_cancelButton, m_okButton);
    }

}
