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

package org.opencms.ade.contenteditor;

import org.opencms.acacia.shared.CmsAttributeConfiguration;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsEntityHtml;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.acacia.shared.CmsType;
import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.CmsElementUtil;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.CmsEditHandlerData;
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.ade.contenteditor.shared.CmsSaveResult;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.file.collectors.I_CmsCollectorPostCreateHandler;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspTagEdit;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCategoryWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsMultiSelectWidget;
import org.opencms.widgets.CmsRadioSelectWidget;
import org.opencms.widgets.CmsSelectComboWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.widgets.I_CmsADEWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsEditor;
import org.opencms.workplace.editors.CmsEditorCssHandlerDefault;
import org.opencms.workplace.editors.CmsXmlContentEditor;
import org.opencms.workplace.editors.directedit.I_CmsEditHandler;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.CmsXmlAccessRestrictionValue;
import org.opencms.xml.types.CmsXmlDynamicCategoryValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;

/**
 * Service to provide entity persistence within OpenCms. <p>
 */
public class CmsContentService extends CmsGwtService implements I_CmsContentService {

    /** Request context attribute to mark a writeFile() triggered by the user saving in the content editor. */
    public static final String ATTR_EDITOR_SAVING = "__EDITOR_SAVING";

    /** The logger for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsContentService.class);

    /** The type name prefix. */
    static final String TYPE_NAME_PREFIX = "http://opencms.org/types/";

    /** The RDFA attributes string. */
    private static final String RDFA_ATTRIBUTES = CmsGwtConstants.ATTR_DATA_ID
        + "=\"%1$s\" "
        + CmsGwtConstants.ATTR_DATA_FIELD
        + "=\"%2$s\"";

    /** The serial version id. */
    private static final long serialVersionUID = 7873052619331296648L;

    /** The setting type name. */
    private static final String SETTING_TYPE_NAME = "###SETTING_TYPE###";

    /** The settings attribute name prefix. */
    private static final String SETTINGS_ATTRIBUTE_NAME_PREFIX = "SETTING:::";

    /** The attribute name used for the client id. */
    private static final String SETTINGS_CLIENT_ID_ATTRIBUTE = "/" + SETTINGS_ATTRIBUTE_NAME_PREFIX + "CLIENT_ID:::";

    /** The settings validation rule type name. */
    private static final String SETTINGS_RULE_TYPE_ERROR = "error";

    /** Mapping client widget names to server side widget classes. */
    private static final Map<String, Class<? extends I_CmsADEWidget>> WIDGET_MAPPINGS = new HashMap<>();

    static {
        WIDGET_MAPPINGS.put("string", CmsInputWidget.class);
        WIDGET_MAPPINGS.put("select", CmsSelectWidget.class);
        WIDGET_MAPPINGS.put("multicheck", org.opencms.widgets.CmsMultiSelectWidget.class);
        WIDGET_MAPPINGS.put("selectcombo", CmsSelectComboWidget.class);
        WIDGET_MAPPINGS.put("checkbox", CmsCheckboxWidget.class);
        WIDGET_MAPPINGS.put("combo", CmsComboWidget.class);
        WIDGET_MAPPINGS.put("datebox", CmsCalendarWidget.class);
        WIDGET_MAPPINGS.put("gallery", CmsVfsFileWidget.class);
        WIDGET_MAPPINGS.put("multiselectbox", CmsMultiSelectWidget.class);
        WIDGET_MAPPINGS.put("radio", CmsRadioSelectWidget.class);
        WIDGET_MAPPINGS.put("groupselection", CmsGroupWidget.class);
    }

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /** The current users workplace locale. */
    private Locale m_workplaceLocale;

    /**
     * Creates a new resource to edit, delegating to an edit handler if edit handler data is passed in.<p>
     *
     * @param cms The CmsObject of the current request context.
     * @param newLink A string, specifying where which new content should be created.
     * @param locale The locale for which the
     * @param referenceSitePath site path of the currently edited content.
     * @param modelFileSitePath site path of the model file
     * @param mode optional creation mode
     * @param postCreateHandler optional class name of an {@link I_CmsCollectorPostCreateHandler} which is invoked after the content has been created.
     *
     * @return The site-path of the newly created resource.
     * @throws CmsException if something goes wrong
     */
    public static String defaultCreateResourceToEdit(
        CmsObject cms,
        String newLink,
        Locale locale,
        String referenceSitePath,
        String modelFileSitePath,
        String mode,
        String postCreateHandler)
    throws CmsException {

        String newFileName;
        if (newLink.startsWith(CmsJspTagEdit.NEW_LINK_IDENTIFIER)) {

            newFileName = CmsJspTagEdit.createResource(
                cms,
                newLink,
                locale,
                referenceSitePath,
                modelFileSitePath,
                mode,
                postCreateHandler);
        } else {
            newFileName = A_CmsResourceCollector.createResourceForCollector(
                cms,
                newLink,
                locale,
                referenceSitePath,
                modelFileSitePath,
                mode,
                postCreateHandler);
        }
        return newFileName;
    }

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
     * @param value the XML content value
     *
     * @return the RDFA
     */
    public static String getRdfaAttributes(I_CmsXmlContentValue value) {

        String path = "";
        String elementPath = value.getPath();
        if (elementPath.contains("/")) {
            path += "/" + removePathIndexes(elementPath.substring(0, elementPath.lastIndexOf("/")) + ":");
        }
        path += CmsContentService.getAttributeName(value);
        return String.format(RDFA_ATTRIBUTES, CmsContentService.getEntityId(value), path);
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

        String id = CmsContentDefinition.uuidToEntityId(
            parentValue.getDocument().getFile().getStructureId(),
            parentValue.getLocale().toString()) + "/" + parentValue.getPath();
        String path = "";
        String[] children = childNames.split("\\|");
        for (int i = 0; i < children.length; i++) {
            I_CmsXmlSchemaType schemaType = parentValue.getContentDefinition().getSchemaType(
                parentValue.getName() + "/" + children[i]);
            if (schemaType != null) {
                if (i > 0) {
                    path += " ";
                }
                String typePath = parentValue.getPath();
                path += "/" + removePathIndexes(typePath) + ":";
                path += getTypeUri(schemaType.getContentDefinition()) + "/" + children[i];
            }
        }
        return String.format(RDFA_ATTRIBUTES, id, path);
    }

