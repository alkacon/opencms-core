/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor;

import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.acacia.shared.Entity;
import com.alkacon.acacia.shared.Type;
import com.alkacon.acacia.shared.ValidationResult;
import com.alkacon.vie.shared.I_Entity;
import com.alkacon.vie.shared.I_EntityAttribute;
import com.alkacon.vie.shared.I_Type;

import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.editors.CmsEditor;
import org.opencms.workplace.editors.CmsXmlContentEditor;
import org.opencms.workplace.explorer.CmsNewResourceXmlContent;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * Service to provide entity persistence within OpenCms.<p>
 */
public class CmsContentService extends CmsGwtService implements I_CmsContentService {

    /** The logger for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsContentService.class);

    /** The type name prefix. */
    static final String TYPE_NAME_PREFIX = "http://opencms.org/types/";

    /** The serial version id. */
    private static final long serialVersionUID = 7873052619331296648L;

    /** The current users workplace locale. */
    private Locale m_workplaceLocale;

    /**
     * Returns the entity attribute name representing the given content value.<p>
     * 
     * @param contentValue the content value
     * 
     * @return the attribute name
     */
    public static String getAttributeName(I_CmsXmlContentValue contentValue) {

        return getTypeUri(contentValue.getContentDefinition()) + "/" + contentValue.getName();
    }

    /**
     * Returns the entity attribute name to use for this element.<p>
     * 
     * @param elementName the element name
     * @param parentType the parent type
     * 
     * @return the attribute name
     */
    public static String getAttributeName(String elementName, String parentType) {

        return parentType + "/" + elementName;
    }

    /**
     * Returns the entity id to the given content value.<p>
     * 
     * @param contentValue the content value
     * 
     * @return the entity id
     */
    public static String getEntityId(I_CmsXmlContentValue contentValue) {

        String result = CmsContentDefinition.uuidToEntityId(
            contentValue.getDocument().getFile().getStructureId(),
            contentValue.getLocale().toString());
        String valuePath = contentValue.getPath();
        if (valuePath.contains("/")) {
            result += "/" + valuePath.substring(0, valuePath.lastIndexOf("/"));
        }
        if (contentValue.isChoiceOption()) {
            result += "/"
                + Type.CHOICE_ATTRIBUTE_NAME
                + "_"
                + contentValue.getName()
                + "["
                + contentValue.getXmlIndex()
                + "]";
        }
        return result;
    }

    /**
     * Returns the type URI.<p>
     * 
     * @param xmlContentDefinition the type content definition
     * 
     * @return the type URI
     */
    public static String getTypeUri(CmsXmlContentDefinition xmlContentDefinition) {

        return xmlContentDefinition.getSchemaLocation() + "/" + xmlContentDefinition.getTypeName();
    }

