/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsNotification.java,v $
 * Date   : $Date: 2010/04/27 09:00:01 $
 * Version: $Revision: 1.1 $
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * User feedback provider.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsNotification {

    /**
     * The notification widget.<p>
     */
    public static class CmsNotificationWidget extends Composite {

        /**
         * @see com.google.gwt.uibinder.client.UiBinder
         */
        @UiTemplate("CmsNotificationWidget.ui.xml")
        protected interface I_CmsNotificationWidgetUiBinder extends UiBinder<Widget, CmsNotificationWidget> {
            // GWT interface, nothing to do here
        }

        /** The ui-binder instance for this class. */
        private static I_CmsNotificationWidgetUiBinder uiBinder = GWT.create(I_CmsNotificationWidgetUiBinder.class);

        /** The container. */
        @UiField
        protected HTMLPanel m_container;

        /** The message. */
        @UiField
        protected Element m_message;

        /**
         * Constructor.<p>
         */
        public CmsNotificationWidget() {

            initWidget(uiBinder.createAndBindUi(this));
        }

        /**
         * Returns the container.<p>
         *
         * @return the container
         */
        public HTMLPanel getContainer() {

            return m_container;
        }

        /**
         * Returns the message.<p>
         *
         * @return the message
         */
        public Element getMessage() {

            return m_message;
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

    /** The timer to remove the last notification. */
    private Timer m_timer;

    /** The widget. */
    private CmsNotificationWidget m_widget;

    /**
     * Hide constructor.<p>
     */
    private CmsNotification() {

        m_widget = new CmsNotificationWidget();
        m_widget.setVisible(false);
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
    public CmsNotificationWidget getWidget() {

        return m_widget;
    }

    /**
     * Hides the notification message.<p>
     */
    public void hide() {

        // remove last notification if still shown
        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
        if (!m_widget.isVisible()) {
            return;
        }
        // hide
        CmsDebugLog.getInstance().printLine("hide: " + m_widget.getMessage().getInnerText());
        m_widget.setVisible(false);
        // back to plain style without error or warning
        m_widget.getMessage().setClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().message());
    }

    /**
     * Sends a new notification.<p>
     * 
     * @param type the notification type
     * @param message the message
     */
    public void send(Type type, String message) {

        // remove last notification if still shown
        hide();

        // create new notification
        switch (type) {
            case ERROR:
                m_widget.getMessage().addClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().error());
                break;
            case NORMAL:
                m_widget.getMessage().addClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().normal());
                break;
            case WARNING:
                m_widget.getMessage().addClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().warning());
                break;
            default:
        }
        m_widget.getMessage().setInnerText(message);
        m_widget.setVisible(true);
        CmsDebugLog.getInstance().printLine("show: " + m_widget.getMessage().getInnerText());

        // create timer to hide the notification
        m_timer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                hide();
            }
        };
        m_timer.schedule(3000);
    }
}
