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

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.logging.Log;

import org.safehaus.uuid.EthernetAddress;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

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
 * @since 6.0.0
 */
public final class CmsUUID extends Object implements Cloneable, Comparable<CmsUUID>, Externalizable {

    /** A regular expression for matching UUIDs. */
    public static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUUID.class);

    /** Ethernet address of the server machine. */
    private static EthernetAddress m_ethernetAddress;

    /** OpenCms UUID (name based uuid of "www.opencms.org" in the dns name space). */
    private static UUID m_opencmsUUID = UUIDGenerator.getInstance().generateNameBasedUUID(
        new UUID(UUID.NAMESPACE_DNS),
        "www.opencms.org");

    /** Constant for the null UUID. */
    private static final CmsUUID NULL_UUID = new CmsUUID(UUID.getNullUUID());

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 1736324454709298676L;

    /** Internal UUID implementation. */
    private transient UUID m_uuid;

    /**
     * Creates a new UUID.<p>
     *
     * Please note that the static init() method has to be called first to initialize the
     * internet address of the machine.<p>
     */
    public CmsUUID() {

        if (m_ethernetAddress == null) {
            // if no ethernet address is available, generate a dummy
            // this is required because otherwise we can't ever de-serialize a CmsUUID outside of OpenCms,
            // since the empty constructor is called when the de-serialization takes place
            init(CmsStringUtil.getEthernetAddress());
        }
        m_uuid = UUIDGenerator.getInstance().generateTimeBasedUUID(m_ethernetAddress);
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

        if (canBeNull && (id == null)) {
            return;
        }
        if ((!canBeNull && (id == null)) || id.isNullUUID()) {
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
     * use this null UUID to check if a UUID has been initialized or not.<p>
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
            throw new CmsInitException(
                Messages.get().container(Messages.ERR_INVALID_ETHERNET_ADDRESS_1, ethernetAddress));
        }
    }

    /**
     * Returns <code>true</code> if the given UUID is valid.<p>
     *
     * @param uuid the UUID to check
     *
     * @return <code>true</code> if the given UUID is valid
     */
    public static boolean isValidUUID(String uuid) {

        try {
            return (null != uuid) && (null != UUID.valueOf(uuid));
        } catch (NumberFormatException e) {
            // return false
        }
        return false;
    }

    /**
     * Returns the given String transformed to a UUID in case the String is a valid UUID.<p>
     *
     * @param uuid the String to transform to a UUID
     *
     * @return the given String transformed to a UUID in case the String is a valid UUID
     *
     * @throws NumberFormatException in case the String is no valid UUID
     */
    public static CmsUUID valueOf(String uuid) throws NumberFormatException {

        return new CmsUUID(UUID.valueOf(uuid));
    }

    /**
     * Creates a clone of this CmsUUID.<p>
     *
     * @return a clone of this CmsUUID
     */
    @Override
    public Object clone() {

        if (this == NULL_UUID) {
            return NULL_UUID;
        }
        return new CmsUUID((UUID)m_uuid.clone());
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
        return m_uuid.equals(UUID.getNullUUID());
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) {

        Object o = null;
        try {
            o = in.readObject();
        } catch (Throwable e) {
            // there are 2 development version of OpenCms (6.1.7 and 6.1.8) which had a different format,
            // here the Object was preceded by a Long
            try {
                // first read the long, we don't really need it but it must be removed from the stream
                in.readLong();
                o = in.readObject();
            } catch (Throwable t) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_READ_UUID_OLD_1, o), t);
                }
            }
        }

        if (o instanceof String) {
            // this UUID has been serialized using the new method
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_READ_UUID_1, o));
            }
            m_uuid = new UUID((String)o);
        }

        // log an error if the uuid could not be deserialized
        if (m_uuid == null) {
            // UUID cannot be deserialized
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ERR_READ_UUID_0));
            }
        }
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
    @Override
    public String toString() {

        return m_uuid.toString();
    }

    /**
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_WRITE_UUID_1, toString()));
        }
        out.writeObject(toString());
    }
}