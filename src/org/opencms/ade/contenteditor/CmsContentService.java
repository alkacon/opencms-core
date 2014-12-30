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

import org.opencms.acacia.shared.CmsAttributeConfiguration;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsEntityHtml;
import org.opencms.acacia.shared.CmsType;
import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.CmsElementUtil;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
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
import org.opencms.json.JSONObject;
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
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * Service to provide entity persistence within OpenCms. <p>
 */
public class CmsContentService extends CmsGwtService implements I_CmsContentService {

    /** The logger for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsContentService.class);

    /** The type name prefix. */
    static final String TYPE_NAME_PREFIX = "http://opencms.org/types/";

    /** The serial version id. */
    private static final long serialVersionUID = 7873052619331296648L;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

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
                + CmsType.CHOICE_ATTRIBUTE_NAME
                + "_"
                + contentValue.getName()
                + "["
                + contentValue.getXmlIndex()
                + "]";
        }
        return result;
    }

    /**
     * Returns the RDF annotations required for in line editing.<p>
     *
     * @param parentValue the parent XML content value
     * @param childNames the child attribute names separated by '|'
     *
     * @return the RDFA
     */
    public static String getRdfaAttributes(I_CmsXmlContentValue parentValue, String childNames) {

        StringBuffer result = new StringBuffer();
        result.append("about=\"");
        result.append(CmsContentDefinition.uuidToEntityId(
            parentValue.getDocument().getFile().getStructureId(),
            parentValue.getLocale().toString()));
        result.append("/").append(parentValue.getPath());
        result.append("\" ");
        String[] children = childNames.split("\\|");
        result.append("property=\"");
        for (int i = 0; i < children.length; i++) {
            I_CmsXmlSchemaType schemaType = parentValue.getContentDefinition().getSchemaType(
                parentValue.getName() + "/" + children[i]);
            if (schemaType != null) {
                if (i > 0) {
                    result.append(" ");
                }
                result.append(getTypeUri(schemaType.getContentDefinition())).append("/").append(children[i]);
            }
        }
        result.append("\"");
        return result.toString();
    }

    /**
     * Returns the RDF annotations required for in line editing.<p>
     *
     * @param document the parent XML document
     * @param contentLocale the content locale
     * @param childName the child attribute name
     *
     * @return the RDFA
     */
    public static String getRdfaAttributes(I_CmsXmlDocument document, Locale contentLocale, String childName) {

        I_CmsXmlSchemaType schemaType = document.getContentDefinition().getSchemaType(childName);
        StringBuffer result = new StringBuffer();
        if (schemaType != null) {
            result.append("about=\"");
            result.append(CmsContentDefinition.uuidToEntityId(
                document.getFile().getStructureId(),
                contentLocale.toString()));
            result.append("\" property=\"");
            result.append(getTypeUri(schemaType.getContentDefinition())).append("/").append(childName);
            result.append("\"");
        }
        return result.toString();
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
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#callEditorChangeHandlers(java.lang.String, org.opencms.acacia.shared.CmsEntity, java.util.Collection, java.util.Collection)
     */
    public CmsContentDefinition callEditorChangeHandlers(
        String entityId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Collection<String> changedScopes) throws CmsRpcException {

        CmsContentDefinition result = null;
        CmsUUID structureId = CmsContentDefinition.entityIdToUuid(editedLocaleEntity.getId());

        if (structureId != null) {
            CmsObject cms = getCmsObject();
            CmsResource resource = null;
            Locale locale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            try {
                resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                ensureLock(resource);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = getContentDocument(file, true).clone();
                checkAutoCorrection(cms, content);
                synchronizeLocaleIndependentForEntity(file, content, skipPaths, editedLocaleEntity);
                for (I_CmsXmlContentEditorChangeHandler handler : content.getContentDefinition().getContentHandler().getEditorChangeHandlers()) {
                    Set<String> handlerScopes = evaluateScope(handler.getScope(), content.getContentDefinition());
                    if (!Collections.disjoint(changedScopes, handlerScopes)) {
                        handler.handleChange(cms, content, locale, changedScopes);
                    }
                }
                result = readContentDefinition(file, content, entityId, locale, false);
            } catch (Exception e) {
                error(e);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#cancelEdit(org.opencms.util.CmsUUID, boolean)
     */
    public void cancelEdit(CmsUUID structureId, boolean delete) throws CmsRpcException {

        try {
            getSessionCache().uncacheXmlContent(structureId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            if (delete) {
                ensureLock(resource);
                getCmsObject().deleteResource(
                    getCmsObject().getSitePath(resource),
                    CmsResource.DELETE_PRESERVE_SIBLINGS);
            }
            tryUnlock(resource);
        } catch (Throwable t) {
            error(t);
        }
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#copyLocale(java.util.Collection, org.opencms.acacia.shared.CmsEntity)
     */
    public void copyLocale(Collection<String> locales, CmsEntity sourceLocale) throws CmsRpcException {

        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(sourceLocale.getId());

            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsFile file = getCmsObject().readFile(resource);
            CmsXmlContent content = getSessionCache().getCacheXmlContent(structureId);
            synchronizeLocaleIndependentForEntity(file, content, Collections.<String> emptyList(), sourceLocale);
            Locale sourceContentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(sourceLocale.getId()));
            for (String loc : locales) {
                Locale targetLocale = CmsLocaleManager.getLocale(loc);
                if (content.hasLocale(targetLocale)) {
                    content.removeLocale(targetLocale);
                }
                content.copyLocale(sourceContentLocale, targetLocale);
            }
        } catch (Throwable t) {
            error(t);
        }
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#loadContentDefinition(java.lang.String)
     */
    public CmsContentDefinition loadContentDefinition(String entityId) throws CmsRpcException {

        throw new CmsRpcException(new UnsupportedOperationException());
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadDefinition(java.lang.String, org.opencms.acacia.shared.CmsEntity, java.util.Collection)
     */
    public CmsContentDefinition loadDefinition(
        String entityId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths) throws CmsRpcException {

        CmsContentDefinition definition = null;
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            CmsFile file = getCmsObject().readFile(resource);
            CmsXmlContent content = getContentDocument(file, true);
            if (editedLocaleEntity != null) {
                synchronizeLocaleIndependentForEntity(file, content, skipPaths, editedLocaleEntity);
            }
            definition = readContentDefinition(
                file,
                content,
                CmsContentDefinition.uuidToEntityId(structureId, contentLocale.toString()),
                contentLocale,
                false);
        } catch (Exception e) {
            error(e);
        }
        return definition;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadInitialDefinition(java.lang.String, java.lang.String, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String)
     */
    public CmsContentDefinition loadInitialDefinition(
        String entityId,
        String newLink,
        CmsUUID modelFileId,
        String editContext,
        String mode,
        String postCreateHandler) throws CmsRpcException {

        CmsContentDefinition result = null;
        getCmsObject().getRequestContext().setAttribute(CmsXmlContentEditor.ATTRIBUTE_EDITCONTEXT, editContext);
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(newLink)) {
                result = readContentDefnitionForNew(
                    newLink,
                    resource,
                    modelFileId,
                    contentLocale,
                    mode,
                    postCreateHandler);
            } else {
                CmsFile file = getCmsObject().readFile(resource);
                CmsXmlContent content = getContentDocument(file, false);
                result = readContentDefinition(
                    file,
                    content,
                    CmsContentDefinition.uuidToEntityId(structureId, contentLocale.toString()),
                    contentLocale,
                    false);
            }
        } catch (Throwable t) {
            error(t);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadNewDefinition(java.lang.String, org.opencms.acacia.shared.CmsEntity, java.util.Collection)
     */
    public CmsContentDefinition loadNewDefinition(
        String entityId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths) throws CmsRpcException {

        CmsContentDefinition definition = null;
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            CmsFile file = getCmsObject().readFile(resource);
            CmsXmlContent content = getContentDocument(file, true);
            synchronizeLocaleIndependentForEntity(file, content, skipPaths, editedLocaleEntity);
            definition = readContentDefinition(
                file,
                content,
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
        String paramDirectEdit = getRequest().getParameter(CmsEditor.PARAM_DIRECTEDIT);
        boolean isDirectEdit = false;
        if (paramDirectEdit != null) {
            isDirectEdit = Boolean.parseBoolean(paramDirectEdit);
        }
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
                    CmsContentDefinition result;
                    if (createNew) {
                        if (locale == null) {
                            locale = OpenCms.getLocaleManager().getDefaultLocale(cms, paramResource);
                        }
                        CmsUUID modelFileId = null;
                        String paramModelFile = getRequest().getParameter(CmsNewResourceXmlContent.PARAM_MODELFILE);

                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramModelFile)) {
                            modelFileId = cms.readResource(paramModelFile).getStructureId();
                        }

                        String mode = getRequest().getParameter(CmsEditorConstants.PARAM_MODE);
                        String postCreateHandler = getRequest().getParameter(
                            CmsEditorConstants.PARAM_POST_CREATE_HANDLER);
                        result = readContentDefnitionForNew(
                            paramNewLink,
                            resource,
                            modelFileId,
                            locale,
                            mode,
                            postCreateHandler);
                    } else {

                        CmsFile file = cms.readFile(resource);
                        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                        getSessionCache().setCacheXmlContent(resource.getStructureId(), content);
                        if (locale == null) {
                            locale = getBestAvailableLocale(resource, content);
                        }
                        result = readContentDefinition(file, content, null, locale, false);
                    }
                    result.setDirectEdit(isDirectEdit);
                    return result;
                }
            } catch (Throwable e) {
                error(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#saveAndDeleteEntities(org.opencms.acacia.shared.CmsEntity, java.util.List, java.util.Collection, java.lang.String, boolean)
     */
    public CmsValidationResult saveAndDeleteEntities(
        CmsEntity lastEditedEntity,
        List<String> deletedEntities,
        Collection<String> skipPaths,
        String lastEditedLocale,
        boolean clearOnSuccess) throws CmsRpcException {

        CmsUUID structureId = null;
        if (lastEditedEntity != null) {
            structureId = CmsContentDefinition.entityIdToUuid(lastEditedEntity.getId());
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
                CmsXmlContent content = getContentDocument(file, true);
                checkAutoCorrection(cms, content);
                if (lastEditedEntity != null) {
                    synchronizeLocaleIndependentForEntity(file, content, skipPaths, lastEditedEntity);
                }
                for (String deleteId : deletedEntities) {
                    Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(deleteId));
                    if (content.hasLocale(contentLocale)) {
                        content.removeLocale(contentLocale);
                    }
                }
                CmsValidationResult validationResult = validateContent(cms, structureId, content);
                if (validationResult.hasErrors()) {
                    return validationResult;
                }
                writeContent(cms, file, content, getFileEncoding(cms, file));
                // update offline indices
                OpenCms.getSearchManager().updateOfflineIndexes(2 * CmsSearchManager.DEFAULT_OFFLINE_UPDATE_FREQNENCY);
                if (clearOnSuccess) {
                    tryUnlock(resource);
                    getSessionCache().uncacheXmlContent(structureId);
                }
            } catch (Exception e) {
                if (resource != null) {
                    tryUnlock(resource);
                    getSessionCache().uncacheXmlContent(structureId);
                }
                error(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#saveEntities(java.util.List)
     */
    public CmsValidationResult saveEntities(List<CmsEntity> entities) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#saveEntity(org.opencms.acacia.shared.CmsEntity)
     */
    public CmsValidationResult saveEntity(CmsEntity entity) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#updateEntityHtml(org.opencms.acacia.shared.CmsEntity, java.lang.String, java.lang.String)
     */
    public CmsEntityHtml updateEntityHtml(CmsEntity entity, String contextUri, String htmlContextInfo) throws Exception {

        CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entity.getId());
        if (structureId != null) {
            CmsObject cms = getCmsObject();
            try {
                CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                String entityId = entity.getId();
                Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
                if (content.hasLocale(contentLocale)) {
                    content.removeLocale(contentLocale);
                }
                content.addLocale(cms, contentLocale);
                addEntityAttributes(cms, content, "", entity, contentLocale);
                CmsValidationResult validationResult = validateContent(cms, structureId, content);
                String htmlContent = null;
                if (!validationResult.hasErrors()) {
                    file.setContents(content.marshal());

                    JSONObject contextInfo = new JSONObject(htmlContextInfo);
                    String containerName = contextInfo.getString(CmsCntPageData.JSONKEY_NAME);
                    String containerType = contextInfo.getString(CmsCntPageData.JSONKEY_TYPE);
                    int containerWidth = contextInfo.getInt(CmsCntPageData.JSONKEY_WIDTH);
                    int maxElements = contextInfo.getInt(CmsCntPageData.JSONKEY_MAXELEMENTS);
                    boolean detailView = contextInfo.getBoolean(CmsCntPageData.JSONKEY_DETAILVIEW);
                    CmsContainer container = new CmsContainer(
                        containerName,
                        containerType,
                        null,
                        containerWidth,
                        maxElements,
                        detailView,
                        true,
                        Collections.<CmsContainerElement> emptyList(),
                        null,
                        null);
                    CmsUUID detailContentId = null;
                    if (contextInfo.has(CmsCntPageData.JSONKEY_DETAIL_ELEMENT_ID)) {
                        detailContentId = new CmsUUID(contextInfo.getString(CmsCntPageData.JSONKEY_DETAIL_ELEMENT_ID));
                    }
                    CmsElementUtil elementUtil = new CmsElementUtil(
                        cms,
                        contextUri,
                        detailContentId,
                        getThreadLocalRequest(),
                        getThreadLocalResponse(),
                        contentLocale);
                    htmlContent = elementUtil.getContentByContainer(
                        file,
                        contextInfo.getString(CmsCntPageData.JSONKEY_ELEMENT_ID),
                        container,
                        true);
                }
                return new CmsEntityHtml(htmlContent, validationResult);

            } catch (Exception e) {
                error(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#validateEntities(java.util.List)
     */
    public CmsValidationResult validateEntities(List<CmsEntity> changedEntities) throws CmsRpcException {

        CmsUUID structureId = null;
        if (changedEntities.isEmpty()) {
            return new CmsValidationResult(null, null);
        }
        structureId = CmsContentDefinition.entityIdToUuid(changedEntities.get(0).getId());
        if (structureId != null) {
            CmsObject cms = getCmsObject();
            try {
                CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                for (CmsEntity entity : changedEntities) {
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
        return new CmsValidationResult(null, null);
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
     * @param visitor the content type visitor
     * @param includeInvisible include invisible attributes
     *
     * @return the entity
     */
    protected CmsEntity readEntity(
        CmsXmlContent content,
        Element element,
        Locale locale,
        String entityId,
        String parentPath,
        String typeName,
        CmsContentTypeVisitor visitor,
        boolean includeInvisible) {

        String newEntityId = entityId + (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parentPath) ? "/" + parentPath : "");
        CmsEntity newEntity = new CmsEntity(newEntityId, typeName);
        CmsEntity result = newEntity;

        List<Element> elements = element.elements();
        CmsType type = visitor.getTypes().get(typeName);
        boolean isChoice = type.isChoice();
        String choiceTypeName = null;
        // just needed for choice attributes
        Map<String, Integer> attributeCounter = null;
        if (isChoice) {
            choiceTypeName = type.getAttributeTypeName(CmsType.CHOICE_ATTRIBUTE_NAME);
            type = visitor.getTypes().get(type.getAttributeTypeName(CmsType.CHOICE_ATTRIBUTE_NAME));
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
            if (visitor.getTypes().get(subTypeName) == null) {
                // in case there is no type configured for this element, the schema may have changed, skip the element
                continue;
            }
            if (!includeInvisible && !visitor.getAttributeConfigurations().get(attributeName).isVisible()) {
                // skip attributes marked as invisible, there content should not be transfered to the client
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
                result = new CmsEntity(newEntityId
                    + "/"
                    + CmsType.CHOICE_ATTRIBUTE_NAME
                    + "_"
                    + child.getName()
                    + "["
                    + counter
                    + "]", choiceTypeName);
                newEntity.addAttributeValue(CmsType.CHOICE_ATTRIBUTE_NAME, result);
            }
            String path = parentPath + child.getName();
            if (visitor.getTypes().get(subTypeName).isSimpleType()) {
                I_CmsXmlContentValue value = content.getValue(path, locale, counter);
                result.addAttributeValue(attributeName, value.getStringValue(cms));
            } else {
                CmsEntity subEntity = readEntity(
                    content,
                    child,
                    locale,
                    entityId,
                    path + "[" + (counter + 1) + "]",
                    subTypeName,
                    visitor,
                    includeInvisible);
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
    protected Map<String, CmsType> readTypes(CmsXmlContentDefinition xmlContentDefinition, Locale locale) {

        CmsContentTypeVisitor visitor = new CmsContentTypeVisitor(getCmsObject(), null, locale);
        visitor.visitTypes(xmlContentDefinition, locale);
        return visitor.getTypes();
    }

    /**
     * Synchronizes the locale independent fields.<p>
     *
     * @param file the content file
     * @param content the XML content
     * @param skipPaths the paths to skip during locale synchronization
     * @param entities the edited entities
     * @param lastEdited the last edited locale
     *
     * @throws CmsXmlException if something goes wrong
     */
    protected void synchronizeLocaleIndependentFields(
        CmsFile file,
        CmsXmlContent content,
        Collection<String> skipPaths,
        Collection<CmsEntity> entities,
        Locale lastEdited) throws CmsXmlException {

        CmsEntity lastEditedEntity = null;
        for (CmsEntity entity : entities) {
            if (lastEdited.equals(CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entity.getId())))) {
                lastEditedEntity = entity;
            } else {
                synchronizeLocaleIndependentForEntity(file, content, skipPaths, entity);
            }
        }
        if (lastEditedEntity != null) {
            // prepare the last edited last, to sync locale independent fields
            synchronizeLocaleIndependentForEntity(file, content, skipPaths, lastEditedEntity);
        }
    }

    /**
     * Transfers values marked as invisible from the original entity to the target entity.<p>
     *
     * @param original the original entity
     * @param target the target entiy
     * @param visitor the type visitor holding the content type configuration
     */
    protected void transferInvisibleValues(CmsEntity original, CmsEntity target, CmsContentTypeVisitor visitor) {

        List<String> invisibleAttributes = new ArrayList<String>();
        for (Entry<String, CmsAttributeConfiguration> configEntry : visitor.getAttributeConfigurations().entrySet()) {
            if (!configEntry.getValue().isVisible()) {
                invisibleAttributes.add(configEntry.getKey());
            }
        }
        CmsContentDefinition.transferValues(
            original,
            target,
            invisibleAttributes,
            visitor.getTypes(),
            visitor.getAttributeConfigurations(),
            true);
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
        CmsEntity entity,
        Locale contentLocale) {

        for (CmsEntityAttribute attribute : entity.getAttributes()) {
            if (CmsType.CHOICE_ATTRIBUTE_NAME.equals(attribute.getAttributeName())) {
                List<CmsEntity> choiceEntities = attribute.getComplexValues();
                for (int i = 0; i < choiceEntities.size(); i++) {
                    List<CmsEntityAttribute> choiceAttributes = choiceEntities.get(i).getAttributes();
                    // each choice entity may only have a single attribute with a single value
                    assert (choiceAttributes.size() == 1) && choiceAttributes.get(0).isSingleValue() : "each choice entity may only have a single attribute with a single value";
                    CmsEntityAttribute choiceAttribute = choiceAttributes.get(0);
                    String elementPath = parentPath + getElementName(choiceAttribute.getAttributeName());
                    if (choiceAttribute.isSimpleValue()) {
                        String value = choiceAttribute.getSimpleValue();
                        I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                        if (field == null) {
                            field = content.addValue(cms, elementPath, contentLocale, i);
                        }
                        field.setStringValue(cms, value);
                    } else {
                        CmsEntity child = choiceAttribute.getComplexValue();
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
                    List<CmsEntity> entities = attribute.getComplexValues();
                    for (int i = 0; i < entities.size(); i++) {
                        CmsEntity child = entities.get(i);
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
     * Evaluates any wildcards in the given scope and returns all allowed permutations of it.<p>
     * 
     * a path like Paragraph* /Image should result in Paragraph[0]/Image, Paragraph[1]/Image and Paragraph[2]/Image
     * in case max occurrence for Paragraph is 3
     * 
     * @param scope the scope
     * @param definition the content definition
     * 
     * @return the evaluate scope permutations
     */
    private Set<String> evaluateScope(String scope, CmsXmlContentDefinition definition) {

        Set<String> evaluatedScopes = new HashSet<String>();
        if (scope.contains("*")) {
            // evaluate wildcards to get all allowed permutations of the scope
            // a path like Paragraph*/Image should result in Paragraph[0]/Image, Paragraph[1]/Image and Paragraph[2]/Image
            // in case max occurrence for Paragraph is 3

            String[] pathElements = scope.split("/");
            String parentPath = "";

            for (int i = 0; i < pathElements.length; i++) {
                String elementName = pathElements[i];
                boolean hasWildCard = elementName.endsWith("*");

                if (hasWildCard) {
                    elementName = elementName.substring(0, elementName.length() - 1);
                    parentPath = CmsStringUtil.joinPaths(parentPath, elementName);
                    I_CmsXmlSchemaType type = definition.getSchemaType(parentPath);
                    Set<String> tempScopes = new HashSet<String>();
                    if (type.getMaxOccurs() == Integer.MAX_VALUE) {
                        throw new IllegalStateException(
                            "Can not use fields with unbounded maxOccurs in scopes for editor change handler.");
                    }
                    for (int j = 0; j < type.getMaxOccurs(); j++) {
                        if (evaluatedScopes.isEmpty()) {
                            tempScopes.add(elementName + "[" + (j + 1) + "]");
                        } else {

                            for (String evScope : evaluatedScopes) {
                                tempScopes.add(CmsStringUtil.joinPaths(evScope, elementName + "[" + (j + 1) + "]"));
                            }
                        }
                    }
                    evaluatedScopes = tempScopes;
                } else {
                    parentPath = CmsStringUtil.joinPaths(parentPath, elementName);
                    Set<String> tempScopes = new HashSet<String>();
                    if (evaluatedScopes.isEmpty()) {
                        tempScopes.add(elementName);
                    } else {
                        for (String evScope : evaluatedScopes) {
                            tempScopes.add(CmsStringUtil.joinPaths(evScope, elementName));
                        }
                    }
                    evaluatedScopes = tempScopes;
                }
            }
        } else {
            evaluatedScopes.add(scope);
        }
        return evaluatedScopes;
    }

    /**
     * Evaluates the values of the locale independent fields and the paths to skip during locale synchronization.<p>
     *
     * @param content the XML content
     * @param syncValues the map of synchronization values
     * @param skipPaths the list o paths to skip
     */
    private void evaluateSyncLocaleValues(
        CmsXmlContent content,
        Map<String, String> syncValues,
        Collection<String> skipPaths) {

        CmsObject cms = getCmsObject();
        for (Locale locale : content.getLocales()) {
            for (String elementPath : content.getContentDefinition().getContentHandler().getSynchronizations()) {
                for (I_CmsXmlContentValue contentValue : content.getSimpleValuesBelowPath(elementPath, locale)) {
                    String valuePath = contentValue.getPath();
                    boolean skip = false;
                    for (String skipPath : skipPaths) {
                        if (valuePath.startsWith(skipPath)) {
                            skip = true;
                            break;
                        }
                    }
                    if (!skip) {
                        String value = contentValue.getStringValue(cms);
                        if (syncValues.containsKey(valuePath)) {
                            if (!syncValues.get(valuePath).equals(value)) {
                                // in case the current value does not match the previously stored value,
                                // remove it and add the parent path to the skipPaths list
                                syncValues.remove(valuePath);
                                int pathLevelDiff = (CmsResource.getPathLevel(valuePath) - CmsResource.getPathLevel(elementPath)) + 1;
                                for (int i = 0; i < pathLevelDiff; i++) {
                                    valuePath = CmsXmlUtils.removeLastXpathElement(valuePath);
                                }
                                skipPaths.add(valuePath);
                            }
                        } else {
                            syncValues.put(valuePath, value);
                        }
                    }
                }
            }
        }
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
     * Returns the change handler scopes.<p>
     * 
     * @param definition the content definition
     * 
     * @return the scopes
     */
    private Set<String> getChangeHandlerScopes(CmsXmlContentDefinition definition) {

        List<I_CmsXmlContentEditorChangeHandler> changeHandlers = definition.getContentHandler().getEditorChangeHandlers();
        Set<String> scopes = new HashSet<String>();
        for (I_CmsXmlContentEditorChangeHandler handler : changeHandlers) {
            String scope = handler.getScope();
            scopes.addAll(evaluateScope(scope, definition));
        }
        return scopes;
    }

    /**
     * Returns the XML content document.<p>
     * 
     * @param file the resource file
     * @param fromCache <code>true</code> to use the cached document
     * 
     * @return the content document
     * 
     * @throws CmsXmlException if reading the XML fails
     */
    private CmsXmlContent getContentDocument(CmsFile file, boolean fromCache) throws CmsXmlException {

        CmsXmlContent content;
        if (fromCache) {
            content = getSessionCache().getCacheXmlContent(file.getStructureId());
        } else {
            content = CmsXmlContentFactory.unmarshal(getCmsObject(), file);
            getSessionCache().setCacheXmlContent(file.getStructureId(), content);
        }
        return content;
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
     * Returns the session cache.<p>
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache() {

        if (m_sessionCache == null) {
            m_sessionCache = CmsADESessionCache.getCache(getRequest(), getCmsObject());
        }
        return m_sessionCache;
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
        List<Locale> availableLocalesList = OpenCms.getLocaleManager().getAvailableLocales(cms, file);
        if (!availableLocalesList.contains(locale)) {
            availableLocalesList.retainAll(content.getLocales());
            List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, file);
            Locale replacementLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
                locale,
                defaultLocales,
                availableLocalesList);
            LOG.info("Can't edit locale "
                + locale
                + " of file "
                + file.getRootPath()
                + " because it is not configured as available locale. Using locale "
                + replacementLocale
                + " instead.");
            locale = replacementLocale;
            entityId = CmsContentDefinition.uuidToEntityId(file.getStructureId(), locale.toString());
        }

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
        CmsEntity entity = null;
        Map<String, String> syncValues = new HashMap<String, String>();
        Collection<String> skipPaths = new HashSet<String>();
        evaluateSyncLocaleValues(content, syncValues, skipPaths);
        if (content.hasLocale(locale) && newLocale) {
            // a new locale is requested, so remove the present one
            content.removeLocale(locale);
        }
        if (!content.hasLocale(locale)) {
            content.addLocale(cms, locale);
            // sync the locale values
            if (!visitor.getLocaleSynchronizations().isEmpty() && (content.getLocales().size() > 1)) {
                for (Locale contentLocale : content.getLocales()) {
                    if (!contentLocale.equals(locale)) {
                        content.synchronizeLocaleIndependentValues(cms, skipPaths, contentLocale);
                    }
                }
            }
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
            visitor,
            false);
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
        boolean autoUnlock = OpenCms.getWorkplaceManager().shouldAcaciaUnlock();
        Map<String, CmsEntity> entities = new HashMap<String, CmsEntity>();
        entities.put(entityId, entity);

        return new CmsContentDefinition(
            entityId,
            entities,
            visitor.getAttributeConfigurations(),
            visitor.getWidgetConfigurations(),
            visitor.getComplexWidgetData(),
            visitor.getTypes(),
            visitor.getTabInfos(),
            locale.toString(),
            contentLocales,
            availableLocales,
            visitor.getLocaleSynchronizations(),
            syncValues,
            skipPaths,
            title,
            cms.getSitePath(file),
            typeName,
            performedAutoCorrection,
            autoUnlock,
            getChangeHandlerScopes(content.getContentDefinition()));
    }

    /**
     * Creates a new resource according to the new link, or returns the model file informations
     * modelFileId is <code>null</code> but required.<p>
     *
     * @param newLink the new link
     * @param referenceResource the reference resource
     * @param modelFileId the model file structure id
     * @param locale the content locale
     * @param mode the content creation mode
     * @param postCreateHandler the class name for the post-create handler
     *
     * @return the content definition
     *
     * @throws CmsException if creating the resource failed
     */
    private CmsContentDefinition readContentDefnitionForNew(
        String newLink,
        CmsResource referenceResource,
        CmsUUID modelFileId,
        Locale locale,
        String mode,
        String postCreateHandler) throws CmsException {

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
            modelFile,
            mode,
            postCreateHandler);
        CmsResource resource = getCmsObject().readResource(newFileName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFile file = getCmsObject().readFile(resource);
        CmsXmlContent content = getContentDocument(file, false);
        CmsContentDefinition contentDefinition = readContentDefinition(file, content, null, locale, false);
        contentDefinition.setDeleteOnCancel(true);
        return contentDefinition;
    }

    /**
     * Synchronizes the locale independent fields for the given entity.<p>
     *
     * @param file the content file
     * @param content the XML content
     * @param skipPaths the paths to skip during locale synchronization
     * @param entity the entity
     *
     * @throws CmsXmlException if something goes wrong
     */
    private void synchronizeLocaleIndependentForEntity(
        CmsFile file,
        CmsXmlContent content,
        Collection<String> skipPaths,
        CmsEntity entity) throws CmsXmlException {

        CmsObject cms = getCmsObject();
        String entityId = entity.getId();
        Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
        CmsContentTypeVisitor visitor = null;
        CmsEntity originalEntity = null;
        if (content.getHandler().hasVisibilityHandlers()) {
            visitor = new CmsContentTypeVisitor(cms, file, contentLocale);
            visitor.visitTypes(content.getContentDefinition(), getWorkplaceLocale(cms));
        }
        if (content.hasLocale(contentLocale)) {
            if ((visitor != null) && visitor.hasInvisibleFields()) {
                // we need to add invisible content values to the entity before saving
                Element element = content.getLocaleNode(contentLocale);
                originalEntity = readEntity(
                    content,
                    element,
                    contentLocale,
                    entityId,
                    "",
                    getTypeUri(content.getContentDefinition()),
                    visitor,
                    true);
            }
            content.removeLocale(contentLocale);
        }
        content.addLocale(cms, contentLocale);
        if ((visitor != null) && visitor.hasInvisibleFields()) {
            transferInvisibleValues(originalEntity, entity, visitor);
        }
        addEntityAttributes(cms, content, "", entity, contentLocale);
        content.synchronizeLocaleIndependentValues(cms, skipPaths, contentLocale);
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
    private CmsValidationResult validateContent(CmsObject cms, CmsUUID structureId, CmsXmlContent content) {

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
        return new CmsValidationResult(errorsByEntity, warningsByEntity);
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
