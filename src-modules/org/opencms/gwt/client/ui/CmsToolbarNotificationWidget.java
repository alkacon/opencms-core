/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsToolbarNotificationWidget.java,v $
 * Date   : $Date: 2010/04/29 07:13:40 $
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

import org.opencms.gwt.client.ui.CmsNotification.Mode;
import org.opencms.gwt.client.ui.CmsNotification.NotificationAnimation;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The toolbar notification widget.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarNotificationWidget extends Composite implements I_CmsNotificationWidget {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsNotificationWidgetUiBinder extends UiBinder<Widget, CmsToolbarNotificationWidget> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsNotificationWidgetUiBinder uiBinder = GWT.create(I_CmsNotificationWidgetUiBinder.class);

    /** The current animation. */
    protected NotificationAnimation m_animation;

    /** The message. */
    @UiField
    protected Element m_message;

    /** The hide animation. */
    private NotificationAnimation m_hideAnimation;

    /** The current mode. */
    private Mode m_mode;

    /** The show animation. */
    private NotificationAnimation m_showAnimation;

    /** The timer to remove the last notification. */
    private Timer m_timer;

    /**
     * Constructor.<p>
     */
    public CmsToolbarNotificationWidget() {

        initWidget(uiBinder.createAndBindUi(this));
        restore();
        CmsNotification.get().setWidget(this);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsNotificationWidget#hide()
     */
    public void hide() {

        hide(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsNotificationWidget#show(org.opencms.gwt.client.ui.CmsNotification.Mode, org.opencms.gwt.client.ui.CmsNotification.Type, java.lang.String)
     */
    public void show(Mode mode, Type type, String message) {

        if (!canBeReplacedBy(mode, type)) {
            return;
        }

        // remove last notification if still shown
        hide(true);

        getElement().addClassName(classForType(type));
        m_message.setInnerHTML(message);
        m_mode = mode;

        // hide content and display 
        getElement().getStyle().setVisibility(Visibility.HIDDEN);
        setVisible(true);

        // wait for correct layout
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                m_animation = getShowAnimation();
                m_animation.run();
                CmsDebugLog.getInstance().printLine("show: " + m_message.getInnerText());
            }
        });

        if (mode != Mode.FIXED) {
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
     * Returns the showAnimation.<p>
     *
     * @return the showAnimation
     */
    protected NotificationAnimation getShowAnimation() {

        if (m_showAnimation == null) {
            m_showAnimation = new NotificationAnimation() {

                /** The height. */
                private int m_height;

                /** The initial top position. */
                private int m_top;

                /**
                 * @see org.opencms.gwt.client.ui.CmsNotification.NotificationAnimation#run()
                 */
                @Override
                public void run() {

                    Element cntElement = getElement();
                    m_height = CmsDomUtil.getCurrentStyleInt(cntElement, CmsDomUtil.Style.height);
                    m_top = CmsDomUtil.getCurrentStyleInt(cntElement, CmsDomUtil.Style.top);
                    CmsDebugLog.getInstance().printLine("setHeight: " + m_height);
                    cntElement.getStyle().setTop(m_top - m_height, Unit.PX);
                    cntElement.getStyle().clearVisibility();
                    run(ANIMATION_DURATION);
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onComplete()
                 */
                @Override
                protected void onComplete() {

                    super.onComplete();
                    Style cntStyle = getElement().getStyle();
                    cntStyle.setTop(m_top, Unit.PX);
                    cntStyle.clearOpacity();
                    onShowComplete();
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
                 */
                @Override
                protected void onUpdate(double progress) {

                    super.onUpdate(progress);
                    double top = m_top + (-1 + progress) * m_height;
                    Style cntStyle = getElement().getStyle();
                    cntStyle.setTop(top, Unit.PX);
                    cntStyle.setOpacity(progress);
                }
            };
        }
        return m_showAnimation;
    }

    /**
     * Should be called by the hide animation on complete.<p>
     */
    protected void onHideComplete() {

        m_animation = null;
        restore();
        // debug
        CmsDebugLog.getInstance().printLine("hide: " + m_message.getInnerText());
    }

    /**
     * Should be called by the show animation on complete.<p>
     */
    protected void onShowComplete() {

        m_animation = null;
    }

    /**
     * Checks if this widget can be replaced in the given mode by the given type.<p>
     * 
     * @param mode the mode
     * @param type the type
     * 
     * @return <code>true</code> if it can be replaced
     */
    private boolean canBeReplacedBy(Mode mode, Type type) {

        if (mode == Mode.FIXED) {
            // do not overwrite a higher or equal level notification
            Element element = getElement();
            switch (type) {
                case ERROR:
                    if (CmsDomUtil.hasClass(classForType(Type.ERROR), element)) {
                        return false;
                    }
                    break;
                case NORMAL:
                    if (CmsDomUtil.hasClass(classForType(Type.ERROR), element)
                        || CmsDomUtil.hasClass(classForType(Type.WARNING), element)
                        || CmsDomUtil.hasClass(classForType(Type.NORMAL), element)) {
                        return false;
                    }
                    break;
                case WARNING:
                    if (CmsDomUtil.hasClass(classForType(Type.ERROR), element)
                        || CmsDomUtil.hasClass(classForType(Type.WARNING), element)) {
                        return false;
                    }
                    break;
                default:
            }
        }
        return true;
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
                return I_CmsLayoutBundle.INSTANCE.toolbarCss().notificationError();
            case NORMAL:
                return I_CmsLayoutBundle.INSTANCE.toolbarCss().notificationNormal();
            case WARNING:
                return I_CmsLayoutBundle.INSTANCE.toolbarCss().notificationWarning();
            default:
                return null;
        }
    }

    /**
     * Returns the hideAnimation.<p>
     *
     * @return the hideAnimation
     */
    private NotificationAnimation getHideAnimation() {

        if (m_hideAnimation == null) {
            m_hideAnimation = new NotificationAnimation() {

                /** The height. */
                private int m_height;

                /** The initial top position. */
                private int m_top;

                /**
                 * @see org.opencms.gwt.client.ui.CmsNotification.NotificationAnimation#run()
                 */
                @Override
                public void run() {

                    Element cntElement = getElement();
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

                    super.onComplete();
                    Style cntStyle = getElement().getStyle();
                    cntStyle.setDisplay(Style.Display.NONE);
                    cntStyle.setTop(m_top, Unit.PX);
                    cntStyle.clearOpacity();
                    onHideComplete();
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
                 */
                @Override
                protected void onUpdate(double progress) {

                    super.onUpdate(progress);
                    Style cntStyle = getElement().getStyle();
                    cntStyle.setTop(m_top - progress * m_height, Unit.PX);
                    cntStyle.setOpacity(1 - progress);
                }
            };
        }
        return m_hideAnimation;
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
        if ((m_mode == Mode.FIXED) && !force) {
            return;
        }
        if (m_animation != null) {
            m_animation.cancel();
        }
        if (!isVisible()) {
            return;
        }
        if (!force) {
            m_animation = getHideAnimation();
            m_animation.run();
        } else {
            onHideComplete();
        }
    }

    /**
     * Restores the initial state.<p>
     */
    private void restore() {

        setVisible(false);
        // back to plain style without error or warning
        getElement().setClassName(I_CmsLayoutBundle.INSTANCE.toolbarCss().notificationContainer());
    }
}