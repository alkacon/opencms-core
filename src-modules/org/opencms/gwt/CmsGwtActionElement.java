/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsGwtActionElement.java,v $
 * Date   : $Date: 2011/02/01 15:08:13 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;

import java.lang.reflect.Method;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;

/**
 * Sitemap action used to generate the sitemap editor.<p>
 * 
 * see jsp file <tt>/system/modules/org.opencms.ade.sitemap/sitemap.jsp</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.0
 */
public class CmsGwtActionElement extends CmsJspActionElement {

    /** The opening script tag. */
    protected static final String SCRIPT_TAG_OPEN = "<script type=\"text/javascript\">";

    /** The closing script tag. */
    protected static final String SCRIPT_TAG_CLOSE = "</script>";

    /** The resource icon CSS URI. */
    private static final String ICON_CSS_URI = "/system/modules/org.opencms.gwt/resourceIcon.css";

    /** The current core data. */
    private CmsCoreData m_coreData;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsGwtActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the serialized data for the core provider wrapped into a script tag.<p>
     * 
     * @return the data
     * 
     * @throws Exception if something goes wrong
     */
    public String export() throws Exception {

        StringBuffer sb = new StringBuffer();

        String prefetchedData = serialize(I_CmsCoreService.class.getMethod("prefetch"), getCoreData());
        sb.append(CmsCoreData.DICT_NAME).append("='").append(prefetchedData).append("';");
        sb.append(ClientMessages.get().export(getRequest()));
        wrapScript(sb);
        sb.append("<style type=\"text/css\">\n @import url(\"").append(link(ICON_CSS_URI)).append("\");\n</style>\n");
        return sb.toString();
    }

    /**
     * Returns the serialized data for the core provider.<p>
     * 
     * @return the data
     * 
     * @throws Exception if something goes wrong
     */
    public String exportAll() throws Exception {

        return export();
    }

    /**
     * Returns the needed server data for client-side usage.<p> 
     *
     * @return the needed server data for client-side usage
     */
    public CmsCoreData getCoreData() {

        if (m_coreData == null) {
            m_coreData = CmsCoreService.newInstance(getRequest()).prefetch();
        }
        return m_coreData;
    }

    /**
     * Returns the workplace locale for the current user.<p>
     * 
     * @return the workplace locale
     */
    public Locale getWorkplaceLocale() {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
    }

    /**
     * Serializes the result of the given method for RPC-prefetching.<p>
     * 
     * @param method the method
     * @param data the result to serialize
     * 
     * @return the serialized data
     * 
     * @throws SerializationException if something goes wrong
     */
    protected String serialize(Method method, Object data) throws SerializationException {

        return escape(RPC.encodeResponseForSuccess(method, data, CmsPrefetchSerializationPolicy.instance()));
    }

    /**
     * Escapes the given string for serialization.<p>
     * 
     * @param s the string to escape
     * 
     * @return the escaped string
     */
    private String escape(String s) {

        // escape back slashes
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        // escape single quotation marks
        return ret.replaceAll("'", "\\\\'");
    }

    /**
     * Wraps the given buffer with surrounding script tags.<p> 
     * 
     * @param sb the string buffer to wrap
     * 
     * @return the string buffer
     */
    protected StringBuffer wrapScript(StringBuffer sb) {

        sb.insert(0, SCRIPT_TAG_OPEN);
        sb.append(SCRIPT_TAG_CLOSE).append("\n");
        return sb;
    }
}
