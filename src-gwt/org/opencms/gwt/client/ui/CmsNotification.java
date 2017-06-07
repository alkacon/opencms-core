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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;

/**
 * User feedback provider.<p>
 *
 * @since 8.0.0
 */
public final class CmsNotification {

    /**
     * Notification Mode.<p>
     */
    public static enum Mode {

        /** Alert mode. */
        BROADCAST,

        /** Blocking mode. */
        BUSY,

        /** Normal mode. */
        NORMAL,

        /** Sticky mode. */
        STICKY;
    }

    /**
     * Notification Type.<p>
     */
    public static enum Type {

        /** Error Notification. */
        ERROR,

        /** Normal Notification. */
        NORMAL,

        /** Warning Notification. */
        WARNING;
    }

    /** The duration of the animations. */
    public static final int ANIMATION_DURATION = 200;

    /** The singleton instance. */
    private static CmsNotification INSTANCE;

    /** The widget. */
    private I_CmsNotificationWidget m_widget;

    /** The current notifications. */
    private List<CmsNotificationMessage> m_messages;

    /**
     * Hide constructor.<p>
     */
    private CmsNotification() {

        m_messages = new ArrayList<CmsNotificationMessage>();
    }

    /**
     * Returns the singleton instance.<p>
     *
     * @return the singleton instance
     */
    public static CmsNotification get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsNotification();
        }
        return INSTANCE;
    }

    /**
     * Returns the widget.<p>
     *
     * @return the widget
     */
    public I_CmsNotificationWidget getWidget() {

        return m_widget;
    }

    /**
     * Returns if the notification widget is set. Only if the widget is set, notifications can be shown.<p>
     *
     * @return <code>true</code> if the notification widget is set
     */
    public boolean hasWidget() {

        return m_widget != null;
    }

    /**
     * Removes the given notification message.<p>
     *
     * @param message the message to remove
     */
    public void removeMessage(CmsNotificationMessage message) {

        m_messages.remove(message);
        if (hasWidget()) {
            m_widget.removeMessage(message);
        }
    }

    /**
     * Sends a new notification, that will be removed automatically.<p>
     *
     * @param type the notification type
     * @param message the message
     */
    public void send(Type type, final String message) {

        final CmsNotificationMessage notificationMessage = new CmsNotificationMessage(Mode.NORMAL, type, message);
        m_messages.add(notificationMessage);
        if (hasWidget()) {
            m_widget.addMessage(notificationMessage);
        }
        Timer timer = new Timer() {

            @Override
            public void run() {

                removeMessage(notificationMessage);
            }
        };
        timer.schedule(4000 * (type == Type.NORMAL ? 1 : 2));

    }

    /**
     * Sends a new blocking alert notification that can be closed by the user.<p>
     *
     * @param type the notification type
     * @param message the message
     */
    public void sendAlert(Type type, String message) {

        CmsNotificationMessage notificationMessage = new CmsNotificationMessage(Mode.BROADCAST, type, message);
        m_messages.add(notificationMessage);
        if (hasWidget()) {
            m_widget.addMessage(notificationMessage);
        }
    }

    /**
     * Sends a new blocking notification that can not be removed by the user.<p>
     *
     * @param type the notification type
     * @param message the message
     *
     * @return the message, use to hide the message
     */
    public CmsNotificationMessage sendBusy(Type type, final String message) {

        CmsNotificationMessage notificationMessage = new CmsNotificationMessage(Mode.BUSY, type, message);
        m_messages.add(notificationMessage);
        if (hasWidget()) {
            m_widget.addMessage(notificationMessage);
        }
        return notificationMessage;
    }

    /**
     * Sends a new notification after all other events have been processed.<p>
     *
     * @param type the notification type
     * @param message the message
     */
    public void sendDeferred(final Type type, final String message) {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                send(type, message);
            }
        });

    }

    /**
     * Sends a new sticky notification that can not be removed by the user.<p>
     *
     * @param type the notification type
     * @param message the message
     *
     *  @return the message, use to hide the message
     */
    public CmsNotificationMessage sendSticky(Type type, String message) {

        CmsNotificationMessage notificationMessage = new CmsNotificationMessage(Mode.STICKY, type, message);
        m_messages.add(notificationMessage);
        if (hasWidget()) {
            m_widget.addMessage(notificationMessage);
        }
        return notificationMessage;
    }

    /**
     * Sets the widget.<p>
     *
     * @param widget the widget to set
     */
    public void setWidget(I_CmsNotificationWidget widget) {

        if (m_widget != null) {
            m_widget.clearMessages();
        }
        m_widget = widget;
        m_widget.clearMessages();
        for (CmsNotificationMessage message : m_messages) {
            m_widget.addMessage(message);
        }
    }
}
