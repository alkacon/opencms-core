/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsConfigurationCopyResource.java,v $
 * Date   : $Date: 2005/03/23 19:08:23 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.file.types.A_CmsResourceType;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsMacroResolver;

/**
 * Describes a resource to copy during the creation of a new resource.<p>
 * 
 * Usually used in folder types to copy some default resources to the folder,
 * but also usable for file resources.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.7.2
 */
public class CmsConfigurationCopyResource {

    /** Indicates "copy resources" should be copied with type {@link I_CmsConstants#C_COPY_AS_NEW}. */
    public static final String COPY_AS_NEW = "new";

    /** Indicates "copy resources" should be copied with type {@link I_CmsConstants#C_COPY_PRESERVE_SIBLING}. */
    public static final String COPY_AS_PRESERVE = "preserve";

    /** Indicates "copy resources" should be copied with type {@link I_CmsConstants#C_COPY_AS_SIBLING}. */
    public static final String COPY_AS_SIBLING = "sibling";

    /** The source resource. */
    private String m_source;

    /** The target resource (may contain macros). */
    private String m_target;

    /** Indicates that the original configured target was <code>null</code>.*/
    private boolean m_targetWasNull;

    /** The type of the copy, for example "as new", "as sibling" etc.*/
    private int m_type;

    /** Indicates that the original configured type setting was <code>null</code>.*/
    private boolean m_typeWasNull;

    /**
     * Creates a new copy resource info container.<p>
     * 
     * If target is <code>null</code>, the macro {@link A_CmsResourceType#MACRO_RESOURCE_FOLDER_PATH} is used as default.
     * If type is <code>null</code>, the copy type {@link I_CmsConstants#C_COPY_AS_NEW} is used as default.<p>
     * 
     * @param source the source resource
     * @param target the target resource (may contain macros)
     * @param type the type of the copy, for example "as new", "as sibling" etc
     */
    public CmsConfigurationCopyResource(String source, String target, String type) {

        m_source = source;

        if (target == null) {
            m_target = CmsMacroResolver.formatMacro(A_CmsResourceType.MACRO_RESOURCE_FOLDER_PATH);
            m_targetWasNull = true;
        } else {
            m_target = target;
        }

        m_type = I_CmsConstants.C_COPY_AS_NEW;
        if (type != null) {
            if (type.equalsIgnoreCase(CmsConfigurationCopyResource.COPY_AS_SIBLING)) {
                m_type = I_CmsConstants.C_COPY_AS_SIBLING;
            } else if (type.equalsIgnoreCase(CmsConfigurationCopyResource.COPY_AS_PRESERVE)) {
                m_type = I_CmsConstants.C_COPY_PRESERVE_SIBLING;
            }
        } else {
            m_typeWasNull = true;
        }
    }

    /**
     * Returns the source resource.<p>
     * 
     * @return the source resource
     */
    public String getSource() {

        return m_source;
    }

    /**
     * Returns the target resource (may contain macros).<p>
     * 
     * @return the target resource (may contain macros)
     */
    public String getTarget() {

        return m_target;
    }

    /**
     * Returns the type of the copy, for example "as new", "as sibling" etc.<p>
     * 
     * Possible types are {@link org.opencms.main.I_CmsConstants#C_COPY_AS_NEW}, {@link org.opencms.main.I_CmsConstants#C_COPY_AS_SIBLING},
     * {@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}.<p>
     * 
     * @return the type of the copy, for example "as new", "as sibling" etc
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the copy type as String.<p>
     * 
     * @see #getType()
     * 
     * @return the copy type as String
     */
    public String getTypeString() {

        if (I_CmsConstants.C_COPY_AS_SIBLING == m_type) {
            return CmsConfigurationCopyResource.COPY_AS_SIBLING;
        } else if (I_CmsConstants.C_COPY_PRESERVE_SIBLING == m_type) {
            return CmsConfigurationCopyResource.COPY_AS_PRESERVE;
        }
        return CmsConfigurationCopyResource.COPY_AS_NEW;
    }

    /**
     * Returns <code>true</code> if the orginal target configuration was <code>null</code>.<p>
     *
     * @return  <code>true</code> if the orginal target configuration was <code>null</code>
     */
    public boolean isTargetWasNull() {

        return m_targetWasNull;
    }

    /**
     * Returns <code>true</code> if the orginal type configuration was <code>null</code>.<p>
     *
     * @return  <code>true</code> if the orginal type configuration was <code>null</code>
     */
    public boolean isTypeWasNull() {

        return m_typeWasNull;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append("[");
        result.append(this.getClass().getName());
        result.append(", source=");
        result.append(getSource());
        result.append(", target=");
        result.append(getTarget());
        result.append(", type=");
        result.append(getTypeString());
        result.append("]");

        return result.toString();
    }
}