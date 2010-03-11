/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/Attic/CmsSitemapService.java,v $
 * Date   : $Date: 2010/03/11 11:26:12 $
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

package org.opencms.ade;

import org.opencms.ade.shared.CmsClientSitemapEntry;
import org.opencms.ade.shared.rpc.I_CmsSitemapService;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.sitemap.CmsSitemapEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.CmsSitemapService
 * @see org.opencms.ade.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.shared.rpc.I_CmsSitemapServiceAsync
 */
public class CmsSitemapService extends CmsGwtService implements I_CmsSitemapService {

    /** Serialization uid. */
    private static final long serialVersionUID = -7136544324371767330L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapService.class);

    /**
     * @see org.opencms.ade.shared.rpc.I_CmsSitemapService#getSitemapEntry(String)
     */
    public CmsClientSitemapEntry getSitemapEntry(String root) throws CmsRpcException {

        try {
            return toGwtEntry(OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), root));
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.ade.shared.rpc.I_CmsSitemapService#getSitemapChildren(java.lang.String)
     */
    public CmsClientSitemapEntry[] getSitemapChildren(String root) throws CmsRpcException {

        try {
            CmsSitemapEntry entry = OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), root);
            List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>(entry.getSubEntries().size());
            for (CmsSitemapEntry child : entry.getSubEntries()) {
                children.add(toGwtEntry(child));
            }
            return children.toArray(new CmsClientSitemapEntry[0]);
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * Converts a site entry bean into a JSON object.<p>
     * 
     * @param entry the entry to convert
     * 
     * @return the JSON representation, can be <code>null</code> in case of not enough permissions
     */
    protected CmsClientSitemapEntry toGwtEntry(CmsSitemapEntry entry) {

        CmsClientSitemapEntry gwtEntry = new CmsClientSitemapEntry();
        gwtEntry.setId(entry.getId().toString());
        gwtEntry.setName(entry.getName());
        gwtEntry.setTitle(entry.getTitle());
        gwtEntry.setResourceId(entry.getResourceId().toString());
        gwtEntry.setProperties(new HashMap<String, String>(entry.getProperties()));
        gwtEntry.setSitePath(entry.getSitePath(getCmsObject()));
        return gwtEntry;
    }
}
