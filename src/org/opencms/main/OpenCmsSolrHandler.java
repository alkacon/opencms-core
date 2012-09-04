/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsRequestUtil;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OpenCms Solr handler.<p>
 * 
 * Reachable under: "/opencms/opencms/handleSolrSelect".<p>
 * 
 * Usage example:<p>
 * <code>http://localhost:8080/opencms/opencms/handleSolrSelect?fq=parent-folders:/sites/+type=v8article&fl=path&rows=10&sort=path%20asc</code>
 */
public class OpenCmsSolrHandler implements I_CmsRequestHandler {

    /** Some HTML to use for display solr errors. */
    private static final String ERROR_HTML = Messages.get().getBundle().key(Messages.GUI_SOLR_ERROR_HTML_0);

    /** 
     * A constant for all handler names that are implemented by this class.<p>
     * This handler is reachable under "/opencms/opencms/handleSolrSelect".<p>
     */
    private final static String[] HANDLER_NAMES = new String[] {"SolrSelect"};

    /**
     * @see org.opencms.main.I_CmsRequestHandler#getHandlerNames()
     */
    public String[] getHandlerNames() {

        return HANDLER_NAMES;
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void handle(HttpServletRequest req, HttpServletResponse res, String name) throws IOException {

        try {
            CmsObject cms = OpenCmsCore.getInstance().initCmsObjectFromSession(req);
            if (cms == null) {
                res.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                String message = Messages.get().getBundle().key(Messages.GUI_SOLR_NOT_LOGGED_IN_0);
                res.getWriter().println(ERROR_HTML.replace("$content$", message));
            } else {
                Map<String, String[]> params = CmsRequestUtil.createParameterMap(req.getParameterMap());
                String indexName = params.get("core") != null ? params.get("core")[0] : (params.get("index") != null
                ? params.get("index")[0]
                : null);
                CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(params);
                if (index != null) {
                    CmsSolrQuery query = new CmsSolrQuery(cms, CmsRequestUtil.createParameterMap(req.getParameterMap()));
                    index.writeResponse(res, index.search(cms, query));
                } else {
                    res.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                    String message = Messages.get().getBundle().key(Messages.GUI_SOLR_INDEX_NOT_FOUND_1, indexName);
                    res.getWriter().println(ERROR_HTML.replace("$content$", message));
                }
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            String message = Messages.get().getBundle().key(Messages.GUI_SOLR_UNEXPECTED_ERROR_0);
            String formattedException = CmsException.getStackTraceAsString(e).replace("\n", "<br/>");
            res.getWriter().println(ERROR_HTML.replace("$content$", message + formattedException));
        } finally {
            res.getWriter().flush();
        }
    }
}
