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

package org.opencms.jlan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.SearchContext;

/**
 * This class represents the state of a search operation in a JLAN repository. It just contains
 * the list of all search results and an index into that list which points to the next result
 * which hasn't been fetched yet.<p>
 */
public class CmsJlanSearch extends SearchContext {

    /** The files constituting the search result. */
    private List<CmsJlanNetworkFile> m_files;

    /** The position of the next unfetched result. */
    private int m_position;

    /**
     * Creates a new instance based on a given result list.<p>
     *
     * @param files the result list
     */
    public CmsJlanSearch(List<CmsJlanNetworkFile> files) {

        m_files = new ArrayList<CmsJlanNetworkFile>(files);
    }

    /**
     * @see org.alfresco.jlan.server.filesys.SearchContext#getResumeId()
     */
    @Override
    public int getResumeId() {

        return m_position;
    }

    /**
     * @see org.alfresco.jlan.server.filesys.SearchContext#hasMoreFiles()
     */
    @Override
    public boolean hasMoreFiles() {

        return m_position < m_files.size();
    }

    /**
     * @see org.alfresco.jlan.server.filesys.SearchContext#nextFileInfo(org.alfresco.jlan.server.filesys.FileInfo)
     */
    @Override
    public boolean nextFileInfo(FileInfo info) {

        try {
            CmsJlanNetworkFile file = nextFile();
            if (file == null) {
                return false;
            }
            info.copyFrom(file.getFileInfo());
            return true;
        } catch (IOException e) {
            // shouldn't normally happen
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.SearchContext#nextFileName()
     */
    @Override
    public String nextFileName() {

        return nextFile().getName();
    }

    /**
     * @see org.alfresco.jlan.server.filesys.SearchContext#restartAt(org.alfresco.jlan.server.filesys.FileInfo)
     */
    @Override
    public boolean restartAt(FileInfo info) {

        return false;
    }

    /**
     * @see org.alfresco.jlan.server.filesys.SearchContext#restartAt(int)
     */
    @Override
    public boolean restartAt(int resumeId) {

        return false;
    }

    /**
     * Returns the next file object in the search result.<p>
     *
     * @return the next file object
     */
    protected CmsJlanNetworkFile nextFile() {

        if (!hasMoreFiles()) {
            return null;
        }
        CmsJlanNetworkFile file = m_files.get(m_position);
        m_position += 1;
        return file;
    }

}
