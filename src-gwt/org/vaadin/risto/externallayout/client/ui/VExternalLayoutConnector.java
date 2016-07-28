
package org.vaadin.risto.externallayout.client.ui;

import org.vaadin.risto.externallayout.ExternalLayout;
import org.vaadin.risto.externallayout.shared.ExternalLayoutState;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector between {@link ExternalLayout} and {@link VExternalLayout}
 */
@Connect(ExternalLayout.class)
public class VExternalLayoutConnector extends AbstractHasComponentsConnector {

    @Override
    public ExternalLayoutState getState() {

        return (ExternalLayoutState)super.getState();
    }

    @Override
    public VExternalLayout getWidget() {

        return (VExternalLayout)super.getWidget();
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {

        ComponentConnector child = getChildComponents().get(0);

        Element targetElement = Document.get().getElementById(getState().externalComponentId);
        if (targetElement == null) {
            targetElement = getWidget().getElement();
        }
        getWidget().setRenderTargetElement(targetElement);

        getWidget().setWidget(child.getWidget());
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        // Captions not supported
    }

    @Override
    protected SimplePanel createWidget() {

        return new VExternalLayout();
    }

}
