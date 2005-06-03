/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/TestCmsModuleMessageBundles.java,v $
 * Date   : $Date: 2005/06/03 17:00:37 $
 * Version: $Revision: 1.3 $
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

/**
 * Tests all {@link org.opencms.i18n.I_CmsMessageBundle} instances for the OpenCms 
 * module classes (folder src-modules, org.* packages). <p>
 * 
 * @author Achim Westermann (a.westermann@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.9.1
 */
public final class TestCmsModuleMessageBundles extends TestCmsMessageBundles {

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getTestMessageBundles()
     */
    protected I_CmsMessageBundle[] getTestMessageBundles() {

        return new I_CmsMessageBundle[] {
            org.opencms.frontend.templateone.Messages.get(), 
            org.opencms.frontend.templateone.form.Messages.get(),
            org.opencms.frontend.templateone.modules.Messages.get(),
            org.opencms.workplace.administration.Messages.get(),
            org.opencms.workplace.tools.cache.Messages.get(),
            org.opencms.workplace.tools.content.Messages.get(),
            org.opencms.workplace.tools.history.Messages.get(),
            org.opencms.workplace.tools.modules.Messages.get(),
            org.opencms.workplace.tools.scheduler.Messages.get(),
            org.opencms.workplace.tools.searchindex.Messages.get(), 
            org.opencms.workplace.tools.staticexport.Messages.get(),
         //   org.opencms.workplace.tools.users.Messages.get(),
            org.opencms.workplace.tools.widgetdemo.Messages.get()};
    }

}
