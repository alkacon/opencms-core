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

package org.opencms.module;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;

/**
 * A version number for an OpenCms module.<p>
 *
 * A module version number has the form <code>n1.n2.n3.n4</code>.
 * Only <code>n1</code> is required, <code>n2 - n4</code> are optional.<p>
 *
 * The valid range for each <code>n</code> is 0 - 999.
 * Examples for valid version numbers are <code>0.9</code>, <code>1.0.0.5</code>
 * or <code>5</code>.
 * The maximum version number is <code>999.999.999.999</code>.<p>
 *
 * The comparison is started with <code>n1</code> being the most important value,
 * followed by <code>n2 - n4</code>.
 * For example <code>5.0.0.1 > 4.999.999.999</code> since <code>5 > 4</code>.<p>
 *
 * For any <code>n1 - n4</code>, if <code>n > 0</code> leading zeros are ignored.
 * So <code>001.002.004.004 = 1.2.3.4</code>. Unrequired leading zeros are automatically
 * stripped from version numbers.<p>
 *
 * @since 6.0.0
 */
public class CmsModuleVersion implements Comparable<Object> {

    /** Default version for new modules. */
    public static final String DEFAULT_VERSION = "0.1";

    /** The dot count of the version. */
    private int m_dots;

    /** The version number (for comparisons). */
    private long m_number;

    /** Indicates if the module version was already updated. */
    private boolean m_updated;

    /** The version String. */
    private String m_version;

    /**
     * Creates a new module version based on a String.<p>
     *
     * @param version the version to set
     */
    public CmsModuleVersion(String version) {

        setVersion(version);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsModuleVersion) {
            if (m_version == null) {
                return 0;
            }
            CmsModuleVersion other = (CmsModuleVersion)obj;
            if (m_number == other.m_number) {
                return 0;
            }
            return (m_number > other.m_number) ? 1 : -1;
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsModuleVersion) {
            CmsModuleVersion other = (CmsModuleVersion)obj;
            if (m_version == null) {
                return (other.m_version == null);
            }
            return m_version.equals(other.m_version);
        }
        return false;
    }

    /**
     * Returns the current version String.<p>
     *
     * @return the current version String
     */
    public String getVersion() {

        return m_version;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_version == null) {
            return 0;
        }

        return m_version.hashCode();
    }

    /**
     * Sets the version as a String.<p>
     *
     * @param version the version String to set
     */
    public void setVersion(String version) {

        m_number = 0L;
        if ((version == null) || (version.charAt(0) == '.') || (version.charAt(version.length() - 1) == '.')) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NOT_NUMBER_0));
        }
        String[] split = CmsStringUtil.splitAsArray(version, '.');
        m_dots = split.length;
        if (m_dots > 4) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_INVALID_VERSION_LENGTH_1, version));
        }
        String[] numbers = new String[5];
        System.arraycopy(split, 0, numbers, 1, m_dots);
        numbers[0] = "1";
        for (int i = 1 + m_dots; i < 5; i++) {
            numbers[i] = "0";
        }
        for (int i = numbers.length - 1; i >= 0; i--) {
            try {
                int number = Integer.valueOf(numbers[numbers.length - i - 1]).intValue();

                if ((number > 999) || (number < 0)) {
                    throw new CmsIllegalArgumentException(
                        Messages.get().container(Messages.ERR_INVALID_VERSION_SUBNUMBER_1, new Integer(number)));
                }
                m_number = ((long)Math.pow(1000.0, i) * number) + m_number;
            } catch (NumberFormatException e) {
                // no valid version provided
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NOT_NUMBER_0));
            }
        }

        setVersion(m_number);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getVersion();
    }

    /**
     * Increments this version number by 1 in the last digit.<p>
     */
    protected void increment() {

        if (m_number < 1999999999999L) {
            m_number += (long)Math.pow(1000.0, (4 - m_dots));
            setVersion(m_number);
        } else {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_MODULE_VERSION_NUMBER_0));
        }
    }

    /**
     * Returns the updated status.<p>
     *
     * @return the updated status
     */
    protected boolean isUpdated() {

        return m_updated;
    }

    /**
     * Sets the updated status.<p>
     *
     * @param updated the updated status to set
     */
    protected void setUpdated(boolean updated) {

        m_updated = updated;
    }

    /**
     * Sets the version as a number.<p>
     *
     * @param number the version number to set
     */
    private void setVersion(long number) {

        String result = "";
        for (int i = 0; i < 4; i++) {
            long mod = number % 1000L;
            number = number / 1000L;
            if (m_dots >= (4 - i)) {
                if (m_dots > (4 - i)) {
                    result = '.' + result;
                }
                result = "" + mod + result;
            }
        }

        m_version = result;
    }
}