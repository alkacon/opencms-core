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

package org.opencms.ade.configuration;

import java.util.List;

import com.google.common.base.Optional;

/**
 * Represents a sequence of inherited module/sitemap configurations, together with an index into that list.<p>
 *
 * Used for computing the configuration inheritance.
 *
 */
public class CmsADEConfigurationSequence {

    /** The list of configuration data. */
    private List<CmsADEConfigDataInternal> m_configDatas;

    /** The index for the current configuration. */
    private int m_configIndex;

    /**
     * Creates a new instance for the given list of configuration data, with the index pointing to the last element.<p>
     *
     * @param configDatas the config data list
     */
    public CmsADEConfigurationSequence(List<CmsADEConfigDataInternal> configDatas) {

        this(configDatas, configDatas.size() - 1);
    }

    /**
     * Creates a new instance.<p>
     *
     * @param configDatas the configuration data list
     * @param index the index into the list
     */
    protected CmsADEConfigurationSequence(List<CmsADEConfigDataInternal> configDatas, int index) {

        assert(0 <= index) && (index < configDatas.size());
        m_configDatas = configDatas;
        m_configIndex = index;

    }

    /**
     * Gets the current configuration data.<p>
     *
     * @return the current configuration data
     */
    public CmsADEConfigDataInternal getConfig() {

        return m_configDatas.get(m_configIndex);
    }

    /**
     * Returns a sequence which only differs from this instance in that its index is one less.<p>
     *
     * However, if the index of this instance is already 0, Optional.absent will be returned.
     *
     * @return the parent sequence, or Optional.absent if we are already at the start of the sequence
     */
    public Optional<CmsADEConfigurationSequence> getParent() {

        if (m_configIndex <= 0) {
            return Optional.absent();
        }
        return Optional.fromNullable(new CmsADEConfigurationSequence(m_configDatas, m_configIndex - 1));
    }

}
