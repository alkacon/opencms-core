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

package org.opencms.ade.contenteditor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean which holds the necessary data for complex value widgets in the Acacia editor.<p>
 */
public class CmsComplexWidgetData implements IsSerializable {

    /** The renderer configuration string. */
    private String m_configuration;

    /**  
     * Information about external resources needed by the complex widget.  
     */
    private CmsExternalWidgetConfiguration m_externalWidgetConfiguration;

    /** The name of the renderer to use for the complex widget. */
    private String m_rendererName;

    /** 
     * Creates a new instance.<p>
     * 
     * @param rendererName the renderer name 
     * @param config the renderer configuration 
     * @param extConfig the external resource configuration for the widget 
     */
    public CmsComplexWidgetData(String rendererName, String config, CmsExternalWidgetConfiguration extConfig) {

        m_rendererName = rendererName;
        m_configuration = config;
        m_externalWidgetConfiguration = extConfig;
    }

    /** 
     * Defaul constructor for serialization.<p>
     */
    protected CmsComplexWidgetData() {

        // do  nothing 
    }

    /** 
     * Gets the renderer configuration string.<p>
     * 
     * @return the renderer configuration string 
     */
    public String getConfiguration() {

        return m_configuration;
    }

    /** 
     * Gets information about the external resources which are needed by this widget.<p>
     * 
     * @return the information about the required external resources 
     */
    public CmsExternalWidgetConfiguration getExternalWidgetConfiguration() {

        return m_externalWidgetConfiguration;
    }

    /** 
     * Gets the renderer name.<p>
     * 
     * @return the renderer name 
     */
    public String getRendererName() {

        return m_rendererName;
    }
}
