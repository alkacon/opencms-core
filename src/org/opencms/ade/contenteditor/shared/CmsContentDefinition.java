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

package org.opencms.ade.contenteditor.shared;

import org.opencms.acacia.shared.CmsAttributeConfiguration;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.acacia.shared.CmsType;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains all information needed for editing an XMLContent.<p>
 */
public class CmsContentDefinition extends org.opencms.acacia.shared.CmsContentDefinition {

    /** The id of the element settings tab. */
    public static final String SETTINGS_TAB_ID = "###formattersettings###";

    /** The entity id prefix. */
    private static final String ENTITY_ID_PREFIX = "http://opencms.org/resources/";

    /** The value of the acacia-unlock configuration option. */
    private boolean m_autoUnlock;

    /** The available locales. */
    private Map<String, String> m_availableLocales;

    /** A map from attribute names to complex widget configurations. */
    private Map<String, CmsComplexWidgetData> m_complexWidgetData;

    /** The content locales. */
    private List<String> m_contentLocales;

    /** Flag indicating the resource needs to removed on cancel. */
    private boolean m_deleteOnCancel;

    /** Indicates that editor change handlers are configured. */
    private Set<String> m_editorChangeScopes;

    /** The external widget configurations. */
    private List<CmsExternalWidgetConfiguration> m_externalWidgetConfigurations;

    /** The resource icon classes. */
    private String m_iconClasses;

    /** The direct edit flag (set to true for classic direct edit mode). */
    private boolean m_isDirectEdit;

    /** The model file informations. */
    private List<CmsModelResourceInfo> m_modelInfos;

    /** The new link. */
    private String m_newLink;

    /** Flag indicating the current content has an invalid XML structure and was auto corrected. */
    private boolean m_performedAutocorrection;

    /** The reference resource structure id. */
    private CmsUUID m_referenceResourceId;

    /** The resource type name. */
    private String m_resourceType;

    /** True if the element should be marked as 'reused' in the content editor. */
    private boolean m_reusedElement;

    /** The site path. */
    private String m_sitePath;

    /** The paths to skip during locale synchronization. */
    private Collection<String> m_skipPaths;

    /** The elements that require a synchronization across all locales. */
    private List<String> m_synchronizations;

    /** The locale synchronization values. */
    private Map<String, String> m_syncValues;

    /** The content title. */
    private String m_title;

    /**
     * Constructor for model file informations object.<p>
     *
     * @param modelInfos the model file informations
     * @param newLink the new link
     * @param referenceId the reference resource structure id
     * @param locale the locale
     */
    public CmsContentDefinition(
        List<CmsModelResourceInfo> modelInfos,
        String newLink,
        CmsUUID referenceId,
        String locale) {

        super(null, null, null, null, null, true, locale);
        m_modelInfos = modelInfos;
        m_newLink = newLink;
        m_referenceResourceId = referenceId;
    }

