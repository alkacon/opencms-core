/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsStaticExportProperties.java,v $
 * Date   : $Date: 2003/08/06 16:32:48 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import com.opencms.core.I_CmsConstants;

import java.util.Vector;

/**
 * Provides a data structure to access the static 
 * export properties read from <code>opencms.properties</code>.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class CmsStaticExportProperties {

    /** Indicates if <code>true</code> is the default value for "export" */
    private boolean m_exportDefaultTrue;

    /** Indicates if links in the static export should be relative */
    private boolean m_exportRelativeLinks;

    // link rules arrays
    private String[] m_linkRulesExport;
    private String[] m_linkRulesExtern;
    private String[] m_linkRulesOffline;
    private String[] m_linkRulesOnline;

    /** The start rule for the extern and the export rules */
    private String m_linkRuleStart;

    /** Indicates if the static export is enabled or diabled */
    private boolean m_staticExportEnabled;

    /** The path to where the static export will be written */
    private String m_staticExportPath;

    /** The starting points for the static export */
    private Vector m_staticExportStart;

    /** Prefix to use for exported files */
    private String m_exportPrefix;
    
    /** Prefix to use for internal OpenCms files */
    private String m_internPrefix;

    /**
     * Creates a new static export property object.<p>
     */
    public CmsStaticExportProperties() {
        m_exportRelativeLinks = false;
        m_staticExportEnabled = false;
        m_exportDefaultTrue = true;
    }

    /**
     * Returns the selected ruleset for link replacement.<p>
     * 
     * @param state defines which ruleset is needed
     * @return the selected ruleset for link replacement
     */
    public String[] getLinkRules(int state) {
        if (state == I_CmsConstants.C_MODUS_ONLINE) {
            return m_linkRulesOnline;
        } else if (state == I_CmsConstants.C_MODUS_OFFLINE) {
            return m_linkRulesOffline;
        } else if (state == I_CmsConstants.C_MODUS_EXPORT) {
            return m_linkRulesExport;
        } else if (state == I_CmsConstants.C_MODUS_EXTERN) {
            return m_linkRulesExtern;
        }
        return null;
    }

    /**
     * Returns the export path for the static export.<p>
     * 
     * @return the export path for the static export
     */
    public String getExportPath() {
        return m_staticExportPath;
    }
    
    /**
     * Returns the prefix for exported links.<p>
     * 
     * @return the prefix for exported links
     */ 
    public String getExportPrefix() {
        return m_exportPrefix;
    }

    /**
     * Returns the prefix for internal links.<p>
     * 
     * @return the prefix for internal links
     */
    public String getInternPrefix() {
        return m_internPrefix;
    }

    /**
     * Returns a Vector (of Strings) with the names of the VFS resources (files
     * and folders) where the export should start.<p>
     *
     * @return a Vector with the resources where the export should start
     */
    public Vector getStartPoints() {
        return m_staticExportStart;
    }

    /**
     * Return the start rule used for export and extern mode.<p>
     * 
     * @return the start rule used for export and extern mode
     */
    public String getStartRule() {
        return m_linkRuleStart;
    }

    /**
     * Returns true if the default value for the resource property "export" is true.<p>
     * 
     * @return true if the default value for the resource property "export" is true
     */
    public boolean isExportDefault() {
        return m_exportDefaultTrue;
    }

    /**
     * Returns true if the static export is enabled.<p>
     * 
     * @return true if the static export is enabled
     */
    public boolean isStaticExportEnabled() {
        return m_staticExportEnabled;
    }

    /**
     * Returns true if the links in the static export should be relative.<p>
     * 
     * @return true if the links in the static export should be relative
     */
    public boolean relativLinksInExport() {
        return m_exportRelativeLinks;
    }
    
    /**
     * Sets the default for the "export" resource property, 
     * possible values are "true", "false" or "dynamic".<p>
     *  
     * @param value the default for the "export" resource property
     */
    public void setExportDefaultValue(String value) {
        if ("dynamic".equalsIgnoreCase(value)) {
            m_exportDefaultTrue = false;
        } else {
            m_exportDefaultTrue = true;
        }
    }
    
    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    public void setExportPath(String path) {
        m_staticExportPath = path;
    }

    /**
     * Sets the prefix for exported links.<p>
     * 
     * @param exportPrefix the prefix for exported links
     */
    public void setExportPrefix(String exportPrefix) {
        m_exportPrefix = exportPrefix;
    }
    
    /**
     * Controls if links in exported files are relative or absolute.<p>
     * 
     * @param value if true, links in exported files are relative
     */
    public void setExportRelativeLinks(boolean value) {
        m_exportRelativeLinks = value;
    }

    /**
     * Sets the prefix for internal links.<p>
     * 
     * @param internPrefix the prefix for internal links
     */
    public void setInternPrefix(String internPrefix) {
        m_internPrefix = internPrefix;
    }
    
    /**
     * Sets the export link rules.<p>
     * 
     * @param rules the export link rules
     */
    public void setLinkRulesExport(String[] rules) {
        m_linkRulesExport = rules;
    }
    
    /**
     * Sets the external link rules.<p> 
     *  
     * @param rules the external link rules
     */
    public void setLinkRulesExtern(String[] rules) {
        m_linkRulesExtern = rules;
    }
    
    /**
     * Sets the Offline link rules.<p>
     * 
     * @param rules the Offline link rules
     */
    public void setLinkRulesOffline(String[] rules) {
        m_linkRulesOffline = rules;
    }
    
    /**
     * Sets the online link rules.<p>
     * 
     * @param rules the online link rules
     */
    public void setLinkRulesOnline(String[] rules) {
        m_linkRulesOnline = rules;
    }
    
    /**
     * Sets the starting point Vector for the static export.<p>
     * 
     * @param startPoints the starting point Vector for the static export
     */
    public void setStartPoints(Vector startPoints) {
        m_staticExportStart = startPoints;
    }
    
    /**
     * Sets the start rule for the static export.<p>
     * 
     * @param rule the start rule for the staitc export
     */
    public void setStartRule(String rule) {
        m_linkRuleStart = rule;
    }
    
    /**
     * Controls if the static export is enabled or not.<p>
     * 
     * @param value if true, the static export is enabled
     */
    public void setStaticExportEnabled(boolean value) {
        m_staticExportEnabled = value;
    }
}