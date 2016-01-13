
package org.opencms.ui.client.contextmenu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.vaadin.client.ui.Icon;

/**
 * ContextMenuItemWidget is client side widget that represents one menu item in
 * context menu.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 */
public class CmsContextMenuItemWidget extends FocusWidget {

    /** The icon. */
    protected Icon m_icon;

    /** The icon container. */
    private final FlowPanel m_iconContainer;

    /** The id. */
    private String m_id;

    /** The overlay. */
    private CmsContextMenuOverlay m_overlay;

    /** The parent item. */
    private CmsContextMenuItemWidget m_parentItem;

    /** The root panel. */
    private final FlowPanel m_root;

    /** The root component. */
    private CmsContextMenuWidget m_rootComponent;

    /** The sub menu. */
    private CmsContextMenuOverlay m_subMenu;

    /** The text label. */
    private final Label m_text;

    /**
     * Constructor.<p>
     */
    @SuppressWarnings("deprecation")
    public CmsContextMenuItemWidget() {
        m_root = new FlowPanel();
        m_root.setStylePrimaryName("v-context-menu-item-basic");

        setElement(m_root.getElement());

        m_root.addStyleName("v-context-submenu");

        m_iconContainer = new FlowPanel();
        m_iconContainer.setStyleName("v-context-menu-item-basic-icon-container");

        m_text = new Label();
        m_text.setStyleName("v-context-menu-item-basic-text");

        m_root.add(m_iconContainer);
        m_root.add(m_text);
    }

    /**
     * Adds given context menu item into the sub menu of this item.<p>
     *
     * @param contextMenuItem the menu item
     */
    public void addSubMenuItem(CmsContextMenuItemWidget contextMenuItem) {

        if (!hasSubMenu()) {
            m_subMenu = new CmsContextMenuOverlay();
            m_subMenu.setOwner(m_rootComponent.getExtensionTarget());
            setStylePrimaryName("v-context-menu-item-basic-submenu");
        }

        contextMenuItem.setParentItem(this);
        m_subMenu.addMenuItem(contextMenuItem);
    }

    /**
     * Removes all the items from the submenu of this item. If this menu item
     * does not have a sub menu, this call has no effect.<p>
     */
    public void clearItems() {

        if (hasSubMenu()) {
            m_subMenu.clearItems();
        }
    }

    /**
     * Closes the sibling menu.<p>
     */
    public void closeSiblingMenus() {

        m_overlay.closeSubMenus();
    }

