/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/configuration/Attic/CmsTestMenuRules.java,v $
 * Date   : $Date: 2004/03/10 11:22:43 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import java.util.HashSet;
import java.util.Set;

/**
 * Test class for counting the different rule for the context menus.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.3
 */
public class CmsTestMenuRules {

    /** Stores all unique rule Strings */
    public Set m_rules = new HashSet();
    
    /**
     * Adds a rule String to the set.<p>
     * 
     * @param rule the rule to add
     */
    public void addRule(String rule) {
        m_rules.add(rule);
    }

}
