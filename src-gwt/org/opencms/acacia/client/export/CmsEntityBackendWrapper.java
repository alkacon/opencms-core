/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.acacia.client.export;

import org.opencms.acacia.client.entity.CmsEntityBackend;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * Exportable wrapper class for the VIE instance.<p>
 */
@Export
@ExportPackage(value = "acacia")
public class CmsEntityBackendWrapper implements Exportable {

    /**
     * Default constructor.<p>
     */
    public CmsEntityBackendWrapper() {

    }

    /**
     * Wrapper method.<p>
     *
     * @param id parameter for the wrapped method
     * @param typeName parameter for the wrapped method
     *
     * @return the result of the wrapped method
     */
    public CmsEntityWrapper createEntity(String id, String typeName) {

        return new CmsEntityWrapper(CmsEntityBackend.getInstance().createEntity(id, typeName));
    }

    /**
     * Wrapper method.<p>
     *
     * @param id parameter for the wrapped method
     * @return the result of the wrapped method
     */
    public CmsTypeWrapper getType(String id) {

        return new CmsTypeWrapper(CmsEntityBackend.getInstance().getType(id));
    }
}
