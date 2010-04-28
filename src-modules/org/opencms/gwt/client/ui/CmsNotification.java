/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsNotification.java,v $
 * Date   : $Date: 2010/04/28 12:10:07 $
 * Version: $Revision: 1.2 $
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
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * User feedback provider.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
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
         * Returns the message.<p>
         *
         * @return the message
         */
        public Element getMessage() {

            return m_message;
        }
    }

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
        protected static final int ANIMATION_DURATION = 300;

        /**
         * Execute the animation for the given widget.<p>
         * 
         * @param widget the widget to animate
         */
        public abstract void run(CmsNotificationWidget widget);

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
            if (CmsNotification.get().getHideAnimation() == this) {
                CmsNotification.get().onHideComplete();
            } else {
                CmsNotification.get().onShowComplete();
            }
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

    /** The current animation. */
    protected NotificationAnimation m_animation;

    /** The hide animation. */
    private NotificationAnimation m_hideAnimation;

    /** The current mode. */
    private Mode m_mode;

    /** The show animation. */
    private NotificationAnimation m_showAnimation;

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
     * Returns the hideAnimation.<p>
     *
     * @return the hideAnimation
     */
    public NotificationAnimation getHideAnimation() {

        if (m_hideAnimation == null) {
            m_hideAnimation = new NotificationAnimation() {

                /** The height. */
                private int m_height;

                /** The widget. */
                private CmsNotificationWidget m_internalWidget;

                /** The initial top position. */
                private int m_top;

                /**
                 * @see org.opencms.gwt.client.ui.CmsNotification.NotificationAnimation#run(org.opencms.gwt.client.ui.CmsNotification.CmsNotificationWidget)
                 */
                @Override
                public void run(CmsNotificationWidget widget) {

                    m_internalWidget = widget;
                    Element cntElement = m_internalWidget.getElement();
                    m_height = CmsDomUtil.getCurrentStyleInt(cntElement, CmsDomUtil.Style.height);
                    m_top = CmsDomUtil.getCurrentStyleInt(cntElement, CmsDomUtil.Style.top);
                    CmsDebugLog.getInstance().printLine("setHeight: " + m_height);
                    run(ANIMATION_DURATION);
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onComplete()
                 */
                @Override
                protected void onComplete() {

                    Style cntStyle = m_internalWidget.getElement().getStyle();
                    cntStyle.setDisplay(Style.Display.NONE);
                    cntStyle.setTop(m_top, Unit.PX);
                    cntStyle.clearOpacity();
                    super.onComplete();
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
                 */
                @Override
                protected void onUpdate(double progress) {

                    super.onUpdate(progress);
                    Style cntStyle = m_internalWidget.getElement().getStyle();
                    cntStyle.setTop(m_top - progress * m_height, Unit.PX);
                    cntStyle.setOpacity(1 - progress);
                }
            };
        }
        return m_hideAnimation;
    }

    /**
     * Returns the showAnimation.<p>
     *
     * @return the showAnimation
     */
    public NotificationAnimation getShowAnimation() {

        if (m_showAnimation == null) {
            m_showAnimation = new NotificationAnimation() {

                /** The height. */
                private int m_height;

                /** The widget. */
                private CmsNotificationWidget m_internalWidget;

                /** The initial top position. */
                private int m_top;

                /**
                 * @see org.opencms.gwt.client.ui.CmsNotification.NotificationAnimation#run(org.opencms.gwt.client.ui.CmsNotification.CmsNotificationWidget)
                 */
                @Override
                public void run(CmsNotificationWidget widget) {

                    m_internalWidget = widget;
                    Element cntElement = m_internalWidget.getElement();
                    m_height = CmsDomUtil.getCurrentStyleInt(cntElement, CmsDomUtil.Style.height);
                    m_top = CmsDomUtil.getCurrentStyleInt(cntElement, CmsDomUtil.Style.top);
                    CmsDebugLog.getInstance().printLine("setHeight: " + m_height);
                    cntElement.getStyle().setTop(m_top - m_height, Unit.PX);
                    m_internalWidget.getMessage().getStyle().clearVisibility();
                    run(ANIMATION_DURATION);
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onComplete()
                 */
                @Override
                protected void onComplete() {

                    CmsDebugLog.getInstance().printLine("complete");
                    Style cntStyle = m_internalWidget.getElement().getStyle();
                    cntStyle.setTop(m_top, Unit.PX);
                    cntStyle.clearOpacity();
                    CmsNotification.get().onShowComplete();
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
                 */
                @Override
                protected void onUpdate(double progress) {

                    super.onUpdate(progress);
                    double top = m_top + (-1 + progress) * m_height;
                    Style cntStyle = m_internalWidget.getElement().getStyle();
                    cntStyle.setTop(top, Unit.PX);
                    cntStyle.setOpacity(progress);
                }
            };
        }
        return m_showAnimation;
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

        hide(false);
    }

    /**
     * Sends a new notification.<p>
     * 
     * @param type the notification type
     * @param message the message
     */
    public void send(Type type, String message) {

        final Element cntElement = m_widget.getElement();
        if (m_mode == Mode.FIXED) {
            // do not overwrite a higher or equal level notification
            switch (type) {
                case ERROR:
                    if (CmsDomUtil.hasClass(classForType(Type.ERROR), cntElement)) {
                        return;
                    }
                    break;
                case NORMAL:
                    if (CmsDomUtil.hasClass(classForType(Type.ERROR), cntElement)
                        || CmsDomUtil.hasClass(classForType(Type.WARNING), cntElement)
                        || CmsDomUtil.hasClass(classForType(Type.NORMAL), cntElement)) {
                        return;
                    }
                    break;
                case WARNING:
                    if (CmsDomUtil.hasClass(classForType(Type.ERROR), cntElement)
                        || CmsDomUtil.hasClass(classForType(Type.WARNING), cntElement)) {
                        return;
                    }
                    break;
                default:
            }
        }

        // remove last notification if still shown
        hide(true);

        // create new notification
        m_widget.addStyleName(classForType(type));
        m_widget.getMessage().setInnerHTML(message);
        // hide content and display 
        m_widget.getMessage().getStyle().setVisibility(Visibility.HIDDEN);
        m_widget.setVisible(true);
        // wait for correct layout
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                m_animation = getShowAnimation();
                m_animation.run(getWidget());
                CmsDebugLog.getInstance().printLine("show: " + cntElement.getInnerText());
            }
        });

        if (m_mode != Mode.FIXED) {
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

    /**
     * Sets the hideAnimation.<p>
     *
     * @param hideAnimation the hideAnimation to set
     */
    public void setHideAnimation(NotificationAnimation hideAnimation) {

        m_hideAnimation = hideAnimation;
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
     * Sets the showAnimation.<p>
     *
     * @param showAnimation the showAnimation to set
     */
    public void setShowAnimation(NotificationAnimation showAnimation) {

        m_showAnimation = showAnimation;
    }

    /**
     * Should be called by the hide animation on complete.<p>
     */
    protected void onHideComplete() {

        m_animation = null;
        m_widget.setVisible(false);
        // back to plain style without error or warning
        m_widget.setStyleName(I_CmsLayoutBundle.INSTANCE.notificationCss().container());
        // debug
        CmsDebugLog.getInstance().printLine("hide: " + m_widget.getMessage().getInnerText());
    }

    /**
     * Should be called by the show animation on complete.<p>
     */
    protected void onShowComplete() {

        m_animation = null;
    }

    /**
     * Returns the class name for the given type.<p>
     * 
     * @param type the type
     * 
     * @return the class name
     */
    private String classForType(Type type) {

        switch (type) {
            case ERROR:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().error();
            case NORMAL:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().normal();
            case WARNING:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().warning();
            default:
                return null;
        }
    }

    /**
     * Hides the notification message.<p>
     * 
     * @param force if <code>true</code> will also hide the message if mode is fixed
     */
    private void hide(boolean force) {

        // remove last notification if still shown
        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
        if (!m_widget.isVisible()) {
            return;
        }
        if ((m_mode == Mode.FIXED) && !force) {
            return;
        }
        if (m_animation != null) {
            m_animation.cancel();
        }
        if (!force) {
            m_animation = getHideAnimation();
            m_animation.run(m_widget);
        } else {
            onHideComplete();
        }
    }
}
