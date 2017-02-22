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

package org.opencms.file;

import org.opencms.file.types.A_CmsResourceTypeLinkParseable;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsRegexSubstitution;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.Messages;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.dom4j.Document;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * A class used to rewrite links and relations in one subtree such that relations from that subtree to another given subtree
 * replaced with relations to the first subtree.<p>
 */
public class CmsLinkRewriter {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLinkRewriter.class);

    /** A map from source folder structure ids to corresponding target folder resources. */
    protected Map<CmsUUID, CmsResource> m_translationsById = new HashMap<CmsUUID, CmsResource>();

    /** A map from source folder root paths to the corresponding target folder resources. */
    protected Map<String, CmsResource> m_translationsByPath = new HashMap<String, CmsResource>();

    /** A map of resources which have been cached by structure id. */
    private Map<CmsUUID, CmsResource> m_cachedResources = new HashMap<CmsUUID, CmsResource>();

    /** The CMS object used for file operations. */
    private CmsObject m_cms;

    /** If true, all XML contents will be rewritten instead of just those containing links to correct. */
    private boolean m_rewriteAllXmlContents = true;

    /** The set of structure ids of resources whose content has been rewritten. */
    private Set<CmsUUID> m_rewrittenContent = new HashSet<CmsUUID>();

    /** A list of path pairs, each containing a source and a target of a copy operation. */
    private List<CmsPair<String, String>> m_sourceTargetPairs = new ArrayList<CmsPair<String, String>>();

    /** The target folder root path. */
    private String m_targetPath;

    /**
     * Creates a link rewriter for use after a multi-copy operation.<p>
     *
     * @param cms the current CMS context
     * @param sources the list of source root paths
     * @param target the target parent folder root path
     */
    public CmsLinkRewriter(CmsObject cms, List<String> sources, String target) {

        m_sourceTargetPairs = new ArrayList<CmsPair<String, String>>();
        for (String source : sources) {
            checkNotSubPath(source, target);
            String targetSub = CmsStringUtil.joinPaths(target, CmsResource.getName(source));
            m_sourceTargetPairs.add(CmsPair.create(source, targetSub));
        }
        m_targetPath = target;
        m_cms = cms;
    }

    /**
     * Creates a new link rewriter for a list of sources and corresponding targets.<p>
     *
     * @param cms the current CMS context
     * @param targetPath the target root path
     * @param sourceTargetPairs the list of source-target pairs
     */
    public CmsLinkRewriter(CmsObject cms, String targetPath, List<CmsPair<String, String>> sourceTargetPairs) {

        m_cms = cms;
        m_targetPath = targetPath;
        m_sourceTargetPairs = sourceTargetPairs;
    }

    /**
     * Creates a link rewriter for use after a single copy operation.<p>
     *
     * @param cms the current CMS context
     * @param source the source folder root path
     * @param target the target folder root path
     */
    public CmsLinkRewriter(CmsObject cms, String source, String target) {

        m_sourceTargetPairs = new ArrayList<CmsPair<String, String>>();
        checkNotSubPath(source, target);

        m_sourceTargetPairs.add(CmsPair.create(source, target));
        m_targetPath = target;
        m_cms = cms;
    }

    /**
     * Checks whether a given resource is a folder and throws an exception otherwise.<p>
     *
     * @param resource the resource to check
     * @throws CmsException if something goes wrong
     */
    protected static void checkIsFolder(CmsResource resource) throws CmsException {

        if (!isFolder(resource)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(
                    org.opencms.file.Messages.ERR_REWRITE_LINKS_ROOT_NOT_FOLDER_1,
                    resource.getRootPath()));
        }
    }

    /**
     * Helper method to check whether a given resource is a folder.<p>
     *
     * @param resource the resouce to check
     * @return true if the resource is a folder
     *
     * @throws CmsLoaderException if the resource type couldn't be found
     */
    protected static boolean isFolder(CmsResource resource) throws CmsLoaderException {

        I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        return resourceType.isFolder();
    }

    /**
     * Starts the link rewriting process.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void rewriteLinks() throws CmsException {

        init();
        List<CmsRelation> relationsToCorrect = findRelationsFromTargetToSource();
        // group relations by the structure id of their source
        Multimap<CmsUUID, CmsRelation> relationsBySourceId = ArrayListMultimap.create();
        for (CmsRelation relation : relationsToCorrect) {
            LOG.info(
                "Found relation which needs to be corrected: "
                    + relation.getSourcePath()
                    + " -> "
                    + relation.getTargetPath()
                    + " ["
                    + relation.getType().getName()
                    + "]");
            relationsBySourceId.put(relation.getSourceId(), relation);
        }

        // make sure we have a lock on the target folder before doing any write operations
        CmsLock lock = m_cms.getLock(m_targetPath);
        if (lock.isUnlocked() || !lock.isOwnedBy(m_cms.getRequestContext().getCurrentUser())) {
            // fail if locked by another user
            m_cms.lockResource(m_targetPath);
        }

        for (CmsUUID structureId : relationsBySourceId.keySet()) {

            Collection<CmsRelation> relationsForResource = relationsBySourceId.get(structureId);
            CmsResource resource = null;
            try {
                resource = getResource(structureId);
                rewriteLinks(resource, relationsForResource);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (!m_rewriteAllXmlContents) {
            return;
        }
        for (Map.Entry<CmsUUID, CmsResource> entry : m_cachedResources.entrySet()) {
            CmsUUID key = entry.getKey();
            CmsResource resource = entry.getValue();
            if (isInTargets(resource.getRootPath()) && !m_rewrittenContent.contains(key)) {
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                // rewrite content for other files so
                if (resType instanceof A_CmsResourceTypeLinkParseable) {
                    try {
                        CmsFile file = m_cms.readFile(resource);
                        m_cms.writeFile(file);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        copyLocaleRelations();
    }

    /**
     * Sets the 'rewriteAllContents' flag, which controls whether all XML contents will be rewritten
     * or just those whose links need to be corrected.<p>
     *
     * @param rewriteAllContents if true, all contents will be rewritten
     */
    public void setRewriteAllContents(boolean rewriteAllContents) {

        m_rewriteAllXmlContents = rewriteAllContents;
    }

    /**
     * Checks that the target path is not a subfolder of the source path.<p>
     *
     * @param source the source path
     * @param target the target path
     */
    protected void checkNotSubPath(String source, String target) {

        source = CmsStringUtil.joinPaths("/", source, "/");
        target = CmsStringUtil.joinPaths("/", target, "/");
        if (target.startsWith(source)) {
            throw new CmsIllegalArgumentException(
                org.opencms.file.Messages.get().container(
                    org.opencms.file.Messages.ERR_REWRITE_LINKS_ROOTS_DEPENDENT_2,
                    source,
                    target));
        }
    }

    /**
     * Separate method for copying locale relations..<p>
     *
     * This is necessary because the default copy mechanism does not copy locale relations.
     *
     * @throws CmsException if something goes wrong
     */
    protected void copyLocaleRelations() throws CmsException {

        long start = System.currentTimeMillis();
        List<CmsRelation> localeRelations = m_cms.readRelations(
            CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT));
        for (CmsRelation rel : localeRelations) {
            if (isInSources(rel.getSourcePath()) && isInSources(rel.getTargetPath())) {
                CmsResource newRelationSource = m_translationsById.get(rel.getSourceId());
                CmsResource newRelationTarget = m_translationsById.get(rel.getTargetId());
                if ((newRelationSource != null) && (newRelationTarget != null)) {
                    try {
                        m_cms.addRelationToResource(
                            newRelationSource,
                            newRelationTarget,
                            CmsRelationType.LOCALE_VARIANT.getName());
                    } catch (CmsException e) {
                        LOG.error("Could not transfer locale relation: " + e.getLocalizedMessage(), e);
                    }
                } else {
                    LOG.warn("Could not transfer locale relation because source/target not found in copy: " + rel);
                }
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("Copied locale relations, took " + (end - start) + "ms");
    }

    /**
     * Decodes a byte array into a string with a given encoding, or the default encoding if that fails.<p>
     *
     * @param bytes the byte array
     * @param encoding the encoding to use
     *
     * @return the decoded string
     */
    protected String decode(byte[] bytes, String encoding) {

        try {
            return new String(bytes, encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    /**
     * Decodes a file's contents and return the content string and the encoding to use for writing the file
     * back to the VFS.<p>
     *
     * @param file the file to decode
     * @return a pair (content, encoding)
     * @throws CmsException if something goes wrong
     */
    protected CmsPair<String, String> decode(CmsFile file) throws CmsException {

        String content = null;
        String encoding = getConfiguredEncoding(m_cms, file);
        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(file.getTypeId());
        if (resType instanceof CmsResourceTypeJsp) {
            content = decode(file.getContents(), encoding);
        } else {
            try {
                CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(m_cms);
                // parse the XML and serialize it back to  a string with the configured encoding
                Document doc = CmsXmlUtils.unmarshalHelper(file.getContents(), resolver);
                content = CmsXmlUtils.marshal(doc, encoding);
            } catch (Exception e) {
                // invalid xml structure, just use the configured encoding
                content = decode(file.getContents(), encoding);
            }
        }
        return CmsPair.create(content, encoding);
    }

    /**
     * Finds relations from the target root folder or its children to the source root folder or its children.<p>
     *
     * @return the list of relations from the target to the source
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsRelation> findRelationsFromTargetToSource() throws CmsException {

        List<CmsRelation> relations = m_cms.readRelations(
            CmsRelationFilter.SOURCES.filterPath(m_targetPath).filterIncludeChildren());
        List<CmsRelation> result = new ArrayList<CmsRelation>();
        for (CmsRelation rel : relations) {
            if (isInTargets(rel.getSourcePath()) && isInSources(rel.getTargetPath())) {
                result.add(rel);
            }
        }
        return result;
    }

    /**
     * Gets the encoding which is configured at the location of a given resource.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource for which the configured encoding should be retrieved
     * @return the configured encoding for the resource
     *
     * @throws CmsException if something goes wrong
     */
    protected String getConfiguredEncoding(CmsObject cms, CmsResource resource) throws CmsException {

        String encoding = null;
        try {
            encoding = cms.readPropertyObject(
                resource.getRootPath(),
                CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                true).getValue();
        } catch (CmsException e) {
            // encoding will be null
        }
        if (encoding == null) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        } else {
            encoding = CmsEncoder.lookupEncoding(encoding, null);
            if (encoding == null) {
                throw new CmsXmlException(
                    Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ENC_1, resource.getRootPath()));
            }
        }
        return encoding;
    }

    /**
     * Gets a list of resource pairs whose paths relative to the source/target roots passed match.<p>
     *
     * @param source the source root
     * @param target the target root
     *
     * @return the list of matching resources
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsPair<CmsResource, CmsResource>> getMatchingResources(String source, String target)
    throws CmsException {

        List<CmsResource> sourceResources = readTree(source);
        Map<String, CmsResource> sourceRelative = getResourcesByRelativePath(sourceResources, source);

        List<CmsResource> targetResources = readTree(target);
        Map<String, CmsResource> targetRelative = getResourcesByRelativePath(targetResources, target);

        List<CmsPair<CmsResource, CmsResource>> result = new ArrayList<CmsPair<CmsResource, CmsResource>>();
        sourceRelative.keySet().retainAll(targetRelative.keySet());
        for (Map.Entry<String, CmsResource> entry : sourceRelative.entrySet()) {
            String key = entry.getKey();
            CmsResource sourceRes = entry.getValue();
            CmsResource targetRes = targetRelative.get(key);
            result.add(CmsPair.create(sourceRes, targetRes));
        }
        return result;
    }

    /**
     * Computes the relative path given an ancestor folder path.<p>
     *
     * @param ancestor the ancestor folder
     * @param rootPath the path for which the relative path should be computed
     *
     * @return the relative path
     */
    protected String getRelativePath(String ancestor, String rootPath) {

        String result = rootPath.substring(ancestor.length());
        result = CmsStringUtil.joinPaths("/", result, "/");
        return result;
    }

    /**
     * Accesses a resource by structure id.<p>
     *
     * @param structureId the structure id of the resource
     * @return the resource with the given structure id
     *
     * @throws CmsException if the resource couldn't be read
     */
    protected CmsResource getResource(CmsUUID structureId) throws CmsException {

        if (m_cachedResources.containsKey(structureId)) {
            return m_cachedResources.get(structureId);
        }
        return m_cms.readResource(structureId);
    }

    /**
     * Collects a list of resources in a map where the key for each resource is the path relative to a given folder.<p>
     *
     * @param resources the resources to put in the map
     * @param basePath the path relative to which the keys of the resulting map should be computed
     *
     * @return a map from relative paths to resources
     */
    protected Map<String, CmsResource> getResourcesByRelativePath(List<CmsResource> resources, String basePath) {

        Map<String, CmsResource> result = new HashMap<String, CmsResource>();
        for (CmsResource resource : resources) {
            String relativeSubPath = CmsStringUtil.getRelativeSubPath(basePath, resource.getRootPath());
            if (relativeSubPath != null) {
                result.put(relativeSubPath, resource);
            }
        }
        return result;
    }

    /**
     * Reads the data needed for rewriting the relations from the VFS.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void init() throws CmsException {

        m_cms = OpenCms.initCmsObject(m_cms);
        // we want to use autocorrection when writing XML contents back
        //m_cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
        m_cms.getRequestContext().setSiteRoot("");
        List<CmsPair<CmsResource, CmsResource>> allMatchingResources = Lists.newArrayList();
        for (CmsPair<String, String> pair : m_sourceTargetPairs) {
            List<CmsPair<CmsResource, CmsResource>> matchingResources = getMatchingResources(
                pair.getFirst(),
                pair.getSecond());
            allMatchingResources.addAll(matchingResources);
        }
        for (CmsPair<CmsResource, CmsResource> resPair : allMatchingResources) {
            CmsResource source = resPair.getFirst();
            CmsResource target = resPair.getSecond();
            m_translationsById.put(source.getStructureId(), target);
            m_translationsByPath.put(source.getRootPath(), target);
        }
    }

    /**
     * Checks if a path belongs to one of the sources.<p>
     *
     * @param path a root path
     *
     * @return true if the path belongs to the sources
     */
    protected boolean isInSources(String path) {

        for (CmsPair<String, String> sourceTargetPair : m_sourceTargetPairs) {
            String source = sourceTargetPair.getFirst();
            if (CmsStringUtil.joinPaths(path, "/").startsWith(CmsStringUtil.joinPaths(source, "/"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a path belongs to one of the targets.<p>
     *
     * @param path a root path
     *
     * @return true if the path belongs to the targets
     */
    protected boolean isInTargets(String path) {

        for (CmsPair<String, String> sourceTargetPair : m_sourceTargetPairs) {
            String target = sourceTargetPair.getSecond();
            if (CmsStringUtil.joinPaths(path, "/").startsWith(CmsStringUtil.joinPaths(target, "/"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the resources in a subtree.<p>
     *
     * @param rootPath the root of the subtree
     *
     * @return the list of resources from the subtree
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> readTree(String rootPath) throws CmsException {

        rootPath = CmsFileUtil.removeTrailingSeparator(rootPath);
        CmsResource base = m_cms.readResource(rootPath);

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(base);
        List<CmsResource> result = new ArrayList<CmsResource>();
        if (resType.isFolder()) {
            rootPath = CmsStringUtil.joinPaths(rootPath, "/");
            List<CmsResource> subResources = m_cms.readResources(rootPath, CmsResourceFilter.ALL, true);
            result.add(base);
            result.addAll(subResources);
        } else {
            result.add(base);
        }
        for (CmsResource resource : result) {
            m_cachedResources.put(resource.getStructureId(), resource);
        }

        return result;
    }

    /**
     * Rewrites the links included in the content itself.<p>
     *
     * @param file the file for which the links should be replaced
     * @param relations the original relations
     *
     * @throws CmsException if something goes wrong
     */
    protected void rewriteContent(CmsFile file, Collection<CmsRelation> relations) throws CmsException {

        LOG.info("Rewriting in-content links for " + file.getRootPath());
        CmsPair<String, String> contentAndEncoding = decode(file);
        String content = contentAndEncoding.getFirst();
        String encodingForSave = contentAndEncoding.getSecond();
        String newContent = rewriteContentString(content);
        byte[] newContentBytes;
        try {
            newContentBytes = newContent.getBytes(encodingForSave);
        } catch (UnsupportedEncodingException e) {
            newContentBytes = newContent.getBytes();
        }
        file.setContents(newContentBytes);
        m_cms.writeFile(file);
    }

    /**
     * Replaces structure ids of resources in the source subtree with the structure ids of the corresponding
     * resources in the target subtree inside a content string.<p>
     *
     * @param originalContent the original content
     *
     * @return the content with the new structure ids
     */
    protected String rewriteContentString(String originalContent) {

        Pattern uuidPattern = Pattern.compile(CmsUUID.UUID_REGEX);
        I_CmsRegexSubstitution substitution = new I_CmsRegexSubstitution() {

            public String substituteMatch(String text, Matcher matcher) {

                String uuidString = text.substring(matcher.start(), matcher.end());
                CmsUUID uuid = new CmsUUID(uuidString);
                String result = uuidString;
                if (m_translationsById.containsKey(uuid)) {
                    result = m_translationsById.get(uuid).getStructureId().toString();
                }
                return result;
            }
        };
        return CmsStringUtil.substitute(uuidPattern, originalContent, substitution);
    }

    /**
     * Rewrites the links for a single resource.<p>
     *
     * @param resource the resource for which the links should be rewritten
     * @param relations the relations to the source folder which have this resource as its source
     *
     * @throws CmsException if something goes wrong
     */
    protected void rewriteLinks(CmsResource resource, Collection<CmsRelation> relations) throws CmsException {

        LOG.info("Rewriting relations for resource " + resource.getRootPath());
        I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        boolean hasContentLinks = false;
        boolean hasOtherLinks = false;

        for (CmsRelation relation : relations) {
            if (relation.getType().isDefinedInContent()) {
                hasContentLinks = true;
            } else {
                hasOtherLinks = true;
            }
        }
        if (hasContentLinks) {
            LOG.info("The resource " + resource.getRootPath() + " has links in the content.");
        }
        if (hasOtherLinks) {
            LOG.info("The resource " + resource.getRootPath() + " has non-content links.");
        }

        if (hasContentLinks) {
            if (resourceType instanceof I_CmsLinkParseable) {
                CmsFile file = m_cms.readFile(resource);
                rewriteContent(file, relations);
                m_rewrittenContent.add(file.getStructureId());
            }
        }
        if (hasOtherLinks) {
            rewriteOtherRelations(resource, relations);
        }
    }

    /**
     * Rewrites relations which are not derived from links in the content itself.<p>
     *
     * @param res the resource for which to rewrite the relations
     * @param relations the original relations
     *
     * @throws CmsException if something goes wrong
     */
    protected void rewriteOtherRelations(CmsResource res, Collection<CmsRelation> relations) throws CmsException {

        LOG.info("Rewriting non-content links for " + res.getRootPath());
        for (CmsRelation rel : relations) {
            CmsUUID targetId = rel.getTargetId();
            CmsResource newTargetResource = m_translationsById.get(targetId);
            CmsRelationType relType = rel.getType();
            if (!relType.isDefinedInContent()) {
                if (newTargetResource != null) {
                    m_cms.deleteRelationsFromResource(
                        rel.getSourcePath(),
                        CmsRelationFilter.TARGETS.filterStructureId(rel.getTargetId()).filterType(relType));
                    m_cms.addRelationToResource(
                        rel.getSourcePath(),
                        newTargetResource.getRootPath(),
                        relType.getName());
                }
            }
        }
    }
}
