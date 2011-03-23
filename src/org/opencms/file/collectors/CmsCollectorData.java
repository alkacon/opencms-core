/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/CmsCollectorData.java,v $
 * Date   : $Date: 2011/03/23 14:50:53 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.collectors;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import org.apache.commons.logging.Log;

/**
 * Data structure for the collector, parsed from the collector parameters.<p>
 * 
 * The input data String must have the following format:<br>
 * <code>"{VFS URI}|{Resource type}|{Count}"</code>, for example:<br>
 * <code>"/my/folder/|xmlcontent|5"</code>.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.16 $
 * 
 * @since 6.0.0
 * 
 * @see CmsExtendedCollectorData
 */
public class CmsCollectorData {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCollectorData.class);

    /** The display count. */
    private int m_count;

    /** The absolute file name. */
    private String m_fileName;

    /** The file type id. */
    private int m_type;

    /**
     * Creates a new extended collector data set.<p>
     * 
     * The input data String must have the following format:<br>
     * <code>"{VFS URI}|{Resource type}|{Count}"</code>, for example:<br>
     * <code>"/my/folder/|xmlcontent|5"</code>.<p>
     * 
     * @param data the data to parse
     */
    public CmsCollectorData(String data) {

        if (data == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_COLLECTOR_PARAM_EMPTY_0));
        }

        int pos1 = data.indexOf('|');
        if (pos1 == -1) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_COLLECTOR_PARAM_INVALID_1, data));
        }

        int pos2 = data.indexOf('|', pos1 + 1);
        if (pos2 == -1) {
            pos2 = data.length();
            m_count = 0;
        } else {
            m_count = Integer.valueOf(data.substring(pos2 + 1)).intValue();
        }

        m_fileName = data.substring(0, pos1);
        String type = data.substring(pos1 + 1, pos2);
        try {
            // try to look up the resource type
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(type);
            m_type = resourceType.getTypeId();
        } catch (CmsLoaderException e) {
            // maybe the int id is directly used?
            int typeInt = Integer.valueOf(type).intValue();
            try {
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeInt);
                m_type = resourceType.getTypeId();
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_RESTYPE_INTID_2,
                        resourceType.getTypeName(),
                        new Integer(resourceType.getTypeId())));
                }
            } catch (CmsLoaderException e1) {
                // this resource type does not exist
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_1, type), e1);
            }
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