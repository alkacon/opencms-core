/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/Attic/CmsMultiMessageBundle.java,v $
 * Date   : $Date: 2005/04/19 12:00:27 $
 * Version: $Revision: 1.1 $
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

package org.opencms.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Provides access to the localized messages for several message bundles simultaneously.<p>
 * 
 * @author  Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.7.3
 */
public class CmsMultiMessageBundle extends A_CmsMessageBundle {

    /** List of resource bundles . */
    private List m_messages;

    /**
     * Constructor for creating a new message bundle object
     * initialized with the provided message bundles.<p>
     * 
     * @param message1 a message bundle
     * @param message2 another message bundle
     */
    public CmsMultiMessageBundle(I_CmsMessageBundle message1, I_CmsMessageBundle message2) {

        this(new I_CmsMessageBundle[] {message1, message2});
    }

    /**
     * Constructor for creating a new message bundle object
     * initialized with the provided array of message bundles.<p>
     * 
     * @param messages array of <code>{@link I_CmsMessageBundle}</code>s, should not be null or empty
     */
    public CmsMultiMessageBundle(I_CmsMessageBundle[] messages) {

        this(Arrays.asList(messages));
    }

    /**
     * Constructor for creating a new message bundle object
     * initialized with the provided list of message bundles.<p>
     * 
     * @param messages list of <code>{@link I_CmsMessageBundle}</code>s, should not be null or empty
     */
    public CmsMultiMessageBundle(List messages) {

        m_messages = messages;
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#getBundle(java.util.Locale)
     */
    public CmsMessages getBundle(Locale locale) {

        // get bundles
        List bundles = new ArrayList();
        Iterator it = m_messages.iterator();
        while (it.hasNext()) {
            bundles.add(((I_CmsMessageBundle)it.next()).getBundle());
        }
        // create a multi bundle
        return new CmsMultiMessages(bundles);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#getBundleName()
     */
    public String getBundleName() {

        return "multi-bundle";
    }
}