
package org.opencms.ui.contextmenu;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.shared.CmsContextMenuState;
import org.opencms.ui.shared.CmsContextMenuState.ContextMenuItemState;
import org.opencms.ui.shared.rpc.I_CmsContextMenuClientRpc;
import org.opencms.ui.shared.rpc.I_CmsContextMenuServerRpc;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.util.ReflectTools;

/**
 * ContextMenu is an extension which can be attached to any Vaadin component to
 * display a popup context menu. Most useful the menu is when attached for
 * example to Tree or Table which support item and property based context menu
 * detection.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 */
public class CmsContextMenu extends AbstractExtension {

    /**
     * ContextMenuClosedEvent is an event fired by the context menu when it's
     * closed.
     */
    public static class ContextMenuClosedEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = -5705205542849351984L;

        /** The context menu. */
        private final CmsContextMenu m_contextMenu;

        /**
         * Constructor.<p>
         *
         * @param contextMenu the context menu
         */
        public ContextMenuClosedEvent(CmsContextMenu contextMenu) {
            super(contextMenu);
            m_contextMenu = contextMenu;
        }

        /**
         * Returns the context menu.<p>
         *
         * @return the menu
         */
        public CmsContextMenu getContextMenu() {

            return m_contextMenu;
        }
    }

    /**
     * ContextMenuClosedListener is used to listen for the event that the
     * context menu is closed, either when a item is clicked or when the popup
     * is canceled.<p>
     */
    public interface ContextMenuClosedListener extends EventListener {

        /** The menu closed method. */
        public static final Method MENU_CLOSED = ReflectTools.findMethod(
            ContextMenuClosedListener.class,
            "onContextMenuClosed",
            ContextMenuClosedEvent.class);

        /**
         * Called when the context menu is closed.<p>
         *
         * @param event the event
         */
        public void onContextMenuClosed(ContextMenuClosedEvent event);
    }

    /**
     * ContextMenuItem represents one clickable item in the context menu. Item may have sub items.<p>
     */
    public class ContextMenuItem implements Serializable {

        /** The serial version id. */
        private static final long serialVersionUID = -6514832427611690050L;

        /** The item state. */
        final ContextMenuItemState m_state;

        /** The item click listeners. */
        private final List<CmsContextMenu.ContextMenuItemClickListener> m_clickListeners;

        /** The item data. */
        private Object m_data;

        /** The parent item. */
        private ContextMenuItem m_parent;

        /**
         * Constructor.<p>
         *
         * @param parent the parent item
         * @param itemState the item state
         */
        protected ContextMenuItem(ContextMenuItem parent, ContextMenuItemState itemState) {
            m_parent = parent;

            if (itemState == null) {
                throw new NullPointerException("Context menu item state must not be null");
            }

            m_clickListeners = new ArrayList<CmsContextMenu.ContextMenuItemClickListener>();
            m_state = itemState;
        }

        /**
         * Adds new item as this item's sub item with given icon.<p>
         *
         * @param icon the icon
         *
         * @return reference to newly added item
         */
        public ContextMenuItem addItem(Resource icon) {

            ContextMenuItem item = this.addItem("");
            item.setIcon(icon);

            return item;
        }

        /**
         * Adds new item as this item's sub item with given caption.<p>
         *
         * @param caption the caption
         * @return reference to newly created item.
         */
        public ContextMenuItem addItem(String caption) {

            ContextMenuItemState childItemState = m_state.addChild(caption, getNextId());
            ContextMenuItem item = new ContextMenuItem(this, childItemState);

            m_items.put(childItemState.getId(), item);
            markAsDirty();
            return item;
        }

        /**
         * Adds new item as this item's sub item with given caption and icon.<p>
         *
         * @param caption the caption
         * @param icon the icon
         * @return reference to newly added item
         */
        public ContextMenuItem addItem(String caption, Resource icon) {

            ContextMenuItem item = this.addItem(caption);
            item.setIcon(icon);

            return item;
        }

        /**
         * Adds context menu item click listener only to this item. This
         * listener will be invoked only when this item is clicked.<p>
         *
         * @param clickListener the click listener
         */
        public void addItemClickListener(CmsContextMenu.ContextMenuItemClickListener clickListener) {

            this.m_clickListeners.add(clickListener);
        }

        /**
         * Add a new style to the menu item. This method is following the same
         * semantics as {@link Component#addStyleName(String)}.<p>
         *
         * @param style
         *            the new style to be added to the component
         */
        public void addStyleName(String style) {

            if ((style == null) || style.isEmpty()) {
                return;
            }
            if (style.contains(" ")) {
                // Split space separated style names and add them one by one.
                StringTokenizer tokenizer = new StringTokenizer(style, " ");
                while (tokenizer.hasMoreTokens()) {
                    addStyleName(tokenizer.nextToken());
                }
                return;
            }

            m_state.getStyles().add(style);
            markAsDirty();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {

            if (this == other) {
                return true;
            }

            if (other instanceof ContextMenuItem) {
                return m_state.getId().equals(((ContextMenuItem)other).m_state.getId());
            }

            return false;
        }

        /**
         * Returns the item data.<p>
         *
         * @return Object associated with ContextMenuItem.
         */
        public Object getData() {

            return m_data;
        }

        /**
         * Returns the item description.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_state.getDescription();
        }

        /**
         * Returns the icon.<p>
         *
         * @return current icon
         */
        public Resource getIcon() {

            return getResource(m_state.getId());
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return m_state.getId().hashCode();
        }

        /**
         * Returns whether the item has a separator.<p>
         *
         * @return <code>true</code> if separator line is visible after this item
         */
        public boolean hasSeparator() {

            return m_state.isSeparator();
        }

        /**
         * Returns whether the item has a sub menu.<p>
         *
         * @return <code>true</code> if this menu item has a sub menu
         */
        public boolean hasSubMenu() {

            return m_state.getChildren().size() > 0;
        }

        /**
         * Returns if the item is enabled.<p>
         * @return <code>true</code> if menu item is enabled
         */
        public boolean isEnabled() {

            return m_state.isEnabled();
        }

        /**
         * Returns whether this item is the root item.<p>
         *
         * @return <code>true</code> if this item is root item
         */
        public boolean isRootItem() {

            return m_parent == null;
        }

        /**
         * Removes given click listener from this item. Removing listener
         * affects only this context menu item.<p>
         *
         * @param clickListener the click listener to remove
         */
        public void removeItemClickListener(CmsContextMenu.ContextMenuItemClickListener clickListener) {

            this.m_clickListeners.remove(clickListener);
        }

        /**
         * Remove a style name from this menu item. This method is following the
         * same semantics as {@link Component#removeStyleName(String)}.<p>
         *
         * @param style the style name or style names to be removed
         */
        public void removeStyleName(String style) {

            if (m_state.getStyles().isEmpty()) {
                return;
            }

            StringTokenizer tokenizer = new StringTokenizer(style, " ");
            while (tokenizer.hasMoreTokens()) {
                m_state.getStyles().remove(tokenizer.nextToken());
            }
        }

        /**
         * Changes the caption of the menu item.<p>
         *
         * @param newCaption the caption
         */
        public void setCaption(String newCaption) {

            m_state.setCaption(newCaption);
            markAsDirty();
        }

        /**
         * Associates given object with this menu item. Given object can be
         * whatever application specific if necessary.<p>
         *
         * @param data the data
         */
        public void setData(Object data) {

            m_data = data;
        }

        /**
         * Sets the item description used as tool-tip.<p>
         *
         * @param description the description
         */
        public void setDescription(String description) {

            m_state.setDescription(description);
            markAsDirty();
        }

        /**
         * Enables or disables this menu item.<p>
         *
         * @param enabled the enabled flag
         */
        public void setEnabled(boolean enabled) {

            m_state.setEnabled(enabled);
            markAsDirty();
        }

        /**
         * Sets given resource as icon of this menu item.<p>
         *
         * @param icon the icon
         */
        public void setIcon(Resource icon) {

            setResource(m_state.getId(), icon);
        }

        /**
         * Sets or disables separator line under this item.<p>
         *
         * @param separatorVisible the visibility flag
         */
        public void setSeparatorVisible(boolean separatorVisible) {

            m_state.setSeparator(separatorVisible);
            markAsDirty();
        }

        /**
         * Returns the item children.<p>
         *
         * @return the children
         */
        protected Set<ContextMenuItem> getAllChildren() {

            Set<ContextMenuItem> children = new HashSet<CmsContextMenu.ContextMenuItem>();

            for (ContextMenuItemState childState : m_state.getChildren()) {
                ContextMenuItem child = m_items.get(childState.getId());
                children.add(child);
                children.addAll(child.getAllChildren());
            }

            return children;
        }

        /**
         * Returns the parent item.<p>
         *
         * @return parent item of this menu item. Null if this item is a root item.
         */
        protected ContextMenuItem getParent() {

            return m_parent;
        }

        /**
         * Notifies all click listeners.<p>
         */
        protected void notifyClickListeners() {

            for (CmsContextMenu.ContextMenuItemClickListener clickListener : m_clickListeners) {
                clickListener.contextMenuItemClicked(new ContextMenuItemClickEvent(this));
            }
        }
    }

    /**
     * ContextMenuItemClickEvent is an event produced by the context menu item
     * when it is clicked. Event contains method for retrieving the clicked item
     * and menu from which the click event originated.
     */
    public static class ContextMenuItemClickEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = -3301204853129409248L;

        /**
         * Constructor.<p>
         *
         * @param component the component
         */
        public ContextMenuItemClickEvent(Object component) {
            super(component);
        }
    }

    /**
     * ContextMenuItemClickListener is listener for context menu items wanting
     * to notify listeners about item click
     */
    public interface ContextMenuItemClickListener extends EventListener {

        /** The item click method. */
        public static final Method ITEM_CLICK_METHOD = ReflectTools.findMethod(
            ContextMenuItemClickListener.class,
            "contextMenuItemClicked",
            ContextMenuItemClickEvent.class);

        /**
         * Called by the context menu item when it's clicked
         *
         * @param event
         *            containing the information of which item was clicked
         */
        public void contextMenuItemClicked(ContextMenuItemClickEvent event);
    }

    /**
     * ContextMenuOpenedListener is used to modify the content of context menu
     * based on what was clicked. For example TableListener can be used to
     * modify context menu based on certain table component clicks.<p>
     */
    public interface ContextMenuOpenedListener extends EventListener {

        /**
         * ComponentListener is used when context menu is extending a component
         * and works in mode where auto opening is disabled. For example if
         * ContextMenu is assigned to a Layout and layout is right clicked when
         * auto open feature is disabled, the open listener would be called
         * instead of menu opening automatically. Example usage is for example
         * as follows:<p>
         *
         * event.getContextMenu().open(event.getRequestSourceComponent());<p>
         */
        public interface ComponentListener extends ContextMenuOpenedListener {

            /** The open menu method. */
            public static final Method MENU_OPENED_FROM_COMPONENT = ReflectTools.findMethod(
                ContextMenuOpenedListener.ComponentListener.class,
                "onContextMenuOpenFromComponent",
                ContextMenuOpenedOnComponentEvent.class);

            /**
             * Called by the context menu when it's opened by clicking on
             * component.<p>
             *
             * @param event the event
             */
            public void onContextMenuOpenFromComponent(ContextMenuOpenedOnComponentEvent event);
        }

        /**
         * ContextMenuOpenedListener.TableListener sub interface for table
         * related context menus.<p>
         */
        public interface TableListener extends ContextMenuOpenedListener {

            /** Opened from method. */
            public static final Method MENU_OPENED_FROM_TABLE_FOOTER_METHOD = ReflectTools.findMethod(
                ContextMenuOpenedListener.TableListener.class,
                "onContextMenuOpenFromFooter",
                ContextMenuOpenedOnTableFooterEvent.class);

            /** Opened from method. */
            public static final Method MENU_OPENED_FROM_TABLE_HEADER_METHOD = ReflectTools.findMethod(
                ContextMenuOpenedListener.TableListener.class,
                "onContextMenuOpenFromHeader",
                ContextMenuOpenedOnTableHeaderEvent.class);

            /** Opened from method. */
            public static final Method MENU_OPENED_FROM_TABLE_ROW_METHOD = ReflectTools.findMethod(
                ContextMenuOpenedListener.TableListener.class,
                "onContextMenuOpenFromRow",
                ContextMenuOpenedOnTableRowEvent.class);

            /**
             * Called by the context menu when it's opened by clicking table
             * component's footer.<p>
             *
             * @param event the event
             */
            public void onContextMenuOpenFromFooter(ContextMenuOpenedOnTableFooterEvent event);

            /**
             * Called by the context menu when it's opened by clicking table
             * component's header.<p>
             *
             * @param event the event
             */
            public void onContextMenuOpenFromHeader(ContextMenuOpenedOnTableHeaderEvent event);

            /**
             * Called by the context menu when it's opened by clicking table
             * component's row.<p>
             *
             * @param event the event
             */
            public void onContextMenuOpenFromRow(ContextMenuOpenedOnTableRowEvent event);
        }

        /**
         * Tree listener interface.<p>
         */
        public interface TreeListener extends ContextMenuOpenedListener {

            /** Opened from method. */
            public static final Method MENU_OPENED_FROM_TREE_ITEM_METHOD = ReflectTools.findMethod(
                ContextMenuOpenedListener.TreeListener.class,
                "onContextMenuOpenFromTreeItem",
                ContextMenuOpenedOnTreeItemEvent.class);

            /**
             * Called by the context menu when it's opened by clicking item on a
             * tree.<p>
             *
             * @param event the event
             */
            public void onContextMenuOpenFromTreeItem(ContextMenuOpenedOnTreeItemEvent event);
        }

    }

    /**
     * ContextMenuOpenedOnComponentEvent is an event fired by the context menu
     * when it's opened from a component.<p>
     *
     */
    public static class ContextMenuOpenedOnComponentEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = 947108059398706966L;

        /** The context menu. */
        private final CmsContextMenu m_contextMenu;

        /** The client x position. */
        private final int m_x;

        /** The client y position. */
        private final int m_y;

        /**
         * Constructor.<p>
         *
         * @param contextMenu the context menu
         * @param x the client x position
         * @param y the client y position
         * @param component the component
         */
        public ContextMenuOpenedOnComponentEvent(CmsContextMenu contextMenu, int x, int y, Component component) {
            super(component);

            m_contextMenu = contextMenu;
            m_x = x;
            m_y = y;
        }

        /**
         * Returns the context menu.<p>
         *
         * @return ContextMenu that was opened.
         */
        public CmsContextMenu getContextMenu() {

            return m_contextMenu;
        }

        /**
         * Returns the source component.<p>
         *
         * @return Component which initiated the context menu open request.
         */
        public Component getRequestSourceComponent() {

            return (Component)getSource();
        }

        /**
         * Returns the client x position.<p>
         *
         * @return x-coordinate of open position.
         */
        public int getX() {

            return m_x;
        }

        /**
         * Returns the client y position.<p>
         *
         * @return y-coordinate of open position.
         */
        public int getY() {

            return m_y;
        }
    }

    /**
     * ContextMenuOpenedOnTableFooterEvent is an event that is fired by the
     * context menu when it's opened by clicking on table footer
     */
    public static class ContextMenuOpenedOnTableFooterEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = 1999781663913723438L;

        /** The context menu. */
        private final CmsContextMenu m_contextMenu;

        /** The property id. */
        private final Object m_propertyId;

        /**
         * Constructor.<p>
         *
         * @param contextMenu the context menu
         * @param sourceTable the source table
         * @param propertyId the property id
         */
        public ContextMenuOpenedOnTableFooterEvent(CmsContextMenu contextMenu, Table sourceTable, Object propertyId) {
            super(sourceTable);

            m_contextMenu = contextMenu;
            m_propertyId = propertyId;
        }

        /**
         * Returns the context menu.<p>
         *
         * @return the context menu
         */
        public CmsContextMenu getContextMenu() {

            return m_contextMenu;
        }

        /**
         * Returns the property id.<p>
         *
         * @return the property id
         */
        public Object getPropertyId() {

            return m_propertyId;
        }
    }

    /**
     * ContextMenuOpenedOnTableHeaderEvent is an event fired by the context menu
     * when it's opened by clicking on table header row.<p>
     */
    public static class ContextMenuOpenedOnTableHeaderEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = -1220618848356241248L;

        /** The context menu. */
        private final CmsContextMenu m_contextMenu;

        /** The property id. */
        private final Object m_propertyId;

        /**
         * Constructor.<p>
         *
         * @param contextMenu the context menu
         * @param sourceTable the source
         * @param propertyId the property id
         */
        public ContextMenuOpenedOnTableHeaderEvent(CmsContextMenu contextMenu, Table sourceTable, Object propertyId) {
            super(sourceTable);

            m_contextMenu = contextMenu;
            m_propertyId = propertyId;
        }

        /**
         * Returns the context menu.<p>
         *
         * @return the context menu
         */
        public CmsContextMenu getContextMenu() {

            return m_contextMenu;
        }

        /**
         * Returns the property id.<p>
         *
         * @return the property id
         */
        public Object getPropertyId() {

            return m_propertyId;
        }
    }

    /**
     * ContextMenuOpenedOnTableRowEvent is an event that is fired when context
     * menu is opened by clicking on table row.<p>
     */
    public static class ContextMenuOpenedOnTableRowEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = -470218301318358912L;

        /** The context menu. */
        private final CmsContextMenu m_contextMenu;

        /** The item id. */
        private final Object m_itemId;

        /** The property id. */
        private final Object m_propertyId;

        /**
         * Constructor.<p>
         *
         * @param contextMenu the context menu
         * @param table the table
         * @param itemId the item id
         * @param propertyId the property id
         */
        public ContextMenuOpenedOnTableRowEvent(
            CmsContextMenu contextMenu,
            Table table,
            Object itemId,
            Object propertyId) {
            super(table);

            m_contextMenu = contextMenu;
            m_itemId = itemId;
            m_propertyId = propertyId;
        }

        /**
         * Returns the context menu.<p>
         *
         * @return the context menu
         */
        public CmsContextMenu getContextMenu() {

            return m_contextMenu;
        }

        /**
         * Returns the item id.<p>
         *
         * @return the item id
         */
        public Object getItemId() {

            return m_itemId;
        }

        /**
         * Returns the property id.<p>
         *
         * @return the property id
         */
        public Object getPropertyId() {

            return m_propertyId;
        }
    }

    /**
     * ContextMenuOpenedOnTreeItemEvent is an event fired by the context menu
     * when it's opened by clicking on tree item.<p>
     */
    public static class ContextMenuOpenedOnTreeItemEvent extends EventObject {

        /** The serial version id. */
        private static final long serialVersionUID = -7705205542849351984L;

        /** The context menu. */
        private final CmsContextMenu m_contextMenu;

        /** The item id. */
        private final Object m_itemId;

        /**
         * Constructor.<p>
         *
         * @param contextMenu the context menu
         * @param tree the tree
         * @param itemId the item id
         */
        public ContextMenuOpenedOnTreeItemEvent(CmsContextMenu contextMenu, Tree tree, Object itemId) {
            super(tree);

            m_contextMenu = contextMenu;
            m_itemId = itemId;
        }

        /**
         * Returns the context menu.<p>
         *
         * @return the context menu
         */
        public CmsContextMenu getContextMenu() {

            return m_contextMenu;
        }

        /**
         * Returns the item id.<p>
         *
         * @return the item id
         */
        public Object getItemId() {

            return m_itemId;
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = 4275181115413786498L;

    /** The items. */
    final Map<String, ContextMenuItem> m_items;

    /** The server RPC. */
    private final I_CmsContextMenuServerRpc m_serverRPC = new I_CmsContextMenuServerRpc() {

        /** The serial version id. */
        private static final long serialVersionUID = 5622864428554337992L;

        @Override
        public void contextMenuClosed() {

            fireEvent(new ContextMenuClosedEvent(CmsContextMenu.this));
        }

        @Override
        public void itemClicked(String itemId, boolean menuClosed) {

            ContextMenuItem item = m_items.get(itemId);
            if (item == null) {
                return;
            }

            item.notifyClickListeners();
            fireEvent(new ContextMenuItemClickEvent(item));
        }

        @Override
        public void onContextMenuOpenRequested(int x, int y, String connectorId) {

            fireEvent(
                new ContextMenuOpenedOnComponentEvent(
                    CmsContextMenu.this,
                    x,
                    y,
                    (Component)UI.getCurrent().getConnectorTracker().getConnector(connectorId)));
        }
    };

    /**
     * Constructor.<p>
     */
    public CmsContextMenu() {
        registerRpc(m_serverRPC);

        m_items = new HashMap<String, CmsContextMenu.ContextMenuItem>();

        setOpenAutomatically(true);
        setHideAutomatically(true);
    }

    /**
     * Adds listener that will be invoked when context menu is closed.<p>
     *
     * @param contextMenuClosedListener menu close listener
     */
    public void addContextMenuCloseListener(ContextMenuClosedListener contextMenuClosedListener) {

        addListener(ContextMenuClosedEvent.class, contextMenuClosedListener, ContextMenuClosedListener.MENU_CLOSED);
    }

    /**
     * Adds listener that will be invoked when context menu is opened from the
     * component to which it's assigned to.<p>
     *
     * @param contextMenuComponentListener the component listener
     */
    public void addContextMenuComponentListener(
        CmsContextMenu.ContextMenuOpenedListener.ComponentListener contextMenuComponentListener) {

        addListener(
            ContextMenuOpenedOnComponentEvent.class,
            contextMenuComponentListener,
            ContextMenuOpenedListener.ComponentListener.MENU_OPENED_FROM_COMPONENT);
    }

    /**
     * Adds listener that will be invoked when context menu is opened from
     * com.vaadin.ui.Table component.<p>
     *
     * @param contextMenuTableListener the table listener
     */
    public void addContextMenuTableListener(
        CmsContextMenu.ContextMenuOpenedListener.TableListener contextMenuTableListener) {

        addListener(
            ContextMenuOpenedOnTableRowEvent.class,
            contextMenuTableListener,
            ContextMenuOpenedListener.TableListener.MENU_OPENED_FROM_TABLE_ROW_METHOD);
        addListener(
            ContextMenuOpenedOnTableHeaderEvent.class,
            contextMenuTableListener,
            ContextMenuOpenedListener.TableListener.MENU_OPENED_FROM_TABLE_HEADER_METHOD);
        addListener(
            ContextMenuOpenedOnTableFooterEvent.class,
            contextMenuTableListener,
            ContextMenuOpenedListener.TableListener.MENU_OPENED_FROM_TABLE_FOOTER_METHOD);
    }

    /**
     * Adds listener that will be invoked when context menu is opened from
     * com.vaadin.ui.Tree component.<p>
     *
     * @param contextMenuTreeListener the menu tree listener
     */
    public void addContextMenuTreeListener(
        CmsContextMenu.ContextMenuOpenedListener.TreeListener contextMenuTreeListener) {

        addListener(
            ContextMenuOpenedOnTreeItemEvent.class,
            contextMenuTreeListener,
            ContextMenuOpenedListener.TreeListener.MENU_OPENED_FROM_TREE_ITEM_METHOD);
    }

    /**
     * Adds new item to context menu root with given icon without caption.<p>
     *
     * @param icon the icon
     * @return reference to newly added item
     */
    public ContextMenuItem addItem(Resource icon) {

        ContextMenuItem item = addItem("");
        item.setIcon(icon);
        return item;
    }

    /**
     * Adds new item to context menu root with given caption.<p>
     *
     * @param caption the caption
     * @return reference to newly added item
     */
    public ContextMenuItem addItem(String caption) {

        ContextMenuItemState itemState = getState().addChild(caption, getNextId());

        ContextMenuItem item = new ContextMenuItem(null, itemState);
        m_items.put(itemState.getId(), item);

        return item;
    }

    /**
     * Adds new item to context menu root with given caption and icon.<p>
     *
     * @param caption the caption
     * @param icon the icon
     * @return reference to newly added item
     */
    public ContextMenuItem addItem(String caption, Resource icon) {

        ContextMenuItem item = addItem(caption);
        item.setIcon(icon);
        return item;
    }

    /**
     * Adds click listener to context menu. This listener will be invoked when
     * any of the menu items in this menu are clicked.<p>
     *
     * @param clickListener the click listener
     */
    public void addItemClickListener(CmsContextMenu.ContextMenuItemClickListener clickListener) {

        addListener(ContextMenuItemClickEvent.class, clickListener, ContextMenuItemClickListener.ITEM_CLICK_METHOD);
    }

    /**
     * @see com.vaadin.server.AbstractExtension#extend(com.vaadin.server.AbstractClientConnector)
     */
    @Override
    public void extend(AbstractClientConnector target) {

        super.extend(target);
    }

    /**
     * Closes the context menu from server side.<p>
     */
    public void hide() {

        getRpcProxy(I_CmsContextMenuClientRpc.class).hide();
    }

    /**
     * Returns if the menu is set to hide automatically.<p>
     *
     * @return <code>true</code> if context menu is hiding automatically after clicks
     */
    public boolean isHideAutomatically() {

        return getState().isHideAutomatically();
    }

    /**
     * Returns if the menu is set to open automatically.<p>
     *
     * @return <code>true</code> if open automatically is on. If open automatically is on, it
     *         means that context menu will always be opened when it's host
     *         component is right clicked. If automatic opening is turned off,
     *         context menu will only open when server side open(x, y) is
     *         called. Automatic opening avoid having to make server roundtrip
     *         whereas "manual" opening allows to have logic in menu before
     *         opening it.
     */
    public boolean isOpenAutomatically() {

        return getState().isOpenAutomatically();
    }

    /**
     * Opens the menu for the given component.<p>
     *
     * @param component the component
     */
    public void open(Component component) {

        getRpcProxy(I_CmsContextMenuClientRpc.class).showContextMenuRelativeTo(component.getConnectorId());
    }

    /**
     * Opens the context menu to given coordinates. ContextMenu must extend
     * component before calling this method. This method is only intended for
     * opening the context menu from server side when using
     * {@link #ContextMenuOpenedListener.ComponentListener}.<p>
     *
     * @param x the client x position
     * @param y the client y position
     */
    @SuppressWarnings("javadoc")
    public void open(int x, int y) {

        getRpcProxy(I_CmsContextMenuClientRpc.class).showContextMenu(x, y);
    }

    /**
     * Opens the context menu of the given table.<p>
     *
     * @param event the click event
     * @param table the table
     */
    public void openForTable(ItemClickEvent event, Table table) {

        fireEvent(new ContextMenuOpenedOnTableRowEvent(this, table, event.getItemId(), event.getPropertyId()));
        open(event.getClientX(), event.getClientY());

    }

    /**
     * Opens the context menu of the given tree.<p>
     *
     * @param event the click event
     * @param tree the tree
     */
    public void openForTree(ItemClickEvent event, Tree tree) {

        fireEvent(new ContextMenuOpenedOnTreeItemEvent(this, tree, event.getItemId()));
        open(event.getClientX(), event.getClientY());
    }

    /**
     * Removes all items from the context menu.<p>
     */
    public void removeAllItems() {

        m_items.clear();
        getState().getRootItems().clear();
    }

    /**
     * Removes given context menu item from the context menu. The given item can
     * be a root item or leaf item or anything in between. If given given is not
     * found from the context menu structure, this method has no effect.<p>
     *
     * @param contextMenuItem the menu item
     */
    public void removeItem(ContextMenuItem contextMenuItem) {

        if (!hasMenuItem(contextMenuItem)) {
            return;
        }

        if (contextMenuItem.isRootItem()) {
            getState().getRootItems().remove(contextMenuItem.m_state);
        } else {
            ContextMenuItem parent = contextMenuItem.getParent();
            parent.m_state.getChildren().remove(contextMenuItem.m_state);
        }

        Set<ContextMenuItem> children = contextMenuItem.getAllChildren();

        m_items.remove(contextMenuItem.m_state.getId());

        for (ContextMenuItem child : children) {
            m_items.remove(child.m_state.getId());
        }

        markAsDirty();
    }

    /**
     * Assigns this as context menu of given component which will react to right
     * mouse button click.<p>
     *
     * @param component the component
     */
    public void setAsContextMenuOf(AbstractClientConnector component) {

        if (component instanceof Table) {
            setAsTableContextMenu((Table)component);
        } else if (component instanceof Tree) {
            setAsTreeContextMenu((Tree)component);
        } else {
            super.extend(component);
        }
    }

    /**
     * Assigns this as the context menu of given table.<p>
     *
     * @param table the table
     */
    public void setAsTableContextMenu(final Table table) {

        extend(table);
        setOpenAutomatically(false);
    }

    /**
     * Assigns this as context menu of given tree.<p>
     *
     * @param tree the tree
     */
    public void setAsTreeContextMenu(final Tree tree) {

        extend(tree);
        setOpenAutomatically(false);
    }

    /**
     * Sets the context menu entries. Removes all previously present entries.<p>
     *
     * @param entries the entries
     * @param data the context data
     */
    public <T> void setEntries(Collection<I_CmsSimpleContextMenuEntry<T>> entries, T data) {

        removeAllItems();
        Locale locale = UI.getCurrent().getLocale();
        for (final I_CmsSimpleContextMenuEntry<T> entry : entries) {
            CmsMenuItemVisibilityMode visibility = entry.getVisibility(data);
            if (!visibility.isInVisible()) {
                ContextMenuItem item = addItem(entry.getTitle(locale));
                if (visibility.isInActive()) {
                    item.setEnabled(false);
                    if (visibility.getMessageKey() != null) {
                        item.setDescription(CmsVaadinUtils.getMessageText(visibility.getMessageKey()));
                    }
                } else {
                    item.setData(data);
                    item.addItemClickListener(new ContextMenuItemClickListener() {

                        @SuppressWarnings("unchecked")
                        public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                            entry.executeAction((T)((ContextMenuItem)event.getSource()).getData());
                        }
                    });
                }
                if (entry instanceof I_CmsSimpleContextMenuEntry.I_HasCssStyles) {
                    item.addStyleName(((I_CmsSimpleContextMenuEntry.I_HasCssStyles)entry).getStyles());
                }
            }
        }
    }

    /**
     * Sets menu to hide automatically after mouse cliks on menu items or area
     * off the menu. If automatic hiding is disabled menu will stay open as long
     * as hide is called from the server side.<p>
     *
     * @param hideAutomatically whether to hide automatically
     */
    public void setHideAutomatically(boolean hideAutomatically) {

        getState().setHideAutomatically(hideAutomatically);
    }

    /**
     * Enables or disables open automatically feature. If open automatically is
     * on, it means that context menu will always be opened when it's host
     * component is right clicked. This will happen on client side without
     * server round trip. If automatic opening is turned off, context menu will
     * only open when server side open(x, y) is called. If automatic opening is
     * disabled you will need a listener implementation for context menu that is
     * called upon client side click event. Another option is to extend context
     * menu and handle the right clicking internally with case specific listener
     * implementation and inside it call open(x, y) method.
     *
     * @param openAutomatically whether to open automatically
     */
    public void setOpenAutomatically(boolean openAutomatically) {

        getState().setOpenAutomatically(openAutomatically);
    }

    /**
     * Added to increase method visibility.<p>
     *
     * @see com.vaadin.server.AbstractClientConnector#fireEvent(java.util.EventObject)
     */
    @Override
    protected void fireEvent(EventObject event) {

        super.fireEvent(event);
    }

    /**
     * Returns a new UUID.<p>
     *
     * @return a new UUID
     */
    protected String getNextId() {

        return UUID.randomUUID().toString();
    }

    /**
     * Added to increase visibility.<p>
     *
     * @see com.vaadin.server.AbstractClientConnector#getResource(java.lang.String)
     */
    @Override
    protected Resource getResource(String key) {

        return super.getResource(key);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsContextMenuState getState() {

        return (CmsContextMenuState)super.getState();
    }

    /**
     * Added to increase visibility.<p>
     *
     * @see com.vaadin.server.AbstractClientConnector#setResource(java.lang.String, com.vaadin.server.Resource)
     */
    @Override
    protected void setResource(String key, Resource resource) {

        super.setResource(key, resource);
    }

    /**
     * Returns whether the menu has a specific item.<p>
     *
     * @param contextMenuItem the item to look for
     *
     * @return <code>true</code> if the item is present
     */
    private boolean hasMenuItem(ContextMenuItem contextMenuItem) {

        return m_items.containsKey(contextMenuItem.m_state.getId());
    }

}
