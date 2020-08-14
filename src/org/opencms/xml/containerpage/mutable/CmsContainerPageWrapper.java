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

package org.opencms.xml.containerpage.mutable;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

/**
 * 'Wrapper' around XML container page used for programmatic editing operations on container pages.
 * <p>
 * Uses mutable helper classes for the container page and its containers.
 */
public class CmsContainerPageWrapper {

    /** The mutable bean containing the container page data. */
    private CmsMutableContainerPage m_page;

    /** The underlying XML container page. */
    private CmsXmlContainerPage m_xml;

    /** The CMS context. */
    private CmsObject m_cms;

    /**
     * Creates a new instance by reading the container page from a file.
     *
     * @param cms the CMS context
     * @param res the resource
     * @throws CmsException if something goes wrong
     */
    public CmsContainerPageWrapper(CmsObject cms, CmsResource res)
    throws CmsException {

        m_cms = cms;
        m_xml = CmsXmlContainerPageFactory.unmarshal(cms, cms.readFile(res));
        m_page = CmsMutableContainerPage.fromImmutable(m_xml.getContainerPage(cms));
    }

    /**
     * Creates a new instance from an existing XML container page object.
     *
     * @param cms the CMS context
     * @param xml the XML container page object
     */
    public CmsContainerPageWrapper(CmsObject cms, CmsXmlContainerPage xml) {

        m_cms = cms;
        m_xml = xml;
        m_page = CmsMutableContainerPage.fromImmutable(m_xml.getContainerPage(cms));
    }

    /**
     * Adds an element to the given container (the first container with the given container suffix is used).
     *
     * @param containerName the container name or suffix
     * @param element the element to add
     * @return false if there was no container to add the element to, true otherwise
     */
    public boolean addElementToContainer(String containerName, CmsContainerElementBean element) {

        CmsMutableContainer container = page().firstContainer(containerName);
        if (container == null) {
            return false;
        }
        container.elements().add(element);
        return true;
    }

    /**
     * Marshals the page data without writing it to the VFS.
     *
     * @return the marshalled page data
     * @throws CmsException if something goes wrong
     */
    public byte[] marshal() throws CmsException {

        m_xml.writeContainerPage(m_cms, m_page.toImmutable());
        return m_xml.marshal();

    }

    /**
     * Gets the mutable page bean instance.
     *
     * @return the mutable page bean
     */
    public CmsMutableContainerPage page() {

        return m_page;
    }

    /**
     * Saves the page data to the VFS, using the same resource from which this object was created.
     *
     * @throws CmsException if something goes wrong
     */
    public void saveToVfs() throws CmsException {

        CmsContainerPageBean immutablePage = page().toImmutable();
        try (AutoCloseable c = CmsLockUtil.withLockedResources(m_cms, m_xml.getFile())) {
            m_xml.save(m_cms, immutablePage);
        } catch (Exception e) {
            if (e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}
