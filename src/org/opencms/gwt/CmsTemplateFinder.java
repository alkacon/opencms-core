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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for getting information about available templates.<p>
 *
 * @since 8.0.0
 */
public class CmsTemplateFinder {

    /** Macro which is used in template.provider property to be substituted with the template path. */
    public static final String MACRO_TEMPLATEPATH = "templatepath";

    /** The cms context. */
    protected CmsObject m_cms;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the cms context to use
     */

    public CmsTemplateFinder(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Returns the available templates.<p>
     *
     * @return the available templates
     *
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsClientTemplateBean> getTemplates() throws CmsException {

        Map<String, CmsClientTemplateBean> result = new HashMap<String, CmsClientTemplateBean>();
        CmsObject cms = getCmsObject();

        // find current site templates
        int templateId = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeJsp.getContainerPageTemplateTypeName()).getTypeId();
        List<CmsResource> templates = cms.readResources(
            "/",
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(templateId),
            true);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot())) {
            // if not in the root site, also add template under /system/
            templates.addAll(
                cms.readResources(
                    CmsWorkplace.VFS_PATH_SYSTEM,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(templateId),
                    true));
        }
        // convert resources to template beans
        for (CmsResource template : templates) {
            CmsClientTemplateBean templateBean = getTemplateBean(cms, template);
            result.put(templateBean.getSitePath(), templateBean);
        }
        return result;
    }

    /**
     * Gets the CMS context to use.<p>
     *
     * @return the CMS context to use
     */
    protected CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns a bean representing the given template resource.<p>
     *
     * @param cms the cms context to use for VFS operations
     * @param resource the template resource
     *
     * @return bean representing the given template resource
     *
     * @throws CmsException if something goes wrong
     */
    private CmsClientTemplateBean getTemplateBean(CmsObject cms, CmsResource resource) throws CmsException {

        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
        CmsProperty descProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        CmsProperty imageProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TEMPLATE_IMAGE, false);
        CmsProperty selectValueProp = cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_TEMPLATE_PROVIDER,
            false);
        String sitePath = cms.getSitePath(resource);
        String templateValue = sitePath;
        if (!selectValueProp.isNullProperty() && !CmsStringUtil.isEmptyOrWhitespaceOnly(selectValueProp.getValue())) {
            String selectValue = selectValueProp.getValue();
            CmsMacroResolver resolver = new CmsMacroResolver();
            resolver.addMacro(MACRO_TEMPLATEPATH, sitePath);
            templateValue = resolver.resolveMacros(selectValue);
        }
        return new CmsClientTemplateBean(
            titleProp.getValue(),
            descProp.getValue(),
            templateValue,
            imageProp.getValue());
    }

}
