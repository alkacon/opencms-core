
package org.opencms.ui.components;

import org.opencms.ui.shared.components.CmsExternalLayoutState;

import java.util.Collections;
import java.util.Iterator;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

/**
 * Single component container that can render the given component in any HTML
 * element.<p>
 *
 * @author Risto Yrjänä / Vaadin Ltd.
 */
public class CmsExternalLayout extends AbstractComponent implements HasComponents {

    /** The serial version id. */
    private static final long serialVersionUID = 4970339558483506331L;

    /** The child component. */
    private final Component m_childComponent;

    /**
     * Create a layout that renders the given component to an external HTML element that
     * has the specified id
     *
     * @param divId
     *            id for the target element, cannot be null
     * @param component
     *            component to be rendered, cannot be null
     */
    public CmsExternalLayout(String divId, Component component) {
        if ((divId == null) || (component == null)) {
            throw new IllegalArgumentException("The div id or the child component cannot be null.");
        }

        getState().m_externalComponentId = divId;
        m_childComponent = component;
        m_childComponent.setParent(this);
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    public CmsExternalLayoutState getState() {

        return (CmsExternalLayoutState)super.getState();
    }

    /**
     * @see com.vaadin.ui.HasComponents#iterator()
     */
    @Override
    public Iterator<Component> iterator() {

        return Collections.singleton(m_childComponent).iterator();
    }
}
