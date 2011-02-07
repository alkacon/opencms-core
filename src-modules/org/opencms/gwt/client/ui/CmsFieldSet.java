/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsFieldSet.java,v $
 * Date   : $Date: 2011/02/07 14:56:03 $
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

import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsSlideAnimation;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that behaves like a HTML fieldset.<p>
 * 
 * @version $Revision: 1.3 $
 * 
 * @author Ruediger Kurz
 * 
 * @since 8.0.0
 */
public class CmsFieldSet extends Composite {

    /** The ui-binder interface for this composite. */
    protected interface I_CmsFieldSetUiBinder extends UiBinder<Widget, CmsFieldSet> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance. */
    private static I_CmsFieldSetUiBinder uiBinder = GWT.create(I_CmsFieldSetUiBinder.class);

    /** Signals whether the fieldset is collapsed or expanded. */
    protected boolean m_collapsed;

    /** The content of the fieldset. */
    @UiField
    protected FlowPanel m_content;

    /** The wrapping panel for this fieldset. */
    @UiField
    protected FlowPanel m_fieldset;

    /** The image for the top and bottom arrow. */
    @UiField
    protected Image m_image;

    /** The legend of the fieldset. */
    @UiField
    protected CmsLabel m_legend;

    /** The legend of the fieldset. */
    @UiField
    protected FlowPanel m_wrapper;

    private Animation m_animation;

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
        m_visibilityStyle = new CmsStyleVariable(m_fieldset);
        setCollapsed(false);
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
     * Returns the content panel.<p>
     * 
     * @return the content panel
     */
    public FlowPanel getContentPanel() {

        return m_content;
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
     * Sets the fieldset collapsed, hiding the content.<p>
     * 
     * @param collapsed <code>true</code> to collapse the fieldset
     */
    public void setCollapsed(boolean collapsed) {

        m_collapsed = collapsed;
        if (!m_collapsed) {
            // show content
            m_visibilityStyle.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()
                + " "
                + I_CmsLayoutBundle.INSTANCE.fieldsetCss().fieldsetVisible());
            m_image.setResource(I_CmsImageBundle.INSTANCE.arrowBottom());
        } else {
            // hide content
            m_visibilityStyle.setValue(I_CmsLayoutBundle.INSTANCE.fieldsetCss().fieldsetInvisible());
            m_image.setResource(I_CmsImageBundle.INSTANCE.arrowRight());
        }
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
     * Adds a click handler to the image icon of this fieldset.<p>
     * 
     * On click the 
     * 
     * @param e the event
     */
    @UiHandler("m_image")
    protected void handleClick(ClickEvent e) {

        if (m_animation != null) {
            m_animation.cancel();
        }
        if (m_collapsed) {

            // slide in
            setCollapsed(!m_collapsed);

            m_animation = CmsSlideAnimation.slideIn(m_content.getElement(), new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    // nothing to do
                }
            }, 300);
        } else {

            // slide out
            m_animation = CmsSlideAnimation.slideOut(m_content.getElement(), new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    setCollapsed(!m_collapsed);

                }
            }, 300);
        }
    }
}
