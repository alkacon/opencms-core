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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsFormatterUtils;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElement.ModelGroupState;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
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
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles all model group specific tasks.<p>
 */
public class CmsModelGroupHelper {

    /** The name of the container storing the groups base element. */
    public static final String MODEL_GROUP_BASE_CONTAINER = "base_container";

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsModelGroupHelper.class);

    /** Settings to keep when resetting. */
    private static final String[] KEEP_SETTING_IDS = new String[] {
        CmsContainerElement.MODEL_GROUP_STATE,
        CmsContainerElement.ELEMENT_INSTANCE_ID,
        CmsContainerElement.USE_AS_COPY_MODEL};

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

        CmsResourceTypeConfig typeConfig = configData.getResourceType(
            CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
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

        return CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME.equals(
            OpenCms.getResourceManager().getResourceType(resource).getTypeName());
    }

    /**
     * Updates a model group resource to the changed data structure.<p>
     * This step is necessary when updating from version 10.0.x to 10.5.x.<p>
     *
     * @param cms the cms context
     * @param group the model group resource
     * @param baseContainerName the new base container name
     *
     * @return <code>true</code> if the resource was updated
     */
    public static boolean updateModelGroupResource(CmsObject cms, CmsResource group, String baseContainerName) {

        if (!isModelGroupResource(group)) {
            // skip resources that are no model group
            return false;
        }
        try {
            CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(cms, group);
            CmsContainerPageBean pageBean = xmlContainerPage.getContainerPage(cms);

            CmsContainerBean baseContainer = pageBean.getContainers().get(MODEL_GROUP_BASE_CONTAINER);
            boolean changedContent = false;
            if ((baseContainer != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(baseContainerName)) {

                List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
                for (CmsContainerBean container : pageBean.getContainers().values()) {
                    if (container.getName().equals(MODEL_GROUP_BASE_CONTAINER)) {
                        CmsContainerBean replacer = new CmsContainerBean(
                            baseContainerName,
                            container.getType(),
                            container.getParentInstanceId(),
                            container.isRootContainer(),
                            container.getElements());
                        containers.add(replacer);
                        changedContent = true;
                    } else {
                        containers.add(container);
                    }
                }
                if (changedContent) {
                    pageBean = new CmsContainerPageBean(containers);
                }
            }
            if (changedContent) {
                ensureLock(cms, group);

                if (changedContent) {
                    xmlContainerPage.save(cms, pageBean);
                }
                if (group.getName().endsWith(".xml")) {
                    // renaming model groups so they will be rendered correctly by the browser
                    String targetPath = cms.getSitePath(group);
                    targetPath = targetPath.substring(0, targetPath.length() - 4) + ".html";
                    cms.renameResource(cms.getSitePath(group), targetPath);
                    group = cms.readResource(group.getStructureId());
                }
                tryUnlock(cms, group);
                return true;
            }
            return false;

        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }

    }

    /**
     * Updates model group resources to the changed data structure.<p>
     * This step is necessary when updating from version 10.0.x to 10.5.x.<p>
     *
     * @param request the request
     * @param response the response
     * @param basePath the path to the model group, or the base path to search for model groups
     * @param baseContainerName the new base container name
     *
     * @throws IOException in case writing to the response fails
     */
    @SuppressWarnings("resource")
    public static void updateModelGroupResources(
        HttpServletRequest request,
        HttpServletResponse response,
        String basePath,
        String baseContainerName)
    throws IOException {

        if (CmsFlexController.isCmsRequest(request)) {

            try {
                CmsFlexController controller = CmsFlexController.getController(request);
                CmsObject cms = controller.getCmsObject();
                CmsResource base = cms.readResource(basePath);
                List<CmsResource> resources;
                I_CmsResourceType groupType = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
                if (base.isFolder()) {
                    resources = cms.readResources(
                        basePath,
                        CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(groupType));
                } else if (OpenCms.getResourceManager().getResourceType(base).equals(groupType)) {
                    resources = Collections.singletonList(base);
                } else {
                    resources = Collections.emptyList();
                }

                if (resources.isEmpty()) {
                    response.getWriter().println("No model group resources found at " + CmsEncoder.escapeXml(basePath) + "<br />");
                } else {
                    for (CmsResource group : resources) {
                        boolean updated = updateModelGroupResource(cms, group, baseContainerName);
                        response.getWriter().println(
                            "Group '" + group.getRootPath() + "' was updated " + updated + "<br />");
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                e.printStackTrace(response.getWriter());
            }
        }
    }

    /**
     * Locks the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource to lock
     *
     * @throws CmsException in case locking fails
     */
    private static void ensureLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
        } else if (!lock.isOwnedInProjectBy(user, cms.getRequestContext().getCurrentProject())) {
            cms.changeLock(resource);
        }
    }

    /**
     * Tries to unlock a resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource to unlock
     */
    private static void tryUnlock(CmsObject cms, CmsResource resource) {

        try {
            cms.unlockResource(resource);
        } catch (CmsException e) {
            LOG.debug("Unable to unlock " + resource.getRootPath(), e);
        }
    }

    /**
     * Adds the model group elements to the page.<p>
     *
     * @param elements the requested elements
     * @param foundGroups list to add the found group element client ids to
     * @param page the page
     * @param alwaysCopy <code>true</code> to create element copies in case of non model groups and createNew is set
     * @param locale the content locale
     * @param createContextPath the context path to pass to CmsResourceTypeConfig#createNewElement
     *
     * @return the adjusted page
     *
     * @throws CmsException in case something goes wrong
     */
    public CmsContainerPageBean prepareforModelGroupContent(
        Map<String, CmsContainerElementBean> elements,
        List<String> foundGroups,
        CmsContainerPageBean page,
        boolean alwaysCopy,
        Locale locale,
        String createContextPath)
    throws CmsException {

        for (Entry<String, CmsContainerElementBean> entry : elements.entrySet()) {
            CmsContainerElementBean element = entry.getValue();
            CmsContainerPageBean modelPage = null;
            String modelInstanceId = null;
            boolean foundInstance = false;
            if (CmsModelGroupHelper.isModelGroupResource(element.getResource())) {
                modelPage = getContainerPageBean(element.getResource());
                CmsContainerElementBean baseElement = getModelBaseElement(modelPage, element.getResource());
                if (baseElement == null) {
                    break;
                }
                String baseInstanceId = baseElement.getInstanceId();
                String originalInstanceId = element.getInstanceId();
                element = getModelReplacementElement(element, baseElement, true);
                List<CmsContainerBean> modelContainers = readModelContainers(
                    baseInstanceId,
                    originalInstanceId,
                    modelPage,
                    baseElement.isCopyModel());
                if (!m_isEditingModelGroups && baseElement.isCopyModel()) {
                    modelContainers = createNewElementsForModelGroup(m_cms, modelContainers, locale, createContextPath);
                }
                modelContainers.addAll(page.getContainers().values());
                page = new CmsContainerPageBean(modelContainers);
                // update the entry element value, as the settings will have changed
                entry.setValue(element);
                if (m_sessionCache != null) {
                    // also update the session cache
                    m_sessionCache.setCacheContainerElement(element.editorHash(), element);
                }
            } else {
                // here we need to make sure to remove the source container page setting and to set a new element instance id

                Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
                String source = settings.get(CmsContainerpageService.SOURCE_CONTAINERPAGE_ID_SETTING);
                settings.remove(CmsContainerpageService.SOURCE_CONTAINERPAGE_ID_SETTING);
                // TODO: Make sure source id is available for second call

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

                        boolean copyRoot = false;
                        if (alwaysCopy && (modelInstanceId != null) && (modelPage != null)) {
                            for (CmsContainerElementBean el : modelPage.getElements()) {
                                if (modelInstanceId.equals(el.getInstanceId())) {
                                    copyRoot = el.isCreateNew();
                                    break;
                                }
                            }
                        }

                        if (copyRoot) {
                            CmsObject cloneCms = OpenCms.initCmsObject(m_cms);
                            cloneCms.getRequestContext().setLocale(locale);
                            String typeName = OpenCms.getResourceManager().getResourceType(
                                element.getResource()).getTypeName();
                            CmsResourceTypeConfig typeConfig = m_configData.getResourceType(typeName);
                            if (typeConfig == null) {
                                throw new IllegalArgumentException(
                                    "Can not copy template model element '"
                                        + element.getResource().getRootPath()
                                        + "' because the resource type '"
                                        + typeName
                                        + "' is not available in this sitemap.");
                            }
                            CmsResource newResource = typeConfig.createNewElement(
                                cloneCms,
                                element.getResource(),
                                createContextPath);

                            element = new CmsContainerElementBean(
                                newResource.getStructureId(),
                                element.getFormatterId(),
                                settings,
                                false);
                        } else {
                            element = CmsContainerElementBean.cloneWithSettings(element, settings);
                        }
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
                                    containerByParent,
                                    true);
                                if (alwaysCopy) {
                                    modelContainers = createNewElementsForModelGroup(
                                        m_cms,
                                        modelContainers,
                                        locale,
                                        createContextPath);
                                }
                                foundGroups.add(element.editorHash());
                                modelContainers.addAll(page.getContainers().values());
                                page = new CmsContainerPageBean(modelContainers);
                            }
                        }

                        // update the entry element value, as the settings will have changed
                        entry.setValue(element);
                        if (m_sessionCache != null) {
                            // also update the session cache
                            m_sessionCache.setCacheContainerElement(element.editorHash(), element);
                        }
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
                        CmsContainerElementBean baseElement = getModelBaseElement(
                            modelGroupPage,
                            element.getResource());
                        if (baseElement == null) {
                            LOG.error(
                                "Error rendering model group '"
                                    + element.getResource().getRootPath()
                                    + "', base element could not be determind.");
                            continue;
                        }
                        String baseInstanceId = baseElement.getInstanceId();
                        CmsContainerElementBean replaceElement = getModelReplacementElement(
                            element,
                            baseElement,
                            false);
                        if (m_sessionCache != null) {
                            m_sessionCache.setCacheContainerElement(replaceElement.editorHash(), replaceElement);
                        }
                        elements.add(replaceElement);
                        resultContainers.addAll(
                            readModelContainers(
                                baseInstanceId,
                                element.getInstanceId(),
                                modelGroupPage,
                                baseElement.isCopyModel()));
                    } else {
                        elements.add(element);
                    }
                } catch (CmsException e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
            if (hasModels) {
                resultContainers.add(container.copyWithNewElements(elements));
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
                        CmsUUID modelId = new CmsUUID(
                            element.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_ID));
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
                containers.add(container.copyWithNewElements(elements));
            }
        }
        return new CmsContainerPageBean(containers);
    }

    /**
     * Saves the model groups of the given container page.<p>
     *
     * @param page the container page
     * @param pageResource the model group resource
     *
     * @return the container page referencing the saved model groups
     *
     * @throws CmsException in case writing the page properties fails
     */
    public CmsContainerPageBean saveModelGroups(CmsContainerPageBean page, CmsResource pageResource)
    throws CmsException {

        CmsUUID modelElementId = null;
        CmsContainerElementBean baseElement = null;
        for (CmsContainerElementBean element : page.getElements()) {
            if (element.isModelGroup()) {
                modelElementId = element.getId();
                baseElement = element;
                break;

            }
        }
        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : page.getContainers().values()) {
            List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
            boolean hasChanges = false;
            for (CmsContainerElementBean element : container.getElements()) {
                if (element.isModelGroup() && !element.getId().equals(modelElementId)) {
                    // there should not be another model group element, remove the model group settings
                    Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
                    settings.remove(CmsContainerElement.MODEL_GROUP_ID);
                    settings.remove(CmsContainerElement.MODEL_GROUP_STATE);
                    elements.add(
                        new CmsContainerElementBean(element.getId(), element.getFormatterId(), settings, false));
                    hasChanges = true;
                } else {
                    elements.add(element);
                }
            }
            if (hasChanges) {
                containers.add(container.copyWithNewElements(elements));
            } else {
                containers.add(container);
            }

        }

        List<CmsProperty> changedProps = new ArrayList<CmsProperty>();
        if (baseElement != null) {
            String val = Boolean.parseBoolean(
                baseElement.getIndividualSettings().get(CmsContainerElement.USE_AS_COPY_MODEL))
                ? CmsContainerElement.USE_AS_COPY_MODEL
                : "";
            changedProps.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS, val, val));
        }
        m_cms.writePropertyObjects(pageResource, changedProps);

        return new CmsContainerPageBean(containers);

    }

    /**
     * Adjusts formatter settings and initializes a new instance id for the given container element.<p>
     *
     * @param element the container element
     * @param originalContainer the original parent container name
     * @param adjustedContainer the target container name
     * @param setNewInstanceId <code>true</code> to set a new instance id
     *
     * @return the new element instance
     */
    private CmsContainerElementBean adjustSettings(
        CmsContainerElementBean element,
        String originalContainer,
        String adjustedContainer,
        boolean setNewInstanceId) {

        Map<String, String> settings = new HashMap<String, String>(element.getIndividualSettings());
        if (setNewInstanceId) {
            settings.put(CmsContainerElement.ELEMENT_INSTANCE_ID, new CmsUUID().toString());
        }
        String formatterId = settings.remove(CmsFormatterConfig.getSettingsKeyForContainer(originalContainer));
        settings.put(CmsFormatterConfig.getSettingsKeyForContainer(adjustedContainer), formatterId);
        return CmsContainerElementBean.cloneWithSettings(element, settings);
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
     * @param isCopyGroup <code>true</code> in case of a copy group
     *
     * @return the collected containers
     */
    private List<CmsContainerBean> collectModelStructure(
        String modelInstanceId,
        String replaceModelId,
        Map<String, List<CmsContainerBean>> containerByParent,
        boolean isCopyGroup) {

        List<CmsContainerBean> result = new ArrayList<CmsContainerBean>();

        if (containerByParent.containsKey(modelInstanceId)) {
            for (CmsContainerBean container : containerByParent.get(modelInstanceId)) {
                String adjustedContainerName = replaceModelId + container.getName().substring(modelInstanceId.length());

                List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                for (CmsContainerElementBean element : container.getElements()) {
                    CmsContainerElementBean copyElement = adjustSettings(
                        element,
                        container.getName(),
                        adjustedContainerName,
                        isCopyGroup);
                    if (m_sessionCache != null) {
                        m_sessionCache.setCacheContainerElement(copyElement.editorHash(), copyElement);
                    }
                    elements.add(copyElement);
                    result.addAll(
                        collectModelStructure(
                            element.getInstanceId(),
                            copyElement.getInstanceId(),
                            containerByParent,
                            isCopyGroup));
                }

                result.add(
                    new CmsContainerBean(
                        adjustedContainerName,
                        container.getType(),
                        replaceModelId,
                        container.isRootContainer(),
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
     * @param createContext the context path to pass to CmsResourceTypeConfig#createNewElement
     *
     * @return the updated model containers
     *
     * @throws CmsException in case something goes wrong
     */
    private List<CmsContainerBean> createNewElementsForModelGroup(
        CmsObject cms,
        List<CmsContainerBean> modelContainers,
        Locale locale,
        String createContext)
    throws CmsException {

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
                        throw new IllegalArgumentException(
                            "Can not copy template model element '"
                                + element.getResource().getRootPath()
                                + "' because the resource type '"
                                + typeName
                                + "' is not available in this sitemap.");
                    }
                    CmsResource newResource = typeConfig.createNewElement(
                        cloneCms,
                        element.getResource(),
                        createContext);
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
                CmsContainerBean updatedContainer = container.copyWithNewElements(updatedElements);
                updatedContainers.add(updatedContainer);
            }
            modelContainers = updatedContainers;
        }
        return modelContainers;
    }

    /**
     * Collects the page containers by parent instance id.<p>
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
     * @param modelGroupResource the model group resource
     *
     * @return the base element
     */
    private CmsContainerElementBean getModelBaseElement(
        CmsContainerPageBean modelGroupPage,
        CmsResource modelGroupResource) {

        CmsContainerElementBean result = null;
        for (CmsContainerElementBean element : modelGroupPage.getElements()) {
            if (CmsContainerElement.ModelGroupState.isModelGroup.name().equals(
                element.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_STATE))) {
                result = element;
                break;
            }
        }
        return result;
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

        boolean resetSettings = false;
        if (!baseElement.isCopyModel() && hasIncompatibleFormatters(baseElement, element)) {
            I_CmsFormatterBean formatter = m_configData.findFormatter(element.getFormatterId());
            resetSettings = (formatter == null)
                || !formatter.getResourceTypeNames().contains(
                    OpenCms.getResourceManager().getResourceType(baseElement.getResource()).getTypeName());
        }
        Map<String, String> settings;
        if (resetSettings) {
            settings = new HashMap<String, String>();
            for (String id : KEEP_SETTING_IDS) {
                if (element.getIndividualSettings().containsKey(id)) {
                    settings.put(id, element.getIndividualSettings().get(id));
                }
            }
            settings.put(CmsContainerElement.MODEL_GROUP_ID, element.getId().toString());
            // transfer all other settings
            for (Entry<String, String> settingEntry : baseElement.getIndividualSettings().entrySet()) {
                if (!settings.containsKey(settingEntry.getKey())) {
                    settings.put(settingEntry.getKey(), settingEntry.getValue());
                }
            }
        } else {
            settings = new HashMap<String, String>(element.getIndividualSettings());
            if (!(baseElement.isCopyModel() && allowCopyModel)) {
                // skip the model id in case of copy models
                settings.put(CmsContainerElement.MODEL_GROUP_ID, element.getId().toString());
                if (allowCopyModel) {
                    // transfer all other settings
                    for (Entry<String, String> settingEntry : baseElement.getIndividualSettings().entrySet()) {
                        if (!settings.containsKey(settingEntry.getKey())) {
                            settings.put(settingEntry.getKey(), settingEntry.getValue());
                        }
                    }
                }

            } else if (baseElement.isCopyModel()) {
                settings.put(CmsContainerElement.MODEL_GROUP_STATE, ModelGroupState.wasModelGroup.name());
            }
        }
        return CmsContainerElementBean.cloneWithSettings(baseElement, settings);
    }

    private boolean hasIncompatibleFormatters(CmsContainerElementBean baseElement, CmsContainerElementBean element) {

        if ((baseElement == null) || (element == null)) {
            return false;
        }
        if ((baseElement.getFormatterId() != null)
            && (element.getFormatterId() != null)
            && !baseElement.getFormatterId().equals(element.getFormatterId())) {
            return true;
        }
        Set<String> baseFormatterKeys = CmsFormatterUtils.getAllFormatterKeys(m_configData, baseElement);
        Set<String> elementFormatterKeys = CmsFormatterUtils.getAllFormatterKeys(m_configData, element);
        boolean hasCommonKeys = baseFormatterKeys.stream().anyMatch(elementFormatterKeys::contains);
        return !hasCommonKeys;
    }

    /**
     * Returns the model containers.<p>
     *
     * @param modelInstanceId the model instance id
     * @param localInstanceId the local instance id
     * @param modelPage the model page bean
     * @param isCopyGroup <code>true</code> in case of a copy group
     *
     * @return the model group containers
     */
    private List<CmsContainerBean> readModelContainers(
        String modelInstanceId,
        String localInstanceId,
        CmsContainerPageBean modelPage,
        boolean isCopyGroup) {

        Map<String, List<CmsContainerBean>> containerByParent = getContainerByParent(modelPage);
        List<CmsContainerBean> modelContainers;
        if (containerByParent.containsKey(modelInstanceId)) {
            modelContainers = collectModelStructure(modelInstanceId, localInstanceId, containerByParent, isCopyGroup);
        } else {
            modelContainers = new ArrayList<CmsContainerBean>();
        }
        return modelContainers;
    }
}
