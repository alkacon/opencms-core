/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsUri.java,v $
* Date   : $Date: 2003/08/04 11:20:06 $
* Version: $Revision: 1.26 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.template.cache;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsResponse;
import com.opencms.file.CmsObject;
import com.opencms.template.CmsCacheDirectives;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An instance of CmsUri represents an requestable ressource in the OpenCms
 * element cache area. It points to the starting element and handles the access-
 * checks to this ressource in a simple way.
 *
 * If access is granted for the current user it starts the startingElement to
 * process the content of this ressource.
 *
 * @author Andreas Schouten
 */
public class CmsUri {

    /**
     * The Key to the Element used to start the contentgeneration for
     * this Uri.
     */
    private CmsElementDescriptor m_startingElement;

    /**
     * A Vector with Element Definitions. For normal URI's this Vector contains
     * only a definition for the body-element.
     */
    private CmsElementDefinitionCollection m_elementDefinitions = null;

    /**
     * Indicates if this uri object needs the https scheme.
     */
    private boolean m_https = false;


    /**
     * Constructor.
     *
     * @param startingElement the Element to start the contentgenerating for this uri.
     * @param readAccessGroup the Group that can read the uri.
     * @param def a content-definition for the an element (normaly body).
     */
    public CmsUri(CmsElementDescriptor startingElement, CmsElementDefinition def,
        boolean https){
        this(startingElement, new CmsElementDefinitionCollection(), https);
        m_elementDefinitions.add(def);
    }

    /**
     * Constructor.
     *
     * @param startingElement the Element to start the contentgenerating for this uri.
     * @param readAccessGroup the Group that can read the uri.
     * @param definitions a vector of definitions for elements.
     */
    public CmsUri(CmsElementDescriptor startingElement, CmsElementDefinitionCollection definitions,
        boolean https) {
        m_startingElement = startingElement;
        m_elementDefinitions = definitions;
        m_https = https;
    }

    public byte[] callCanonicalRoot(CmsElementCache elementCache, CmsObject cms, Hashtable parameters) throws CmsException  {
        // check for read access
        checkReadAccess(cms);
        // get the startelement. If it is not there it will be created.
        A_CmsElement elem = elementCache.getElementLocator().get(cms, m_startingElement, parameters);

        if(elem == null){
            throw new CmsException("Couldn't create start element for this uri, have a look at the log file for details.");
        }
        // put the elementdefinition infos in the parameter
        try {
            Enumeration els = m_elementDefinitions.getAllElementNames();
            while(els.hasMoreElements()){
                String currentElement = (String)els.nextElement();
                CmsElementDefinition eldef = m_elementDefinitions.get(currentElement);
                parameters.put(currentElement +"._TEMPLATE_", eldef.getTemplateName());
                parameters.put(currentElement +"._CLASS_", eldef.getClassName());
                if(eldef.getTemplateSelector()!= null) {
                    parameters.put(currentElement +"._TEMPLATESELECTOR_", eldef.getTemplateSelector());
                } else {
                    parameters.put(currentElement +"._TEMPLATESELECTOR_", "default");
                }
            }
        } catch(NullPointerException exc) {
            // no body-element found - ignoring
        }

        // check the proxistuff and set the response header
        CmsCacheDirectives proxySettings = new CmsCacheDirectives(true);
        elem.checkProxySettings(cms, proxySettings, parameters);

        // now for the subelements
        if(m_elementDefinitions != null){
            Enumeration elementNames = m_elementDefinitions.getAllElementNames();
            while(elementNames.hasMoreElements()){
                String name = (String)elementNames.nextElement();
                CmsElementDefinition currentDef = m_elementDefinitions.get(name);
                
                if (I_CmsConstants.C_XML_BODY_ELEMENT.equalsIgnoreCase(name)) {
                    // need to check for the body template here so that non-XMLTemplate templates
                    // like JSPs know where to find the body defined in the XMLTemplate
                    if (currentDef.getTemplateName() != null) {
                        cms.getRequestContext().setAttribute(I_CmsConstants.C_XML_BODY_ELEMENT, currentDef.getTemplateName());
                    }
                }
                                
                A_CmsElement currentEle = elementCache.getElementLocator().get(
                                        cms, new CmsElementDescriptor(currentDef.getClassName(),
                                        currentDef.getTemplateName()), parameters);
                if(currentEle == null){
                    throw new CmsException("Couldn't create element '"+name+"', have a look at the log file for details.");
                }
                currentEle.checkProxySettings(cms, proxySettings, parameters);
            }
        }

        I_CmsResponse resp = cms.getRequestContext().getResponse();
        // was there already a cache-control header set?
        if(!resp.containsHeader("Cache-Control")) {
            // only if the resource is cacheable and if the current project is online,
            // then the browser may cache the resource
            if(proxySettings.isProxyPrivateCacheable()
                        && cms.getRequestContext().currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID){
                // set max-age to 5 minutes. In this time a proxy may cache this content.
                resp.setHeader("Cache-Control", "max-age=300");
                if(!proxySettings.isProxyPublicCacheable()){
                    resp.addHeader("Cache-Control", "private");
                }
           }else{
                // set the http-header to pragma no-cache.
                //HTTP 1.1
                resp.setHeader("Cache-Control", "no-cache");
                //HTTP 1.0
                resp.setHeader("Pragma", "no-cache");
            }
        }


        return elem.getContent(elementCache, cms, m_elementDefinitions, I_CmsConstants.C_ROOT_TEMPLATE_NAME, parameters, null);
    }
    /**
     * checks the read access.
     * @param cms The cms Object for reading groups.
     * @throws CmsException if no read access.
     */
    public void checkReadAccess(CmsObject cms) throws CmsException{
        return;
//        if (m_readAccessGroup == null || "".equals(m_readAccessGroup )){
//            // everyone can read this
//            return;
//        }
//        CmsGroup currentGroup = cms.getRequestContext().currentGroup();
//        if (m_readAccessGroup.equals(currentGroup.getName())){
//            // easy: same group; access granted
//            return;
//        }
//        // maybe it is an Admin
//        if(currentGroup.getName().equals(I_CmsConstants.C_GROUP_ADMIN)){
//            // ok Admins can read everything
//            return;
//        }
//        // limited access and not the same group, but maybe parentgroup?
//        CmsGroup group1 = currentGroup;
//        CmsGroup group2 = cms.readGroup(m_readAccessGroup);
//        do{
//            group1 = cms.getParent(group1.getName());
//            if(group1 != null && group1.getId() == group2.getId()){
//                // is parent; access granted
//                return;
//            }
//        }while(group1 != null);
//
//        // maybe an other group of this user has access
//        Vector allGroups = cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName());
//        for(int i=0; i<allGroups.size(); i++){
//            if(m_readAccessGroup.equals(((CmsGroup)allGroups.elementAt(i)).getName())){
//                return;
//            }
//        }
//        // no way to read this sorry
//        throw new CmsException(currentGroup.getName()+" has no read access. ",
//                                CmsException.C_ACCESS_DENIED);
    }

    /**
     *
     */
    public boolean isHttpsResource(){
        return m_https;
    }
}