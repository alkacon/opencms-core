/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsDefaultContainerPageNameGenerator.java,v $
 * Date   : $Date: 2010/11/11 14:12:15 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;

import org.apache.commons.collections.Factory;

/**
 * The default container page name generator.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsDefaultContainerPageNameGenerator implements I_CmsContainerPageNameGenerator {

    /** The CMS context. */
    protected CmsObject m_cms;

    /** The file name pattern from the config file. */
    private String m_pattern;

    /** The sitemap path of the container page. */
    protected String m_sitePath;

    /** The title of the container page. */
    protected String m_title;

    /**
     * @see org.opencms.xml.sitemap.I_CmsContainerPageNameGenerator#getNextName()
     */
    public String getNextName() throws CmsException {

        CmsMacroResolver resolver = new CmsMacroResolver();
        String name = CmsResource.getName(m_sitePath);
        String niceName = OpenCms.getResourceManager().getFileTranslator().translateResource(name);
        String nicePath = OpenCms.getResourceManager().getFileTranslator().translateResource(m_sitePath);
        nicePath = nicePath.replaceAll("/$", "");
        nicePath = nicePath.replaceAll("^/", "");
        nicePath = nicePath.replace('/', '_');
        String niceTitle = OpenCms.getResourceManager().getFileTranslator().translateResource(m_title);
        niceTitle = niceTitle.replace('/', '_');
        // we don't want to increment the counter if the macro isn't actually used, so 
        // we use a dynamic macro.
        resolver.addDynamicMacro("number", new Factory() {

            /**
             * @see org.apache.commons.collections.Factory#create()
             */
            public String create() {

                try {
                    PrintfFormat fmt = new PrintfFormat("%0.6d");
                    String result = fmt.sprintf(m_cms.incrementCounter(CmsResourceTypeXmlContainerPage.getStaticTypeName()));
                    return result;
                } catch (CmsException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        resolver.addMacro("sitepath", nicePath);
        resolver.addMacro("title", niceTitle);
        resolver.addMacro("name", niceName);
        try {
            return resolver.resolveMacros(m_pattern);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof CmsException) {
                throw (CmsException)e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsContainerPageNameGenerator#init(org.opencms.file.CmsObject, java.lang.String, java.lang.String, java.lang.String)
     */
    public void init(CmsObject cms, String pattern, String title, String sitePath) {

        m_cms = cms;
        m_pattern = pattern;
        m_title = title;
        m_sitePath = sitePath;
    }

}
