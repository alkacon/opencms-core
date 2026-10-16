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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsQuotaWarning;
import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dismissible budget notice using the gallery popup's standard header.<p>
 */
public class CmsGalleryQuotaNotice extends Composite {

    /** The containing popup, temporarily showing the notice caption. */
    private CmsPopup m_popup;

    /** Original popup caption, restored on continuation and closing. */
    private String m_originalCaption;

    /** Whether the notice added a close button to the containing popup. */
    private boolean m_addedClose;

    /** Gallery content, hidden while the notice is active. */
    private final Widget m_content;

    /** Notice panel. */
    private final FlowPanel m_notice = new FlowPanel();

    /** Shared budget warning. */
    private final CmsQuotaWarning m_warning = new CmsQuotaWarning();

    /** Explicit acknowledgement button. */
    private final CmsPushButton m_continue = new CmsPushButton();

    /** Restores gallery sizing after displaying the normal content. */
    private final Command m_onContinue;

    /** Context resource, independent of gallery search/navigation. */
    private String m_referenceRootPath;

    /** Last displayed snapshot; acknowledge its state, not a newer server state. */
    private CmsQuotaStatus m_status;

    /** Whether the content may be displayed and measured. */
    private boolean m_ready;

    /** Preview requested while checking or waiting for acknowledgement. */
    private Command m_pendingPreview;

    /** Guards callbacks after closing, reopening or changing context. */
    private int m_request;

