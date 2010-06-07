/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/tree/Attic/CmsDnDTreeDropEvent.java,v $
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

package org.opencms.gwt.client.ui.tree;

import org.opencms.gwt.client.ui.CmsDnDList;
import org.opencms.gwt.client.ui.CmsDnDListItem;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Drag and Drop tree drop event.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsDnDTreeDropEvent extends GwtEvent<I_CmsDnDTreeDropHandler> {

    /** Event type for sitemap change events. */
    private static final Type<I_CmsDnDTreeDropHandler> TYPE = new Type<I_CmsDnDTreeDropHandler>();

    /** The destination path. */
    private String m_destPath;

    /** The destination tree. */
    private CmsDnDTree<CmsDnDTreeItem> m_destTree;

    /** The source list. */
    private CmsDnDList<CmsDnDListItem> m_srcList;

    /** The source path. */
    private String m_srcPath;

    /**
     * Constructor.<p>
     * 
     * @param srcList the source list
     * @param srcPath the source path
     * @param destTree the destination tree
     * @param destPath the destination path
     */
    public CmsDnDTreeDropEvent(
        CmsDnDList<CmsDnDListItem> srcList,
        String srcPath,
        CmsDnDTree<CmsDnDTreeItem> destTree,
        String destPath) {

        m_srcList = srcList;
        m_srcPath = srcPath;
        m_destTree = destTree;
        m_destPath = destPath;
    }

    /**
     * Gets the event type associated with change events.<p>
     * 
     * @return the handler type
     */
    public static Type<I_CmsDnDTreeDropHandler> getType() {

        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public final Type<I_CmsDnDTreeDropHandler> getAssociatedType() {

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
     * Returns the destination tree.<p>
     *
     * @return the destination tree
     */
    public CmsDnDTree<CmsDnDTreeItem> getDestTree() {

        return m_destTree;
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
     * Returns the source path.<p>
     *
     * @return the source path
     */
    public String getSrcPath() {

        return m_srcPath;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsDnDTreeDropHandler handler) {

        handler.onDrop(this);
    }
}