    /**
     * Fetches the initial content definition.<p>
     * 
     * @param request the current request
     * 
     * @return the initial content definition
     * 
     * @throws CmsRpcException if something goes wrong
     */
    public static CmsContentDefinition prefetch(HttpServletRequest request) throws CmsRpcException {

        CmsContentService srv = new CmsContentService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsContentDefinition result = null;
        try {
            result = srv.prefetch();
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * @see com.alkacon.acacia.shared.rpc.I_ContentService#loadContentDefinition(java.lang.String)
     */
    public ContentDefinition loadContentDefinition(String entityId) throws CmsRpcException {

        return loadDefinition(entityId);
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadDefinition(java.lang.String)
     */
    public CmsContentDefinition loadDefinition(String entityId) throws CmsRpcException {

        CmsContentDefinition definition = null;
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            definition = readContentDefinition(
                resource,
                CmsContentDefinition.uuidToEntityId(structureId, contentLocale.toString()),
                contentLocale,
                false);
        } catch (Exception e) {
            error(e);
        }
        return definition;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadDefinition(java.lang.String, java.lang.String, org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsContentDefinition loadDefinition(String entityId, String newLink, CmsUUID modelFileId, String editContext)
    throws CmsRpcException {

        CmsContentDefinition result = null;
        getCmsObject().getRequestContext().setAttribute(CmsXmlContentEditor.ATTRIBUTE_EDITCONTEXT, editContext);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(newLink)) {
            try {
                CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
                CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
                result = readContentDefnitionForNew(newLink, resource, modelFileId, contentLocale);
            } catch (Throwable t) {
                error(t);
            }
        } else {
            result = loadDefinition(entityId);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadNewDefinition(java.lang.String)
     */
    public CmsContentDefinition loadNewDefinition(String entityId) throws CmsRpcException {

        CmsContentDefinition definition = null;
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            definition = readContentDefinition(
                resource,
                CmsContentDefinition.uuidToEntityId(structureId, contentLocale.toString()),
                contentLocale,
                true);
        } catch (Exception e) {
            error(e);
        }
        return definition;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#prefetch()
     */
    public CmsContentDefinition prefetch() throws CmsRpcException {

        String paramResource = getRequest().getParameter(CmsDialog.PARAM_RESOURCE);
        String paramNewLink = getRequest().getParameter(CmsXmlContentEditor.PARAM_NEWLINK);
        boolean createNew = false;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramNewLink)) {
            createNew = true;
            paramNewLink = decodeNewLink(paramNewLink);
        }
        String paramLocale = getRequest().getParameter(CmsEditor.PARAM_ELEMENTLANGUAGE);
        Locale locale = null;
        CmsObject cms = getCmsObject();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramResource)) {
            try {
                CmsResource resource = cms.readResource(paramResource, CmsResourceFilter.IGNORE_EXPIRATION);
                if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramLocale)) {
                        locale = CmsLocaleManager.getLocale(paramLocale);
                    }

                    if (createNew) {
                        if (locale == null) {
                            locale = OpenCms.getLocaleManager().getDefaultLocale(cms, paramResource);
                        }
                        CmsUUID modelFileId = null;
                        String paramModelFile = getRequest().getParameter(CmsNewResourceXmlContent.PARAM_MODELFILE);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramModelFile)) {
                            modelFileId = cms.readResource(paramModelFile).getStructureId();
                        }
                        return readContentDefnitionForNew(paramNewLink, resource, modelFileId, locale);
                    } else {

                        CmsFile file = cms.readFile(resource);
                        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                        if (locale == null) {
                            locale = getBestAvailableLocale(resource, content);
                        }
                        return readContentDefinition(file, content, null, locale, false);
                    }
                }
            } catch (Throwable e) {
                error(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#saveAndDeleteEntities(java.util.List, java.util.List, boolean)
     */
    public ValidationResult saveAndDeleteEntities(
        List<Entity> changedEntities,
        List<String> deletedEntities,
        boolean clearOnSuccess) throws CmsRpcException {

        CmsUUID structureId = null;
        if (!changedEntities.isEmpty()) {
            structureId = CmsContentDefinition.entityIdToUuid(changedEntities.get(0).getId());
        }
        if ((structureId == null) && !deletedEntities.isEmpty()) {
            structureId = CmsContentDefinition.entityIdToUuid(deletedEntities.get(0));
        }
        if (structureId != null) {
            CmsObject cms = getCmsObject();
            CmsResource resource = null;
            try {
                resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                ensureLock(resource);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                checkAutoCorrection(cms, content);
                for (Entity entity : changedEntities) {
                    String entityId = entity.getId();
                    Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
                    if (content.hasLocale(contentLocale)) {
                        content.removeLocale(contentLocale);
                    }
                    content.addLocale(cms, contentLocale);
                    addEntityAttributes(cms, content, "", entity, contentLocale);
                }
                for (String deleteId : deletedEntities) {
                    Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(deleteId));
                    if (content.hasLocale(contentLocale)) {
                        content.removeLocale(contentLocale);
                    }
                }
                ValidationResult validationResult = validateContent(cms, structureId, content);
                if (validationResult.hasErrors()) {
                    return validationResult;
                }
                writeContent(cms, file, content, getFileEncoding(cms, file));
                // update offline indices
                OpenCms.getSearchManager().updateOfflineIndexes(2 * CmsSearchManager.DEFAULT_OFFLINE_UPDATE_FREQNENCY);
                if (clearOnSuccess) {
                    tryUnlock(resource);
                }
            } catch (Exception e) {
                if (resource != null) {
                    tryUnlock(resource);
                }
                error(e);
            }
        }
        return null;
    }

    /**
     * @see com.alkacon.acacia.shared.rpc.I_ContentService#saveEntities(java.util.List)
     */
    public ValidationResult saveEntities(List<Entity> entities) throws CmsRpcException {

        return saveAndDeleteEntities(entities, Collections.<String> emptyList(), true);
    }

    /**
     * @see com.alkacon.acacia.shared.rpc.I_ContentService#saveEntity(com.alkacon.acacia.shared.Entity)
     */
    public ValidationResult saveEntity(Entity entity) throws CmsRpcException {

        return saveEntities(Collections.singletonList(entity));
    }

    /**
     * @see com.alkacon.acacia.shared.rpc.I_ContentService#validateEntities(java.util.List)
     */
    public ValidationResult validateEntities(List<Entity> changedEntities) throws CmsRpcException {

        CmsUUID structureId = null;
        if (changedEntities.isEmpty()) {
            return new ValidationResult(null, null);
        }
        structureId = CmsContentDefinition.entityIdToUuid(changedEntities.get(0).getId());
        if (structureId != null) {
            CmsObject cms = getCmsObject();
            try {
                CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                for (Entity entity : changedEntities) {
                    String entityId = entity.getId();
                    Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
                    if (content.hasLocale(contentLocale)) {
                        content.removeLocale(contentLocale);
                    }
                    content.addLocale(cms, contentLocale);
                    addEntityAttributes(cms, content, "", entity, contentLocale);
                }
                return validateContent(cms, structureId, content);
            } catch (Exception e) {
                error(e);
            }
        }
        return new ValidationResult(null, null);
    }

    /**
     * Decodes the newlink request parameter if possible.<p>
     * 
     * @param newLink the parameter to decode 
     * 
     * @return the decoded value 
     */
    protected String decodeNewLink(String newLink) {

        String result = newLink;
        if (result == null) {
            return null;
        }
        try {
            result = CmsEncoder.decode(result);
            try {
                result = CmsEncoder.decode(result);
            } catch (Throwable e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        } catch (Throwable e) {
            LOG.info(e.getLocalizedMessage(), e);
        }

        return result;
    }

    /**
     * Returns the element name to the given element.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the element name
     */
    protected String getElementName(String attributeName) {

        if (attributeName.contains("/")) {
            return attributeName.substring(attributeName.lastIndexOf("/") + 1);
        }
        return attributeName;
    }

    /**
     * Helper method to determine the encoding of the given file in the VFS,
     * which must be set using the "content-encoding" property.<p>
     * 
     * @param cms the CmsObject
     * @param file the file which is to be checked
     * @return the encoding for the file
     */
    protected String getFileEncoding(CmsObject cms, CmsResource file) {

        String result;
        try {
            result = cms.readPropertyObject(file, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            result = OpenCms.getSystemInfo().getDefaultEncoding();
        }
        return CmsEncoder.lookupEncoding(result, OpenCms.getSystemInfo().getDefaultEncoding());
    }

    /**
     * Parses the element into an entity.<p>
     * 
     * @param content the entity content
     * @param element the current element
     * @param locale the content locale
     * @param entityId the entity id
     * @param parentPath the parent path
     * @param typeName the entity type name
     * @param registeredTypes the types used within the entity
     * 
     * @return the entity
     */
    protected Entity readEntity(
        CmsXmlContent content,
        Element element,
        Locale locale,
        String entityId,
        String parentPath,
        String typeName,
        Map<String, I_Type> registeredTypes) {

        String newEntityId = entityId + (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parentPath) ? "/" + parentPath : "");
        Entity newEntity = new Entity(newEntityId, typeName);
        Entity result = newEntity;

        @SuppressWarnings("unchecked")
        List<Element> elements = element.elements();
        I_Type type = registeredTypes.get(typeName);
        boolean isChoice = type.isChoice();
        String choiceTypeName = null;
        // just needed for choice attributes
        Map<String, Integer> attributeCounter = null;
        if (isChoice) {
            choiceTypeName = type.getAttributeTypeName(Type.CHOICE_ATTRIBUTE_NAME);
            type = registeredTypes.get(type.getAttributeTypeName(Type.CHOICE_ATTRIBUTE_NAME));
            attributeCounter = new HashMap<String, Integer>();
        }
        int counter = 0;
        CmsObject cms = getCmsObject();
        String previousName = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parentPath)) {
            parentPath += "/";
        }
        for (Element child : elements) {
            String attributeName = getAttributeName(child.getName(), typeName);
            String subTypeName = type.getAttributeTypeName(attributeName);
            if (registeredTypes.get(subTypeName) == null) {
                // in case there is no type configured for this element, the schema may have changed, skip the element
                continue;
            }
            if (isChoice && (attributeCounter != null)) {
                if (!attributeName.equals(previousName)) {
                    if (attributeCounter.get(attributeName) != null) {
                        counter = attributeCounter.get(attributeName).intValue();
                    } else {
                        counter = 0;
                    }
                    previousName = attributeName;
                }
                attributeCounter.put(attributeName, Integer.valueOf(counter + 1));
            } else if (!attributeName.equals(previousName)) {

                // reset the attribute counter for every attribute name
                counter = 0;

                previousName = attributeName;
            }
            if (isChoice) {
                result = new Entity(newEntityId
                    + "/"
                    + Type.CHOICE_ATTRIBUTE_NAME
                    + "_"
                    + child.getName()
                    + "["
                    + counter
                    + "]", choiceTypeName);
                newEntity.addAttributeValue(Type.CHOICE_ATTRIBUTE_NAME, result);
            }
            String path = parentPath + child.getName();
            if (registeredTypes.get(subTypeName).isSimpleType()) {
                I_CmsXmlContentValue value = content.getValue(path, locale, counter);
                result.addAttributeValue(attributeName, value.getStringValue(cms));
            } else {
                Entity subEntity = readEntity(
                    content,
                    child,
                    locale,
                    entityId,
                    path + "[" + (counter + 1) + "]",
                    subTypeName,
                    registeredTypes);
                result.addAttributeValue(attributeName, subEntity);

            }
            counter++;
        }
        return newEntity;
    }

    /**
     * Reads the types from the given content definition and adds the to the map of already registered
     * types if necessary.<p>
     * 
     * @param xmlContentDefinition the XML content definition
     * @param locale the messages locale
     * 
     * @return the types of the given content definition 
     */
    protected Map<String, I_Type> readTypes(CmsXmlContentDefinition xmlContentDefinition, Locale locale) {

        CmsContentTypeVisitor visitor = new CmsContentTypeVisitor(getCmsObject(), null, locale);
        visitor.visitTypes(xmlContentDefinition, locale);
        return visitor.getTypes();
    }

    /**
     * Adds the attribute values of the entity to the given XML content.<p>
     * 
     * @param cms the current cms context
     * @param content the XML content
     * @param parentPath the parent path
     * @param entity the entity
     * @param contentLocale the content locale
     */
    private void addEntityAttributes(
        CmsObject cms,
        CmsXmlContent content,
        String parentPath,
        I_Entity entity,
        Locale contentLocale) {

        for (I_EntityAttribute attribute : entity.getAttributes()) {
            if (Type.CHOICE_ATTRIBUTE_NAME.equals(attribute.getAttributeName())) {
                List<I_Entity> choiceEntities = attribute.getComplexValues();
                for (int i = 0; i < choiceEntities.size(); i++) {
                    List<I_EntityAttribute> choiceAttributes = choiceEntities.get(i).getAttributes();
                    // each choice entity may only have a single attribute with a single value
                    assert (choiceAttributes.size() == 1) && choiceAttributes.get(0).isSingleValue() : "each choice entity may only have a single attribute with a single value";
                    I_EntityAttribute choiceAttribute = choiceAttributes.get(0);
                    String elementPath = parentPath + getElementName(choiceAttribute.getAttributeName());
                    if (choiceAttribute.isSimpleValue()) {
                        String value = choiceAttribute.getSimpleValue();
                        I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                        if (field == null) {
                            field = content.addValue(cms, elementPath, contentLocale, i);
                        }
                        field.setStringValue(cms, value);
                    } else {
                        I_Entity child = choiceAttribute.getComplexValue();
                        I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                        if (field == null) {
                            field = content.addValue(cms, elementPath, contentLocale, i);
                        }
                        addEntityAttributes(cms, content, field.getPath() + "/", child, contentLocale);
                    }
                }
            } else {
                String elementPath = parentPath + getElementName(attribute.getAttributeName());
                if (attribute.isSimpleValue()) {
                    List<String> values = attribute.getSimpleValues();
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                        if (field == null) {
                            field = content.addValue(cms, elementPath, contentLocale, i);
                        }
                        field.setStringValue(cms, value);

                    }
                } else {
                    List<I_Entity> entities = attribute.getComplexValues();
                    for (int i = 0; i < entities.size(); i++) {
                        I_Entity child = entities.get(i);
                        I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                        if (field == null) {
                            field = content.addValue(cms, elementPath, contentLocale, i);
                        }
                        addEntityAttributes(cms, content, field.getPath() + "/", child, contentLocale);
                    }
                }
            }
        }
    }

    /**
     * Check if automatic content correction is required. Returns <code>true</code> if the content was changed.<p>
     * 
     * @param cms the cms context
     * @param content the content to check
     * 
     * @return <code>true</code> if the content was changed
     * @throws CmsXmlException if the automatic content correction failed
     */
    private boolean checkAutoCorrection(CmsObject cms, CmsXmlContent content) throws CmsXmlException {

        boolean performedAutoCorrection = false;
        try {
            content.validateXmlStructure(new CmsXmlEntityResolver(cms));
        } catch (CmsXmlException eXml) {
            // validation failed
            content.setAutoCorrectionEnabled(true);
            content.correctXmlStructure(cms);
            performedAutoCorrection = true;
        }
        return performedAutoCorrection;
    }

    /**
     * Returns the best available locale present in the given XML content, or the default locale.<p>
     * 
     * @param resource the resource
     * @param content the XML content
     * 
     * @return the locale
     */
    private Locale getBestAvailableLocale(CmsResource resource, CmsXmlContent content) {

        CmsObject cms = getCmsObject();
        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(getCmsObject(), resource);
        if (!content.hasLocale(locale)) {
            // if the requested locale is not available, get the first matching default locale,
            // or the first matching available locale
            boolean foundLocale = false;
            if (content.getLocales().size() > 0) {
                List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
                for (Locale defaultLocale : locales) {
                    if (content.hasLocale(defaultLocale)) {
                        locale = defaultLocale;
                        foundLocale = true;
                        break;
                    }
                }
                if (!foundLocale) {
                    locales = OpenCms.getLocaleManager().getAvailableLocales(cms, resource);
                    for (Locale availableLocale : locales) {
                        if (content.hasLocale(availableLocale)) {
                            locale = availableLocale;
                            foundLocale = true;
                            break;
                        }
                    }
                }
            }
        }
        return locale;
    }

    /**
     * Returns the path elements for the given content value.<p>
     * 
     * @param content the XML content
     * @param value the content value
     * 
     * @return the path elements
     */
    private String[] getPathElements(CmsXmlContent content, I_CmsXmlContentValue value) {

        List<String> pathElements = new ArrayList<String>();
        String[] paths = value.getPath().split("/");
        String path = "";
        for (int i = 0; i < paths.length; i++) {
            path += paths[i];
            I_CmsXmlContentValue ancestor = content.getValue(path, value.getLocale());
            int valueIndex = ancestor.getXmlIndex();
            if (ancestor.isChoiceOption()) {
                Element parent = ancestor.getElement().getParent();
                valueIndex = parent.indexOf(ancestor.getElement());
            }
            String pathElement = getAttributeName(ancestor.getName(), getTypeUri(ancestor.getContentDefinition()));
            pathElements.add(pathElement + "[" + valueIndex + "]");
            path += "/";
        }
        return pathElements.toArray(new String[pathElements.size()]);
    }

    /**
     * Returns the workplace locale.<p>
     * 
     * @param cms the current OpenCms context 
     * 
     * @return the current users workplace locale
     */
    private Locale getWorkplaceLocale(CmsObject cms) {

        if (m_workplaceLocale == null) {
            m_workplaceLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        }
        return m_workplaceLocale;
    }

    /**
     * Reads the content definition for the given resource and locale.<p>
     * 
     * @param file the resource file
     * @param content the XML content
     * @param entityId the entity id
     * @param locale the content locale
     * @param newLocale if the locale content should be created as new
     * 
     * @return the content definition
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsContentDefinition readContentDefinition(
        CmsFile file,
        CmsXmlContent content,
        String entityId,
        Locale locale,
        boolean newLocale) throws CmsException {

        long timer = 0;
        if (LOG.isDebugEnabled()) {
            timer = System.currentTimeMillis();
        }
        CmsObject cms = getCmsObject();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(entityId)) {
            entityId = CmsContentDefinition.uuidToEntityId(file.getStructureId(), locale.toString());
        }
        boolean performedAutoCorrection = checkAutoCorrection(cms, content);
        if (performedAutoCorrection) {
            content.initDocument();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_TAKE_UNMARSHALING_TIME_1,
                "" + (System.currentTimeMillis() - timer)));
        }
        CmsContentTypeVisitor visitor = new CmsContentTypeVisitor(cms, file, locale);
        if (LOG.isDebugEnabled()) {
            timer = System.currentTimeMillis();
        }
        visitor.visitTypes(content.getContentDefinition(), getWorkplaceLocale(cms));
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_TAKE_VISITING_TYPES_TIME_1,
                "" + (System.currentTimeMillis() - timer)));
        }
        Entity entity = null;
        if (content.hasLocale(locale) && newLocale) {
            // a new locale is requested, so remove the present one
            content.removeLocale(locale);
        }
        if (!content.hasLocale(locale)) {
            content.addLocale(cms, locale);
        }
        Element element = content.getLocaleNode(locale);
        if (LOG.isDebugEnabled()) {
            timer = System.currentTimeMillis();
        }
        entity = readEntity(
            content,
            element,
            locale,
            entityId,
            "",
            getTypeUri(content.getContentDefinition()),
            visitor.getTypes());
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_TAKE_READING_ENTITY_TIME_1,
                "" + (System.currentTimeMillis() - timer)));
        }
        List<String> contentLocales = new ArrayList<String>();
        for (Locale contentLocale : content.getLocales()) {
            contentLocales.add(contentLocale.toString());
        }
        Locale workplaceLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        TreeMap<String, String> availableLocales = new TreeMap<String, String>();
        for (Locale availableLocale : OpenCms.getLocaleManager().getAvailableLocales(cms, file)) {
            availableLocales.put(availableLocale.toString(), availableLocale.getDisplayName(workplaceLocale));
        }
        String title = cms.readPropertyObject(file, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        try {
            CmsGallerySearchResult searchResult = CmsGallerySearch.searchById(cms, file.getStructureId(), locale);
            title = searchResult.getTitle();
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        String typeName = OpenCms.getResourceManager().getResourceType(file.getTypeId()).getTypeName();
        return new CmsContentDefinition(
            entity,
            visitor.getAttributeConfigurations(),
            visitor.getWidgetConfigurations(),
            visitor.getComplexWidgetData(),
            visitor.getTypes(),
            visitor.getTabInfos(),
            locale.toString(),
            contentLocales,
            availableLocales,
            title,
            cms.getSitePath(file),
            typeName,
            performedAutoCorrection);
    }

    /**
     * Reads the content definition for the given resource and locale.<p>
     * 
     * @param resource the resource
     * @param entityId the entity id
     * @param locale the content locale
     * @param newLocale if the locale content should be created as new
     * 
     * @return the content definition
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsContentDefinition readContentDefinition(
        CmsResource resource,
        String entityId,
        Locale locale,
        boolean newLocale) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsFile file = cms.readFile(resource);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
        return readContentDefinition(file, content, entityId, locale, newLocale);
    }

    /**
     * Creates a new resource according to the new link, or returns the model file informations
     * modelFileId is <code>null</code> but required.<p>
     * 
     * @param newLink the new link
     * @param referenceResource the reference resource
     * @param modelFileId the model file structure id
     * @param locale the content locale
     * 
     * @return the content definition
     * 
     * @throws CmsException if creating the resource failed
     */
    private CmsContentDefinition readContentDefnitionForNew(
        String newLink,
        CmsResource referenceResource,
        CmsUUID modelFileId,
        Locale locale) throws CmsException {

        String sitePath = getCmsObject().getSitePath(referenceResource);
        String resourceType = OpenCms.getResourceManager().getResourceType(referenceResource.getTypeId()).getTypeName();
        String modelFile = null;
        if (modelFileId == null) {
            List<CmsResource> modelResources = CmsNewResourceXmlContent.getModelFiles(
                getCmsObject(),
                CmsResource.getFolderPath(sitePath),
                resourceType);
            if (!modelResources.isEmpty()) {
                List<CmsModelResourceInfo> modelInfos = CmsContainerpageService.generateModelResourceList(
                    getCmsObject(),
                    resourceType,
                    modelResources,
                    locale);
                return new CmsContentDefinition(
                    modelInfos,
                    newLink,
                    referenceResource.getStructureId(),
                    locale.toString());
            }
        } else if (!modelFileId.isNullUUID()) {
            modelFile = getCmsObject().getSitePath(
                getCmsObject().readResource(modelFileId, CmsResourceFilter.IGNORE_EXPIRATION));
        }
        String newFileName = A_CmsResourceCollector.createResourceForCollector(
            getCmsObject(),
            newLink,
            locale,
            sitePath,
            modelFile);
        CmsResource resource = getCmsObject().readResource(newFileName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsContentDefinition contentDefinition = readContentDefinition(resource, null, locale, false);
        contentDefinition.setDeleteOnCancel(true);
        return contentDefinition;
    }

    /**
     * Validates the given XML content.<p>
     *  
     * @param cms the cms context
     * @param structureId the structure id
     * @param content the XML content
     * 
     * @return the validation result
     */
    private ValidationResult validateContent(CmsObject cms, CmsUUID structureId, CmsXmlContent content) {

        CmsXmlContentErrorHandler errorHandler = content.validate(cms);
        Map<String, Map<String[], String>> errorsByEntity = new HashMap<String, Map<String[], String>>();
        if (errorHandler.hasErrors()) {

            for (Entry<Locale, Map<String, String>> localeEntry : errorHandler.getErrors().entrySet()) {
                Map<String[], String> errors = new HashMap<String[], String>();
                for (Entry<String, String> error : localeEntry.getValue().entrySet()) {
                    I_CmsXmlContentValue value = content.getValue(error.getKey(), localeEntry.getKey());
                    errors.put(getPathElements(content, value), error.getValue());
                }
                errorsByEntity.put(
                    CmsContentDefinition.uuidToEntityId(structureId, localeEntry.getKey().toString()),
                    errors);
            }
        }
        Map<String, Map<String[], String>> warningsByEntity = new HashMap<String, Map<String[], String>>();
        if (errorHandler.hasWarnings()) {
            for (Entry<Locale, Map<String, String>> localeEntry : errorHandler.getWarnings().entrySet()) {
                Map<String[], String> warnings = new HashMap<String[], String>();
                for (Entry<String, String> warning : localeEntry.getValue().entrySet()) {
                    I_CmsXmlContentValue value = content.getValue(warning.getKey(), localeEntry.getKey());
                    warnings.put(getPathElements(content, value), warning.getValue());
                }
                warningsByEntity.put(
                    CmsContentDefinition.uuidToEntityId(structureId, localeEntry.getKey().toString()),
                    warnings);
            }
        }
        return new ValidationResult(errorsByEntity, warningsByEntity);
    }

    /**
     * Writes the xml content to the vfs and re-initializes the member variables.<p>
     * 
     * @param cms the cms context
     * @param file the file to write to
     * @param content the content
     * @param encoding the file encoding
     * 
     * @return the content 
     * 
     * @throws CmsException if writing the file fails
     */
    private CmsXmlContent writeContent(CmsObject cms, CmsFile file, CmsXmlContent content, String encoding)
    throws CmsException {

        String decodedContent = content.toString();
        try {
            file.setContents(decodedContent.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new CmsException(org.opencms.workplace.editors.Messages.get().container(
                org.opencms.workplace.editors.Messages.ERR_INVALID_CONTENT_ENC_1,
                file.getRootPath()), e);
        }
        // the file content might have been modified during the write operation    
        file = cms.writeFile(file);
        return CmsXmlContentFactory.unmarshal(cms, file);
    }
}
