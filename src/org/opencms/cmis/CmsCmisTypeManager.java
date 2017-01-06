/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.opencms.cmis;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelationType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.WSConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RelationshipTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.commons.logging.Log;

/**
 * This class keeps track of all the types which should be available for a {@link I_CmsCmisRepository}.
 */
public class CmsCmisTypeManager {

    /** CMIS type id for documents. */
    public static final String DOCUMENT_TYPE_ID = BaseTypeId.CMIS_DOCUMENT.value();

    /** CMIS type id for folders. */
    public static final String FOLDER_TYPE_ID = BaseTypeId.CMIS_FOLDER.value();

    /** Prefix for inherited properties. */
    public static final String INHERITED_PREFIX = "opencms-inherited:";

    /** CMIS type id for policies. */
    public static final String POLICY_TYPE_ID = BaseTypeId.CMIS_POLICY.value();

    /** The prefix used for normal OpenCms resource properties. */
    public static final String PROPERTY_PREFIX = "opencms:";

    /** Prefix for dynamic properties. */
    public static final String PROPERTY_PREFIX_DYNAMIC = "opencms-dynamic:";

    /** The prefix for special properties. */
    public static final String PROPERTY_PREFIX_SPECIAL = "opencms-special:";

    /** The name of the propery containing the resource type name. */
    public static final String PROPERTY_RESOURCE_TYPE = PROPERTY_PREFIX_SPECIAL + "resource-type";

    /** CMIS type id for relationships. */
    public static final String RELATIONSHIP_TYPE_ID = BaseTypeId.CMIS_RELATIONSHIP.value();

    /** Need to refresh property data after this time. */
    public static final long UPDATE_INTERVAL = 1000 * 60 * 5;

    /** The logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsCmisTypeManager.class);

    /** The namespace used for properties. */
    private static final String NAMESPACE = "http://opencms.org/opencms-cmis";

    /** The admin CMS context. */
    private CmsObject m_adminCms;

    /** The list of OpenCms property definitions. */
    private List<CmsPropertyDefinition> m_cmsPropertyDefinitions;

    /** The last update time. */
    private long m_lastUpdate;

    /** List of dynamic property providers. */
    private List<I_CmsPropertyProvider> m_propertyProviders = new ArrayList<I_CmsPropertyProvider>();

    /** The internal list of type definitions. */
    private List<TypeDefinitionContainer> m_typeList;

    /** The internal map of type definitions. */
    private Map<String, TypeDefinitionContainerImpl> m_types;

    /**
     * Creates a new type manager instance.<p>
     *
     * @param adminCms a CMS context with admin privileges
     * @param propertyProviders list which will be filled with property providers
     *
     * @throws CmsException if something goes wrong
     */
    public CmsCmisTypeManager(CmsObject adminCms, List<I_CmsPropertyProvider> propertyProviders)
    throws CmsException {

        m_adminCms = adminCms;
        m_propertyProviders = propertyProviders;
        setup();
    }

