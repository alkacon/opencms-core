/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsPointerLoader.java,v $
 * Date   : $Date: 2011/03/23 14:50:05 $
 * Version: $Revision: 1.58 $
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsRequestUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Loader for "pointers" to resources in the VFS or to external resources.<p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.58 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPointerLoader extends CmsDumpLoader {

    /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID = 4;

    /** The html-code prefix for generating the export file for external links. */
    private static String EXPORT_PREFIX = "<html>\n<head>\n<meta http-equiv="
        + '"'
        + "refresh"
        + '"'
        + " content="
        + '"'
        + "0; url=";

    /** The html-code suffix for generating the export file for external links. */
    private static String EXPORT_SUFFIX = '"' + ">\n</head>\n<body></body>\n</html>";

    /**
     * The constructor of the class is empty and does nothing.<p>
     */
    public CmsPointerLoader() {

        // NOOP
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_REQUEST_PARAM_SUPPORT_ENABLED.equals(paramName)) {
                m_requestParamSupportEnabled = Boolean.valueOf(paramValue).booleanValue();
            }
        }
    }

    /** 
     * Destroy this ResourceLoder, this is a NOOP so far.<p>
     */
    public void destroy() {

        // NOOP
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] dump(
        CmsObject cms,
        CmsResource resource,
        String element,
        Locale locale,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException {

        return cms.readFile(resource).getContents();
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        String pointer = new String(cms.readFile(resource).getContents());
        StringBuffer result = new StringBuffer(128);
        result.append(EXPORT_PREFIX);
        // conditionally append parameters of the current request:
        pointer = appendLinkParams(pointer, req);
        if (pointer.indexOf(':') < 0) {
            result.append(OpenCms.getLinkManager().substituteLink(cms, pointer));
        } else {
            result.append(pointer);
        }
        result.append(EXPORT_SUFFIX);
        load(cms, resource, req, res);
        return result.toString().getBytes(OpenCms.getSystemInfo().getDefaultEncoding());
    }

   /**
    * Flag that controls if parameters in requests to pointer resources are
    * appended to the target link when redirecting.
    */
    protected static boolean m_requestParamSupportEnabled;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getConfiguration() {

        Map<String, String> config = super.getConfiguration();
        SortedMap<String, String> result = new TreeMap<String, String>();
        if (config != null) {
            result.putAll(config);
        }
        result.put(CONFIGURATION_REQUEST_PARAM_SUPPORT_ENABLED, String.valueOf(m_requestParamSupportEnabled));
        return result;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return RESOURCE_LOADER_ID;
    }

    /**
     * Return a String describing the ResourceLoader,
     * which is (localized to the system default locale)
     * <code>"The OpenCms default resource loader for pointers"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {

        return Messages.get().getBundle().key(Messages.GUI_LOADER_POINTER_DEFAULT_DESC_0);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_LOADER_INITIALIZED_1,
                this.getClass().getName()));
        }
    }

    /**
     * Returns true if request parameter support is disabled. <p>
     *
     * @return 
     *      true if request parameter support is disabled
     *
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
     */
    public boolean isStaticExportEnabled() {

        return !m_requestParamSupportEnabled;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
     */
    public boolean isUsableForTemplates() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
     */
    public boolean isUsingUriWhenLoadingTemplate() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        if ((res == null) || res.isCommitted()) {
            // nothing we can do
            return;
        }

        String pointer = new String(cms.readFile(resource).getContents());
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(pointer)) {
            throw new CmsLoaderException(Messages.get().container(
                Messages.ERR_INVALID_POINTER_FILE_1,
                resource.getName()));
        }
        if (pointer.indexOf(':') < 0) {
            pointer = OpenCms.getLinkManager().substituteLink(cms, pointer);
        }

        // conditionally append parameters of the current request:
        pointer = appendLinkParams(pointer, req);
        res.sendRedirect(pointer);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) {

        throw new CmsRuntimeException(
            Messages.get().container(Messages.ERR_SERVICE_UNSUPPORTED_1, getClass().getName()));
    }

    /**
     * Internal helper that is used by
     * <code>{@link #load(CmsObject, CmsResource, HttpServletRequest, HttpServletResponse)}</code>
     * and
     * <code>{@link #export(CmsObject, CmsResource, HttpServletRequest, HttpServletResponse)}</code>
     * to handle conditional request parameter support for links to pointer
     * resources.
     * <p>
     * 
     * @param pointerLink
     *            the link to append request parameters to
     * 
     * @param req
     *            the original request to the pointer
     * 
     * @return the pointer with the parameters (if {@link #
     */
    @SuppressWarnings("unchecked")
    private static String appendLinkParams(String pointerLink, HttpServletRequest req) {
        String result = pointerLink;
        if (isRequestParamSupportEnabled()) {
            Map<String, String[]> params = req.getParameterMap();
            if (params.size() > 0) {
                result = CmsRequestUtil.appendParameters(result, params, false);
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if parameters in requests to pointer resources
     * are appended to the target link when redirecting.
     * <p>
     * This is controlled by the configuration of this loader in
     * <code>opencms-system.xml</code>.
     * <p>
     * 
     * @return <code>true</code> if parameters in requests to pointer resources
     *         are appended to the target link when redirecting.
     */
    public static boolean isRequestParamSupportEnabled() {

        return m_requestParamSupportEnabled;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable()
     */
    public boolean isStaticExportProcessable() {

        return false;
    }

    /**
     * The configuration parameter for the OpenCms XML configuration to enable
     * that the parameters in requests to pointer resources are appended to the
     * pointer target link.
     */
    public static final String CONFIGURATION_REQUEST_PARAM_SUPPORT_ENABLED = "pointer.requestparamsupport.enabled";
}
