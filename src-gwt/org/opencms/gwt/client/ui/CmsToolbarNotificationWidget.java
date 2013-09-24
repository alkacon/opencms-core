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

import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;
import org.opencms.gwt.client.util.CmsFadeAnimation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

/**
 * The toolbar notification widget.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolbarNotificationWidget extends A_CmsNotificationWidget {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsNotificationWidgetUiBinder extends UiBinder<Widget, CmsToolbarNotificationWidget> {
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
    public CmsToolbarNotificationWidget() {

        initWidget(uiBinder.createAndBindUi(this));
        restore();
        CmsNotification.get().setWidget(this);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsNotificationWidget#setBlocking()
     */
    public void setBlocking() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.notificationCss().blocking());
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsNotificationWidget#animateHide()
     */
    @Override
    protected void animateHide() {

        setAnimation(CmsFadeAnimation.fadeOut(getElement(), new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                onHideComplete();
            }
        }, 200));
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsNotificationWidget#animateShow()
     */
    @Override
    protected void animateShow() {

        // ensure to display the notification above everything else
        Element parent = getElement().getParentElement();
        if (parent != null) {
            int parentZIndex = CmsDomUtil.getCurrentStyleInt(parent, Style.zIndex);
            parent.getStyle().setZIndex(parentZIndex * 2);
        }
        getElement().getStyle().clearVisibility();
        setAnimation(CmsFadeAnimation.fadeIn(getElement(), new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                onShowComplete();
            }
        }, 200));
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsNotificationWidget#getMessage()
     */
    @Override
    protected String getMessage() {

        return m_message.getInnerHTML();
    }

    /**
     * Should be called by the hide animation on complete.<p>
     */
    @Override
    protected void onHideComplete() {

        setAnimation(null);
        restore();
    }

    /**
     * Should be called by the show animation on complete.<p>
     */
    @Override
    protected void onShowComplete() {

        setAnimation(null);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsNotificationWidget#setClassForType(org.opencms.gwt.client.ui.CmsNotification.Type)
     */
    @Override
    protected void setClassForType(Type type) {

        if (type == null) {
            return;
        }
        // set the right class
        getElement().addClassName(classForType(type));
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsNotificationWidget#setMessage(java.lang.String)
     */
    @Override
    protected void setMessage(String message) {

        m_message.setInnerHTML(message);
    }

    /**
     * Returns the class name for the given type.<p>
     * 
     * @param type the type
     * 
     * @return the class name
     */
    private String classForType(Type type) {

        if (type == null) {
            return null;
        }
        switch (type) {
            case ERROR:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationError();
            case NORMAL:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationNormal();
            case WARNING:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationWarning();
            default:
                return null;
        }
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
        // back to plain style without error or warning
        getElement().setClassName(I_CmsLayoutBundle.INSTANCE.toolbarCss().notification());
    }
}