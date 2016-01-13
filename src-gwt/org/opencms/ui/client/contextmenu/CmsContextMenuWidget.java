
package org.opencms.ui.client.contextmenu;

import org.opencms.ui.shared.CmsContextMenuState.ContextMenuItemState;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client side implementation for ContextMenu component.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 */
public class CmsContextMenuWidget extends Widget {

    /** The menu overlay. */
    private final CmsContextMenuOverlay m_menuOverlay;

    /** The event preview handler. */
    private final NativePreviewHandler m_nativeEventHandler = new NativePreviewHandler() {

        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                // Always close the context menu on esc, no matter the focus
                hide();
            }

            Event nativeEvent = Event.as(event.getNativeEvent());
            boolean targetsContextMenu = eventTargetContextMenu(nativeEvent);

            if (!targetsContextMenu && (nativeEvent.getTypeInt() == Event.ONMOUSEDOWN) && isHideAutomatically()) {
                hide();
            }
        }
    };

    /** The preview handler registration. */
    private final HandlerRegistration m_nativeEventHandlerRegistration;

    /** The hide automatically flag. */
    private boolean m_hideAutomatically;

    /** The extension target widget. */
    private Widget m_extensionTarget;

    /**
     * Constructor.<p>
     */
    public CmsContextMenuWidget() {
        Element element = DOM.createDiv();
        setElement(element);

        m_nativeEventHandlerRegistration = Event.addNativePreviewHandler(m_nativeEventHandler);

        m_menuOverlay = new CmsContextMenuOverlay();
    }

    /**
     * Adds a menu popup close handler.<p>
     *
     * @param popupCloseHandler the close handler
     *
     * @return the handler registration
     */
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> popupCloseHandler) {

        return m_menuOverlay.addCloseHandler(popupCloseHandler);
    }

    /**
     * Adds new item as context menu root item.<p>
     *
     * @param rootItem the root item
     * @param connector the connector
     */
    public void addRootMenuItem(ContextMenuItemState rootItem, CmsContextMenuConnector connector) {

        CmsContextMenuItemWidget itemWidget = createEmptyItemWidget(
            rootItem.getId(),
            rootItem.getCaption(),
            rootItem.getDescription(),
            connector);
        itemWidget.setEnabled(rootItem.isEnabled());
        itemWidget.setSeparatorVisible(rootItem.isSeparator());

        setStyleNames(itemWidget, rootItem.getStyles());

        m_menuOverlay.addMenuItem(itemWidget);

        for (ContextMenuItemState childState : rootItem.getChildren()) {
            createSubMenu(itemWidget, childState, connector);
        }
    }

    /**
     * Clears the menu items.<p>
     */
    public void clearItems() {

        m_menuOverlay.clearItems();
    }

    /**
     * Returns the extension target widget.<p>
     *
     * @return the extension target widget
     */
    public Widget getExtensionTarget() {

        return m_extensionTarget;
    }

    /**
     * Hides the menu popup.<p>
     */
    public void hide() {

        m_menuOverlay.hide();
    }

    /**
     * Returns whether the menu is set to hide automatically.<p>
     *
     * @return <code>true</code> if the menu is set to hide automatically
     */
    public boolean isHideAutomatically() {

        return m_hideAutomatically;
    }

    /**
     * Sets the extension target.<p>
     *
     * @param extensionTarget the etension target
     */
    public void setExtensionTarget(Widget extensionTarget) {

        this.m_extensionTarget = extensionTarget;
        m_menuOverlay.setOwner(extensionTarget);
    }

    /**
     * Sets the hide automatically flag.<p>
     *
     * @param hideAutomatically the hide automatically flag
     */
    public void setHideAutomatically(boolean hideAutomatically) {

        this.m_hideAutomatically = hideAutomatically;
    }

    /**
     * Shows the context menu at the given position.<p>
     *
     * @param rootMenuX the client x position
     * @param rootMenuY the client y position
     */
    public void showContextMenu(int rootMenuX, int rootMenuY) {

        m_menuOverlay.showAt(rootMenuX, rootMenuY);
    }

    /**
     * Shows the context menu relative to the given widget.<p>
     *
     * @param widget the widget
     */
    public void showContextMenu(Widget widget) {

        m_menuOverlay.showRelativeTo(widget);
    }

    /**
     * Unregisters the menu.<p>
     */
    public void unregister() {

        m_nativeEventHandlerRegistration.removeHandler();
        m_menuOverlay.unregister();
    }

    /**
     * Returns whether the given event targets the context menu.<p>
     *
     * @param nativeEvent the event to check
     *
     * @return <code>true</code> if the event targets the menu
     */
    protected boolean eventTargetContextMenu(Event nativeEvent) {

        for (CmsContextMenuItemWidget item : m_menuOverlay.getMenuItems()) {
            if (item.eventTargetsPopup(nativeEvent)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the menu is showing.<p>
     *
     * @return <code>true</code> if the menu is showing
     */
    protected boolean isShowing() {

        return m_menuOverlay.isShowing();
    }

    /**
     * Creates new empty menu item.<p>
     *
     * @param id the id
     * @param caption the caption
     * @param description the item description used as tool-tip
     * @param contextMenuConnector the connector
     *
     * @return the menu item
     */
    private CmsContextMenuItemWidget createEmptyItemWidget(
        String id,
        String caption,
        String description,
        CmsContextMenuConnector contextMenuConnector) {

        CmsContextMenuItemWidget widget = GWT.create(CmsContextMenuItemWidget.class);
        widget.setId(id);
        widget.setCaption(caption);
        widget.setTitle(description);
        widget.setIcon(contextMenuConnector.getConnection().getIcon(contextMenuConnector.getResourceUrl(id)));

        CmsContextMenuItemWidgetHandler handler = new CmsContextMenuItemWidgetHandler(widget, contextMenuConnector);
        widget.addClickHandler(handler);
        widget.addMouseOutHandler(handler);
        widget.addMouseOverHandler(handler);
        widget.addKeyUpHandler(handler);
        widget.setRootComponent(this);

        return widget;
    }

    /**
     * Creates a new sub menu.<p>
     *
     * @param parentWidget the parent widget
     * @param childState the child state
     * @param connector the connector
     */
    private void createSubMenu(
        CmsContextMenuItemWidget parentWidget,
        ContextMenuItemState childState,
        CmsContextMenuConnector connector) {

        CmsContextMenuItemWidget childWidget = createEmptyItemWidget(
            childState.getId(),
            childState.getCaption(),
            childState.getDescription(),
            connector);
        childWidget.setEnabled(childState.isEnabled());
        childWidget.setSeparatorVisible(childState.isSeparator());
        setStyleNames(childWidget, childState.getStyles());
        parentWidget.addSubMenuItem(childWidget);

        for (ContextMenuItemState child : childState.getChildren()) {
            createSubMenu(childWidget, child, connector);
        }
    }

    /**
     * Adds the given style names to the item widget.<p>
     *
     * @param item the item
     * @param styles the style names
     */
    private void setStyleNames(CmsContextMenuItemWidget item, Set<String> styles) {

        for (String style : styles) {
            item.addStyleName(style);
        }
    }
}
