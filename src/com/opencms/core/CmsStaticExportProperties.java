package com.opencms.core;
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsStaticExportProperties.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import java.util.*;

/**
 * This class provides a special data structure to access the static 
 * export properties read from <code>opencms.properties</code>.
 *
 * @author Hanjo Riege
 * 
 * @version $Revision: 1.3 $ $Date: 2003/07/31 13:19:37 $
 */

public class CmsStaticExportProperties {

    /**
     * the link in the static export that link to pages that are exportet too
     * are generated relative. This is only bugfree if the linkrules stay standard.
     */
    private static boolean c_exportRelativeLinks = false;

    /**
     * the vectors to store the three different rulesets needed for the link replacement.
     * Each vector contains a ruleset. The elements are regular expressions (Strings) the
     * way perl5 uses them.
     */
    private static String[] c_linkRulesExport = null;
    private static String[] c_linkRulesOnline = null;
    private static String[] c_linkRulesOffline = null;
    private static String[] c_linkRulesExtern = null;

    /**
     * the start rule for the extern and the export rules
     */
    private static String c_linkRuleStart = null;

    /**
     * Is the static export enabled or diabled
     */
    private static boolean c_staticExportEnabled = false;

    /**
     * is export=true the default value for the resources property "export"
     */
    private static boolean c_exportDefaultTrue = true;

    /**
     * the value of the exportEnabled parameter.
     */
    private static String c_staticExportEnabledValue = "";

    /**
     * The path to where the export will go
     */
    private static String c_staticExportPath = null;

    /**
     * The startpoints for the static export.
     */
    private static Vector c_staticExportStart = null;

    /**
     * contains the four url prefixe for the lnikreplacement.
     * That are the prefix for export, http, https and servername. The last
     * two are used only wenn https is needed.
     */
    private static String[] c_staticUrlPrefix = new String[4];

    /*****************************************************************
     * Constuctor.
     */
    public CmsStaticExportProperties() {
    }

    /**
     * Returns the exportpath for the static export.
     */
    public String getExportPath(){
        return c_staticExportPath;
    }

    /**
     * Returns the ruleset for link replacement.
     * @param state. defines which set is needed.
     * @return String[] the ruleset.
     */
    public static String[] getLinkRules(int state){

        if(state == I_CmsConstants.C_MODUS_ONLINE){
            return c_linkRulesOnline;
        }else if(state == I_CmsConstants.C_MODUS_OFFLINE){
            return c_linkRulesOffline;
        }else if(state == I_CmsConstants.C_MODUS_EXPORT){
            return c_linkRulesExport;
        }else if(state == I_CmsConstants.C_MODUS_EXTERN){
            return c_linkRulesExtern;
        }
        return null;
    }

    /**
     * Returns a Vector (of Strings) with the names of the vfs resources (files
     * and folders) where the export should start.
     *
     * @return Vector with resources for the export.
     */
    public Vector getStartPoints(){
        return c_staticExportStart;
    }

    /**
     * return the start rule used for export and extern mode.
     */
    public String getStartRule(){
        return c_linkRuleStart;
    }

    /**
     * Returns the value of the static export enable.
     * (needed for the false_ssl feature)
     */
    public String getStaticExportEnabledValue(){
        return c_staticExportEnabledValue;
    }

    /**
     * Gets the prefix array for the linkreplacement
     * @return String[4]
     */
    public String[] getUrlPrefixArray(){
        return c_staticUrlPrefix;
    }

    /**
     * returns true if the default value for the resource property "export"
     * is true.
     */
    public boolean isExportDefault(){
        return c_exportDefaultTrue;
    }

    /**
     * Returns true if the static export is enabled
     */
    public boolean isStaticExportEnabled(){
        return c_staticExportEnabled;
    }

    /**
     * Returns true if the links in the static export should be relative.
     */
    public boolean relativLinksInExport(){
        return c_exportRelativeLinks;
    }

    public void setExportDefaultValue(String value){
        if("dynamic".equalsIgnoreCase(value)){
            c_exportDefaultTrue = false;
        }else{
            c_exportDefaultTrue = true;
        }
    }
    public void setExportPath(String path){
        c_staticExportPath = path;
    }
    public void setExportRelativeLinks(boolean relLinks){
        c_exportRelativeLinks = relLinks;
    }
    public void setLinkRulesExport(String[] rule){
        c_linkRulesExport = rule;
    }
    public void setLinkRulesOnline(String[] rule){
        c_linkRulesOnline = rule;
    }
    public void setLinkRulesOffline(String[] rule){
        c_linkRulesOffline = rule;
    }
    public void setLinkRulesExtern(String[] rule){
        c_linkRulesExtern = rule;
    }
    public void setStartPoints(Vector sp){
        c_staticExportStart = sp;
    }
    public void setStartRule(String rule){
        c_linkRuleStart = rule;
    }
    public void setStaticExportEnabled(boolean active){
        c_staticExportEnabled = active;
    }
    public void setStaticExportEnabledValue(String value){
        c_staticExportEnabledValue = value;
    }
    public void setUrlPrefixArray(String[] urls){
        c_staticUrlPrefix = urls;
    }

}