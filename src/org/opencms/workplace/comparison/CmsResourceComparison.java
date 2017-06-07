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

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsDateUtil;
import org.opencms.workplace.commons.Messages;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Comparison of two OpenCms resources.<p>
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

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceComparison.class);

    /**
     * Constructs a new resource comparison object.<p>
     *
     */
    protected CmsResourceComparison() {

        super();
    }

    /**
     * Helper method that collects all meta attributes of the two file versions and
     * finds out, which of the attributes were added, removed, modified or remain unchanged.<p>
     *
     * @param cms the CmsObject to use
     * @param resource1 the first resource to read the properties from
     * @param resource2 the second resource to read the properties from
     *
     * @return a list of the compared attributes
     */
    public static List<CmsAttributeComparison> compareAttributes(
        CmsObject cms,
        CmsResource resource1,
        CmsResource resource2) {

        List<CmsAttributeComparison> comparedAttributes = new ArrayList<CmsAttributeComparison>();
        comparedAttributes.add(
            new CmsAttributeComparison(
                Messages.GUI_HISTORY_COLS_SIZE_0,
                String.valueOf(resource1.getLength()),
                String.valueOf(resource2.getLength())));
        String release1;
        if (CmsResource.DATE_RELEASED_DEFAULT == resource1.getDateReleased()) {
            release1 = "-";
        } else {
            release1 = CmsDateUtil.getDateTime(
                new Date(resource1.getDateReleased()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        String release2;
        if (CmsResource.DATE_RELEASED_DEFAULT == resource2.getDateReleased()) {
            release2 = "-";
        } else {
            release2 = CmsDateUtil.getDateTime(
                new Date(resource2.getDateReleased()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        comparedAttributes.add(new CmsAttributeComparison(Messages.GUI_LABEL_DATE_RELEASED_0, release1, release2));
        String expire1;
        if (CmsResource.DATE_EXPIRED_DEFAULT == resource1.getDateExpired()) {
            expire1 = "-";
        } else {
            expire1 = CmsDateUtil.getDateTime(
                new Date(resource1.getDateExpired()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        String expire2;
        if (CmsResource.DATE_EXPIRED_DEFAULT == resource2.getDateExpired()) {
            expire2 = "-";
        } else {
            expire2 = CmsDateUtil.getDateTime(
                new Date(resource2.getDateExpired()),
                DateFormat.SHORT,
                cms.getRequestContext().getLocale());
        }
        comparedAttributes.add(new CmsAttributeComparison(Messages.GUI_LABEL_DATE_EXPIRED_0, expire1, expire2));
        comparedAttributes.add(
            new CmsAttributeComparison(
                Messages.GUI_PERMISSION_INTERNAL_0,
                String.valueOf(resource1.isInternal()),
                String.valueOf(resource2.isInternal())));
        String dateLastModified1 = CmsDateUtil.getDateTime(
            new Date(resource1.getDateLastModified()),
            DateFormat.SHORT,
            cms.getRequestContext().getLocale());
        String dateLastModified2 = CmsDateUtil.getDateTime(
            new Date(resource2.getDateLastModified()),
            DateFormat.SHORT,
            cms.getRequestContext().getLocale());
        comparedAttributes.add(
            new CmsAttributeComparison(Messages.GUI_LABEL_DATE_LAST_MODIFIED_0, dateLastModified1, dateLastModified2));
        try {
            String type1 = OpenCms.getResourceManager().getResourceType(resource1.getTypeId()).getTypeName();
            String type2 = OpenCms.getResourceManager().getResourceType(resource2.getTypeId()).getTypeName();
            comparedAttributes.add(new CmsAttributeComparison(Messages.GUI_HISTORY_COLS_FILE_TYPE_0, type1, type2));
        } catch (CmsLoaderException e) {
            LOG.debug(e.getMessage(), e);
        }
        String dateCreated1 = CmsDateUtil.getDateTime(
            new Date(resource1.getDateCreated()),
            DateFormat.SHORT,
            cms.getRequestContext().getLocale());
        String dateCreated2 = CmsDateUtil.getDateTime(
            new Date(resource2.getDateCreated()),
            DateFormat.SHORT,
            cms.getRequestContext().getLocale());
        comparedAttributes.add(
            new CmsAttributeComparison(Messages.GUI_HISTORY_COLS_DATE_PUBLISHED_0, dateCreated1, dateCreated2));
        try {
            String userLastModified1 = resource1.getUserLastModified().toString();
            try {
                userLastModified1 = CmsPrincipal.readPrincipalIncludingHistory(
                    cms,
                    resource1.getUserLastModified()).getName();
            } catch (CmsDbEntryNotFoundException e) {
                // ignore
            }
            String userLastModified2 = resource2.getUserLastModified().toString();
            try {
                userLastModified2 = CmsPrincipal.readPrincipalIncludingHistory(
                    cms,
                    resource2.getUserLastModified()).getName();
            } catch (CmsDbEntryNotFoundException e) {
                // ignore
            }
            comparedAttributes.add(
                new CmsAttributeComparison(
                    Messages.GUI_LABEL_USER_LAST_MODIFIED_0,
                    userLastModified1,
                    userLastModified2));
        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        }
        String path1 = cms.getRequestContext().removeSiteRoot(resource1.getRootPath());
        String path2 = cms.getRequestContext().removeSiteRoot(resource2.getRootPath());
        comparedAttributes.add(new CmsAttributeComparison(Messages.GUI_HISTORY_COLS_RESOURCE_PATH_0, path1, path2));
        return comparedAttributes;
    }

    /**
     * Helper method that finds out, which of the properties were added, removed, modified or remain unchanged.<p>
     *
     * @param cms the CmsObject to use
     * @param resource1 the first resource to read the properties from
     * @param version1 the version of the first resource
     * @param resource2 the second resource to read the properties from
     * @param version2 the version of the second resource
     *
     * @return a list of the compared attributes
     *
     * @throws CmsException if something goes wrong
     */
    public static List<CmsAttributeComparison> compareProperties(
        CmsObject cms,
        CmsResource resource1,
        String version1,
        CmsResource resource2,
        String version2) throws CmsException {

        List<CmsProperty> properties1;
        if (resource1 instanceof I_CmsHistoryResource) {
            properties1 = cms.readHistoryPropertyObjects((I_CmsHistoryResource)resource1);
        } else {
            if (Integer.parseInt(version1) < 0) {
                // switch to the online project
                CmsProject prj = cms.getRequestContext().getCurrentProject();
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    properties1 = cms.readPropertyObjects(resource1, false);
                } finally {
                    cms.getRequestContext().setCurrentProject(prj);
                }
            } else {
                properties1 = cms.readPropertyObjects(resource1, false);
            }
        }
        List<CmsProperty> properties2;
        if (resource2 instanceof I_CmsHistoryResource) {
            properties2 = cms.readHistoryPropertyObjects((I_CmsHistoryResource)resource2);
        } else {
            if (Integer.parseInt(version2) < 0) {
                // switch to the online project
                CmsProject prj = cms.getRequestContext().getCurrentProject();
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    properties2 = cms.readPropertyObjects(resource2, false);
                } finally {
                    cms.getRequestContext().setCurrentProject(prj);
                }
            } else {
                properties2 = cms.readPropertyObjects(resource2, false);
            }
        }
        List<CmsAttributeComparison> comparedProperties = new ArrayList<CmsAttributeComparison>();
        List<CmsProperty> removedProperties = new ArrayList<CmsProperty>(properties1);
        removedProperties.removeAll(properties2);
        List<CmsProperty> addedProperties = new ArrayList<CmsProperty>(properties2);
        addedProperties.removeAll(properties1);
        List<CmsProperty> retainedProperties = new ArrayList<CmsProperty>(properties2);
        retainedProperties.retainAll(properties1);
        CmsProperty prop;
        Iterator<CmsProperty> i = addedProperties.iterator();
        while (i.hasNext()) {
            prop = i.next();
            comparedProperties.add(
                new CmsAttributeComparison(prop.getName(), "", prop.getValue(), CmsResourceComparison.TYPE_ADDED));
        }
        i = removedProperties.iterator();
        while (i.hasNext()) {
            prop = i.next();
            comparedProperties.add(
                new CmsAttributeComparison(prop.getName(), prop.getValue(), "", CmsResourceComparison.TYPE_REMOVED));
        }
        i = retainedProperties.iterator();
        while (i.hasNext()) {
            prop = i.next();
            String value1 = properties1.get(properties1.indexOf(prop)).getValue();
            String value2 = properties2.get(properties2.indexOf(prop)).getValue();
            if (value1.equals(value2)) {
                comparedProperties.add(
                    new CmsAttributeComparison(prop.getName(), value1, value2, CmsResourceComparison.TYPE_UNCHANGED));
            } else {
                comparedProperties.add(
                    new CmsAttributeComparison(prop.getName(), value1, value2, CmsResourceComparison.TYPE_CHANGED));
            }
        }
        return comparedProperties;
    }
}
