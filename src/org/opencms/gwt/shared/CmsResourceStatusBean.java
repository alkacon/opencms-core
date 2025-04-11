/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.shared;

import org.opencms.db.CmsResourceState;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Information used to display the resource info dialog.
 */
public class CmsResourceStatusBean implements IsSerializable {

    /** Resource state object. */
    CmsResourceState m_stateBean;

    /** Additional resource attributes to display. */
    private Map<String, String> m_additionalAttributes;

    /** Creation date.*/
    private String m_dateCreated;

    /** Expiration date. */
    private String m_dateExpired;

    /** Modification date.**/
    private String m_dateLastModified;

    /** Release date. */
    private String m_dateReleased;

    /** Last project. */
    private String m_lastProject;

    /** List info. */
    private CmsListInfoBean m_listInfo;

    /** Locales of XML content. */
    private List<String> m_locales;

    /** Lock state. */
    private String m_lockState;

    /** Navigation text. */
    private String m_navText;

    /** Relation sources from other sites. */
    private ArrayList<CmsResourceStatusRelationBean> m_otherSiteRelationSources = Lists.newArrayList();

    /** Permissions. */
    private String m_permissions;

    /** List of beans representing resources which have relations toward this resource. */
    private ArrayList<CmsResourceStatusRelationBean> m_relationSources = new ArrayList<CmsResourceStatusRelationBean>();

    /** List of relation target beans. */
    private ArrayList<CmsResourceStatusRelationBean> m_relationTargets = new ArrayList<CmsResourceStatusRelationBean>();

    /** Resource type. */
    private String m_resourceType;

    /** Beans representing siblings. */
    private ArrayList<CmsResourceStatusRelationBean> m_siblings = new ArrayList<CmsResourceStatusRelationBean>();

    /** Size. */
    private int m_size;

    /** The start tab id. */
    private CmsResourceStatusTabId m_startTab;

    /** Structure id of the resource. */
    private CmsUUID m_structureId;

    /** An ordered map defining the tabs to display and their order, with the tab labels as values. */
    private LinkedHashMap<CmsResourceStatusTabId, String> m_tabs;

    /** Title property. */
    private String m_title;

    /** Creator. */
    private String m_userCreated;

    /** Last modifier. */
    private String m_userLastModified;

    /** Custom error message to display in the relation sources tab. */
    private String m_sourcesError;

    /** Custom error message to display in the relation targets tab. */
    private String m_targetsError;

    /**
     * Returns the additional resource attributes to display.<p>
     * @return the additional resource attributes
     */
    public Map<String, String> getAdditionalAttributes() {

        return m_additionalAttributes;
    }

    /**
     * Gets the date created.
     *
     * @return the date created
     */
    public String getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Gets the date expired.
     *
     * @return the date expired
     */
    public String getDateExpired() {

        return m_dateExpired;
    }

    /**
     * Gets the date last modified.
     *
     * @return the date last modified
     */
    public String getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * Gets the date released.
     *
     * @return the date released
     */
    public String getDateReleased() {

        return m_dateReleased;
    }

    /**
     * Gets the last project.
     *
     * @return the last project
     */
    public String getLastProject() {

        return m_lastProject;
    }

    /**
     * Gets the list info.
     *
     * @return the list info
     */
    public CmsListInfoBean getListInfo() {

        return m_listInfo;
    }

    /**
     * Gets the locales.
     *
     * @return the locales
     */
    public List<String> getLocales() {

        return m_locales;
    }

    /**
     * Gets the lock state.
     *
     * @return the lock state
     */
    public String getLockState() {

        return m_lockState;
    }

    /**
     * Gets the nav text.
     *
     * @return the nav text
     */
    public String getNavText() {

        return m_navText;
    }

    /**
     * Gets relation sources for other sites.<p>
     *
     * @return relation sources for other sites
     */
    public ArrayList<CmsResourceStatusRelationBean> getOtherSiteRelationSources() {

        return m_otherSiteRelationSources;
    }

    /**
     * Gets the permissions.
     *
     * @return the permissions
     */
    public String getPermissions() {

        return m_permissions;
    }

    /**
     * Gets the list info beans for the source resources of relations pointing to this resource.<p>
     *
     * @return the relation source beans
     */
    public ArrayList<CmsResourceStatusRelationBean> getRelationSources() {

        return m_relationSources;
    }

