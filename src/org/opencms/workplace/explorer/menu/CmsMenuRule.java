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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer.menu;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Defines a named menu rule set to check the visibility of a single context menu item in the explorer view.<p>
 *
 * @since 6.5.6
 */
public class CmsMenuRule {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMenuRule.class);

    /** Indicates if the menu rule set is frozen. */
    private boolean m_frozen;

    /** The menu item rules that are part of this rule set. */
    private List<I_CmsMenuItemRule> m_menuItemRules;

    /** The name of the menu rule set. */
    private String m_name;

    /**
     * Constructor without parameters, needed for initialization from OpenCms configuration.<p>
     */
    public CmsMenuRule() {

        // initialize members
        m_menuItemRules = new ArrayList<I_CmsMenuItemRule>(5);
    }

    /**
     * Adds a single menu item rule to the list of rules.<p>
     *
     * @param menuItemRule the menu item rule to add
     */
    public void addMenuItemRule(I_CmsMenuItemRule menuItemRule) {

        try {
            m_menuItemRules.add(menuItemRule);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_INITIALIZE_MENUITEMRULE_1,
                        menuItemRule.getClass().getName()));
            }
        }
    }

    /**
     * Adds a single menu item rule to the list of rules.<p>
     *
     * @param className the class name of the menu item rule to add
     */
    public void addMenuItemRuleName(String className) {

        try {
            m_menuItemRules.add((I_CmsMenuItemRule)Class.forName(className).newInstance());
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INITIALIZE_MENUITEMRULE_1, className));
            }
        }
    }

    /**
     * Freezes the name and the items of the menu rule set.<p>
     *
     * They can not be modified anymore after calling this method.<p>
     */
    public void freeze() {

        if (!m_frozen) {
            m_frozen = true;
            // freeze the item rules list
            m_menuItemRules = Collections.unmodifiableList(m_menuItemRules);
        }
    }

    /**
     * Returns the first matching rule for the resource to create the context menu for.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceUtil the initialized resource utilities of the resource
     * @return the first matching rule for the resource
     */
    public I_CmsMenuItemRule getMatchingRule(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        Iterator<I_CmsMenuItemRule> i = getMenuItemRules().iterator();
        while (i.hasNext()) {
            I_CmsMenuItemRule rule = i.next();
            if (rule.matches(cms, resourceUtil)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * Returns the menu item rules class instances.<p>
     *
     * @return the menu item rules class instances
     */
    public List<I_CmsMenuItemRule> getMenuItemRules() {

        return m_menuItemRules;
    }

    /**
     * Returns the name of the menu rule set.<p>
     *
     * @return the name of the menu rule set
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns <code>true</code> if this menu rule set is frozen, that is read only.<p>
     *
     * @return <code>true</code> if this menu rule set is frozen, that is read only
     */
    public boolean isFrozen() {

        return m_frozen;
    }

    /**
     * Sets the menu item rules class instances.<p>
     *
     * @param menuItemRules the menu item rules class instances
     */
    public void setMenuItemRules(List<I_CmsMenuItemRule> menuItemRules) {

        m_menuItemRules = menuItemRules;
    }

    /**
     * Sets the name of the menu rule set.<p>
     *
     * @param name the name of the menu rule set
     */
    public void setName(String name) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_MENURULE_FROZEN_0));
        }
        m_name = name;
    }

}