    /**
     * Checks whether the given event targets the menu popup.<p>
     *
     * @param nativeEvent the event to check
     *
     * @return <code>true</code> if given event targets the overlay of this menu item or
     *         overlay of any of this item's child item.
     */
    public boolean eventTargetsPopup(Event nativeEvent) {

        if (m_overlay.eventTargetsPopup(nativeEvent)) {
            return true;
        }

        if (hasSubMenu()) {
            for (CmsContextMenuItemWidget item : m_subMenu.getMenuItems()) {
                if (item.eventTargetsPopup(nativeEvent)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the parent item.<p>
     *
     * @return menu item that opened the menu to which this item belongs
     */
    public CmsContextMenuItemWidget getParentItem() {

        return m_parentItem;
    }

    /**
     * Returns if the item has a sub menu.<p>
     *
     * @return <code>true</code> if this item has a sub menu
     */
    public boolean hasSubMenu() {

        return (m_subMenu != null) && (m_subMenu.getNumberOfItems() > 0);
    }

    /**
     * Hides the sub menu that's been opened from this item.<p>
     */
    public void hideSubMenu() {

        if (hasSubMenu()) {
            m_subMenu.hide();
            removeStyleName("v-context-menu-item-basic-open");
        }
    }

    /**
     * Returns whether this is the root item.<p>
     *
     * @return <code>true</code> if this item is an item in the root menu
     */
    public boolean isRootItem() {

        return m_parentItem == null;
    }

    /**
     * Returns if the sub menu is open.<p>
     *
     * @return <code>true</code> if this menu has a sub menu and it's open
     */
    public boolean isSubmenuOpen() {

        return hasSubMenu() && m_subMenu.isShowing();
    }

    /**
     * Sets the caption.<p>
     *
     * @param caption the caption to set
     */
    public void setCaption(String caption) {

        m_text.setText(caption);
    }

    /**
     * @see com.google.gwt.user.client.ui.FocusWidget#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        if (enabled) {
            m_root.removeStyleName("v-context-menu-item-disabled");
        } else {
            m_root.addStyleName("v-context-menu-item-disabled");
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.FocusWidget#setFocus(boolean)
     */
    @Override
    public void setFocus(boolean focused) {

        if (hasSubMenu()) {
            m_subMenu.setFocus(false);
        }

        super.setFocus(focused);

        if (!focused) {
            DOM.releaseCapture(getElement());
        }
    }

    /**
     * Sets the icon.<p>
     *
     * @param icon the icon
     */
    public void setIcon(Icon icon) {

        if (icon == null) {
            m_iconContainer.clear();
            this.m_icon = null;
        } else {
            m_iconContainer.getElement().appendChild(icon.getElement());
            this.m_icon = icon;
        }
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id
     */
    public void setId(String id) {

        this.m_id = id;
    }

    /**
     * Sets the menu component to which this item belongs to.<p>
     *
     * @param owner the owner
     */
    public void setOverlay(CmsContextMenuOverlay owner) {

        this.m_overlay = owner;
    }

    /**
     * Sets parent item meaning that this item is in the sub menu of given parent item.<p>
     *
     * @param parentItem the parent item
     */
    public void setParentItem(CmsContextMenuItemWidget parentItem) {

        this.m_parentItem = parentItem;
    }

    /**
     * Sets the root component.<p>
     *
     * @param rootComponent the root component
     */
    public void setRootComponent(CmsContextMenuWidget rootComponent) {

        this.m_rootComponent = rootComponent;
    }

    /**
     * Sets the separator visibility.<p>
     *
     * @param separatorVisible <code>true</code> to set the separator visible
     */
    public void setSeparatorVisible(boolean separatorVisible) {

        if (separatorVisible) {
            m_root.addStyleName("v-context-menu-item-separator");
        } else {
            m_root.removeStyleName("v-context-menu-item-separator");
        }
    }

    /**
     * Closes this item and selects the parent.<p>
     */
    protected void closeThisAndSelectParent() {

        if (!isRootItem()) {
            setFocus(false);
            m_parentItem.hideSubMenu();
            m_parentItem.setFocus(true);
        }
    }

    /**
     * Called when context menu item is clicked or is focused and enter is
     * pressed.<p>
     *
     * @return <code>true</code> if context menu was closed after the click
     */
    protected boolean onItemClicked() {

        if (isEnabled()) {
            m_overlay.closeSubMenus();

            if (hasSubMenu()) {
                openSubMenu();
                return false;
            } else {
                if (m_rootComponent.isHideAutomatically()) {
                    closeContextMenu();
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Selects the next sibling.<p>
     */
    protected void selectLowerSibling() {

        setFocus(false);
        m_overlay.selectItemAfter(CmsContextMenuItemWidget.this);

    }

    /**
     * Selects the previous sibling.<p>
     */
    protected void selectUpperSibling() {

        setFocus(false);
        m_overlay.selectItemBefore(CmsContextMenuItemWidget.this);
    }

    /**
     * Closes the menu.<p>
     */
    private void closeContextMenu() {

        if (isRootItem()) {
            m_rootComponent.hide();
        } else {
            m_parentItem.closeContextMenu();
        }
    }

    /**
     * Programmatically opens the sub menu of this item.<p>
     */
    private void openSubMenu() {

        if (isEnabled() && hasSubMenu() && !m_subMenu.isShowing()) {
            m_overlay.closeSubMenus();

            setFocus(false);
            addStyleName("v-context-menu-item-basic-open");
            m_subMenu.openNextTo(this);
        }
    }
}
