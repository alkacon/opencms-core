/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragSubcontainer.java,v $
 * Date   : $Date: 2010/06/01 12:08:21 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.containerpage.client.draganddrop;

import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implementation of a draggable sub-container element. To be used for content elements within a container-page.<p>
 * The sub-container acts as a draggable element and if edited as a container.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsDragSubcontainer extends CmsDragContainerElement implements I_CmsDragTargetContainer {

    /** The container type. */
    private String m_containerType;

    /** The sub-container's place-holder while it's being edited. */
    private Widget m_placeholder;

    /**
     * Constructor.<p>
     * 
     * @param element the DOM element
     * @param parent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason, if empty, editing is allowed
     * @param hasProps if true, the subcontainer has properties to edit 
     */
    public CmsDragSubcontainer(
        Element element,
        I_CmsDragTargetContainer parent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps) {

        super(element, parent, clientId, sitePath, noEditReason, hasProps);
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#getContainerType()
     */
    public String getContainerType() {

        return m_containerType;
    }

    /**
     * Returns the place-holder.<p>
     *
     * @return the place-holder
     */
    public Widget getPlaceholder() {

        return m_placeholder;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#highlightContainer()
     */
    public void highlightContainer() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());

        // adding the 'clearFix' style to all targets containing floated elements
        // in some layouts this may lead to inappropriate clearing after the target, 
        // but it is still necessary as it forces the target to enclose it's floated content 
        if ((getWidgetCount() > 0)
            && !CmsDomUtil.getCurrentStyle(getWidget(0).getElement(), CmsDomUtil.Style.floatCss).equals(
                CmsDomUtil.StyleValue.none.toString())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
        }
        m_highlighting = new CmsHighlightingBorder(
            CmsPositionBean.getInnerDimensions(this),
            CmsHighlightingBorder.BorderColor.red);
        RootPanel.get().add(m_highlighting);
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#refreshHighlighting()
     */
    public void refreshHighlighting() {

        CmsPositionBean position = CmsPositionBean.getInnerDimensions(this);
        m_placeholder.setHeight(position.getHeight() + 10 + Unit.PX.getType());
        m_highlighting.setPosition(position);
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#removeHighlighting()
     */
    @Override
    public void removeHighlighting() {

        m_placeholder.getElement().getStyle().clearHeight();
        m_highlighting.removeFromParent();
        m_highlighting = null;
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
    }

    /**
     * Sets the container type.<p>
     *
     * @param containerType the container type to set
     */
    public void setContainerType(String containerType) {

        m_containerType = containerType;
    }

    /**
     * Sets the placeholder.<p>
     *
     * @param placeholder the placeholder to set
     */
    public void setPlaceholder(Widget placeholder) {

        m_placeholder = placeholder;
    }
}
