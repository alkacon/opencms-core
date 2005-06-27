/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolMacroResolver.java,v $
 * Date   : $Date: 2005/06/27 23:22:07 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.workplace.CmsWorkplace;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Resolves special macros for the admin view.<p>
 * 
 * Supported macros are:<p>
 * <ul>
 *   <li>admin.userName|id</li>
 *   <li>admin.groupName|id</li>
 *   <li>admin.jobName|id</li>
 *   <li>admin.projectName|id</li>
 * </ul><p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * @since 6.0.0 
 */
public class CmsToolMacroResolver implements I_CmsMacroResolver {

    /** Identifier for admin macros prefix. */
    public static final String PREFIX_ADMIN = "admin.";

    /** Identifier for admin parameter names. */
    public static final String KEY_USERNAME = "userName.";

    /** Identifier for admin parameter names. */
    public static final String KEY_GROUPNAME = "groupName.";

    /** Identifier for admin parameter names. */
    public static final String KEY_JOBNAME = "jobName.";

    /** Identifier for admin parameter names. */
    public static final String KEY_PROJECTNAME = "projectName.";

    /** Identified for admin parameter commands. */
    public static final String[] VALUE_NAME_ARRAY = {KEY_USERNAME, KEY_GROUPNAME, KEY_JOBNAME, KEY_PROJECTNAME};

    /** The admin commands wrapped in a List. */
    public static final List VALUE_NAMES = Collections.unmodifiableList(Arrays.asList(VALUE_NAME_ARRAY));

    /** The workplace class for falling back, and use the cms context. */
    private CmsWorkplace m_wp;

    /**
     * Default private constructor.<p>
     * 
     * @param wp the workplace instance
     */
    public CmsToolMacroResolver(CmsWorkplace wp) {

        m_wp = wp;
    }

    /**
     * Resolves the macros in the given input using the provided parameters.<p>
     * 
     * A macro in the form <code>${key}</code> in the content is replaced with it's assigned value
     * returned by the <code>{@link I_CmsMacroResolver#getMacroValue(String)}</code> method of the given 
     * <code>{@link I_CmsMacroResolver}</code> instance.<p>
     * 
     * If a macro is found that can not be mapped to a value by the given macro resolver,
     * it is left untouched in the input.<p>
     * 
     * @param input the input in which to resolve the macros
     * @param wp the workplace class for falling back
     * 
     * @return the input with the macros resolved
     */
    public static String resolveMacros(String input, CmsWorkplace wp) {

        return new CmsToolMacroResolver(wp).resolveMacros(input);
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#getMacroValue(java.lang.String)
     */
    public String getMacroValue(String macro) {

        if (!macro.startsWith(CmsToolMacroResolver.PREFIX_ADMIN)) {
            // the key is not an admin macro, fallback
            return m_wp.getMacroResolver().getMacroValue(macro);
        }
        macro = macro.substring(CmsToolMacroResolver.PREFIX_ADMIN.length());
        String id = null;
        // validate macro command
        Iterator it = VALUE_NAMES.iterator();
        while (it.hasNext()) {
            String cmd = it.next().toString();
            if (macro.startsWith(cmd)) {
                id = macro.substring(cmd.length());
                macro = cmd;
            }
        }
        if (id == null) {
            // macro command not found
            return null;
        }
        try {
            if (macro == CmsToolMacroResolver.KEY_USERNAME) {
                return m_wp.getCms().readUser(new CmsUUID(id)).getName();
            }
            if (macro == CmsToolMacroResolver.KEY_GROUPNAME) {
                return m_wp.getCms().readGroup(new CmsUUID(id)).getName();
            }
            if (macro == CmsToolMacroResolver.KEY_PROJECTNAME) {
                return m_wp.getCms().readProject(new Integer(id).intValue()).getName();
            }
            if (macro == CmsToolMacroResolver.KEY_JOBNAME) {
                return OpenCms.getScheduleManager().getJob(id).getJobName();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * Resolves the macros in the given input.<p>
     * 
     * Calls <code>{@link #resolveMacros(String)}</code> until no more macros can 
     * be resolved in the input. This way "nested" macros in the input are resolved as well.<p> 
     * 
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        String result = input;

        if (input != null) {
            String lastResult;
            do {
                // save result for next comparison
                lastResult = result;
                // resolve the macros
                result = CmsMacroResolver.resolveMacros(result, this);
                // if nothing changes then the final result is found
            } while (!result.equals(lastResult));
        }
        // return the result
        return result;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return true;
    }
}