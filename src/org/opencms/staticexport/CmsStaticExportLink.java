/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsStaticExportLink.java,v $
 * Date   : $Date: 2003/08/06 16:32:48 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import java.util.*;

/**
 * Provides a data structure for a link with parameters used in the static export.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsStaticExportLink {

    /** Contains the uri's of all OpenCms resouces on wich this static export link depends on */
    private Vector m_dependencies;

    /** The date of the export, to check if a resource is newer as must be exported again */
    private long m_lastExportDate;

    /** The link uri starting with the resourcename, this is the value found in the &lgt;link&gt; tags */
    private String m_link;

    /** The link id in the database */
    private int m_linkId;

    /**
     * Indicates if this request was processed by the static export.<p>
     * 
     * If after an export there is a request which was not processed, 
     * it is deleted in the vfs and must be deleted in the export folder too.
     */
    private boolean m_processedState;

    /**
     * Constructor, creates a new static export link object.<p>
     *
     * @param linkId the database id of this static export link
     * @param link the link name
     * @param lastExportDate The export date as a long value.
     * @param dependencies the dependency list
     */
    public CmsStaticExportLink(int linkId, String link, long lastExportDate, Vector dependencies) {
        init(linkId, link, lastExportDate, dependencies);
    }

    /**
     * Constructor, creates a new static export link object.<p>
     *
     * @param link the link name
     * @param lastExportDate The export date as a long value.
     * @param dependencies the dependency list
     */
    public CmsStaticExportLink(String link, long lastExportDate, Vector dependencies) {
        init(0, link, lastExportDate, dependencies);
    }

    /**
     * Adds a dependency to the dependencies vector.<p>
     * 
     * @param dependency the dependency to add
     */
    public void addDependency(String dependency) {
        m_dependencies.add(dependency);
    }

    /**
     * Returns the dependencies vector of this link.<p>
     * 
     * @return the dependencies vector of this link
     */
    public Vector getDependencies() {
        return m_dependencies;
    }

    /**
     * Returns the database id of this link.<p>
     * 
     * @return the database id of this link
     */
    public int getId() {
        return m_linkId;
    }

    /**
     * Returns the time of the last static export of this link.<p>
     * 
     * @return the time of the last static export
     */
    public long getLastExportDate() {
        return m_lastExportDate;
    }

    /**
     * Returns the request link.<p>
     * 
     * @return the request link
     */
    public String getLink() {
        return m_link;
    }

    /**
     * Returns true if this request has already been exported.<p>
     * 
     * @return true if this request has already been exported
     */
    public boolean getProcessedState() {
        return m_processedState;
    }

    /**
     * Initializes all values of this static export link.<p>
     *
     * @param linkId the database id of this static export link
     * @param link the link name
     * @param lastExportDate The export date as a long value.
     * @param dependencies the dependency list
     */
    private void init(int linkId, String link, long lastExportDate, Vector dependencies) {
        m_processedState = false;
        m_linkId = linkId;
        m_link = link;
        m_lastExportDate = lastExportDate;
        m_dependencies = dependencies;
        if (m_dependencies == null) {
            m_dependencies = new Vector();
        }
    }

    /**
     * Sets the dependency vector.<p>
     *
     * @param dependencies the dependency vector to set
     */
    public void setDeqendencies(Vector dependencies) {
        m_dependencies = dependencies;
    }

    /**
     * Sets the export time.<p>
     * 
     * @param date the export time
     */
    public void setLastExportDate(long date) {
        m_lastExportDate = date;
    }

    /**
     * Sets this link database id.<p>
     * 
     * @param id the link database id to set
     */
    public void setLinkId(int id) {
        m_linkId = id;
    }

    /**
     * Sets the indicator to determine if this link is processed.<p>
     * 
     * @param value the state to set
     */
    public void setProcessedState(boolean value) {
        m_processedState = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[CmsExportLink]:");
        output.append(m_link);
        output.append(" ID:");
        output.append(m_linkId);
        output.append(" date:");
        output.append((new Date(m_lastExportDate)).toString());
        output.append(" exported:");
        output.append("" + m_processedState);
        return output.toString();
    }
}