
package org.opencms.ui.client.contextmenu;

import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.shared.CmsContextMenuState;
import org.opencms.ui.shared.CmsContextMenuState.ContextMenuItemState;
import org.opencms.ui.shared.rpc.I_CmsContextMenuClientRpc;
import org.opencms.ui.shared.rpc.I_CmsContextMenuServerRpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorMap;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * ContextMenuConnector is client side object that receives updates from server
 * and passes them to context menu client side widget. Connector is also
 * responsible for handling user interaction and communicating it back to
 * server.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 *
 */
@Connect(CmsContextMenu.class)
public class CmsContextMenuConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = 3830712282306785118L;

    /** The client to server RPC. */
    I_CmsContextMenuServerRpc m_clientToServerRPC = RpcProxy.create(I_CmsContextMenuServerRpc.class, this);

    /** The exntion target widget. */
    Widget m_extensionTarget;

    /** The context menu widget. */
    CmsContextMenuWidget m_widget;

    /** The close handler. */
    private CloseHandler<PopupPanel> m_contextMenuCloseHandler = new CloseHandler<PopupPanel>() {

        @Override
        public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {

            m_clientToServerRPC.contextMenuClosed();
        }
    };

    /** The close handler registration. */
    private HandlerRegistration m_contextMenuCloseHandlerRegistration;

    /** The menu handler. */
    private final ContextMenuHandler m_contextMenuHandler = new ContextMenuHandler() {

        @Override
        public void onContextMenu(ContextMenuEvent event) {

            event.preventDefault();
            event.stopPropagation();

            EventTarget eventTarget = event.getNativeEvent().getEventTarget();

            ComponentConnector connector = Util.getConnectorForElement(
                getConnection(),
                getConnection().getUIConnector().getWidget(),
                (Element)eventTarget.cast());

            Widget clickTargetWidget = connector.getWidget();

            if (m_extensionTarget.equals(clickTargetWidget)) {
                if (getState().isOpenAutomatically()) {
                    m_widget.showContextMenu(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                } else {
                    m_clientToServerRPC.onContextMenuOpenRequested(
                        event.getNativeEvent().getClientX(),
                        event.getNativeEvent().getClientY(),
                        connector.getConnectorId());
                }
            }
        }
    };

    /** The menu handler registration. */
    private HandlerRegistration m_contextMenuHandlerRegistration;

    /** The server tot client RPC. */
    private I_CmsContextMenuClientRpc m_serverToClientRPC = new I_CmsContextMenuClientRpc() {

        private static final long serialVersionUID = 1L;

        @Override
        public void hide() {

            m_widget.hide();
        }

        @Override
        public void showContextMenu(int x, int y) {

            m_widget.showContextMenu(x, y);
        }

        @Override
        public void showContextMenuRelativeTo(String connectorId) {

            ServerConnector connector = ConnectorMap.get(getConnection()).getConnector(connectorId);

            if (connector instanceof AbstractComponentConnector) {
                AbstractComponentConnector componentConnector = (AbstractComponentConnector)connector;
                componentConnector.getWidget();

                m_widget.showContextMenu(componentConnector.getWidget());
            }
        }

    };

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsContextMenuState getState() {

        return (CmsContextMenuState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);

        m_widget.clearItems();
        m_widget.setHideAutomatically(getState().isHideAutomatically());

        for (ContextMenuItemState rootItem : getState().getRootItems()) {
            m_widget.addRootMenuItem(rootItem, this);
        }
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#onUnregister()
     */
    @Override
    public void onUnregister() {

        m_contextMenuCloseHandlerRegistration.removeHandler();
        m_contextMenuHandlerRegistration.removeHandler();

        m_widget.unregister();

        super.onUnregister();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector extensionTarget) {

        this.m_extensionTarget = ((ComponentConnector)extensionTarget).getWidget();

        m_widget.setExtensionTarget(this.m_extensionTarget);

        m_contextMenuHandlerRegistration = this.m_extensionTarget.addDomHandler(
            m_contextMenuHandler,
            ContextMenuEvent.getType());
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#init()
     */
    @Override
    protected void init() {

        m_widget = GWT.create(CmsContextMenuWidget.class);
        m_contextMenuCloseHandlerRegistration = m_widget.addCloseHandler(m_contextMenuCloseHandler);
        registerRpc(I_CmsContextMenuClientRpc.class, m_serverToClientRPC);
    }
}
