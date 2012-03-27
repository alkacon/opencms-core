/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsPrincipalBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the Availability Dialog to set:
 * <ul>
 * <li>publish scheduled date
 * <li>date released and expiration date
 * <li>notification infos
 * </ul>
 * for a given resource.<p>
 * 
 * @since 8.0.0
 */
public class CmsAvailabilityDialog extends CmsPopup implements I_CmsHasContextMenuCommand {

    /** The bean that stores the dialog data. */
    protected CmsAvailabilityInfoBean m_availabilityInfo;

    /** The structure id of the resource. */
    protected CmsUUID m_structureId;

    /** The date box for the expiration date. */
    private CmsDateBox m_dateExpired = new CmsDateBox();

    /** The checkbox for enabling the date expired box. */
    private CmsCheckBox m_dateExpiredCheck = new CmsCheckBox();

    /** The date box for the released date. */
    private CmsDateBox m_dateReleased = new CmsDateBox();

    /** The checkbox for enabling the date released box. */
    private CmsCheckBox m_dateReleasedCheck = new CmsCheckBox();

    /** The icon class. */
    private String m_iconClass;

    /** The checkbox for the modify sibling info. */
    private CmsCheckBox m_modifySiblings = new CmsCheckBox();

    /** The checkbox for enabling or disabling the notification. */
    private CmsCheckBox m_notificationEnabled = new CmsCheckBox();

    /** The input field for the notification interval. */
    private CmsTextBox m_notificationInterval = new CmsTextBox();

    /** The main panel for the availability dialog. */
    private FlowPanel m_panel = new FlowPanel();

    /** The date box for the publish scheduled date. */
    private CmsDateBox m_publishScheduled = new CmsDateBox();

    /** The checkbox for enabling the publish date box. */
    private CmsCheckBox m_publishScheduledCheck = new CmsCheckBox();

    /** The value used for enable and disable the notification text box. */
    private String m_tmpNotificationInterval;

    /**
     * Creates the availability dialog.<p>
     * 
     * @param structureId the structure id of the resource to create the dialog for
     */
    public CmsAvailabilityDialog(CmsUUID structureId) {

        this();
        m_structureId = structureId;
    }

    /**
     * Creates the availability dialog.<p>
     * 
     * @param structureId the structure id of the resource to create the dialog for
     * @param iconClass icon class to override the resource type icon
     */
    public CmsAvailabilityDialog(CmsUUID structureId, String iconClass) {

        this(structureId);
        m_iconClass = iconClass;
    }

