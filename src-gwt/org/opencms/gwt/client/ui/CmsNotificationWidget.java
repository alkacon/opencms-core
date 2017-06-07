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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsFadeAnimation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The toolbar notification widget.<p>
 *
 * @since 8.0.0
 */
public class CmsNotificationWidget extends Composite implements I_CmsNotificationWidget {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsNotificationWidgetUiBinder extends UiBinder<Widget, CmsNotificationWidget> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsNotificationWidgetUiBinder uiBinder = GWT.create(I_CmsNotificationWidgetUiBinder.class);

    /** The message. */
    @UiField
    FlowPanel m_messages;

    /** The current animation. */
    private Animation m_animation;

    /**
     * Constructor.<p>
     */
    public CmsNotificationWidget() {

        initWidget(uiBinder.createAndBindUi(this));
        restore();
        CmsNotification.get().setWidget(this);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsNotificationWidget#addMessage(org.opencms.gwt.client.ui.CmsNotificationMessage)
     */
    public void addMessage(CmsNotificationMessage message) {

        clearAnimation();
        if (m_messages.getWidgetCount() == 0) {
            animateShow();
        }
        m_messages.add(message);
        if (message.isBlockingMode()) {
            setBlocking(true);
        }
        if (message.isBusyMode()) {
            setBusy(true);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsNotificationWidget#clearMessages()
     */
    public void clearMessages() {

        animateHide();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsNotificationWidget#removeMessage(org.opencms.gwt.client.ui.CmsNotificationMessage)
     */
    public void removeMessage(CmsNotificationMessage message) {

        clearAnimation();
        if ((m_messages.getWidgetCount() == 1) && (m_messages.getWidgetIndex(message) != -1)) {
            animateHide();
        } else {
            m_messages.remove(message);
            setBlocking(requiresBlocking());
            setBusy(requiresBusy());
        }
    }

    /**
     * Clears the messages once the hide animation is completed.<p>
     */
    protected void onHideComplete() {

        m_animation = null;
        m_messages.clear();
        setBlocking(false);
        setBusy(false);
        restore();
    }

    /**
     * Called once the show animation is completed.<p>
     */
    protected void onShowComplete() {

        m_animation = null;
    }

    /**
     * Animates hiding the messages.<p>
     */
    private void animateHide() {

        m_animation = CmsFadeAnimation.fadeOut(getElement(), new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                onHideComplete();
            }
        }, 200);
    }

    /**
     * Animates showing the messages.<p>
     */
    private void animateShow() {

        // ensure to display the notification above everything else
        Element parent = getElement().getParentElement();
        if (parent != null) {
            parent.getStyle().setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexDND());
        }
        getElement().getStyle().clearVisibility();
        m_animation = CmsFadeAnimation.fadeIn(getElement(), new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                onShowComplete();
            }
        }, 200);
    }

    /**
     * Cancels the current animation.<p>
     */
    private void clearAnimation() {

        if (m_animation != null) {
            m_animation.cancel();
            m_animation = null;
        }
    }

    /**
     * Checks if any message requires a blocking overlay.<p>
     *
     * @return <code>true</code> if the blocking overlay is required
     */
    private boolean requiresBlocking() {

        for (Widget message : m_messages) {
            if (((CmsNotificationMessage)message).isBlockingMode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any message requires the busy icon.<p>
     *
     * @return <code>true</code> if the busy icon is required
     */
    private boolean requiresBusy() {

        for (Widget message : m_messages) {
            if (((CmsNotificationMessage)message).isBusyMode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Restores the initial state.<p>
     */
    private void restore() {

        Element parent = getElement().getParentElement();
        if (parent != null) {
            parent.getStyle().clearZIndex();
        }
        getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }

    /**
     * Toggles the blocking overlay.<p>
     *
     * @param blocking <code>true</code> to show the blocking overlay
     */
    private void setBlocking(boolean blocking) {

        if (blocking) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().blocking());
        } else {
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().blocking());
        }
    }

    /**
     * Toggles the busy icon.<p>
     *
     * @param busy <code>true</code> to show the busy icon
     */
    private void setBusy(boolean busy) {

        if (busy) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().busy());
        } else {
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().busy());
        }
    }
}