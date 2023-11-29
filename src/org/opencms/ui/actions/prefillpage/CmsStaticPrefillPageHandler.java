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

package org.opencms.ui.actions.prefillpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.mutable.CmsMutableContainerPage;

import org.apache.commons.logging.Log;

/**
 * Pre-fills the page by adding contents from a template page into a specific container of the page to fill.
 */
public class CmsStaticPrefillPageHandler implements I_CmsPrefillPageHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStaticPrefillPageHandler.class);

    /** The sitemap attribute used to configure the container which should be filled. */
    private static final String ATTR_PREFILL_CONTAINER = "template.prefill.container";

    /** The sitemap attribute used to configure the prefill template. */
    private static final String ATTR_PREFILL_TEMPLATE = "template.prefill.file";

    /**
     * @see org.opencms.ui.actions.prefillpage.I_CmsPrefillPageHandler#execute(org.opencms.ui.I_CmsDialogContext)
     */
    public void execute(I_CmsDialogContext context) {

        CmsObject cms = context.getCms();
        CmsResource resource = context.getResources().get(0);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());
        try (AutoCloseable c = CmsLockUtil.withLockedResources(cms, resource)) {
            CmsFile file = cms.readFile(resource);
            CmsXmlContainerPage pageXml = CmsXmlContainerPageFactory.unmarshal(cms, file);
            CmsContainerPageBean page = pageXml.getContainerPage(cms);
            String containerName = config.getAttribute(ATTR_PREFILL_CONTAINER, null);
            CmsResource prefillTemplate = getPrefillTemplate(cms, resource);
            if (prefillTemplate == null) {
                // context menu option should not be available if prefill template is not available
                throw new RuntimeException("Prefill template not found in subsitemap " + config.getBasePath());
            }
            CmsContainerBean container = page.getContainers().get(containerName);
            if (container == null) {
                CmsFile templatePageFile = cms.readFile(prefillTemplate);
                CmsXmlContainerPage templatePageXml = CmsXmlContainerPageFactory.unmarshal(cms, templatePageFile);
                CmsContainerPageBean templatePage = templatePageXml.getContainerPage(cms);
                CmsMutableContainerPage pageToRewrite = CmsMutableContainerPage.fromImmutable(page);
                CmsMutableContainerPage templatePageBean = CmsMutableContainerPage.fromImmutable(templatePage);
                pageToRewrite.containers().addAll(templatePageBean.containers());
                pageXml.save(cms, pageToRewrite.toImmutable());
            }
            context.reload();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            context.error(e);
        }

    }

    /**
     * @see org.opencms.ui.actions.prefillpage.I_CmsPrefillPageHandler#isExecutable(org.opencms.ui.I_CmsDialogContext)
     */
    public boolean isExecutable(I_CmsDialogContext context) {

        CmsObject cms = context.getCms();
        CmsResource resource = context.getResources().get(0);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());
        String containerName = config.getAttribute(ATTR_PREFILL_CONTAINER, null);
        if (containerName == null) {
            return false;
        }
        CmsResource prefillTemplate = getPrefillTemplate(cms, resource);
        if (prefillTemplate == null) {
            return false;
        }

        try {
            // check that the container doesn't exist yet in the container page
            try {
                CmsFile file = cms.readFile(resource);
                CmsXmlContainerPage pageXml = CmsXmlContainerPageFactory.unmarshal(cms, file);
                CmsContainerPageBean page = pageXml.getContainerPage(cms);
                CmsContainerBean container = page.getContainers().get(containerName);
                if (container != null) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    /**
     * Gets the container page to use as a prefill template for the given page.
     *
     * @param cms the current CMS context
     * @param resource the currently edited container page
     * @return the prefill template resource, or null if none was configured or the configured one was not found
     */
    private CmsResource getPrefillTemplate(CmsObject cms, CmsResource resource) {

        try {

            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());
            String prefillTemplateStr = config.getAttribute(ATTR_PREFILL_TEMPLATE, null);
            if (prefillTemplateStr == null) {
                return null;
            }
            CmsMacroResolver resolver = new CmsMacroResolver();
            resolver.setCmsObject(cms);
            String subsite = cms.getRequestContext().removeSiteRoot(
                OpenCms.getADEManager().getSubSiteRoot(cms, resource.getRootPath()));
            if (subsite != null) {
                resolver.addMacro("subsite", subsite);
            }
            // remove duplicate slashes in case subsite macro contains a trailing slash and is used as %(subsite)/xyz
            prefillTemplateStr = resolver.resolveMacros(prefillTemplateStr).replace("//", "/");
            return cms.readResource(prefillTemplateStr, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
            return null;
        }

    }

}
