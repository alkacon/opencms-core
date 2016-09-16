
package org.opencms.ui.client;

import org.opencms.ui.shared.components.CmsExternalLayoutState;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector between {@link org.opencms.ui.components.CmsExternalLayout} and {@link CmsExternalLayout}.<p>
 *
 * @author Risto Yrjänä / Vaadin Ltd.
 */
@Connect(org.opencms.ui.components.CmsExternalLayout.class)
public class CmsExternalLayoutConnector extends AbstractHasComponentsConnector {

    /** The serial version id. */
    private static final long serialVersionUID = 7508554962069048058L;

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public CmsExternalLayoutState getState() {

        return (CmsExternalLayoutState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getWidget()
     */
    @Override
    public CmsExternalLayout getWidget() {

        return (CmsExternalLayout)super.getWidget();
    }

    /**
     * @see com.vaadin.client.ConnectorHierarchyChangeEvent.ConnectorHierarchyChangeHandler#onConnectorHierarchyChange(com.vaadin.client.ConnectorHierarchyChangeEvent)
     */
    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {

        ComponentConnector child = getChildComponents().get(0);

        Element targetElement = Document.get().getElementById(getState().m_externalComponentId);
        if (targetElement == null) {
            targetElement = getWidget().getElement();
        }
        getWidget().setRenderTargetElement(targetElement);

        getWidget().setWidget(child.getWidget());
    }

    /**
     * @see com.vaadin.client.HasComponentsConnector#updateCaption(com.vaadin.client.ComponentConnector)
     */
    @Override
    public void updateCaption(ComponentConnector connector) {
        // Captions not supported
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    protected SimplePanel createWidget() {

        return new CmsExternalLayout();
    }

}
