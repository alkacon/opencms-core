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

package org.opencms.jlan;

import org.opencms.main.OpenCms;

import java.util.List;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.ShareMapper;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DefaultShareMapper;
import org.springframework.extensions.config.ConfigElement;

/**
 * Implementation of the JLAN ShareMapper interface which is used to generate the list
 * of available shares based on the OpenCms repository configuration in opencms-importexport.xml instead
 * of jlanConfig.xml.<p>
 */
public class CmsJlanShareMapper implements ShareMapper {

    /** An instance of the default share mapper. */
    public ShareMapper m_defaultMapper;

    /**
     * @see org.alfresco.jlan.server.core.ShareMapper#closeMapper()
     */
    public void closeMapper() {

        // TODO Auto-generated method stub

    }

    /**
     * @see org.alfresco.jlan.server.core.ShareMapper#deleteShares(org.alfresco.jlan.server.SrvSession)
     */
    public void deleteShares(SrvSession session) {

        // ignore
    }

    /**
     * @see org.alfresco.jlan.server.core.ShareMapper#findShare(java.lang.String, java.lang.String, int, org.alfresco.jlan.server.SrvSession, boolean)
     */
    public SharedDevice findShare(String host, String name, int type, SrvSession session, boolean create)
    throws Exception {

        SharedDeviceList shares = getShareList(host, session, true);
        return shares.findShare(name);

    }

    /**
     * @see org.alfresco.jlan.server.core.ShareMapper#getShareList(java.lang.String, org.alfresco.jlan.server.SrvSession, boolean)
     */
    public SharedDeviceList getShareList(String host, SrvSession sess, boolean allShares) {

        SharedDeviceList shrList = new SharedDeviceList();
        shrList.addShares(m_defaultMapper.getShareList(host, sess, allShares));
        List<CmsJlanRepository> repos = OpenCms.getRepositoryManager().getRepositories(CmsJlanRepository.class);
        for (CmsJlanRepository repo : repos) {
            shrList.addShare(repo.getSharedDevice());
        }
        return shrList;
    }

    /**
     * @see org.alfresco.jlan.server.core.ShareMapper#initializeMapper(org.alfresco.jlan.server.config.ServerConfiguration, org.springframework.extensions.config.ConfigElement)
     */
    public void initializeMapper(ServerConfiguration config, ConfigElement configElement)
    throws InvalidConfigurationException {

        m_defaultMapper = new DefaultShareMapper();
        m_defaultMapper.initializeMapper(config, configElement);
    }

}
