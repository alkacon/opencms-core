/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapProvider.java,v $
 * Date   : $Date: 2010/05/03 14:33:05 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;

/**
 * Client side sitemap data provider.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapActionElement
 */
public final class CmsSitemapProvider extends CmsSitemapData {

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsSitemapProvider INSTANCE;

    /** The sitemap service instance. */
    private static I_CmsSitemapServiceAsync SERVICE;

    /**
     * Prevent instantiation.<p> 
     */
    protected CmsSitemapProvider() {

        super(deserialize());
    }

    /**
     * Deserializes the prefetched RPC data.<p>
     * 
     * @return the prefetched RPC data
     */
    private static CmsSitemapData deserialize() {

        String data = getPrefetchedData();
        SerializationStreamFactory streamFactory = (SerializationStreamFactory)getService();
        try {
            return (CmsSitemapData)streamFactory.createStreamReader(data).readObject();
        } catch (SerializationException e) {
            // should never happen
            CmsLog.log(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Retrieves the prefetched data from the host page.<p>
     */
    private static native String getPrefetchedData() /*-{
        return $wnd[@org.opencms.ade.sitemap.shared.CmsSitemapData::DICT_NAME];
    }-*/;

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsSitemapProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsSitemapProvider();
        }
        return INSTANCE;
    }

    /**
     * Returns the sitemap service instance.<p>
     * 
     * @return the sitemap service instance
     */
    public static I_CmsSitemapServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsSitemapService.class);
        }
        return SERVICE;
    }

    /**
     * Checks if the current sitemap is editable.<p>
     *
     * @return <code>true</code> if the current sitemap is editable
     */
    public boolean isEditable() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(getNoEditReason());
    }
}