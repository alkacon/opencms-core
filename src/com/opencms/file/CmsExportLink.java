/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsExportLink.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.file;

import java.util.*;

/**
 * Describes a link for the static export.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsExportLink {

    /**
     * The database ID
     */
    private int m_linkId;

    /**
     * The url request starting with the resourcename. This is the value in the
     * link tags.
     */
    private String m_link;

    /**
     * The date of the export. Used only to check if it changed and must be
     * exported again.
     */
    private long m_lastExportDate;

    /**
     * Indicates if this request was processed by the static export. If after
     * an export there is a request witch was not processed it is deleted in
     * the vfs and must be deleted in the export folder too.
     */
    private boolean m_processedState = false;

    /**
     * Contains all OpenCms resouces on wich this request depends as a String
     * (getRootName). There are three kinds of dependencies for a exportRequest.
     * First the Resource the request links to (all resources have this one).
     * Then (only for page resources) the template files of elements in the page.
     * At last all dependencies registerd by the template classes.
     */
    private Vector m_dependencies;

    /**
     * Constructor, creates a new CmsExportLink object.
     *
     * @param requestId The database Id.
     * @param request The resourcenam with the url parameter.
     * @param date The export date as a long value.
     * @param dependencies The dep Vector.
     */
    public CmsExportLink(int linkId, String link, long lastExportDate, Vector dependencies){
        init(linkId, link, lastExportDate, dependencies);
    }

    /**
     * Constructor, creates a new CmsExportLink object.
     *
     * @param request The resourcenam with the url parameter.
     * @param date The export date as a long value.
     * @param dependencies The dep Vector.
     */
    public CmsExportLink(String link, long lastExportDate, Vector dependencies){
        init(0, link, lastExportDate, dependencies);
    }

    /**
     * initializes all values.
     *
     * @param requestId The database Id.
     * @param request The resourcenam with the url parameter.
     * @param date The export date as a long value.
     * @param dependencies The dep Vector.
     */
    private void init(int linkId, String link, long lastExportDate, Vector dependencies){
        m_linkId = linkId;
        m_link = link;
        m_lastExportDate = lastExportDate;
        m_dependencies = dependencies;
        if(m_dependencies == null){
            m_dependencies = new Vector();
        }
    }

    /**
     * Adds one dependency to the dependencies vector
     * @param dependency The rootname of a OpenCms resource this request depends on.
     */
    public void addDependency(String dependency){
        m_dependencies.add(dependency);
    }

    /**
     * returns the dependencies vector
     * @return dependencies
     */
    public Vector getDependencies(){
        return m_dependencies;
    }

    /**
     * returns the database id of this link
     */
    public int getId(){
        return m_linkId;
    }

    /**
     * gets the export time
     * @return long date.
     */
    public long getLastExportDate(){
        return m_lastExportDate;
    }

    /**
     * returns the request.
     */
    public String getLink(){
        return m_link;
    }

    /**
     * returns if this request is already exported.
     */
    public boolean getProcessedState(){
        return m_processedState;
    }

    /**
     * sets the dependencies vector (old values are deleted)
     * @param dependencies.
     */
    public void setDeqendencies(Vector dependencies){
        m_dependencies = dependencies;
    }

    /**
     * sets the id.
     */
    public void setLinkId(int id){
        m_linkId = id;
    }

    /**
     * sets the export time
     * @param date.
     */
    public void setLastExportDate(long date){
        m_lastExportDate = date;
    }

    /**
     * indicates that this request is processed.
     * @param processed true or false.
     */
    public void setProcessedState(boolean processed){
        m_processedState = processed;
    }

    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
    public String toString(){
        StringBuffer output = new StringBuffer();
        output.append("[CmsExportLink]:");
        output.append(m_link);
        output.append(" ID:");
        output.append(m_linkId);
        output.append(" date:");
        output.append((new Date(m_lastExportDate)).toString());
        output.append(" exported:");
        output.append(""+m_processedState);
        return output.toString();
    }
}