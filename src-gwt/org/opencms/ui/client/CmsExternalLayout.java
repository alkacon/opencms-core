
package org.opencms.ui.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client-side implementation for the ExternalLayout addon. A simple, single-component panel
 * that renders the contained component in the given element.<p>
 *
 * @author Risto Yrjänä / Vaadin Ltd.
 */
public class CmsExternalLayout extends SimplePanel {

    /** The CSS class name. */
    private static final String CLASSNAME = "v-externallayout";

    /** The target element. */
    private Element m_renderTargetElement;

    /**
     * Constructor.<p>
     */
    public CmsExternalLayout() {
        m_renderTargetElement = getElement();
    }

    /**
     * Returns the target element.<p>
     *
     * @return the target element
     */
    public Element getRenderTargetElement() {

        return m_renderTargetElement;
    }

    /**
     * Sets the target element.<p>
     *
     * @param renderTargetElement the target element
     */
    public void setRenderTargetElement(Element renderTargetElement) {

        this.m_renderTargetElement = renderTargetElement;
        renderTargetElement.setClassName(CLASSNAME);
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
}
