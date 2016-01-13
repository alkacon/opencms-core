
package org.opencms.ui.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.shared.AbstractComponentState;

/**
 * The context menu state.<p>
 */
public class CmsContextMenuState extends AbstractComponentState {

    /**
     * The menu item state.<p>
     */
    public static class ContextMenuItemState implements Serializable {

        /** The serial version id. */
        private static final long serialVersionUID = 3836772122928080543L;

        /** The caption. */
        private String m_caption;

        /** The item children. */
        private List<ContextMenuItemState> m_children;

        /** The description, used as tooltip. */
        private String m_description;

        /** The enabled flag. */
        private boolean m_enabled = true;

        /** The item id. */
        private String m_id;

        /** The separator flag. */
        private boolean m_separator;

        /** The styles. */
        private Set<String> m_styles;

        /**
         * Constructor.<p>
         */
        public ContextMenuItemState() {
            m_children = new ArrayList<CmsContextMenuState.ContextMenuItemState>();
            m_styles = new HashSet<String>();
        }

        /**
         * Adds a child item.<p>
         *
         * @param caption the caption
         * @param id the id
         *
         * @return the child item state
         */
        public ContextMenuItemState addChild(String caption, String id) {

            ContextMenuItemState child = new ContextMenuItemState();
            child.setCaption(caption);
            child.m_id = id;

            m_children.add(child);

            return child;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            if (obj instanceof ContextMenuItemState) {
                return m_id.equals(((ContextMenuItemState)obj).m_id);
            }

            return false;
        }

        /**
         * Returns the caption.<p>
         *
         * @return the caption
         */
        public String getCaption() {

            return m_caption;
        }

        /**
         * Returns the child items.<p>
         *
         * @return the child items
         */
        public List<ContextMenuItemState> getChildren() {

            return m_children;
        }

        /**
         * Returns the description.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_description;
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
         * Returns the styles.<p>
         *
         * @return the styles
         */
        public Set<String> getStyles() {

            return m_styles;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return m_id.hashCode();
        }

        /**
         * Returns whether a separator should be displayed.<p>
         *
         * @return <code>true</code> if a separator should be displayed
         */
        public boolean isSeparator() {

            return m_separator;
        }

        /**
         * Returns whether the item is enabled.<p>
         *
         * @return <code>true</code> if the item is enabled
         */
        public boolean isEnabled() {

            return m_enabled;
        }

        /**
         * Removes the given child.<p>
         *
         * @param child the child to remove
         */
        public void removeChild(ContextMenuItemState child) {

            m_children.remove(child);
        }

        /**
         * Sets the caption.<p>
         *
         * @param caption the caption
         */
        public void setCaption(String caption) {

            m_caption = caption;
        }

        /**
         * Sets the child items.<p>
         *
         * @param children the children
         */
        public void setChildren(List<ContextMenuItemState> children) {

            m_children = children;
        }

        /**
         * Sets the description.<p>
         *
         * @param description the description to set
         */
        public void setDescription(String description) {

            m_description = description;
        }

        /**
         * Sets the item enabled.<p>
         *
         * @param enabled <code>true</code> to enable the item
         */
        public void setEnabled(boolean enabled) {

            m_enabled = enabled;
        }

        /**
         * Sets the id.<p>
         *
         * @param id the id to set
         */
        public void setId(String id) {

            m_id = id;
        }

        /**
         * Sets whether a separator should be displayed.<p>
         *
         * @param separator <code>true</code> if a separator should be displayed
         */
        public void setSeparator(boolean separator) {

            m_separator = separator;
        }

        /**
         * Sets the styles.<p>
         *
         * @param styleNames the styles
         */
        public void setStyles(Set<String> styleNames) {

            m_styles = styleNames;
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = -247856391284942254L;

    /** The hides automatically flag. */
    private boolean m_hideAutomatically;

    /** The opens automatically flag. */
    private boolean m_openAutomatically;

    /** The root items. */
    private List<ContextMenuItemState> m_rootItems;

    /**
     * Constructor.<p>
     */
    public CmsContextMenuState() {
        m_rootItems = new ArrayList<CmsContextMenuState.ContextMenuItemState>();
    }

    /**
     * Adds a child item.<p>
     *
     * @param itemCaption the caption
     * @param itemId the id
     *
     * @return the item state
     */
    public ContextMenuItemState addChild(String itemCaption, String itemId) {

        ContextMenuItemState rootItem = new ContextMenuItemState();
        rootItem.setCaption(itemCaption);
        rootItem.setId(itemId);

        m_rootItems.add(rootItem);

        return rootItem;
    }

    /**
     * Returns the root items.<p>
     *
     * @return the root items
     */
    public List<ContextMenuItemState> getRootItems() {

        return m_rootItems;
    }

    /**
     * Returns whether the menu is set to hide automatically.<p>
     *
     * @return <code>true</code> if context menu is hidden automatically
     */
    public boolean isHideAutomatically() {

        return m_hideAutomatically;
    }

    /**
     * Returns whether the menu is set to open automatically.<p>
     *
     * @return <code>true</code> if open automatically is on. If open automatically is on, it
     *         means that context menu will always be opened when it's host
     *         component is right clicked. If automatic opening is turned off,
     *         context menu will only open when server side open(x, y) is
     *         called.
     */
    public boolean isOpenAutomatically() {

        return m_openAutomatically;
    }

    /**
     * Enables or disables automatic hiding of context menu.<p>
     *
     * @param hideAutomatically the hide automatically flag
     */
    public void setHideAutomatically(boolean hideAutomatically) {

        this.m_hideAutomatically = hideAutomatically;
    }

    /**
     * Enables or disables open automatically feature. If open automatically is
     * on, it means that context menu will always be opened when it's host
     * component is right clicked. If automatic opening is turned off, context
     * menu will only open when server side open(x, y) is called.<p>
     *
     * @param openAutomatically the open automatically flag
     */
    public void setOpenAutomatically(boolean openAutomatically) {

        this.m_openAutomatically = openAutomatically;
    }

    /**
     * Sets the root items.<p>
     *
     * @param rootItems the root items
     */
    public void setRootItems(List<ContextMenuItemState> rootItems) {

        this.m_rootItems = rootItems;
    }
}
