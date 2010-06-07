/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDnDListDropEvent.java,v $
 * Date   : $Date: 2010/06/07 14:27:01 $
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

package org.opencms.gwt.client.ui;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Drag and Drop list drop event.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsDnDListDropEvent extends GwtEvent<I_CmsDnDListDropHandler> {

    /** Event type for sitemap change events. */
    private static final Type<I_CmsDnDListDropHandler> TYPE = new Type<I_CmsDnDListDropHandler>();

    /** The destination path. */
    private String m_destPath;

    /** The destination list. */
    private CmsDnDList<CmsDnDListItem> m_destList;

    /** The source path. */
    private String m_srcPath;

    /** The source list. */
    private CmsDnDList<CmsDnDListItem> m_srcList;

    /**
     * Constructor.<p>
     * 
     * @param srcList the source list
     * @param srcPath the source path
     * @param destList the destination list
     * @param destPath the destination path
     */
    public CmsDnDListDropEvent(
        CmsDnDList<CmsDnDListItem> srcList,
        String srcPath,
        CmsDnDList<CmsDnDListItem> destList,
        String destPath) {

        m_srcList = srcList;
        m_srcPath = srcPath;
        m_destList = destList;
        m_destPath = destPath;
    }

    /**
     * Gets the event type associated with change events.<p>
     * 
     * @return the handler type
     */
    public static Type<I_CmsDnDListDropHandler> getType() {

        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public final Type<I_CmsDnDListDropHandler> getAssociatedType() {

        return TYPE;
    }

    /**
     * Returns the destination path.<p>
     *
     * @return the destination path
     */
    public String getDestPath() {

        return m_destPath;
    }

    /**
     * Returns the destination list.<p>
     *
     * @return the destination list
     */
    public CmsDnDList<CmsDnDListItem> getDestList() {

        return m_destList;
    }

    /**
     * Returns the source path.<p>
     *
     * @return the source path
     */
    public String getSrcPath() {

        return m_srcPath;
    }

    /**
     * Returns the source list.<p>
     *
     * @return the source list
     */
    public CmsDnDList<CmsDnDListItem> getSrcList() {

        return m_srcList;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsDnDListDropHandler handler) {

        handler.onDrop(this);
    }
}
