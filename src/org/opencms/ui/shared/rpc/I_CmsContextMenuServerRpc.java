
package org.opencms.ui.shared.rpc;

import com.vaadin.shared.communication.ServerRpc;

/**
 * The context menu server RPC.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 */
public interface I_CmsContextMenuServerRpc extends ServerRpc {

    /**
     * Called by the client side when context menu is closed.<p>
     */
    public void contextMenuClosed();

    /**
     * Called by the client widget when context menu item is clicked.<p>
     * 
     * @param itemId id of the clicked item
     * @param menuClosed will be true if menu was closed after the click
     */
    public void itemClicked(String itemId, boolean menuClosed);

    /**
     * Called by the client side when context menu is about to be opened.<p>
     * 
     * @param x mouse x coordinate
     * @param y mouse y coordinate
     * @param connectorIdOfComponent component connector id on which the click was made
     */
    public void onContextMenuOpenRequested(int x, int y, String connectorIdOfComponent);
}
