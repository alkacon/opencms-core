/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/Attic/TestCmsMessageContainerCompound.java,v $
 * Date   : $Date: 2005/05/31 11:08:23 $
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

package org.opencms.i18n;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Comment for <code>TestCmsMessageContainerCompound</code>.<p>
 */
public class TestCmsMessageContainerCompound extends TestCase {

    private CmsMessageContainerCompound m_test;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();
        m_test = new CmsMessageContainerCompound();
        m_test.addNewline();
        m_test.addSpace();
        m_test.addSpace();
        m_test.add(org.opencms.main.Messages.get().container(
            org.opencms.main.Messages.ERR_ALREADY_INITIALIZED_0));
        m_test.addNewline();
        m_test.addSpace();
        m_test.addSpace();
        m_test.add(org.opencms.db.Messages.get().container(
            org.opencms.db.Messages.ERR_ACCEPT_TASK_1,
            "SomeArgument"));
        m_test.addNewline();
        m_test.addSpace();
        m_test.addSpace();
        m_test.add(org.opencms.file.Messages.get().container(
            org.opencms.file.Messages.ERR_UNKNOWN_RESOURCE_TYPE_1,
            "SomeArgument"));
        m_test.addNewline();
        m_test.addSpace();
        m_test.addSpace();
        m_test.add(org.opencms.workplace.Messages.get().container(
            org.opencms.workplace.Messages.INIT_ADD_DIALOG_HANDLER_2,
            "SomeArgument",
            "AnotherArgument"));
    }

    /**
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {

        super.tearDown();
        m_test = null;
    }

    /**
     * Invokes the {@link CmsMessageContainerCompound#key()} method for a compound 
     * message spread over several packages (bundles) with different amount of args. <p>
     *
     */
    public void testKey() {

        assertNotNull(m_test);
        String msg = m_test.key();
        assertTrue("The message has a length of zero.", msg.length() > 0);
        System.out.println("Message.key(): ");
        System.out.println(msg);
    }

    /**
     * Invokes the {@link CmsMessageContainerCompound#key(Locale)} method
     * with all available locales for a compound message spread over several packages (bundles) with different amount of args. <p>
     *
     */
    public void testKeyLocale() {

        assertNotNull(m_test);
        Locale[] locales = Locale.getAvailableLocales();
        String msg;
        for (int i = 0; i < locales.length; i++) {
            msg = m_test.key(locales[i]);
            assertTrue("The message has a length of zero.", msg.length() > 0);
            System.out.println("\nMessage.key(" + locales[i].getDisplayLanguage() + "): ");
            System.out.println(msg);
        }
    }
}
