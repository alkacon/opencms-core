/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/CmsWebdavLockInfo.java,v $
 * Date   : $Date: 2007/01/23 16:58:11 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.webdav;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * The class represents a lock to a resource with all information
 * a lock needs for WebDAV.
 * 
 * @author Peter Bonrad
 */
public class CmsWebdavLockInfo {

    /** The default lock timeout. */
    public static final int DEFAULT_TIMEOUT = 604800;
    
    /** Default depth is infinite. */
    public static final int DEPTH_INFINITY = 3; // To limit tree browsing a bit
    
    /** The lock scope "exclusive". */
    public static final String SCOPE_EXCLUSIVE = "exclusive";
    
    /** The lock scope "shared". */
    public static final String SCOPE_SHARED = "shared";
    
    /** The lock type "write". */
    public static final String TYPE_WRITE = "write";

    /** The default scope for locks. */
    public static final String DEFAULT_SCOPE = SCOPE_EXCLUSIVE;
    
    /** The default type for locks. */
    public static final String DEFAULT_TYPE = TYPE_WRITE;
    
    /** The creation date of the lock. */
    private Date m_creationDate = new Date();

    /** The depth the lock is valid for (0 for the resource or "Infinity" for inheriting). */
    private int m_depth = 0;

    /** The time when the lock expires. */
    private long m_expiresAt = DEFAULT_TIMEOUT;

    /** The owner of the lock (submitted while creation). */
    private String m_owner = "";
    
    /** The path of the resource item the lock belongs to. */
    private String m_path = "/";

    /** The scope of the lock (shared or exclusive). */
    private String m_scope = DEFAULT_SCOPE;

    /** The list of token belonging to the lock. */
    private List m_tokens = new Vector();

    /** The type of the lock (write or read). */
    private String m_type = DEFAULT_TYPE;

    /**
     * Constructor.
     */
    public CmsWebdavLockInfo() {

        // empty default constructor
    }

    /**
     * Returns the creationDate.<p>
     *
     * @return the creationDate
     */
    public Date getCreationDate() {

        return m_creationDate;
    }

    /**
     * Returns the depth.<p>
     *
     * @return the depth
     */
    public int getDepth() {

        return m_depth;
    }

    /**
     * Returns the expiresAt.<p>
     *
     * @return the expiresAt
     */
    public long getExpiresAt() {

        return m_expiresAt;
    }

    /**
     * Returns the owner.<p>
     *
     * @return the owner
     */
    public String getOwner() {

        return m_owner;
    }

    /**
     * Returns the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the scope.<p>
     *
     * @return the scope
     */
    public String getScope() {

        return m_scope;
    }

    /**
     * Returns the tokens.<p>
     *
     * @return the tokens
     */
    public List getTokens() {

        return m_tokens;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Return true if the lock has expired.
     * 
     * @return true if the lock has expired
     */
    public boolean hasExpired() {

        return (System.currentTimeMillis() > m_expiresAt);
    }

    /**
     * Return true if the lock is exclusive.
     * 
     * @return true if the lock is exclusive
     */
    public boolean isExclusive() {

        return (m_scope.equals("exclusive"));

    }

    /**
     * Sets the depth.<p>
     *
     * @param depth the depth to set
     */
    public void setDepth(int depth) {

        m_depth = depth;
    }

    /**
     * Sets the expiresAt.<p>
     *
     * @param expiresAt the expiresAt to set
     */
    public void setExpiresAt(long expiresAt) {

        m_expiresAt = expiresAt;
    }

    /**
     * Sets the owner.<p>
     *
     * @param owner the owner to set
     */
    public void setOwner(String owner) {

        m_owner = owner;
    }

    /**
     * Sets the path.<p>
     *
     * @param path the path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the scope.<p>
     *
     * @param scope the scope to set
     */
    public void setScope(String scope) {

        m_scope = scope;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Get a String representation of this lock token.
     * 
     * @return a String representation of this lock
     */
    public String toString() {

        String result = "Type:" + m_type + "\n";
        result += "Scope:" + m_scope + "\n";
        result += "Depth:" + m_depth + "\n";
        result += "Owner:" + m_owner + "\n";
        result += "Expiration:" + CmsWebdavServlet.HTTP_DATE_FORMAT.format(new Date(m_expiresAt)) + "\n";

        Iterator iter = m_tokens.iterator();
        while (iter.hasNext()) {
            result += "Token:" + iter.next() + "\n";
        }
        return result;
    }

}
