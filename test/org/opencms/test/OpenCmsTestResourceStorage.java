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

package org.opencms.test;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage object for storing all attributes of vfs resources.<p>
 *
 */
public class OpenCmsTestResourceStorage {

    /** the name of the default storage. */
    public static String DEFAULT_STORAGE = "default";

    /** the name of the default storage. */
    public static String GLOBAL_STORAGE = "global";

    /** the name of the storage. */
    private String m_name;

    /** storeage for precalculation of states. **/
    private Map<String, CmsResourceState> m_precalcState;

    /** Strings for mapping the filename. */
    private String m_sourceNameMapping;

    /** internal storage. */
    private Map<String, OpenCmsTestResourceStorageEntry> m_storage;

    /** Prefix mapping for target name. */
    private String m_targetNameMapping;

    /**
     * Creates a new OpenCmsTestResourceStorage.<p>
     *
     * @param name the name of the storage
     */
    public OpenCmsTestResourceStorage(String name) {

        m_storage = new HashMap<String, OpenCmsTestResourceStorageEntry>();
        m_precalcState = new HashMap<String, CmsResourceState>();
        m_sourceNameMapping = null;
        m_targetNameMapping = null;
        m_name = name;
    }

    /**
     * Adds a CmsResource to the resource storage.<p>
     *
     * @param cms the CmsObject
     * @param resourceName the resource name to add
     * @param resource the resource to add
     * @throws CmsException if something goes wrong
     */
    public void add(CmsObject cms, String resourceName, CmsResource resource) throws CmsException {

        m_storage.put(resourceName, new OpenCmsTestResourceStorageEntry(cms, resourceName, resource));
        m_precalcState.put(resourceName, preCalculateState(resource));
    }

    /**
     * Gets an entry from the storage.<p>
     *
     * @param resourceName the name of the resource to get
     * @return OpenCmsTestResourceStorageEntry with all the attributes of a CmsResource
     * @throws Exception in case something goes wrong
     */
    public OpenCmsTestResourceStorageEntry get(String resourceName) throws Exception {

        String mappedResourceName = mapResourcename(resourceName);

        OpenCmsTestResourceStorageEntry entry = null;
        entry = m_storage.get(mappedResourceName);

        if (entry == null) {
            throw new Exception(
                "resource "
                    + resourceName
                    + " -> "
                    + mappedResourceName
                    + " not found in storage "
                    + m_storage.keySet().toString());
        }

        return entry;
    }

    /**
     * Gets the name of the storage.<p>
     *
     * @return the name of the storage
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the size of the storage.<p>
     *
     * @return the size of the storage
     */
    public int size() {

        return m_storage.size();
    }

    /**
     * Gets an precalculate resource state from the storage.<p>
     *
     * @param resourceName the name of the resource to get  the state
     *
     * @return precalculated resource state
     *
     * @throws Exception in case something goes wrong
     */
    public CmsResourceState getPreCalculatedState(String resourceName) throws Exception {

        String mappedResourceName = mapResourcename(resourceName);

        CmsResourceState state = m_precalcState.get(mappedResourceName);
        if (state == null) {
            throw new Exception("Not found in storage " + resourceName + " -> " + mappedResourceName);
        }
        return state;
    }

    /**
     * Returns the source name mapping.<p>
     *
     * @return the source name mapping
     */
    public String getSourceNameMapping() {

        return m_sourceNameMapping;
    }

    /**
     * Returns the target name mapping.<p>
     *
     * @return the the target name mapping
     */
    public String getTargetNameMapping() {

        return m_targetNameMapping;
    }

    /**
     * Sets the mapping for resourcenames.<p>
     *
     * @param source the source resource name
     * @param target the target resource name
     */
    public void setMapping(String source, String target) {

        m_sourceNameMapping = source;
        m_targetNameMapping = target;
    }

    /**
     * Resets the mapping for resourcenames.<p>
     */
    public void resetMapping() {

        m_sourceNameMapping = null;
        m_targetNameMapping = null;
    }

    /**
     * Does the name mapping of a resourceName.<p>
     *
     * This is required to find resources in the resource storage afer their path in the vfs
     * has changed (e.g. after a copy operation).<p>
     *
     * @param resourceName the resource name to map
     * @return mapped resource name
     */
    public String mapResourcename(String resourceName) {

        // only modify the name if we have set some kind of mapping
        if ((m_sourceNameMapping != null) && (m_targetNameMapping != null)) {
            // check if the resourcename starts with the source map name
            if (resourceName.startsWith(m_sourceNameMapping)) {
                // exchange the prefix with the target map name
                resourceName = m_targetNameMapping + resourceName.substring(m_sourceNameMapping.length());
            }
        }
        return resourceName;
    }

    /**
     * Precalculates the state of a resource after an operation based on its state before
     * the operation is excecuted.<p>
     *
     * The following states are precalculated:
     * <ul>
     * <li>Unchanged -> Changed</li>
     * <li>Changed -> Changed</li>
     * <li>New -> New</li>
     * <li>Deleted -> Deleted</li>
     * </ul>
     * @param res the resource
     * @return new precalculated state
     */
    private CmsResourceState preCalculateState(CmsResource res) {

        CmsResourceState newState = CmsResource.STATE_UNCHANGED;
        CmsResourceState state = res.getState();
        if (state.isUnchanged()) {
            newState = CmsResource.STATE_CHANGED;
        } else if (state.isChanged()) {
            newState = CmsResource.STATE_CHANGED;
        } else if (state.isNew()) {
            newState = CmsResource.STATE_NEW;
        } else if (state.isDeleted()) {
            newState = CmsResource.STATE_DELETED;
        } else {
            newState = CmsResource.STATE_UNCHANGED;
        }
        return newState;
    }
}