package edu.unc.lib.dl.data.ingest.solr.indexing;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.unc.lib.dl.data.ingest.solr.util.JDOMQueryUtil;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.search.solr.model.IndexDocumentBean;
import edu.unc.lib.dl.search.solr.util.ResourceType;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;
import edu.unc.lib.dl.xml.NamespaceConstants;

public class DocumentIndexingPackage {
	private static final Logger log = LoggerFactory.getLogger(DocumentIndexingPackage.class);

	private static XPath retrieveLabelXPath = JDOMNamespaceUtil.instantiateXPath(
			"foxml:property[@NAME='info:fedora/fedora-system:def/model#label']/@VALUE",
			new Namespace[] { Namespace.getNamespace("foxml", NamespaceConstants.FOXML_URI) });

	private static XPath retrieveObjectPropertiesXPath = JDOMNamespaceUtil.instantiateXPath(
			"/foxml:digitalObject/foxml:objectProperties",
			new Namespace[] { Namespace.getNamespace("foxml", NamespaceConstants.FOXML_URI) });

	private PID pid;
	private DocumentIndexingPackage parentDocument;
	private boolean attemptedToRetrieveDefaultWebObject;
	private DocumentIndexingPackage defaultWebObject;
	private String defaultWebData;
	private Document foxml;
	private Element relsExt;
	private Element mods;
	private Element mdContents;
	private Element objectProperties;
	private IndexDocumentBean document;
	private String label;
	private ResourceType resourceType;

	public DocumentIndexingPackage() {
		document = new IndexDocumentBean();
		this.attemptedToRetrieveDefaultWebObject = false;
	}

	public DocumentIndexingPackage(String pid) {
		this();
		this.pid = new PID(pid);
		document.setId(this.pid.getPid());
	}

	public DocumentIndexingPackage(PID pid, Document foxml) {
		this();
		this.pid = pid;
		document.setId(this.pid.getPid());
		this.foxml = foxml;
	}

	public PID getPid() {
		return pid;
	}

	public void setPid(PID pid) {
		this.pid = pid;
	}

	public DocumentIndexingPackage getParentDocument() {
		return parentDocument;
	}

	public void setParentDocument(DocumentIndexingPackage parentDocument) {
		this.parentDocument = parentDocument;
	}

	public boolean isAttemptedToRetrieveDefaultWebObject() {
		return attemptedToRetrieveDefaultWebObject;
	}

	public void setAttemptedToRetrieveDefaultWebObject(boolean attemptedToRetrieveDefaultWebObject) {
		this.attemptedToRetrieveDefaultWebObject = attemptedToRetrieveDefaultWebObject;
	}

	public DocumentIndexingPackage getDefaultWebObject() {
		return defaultWebObject;
	}

	public void setDefaultWebObject(DocumentIndexingPackage defaultWebObject) {
		this.defaultWebObject = defaultWebObject;
	}

	public String getDefaultWebData() {
		return defaultWebData;
	}

	public void setDefaultWebData(String defaultWebData) {
		this.defaultWebData = defaultWebData;
	}

	public Document getFoxml() {
		return foxml;
	}

	public void setFoxml(Document foxml) {
		this.foxml = foxml;
	}

	public IndexDocumentBean getDocument() {
		return document;
	}

	public void setDocument(IndexDocumentBean document) {
		this.document = document;
	}

	public Element getRelsExt() {
		if (relsExt == null && foxml != null) {
			try {
				Element rdf = extractDatastream(ContentModelHelper.Datastream.RELS_EXT);
				setRelsExt((Element)rdf.getChildren().get(0));
			} catch (NullPointerException e) {
				return null;
			}
		}
		return relsExt;
	}

	public void setRelsExt(Element relsExt) {
		this.relsExt = relsExt;
	}

	public Element getMods() {
		if (mods == null && foxml != null) {
			try {
				setMods(extractDatastream(ContentModelHelper.Datastream.MD_DESCRIPTIVE));
			} catch (NullPointerException e) {
				return null;
			}
		}
		return mods;
	}
	
	public void setMods(Element mods) {
		this.mods = mods;
	}

