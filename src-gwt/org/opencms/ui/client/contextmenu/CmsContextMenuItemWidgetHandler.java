
package org.opencms.ui.client.contextmenu;

import org.opencms.ui.shared.rpc.I_CmsContextMenuServerRpc;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;

/**
 * ContextMenuItemWidgetHandler is context menu item specific object that
 * handles the server communication when item is interacted with.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 */
public class CmsContextMenuItemWidgetHandler implements ClickHandler, MouseOverHandler, MouseOutHandler, KeyUpHandler {

    /** The menu RPC. */
    private I_CmsContextMenuServerRpc m_contextMenuRpc;

    /** The menu timer. */
    private final Timer m_openTimer = new Timer() {

        @Override
        public void run() {

            onItemClicked();
        }
    };

    /** The menu item widget. */
    private CmsContextMenuItemWidget m_widget;

    /**
     * Constructor.<p>
     *
     * @param widget the menu item widget
     * @param connector the server connector
     */
    public CmsContextMenuItemWidgetHandler(CmsContextMenuItemWidget widget, ServerConnector connector) {
        this.m_widget = widget;

        m_contextMenuRpc = RpcProxy.create(I_CmsContextMenuServerRpc.class, connector);
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @Override
    public void onClick(ClickEvent event) {

        if (isEnabled()) {
            m_openTimer.cancel();

            if (m_widget.hasSubMenu()) {
                if (!m_widget.isSubmenuOpen()) {
                    m_widget.onItemClicked();
                    m_contextMenuRpc.itemClicked(m_widget.getId(), false);
                }
            } else {
                boolean menuClosed = m_widget.onItemClicked();
                m_contextMenuRpc.itemClicked(m_widget.getId(), menuClosed);
            }
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.KeyUpHandler#onKeyUp(com.google.gwt.event.dom.client.KeyUpEvent)
     */
    @Override
    public void onKeyUp(KeyUpEvent event) {

        int keycode = event.getNativeEvent().getKeyCode();

        if (keycode == KeyCodes.KEY_LEFT) {
            onLeftPressed();
        } else if (keycode == KeyCodes.KEY_RIGHT) {
            onRightPressed();
        } else if (keycode == KeyCodes.KEY_UP) {
            onUpPressed();
        } else if (keycode == KeyCodes.KEY_DOWN) {
            onDownPressed();
        } else if (keycode == KeyCodes.KEY_ENTER) {
            onEnterPressed();
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    @Override
    public void onMouseOut(MouseOutEvent event) {

        m_openTimer.cancel();
        m_widget.setFocus(false);
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    @Override
    public void onMouseOver(MouseOverEvent event) {

        m_openTimer.cancel();

        if (isEnabled()) {
            if (!m_widget.isSubmenuOpen()) {
                m_widget.closeSiblingMenus();
            }
            m_widget.setFocus(true);

            if (m_widget.hasSubMenu() && !m_widget.isSubmenuOpen()) {
                m_openTimer.schedule(100);
            }
        }
    }

    /**
     * On item click.<p>
     */
    void onItemClicked() {

        boolean menuClosed = m_widget.onItemClicked();
        m_contextMenuRpc.itemClicked(m_widget.getId(), menuClosed);
    }

    /**
     * Returns whether the item is enabled.<p>
     *
     * @return <code>true</code> if the item is enabled
     */
    private boolean isEnabled() {

        return m_widget.isEnabled();
    }

    /**
     * On down press.<p>
     */
    private void onDownPressed() {

        if (isEnabled()) {
            m_widget.selectLowerSibling();
        }
    }

    /**
     * On enter press.<p>
     */
    private void onEnterPressed() {

        if (isEnabled()) {
            if (m_widget.hasSubMenu()) {
                if (!m_widget.isSubmenuOpen()) {
                    boolean menuClosed = m_widget.onItemClicked();
                    m_contextMenuRpc.itemClicked(m_widget.getId(), menuClosed);
                }
            } else {
                boolean menuClosed = m_widget.onItemClicked();
                m_contextMenuRpc.itemClicked(m_widget.getId(), menuClosed);
            }
        }
    }

    /**
     * On left press.<p>
     */
    private void onLeftPressed() {

        if (isEnabled()) {
            m_widget.closeThisAndSelectParent();
        }
    }

    /**
     * On right press.<p>
     */
    private void onRightPressed() {

        if (isEnabled()) {
            if (m_widget.hasSubMenu()) {
                onItemClicked();
            }
        }
    }

    /**
     * On up press.<p>
     */
    private void onUpPressed() {

        if (isEnabled()) {
            m_widget.selectUpperSibling();
        }
    }
}
