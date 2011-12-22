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

package org.opencms.gwt.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

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

    /**
     * Hide constructor.<p>
     */
    private CmsNotification() {

        // empty
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
     * Returns if the message of the old mode and type needs to be restored after a new message has been shown.<p>
     * 
     * @param oldMode the old mode
     * @param newMode the new mode
     * @param oldType the old type
     * @param newType the new type
     * 
     * @return <code>true</code> if the message needs to be restored
     */
    public static boolean shouldRestoreMessage(Mode oldMode, Mode newMode, Type oldType, Type newType) {

        // only sticky messages will be restored
        if (!Mode.STICKY.equals(oldMode)) {
            return false;
        }

        // if new mode is not sticky also, restore
        if (!Mode.STICKY.equals(newMode)) {
            return true;
        }

        // if new type is superior to the old one, don't restore
        if (isSuperiorType(oldType, newType)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the new type is superior to the old type.<p>
     * 
     * @param oldType the old type
     * @param newType the new type
     * 
     * @return <code>true</code> if the new type is superior to the old type
     */
    private static boolean isSuperiorType(Type oldType, Type newType) {

        if (oldType == null) {
            return true;
        }
        if (newType == null) {
            return false;
        }
        // do not overwrite a higher or equal level notification
        switch (newType) {
            case ERROR:
                return !oldType.equals(Type.ERROR);
            case NORMAL:
                return false;
            case WARNING:
                return oldType.equals(Type.NORMAL);
            default:
        }
        return true;
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
     * Hides the notification message.<p>
     */
    public void hide() {

        if (m_widget != null) {
            m_widget.hide();
        }
    }

    /**
     * Sends a new notification.<p>
     * 
     * @param type the notification type
     * @param message the message
     */
    public void send(Type type, final String message) {

        if (m_widget != null) {
            m_widget.show(Mode.NORMAL, type, message);
        }
    }

    /**
     * Sends a new blocking notification.<p>
     * 
     * @param type the notification type
     * @param message the message
     */
    public void sendBlocking(Type type, final String message) {

        sendSticky(type, message);
        if (m_widget != null) {
            m_widget.setBlocking();
        }
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
     * Sends a new sticky notification.<p>
     * 
     * @param type the notification type
     * @param message the message
     */
    public void sendSticky(Type type, final String message) {

        if (m_widget != null) {
            m_widget.show(Mode.STICKY, type, message);
        }
    }

    /**
     * Sets the widget.<p>
     *
     * @param widget the widget to set
     */
    public void setWidget(I_CmsNotificationWidget widget) {

        m_widget = widget;
    }
}
