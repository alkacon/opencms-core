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

package org.opencms.gwt.shared;

import java.util.List;
import java.util.Map;

/**
 * AutoBean interface for the data needed to open an embedded Vaadin dialog from GWT.
 */
public interface I_CmsEmbeddedDialogInfo {

    /**
     * Gets the context type.
     *
     * @return the context type
     */
    public String getContextType();

    /**
     * Gets the dialog id.
     *
     * @return the dialog id
     */
    public String getDialogId();

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters();

    /**
     * Gets the structure ids.
     *
     * @return the structure ids
     */
    public List<String> getStructureIds();

    /**
     * Sets the context type.
     *
     * @param contextType the new context type
     */
    public void setContextType(String contextType);

    /**
     * Sets the dialog id.
     *
     * @param dialogId the new dialog id
     */
    public void setDialogId(String dialogId);

    /**
     * Sets the parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(Map<String, String> parameters);

    /**
     * Sets the structure ids.
     *
     * @param structureIds the structure ids
     */
    public void setStructureIds(List<String> structureIds);
}
