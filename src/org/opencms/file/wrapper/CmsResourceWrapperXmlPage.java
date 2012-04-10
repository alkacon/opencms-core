/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * A resource type wrapper for xml page files, which explodes the xml pages to folders.<p>
 *
 * Every resource of type "xmlpage" becomes a folder with the same name. That folder 
 * contains the locales of the xml page as folders too. In the locale folder there are 
 * the elements for that locale as files. The files have the names of the elements with the
 * extension "html". Additionaly there is a file in the root folder of that xml page that
 * contains the controlcode of the xml page. This file has the name "controlcode.xml".<p>
 * 
 * @since 6.5.6
 */
public class CmsResourceWrapperXmlPage extends A_CmsResourceWrapper {

    /** The extension to use for elements. */
    private static final String EXTENSION_ELEMENT = "html";

    /** The name of the element to use for the controlcode. */
    private static final String NAME_ELEMENT_CONTROLCODE = "controlcode.xml";

    /** Table with the states of the virtual files. */
    private static final List<String> TMP_FILE_TABLE = new ArrayList<String>();

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#addResourcesToFolder(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public List<CmsResource> addResourcesToFolder(CmsObject cms, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {
            String path = getSubPath(cms, xmlPage, resourcename);
            String rootPath = cms.getRequestContext().removeSiteRoot(xmlPage.getRootPath());

            ArrayList<CmsResource> ret = new ArrayList<CmsResource>();

