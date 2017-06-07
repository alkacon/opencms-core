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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comparator for sorting resource objects based on dates.<p>
 *
 * The comparator can be configured to use any date based on resource attributes or properties.
 * The user must in the constructor {@link #CmsDateResourceComparator(CmsObject, List, boolean)}
 * provide a list of one or more <b>date identifiers</b> that should be checked, in the order they
 * should be checked. This list of dates identifiers must be Strings which tell the comparator which dates to use.
 * The first valid date identifier that is found for a resource is used as date for
 * comparing this resource to other resources.<p>
 *
 * The following date identifiers can be used
 * to access the corresponding value of a {@link CmsResource}:<ul>
 * <li><code>"dateCreated"</code>, which means {@link CmsResource#getDateCreated()}.
 * <li><code>"dateLastModified"</code>, which means {@link CmsResource#getDateLastModified()}.
 * <li><code>"dateContent"</code>, which means {@link CmsResource#getDateContent()}.
 * <li><code>"dateReleased"</code>, which means {@link CmsResource#getDateReleased()}.
 * <li><code>"dateExpired"</code>, which means {@link CmsResource#getDateExpired()}.
 * <li>Anything else will be treated as property name, so it will be attempted to read a property
 * with that name from the resource, and convert that value to a long. Should this fail
 * for any reason, the next entry from the list of dates will be processed.<p>
 * <li>If no match is found at all, {@link CmsResource#getDateCreated()} is used as default.
 * </ul>
 *
 * Serves as {@link java.util.Comparator} for resources and as comparator key for the resource
 * at the same time. Uses lazy initializing of comparator keys for a resource.<p>
 *
 * @since 6.0.0
 */
public class CmsDateResourceComparator implements Comparator<CmsResource> {

    /** Possible keywords to read dates from the resource attributes. */
    private static final String[] DATE_ATTRIBUTES = {
        "dateCreated",
        "dateLastModified",
        "dateContent",
        "dateReleased",
        "dateExpired"};

    /**  Possible keywords to read dates from the resource attributes in a List. */
    public static final List<String> DATE_ATTRIBUTES_LIST = Arrays.asList(DATE_ATTRIBUTES);

    /** The date sort order. */
    private boolean m_asc;

    /** The current OpenCms user context. */
    private CmsObject m_cms;

    /** The date of this comparator key. */
    private long m_date;

    /** The list that describes the dates to check, in the order they should be checked. */
    private List<String> m_dateIdentifiers;

    /** The internal map of comparator keys. */
    private Map<CmsUUID, CmsDateResourceComparator> m_keys;

    /**
     * Creates a new instance of this comparator key.<p>
     *
     * @param cms the current OpenCms user context
     * @param dateIdentifiers the names of the dates to check
     * @param asc if true, the date sort order is ascending, otherwise descending
     */
    public CmsDateResourceComparator(CmsObject cms, List<String> dateIdentifiers, boolean asc) {

        m_cms = cms;
        m_asc = asc;
        m_dateIdentifiers = dateIdentifiers;
        if (m_dateIdentifiers == null) {
            m_dateIdentifiers = Collections.emptyList();
        }
        m_keys = new HashMap<CmsUUID, CmsDateResourceComparator>();
    }

    /**
     * Creates a new, empty instance of this comparator key, used for the calculated map valued.<p>
     */
    private CmsDateResourceComparator() {

        // NOOP
    }

    /**
     * Calculates the date to use for comparison of this resource based on the given date identifiers.<p>
     *
     * @param cms the current OpenCms user context
     * @param resource the resource to create the key for
     * @param dateIdentifiers the date identifiers to use for selecting the date
     * @param defaultValue the default value to use in case no value can be calculated
     *
     * @return the calculated date
     *
     * @see CmsDateResourceComparator for a description about how the date identifieres are used
     */
    public static long calculateDate(
        CmsObject cms,
        CmsResource resource,
        List<String> dateIdentifiers,
        long defaultValue) {

        long result = 0;
        List<CmsProperty> properties = null;
        for (int i = 0, size = dateIdentifiers.size(); i < size; i++) {
            // check all configured comparisons
            String date = dateIdentifiers.get(i);
            int pos = DATE_ATTRIBUTES_LIST.indexOf(date);
            switch (pos) {
                case 0: // "dateCreated"
                    result = resource.getDateCreated();
                    break;
                case 1: // "dateLastModified"
                    result = resource.getDateLastModified();
                    break;
                case 2: // "dateContent"
                    if (resource.isFile()) {
                        // date content makes no sense for folders
                        result = resource.getDateContent();
                    }
                    break;
                case 3: // "dateReleased"
                    long dr = resource.getDateReleased();
                    if (dr != CmsResource.DATE_RELEASED_DEFAULT) {
                        // default release date must be ignored
                        result = dr;
                    }
                    break;
                case 4: // "dateExpired"
                    long de = resource.getDateExpired();
                    if (de != CmsResource.DATE_EXPIRED_DEFAULT) {
                        // default expiration date must be ignored
                        result = de;
                    }
                    break;
                default:
                    // of this is not an attribute, assume this is a property
                    if (properties == null) {
                        // we may not have to read the properties since the user may only use attributes,
                        // so use lazy initializing here
                        try {
                            properties = cms.readPropertyObjects(resource, false);
                        } catch (CmsException e) {
                            // use empty list in case of an error, to avoid further re-read tries
                            properties = Collections.emptyList();
                        }
                    }
                    String propValue = CmsProperty.get(date, properties).getValue();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propValue)) {
                        try {
                            result = Long.parseLong(propValue.trim());
                        } catch (NumberFormatException e) {
                            // maybe we have better luck with the next property
                        }
                    }
                    break;
            }
            if (result != 0) {
                // if a date value has been found, terminate the loop
                break;
            }
        }
        if (result == 0) {
            // if nothing else was found, use default
            result = defaultValue;
        }
        return result;
    }

    /**
     * Creates a new instance of this comparator key.<p>
     *
     * @param cms the current OpenCms user context
     * @param resource the resource to create the key for
     * @param dateIdentifiers the date identifiers to use for selecting the date
     *
     * @return a new instance of this comparator key
     */
    private static CmsDateResourceComparator create(CmsObject cms, CmsResource resource, List<String> dateIdentifiers) {

        CmsDateResourceComparator result = new CmsDateResourceComparator();
        result.m_date = calculateDate(cms, resource, dateIdentifiers, resource.getDateCreated());
        return result;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(CmsResource res0, CmsResource res1) {

        if (res0 == res1) {
            return 0;
        }

        CmsDateResourceComparator key0 = m_keys.get(res0.getStructureId());
        CmsDateResourceComparator key1 = m_keys.get(res1.getStructureId());

        if (key0 == null) {
            // initialize key if null
            key0 = CmsDateResourceComparator.create(m_cms, res0, m_dateIdentifiers);
            m_keys.put(res0.getStructureId(), key0);
        }
        if (key1 == null) {
            // initialize key if null
            key1 = CmsDateResourceComparator.create(m_cms, res1, m_dateIdentifiers);
            m_keys.put(res1.getStructureId(), key1);
        }

        if (m_asc) {
            // sort in ascending order
            if (key0.m_date > key1.m_date) {
                return 1;
            }
            if (key0.m_date < key1.m_date) {
                return -1;
            }
        } else {
            // sort in descending order
            if (key0.m_date > key1.m_date) {
                return -1;
            }
            if (key0.m_date < key1.m_date) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Returns the date of this resource comparator key.<p>
     *
     * @return the date of this resource comparator key
     */
    public long getDate() {

        return m_date;
    }
}