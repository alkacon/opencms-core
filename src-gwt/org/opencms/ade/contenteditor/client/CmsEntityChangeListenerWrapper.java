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

package org.opencms.ade.contenteditor.client;

import org.opencms.acacia.shared.CmsEntity;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Native change handler wrapper.<p>
 */
public final class CmsEntityChangeListenerWrapper implements I_CmsEntityChangeListener {

    /** The wrapped native listener. */
    private JavaScriptObject m_wrappedListener;

    /**
     * Constructor.<p>
     *
     * @param listener the native listener to wrap
     */
    protected CmsEntityChangeListenerWrapper(JavaScriptObject listener) {

        m_wrappedListener = listener;
    }

    /**
     * @see org.opencms.ade.contenteditor.client.I_CmsEntityChangeListener#onEntityChange(org.opencms.acacia.shared.CmsEntity)
     */
    public void onEntityChange(CmsEntity entity) {

        onChange(entity);
    }

    /**
     * Handles the on change call.<p>
     *
     * @param entity the changed entiy
     */
    private native void onChange(CmsEntity entity) /*-{
                                                   var listener = this.@org.opencms.ade.contenteditor.client.CmsEntityChangeListenerWrapper::m_wrappedListener;
                                                   var wrappedEntity=new $wnd.acacia.CmsEntityWrapper(entity);
                                                   listener.onChange(wrappedEntity);
                                                   }-*/;
}
