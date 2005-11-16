/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsResourceComparison.java,v $
 * Date   : $Date: 2005/11/16 12:12:55 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.comparison;

import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsDateUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Comparison of two OpenCms resources.<p>
 * 
 * @author Jan Baudisch
 */
public class CmsResourceComparison {

    /** Constant indicating that an item (e.g. element or property) has been added.<p> */
    public static final String TYPE_ADDED = "added";

    /** Constant indicating that an item has been changed.<p> */
    public static final String TYPE_CHANGED = "changed";

    /** Constant indicating that an item has been removed.<p> */
    public static final String TYPE_REMOVED = "removed";

    /** Constant indicating that an item has not been changed.<p> */
    public static final String TYPE_UNCHANGED = "unchanged";

    /** The compared attributes.<p> */
    private List m_comparedAttributes;

    /** The compared properties.<p> */
    private List m_comparedProperties;

    /**
     * Creates a new resource comparison.<p>
     * 
     * @param cms the cms object to use
     * @param file1 the first file to generate a comparison from
     * @param file2 the second file to generate a comparison from
     * @throws CmsException if something goes wrong
     */
    public CmsResourceComparison(CmsObject cms, CmsFile file1, CmsFile file2)
    throws CmsException {

        compareProperties(cms, file1, file2);
        compareAttributes(cms, file1, file2);
    }

    /**
     * Returns comparisons for the meta attributes of the specified files.<p>
     * 
     * @return comparisons for the meta attributes of the specified files
     */
    public List getComparedAttributes() {

        return m_comparedAttributes;
    }

    /**
     * Returns comparisons for the properties of the specified files.<p>
     * 
     * @return comparisons for the properties of the specified files
     */
    public List getComparedProperties() {

        return m_comparedProperties;
    }

    /**
     * Helper method that finds out, which of the attributes were added, removed, modified or remain unchanged.<p>
     * 
     * @param cms the CmsObject to use
     * @param file1 the first file to read the properties from
     * @param file2 the second file to read the properties from
     * 
     * @throws CmsException if something goes wrong
     */
    private void compareAttributes(CmsObject cms, CmsFile file1, CmsFile file2) {

        m_comparedAttributes = new ArrayList();
        m_comparedAttributes.add(new CmsAttributeComparison(
            "Size",
            String.valueOf(file1.getLength()),
            String.valueOf(file2.getLength())));
        String release1;
        if (CmsResource.DATE_RELEASED_DEFAULT == file1.getDateReleased()) {
            release1 = "-";
        } else {
            release1 = CmsDateUtil.getDateTime(
                new Date(file1.getDateReleased()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        String release2;
        if (CmsResource.DATE_RELEASED_DEFAULT == file2.getDateReleased()) {
            release2 = "-";
        } else {
            release2 = CmsDateUtil.getDateTime(
                new Date(file2.getDateReleased()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        m_comparedAttributes.add(new CmsAttributeComparison("Date Released", release1, release2));
        String expire1;
        if (CmsResource.DATE_EXPIRED_DEFAULT == file1.getDateExpired()) {
            expire1 = "-";
        } else {
            expire1 = CmsDateUtil.getDateTime(
                new Date(file1.getDateExpired()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        String expire2;
        if (CmsResource.DATE_EXPIRED_DEFAULT == file2.getDateExpired()) {
            expire2 = "-";
        } else {
            expire2 = CmsDateUtil.getDateTime(
                new Date(file2.getDateExpired()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        m_comparedAttributes.add(new CmsAttributeComparison("Date Expired", expire1, expire2));
        m_comparedAttributes.add(new CmsAttributeComparison(
            "Internal",
            String.valueOf((file1.getFlags() & CmsResource.FLAG_INTERNAL) > 0),
            String.valueOf((file2.getFlags() & CmsResource.FLAG_INTERNAL) > 0)));
    }

    /**
     * Helper method that finds out, which of the properties were added, removed, modified or remain unchanged.<p>
     * 
     * @param cms the CmsObject to use
     * @param file1 the first file to read the properties from
     * @param file2 the second file to read the properties from
     * 
     * @throws CmsException if something goes wrong
     */
    private void compareProperties(CmsObject cms, CmsFile file1, CmsFile file2) throws CmsException {

        m_comparedProperties = new ArrayList();
        List properties1;
        if (file1 instanceof CmsBackupResource) {
            properties1 = cms.readBackupPropertyObjects((CmsBackupResource)file1);
        } else {
            properties1 = cms.readPropertyObjects(file1, false);
        }
        List properties2;
        if (file2 instanceof CmsBackupResource) {
            properties2 = cms.readBackupPropertyObjects((CmsBackupResource)file2);
        } else {
            properties2 = cms.readPropertyObjects(file2, false);
        }
        m_comparedProperties = new ArrayList();
        List removedProperties = new ArrayList(properties1);
        removedProperties.removeAll(properties2);
        List addedProperties = new ArrayList(properties2);
        addedProperties.removeAll(properties1);
        List retainedProperties = new ArrayList(properties2);
        retainedProperties.retainAll(properties1);
        CmsProperty prop;
        Iterator i = addedProperties.iterator();
        while (i.hasNext()) {
            prop = (CmsProperty)i.next();
            m_comparedProperties.add(new CmsAttributeComparison(
                prop.getName(),
                "",
                prop.getValue(),
                CmsResourceComparison.TYPE_ADDED));
        }
        i = removedProperties.iterator();
        while (i.hasNext()) {
            prop = (CmsProperty)i.next();
            m_comparedProperties.add(new CmsAttributeComparison(
                prop.getName(),
                prop.getValue(),
                "",
                CmsResourceComparison.TYPE_REMOVED));
        }
        i = retainedProperties.iterator();
        while (i.hasNext()) {
            prop = (CmsProperty)i.next();
            String value1 = ((CmsProperty)properties1.get(properties1.indexOf(prop))).getValue();
            String value2 = ((CmsProperty)properties2.get(properties1.indexOf(prop))).getValue();
            if (value1.equals(value2)) {
                m_comparedProperties.add(new CmsAttributeComparison(
                    prop.getName(),
                    value1,
                    value2,
                    CmsResourceComparison.TYPE_UNCHANGED));
            } else {
                m_comparedProperties.add(new CmsAttributeComparison(
                    prop.getName(),
                    value1,
                    value2,
                    CmsResourceComparison.TYPE_CHANGED));
            }
        }
    }
}
