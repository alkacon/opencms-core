/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsListCollectorEditor.java,v $
 * Date   : $Date: 2011/03/22 13:55:08 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsEditableDataJSO;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
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
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Class to provide direct edit buttons within list collector elements.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsListCollectorEditor extends FlowPanel implements HasMouseOverHandlers, HasMouseOutHandlers {

    /**
     * Button handler for this class.<p>
     */
    private class MouseHandler implements MouseOverHandler, MouseOutHandler, ClickHandler {

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
                removeHighlighting();
                CmsConfirmDialog dialog = new CmsConfirmDialog(
                    "Deleting Resource",
                    "You are about to delete a resource from the VFS. Are you sure you want to do that?");
                dialog.setHandler(new I_CmsConfirmDialogHandler() {

                    public void onClose() {

                        // nothing to do
                    }

                    public void onOk() {

                        deleteElement();

                    }
                });
                CmsDomUtil.ensureMouseOut(m_delete.getElement());
                CmsDomUtil.ensureMouseOut(getElement());
                dialog.center();
            }
            if (source == m_edit) {
                openEditDialog(false);
                removeHighlighting();
            }
            if (source == m_new) {
                openEditDialog(true);
                removeHighlighting();
            }

        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        public void onMouseOut(MouseOutEvent event) {

            getElement().removeClassName(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
            removeHighlighting();
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        public void onMouseOver(MouseOverEvent event) {

            getElement().addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
            highlightElement();
        }

    }

    /** The delete button. */
    protected CmsPushButton m_delete;

    /** The edit button. */
    protected CmsPushButton m_edit;

    /** Highlighting border for this element. */
    protected CmsHighlightingBorder m_highlighting;

    /** The new button. */
    protected CmsPushButton m_new;

    /** The editable data. */
    private CmsEditableDataJSO m_editableData;

    /** The editable marker tag. */
    private Element m_markerTag;

    /** The parent element id. */
    private String m_parentResourceId;

    /** The editable element position. */
    private CmsPositionBean m_position;

    /**
     * Constructor.<p>
     * 
     * @param editable the editable marker tag
     * @param parentId the parent element id
     */
    public CmsListCollectorEditor(Element editable, String parentId) {

        try {
            setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().listCollectorEditor());
            addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().optionBar());
            addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
            m_markerTag = editable;
            m_parentResourceId = parentId;

            String jsonText = editable.getAttribute("rel");
            m_editableData = CmsEditableDataJSO.parseEditableData(jsonText);

            MouseHandler handler = new MouseHandler();
            addMouseOutHandler(handler);
            addMouseOverHandler(handler);
            if (m_editableData.hasNew()) {
                m_new = new CmsPushButton();
                m_new.setImageClass(I_CmsButton.ButtonData.NEW.getIconClass());
                m_new.addStyleName(I_CmsButton.ButtonData.NEW.getIconClass());
                m_new.setTitle(I_CmsButton.ButtonData.NEW.getTitle());
                m_new.setShowBorder(false);
                add(m_new);
                m_new.addClickHandler(handler);
            }
            if (m_editableData.hasEdit()) {
                m_edit = new CmsPushButton();
                m_edit.setImageClass(I_CmsButton.ButtonData.EDIT.getIconClass());
                m_edit.addStyleName(I_CmsButton.ButtonData.EDIT.getIconClass());
                m_edit.setTitle(I_CmsButton.ButtonData.EDIT.getTitle());
                m_edit.setShowBorder(false);
                add(m_edit);
                m_edit.addClickHandler(handler);
            }
            if (m_editableData.hasDelete()) {
                m_delete = new CmsPushButton();
                m_delete.setImageClass(I_CmsButton.ButtonData.DELETE.getIconClass());
                m_delete.addStyleName(I_CmsButton.ButtonData.DELETE.getIconClass());
                m_delete.setTitle(I_CmsButton.ButtonData.DELETE.getTitle());
                m_delete.setShowBorder(false);
                add(m_delete);
                m_delete.addClickHandler(handler);
            }
            if (this.getWidgetCount() > 0) {
                CmsPushButton selection = new CmsPushButton();
                selection.setImageClass(I_CmsButton.ButtonData.SELECTION.getIconClass());
                selection.addStyleName(I_CmsButton.ButtonData.SELECTION.getIconClass());
                selection.setTitle(I_CmsButton.ButtonData.SELECTION.getTitle());
                selection.setShowBorder(false);
                insert(selection, 0);
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
    public void setPosition(CmsPositionBean position, CmsContainerPageElement containerElement) {

        m_position = position;
        Element parent = CmsDomUtil.getPositioningParent(getElement());
        if ((parent == null) || !containerElement.getElement().isOrHasChild(parent)) {
            parent = containerElement.getElement();
        }
        Style style = getElement().getStyle();
        style.setRight(
            parent.getOffsetWidth() - (m_position.getLeft() + m_position.getWidth() - parent.getAbsoluteLeft()),
            Unit.PX);
        int top = m_position.getTop() - parent.getAbsoluteTop();
        if (top < 25) {
            // if top is <25 the buttons might overlap with the option bar, so increase to 25
            top = 25;
        }
        style.setTop(top, Unit.PX);
    }

    /**
     * Delete the editable element from page and VFS.<p>
     */
    protected void deleteElement() {

        CmsContainerpageController.get().deleteElement(m_editableData.getStructureId(), m_parentResourceId);
    }

    /**
     * Opens the content editor.<p>
     * 
     * @param isNew <code>true</code> to create and edit a new resource
     */
    protected void openEditDialog(boolean isNew) {

        if (isNew) {
            CmsContainerpageController.get().getContentEditorHandler().openDialog(
                m_parentResourceId,
                m_editableData.getSitePath() + "&amp;newlink=" + URL.encodeQueryString(m_editableData.getNewLink()),
                true);
        } else {
            CmsContainerpageController.get().getContentEditorHandler().openDialog(
                m_editableData.getStructureId(),
                m_editableData.getSitePath(),
                false,
                m_parentResourceId);
        }
    }
}