    /**
     * Gets the list info beans for the targets of relations pointing away from this resource.<p>
     *
     * @return the relation target beans
     */
    public ArrayList<CmsResourceStatusRelationBean> getRelationTargets() {

        return m_relationTargets;
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Gets the sibling information.<p>
     *
     * @return the sibling information
     */
    public ArrayList<CmsResourceStatusRelationBean> getSiblings() {

        return m_siblings;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {

        return m_size;
    }

    /**
     * Gets the custom error message for the relation sources tab.
     *
     * @return the custom error message for the relation sources tab
     */
    public String getSourcesError() {

        return m_sourcesError;
    }

    /**
     * Gets the start tab.<p>
     *
     * @return the start tab
     */
    public CmsResourceStatusTabId getStartTab() {

        return m_startTab;
    }

    /**
     * Gets the resource state object.<p>
     *
     * @return the resource state object
     */
    public CmsResourceState getStateBean() {

        return m_stateBean;
    }

    /**
     * Gets the structure id of the resource.<p>
     *
     * @return the structure id of the resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the tab configuration, which is an ordered map defining the tabs to display and their order, with the tab labels as values.
     *
     * @return the tab configuration
     */
    public LinkedHashMap<CmsResourceStatusTabId, String> getTabs() {

        return m_tabs;
    }

    /**
     * Gets the custom error message for the relation targets tab.
     *
     * @return the custom error message for the relation target tab
     */
    public String getTargetsError() {

        return m_targetsError;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Gets the user created.
     *
     * @return the user created
     */
    public String getUserCreated() {

        return m_userCreated;
    }

    /**
     * Gets the user last modified.
     *
     * @return the user last modified
     */
    public String getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * Sets the additional resource attributes.<p>
     *
     * @param additionalAttributes the additional resource attributes
     */
    public void setAdditionalAttributes(Map<String, String> additionalAttributes) {

        m_additionalAttributes = additionalAttributes;
    }

    /**
     * Sets the date created.
     *
     * @param dateCreated the new date created
     */
    public void setDateCreated(String dateCreated) {

        m_dateCreated = dateCreated;
    }

    /**
     * Sets the date expired.
     *
     * @param dateExpired the new date expired
     */
    public void setDateExpired(String dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * Sets the date last modified.
     *
     * @param dateLastModified the new date last modified
     */
    public void setDateLastModified(String dateLastModified) {

        m_dateLastModified = dateLastModified;
    }

    /**
     * Sets the date released.
     *
     * @param dateReleased the new date released
     */
    public void setDateReleased(String dateReleased) {

        m_dateReleased = dateReleased;
    }

    /**
     * Sets the last project.
     *
     * @param lastProject the new last project
     */
    public void setLastProject(String lastProject) {

        m_lastProject = lastProject;
    }

    /**
     * Sets the list info.
     *
     * @param listInfo the new list info
     */
    public void setListInfo(CmsListInfoBean listInfo) {

        m_listInfo = listInfo;
    }

    /**
     * Sets the locales.
     *
     * @param locales the new locales
     */
    public void setLocales(List<String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the lock state.
     *
     * @param lockState the new lock state
     */
    public void setLockState(String lockState) {

        m_lockState = lockState;
    }

    /**
     * Sets the nav text.
     *
     * @param navText the new nav text
     */
    public void setNavText(String navText) {

        m_navText = navText;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions the new permissions
     */
    public void setPermissions(String permissions) {

        m_permissions = permissions;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the new resource type
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the size.
     *
     * @param size the new size
     */
    public void setSize(int size) {

        m_size = size;
    }

    /**
     * Sets the custom error message for the relation sources tab.
     *
     * @param sourcesError the custom error message
     */
    public void setSourcesError(String sourcesError) {

        m_sourcesError = sourcesError;
    }

    /**
     * Sets the start tab.<p>
     *
     * @param startTab the start tab id
     */
    public void setStartTab(CmsResourceStatusTabId startTab) {

        m_startTab = startTab;
    }

    /**
     * Sets the resource state object.<p>
     *
     * @param stateBean the new resource state object
     */
    public void setStateBean(CmsResourceState stateBean) {

        m_stateBean = stateBean;
    }

    /**
     * Sets the structure id of the resource.<p>
     *
     * @param structureId the structure id of the resource
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the tab configuration.<p>
     *
     * @param tabs the tab configuration
     */
    public void setTabs(LinkedHashMap<CmsResourceStatusTabId, String> tabs) {

        m_tabs = tabs;
    }

    /**
     * Sets the custom error message for the relation target tab.
     *
     * @param targetsError the custom error message
     */
    public void setTargetsError(String targetsError) {

        m_targetsError = targetsError;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the user created.
     *
     * @param userCreated the new user created
     */
    public void setUserCreated(String userCreated) {

        m_userCreated = userCreated;
    }

    /**
    * Sets the user last modified.
    *
    * @param userLastModified the new user last modified
    */
    public void setUserLastModified(String userLastModified) {

        m_userLastModified = userLastModified;
    }

}
