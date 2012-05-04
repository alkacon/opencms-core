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
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyTypeDefinitionImpl;
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
import org.apache.chemistry.opencmis.commons.server.CallContext;

/**
 * Type Manager.
 */
public class CmsCmisTypeManager {

    /** CMIS type id for documents. */
    public static final String DOCUMENT_TYPE_ID = BaseTypeId.CMIS_DOCUMENT.value();

    /** CMIS type id for folders. */
    public static final String FOLDER_TYPE_ID = BaseTypeId.CMIS_FOLDER.value();

    /** CMIS type id for relationships. */
    public static final String RELATIONSHIP_TYPE_ID = BaseTypeId.CMIS_RELATIONSHIP.value();

    /** CMIS type id for policies. */
    public static final String POLICY_TYPE_ID = BaseTypeId.CMIS_POLICY.value();

    /** The prefix used for normal OpenCms resource properties. */
    public static final String PROPERTY_PREFIX = "opencms:";

    /** The prefix used for special properties which are OpenCms specific but are not resource properties. */
    public static final String SPECIAL_PROPERTY_PREFIX = "opencms-special:";

    /** The name of the propery containing the resource type name. */
    public static final String PROPERTY_RESOURCE_TYPE = SPECIAL_PROPERTY_PREFIX + "resource-type";

    /** The namespace used for properties. */
    private static final String NAMESPACE = "http://opencms.org/opencms-cmis";

    /** The internal map of type definitions. */
    private Map<String, TypeDefinitionContainerImpl> m_types;

    /** The internal list of type definitions. */
    private List<TypeDefinitionContainer> m_typeList;

    /** The admin CMS context. */
    private CmsObject m_adminCms;

    /**
     * Creates a new type manager instance.<p>
     * 
     * @param adminCms a CMS context with admin privileges 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsCmisTypeManager(CmsObject adminCms)
    throws CmsException {

        m_adminCms = adminCms;

        setup();
    }

    /**
     * Gets a list of names of OpenCms property definitions.<p>
     * 
     * @return the list of OpenCms property names 
     */
    public List<String> getCmsPropertyNames() {

        List<String> result = new ArrayList<String>();
        for (CmsPropertyDefinition propDef : m_cmsPropertyDefinitions) {
            result.add(propDef.getName());
        }
        return result;
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
        folderType.setIsQueryable(Boolean.FALSE);
        folderType.setQueryName("cmis:folder");
        folderType.setId(FOLDER_TYPE_ID);

        addBasePropertyDefinitions(folderType);
        addFolderPropertyDefinitions(folderType);
        addCmsPropertyDefinitions(folderType);

        internalAddType(folderType);

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
        documentType.setIsQueryable(Boolean.FALSE);
        documentType.setQueryName("cmis:document");
        documentType.setId(DOCUMENT_TYPE_ID);

        documentType.setIsVersionable(Boolean.FALSE);
        documentType.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);

        addBasePropertyDefinitions(documentType);
        addDocumentPropertyDefinitions(documentType);
        addCmsPropertyDefinitions(documentType);

        internalAddType(documentType);

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

        addBasePropertyDefinitions(relationshipType);

        // not supported - don't expose it
        // addTypeInteral(relationshipType);

        // policy type
        PolicyTypeDefinitionImpl policyType = new PolicyTypeDefinitionImpl();
        policyType.setBaseTypeId(BaseTypeId.CMIS_POLICY);
        policyType.setIsControllableAcl(Boolean.FALSE);
        policyType.setIsControllablePolicy(Boolean.FALSE);
        policyType.setIsCreatable(Boolean.FALSE);
        policyType.setDescription("Policy");
        policyType.setDisplayName("Policy");
        policyType.setIsFileable(Boolean.FALSE);
        policyType.setIsIncludedInSupertypeQuery(Boolean.TRUE);
        policyType.setLocalName("Policy");
        policyType.setLocalNamespace(NAMESPACE);
        policyType.setIsQueryable(Boolean.FALSE);
        policyType.setQueryName("cmis:policy");
        policyType.setId(POLICY_TYPE_ID);

        addBasePropertyDefinitions(policyType);

