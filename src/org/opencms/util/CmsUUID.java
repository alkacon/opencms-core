/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsUUID.java,v $
 * Date   : $Date: 2005/09/06 15:25:20 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsRuntimeException;

import java.io.Serializable;

import org.doomdark.uuid.EthernetAddress;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;

/**
 * Generates a UUID using spatial and temporal uniqueness.<p> 
 * 
 * Spatial uniqueness is derived from
 * ethernet address (MAC, 802.1); temporal from system clock.<p>
 * 
 * For more information about the algorithm used, please see 
 * <a href="http://www.opengroup.org/dce/info/draft-leach-uuids-guids-01.txt">
 * draft-leach-uuids-guids-01.txt</a>.<p>
 * 
 * Because Java is unable to read the MAC address of the machine 
 * (without using JNI), the MAC address has to be provided first 
 * by using the static {@link #init(String)} method.<p>
 * 
 * This class is just a facade wrapper for the "real" UUID implementation.<p> 
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.19 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsUUID extends Object implements Serializable, Cloneable, Comparable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 1726324354709298575L;

    /** Ethernet addess of the server machine. */
    private static EthernetAddress m_ethernetAddress;

    /** Flag to indicate if the ethernet address has been initialized. */
    private static boolean m_isNotInitialized = true;

    /** OpenCms UUID (name based uuid of "www.opencms.org" in the dns name space). */
    private static UUID m_opencmsUUID = UUIDGenerator.getInstance().generateNameBasedUUID(
        new UUID(UUID.NAMESPACE_DNS),
        "www.opencms.org");

    /** Constant for the null UUID. */
    private static final CmsUUID NULL_UUID = new CmsUUID(UUID.getNullUUID());

    /** Internal UUID implementation. */
    private UUID m_uuid;

    /**
     * Creates a new UUID.<p>
     * 
     * Please note that the static init() method has to be called first to initialize the 
     * enternet address of the machine.<p>
     */
    public CmsUUID() {

        synchronized (this) {
            if (m_isNotInitialized) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_INVALID_ETHERNET_ADDRESS_0));
            }
            m_uuid = UUIDGenerator.getInstance().generateTimeBasedUUID(m_ethernetAddress);
        }
    }

    /**
     * Create a UUID based on a binary data array.<p>
     * 
     * @param data a binary data array representing a UUID
     */
    public CmsUUID(byte[] data) {

        m_uuid = new UUID(data);
    }

    /**
     * Create a UUID based on a String.<p>
     * 
     * @param uuid a String representing a UUID
     * @throws NumberFormatException in case uuid is not a valid UUID
     */
    public CmsUUID(String uuid)
    throws NumberFormatException {

        m_uuid = new UUID(uuid);
    }

    /**
     * Create a new UUID based on another one (used internal for cloning).<p>
     * 
     * @param uuid the UUID to clone
     */
    private CmsUUID(UUID uuid) {

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

        if (canBeNull && id == null) {
            return;
        }
        if ((!canBeNull && id == null) || id.isNullUUID()) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_UUID_1, id));
        }
    }

    /**
     * Returns a constant (name based) UUID,
     * based on the given name in the OpenCms name space.
     * 
     * @param name the name to derive the uuid from
     * @return name based UUID of the given name
     */
    public static CmsUUID getConstantUUID(String name) {

        return new CmsUUID(UUIDGenerator.getInstance().generateNameBasedUUID(m_opencmsUUID, name));
    }

    /**
     * Returns a String representing a dummy (random based) ethernet address.<p>
     * 
     * @return a String representing a dummy (random based) ethernet address
     */
    public static String getDummyEthernetAddress() {

        return UUIDGenerator.getInstance().getDummyAddress().toString();
    }

    /**
     * Returns a null UUID,
     * use this null UUID to check if a UUID has been initilized or not.<p>
     * 
     * @return a null UUID
     */
    public static CmsUUID getNullUUID() {

        return NULL_UUID;
    }

    /**
     * Returns a constant (name based) UUID for OpenCms,
     * based on "www.opencms.org" in the dns name space.
     * 
     * @return name based UUID of OpenCms
     */
    public static CmsUUID getOpenCmsUUID() {

        return new CmsUUID(m_opencmsUUID);
    }

    /**
     * Initialize the UUID generator with the ethernet address of the server machine.<p>
     * 
     * The ethernetAddress parameter must represent a 'standard' ethernet MAC address string
     * (e.g. '00:C0:F0:3D:5B:7C').
     * 
     * @param ethernetAddress the ethernet address of the server machine
     * @throws CmsInitException in case the ethernetAddress String is not a valid ethernet address
     */
    public static void init(String ethernetAddress) throws CmsInitException {

        try {
            m_ethernetAddress = new EthernetAddress(ethernetAddress);
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(
                Messages.ERR_INVALID_ETHERNET_ADDRESS_1,
                ethernetAddress));
        }
        m_isNotInitialized = false;
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
        return new CmsUUID((UUID)m_uuid.clone());
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj instanceof CmsUUID) {
            return m_uuid.compareTo(((CmsUUID)obj).m_uuid);
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj instanceof CmsUUID) {
            return ((CmsUUID)obj).m_uuid.equals(m_uuid);
        }
        return false;
    }

    /**
     * Optimized hashCode implementation for UUID's.<p>
     * 
     * @see java.lang.Object#hashCode()
     */
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
        return m_uuid.equals(UUID.getNullUUID());
    }

    /**
     * Returns the UUID as a 16-byte byte array.<p>
     *
     * @return 16-byte byte array that contains the UUID's bytes in the network byte order
     */
    public byte[] toByteArray() {

        return m_uuid.toByteArray();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_uuid.toString();
    }

}