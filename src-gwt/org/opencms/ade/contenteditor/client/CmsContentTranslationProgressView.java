/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.contenteditor.client;

import org.opencms.ade.contenteditor.shared.CmsContentAugmentationDetails;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * Displays the progress about a translation job while said job is running on the server.
 *
 * <p>Also allows a user to try to abort the job.
 */
public class CmsContentTranslationProgressView extends Composite {

    /**
     * UiBinder interface for this dialog.<p>
     */
    interface I_UiBinder extends UiBinder<Panel, CmsContentTranslationProgressView> {
        // empty uibinder interface
    }

    /** Length of the progress check interval in milliseconds. */
    public static final int PROGRESS_CHECK_INTERVAL = 1000;

    /** UiBinder instance for this dialog. */
    private static I_UiBinder uibinder = GWT.create(I_UiBinder.class);

    /** The label used to display the progress. */
    @UiField
    protected Label m_message;

    /** The action to execute after the translation has finished. */
    private Consumer<CmsContentAugmentationDetails> m_action;

    /** The buttons. */
    private List<CmsPushButton> m_buttons = new ArrayList<>();

    /** The ID of the job whose progress should be checked. */
    private CmsUUID m_jobId;

    /** The popup in which the widget is displayed. */
    private CmsPopup m_popup;

    /** The content service. */
    private I_CmsContentServiceAsync m_service;

    /** The timer used to periodically check the progress. */
    private Timer m_timer;

    /**
     * Creates a new instance.
     *
     * @param jobId the id of the job whose progress to check
     * @param service the content service
     * @param action the action to execute after the job has finished
     */
    public CmsContentTranslationProgressView(
        CmsUUID jobId,
        I_CmsContentServiceAsync service,
        Consumer<CmsContentAugmentationDetails> action) {

        m_jobId = jobId;
        m_action = action;
        m_service = service;
        initWidget(uibinder.createAndBindUi(this));
        m_message.setText(Messages.get().key(Messages.GUI_TRANSLATION_DIALOG_INITIAL_PROGRESS_0));
        CmsPushButton abortButton = new CmsPushButton();
        abortButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        abortButton.setText(Messages.get().key(Messages.GUI_TRANSLATION_DIALOG_ABORT_BUTTON_0));
        abortButton.setUseMinWidth(true);
        abortButton.addClickHandler(event -> {
            if (m_timer != null) {
                m_timer.cancel();
                m_timer = null;
            }
            service.abortAugmentationJob(m_jobId, new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {

                    m_popup.hide();
                }

                @Override
                public void onSuccess(Void result) {

                    m_popup.hide();
                }
            });

        });

        m_buttons.add(abortButton);

    }

    /**
     * Displays a popup containing a widget of this class.
     *
     * @param jobId the ID of the job whose progress should be checked
     * @param service the content service
     * @param action the action to execute after the translation has finished
     */
    public static void showDialog(
        CmsUUID jobId,
        I_CmsContentServiceAsync service,
        Consumer<CmsContentAugmentationDetails> action) {

        CmsPopup popup = new CmsPopup(
            Messages.get().key(Messages.GUI_TRANSLATION_DIALOG_PROGRESS_TITLE_0),
            CmsPopup.DEFAULT_WIDTH);
        CmsContentTranslationProgressView view = new CmsContentTranslationProgressView(jobId, service, action);
        popup.setMainContent(view);
        popup.setModal(true);
        popup.setGlassEnabled(true);
        view.setPopup(popup);
        for (CmsPushButton button : view.getButtons()) {
            popup.addButton(button);
        }

        popup.center();
    }

    /**
     * Gets the dialog buttons.
     *
     * @return the dialog buttons
     */
    public List<CmsPushButton> getButtons() {

        return m_buttons;
    }

    /**
     * Sets the action to execute after the job has finished.
     * @param action
     */
    public void setAction(Consumer<CmsContentAugmentationDetails> action) {

        m_action = action;

    }

    /**
     * Sets the popup in which this widget is displayed.
     *
     * @param popup the popup
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

    /**
     * Gets information about the job progress from the server, and if the job isn't finished, schedules another progress check.
     */
    protected void checkProgress() {

        m_service.getAugmentationProgress(m_jobId, new AsyncCallback<CmsContentAugmentationDetails>() {

            @Override
            public void onFailure(Throwable caught) {

                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(CmsContentAugmentationDetails result) {

                if (result.isDone() || result.isAborted()) {
                    m_popup.hide();
                    m_action.accept(result);
                } else {
                    if (result.getProgress() != null) {
                        m_message.setText(result.getProgress());
                    }
                    m_timer = new Timer() {

                        @Override
                        public void run() {

                            checkProgress();
                        }

                    };
                    m_timer.schedule(PROGRESS_CHECK_INTERVAL);

                }
            }
        });

    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        checkProgress();

    }

}
