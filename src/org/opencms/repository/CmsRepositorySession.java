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

package org.opencms.repository;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.A_CmsResourceTypeFolderBase;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.io.BaseEncoding;

/**
 * This is the session class to work with the {@link CmsRepository}.<p>
 *
 * You can get an instance of this class by calling
 * {@link CmsRepository#login(String, String)}.<p>
 *
 * This class provides basic file and folder operations on the resources
 * in the VFS of OpenCms.<p>
 *
 * @see A_CmsRepositorySession
 * @see I_CmsRepositorySession
 *
 * @since 6.5.6
 */
public class CmsRepositorySession extends A_CmsRepositorySession {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRepositorySession.class);

    /** Default namespace for OpenCms properties. */
    public static final String PROPERTY_NAMESPACE = "http://opencms.org/ns/property";

    /** Prefix used for encoded property names outside the default property namespace. */
    public static final String EXTERNAL_PREFIX = "xDAV_";

    /** Base for the namespace encoding. */
    private static final BaseEncoding PROPERTY_NS_CODEC = BaseEncoding.base64Url().withPadChar('$');

    /** Repository-specific file translations to use (may be null). */
    private CmsResourceTranslator m_translation;

    /** The initialized {@link CmsObjectWrapper}. */
    private final CmsObjectWrapper m_cms;

    /**
     * Constructor with an initialized {@link CmsObjectWrapper} and a
     * {@link CmsRepositoryFilter} to use.<p>
     *
     * @param cms the initialized CmsObject
     * @param filter the repository filter to use
     * @param translation the repository specific file translations (may be null)
     */
    public CmsRepositorySession(CmsObjectWrapper cms, CmsRepositoryFilter filter, CmsResourceTranslator translation) {

        m_cms = cms;
        setFilter(filter);
        m_translation = translation;
    }

    /**
     * Decodes the namespace URI.
     *
     * @param code the encoded namespace URI
     * @return the decoded namespace URI
     * @throws Exception if something goes wrong
     */
    public static String decodeNamespace(String code) throws Exception {

        code = code.replace("~", "_");
        return new String(PROPERTY_NS_CODEC.decode(code), "UTF-8");
    }

    /**
     * Encodes the namespace URI.
     *
     * @param data a namesapce URI
     * @return the encoded namespace URI
     *
     * @throws Exception if something goes wrong
     */
    public static String encodeNamespace(String data) throws Exception {

        String s = BaseEncoding.base64Url().withPadChar('$').encode(data.getBytes("UTF-8"));
        s = s.replace('_', '~');
        return s;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#copy(java.lang.String, java.lang.String, boolean)
     */
    public void copy(String src, String dest, boolean overwrite, boolean shallow) throws CmsException {

        src = validatePath(src);
        dest = validatePath(dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_COPY_ITEM_2, src, dest));
        }

        // It is only possible in OpenCms to overwrite files.
        // Folder are not possible to overwrite.
        if (exists(dest)) {

            if (overwrite) {
                CmsResource srcRes = m_cms.readResource(src, CmsResourceFilter.DEFAULT);
                CmsResource destRes = m_cms.readResource(dest, CmsResourceFilter.DEFAULT);

                if ((srcRes.isFile()) && (destRes.isFile())) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_DEST_0));
                    }

                    // delete existing resource
                    delete(dest);
                } else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.ERR_OVERWRITE_0));
                    }

                    // internal error (not possible)
                    throw new CmsException(Messages.get().container(Messages.ERR_OVERWRITE_0));
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_DEST_EXISTS_0));
                }

                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(Messages.ERR_DEST_EXISTS_0));
            }
        }

        // copy resource
        if (shallow) {
            m_cms.getRequestContext().setAttribute(A_CmsResourceTypeFolderBase.ATTR_SHALLOW_FOLDER_COPY, Boolean.TRUE);
        }
        try {
            m_cms.copyResource(src, dest, CmsResource.COPY_PRESERVE_SIBLING);
        } finally {
            m_cms.getRequestContext().removeAttribute(A_CmsResourceTypeFolderBase.ATTR_SHALLOW_FOLDER_COPY);
        }

        // unlock destination resource
        m_cms.unlockResource(dest);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String)
     */
    public void create(String path) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_ITEM_1, path));
        }

        // create the folder
        CmsResource res = m_cms.createResource(path, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // unlock new created folders if lock is not inherited
        if (!m_cms.getLock(res).isInherited()) {
            m_cms.unlockResource(path);
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#delete(java.lang.String)
     */
    public void delete(String path) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_ITEM_1, path));
        }

        CmsRepositoryLockInfo lock = getLock(path);

        // lock resource
        m_cms.lockResource(path);

        // delete resource
        m_cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // if deleting items out of a xml page restore lock state after deleting
        try {
            if (lock == null) {
                m_cms.unlockResource(path);
            }
        } catch (CmsException ex) {
            // noop
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#exists(java.lang.String)
     */
    public boolean exists(String path) {

        try {
            path = validatePath(path);
            return m_cms.existsResource(path);
        } catch (CmsException ex) {
            return false;
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getItem(java.lang.String)
     */
    public I_CmsRepositoryItem getItem(String path) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_READ_ITEM_1, path));
        }

        CmsResource res = m_cms.readResource(path, CmsResourceFilter.DEFAULT);

        CmsRepositoryItem item = new CmsRepositoryItem(res, m_cms);
        return item;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getLock(java.lang.String)
     */
    public CmsRepositoryLockInfo getLock(String path) {

        try {
            CmsRepositoryLockInfo lockInfo = new CmsRepositoryLockInfo();

            path = validatePath(path);

            CmsResource res = m_cms.readResource(path, CmsResourceFilter.DEFAULT);

            // check user locks
            CmsLock cmsLock = m_cms.getLock(res);
            if (!cmsLock.isUnlocked()) {
                lockInfo.setPath(path);

                CmsUser owner = m_cms.readUser(cmsLock.getUserId());
                if (owner != null) {
                    lockInfo.setUsername(owner.getName());
                    lockInfo.setOwner(owner.getName() + "||" + owner.getEmail());
                }
                return lockInfo;
            }

            return null;
        } catch (CmsException ex) {

            // error occurred while finding locks
            // return null (no lock found)
            return null;
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getProperties(java.lang.String)
     */
    public Map<CmsPropertyName, String> getProperties(String path) throws CmsException {

        Map<String, CmsProperty> props = m_cms.readProperties(path);
        Map<CmsPropertyName, String> out = new HashMap<>();
        for (Map.Entry<String, CmsProperty> entry : props.entrySet()) {
            String name = entry.getKey();
            CmsProperty prop = entry.getValue();
            if (name.startsWith(EXTERNAL_PREFIX)) {
                try {
                    String remainder = name.substring(EXTERNAL_PREFIX.length());
                    int pos = remainder.indexOf("_");
                    String nsEncoded = remainder.substring(0, pos);
                    String actualName = remainder.substring(pos + 1);
                    String ns = decodeNamespace(nsEncoded);
                    CmsPropertyName pn = new CmsPropertyName(ns, actualName);
                    out.put(pn, prop.getStructureValue());
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } else {
                if (prop.getStructureValue() != null) {
                    String outName = name + ".s";
                    out.put(new CmsPropertyName(PROPERTY_NAMESPACE, outName), prop.getStructureValue());
                }
                if (prop.getResourceValue() != null) {
                    String outName = name + ".r";
                    out.put(new CmsPropertyName(PROPERTY_NAMESPACE, outName), prop.getResourceValue());
                }
            }
        }
        return out;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#list(java.lang.String)
     */
    public List<I_CmsRepositoryItem> list(String path) throws CmsException {

        List<I_CmsRepositoryItem> ret = new ArrayList<I_CmsRepositoryItem>();

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LIST_ITEMS_1, path));
        }

        List<CmsResource> resources = m_cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT);
        Iterator<CmsResource> iter = resources.iterator();
        while (iter.hasNext()) {
            CmsResource res = iter.next();

            if (!isFiltered(m_cms.getRequestContext().removeSiteRoot(res.getRootPath()))) {

                // open the original resource (for virtual files this is the resource in the VFS
                // which the virtual resource is based on)
                // this filters e.g. property files for resources that are filtered out and thus
                // should not be displayed
                try {
                    CmsResource org = m_cms.readResource(res.getStructureId(), CmsResourceFilter.DEFAULT);
                    if (!isFiltered(m_cms.getRequestContext().removeSiteRoot(org.getRootPath()))) {
                        ret.add(new CmsRepositoryItem(res, m_cms));
                    }
                } catch (CmsVfsResourceNotFoundException e) {
                    // Pure virtual resources with an ID that does not correspond to any real resource
                    ret.add(new CmsRepositoryItem(res, m_cms));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LIST_ITEMS_SUCESS_1, Integer.valueOf(ret.size())));
        }

        return ret;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#lock(java.lang.String, org.opencms.repository.CmsRepositoryLockInfo)
     */
    public boolean lock(String path, CmsRepositoryLockInfo lock) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LOCK_ITEM_1, path));
        }

        m_cms.lockResource(path);
        return true;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#move(java.lang.String, java.lang.String, boolean)
     */
    public void move(String src, String dest, boolean overwrite) throws CmsException {

        src = validatePath(src);
        dest = validatePath(dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MOVE_ITEM_2, src, dest));
        }

        // It is only possible in OpenCms to overwrite files.
        // Folder are not possible to overwrite.
        if (exists(dest)) {

            if (overwrite) {
                CmsResource srcRes = m_cms.readResource(src, CmsResourceFilter.DEFAULT);
                CmsResource destRes = m_cms.readResource(dest, CmsResourceFilter.DEFAULT);

                if ((srcRes.isFile()) && (destRes.isFile())) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_DEST_0));
                    }

                    // delete existing resource
                    delete(dest);
                } else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.ERR_OVERWRITE_0));
                    }

                    throw new CmsException(Messages.get().container(Messages.ERR_OVERWRITE_0));
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_DEST_EXISTS_0));
                }

                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(Messages.ERR_DEST_EXISTS_0));
            }
        }

        // lock source resource
        m_cms.lockResource(src);

        // moving
        m_cms.moveResource(src, dest);

        // unlock destination resource
        m_cms.unlockResource(dest);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#save(java.lang.String, java.io.InputStream, boolean)
     */
    public void save(String path, InputStream inputStream, boolean overwrite) throws CmsException, IOException {

        path = validatePath(path);
        byte[] content = CmsFileUtil.readFully(inputStream);

        try {
            CmsFile file = m_cms.readFile(path, CmsResourceFilter.DEFAULT);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UPDATE_ITEM_1, path));
            }

            if (overwrite) {

                file.setContents(content);

                CmsLock lock = m_cms.getLock(file);

                // lock resource
                if (!lock.isInherited()) {
                    m_cms.lockResource(path);
                }

                // write file
                m_cms.writeFile(file);

                if (lock.isNullLock()) {
                    m_cms.unlockResource(path);
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_DEST_EXISTS_0));
                }

                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(Messages.ERR_DEST_EXISTS_0));
            }
        } catch (CmsVfsResourceNotFoundException ex) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_ITEM_1, path));
            }

            int type = OpenCms.getResourceManager().getDefaultTypeForName(path).getTypeId();

            // create the file
            CmsResource res = m_cms.createResource(path, type, content, null);

            // unlock file after creation if lock is not inherited
            if (!m_cms.getLock(res).isInherited()) {
                m_cms.unlockResource(path);
            }
        }

    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#unlock(java.lang.String)
     */
    public void unlock(String path) {

        try {
            path = validatePath(path);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UNLOCK_ITEM_1, path));
            }

            m_cms.unlockResource(path);
        } catch (CmsException ex) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_UNLOCK_FAILED_0), ex);
            }
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#updateProperties(java.lang.String, java.util.Map)
     */
    public void updateProperties(String path, Map<CmsPropertyName, String> properties) throws CmsException {

        Map<String, CmsProperty> propsToWrite = new HashMap<>();
        for (Map.Entry<CmsPropertyName, String> entry : properties.entrySet()) {
            CmsPropertyName pn = entry.getKey();
            String value = entry.getValue();
            if (pn.getNamespace().equals(PROPERTY_NAMESPACE)) {
                String baseName = pn.getName().substring(0, pn.getName().length() - 2);
                if (!propsToWrite.containsKey(baseName)) {
                    CmsProperty prop = new CmsProperty(baseName, null, null);
                    propsToWrite.put(baseName, prop);
                }
                CmsProperty prop = propsToWrite.get(baseName);
                if (pn.getName().endsWith(".s")) {
                    prop.setStructureValue(value);
                } else if (pn.getName().endsWith(".r")) {
                    prop.setResourceValue(value);
                } else {
                    LOG.error("Invalid name for repository property, must end with .s or .r");
                }
            } else {
                try {
                    String propName = EXTERNAL_PREFIX + encodeNamespace(pn.getNamespace()) + "_" + pn.getName();
                    CmsProperty prop = new CmsProperty(propName, value, null);
                    propsToWrite.put(propName, prop);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        boolean needUnlock = false;
        if (null == getLock(path)) {
            m_cms.lockResource(path);
            needUnlock = true;
        }
        try {
            LOG.debug("Writing properties: " + propsToWrite);
            m_cms.writeProperties(path, propsToWrite);
        } finally {
            if (needUnlock) {
                m_cms.unlockResource(path);
            }
        }
    }

    /**
     * Adds the site root to the path name and checks then if the path
     * is filtered.<p>
     *
     * @see org.opencms.repository.A_CmsRepositorySession#isFiltered(java.lang.String)
     */
    @Override
    protected boolean isFiltered(String name) {

        boolean ret = super.isFiltered(m_cms.getRequestContext().addSiteRoot(name));
        if (ret) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_ITEM_FILTERED_1, name));
            }

        }

        return ret;
    }

    /**
     * Gets the resource translator to use for path translations.
     *
     * @return the resource translator to use
     */
    private CmsResourceTranslator getEffectiveResourceTranslator() {

        if (m_translation != null) {
            return m_translation;
        }
        return m_cms.getRequestContext().getFileTranslator();
    }

    /**
     * Validates (translates) the given path and checks if it is filtered out.<p>
     *
     * @param path the path to validate
     *
     * @return the validated path
     *
     * @throws CmsSecurityException if the path is filtered out
     */
    private String validatePath(String path) throws CmsSecurityException {

        // Problems with spaces in new folders (default: "Neuer Ordner")
        // Solution: translate this to a correct name.
        CmsResourceTranslator translator = getEffectiveResourceTranslator();
        String ret = CmsStringUtil.translatePathComponents(translator, path);

        // add site root only works correct if system folder ends with a slash
        if (CmsResource.VFS_FOLDER_SYSTEM.equals(ret)) {
            ret = ret.concat("/");
        }

        // filter path
        if (isFiltered(ret)) {
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_ITEM_FILTERED_1, path));
        }

        return ret;
    }
}