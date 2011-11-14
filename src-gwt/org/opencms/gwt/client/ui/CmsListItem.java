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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListItemCss;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * List item which uses a float panel for layout.<p>
 *  
 * @since 8.0.0 
 */
public class CmsListItem extends Composite implements I_CmsListItem {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsSimpleListItemUiBinder extends UiBinder<CmsFlowPanel, CmsListItem> {
        // GWT interface, nothing to do here
    }

    /** The move handle. */
    protected class MoveHandle extends CmsPushButton implements I_CmsDragHandle {

        /** The draggable. */
        private CmsListItem m_draggable;

        /**
         * Constructor.<p>
         * 
         * @param draggable the draggable
         */
        MoveHandle(CmsListItem draggable) {

            setImageClass(I_CmsImageBundle.INSTANCE.style().moveIcon());
            setButtonStyle(ButtonStyle.TRANSPARENT, null);
            setTitle(Messages.get().key(Messages.GUI_TOOLBAR_MOVE_TO_0));
            m_draggable = draggable;
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDragHandle#getDraggable()
         */
        public I_CmsDraggable getDraggable() {

            return m_draggable;
        }

    }

    /** The width of a checkbox. */
    private static final int CHECKBOX_WIDTH = 20;

    /** The CSS bundle used for this widget. */
    private static final I_CmsListItemCss CSS = I_CmsLayoutBundle.INSTANCE.listItemCss();

    /** The ui-binder instance for this class. */
    private static I_CmsSimpleListItemUiBinder uiBinder = GWT.create(I_CmsSimpleListItemUiBinder.class);

    /** The checkbox of this list item, or null if there is no checkbox. */
    protected CmsCheckBox m_checkbox;

    /** The panel which contains both the decorations (checkbox, etc.) and the main widget. */
    protected CmsSimpleDecoratedPanel m_decoratedPanel;

    /** A list of decoration widgets which is used to initialize {@link CmsListItem#m_decoratedPanel}. */
    protected LinkedList<Widget> m_decorationWidgets = new LinkedList<Widget>();

    /** The decoration width which should be used to initialize {@link CmsListItem#m_decoratedPanel}. */
    protected int m_decorationWidth;

    /** The logical id, it is not the HTML id. */
    protected String m_id;

    /** The list item widget, if this widget has one. */
    protected CmsListItemWidget m_listItemWidget;

    /** The main widget of the list item. */
    protected Widget m_mainWidget;

    /** This widgets panel. */
    protected CmsFlowPanel m_panel;

    /** The drag'n drop place holder element. */
    protected Element m_placeholder;

    /** The provisional drag parent. */
    protected Element m_provisionalParent;

    /** The drag helper. */
    private Element m_helper;

    /** The move handle. */
    private MoveHandle m_moveHandle;

    /** The list item's set of tags. */
    private Set<String> m_tags;

    /** 
     * Default constructor.<p>
     */
    public CmsListItem() {

        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
    }

    /** 
     * Default constructor.<p>
     * 
     * @param checkBox the checkbox
     * @param widget the widget to use 
     */
    public CmsListItem(CmsCheckBox checkBox, CmsListItemWidget widget) {

        this();
        initContent(checkBox, widget);
    }

    /** 
     * Default constructor.<p>
     * 
     * @param widget the widget to use 
     */
    public CmsListItem(CmsListItemWidget widget) {

        this();
        initContent(widget);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget w) {

        throw new UnsupportedOperationException();
    }

    /**
     * Adds a decoration widget to the list item.<p>
     * 
     * @param widget the widget
     * @param width the widget width
     */
    public void addDecorationWidget(Widget widget, int width) {

        addDecoration(widget, width, false);
        initContent();
    }

    /**
     * Adds a tag to the widget.<p>
     * 
     * @param tag the tag which should be added 
     */
    public void addTag(String tag) {

        if (m_tags == null) {
            m_tags = new HashSet<String>();
        }
        m_tags.add(tag);
    }

    /**
     * Gets the checkbox of this list item.<p>
     * 
     * This method will return a checkbox if this list item has one, or null if it doesn't.
     * 
     * @return a check box or null
     */
    public CmsCheckBox getCheckBox() {

        return m_checkbox;
    }

