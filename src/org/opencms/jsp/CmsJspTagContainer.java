/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspTagContainer.java,v $
 * Date   : $Date: 2009/08/25 15:03:34 $
 * Version: $Revision: 1.1.2.3 $
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.ade.CmsADEElementManager;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the page container elements.<p>
 *
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 7.6 
 */
public class CmsJspTagContainer extends TagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1228397990961282556L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContainer.class);

    /** The maxElements attribute value. */
    private String m_maxElements;

    /** The name attribute value. */
    private String m_name;

    /** The type attribute value. */
    private String m_type;

    /** Container page loader reference. */
    private static final CmsContainerPageLoader LOADER = (CmsContainerPageLoader)OpenCms.getResourceManager().getLoader(
        CmsContainerPageLoader.RESOURCE_LOADER_ID);

    /**
     * Internal action method.<p>
     * 
     * @param pageContext the current JSP page context
     * @param containerName the name of the container
     * @param containerType the type of the container
     * @param containerMaxElements the maximal number of elements in the container 
     * @param req the current request
     * @param res the current response
     * 
     * @throws CmsException if something goes wrong
     * @throws IOException if there is a problem writing to the response
     * @throws ServletException if there is some problem calling the jsp formatter
     */
    public static void containerTagAction(
        PageContext pageContext,
        String containerName,
        String containerType,
        String containerMaxElements,
        ServletRequest req,
        ServletResponse res) throws CmsException, IOException, ServletException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        // get the container page itself
        CmsResource containerPage = cms.readResource(cms.getRequestContext().getUri());
        JSONObject localeData = LOADER.getCache(cms, containerPage, cms.getRequestContext().getLocale());
        JSONObject cntData = localeData.optJSONObject(CmsContainerPageLoader.N_CONTAINER);

        // get the container
        if (!cntData.has(containerName)) {
            LOG.warn(Messages.get().container(
                Messages.LOG_CONTAINER_NOT_FOUND_3,
                cms.getSitePath(containerPage),
                localeData.optString(CmsContainerPageLoader.N_LOCALE),
                containerName).key());
            return;
        }
        JSONObject container = cntData.optJSONObject(containerName);

        // validate the type
        if (!containerType.equals(container.optString(CmsContainerPageLoader.N_TYPE))) {
            LOG.warn(Messages.get().container(
                Messages.LOG_WRONG_CONTAINER_TYPE_4,
                new Object[] {
                    cms.getSitePath(containerPage),
                    localeData.optString(CmsContainerPageLoader.N_LOCALE),
                    containerName,
                    containerType}).key());
        }

        // get the maximal number of elements
        int maxElements = -1;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(containerMaxElements)) {
            try {
                maxElements = Integer.parseInt(containerMaxElements);
            } catch (NumberFormatException e) {
                LOG.warn(Messages.get().container(
                    Messages.LOG_WRONG_CONTAINER_MAXELEMENTS_4,
                    new Object[] {
                        cms.getSitePath(containerPage),
                        localeData.optString(CmsContainerPageLoader.N_LOCALE),
                        containerName,
                        containerMaxElements}).key());
            }
            // actualize the cache
            try {
                container.put(CmsContainerPageLoader.N_MAXELEMENTS, maxElements);
            } catch (JSONException e) {
                // should never happen
                LOG.warn(e.getMessage(), e);
            }
        }

        // iterate the elements
        JSONArray elements = container.optJSONArray(CmsContainerPageLoader.N_ELEMENT);
        // get the actual number of elements to render
        int renderElems = elements.length();
        if ((maxElements > -1) && (renderElems > maxElements)) {
            renderElems = maxElements;
        }

        for (int i = 0; i < renderElems; i++) {
            JSONObject element = elements.optJSONObject(i);
            String elementUri = element.optString(CmsContainerPageLoader.N_URI);

            CmsResource resUri = cms.readResource(elementUri);
            if (resUri.getTypeId() == CmsResourceTypeContainerPage.getStaticTypeId()) {
                // get the subcontainer data from cache
                JSONObject sublocaleData = LOADER.getCache(cms, resUri, cms.getRequestContext().getLocale());
                // get the first subcontainer
                JSONObject subcontainers = sublocaleData.optJSONObject(CmsContainerPageLoader.N_CONTAINER);
                JSONObject subcontainer = subcontainers.optJSONObject(sublocaleData.names().optString(0));
                // iterate the subelements
                JSONArray subelements = subcontainer.optJSONArray(CmsContainerPageLoader.N_ELEMENT);

                for (int j = 0; j < subelements.length(); j++) {
                    JSONObject subelement = subelements.optJSONObject(j);
                    String subelementUri = subelement.optString(CmsContainerPageLoader.N_URI);
                    String subelementFormatter = subelement.optString(CmsContainerPageLoader.N_FORMATTER);

                    CmsResource subresUri = cms.readResource(subelementUri);
                    // execute the formatter jsp for the given element uri
                    String jspResult = CmsADEElementManager.getInstance().getElementContent(
                        cms,
                        subresUri,
                        subelementFormatter,
                        (HttpServletRequest)req,
                        (HttpServletResponse)res);

                    // write the result
                    res.getWriter().println(jspResult);
                }
            } else {
                String elementFormatter = element.optString(CmsContainerPageLoader.N_FORMATTER);
                // execute the formatter jsp for the given element uri
                String jspResult = CmsADEElementManager.getInstance().getElementContent(
                    cms,
                    resUri,
                    elementFormatter,
                    (HttpServletRequest)req,
                    (HttpServletResponse)res);

                // write the result
                res.getWriter().println(jspResult);
            }

            // TODO: would not this be better??
            // CmsJspTagInclude.includeTagAction(pageContext, uri, null, false, null, request, response);
        }
    }

    /**
     * @return SKIP_BODY
     * @throws JspException in case something goes wrong
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                containerTagAction(pageContext, getName(), getType(), getMaxElements(), req, res);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "container"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the maxElements attribute value.<p>
     * 
     * @return the maxElements attribute value
     */
    public String getMaxElements() {

        return m_maxElements;
    }

    /**
     * Returns the name attribute value.<p>
     * 
     * @return String the name attribute value
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the type attribute value.<p>
     * 
     * @return the type attribute value
     */
    public String getType() {

        return m_type;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_type = null;
        m_name = null;
        m_maxElements = null;
    }

    /**
     * Sets the maxElements attribute value.<p>
     *
     * @param maxElements the maxElements value to set
     */
    public void setMaxElements(String maxElements) {

        m_maxElements = maxElements;
    }

    /**
     * Sets the name attribute value.<p>
     *
     * @param name the name value to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the type attribute value.<p>
     *
     * @param type the type value to set
     */
    public void setType(String type) {

        m_type = type;
    }

}
