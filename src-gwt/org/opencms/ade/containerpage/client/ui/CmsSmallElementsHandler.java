/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/** 
 * This class is responsible for managing the visibility of edit points on small elements.<p>
 */
public class CmsSmallElementsHandler {

    /** The height necessary for a container page element. */
    public static int NECESSARY_HEIGHT = 24;

    /** True if currently small elements are editable. */
    boolean m_editing;

    /** The container page service instance. */
    I_CmsContainerpageServiceAsync m_service;

    /** True if any small elements have been detected. */
    private boolean m_hasSmallElements;

    /** The style variable for the display mode for small elements. */
    private CmsStyleVariable m_smallElementsStyle;

    /** 
     * Creates a new small elements handler.<p>
     * 
     * @param service
     */
    public CmsSmallElementsHandler(I_CmsContainerpageServiceAsync service) {

        m_smallElementsStyle = new CmsStyleVariable(RootPanel.get());
        m_service = service;
    }

    /**
     * Returns the necessary height as a CSS height string in pixels.<p>
     * 
     * @return the necessary height as a CSS string 
     */
    public static String getNecessaryHeight() {

        return NECESSARY_HEIGHT + "px !important";
    }

    /** 
     * Checks if  a given widget counts as 'small'.<p>
     * 
     * @param widget the widget to check  
     * 
     * @return  true if the widget is small 
     */
    public static boolean isSmall(Widget widget) {

        assert widget.isAttached();
        return (CmsPositionBean.generatePositionInfo(widget.getElement()).getHeight() < NECESSARY_HEIGHT)
            && (CmsPositionBean.getInnerDimensions(widget.getElement()).getHeight() < NECESSARY_HEIGHT);

    }

    /**
     * Returns true if currently small elements are editable.<p>
     * 
     * @return true if small elements are editable
     */
    public boolean areSmallElementsEditable() {

        return m_editing;
    }

    /**
     * Returns true if any small elements are present.<p>
     * 
     * @return true if any small elements are present 
     */
    public boolean hasSmallElements() {

        return m_hasSmallElements;
    }

    /**
     * Prepares a small element.<p>
     * 
     * @param widget the small element 
     */
    public void prepareSmallElement(Widget widget) {

        m_hasSmallElements = true;
        widget.addStyleName(org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().smallElement());

    }

    /** 
     * Enables or disables editing for small elements and optionally saves the setting.<p>
     * 
     * @param editable if true, enables editing for small elements 
     * @param save true if the setting should be saved
     */
    public void setEditSmallElements(final boolean editable, boolean save) {

        if (save) {
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @Override
                public void execute() {

                    start(200, false);
                    internalSetEditSmallElements(editable);
                    m_service.setEditSmallElements(editable, this);

                }

                @Override
                protected void onResponse(Void result) {

                    stop(false);
                    m_editing = editable;
                }
            };
            action.execute();
        } else {
            internalSetEditSmallElements(editable);
            m_editing = editable;
        }
    }

    /**
     * Sets the mode for displaying small elements.<p>
     * 
     * @param editable if true, small elements will be enlarged and their edit buttons shown; if false, the edit buttons will be hidden
     */
    protected void internalSetEditSmallElements(boolean editable) {

        String newClass = editable
        ? I_CmsLayoutBundle.INSTANCE.containerpageCss().enlargeSmallElements()
        : I_CmsLayoutBundle.INSTANCE.containerpageCss().ignoreSmallElements();
        m_smallElementsStyle.setValue(newClass);
    }

}
