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

import org.opencms.gwt.client.CmsEditableDataJSO;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Class to provide direct edit buttons.<p>
 * 
 * @since 8.0.0
 */
public abstract class A_CmsDirectEditButtons extends FlowPanel implements HasMouseOverHandlers, HasMouseOutHandlers {

    /**
     * Button handler for this class.<p>
     */
    private class MouseHandler extends A_CmsHoverHandler implements ClickHandler {

        /**
         * Constructor.<p>
         */
        protected MouseHandler() {

            // nothing to do
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            Object source = event.getSource();
            if (source == m_delete) {
                onClickDelete();
            }
            if (source == m_edit) {
                onClickEdit();
            }
            if (source == m_new) {
                onClickNew();
            }
        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        @Override
        public void onHoverIn(MouseOverEvent event) {

            if (activeBar != null) {
                try {
                    activeBar.removeHighlightingAndBar();
                } catch (Throwable t) {
                    // ignore 
                }
            }
            activeBar = null;
            addHighlightingAndBar();
        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        @Override
        public void onHoverOut(MouseOutEvent event) {

            timer = new Timer() {

                @Override
                public void run() {

                    if (timer == this) {
                        removeHighlightingAndBar();
                    }
                }
            };
            timer.schedule(750);
        }

    }

    /** The currently active option bar. */
    /*default */static A_CmsDirectEditButtons activeBar;

    /** The timer used for hiding the option bar. */
    /*default */static Timer timer;

    /** The delete button. */
    protected CmsPushButton m_delete;

    /** The edit button. */
    protected CmsPushButton m_edit;
    /** The editable data. */
    protected CmsEditableDataJSO m_editableData;
    /** Highlighting border for this element. */
    protected CmsHighlightingBorder m_highlighting;

    /** The editable marker tag. */
    protected Element m_markerTag;

    /** The new button. */
    protected CmsPushButton m_new;

    /** The parent element id. */
    protected String m_parentResourceId;

    /** The editable element position. */
    protected CmsPositionBean m_position;

    /**
     * Constructor.<p>
     * 
     * @param editable the editable marker tag
     * @param parentId the parent element id
     */
    public A_CmsDirectEditButtons(Element editable, String parentId) {

        try {
            setStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.directEditCss().directEditButtons());
            addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.directEditCss().optionBar());
            addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
            m_markerTag = editable;
            m_parentResourceId = parentId;

            String jsonText = editable.getAttribute("rel");
            m_editableData = CmsEditableDataJSO.parseEditableData(jsonText);

            MouseHandler handler = new MouseHandler();
            addMouseOutHandler(handler);
            addMouseOverHandler(handler);

            if (m_editableData.hasDelete()) {
                m_delete = new CmsPushButton();
                m_delete.setImageClass(I_CmsButton.ButtonData.DELETE.getIconClass());
                m_delete.addStyleName(I_CmsButton.ButtonData.DELETE.getIconClass());
                m_delete.setTitle(I_CmsButton.ButtonData.DELETE.getTitle());
                m_delete.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                add(m_delete);
                m_delete.addClickHandler(handler);
            }
            if (m_editableData.hasEdit()) {
                m_edit = new CmsPushButton();
                m_edit.setImageClass(I_CmsButton.ButtonData.EDIT.getIconClass());
                m_edit.addStyleName(I_CmsButton.ButtonData.EDIT.getIconClass());
                m_edit.setTitle(I_CmsButton.ButtonData.EDIT.getTitle());
                m_edit.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                add(m_edit);
                m_edit.addClickHandler(handler);
            }
            if (m_editableData.hasNew()) {
                m_new = new CmsPushButton();
                m_new.setImageClass(I_CmsButton.ButtonData.NEW.getIconClass());
                m_new.addStyleName(I_CmsButton.ButtonData.NEW.getIconClass());
                m_new.setTitle(I_CmsButton.ButtonData.NEW.getTitle());
                m_new.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                add(m_new);
                m_new.addClickHandler(handler);
            }
            if (this.getWidgetCount() > 0) {
                CmsPushButton selection = new CmsPushButton();
                selection.setImageClass(I_CmsButton.ButtonData.SELECTION.getIconClass());
                selection.addStyleName(I_CmsButton.ButtonData.SELECTION.getIconClass());
                selection.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                add(selection);
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error while parsing editable tag information: " + e.getMessage());
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());

    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /**
     * Returns the marker tag.<p>
     *
     * @return the marker tag
     */
    public Element getMarkerTag() {

        return m_markerTag;
    }

    /**
     * Puts a highlighting border around the element.<p>
     */
    public void highlightElement() {

        if (m_highlighting == null) {
            m_highlighting = new CmsHighlightingBorder(m_position, CmsHighlightingBorder.BorderColor.red);
            RootPanel.get().add(m_highlighting);
        } else {
            m_highlighting.setPosition(CmsPositionBean.generatePositionInfo(this));
        }
    }

    /**
     * Returns if this edit button is still valid.<p>
     * 
     * @return <code>true</code> if this edit button is valid
     */
    public boolean isValid() {

        return RootPanel.getBodyElement().isOrHasChild(m_markerTag);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#removeFromParent()
     */
    @Override
    public void removeFromParent() {

        removeHighlighting();
        super.removeFromParent();
    }

    /**
     * Removes the highlighting border.<p>
     */
    public void removeHighlighting() {

        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
            m_highlighting = null;
        }
    }

    /**
     * Sets the position. Make sure the widget is attached to the DOM.<p>
     * 
     * @param position the absolute position
     * @param containerElement the parent container element
     */
    public void setPosition(CmsPositionBean position, com.google.gwt.user.client.Element containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());

        Style style = getElement().getStyle();
        style.setRight(parent.getOffsetWidth()
            - ((m_position.getLeft() + m_position.getWidth()) - parent.getAbsoluteLeft()), Unit.PX);
        int top = m_position.getTop() - parent.getAbsoluteTop();
        if (top < 25) {
            // if top is <25 the buttons might overlap with the option bar, so increase to 25
            top = 25;
        }
        style.setTop(top, Unit.PX);
    }

    /**
     * Adds the highlighting and option bar.<p>
     */
    protected void addHighlightingAndBar() {

        timer = null;
        highlightElement();
        getElement().addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        activeBar = this;
    }

    /**
     * This method should be executed when the "delete" direct edit button is clicked.<p>
     */
    protected abstract void onClickDelete();

    /**
     * This method should be executed when the "edit" direct edit button is clicked.<p>
     */
    protected abstract void onClickEdit();

    /**
     * This method should be executed when the "new" direct edit button is clicked.<p>
     */
    protected abstract void onClickNew();

    /**
     * Removes the highlighting and option bar.<p>
     */
    protected void removeHighlightingAndBar() {

        timer = null;
        if (activeBar == this) {
            activeBar = null;
        }
        removeHighlighting();
        getElement().removeClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
    }

}
