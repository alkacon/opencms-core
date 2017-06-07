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

import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsSlideAnimation;
import org.opencms.gwt.client.util.CmsStyleVariable;

import java.util.Iterator;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that behaves like a HTML fieldset.<p>
 *
 * @since 8.0.0
 */
public class CmsFieldSet extends Composite
implements HasOpenHandlers<CmsFieldSet>, HasCloseHandlers<CmsFieldSet>, HasWidgets, I_CmsTruncable {

    /** The ui-binder interface for this composite. */
    protected interface I_CmsFieldSetUiBinder extends UiBinder<Widget, CmsFieldSet> {
        // GWT interface, nothing to do here
    }

    /** Default animation duration.*/
    public static final int DEFAULT_ANIMATION_DURATION = 300;

    /** The ui-binder instance. */
    private static I_CmsFieldSetUiBinder uiBinder = GWT.create(I_CmsFieldSetUiBinder.class);

    /** The content of the fieldset. */
    @UiField
    protected FlowPanel m_content;

    /** The wrapping panel for this fieldset. */
    @UiField
    protected FlowPanel m_fieldset;

    /** The image for the top and bottom arrow. */
    @UiField
    protected CmsPushButton m_opener;

    /** The legend of the fieldset. */
    @UiField
    protected CmsLabel m_legend;

    /** Signals whether the fieldset is opened. */
    protected boolean m_opened;

    /** The legend of the fieldset. */
    @UiField
    protected FlowPanel m_wrapper;

    /** The running slide in/out animation. */
    private Animation m_animation;

    /** The animation duration. */
    private int m_animationDuration = DEFAULT_ANIMATION_DURATION;

    /** The fieldset visibility style. */
    private CmsStyleVariable m_visibilityStyle;

    /**
     * Default constructor.<p>
     *
     * Because this class has a default constructor, it can
     * be used as a binder template. In other words, it can be used in other
     * *.ui.xml files:
     */
    public CmsFieldSet() {

        initWidget(uiBinder.createAndBindUi(this));
        m_opener.setImageClass(I_CmsButton.ICON_FONT + " " + I_CmsButton.TRIANGLE_RIGHT);
        m_opener.setDownImageClass(I_CmsButton.ICON_FONT + " " + I_CmsButton.TRIANGLE_DOWN);
        m_opener.setButtonStyle(ButtonStyle.TEXT, ButtonColor.GRAY);
        m_opener.setSize(Size.small);
        m_visibilityStyle = new CmsStyleVariable(m_fieldset);
        setOpen(true);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget widget) {

        m_content.add(widget);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasCloseHandlers#addCloseHandler(com.google.gwt.event.logical.shared.CloseHandler)
     */
    public HandlerRegistration addCloseHandler(CloseHandler<CmsFieldSet> handler) {

        return addHandler(handler, CloseEvent.getType());
    }

    /**
     * Adds a widget to this field set.<p>
     *
     * @param w the widget to add
     */
    public void addContent(Widget w) {

        m_content.add(w);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasOpenHandlers#addOpenHandler(com.google.gwt.event.logical.shared.OpenHandler)
     */
    public HandlerRegistration addOpenHandler(OpenHandler<CmsFieldSet> handler) {

        return addHandler(handler, OpenEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#clear()
     */
    public void clear() {

        m_content.clear();
    }

    /**
     * Returns the content panel.<p>
     *
     * @return the content panel
     */
    public FlowPanel getContentPanel() {

        return m_content;
    }

    /**
     * Returns the count of widgets inside this fieldset.<p>
     *
     * @return the count of widgets inside this fieldset
     */
    public int getWidgetCount() {

        return m_content.getWidgetCount();
    }

    /**
     * Returns the wrapper.<p>
     *
     * @return the wrapper
     */
    public FlowPanel getWrapper() {

        return m_wrapper;
    }

    /**
     * Returns if the fieldset is opened.<p>
     *
     * @return <code>true</code> if the fieldset is opened
     */
    public boolean isOpen() {

        return m_opened;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    public Iterator<Widget> iterator() {

        return m_content.iterator();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
     */
    public boolean remove(Widget widget) {

        return m_content.remove(widget);
    }

    /**
     * Sets the animation duration.
     * @param animDuration the animation duration
     */
    public void setAnimationDuration(int animDuration) {

        m_animationDuration = animDuration;
    }

    /**
     * Sets the text for the legend of this field set.<p>
     *
     * @param legendText the legend text
     */
    public void setLegend(String legendText) {

        m_legend.setText(legendText);
    }

    /**
     * Sets the fieldset open, showing the content.<p>
     *
     * @param open <code>true</code> to open the fieldset
     */
    public void setOpen(boolean open) {

        m_opened = open;
        if (m_opened) {
            // show content
            m_visibilityStyle.setValue(
                I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()
                    + " "
                    + I_CmsLayoutBundle.INSTANCE.fieldsetCss().fieldsetVisible());
            m_opener.setDown(true);
        } else {
            // hide content
            m_visibilityStyle.setValue(I_CmsLayoutBundle.INSTANCE.fieldsetCss().fieldsetInvisible());
            m_opener.setDown(false);
        }
        CmsDomUtil.resizeAncestor(getParent());
    }

    /**
     * Sets the opener visible.<p>
     *
     * @param visible <code>true</code> to set the opener visible
     */
    public void setOpenerVisible(boolean visible) {

        if (visible) {
            m_opener.getElement().getStyle().clearDisplay();
        } else {
            m_opener.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        int availableWidth = clientWidth - 12;
        for (Widget child : m_content) {
            if (child instanceof I_CmsTruncable) {
                ((I_CmsTruncable)child).truncate(textMetricsKey, availableWidth);
            }
        }
    }

    /**
     * Adds a click handler to the image icon of this fieldset.<p>
     *
     * On click the
     *
     * @param e the event
     */
    @UiHandler("m_opener")
    protected void handleClick(ClickEvent e) {

        if (m_animation != null) {
            m_animation.cancel();
        }
        if (!m_opened) {

            // show content
            setOpen(true);

            m_animation = CmsSlideAnimation.slideIn(m_content.getElement(), new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    OpenEvent.fire(CmsFieldSet.this, CmsFieldSet.this);
                    CmsDomUtil.resizeAncestor(getParent());
                }
            }, m_animationDuration);
        } else {

            // hide content
            m_animation = CmsSlideAnimation.slideOut(m_content.getElement(), new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    setOpen(false);
                    CloseEvent.fire(CmsFieldSet.this, CmsFieldSet.this);
                    CmsDomUtil.resizeAncestor(getParent());
                }
            }, m_animationDuration);
        }
    }
}
