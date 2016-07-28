package org.vaadin.risto.externallayout.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client-side implementation for the ExternalLayout addon. A simple, single-component panel
 * that renders the contained component in the given element.
 */
public class VExternalLayout extends SimplePanel implements Iterable<Widget> {

    private static final String CLASSNAME = "v-externallayout";

    private Element renderTargetElement;

    public VExternalLayout() {
        renderTargetElement = getElement();
    }

    /**
     * This is overridden so that {@link #setWidget(Widget)} uses the specified
     * external element.
     */
    @Override
    @SuppressWarnings("deprecation")
    //have to use old Element here because of superclass
    protected com.google.gwt.user.client.Element getContainerElement() {
        return DOM.asOld(getRenderTargetElement());
    }

    public Element getRenderTargetElement() {
        return renderTargetElement;
    }

    public void setRenderTargetElement(Element renderTargetElement) {
        this.renderTargetElement = renderTargetElement;
        renderTargetElement.setClassName(CLASSNAME);
    }

}
