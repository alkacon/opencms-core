/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsNotification.java,v $
 * Date   : $Date: 2010/04/29 07:13:40 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.animation.client.Animation;

/**
 * User feedback provider.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public final class CmsNotification {

    /**
     * Notification Mode.<p>
     */
    public static enum Mode {

        /** Error Notification, wont be removed and can only be replaced by higher level notifications. */
        FIXED,

        /** Normal Notification mode, will be removed after a while or replaced with next message. */
        NORMAL;
    }

    /**
     * To animate notification messages.<p>
     */
    public abstract static class NotificationAnimation extends Animation {

        /** The duration of the animations. */
        protected static final int ANIMATION_DURATION = 200;

        /**
         * Execute the animation for the given widget.<p>
         */
        public abstract void run();

        /**
         * @see com.google.gwt.animation.client.Animation#onCancel()
         */
        @Override
        protected void onCancel() {

            CmsDebugLog.getInstance().printLine("cancel");
            onComplete();
        }

        /**
         * @see com.google.gwt.animation.client.Animation#onComplete()
         */
        @Override
        protected void onComplete() {

            CmsDebugLog.getInstance().printLine("complete");
        }

        /**
         * @see com.google.gwt.animation.client.Animation#onUpdate(double)
         */
        @Override
        protected void onUpdate(double progress) {

            CmsDebugLog.getInstance().printLine("update: " + progress);
        }
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

    /** The singleton instance. */
    private static CmsNotification INSTANCE;

    /** The current mode. */
    private Mode m_mode;

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
     * Returns the widget.<p>
     *
     * @return the widget
     */
    public I_CmsNotificationWidget getWidget() {

        return m_widget;
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
            m_widget.show(m_mode, type, message);
        }
    }

    /**
     * Sets the current mode.<p>
     * 
     * @param mode the mode to use
     */
    public void setMode(Mode mode) {

        if (mode == Mode.FIXED) {
            hide();
        }
        m_mode = mode;
        if (mode == Mode.NORMAL) {
            hide();
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