    /**
     * Private constructor.<p>
     */
    private CmsAvailabilityDialog() {

        super(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_TITLE_0), 439);
        setModal(true);
        setGlassEnabled(true);
    }

    /**
     * Returns the context menu command according to 
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     * 
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new I_CmsContextMenuCommand() {

            public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

                if (handler.ensureLockOnResource(structureId)) {
                    new CmsAvailabilityDialog(CmsCoreProvider.get().getStructureId()).loadAndShow();
                }
            }

            public String getCommandIconClass() {

                return org.opencms.gwt.client.ui.css.I_CmsImageBundle.INSTANCE.contextMenuIcons().availability();
            }
        };
    }

    /**
     * Executes the RPC action to retrieve the bean that stores the information for this dialog.<p>
     * 
     * After the information has been retrieved the dialog will be opened.<p>
     */
    public void loadAndShow() {

        CmsRpcAction<CmsAvailabilityInfoBean> availabilityCallback = new CmsRpcAction<CmsAvailabilityInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getAvailabilityInfo(m_structureId, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsAvailabilityInfoBean availabilityInfo) {

                stop(false);
                showDialog(availabilityInfo);
            }
        };
        availabilityCallback.execute();
    }

    /**
     * Checks if the notification interval is a positive integer.<p>
     * 
     * @return <code>true</code> if the notification interval is a positive integer
     */
    protected boolean checkNotificationInterval() {

        boolean result = true;

        if (m_notificationEnabled.isChecked()
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_notificationInterval.getText())) {
            try {
                int noti = Integer.parseInt(m_notificationInterval.getText());
                if (noti < 1) {
                    m_notificationInterval.setErrorMessage(Messages.get().key(
                        Messages.GUI_DIALOG_AVAILABILITY_ERR_POS_0));
                    result = false;
                } else {
                    m_notificationInterval.setErrorMessage(null);
                }
            } catch (NumberFormatException e) {
                m_notificationInterval.setErrorMessage(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_ERR_POS_0));
                result = false;
            }
        }
        return result;
    }

    /**
     * Returns the content panel.<p>
     * 
     * @return the content panel
     */
    protected FlowPanel getPanel() {

        return m_panel;
    }

    /**
     * When the notification checkbox is clicked the according textbox is enabled/disabled.<p>
     */
    protected void onNotificationCheckboxClick() {

        if (m_notificationEnabled.isChecked()) {
            m_notificationInterval.setFormValueAsString(m_tmpNotificationInterval);
            m_notificationInterval.setEnabled(true);
        } else {
            m_tmpNotificationInterval = m_notificationInterval.getText();
            m_notificationInterval.setFormValueAsString(Messages.get().key(Messages.GUI_INPUT_NOT_USED_0));
            m_notificationInterval.setEnabled(false);
        }
    }

    /**
     * On submit the availability dialog is validated, the bean is sent to the server and the dialog will be closed.<p>
     */
    protected void onSubmit() {

        // submit the form
        if (validateForm()) {
            // if the user input is valid, submit the bean as RPC call
            submitBean();
            // close the availability dialog
            hide();
        }
    }

    /**
     * This method creates the dialog components.<p>
     * 
     * @param dialogBean the bean that stores the dialog information
     */
    protected void showDialog(CmsAvailabilityInfoBean dialogBean) {

        m_availabilityInfo = dialogBean;
        // create the info box
        final CmsListItemWidget info = new CmsListItemWidget(m_availabilityInfo.getPageInfo());
        if (m_iconClass != null) {
            info.setIcon(m_iconClass);
        }
        m_panel.add(info);

        // create the publish scheduled field
        CmsFieldSet publishScheduledField = new CmsFieldSet();
        publishScheduledField.getWrapper().addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().fieldsetSpacer());
        publishScheduledField.setLegend(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_PUB_SCHEDULED_0));
        m_publishScheduled.setAutoHideParent(this);
        FlowPanel publishScheduled = createInputCombinationPanel(m_publishScheduledCheck, m_publishScheduled);
        addClickHandlerToInputCheckbox(m_publishScheduledCheck, m_publishScheduled);
        publishScheduledField.addContent(createTwoColumnRow(
            Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_PUB_SCHEDULED_DATE_0),
            publishScheduled));
        m_panel.add(publishScheduledField);

        // create the release and expiration field
        CmsFieldSet availabilityField = new CmsFieldSet();
        availabilityField.getWrapper().addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().fieldsetSpacer());
        availabilityField.setLegend(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_AVAILABILITY_0));
        m_dateReleased.setAutoHideParent(this);
        FlowPanel dateReleased = createInputCombinationPanel(m_dateReleasedCheck, m_dateReleased);
        availabilityField.addContent(createTwoColumnRow(
            Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_RELEASED_DATE_0),
            dateReleased));
        addClickHandlerToInputCheckbox(m_dateReleasedCheck, m_dateReleased);
        m_dateExpired.setAutoHideParent(this);
        FlowPanel dateExpired = createInputCombinationPanel(m_dateExpiredCheck, m_dateExpired);
        availabilityField.addContent(createTwoColumnRow(
            Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_EXPIRED_DATE_0),
            dateExpired));
        addClickHandlerToInputCheckbox(m_dateExpiredCheck, m_dateExpired);
        m_panel.add(availabilityField);

        // create the notification setting field
        if (!m_availabilityInfo.getResponsibles().isEmpty()) {
            CmsFieldSet notificationField = new CmsFieldSet();
            notificationField.getWrapper().addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().fieldsetSpacer());
            notificationField.setLegend(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_NOTI_SETTINGS_0));
            m_notificationInterval.addBlurHandler(new BlurHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
                 */
                public void onBlur(BlurEvent event) {

                    checkNotificationInterval();

                }
            });

            FlowPanel notificationInterval = createInputCombinationPanel(m_notificationEnabled, m_notificationInterval);
            notificationField.addContent(createTwoColumnRow(
                Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_NOTI_INTERVAL_0),
                notificationInterval));
            addClickHandlerToNotificationCheckBox();
            if (m_availabilityInfo.isHasSiblings()) {
                notificationField.addContent(createTwoColumnRow(
                    Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_MODIFY_SIBLINGS_0),
                    m_modifySiblings));
            }
            notificationField.addContent(createResposibles());
            m_panel.add(notificationField);
        }

        // create the buttons
        createButtons();

        // fill the dialog with values from the bean
        insertValuesIntoForm();

        // add the main panel and center the popup
        add(m_panel);
        center();
        catchNotifications();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                info.truncate(CmsAvailabilityDialog.this.hashCode() + "", getPanel().getElement().getClientWidth());

            }
        });
    }

    /**
     * Adds a click handler to the checkbox that enables or disables the datebox.<p>
     * 
     * @param inputCheckbox the checkbox
     * @param formWidget the datebox
     */
    private void addClickHandlerToInputCheckbox(final CmsCheckBox inputCheckbox, final I_CmsFormWidget formWidget) {

        inputCheckbox.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (inputCheckbox.isChecked()) {
                    formWidget.setEnabled(true);
                } else {
                    formWidget.setEnabled(false);
                }
            }
        });
    }

    /**
     * Adds a click handler to the notification enabled checkbox.<p>
     */
    private void addClickHandlerToNotificationCheckBox() {

        m_notificationEnabled.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onNotificationCheckboxClick();

            }
        });
    }

    /**
     * Creates the buttons.<p>
     */
    private void createButtons() {

        addDialogClose(null);

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setTitle(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setSize(I_CmsButton.Size.medium);
        cancelButton.setUseMinWidth(true);
        cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsAvailabilityDialog.this.hide();
            }
        });
        addButton(cancelButton);

        CmsPushButton saveButton = new CmsPushButton();
        saveButton.setTitle(Messages.get().key(Messages.GUI_OK_0));
        saveButton.setText(Messages.get().key(Messages.GUI_OK_0));
        saveButton.setSize(I_CmsButton.Size.medium);
        saveButton.setUseMinWidth(true);
        saveButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onSubmit();
            }
        });
        addButton(saveButton);
    }

    /**
     * Creates a flowpanel with a widget and a checkbox in front of it.<p>
     * 
     * @param checkbox the checkbox for the date box 
     * @param w the widget behind the checkbox
     * 
     * @return the flowpanel with a widget and a checkbox
     */
    private FlowPanel createInputCombinationPanel(CmsCheckBox checkbox, Widget w) {

        FlowPanel inputCombination = new FlowPanel();
        inputCombination.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().inputCombination());
        checkbox.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().checkBox());
        w.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().dateBox());
        inputCombination.add(checkbox);
        inputCombination.add(w);
        return inputCombination;

    }

    /**
     * Creates a flow panel for the responsible users of the current uri.<p>
     * 
     * @return the flow panel for responsible users
     */
    private Widget createResposibles() {

        FlowPanel resposibles = new FlowPanel();
        CmsLabel resposibleLabel = new CmsLabel(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_RES_USERS_0));
        resposibleLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().responsabilityLabel());
        resposibles.add(resposibleLabel);
        for (Map.Entry<CmsPrincipalBean, String> entry : m_availabilityInfo.getResponsibles().entrySet()) {
            FlowPanel resEntry = new FlowPanel();
            String prinText = entry.getKey().getName();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getValue())) {
                prinText += Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_INHERITED_FROM_1, entry.getValue());
            }
            CmsLabel label = new CmsLabel(prinText);
            label.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().inlineBlock());
            Image img = new Image();
            if (entry.getKey().isGroup()) {
                img.setResource(I_CmsImageBundle.INSTANCE.groupImage());
            } else {
                img.setResource(I_CmsImageBundle.INSTANCE.userImage());
            }
            img.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().principalIcon());
            img.getElement().setAttribute("align", "top");
            resEntry.add(img);
            resEntry.add(label);
            resposibles.add(resEntry);
        }
        return resposibles;
    }

    /**
     * Creates a two column row used for a field set.<p>
     * 
     * @param labelText the label for this row
     * @param w the input widget for this row
     * 
     * @return a flow panel that contains the label and the input widget
     */
    private Widget createTwoColumnRow(String labelText, Widget w) {

        FlowPanel result = new FlowPanel();
        result.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().inlineBlock());

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(labelText) && (w != null)) {
            CmsLabel inputLabel = new CmsLabel(labelText);
            inputLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().labelColumn());
            result.add(inputLabel);
            w.addStyleName(I_CmsLayoutBundle.INSTANCE.availabilityCss().inputCombination());
            result.add(w);
        } else if (w != null) {
            result.add(w);
        }
        return result;
    }

    /**
     * Inserts the values of this form into the dialog bean.<p>
     */
    private void insertValuesIntoBean() {

        if (m_publishScheduled.isEnabled() && (m_publishScheduled.getValue() != null)) {
            m_availabilityInfo.setDatePubScheduled(m_publishScheduled.getValue().getTime());
        } else if (!m_publishScheduled.isEnabled()) {
            m_availabilityInfo.setDatePubScheduled(CmsAvailabilityInfoBean.DATE_PUBLISH_SCHEDULED_DEFAULT);
        }
        if (m_dateReleased.isEnabled() && (m_dateReleased.getValue() != null)) {
            m_availabilityInfo.setDateReleased(m_dateReleased.getValue().getTime());
        } else if (!m_dateReleased.isEnabled()) {
            m_availabilityInfo.setDateReleased(CmsAvailabilityInfoBean.DATE_RELEASED_DEFAULT);
        }
        if (m_dateExpired.isEnabled() && (m_dateExpired.getValue() != null)) {
            m_availabilityInfo.setDateExpired(m_dateExpired.getValue().getTime());
        } else if (!m_dateExpired.isEnabled()) {
            m_availabilityInfo.setDateExpired(CmsAvailabilityInfoBean.DATE_EXPIRED_DEFAULT);
        }
        if (m_notificationEnabled.isChecked()) {
            m_availabilityInfo.setNotificationEnabled(true);
            int noti = Integer.parseInt(m_notificationInterval.getText());
            m_availabilityInfo.setNotificationInterval(noti);
            m_availabilityInfo.setModifySiblings(m_modifySiblings.isChecked());
        } else {
            m_availabilityInfo.setNotificationEnabled(false);
            m_availabilityInfo.setModifySiblings(m_modifySiblings.isChecked());
        }
    }

    /**
     * Inserts the values of the bean into the form.<p>
     */
    private void insertValuesIntoForm() {

        m_publishScheduled.setValue(null);
        m_publishScheduled.setEnabled(false);
        m_publishScheduledCheck.setChecked(false);

        m_dateReleased.setEnabled(false);
        if (m_availabilityInfo.getDateReleased() != CmsAvailabilityInfoBean.DATE_RELEASED_DEFAULT) {
            m_dateReleased.setValue(new Date(m_availabilityInfo.getDateReleased()));
            m_dateReleased.setEnabled(true);
            m_dateReleasedCheck.setChecked(true);
        }
        m_dateExpired.setEnabled(false);
        if (m_availabilityInfo.getDateExpired() != CmsAvailabilityInfoBean.DATE_EXPIRED_DEFAULT) {
            m_dateExpired.setValue(new Date(m_availabilityInfo.getDateExpired()));
            m_dateExpired.setEnabled(true);
            m_dateExpiredCheck.setChecked(true);
        }

        m_tmpNotificationInterval = Integer.toString(m_availabilityInfo.getNotificationInterval());
        if (m_availabilityInfo.isNotificationEnabled()) {
            m_notificationInterval.setFormValueAsString(m_tmpNotificationInterval);
        } else {
            m_notificationInterval.setFormValueAsString(Messages.get().key(Messages.GUI_INPUT_NOT_USED_0));
        }
        m_notificationInterval.setEnabled(m_availabilityInfo.isNotificationEnabled());
        m_notificationEnabled.setChecked(m_availabilityInfo.isNotificationEnabled());

        m_modifySiblings.setChecked(false);
    }

    /**
     * Submits the bean.<p>
     */
    private void submitBean() {

        // insert the user input data into the bean
        insertValuesIntoBean();

        CmsRpcAction<Void> callback = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().setAvailabilityInfo(m_structureId, m_availabilityInfo, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(Void result) {

                // noop
            }
        };
        callback.execute();
    }

    /**
     * Validates the users input.<p>
     * 
     * @return <code>true</code> if the user input is valid <code>false</code> otherwise
     */
    private boolean validateForm() {

        boolean isValid = true;

        if (m_publishScheduled.hasErrors()
            || m_dateReleased.hasErrors()
            || m_dateExpired.hasErrors()
            || m_notificationInterval.hasError()) {
            m_publishScheduled.setErrorMessage(null);
            m_dateReleased.setErrorMessage(null);
            m_dateExpired.setErrorMessage(null);
            m_notificationInterval.setErrorMessage(null);
            isValid = false;
        }

        // check the publish scheduled date
        Date bublishScheduled = m_publishScheduled.getValue();
        if (bublishScheduled != null) {
            if (bublishScheduled.before(new Date(System.currentTimeMillis()))) {
                m_publishScheduled.setErrorMessage(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_ERR_PAST_0));
                isValid = false;
            } else {
                m_publishScheduled.setErrorMessage(null);
            }
        }

        // check date released and date expired
        Date dateReleased = m_dateReleased.getValue();
        Date dateExpired = m_dateExpired.getValue();
        if ((dateReleased != null) && (dateExpired != null)) {
            if (dateReleased.after(dateExpired)) {
                m_dateReleased.setErrorMessage(Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_ERR_BEFORE_0));
                isValid = false;
            } else {
                m_dateReleased.setErrorMessage(null);
            }
        }

        // check the notification interval
        if (!checkNotificationInterval()) {
            isValid = false;
        }

        if (!isValid) {
            CmsNotification.get().send(Type.WARNING, Messages.get().key(Messages.GUI_DIALOG_AVAILABILITY_DIALOG_ERR_0));
        }
        return isValid;
    }
}
