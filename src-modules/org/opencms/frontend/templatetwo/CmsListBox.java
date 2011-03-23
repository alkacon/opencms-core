/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsListBox.java,v $
 * Date   : $Date: 2011/03/23 14:52:16 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.frontend.templatetwo;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;

/**
 * Creates a list box from an XML content that uses the listbox schema XSD.<p>
 * 
 * @author Alexander Kandzior 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 7.0.4
 */
public class CmsListBox extends CmsJspActionElement {

    /** The prefix of the macros used in the parameters. */
    public static final String MACRO_LINK_PREFIX = "link";

    /** Node name in the listbox XSD. */
    public static final String NODE_LINKS = "Links";

    /** Node name in the listbox XSD. */
    public static final String NODE_MAPPING = "Mapping";

    /** Node name in the listbox XSD. */
    public static final String NODE_PARAMETER = "Parameter";

    /** Name of the parameter with the path to the resource. */
    public static final String PARAM_FILE = "file";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsListBox.class);

    /** The XML content that contains the definition of the listbox. */
    private CmsXmlContent m_content;

    /** Lazy map with the mapped entries for the collected resources. */
    private Map m_mappedEntries;

    /** The mapping of the xml content to the list box entries. */
    private CmsListBoxContentMapping m_mapping;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsListBox() {

        super();
    }

    /**
     * Constructor, with parameters.<p>
     * 
     * Use this constructor for the template.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsListBox(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns a lazy initialized map with the mapped entries of the collected resources.<p>
     * 
     * @return a lazy initialized map
     */
    public Map getMappedEntry() {

        if (m_mappedEntries == null) {
            m_mappedEntries = LazyMap.decorate(new HashMap(), new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    CmsListBoxEntry entry = null;

                    try {
                        CmsXmlContent content;
                        if (input instanceof CmsXmlContent) {
                            content = (CmsXmlContent)input;
                        } else {
                            CmsResource resource = (CmsResource)input;
                            content = CmsXmlContentFactory.unmarshal(getCmsObject(), getCmsObject().readFile(resource));
                        }

                        if (getMapping() != null) {
                            entry = getMapping().getEntryFromXmlContent(
                                getCmsObject(),
                                content,
                                getRequestContext().getLocale());
                        }
                    } catch (CmsException ex) {
                        // noop
                    }

                    return entry;
                }
            });
        }
        return m_mappedEntries;
    }

    /**
     * Returns the parameters of the collector with resolved macros.<p>
     * 
     * @return the parameters of the collector with resolved macros
     */
    public String getParameter() {

        Locale locale = getRequestContext().getLocale();

        String params = m_content.getStringValue(getCmsObject(), NODE_PARAMETER, locale);
        List links = m_content.getValues(NODE_LINKS, locale);

        CmsMacroResolver macroResolver = CmsMacroResolver.newInstance();
        macroResolver.setKeepEmptyMacros(true);
        for (int i = 0; i < links.size(); i++) {
            I_CmsXmlContentValue xmlValue = (I_CmsXmlContentValue)links.get(i);
            String value = xmlValue.getStringValue(getCmsObject());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                StringBuffer macro = new StringBuffer(10);
                macro.append(MACRO_LINK_PREFIX).append(i + 1);
                macroResolver.addMacro(macro.toString(), getRequestContext().removeSiteRoot(value));
            }
        }

        return macroResolver.resolveMacros(params);
    }

    /**
     * @see org.opencms.jsp.CmsJspBean#init(javax.servlet.jsp.PageContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super.init(context, req, res);

        // collect the configuration information 
        try {
            String path = req.getParameter(PARAM_FILE);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
                // TODO
            }

            CmsFile file = getCmsObject().readFile(
                getCmsObject().getRequestContext().removeSiteRoot(path),
                CmsResourceFilter.IGNORE_EXPIRATION);
            m_content = CmsXmlContentFactory.unmarshal(getCmsObject(), file);

            // process the default mappings (if set / available)
            m_mapping = null;
            int mapsize = m_content.getValues(NODE_MAPPING, getRequestContext().getLocale()).size();
            if (mapsize > 0) {
                m_mapping = new CmsListBoxContentMapping();
                for (int i = 1; i <= mapsize; i++) {
                    String basePath = CmsXmlUtils.createXpath(NODE_MAPPING, i);

                    String field = m_content.getStringValue(
                        getCmsObject(),
                        CmsXmlUtils.concatXpath(basePath, "Field"),
                        getRequestContext().getLocale());
                    String defaultValue = m_content.getStringValue(getCmsObject(), CmsXmlUtils.concatXpath(
                        basePath,
                        "Default"), getRequestContext().getLocale());
                    String maxLenghtStr = m_content.getStringValue(getCmsObject(), CmsXmlUtils.concatXpath(
                        basePath,
                        "MaxLength"), getRequestContext().getLocale());
                    List xmlNodes = m_content.getValues(
                        CmsXmlUtils.concatXpath(basePath, "XmlNode"),
                        getRequestContext().getLocale());
                    List nodes = new ArrayList(xmlNodes.size());
                    for (int j = 0; j < xmlNodes.size(); j++) {
                        nodes.add(((I_CmsXmlContentValue)xmlNodes.get(j)).getStringValue(getCmsObject()));
                    }
                    m_mapping.addListBoxFieldMapping(nodes, field, maxLenghtStr, defaultValue);
                }
            }

        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns the mapping of the xml content to the list box entries.<p>
     * 
     * @return the mapping of the xml content to the list box entries
     */
    protected CmsListBoxContentMapping getMapping() {

        return m_mapping;
    }
}