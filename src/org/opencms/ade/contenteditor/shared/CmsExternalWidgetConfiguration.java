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

package org.opencms.ade.contenteditor.shared;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * External widget configuration settings.<p>
 */
public class CmsExternalWidgetConfiguration implements IsSerializable {

    /** The links to required CSS resources. */
    private List<String> m_cssResourceLinks;

    /** The java script initialization call. */
    private String m_initCall;

    /** The links to the required java script resources. */
    private List<String> m_javaScriptResourceLinks;

    /** The widget name. */
    private String m_widgetName;

    /**
     * @param widgetName the widget name
     * @param initCall the java script initialization call
     * @param javaScriptResourceLinks the java script resource links
     * @param cssResourceLinks the CSS resource links
     */
    public CmsExternalWidgetConfiguration(
        String widgetName,
        String initCall,
        List<String> javaScriptResourceLinks,
        List<String> cssResourceLinks) {

        m_widgetName = widgetName;
        m_initCall = initCall;
        m_javaScriptResourceLinks = javaScriptResourceLinks;
        m_cssResourceLinks = cssResourceLinks;
    }

    /**
     * Constructor, for serialization only.<p>
     */
    protected CmsExternalWidgetConfiguration() {

        // nothing to do
    }

    /**
     * Returns the CSS resource links.<p>
     *
     * @return the CSS resource links
     */
    public List<String> getCssResourceLinks() {

        return m_cssResourceLinks != null ? m_cssResourceLinks : Collections.<String> emptyList();
    }

    /**
     * Returns the java script initialization call.<p>
     *
     * @return the java script initialization call
     */
    public String getInitCall() {

        return m_initCall;
    }

    /**
     * Returns the java script resource links.<p>
     *
     * @return the java script resource links
     */
    public List<String> getJavaScriptResourceLinks() {

        return m_javaScriptResourceLinks != null ? m_javaScriptResourceLinks : Collections.<String> emptyList();
    }

    /**
     * Returns the widget name.<p>
     *
     * @return the widget name
     */
    public String getWidgetName() {

        return m_widgetName;
    }

}
