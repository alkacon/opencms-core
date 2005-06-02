/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/TestCmsCoreMessageBundles.java,v $
 * Date   : $Date: 2005/06/02 09:36:55 $
 * Version: $Revision: 1.2 $
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
 * core classes (folder src, org.* packages). <p>
 * 
 * @author Achim Westermann (a.westermann@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.9.1
 */
public final class TestCmsCoreMessageBundles extends TestCmsMessageBundles {

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getTestMessageBundles()
     */
    protected I_CmsMessageBundle[] getTestMessageBundles() {

        return new I_CmsMessageBundle[] {
            org.opencms.cache.Messages.get(),
            org.opencms.configuration.Messages.get(),
            org.opencms.db.Messages.get(),
            org.opencms.db.generic.Messages.get(),
            org.opencms.file.Messages.get(),
            org.opencms.file.collectors.Messages.get(),
            org.opencms.flex.Messages.get(),
            org.opencms.i18n.Messages.get(),
            org.opencms.importexport.Messages.get(),
            org.opencms.jsp.Messages.get(),
            org.opencms.loader.Messages.get(),
            org.opencms.lock.Messages.get(),
            org.opencms.mail.Messages.get(),
            org.opencms.main.Messages.get(),
            org.opencms.module.Messages.get(),
            org.opencms.monitor.Messages.get(),
            org.opencms.scheduler.Messages.get(),
            org.opencms.search.Messages.get(),
            org.opencms.search.documents.Messages.get(),
            org.opencms.security.Messages.get(),
            org.opencms.setup.Messages.get(),
            org.opencms.site.Messages.get(),
            org.opencms.staticexport.Messages.get(),
            org.opencms.synchronize.Messages.get(),
            org.opencms.workplace.threads.Messages.get(),
            org.opencms.util.Messages.get(),
            org.opencms.validation.Messages.get(),
            org.opencms.workflow.Messages.get(),
            org.opencms.workplace.Messages.get(),
            org.opencms.workplace.commons.Messages.get(),
            org.opencms.workplace.explorer.Messages.get(),
            org.opencms.workplace.tools.Messages.get(),
            org.opencms.workplace.list.Messages.get(),
            org.opencms.widgets.Messages.get(),
            org.opencms.xml.Messages.get(),
            org.opencms.xml.content.Messages.get(),
            org.opencms.xml.page.Messages.get(),
            org.opencms.xml.types.Messages.get()};
    }

}