    /**
     * Adds the base CMIS property definitions common to folders and documents.<p>
     *
     * @param type the type definition to which the property definitions should be added
     */
    private static void addBasePropertyDefinitions(AbstractTypeDefinition type) {

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.BASE_TYPE_ID,
                "Base Type Id",
                "Base Type Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.OBJECT_ID,
                "Object Id",
                "Object Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.OBJECT_TYPE_ID,
                "Type Id",
                "Type Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.ONCREATE,
                false,
                true));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.NAME,
                "Name",
                "Name",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READWRITE,
                false,
                true));

        type.addPropertyDefinition(
            queryableAndOrderable(
                createPropDef(
                    PropertyIds.CREATED_BY,
                    "Created By",
                    "Created By",
                    PropertyType.STRING,
                    Cardinality.SINGLE,
                    Updatability.READONLY,
                    false,
                    false)));

        type.addPropertyDefinition(
            queryableAndOrderable(
                createPropDef(
                    PropertyIds.CREATION_DATE,
                    "Creation Date",
                    "Creation Date",
                    PropertyType.DATETIME,
                    Cardinality.SINGLE,
                    Updatability.READONLY,
                    false,
                    false)));

        type.addPropertyDefinition(
            queryableAndOrderable(
                createPropDef(
                    PropertyIds.LAST_MODIFIED_BY,
                    "Last Modified By",
                    "Last Modified By",
                    PropertyType.STRING,
                    Cardinality.SINGLE,
                    Updatability.READONLY,
                    false,
                    false)));

        type.addPropertyDefinition(
            queryableAndOrderable(
                createPropDef(
                    PropertyIds.LAST_MODIFICATION_DATE,
                    "Last Modification Date",
                    "Last Modification Date",
                    PropertyType.DATETIME,
                    Cardinality.SINGLE,
                    Updatability.READONLY,
                    false,
                    false)));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.CHANGE_TOKEN,
                "Change Token",
                "Change Token",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

    }

    /**
     * Adds CMIS property definitions for documents.<p>
     *
     * @param type the document type
     */
    private static void addDocumentPropertyDefinitions(DocumentTypeDefinitionImpl type) {

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.IS_IMMUTABLE,
                "Is Immutable",
                "Is Immutable",
                PropertyType.BOOLEAN,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.IS_LATEST_VERSION,
                "Is Latest Version",
                "Is Latest Version",
                PropertyType.BOOLEAN,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.IS_MAJOR_VERSION,
                "Is Major Version",
                "Is Major Version",
                PropertyType.BOOLEAN,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.IS_LATEST_MAJOR_VERSION,
                "Is Latest Major Version",
                "Is Latest Major Version",
                PropertyType.BOOLEAN,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.VERSION_LABEL,
                "Version Label",
                "Version Label",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.VERSION_SERIES_ID,
                "Version Series Id",
                "Version Series Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,
                "Is Verison Series Checked Out",
                "Is Verison Series Checked Out",
                PropertyType.BOOLEAN,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
                "Version Series Checked Out Id",
                "Version Series Checked Out Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,
                "Version Series Checked Out By",
                "Version Series Checked Out By",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.CHECKIN_COMMENT,
                "Checkin Comment",
                "Checkin Comment",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.CONTENT_STREAM_LENGTH,
                "Content Stream Length",
                "Content Stream Length",
                PropertyType.INTEGER,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.CONTENT_STREAM_MIME_TYPE,
                "MIME Type",
                "MIME Type",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.CONTENT_STREAM_FILE_NAME,
                "Filename",
                "Filename",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.CONTENT_STREAM_ID,
                "Content Stream Id",
                "Content Stream Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));
    }

    /**
     * Adds folder specific CMIS property definitions.<p>
     *
     * @param type the folder type
     */
    private static void addFolderPropertyDefinitions(FolderTypeDefinitionImpl type) {

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.PARENT_ID,
                "Parent Id",
                "Parent Id",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS,
                "Allowed Child Object Type Ids",
                "Allowed Child Object Type Ids",
                PropertyType.ID,
                Cardinality.MULTI,
                Updatability.READONLY,
                false,
                false));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.PATH,
                "Path",
                "Path",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.READONLY,
                false,
                false));
    }

    /**
     * Helper method to add the property definitions specific to relationship types.<p>
     *
     * @param type the type definition to which the property definitions should be added
     */
    private static void addRelationPropertyDefinitions(RelationshipTypeDefinitionImpl type) {

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.SOURCE_ID,
                "Source",
                "Source",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.ONCREATE,
                false,
                true));

        type.addPropertyDefinition(
            createPropDef(
                PropertyIds.TARGET_ID,
                "Target",
                "Target",
                PropertyType.ID,
                Cardinality.SINGLE,
                Updatability.ONCREATE,
                false,
                true));

    }

    /**
     * Copies a type definition.<p>
     *
     * @param type the type definition to copy
     *
     * @return the copied type definition
     */
    private static TypeDefinition copyTypeDefintion(TypeDefinition type) {

        return WSConverter.convert(WSConverter.convert(type));
    }

    /**
     * Creates a property definition.<p>
     *
     * @param id the property definition id
     * @param displayName the property display name
     * @param description the property description
     * @param datatype the property type
     * @param cardinality the property cardinality
     * @param updateability the property updatability
     * @param inherited the property inheritance status
     * @param required true true if the property is required
     *
     * @return the property definition
     */
    private static AbstractPropertyDefinition<?> createPropDef(
        String id,
        String displayName,
        String description,
        PropertyType datatype,
        Cardinality cardinality,
        Updatability updateability,
        boolean inherited,
        boolean required) {

        AbstractPropertyDefinition<?> result = null;

        switch (datatype) {
            case BOOLEAN:
                result = new PropertyBooleanDefinitionImpl();
                break;
            case DATETIME:
                result = new PropertyDateTimeDefinitionImpl();
                break;
            case DECIMAL:
                result = new PropertyDecimalDefinitionImpl();
                break;
            case HTML:
                result = new PropertyHtmlDefinitionImpl();
                break;
            case ID:
                result = new PropertyIdDefinitionImpl();
                break;
            case INTEGER:
                result = new PropertyIntegerDefinitionImpl();
                break;
            case STRING:
                result = new PropertyStringDefinitionImpl();
                break;
            case URI:
                result = new PropertyUriDefinitionImpl();
                break;
            default:
                throw new RuntimeException("Unknown datatype! Spec change?");
        }

        result.setId(id);
        result.setLocalName(id);
        result.setDisplayName(displayName);
        result.setDescription(description);
        result.setPropertyType(datatype);
        result.setCardinality(cardinality);
        result.setUpdatability(updateability);
        result.setIsInherited(Boolean.valueOf(inherited));
        result.setIsRequired(Boolean.valueOf(required));
        result.setIsQueryable(Boolean.FALSE);
        result.setIsOrderable(Boolean.FALSE);
        result.setQueryName(id);
        return result;
    }

    /**
     * Helper method to make a property definition queryable and orderable.<p>
     *
     * @param propDef the property definition
     *
     * @return the modified property definition
     */
    private static AbstractPropertyDefinition<?> queryableAndOrderable(AbstractPropertyDefinition<?> propDef) {

        propDef.setIsQueryable(Boolean.TRUE);
        propDef.setIsOrderable(Boolean.TRUE);
        return propDef;
    }

    /**
     * Gets a list of names of OpenCms property definitions.<p>
     *
     * @return the list of OpenCms property names
     */
    public List<String> getCmsPropertyNames() {

        refresh();
        List<String> result = new ArrayList<String>();
        for (CmsPropertyDefinition propDef : m_cmsPropertyDefinitions) {
            result.add(propDef.getName());
        }
        return result;
    }

    /**
     * Gets the property provider for a given key.<p>
     *
     * @param key the property nme
     *
     * @return the property provider for the given name, or null if there isn't any
     */
    public I_CmsPropertyProvider getPropertyProvider(String key) {

        if (key.startsWith(PROPERTY_PREFIX_DYNAMIC)) {
            key = key.substring(PROPERTY_PREFIX_DYNAMIC.length());
        }
        for (I_CmsPropertyProvider provider : m_propertyProviders) {
            if (provider.getName().equals(key)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Gets the list of all property providers.<p>
     *
     * @return the list of property providers
     */
    public List<I_CmsPropertyProvider> getPropertyProviders() {

        return Collections.unmodifiableList(m_propertyProviders);
    }

    /**
     * Gets a type definition by id.<p>
     *
     * @param typeId the type id
     * @return the type definition
     */
    public TypeDefinition getType(String typeId) {

        refresh();
        TypeDefinitionContainer tc = m_types.get(typeId);
        if (tc == null) {
            return null;
        }

        return tc.getTypeDefinition();
    }

    /**
     * Collects the children of a type.<p>
     *
     * @param typeId the id of the type
     * @param includePropertyDefinitions true if the property definitions should be included
     * @param maxItems the maximum number of items to return
     * @param skipCount the number of items to skip
     *
     * @return the children of the type
     */
    public TypeDefinitionList getTypeChildren(

        String typeId,
        boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount) {

        refresh();
        TypeDefinitionListImpl result = new TypeDefinitionListImpl(new ArrayList<TypeDefinition>());

        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 1) {
            return result;
        }

        if (typeId == null) {
            if (skip < 1) {
                result.getList().add(copyTypeDefintion(m_types.get(FOLDER_TYPE_ID).getTypeDefinition()));
                max--;
            }
            if ((skip < 2) && (max > 0)) {
                result.getList().add(copyTypeDefintion(m_types.get(DOCUMENT_TYPE_ID).getTypeDefinition()));
                max--;
            }

            result.setHasMoreItems(Boolean.valueOf((result.getList().size() + skip) < 2));
            result.setNumItems(BigInteger.valueOf(2));
        } else {
            TypeDefinitionContainer tc = m_types.get(typeId);
            if ((tc == null) || (tc.getChildren() == null)) {
                return result;
            }

            for (TypeDefinitionContainer child : tc.getChildren()) {
                if (skip > 0) {
                    skip--;
                    continue;
                }

                result.getList().add(copyTypeDefintion(child.getTypeDefinition()));

                max--;
                if (max == 0) {
                    break;
                }
            }

            result.setHasMoreItems(Boolean.valueOf((result.getList().size() + skip) < tc.getChildren().size()));
            result.setNumItems(BigInteger.valueOf(tc.getChildren().size()));
        }

        if (!includePropertyDefinitions) {
            for (TypeDefinition type : result.getList()) {
                type.getPropertyDefinitions().clear();
            }
        }

        return result;
    }

    /**
     * Gets the type definition for a given id in the given call context.<p>
     *
     * @param typeId the type id
     *
     * @return the matching type definition
     */
    public TypeDefinition getTypeDefinition(String typeId) {

        refresh();
        TypeDefinitionContainer tc = m_types.get(typeId);
        if (tc == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        return copyTypeDefintion(tc.getTypeDefinition());
    }

    /**
     * Gets the descendants of a type.<p>
     *
     * @param typeId the parent type id
     * @param depth the depth up to which the descendant types should be collected
     * @param includePropertyDefinitions true if the property definitions should be included
     *
     * @return the descendants of the type
     */
    public List<TypeDefinitionContainer> getTypeDescendants(

        String typeId,
        BigInteger depth,
        boolean includePropertyDefinitions) {

        refresh();
        List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

        // check depth
        int d = (depth == null ? -1 : depth.intValue());
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }

        if (typeId == null) {
            result.add(getTypeDescendants(d, m_types.get(FOLDER_TYPE_ID), includePropertyDefinitions));
            result.add(getTypeDescendants(d, m_types.get(DOCUMENT_TYPE_ID), includePropertyDefinitions));
            result.add(getTypeDescendants(d, m_types.get(RELATIONSHIP_TYPE_ID), includePropertyDefinitions));
        } else {
            TypeDefinitionContainer tc = m_types.get(typeId);
            if (tc != null) {
                result.add(getTypeDescendants(d, tc, includePropertyDefinitions));
            }
        }

        return result;
    }

    /**
     * Creates the CMIS property definition for an OpenCms resource property definition.<p>
     *
     * @param cmsDef the OpenCms property definition
     *
     * @return the CMIS property definition
     */
    PropertyDefinition<?> createOpenCmsPropertyDefinition(CmsPropertyDefinition cmsDef) {

        return createPropDef(
            PROPERTY_PREFIX + cmsDef.getName(),
            cmsDef.getName(),
            cmsDef.getName(),
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READWRITE,
            false,
            false);
    }

    /**
     * Creates the base types.
     *
     * @throws CmsException if something goes wrong
     */
    void setup() throws CmsException {

        m_types = new HashMap<String, TypeDefinitionContainerImpl>();
        m_typeList = new ArrayList<TypeDefinitionContainer>();
        m_cmsPropertyDefinitions = m_adminCms.readAllPropertyDefinitions();

        // folder type
        FolderTypeDefinitionImpl folderType = new FolderTypeDefinitionImpl();
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        folderType.setIsControllableAcl(Boolean.TRUE);
        folderType.setIsControllablePolicy(Boolean.FALSE);
        folderType.setIsCreatable(Boolean.TRUE);
        folderType.setDescription("Folder");
        folderType.setDisplayName("Folder");
        folderType.setIsFileable(Boolean.TRUE);
        folderType.setIsFulltextIndexed(Boolean.FALSE);
        folderType.setIsIncludedInSupertypeQuery(Boolean.TRUE);
        folderType.setLocalName("Folder");
        folderType.setLocalNamespace(NAMESPACE);
        folderType.setIsQueryable(Boolean.TRUE);
        folderType.setQueryName("cmis:folder");
        folderType.setId(FOLDER_TYPE_ID);

        addBasePropertyDefinitions(folderType);
        addFolderPropertyDefinitions(folderType);
        addCmsPropertyDefinitions(folderType);
        addProviderPropertyDefinitions(folderType);

        addTypeInternal(folderType);

        // document type
        DocumentTypeDefinitionImpl documentType = new DocumentTypeDefinitionImpl();
        documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        documentType.setIsControllableAcl(Boolean.TRUE);
        documentType.setIsControllablePolicy(Boolean.FALSE);
        documentType.setIsCreatable(Boolean.TRUE);
        documentType.setDescription("Document");
        documentType.setDisplayName("Document");
        documentType.setIsFileable(Boolean.TRUE);
        documentType.setIsFulltextIndexed(Boolean.FALSE);
        documentType.setIsIncludedInSupertypeQuery(Boolean.TRUE);
        documentType.setLocalName("Document");
        documentType.setLocalNamespace(NAMESPACE);
        documentType.setIsQueryable(Boolean.TRUE);
        documentType.setQueryName("cmis:document");
        documentType.setId(DOCUMENT_TYPE_ID);

        documentType.setIsVersionable(Boolean.FALSE);
        documentType.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);

        addBasePropertyDefinitions(documentType);
        addDocumentPropertyDefinitions(documentType);
        addCmsPropertyDefinitions(documentType);
        addProviderPropertyDefinitions(documentType);

        addTypeInternal(documentType);

        // relationship types
        RelationshipTypeDefinitionImpl relationshipType = new RelationshipTypeDefinitionImpl();
        relationshipType.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
        relationshipType.setIsControllableAcl(Boolean.FALSE);
        relationshipType.setIsControllablePolicy(Boolean.FALSE);
        relationshipType.setIsCreatable(Boolean.FALSE);
        relationshipType.setDescription("Relationship");
        relationshipType.setDisplayName("Relationship");
        relationshipType.setIsFileable(Boolean.FALSE);
        relationshipType.setIsIncludedInSupertypeQuery(Boolean.TRUE);
        relationshipType.setLocalName("Relationship");
        relationshipType.setLocalNamespace(NAMESPACE);
        relationshipType.setIsQueryable(Boolean.FALSE);
        relationshipType.setQueryName("cmis:relationship");
        relationshipType.setId(RELATIONSHIP_TYPE_ID);
        List<String> typeList = new ArrayList<String>();
        typeList.add("cmis:document");
        typeList.add("cmis:folder");
        relationshipType.setAllowedSourceTypes(typeList);
        relationshipType.setAllowedTargetTypes(typeList);
        addBasePropertyDefinitions(relationshipType);
        addRelationPropertyDefinitions(relationshipType);
        addTypeInternal(relationshipType);

        for (CmsRelationType relType : CmsRelationType.getAll()) {
            createRelationshipType(relType);
        }
        m_lastUpdate = System.currentTimeMillis();
    }

    /**
     * Adds the CMIS property definitions corresponding to the OpenCms property definitions to a CMIS type definition.<p>
     *
     * @param type the type to which the property definitions should be added
     */
    private void addCmsPropertyDefinitions(AbstractTypeDefinition type) {

        for (CmsPropertyDefinition propDef : m_cmsPropertyDefinitions) {
            type.addPropertyDefinition(createOpenCmsPropertyDefinition(propDef));
            type.addPropertyDefinition(
                createPropDef(
                    INHERITED_PREFIX + propDef.getName(),
                    propDef.getName(),
                    propDef.getName(),
                    PropertyType.STRING,
                    Cardinality.SINGLE,
                    Updatability.READONLY,
                    false,
                    false));

        }
        type.addPropertyDefinition(
            createPropDef(
                PROPERTY_RESOURCE_TYPE,
                "Resource type",
                "Resource type",
                PropertyType.STRING,
                Cardinality.SINGLE,
                Updatability.ONCREATE,
                false,
                true));
    }

    /**
     * Helper method for adding property definitions for the dynamic properties.<p>
     *
     * @param type the type definition to which the properties should be added
     */
    private void addProviderPropertyDefinitions(AbstractTypeDefinition type) {

        for (I_CmsPropertyProvider provider : m_propertyProviders) {
            type.addPropertyDefinition(
                createPropDef(
                    PROPERTY_PREFIX_DYNAMIC + provider.getName(),
                    provider.getName(),
                    provider.getName(),
                    PropertyType.STRING,
                    Cardinality.SINGLE,
                    provider.isWritable() ? Updatability.READWRITE : Updatability.READONLY,
                    false,
                    false));
        }
    }

    /**
     * Adds a type to collection with inheriting base type properties.
     *
     * @param type the type definition to add
     *
     * @return true if the type definition was added
     */
    private boolean addType(TypeDefinition type) {

        if (type == null) {
            return false;
        }

        if (type.getBaseTypeId() == null) {
            return false;
        }

        // find base type
        TypeDefinition baseType = null;
        if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
            baseType = copyTypeDefintion(m_types.get(DOCUMENT_TYPE_ID).getTypeDefinition());
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
            baseType = copyTypeDefintion(m_types.get(FOLDER_TYPE_ID).getTypeDefinition());
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_RELATIONSHIP) {
            baseType = copyTypeDefintion(m_types.get(RELATIONSHIP_TYPE_ID).getTypeDefinition());
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_POLICY) {
            baseType = copyTypeDefintion(m_types.get(POLICY_TYPE_ID).getTypeDefinition());
        } else {
            return false;
        }

        AbstractTypeDefinition newType = (AbstractTypeDefinition)copyTypeDefintion(type);

        // copy property definition
        for (PropertyDefinition<?> propDef : baseType.getPropertyDefinitions().values()) {
            ((AbstractPropertyDefinition<?>)propDef).setIsInherited(Boolean.TRUE);
            newType.addPropertyDefinition(propDef);
        }

        // add it
        addTypeInternal(newType);
        return true;
    }

    /**
     * Internal method which adds a new type, without adding any property definitions automatically.<p>
     *
     * @param type the type to add
     */
    private void addTypeInternal(AbstractTypeDefinition type) {

        if (type == null) {
            return;
        }

        if (m_types.containsKey(type.getId())) {
            // can't overwrite a type
            return;
        }

        TypeDefinitionContainerImpl tc = new TypeDefinitionContainerImpl();
        tc.setTypeDefinition(type);

        // add to parent
        if (type.getParentTypeId() != null) {
            TypeDefinitionContainerImpl tdc = m_types.get(type.getParentTypeId());
            if (tdc != null) {
                if (tdc.getChildren() == null) {
                    tdc.setChildren(new ArrayList<TypeDefinitionContainer>());
                }
                tdc.getChildren().add(tc);
            }
        }

        m_types.put(type.getId(), tc);
        m_typeList.add(tc);
    }

    /**
     * Creates a CMIS relationship subtype for a given OpenCms relation type.<p>
     *
     * @param relType the OpenCms relation type
     */
    private void createRelationshipType(CmsRelationType relType) {

        // relationship types
        RelationshipTypeDefinitionImpl relationshipType = new RelationshipTypeDefinitionImpl();
        relationshipType.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
        relationshipType.setParentTypeId(RELATIONSHIP_TYPE_ID);
        relationshipType.setIsControllableAcl(Boolean.FALSE);
        relationshipType.setIsControllablePolicy(Boolean.FALSE);
        relationshipType.setIsCreatable(Boolean.valueOf(!relType.isDefinedInContent()));
        relationshipType.setDescription(relType.getName());
        relationshipType.setDisplayName(relType.getName());
        relationshipType.setIsFileable(Boolean.FALSE);
        relationshipType.setIsIncludedInSupertypeQuery(Boolean.TRUE);
        relationshipType.setLocalName(relType.getName());
        relationshipType.setLocalNamespace(NAMESPACE);
        relationshipType.setIsQueryable(Boolean.FALSE);
        String id = "opencms:" + relType.getName().toUpperCase();
        relationshipType.setQueryName(id);
        relationshipType.setId(id);
        List<String> typeList = new ArrayList<String>();
        typeList.add("cmis:document");
        typeList.add("cmis:folder");
        relationshipType.setAllowedSourceTypes(typeList);
        relationshipType.setAllowedTargetTypes(typeList);
        addType(relationshipType);
    }

    /**
     * Collects the descendants of a type.<p>
     *
     * @param depth the depth up to which the descendants should be collected
     * @param tc the parent type
     * @param includePropertyDefinitions true if the property definitions should be included
     *
     * @return the descendants of the type
     */
    private TypeDefinitionContainer getTypeDescendants(
        int depth,
        TypeDefinitionContainer tc,
        boolean includePropertyDefinitions) {

        TypeDefinitionContainerImpl result = new TypeDefinitionContainerImpl();

        TypeDefinition type = copyTypeDefintion(tc.getTypeDefinition());
        if (!includePropertyDefinitions) {
            type.getPropertyDefinitions().clear();
        }

        result.setTypeDefinition(type);

        if (depth != 0) {
            if (tc.getChildren() != null) {
                result.setChildren(new ArrayList<TypeDefinitionContainer>());
                for (TypeDefinitionContainer tdc : tc.getChildren()) {
                    result.getChildren().add(
                        getTypeDescendants(depth < 0 ? -1 : depth - 1, tdc, includePropertyDefinitions));
                }
            }
        }

        return result;
    }

    /**
     * Refreshes the internal data if the last update was longer ago than the udpate interval.<p>
     */
    private synchronized void refresh() {

        try {
            long now = System.currentTimeMillis();
            if ((now - m_lastUpdate) > UPDATE_INTERVAL) {
                setup();
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
