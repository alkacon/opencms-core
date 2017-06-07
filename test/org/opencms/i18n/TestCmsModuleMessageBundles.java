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

package org.opencms.i18n;

import org.opencms.gwt.I_CmsClientMessageBundle;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Tests all {@link org.opencms.i18n.I_CmsMessageBundle} instances for the OpenCms
 * module classes (folder src-modules, org.* packages). <p>
 *
 * @since 6.0.0
 */
public final class TestCmsModuleMessageBundles extends TestCmsMessageBundles {

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getNotLocalizedBundles(Locale)
     */
    @Override
    protected List<I_CmsMessageBundle> getNotLocalizedBundles(Locale locale) {

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getTestClientMessageBundles()
     */
    @Override
    protected List<I_CmsClientMessageBundle> getTestClientMessageBundles() throws Exception {

        List<I_CmsClientMessageBundle> result = new ArrayList<I_CmsClientMessageBundle>();
        List<String> classNames = OpenCmsTestCase.getClassNames();
        for (String className : classNames) {
            if (className.endsWith("ClientMessages")) {
                Class<?> cls = Class.forName(className);
                try {
                    Object instance = cls.getMethod("get", new Class[] {}).invoke(null);
                    result.add((I_CmsClientMessageBundle)instance);
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getTestMessageBundles()
     */
    @Override
    protected I_CmsMessageBundle[] getTestMessageBundles() {

        return new I_CmsMessageBundle[] {
            org.opencms.editors.codemirror.Messages.get(),
            org.opencms.ugc.Messages.get(),
            org.opencms.workplace.tools.searchindex.sourcesearch.Messages.get(),
            org.opencms.workplace.tools.workplace.logging.Messages.get(),
            org.opencms.workplace.administration.Messages.get(),
            org.opencms.workplace.tools.accounts.Messages.get(),
            org.opencms.workplace.tools.cache.Messages.get(),
            org.opencms.workplace.tools.content.Messages.get(),
            org.opencms.workplace.tools.content.check.Messages.get(),
            org.opencms.workplace.tools.content.convertxml.Messages.get(),
            org.opencms.workplace.tools.content.languagecopy.Messages.get(),
            org.opencms.workplace.tools.content.updatexml.Messages.get(),
            org.opencms.workplace.tools.content.propertyviewer.Messages.get(),
            org.opencms.workplace.tools.database.Messages.get(),
            org.opencms.workplace.tools.galleryoverview.Messages.get(),
            org.opencms.workplace.tools.history.Messages.get(),
            org.opencms.workplace.tools.link.Messages.get(),
            org.opencms.workplace.tools.modules.Messages.get(),
            org.opencms.workplace.tools.projects.Messages.get(),
            org.opencms.workplace.tools.publishqueue.Messages.get(),
            org.opencms.workplace.tools.scheduler.Messages.get(),
            org.opencms.workplace.tools.searchindex.Messages.get(),
            org.opencms.workplace.tools.sites.Messages.get(),
            org.opencms.workplace.tools.workplace.Messages.get(),
            org.opencms.workplace.tools.workplace.broadcast.Messages.get(),
            org.opencms.workplace.tools.workplace.rfsfile.Messages.get()};
    }
}