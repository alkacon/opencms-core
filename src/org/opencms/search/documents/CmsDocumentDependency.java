/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.documents;

import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;

/**
 * Provides the dependency information about one search result document,
 * used to generate the list of document search results.<p>
 *
 * @since 8.5.0
 */
public final class CmsDocumentDependency {

    /**
     * Defines the possible dependency types.<p>
     */
    public static enum DependencyType {

        /** A attachment dependency. */
        attachment,

        /** The main document dependency. */
        document,

        /** A variant dependency. */
        variant
    }

    /** Prefix for context attributes. */
    private static final String ATTR_DOC_DEPENDENCY = "CmsDocumentDependency.";

    /** Field name in JSON. */
    private static final String JSON_ATTACHMENTS = "attachments";

    /** Field name in JSON. */
    private static final String JSON_DATE_CREATED = "dateCreated";

    /** Field name in JSON. */
    private static final String JSON_DATE_MODIFIED = "dateModified";

    /** Field name in JSON. */
    private static final String JSON_LANGUAGES = "languages";

    /** Field name in JSON. */
    private static final String JSON_LOCALE = "locale";

    /** Field name in JSON. */
    private static final String JSON_MAIN = "main";

    /** Field name in JSON. */
    private static final String JSON_PATH = "path";

    /** Field name in JSON. */
    private static final String JSON_TITLE = "title";

