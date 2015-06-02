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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Handles all model group specific tasks.<p>
 */
public class CmsModelGroupHelper {

    /** The name of the container storing the groups base element. */
    public static final String MODEL_GROUP_BASE_CONTAINER = "base_container";

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsModelGroupHelper.class);

    /** The current cms context. */
    private CmsObject m_cms;

    /** The session cache instance. */
    private CmsADESessionCache m_sessionCache;

    /** The configuration data of the current container page location. */
    private CmsADEConfigData m_configData;

    /** Indicating the edit model groups mode. */
    private boolean m_isEditingModelGroups;

    /**
     * Constructor.<p>
     * 
     * @param cms the current cms context
     * @param configData the configuration data
     * @param sessionCache the session cache
     * @param isEditingModelGroups the edit model groups flag
     */
    public CmsModelGroupHelper(
        CmsObject cms,
        CmsADEConfigData configData,
        CmsADESessionCache sessionCache,
        boolean isEditingModelGroups) {

        m_cms = cms;
        m_sessionCache = sessionCache;
        m_configData = configData;
        m_isEditingModelGroups = isEditingModelGroups;
    }

    /**
     * Creates a new model group resource.<p>
     * 
     * @param cms the current cms context
     * @param configData the configuration data
     * 
     * @return the new resource
     * 
     * @throws CmsException in case creating the resource fails
     */
    public static CmsResource createModelGroup(CmsObject cms, CmsADEConfigData configData) throws CmsException {

        CmsResourceTypeConfig typeConfig = configData.getResourceType(CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
        return typeConfig.createNewElement(cms, configData.getBasePath());
    }

    /**
     * Returns if the given resource is a model group resource.<p>
     * 
     * @param resource the resource
     * 
     * @return <code>true</code> if the given resource is a model group resource
     */
    public static boolean isModelGroupResource(CmsResource resource) {

        return CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME.equals(OpenCms.getResourceManager().getResourceType(
            resource).getTypeName());
    }

    /**
     * Adds the model group elements to the page.<p>
     * 
     * @param elements the requested elements
     * @param page the page
     * @param locale the content locale
     * 
     * @return the adjusted page
     * 
     * @throws CmsException in case something goes wrong
     */
    public CmsContainerPageBean prepareforModelGroupContent(
        Map<String, CmsContainerElementBean> elements,
        CmsContainerPageBean page,
        Locale locale) throws CmsException {

        for (Entry<String, CmsContainerElementBean> entry : elements.entrySet()) {
            CmsContainerElementBean element = entry.getValue();
            CmsContainerPageBean modelPage = null;
            String modelInstanceId = null;
            boolean foundInstance = false;
            if (CmsModelGroupHelper.isModelGroupResource(element.getResource())) {
                modelPage = getContainerPageBean(element.getResource());
                CmsContainerElementBean baseElement = getModelBaseElement(modelPage);
                if (baseElement == null) {
                    break;
                }
                String baseInstanceId = baseElement.getInstanceId();
                String originalInstanceId = element.getInstanceId();
                element = getModelReplacementElement(element, baseElement, true);
                List<CmsContainerBean> modelContainers = readModelContainers(
                    baseInstanceId,
                    originalInstanceId,
                    modelPage);
                if (!m_isEditingModelGroups && baseElement.isCopyModel()) {
                    modelContainers = createNewElementsForModelGroup(m_cms, modelContainers, locale);
                }
                modelContainers.addAll(page.getContainers().values());
                page = new CmsContainerPageBean(modelContainers);
                // update the entry element value, as the settings will have changed
                entry.setValue(element);
                // also update the session cache
                m_sessionCache.setCacheContainerElement(element.editorHash(), element);
            } else {
                // here we need to make sure to remove the source container page setting and to set a new element instance id

                Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
                String source = settings.get(CmsContainerpageService.SOURCE_CONTAINERPAGE_ID_SETTING);
                settings.remove(CmsContainerpageService.SOURCE_CONTAINERPAGE_ID_SETTING);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(source)) {
                    try {
                        CmsUUID sourceId = new CmsUUID(source);
                        CmsResource sourcePage = m_cms.readResource(sourceId);
                        if (CmsResourceTypeXmlContainerPage.isContainerPage(sourcePage)) {
                            CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(
                                m_cms,
                                m_cms.readFile(sourcePage));
                            modelPage = xmlCnt.getContainerPage(m_cms);
                            modelInstanceId = element.getInstanceId();
                        }

                        settings.remove(CmsContainerElement.ELEMENT_INSTANCE_ID);
                        element = CmsContainerElementBean.cloneWithSettings(element, settings);
                        if (modelPage != null) {
                            Map<String, List<CmsContainerBean>> containerByParent = new HashMap<String, List<CmsContainerBean>>();

                            for (CmsContainerBean container : modelPage.getContainers().values()) {
                                if (container.getParentInstanceId() != null) {
                                    if (!containerByParent.containsKey(container.getParentInstanceId())) {
                                        containerByParent.put(
                                            container.getParentInstanceId(),
                                            new ArrayList<CmsContainerBean>());
                                    }
                                    containerByParent.get(container.getParentInstanceId()).add(container);
                                }
                                if (!foundInstance) {
                                    for (CmsContainerElementBean child : container.getElements()) {
                                        if (modelInstanceId == null) {
                                            if (child.getId().equals(element.getId())) {
                                                modelInstanceId = child.getInstanceId();
                                                foundInstance = true;
                                                // we also want to keep the settings of the model group
                                                Map<String, String> setting = new HashMap<String, String>(
                                                    child.getIndividualSettings());
                                                setting.remove(CmsContainerElement.ELEMENT_INSTANCE_ID);
                                                element = CmsContainerElementBean.cloneWithSettings(element, setting);
                                                break;
                                            }
                                        } else {
                                            if (modelInstanceId.equals(child.getInstanceId())) {
                                                foundInstance = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (foundInstance && containerByParent.containsKey(modelInstanceId)) {
                                List<CmsContainerBean> modelContainers = collectModelStructure(
                                    modelInstanceId,
                                    element.getInstanceId(),
                                    containerByParent);
                                modelContainers = createNewElementsForModelGroup(m_cms, modelContainers, locale);
                                modelContainers.addAll(page.getContainers().values());
                                page = new CmsContainerPageBean(modelContainers);
                            }
                        }

                        // update the entry element value, as the settings will have changed
                        entry.setValue(element);
                        // also update the session cache
                        m_sessionCache.setCacheContainerElement(element.editorHash(), element);
                    } catch (Exception e) {
                        LOG.info(e.getLocalizedMessage(), e);
                    }

                }
            }

        }
        return page;
    }

    /**
     * Reads the present model groups and merges their containers into the page.<p>
     * 
     * @param page the container page
     * 
     * @return the resulting container page
     */
    public CmsContainerPageBean readModelGroups(CmsContainerPageBean page) {

        List<CmsContainerBean> resultContainers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : page.getContainers().values()) {
            boolean hasModels = false;
            List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
            for (CmsContainerElementBean element : container.getElements()) {
                try {
                    element.initResource(m_cms);
                    if (isModelGroupResource(element.getResource())) {
                        hasModels = true;
                        CmsContainerPageBean modelGroupPage = getContainerPageBean(element.getResource());
                        CmsContainerElementBean baseElement = getModelBaseElement(modelGroupPage);
                        if (baseElement == null) {
                            break;
                        }
                        String baseInstanceId = baseElement.getInstanceId();
                        CmsContainerElementBean replaceElement = getModelReplacementElement(element, baseElement, false);
                        m_sessionCache.setCacheContainerElement(replaceElement.editorHash(), replaceElement);
                        elements.add(replaceElement);
                        resultContainers.addAll(readModelContainers(
                            baseInstanceId,
                            element.getInstanceId(),
                            modelGroupPage));
                    } else {
                        elements.add(element);
                    }
                } catch (CmsException e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
            if (hasModels) {
                resultContainers.add(new CmsContainerBean(
                    container.getName(),
                    container.getType(),
                    container.getParentInstanceId(),
                    container.getMaxElements(),
                    elements));
            } else {
                resultContainers.add(container);
            }
        }
        return new CmsContainerPageBean(resultContainers);
    }

    /**
     * Removes the model group containers.<p>
     * 
     * @param page the container page state
     * 
     * @return the container page without the model group containers
     */
    public CmsContainerPageBean removeModelGroupContainers(CmsContainerPageBean page) {

        Map<String, List<CmsContainerBean>> containersByParent = getContainerByParent(page);
        Set<String> modelInstances = new HashSet<String>();
        for (CmsContainerElementBean element : page.getElements()) {
            if (element.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_ID)) {
                modelInstances.add(element.getInstanceId());
            }
        }

        Set<String> descendingInstances = new HashSet<String>();
        for (String modelInstance : modelInstances) {
            descendingInstances.addAll(collectDescendingInstances(modelInstance, containersByParent));
        }
        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : page.getContainers().values()) {
            if ((container.getParentInstanceId() == null)
                || !descendingInstances.contains(container.getParentInstanceId())) {
                // iterate the container elements to replace the model group elements
                List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                for (CmsContainerElementBean element : container.getElements()) {
                    if (modelInstances.contains(element.getInstanceId())) {
                        CmsUUID modelId = new CmsUUID(element.getIndividualSettings().get(
                            CmsContainerElement.MODEL_GROUP_ID));
                        CmsContainerElementBean replacer = new CmsContainerElementBean(
                            modelId,
                            element.getFormatterId(),
                            element.getIndividualSettings(),
                            false);
                        elements.add(replacer);
                    } else {
                        elements.add(element);
                    }
                }
                containers.add(new CmsContainerBean(
                    container.getName(),
                    container.getType(),
                    container.getParentInstanceId(),
                    container.getMaxElements(),
                    elements));

            }
        }
        return new CmsContainerPageBean(containers);
    }

    /**
     * Saves the model groups of the given container page.<p>
     * 
     * @param page the container page
     * 
     * @return the container page referencing the saved model groups
     */
    public CmsContainerPageBean saveModelGroups(CmsContainerPageBean page) {

        Map<String, List<CmsContainerBean>> containersByParent = getContainerByParent(page);
        Map<String, String> modelInstances = new HashMap<String, String>();
        Set<String> descendingInstances = new HashSet<String>();
        for (CmsContainerElementBean element : page.getElements()) {
            String modelGroupId = null;
            if (element.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_ID)
                || Boolean.valueOf(element.getIndividualSettings().get(CmsContainerElement.IS_MODEL_GROUP)).booleanValue()) {
                modelGroupId = element.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_ID);
                modelInstances.put(element.getInstanceId(), modelGroupId);
                Set<String> childInstances = collectDescendingInstances(element.getInstanceId(), containersByParent);
                descendingInstances.addAll(childInstances);
                CmsResource modelGroup = null;
                try {
                    modelGroup = m_cms.readResource(new CmsUUID(modelGroupId));
                    ensureLock(modelGroup);
                    String title = element.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_TITLE);
                    String description = element.getIndividualSettings().get(
                        CmsContainerElement.MODEL_GROUP_DESCRIPTION);
                    List<CmsProperty> props = new ArrayList<CmsProperty>();
                    props.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, title));
                    props.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, description, description));
                    m_cms.writePropertyObjects(modelGroup, props);
                    List<CmsContainerBean> modelContainers = new ArrayList<CmsContainerBean>();
                    CmsContainerElementBean baseElement = element.clone();
                    CmsContainerBean baseContainer = new CmsContainerBean(
                        MODEL_GROUP_BASE_CONTAINER,
                        MODEL_GROUP_BASE_CONTAINER,
                        null,
                        Collections.singletonList(baseElement));
                    modelContainers.add(baseContainer);
                    for (String childInstance : childInstances) {
                        if (containersByParent.containsKey(childInstance)) {
                            modelContainers.addAll(containersByParent.get(childInstance));
                        }
                    }
                    CmsContainerPageBean modelPage = new CmsContainerPageBean(modelContainers);
                    CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(m_cms, m_cms.readFile(modelGroup));
                    xmlCnt.save(m_cms, modelPage);
                    tryUnlock(modelGroup);
                } catch (CmsException e) {
                    LOG.error("Error saving model group resource.", e);
                }
            }
        }
        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : page.getContainers().values()) {
            if ((container.getParentInstanceId() == null)
                || !descendingInstances.contains(container.getParentInstanceId())) {
                // iterate the container elements to replace the model group elements
                List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                for (CmsContainerElementBean element : container.getElements()) {
                    if (modelInstances.containsKey(element.getInstanceId())) {
                        CmsUUID modelId = new CmsUUID(modelInstances.get(element.getInstanceId()));
                        CmsContainerElementBean replacer = new CmsContainerElementBean(
                            modelId,
                            element.getFormatterId(),
                            element.getIndividualSettings(),
                            false);
                        elements.add(replacer);
                    } else {
                        elements.add(element);
                    }
                }
                containers.add(new CmsContainerBean(
                    container.getName(),
                    container.getType(),
                    container.getParentInstanceId(),
                    container.getMaxElements(),
                    elements));

            }
        }
        return new CmsContainerPageBean(containers);

    }

    /**
     * Returns the descending instance id's to the given element instance.<p>
     * 
     * @param instanceId the instance id
     * @param containersByParent the container page containers by parent instance id
     * 
     * @return the containers
     */
    private Set<String> collectDescendingInstances(
        String instanceId,
        Map<String, List<CmsContainerBean>> containersByParent) {

        Set<String> descendingInstances = new HashSet<String>();
        descendingInstances.add(instanceId);
        if (containersByParent.containsKey(instanceId)) {
            for (CmsContainerBean container : containersByParent.get(instanceId)) {
                for (CmsContainerElementBean element : container.getElements()) {
                    descendingInstances.addAll(collectDescendingInstances(element.getInstanceId(), containersByParent));
                }
            }
        }
        return descendingInstances;
    }

    /**
     * Collects the model group structure.<p>
     * 
     * @param modelInstanceId the model instance id
     * @param replaceModelId the local instance id
     * @param containerByParent the model group page containers by parent instance id
     * 
     * @return the collected containers
     */
    private List<CmsContainerBean> collectModelStructure(
        String modelInstanceId,
        String replaceModelId,
        Map<String, List<CmsContainerBean>> containerByParent) {

        List<CmsContainerBean> result = new ArrayList<CmsContainerBean>();

        if (containerByParent.containsKey(modelInstanceId)) {
            for (CmsContainerBean container : containerByParent.get(modelInstanceId)) {
                List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                for (CmsContainerElementBean element : container.getElements()) {
                    CmsContainerElementBean copyElement = initNewInstanceId(element);
                    m_sessionCache.setCacheContainerElement(copyElement.editorHash(), copyElement);
                    elements.add(copyElement);
                    result.addAll(collectModelStructure(
                        element.getInstanceId(),
                        copyElement.getInstanceId(),
                        containerByParent));
                }
                String adjustedName = replaceModelId + container.getName().substring(modelInstanceId.length());
                result.add(new CmsContainerBean(
                    adjustedName,
                    container.getType(),
                    replaceModelId,
                    container.getMaxElements(),
                    elements));
            }
        }
        return result;
    }

    /**
     * Creates new resources for elements marked with create as new.<p>
     * 
     * @param cms the cms context
     * @param modelContainers the model containers
     * @param locale the content locale
     * 
     * @return the updated model containers
     * 
     * @throws CmsException in case something goes wrong
     */
    private List<CmsContainerBean> createNewElementsForModelGroup(
        CmsObject cms,
        List<CmsContainerBean> modelContainers,
        Locale locale) throws CmsException {

        Map<CmsUUID, CmsResource> newResources = new HashMap<CmsUUID, CmsResource>();
        CmsObject cloneCms = OpenCms.initCmsObject(cms);
        cloneCms.getRequestContext().setLocale(locale);
        for (CmsContainerBean container : modelContainers) {
            for (CmsContainerElementBean element : container.getElements()) {
                if (element.isCreateNew() && !newResources.containsKey(element.getId())) {
                    element.initResource(cms);
                    String typeName = OpenCms.getResourceManager().getResourceType(element.getResource()).getTypeName();
                    CmsResourceTypeConfig typeConfig = m_configData.getResourceType(typeName);
                    if (typeConfig == null) {
                        throw new IllegalArgumentException("Can not copy template model element '"
                            + element.getResource().getRootPath()
                            + "' because the resource type '"
                            + typeName
                            + "' is not available in this sitemap.");
                    }
                    CmsResource newResource = typeConfig.createNewElement(
                        cloneCms,
                        element.getResource(),
                        m_configData.getBasePath());
                    newResources.put(element.getId(), newResource);
                }
            }
        }
        if (!newResources.isEmpty()) {
            List<CmsContainerBean> updatedContainers = new ArrayList<CmsContainerBean>();
            for (CmsContainerBean container : modelContainers) {
                List<CmsContainerElementBean> updatedElements = new ArrayList<CmsContainerElementBean>();
                for (CmsContainerElementBean element : container.getElements()) {
                    if (newResources.containsKey(element.getId())) {
                        CmsContainerElementBean newBean = new CmsContainerElementBean(
                            newResources.get(element.getId()).getStructureId(),
                            element.getFormatterId(),
                            element.getIndividualSettings(),
                            false);
                        updatedElements.add(newBean);
                    } else {
                        updatedElements.add(element);
                    }
                }
                CmsContainerBean updatedContainer = new CmsContainerBean(
                    container.getName(),
                    container.getType(),
                    container.getParentInstanceId(),
                    container.getMaxElements(),
                    updatedElements);
                updatedContainers.add(updatedContainer);
            }
            modelContainers = updatedContainers;
        }
        return modelContainers;
    }

    /**
     * Locks the given resource.<p>
     * 
     * @param resource the resource to lock
     * 
     * @throws CmsException in case locking fails
     */
    private void ensureLock(CmsResource resource) throws CmsException {

        CmsUser user = m_cms.getRequestContext().getCurrentUser();
        CmsLock lock = m_cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            m_cms.lockResourceTemporary(resource);
        } else if (!lock.isOwnedInProjectBy(user, m_cms.getRequestContext().getCurrentProject())) {
            m_cms.changeLock(resource);
        }
    }

    /**
     * Collects the page containers by parent instance id
     * 
     * @param page the page
     * 
     * @return the containers by parent id
     */
    private Map<String, List<CmsContainerBean>> getContainerByParent(CmsContainerPageBean page) {

        Map<String, List<CmsContainerBean>> containerByParent = new HashMap<String, List<CmsContainerBean>>();

        for (CmsContainerBean container : page.getContainers().values()) {
            if (container.getParentInstanceId() != null) {
                if (!containerByParent.containsKey(container.getParentInstanceId())) {
                    containerByParent.put(container.getParentInstanceId(), new ArrayList<CmsContainerBean>());
                }
                containerByParent.get(container.getParentInstanceId()).add(container);
            }
        }
        return containerByParent;
    }

    /**
     * Unmarshals the given resource.<p>
     * 
     * @param resource the resource
     * 
     * @return the container page bean
     * 
     * @throws CmsException in case unmarshalling fails
     */
    private CmsContainerPageBean getContainerPageBean(CmsResource resource) throws CmsException {

        CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(m_cms, m_cms.readFile(resource));
        return xmlCnt.getContainerPage(m_cms);
    }

    /**
     * Returns the model group base element.<p>
     * 
     * @param modelGroupPage the model group page
     * 
     * @return the base element
     */
    private CmsContainerElementBean getModelBaseElement(CmsContainerPageBean modelGroupPage) {

        CmsContainerBean container = modelGroupPage.getContainers().get(MODEL_GROUP_BASE_CONTAINER);
        return container != null ? container.getElements().get(0) : null;
    }

    /**
     * Returns the the element to be rendered as the model group base.<p>
     * 
     * @param element the original element
     * @param baseElement the model group base
     * @param allowCopyModel if copy models are allowed
     * 
     * @return the element
     */
    private CmsContainerElementBean getModelReplacementElement(
        CmsContainerElementBean element,
        CmsContainerElementBean baseElement,
        boolean allowCopyModel) {

        Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
        if (m_isEditingModelGroups || !(baseElement.isCopyModel() && allowCopyModel)) {
            // skip the model id in case of copy models
            settings.put(CmsContainerElement.MODEL_GROUP_ID, element.getId().toString());
            try {
                CmsProperty titleProp = m_cms.readPropertyObject(
                    element.getResource(),
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false);
                settings.put(CmsContainerElement.MODEL_GROUP_TITLE, titleProp.getValue());
                CmsProperty descProp = m_cms.readPropertyObject(
                    element.getResource(),
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    false);
                settings.put(CmsContainerElement.MODEL_GROUP_DESCRIPTION, descProp.getValue());
            } catch (CmsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return CmsContainerElementBean.cloneWithSettings(baseElement, settings);
    }

    /**
     * Initializes a new instance id for the given container element.<p>
     * 
     * @param element the container element
     * 
     * @return the new element instance
     */
    private CmsContainerElementBean initNewInstanceId(CmsContainerElementBean element) {

        Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
        settings.put(CmsContainerElement.ELEMENT_INSTANCE_ID, new CmsUUID().toString());
        return CmsContainerElementBean.cloneWithSettings(element, settings);
    }

    /**
     * Returns the model containers.<p>
     * 
     * @param modelInstanceId the model instance id
     * @param localInstanceId the local instance id
     * @param modelPage the model page bean
     * 
     * @return the model group containers
     */
    private List<CmsContainerBean> readModelContainers(
        String modelInstanceId,
        String localInstanceId,
        CmsContainerPageBean modelPage) {

        Map<String, List<CmsContainerBean>> containerByParent = getContainerByParent(modelPage);
        List<CmsContainerBean> modelContainers;
        if (containerByParent.containsKey(modelInstanceId)) {
            modelContainers = collectModelStructure(modelInstanceId, localInstanceId, containerByParent);
        } else {
            modelContainers = Collections.emptyList();
        }
        return modelContainers;
    }

    /**
     * Tries to unlock a resource.<p>
     * 
     * @param resource the resource to unlock
     */
    private void tryUnlock(CmsResource resource) {

        try {
            m_cms.unlockResource(resource);
        } catch (CmsException e) {
            LOG.debug("Unable to unlock " + resource.getRootPath(), e);
        }
    }
}
