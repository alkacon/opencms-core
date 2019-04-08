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

package org.opencms.xml.xml2json;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles /json requests.
 */
public class CmsJsonResourceHandler implements I_CmsResourceInit {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonResourceHandler.class);

    /** URL prefix. */
    public static final String PREFIX = "/json";

    /** Service loader used to load external JSON handler classes. */
    private ServiceLoader<I_CmsJsonHandlerProvider> m_serviceLoader = ServiceLoader.load(
        I_CmsJsonHandlerProvider.class);

    /**
     * Gets the list of sub-handlers, sorted by ascending order.
     *
     * @return the sorted list of sub-handlers
     */
    public List<I_CmsJsonHandler> getSubHandlers() {

        List<I_CmsJsonHandler> result = new ArrayList<>(CmsDefaultJsonHandlers.getHandlers());
        for (I_CmsJsonHandlerProvider provider : m_serviceLoader) {
            result.addAll(provider.getJsonHandlers());
        }

        result.sort((h1, h2) -> Double.compare(h1.getOrder(), h2.getOrder()));
        return result;

    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource origRes, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException, CmsSecurityException {

        String uri = cms.getRequestContext().getUri();

        if (origRes != null) {
            return origRes;
        }
        if (res == null) {
            // called from locale handler
            return origRes;
        }
        if (!CmsStringUtil.isPrefixPath(PREFIX, uri)) {
            return null;
        }
        String path = uri.substring(PREFIX.length());
        if (path.isEmpty()) {
            path = "/";
        } else if (path.length() > 1) {
            path = CmsFileUtil.removeTrailingSeparator(path);
        }
        Map<String, String> singleParams = new HashMap<>();
        // we don't care about multiple parameter values, single parameter values are easier to work with
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String[] data = entry.getValue();
            String value = null;
            if (data.length > 0) {
                value = data[0];
            }
            singleParams.put(entry.getKey(), value);
        }

        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsResource resource = rootCms.readResource(path);
            CmsJsonHandlerContext context = new CmsJsonHandlerContext(cms, path, resource, singleParams);
            String encoding = "UTF-8";
            res.setContentType("application/json; charset=" + encoding);
            boolean foundHandler = false;
            for (I_CmsJsonHandler handler : getSubHandlers()) {
                if (handler.matches(context)) {
                    CmsJsonResult result = handler.renderJson(context);
                    if (result.getNextResource() != null) {
                        return result.getNextResource();
                    } else {
                        PrintWriter writer = res.getWriter();
                        writer.write(result.getJson().toString());
                        writer.flush();
                        res.setStatus(result.getStatus());
                        foundHandler = true;
                        break;
                    }
                }
            }
            if (!foundHandler) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            CmsResourceInitException ex = new CmsResourceInitException(CmsJsonResourceHandler.class);
            ex.setClearErrors(res.getStatus() == HttpServletResponse.SC_OK);
            throw ex;
        } catch (CmsSecurityException e) {
            throw e;
        } catch (CmsResourceInitException e) {
            throw e;
        } catch (Exception e) {
            CmsMessageContainer msg = org.opencms.ade.detailpage.Messages.get().container(
                org.opencms.ade.detailpage.Messages.ERR_RESCOURCE_NOT_FOUND_1,
                uri);
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsResourceInitException(msg, e);
        }
    }

}
