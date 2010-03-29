/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/rpc/Attic/I_CmsPublishServiceAsync.java,v $
 * Date   : $Date: 2010/03/29 08:47:34 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.publish.shared.rpc;

import org.opencms.ade.publish.shared.CmsClientPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishGroups;
import org.opencms.ade.publish.shared.CmsPublishOptionsAndProjects;
import org.opencms.ade.publish.shared.CmsPublishStatus;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The asynchronous interface to the publish service.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsPublishServiceAsync {

    /**
     * Asynchronous version of {@link I_CmsPublishService#getProjects()}.<p>
     * 
     * @param callback the result callback
     */
    void getProjects(AsyncCallback<Map<String, String>> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getPublishGroups(CmsClientPublishOptions)} .<p>
     * 
     * @param options the publish list options
     * @param callback the result callback
     */
    void getPublishGroups(CmsClientPublishOptions options, AsyncCallback<CmsPublishGroups> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getPublishOptions()}.<p>
     * @param callback the result callback
     */
    void getPublishOptions(AsyncCallback<CmsClientPublishOptions> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getPublishOptionsAndProjects()}.<p>
     * 
     * @param callback the result callback
     */
    void getPublishOptionsAndProjects(AsyncCallback<CmsPublishOptionsAndProjects> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#publishResources(List, List, boolean)}.<p>
     * 
     * @param toPublish the resources to publish 
     * @param toRemove the resources to remove
     * @param force if true, try to ignore broken links
     * @param callback the result callback 
     */
    void publishResources(
        List<String> toPublish,
        List<String> toRemove,
        boolean force,
        AsyncCallback<CmsPublishStatus> callback);

}