	public Element getMdContents() {
		if (mdContents == null && foxml != null) {
			try {
				setMdContents(extractDatastream(ContentModelHelper.Datastream.MD_CONTENTS));
			} catch (NullPointerException e) {
				return null;
			}
		}
		return mdContents;
	}
	
	public void setMdContents(Element mdContents) {
		this.mdContents = mdContents;
	}

	private Element extractDatastream(ContentModelHelper.Datastream datastream) {
		Element datastreamEl = JDOMQueryUtil.getChildByAttribute(foxml.getRootElement(), "datastream",
				JDOMNamespaceUtil.FOXML_NS, "ID", datastream.getName());
		Element dsVersion;
		if (datastream.isVersionable()) {
			dsVersion = JDOMQueryUtil.getMostRecentDatastreamVersion(datastreamEl.getChildren("datastreamVersion",
					JDOMNamespaceUtil.FOXML_NS));
		} else {
			dsVersion = (Element) datastreamEl.getChild("datastreamVersion", JDOMNamespaceUtil.FOXML_NS);
		}
		return (Element)dsVersion.getChild("xmlContent", JDOMNamespaceUtil.FOXML_NS).getChildren().get(0);
	}

	public Element getObjectProperties() {
		if (objectProperties == null && foxml != null) {
			try {
				this.objectProperties = (Element) DocumentIndexingPackage.retrieveObjectPropertiesXPath
						.selectSingleNode(this.foxml);
			} catch (JDOMException e) {
				log.debug("Did not find object properties", e);
				return null;
			}
		}
		return this.objectProperties;
	}

	public String getLabel() {
		if (label == null && foxml != null) {
			Element objectProperties = this.getObjectProperties();
			try {
				Attribute value = (Attribute) retrieveLabelXPath.selectSingleNode(objectProperties);
				this.label = value.getValue();
			} catch (JDOMException e) {
				return null;
			}
		}
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ResourceType getResourceType() {
		/*
		 * if (resourceType == null && document != null && document.getResourceType() != null) {
		 * 
		 * }
		 */
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public void setSystemDates() {

	}

	/**
	 * Retrieves and stores path related attributes, including ancestorPath, ancestorNames, parentCollection, rollup
	 * 
	 * Depends on parentDocuments or triple store Side effect of triple store query, retrieves content models
	 */
	public void setPath() {

	}

	/**
	 * 
	 * Depends on parentDocuments and parent MD_CONTENTS
	 */
	public void setDisplayOrder() {

	}

	/**
	 * Sets datastreams and file sizes
	 * 
	 * Depends on FOXML
	 */
	public void setDatastreams() {

	}

	/**
	 * Sets the full tExt field
	 * 
	 * Same dependencies as setDatastreams and a FULL_TExt datastream
	 */
	public void setFullTExt() {

	}

	/**
	 * Sets contentModel, resourceType, resourceTypeSort
	 * 
	 * Depends on contentModel
	 */
	public void setObjectType() {

	}

	/**
	 * Sets contentType
	 * 
	 * Depends on datastreams, resourceType, defaultWebObject
	 */
	public void setContentType() {

	}

	/**
	 * Adds datastreams from this objects default web object
	 * 
	 * Depends on FOXML of defaultWebObject if it is not the current object
	 */
	public void setDefaultWebObject() {

	}

	/**
	 * Sets roleGroup, readGroup, adminGroup
	 * 
	 * Depends on accessControlUtils or foxml and parentDocuments or foxml and retrieve parents from solr
	 */
	public void setAccessControl() {

	}

	/**
	 * Sets the publication status, taking into account the publication status of its parents Published, Unpublished,
	 * UnpublishedParent
	 * 
	 * Depends on parentDocuments and RELS-Ext or triple query
	 */
	public void setPublicationStatus() {

	}

	/**
	 * Sets all descriptive metadata fields
	 * 
	 * Depends on foxml / mods / dc
	 */
	public void setDescriptiveMetadata() {

	}

	/**
	 * Populates the relations field with pertinent triples from RELS-Ext that are not intended for querying purposes.
	 * defaultWebData, defaultWebObject,
	 * 
	 * Depends on FOXML/RELS-Ext or triple query
	 */
	public void setRelations() {

	}

}