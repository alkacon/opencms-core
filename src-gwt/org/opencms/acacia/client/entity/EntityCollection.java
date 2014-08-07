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

package org.opencms.acacia.client.entity;

import org.opencms.acacia.shared.I_Entity;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The entity collection.<p>
 */
public final class EntityCollection extends JavaScriptObject implements I_EntityCollection {

    /**
     * Constructor, for internal use only.<p>
     */
    protected EntityCollection() {

        // nothing to do
    }

    /**
     * Creates a new entity collection.<p>
     * 
     * @param vieInstance the vie instance to use for creation
     * 
     * @return the new entity collection
     */
    public static native I_EntityCollection createCollection(JavaScriptObject vieInstance) /*-{

                                                                                           return new vieInstance.Collection();
                                                                                           }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_EntityCollection#addOrUpdate(org.opencms.acacia.shared.I_Entity)
     */
    public native void addOrUpdate(I_Entity entity) /*-{

                                                    this.addOrUpdate(entity);
                                                    }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_EntityCollection#getEntity(int)
     */
    public native I_Entity getEntity(int index) /*-{

                                                return this.at(index);
                                                }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_EntityCollection#getEntityById(java.lang.String)
     */
    public native I_Entity getEntityById(String uri) /*-{

                                                     return this.getByCid(uri);
                                                     }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_EntityCollection#size()
     */
    public native int size() /*-{

                             return this.length;
                             }-*/;
}
