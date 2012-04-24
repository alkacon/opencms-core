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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Type Manager.
 */
public class CmsCmisTypeManager {

    public static final String DOCUMENT_TYPE_ID = BaseTypeId.CMIS_DOCUMENT.value();
    public static final String FOLDER_TYPE_ID = BaseTypeId.CMIS_FOLDER.value();
    public static final String RELATIONSHIP_TYPE_ID = BaseTypeId.CMIS_RELATIONSHIP.value();
    public static final String POLICY_TYPE_ID = BaseTypeId.CMIS_POLICY.value();

    public static final String PROPERTY_PREFIX = "opencms:";
    public static final String SPECIAL_PROPERTY_PREFIX = "opencms-special:";
    public static final String PROPERTY_RESOURCE_TYPE = SPECIAL_PROPERTY_PREFIX + "resource-type";

    private static final String NAMESPACE = "http://opencms.org/opencms-cmis";

    private static final Log log = LogFactory.getLog(CmsCmisTypeManager.class);

    private Map<String, TypeDefinitionContainerImpl> types;
    private List<TypeDefinitionContainer> typesList;

    private CmsObject m_adminCms;

    public CmsCmisTypeManager(CmsObject adminCms)
    throws CmsException {

        m_adminCms = adminCms;

        setup();
    }

    public List<String> getCmsPropertyNames() {

        List<String> result = new ArrayList<String>();
        for (CmsPropertyDefinition propDef : m_cmsPropertyDefinitions) {
            result.add(propDef.getName());
        }
        return result;
    }

    /**
     * Creates the base types.
     */
    private void setup() throws CmsException {

        types = new HashMap<String, TypeDefinitionContainerImpl>();
        typesList = new ArrayList<TypeDefinitionContainer>();
        m_cmsPropertyDefinitions = m_adminCms.readAllPropertyDefinitions();

        // folder type
        FolderTypeDefinitionImpl folderType = new FolderTypeDefinitionImpl();
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        folderType.setIsControllableAcl(false);
        folderType.setIsControllablePolicy(false);
        folderType.setIsCreatable(true);
        folderType.setDescription("Folder");
        folderType.setDisplayName("Folder");
        folderType.setIsFileable(true);
        folderType.setIsFulltextIndexed(false);
        folderType.setIsIncludedInSupertypeQuery(true);
        folderType.setLocalName("Folder");
        folderType.setLocalNamespace(NAMESPACE);
        folderType.setIsQueryable(false);
        folderType.setQueryName("cmis:folder");
        folderType.setId(FOLDER_TYPE_ID);

        addBasePropertyDefinitions(folderType);
        addFolderPropertyDefinitions(folderType);
        addCmsPropertyDefinitions(folderType);

        addTypeInteral(folderType);

        // document type
        DocumentTypeDefinitionImpl documentType = new DocumentTypeDefinitionImpl();
        documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        documentType.setIsControllableAcl(false);
        documentType.setIsControllablePolicy(false);
        documentType.setIsCreatable(true);
        documentType.setDescription("Document");
        documentType.setDisplayName("Document");
        documentType.setIsFileable(true);
        documentType.setIsFulltextIndexed(false);
        documentType.setIsIncludedInSupertypeQuery(true);
        documentType.setLocalName("Document");
        documentType.setLocalNamespace(NAMESPACE);
        documentType.setIsQueryable(false);
        documentType.setQueryName("cmis:document");
        documentType.setId(DOCUMENT_TYPE_ID);

        documentType.setIsVersionable(false);
        documentType.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        addBasePropertyDefinitions(documentType);
        addDocumentPropertyDefinitions(documentType);
        addCmsPropertyDefinitions(documentType);

        addTypeInteral(documentType);

        // relationship types
        RelationshipTypeDefinitionImpl relationshipType = new RelationshipTypeDefinitionImpl();
        relationshipType.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
        relationshipType.setIsControllableAcl(false);
        relationshipType.setIsControllablePolicy(false);
        relationshipType.setIsCreatable(false);
        relationshipType.setDescription("Relationship");
        relationshipType.setDisplayName("Relationship");
        relationshipType.setIsFileable(false);
        relationshipType.setIsIncludedInSupertypeQuery(true);
        relationshipType.setLocalName("Relationship");
        relationshipType.setLocalNamespace(NAMESPACE);
        relationshipType.setIsQueryable(false);
        relationshipType.setQueryName("cmis:relationship");
        relationshipType.setId(RELATIONSHIP_TYPE_ID);

        addBasePropertyDefinitions(relationshipType);

        // not supported - don't expose it
        // addTypeInteral(relationshipType);

        // policy type
        PolicyTypeDefinitionImpl policyType = new PolicyTypeDefinitionImpl();
        policyType.setBaseTypeId(BaseTypeId.CMIS_POLICY);
        policyType.setIsControllableAcl(false);
        policyType.setIsControllablePolicy(false);
        policyType.setIsCreatable(false);
        policyType.setDescription("Policy");
        policyType.setDisplayName("Policy");
        policyType.setIsFileable(false);
        policyType.setIsIncludedInSupertypeQuery(true);
        policyType.setLocalName("Policy");
        policyType.setLocalNamespace(NAMESPACE);
        policyType.setIsQueryable(false);
        policyType.setQueryName("cmis:policy");
        policyType.setId(POLICY_TYPE_ID);

        addBasePropertyDefinitions(policyType);

        // not supported - don't expose it
        // addTypeInteral(policyType);
    }

