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

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElement.ModelGroupState;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsNullIgnoringConcurrentMap;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * One element of a container in a container page.<p>
 *
 * @since 8.0
 */
public class CmsContainerElementBean implements Cloneable {

    /** Flag indicating if a new element should be created replacing the given one on first edit of a container-page. */
    private final boolean m_createNew;

    /** The client ADE editor hash. */
    private transient String m_editorHash;

    /** The element's structure id. */
    private CmsUUID m_elementId;

    /** The formatter's structure id. */
    private CmsUUID m_formatterId;

    /** The configured properties. */
    private Map<String, String> m_individualSettings;

    /** The inheritance info of this element. */
    private CmsInheritanceInfo m_inheritanceInfo;

    /** Indicates whether the represented resource is in memory only and not in the VFS. */
    private boolean m_inMemoryOnly;

    /** Indicating if the element resource is released and not expired. */
    private boolean m_releasedAndNotExpired;

    /** The resource of this element. */
    private transient CmsResource m_resource;

    /** The settings of this element containing also default values. */
    private transient Map<String, String> m_settings;

    /** The element site path, only set while rendering. */
    private String m_sitePath;

    /** Indicates the element bean has a temporary file content set. */
    private boolean m_temporaryContent;

    /**
     * Creates a new container page element bean.<p>
     *
     * @param file the element's file
     * @param formatterId the formatter's structure id, could be <code>null</code>
     * @param individualSettings the element settings as a map of name/value pairs
     * @param inMemoryOnly the in memory flag
     * @param editorHash the editor hash to use
     * @param createNew <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     **/
    public CmsContainerElementBean(
        CmsFile file,
        CmsUUID formatterId,
        Map<String, String> individualSettings,
        boolean inMemoryOnly,
        String editorHash,
        boolean createNew) {
        this(file.getStructureId(), formatterId, individualSettings, createNew);
        m_inMemoryOnly = inMemoryOnly;
        m_editorHash = editorHash;
        m_resource = file;
    }

    /**
     * Creates a new container page element bean.<p>
     *
     * @param elementId the element's structure id
     * @param formatterId the formatter's structure id, could be <code>null</code>
     * @param individualSettings the element settings as a map of name/value pairs
     * @param createNew <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     **/
    public CmsContainerElementBean(
        CmsUUID elementId,
        CmsUUID formatterId,
        Map<String, String> individualSettings,
        boolean createNew) {

        m_elementId = elementId;
        m_formatterId = formatterId;
        Map<String, String> newSettings = (individualSettings == null
        ? new HashMap<String, String>()
        : new HashMap<String, String>(individualSettings));
        if (!newSettings.containsKey(CmsContainerElement.ELEMENT_INSTANCE_ID)) {
            newSettings.put(CmsContainerElement.ELEMENT_INSTANCE_ID, new CmsUUID().toString());
        }
        newSettings.values().removeAll(Collections.singletonList(null));
        m_individualSettings = Collections.unmodifiableMap(newSettings);
        m_editorHash = m_elementId.toString() + getSettingsHash();
        m_createNew = createNew;
    }

    /**
     * Constructor to enable wrapped elements.<p>
     */
    protected CmsContainerElementBean() {

        m_elementId = null;
        m_createNew = false;
    }

    /**
     * Cloning constructor.<p>
     *
     * @param createNew create new flag
     * @param elementId element id
     * @param formatterId formatter id
     * @param individualSettings individual settings
     * @param inheritanceInfo inheritance info
     * @param inMemoryOnly in memory only flag
     * @param temporaryContent temporary content flag
     * @param releasedAndNotExpired released and not expired flag
     * @param resource the resource/file object
     * @param settings the settings
     * @param sitePath the site path
     */
    private CmsContainerElementBean(
        boolean createNew,
        CmsUUID elementId,
        CmsUUID formatterId,
        Map<String, String> individualSettings,
        CmsInheritanceInfo inheritanceInfo,
        boolean inMemoryOnly,
        boolean temporaryContent,
        boolean releasedAndNotExpired,
        CmsResource resource,
        Map<String, String> settings,
        String sitePath) {

        m_createNew = createNew;
        m_elementId = elementId;
        m_formatterId = formatterId;
        m_individualSettings = Collections.unmodifiableMap(individualSettings);
        m_inheritanceInfo = inheritanceInfo;
        m_inMemoryOnly = inMemoryOnly;
        m_releasedAndNotExpired = releasedAndNotExpired;
        m_resource = resource;
        setSettings(settings);
        m_sitePath = sitePath;
        m_temporaryContent = temporaryContent;
    }

