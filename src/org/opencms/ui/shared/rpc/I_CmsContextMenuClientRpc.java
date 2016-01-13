
package org.opencms.ui.shared.rpc;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Server to client RPC communication.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 *
 */
public interface I_CmsContextMenuClientRpc extends ClientRpc {

    /**
     * Sends request to client widget to close context menu.<p>
     */
    public void hide();

    /**
     * Sends request to client widget to open context menu to given position.<p>
     * 
     * @param x the client x position
     * @param y the client y position
     */
    public void showContextMenu(int x, int y);

    /**
     * Sends request to client widget to open context menu relative to component
     * identified by given connectorId. (Method is on purpose with different
     * name from showContextMenu as overloading does not work properly in
     * javascript environment.)<p>
     * 
     * @param connectorId the connector id
     */
    public void showContextMenuRelativeTo(String connectorId);

}