    private List<CmsPropertyDefinition> m_cmsPropertyDefinitions;

    private void addCmsPropertyDefinitions(AbstractTypeDefinition type) throws CmsException {

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

        type.addPropertyDefinition(createPropDef(
            PropertyIds.CREATED_BY,
            "Created By",
            "Created By",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.CREATION_DATE,
            "Creation Date",
            "Creation Date",
            PropertyType.DATETIME,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.LAST_MODIFIED_BY,
            "Last Modified By",
            "Last Modified By",
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.LAST_MODIFICATION_DATE,
            "Last Modification Date",
            "Last Modification Date",
            PropertyType.DATETIME,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false));

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

    PropertyDefinition<?> createOpenCmsPropertyDefinition(CmsPropertyDefinition cmsDef) {

        return createPropDef(
            PROPERTY_PREFIX + cmsDef.getName(),
            cmsDef.getName(),
            cmsDef.getName(),
            PropertyType.STRING,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            false);
    }

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
            true));

        type.addPropertyDefinition(createPropDef(
            PropertyIds.VERSION_SERIES_ID,
            "Version Series Id",
            "Version Series Id",
            PropertyType.ID,
            Cardinality.SINGLE,
            Updatability.READONLY,
            false,
            true));

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
     * Creates a property definition object.
     */
    private static PropertyDefinition<?> createPropDef(
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
     */
    private void addTypeInteral(AbstractTypeDefinition type) {

        if (type == null) {
            return;
        }

        if (types.containsKey(type.getId())) {
            // can't overwrite a type
            return;
        }

        TypeDefinitionContainerImpl tc = new TypeDefinitionContainerImpl();
        tc.setTypeDefinition(type);

        // add to parent
        if (type.getParentTypeId() != null) {
            TypeDefinitionContainerImpl tdc = types.get(type.getParentTypeId());
            if (tdc != null) {
                if (tdc.getChildren() == null) {
                    tdc.setChildren(new ArrayList<TypeDefinitionContainer>());
                }
                tdc.getChildren().add(tc);
            }
        }

        types.put(type.getId(), tc);
        typesList.add(tc);
    }

    /**
     * CMIS getTypesChildren.
     */
    public TypeDefinitionList getTypesChildren(
        CallContext context,
        String typeId,
        boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount) {

        TypeDefinitionListImpl result = new TypeDefinitionListImpl(new ArrayList<TypeDefinition>());
        return result;
    }

    /**
     * CMIS getTypesDescendants.
     */
    public List<TypeDefinitionContainer> getTypesDescendants(
        CallContext context,
        String typeId,
        BigInteger depth,
        Boolean includePropertyDefinitions) {

        List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();
        TypeDefinitionContainerImpl tdci = new TypeDefinitionContainerImpl();
        tdci.setTypeDefinition(getType(typeId));
        result.add(tdci);
        return result;
    }

    /**
     * Gathers the type descendants tree.
     */
    private TypeDefinitionContainer getTypesDescendants(
        int depth,
        TypeDefinitionContainer tc,
        boolean includePropertyDefinitions) {

        return tc;
    }

    /**
     * For internal use.
     */
    public TypeDefinition getType(String typeId) {

        TypeDefinitionContainer tc = types.get(typeId);
        if (tc == null) {
            return null;
        }

        return tc.getTypeDefinition();
    }

    /**
     * CMIS getTypeDefinition.
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId) {

        TypeDefinitionContainer tc = types.get(typeId);
        if (tc == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        return copyTypeDefintion(tc.getTypeDefinition());
    }

    private static TypeDefinition copyTypeDefintion(TypeDefinition type) {

        return Converter.convert(Converter.convert(type));
    }
}