    /**
     * Returns the decoration widgets of this list item.<p>
     * 
     * @return the decoration widgets
     */
    public List<Widget> getDecorationWidgets() {

        return Collections.unmodifiableList(m_decorationWidgets);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(I_CmsDropTarget)
     */
    public Element getDragHelper(I_CmsDropTarget target) {

        if (m_helper == null) {
            if (m_listItemWidget != null) {
                m_listItemWidget.setAdditionalInfoVisible(false);
                Iterator<Widget> buttonIterator = m_listItemWidget.getButtonPanel().iterator();
                while (buttonIterator.hasNext()) {
                    Widget button = buttonIterator.next();
                    if (button != m_moveHandle) {
                        button.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                    }
                }
            }
            m_helper = CmsDomUtil.clone(getElement());
            // remove all decorations
            List<com.google.gwt.dom.client.Element> elems = CmsDomUtil.getElementsByClass(
                I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss().decorationBox(),
                CmsDomUtil.Tag.div,
                m_helper);
            for (com.google.gwt.dom.client.Element elem : elems) {
                elem.removeFromParent();
            }

            // we append the drag helper to the body to prevent any kind of issues 
            // (ie when the parent is styled with overflow:hidden)
            // and we put it additionally inside a absolute positioned provisional parent  
            // ON the original parent for the eventual animation when releasing 
            Element parentElement = getElement().getParentElement();
            if (parentElement == null) {
                parentElement = target.getElement();
            }
            int elementTop = getElement().getAbsoluteTop();
            int parentTop = parentElement.getAbsoluteTop();
            m_provisionalParent = DOM.createElement(parentElement.getTagName());
            RootPanel.getBodyElement().appendChild(m_provisionalParent);
            m_provisionalParent.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().clearStyles());
            m_provisionalParent.getStyle().setWidth(parentElement.getOffsetWidth(), Unit.PX);
            m_provisionalParent.appendChild(m_helper);
            Style style = m_helper.getStyle();
            style.setWidth(m_helper.getOffsetWidth(), Unit.PX);
            // the dragging class will set position absolute
            m_helper.addClassName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().dragging());
            style.setTop(elementTop - parentTop, Unit.PX);
            m_provisionalParent.getStyle().setPosition(Position.ABSOLUTE);
            m_provisionalParent.getStyle().setTop(parentTop, Unit.PX);
            m_provisionalParent.getStyle().setLeft(parentElement.getAbsoluteLeft(), Unit.PX);
            m_provisionalParent.getStyle().setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexDND());

        }
        // ensure mouse out
        if (m_listItemWidget != null) {
            m_listItemWidget.forceMouseOut();
        }
        CmsDomUtil.ensureMouseOut(this);
        return m_helper;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the list item widget of this list item, or null if this item doesn't have a list item widget.<p>
     * 
     * @return a list item widget or null
     */
    public CmsListItemWidget getListItemWidget() {

        if ((m_mainWidget == null) || !(m_mainWidget instanceof CmsListItemWidget)) {
            return null;
        }
        return (CmsListItemWidget)m_mainWidget;
    }

