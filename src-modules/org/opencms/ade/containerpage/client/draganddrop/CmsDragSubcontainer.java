/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragSubcontainer.java,v $
 * Date   : $Date: 2010/05/04 06:58:13 $
 * Version: $Revision: 1.1 $
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

import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Implementation of a draggable sub-container element. To be used for content elements within a container-page.<p>
 * The sub-container acts as a draggable element and if edited as a container.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDragSubcontainer extends CmsDragContainerElement implements I_CmsDragTargetContainer {

    /** Highlighting border for this container. */
    private CmsHighlightingBorder m_highlighting;

    /**
     * Constructor.<p>
     * 
     * @param element the DOM element
     * @param parent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason, if empty, editing is allowed
     */
    public CmsDragSubcontainer(
        Element element,
        I_CmsDragTarget parent,
        String clientId,
        String sitePath,
        String noEditReason) {

        super(element, parent, clientId, sitePath, noEditReason);

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#getPositionInfo()
     */
    public CmsPositionBean getPositionInfo() {

        return CmsPositionBean.generatePositionInfo(this);
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
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragEnter(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragEnter(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragInside(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragInside(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragLeave(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragLeave(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDrop(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDrop(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#refreshHighlighting()
     */
    public void refreshHighlighting() {

        m_highlighting.setPosition(CmsPositionBean.getInnerDimensions(this));
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#removeHighlighting()
     */
    public void removeHighlighting() {

        m_highlighting.removeFromParent();
        m_highlighting = null;
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
    }
}