    /**
     * Wraps the gallery without changing the layout of its tabs and previews.<p>
     *
     * @param content the gallery content
     * @param onContinue restores gallery sizing
     */
    public CmsGalleryQuotaNotice(Widget content, Command onContinue) {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        m_content = content;
        m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticeContent());
        m_onContinue = onContinue;
        FlowPanel root = new FlowPanel();
        root.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticeWrapper());
        root.add(content);
        m_notice.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticePanel());
        m_notice.getElement().setAttribute("role", "region");
        m_notice.getElement().setAttribute("aria-label", Messages.get().key(Messages.GUI_QUOTA_LABEL_0));
        m_continue.setText(Messages.get().key(Messages.GUI_QUOTA_GALLERY_CONTINUE_0));
        m_continue.addClickHandler(event -> acknowledge());
        m_notice.add(m_warning);
        FlowPanel actions = new FlowPanel();
        actions.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticeActions());
        actions.add(m_continue);
        m_notice.add(actions);
        root.add(m_notice);
        initWidget(root);
        showPending();
    }

    /**
     * Runs a preview action after checking and, if necessary, acknowledging the budget.<p>
     *
     * @param action the preview action; a newer request replaces an earlier pending preview
     */
    public void runAfterCheck(Command action) {

        if (m_ready && isAttached()) {
            action.execute();
        } else {
            m_pendingPreview = action;
        }
    }

    /**
     * Sets the gallery context resource used to resolve the enclosing budget scope.<p>
     *
     * @param rootPath its VFS root path
     */
    public void setReferenceRootPath(String rootPath) {

        if ((rootPath != null) && !rootPath.equals(m_referenceRootPath)) {
            m_referenceRootPath = rootPath;
            if (isAttached()) {
                checkWarning();
            }
        }
    }

    /**
     * Persists the explicitly confirmed snapshot in the current session.<p>
     *
     * @param status the displayed snapshot
     * @param callback the completion callback
     */
    protected void acknowledgeWarning(CmsQuotaStatus status, AsyncCallback<Void> callback) {

        CmsGalleryController.getGalleryService().acknowledgeQuotaWarning(
            status.getRootPath(),
            status.getState(),
            callback);
    }

    /**
     * Loads an unacknowledged warning using the existing cached consumption values.<p>
     *
     * @param rootPath the context resource
     * @param callback the result callback
     */
    protected void loadWarning(String rootPath, AsyncCallback<CmsQuotaStatus> callback) {

        CmsGalleryController.getGalleryService().getQuotaWarning(rootPath, callback);
    }

    /**
     * Checks again whenever a retained gallery dialog is reopened.<p>
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        showPending();
        final int request = ++m_request;
        // The popup must finish attaching its caption first.
        Scheduler.get().scheduleDeferred(() -> {
            if (isAttached() && (request == m_request) && (m_referenceRootPath != null)) {
                checkWarning();
            }
        });
    }

    /**
     * Discards late callbacks without acknowledging a closed notice.<p>
     */
    @Override
    protected void onUnload() {

        ++m_request;
        m_ready = false;
        m_pendingPreview = null;
        restorePopup();
        super.onUnload();
    }

    /**
     * Stores confirmation only after clicking the continue button.<p>
     */
    private void acknowledge() {

        if (m_status == null) {
            return;
        }
        final int request = m_request;
        m_continue.setEnabled(false);
        acknowledgeWarning(m_status, new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {

                // Advisory warning: a session write failure must not prevent gallery access.
                complete();
            }

            public void onSuccess(Void result) {

                complete();
            }

            private void complete() {

                if (isAttached() && (request == m_request)) {
                    showGallery();
                }
            }
        });
    }

    /**
     * Loads the warning once for this opening, never on gallery selection changes.<p>
     */
    private void checkWarning() {

        final int request = ++m_request;
        showPending();
        loadWarning(m_referenceRootPath, new AsyncCallback<CmsQuotaStatus>() {

            public void onFailure(Throwable caught) {

                if (isAttached() && (request == m_request)) {
                    showGallery();
                }
            }

            public void onSuccess(CmsQuotaStatus status) {

                if (!isAttached() || (request != m_request)) {
                    return;
                }
                if ((status == null)
                    || (status.getQuotaBytes() <= 0)
                    || (status.getUsedBytes() < 0)
                    || (status.getState() != State.EXCEEDED)) {
                    showGallery();
                    return;
                }
                configurePopup();
                m_status = status;
                m_warning.setGalleryStatus(status);
                removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticePending());
                addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticeActive());
                m_notice.setVisible(true);
                if (m_popup instanceof CmsGalleryPopup) {
                    m_popup.center();
                }
                m_continue.setEnabled(true);
                m_continue.setFocus(true);
            }
        });
    }

    /**
     * Uses the same caption and close control as the publish dialog.<p>
     */
    private void configurePopup() {

        if (m_popup != null) {
            return;
        }
        Widget parent = getParent();
        while ((parent != null) && !(parent instanceof CmsPopup)) {
            parent = parent.getParent();
        }
        if (parent instanceof CmsPopup) {
            m_popup = (CmsPopup)parent;
            // An empty caption hides the header, but CmsPopup retains its previous text.
            m_originalCaption = m_popup.getElement().hasClassName(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dialogCss().hideCaption())
                ? ""
                : m_popup.getCaption();
            m_popup.setCaption(Messages.get().key(Messages.GUI_QUOTA_GALLERY_TITLE_0));
            m_addedClose = !m_popup.hasDialogClose();
            if (m_addedClose) {
                m_popup.addDialogClose(null);
            }
        }
    }

    /**
     * Restores the containing popup without changing any existing close command.<p>
     */
    private void restorePopup() {

        if (m_popup != null) {
            m_popup.setCaption(m_originalCaption);
            if (m_addedClose) {
                m_popup.removeDialogClose();
            }
            m_popup = null;
        }
    }

    /**
     * Restores the normal gallery, including keyboard access and its original sizing.<p>
     */
    private void showGallery() {

        m_status = null;
        restorePopup();
        m_notice.setVisible(false);
        removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticeActive());
        removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticePending());
        m_content.getElement().removeAttribute("inert");
        m_content.getElement().removeAttribute("aria-hidden");
        m_ready = true;
        Command preview = m_pendingPreview;
        m_pendingPreview = null;
        if (preview != null) {
            preview.execute();
        }
        m_onContinue.execute();
    }

    /**
     * Prevents interaction with the gallery while checking or awaiting acknowledgement.<p>
     */
    private void showPending() {

        m_status = null;
        m_ready = false;
        m_notice.setVisible(false);
        removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticeActive());
        addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().quotaNoticePending());
        m_content.getElement().setAttribute("inert", "");
        m_content.getElement().setAttribute("aria-hidden", "true");
    }
}