    /**
     * Clones the given element bean with a different formatter.<p>
     *
     * @param source the element to clone
     * @param formatterId the new formatter id
     *
     * @return the element bean
     */
    public static CmsContainerElementBean cloneWithFormatter(CmsContainerElementBean source, CmsUUID formatterId) {

        CmsContainerElementBean result = source.clone();
        result.m_formatterId = formatterId;
        return result;
    }

    /**
     * Clones the given element bean with a different set of settings.<p>
     *
     * @param source the element to clone
     * @param settings the new settings
     *
     * @return the element bean
     */
    public static CmsContainerElementBean cloneWithSettings(
        CmsContainerElementBean source,
        Map<String, String> settings) {

        boolean createNew = source.m_createNew;
        if (settings.containsKey(CmsContainerElement.CREATE_AS_NEW)) {
            createNew = Boolean.valueOf(settings.get(CmsContainerElement.CREATE_AS_NEW)).booleanValue();
            settings = new HashMap<String, String>(settings);
            settings.remove(CmsContainerElement.CREATE_AS_NEW);
        }
        CmsContainerElementBean result = new CmsContainerElementBean(
            source.m_elementId,
            source.m_formatterId,
            settings,
            createNew);
        result.m_resource = source.m_resource;
        result.m_sitePath = source.m_sitePath;
        result.m_inMemoryOnly = source.m_inMemoryOnly;
        result.m_inheritanceInfo = source.m_inheritanceInfo;
        if (result.m_inMemoryOnly) {
            String editorHash = source.m_editorHash;
            if (editorHash.contains(CmsADEManager.CLIENT_ID_SEPERATOR)) {
                editorHash = editorHash.substring(0, editorHash.indexOf(CmsADEManager.CLIENT_ID_SEPERATOR));
            }
            editorHash += result.getSettingsHash();
            result.m_editorHash = editorHash;
        }
        return result;
    }

