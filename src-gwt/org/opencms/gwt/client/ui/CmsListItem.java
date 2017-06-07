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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
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

    /** The move handle. */
    public class MoveHandle extends CmsPushButton implements I_CmsDragHandle {

        /** The draggable. */
        private CmsListItem m_draggable;

        /**
         * Constructor.<p>
         *
         * @param draggable the draggable
         */
        MoveHandle(CmsListItem draggable) {

            setImageClass(I_CmsButton.MOVE_SMALL);
            setButtonStyle(ButtonStyle.FONT_ICON, null);
            setTitle(Messages.get().key(Messages.GUI_TOOLBAR_MOVE_TO_0));
            addStyleName(MOVE_HANDLE_MARKER_CLASS);
            m_draggable = draggable;
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDragHandle#getDraggable()
         */
        public I_CmsDraggable getDraggable() {

            return m_draggable;
        }

    }

    /** The CSS class to mark the move handle. */
    public static final String MOVE_HANDLE_MARKER_CLASS = "cmsMoveHandle";

    /** The width of a checkbox. */
    private static final int CHECKBOX_WIDTH = 20;

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

    /** Arbitrary data belonging to the list item. */
    private Object m_data;

    /** The class to set on the DND helper. */
    private String m_dndHelperClass;

    /** The class to set on the DND parent. */
    private String m_dndParentClass;

    /** The drag helper. */
    private Element m_helper;

    /** The move handle. */
    private MoveHandle m_moveHandle;

    /** The offset delta. */
    private Optional<int[]> m_offsetDelta = Optional.absent();

    /** Indicating this box has a reduced height. */
    private boolean m_smallView;

    /**
     * Default constructor.<p>
     */
    public CmsListItem() {

        m_panel = new CmsFlowPanel("li");
        m_panel.setStyleName(I_CmsLayoutBundle.INSTANCE.listTreeCss().listTreeItem());
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
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getCursorOffsetDelta()
     */
    public Optional<int[]> getCursorOffsetDelta() {

        return m_offsetDelta;
    }

    /**
     * Gets the data belonging to the list item.<p>
     *
     * @return the data belonging to the list item
     */
    @SuppressWarnings("unchecked")
    public <T> T getData() {

        return (T)m_data;
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
     * Gets the class for the DND helper.<p>
     *
     * @return the class for the DND helper
     */
    public String getDndHelperClass() {

        return m_dndHelperClass;
    }

    /**
     * Gets the class for the DND parent.<p>
     *
     * @return the class for the DND parent
     */
    public String getDndParentClass() {

        return m_dndParentClass;
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
            int oldMoveHandleLeft = moveHandleLeft(getElement());
            int oldElemLeft = getElement().getAbsoluteLeft();
            //int oldMoveHandleLeft = m_moveHandle.getAbsoluteLeft();
            m_helper = CmsDomUtil.clone(getElement());
            if (m_dndHelperClass != null) {
                m_helper.addClassName(m_dndHelperClass);
            }
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
            if (m_dndParentClass != null) {
                m_provisionalParent.addClassName(m_dndParentClass);
            }
            RootPanel.getBodyElement().appendChild(m_provisionalParent);

            m_provisionalParent.getStyle().setWidth(parentElement.getOffsetWidth(), Unit.PX);
            m_provisionalParent.appendChild(m_helper);

            m_provisionalParent.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().clearStyles());

            Style style = m_helper.getStyle();
            style.setWidth(m_helper.getOffsetWidth(), Unit.PX);
            // the dragging class will set position absolute
            m_helper.addClassName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().dragging());
            style.setTop(elementTop - parentTop, Unit.PX);
            m_provisionalParent.getStyle().setPosition(Position.ABSOLUTE);
            m_provisionalParent.getStyle().setTop(parentTop, Unit.PX);
            m_provisionalParent.getStyle().setLeft(parentElement.getAbsoluteLeft(), Unit.PX);
            int newMoveHandleLeft = moveHandleLeft(m_helper);
            int newElemLeft = m_helper.getAbsoluteLeft();
            m_offsetDelta = Optional.fromNullable(
                new int[] {((newMoveHandleLeft - oldMoveHandleLeft) + oldElemLeft) - newElemLeft, 0});
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
     * Returns the main widget.<p>
     *
     * @return the main widget
     */
    public Widget getMainWidget() {

        return m_mainWidget;
    }

    /**
     * Returns the move handle.<p>
     *
     * @return the move handle
     */
    public I_CmsDragHandle getMoveHandle() {

        return m_moveHandle;
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
     * Sets the data for this list item.<p>
     *
     * @param data the data to set
     */
    public void setData(Object data) {

        m_data = data;
    }

    /**
     * Sets the class for the DND helper.<p>
     *
     * @param dndHelperClass the class for the DND helper
     */
    public void setDndHelperClass(String dndHelperClass) {

        m_dndHelperClass = dndHelperClass;
    }

    /**
     * Sets the class for the DND parent.<p>
     *
     * @param dndParentClass the class for the DND parent
     */
    public void setDndParentClass(String dndParentClass) {

        m_dndParentClass = dndParentClass;
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
     * Sets the decoration style to fit with the small view of list items.<p>
     *
     * @param smallView true if the decoration has to fit with the small view of list items
     */
    public void setSmallView(boolean smallView) {

        m_smallView = smallView;
        if (m_smallView) {
            m_decoratedPanel.addDecorationBoxStyle(
                I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss().decorationBoxSmall());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        boolean hasDataValue = m_mainWidget instanceof CmsDataValue;
        for (Widget widget : m_panel) {
            if (!(widget instanceof I_CmsTruncable)) {
                continue;
            }
            int width = widgetWidth - 4; // just to be on the safe side
            if (widget instanceof CmsList<?>) {
                if (hasDataValue) {
                    width = widgetWidth;
                } else {
                    width -= 25; // 25px left margin
                }
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
     * @param widget the main content widget
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
     * This internal helper method creates the actual contents of the widget by combining the decorators and the main widget.<p>
     */
    protected void initContent() {

        if (m_decoratedPanel != null) {
            m_decoratedPanel.removeFromParent();
        }
        m_decoratedPanel = new CmsSimpleDecoratedPanel(m_decorationWidth, m_mainWidget, m_decorationWidgets);
        m_panel.insert(m_decoratedPanel, 0);
        setSmallView(m_smallView);
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
     * Gets the left edge of the move handle located in the element.<p>
     *
     * @param elem the element to search in
     *
     * @return the left edge of the move handle
     */
    protected int moveHandleLeft(Element elem) {

        return CmsDomUtil.getElementsByClass(MOVE_HANDLE_MARKER_CLASS, elem).get(0).getAbsoluteLeft();
    }

    /**
     * Removes a decoration widget.<p>
     *
     * @param widget the widget to remove
     * @param width the widget width
     */
    protected void removeDecorationWidget(Widget widget, int width) {

        if ((widget != null) && m_decorationWidgets.remove(widget)) {
            m_decorationWidth -= width;
            initContent();
        }
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