    /**
     * Constructor.<p>
     *
     * @param entityId the entity id
     * @param entities the locale specific entities of the content
     * @param configurations the attribute configurations
     * @param externalWidgetConfigurations the external widget configurations
     * @param complexWidgetData the complex widget configurations
     * @param types the types
     * @param tabInfos the tab information
     * @param locale the content locale
     * @param contentLocales the content locales
     * @param availableLocales the available locales
     * @param synchronizations the elements that require a synchronization across all locales
     * @param syncValues the locale synchronization values
     * @param skipPaths the paths to skip during locale synchronization
     * @param title the content title
     * @param sitePath the site path
     * @param resourceType the resource type name
     * @param iconClasses the resource icon classes
     * @param performedAutocorrection flag indicating the current content has an invalid XML structure and was auto corrected
     * @param autoUnlock false if the editor should not unlock resources automatically in standalone mode
     * @param editorChangeScopes the editor change handler scopes
     */
    public CmsContentDefinition(
        String entityId,
        Map<String, CmsEntity> entities,
        Map<String, CmsAttributeConfiguration> configurations,
        Collection<CmsExternalWidgetConfiguration> externalWidgetConfigurations,
        Map<String, CmsComplexWidgetData> complexWidgetData,
        Map<String, CmsType> types,
        List<CmsTabInfo> tabInfos,
        String locale,
        List<String> contentLocales,
        Map<String, String> availableLocales,
        List<String> synchronizations,
        Map<String, String> syncValues,
        Collection<String> skipPaths,
        String title,
        String sitePath,
        String resourceType,
        String iconClasses,
        boolean performedAutocorrection,
        boolean autoUnlock,
        Set<String> editorChangeScopes) {

        super(entityId, entities, configurations, types, tabInfos, true, locale);
        m_contentLocales = contentLocales;
        m_availableLocales = availableLocales;
        m_synchronizations = synchronizations;
        m_syncValues = syncValues;
        m_skipPaths = skipPaths;
        m_complexWidgetData = complexWidgetData;
        m_title = title;
        m_sitePath = sitePath;
        m_resourceType = resourceType;
        m_iconClasses = iconClasses;
        m_externalWidgetConfigurations = new ArrayList<CmsExternalWidgetConfiguration>(externalWidgetConfigurations);
        m_performedAutocorrection = performedAutocorrection;
        m_autoUnlock = autoUnlock;
        m_editorChangeScopes = editorChangeScopes;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsContentDefinition() {

        super();
    }

    /**
     * Returns the UUID according to the given entity id.<p>
     *
     * @param entityId the entity id
     *
     * @return the entity id
     */
    public static CmsUUID entityIdToUuid(String entityId) {

        if (entityId.startsWith(ENTITY_ID_PREFIX)) {
            entityId = entityId.substring(entityId.lastIndexOf("/") + 1);
        }
        return new CmsUUID(entityId);
    }

    /**
     * Extracts the locale from the entity id.<p>
     *
     * @param entityId the entity id
     *
     * @return the locale
     */
    public static String getLocaleFromId(String entityId) {

        if (entityId.startsWith(ENTITY_ID_PREFIX)) {
            return entityId.substring(ENTITY_ID_PREFIX.length(), entityId.lastIndexOf("/"));
        }
        return null;
    }

    /**
     * Returns the value for the given XPath expression.<p>
     *
     * @param entity the entity
     * @param path the path
     *
     * @return the value
     */
    public static String getValueForPath(CmsEntity entity, String path) {

        String result = null;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String attributeName;
        if (path.contains("/")) {
            attributeName = path.substring(0, path.indexOf("/"));
            path = path.substring(path.indexOf("/"));
        } else {
            attributeName = path;
            path = null;
        }
        int index = org.opencms.acacia.shared.CmsContentDefinition.extractIndex(attributeName);
        if (index > 0) {
            index--;
        }
        attributeName = entity.getTypeName()
            + "/"
            + org.opencms.acacia.shared.CmsContentDefinition.removeIndex(attributeName);
        CmsEntityAttribute attribute = entity.getAttribute(attributeName);
        if (!((attribute == null) || (attribute.isComplexValue() && (path == null)))) {
            if (attribute.isSimpleValue()) {
                if ((path == null) && (attribute.getValueCount() > 0)) {
                    List<String> values = attribute.getSimpleValues();
                    result = values.get(index);
                }
            } else if (attribute.getValueCount() > (index)) {
                List<CmsEntity> values = attribute.getComplexValues();
                result = getValueForPath(values.get(index), path);
            }
        }
        return result;
    }

    /**
     * Transfers values from the original entity to the given target entity.<p>
     *
     * @param original the original entity
     * @param target the target entity
     * @param transferAttributes the attributes to consider for the value transfer
     * @param entityTypes the entity types
     * @param attributeConfigurations the attribute configurations
     * @param considerDefaults if default values should be added according to minimum occurrence settings
     */
    public static void transferValues(
        CmsEntity original,
        CmsEntity target,
        List<String> transferAttributes,
        Map<String, CmsType> entityTypes,
        Map<String, CmsAttributeConfiguration> attributeConfigurations,
        boolean considerDefaults) {

        CmsType entityType = entityTypes.get(target.getTypeName());
        for (String attributeName : entityType.getAttributeNames()) {
            CmsType attributeType = entityTypes.get(entityType.getAttributeTypeName(attributeName));
            if (transferAttributes.contains(attributeName)) {

                target.removeAttribute(attributeName);
                CmsEntityAttribute attribute = original != null ? original.getAttribute(attributeName) : null;
                if (attribute != null) {
                    if (attributeType.isSimpleType()) {
                        for (String value : attribute.getSimpleValues()) {
                            target.addAttributeValue(attributeName, value);
                        }
                        if (considerDefaults) {
                            for (int i = attribute.getValueCount(); i < entityType.getAttributeMinOccurrence(
                                attributeName); i++) {
                                target.addAttributeValue(
                                    attributeName,
                                    attributeConfigurations.get(attributeName).getDefaultValue());
                            }
                        }
                    } else {
                        for (CmsEntity value : attribute.getComplexValues()) {
                            target.addAttributeValue(attributeName, value);
                        }
                        if (considerDefaults) {
                            for (int i = attribute.getValueCount(); i < entityType.getAttributeMinOccurrence(
                                attributeName); i++) {
                                target.addAttributeValue(
                                    attributeName,
                                    createDefaultValueEntity(attributeType, entityTypes, attributeConfigurations));
                            }
                        }
                    }
                } else if (considerDefaults) {
                    for (int i = 0; i < entityType.getAttributeMinOccurrence(attributeName); i++) {
                        if (attributeType.isSimpleType()) {
                            target.addAttributeValue(
                                attributeName,
                                attributeConfigurations.get(attributeName).getDefaultValue());
                        } else {
                            target.addAttributeValue(
                                attributeName,
                                createDefaultValueEntity(attributeType, entityTypes, attributeConfigurations));
                        }
                    }
                }
            } else {
                if (!attributeType.isSimpleType()) {
                    CmsEntityAttribute targetAttribute = target.getAttribute(attributeName);
                    CmsEntityAttribute originalAttribute = original != null
                    ? original.getAttribute(attributeName)
                    : null;
                    if (targetAttribute != null) {
                        for (int i = 0; i < targetAttribute.getComplexValues().size(); i++) {
                            CmsEntity subTarget = targetAttribute.getComplexValues().get(i);
                            CmsEntity subOriginal = (originalAttribute != null)
                                && (originalAttribute.getComplexValues().size() > i)
                                ? originalAttribute.getComplexValues().get(i)
                                : null;
                            transferValues(
                                subOriginal,
                                subTarget,
                                transferAttributes,
                                entityTypes,
                                attributeConfigurations,
                                considerDefaults);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the entity id according to the given UUID.<p>
     *
     * @param uuid the UUID
     * @param locale the content locale
     *
     * @return the entity id
     */
    public static String uuidToEntityId(CmsUUID uuid, String locale) {

        return ENTITY_ID_PREFIX + locale + "/" + uuid.toString();
    }

    /**
     * Creates an entity object containing the default values configured for it's type.<p>
     *
     * @param entityType the entity type
     * @param entityTypes the entity types
     * @param attributeConfigurations the attribute configurations
     *
     * @return the created entity
     */
    protected static CmsEntity createDefaultValueEntity(
        CmsType entityType,
        Map<String, CmsType> entityTypes,
        Map<String, CmsAttributeConfiguration> attributeConfigurations) {

        CmsEntity result = new CmsEntity(null, entityType.getId());
        for (String attributeName : entityType.getAttributeNames()) {
            CmsType attributeType = entityTypes.get(entityType.getAttributeTypeName(attributeName));
            for (int i = 0; i < entityType.getAttributeMinOccurrence(attributeName); i++) {
                if (attributeType.isSimpleType()) {
                    result.addAttributeValue(
                        attributeName,
                        attributeConfigurations.get(attributeName).getDefaultValue());
                } else {
                    result.addAttributeValue(
                        attributeName,
                        createDefaultValueEntity(attributeType, entityTypes, attributeConfigurations));
                }
            }
        }
        return result;
    }

    /**
     * Returns the available locales.<p>
     *
     * @return the available locales
     */
    public Map<String, String> getAvailableLocales() {

        return m_availableLocales;
    }

    /**
     * Gets the complex widget configurations.<p>
     *
     * @return the complex widget configurations
     */
    public Map<String, CmsComplexWidgetData> getComplexWidgetData() {

        return m_complexWidgetData;
    }

    /**
     * Returns the content locales.<p>
     *
     * @return the content locales
     */
    public List<String> getContentLocales() {

        return m_contentLocales;
    }

    /**
     * Returns the editor change handler scopes.<p>
     *
     * @return the editor change handler scopes
     */
    public Set<String> getEditorChangeScopes() {

        return m_editorChangeScopes;
    }

    /**
     * Returns the external widget configurations.<p>
     *
     * @return the external widget configurations
     */
    public List<CmsExternalWidgetConfiguration> getExternalWidgetConfigurations() {

        return m_externalWidgetConfigurations;
    }

    /**
     * Returns the resource icon classes.<p>
     *
     * @return the resource icon classes
     */
    public String getIconClasses() {

        return m_iconClasses;
    }

    /**
     * Returns the model file informations.<p>
     *
     * @return the model file informations
     */
    public List<CmsModelResourceInfo> getModelInfos() {

        return m_modelInfos;
    }

    /**
     * Returns the new link.<p>
     *
     * @return the new link
     */
    public String getNewLink() {

        return m_newLink;
    }

    /**
     * Returns the reference resource structure id.<p>
     *
     * @return the reference resource structure id
     */
    public CmsUUID getReferenceResourceId() {

        return m_referenceResourceId;
    }

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the paths to skip during locale synchronization.<p>
     *
     * @return the paths to skip during locale synchronization
     */
    public Collection<String> getSkipPaths() {

        return m_skipPaths;
    }

    /**
     * Returns the elements that require a synchronization across all locales.<p>
     *
     * @return the element paths
     */
    public List<String> getSynchronizations() {

        return m_synchronizations;
    }

    /**
     * Returns the locale synchronization values.<p>
     *
     * @return the locale synchronization values
     */
    public Map<String, String> getSyncValues() {

        return m_syncValues;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns <code>true</code> if any editor change handlers have been configured for this content type.<p>
     *
     * @return <code>true</code> if any editor change handlers have been configured for this content type.<p>
     */
    public boolean hasEditorChangeHandlers() {

        return (m_editorChangeScopes != null) && !m_editorChangeScopes.isEmpty();
    }

    /**
     * Returns if there are locale synchronized elements configured.<p>
     *
     * @return <code>true</code> if there are locale synchronized elements configured
     */
    public boolean hasSynchronizedElements() {

        return !m_synchronizations.isEmpty();
    }

    /**
     * Returns the value of the acacia-unlock configuration option.<p>
     *
     * @return the value of the acacia-unlock configuration option
     */
    public boolean isAutoUnlock() {

        return m_autoUnlock;
    }

    /**
     * Returns if the resource needs to removed on cancel.<p>
     *
     * @return <code>true</code> if the resource needs to removed on cancel
     */
    public boolean isDeleteOnCancel() {

        return m_deleteOnCancel;
    }

    /**
     * Returns true if the direct edit flag is set, which means that the editor was opened from the classic direct edit mode.<p>
     *
     * @return true if the direct edit flag is set
     */
    public boolean isDirectEdit() {

        return m_isDirectEdit;
    }

    /**
     * Returns if the model file informations are present, in this case no additional data is contained.<p>
     *
     * @return <code>true</code> if the definition contains the model file informations
     */
    public boolean isModelInfo() {

        return m_modelInfos != null;
    }

    /**
     * Returns if auto correction was performed.<p>
     *
     * @return <code>true</code> if auto correction was performed
     */
    public boolean isPerformedAutocorrection() {

        return m_performedAutocorrection;
    }

    /**
     * Checks if the element should be marked as 'reused' in the editor.
     *
     * @return true if the element should be marked as 'reused' in the editor
     */
    public boolean isReusedElement() {

        return m_reusedElement;
    }

    /**
     * Sets if the resource needs to removed on cancel.<p>
     *
     * @param deleteOnCancel <code>true</code> if the resource needs to removed on cancel
     */
    public void setDeleteOnCancel(boolean deleteOnCancel) {

        m_deleteOnCancel = deleteOnCancel;
    }

    /**
     * Sets the value of the direct edit flag.<p>
     *
     * @param isDirectEdit the new value for the direct edit flag
     */
    public void setDirectEdit(boolean isDirectEdit) {

        m_isDirectEdit = isDirectEdit;
    }

    /**
     * Enables / disables marking of the element as 'reused' in the content editor.
     *
     * @param reused true if the element should be shown as 'reused'
     */
    public void setReusedElement(boolean reused) {

        m_reusedElement = reused;

    }
}
