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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.file.CmsResource;

import java.util.List;

import javax.servlet.jsp.JspException;

/**
 * Provides access to a <code>{@link org.opencms.file.CmsResource}</code> object that was previously loaded by a parent tag.<p> 
 * 
 * @since 8.0 
 */
public interface I_CmsResourceContainer {

    /**
     * Returns the name of the currently used resource collector.<p>
     * 
     * @return the name of the currently used resource collector
     */
    String getCollectorName();

    /**
     * Returns the parameters of the currently used resource collector.<p>
     * 
     * @return the parameters of the currently used resource collector
     */
    String getCollectorParam();

    /**
     * Returns the list of all currently loaded resources (instances of <code>{@link org.opencms.file.CmsResource}</code>).<p>
     * 
     * @return the list of all currently loaded resources
     */
    List<CmsResource> getCollectorResult();

    /**
     * Returns the currently loaded resource.<p>
     *
     * @return the currently loaded resource
     */
    CmsResource getResource();

    /**
     * Returns the resource name in the VFS for the currently loaded resource.<p>
     *
     * @return the resource name in the VFS for the currently loaded resource
     */
    String getResourceName();

    /**
     * Resource iteration method to be used by JSP scriptlet code.<p>
     * 
     * Calling this method will insert "direct edit" HTML to the output page (if required).<p>
     * 
     * @return <code>true</code> if more resources are to be iterated
     * 
     * @throws JspException in case something goes wrong
     */
    boolean hasMoreResources() throws JspException;

    /**
     * Resource iteration method to be used by JSP scriptlet code.<p>
     * 
     * Calling this method will insert "direct edit" HTML to the output page (if required).<p>
     * 
     * @return <code>true</code> if more resources are to be iterated
     * 
     * @deprecated use {@link #hasMoreResources()}
     * 
     * @throws JspException in case something goes wrong
     */
    @Deprecated
    boolean hasMoreContent() throws JspException;

    /**
     * Returns <code>true</code> if this container is used as a resource preloader.<p> 
     * 
     * A resource preloader is used to load resources without looping through it.<p> 
     * 
     * @return <code>true</code> if this container is used as a resource preloader
     */
    boolean isPreloader();
}