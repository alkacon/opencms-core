/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/I_CmsEventListener.java,v $
 * Date   : $Date: 2004/03/31 14:01:10 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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


/**
 * Implement this interface in case your class has to react 
 * to CmsEvents that are thrown by system.<p>
 * 
 * In order to recieve system events, your class must register with 
 * the OpenCms event mechanism. This can be done in the constructor of a class
 * like this:
 * <pre>
 * org.opencms.main.A_OpenCms.addCmsEventListener(this);
 * </pre>
 * 
 * A typical implementation might look like this:
 * <pre>
 * public void cmsEvent(com.opencms.flex.CmsEvent event) {
 *     switch (event.getType()) {
 *         case com.opencms.flex.I_CmsEventListener.EVENT_PUBLISH_PROJECT:
 *         case com.opencms.flex.I_CmsEventListener.EVENT_CLEAR_CACHES:
 *             // do something
 *             break;
 *         case com.opencms.flex.I_CmsEventListener.EVENT_LOGIN_USER:
 *            // do something else
 *             break;
 *         }
 * }
 * </pre>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.13 $
 * @since FLEX alpha 1
 * 
 * @see CmsEvent
 * @see org.opencms.main.OpenCms#addCmsEventListener(I_CmsEventListener)
 * @see org.opencms.main.OpenCms#addCmsEventListener(I_CmsEventListener, int[])
 */
public interface I_CmsEventListener {
    
    /**
     * Marker for "all events".<p>
     */
    Integer LISTENERS_FOR_ALL_EVENTS = new Integer(-1);
    
    /** 
     * Event "user has logged in".<p>
     *
     * @see org.opencms.file.CmsObject#loginUser(String, String) 
     */
    int EVENT_LOGIN_USER = 1;

    /**
     * Event "a project was published".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "publishHistoryId" (mandatory): the ID of the publish task in the publish history</li>
     * <li>key "report" (optional): an I_CmsReport instance to print output messages</li>
     * </ul>
     * @see org.opencms.file.CmsObject#publishProject()
     */    
    int EVENT_PUBLISH_PROJECT = 2;
    
    /** 
     * Event "a resource was published".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "resource": the published CmsResource</li>
     * </ul>
     * 
     * @see org.opencms.file.CmsObject#publishResource(String, boolean, org.opencms.report.I_CmsReport)
     */    
    int EVENT_PUBLISH_RESOURCE = 3; 
        
    /** 
     * Event "all caches must be cleared".<p>
     *
     * Not thrown by the core classes, but might be used in modules.
     */
    int EVENT_CLEAR_CACHES = 5;

    /** 
     * Event used to identify servers in the cluster.<p>
     */ 
    int EVENT_FLEX_CLUSTER_CHECK_SOURCE = 6;

    /** 
     * Event "an event has been forwarded to all servers in the cluster".<p>
     */      
    int EVENT_FLEX_CLUSTER_HOOK = 7;

    /** 
     * Event "delete all JSP pages in the "real" file system 
     * (so they will be rebuild next time the JSP is requested)".<p>
     * 
     * This is thrown on the "FlexCache Administration" page if you press
     * the button "Purge JSP repository", or if you use the <code>_flex=purge</code>
     * request parameter.
     */
    int EVENT_FLEX_PURGE_JSP_REPOSITORY = 8;
    
    /** 
     * Event "the FlexCache must be cleared".<p>
     * 
     * This is thrown on the "FlexCache Administration" page if you press
     * one ot the "Clear cache" buttons, or if you use the <code>_flex=clearcache</code>
     * request parameter.
     */
    int EVENT_FLEX_CACHE_CLEAR = 9;
    
    /** 
     * Event "static export has just happened".
     */
    int EVENT_STATIC_EXPORT = 10;
    
    /** 
     * Event "a single resource has been modified".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the modified CmsResource</li>
     * </ul>
     */
    int EVENT_RESOURCE_MODIFIED = 11;
    
