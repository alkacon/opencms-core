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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Dialog to select a resource model for a new resource.<p>
 *
 * @param <INFO> the type of list info bean which should be used as select options
 *
 * @since 8.0.3
 */
public abstract class A_CmsListItemSelectDialog<INFO extends CmsListInfoBean> extends CmsPopup {

    /** The text metrics key. */
    private static final String TEXT_METRICS_KEY = "CMS_LIST_ITEM_SELECT_DIALOG_METRICS";

    /** Static counter used for generating ids. */
    private static long idCounter;

    /** The close button. */
    private CmsPushButton m_cancelButton;

    /** The scroll panel. */
    private CmsList<CmsListItem> m_listPanel;

    /** The label to display the dialog message. */
    private Label m_messageLabel;

    /** The unlock button. */
    private CmsPushButton m_okButton;

    /** The radio button group. */
    private CmsRadioButtonGroup m_radioGroup;

    /** Internal map used to keep track of the list info beans. */
    private Map<String, INFO> m_infosByName = new HashMap<String, INFO>();

    /**
     * Constructor.<p>
     *
     * @param itemInfos the list info beans for the possible select options
     * @param title the title for the model selection dialog
     * @param message the message to display in the model selection dialog
     */
    public A_CmsListItemSelectDialog(List<INFO> itemInfos, String title, String message) {

        super(title);
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
        addDialogClose(null);
        m_okButton = new CmsPushButton();
        m_okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        m_okButton.setUseMinWidth(true);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onClickOk();
            }
        });
        addButton(m_okButton);
        setGlassEnabled(true);
        FlowPanel content = new FlowPanel();
        m_messageLabel = new Label(message);

        content.add(m_messageLabel);
        m_radioGroup = new CmsRadioButtonGroup();
        m_listPanel = new CmsList<CmsListItem>();
        m_listPanel.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dialogCss().modelSelectList());
        m_listPanel.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        content.add(m_listPanel);
        boolean first = true;
        for (INFO info : itemInfos) {
            String name = generateName();
            m_infosByName.put(name, info);
            final CmsRadioButton radioButton = new CmsRadioButton(name, "");
            radioButton.setGroup(m_radioGroup);
            ClickHandler clickHandler = new ClickHandler() {

                public void onClick(ClickEvent event) {

                    radioButton.setChecked(true);

                }
            };
            if (first) {
                radioButton.setChecked(true);
                first = false;
            }
            CmsListItemWidget itemWidget = new CmsListItemWidget(info);
            itemWidget.addClickHandler(clickHandler);
            CmsListItem listItem = new CmsListItem(itemWidget);
            listItem.addDecorationWidget(radioButton, 18);
            //listItem.setId(String.valueOf(modelInfo.getStructureId()));
            m_listPanel.add(listItem);
        }
        setMainContent(content);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        super.center();
        adjustListHeight();
        // ensure centerd
        super.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
        adjustListHeight();
    }

    /**
     * Makes sure the item with the given structure id is checked.<p>
     *
     * @param structureId the structure id
     */
    protected void ensureChecked(CmsUUID structureId) {

        CmsRadioButton button = (CmsRadioButton)m_listPanel.getItem(
            String.valueOf(structureId)).getDecorationWidgets().get(0);
        if (!button.isChecked()) {
            button.setChecked(true);
        }
    }

    /**
     * The method that will be called with the selected item when the user clicks on OK.<p>
     *
     * @param info the selected item
     */
    protected abstract void handleSelection(INFO info);

    /**
     * Creates the new element with selected model resource.<p>
     */
    protected void onClickOk() {

        String radioButtonName = m_radioGroup.getSelectedButton().getName();
        INFO selectedItemInfo = m_infosByName.get(radioButtonName);
        handleSelection(selectedItemInfo);
        hide();
    }

    /**
     * Adjusts the max height setting of the model list.<p>
     */
    private void adjustListHeight() {

        int maxHeight = getAvailableHeight(m_messageLabel.getOffsetHeight() + 5);
        m_listPanel.getElement().getStyle().setProperty("maxHeight", maxHeight, Unit.PX);
        m_listPanel.truncate(TEXT_METRICS_KEY, CmsPopup.DEFAULT_WIDTH - 20);
    }

    /**
     * Generates internal names for the radio button widgets.<p>
     *
     * @return the generated name
     */
    private String generateName() {

        return "dialogselectitem_" + (++idCounter);
    }

}
