/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsEditProperties.java,v $
 * Date   : $Date: 2011/05/26 08:26:40 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler;
import org.opencms.gwt.client.property.CmsVfsModePropertyEditor;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

/**
 * The class for the "edit properties" context menu entries in the container page editor.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsEditProperties {

    /**
     * Starts the property editor for the resource with the given structure id.<p>
     * 
     * @param id the structure id of a resource 
     */
    public void editProperties(final CmsUUID id) {

        CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

            @Override
            public void execute() {

                start(300, false);
                CmsCoreProvider.getVfsService().loadPropertyData(id, this);
            }

            @Override
            protected void onResponse(CmsPropertiesBean result) {

                stop(false);
                CmsSimplePropertyEditorHandler handler = new CmsSimplePropertyEditorHandler();
                handler.setPropertiesBean(result);
                CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(result.getPropertyDefinitions(), handler);
                editor.setShowResourceProperties(!handler.isFolder());
                editor.start();
            }
        };
        action.execute();
    }

}
