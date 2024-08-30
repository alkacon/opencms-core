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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.I_CmsAutoBeanFactory;
import org.opencms.gwt.shared.I_CmsListAddMetadata;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.logging.Log;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Generates a special HTML element which enables the 'create list element' dialog for a set of types when used in a formatter.
 */
public class CmsJspTagEnableListAdd extends SimpleTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagEnableListAdd.class);

    /** The post-create handler. */
    private String m_postCreateHandler;

    /** The resource type names. */
    private List<String> m_types = new ArrayList<>();

    /** The upload folder. */
    private String m_uploadFolder;

    /**
     * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
    @Override
    public void doTag() throws IOException {

        // in practice, the JSP context will always be a page context
        PageContext pageContext = (PageContext)getJspContext();
        CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
        CmsObject cms = controller.getCmsObject();
        CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(pageContext.getRequest());
        // the list add information is only used by the page editor
        if (standardContext.getIsEditMode()) {
            // use autobean factory to create the necessary JSON
            I_CmsAutoBeanFactory beanFactory = AutoBeanFactorySource.create(I_CmsAutoBeanFactory.class);
            AutoBean<I_CmsListAddMetadata> bean = beanFactory.createListAddMetadata();
            bean.as().setTypes(m_types);
            bean.as().setPostCreateHandler(m_postCreateHandler);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_uploadFolder) && !"none".equals(m_uploadFolder)) {
                try {
                    // don't bother with enabling uploads if the upload folder doesn't exist
                    cms.readResource(m_uploadFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    bean.as().setUploadFolder(m_uploadFolder);
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
            String jsonData = AutoBeanCodex.encode(bean).getPayload();
            StringBuilder buffer = new StringBuilder();
            String tag = CmsGwtConstants.TAG_OC_LISTADD;
            buffer.append("<" + tag + " style='display: none !important;' " + CmsGwtConstants.ATTR_DATA_LISTADD + "='");
            buffer.append(CmsEncoder.escapeXml(jsonData));
            buffer.append("'></" + tag + ">");
            pageContext.getOut().println(buffer.toString());
        }
    }

    /**
     * Sets the post-create handler to use after creating new elements.
     *
     * @param postCreateHandler the post-create handler
     */
    public void setPostCreateHandler(String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
    }

    /**
     * Sets the types to offer to create.
     *
     * @param typesObj either a string containing type names separated by commas, or a collection of objects whose toString() methods return resource type names.
     *
     * @throws JspException if something goes wrong
     */
    public void setTypes(Object typesObj) throws JspException {

        if (typesObj instanceof String) {
            String[] tokens = ((String)typesObj).split(",");
            setTypes(Arrays.asList(tokens));
        } else if (typesObj instanceof Collection) {
            Collection<?> types = (Collection<?>)typesObj;
            // list elements may be value wrappers - convert to string
            // list elements may be display type strings - convert to resource type names
            m_types = types.stream().map(
                type -> CmsConfigurationBean.getResourceTypeForDisplayType("" + type).trim()).distinct().collect(
                    Collectors.toList());
        } else {
            throw new JspException("Invalid type for types attribute of enable-list-add tag: " + typesObj);
        }
    }

    /**
     * Sets the upload folder.
     *
     * @param uploadFolder the upload folder
     */
    public void setUploadFolder(String uploadFolder) {

        m_uploadFolder = uploadFolder;
    }

}
