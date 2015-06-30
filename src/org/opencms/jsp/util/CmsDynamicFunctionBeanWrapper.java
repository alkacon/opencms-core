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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean.Format;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * A wrapper class for using dynamic function beans inside JSPs via the EL.<p>
 */
public class CmsDynamicFunctionBeanWrapper {

    /** The internal CMS object to use. */
    protected CmsObject m_cms;

    /** The dynamic function bean which is being wrapped. */
    protected CmsDynamicFunctionBean m_functionBean;

    /**
     * Creates a new wrapper instance.<p>
     *
     * @param cms the CMS context to use
     * @param functionBean the dynamic function bean to wrap
     */
    public CmsDynamicFunctionBeanWrapper(CmsObject cms, CmsDynamicFunctionBean functionBean) {

        m_cms = cms;
        m_functionBean = functionBean;
    }

    /**
     * Gets the lazy map for accessing the various function formats.<p>
     *
     * @return a map which allows access to the various function formats
     */
    public Object getFormatFor() {

        Transformer mapFunction = new Transformer() {

            public Object transform(Object param) {

                if (m_functionBean == null) {
                    return new CmsDynamicFunctionFormatWrapper(m_cms, null);
                }
                int width = -1;
                String type = null;
                boolean isWidth = false;
                if (param instanceof Long) {
                    width = (int)((Long)param).longValue();
                    isWidth = true;
                } else if (param instanceof Integer) {
                    width = ((Integer)param).intValue();
                    isWidth = true;
                } else {
                    type = param.toString();
                }
                Format format;
                if (isWidth) {
                    format = m_functionBean.getFormatForContainer(m_cms, "", width);
                } else {
                    format = m_functionBean.getFormatForContainer(m_cms, type, -1);
                }
                CmsDynamicFunctionFormatWrapper wrapper = new CmsDynamicFunctionFormatWrapper(m_cms, format);
                return wrapper;
            }
        };
        return CmsCollectionsGenericWrapper.createLazyMap(mapFunction);
    }

    /**
     * Gets the JSP file name of the wrapped dynamic function bean's main format.<p>
     *
     * @return a jsp file name
     */
    public String getJsp() {

        if (m_functionBean == null) {
            return "";
        }
        Format format = m_functionBean.getMainFormat();
        CmsDynamicFunctionFormatWrapper wrapper = new CmsDynamicFunctionFormatWrapper(m_cms, format);
        return wrapper.getJsp();
    }

    /**
     * Gets the parameters of the wrapped dynamic function bean's main format.<p>
     *
     * @return the map of parameters
     */
    public Map<String, String> getParam() {

        return getParameters();
    }

    /**
     * Gets the parameters of the wrapped dynamic function bean's main format.<p>
     *
     * @return the map of parameters
     */
    public Map<String, String> getParameters() {

        if (m_functionBean == null) {
            return Collections.emptyMap();
        }
        Format format = m_functionBean.getMainFormat();
        CmsDynamicFunctionFormatWrapper wrapper = new CmsDynamicFunctionFormatWrapper(m_cms, format);
        return wrapper.getParameters();
    }
}
