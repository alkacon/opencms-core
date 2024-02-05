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
 * Extended data structure for the collector, parsed from the collector parameters.<p>
 *
 * The input data String must have the following format:<br>
 * <code>"{VFS URI}|{Resource type}|{Count}|excludeTimerange|{AddParam1}|{AddParam2}..."</code>, for example:<br>
 * <code>"/my/folder/|xmlcontent|5|excludeTimerange|p1|p2|p3|p4"</code> or <code>"/my/folder/|xmlcontent|5|p1|p2|p3|p4"</code>.<p>
 *
 * This extends the basic {@link CmsCollectorData} by allowing to append additional
 * parameters to the input String. The parameters can then be obtained by the collector
 * using {@link #getAdditionalParams()}. It will depend on the collector implementation
 * how these additional parameters are used.<p>
 *
 * @since 7.0.2
 *
 * @see CmsCollectorData
 */
public class CmsExtendedCollectorData extends CmsCollectorData {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExtendedCollectorData.class);

    /** The list of additional parameters. */
    private List<String> m_additionalParams;

    /**
     * Required constructor for subclasses.<p>
     */
    protected CmsExtendedCollectorData() {

        // NOOP
    }

    /**
     * Creates a new extended collector data set.<p>
     *
     * The input data String must have the following format:<br>
     * <code>"{VFS URI}|{Resource type}|{Count}|{AddParam1}|{AddParam2}..."</code>, for example:<br>
     * <code>"/my/folder/|xmlcontent|5|excludeTimerange|p1|p2|p3|p4"</code> or <code>"/my/folder/|xmlcontent|5|p1|p2|p3|p4"</code>.<p>
     *
     * @param data the data to parse
     */
    public CmsExtendedCollectorData(String data) {

        if (data == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_COLLECTOR_PARAM_EMPTY_0));
        }
        List<String> args = CmsStringUtil.splitAsList(data, '|', true);
        if (args.size() < 3) {
            // we need at least 2 arguments: VFS URI and Resource Type
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_COLLECTOR_PARAM_INVALID_1, data));
        }
        setFileName(args.get(0));
        String type = args.get(1);
        try {
            // try to look up the resource type
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(type);
            setType(resourceType.getTypeId());
        } catch (CmsLoaderException e1) {
            // maybe the int id is directly used?
            try {
                int typeInt = Integer.parseInt(type);
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeInt);
                setType(resourceType.getTypeId());
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_RESTYPE_INTID_2,
                            resourceType.getTypeName(),
                            Integer.valueOf(resourceType.getTypeId())));
                }
            } catch (NumberFormatException e2) {
                // bad number format used for type
                throw new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_COLLECTOR_PARAM_INVALID_1, data),
                    e2);
            } catch (CmsLoaderException e2) {
                // this resource type does not exist
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_1, type), e2);
            }
        }
        setCount(5);
        if (args.size() > 2) {
            String count = args.get(2);
            if (CmsStringUtil.isNotEmpty(count)) {
                try {
                    setCount(Integer.parseInt(count));
                } catch (NumberFormatException e) {
                    // bad number format used for count
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_COLLECTOR_PARAM_INVALID_1, data),
                        e);
                }
            }
        }
        if (args.size() > 3) {
            if (PARAM_EXCLUDETIMERANGE.equalsIgnoreCase(args.get(3))) {
                setExcludeTimerange(true);
                if (args.size() > 4) {
                    m_additionalParams = args.subList(4, args.size());
                }
            } else {
                m_additionalParams = args.subList(3, args.size());
            }
        }
    }

    /**
     * Returns the List of additional parameters (String objects).<p>
     *
     * @return the List of additional parameters (String objects)
     */
    public List<String> getAdditionalParams() {

        return m_additionalParams;
    }

    /**
     * Sets the List of additional parameters (String objects).<p>
     *
     * @param additionalParams the List of additional parameters (String objects) to set
     */
    protected void setAdditionalParams(List<String> additionalParams) {

        m_additionalParams = additionalParams;
    }
}