            CmsFile file = cms.readFile(xmlPage);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {

                // sub path is empty -> return all existing locales for the resource
                if (file.getLength() == 0) {
                    return ret;
                }

                List<Locale> locales = xml.getLocales();
                Iterator<Locale> iter1 = locales.iterator();
                while (iter1.hasNext()) {
                    Locale locale = iter1.next();
                    ret.add(getResourceForLocale(xmlPage, locale));
                }

                int plainId = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()).getTypeId();
                // check temp file table to add virtual file
                Iterator<String> iter2 = getVirtualFiles().iterator();
                while (iter2.hasNext()) {

                    String virtualFileName = iter2.next();
                    String virtualFilePath = rootPath + "/" + virtualFileName;

                    if (!TMP_FILE_TABLE.contains(virtualFilePath)) {

                        // read the control code resource
                        if (virtualFileName.equals(NAME_ELEMENT_CONTROLCODE)) {

                            CmsWrappedResource wrap = new CmsWrappedResource(xmlPage);
                            wrap.setRootPath(xmlPage.getRootPath() + "/" + NAME_ELEMENT_CONTROLCODE);
                            wrap.setTypeId(plainId);

                            CmsFile tmpFile = wrap.getFile();
                            tmpFile.setContents(file.getContents());
                            ret.add(tmpFile);
                        }
                    }
                }
            } else {
                // sub path is a locale -> return all elements for this locale
                Locale locale = CmsLocaleManager.getLocale(path);
                List<String> names = xml.getNames(locale);
                Iterator<String> iter = names.iterator();
                while (iter.hasNext()) {
                    String name = iter.next();
                    String content = xml.getStringValue(cms, name, locale);
                    String fullPath = xmlPage.getRootPath() + "/" + path + "/" + name + "." + EXTENSION_ELEMENT;
                    content = prepareContent(content, cms, xmlPage, fullPath);

                    int length = content.length();
                    try {
                        length = content.getBytes(CmsLocaleManager.getResourceEncoding(cms, xmlPage)).length;
                    } catch (UnsupportedEncodingException e) {
                        // this will never happen since UTF-8 is always supported
                    }

                    ret.add(getResourceForElement(xmlPage, fullPath, length));
                }

            }

            return ret;
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#copyResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.file.CmsResource.CmsResourceCopyMode)
     */
    @Override
    public boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        // only allow copying of xml pages at whole or locales and elements inside the same xml page
        CmsResource srcXmlPage = findXmlPage(cms, source);

        if (srcXmlPage != null) {
            String srcPath = getSubPath(cms, srcXmlPage, source);

            // if the source is the xml page itself just copy the resource
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(srcPath)) {
                cms.copyResource(source, destination, siblingMode);
                return true;
            } else {

                // only a locale or an element should be copied
                CmsResource destXmlPage = findXmlPage(cms, destination);
                if (srcXmlPage.equals(destXmlPage)) {

                    // copying inside the same xml page resource
                    String destPath = getSubPath(cms, destXmlPage, destination);

                    String[] srcTokens = srcPath.split("/");
                    String[] destTokens = destPath.split("/");

                    if (srcTokens.length == destTokens.length) {

                        CmsFile srcFile = cms.readFile(srcXmlPage);
                        CmsXmlPage srcXml = CmsXmlPageFactory.unmarshal(cms, srcFile);

                        if (srcTokens.length == 1) {

                            if (srcTokens[0].equals(NAME_ELEMENT_CONTROLCODE)) {

                                // do nothing
                            } else {

                                // copy locale
                                srcXml.copyLocale(
                                    CmsLocaleManager.getLocale(srcTokens[0]),
                                    CmsLocaleManager.getLocale(destTokens[0]));
                            }
                        } else if (srcTokens.length == 2) {

                            // TODO: copy element
                        }

                        // write files
                        srcFile.setContents(srcXml.marshal());
                        cms.writeFile(srcFile);

                        return true;
                    } else {

                        // TODO: error: destination path is invalid
                    }
                } else {

                    // TODO: error: copying only allowed inside the same xml page
                }
            }
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException {

        // cut off trailing slash
        if (resourcename.endsWith("/")) {
            resourcename = resourcename.substring(0, resourcename.length() - 1);
        }

        // creating new xml pages if type is a folder and the name ends with .html
        if (resourcename.endsWith(".html")
            && (type == OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()).getTypeId())) {

            // mark in temp file table that the visual files does not exist yet
            Iterator<String> iter = getVirtualFiles().iterator();
            while (iter.hasNext()) {
                TMP_FILE_TABLE.add(resourcename + "/" + iter.next());
            }

            return cms.createResource(
                resourcename,
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeXmlPage.getStaticTypeName()).getTypeId());
        }

        // find the xml page this is for
        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {

            // get the path below the xml page
            String path = getSubPath(cms, xmlPage, resourcename);

            // and the path without the site root
            String rootPath = cms.getRequestContext().removeSiteRoot(xmlPage.getRootPath());

            CmsFile file = cms.readFile(xmlPage);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            // mark virtual files as created in temp file table
            if (getVirtualFiles().contains(path)) {
                TMP_FILE_TABLE.remove(resourcename);

                // at least lock file, because creating resources usually locks the resource
                cms.lockResource(rootPath);

                return file;
            }

            String[] tokens = path.split("/");
            if (tokens.length == 1) {

                Locale locale = CmsLocaleManager.getLocale(tokens[0]);

                // workaround: empty xmlpages always have the default locale "en" set
                if (file.getLength() == 0) {
                    Iterator<Locale> iter = xml.getLocales().iterator();
                    while (iter.hasNext()) {
                        xml.removeLocale(iter.next());
                    }
                }

                // create new locale
                xml.addLocale(cms, locale);

                // save the xml page
                file.setContents(xml.marshal());

                // lock the resource
                cms.lockResource(rootPath);

                // write file
                cms.writeFile(file);

            } else if (tokens.length == 2) {

                String name = tokens[1];
                if (name.endsWith(EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - EXTENSION_ELEMENT.length() - 1);
                }

                // create new element
                xml.addValue(name, CmsLocaleManager.getLocale(tokens[0]));

                // set the content
                xml.setStringValue(cms, name, CmsLocaleManager.getLocale(tokens[0]), getStringValue(cms, file, content));

                // save the xml page
                file.setContents(xml.marshal());

                // lock the resource
                cms.lockResource(rootPath);

                // write file
                cms.writeFile(file);
            }

            return file;
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(CmsObject, String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode)
    throws CmsException {

        // find the xml page this is for
        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            // get the path below the xml page
            String path = getSubPath(cms, xmlPage, resourcename);

            // if sub path is empty
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {

                // delete the xml page itself
                cms.deleteResource(resourcename, siblingMode);

                // remove all virtual files for this resource
                Iterator<String> iter = getVirtualFiles().iterator();
                while (iter.hasNext()) {
                    TMP_FILE_TABLE.remove(resourcename + "/" + iter.next());
                }

                return true;
            }

            CmsFile file = cms.readFile(xmlPage);
            CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

            String[] tokens = path.split("/");
            if (tokens.length == 1) {

                // deleting a virtual file
                if (getVirtualFiles().contains(tokens[0])) {

                    // mark the virtual file in the temp file table as deleted
                    TMP_FILE_TABLE.add(resourcename);
                } else {

                    // delete locale
                    xml.removeLocale(CmsLocaleManager.getLocale(tokens[0]));

                    // save the xml page
                    file.setContents(xml.marshal());
                    cms.writeFile(file);
                }

            } else if (tokens.length == 2) {

                String name = tokens[1];
                if (name.endsWith(EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - EXTENSION_ELEMENT.length() - 1);
                }

                // delete element
                xml.removeValue(name, CmsLocaleManager.getLocale(tokens[0]));

                // save the xml page
                file.setContents(xml.marshal());
                cms.writeFile(file);
            }

            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsResource xmlPage = cms.readResource(resource.getStructureId());
        //CmsResource xmlPage = findXmlPage(cms, resource.getRootPath());
        if (xmlPage != null) {

            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(xmlPage.getTypeId());
            if (resType instanceof CmsResourceTypeXmlPage) {
                return cms.getLock(xmlPage);
            }
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            if (resType instanceof CmsResourceTypeXmlPage) {
                return true;
            }
        } catch (CmsException ex) {
            // noop
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public boolean lockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = findXmlPage(cms, resourcename);
        if (res != null) {
            cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException {

        // only allow copying of xml pages at whole or locales and elements inside the same xml page
        CmsResource srcXmlPage = findXmlPage(cms, source);

        if (srcXmlPage != null) {
            String srcPath = getSubPath(cms, srcXmlPage, source);

            // if the source is the xml page itself just copy the resource
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(srcPath)) {
                cms.moveResource(source, destination);
                return true;
            } else {

                // only a locale or an element should be copied
                CmsResource destXmlPage = findXmlPage(cms, destination);
                if (srcXmlPage.equals(destXmlPage)) {

                    // copying inside the same xml page resource
                    String destPath = getSubPath(cms, destXmlPage, destination);

                    String[] srcTokens = srcPath.split("/");
                    String[] destTokens = destPath.split("/");

                    if (srcTokens.length == destTokens.length) {

                        CmsFile srcFile = cms.readFile(srcXmlPage);
                        CmsXmlPage srcXml = CmsXmlPageFactory.unmarshal(cms, srcFile);

                        if (srcTokens.length == 1) {

                            // copy locale
                            srcXml.moveLocale(
                                CmsLocaleManager.getLocale(srcTokens[0]),
                                CmsLocaleManager.getLocale(destTokens[0]));
                        } else if (srcTokens.length == 2) {

                            // TODO: move element
                        }

                        // write file
                        srcFile.setContents(srcXml.marshal());
                        cms.writeFile(srcFile);
                    } else {

                        // TODO: error: destination path is invalid
                    }
                } else {

                    // TODO: error: moving only allowed inside the same xml page
                }
            }

            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        // find the xml page this is for
        CmsResource xmlPage = findXmlPage(cms, resourcename);
        if (xmlPage != null) {

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            // get the path below the xml page
            String path = getSubPath(cms, xmlPage, resourcename);

            String[] tokens = path.split("/");
            if (tokens.length == 1) {

                CmsFile file = cms.readFile(xmlPage);

                // check temp file table to remove deleted virtual files
                if (TMP_FILE_TABLE.contains(resourcename)) {
                    return null;
                }

                // read the control code resource
                if (tokens[0].equals(NAME_ELEMENT_CONTROLCODE)) {

                    CmsWrappedResource wrap = new CmsWrappedResource(xmlPage);
                    wrap.setRootPath(xmlPage.getRootPath() + "/" + NAME_ELEMENT_CONTROLCODE);

                    CmsFile ret = wrap.getFile();
                    ret.setContents(file.getContents());
                    return ret;
                }
            } else if (tokens.length == 2) {

                CmsFile file = cms.readFile(xmlPage);
                CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

                // cut off the html suffix
                String name = tokens[1];
                if (name.endsWith("." + EXTENSION_ELEMENT)) {
                    name = name.substring(0, name.length() - 5);
                }

                if (xml.hasValue(name, CmsLocaleManager.getLocale(tokens[0]))) {

                    String contentString = xml.getStringValue(cms, name, CmsLocaleManager.getLocale(tokens[0]));
                    String fullPath = xmlPage.getRootPath() + "/" + tokens[0] + "/" + name + "." + EXTENSION_ELEMENT;
                    contentString = prepareContent(contentString, cms, xmlPage, fullPath);

                    byte[] content;
                    try {
                        content = contentString.getBytes(CmsLocaleManager.getResourceEncoding(cms, xmlPage));
                    } catch (UnsupportedEncodingException e) {
                        // should never happen
                        content = contentString.getBytes();
                    }
                    CmsResource resElem = getResourceForElement(xmlPage, fullPath, content.length);
                    CmsFile fileElem = new CmsFile(resElem);

                    fileElem.setContents(content);
                    return fileElem;
                }
            }
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        try {

            // try to read the resource for the resource name
            CmsResource res = null;
            try {
                // catch this exception to try to read the resource again if it fails
                res = cms.readResource(resourcename, filter);
            } catch (CmsException e) {
                // read resource failed, so check if the resource name ends with a slash
                if (resourcename.endsWith("/")) {
                    // try to read resource without a slash
                    resourcename = CmsFileUtil.removeTrailingSeparator(resourcename);
                    // try to read the resource name without ending slash
                    res = cms.readResource(resourcename, filter);
                } else {
                    // throw the exception which caused this catch block
                    throw e;
                }
            }
            if (CmsResourceTypeXmlPage.isXmlPage(res)) {
                // return the xml page resource as a folder
                return wrapResource(cms, res);
            }

            return null;
        } catch (CmsVfsResourceNotFoundException ex) {

            // find the xml page this is for
            CmsResource xmlPage = findXmlPage(cms, resourcename);
            if (xmlPage != null) {

                // cut off trailing slash
                if (resourcename.endsWith("/")) {
                    resourcename = resourcename.substring(0, resourcename.length() - 1);
                }

                // get the path below the xml page
                String path = getSubPath(cms, xmlPage, resourcename);

                CmsFile file = cms.readFile(xmlPage);
                CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

                String[] tokens = path.split("/");
                if (tokens.length == 1) {

                    // check temp file table to remove deleted virtual files
                    if (TMP_FILE_TABLE.contains(resourcename)) {
                        return null;
                    }

                    // read the control code resource
                    if (tokens[0].equals(NAME_ELEMENT_CONTROLCODE)) {

                        CmsWrappedResource wrap = new CmsWrappedResource(xmlPage);
                        wrap.setRootPath(xmlPage.getRootPath() + "/" + NAME_ELEMENT_CONTROLCODE);
                        return wrap.getResource();
                    } else {

                        Locale locale = CmsLocaleManager.getLocale(tokens[0]);
                        if (xml.hasLocale(locale) && (file.getLength() > 0)) {
                            return getResourceForLocale(xmlPage, locale);
                        }
                    }
                } else if (tokens.length == 2) {

                    // cut off the html suffix
                    String name = tokens[1];
                    if (name.endsWith("." + EXTENSION_ELEMENT)) {
                        name = name.substring(0, name.length() - 5);
                    }

                    Locale locale = CmsLocaleManager.getLocale(tokens[0]);
                    if (xml.hasValue(name, locale)) {
                        String content = xml.getStringValue(cms, name, locale);
                        String fullPath = xmlPage.getRootPath()
                            + "/"
                            + tokens[0]
                            + "/"
                            + name
                            + "."
                            + EXTENSION_ELEMENT;
                        content = prepareContent(content, cms, xmlPage, fullPath);

                        int length = content.length();
                        try {
                            length = content.getBytes(CmsLocaleManager.getResourceEncoding(cms, xmlPage)).length;
                        } catch (UnsupportedEncodingException e) {
                            // this will never happen since UTF-8 is always supported
                        }

                        return getResourceForElement(xmlPage, fullPath, length);
                    }
                }

            }

            return null;
        }
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#restoreLink(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public String restoreLink(CmsObject cms, String uri) {

        CmsResource res = findXmlPage(cms, uri);
        if (res != null) {
            return res.getRootPath();
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#rewriteLink(CmsObject, CmsResource)
     */
    @Override
    public String rewriteLink(CmsObject cms, CmsResource res) {

        if (isWrappedResource(cms, res)) {
            String path = res.getRootPath();
            if (!path.endsWith("/")) {
                path += "/";
            }

            return path + NAME_ELEMENT_CONTROLCODE;
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#unlockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public boolean unlockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = findXmlPage(cms, resourcename);
        if (res != null) {
            cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#wrapResource(CmsObject, CmsResource)
     */
    @Override
    public CmsResource wrapResource(CmsObject cms, CmsResource res) {

        CmsWrappedResource wrap = new CmsWrappedResource(res);
        wrap.setFolder(true);
        return wrap.getResource();
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        CmsResource xmlPage = cms.readResource(resource.getStructureId());
        //CmsResource xmlPage = findXmlPage(cms, resource.getRootPath());
        if (xmlPage != null) {

            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(xmlPage.getTypeId());
            if (resType instanceof CmsResourceTypeXmlPage) {

                String path = getSubPath(cms, xmlPage, cms.getRequestContext().removeSiteRoot(resource.getRootPath()));

                CmsFile file = cms.readFile(xmlPage);

                String[] tokens = path.split("/");
                if (tokens.length == 2) {

                    CmsXmlPage xml = CmsXmlPageFactory.unmarshal(cms, file);

                    // cut off the html suffix
                    String name = tokens[1];
                    if (name.endsWith("." + EXTENSION_ELEMENT)) {
                        name = name.substring(0, name.length() - 5);
                    }

                    // set content
                    String content = getStringValue(cms, file, resource.getContents());
                    content = CmsStringUtil.extractHtmlBody(content).trim();
                    xml.setStringValue(cms, name, CmsLocaleManager.getLocale(tokens[0]), content);

                    // write file
                    file.setContents(xml.marshal());
                    cms.writeFile(file);

                }

                return file;
            }
        }

        return null;
    }

    /**
     * Returns the OpenCms VFS uri of the style sheet of the resource.<p>
     * 
     * @param cms the initialized CmsObject
     * @param res the resource where to read the style sheet for
     * 
     * @return the OpenCms VFS uri of the style sheet of resource
     */
    protected String getUriStyleSheet(CmsObject cms, CmsResource res) {

        String result = "";
        try {
            String currentTemplate = getUriTemplate(cms, res);
            if (!"".equals(currentTemplate)) {
                // read the stylesheet from the template file
                result = cms.readPropertyObject(currentTemplate, CmsPropertyDefinition.PROPERTY_TEMPLATE, false).getValue(
                    "");
            }
        } catch (CmsException e) {
            // noop
        }
        return result;
    }

    /**
     * Returns the OpenCms VFS uri of the template of the resource.<p>
     * 
     * @param cms the initialized CmsObject
     * @param res the resource where to read the template for
     * 
     * @return the OpenCms VFS uri of the template of the resource
     */
    protected String getUriTemplate(CmsObject cms, CmsResource res) {

        String result = "";
        try {
            result = cms.readPropertyObject(
                cms.getRequestContext().removeSiteRoot(res.getRootPath()),
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                true).getValue("");
        } catch (CmsException e) {
            // noop
        }
        return result;
    }

    /**
     * Prepare the content of a xml page before returning.<p>
     * 
     * Mainly adds the basic html structure and the css style sheet.<p>
     * 
     * @param content the origin content of the xml page element
     * @param cms the initialized CmsObject
     * @param xmlPage the xml page resource
     * @param path the full path to set as the title in the html head
     * 
     * @return the prepared content with the added html structure
     */
    protected String prepareContent(String content, CmsObject cms, CmsResource xmlPage, String path) {

        // cut off eventually existing html skeleton
        content = CmsStringUtil.extractHtmlBody(content);

        // add tags for stylesheet
        String stylesheet = getUriStyleSheet(cms, xmlPage);

        // content-type
        String encoding = CmsLocaleManager.getResourceEncoding(cms, xmlPage);
        String contentType = CmsResourceManager.MIMETYPE_HTML + "; charset=" + encoding;

        content = CmsEncoder.adjustHtmlEncoding(content, encoding);

        // rewrite uri
        Object obj = cms.getRequestContext().getAttribute(CmsObjectWrapper.ATTRIBUTE_NAME);
        if (obj != null) {
            CmsObjectWrapper wrapper = (CmsObjectWrapper)obj;
            stylesheet = wrapper.rewriteLink(stylesheet);
        }

        String base = OpenCms.getSystemInfo().getOpenCmsContext();

        StringBuffer result = new StringBuffer(content.length() + 1024);
        result.append("<html><head>");

        // meta: content-type
        result.append("<meta http-equiv=\"content-type\" content=\"");
        result.append(contentType);
        result.append("\">");

        // title as full path
        result.append("<title>");
        result.append(cms.getRequestContext().removeSiteRoot(path));
        result.append("</title>");

        // stylesheet
        if (!"".equals(stylesheet)) {
            result.append("<link href=\"");
            result.append(base);
            result.append(stylesheet);
            result.append("\" rel=\"stylesheet\" type=\"text/css\">");
        }

        result.append("</head><body>");
        result.append(content);
        result.append("</body></html>");
        content = result.toString();

        return content.trim();
    }

    /**
     * Returns the {@link CmsResource} of the xml page which belongs to 
     * the given resource name (full path).<p>
     * 
     * It works up the path till a resource for the path exists in the VFS. 
     * If the found resource is a xml page, this resource is returned. If 
     * the path does not belong to a xml page <code>null</code> will be returned.<p> 
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource (full path) to check
     * 
     * @return the found resource of type xml page or null if not found
     */
    private CmsResource findXmlPage(CmsObject cms, String resourcename) {

        // get the full folder path of the resource to start from
        String path = cms.getRequestContext().removeSiteRoot(resourcename);
        // the path without the trailing slash
        // for example: .../xmlpage.xml/ -> .../xmlpagepage.xml
        String reducedPath = CmsFileUtil.removeTrailingSeparator(path);
        do {

            // check if a resource without the trailing shalsh exists
            boolean existResource = cms.existsResource(reducedPath);
            // check if the current folder exists
            if (cms.existsResource(path) || existResource) {
                // prove if a resource without the trailing slash does exist
                if (existResource) {
                    // a resource without the trailing slash does exist, so take the path without the trailing slash
                    path = reducedPath;
                }
                try {
                    CmsResource res = cms.readResource(path);
                    I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
                    if (resType instanceof CmsResourceTypeXmlPage) {
                        return res;
                    } else {
                        break;
                    }
                } catch (CmsException ex) {
                    break;
                }

            } else {

                // folder does not exist, go up one folder
                path = CmsResource.getParentFolder(path);
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
            }

            if (CmsStringUtil.isEmpty(path)) {

                // site root or root folder reached and no folder found
                break;
            }
        } while (true);

        return null;
    }

    /**
     * Returns a virtual resource for an element inside a locale.<p>
     * 
     * A new (virtual) resource is created with the given path and length. The
     * new created resource uses the values of the origin resource of the xml page 
     * where it is possible.<p>
     * 
     * @param xmlPage the xml page resource with the element to create a virtual resource
     * @param path the full path to set for the resource
     * @param length the length of the element content
     * 
     * @return a new created virtual {@link CmsResource}
     */
    private CmsResource getResourceForElement(CmsResource xmlPage, String path, int length) {

        CmsWrappedResource wrap = new CmsWrappedResource(xmlPage);
        wrap.setRootPath(path);
        int plainId;
        try {
            plainId = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e) {
            // this should really never happen
            plainId = CmsResourceTypePlain.getStaticTypeId();
        }
        wrap.setTypeId(plainId);
        wrap.setFolder(false);
        wrap.setLength(length);

        return wrap.getResource();
    }

    /**
     * Creates a new virtual resource for the locale in the xml page as a folder.<p>
     * 
     * The new created resource uses the values of the origin resource of the xml page where it is possible.<p>
     * 
     * @param xmlPage the xml page resource with the locale to create a resource of
     * @param locale the locale in the xml page to use for the new resource
     * 
     * @return a new created CmsResource
     */
    private CmsResource getResourceForLocale(CmsResource xmlPage, Locale locale) {

        CmsWrappedResource wrap = new CmsWrappedResource(xmlPage);
        wrap.setRootPath(xmlPage.getRootPath() + "/" + locale.getLanguage() + "/");
        int plainId;
        try {
            plainId = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e) {
            // this should really never happen
            plainId = CmsResourceTypePlain.getStaticTypeId();
        }
        wrap.setTypeId(plainId);
        wrap.setFolder(true);

        return wrap.getResource();
    }

    /**
     * Returns the content as a string while using the correct encoding.<p>
     * 
     * @param cms the initialized CmsObject
     * @param resource the resource where the content belongs to
     * @param content the byte array which should be converted into a string
     * 
     * @return the content as a string
     * 
     * @throws CmsException if something goes wrong
     */
    private String getStringValue(CmsObject cms, CmsResource resource, byte[] content) throws CmsException {

        // get the encoding for the resource
        CmsProperty prop = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true);
        String enc = prop.getValue();
        if (enc == null) {
            enc = OpenCms.getSystemInfo().getDefaultEncoding();
        }

        // create a String with the right encoding
        return CmsEncoder.createString(content, enc);
    }

    /**
     * Returns the path inside a xml page.<p>
     * 
     * The remaining path inside a xml page can be the locale and the element name
     * or the name of the control code file.<p>
     * 
     * @param cms the initialized CmsObject
     * @param xmlPage the xml page where the resourcename belongs to
     * @param resourcename the full path of the resource (pointing inside the xml page)
     * 
     * @return the remaining path inside the xml page without the leading slash
     */
    private String getSubPath(CmsObject cms, CmsResource xmlPage, String resourcename) {

        if (xmlPage != null) {
            String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
            String path = rootPath.substring(xmlPage.getRootPath().length());

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            return path;
        }

        return null;
    }

    /**
     * Returns a list with virtual file names for the xml page.<p>
     * 
     * Actually that is only the name of the control code file.<p>
     * 
     * @return a list containing strings with the names of the virtual files
     */
    private List<String> getVirtualFiles() {

        ArrayList<String> list = new ArrayList<String>();
        list.add(NAME_ELEMENT_CONTROLCODE);

        return list;
    }
}