    /**
     * Returns the parent list.<p>
     * 
     * @return the parent list
     */
    @SuppressWarnings("unchecked")
    public CmsList<CmsListItem> getParentList() {

        Widget parent = getParent();
        if (parent == null) {
            return null;
        }
        return (CmsList<CmsListItem>)parent;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getParentTarget()
     */
    public I_CmsDropTarget getParentTarget() {

        return getParentList();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getPlaceholder(I_CmsDropTarget)
     */
    public Element getPlaceholder(I_CmsDropTarget target) {

        if (m_placeholder == null) {
            if (m_listItemWidget != null) {
                m_listItemWidget.setAdditionalInfoVisible(false);
            }
            m_placeholder = cloneForPlaceholder(this);
        }
        return m_placeholder;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#hasTag(java.lang.String)
     */
    public boolean hasTag(String tag) {

        return (m_tags != null) && m_tags.contains(tag);
    }

    /**
     * Initializes the move handle with the given drag and drop handler and adds it to the list item widget.<p>
     * 
     * This method will not work for list items that don't have a list-item-widget.<p>
     * 
     * @param dndHandler the drag and drop handler
     * 
     * @return <code>true</code> if initialization was successful
     */
    public boolean initMoveHandle(CmsDNDHandler dndHandler) {

        return initMoveHandle(dndHandler, false);
    }

    /**
     * Initializes the move handle with the given drag and drop handler and adds it to the list item widget.<p>
     * 
     * This method will not work for list items that don't have a list-item-widget.<p>
     * 
     * @param dndHandler the drag and drop handler
     * 
     * @param addFirst if true, adds the move handle as first child 
     * 
     * @return <code>true</code> if initialization was successful
     */
    public boolean initMoveHandle(CmsDNDHandler dndHandler, boolean addFirst) {

        if (m_moveHandle != null) {
            return true;
        }
        if (m_listItemWidget == null) {
            return false;
        }
        m_moveHandle = new MoveHandle(this);
        if (addFirst) {
            m_listItemWidget.addButtonToFront(m_moveHandle);
        } else {
            m_listItemWidget.addButton(m_moveHandle);
        }

        m_moveHandle.addMouseDownHandler(dndHandler);
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    public void onDragCancel() {

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

        CmsDomUtil.ensureMouseOut(getMoveHandle().getElement());
        setVisible(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#setId(java.lang.String)
     */
    public void setId(String id) {

        CmsList<CmsListItem> parentList = getParentList();
        if (parentList != null) {
            parentList.changeId(this, id);
        }
        m_id = id;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        for (Widget widget : m_panel) {
            if (!(widget instanceof I_CmsTruncable)) {
                continue;
            }
            int width = widgetWidth - 4; // just to be on the safe side
            if (widget instanceof CmsList<?>) {
                width -= 25; // 25px left margin
            }
            ((I_CmsTruncable)widget).truncate(textMetricsPrefix, width);
        }
    }

    /**
     * Adds a check box to this list item.<p>
     * 
     * @param checkbox the check box 
     */
    protected void addCheckBox(CmsCheckBox checkbox) {

        assert m_checkbox == null;
        m_checkbox = checkbox;
        addDecoration(m_checkbox, CHECKBOX_WIDTH, false);
        m_checkbox.addStyleName(CSS.listItemCheckbox());

    }

    /**
     * Helper method for adding a decoration widget and updating the decoration width accordingly.<p>
     * 
     * @param widget the decoration widget to add 
     * @param width the intended width of the decoration widget
     * @param first if true, inserts the widget at the front of the decorations, else at the end.
     */
    protected void addDecoration(Widget widget, int width, boolean first) {

        m_decorationWidgets.add(widget);
        m_decorationWidth += width;
    }

    /**
     * Adds the main widget to the list item.<p>
     * 
     * In most cases, the widget will be a list item widget. If this is the case, then further calls to {@link CmsListItem#getListItemWidget()} will 
     * return the widget which was passed as a parameter to this method. Otherwise, the method will return null.<p>
     * 
     * @param widget
     */
    protected void addMainWidget(Widget widget) {

        assert m_mainWidget == null;
        assert m_listItemWidget == null;
        if (widget instanceof CmsListItemWidget) {
            m_listItemWidget = (CmsListItemWidget)widget;
        }
        m_mainWidget = widget;
    }

    /**
     * Clones the given item to be used as a place holder.<p>
     * 
     * @param listItem the item to clone
     * 
     * @return the cloned item
     */
    protected Element cloneForPlaceholder(CmsListItem listItem) {

        Element clone = CmsDomUtil.clone(listItem.getElement());
        clone.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());

        // remove hoverbar
        List<Element> elems = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().buttonPanel(),
            CmsDomUtil.Tag.div,
            clone);
        for (com.google.gwt.dom.client.Element elem : elems) {
            elem.removeFromParent();
        }

        return clone;
    }

    /**
     * Returns the move handle.<p>
     * 
     * @return the move handle
     */
    protected I_CmsDragHandle getMoveHandle() {

        return m_moveHandle;
    }

    /**
     * This internal helper method creates the actual contents of the widget by combining the decorators and the main widget.<p>
     */
    protected void initContent() {

        if (m_decoratedPanel != null) {
            m_decoratedPanel.removeFromParent();
        }
        m_decoratedPanel = new CmsSimpleDecoratedPanel(m_decorationWidth, m_mainWidget, m_decorationWidgets);
        m_panel.insert(m_decoratedPanel, 0);
    }

    /**
     * This method is a convenience method which sets the checkbox and main widget of this widget, and then calls {@link CmsListItem#initContent()}.<p>
     *  
     * @param checkbox the checkbox to add
     * @param mainWidget the mainWidget to add
     */
    protected void initContent(CmsCheckBox checkbox, Widget mainWidget) {

        addCheckBox(checkbox);
        addMainWidget(mainWidget);
        initContent();
    }

    /**
     * This method is a convenience method which sets the main widget of this widget, and then calls {@link CmsListItem#initContent()}.<p>
     * 
     * @param mainWidget the main widget to add 
     */
    protected void initContent(Widget mainWidget) {

        addMainWidget(mainWidget);
        initContent();
    }

    /**
     * Called when a drag operation for this widget is stopped.<p>
     */
    private void clearDrag() {

        if (m_listItemWidget != null) {
            Iterator<Widget> buttonIterator = m_listItemWidget.getButtonPanel().iterator();
            while (buttonIterator.hasNext()) {
                Widget button = buttonIterator.next();
                button.getElement().getStyle().clearVisibility();
            }
        }
        if (m_helper != null) {
            m_helper.removeFromParent();
            m_helper = null;
        }
        if (m_provisionalParent != null) {
            m_provisionalParent.removeFromParent();
            m_provisionalParent = null;
        }
        setVisible(true);

    }
}
