/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/control/Attic/CmsPropertyModification.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsPropertyModificationData;

import java.util.Map;

/**
 * A class which represents a property modification.<p>
 * 
 * @author Georg Westenberger 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsPropertyModification {

    /** The data of the property modification. */
    private CmsPropertyModificationData m_data;

    /**
     * Creates a new property modification object.<p>
     * 
     * @param name the path identifying the change location 
     * @param value the new property value 
     */
    public CmsPropertyModification(String name, String value) {

        m_data = new CmsPropertyModificationData(name, value);
    }

    /**
     * Applies the change to the model.<p>
     */
    public void execute() {

        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
        CmsClientSitemapEntry entry = controller.getEntryById(m_data.getId());
        if (entry != null) {
            Map<String, CmsClientProperty> props = entry.getOwnInternalProperties();
            internalUpdateProperty(props);
        }
        CmsClientSitemapEntry defaultEntry = controller.getEntryByDefaultFileId(m_data.getId());
        if (defaultEntry != null) {
            Map<String, CmsClientProperty> props = defaultEntry.getDefaultFileInternalProperties();
            internalUpdateProperty(props);
        }
    }

    /**
     * Returns the property modification data.<p>
     * 
     * @return the property modification data 
     */
    public CmsPropertyModificationData getData() {

        return m_data;
    }

    /**
     * Helper method for applying the change to a property map.<p>
     * 
     * @param props a map of properties 
     */
    private void internalUpdateProperty(Map<String, CmsClientProperty> props) {

        CmsClientProperty prop = props.get(m_data.getName());
        if (prop == null) {
            prop = new CmsClientProperty(m_data.getName(), "", "");
            props.put(m_data.getName(), prop);
        }
        if (m_data.isStructureValue()) {
            prop.setStructureValue(m_data.getValue());
        } else {
            prop.setResourceValue(m_data.getValue());
            prop.setStructureValue(m_data.getValue());
        }
    }

}