        // not supported - don't expose it
        // addTypeInteral(policyType);
    }

    /** The list of OpenCms property definitions. */
    private List<CmsPropertyDefinition> m_cmsPropertyDefinitions;

    /**
     * Adds the CMIS property definitions corresponding to the OpenCms property definitions to a CMIS type definition.<p>
     *  
     * @param type the type to which the property definitions should be added
     */
    private void addCmsPropertyDefinitions(AbstractTypeDefinition type) {

        for (CmsPropertyDefinition propDef : m_cmsPropertyDefinitions) {
            type.addPropertyDefinition(createOpenCmsPropertyDefinition(propDef));
        }
        type.addPropertyDefinition(createPropDef(
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
     * Adds the base CMIS property definitions common to folders and documents.<p>
     * 
     * @param type the type definition to which the property definitions should be added 
     */
    private static void addBasePropertyDefinitions(AbstractTypeDefinition type) {

        type.addPropertyDefinition(createPropDef(
            PropertyIds.BASE_TYPE_ID,
            "Base Type Id",
            "Base Type Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.OBJECT_ID,
            "Object Id",
            "Object Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.OBJECT_TYPE_ID,
            "Type Id",
            "Type Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.ONCREATE,
            false,
            true));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.NAME,
            "Name",
            "Name",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READWRITE,
            false,
            true));

        type.addPropertyDefinition(queryableAndOrderable(createPropDef(
            PropertyIds.CREATED_BY,
            "Created By",
            "Created By",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false)));

        type.addPropertyDefinition(queryableAndOrderable(createPropDef(
            PropertyIds.CREATION_DATE,
            "Creation Date",
            "Creation Date",
            PropertyType.DATETIME,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false)));

        type.addPropertyDefinition(queryableAndOrderable(createPropDef(
            PropertyIds.LAST_MODIFIED_BY,
            "Last Modified By",
            "Last Modified By",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false)));

        type.addPropertyDefinition(queryableAndOrderable(createPropDef(
            PropertyIds.LAST_MODIFICATION_DATE,
            "Last Modification Date",
            "Last Modification Date",
            PropertyType.DATETIME,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false)));

        type.addPropertyDefinition(createPropDef(
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
     * Adds folder specific CMIS property definitions.<p>
     * 
     * @param type the folder type 
     */
    private static void addFolderPropertyDefinitions(FolderTypeDefinitionImpl type) {

        type.addPropertyDefinition(createPropDef(
            PropertyIds.PARENT_ID,
            "Parent Id",
            "Parent Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS,
            "Allowed Child Object Type Ids",
            "Allowed Child Object Type Ids",
            PropertyType.ID,
            Cardinality.MULTI,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
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
     * Adds CMIS property definitions for documents.<p>
     * 
     * @param type the document type 
     */
    private static void addDocumentPropertyDefinitions(DocumentTypeDefinitionImpl type) {

        type.addPropertyDefinition(createPropDef(
            PropertyIds.IS_IMMUTABLE,
            "Is Immutable",
            "Is Immutable",
            PropertyType.BOOLEAN,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.IS_LATEST_VERSION,
            "Is Latest Version",
            "Is Latest Version",
            PropertyType.BOOLEAN,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.IS_MAJOR_VERSION,
            "Is Major Version",
            "Is Major Version",
            PropertyType.BOOLEAN,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.IS_LATEST_MAJOR_VERSION,
            "Is Latest Major Version",
            "Is Latest Major Version",
            PropertyType.BOOLEAN,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.VERSION_LABEL,
            "Version Label",
            "Version Label",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.VERSION_SERIES_ID,
            "Version Series Id",
            "Version Series Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,
            "Is Verison Series Checked Out",
            "Is Verison Series Checked Out",
            PropertyType.BOOLEAN,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
            "Version Series Checked Out Id",
            "Version Series Checked Out Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,
            "Version Series Checked Out By",
            "Version Series Checked Out By",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.CHECKIN_COMMENT,
            "Checkin Comment",
            "Checkin Comment",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.CONTENT_STREAM_LENGTH,
            "Content Stream Length",
            "Content Stream Length",
            PropertyType.INTEGER,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.CONTENT_STREAM_MIME_TYPE,
            "MIME Type",
            "MIME Type",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.CONTENT_STREAM_FILE_NAME,
            "Filename",
            "Filename",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
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
     * Adds a type to collection.
     * 
     * @param type the type definition to add 
     */
    private void internalAddType(AbstractTypeDefinition type) {

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
     * Gets a type definition by id.<p>
     * 
     * @param typeId the type id 
     * @return the type definition 
     */
    public TypeDefinition getType(String typeId) {

        TypeDefinitionContainer tc = m_types.get(typeId);
        if (tc == null) {
            return null;
        }

        return tc.getTypeDefinition();
    }

    /**
     * Gets the type definition for a given id in the given call context.<p>
     * 
     * @param context the call context 
     * @param typeId the type id
     * 
     *  @return the matching type definition 
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId) {

        TypeDefinitionContainer tc = m_types.get(typeId);
        if (tc == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        return copyTypeDefintion(tc.getTypeDefinition());
    }

    /**
     * Copies a type definition.<p>
     * 
     * @param type the type definition to copy
     *  
     * @return the copied type definition 
     */
    private static TypeDefinition copyTypeDefintion(TypeDefinition type) {

        return Converter.convert(Converter.convert(type));
    }

    /**
     * Copies a type definition.<p>
     * 
     * @param type the type definition to copy
     *  
     * @return the copied type definition 
     */
    private static TypeDefinition copyTypeDefintion(TypeDefinition type, boolean keepProperties) {

        TypeDefinition result = Converter.convert(Converter.convert(type));
        if (!keepProperties) {
            result.getPropertyDefinitions().clear();
        }
        return result;
    }

    protected List getTypeChildren(TypeDefinition typeDef) {

        return Collections.<TypeDefinition> emptyList();
    }

    public List<TypeDefinitionContainer> getTypeDescendants(
        CallContext context,
        String typeId,
        BigInteger depth,
        Boolean includePropertyDefinitions) {

        if (typeId == null) {
            List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();
            TypeDefinitionContainerImpl folderType = new TypeDefinitionContainerImpl();
            folderType.setTypeDefinition(copyTypeDefintion(
                getType(FOLDER_TYPE_ID),
                includePropertyDefinitions.booleanValue()));
            folderType.setChildren(Collections.<TypeDefinitionContainer> emptyList());
            result.add(folderType);
            TypeDefinitionContainerImpl documentType = new TypeDefinitionContainerImpl();
            documentType.setTypeDefinition(copyTypeDefintion(
                getType(DOCUMENT_TYPE_ID),
                includePropertyDefinitions.booleanValue()));
            documentType.setChildren(Collections.<TypeDefinitionContainer> emptyList());
            result.add(documentType);
            return result;
        } else {
            return Collections.emptyList();
        }
    }
}
