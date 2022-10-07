/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.postupload;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.postupload.shared.CmsPostUploadDialogBean;
import org.opencms.ade.postupload.shared.CmsPostUploadDialogPanelBean;
import org.opencms.ade.postupload.shared.I_CmsDialogConstants;
import org.opencms.ade.postupload.shared.rpc.I_CmsPostUploadDialogService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsPropertyEditorHelper;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.collect.Iterables;

/**
 * The service implementation for the org.opencms.ade.postupload module.<p>
 */
public class CmsPostUploadDialogService extends CmsGwtService implements I_CmsPostUploadDialogService {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPostUploadDialogService.class);

    /**
     * Creates a new instance.<p>
     */
    public CmsPostUploadDialogService() {

        super();
    }

    /**
     * Fetches the dialog data.<p>
     *
     * @param request the servlet request
     *
     * @return the dialog data
     * @throws CmsRpcException if something goes wrong
     */
    public static CmsPostUploadDialogBean prefetch(HttpServletRequest request) throws CmsRpcException {

        CmsPostUploadDialogService srv = new CmsPostUploadDialogService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsPostUploadDialogBean result = null;
        try {
            result = srv.prefetch();
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * @see org.opencms.ade.postupload.shared.rpc.I_CmsPostUploadDialogService#load(org.opencms.util.CmsUUID, boolean,boolean)
     */
    public CmsPostUploadDialogPanelBean load(CmsUUID id, boolean useConfiguration, boolean addBasicProperties)
    throws CmsRpcException {

        try {
            CmsResource res = getCmsObject().readResource(id);
            List<CmsProperty> properties = getCmsObject().readPropertyObjects(res, false);
            String title = CmsProperty.get(CmsPropertyDefinition.PROPERTY_TITLE, properties).getValue();
            if (title == null) {
                title = res.getName();
            }
            String description = CmsProperty.get(CmsPropertyDefinition.PROPERTY_DESCRIPTION, properties).getValue();
            if (description == null) {
                description = getCmsObject().getSitePath(res);
            }
            CmsListInfoBean listInfo = CmsVfsService.getPageInfo(getCmsObject(), res);

            CmsPostUploadDialogPanelBean result = new CmsPostUploadDialogPanelBean(id, listInfo);
            String warning = OpenCms.getADEManager().getUploadWarningTable().getMessage(res.getStructureId());
            if (warning != null) {
                Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
                CmsMacroResolver resolver = new CmsMacroResolver();
                resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(wpLocale));
                warning = resolver.resolveMacros(warning);
                result.setWarning(warning);
            }

            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            String typeName = type.getTypeName();
            listInfo.setResourceType(typeName);

            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);

            List<String> defaultProperties = settings.getProperties();
            while (properties.isEmpty() && !CmsStringUtil.isEmptyOrWhitespaceOnly(settings.getReference())) {
                settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(settings.getReference());
                defaultProperties = settings.getProperties();
            }

            Map<String, CmsXmlContentProperty> propertyDefinitions = new LinkedHashMap<String, CmsXmlContentProperty>();
            Map<String, CmsClientProperty> clientProperties = new LinkedHashMap<String, CmsClientProperty>();

            // add the file name to the list of properties to allow renaming the uploaded file
            CmsXmlContentProperty fileNamePropDef = new CmsXmlContentProperty(
                CmsPropertyModification.FILE_NAME_PROPERTY,
                "string",
                "string",
                "",
                "",
                "",
                "",
                Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject())).key(
                    Messages.GUI_UPLOAD_FILE_NAME_0),
                "",
                "",
                "false");
            propertyDefinitions.put(CmsPropertyModification.FILE_NAME_PROPERTY, fileNamePropDef);
            clientProperties.put(
                CmsPropertyModification.FILE_NAME_PROPERTY,
                new CmsClientProperty(CmsPropertyModification.FILE_NAME_PROPERTY, res.getName(), res.getName()));

            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                getCmsObject(),
                res.getRootPath());
            Map<String, CmsXmlContentProperty> propertyConfiguration = configData.getPropertyConfigurationAsMap();

            Set<String> propertiesToShow = new LinkedHashSet<String>();
            propertiesToShow.addAll(defaultProperties);
            if (addBasicProperties) {
                propertiesToShow.addAll(propertyConfiguration.keySet());
            }
            Set<String> requiredProperties = getRequiredProperties(getCmsObject(), res);
            for (String propertyName : propertiesToShow) {
                CmsXmlContentProperty propDef = null;
                if (useConfiguration) {
                    propDef = propertyConfiguration.get(propertyName);
                }
                if (propDef == null) {
                    propDef = new CmsXmlContentProperty(
                        propertyName,
                        "string",
                        "string",
                        "",
                        "",
                        "",
                        "",
                        null,
                        "",
                        "",
                        "false");
                }
                if (requiredProperties.contains(propertyName)) {
                    String validationErrorMessage = Messages.get().getBundle(
                        OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject())).key(
                            Messages.GUI_POSTUPLOAD_REQUIRED_PROPERTY_1,
                            propertyName);
                    propDef = propDef.withValidation(".*?[^ ].*", "error", validationErrorMessage);
                }

                propertyDefinitions.put(propertyName, propDef);
                CmsProperty property = CmsProperty.get(propertyName, properties);
                if (property != null) {
                    CmsClientProperty clientProperty = new CmsClientProperty(
                        propertyName,
                        property.getStructureValue(),
                        property.getResourceValue());
                    clientProperties.put(clientProperty.getName(), clientProperty);
                }
            }

            propertyDefinitions = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
                propertyDefinitions,
                CmsMacroResolver.newWorkplaceLocaleResolver(getCmsObject()));

            CmsPropertyEditorHelper.updateWysiwygConfig(propertyDefinitions, getCmsObject(), res);

            result.setPropertyDefinitions(propertyDefinitions);
            result.setProperties(clientProperties);
            return result;
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * @see org.opencms.ade.postupload.shared.rpc.I_CmsPostUploadDialogService#prefetch()
     */
    public CmsPostUploadDialogBean prefetch() throws CmsRpcException {

        try {

            List<CmsResource> resources = new ArrayList<>();

            if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                getRequest().getParameter(I_CmsDialogConstants.PARAM_RESOURCES)))) {
                // if the request parameter resources exists and contains a list of UUIDs
                // this dialog is used as upload hook
                String resourcesParam = getRequest().getParameter(I_CmsDialogConstants.PARAM_RESOURCES);
                List<String> resourceUUIDs = CmsStringUtil.splitAsList(resourcesParam, ",");
                for (String uuidAsString : resourceUUIDs) {
                    CmsUUID uuid = new CmsUUID(uuidAsString);
                    CmsResource res = getCmsObject().readResource(uuid);
                    resources.add(res);
                }
            } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getRequest().getParameter("resource"))) {
                // if there was no parameter "resources" set as request parameter
                // this dialog is not used as upload hook try to read the resource parameter
                String resourceParam = getRequest().getParameter("resource");
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourceParam)) {
                    CmsResource res = getCmsObject().readResource(resourceParam);
                    resources.add(res);
                }
            }
            return createUploadDialogBean(resources);
        } catch (CmsException e) {
            error(e);
            return null; // will never be reached
        }
    }

    /**
     * Creates the data bean for the dialog from the list of created resources.
     *
     * @param resources the resources
     * @return the data bean for the dialog
     */
    private CmsPostUploadDialogBean createUploadDialogBean(List<CmsResource> resources) {

        Map<CmsUUID, String> result = new LinkedHashMap<>();
        CmsObject cms = getCmsObject();
        // split resource list into two parts, ones that have required properties and ones that don't,
        // then iterate over the ones with required properties first.
        //
        // this is because the buttons in the upload property dialog only trigger validation for the current tab,
        // so we want the user to go through all resources which require validation first before they can exit the dialog.
        Map<Boolean, List<CmsResource>> parts = resources.stream().collect(
            Collectors.partitioningBy(res -> getRequiredProperties(cms, res).size() > 0));

        for (CmsResource res : Iterables.concat(parts.get(Boolean.TRUE), parts.get(Boolean.FALSE))) {
            result.put(res.getStructureId(), cms.getRequestContext().removeSiteRoot(res.getRootPath()));
        }
        Set<CmsUUID> reqValIds = parts.get(Boolean.TRUE).stream().map(res -> res.getStructureId()).collect(
            Collectors.toSet());
        return new CmsPostUploadDialogBean(result, reqValIds);
    }

    /**
     * Gets the properties required for the given resource (as defined by the requiredOnUpload setting on the corresponding explorertype).
     *
     * @param cms the CMS context
     * @param res a resource
     * @return the set of required properties
     */
    private Set<String> getRequiredProperties(CmsObject cms, CmsResource res) {

        Set<String> requiredProps = new HashSet<>();
        try {
            String typeName = OpenCms.getResourceManager().getResourceType(res).getTypeName();
            CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            if (explorerType != null) {
                for (String prop : explorerType.getProperties()) {
                    if (explorerType.isPropertyRequiredOnUpload(prop)) {
                        requiredProps.add(prop);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return requiredProps;
        }
        return requiredProps;
    }

}
