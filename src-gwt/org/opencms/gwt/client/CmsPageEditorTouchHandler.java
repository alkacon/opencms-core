/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.gwt.client;

import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * Singleton class that manages showing / hiding of toolbars in touch-only mode in the page editor.
 */
public class CmsPageEditorTouchHandler {

    /** The instance of the class. */
    private static CmsPageEditorTouchHandler instance = new CmsPageEditorTouchHandler();

    /** The currently active toolbar context. */
    private I_CmsElementToolbarContext m_activeContext;

    /**
     * Creates a new instance.
     */
    private CmsPageEditorTouchHandler() {

        // Clicks on non-toolbar elements should deactivate the currently activated toolbar
        // when in touch-only mode. We catch click events and check the target's ancestors
        // for the absence of the CSS class for toolbars to detect this case.
        if (CmsCoreProvider.isTouchOnly()) {
            Event.addNativePreviewHandler(new NativePreviewHandler() {

                public void onPreviewNativeEvent(NativePreviewEvent event) {

                    if (m_activeContext != null) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (event.getTypeInt() == Event.ONCLICK) {
                            JavaScriptObject target = nativeEvent.getEventTarget();
                            if (Element.is(target)) {
                                Element element = Element.as(nativeEvent.getEventTarget());
                                Element optionBar = CmsDomUtil.getAncestor(
                                    element,
                                    I_CmsElementToolbarContext.ELEMENT_OPTION_BAR_CSS_CLASS);
                                if (optionBar == null) {
                                    deactivateContext();
                                }
                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * Gets the singleton instance of the handler.
     *
     * @return the singleton instance
     */
    public static CmsPageEditorTouchHandler get() {

        return instance;
    }

    /**
     * This method is called to give this class a chance to process clicks on toolbar buttons by itself, for the purpose
     * of showing / hiding toolbars in touch-only mode. If this class decided to handle the click event, returns true, otherwise false.
     *
     * @param context the toolbar context
     * @return true if the click was handled by this class
     */
    public boolean eatClick(I_CmsElementToolbarContext context) {

        if (!CmsCoreProvider.isTouchOnly()) {
            return false;
        }

        if (m_activeContext == context) {
            return false;
        } else {
            deactivateContext();
            m_activeContext = context;
            m_activeContext.activateToolbarContext();
            return true;
        }
    }

    /**
     * Check if hover events should be ignored.
     *
     * @return true if hover events should be ignored
     */
    public boolean ignoreHover() {

        return CmsCoreProvider.isTouchOnly();
    }

    /**
     * Deactivates and clears the current context, if it is set.
     */
    private void deactivateContext() {

        if (m_activeContext != null) {
            m_activeContext.deactivateToolbarContext();
            m_activeContext = null;
        }
    }

}
