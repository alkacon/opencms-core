/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/Attic/I_CmsContainer.java,v $
 * Date   : $Date: 2010/03/26 13:13:11 $
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

package org.opencms.ade.containerpage.shared;

/**
 * Interface representing the container methods to be used by overlay type CmsContainerJso and other types.<p>
 */
public interface I_CmsContainer {

    /** Key 'elements' used within the JSON representation of a container object. */
    String JSONKEY_ELEMENTS = "elements";

    /** Key 'maxElements' used within the JSON representation of a container object. */
    String JSONKEY_MAXELEMENTS = "maxElements";

    /** Key 'name' used within the JSON representation of a container object. */
    String JSONKEY_NAME = "name";

    /** Key 'type' used within the JSON representation of a container object. */
    String JSONKEY_TYPE = "type";

    /** 
     * Key used to write container data into the javascript window object. This has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#KEY_CONTAINER_DATA}. */
    String KEY_CONTAINER_DATA = "org_opencms_ade_containerpage_containers";

    //  not using an enumeration for JSON keys because constants are easier to access from within JSNI code
    //
    //    /** Keys used within the JSON representation of a container object. */
    //    static enum JsonKey {
    //
    //        /** The elements. */
    //        elements,
    //
    //        /** The maximum elements. */
    //        maxElements,
    //
    //        /** The container name. */
    //        name,
    //
    //        /** The container type. */
    //        type
    //    }

    /**
     * Returns the elements client id's contained in this container.<p>
     * 
     * @return the elements
     */
    String[] getElements();

    /**
     * Returns the maximum number of elements allowed in this container.<p>
     * 
     * @return the maximum number of elements
     */
    int getMaxElements();

    /**
     * Returns the container name, also used as HTML-id for the container DOM-element. Has to be unique within the template.<p>
     *  
     * @return the container name
     */
    String getName();

    /**
     * Returns the container type. Used to determine the formatter used to render the contained elements.<p>
     * 
     * @return the container type
     */
    String getType();

    /**
     * Sets the elements contained in this container.<p>
     * 
     * @param elements the elements
     */
    void setElements(String[] elements);
}
