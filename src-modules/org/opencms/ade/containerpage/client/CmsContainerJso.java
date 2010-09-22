/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerJso.java,v $
 * Date   : $Date: 2010/09/22 14:27:47 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.shared.I_CmsContainer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Javascript overlay type for container objects.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsContainerJso extends JavaScriptObject implements I_CmsContainer {

    /** Key 'elements' used within the JSON representation of a container object. */
    public static final String JSONKEY_ELEMENTS = "elements";

    /** Key 'maxElements' used within the JSON representation of a container object. */
    public static final String JSONKEY_MAXELEMENTS = "maxElem";

    /** Key 'name' used within the JSON representation of a container object. */
    public static final String JSONKEY_NAME = "name";

    /** Key 'type' used within the JSON representation of a container object. */
    public static final String JSONKEY_TYPE = "type";

    /** Key 'width' used within the JSON representation of a container object. */
    public static final String JSONKEY_WIDTH = "width";

    /**
     * Constructor. Overlay types always have protected, zero-argument constructors.<p>
     */
    protected CmsContainerJso() {

        // nothing to do here
    }

    /**
     * Returns the containers of the page.<p>
     * 
     * @return the containers
     */
    public static final native JsArray<CmsContainerJso> getContainers() /*-{
        return $wnd[@org.opencms.ade.containerpage.shared.CmsContainer::KEY_CONTAINER_DATA];
    }-*/;

    /**
     * @see org.opencms.ade.containerpage.shared.I_CmsContainer#getElements()
     */
    public final native String[] getElements() /*-{
        return this[@org.opencms.ade.containerpage.client.CmsContainerJso::JSONKEY_ELEMENTS];
    }-*/;

    /**
     * @see org.opencms.ade.containerpage.shared.I_CmsContainer#getMaxElements()
     */
    public final native int getMaxElements() /*-{
        return this[@org.opencms.ade.containerpage.client.CmsContainerJso::JSONKEY_MAXELEMENTS];
    }-*/;

    /**
     * @see org.opencms.ade.containerpage.shared.I_CmsContainer#getName()
     */
    public final native String getName() /*-{
        return this[@org.opencms.ade.containerpage.client.CmsContainerJso::JSONKEY_NAME];
    }-*/;

    /**
     * @see org.opencms.ade.containerpage.shared.I_CmsContainer#getType()
     */
    public final native String getType() /*-{
        return this[@org.opencms.ade.containerpage.client.CmsContainerJso::JSONKEY_TYPE];
    }-*/;

    /**
     * @see org.opencms.ade.containerpage.shared.I_CmsContainer#getWidth()
     */
    public final native int getWidth() /*-{
        return this[@org.opencms.ade.containerpage.client.CmsContainerJso::JSONKEY_WIDTH];
    }-*/;

    /**
     * @see org.opencms.ade.containerpage.shared.I_CmsContainer#setElements(java.lang.String[])
     */
    public final native void setElements(String[] elements) /*-{
        this[@org.opencms.ade.containerpage.client.CmsContainerJso::JSONKEY_ELEMENTS]=elements;
    }-*/;

}