    /**
     * Returns the RDF annotations required for in line editing.<p>
     *
     * @param document the parent XML document
     * @param contentLocale the content locale
     * @param elementPath the element xpath to get the RDF annotation for
     *
     * @return the RDFA
     */
    public static String getRdfaAttributes(I_CmsXmlDocument document, Locale contentLocale, String elementPath) {

        I_CmsXmlSchemaType schemaType = document.getContentDefinition().getSchemaType(elementPath);
        if (schemaType != null) {
            String path = "";
            if (elementPath.contains("/")) {
                path += "/" + removePathIndexes(elementPath.substring(0, elementPath.lastIndexOf("/")) + ":");
            }
            path += getTypeUri(schemaType.getContentDefinition()) + "/" + elementPath;
            return String.format(
                RDFA_ATTRIBUTES,
                CmsContentDefinition.uuidToEntityId(document.getFile().getStructureId(), contentLocale.toString()),
                path);
        } else {
            return "";
        }
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
     * Removes the XPath indexes from the given path.<p>
     *
     * @param path the path
     *
     * @return the changed path
     */
    private static String removePathIndexes(String path) {

        return path.replaceAll("\\[.*\\]", "");
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#callEditorChangeHandlers(java.lang.String, org.opencms.acacia.shared.CmsEntity, java.util.Collection, java.util.Collection)
     */
    public CmsContentDefinition callEditorChangeHandlers(
        String entityId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Collection<String> changedScopes)
    throws CmsRpcException {

        CmsContentDefinition result = null;
        CmsObject cms = getCmsObject();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        CmsUUID structureId = CmsContentDefinition.entityIdToUuid(editedLocaleEntity.getId());
        if (structureId != null) {

            CmsResource resource = null;
            Locale locale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            try {
                resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                ensureLock(resource);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = getContentDocument(file, true).clone();
                checkAutoCorrection(cms, content);
                synchronizeLocaleIndependentForEntity(file, content, skipPaths, editedLocaleEntity);
                for (I_CmsXmlContentEditorChangeHandler handler : content.getContentDefinition().getContentHandler().getEditorChangeHandlers(
                    false)) {
                    Set<String> handlerScopes = evaluateScope(handler.getScope(), content.getContentDefinition());
                    if (!Collections.disjoint(changedScopes, handlerScopes)) {
                        handler.handleChange(cms, content, locale, changedScopes);
                    }
                }
                result = readContentDefinition(
                    file,
                    content,
                    entityId,
                    null,
                    locale,
                    false,
                    null,
                    editedLocaleEntity,
                    Collections.emptyMap(),
                    config);
            } catch (Exception e) {
                error(e);
            }
        }
        return result;
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
            Locale sourceContentLocale = CmsLocaleManager.getLocale(
                CmsContentDefinition.getLocaleFromId(sourceLocale.getId()));
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
     * @see org.opencms.gwt.CmsGwtService#getCmsObject()
     */
    @Override
    public CmsObject getCmsObject() {

        CmsObject result = super.getCmsObject();
        // disable link invalidation in the editor
        result.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        return result;
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#loadContentDefinition(java.lang.String)
     */
    public CmsContentDefinition loadContentDefinition(String entityId) throws CmsRpcException {

        throw new CmsRpcException(new UnsupportedOperationException());
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadDefinition(java.lang.String, java.lang.String, org.opencms.acacia.shared.CmsEntity, java.util.Collection, java.util.Map)
     */
    public CmsContentDefinition loadDefinition(

        String entityId,
        String clientId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Map<String, String> settingPresets)
    throws CmsRpcException {

        CmsContentDefinition definition = null;
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                getCmsObject(),
                getCmsObject().getRequestContext().getRootUri());

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
                clientId,
                contentLocale,
                false,
                null,
                editedLocaleEntity,
                settingPresets,
                config);
        } catch (Exception e) {
            error(e);
        }
        return definition;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadInitialDefinition(java.lang.String, java.lang.String, java.lang.String, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.opencms.ade.contenteditor.shared.CmsEditHandlerData, java.util.Map, java.lang.String)
     */
    public CmsContentDefinition loadInitialDefinition(

        String entityId,
        String clientId,
        String newLink,
        CmsUUID modelFileId,
        String editContext,
        String mainLocale,
        String mode,
        String postCreateHandler,
        CmsEditHandlerData editHandlerDataForNew,
        Map<String, String> settingPresets,
        String editorStylesheet)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        CmsContentDefinition result = null;
        getCmsObject().getRequestContext().setAttribute(CmsXmlContentEditor.ATTRIBUTE_EDITCONTEXT, editContext);
        if (editorStylesheet != null) {
            getCmsObject().getRequestContext().setAttribute(
                CmsEditorCssHandlerDefault.ATTRIBUTE_EDITOR_STYLESHEET,
                editorStylesheet);
        }
        try {
            CmsUUID structureId = CmsContentDefinition.entityIdToUuid(entityId);
            CmsResource resource = getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
            getSessionCache().clearDynamicValues();
            getSessionCache().uncacheXmlContent(structureId);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(newLink)) {
                result = readContentDefinitionForNew(
                    newLink,
                    resource,
                    modelFileId,
                    contentLocale,
                    mode,
                    postCreateHandler,
                    editHandlerDataForNew,
                    settingPresets);
            } else {
                CmsFile file = getCmsObject().readFile(resource);
                CmsXmlContent content = getContentDocument(file, false);
                result = readContentDefinition(
                    file,
                    content,
                    CmsContentDefinition.uuidToEntityId(structureId, contentLocale.toString()),
                    clientId,
                    contentLocale,
                    false,
                    mainLocale != null ? CmsLocaleManager.getLocale(mainLocale) : null,
                    null,
                    settingPresets,
                    config);
            }
        } catch (Throwable t) {
            error(t);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#loadNewDefinition(java.lang.String, java.lang.String, org.opencms.acacia.shared.CmsEntity, java.util.Collection, java.util.Map)
     */
    public CmsContentDefinition loadNewDefinition(

        String entityId,
        String clientId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Map<String, String> settingPresets)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
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
                clientId,
                contentLocale,
                true,
                null,
                editedLocaleEntity,
                settingPresets,
                config);
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

            // not necessary in most cases, but some old dialogs pass the path in encoded form
            paramResource = CmsEncoder.decode(paramResource);

            try {
                CmsResource resource = cms.readResource(paramResource, CmsResourceFilter.IGNORE_EXPIRATION);

                if (OpenCms.getADEManager().isEditorRestricted(cms, resource)) {
                    // Context menus / buttons for editing the file should be disabled if above condition is true.
                    // You only get here if you directly open the editor URL, so this does not need
                    // a particularly nice error message
                    throw new CmsRuntimeException(
                        org.opencms.ade.contenteditor.Messages.get().container(
                            org.opencms.ade.contenteditor.Messages.ERR_EDITOR_RESTRICTED_0));
                }
                if (CmsResourceTypeXmlContent.isXmlContent(resource) || createNew) {
                    CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());
                    boolean reused = false;
                    try {
                        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
                        if (type != null) {
                            String typeStr = type.getTypeName();
                            CmsResourceTypeConfig typeConfig = config.getResourceType(typeStr);
                            if ((typeConfig != null) && typeConfig.isCheckReuse()) {
                                if (OpenCms.getADEManager().getOfflineElementUses(resource).limit(2).count() > 1) {
                                    reused = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramLocale)) {
                        locale = CmsLocaleManager.getLocale(paramLocale);
                    }
                    CmsContentDefinition result;
                    getSessionCache().clearDynamicValues();
                    if (createNew) {
                        if (locale == null) {
                            locale = OpenCms.getLocaleManager().getDefaultLocale(cms, paramResource);
                        }
                        CmsUUID modelFileId = null;
                        String paramModelFile = getRequest().getParameter(CmsWorkplace.PARAM_MODELFILE);

                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramModelFile)) {
                            modelFileId = cms.readResource(paramModelFile).getStructureId();
                        }

                        String mode = getRequest().getParameter(CmsEditorConstants.PARAM_MODE);
                        String postCreateHandler = getRequest().getParameter(
                            CmsEditorConstants.PARAM_POST_CREATE_HANDLER);
                        result = readContentDefinitionForNew(
                            paramNewLink,
                            resource,
                            modelFileId,
                            locale,
                            mode,
                            postCreateHandler,
                            null,
                            Collections.emptyMap());
                    } else {

                        CmsFile file = cms.readFile(resource);
                        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                        getSessionCache().setCacheXmlContent(resource.getStructureId(), content);
                        if (locale == null) {
                            locale = OpenCms.getLocaleManager().getBestAvailableLocaleForXmlContent(
                                getCmsObject(),
                                resource,
                                content);
                        }
                        result = readContentDefinition(
                            file,
                            content,
                            null,
                            null,
                            locale,
                            false,
                            null,
                            null,
                            Collections.emptyMap(),
                            null);
                    }
                    result.setDirectEdit(isDirectEdit);
                    result.setReusedElement(reused);
                    return result;
                }
            } catch (Throwable e) {
                error(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#saveAndDeleteEntities(org.opencms.acacia.shared.CmsEntity, java.lang.String, java.util.List, java.util.Collection, java.lang.String, boolean)
     */
    public CmsSaveResult saveAndDeleteEntities(
        CmsEntity lastEditedEntity,
        String clientId,
        List<String> deletedEntities,
        Collection<String> skipPaths,
        String lastEditedLocale,
        boolean clearOnSuccess)
    throws CmsRpcException {

        CmsUUID structureId = null;
        if (lastEditedEntity != null) {
            structureId = CmsContentDefinition.entityIdToUuid(lastEditedEntity.getId());
        }
        if ((structureId == null) && !deletedEntities.isEmpty()) {
            structureId = CmsContentDefinition.entityIdToUuid(deletedEntities.get(0));
        }
        CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
            getCmsObject(),
            getCmsObject().getRequestContext().getRootUri());
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
                    return new CmsSaveResult(false, validationResult);
                }
                boolean hasChangedSettings = false;
                if ((clientId != null) && (lastEditedEntity != null)) {
                    CmsContainerElementBean containerElement = getSessionCache().getCacheContainerElement(clientId);
                    I_CmsFormatterBean formatter = getFormatterForElement(configData, containerElement);
                    if ((formatter != null)
                        && formatter.isAllowsSettingsInEditor()
                        && !formatter.getSettings(configData).isEmpty()) {
                        Locale locale = CmsLocaleManager.getLocale(lastEditedLocale);
                        Map<String, CmsXmlContentProperty> settingsConfig = OpenCms.getADEManager().getFormatterSettings(
                            cms,
                            configData,
                            formatter,
                            containerElement.getResource(),
                            locale,
                            getRequest());
                        validateSettings(lastEditedEntity, validationResult, settingsConfig);
                        if (validationResult.hasErrors()) {
                            return new CmsSaveResult(false, validationResult);
                        }

                        List<I_CmsFormatterBean> nestedFormatters = OpenCms.getADEManager().getNestedFormatters(
                            cms,
                            configData,
                            containerElement.getResource(),
                            locale,
                            getRequest());
                        hasChangedSettings = saveSettings(
                            lastEditedEntity,
                            containerElement,
                            settingsConfig,
                            nestedFormatters);

                    }
                }
                CmsAccessRestrictionInfo restrictionInfo = CmsAccessRestrictionInfo.getRestrictionInfo(
                    cms,
                    content.getContentDefinition());

                if (restrictionInfo != null) {
                    // the value from the editor is still stored in the content value object, so we can use it here, even if it is not written to the XML content file in the end.
                    I_CmsXmlContentValue restrictionValue = content.getValue(
                        restrictionInfo.getPath(),
                        CmsLocaleManager.getLocale(lastEditedLocale));
                    boolean restricted = false;
                    if (restrictionValue != null) {
                        restricted = Boolean.parseBoolean(restrictionValue.getStringValue(cms));
                    }
                    cms.setRestricted(content.getFile(), restrictionInfo.getGroup().getName(), restricted);
                }

                writeCategories(file, content, lastEditedEntity);

                writeContent(cms, file, content, getFileEncoding(cms, file));

                // update offline indices
                OpenCms.getSearchManager().updateOfflineIndexes();
                if (clearOnSuccess) {
                    tryUnlock(resource);
                    getSessionCache().uncacheXmlContent(structureId);
                }
                return new CmsSaveResult(hasChangedSettings, null);
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
     * @see org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService#saveValue(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public String saveValue(String contentId, String contentPath, String localeString, String newValue)
    throws CmsRpcException {

        OpenCms.getLocaleManager();
        Locale locale = CmsLocaleManager.getLocale(localeString);

        try {
            CmsObject cms = getCmsObject();
            CmsResource element = cms.readResource(new CmsUUID(contentId), CmsResourceFilter.IGNORE_EXPIRATION);
            ensureLock(element);
            CmsFile elementFile = cms.readFile(element);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, elementFile);
            I_CmsXmlContentValue value = content.getValue(contentPath, locale);
            value.setStringValue(cms, newValue);
            for (I_CmsXmlContentEditorChangeHandler handler : content.getContentDefinition().getContentHandler().getEditorChangeHandlers(
                false)) {
                Set<String> handlerScopes = evaluateScope(handler.getScope(), content.getContentDefinition());
                if (handlerScopes.contains(contentPath)) {
                    handler.handleChange(cms, content, locale, Collections.singletonList(contentPath));
                }
            }
            content.synchronizeLocaleIndependentValues(cms, Collections.<String> emptyList(), locale);
            byte[] newData = content.marshal();
            elementFile.setContents(newData);
            cms.writeFile(elementFile);
            tryUnlock(elementFile);
            return "";
        } catch (Exception e) {
            error(e);
            return null;
        }

    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#updateEntityHtml(org.opencms.acacia.shared.CmsEntity, java.lang.String, java.lang.String)
     */
    public CmsEntityHtml updateEntityHtml(CmsEntity entity, String contextUri, String htmlContextInfo)
    throws Exception {

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
                    boolean isDetailViewContainer = contextInfo.getBoolean(
                        CmsCntPageData.JSONKEY_ISDETAILVIEWCONTAINER);
                    JSONObject presets = contextInfo.getJSONObject(CmsCntPageData.JSONKEY_PRESETS);
                    HashMap<String, String> presetsMap = new HashMap<String, String>();
                    for (String key : presets.keySet()) {
                        String val = presets.getString(key);
                        presetsMap.put(key, val);
                    }
                    CmsContainer container = new CmsContainer(
                        containerName,
                        containerType,
                        null,
                        containerWidth,
                        maxElements,
                        isDetailViewContainer,
                        detailView,
                        true,
                        Collections.<CmsContainerElement> emptyList(),
                        null,
                        null,
                        presetsMap);
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
                        container);
                }
                return new CmsEntityHtml(htmlContent, validationResult);

            } catch (Exception e) {
                error(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsContentService#validateEntity(org.opencms.acacia.shared.CmsEntity)
     */
    public CmsValidationResult validateEntity(CmsEntity changedEntity) throws CmsRpcException {

        CmsUUID structureId = null;
        if (changedEntity == null) {
            return new CmsValidationResult(null, null);
        }
        structureId = CmsContentDefinition.entityIdToUuid(changedEntity.getId());

        if (structureId != null) {
            CmsObject cms = getCmsObject();
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().getRootUri());
            Set<String> setFieldNames = Sets.newHashSet();
            try {
                CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsFile file = cms.readFile(resource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                String entityId = changedEntity.getId();
                Locale contentLocale = CmsLocaleManager.getLocale(CmsContentDefinition.getLocaleFromId(entityId));
                if (content.hasLocale(contentLocale)) {
                    content.removeLocale(contentLocale);
                }
                content.addLocale(cms, contentLocale);
                setFieldNames.addAll(addEntityAttributes(cms, content, "", changedEntity, contentLocale));

                CmsValidationResult result = validateContent(cms, structureId, content, setFieldNames);
                CmsEntityAttribute clientIdAttr = changedEntity.getAttribute(SETTINGS_CLIENT_ID_ATTRIBUTE);
                if (clientIdAttr != null) {
                    String clientId = clientIdAttr.getSimpleValue();
                    CmsContainerElementBean containerElement = getSessionCache().getCacheContainerElement(clientId);
                    I_CmsFormatterBean formatter = getFormatterForElement(config, containerElement);
                    if ((formatter != null)
                        && formatter.isAllowsSettingsInEditor()
                        && !formatter.getSettings(config).isEmpty()) {
                        Map<String, CmsXmlContentProperty> settingsConfig = OpenCms.getADEManager().getFormatterSettings(
                            cms,
                            config,
                            formatter,
                            containerElement.getResource(),
                            contentLocale,
                            getRequest());
                        validateSettings(changedEntity, result, settingsConfig);
                    }
                }
                return result;
            } catch (Throwable e) {
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
     * @param editedLocalEntity the edited locale entity
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
        boolean includeInvisible,
        CmsEntity editedLocalEntity) {

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
                result = new CmsEntity(
                    newEntityId + "/" + CmsType.CHOICE_ATTRIBUTE_NAME + "_" + child.getName() + "[" + counter + "]",
                    choiceTypeName);
                newEntity.addAttributeValue(CmsType.CHOICE_ATTRIBUTE_NAME, result);
            }
            String path = parentPath + child.getName();
            if (visitor.isDynamicallyLoaded(attributeName)) {
                I_CmsXmlContentValue value = content.getValue(path, locale, counter);
                String attributeValue = getDynamicAttributeValue(
                    content.getFile(),
                    value,
                    attributeName,
                    editedLocalEntity);
                result.addAttributeValue(attributeName, attributeValue);
            } else if (visitor.getTypes().get(subTypeName).isSimpleType()) {
                I_CmsXmlContentValue value = content.getValue(path, locale, counter);
                result.addAttributeValue(attributeName, value.getStringValue(cms));
            } else {
                CmsEntity editedSubEntity = null;
                if ((editedLocalEntity != null) && (editedLocalEntity.getAttribute(attributeName) != null)) {
                    editedSubEntity = editedLocalEntity.getAttribute(attributeName).getComplexValue();
                }
                CmsEntity subEntity = readEntity(
                    content,
                    child,
                    locale,
                    entityId,
                    path + "[" + (counter + 1) + "]",
                    subTypeName,
                    visitor,
                    includeInvisible,
                    editedSubEntity);
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
        Locale lastEdited)
    throws CmsXmlException {

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
    *
    * @return the set of xpaths of simple fields in the XML content which were set by this method
    */
    private Set<String> addEntityAttributes(
        CmsObject cms,
        CmsXmlContent content,
        String parentPath,
        CmsEntity entity,
        Locale contentLocale) {

        Set<String> fieldsSet = Sets.newHashSet();
        addEntityAttributes(cms, content, parentPath, entity, contentLocale, fieldsSet);
        return fieldsSet;
    }

    /**
     * Adds the attribute values of the entity to the given XML content.<p>
     *
     * @param cms the current cms context
     * @param content the XML content
     * @param parentPath the parent path
     * @param entity the entity
     * @param contentLocale the content locale
     * @param fieldsSet set to store which fields were set in the XML content
     */
    private void addEntityAttributes(
        CmsObject cms,
        CmsXmlContent content,
        String parentPath,
        CmsEntity entity,
        Locale contentLocale,
        Set<String> fieldsSet) {

        for (CmsEntityAttribute attribute : entity.getAttributes()) {
            if (!isSettingsAttribute(attribute.getAttributeName())) {
                if (CmsType.CHOICE_ATTRIBUTE_NAME.equals(attribute.getAttributeName())) {
                    List<CmsEntity> choiceEntities = attribute.getComplexValues();
                    for (int i = 0; i < choiceEntities.size(); i++) {
                        List<CmsEntityAttribute> choiceAttributes = choiceEntities.get(i).getAttributes();
                        // each choice entity may only have a single attribute with a single value
                        assert (choiceAttributes.size() == 1)
                            && choiceAttributes.get(
                                0).isSingleValue() : "each choice entity may only have a single attribute with a single value";
                        CmsEntityAttribute choiceAttribute = choiceAttributes.get(0);
                        String elementPath = parentPath + getElementName(choiceAttribute.getAttributeName());
                        if (choiceAttribute.isSimpleValue()) {
                            String value = choiceAttribute.getSimpleValue();
                            I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                            if (field == null) {
                                field = content.addValue(cms, elementPath, contentLocale, i);
                            }
                            field.setStringValue(cms, value);
                            fieldsSet.add(field.getPath());
                        } else {
                            CmsEntity child = choiceAttribute.getComplexValue();
                            I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                            if (field == null) {
                                field = content.addValue(cms, elementPath, contentLocale, i);
                            }
                            addEntityAttributes(cms, content, field.getPath() + "/", child, contentLocale, fieldsSet);
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
                            fieldsSet.add(field.getPath());
                        }
                    } else {
                        List<CmsEntity> entities = attribute.getComplexValues();
                        for (int i = 0; i < entities.size(); i++) {
                            CmsEntity child = entities.get(i);
                            I_CmsXmlContentValue field = content.getValue(elementPath, contentLocale, i);
                            if (field == null) {
                                field = content.addValue(cms, elementPath, contentLocale, i);
                            }
                            addEntityAttributes(cms, content, field.getPath() + "/", child, contentLocale, fieldsSet);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the setting attributes according to the setting configuration.<p>
     *
     * @param attributeConfiguration the attribute configuration
     * @param settingsConfig the setting configuration
     * @param nestedFormatters the nested formatters
     * @param messages the messages
     * @param contentLocale the content locale
     * @param settingPresets the setting presets
     *
     * @return the list of names of added attributes
     */
    private List<String> addSettingsAttributes(
        Map<String, CmsAttributeConfiguration> attributeConfiguration,
        Map<String, CmsXmlContentProperty> settingsConfig,
        List<I_CmsFormatterBean> nestedFormatters,
        CmsMessages messages,
        Locale contentLocale,
        Map<String, String> settingPresets) {

        String attrName;
        List<String> attributes = new ArrayList<String>();
        attributeConfiguration.put(
            SETTINGS_CLIENT_ID_ATTRIBUTE,
            new CmsAttributeConfiguration(
                "internal_client_id",
                "",
                null,
                null,
                null,
                DisplayType.none.name(),
                false,
                false,
                false));
        for (Entry<String, CmsXmlContentProperty> entry : settingsConfig.entrySet()) {
            CmsXmlContentProperty prop = entry.getValue();
            String niceName = prop.getNiceName();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(niceName)) {
                niceName = prop.getName();
            }
            attrName = getSettingsAttributeName(entry.getKey());
            boolean visible = !CmsGwtConstants.HIDDEN_SETTINGS_WIDGET_NAME.equals(prop.getWidget())
                && !settingPresets.containsKey(prop.getName());
            if (visible) {
                attributes.add(attrName);
            }

            attributeConfiguration.put(
                attrName,
                new CmsAttributeConfiguration(
                    niceName,
                    prop.getDescription(),
                    getWidgetName(prop.getWidget()),
                    getWidgetConfig(prop.getWidget(), prop.getWidgetConfiguration(), messages, contentLocale),
                    prop.getDefault(),
                    DisplayType.singleline.name(),
                    visible,
                    false,
                    false));
        }
        if (nestedFormatters != null) {
            for (I_CmsFormatterBean formatter : nestedFormatters) {
                attrName = getSettingsAttributeName(formatter.getId());
                attributes.add(attrName);
                attributeConfiguration.put(
                    attrName,
                    new CmsAttributeConfiguration(
                        formatter.getNiceName(m_workplaceLocale),
                        "",
                        null,
                        null,
                        null,
                        DisplayType.none.name(),
                        true,
                        false,
                        false));

            }
        }
        return attributes;
    }

    /**
     * Adds the entity types according to the setting configuration.<p>
     *
     * @param entityType the entity base type name
     * @param types the types
     * @param settingsConfig the setting configuration
     * @param nestedFormatters the nested formatters
     */
    private void addSettingsTypes(
        String entityType,
        Map<String, CmsType> types,
        Map<String, CmsXmlContentProperty> settingsConfig,
        List<I_CmsFormatterBean> nestedFormatters) {

        CmsType baseType = types.get(entityType);
        CmsType settingType = new CmsType(SETTING_TYPE_NAME);
        types.put(settingType.getId(), settingType);
        baseType.addAttribute(SETTINGS_CLIENT_ID_ATTRIBUTE, settingType, 1, 1);
        // add main settings first
        for (Entry<String, CmsXmlContentProperty> entry : settingsConfig.entrySet()) {
            boolean nested = false;
            if (nestedFormatters != null) {
                for (I_CmsFormatterBean formatter : nestedFormatters) {
                    if (entry.getKey().startsWith(formatter.getId())) {
                        nested = true;
                        break;
                    }
                }
            }
            if (!nested) {
                baseType.addAttribute(getSettingsAttributeName(entry.getKey()), settingType, 0, 1);
            }
        }
        // add nested formatter settings after
        for (Entry<String, CmsXmlContentProperty> entry : settingsConfig.entrySet()) {
            if (nestedFormatters != null) {
                for (I_CmsFormatterBean formatter : nestedFormatters) {
                    if (entry.getKey().startsWith(formatter.getId())
                        && !CmsGwtConstants.HIDDEN_SETTINGS_WIDGET_NAME.equals(entry.getValue().getWidget())) {
                        CmsType parent = types.get(formatter.getId());
                        if (parent == null) {
                            parent = new CmsType(formatter.getId());
                            types.put(parent.getId(), parent);
                            baseType.addAttribute(getSettingsAttributeName(formatter.getId()), parent, 1, 1);
                        }
                        parent.addAttribute(getSettingsAttributeName(entry.getKey()), settingType, 0, 1);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds the settings values to the given entity.<p>
     *
     * @param entity the entity
     * @param containerElement the container element bean providing the values
     * @param nestedFormatters the nested formatters
     */
    private void addSettingsValues(
        CmsEntity entity,
        CmsContainerElementBean containerElement,
        List<I_CmsFormatterBean> nestedFormatters) {

        entity.addAttributeValue(SETTINGS_CLIENT_ID_ATTRIBUTE, containerElement.editorHash());
        for (Entry<String, String> settingEntry : containerElement.getIndividualSettings().entrySet()) {
            boolean nested = false;
            if (nestedFormatters != null) {
                for (I_CmsFormatterBean formatter : nestedFormatters) {
                    if (settingEntry.getKey().startsWith(formatter.getId())) {
                        String nestedSettingAttributeName = getSettingsAttributeName(formatter.getId());
                        CmsEntity nestedEntity = null;
                        CmsEntityAttribute attribute = entity.getAttribute(nestedSettingAttributeName);
                        if (attribute != null) {
                            nestedEntity = attribute.getComplexValue();
                        } else {
                            nestedEntity = new CmsEntity(nestedSettingAttributeName + "[1]", formatter.getId());
                            entity.addAttributeValue(nestedSettingAttributeName, nestedEntity);
                        }
                        nestedEntity.addAttributeValue(
                            getSettingsAttributeName(settingEntry.getKey()),
                            settingEntry.getValue());
                        nested = true;
                        break;
                    }
                }
            }
            if (!nested) {
                entity.addAttributeValue(getSettingsAttributeName(settingEntry.getKey()), settingEntry.getValue());
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

        boolean performedAutoCorrection = content.isTransformedVersion();
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
     * Creates a new resource to edit, delegating to an edit handler if edit handler data is passed in.<p>
     *
     * @param newLink A string, specifying where which new content should be created.
     * @param locale The locale for which the
     * @param referenceSitePath site path of the currently edited content.
     * @param modelFileSitePath site path of the model file
     * @param mode optional creation mode
     * @param postCreateHandler optional class name of an {@link I_CmsCollectorPostCreateHandler} which is invoked after the content has been created.
     * @param editHandlerData edit handler data (will be null if no edit handler is configured or the edit handler does not handle 'new')
     *
     * @return The site-path of the newly created resource.
     * @throws CmsException if something goes wrong
     */
    private String createResourceToEdit(
        String newLink,
        Locale locale,
        String referenceSitePath,
        String modelFileSitePath,
        String mode,
        String postCreateHandler,
        CmsEditHandlerData editHandlerData)
    throws CmsException {

        CmsObject cms = getCmsObject();
        HttpServletRequest request = getRequest();
        if (editHandlerData != null) {
            CmsContainerpageService containerpageService = new CmsContainerpageService();
            containerpageService.setCms(cms);
            containerpageService.setRequest(request);
            CmsResource page = cms.readResource(editHandlerData.getPageContextId(), CmsResourceFilter.ALL);
            CmsContainerElementBean elementBean = containerpageService.getCachedElement(
                editHandlerData.getClientId(),
                page.getRootPath());
            Map<String, String[]> params = CmsRequestUtil.createParameterMap(
                CmsEncoder.decode(editHandlerData.getRequestParams()),
                true,
                CmsEncoder.ENCODING_UTF_8);
            elementBean.initResource(cms);
            CmsResource elementResource = elementBean.getResource();
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(elementResource);
            if (type instanceof CmsResourceTypeXmlContent) {
                CmsResourceTypeXmlContent xmlType = (CmsResourceTypeXmlContent)type;
                I_CmsEditHandler handler = xmlType.getEditHandler(cms);
                if (handler != null) {
                    return handler.handleNew(
                        cms,
                        newLink,
                        locale,
                        referenceSitePath,
                        modelFileSitePath,
                        postCreateHandler,
                        elementBean,
                        editHandlerData.getPageContextId(),
                        params,
                        editHandlerData.getOption());

                } else {
                    LOG.warn(
                        "Invalid state: edit handler data passed in, but edit handler is undefined. type = "
                            + type.getTypeName());
                }
            } else {
                LOG.warn("Invalid state: edit handler data passed in for non-XML content type.");
            }
        }
        return defaultCreateResourceToEdit(
            cms,
            newLink,
            locale,
            referenceSitePath,
            modelFileSitePath,
            mode,
            postCreateHandler);
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
            for (String elementPath : content.getContentDefinition().getContentHandler().getSynchronizations(
                true).getSynchronizationPaths()) {
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
                                int pathLevelDiff = (CmsResource.getPathLevel(valuePath)
                                    - CmsResource.getPathLevel(elementPath)) + 1;
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
     * Returns the change handler scopes.<p>
     *
     * @param definition the content definition
     *
     * @return the scopes
     */
    private Set<String> getChangeHandlerScopes(CmsXmlContentDefinition definition) {

        List<I_CmsXmlContentEditorChangeHandler> changeHandlers = definition.getContentHandler().getEditorChangeHandlers(
            false);
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

        CmsXmlContent content = null;
        if (fromCache) {
            content = getSessionCache().getCacheXmlContent(file.getStructureId());
        }
        if (content == null) {
            content = CmsXmlContentFactory.unmarshal(getCmsObject(), file);
            getSessionCache().setCacheXmlContent(file.getStructureId(), content);
        }
        CmsContentTypeVisitor visitor = new CmsContentTypeVisitor(getCmsObject(), file, Locale.ENGLISH);
        visitor.visitTypes(content.getContentDefinition(), Locale.ENGLISH);
        CmsDynamicCategoryFieldList dynCatFields = visitor.getOptionalDynamicCategoryFields();
        dynCatFields.ensureFields(getCmsObject(), content);
        return content;
    }

    /**
     * Returns the value that has to be set for the dynamic attribute.
     *
     * @param file the file where the current content is stored
     * @param value the content value that is represented by the attribute
     * @param attributeName the attribute's name
     * @param editedLocalEntity the entities that where edited last
     * @return the value that has to be set for the dynamic attribute.
     */
    private String getDynamicAttributeValue(
        CmsFile file,
        I_CmsXmlContentValue value,
        String attributeName,
        CmsEntity editedLocalEntity) {

        if ((null != editedLocalEntity) && (editedLocalEntity.getAttribute(attributeName) != null)) {
            getSessionCache().setDynamicValue(
                attributeName,
                editedLocalEntity.getAttribute(attributeName).getSimpleValue());
        }
        String currentValue = getSessionCache().getDynamicValue(attributeName);
        if (null != currentValue) {
            return currentValue;
        }
        if (null != file) {
            if (value.getTypeName().equals(CmsXmlDynamicCategoryValue.TYPE_NAME)) {
                List<CmsCategory> categories = new ArrayList<CmsCategory>(0);
                try {
                    categories = CmsCategoryService.getInstance().readResourceCategories(getCmsObject(), file);
                } catch (CmsException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERROR_FAILED_READING_CATEGORIES_1), e);
                }
                I_CmsWidget widget = null;
                widget = CmsWidgetUtil.collectWidgetInfo(getCmsObject(), value).getWidget();
                if ((null != widget) && (widget instanceof CmsCategoryWidget)) {
                    String mainCategoryPath = ((CmsCategoryWidget)widget).getStartingCategory(
                        getCmsObject(),
                        getCmsObject().getSitePath(file));
                    StringBuffer pathes = new StringBuffer();
                    for (CmsCategory category : categories) {
                        if (category.getPath().startsWith(mainCategoryPath)) {
                            String path = category.getBasePath() + category.getPath();
                            path = getCmsObject().getRequestContext().removeSiteRoot(path);
                            pathes.append(path).append(',');
                        }
                    }
                    String dynamicConfigString = pathes.length() > 0 ? pathes.substring(0, pathes.length() - 1) : "";
                    getSessionCache().setDynamicValue(attributeName, dynamicConfigString);
                    return dynamicConfigString;
                }
            } else if (value.getTypeName().equals(CmsXmlAccessRestrictionValue.TYPE_NAME)) {
                CmsAccessRestrictionInfo restrictionInfo = CmsAccessRestrictionInfo.getRestrictionInfo(
                    getCmsObject(),
                    value.getDocument().getContentDefinition());
                if (restrictionInfo != null) {
                    try {
                        List<CmsAccessControlEntry> aces = getCmsObject().getAccessControlEntries(
                            getCmsObject().getSitePath(value.getDocument().getFile()));
                        boolean hasEntry = aces.stream().anyMatch(
                            ace -> ace.getPrincipal().equals(restrictionInfo.getGroup().getId())
                                && ace.isResponsible());
                        return "" + hasEntry;
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    return "false";
                }
            }
        }
        return "";

    }

    /**
     * Returns the formatter configuration for the given container element bean.<p>
     *
     * @param containerElement the container element
     *
     * @return the formatter configuration
     */
    private I_CmsFormatterBean getFormatterForElement(
        CmsADEConfigData config,
        CmsContainerElementBean containerElement) {

        if ((containerElement != null)
            && (containerElement.getFormatterId() != null)
            && !containerElement.getFormatterId().isNullUUID()) {
            CmsUUID formatterId = containerElement.getFormatterId();

            // find formatter config setting
            for (Entry<String, String> settingEntry : containerElement.getIndividualSettings().entrySet()) {
                if (settingEntry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                    String formatterConfigId = settingEntry.getValue();
                    I_CmsFormatterBean dynamicFmt = config.findFormatter(formatterConfigId);
                    if ((dynamicFmt != null) && dynamicFmt.getJspStructureId().equals(formatterId)) {
                        return dynamicFmt;
                    }
                }
            }
        }
        return null;
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
            String pathElement = getAttributeName(ancestor);
            pathElements.add(pathElement + "[" + valueIndex + "]");
            if (ancestor.isChoiceType()) {
                pathElements.add("ATTRIBUTE_CHOICE");
            }
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
     * Returns the attribute name to use for the given setting name.<p>
     *
     * @param settingName the setting name
     *
     * @return the attribute name
     */
    private String getSettingsAttributeName(String settingName) {

        return "/" + SETTINGS_ATTRIBUTE_NAME_PREFIX + settingName;
    }

    /**
     * Transforms the widget configuration.<p>
     * @param settingsWidget the settings widget name
     * @param settingsConfig the setting widget configuration
     * @param messages the workplace messages
     * @param contentLocale the content locale
     *
     * @return the client widget configuration string
     */
    private String getWidgetConfig(
        String settingsWidget,
        String settingsConfig,
        CmsMessages messages,
        Locale contentLocale) {

        Class<? extends I_CmsADEWidget> widgetClass = WIDGET_MAPPINGS.get(settingsWidget);
        String config = "";
        if (widgetClass == null) {
            widgetClass = CmsInputWidget.class;
        }
        try {
            I_CmsADEWidget widget = widgetClass.newInstance();
            widget.setConfiguration(settingsConfig);
            config = widget.getConfiguration(getCmsObject(), null, messages, null, contentLocale);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return config;
    }

    /**
     * Returns the widget class name.<p>
     *
     * @param settingsWidget the settings widget name
     *
     * @return the widget class name
     */
    private String getWidgetName(String settingsWidget) {

        if (WIDGET_MAPPINGS.containsKey(settingsWidget)) {
            return WIDGET_MAPPINGS.get(settingsWidget).getName();
        } else {
            return CmsInputWidget.class.getName();
        }
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
     * Checks whether the given attribute name indicates it is a settings attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return <code>true</code> in case of settings attributes
     */
    private boolean isSettingsAttribute(String attributeName) {

        return attributeName.startsWith("/" + SETTINGS_ATTRIBUTE_NAME_PREFIX);
    }

    private CmsADEConfigData readConfig(CmsUUID pageId) {

        if (pageId == null) {
            return null;
        }
        try {
            CmsResource resource = getCmsObject().readResource(pageId, CmsResourceFilter.IGNORE_EXPIRATION);
            return OpenCms.getADEManager().lookupConfiguration(getCmsObject(), resource.getRootPath());
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Reads the content definition for the given resource and locale.<p>
     *
     * @param file the resource file
     * @param content the XML content
     * @param entityId the entity id
     * @param clientId the container element client id if available
     * @param locale the content locale
     * @param newLocale if the locale content should be created as new
     * @param mainLocale the main language to copy in case the element language node does not exist yet
     * @param editedLocaleEntity the edited locale entity
     * @param settingPresets the presets for settings
     * @param configData the sitemap configuration to use
     *
     * @return the content definition
     *
     * @throws CmsException if something goes wrong
     */
    private CmsContentDefinition readContentDefinition(
        CmsFile file,
        CmsXmlContent content,
        String entityId,
        String clientId,
        Locale locale,
        boolean newLocale,
        Locale mainLocale,
        CmsEntity editedLocaleEntity,
        Map<String, String> settingPresets,
        CmsADEConfigData configData)
    throws CmsException {

        long timer = 0;
        if (LOG.isDebugEnabled()) {
            timer = System.currentTimeMillis();
        }
        CmsObject cms = getCmsObject();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        List<Locale> availableLocalesList = OpenCms.getLocaleManager().getAvailableLocales(cms, file);
        if (!availableLocalesList.contains(locale)) {
            availableLocalesList.retainAll(content.getLocales());
            List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, file);
            Locale replacementLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
                locale,
                defaultLocales,
                availableLocalesList);
            LOG.info(
                "Can't edit locale "
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
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_TAKE_UNMARSHALING_TIME_1,
                    "" + (System.currentTimeMillis() - timer)));
        }
        CmsContentTypeVisitor visitor = new CmsContentTypeVisitor(cms, file, locale);
        if (LOG.isDebugEnabled()) {
            timer = System.currentTimeMillis();
        }
        visitor.visitTypes(content.getContentDefinition(), getWorkplaceLocale(cms));
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
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
            if ((mainLocale != null) && content.hasLocale(mainLocale)) {
                content.copyLocale(mainLocale, locale);
            } else {
                content.addLocale(cms, locale);
            }
            // sync the locale values
            if (!visitor.getLocaleSynchronizations().isEmpty() && (content.getLocales().size() > 1)) {
                for (Locale contentLocale : content.getLocales()) {
                    if (!contentLocale.equals(locale)) {
                        content.synchronizeLocaleIndependentValues(cms, skipPaths, contentLocale);
                    }
                }
            }
        }
        visitor.getOptionalDynamicCategoryFields().ensureFields(cms, content, locale);
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
            false,
            editedLocaleEntity);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
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

        Map<String, CmsAttributeConfiguration> attrConfig = visitor.getAttributeConfigurations();
        Map<String, CmsType> types = visitor.getTypes();
        List<CmsTabInfo> tabInfos = visitor.getTabInfos();

        if (clientId != null) {
            CmsContainerElementBean containerElement = getSessionCache().getCacheContainerElement(clientId);
            I_CmsFormatterBean formatter = getFormatterForElement(configData, containerElement);
            if ((formatter != null)
                && formatter.isAllowsSettingsInEditor()
                && !formatter.getSettings(config).isEmpty()) {
                Map<String, CmsXmlContentProperty> settingsConfig = OpenCms.getADEManager().getFormatterSettings(
                    cms,
                    config,
                    formatter,
                    containerElement.getResource(),
                    locale,
                    getRequest());
                com.google.common.base.Supplier<CmsXmlContent> contentSupplier = Suppliers.memoize(() -> {
                    try {
                        return CmsXmlContentFactory.unmarshal(cms, cms.readFile(containerElement.getResource()));
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                });
                settingsConfig = CmsXmlContentPropertyHelper.resolveMacrosForPropertyInfo(
                    cms,
                    null,
                    containerElement.getResource(),
                    contentSupplier,
                    CmsElementUtil.createStringTemplateSource(formatter, contentSupplier),
                    settingsConfig);
                CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(m_workplaceLocale);
                List<I_CmsFormatterBean> nestedFormatters = formatter.hasNestedFormatterSettings()
                ? OpenCms.getADEManager().getNestedFormatters(
                    cms,
                    config,
                    containerElement.getResource(),
                    locale,
                    getRequest())
                : Collections.emptyList();
                String firstContentAttributeName = types.get(
                    entity.getTypeName()).getAttributeNames().iterator().next();
                List<String> addedVisibleAttrs = addSettingsAttributes(
                    attrConfig,
                    settingsConfig,
                    nestedFormatters,
                    messages,
                    locale,
                    settingPresets);
                addSettingsTypes(entity.getTypeName(), types, settingsConfig, nestedFormatters);
                if (editedLocaleEntity != null) {
                    transferSettingValues(editedLocaleEntity, entity);
                } else {
                    addSettingsValues(entity, containerElement, nestedFormatters);
                }
                if (tabInfos.isEmpty()) {
                    tabInfos.add(
                        new CmsTabInfo(
                            Messages.get().getBundle(workplaceLocale).key(Messages.GUI_CONTENT_TAB_LABEL_0),
                            Messages.GUI_CONTENT_TAB_LABEL_0,
                            Messages.GUI_CONTENT_TAB_LABEL_0,
                            "content",
                            firstContentAttributeName.substring(entity.getTypeName().length() + 1),
                            false,
                            null,
                            null,
                            null));
                }
                if (addedVisibleAttrs.size() > 0) {
                    tabInfos.add(
                        new CmsTabInfo(
                            Messages.get().getBundle(workplaceLocale).key(Messages.GUI_SETTINGS_TAB_LABEL_0),
                            Messages.GUI_SETTINGS_TAB_LABEL_0,
                            Messages.GUI_SETTINGS_TAB_LABEL_0,
                            CmsContentDefinition.SETTINGS_TAB_ID,
                            CmsFileUtil.removeLeadingSeparator(addedVisibleAttrs.iterator().next()),
                            false,
                            Messages.get().getBundle(workplaceLocale).key(Messages.GUI_SETTINGS_TAB_DESCRIPTION_0),
                            null,
                            null));
                }
            }

        }

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
            CmsIconUtil.getIconClasses(CmsIconUtil.getDisplayType(cms, file), file.getName(), false),
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
     * @param editHandlerData the edit handler data, in case the 'new' function is handled by an edit handler
     * @param settingPresets the presets for settings
     *
     * @return the content definition
     *
     * @throws CmsException if creating the resource failed
     */
    private CmsContentDefinition readContentDefinitionForNew(
        String newLink,
        CmsResource referenceResource,
        CmsUUID modelFileId,
        Locale locale,
        String mode,
        String postCreateHandler,
        CmsEditHandlerData editHandlerData,
        Map<String, String> settingPresets)
    throws CmsException {

        String sitePath = getCmsObject().getSitePath(referenceResource);
        String resourceType;
        if (newLink.startsWith(CmsJspTagEdit.NEW_LINK_IDENTIFIER)) {
            resourceType = CmsJspTagEdit.getTypeFromNewLink(newLink);
        } else {
            resourceType = OpenCms.getResourceManager().getResourceType(referenceResource.getTypeId()).getTypeName();
        }
        String modelFile = null;
        if (modelFileId == null) {
            List<CmsResource> modelResources = CmsResourceTypeXmlContent.getModelFiles(
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
        String newFileName = createResourceToEdit(
            newLink,
            locale,
            sitePath,
            modelFile,
            mode,
            postCreateHandler,
            editHandlerData);
        CmsResource resource = getCmsObject().readResource(newFileName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFile file = getCmsObject().readFile(resource);
        CmsXmlContent content = getContentDocument(file, false);
        CmsContentDefinition contentDefinition = readContentDefinition(
            file,
            content,
            null,
            null,
            locale,
            false,
            null,
            null,
            settingPresets,
            null);
        contentDefinition.setDeleteOnCancel(true);
        return contentDefinition;
    }

    /**
     * Stores the settings attributes to the container element bean persisted in the session cache.<p>
     * The container page editor will write the container page XML.<p>
     *
     * @param entity the entity
     * @param containerElement the container element
     * @param settingsConfig the settings configuration
     * @param nestedFormatters the nested formatters
     *
     * @return <code>true</code> in case any changed settings where saved
     */
    @SuppressWarnings("null")
    private boolean saveSettings(
        CmsEntity entity,
        CmsContainerElementBean containerElement,
        Map<String, CmsXmlContentProperty> settingsConfig,
        List<I_CmsFormatterBean> nestedFormatters) {

        boolean hasChangedSettings = false;
        Map<String, String> values = new HashMap<>(containerElement.getIndividualSettings());
        for (Entry<String, CmsXmlContentProperty> settingsEntry : settingsConfig.entrySet()) {
            String value = null;
            boolean nested = false;
            if (nestedFormatters != null) {
                for (I_CmsFormatterBean formatter : nestedFormatters) {
                    if (settingsEntry.getKey().startsWith(formatter.getId())) {

                        CmsEntity nestedEntity = null;
                        CmsEntityAttribute attribute = entity.getAttribute(getSettingsAttributeName(formatter.getId()));
                        if (attribute != null) {
                            nestedEntity = attribute.getComplexValue();
                            CmsEntityAttribute valueAttribute = nestedEntity.getAttribute(
                                getSettingsAttributeName(settingsEntry.getKey()));
                            if (valueAttribute != null) {
                                value = valueAttribute.getSimpleValue();
                            }
                        }
                        nested = true;
                        break;
                    }
                }
            }
            if (!nested) {
                CmsEntityAttribute valueAttribute = entity.getAttribute(
                    getSettingsAttributeName(settingsEntry.getKey()));
                if (valueAttribute != null) {
                    value = valueAttribute.getSimpleValue();
                }
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)
                && !CmsGwtConstants.HIDDEN_SETTINGS_WIDGET_NAME.equals(settingsEntry.getValue().getWidget())
                && values.containsKey(settingsEntry.getKey())) {
                values.remove(settingsEntry.getKey());
                hasChangedSettings = true;
            } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)
                && !CmsGwtConstants.HIDDEN_SETTINGS_WIDGET_NAME.equals(settingsEntry.getValue().getWidget())
                && !value.equals(values.get(settingsEntry.getKey()))) {
                values.put(settingsEntry.getKey(), value);
                hasChangedSettings = true;
            }
        }
        if (hasChangedSettings) {
            containerElement.updateIndividualSettings(values);
            getSessionCache().setCacheContainerElement(containerElement.editorHash(), containerElement);
        }
        return hasChangedSettings;
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
        CmsEntity entity)
    throws CmsXmlException {

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
                    true,
                    entity);
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
     * Transfers settings attribute values from one entity to another.<p>
     *
     * @param source the source entity
     * @param target the target entity
     */
    private void transferSettingValues(CmsEntity source, CmsEntity target) {

        for (CmsEntityAttribute attr : source.getAttributes()) {
            if (isSettingsAttribute(attr.getAttributeName())) {
                if (attr.isSimpleValue()) {
                    target.addAttributeValue(attr.getAttributeName(), attr.getSimpleValue());
                } else {
                    CmsEntity nestedSource = attr.getComplexValue();

                    CmsEntity nested = new CmsEntity(nestedSource.getId(), nestedSource.getTypeName());
                    for (CmsEntityAttribute nestedAttr : nestedSource.getAttributes()) {
                        nested.addAttributeValue(nestedAttr.getAttributeName(), nestedAttr.getSimpleValue());
                    }
                    target.addAttributeValue(attr.getAttributeName(), nested);
                }
            }
        }
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

        return validateContent(cms, structureId, content, null);
    }

    /**
     * Validates the given XML content.<p>
     *
     * @param cms the cms context
     * @param structureId the structure id
     * @param content the XML content
     * @param fieldNames if not null, only validation errors in paths from this set will be added to the validation result
     *
     * @return the validation result
     */
    private CmsValidationResult validateContent(
        CmsObject cms,
        CmsUUID structureId,
        CmsXmlContent content,
        Set<String> fieldNames) {

        CmsXmlContentErrorHandler errorHandler = content.validate(cms);
        Map<String, Map<String[], CmsPair<String, String>>> errorsByEntity = new HashMap<String, Map<String[], CmsPair<String, String>>>();

        if (errorHandler.hasErrors()) {
            boolean reallyHasErrors = false;
            for (Entry<Locale, Map<String, String>> localeEntry : errorHandler.getErrors().entrySet()) {
                Map<String[], CmsPair<String, String>> errors = new HashMap<String[], CmsPair<String, String>>();
                for (Entry<String, String> error : localeEntry.getValue().entrySet()) {
                    I_CmsXmlContentValue value = content.getValue(error.getKey(), localeEntry.getKey());
                    if ((fieldNames == null) || fieldNames.contains(value.getPath())) {
                        errors.put(
                            getPathElements(content, value),
                            new CmsPair<String, String>(error.getValue(), error.getKey()));
                        reallyHasErrors = true;
                    }

                }
                if (reallyHasErrors) {
                    errorsByEntity.put(
                        CmsContentDefinition.uuidToEntityId(structureId, localeEntry.getKey().toString()),
                        errors);
                }
            }
        }
        Map<String, Map<String[], CmsPair<String, String>>> warningsByEntity = new HashMap<String, Map<String[], CmsPair<String, String>>>();
        if (errorHandler.hasWarnings()) {
            boolean reallyHasErrors = false;
            for (Entry<Locale, Map<String, String>> localeEntry : errorHandler.getWarnings().entrySet()) {
                Map<String[], CmsPair<String, String>> warnings = new HashMap<String[], CmsPair<String, String>>();
                for (Entry<String, String> warning : localeEntry.getValue().entrySet()) {
                    I_CmsXmlContentValue value = content.getValue(warning.getKey(), localeEntry.getKey());
                    if ((fieldNames == null) || fieldNames.contains(value.getPath())) {
                        warnings.put(
                            getPathElements(content, value),
                            new CmsPair<String, String>(warning.getValue(), warning.getKey()));
                        reallyHasErrors = true;
                    }
                }
                if (reallyHasErrors) {
                    warningsByEntity.put(
                        CmsContentDefinition.uuidToEntityId(structureId, localeEntry.getKey().toString()),
                        warnings);
                }
            }
        }
        return new CmsValidationResult(errorsByEntity, warningsByEntity);
    }

    /**
     * Validates the settings attribute values.<p>
     *
     * @param entity the entity to validate
     * @param validationResult the validation result
     * @param settingsConfig the settings configuration
     */
    private void validateSettings(
        CmsEntity entity,
        CmsValidationResult validationResult,
        Map<String, CmsXmlContentProperty> settingsConfig) {

        Map<String, Map<String[], CmsPair<String, String>>> errors = validationResult.getErrors();
        Map<String[], CmsPair<String, String>> entityErrors = errors.get(entity.getId());
        if (entityErrors == null) {
            entityErrors = new HashMap<String[], CmsPair<String, String>>();
        }
        Map<String, Map<String[], CmsPair<String, String>>> warnings = validationResult.getWarnings();
        Map<String[], CmsPair<String, String>> entityWarnings = warnings.get(entity.getId());
        if (entityWarnings == null) {
            entityWarnings = new HashMap<String[], CmsPair<String, String>>();
        }

        for (CmsEntityAttribute attribute : entity.getAttributes()) {
            if (isSettingsAttribute(attribute.getAttributeName())) {
                if (attribute.isSimpleValue()) {
                    String settingsKey = attribute.getAttributeName().substring(
                        SETTINGS_ATTRIBUTE_NAME_PREFIX.length() + 1);
                    CmsXmlContentProperty prop = settingsConfig.get(settingsKey);
                    if (prop != null) {
                        String regex = prop.getRuleRegex();
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(regex)) {
                            if (!attribute.getSimpleValue().matches(regex)) {
                                String[] path = new String[] {attribute.getAttributeName()};

                                if (SETTINGS_RULE_TYPE_ERROR.equals(prop.getRuleType())) {
                                    entityErrors.put(
                                        path,
                                        new CmsPair<String, String>(prop.getError(), prop.getNiceName()));
                                } else {
                                    entityWarnings.put(
                                        path,
                                        new CmsPair<String, String>(prop.getError(), prop.getNiceName()));
                                }
                            }
                        }
                    }
                } else {
                    CmsEntity nested = attribute.getComplexValue();
                    for (CmsEntityAttribute nestedAttribute : nested.getAttributes()) {
                        String settingsKey = nestedAttribute.getAttributeName().substring(
                            SETTINGS_ATTRIBUTE_NAME_PREFIX.length() + 1);
                        CmsXmlContentProperty prop = settingsConfig.get(settingsKey);
                        if (prop != null) {
                            String regex = prop.getRuleRegex();
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(regex)) {
                                if (!nestedAttribute.getSimpleValue().matches(regex)) {
                                    String[] path = new String[] {
                                        attribute.getAttributeName(),
                                        nestedAttribute.getAttributeName()};
                                    if (SETTINGS_RULE_TYPE_ERROR.equals(prop.getRuleType())) {
                                        entityErrors.put(
                                            path,
                                            new CmsPair<String, String>(prop.getError(), prop.getNiceName()));
                                    } else {
                                        entityWarnings.put(
                                            path,
                                            new CmsPair<String, String>(prop.getError(), prop.getNiceName()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!entityErrors.isEmpty()) {
            errors.put(entity.getId(), entityErrors);
        }
        if (!entityWarnings.isEmpty()) {
            warnings.put(entity.getId(), entityWarnings);
        }

    }

    /**
     * Writes the categories that are dynamically read/wrote by the content editor.
     *
     * @param file the file where the content is stored.
     * @param content the content.
     * @param lastEditedEntity the last edited entity
     */
    private void writeCategories(CmsFile file, CmsXmlContent content, CmsEntity lastEditedEntity) {

        // do nothing if one of the arguments is empty.
        if ((null == content) || (null == file)) {
            return;
        }

        CmsObject cms = getCmsObject();
        if (!content.getLocales().isEmpty()) {
            Locale locale = content.getLocales().iterator().next();
            CmsEntity entity = lastEditedEntity;
            List<I_CmsXmlContentValue> values = content.getValues(locale);
            for (I_CmsXmlContentValue value : values) {
                if (value.getTypeName().equals(CmsXmlDynamicCategoryValue.TYPE_NAME)) {
                    I_CmsWidget widget = CmsWidgetUtil.collectWidgetInfo(cms, value).getWidget();
                    List<CmsCategory> categories = new ArrayList<CmsCategory>(0);
                    try {
                        categories = CmsCategoryService.getInstance().readResourceCategories(cms, file);
                    } catch (CmsException e) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERROR_FAILED_READING_CATEGORIES_1), e);
                    }
                    if ((null != widget) && (widget instanceof CmsCategoryWidget)) {
                        String mainCategoryPath = ((CmsCategoryWidget)widget).getStartingCategory(
                            cms,
                            cms.getSitePath(file));
                        for (CmsCategory category : categories) {
                            if (category.getPath().startsWith(mainCategoryPath)) {
                                try {
                                    CmsCategoryService.getInstance().removeResourceFromCategory(
                                        cms,
                                        cms.getSitePath(file),
                                        category);
                                } catch (CmsException e) {
                                    LOG.error(e.getLocalizedMessage(), e);
                                }
                            }
                        }
                        if (null == entity) {
                            try {
                                CmsContentDefinition definition = readContentDefinition(
                                    file,
                                    content,
                                    "dummy",
                                    null,
                                    locale,
                                    false,
                                    null,
                                    null,
                                    Collections.emptyMap(),
                                    null);
                                entity = definition.getEntity();
                            } catch (CmsException e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                        String checkedCategories = "";
                        if (null != entity) {
                            checkedCategories = CmsEntity.getValueForPath(entity, value.getPath().split("/"));
                        }
                        List<String> checkedCategoryList = Arrays.asList(checkedCategories.split(","));
                        for (String category : checkedCategoryList) {
                            try {
                                CmsCategoryService.getInstance().addResourceToCategory(
                                    cms,
                                    cms.getSitePath(file),
                                    CmsCategoryService.getInstance().getCategory(cms, category));
                            } catch (CmsException e) {
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn(e.getLocalizedMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
        }
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
            throw new CmsException(
                org.opencms.workplace.editors.Messages.get().container(
                    org.opencms.workplace.editors.Messages.ERR_INVALID_CONTENT_ENC_1,
                    file.getRootPath()),
                e);
        }
        // the file content might have been modified during the write operation

        cms.getRequestContext().setAttribute(ATTR_EDITOR_SAVING, "true");
        try {
            file = cms.writeFile(file);
        } finally {
            cms.getRequestContext().removeAttribute(ATTR_EDITOR_SAVING);
        }
        return CmsXmlContentFactory.unmarshal(cms, file);
    }

}
