/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsContainerpageService.java,v $
 * Date   : $Date: 2010/04/12 15:00:37 $
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

package org.opencms.ade.containerpage;

import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.ade.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * The RPC service used by the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageService extends CmsGwtService implements I_CmsContainerpageService {

    /** Serial version UID. */
    private static final long serialVersionUID = -6188370638303594280L;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementsData(java.lang.String, java.lang.String, java.util.Collection, java.util.Set)
     */
    public Map<String, CmsContainerElement> getElementsData(
        String containerpageUri,
        String reqParams,
        Collection<String> clientIds,
        Set<String> containerTypes) throws CmsRpcException {

        try {
            return getElements(clientIds, containerpageUri, getRequest(), containerTypes);
        } catch (Exception e) {
            log(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    /**
     * Returns the data of the given elements.<p>
     * 
     * @param clientIds the list of IDs of the elements to retrieve the data for
     * @param uriParam the current URI
     * @param request the current request
     * @param types the container types to consider
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    protected Map<String, CmsContainerElement> getElements(
        Collection<String> clientIds,
        String uriParam,
        HttpServletRequest request,
        Set<String> types) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
        Map<String, CmsContainerElement> result = new HashMap<String, CmsContainerElement>();
        Set<String> ids = new HashSet<String>();
        Iterator<String> it = clientIds.iterator();
        while (it.hasNext()) {
            String elemId = it.next();
            if (ids.contains(elemId)) {
                continue;
            }
            CmsContainerElementBean element = getCachedElement(elemId);
            CmsContainerElement elementData = elemUtil.getElementData(element, types);
            result.put(element.getClientId(), elementData);
            if (elementData.isSubContainer()) {
                // this is a sub-container 

                CmsResource elementRes = cms.readResource(element.getElementId());
                CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(cms, elementRes, getRequest());
                CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                    cms,
                    cms.getRequestContext().getLocale());

                // adding all sub-items to the elements data
                for (CmsContainerElementBean subElement : subContainer.getElements()) {
                    if (!ids.contains(subElement.getElementId())) {
                        String subId = subElement.getClientId();
                        if (ids.contains(subId)) {
                            continue;
                        }
                        CmsContainerElement subItemData = elemUtil.getElementData(subElement, types);
                        ids.add(subId);
                        result.put(subId, subItemData);
                    }
                }
            }
            ids.add(elemId);
        }
        return result;
    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * 
     * @return the cached container element bean
     */
    protected CmsContainerElementBean getCachedElement(String clientId) {

        String id = clientId;
        CmsContainerElementBean element = null;
        element = getSessionCache().getCacheContainerElement(id);
        if (element != null) {
            return element;
        }
        if (id.contains("#")) {
            id = id.substring(0, id.indexOf("#"));
            element = getSessionCache().getCacheContainerElement(id);
            if (element != null) {
                return element;
            }
        }
        // this is necessary if the element has not been cached yet
        element = new CmsContainerElementBean(OpenCms.getADEManager().convertToServerId(id), null, null);
        getSessionCache().setCacheContainerElement(id, element);
        return element;
    }

    /**
     * Returns the session cache.<p>
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache() {

        if (m_sessionCache == null) {
            m_sessionCache = (CmsADESessionCache)getRequest().getSession().getAttribute(
                CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
            if (m_sessionCache == null) {
                m_sessionCache = new CmsADESessionCache(getCmsObject());
                getRequest().getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, m_sessionCache);
            }
        }
        return m_sessionCache;
    }

}
