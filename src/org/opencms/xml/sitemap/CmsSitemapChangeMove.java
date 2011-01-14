/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeMove.java,v $
 * Date   : $Date: 2011/01/14 11:59:10 $
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

package org.opencms.xml.sitemap;

import org.opencms.util.CmsUUID;

/**
 * Stores one move change to the sitemap.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeMove implements I_CmsSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = 3355467429017688564L;

    /** The destination path. */
    private String m_destinationPath;

    /** The destination position. */
    private int m_destinationPosition;

    /** The entry path name. */
    private String m_name;

    /** The parent entry id. */
    private CmsUUID m_parentId;

    /** The source path. */
    private String m_sourcePath;

    /** The structure id. */
    private CmsUUID m_structureId;

    /**
     * Constructor.<p>
     * 
     * @param structureId the structure id
     * @param sourcePath the source path
     * @param destinationPath the destination path
     * @param destinationPosition the destination position
     * @param name the entry path name
     * @param parentId the parent entry id
     */
    public CmsSitemapChangeMove(
        CmsUUID structureId,
        String sourcePath,
        String destinationPath,
        int destinationPosition,
        String name,
        CmsUUID parentId) {

        m_structureId = structureId;
        m_sourcePath = sourcePath;
        m_destinationPath = destinationPath;
        m_destinationPosition = destinationPosition;
        m_name = name;
        m_parentId = parentId;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsSitemapChangeMove() {

        // empty
    }

    /**
     * Returns the destination path.<p>
     *
     * @return the destination path
     */
    public String getDestinationPath() {

        return m_destinationPath;
    }

    /**
     * Returns the destination position.<p>
     *
     * @return the destination position
     */
    public int getDestinationPosition() {

        return m_destinationPosition;
    }

    /**
     * Returns the entry path name.<p>
     *
     * @return the entry path name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the parent entry id.<p>
     *
     * @return the parent entry id
     */
    public CmsUUID getParentId() {

        return m_parentId;
    }

    /**
     * Returns the source path.<p>
     *
     * @return the source path
     */
    public String getSourcePath() {

        return m_sourcePath;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getStructureId()
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getType()
     */
    public Type getType() {

        return Type.MOVE;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "MOVE " + getSourcePath() + " " + getDestinationPath();

    }

}