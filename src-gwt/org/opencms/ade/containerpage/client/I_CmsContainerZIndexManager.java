/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import com.google.gwt.dom.client.Element;

/**
 * The interface for the container z-index manager.<p>
 * 
 * We need this interface only because IE7's z index management doesn't work right,
 * so we need to manually change around z indices during container page drag and drop
 * operations.<p>
 * 
 * @since 8.0.0
 * 
 */
public interface I_CmsContainerZIndexManager {

    /** 
     * Adds a container to handle.<p>
     * 
     * @param name the name of the container
     * @param element the container HTML element 
     */
    void addContainer(String name, Element element);

    /**
     * Clears the z-index chains.<p>
     */
    void clear();

    /** 
     * Called when the user drags an element over a container.<p>
     * 
     * @param containerName the name of the container 
     */
    void enter(String containerName);

    /**
     * Called when the user drags and element out of a container.<p>
     * 
     * @param containerName the name of the container 
     */
    void leave(String containerName);

    /**
     * Called when the user starts dragging an element from a container.<p>
     * 
     * @param containerName the name of the container 
     */
    void start(String containerName);

    /** 
     * Called when the drag operations is stopped.<p>
     */
    void stop();

}