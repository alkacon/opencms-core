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

package org.opencms.file.collectors;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Data structure for the collector, parsed from the collector parameters.<p>
 *
 * The input data String must have the following format:<br>
 * <code>"{VFS URI}|{Resource type}|{Count}|excludeTimerange"</code>.<br>
 * The <code>{Count}</code> and <code>excludeTimerange</code> values are optional.<br>
 * Example:<br>
 * <code>"/my/folder/|xmlcontent|5|excludeTimerange"</code>.<p>
 *
 * @since 6.0.0
 *
 * @see CmsExtendedCollectorData
 */
public class CmsCollectorData {

    /** The value of the optional parameter to exclude the time range. */
    public static final String PARAM_EXCLUDETIMERANGE = "excludeTimerange";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCollectorData.class);

    /** The display count. */
    private int m_count;

    /** The flag to exclude the time range in an offline project. */
    private boolean m_excludeTimerange;

    /** The absolute file name. */
    private String m_fileName;

    /** The file type id. */
    private int m_type;

    /**
     * Creates a new extended collector data set.<p>
     *
     * The input data String must have the following format:<br>
     * <code>"{VFS URI}|{Resource type}|{Count}|excludeTimerange"</code>, for example:<br>
     * <code>"/my/folder/|xmlcontent|5|excludeTimerange"</code>.<p>
     *
     * @param data the data to parse
     */
    public CmsCollectorData(String data) {

        if (data == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_COLLECTOR_PARAM_EMPTY_0));
        }

        if (!data.contains("|")) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_COLLECTOR_PARAM_INVALID_1, data));
        }

        List<String> args = CmsStringUtil.splitAsList(data, '|', true);

        m_fileName = args.get(0);
        String type = args.get(1);
        m_count = 0;
        if (args.size() >= 3) {
            String value = args.get(2);
            if (PARAM_EXCLUDETIMERANGE.equalsIgnoreCase(value)) {
                m_excludeTimerange = true;
            } else {
                try {
                    m_count = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new CmsIllegalArgumentException(
                        Messages.get().container(Messages.ERR_COLLECTOR_PARAM_INVALID_1, data));
                }
            }
            if ((args.size() == 4) && PARAM_EXCLUDETIMERANGE.equalsIgnoreCase(args.get(3))) {
                m_excludeTimerange = true;
            }
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(type)) {
            try {
                // try to look up the resource type
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(type);
                m_type = resourceType.getTypeId();
            } catch (CmsLoaderException e) {
                // maybe the int id is directly used?
                try {
                    int typeInt = Integer.valueOf(type).intValue();
                    I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeInt);
                    m_type = resourceType.getTypeId();
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_RESTYPE_INTID_2,
                                resourceType.getTypeName(),
                                Integer.valueOf(resourceType.getTypeId())));
                    }
                } catch (CmsLoaderException | NumberFormatException e1) {
                    // this resource type does not exist
                    throw new CmsRuntimeException(Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_1, type), e1);
                }
            }
        } else {
            m_type = -1;
        }
    }

    /**
     * Required constructor for subclasses.<p>
     */
    protected CmsCollectorData() {

        // NOOP
    }

    /**
     * Returns the count.<p>
     *
     * @return the count
     */
    public int getCount() {

        return m_count;
    }

    /**
     * Returns the file name.<p>
     *
     * @return the file name
     */
    public String getFileName() {

        return m_fileName;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the flag to exclude the time range in an offline project.<p>
     *
     * @return the flag to exclude the time range in an offline project
     */
    public boolean isExcludeTimerange() {

        return m_excludeTimerange;
    }

    /**
     * Sets the flag to exclude the time range in an offline project.<p>
     *
     * @param excludeTimerange the flag to exclude the time range in an offline project
     */
    public void setExcludeTimerange(boolean excludeTimerange) {

        m_excludeTimerange = excludeTimerange;
    }

    /**
     * Sets the fileName.<p>
     *
     * @param fileName the file name to set
     */
    public void setFileName(String fileName) {

        m_fileName = fileName;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(int type) {

        m_type = type;
    }

    /**
     * Sets the count.<p>
     *
     * @param count the count
     */
    protected void setCount(int count) {

        m_count = count;
    }
}