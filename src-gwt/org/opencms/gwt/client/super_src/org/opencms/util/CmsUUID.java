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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.opencms.gwt.client.Messages;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Client implementation.<p> 
 * 
 * @since 8.0.0 
 */
public final class CmsUUID implements IsSerializable, Cloneable, Comparable<CmsUUID> {

    /** Constant for the null UUID. */
    private static final CmsUUID NULL_UUID = new CmsUUID("00000000-0000-0000-0000-000000000000");

    /** Internal UUID implementation. */
    private String m_uuid;

    /**
     * Not implemented on the client, it is here only for serialization.<p>
     */
    protected CmsUUID() {

        // not implemented
    }

    /**
     * Creates a UUID based on the given string.<p>
     * 
     * No validation is implemented!.<p>
     * 
     * @param uuid a String representing a UUID
     */
    public CmsUUID(String uuid) {
        if (!uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            throw new IllegalArgumentException("Invalid UUID syntax!");  
        }
        m_uuid = uuid;
        
    }

    /**
     * Check that the given id is not the null id.<p>
     * 
     * @param id the id to check
     * @param canBeNull only if flag is set, <code>null</code> is accepted
     * 
     * @see #isNullUUID()
     */
    public static void checkId(CmsUUID id, boolean canBeNull) {

        if (canBeNull && (id == null)) {
            return;
        }
        if ((!canBeNull && (id == null)) || id.isNullUUID()) {
            throw new IllegalArgumentException(Messages.get().key(Messages.ERR_INVALID_UUID_1, id));
        }
    }

    /**
     * Returns a null UUID,
     * use this null UUID to check if a UUID has been initialized or not.<p>
     * 
     * @return a null UUID
     */
    public static CmsUUID getNullUUID() {

        return NULL_UUID;
    }

    /**
     * Returns the given String transformed to a UUID in case the String is a valid UUID.<p>
     * 
     * @param uuid the String to transform to a UUID
     * 
     * @return the given String transformed to a UUID in case the String is a valid UUID
     */
    public static CmsUUID valueOf(String uuid) {

        return new CmsUUID(uuid);
    }

    /**
     * Creates a clone of this CmsUUID.<p>
     * 
     * @return a clone of this CmsUUID
     */
    public Object clone() {

        if (this == NULL_UUID) {
            return NULL_UUID;
        }
        return new CmsUUID(m_uuid);
    }

    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(CmsUUID obj) {

        return m_uuid.compareTo(obj.m_uuid);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsUUID) {
            return ((CmsUUID)obj).m_uuid.equals(m_uuid);
        }
        return false;
    }

    /**
     * Returns the String representation of this UUID, same as {@link #toString()}.<p>
     * 
     * This method is useful if bean like access to the UUID String is required.<p>
     * 
     * @return the String representation of this UUID
     */
    public String getStringValue() {

        return toString();
    }

    /**
     * Optimized hashCode implementation for UUID's.<p>
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_uuid.hashCode();
    }

    /**
     * Returns true if this UUID is equal to the null UUID.<p>
     * 
     * @return true if this UUID is equal to the null UUID
     */
    public boolean isNullUUID() {

        if (this == NULL_UUID) {
            return true;
        }
        return m_uuid.equals(NULL_UUID.m_uuid);
    }
    
    /**
     * Returns <code>true</code> if the given UUID is valid.<p>
     * 
     * @param uuid the UUID to check
     * 
     * @return <code>true</code> if the given UUID is valid
     */
    public static native boolean isValidUUID(String uuid)/*-{
      var regex = /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/;
      return regex.test(uuid);
    }-*/;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_uuid;
    }
}