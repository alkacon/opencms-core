/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsContainerPageElement.java,v $
 * Date   : $Date: 2011/04/06 10:11:13 $
 * Version: $Revision: 1.10 $
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

import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Tag;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Content element within a container-page.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageElement extends AbsolutePanel implements I_CmsDraggable {

    /** Highlighting border for this element. */
    protected CmsHighlightingBorder m_highlighting;

    /** The elements client id. */
    private String m_clientId;

    private List<CmsListCollectorEditor> m_editables;

    /** The option bar, holding optional function buttons. */
    private CmsElementOptionBar m_elementOptionBar;

    /** Indicates whether this element has properties to edit. */
    private boolean m_hasProperties;

    /** The is new element type. */
    private String m_newType;

    /** The no edit reason, if empty editing is allowed. */
    private String m_noEditReason;

    /** The parent drop target. */
    private I_CmsDropContainer m_parent;

    /** The element resource site-path. */
    private String m_sitePath;

    /**
     * Constructor.<p>
     * 
     * @param element the DOM element
     * @param parent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason, if empty, editing is allowed
     * @param hasProps should be true if the element has properties which can be edited 
     */
    public CmsContainerPageElement(
        Element element,
        I_CmsDropContainer parent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps) {

        super((com.google.gwt.user.client.Element)element);
        m_clientId = clientId;
        m_sitePath = sitePath;
        m_noEditReason = noEditReason;
        m_hasProperties = hasProps;
        m_parent = parent;
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getDragHelper(I_CmsDropTarget target) {

        Element helper = CmsDomUtil.clone(getElement());
        target.getElement().appendChild(helper);
        // preparing helper styles
        String width = CmsDomUtil.getCurrentStyle(helper, CmsDomUtil.Style.width);
        Style style = helper.getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setMargin(0, Unit.PX);
        style.setProperty(CmsDomUtil.Style.width.name(), width);
        style.setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexDND());
        helper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        helper.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        if (!CmsDomUtil.hasBackground(helper)) {
            helper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBackground());
        }

        if (!CmsDomUtil.hasBorder(helper)) {
            helper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        }
        return helper;
    }

    /**
     * Returns the option bar of this element.<p>
     * 
     * @return the option bar widget
     */
    public CmsElementOptionBar getElementOptionBar() {

        return m_elementOptionBar;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getId()
     */
    public String getId() {

        return m_clientId;
    }

    /**
     * Returns the new element type.
     * 
     * @return the new element type
     */
    public String getNewType() {

        return m_newType;
    }

    /**
     * Returns the no edit reason.<p>
     *
     * @return the no edit reason
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getParentTarget()
     */
    public I_CmsDropContainer getParentTarget() {

        return m_parent;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getPlaceholder(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getPlaceholder(I_CmsDropTarget target) {

        Element placeholder = CmsDomUtil.clone(getElement());
        placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        return placeholder;
    }

    /**
     * Returns the site-path.<p>
     *
     * @return the site-path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns true if the element has properties to edit.<p>
     * 
     * @return true if the element has properties to edit 
     */
    public boolean hasProperties() {

        return m_hasProperties;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#hasTag(java.lang.String)
     */
    public boolean hasTag(String tag) {

        return false;
    }

    /**
     * Hides list collector direct edit buttons, if present.<p>
     */
    public void hideEditableListButtons() {

        if (m_editables == null) {
            for (CmsListCollectorEditor editor : m_editables) {
                editor.getElement().getStyle().setDisplay(Display.NONE);
            }
        }
    }

    /**
     * Puts a highlighting border around the element.<p>
     */
    public void highlightElement() {

        if (m_highlighting == null) {
            m_highlighting = new CmsHighlightingBorder(CmsPositionBean.generatePositionInfo(this), isNew()
            ? CmsHighlightingBorder.BorderColor.blue
            : CmsHighlightingBorder.BorderColor.red);
            RootPanel.get().add(m_highlighting);
        } else {
            m_highlighting.setPosition(CmsPositionBean.generatePositionInfo(this));
        }
    }

    /**
     * Returns if this is e newly created element.<p>
     * 
     * @return <code>true</code> if the element is new
     */
    public boolean isNew() {

        return m_newType != null;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    public void onDragCancel() {

        removeAttach();
        clearDrag();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void onDrop(I_CmsDropTarget target) {

        clearDrag();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void onStartDrag(I_CmsDropTarget target) {

        CmsDomUtil.addDisablingOverlay(getElement());
        getElement().getStyle().setOpacity(0.5);
        removeHighlighting();
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
     * Sets the elementOptionBar.<p>
     *
     * @param elementOptionBar the elementOptionBar to set
     */
    public void setElementOptionBar(CmsElementOptionBar elementOptionBar) {

        if ((m_elementOptionBar != null) && (getWidgetIndex(m_elementOptionBar) >= 0)) {
            m_elementOptionBar.removeFromParent();
        }
        add(elementOptionBar);
        m_elementOptionBar = elementOptionBar;
    }

    /**
     * Sets the element id.<p>
     * 
     * @param id the id
     */
    public void setId(String id) {

        m_clientId = id;
    }

    /**
     * Sets the new-type of the element.<p>
     * 
     * @param newType the new-type
     */
    public void setNewType(String newType) {

        m_newType = newType;
    }

    /**
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason to set
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Shows list collector direct edit buttons (old direct edit style), if present.<p>
     */
    public void showEditableListButtons() {

        if (m_editables == null) {
            m_editables = new ArrayList<CmsListCollectorEditor>();
            List<Element> editables = CmsDomUtil.getElementsByClass("cms-editable", Tag.div, getElement());
            if ((editables != null) && (editables.size() > 0)) {
                for (Element editable : editables) {
                    try {
                        CmsListCollectorEditor editor = new CmsListCollectorEditor(editable, m_clientId);
                        add(editor);
                        editor.setPosition(getEditablePosition(editable), this);
                        m_editables.add(editor);
                    } catch (UnsupportedOperationException e) {
                        CmsDebugLog.getInstance().printLine(e.getMessage());
                    }
                }
            }
        } else {
            for (CmsListCollectorEditor editor : m_editables) {
                editor.getElement().getStyle().clearDisplay();
                editor.setPosition(getEditablePosition(editor.getMarkerTag()), this);
            }
        }
    }

    /**
     * Removes all styling done during drag and drop.<p>
     */
    private void clearDrag() {

        CmsDomUtil.removeDisablingOverlay(getElement());
        m_elementOptionBar.getElement().removeClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        // using own implementation as GWT won't do it properly on IE7-8
        CmsDomUtil.clearOpacity(getElement());
        getElement().getStyle().clearDisplay();
        showEditableListButtons();
    }

    /**
     * Determines the position of the list collector editable content.<p> 
     * 
     * @param editable the editable marker tag
     * 
     * @return the position
     */
    private CmsPositionBean getEditablePosition(Element editable) {

        CmsPositionBean result = new CmsPositionBean();
        int dummy = -999;
        // setting minimum height
        result.setHeight(20);
        result.setWidth(60);
        result.setLeft(dummy);
        result.setTop(dummy);
        Element sibling = editable.getNextSiblingElement();
        while ((sibling != null)
            && !CmsDomUtil.hasClass("cms-editable", sibling)
            && !CmsDomUtil.hasClass("cms-editable-end", sibling)) {
            CmsPositionBean siblingPos = CmsPositionBean.generatePositionInfo(sibling);
            result.setLeft(((result.getLeft() == dummy) || (siblingPos.getLeft() < result.getLeft()))
            ? siblingPos.getLeft()
            : result.getLeft());
            result.setTop(((result.getTop() == dummy) || (siblingPos.getTop() < result.getTop()))
            ? siblingPos.getTop()
            : result.getTop());
            result.setHeight((result.getTop() + result.getHeight() > siblingPos.getTop() + siblingPos.getHeight())
            ? result.getHeight()
            : siblingPos.getTop() + siblingPos.getHeight() - result.getTop());
            result.setWidth((result.getLeft() + result.getWidth() > siblingPos.getLeft() + siblingPos.getWidth())
            ? result.getWidth()
            : siblingPos.getLeft() + siblingPos.getWidth() - result.getLeft());
            sibling = sibling.getNextSiblingElement();
        }

        if (result.getHeight() == -1) {
            // in case no height was set
            result = CmsPositionBean.generatePositionInfo(editable);
            result.setHeight(20);
            result.setWidth((result.getWidth() < 60) ? 60 : result.getWidth());
        }

        return result;
    }

    /**
     * This method removes the widget from DOM and re-attaches it at it's original position.<p>
     * Use to avoid mouse-over and mouse-down malfunction.<p> 
     */
    private void removeAttach() {

        int index = m_parent.getWidgetIndex(this);
        this.removeFromParent();
        if (m_parent.getWidgetCount() > index) {
            m_parent.insert(this, index);
        } else {
            m_parent.add(this);
        }
    }
}
