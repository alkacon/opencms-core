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

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableDataJSO;
import org.opencms.gwt.client.ui.resourceinfo.CmsResourceInfoDialog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsNewLinkFunctionTable;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.I_CmsUniqueActiveItem;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Maps;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Class to provide direct edit buttons.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsDirectEditButtons extends FlowPanel
implements HasMouseOverHandlers, HasMouseOutHandlers, I_CmsUniqueActiveItem {

    /**
     * Button handler for this  class.<p>
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
                onClickNew(true);
            }
        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        @Override
        public void onHoverIn(MouseOverEvent event) {

            CmsCoreProvider.get().getFlyoutMenuContainer().setActiveItem(A_CmsDirectEditButtons.this);
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

    /** The timer used for hiding the option bar. */
    /*default */static Timer timer;

    /** The delete button. */
    protected CmsPushButton m_delete;

    /** The edit button. */
    protected CmsPushButton m_edit;

    /** The editable data. */
    protected CmsEditableDataJSO m_editableData;

    /** The expired resources overlay element. */
    protected Element m_expiredOverlay;

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
            CmsNewLinkFunctionTable.INSTANCE.setHandler(m_editableData.getContextId(), new Runnable() {

                public void run() {

                    A_CmsDirectEditButtons.this.onClickNew(false);
                }
            });

            MouseHandler handler = new MouseHandler();
            addMouseOutHandler(handler);
            addMouseOverHandler(handler);
            TreeMap<Integer, CmsPushButton> buttonMap = Maps.newTreeMap();

            if (m_editableData.hasDelete() && CmsStringUtil.isEmptyOrWhitespaceOnly(m_editableData.getNoEditReason())) {
                m_delete = new CmsPushButton();
                m_delete.setImageClass(I_CmsButton.ButtonData.DELETE.getIconClass());
                m_delete.addStyleName(I_CmsButton.ButtonData.DELETE.getIconClass());
                m_delete.setTitle(I_CmsButton.ButtonData.DELETE.getTitle());
                m_delete.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                buttonMap.put(Integer.valueOf(100), m_delete);
                m_delete.addClickHandler(handler);
            }
            if (m_editableData.hasNew()) {
                m_new = new CmsPushButton();
                m_new.setImageClass(I_CmsButton.ButtonData.NEW.getIconClass());
                m_new.addStyleName(I_CmsButton.ButtonData.NEW.getIconClass());
                m_new.setTitle(I_CmsButton.ButtonData.NEW.getTitle());
                m_new.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                buttonMap.put(Integer.valueOf(200), m_new);
                m_new.addClickHandler(handler);
            }
            buttonMap.putAll(getAdditionalButtons());
            if ((buttonMap.size() > 0) || m_editableData.hasEdit()) {
                m_edit = new CmsPushButton();
                m_edit.setImageClass(I_CmsButton.ButtonData.SELECTION.getIconClass());
                m_edit.addStyleName(I_CmsButton.ButtonData.SELECTION.getIconClass());
                m_edit.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
                buttonMap.put(Integer.valueOf(300), m_edit);
                if (m_editableData.hasEdit()) {
                    m_edit.addStyleName(I_CmsButton.ButtonData.EDIT.getIconClass());
                    m_edit.setTitle(I_CmsButton.ButtonData.EDIT.getTitle());
                    m_edit.addClickHandler(handler);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_editableData.getNoEditReason())) {
                        m_edit.disable(m_editableData.getNoEditReason());
                    }
                }
            }

            for (CmsPushButton button : buttonMap.values()) {
                add(button);
                button.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent e) {

                        CmsCoreProvider.get().getFlyoutMenuContainer().clearIfMatches(A_CmsDirectEditButtons.this);

                    }
                });
            }

            if (m_editableData.isUnreleasedOrExpired()) {
                m_expiredOverlay = DOM.createDiv();
                m_expiredOverlay.setClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.directEditCss().expiredListElementOverlay());
                m_markerTag.getParentElement().insertBefore(m_expiredOverlay, m_markerTag);
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
     * Creates the button for displaying element information.<p>
     * 
     * @return the created button 
     */
    public CmsPushButton createInfoButton() {

        CmsPushButton favButton = new CmsPushButton();
        favButton.setImageClass(I_CmsButton.ButtonData.INFO.getIconClass());
        favButton.addStyleName(I_CmsButton.ButtonData.INFO.getIconClass());
        favButton.setTitle(I_CmsButton.ButtonData.INFO.getTitle());
        favButton.setButtonStyle(I_CmsButton.ButtonStyle.TRANSPARENT, null);
        add(favButton);
        favButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsResourceInfoDialog.load(
                    m_editableData.getStructureId(),
                    true,
                    Collections.<CmsUUID> emptyList(),
                    null);
            }
        });
        return favButton;
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
            m_highlighting.setPosition(CmsPositionBean.getBoundingClientRect(getElement()));
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
     * @see org.opencms.gwt.client.util.I_CmsUniqueActiveItem#onDeactivate()
     */
    public void onDeactivate() {

        removeHighlightingAndBar();
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
    public void setPosition(CmsPositionBean position, Element containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());

        Style style = getElement().getStyle();
        style.setRight(
            parent.getOffsetWidth() - ((m_position.getLeft() + m_position.getWidth()) - parent.getAbsoluteLeft()),
            Unit.PX);
        int top = m_position.getTop() - parent.getAbsoluteTop();
        if (m_position.getHeight() < 24) {
            // if the highlighted area has a lesser height than the buttons, center vertically
            top -= (24 - m_position.getHeight()) / 2;
        }
        style.setTop(top, Unit.PX);

        updateExpiredOverlayPosition(parent);
    }

    /**
     * Adds the highlighting and option bar.<p>
     */
    protected void addHighlightingAndBar() {

        timer = null;
        highlightElement();
        getElement().addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
    }

    /**
     * Returns a map of additional buttons in a map, with the button position as key (buttons will be ordered by their position).<p>
     *  
     * @return the map of additional buttons
     */
    protected Map<Integer, CmsPushButton> getAdditionalButtons() {

        return Collections.emptyMap();

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
     *
     * @param askCreateMode true if the user should be asked for the 'content create mode'
     */
    protected abstract void onClickNew(boolean askCreateMode);

    /**
     * Removes the highlighting and option bar.<p>
     */
    protected void removeHighlightingAndBar() {

        removeHighlighting();
        getElement().removeClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
    }

    /**
     * Updates the position of the expired resources overlay if present.<p>
     *
     * @param positioningParent the positioning parent element
     */
    protected void updateExpiredOverlayPosition(Element positioningParent) {

        if (m_expiredOverlay != null) {
            Style expiredStyle = m_expiredOverlay.getStyle();
            expiredStyle.setHeight(m_position.getHeight() + 4, Unit.PX);
            expiredStyle.setWidth(m_position.getWidth() + 4, Unit.PX);
            expiredStyle.setTop(m_position.getTop() - positioningParent.getAbsoluteTop() - 2, Unit.PX);
            expiredStyle.setLeft(m_position.getLeft() - positioningParent.getAbsoluteLeft() - 2, Unit.PX);
        }
    }

}