    /** Field name in JSON. */
    private static final String JSON_UUID = "uuid";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDocumentDependency.class);

    /** The attachment number. */
    private Integer m_attachmentNumber;

    /** The document attachments . */
    private List<CmsDocumentDependency> m_attachments = new ArrayList<CmsDocumentDependency>();

    /** The list of resources the main resource depends on, including the main resource itself. */
    private List<CmsDocumentDependency> m_dependencies = new ArrayList<CmsDocumentDependency>();

    /** The file name of the document without attachment or locale suffixes. */
    private String m_documentName;

    /** The file suffix of the document. */
    private String m_documentSuffix;

    /** The locale of this document container. */
    private Locale m_locale;

    /** Signals whether this document exists with different locale file name extensions.  */
    private boolean m_localeFileName;

    /** The main document in case this document is an attachment. */
    private CmsDocumentDependency m_mainDocument;

    /** The VFS resource for which the dependencies are calculated. */
    private CmsPublishedResource m_resource;

    /** Stores the root path of this dependency. */
    private String m_rootPath;

    /** The dependency type for this dependency. */
    private DependencyType m_type;

    /** The language version dependencies. */
    private List<CmsDocumentDependency> m_variants = new ArrayList<CmsDocumentDependency>();

    /**
     * Creates a new dependency container for the given VFS resource.<p>
     *
     * @param res the VFS resource for which the dependencies are calculated
     */
    private CmsDocumentDependency(CmsPublishedResource res) {

        this(res, res.getRootPath());
    }

    /**
     * Creates a new dependency container for the given VFS resource.<p>
     *
     * Additional constructor with extra root path, to be used only for test cases.<p>
     *
     * @param resource the VFS resource for which the dependencies are calculated
     * @param rootPath the root path to use
     */
    private CmsDocumentDependency(CmsPublishedResource resource, String rootPath) {

        m_resource = resource;
        m_rootPath = rootPath;
        String docName = CmsResource.getName(rootPath);

        // check if an attachment number is present
        Matcher matcher = CmsStringUtil.PATTERN_NUMBER_SUFFIX.matcher(docName);
        if (matcher.find()) {
            docName = matcher.group(1);
            try {
                Integer partNumber = Integer.valueOf(Integer.parseInt(matcher.group(2)));
                setAttachmentNumber(partNumber);
            } catch (NumberFormatException e) {
                // ignore, can happen if the second group of the matcher is larger than Integer.MAX_VALUE
            }
        }

        Locale locale = null;
        matcher = CmsStringUtil.PATTERN_LOCALE_SUFFIX.matcher(docName);
        if (matcher.find()) {
            docName = matcher.group(1);
            String suffix = matcher.group(2);
            String langString = suffix.substring(0, 2);
            locale = suffix.length() == 5 ? new Locale(langString, suffix.substring(3, 5)) : new Locale(langString);
            m_localeFileName = true;
        }
        if (locale == null) {
            locale = CmsLocaleManager.getDefaultLocale();
        }
        if ((locale == null) || !OpenCms.getLocaleManager().getAvailableLocales().contains(locale)) {
            // check if the determined locale is available in the opencms-system.xml
            // use the default locale as fall-back
            // locale = CmsLocaleManager.getDefaultLocale();
        }
        setLocale(locale);

        // we must remove file suffixes like "plapla.doc", ".pdf" etc. because attachments can have different types
        int index = rootPath.lastIndexOf('.');
        if (index != -1) {
            // store the suffix for comparison to decide if this is a main document or a variation later
            String suffix = rootPath.substring(index);
            setDocumentSuffix(suffix);
            if ((docName.lastIndexOf(suffix) == (docName.length() - suffix.length())) && docName.contains(suffix)) {
                docName = docName.substring(0, docName.length() - suffix.length());
            }
        }
        setDocumentName(CmsResource.getFolderPath(rootPath) + docName);
    }

    /**
     * Creates a new dependency container for the given VFS resource.<p>
     *
     * @param res the VFS resource for which the dependencies are calculated
     */
    private CmsDocumentDependency(CmsResource res) {

        this(new CmsPublishedResource(res, -1, CmsResourceState.STATE_CHANGED));
    }

    /**
     * Creates a dependency object from a String representation.<p>
     *
     * @param input the String representation
     * @param rootPath the root path of the base document of which the dependencies are encoded
     *
     * @return the dependency object created from a String representation
     */
    public static CmsDocumentDependency fromDependencyString(String input, String rootPath) {

        CmsDocumentDependency result = new CmsDocumentDependency(null, rootPath);
        if (input != null) {

            try {
                if (input.startsWith("{")) {
                    JSONObject jsonDoc = new JSONObject(input);
                    result.fromJSON(jsonDoc, rootPath);

                    // main document
                    if (jsonDoc.has(JSON_MAIN)) {

                        JSONObject jsonMain = jsonDoc.getJSONObject(JSON_MAIN);

                        CmsDocumentDependency main = new CmsDocumentDependency(null, jsonMain.getString(JSON_PATH));
                        main.fromJSON(jsonMain, rootPath);
                        result.setMainDocument(main);
                    }
                } else {
                    // special handling for news
                    String[] docs = CmsStringUtil.splitAsArray(input, '|');
                    for (int i = 0; i < docs.length; i++) {
                        String doc = docs[i];

                        String lang = doc.substring(0, 2);
                        Locale loc = new Locale(lang);

                        if (i == 0) {
                            result.setLocale(loc);
                        } else {
                            String rp = doc.substring(3);
                            CmsDocumentDependency dep = new CmsDocumentDependency(null, rp);
                            if (!loc.equals(result.getLocale())) {
                                dep.setLocale(new Locale(lang));
                                result.addVariant(dep);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        }
        return result;
    }

    /**
     * Returns the locale (language) of the given resource based on the resource root path.<p>
     *
     * @param rootPath the resource name to check for the locale information
     *
     * @return the locale of the given resource based on the resource root path
     */
    public static Locale getLocale(String rootPath) {

        return (new CmsDocumentDependency(null, rootPath)).getLocale();
    }

    /**
     * Loads or creates a dependency object for the given parameters.<p>
     *
     * @param cms the current OpenCms user context
     * @param pubRes the published resource to get the dependency object for
     *
     * @return a dependency object for the given parameters
     */
    public static CmsDocumentDependency load(CmsObject cms, CmsPublishedResource pubRes) {

        CmsDocumentDependency result = readFromContext(cms, pubRes.getRootPath());
        if (result == null) {
            result = new CmsDocumentDependency(pubRes);
            result.readDependencies(cms);
        }
        return result;
    }

    /**
     * Loads or creates a dependency object for the given parameters.<p>
     *
     * @param cms the current OpenCms user context
     * @param res the VFS resource to get the dependency object for
     *
     * @return a dependency object for the given parameters
     */
    public static CmsDocumentDependency load(CmsObject cms, CmsResource res) {

        CmsDocumentDependency result = readFromContext(cms, res.getRootPath());
        if (result == null) {
            result = new CmsDocumentDependency(res);
            result.readDependencies(cms);
        }
        return result;
    }

    /**
     * Loads or creates a dependency object for the given parameters.<p>
     *
     * @param cms the current OpenCms user context
     * @param res the VFS resource to get the dependency object for
     * @param resources the resource folder data to check for dependencies
     *
     * @return a dependency object for the given parameters
     */
    public static CmsDocumentDependency load(CmsObject cms, CmsResource res, List<CmsResource> resources) {

        CmsDocumentDependency result = readFromContext(cms, res.getRootPath());
        if (result == null) {
            result = new CmsDocumentDependency(res);
            result.readDependencies(cms, resources);
        }
        return result;
    }

    /**
     * Creates a dependency object for the given root path, to be used only for test cases.<p>
     *
     * @param rootPath the root path to create the dependency object for
     *
     * @return a dependency object for the given parameters
     */
    protected static CmsDocumentDependency loadForTest(String rootPath) {

        return new CmsDocumentDependency(null, rootPath);
    }

    /**
     * Removes the dependency object for a published resource from the OpenCms
     * runtime context.<p>
     *
     * <b>Please note:</b> This must be used with caution since the information
     * may be required to generate documents for several configured indexes. It
     * must be ensured that this is called only when all indexes have been
     * updated.<p>
     *
     * @param cms the current OpenCms user context
     * @param pubRes the published resource info
     *
     * @see #storeInContext(CmsObject)
     */
    protected static void removeFromContext(CmsObject cms, CmsPublishedResource pubRes) {

        cms.getRequestContext().removeAttribute(getAttributeKey(pubRes.getRootPath()));
    }

    /**
     * Generates a context attribute name for a root path.
     *
     * @param rootPath the root path
     *
     * @return the Attr_Doc_Deps + rootPapth
     */
    private static String getAttributeKey(String rootPath) {

        return ATTR_DOC_DEPENDENCY + rootPath;
    }

    /**
     * Reads the dependency object for the given root path in the OpenCms runtime context.<p>
     *
     * @param cms the current OpenCms user context
     * @param rootPath the root path to look up the dependency object for
     *
     * @return the deps
     */
    private static CmsDocumentDependency readFromContext(CmsObject cms, String rootPath) {

        return (CmsDocumentDependency)cms.getRequestContext().getAttribute(getAttributeKey(rootPath));
    }

    /**
     * Adds another document attachment dependency to this document.<p>
     *
     * @param dep the document attachment dependency to add
     */
    public void addAttachment(CmsDocumentDependency dep) {

        // don't add attachment if this is a language version of an already existing attachment
        boolean exist = false;
        for (CmsDocumentDependency att : m_attachments) {
            if (att.getAttachmentNumber() == dep.getAttachmentNumber()) {

                if (m_locale.equals(dep.getLocale())) {
                    // if dependency has same locale as main document it is added as attachment
                    // and gets the old attachment as a language-version with all previous language-versions
                    for (CmsDocumentDependency langAtt : att.getVariants()) {
                        dep.addVariant(langAtt);
                    }
                    dep.addVariant(att);
                    m_attachments.remove(att);
                } else {
                    exist = true;
                    att.addVariant(dep);
                }
                break;
            }
        }

        if (!exist) {
            dep.setType(DependencyType.attachment);
            m_attachments.add(dep);
        } else {
            dep.setType(DependencyType.variant);
        }
        addDependency(dep);
    }

    /**
     * Adds another document dependency to this document.<p>
     *
     * @param dep the document dependency to add
     */
    public void addDependency(CmsDocumentDependency dep) {

        m_dependencies.add(dep);
    }

    /**
     * Adds another language version document dependency to this document.<p>
     *
     * @param dep the language version document dependency to add
     */
    public void addVariant(CmsDocumentDependency dep) {

        // check if already exists
        for (CmsDocumentDependency lang : m_variants) {
            if (lang.getLocale().equals(dep.getLocale())) {
                return;
            }
        }
        dep.setType(DependencyType.variant);
        m_variants.add(dep);
        addDependency(dep);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsDocumentDependency) {
            CmsDocumentDependency other = (CmsDocumentDependency)obj;
            return m_resource.getRootPath().equals(other.getResource().getRootPath())
                && m_locale.equals(other.m_locale);
        }
        return false;
    }

    /**
     * Read the information out of the given JSON object to fill
     * the values of the document.<p>
     *
     * @param json the JSON object with the information about this document
     * @param rootPath the current path the home division
     */
    public void fromJSON(JSONObject json, String rootPath) {

        try {
            // language versions
            if (json.has(JSON_LANGUAGES)) {
                JSONArray jsonLanguages = json.getJSONArray(JSON_LANGUAGES);
                for (int i = 0; i < jsonLanguages.length(); i++) {
                    JSONObject jsonLang = (JSONObject)jsonLanguages.get(i);

                    CmsDocumentDependency lang = new CmsDocumentDependency(null, jsonLang.getString(JSON_PATH));
                    lang.fromJSON(jsonLang, rootPath);
                    addVariant(lang);
                }
            }

            // attachments
            if (json.has(JSON_ATTACHMENTS)) {
                JSONArray jsonAttachments = json.getJSONArray(JSON_ATTACHMENTS);
                for (int i = 0; i < jsonAttachments.length(); i++) {
                    try {
                        JSONObject jsonAttachment = (JSONObject)jsonAttachments.get(i);

                        CmsDocumentDependency att = new CmsDocumentDependency(
                            null,
                            jsonAttachment.getString(JSON_PATH));
                        att.fromJSON(jsonAttachment, rootPath);

                        // language versions of attachment
                        if (jsonAttachment.has(JSON_LANGUAGES)) {
                            JSONArray jsonAttLanguages = jsonAttachment.getJSONArray(JSON_LANGUAGES);
                            for (int j = 0; j < jsonAttLanguages.length(); j++) {
                                JSONObject jsonAttLanguage = (JSONObject)jsonAttLanguages.get(j);

                                CmsDocumentDependency attLang = new CmsDocumentDependency(
                                    null,
                                    jsonAttLanguage.getString(JSON_PATH));
                                attLang.fromJSON(jsonAttLanguage, rootPath);
                                att.addVariant(attLang);
                            }
                        }
                        addAttachment(att);
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        } catch (Exception ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Returns the attachment number.<p>
     *
     * @return the attachment number
     */
    public int getAttachmentNumber() {

        if (m_attachmentNumber != null) {
            return m_attachmentNumber.intValue();
        }
        return 0;
    }

    /**
     * Returns the attachments.<p>
     *
     * @return the attachments
     */
    public List<CmsDocumentDependency> getAttachments() {

        return m_attachments;
    }

    /**
     * Returns the list of resources the main resource depends on, including the main resource itself.<p>
     *
     * @return the list of resources the main resource depends on, including the main resource itself
     */
    public List<CmsDocumentDependency> getDependencies() {

        return m_dependencies;
    }

    /**
     * Returns the file name of the document without attachment or locale suffixes.<p>
     *
     * @return the file name of the document without attachment or locale suffixes
     */
    public String getDocumentName() {

        return m_documentName;
    }

    /**
     * Returns the suffix of the document.<p>
     *
     * @return the suffix of the document
     */
    public String getDocumentSuffix() {

        return m_documentSuffix;
    }

    /**
     * Returns the locale of this document container.<p>
     *
     * @return the locale of this document container
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the main document in case this document is an attachment.<p>
     *
     * @return the main document in case this document is an attachment
     */
    public CmsDocumentDependency getMainDocument() {

        return m_mainDocument;
    }

    /**
     * Returns the VFS resource for which the dependencies are calculated.<p>
     *
     * @return the VFS resource for which the dependencies are calculated
     */
    public CmsPublishedResource getResource() {

        return m_resource;
    }

    /**
     * Returns the root path of this dependency.<p>
     *
     * @return the root path
     */
    public String getRootPath() {

        if (m_resource != null) {
            return m_resource.getRootPath();
        } else {
            return m_rootPath;
        }
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public DependencyType getType() {

        return m_type;
    }

    /**
     * Returns the variants.<p>
     *
     * @return the variants
     */
    public List<CmsDocumentDependency> getVariants() {

        return m_variants;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getResource().hashCode();
    }

    /**
     * Returns the locale file name flag.<p>
     *
     * @return the locale file name flag
     */
    public boolean hasLocaleFileName() {

        return m_localeFileName;
    }

    /**
     * Returns true if this document is an attachment, i.e. an attachment number is provided.<p>
     *
     * @return true if this document is an attachment, otherwise false
     */
    public boolean isAttachment() {

        return m_attachmentNumber != null;
    }

    /**
     * Reads all dependencies that exist for this main resource in the OpenCms VFS.<p>
     *
     * To be used when incremental updating an index.<p>
     *
     * @param cms the current users OpenCms context
     */
    public void readDependencies(CmsObject cms) {

        try {
            // read all resources in the parent folder of the published resource
            List<CmsResource> folderContent = cms.getResourcesInFolder(
                CmsResource.getParentFolder(cms.getRequestContext().removeSiteRoot(getResource().getRootPath())),
                CmsResourceFilter.DEFAULT);
            // now calculate the dependencies form the folder content that has been read
            readDependencies(cms, folderContent);
        } catch (CmsException e) {
            LOG.warn("Unable to read dependencies for " + getResource().getRootPath(), e);
        }
    }

    /**
     * Reads all dependencies that exist for this main resource in provided list of resources.<p>
     *
     * @param cms the current users OpenCms context
     * @param folderContent the contents of the folder to check the dependencies for
     */
    public void readDependencies(CmsObject cms, List<CmsResource> folderContent) {

        Map<Integer, CmsDocumentDependency> attachments = new HashMap<Integer, CmsDocumentDependency>();
        if (isAttachment()) {
            attachments.put(Integer.valueOf(getAttachmentNumber()), this);
        }

        // iterate all resources in the folder to check if this is a language version
        Iterator<CmsResource> i = folderContent.iterator();
        while (i.hasNext()) {
            CmsResource r = i.next();
            // only add files and don't add the resource itself again
            if (r.isFile() && !getResource().getRootPath().equals(r.getRootPath())) {
                CmsPublishedResource pubRes = new CmsPublishedResource(
                    r,
                    getResource().getPublishTag(),
                    CmsResourceState.STATE_CHANGED);
                CmsDocumentDependency dep = new CmsDocumentDependency(pubRes);
                if (getDocumentName().equals(dep.getDocumentName())) {
                    if (isAttachment()) {
                        // this document is an attachment
                        if (dep.isAttachment()) {
                            if ((getAttachmentNumber() == dep.getAttachmentNumber()) && dep.hasLocaleFileName()) {
                                // the dependency must be a language version of this attachment document
                                addVariant(dep);
                            }
                            if (getAttachmentNumber() == dep.getAttachmentNumber()) {
                                dep.setMainDocument(dep);
                            } else {
                                Integer attNum = Integer.valueOf(dep.getAttachmentNumber());
                                CmsDocumentDependency att = attachments.get(attNum);
                                if (att != null) {
                                    att.addVariant(dep);
                                } else {
                                    attachments.put(attNum, dep);
                                }
                            }
                        } else {
                            // this is the main document of the dependency
                            setMainDocument(dep);
                        }
                    } else {
                        // this document is a main document
                        if (dep.isAttachment()) {
                            // add this dependency as an attachment
                            addAttachment(dep);
                        } else if (CmsStringUtil.isEqual(getDocumentSuffix(), dep.getDocumentSuffix())) {
                            // if this is no attachment, and the file suffix is equal,
                            // this must be a language version of the main document
                            addVariant(dep);
                        }
                        // if the file suffix is NOT equal, this is a new main document
                        setMainDocument(this);
                    }
                }
            }
        }

        if (m_mainDocument != null) {
            for (CmsDocumentDependency att : attachments.values()) {
                m_mainDocument.addAttachment(att);
            }
            // add the main document as dependency for this attachment
            addDependency(m_mainDocument);
            for (CmsDocumentDependency var : getVariants()) {
                String mainFileName = getMainDocument().getDocumentName();
                if (getMainDocument().getDocumentSuffix() != null) {
                    mainFileName += getMainDocument().getDocumentSuffix();
                }
                if (mainFileName.equals(var.getResource().getRootPath())) {
                    setType(DependencyType.variant);
                    break;
                } else {
                    setType(DependencyType.document);
                    break;
                }
            }
        }
    }

    /**
     * Sets the attachment number.<p>
     *
     * @param attachmentNumber the attachment number
     */
    public void setAttachmentNumber(Integer attachmentNumber) {

        m_attachmentNumber = attachmentNumber;
    }

    /**
     * Sets the file name of the document without attachment or locale suffixes.<p>
     *
     * @param documentName the file name of the document without attachment or locale suffixes
     */
    public void setDocumentName(String documentName) {

        m_documentName = documentName;
    }

    /**
     * Sets the suffix (.pdf, .doc etc.) of the document.<p>
     *
     * @param documentSuffix the suffix to set
     */
    public void setDocumentSuffix(String documentSuffix) {

        m_documentSuffix = documentSuffix;
    }

    /**
     * Sets the locale of this document container.<p>
     *
     * @param locale the locale of this document container
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the main document in case this document is an attachment.<p>
     *
     * @param mainDocument the main document to set
     */
    public void setMainDocument(CmsDocumentDependency mainDocument) {

        if (m_mainDocument == null) {
            // we currently have no main document at all
            m_mainDocument = mainDocument;
        } else {
            // check if we find a better match for the main document locale
            if (mainDocument.getLocale().equals(getLocale())) {
                // mainDocument.addVariant(m_mainDocument);
                // main document locale is the "best" one
                m_mainDocument = mainDocument;
            } else {
                // check if the new document is a "better" one
                List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales();
                int pos1 = locales.indexOf(m_mainDocument.getLocale());
                if (pos1 > 0) {
                    int pos2 = locales.indexOf(mainDocument.getLocale());
                    if (pos2 < pos1) {
                        mainDocument.addVariant(m_mainDocument);
                        // locale is closer to the default
                        m_mainDocument = mainDocument;
                    }
                } else {
                    m_mainDocument.addVariant(mainDocument);
                }
            }
        }
    }

    /**
     * Sets the type for this dependency.<p>
     *
     * @param type the type to set
     */
    public void setType(DependencyType type) {

        m_type = type;
    }

    /**
     * Stores this dependency object for a published resource in the OpenCms runtime context.<p>
     *
     * This done to optimize indexing speed. When the index update information is calculated,
     * all dependencies for a resource must be calculated also. The same information is later needed when
     * the Lucene document is created, for example in order to store the list of other available languages.<p>
     *
     * @param cms the current OpenCms user context
     */
    public void storeInContext(CmsObject cms) {

        cms.getRequestContext().setAttribute(getAttributeKey(getResource().getRootPath()), this);
    }

    /**
     * Creates the String representation of this dependency object.<p>
     *
     * @param cms the current OpenCms user context
     *
     * @return the String representation of this dependency object
     */
    public String toDependencyString(CmsObject cms) {

        JSONObject jsonDoc = toJSON(cms, true);

        try {
            if ((!isAttachment()) && (m_attachments != null)) {
                // iterate through all attachments
                JSONArray jsonAttachments = new JSONArray();
                for (CmsDocumentDependency att : m_attachments) {
                    JSONObject jsonAttachment = att.toJSON(cms, true);
                    jsonAttachments.put(jsonAttachment);
                }
                jsonDoc.put(JSON_ATTACHMENTS, jsonAttachments);
            } else if (isAttachment()) {
                CmsDocumentDependency main = getMainDocument();
                if (main != null) {
                    JSONObject jsonMain = main.toJSON(cms, true);
                    // iterate through all attachments of the main document
                    List<CmsDocumentDependency> attachments = main.getAttachments();
                    if (attachments != null) {
                        JSONArray jsonAttachments = new JSONArray();
                        for (CmsDocumentDependency att : attachments) {
                            JSONObject jsonAttachment = att.toJSON(cms, true);
                            jsonAttachments.put(jsonAttachment);
                        }
                        jsonMain.put(JSON_ATTACHMENTS, jsonAttachments);
                    }
                    jsonDoc.put(JSON_MAIN, jsonMain);
                }
            }
            return jsonDoc.toString();
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * Returns a JSON object describing this dependency document.<p>
     *
     * @param cms the current cms object
     * @param includeLang flag if language versions should be included
     *
     * @return a JSON object describing this dependency document
     */
    public JSONObject toJSON(CmsObject cms, boolean includeLang) {

        try {
            CmsObject clone = OpenCms.initCmsObject(cms);
            clone.getRequestContext().setSiteRoot("");
            JSONObject jsonAttachment = new JSONObject();
            CmsResource res = clone.readResource(m_rootPath, CmsResourceFilter.IGNORE_EXPIRATION);
            Map<String, String> props = CmsProperty.toMap(clone.readPropertyObjects(res, false));
            // id and path
            jsonAttachment.put(JSON_UUID, res.getStructureId());
            jsonAttachment.put(JSON_PATH, res.getRootPath());
            // title
            jsonAttachment.put(JSON_TITLE, props.get(CmsPropertyDefinition.PROPERTY_TITLE));
            // date created
            jsonAttachment.put(JSON_DATE_CREATED, res.getDateCreated());
            // date created
            jsonAttachment.put(JSON_LOCALE, m_locale);
            // date modified
            jsonAttachment.put(JSON_DATE_MODIFIED, res.getDateLastModified());

            if (includeLang) {
                // get all language versions of the document
                List<CmsDocumentDependency> langs = getVariants();
                if (langs != null) {
                    JSONArray jsonLanguages = new JSONArray();
                    for (CmsDocumentDependency lang : langs) {

                        JSONObject jsonLanguage = lang.toJSON(cms, false);
                        jsonLanguages.put(jsonLanguage);
                    }
                    jsonAttachment.put(JSON_LANGUAGES, jsonLanguages);
                }
            }
            return jsonAttachment;
        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        if (m_resource != null) {
            return m_resource.toString();
        } else {
            return m_rootPath;
        }
    }
}
