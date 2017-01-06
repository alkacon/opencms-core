/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cmis;

import java.io.File;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;

/**
 * Call context implementation which delegates most methods to a wrapped call context, but also provides additional functionality.<p>
 */
public class CmsCmisCallContext implements CallContext {

    /** The wrapped call context. */
    private CallContext m_context;

    /** The object info handler. */
    private ObjectInfoHandler m_objectInfo;

    /**
     * Creates a new instance.<p>
     *
     * @param originalContext the context to wrap
     * @param objectInfo the object info handler to use
     */
    public CmsCmisCallContext(CallContext originalContext, ObjectInfoHandler objectInfo) {

        m_context = originalContext;
        m_objectInfo = objectInfo;
    }

    public boolean encryptTempFiles() {

        return false;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#get(java.lang.String)
     */
    public Object get(String attr) {

        return m_context.get(attr);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getBinding()
     */
    public String getBinding() {

        return m_context.getBinding();
    }

    public CmisVersion getCmisVersion() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getLength()
     */
    public BigInteger getLength() {

        return m_context.getLength();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getLocale()
     */
    public String getLocale() {

        return m_context.getLocale();
    }

    public long getMaxContentSize() {

        return Integer.MAX_VALUE;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getMemoryThreshold()
     */
    public int getMemoryThreshold() {

        return m_context.getMemoryThreshold();
    }

    /**
     * The object info handler to use.<p>
     *
     * @return the object info handler
     */
    public ObjectInfoHandler getObjectInfoHandler() {

        return m_objectInfo;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getOffset()
     */
    public BigInteger getOffset() {

        return m_context.getOffset();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getPassword()
     */
    public String getPassword() {

        return m_context.getPassword();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getRepositoryId()
     */
    public String getRepositoryId() {

        return m_context.getRepositoryId();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getTempDirectory()
     */
    public File getTempDirectory() {

        return m_context.getTempDirectory();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#getUsername()
     */
    public String getUsername() {

        return m_context.getUsername();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.server.CallContext#isObjectInfoRequired()
     */
    public boolean isObjectInfoRequired() {

        return m_context.isObjectInfoRequired();
    }

}