    /**
     * Creates an element bean for the given resource type.<p>
     * <b>The represented resource will be in memory only and not in the VFS!!!.</b><p>
     *
     * @param cms the CMS context
     * @param resourceType the resource type
     * @param targetFolder the parent folder of the resource
     * @param individualSettings the element settings as a map of name/value pairs
     * @param isCopyModels if this element when used in models should be copied instead of reused
     * @param locale the locale to use
     *
     * @return the created element bean
     * @throws CmsException if something goes wrong creating the element
     * @throws IllegalArgumentException if the resource type not instance of {@link org.opencms.file.types.CmsResourceTypeXmlContent}
     */
    public static CmsContainerElementBean createElementForResourceType(
        CmsObject cms,
        I_CmsResourceType resourceType,
        String targetFolder,
        Map<String, String> individualSettings,
        boolean isCopyModels,
        Locale locale)
    throws CmsException {

        if (!(resourceType instanceof CmsResourceTypeXmlContent)) {
            throw new IllegalArgumentException();
        }

        byte[] content = new byte[0];
        String schema = ((CmsResourceTypeXmlContent)resourceType).getSchema();
        if (schema != null) {
            // must set URI of OpenCms user context to parent folder of created resource,
            // in order to allow reading of properties for default values
            CmsObject newCms = OpenCms.initCmsObject(cms);
            newCms.getRequestContext().setUri(targetFolder);
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);
            CmsXmlContent xmlContent = CmsXmlContentFactory.createDocument(
                newCms,
                locale,
                OpenCms.getSystemInfo().getDefaultEncoding(),
                contentDefinition);
            // adding all other available locales
            for (Locale otherLocale : OpenCms.getLocaleManager().getAvailableLocales()) {
                if (!locale.equals(otherLocale)) {
                    xmlContent.addLocale(newCms, otherLocale);
                }
            }
            content = xmlContent.marshal();
        }
        CmsFile file = new CmsFile(
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            targetFolder + "~",
            resourceType.getTypeId(),
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            0,
            cms.getRequestContext().getCurrentUser().getId(),
            0,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            content.length,
            0,
            0,
            content);
        CmsContainerElementBean elementBean = new CmsContainerElementBean(
            file,
            null,
            individualSettings,
            true,
            resourceType.getTypeName() + getSettingsHash(individualSettings, !isCopyModels),
            !isCopyModels);
        return elementBean;
    }

    /**
     * Gets the hash code for the element settings.<p>
     *
     * @param individualSettings the individual settings
     * @param createNew the create new flag
     *
     * @return the hash code for the element settings
     */
    private static String getSettingsHash(Map<String, String> individualSettings, boolean createNew) {

        if (!individualSettings.isEmpty() || createNew) {
            int hash = (individualSettings.toString() + createNew).hashCode();
            return CmsADEManager.CLIENT_ID_SEPERATOR + hash;
        }
        return "";
    }

    /**
     * Adds a formatter setting.<p>
     *
     * @param containerName the container name
     * @param formatterId the formatter id
     */
    public void addFormatterSetting(String containerName, String formatterId) {

        Map<String, String> newSettings = new HashMap<String, String>(m_individualSettings);
        newSettings.put(CmsFormatterConfig.getSettingsKeyForContainer(containerName), formatterId);
        m_individualSettings = Collections.unmodifiableMap(newSettings);
        m_editorHash = m_elementId.toString() + getSettingsHash();
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public CmsContainerElementBean clone() {

        return new CmsContainerElementBean(
            m_createNew,
            m_elementId,
            m_formatterId,
            m_individualSettings,
            m_inheritanceInfo,
            m_inMemoryOnly,
            m_temporaryContent,
            m_releasedAndNotExpired,
            m_resource,
            m_settings,
            m_sitePath);
    }

    /**
     * Returns the ADE client editor has value.<p>
     *
     * @return the ADE client editor has value
     */
    public String editorHash() {

        if (m_editorHash == null) {
            m_editorHash = m_elementId.toString() + getSettingsHash();
        }
        return m_editorHash;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsContainerElementBean)) {
            return false;
        }
        return editorHash().equals(((CmsContainerElementBean)obj).editorHash());
    }

    /**
     * Returns the structure id of the formatter of this element.<p>
     *
     * @return the structure id of the formatter of this element
     */
    public CmsUUID getFormatterId() {

        return m_formatterId;
    }

    /**
     * Returns the structure id of the resource of this element.<p>
     *
     * @return the structure id of the resource of this element
     */
    public CmsUUID getId() {

        return m_elementId;
    }

    /**
     * Returns the settings of this element.<p>
     *
     * @return the settings of this element
     */
    public Map<String, String> getIndividualSettings() {

        return m_individualSettings;
    }

    /**
     * Returns the inheritance info.<p>
     *
     * @return the inheritance info or <code>null</code> if not available
     */
    public CmsInheritanceInfo getInheritanceInfo() {

        return m_inheritanceInfo;
    }

    /**
     * Returns the element instance id.<p>
     *
     * @return the element instance id
     */
    public String getInstanceId() {

        return getIndividualSettings().get(CmsContainerElement.ELEMENT_INSTANCE_ID);
    }

    /**
     * Returns the resource of this element.<p>
     *
     * It is required to call {@link #initResource(CmsObject)} before this method can be used.<p>
     *
     * @return the resource of this element
     *
     * @see #initResource(CmsObject)
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the element settings including default values for settings not set.<p>
     * Will return <code>null</code> if the element bean has not been initialized with {@link #initResource(org.opencms.file.CmsObject)}.<p>
     *
     * @return the element settings
     */
    public Map<String, String> getSettings() {

        return m_settings;
    }

    /**
     * Returns the site path of the resource of this element.<p>
     *
     * It is required to call {@link #initResource(CmsObject)} before this method can be used.<p>
     *
     * @return the site path of the resource of this element
     *
     * @see #initResource(CmsObject)
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the type name
     */
    public String getTypeName() {

        if (getResource() != null) {
            return OpenCms.getResourceManager().getResourceType(getResource()).getTypeName();
        } else {
            return "unknown";
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_editorHash.hashCode();
    }

    /**
     * Initializes the resource and the site path of this element.<p>
     *
     * @param cms the CMS context
     *
     * @throws CmsException if something goes wrong reading the element resource
     */
    public void initResource(CmsObject cms) throws CmsException {

        if (m_resource == null) {
            m_resource = cms.readResource(getId(), CmsResourceFilter.IGNORE_EXPIRATION);
            m_releasedAndNotExpired = m_resource.isReleasedAndNotExpired(cms.getRequestContext().getRequestTime());
        } else if (!isInMemoryOnly()) {
            CmsUUID id = m_resource.getStructureId();
            if (id == null) {
                id = getId();
            }
            // the resource object may have a wrong root path, e.g. if it was created before the resource was moved
            if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                m_resource = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
                m_releasedAndNotExpired = m_resource.isReleasedAndNotExpired(cms.getRequestContext().getRequestTime());
            } else {
                if (!isTemporaryContent()) {
                    m_resource = cms.readResource(getId(), CmsResourceFilter.IGNORE_EXPIRATION);
                }
                m_releasedAndNotExpired = m_resource.isReleasedAndNotExpired(cms.getRequestContext().getRequestTime());
            }
        }
        if (m_settings == null) {
            setSettings(new HashMap<String, String>(getIndividualSettings()));
        }
        // redo on every init call to ensure sitepath is calculated for current site
        m_sitePath = cms.getSitePath(m_resource);
    }

    /**
     * Initializes the element settings.<p>
     *
     * @param cms the CMS context
     * @param formatterBean the formatter configuration bean
     */
    public void initSettings(CmsObject cms, I_CmsFormatterBean formatterBean) {

        Map<String, String> mergedSettings;
        if (formatterBean == null) {
            mergedSettings = CmsXmlContentPropertyHelper.mergeDefaults(cms, m_resource, getIndividualSettings());
        } else {
            mergedSettings = CmsXmlContentPropertyHelper.mergeDefaults(
                cms,
                formatterBean.getSettings(),
                getIndividualSettings());
        }
        if (m_settings == null) {
            setSettings(mergedSettings);
        } else {
            m_settings.putAll(mergedSettings);
        }
    }

    /**
     * Returns if the given element should be used as a copy model.<p>
     *
     * @return <code>true</code> if the given element should be used as a copy model
     */
    public boolean isCopyModel() {

        return Boolean.valueOf(getIndividualSettings().get(CmsContainerElement.USE_AS_COPY_MODEL)).booleanValue();
    }

    /**
     * Returns if a new element should be created replacing the given one on first edit of a container-page.<p>
     *
     * @return <code>true</code> if a new element should be created replacing the given one on first edit of a container-page
     */
    public boolean isCreateNew() {

        return m_createNew;
    }

    /**
     * Tests whether this element refers to a group container.<p>
     *
     * @param cms the CmsObject used for VFS operations
     *
     * @return <code>true</code> if the container element refers to a group container
     *
     * @throws CmsException if something goes wrong
     */
    public boolean isGroupContainer(CmsObject cms) throws CmsException {

        if (m_resource == null) {
            initResource(cms);
        }
        return CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(
            OpenCms.getResourceManager().getResourceType(m_resource).getTypeName());
    }

    /**
     * Returns whether this element refers to an inherited container element.<p>
     *
     * @param cms the CmsObject used for VFS operations
     *
     * @return <code>true</code> if the container element refers to an inherited container
     *
     * @throws CmsException if something goes wrong
     */
    public boolean isInheritedContainer(CmsObject cms) throws CmsException {

        if (m_resource == null) {
            initResource(cms);
        }
        return OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME).getTypeId() == m_resource.getTypeId();
    }

    /**
     * Returns if the represented resource is in memory only and not persisted in the VFS.<p>
     *
     * @return <code>true</code> if the represented resource is in memory only and not persisted in the VFS
     */
    public boolean isInMemoryOnly() {

        return m_inMemoryOnly;
    }

    /**
     * Returns if the given element is a model group.<p>
     *
     * @return <code>true</code> if the given element is a model group
     */
    public boolean isModelGroup() {

        ModelGroupState state = ModelGroupState.evaluate(
            getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_STATE));
        return state == ModelGroupState.isModelGroup;
    }

    /**
     * Returns if all instances of this element should be replaced within a copy model.<p>
     *
     * @return <code>true</code> if all instances of this element should be replaced within a copy model
     */
    public boolean isModelGroupAlwaysReplace() {

        return Boolean.parseBoolean(getIndividualSettings().get(CmsContainerElement.IS_MODEL_GROUP_ALWAYS_REPLACE));
    }

    /**
     * Returns if the element resource is released and not expired.<p>
     *
     * @return <code>true</code> if the element resource is released and not expired
     */
    public boolean isReleasedAndNotExpired() {

        return isInMemoryOnly() || m_releasedAndNotExpired;
    }

    /**
     * Returns if the element resource contains temporary file content.<p>
     *
     * @return <code>true</code> if the element resource contains temporary file content
     */
    public boolean isTemporaryContent() {

        return m_temporaryContent;
    }

    /**
     * Removes the instance id.<p>
     */
    public void removeInstanceId() {

        Map<String, String> newSettings = new HashMap<String, String>(m_individualSettings);
        newSettings.remove(CmsContainerElement.ELEMENT_INSTANCE_ID);
        m_individualSettings = Collections.unmodifiableMap(newSettings);
        m_editorHash = m_elementId.toString() + getSettingsHash();
    }

    /**
     * Sets the formatter id.<p>
     *
     * @param formatterId the formatter id
     */
    public void setFormatterId(CmsUUID formatterId) {

        m_formatterId = formatterId;
    }

    /**
     * Sets a historical file.<p>
     *
     * @param file the historical file
     */
    public void setHistoryFile(CmsFile file) {

        m_resource = file;
        m_inMemoryOnly = true;
    }

    /**
     * Sets the inheritance info for this element.<p>
     *
     * @param inheritanceInfo the inheritance info
     */
    public void setInheritanceInfo(CmsInheritanceInfo inheritanceInfo) {

        m_inheritanceInfo = inheritanceInfo;
    }

    /**
     * Sets the element resource as a temporary file.<p>
     *
     * @param elementFile the temporary file
     */
    public void setTemporaryFile(CmsFile elementFile) {

        m_resource = elementFile;
        m_temporaryContent = true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return editorHash();
    }

    /**
     * Gets the hash code for the element settings.<p>
     *
     * @return the hash code for the element settings
     */
    private String getSettingsHash() {

        return getSettingsHash(getIndividualSettings(), m_createNew);
    }

    /**
     * Sets the settings map.<p>
     *
     * @param settings the settings
     */
    private void setSettings(Map<String, String> settings) {

        if (settings == null) {
            m_settings = null;
        } else {
            m_settings = new CmsNullIgnoringConcurrentMap<String, String>(settings);
        }
    }
}
