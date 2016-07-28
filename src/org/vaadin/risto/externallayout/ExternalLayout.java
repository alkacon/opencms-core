
package org.vaadin.risto.externallayout;

import java.util.Collections;
import java.util.Iterator;

import org.vaadin.risto.externallayout.shared.ExternalLayoutState;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

/**
 * Single component container that can render the given component in any HTML
 * element.
 *
 * @author Risto Yrjänä / Vaadin Ltd.
 */
public class ExternalLayout extends AbstractComponent implements HasComponents {

    private static final long serialVersionUID = 4970339558483506331L;

    private final Component childComponent;

    /**
     * Create a layout that renders the given component to an external HTML element that
     * has the specified id
     *
     * @param divId
     *            id for the target element, cannot be null
     * @param component
     *            component to be rendered, cannot be null
     */
    public ExternalLayout(String divId, Component component) {
        if ((divId == null) || (component == null)) {
            throw new IllegalArgumentException("The div id or the child component cannot be null.");
        }

        getState().externalComponentId = divId;
        childComponent = component;
        childComponent.setParent(this);
    }

    @Override
    public ExternalLayoutState getState() {

        return (ExternalLayoutState)super.getState();
    }

    @Override
    public Iterator<Component> iterator() {

        return Collections.singleton(childComponent).iterator();
    }
}
