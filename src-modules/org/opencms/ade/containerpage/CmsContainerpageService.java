/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsContainerpageService.java,v $
 * Date   : $Date: 2010/04/13 14:27:44 $
 * Version: $Revision: 1.4 $
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.ade.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The RPC service used by the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageService extends CmsGwtService implements I_CmsContainerpageService {

    /** Serial version UID. */
    private static final long serialVersionUID = -6188370638303594280L;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#addToFavoriteList(java.lang.String)
     */
    public void addToFavoriteList(String clientId) throws CmsRpcException {

        try {
            CmsContainerElementBean element = getCachedElement(clientId);
            List<CmsContainerElementBean> list = OpenCms.getADEManager().getFavoriteList(getCmsObject());
            if (list.contains(element)) {
                list.remove(list.indexOf(element));
            }
            list.add(0, element);
            OpenCms.getADEManager().saveFavoriteList(getCmsObject(), list);
        } catch (Exception e) {
            log(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#addToRecentList(java.lang.String)
     */
    public void addToRecentList(String clientId) {

        CmsContainerElementBean element = getCachedElement(clientId);
        List<CmsContainerElementBean> list = getSessionCache().getRecentList();
        if (list.contains(element)) {
            list.remove(list.indexOf(element));
        }
        list.add(0, element);
        getSessionCache().setCacheRecentList(list);

    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getElementsData(java.lang.String, java.lang.String, java.util.Collection, java.util.Set)
     */
    public Map<String, CmsContainerElement> getElementsData(
        String containerpageUri,
        String reqParams,
        Collection<String> clientIds,
        Set<String> containerTypes) throws CmsRpcException {

        try {
            return getElements(clientIds, containerpageUri, containerTypes);
        } catch (Exception e) {
            log(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getFavoriteList(java.lang.String, java.util.Set)
     */
    public LinkedHashMap<String, CmsContainerElement> getFavoriteList(
        String containerpageUri,
        Set<String> containerTypes) throws CmsRpcException {

        try {
            return getListElementsData(
                OpenCms.getADEManager().getFavoriteList(getCmsObject()),
                containerpageUri,
                containerTypes);
        } catch (Exception e) {
            log(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#getRecentList(java.lang.String, java.util.Set)
     */
    public LinkedHashMap<String, CmsContainerElement> getRecentList(String containerpageUri, Set<String> containerTypes)
    throws CmsRpcException {

        try {
            return getListElementsData(getSessionCache().getRecentList(), containerpageUri, containerTypes);
        } catch (Exception e) {
            log(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveFavoriteList(java.util.List)
     */
    public void saveFavoriteList(List<String> clientIds) throws CmsRpcException {

        try {
            OpenCms.getADEManager().saveFavoriteList(getCmsObject(), getCachedElements(clientIds));
        } catch (CmsException e) {
            log(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    /**
     * @see org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService#saveRecentList(java.util.List)
     */
    public void saveRecentList(List<String> clientIds) {

        getSessionCache().setCacheRecentList(getCachedElements(clientIds));

    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * 
     * @return the cached container element bean
     */
    private CmsContainerElementBean getCachedElement(String clientId) {

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
     * Returns a list of container elements from a list with client id's.<p>
     * 
     * @param clientIds list of client id's
     * 
     * @return a list of element beans
     */
    private List<CmsContainerElementBean> getCachedElements(List<String> clientIds) {

        List<CmsContainerElementBean> result = new ArrayList<CmsContainerElementBean>();
        for (String id : clientIds) {
            try {
                result.add(getCachedElement(id));
            } catch (CmsIllegalArgumentException e) {
                log(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the data of the given elements.<p>
     * 
     * @param clientIds the list of IDs of the elements to retrieve the data for
     * @param uriParam the current URI
     * @param types the container types to consider
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    private Map<String, CmsContainerElement> getElements(
        Collection<String> clientIds,
        String uriParam,
        Set<String> types) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, getRequest(), getResponse());
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
     * Returns the data of the given elements.<p>
     * 
     * @param listElements the list of element beans to retrieve the data for
     * @param containerpageUri the current URI
     * @param types the container types to consider
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     */
    private LinkedHashMap<String, CmsContainerElement> getListElementsData(
        List<CmsContainerElementBean> listElements,
        String containerpageUri,
        Set<String> containerTypes) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, containerpageUri, getRequest(), getResponse());
        LinkedHashMap<String, CmsContainerElement> result = new LinkedHashMap<String, CmsContainerElement>();

        for (CmsContainerElementBean element : listElements) {
            // checking if resource exists
            if (cms.existsResource(element.getElementId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                CmsContainerElement elementData = elemUtil.getElementData(element, containerTypes);
                result.put(element.getClientId(), elementData);
                if (elementData.isSubContainer()) {
                    // this is a sub-container 

                    CmsResource elementRes = cms.readResource(element.getElementId());
                    CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(
                        cms,
                        elementRes,
                        getRequest());
                    CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                        cms,
                        cms.getRequestContext().getLocale());

                    // adding all sub-items to the elements data
                    for (CmsContainerElementBean subElement : subContainer.getElements()) {

                        String subId = subElement.getClientId();

                        CmsContainerElement subItemData = elemUtil.getElementData(subElement, containerTypes);

                        result.put(subId, subItemData);

                    }
                }
            }
        }

        return result;
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
