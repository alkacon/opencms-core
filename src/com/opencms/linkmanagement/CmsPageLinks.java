/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/linkmanagement/Attic/CmsPageLinks.java,v $
* Date   : $Date: 2003/05/15 12:39:35 $
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
package com.opencms.linkmanagement;

import com.opencms.flex.util.CmsUUID;

import java.util.Vector;

/**
 * Contains all link destinations (anchors) of a OpenCms page.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsPageLinks {

    /**
     * the database id of the resource containing the links.
     */
    private CmsUUID m_resourceId;

    /**
     * The resource name.
     */
    private String m_resourceName = null;

    /**
     * Indicates if the resource id is from the table cms_resources (offline) or
     * from the table cms_online_resources.
     */
    private boolean m_online = false;

    /**
     * The Vector (Strings) with the targets (resourcenames) of the links
     * used on this page.
     */
    private Vector m_linkDestinations;

    /**
     * Constructor.
     * @param resourceId The database id of the resource
     * @param linkTargets A Vector (of resourcenames, Strings) with the targets of the links
     */
    public CmsPageLinks(CmsUUID resourceId) {
        this(resourceId, null);
    }

    /**
     * Constructor.
     * @param resourceId The database id of the resource
     * @param linkTargets A Vector (of resourcenames, Strings) with the targets of the links
     */
    public CmsPageLinks(CmsUUID resourceId, Vector linkTargets) {
        m_resourceId = resourceId;
        if(linkTargets == null){
            m_linkDestinations = new Vector();
        }else{
            m_linkDestinations = linkTargets;
        }
    }

    /**
     * adds a single target to the page.
     */
    public void addLinkTarget(String target){
        m_linkDestinations.add(target);
    }

    /**
     * returns the id of this.
     */
    public CmsUUID getResourceId(){
        return m_resourceId;
    }

    /**
     * returns the resourcename
     */
    public String getResourceName(){
        return m_resourceName;
    }

    /**
     * retuns the Vector with the link targets.
     */
    public Vector getLinkTargets(){
        return m_linkDestinations;
    }

    public void setOnline(boolean isOnline){
        m_online = isOnline;
    }

    /**
     * sets the resourcename
     */
    public void setResourceName(String name){
        m_resourceName = name;
    }

    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
      public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Resource Id]:");
        output.append(m_resourceId);
        output.append(" [Online]:");
        output.append(m_online);
        if(m_resourceName != null){
            output.append(" [Resource Name]:");
            output.append(m_resourceName);
        }
        output.append(" [LinkTargets]:");
        output.append(m_linkDestinations.toString());
        return output.toString();
      }
}