    /** 
     * Event "a bunch of resources has been modified".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "resources" (mandatory): a List of modified CmsResources</li>
     * </ul>
     */
    int EVENT_RESOURCES_MODIFIED = 12;
    
    /** 
     * Event "the list of sub-resources of a folder has been modified", (e.g. a new resource has been created).<p>
     * 
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the modified CmsResource (a folder)</li>
     * </ul>
     */
    int EVENT_RESOURCE_LIST_MODIFIED = 13;
    
    /** 
     * Event "a single property (and so the resource itself, too) have been modified".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the CmsResource that has the modified property attached</li>
     * <li>key "property" (mandatory): the modified property</li>
     * </ul>
     */
    int EVENT_PROPERTY_MODIFIED = 14;
    
    /** 
     * Event "all properties (and so the resource itself, too) have been modified".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "resource" (mandatory): the CmsResource that has the modified properties attached</li>
     * </ul>
     */
    int EVENT_RESOURCE_AND_PROPERTIES_MODIFIED = 15;
    
    /** 
     * Event "clear all offline caches".<p>
     * 
     * Event data: none
     */
    int EVENT_CLEAR_OFFLINE_CACHES = 16;
    
    /** 
     * Event "clear all online caches".<p>
     * 
     * Event data: none
     */
    int EVENT_CLEAR_ONLINE_CACHES = 17;
    
    /**
     * Event "a project was modified" (e.g. a project has been deleted, 
     * or the project resources have been changed).<p>
     * 
     * Event data:
     * <ul>
     * <li>key "project" (mandatory): the deleted CmsProject</li>
     * </ul>
     */
    int EVENT_PROJECT_MODIFIED = 18;
    
    /**
     * Event "update exported resources".<p>
     * 
     * This event updates all export points, deletes the content
     * of the "export" folder, purges the JSP repository, and clears
     * all caches.<p>
     * 
     * This event is for internal use.<p>
     */     
    int EVENT_UPDATE_EXPORTS = 19;
    
    /**
     * Event "a project was published".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "publishHistoryId" (mandatory): the ID of the publish task in the publish history</li>
     * <li>key "report" (optional): an I_CmsReport instance to print output messages</li>
     * </ul>
     * @see org.opencms.file.CmsObject#publishProject()
     */    
    int EVENT_CLUSTER_PUBLISH_PROJECT = 20;    
    
    /** 
     * Event "validate the registry.xml".<p>
     * 
     * This event is forwarded to all servers in the cluster so that they
     * validate their registry.xml and sending in the response whether the
     * validation failed or not.<p>
     */ 
    int EVENT_FLEX_CLUSTER_CHECK_REGISTRY = 21;  
    
    /**
     * Event "update exported resources".<p>
     * 
     * This event updates all export points, deletes the content
     * of the "export" folder, purges the JSP repository, and clears
     * all caches.<p>
     * 
     * This event is forwarded to all servers in the cluster.<p>
     */    
    int EVENT_FLEX_CLUSTER_UPDATE_EXPORTS = 22;
   
    /**
     * Acknowledge the occurrence of the specified event, implement this 
     * method to check for CmsEvents in your class.
     *
     * @param event CmsEvent that has occurred
     */
    void cmsEvent(CmsEvent event);
    
    /**
     * @see #EVENT_RESOURCE_AND_PROPERTIES_MODIFIED
     */
    int EVENT_RESOURCE_CREATED = 23;
    
    /**
     * @see #EVENT_RESOURCES_MODIFIED
     */
    int EVENT_RESOURCE_COPIED = 24;
    
    /**
     * @see #EVENT_RESOURCES_MODIFIED
     */
    int EVENT_RESOURCE_DELETED = 25;

    /**
     * Event "a property definition has been modified".<p>
     * 
     * Event data:
     * <ul>
     * <li>key "propertyDefinition" (mandatory): the modified property definition</li>
     * </ul>
     */
    int EVENT_PROPERTY_DEFINITION_MODIFIED = 26;
    
}

