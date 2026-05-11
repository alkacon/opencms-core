/*
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.relations.CmsExternalLinksValidator;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsUriSplitter;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @since 7.0.4
 */
public class TestCmsExternalLinksValidator extends OpenCmsTestRunner {

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * This test can go wrong if the external sites in the WWW not exists anymore or
     * no connect to the WWW exists.<p>
     *
     * Please remove sites, which not exists anymore.<p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void testExternalLinksOutside() throws Exception {

        CmsObject cms = getCmsObject();

        List<String> list = new ArrayList<String>();
        list.add("http://www.bing.com/search?q=%C3%BCberall&qs=n&form=QBLH&filt=all&pq=%C3%BCberall");

        // removing invalid links
        //        list.add("http://www.dsb.dk/servlet/Satellite?pagename=Millenium/Page/StandardForside&c=Page&cid=1002806878464");
        //        list.add("http://www.nbi.dk/%7Enatphil/hug/hug.intro.html");
        //        list.add("http://www.si-folkesundhed.dk/Forskning/Sygdomme og tilskadekomst/Ulykker/Nyhedsbrev.aspx");
        //        list.add("http://www.mim.dk/Udgivelser/Milj%F8Danmark/");
        //        list.add("http://www.ug.dk/Videnscenter for vejledning/Forside/Virtuelt tidsskrift.aspx");

        // checks the list of external links
        for (int i = 0; i < list.size(); i++) {
            String url = list.get(i);
            System.out.println("Checking external link: " + url);
            System.out.println("  Extenal link encoded: " + new CmsUriSplitter(url, true).toURI().toURL());
            assertTrue(CmsExternalLinksValidator.checkUrl(cms, url), "External link check failed:" + url);
        }
    }
}
