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

package org.opencms.ade.configuration.plugins;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.jsp.Messages;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLinkInfo;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Helper class for finding the list of active template plugins for the current page.
 */
public class CmsTemplatePluginFinder {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplatePluginFinder.class);

    /** Current standard context bean. */
    private CmsJspStandardContextBean m_standardContextBean;

    /**
     * Creates a new instance.
     *
     * @param standardContextBean the current standard context bean
     */
    public CmsTemplatePluginFinder(CmsJspStandardContextBean standardContextBean) {

        m_standardContextBean = standardContextBean;
    }

    /**
     * Gets the active plugins from site plugins only.
     *
     * @param config the sitemap configuration for which to get the plugins
     *
     * @return the multimap of active plugins by group
     */
    public static Multimap<String, CmsTemplatePlugin> getActiveTemplatePluginsFromSitePlugins(CmsADEConfigData config) {

        List<CmsTemplatePlugin> plugins = new ArrayList<>();
        for (CmsSitePlugin sitePlugin : config.getSitePlugins()) {
            plugins.addAll(sitePlugin.getPlugins());
        }
        return getActivePlugins(plugins);
    }

    /**
     * Collects the referenced plugins from the current page (from container elements, detail elements, etc.).
     *
     * @param standardContext the standard context bean
     * @return the list of plugins (unsorted, with duplicates)
     *
     * @throws CmsException if something goes wrong
     */
    private static List<CmsTemplatePlugin> collectPluginsForCurrentPage(CmsJspStandardContextBean standardContext)
    throws CmsException {

        List<CmsTemplatePlugin> plugins = new ArrayList<>();
        CmsObject cms = standardContext.getVfs().getCmsObject();
        HttpServletRequest req = (HttpServletRequest)(standardContext.getRequest());
        standardContext.initPage();

        CmsContainerPageBean containerPage = standardContext.getPage();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
            cms,
            cms.getRequestContext().getRootUri());
        if ((containerPage != null) && (containerPage.getElements() != null)) {
            List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>(containerPage.getContainers().values());
            // add detail only containers if available
            if (standardContext.isDetailRequest()) {
                CmsContainerPageBean detailOnly = CmsDetailOnlyContainerUtil.getDetailOnlyPage(
                    cms,
                    req,
                    cms.getRequestContext().getRootUri());
                if (detailOnly != null) {
                    containers.addAll(detailOnly.getContainers().values());
                }
            }
            for (CmsContainerBean container : containers) {
                for (CmsContainerElementBean element : container.getElements()) {
                    try {
                        element.initResource(cms);
                        if (!standardContext.getIsOnlineProject()
                            || element.getResource().isReleasedAndNotExpired(
                                cms.getRequestContext().getRequestTime())) {
                            if (element.isGroupContainer(cms) || element.isInheritedContainer(cms)) {
                                List<CmsContainerElementBean> subElements;
                                if (element.isGroupContainer(cms)) {
                                    subElements = CmsJspTagContainer.getGroupContainerElements(
                                        cms,
                                        element,
                                        req,
                                        container.getType());
                                } else {
                                    subElements = CmsJspTagContainer.getInheritedContainerElements(cms, element);
                                }
                                for (CmsContainerElementBean subElement : subElements) {
                                    subElement.initResource(cms);
                                    if (!standardContext.getIsOnlineProject()
                                        || subElement.getResource().isReleasedAndNotExpired(
                                            cms.getRequestContext().getRequestTime())) {
                                        I_CmsFormatterBean formatter = getFormatterBeanForElement(
                                            cms,
                                            config,
                                            subElement,
                                            container);
                                        if (formatter != null) {
                                            plugins.addAll(formatter.getTemplatePlugins());
                                        }
                                    }
                                }
                            } else {
                                I_CmsFormatterBean formatter = getFormatterBeanForElement(
                                    cms,
                                    config,
                                    element,
                                    container);
                                if (formatter != null) {
                                    plugins.addAll(formatter.getTemplatePlugins());
                                }
                            }
                        }
                    } catch (CmsException e) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_READING_REQUIRED_RESOURCE_1,
                                element.getSitePath()),
                            e);
                    }
                }
            }
        }
        if (standardContext.getDetailContentId() != null) {
            try {
                CmsResource detailContent = cms.readResource(
                    standardContext.getDetailContentId(),
                    CmsResourceFilter.ignoreExpirationOffline(cms));
                CmsFormatterConfiguration detailContentFormatters = config.getFormatters(cms, detailContent);
                for (I_CmsFormatterBean formatter : detailContentFormatters.getDetailFormatters()) {
                    plugins.addAll(formatter.getTemplatePlugins());
                }
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_READING_REQUIRED_RESOURCE_1,
                        standardContext.getDetailContentId()),
                    e);
            }
        }
        for (CmsSitePlugin sitePlugin : config.getSitePlugins()) {
            plugins.addAll(sitePlugin.getPlugins());
        }
        return plugins;
    }

    /**
     * Gets the active plugins by group as a multimap, with each group sorted by descending order.
     *
     * @param plugins unsorted list of plugins, possibly with duplicates
     * @return multimap of plugins by group
     */
    private static Multimap<String, CmsTemplatePlugin> getActivePlugins(List<CmsTemplatePlugin> plugins) {

        Multimap<String, CmsTemplatePlugin> pluginsByGroup = ArrayListMultimap.create();
        for (CmsTemplatePlugin plugin : plugins) {
            pluginsByGroup.put(plugin.getGroup(), plugin);
        }
        Multimap<String, CmsTemplatePlugin> result = ArrayListMultimap.create();
        for (String group : pluginsByGroup.keySet()) {
            List<CmsTemplatePlugin> active = sortAndDeduplicatePlugins(
                (List<CmsTemplatePlugin>)(pluginsByGroup.get(group)));
            result.putAll(group, active);
        }
        return result;
    }

    /**
     * Returns the formatter configuration for the given element, will return <code>null</code> for schema formatters.<p>
     *
     * @param cms the current CMS context
     * @param config the current sitemap configuration
     * @param element the element bean
     * @param container the container bean
     *
     * @return the formatter configuration bean
     */
    private static I_CmsFormatterBean getFormatterBeanForElement(
        CmsObject cms,
        CmsADEConfigData config,
        CmsContainerElementBean element,
        CmsContainerBean container) {

        int containerWidth = -1;
        if (container.getWidth() == null) {
            // the container width has not been set yet
            containerWidth = CmsFormatterConfiguration.MATCH_ALL_CONTAINER_WIDTH;
        } else {
            try {
                containerWidth = Integer.parseInt(container.getWidth());
            } catch (NumberFormatException e) {
                // do nothing, set width to -1
            }
        }
        I_CmsFormatterBean result = CmsJspTagContainer.getFormatterConfigurationForElement(
            cms,
            element,
            config,
            container.getName(),
            container.getType(),
            containerWidth);
        return result;
    }

    /**
     * Sorts and de-duplicates a list of plugins.
     *
     * <p>This returns a new list and does not modify the input list.
     *
     * @param plugins the list of plugins
     * @return the sorted and de-duplicated plugins
     */
    private static List<CmsTemplatePlugin> sortAndDeduplicatePlugins(List<CmsTemplatePlugin> plugins) {

        List<CmsTemplatePlugin> result = new ArrayList<>();
        Set<CmsLinkInfo> seenTargets = new HashSet<>();
        // sort in descending order
        Collections.sort(plugins, (p1, p2) -> Integer.compare(p2.getOrder(), p1.getOrder()));
        for (CmsTemplatePlugin plugin : plugins) {
            try {
                // only add the first occurrence of a given target
                CmsLinkInfo target = plugin.getTarget();
                if (!seenTargets.contains(target)) {
                    seenTargets.add(target);
                    result.add(plugin);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Gets the multimap of plugins for the current page by group, with each group sorted and de-duplicated.
     *
     * @return the multimap of plugins
     */
    public Multimap<String, CmsTemplatePlugin> getTemplatePlugins() {

        try {
            return getActivePlugins(collectPluginsForCurrentPage(m_standardContextBean));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return ArrayListMultimap.create();
        }
    }

}
