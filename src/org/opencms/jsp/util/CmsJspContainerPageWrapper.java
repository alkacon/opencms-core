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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Wrapper for using container pages in JSPs.
 */
public class CmsJspContainerPageWrapper {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspContainerPageWrapper.class);

    /** The wrapped container page bean. */
    private CmsContainerPageBean m_page;

    /**
     * Creates a new instance.
     *
     * @param page the container page to wrap
     */
    public CmsJspContainerPageWrapper(CmsContainerPageBean page) {

        m_page = page;

    }

    /**
     * Renders the element in the container with the given name or name prefix.
     * @param context the context bean
     * @param name the container name or name prefix
     * @return the rendered HTML
     */
    public String renderContainer(CmsJspStandardContextBean context, String name) {

        CmsContainerBean container = findContainer(name);
        if (container == null) {
            return null;
        }
        return render(context, container);
    }

    /**
     * Helper method for locating a container with the given name or name prefix.
     * @param name the name or name prefix
     * @return the container, or null if none were found
     */
    private CmsContainerBean findContainer(String name) {

        CmsContainerBean result = m_page.getContainers().get(name);
        if (result == null) {
            for (Map.Entry<String, CmsContainerBean> entry : m_page.getContainers().entrySet()) {
                if (entry.getKey().endsWith("-" + name)) {
                    result = entry.getValue();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Renders the elements from the given container as HTML and returns it.
     *
     * @param context the context bean
     * @param container the container whose elements should be rendered
     * @return the HTML of the container elements, without a surrounding element
     */
    private String render(CmsJspStandardContextBean context, CmsContainerBean container) {

        CmsFlexController controller = CmsFlexController.getController(context.getRequest());
        CmsObject m_cms = context.getCmsObject();
        CmsContainerBean oldContainer = context.getContainer();
        CmsContainerElementBean oldElement = context.getElement();
        CmsContainerPageBean oldPage = context.getPage();
        boolean oldForceDisableEdit = context.isForceDisableEditMode();
        Locale locale = m_cms.getRequestContext().getLocale();
        context.getRequest();
        try {
            context.setContainer(container);
            context.setPage(m_page);
            // The forceDisableEditMode flag may be incorrectly cached in the standard
            // context bean copies stored in flex cache entries, but it doesn't matter since edit mode is never
            // active in the Online project anyway
            context.setForceDisableEditMode(true);

            int containerWidth = -1;
            try {
                containerWidth = Integer.parseInt(container.getWidth());
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            CmsADEConfigData adeConfig = context.getSitemapConfigInternal();
            StringBuilder buffer = new StringBuilder();
            for (CmsContainerElementBean element : container.getElements()) {

                try {
                    element.initResource(m_cms);
                    I_CmsFormatterBean formatterBean = CmsJspTagContainer.ensureValidFormatterSettings(
                        m_cms,
                        element,
                        adeConfig,
                        container.getName(),
                        container.getType(),
                        containerWidth);
                    element.initSettings(m_cms, adeConfig, formatterBean, locale, controller.getCurrentRequest(), null);
                    context.setElement(element);
                    CmsResource formatterRes = m_cms.readResource(
                        formatterBean.getJspStructureId(),
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    byte[] formatterOutput = OpenCms.getResourceManager().getLoader(formatterRes).dump(
                        m_cms,
                        formatterRes,
                        null,
                        locale,
                        controller.getCurrentRequest(),
                        controller.getCurrentResponse());
                    String encoding = controller.getCurrentResponse().getEncoding();
                    String formatterOutputStr = new String(formatterOutput, encoding);
                    buffer.append(formatterOutputStr);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            String resultHtml = buffer.toString();
            return resultHtml;
        } finally {
            context.setPage(oldPage);
            context.setContainer(oldContainer);
            context.setElement(oldElement);
            context.setForceDisableEditMode(oldForceDisableEdit);
        }

    }

}
