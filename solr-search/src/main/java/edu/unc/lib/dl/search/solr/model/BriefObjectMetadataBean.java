/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.search.solr.model;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.apache.solr.client.solrj.beans.Field;
import edu.unc.lib.dl.security.access.AccessGroupSet;
import edu.unc.lib.dl.search.solr.model.HierarchicalFacet;
import edu.unc.lib.dl.search.solr.util.SearchFieldKeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores a single Solr tuple representing an object from a search result.  Can be populated 
 * directly by Solrj's queryResponse.getBeans.
 * @author bbpennel
 */
public class BriefObjectMetadataBean {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(BriefObjectMetadataBean.class);
	private String id;
	private HierarchicalFacet ancestorPath;
	private HierarchicalFacet path;
	private String ancestorNames;
	private AccessGroupSet fileAccess;
	private AccessGroupSet recordAccess;
	private AccessGroupSet surrogateAccess;
	private String resourceType;
	private Long displayOrder;
	private HierarchicalFacet contentType;
	private Set<String> datastream;
	private String parentCollection;
	private String title;
	private String abstractText;
	private List<String> keyword;
	private List<String> subject;
	private String language;
	private List<String> creator;
	private List<String> name;
	private List<String> department;
	private String creatorType;
	private Date dateCreated;
	private Date dateAdded;
	private Date dateUpdated;
	private Date timestamp;
	private String filesize;
	private long childCount;
	
	public BriefObjectMetadataBean(){
	}
	
	public String getIdWithoutPrefix(){
		int index = id.indexOf(":");
		if (index != -1){
			return id.substring(index + 1);
		}
		return id;
	}
	public String getId() {
		return id;
	}	
	@Field
	public void setId(String id) {
		this.id = id;
	}
	
	public HierarchicalFacet getAncestorPath() {
		return ancestorPath;
	}
	@Field
	public void setAncestorPath(ArrayList<String> ancestorPaths) {
		ArrayList<HierarchicalFacet.HierarchicalFacetTier> tierList = HierarchicalFacet.createFacetTierList(ancestorPaths);
		
		if (tierList.size() > 0)
			this.ancestorPath = new HierarchicalFacet(SearchFieldKeys.ANCESTOR_PATH, tierList, 0);
	}
	
	/**
	 * Returns a HierarchicalFacet of the full path for this object, including the ancestor path and itself.
	 * @return
	 */
	public HierarchicalFacet getPath(){
		if (path == null){
			if (this.ancestorPath == null){
				path = new HierarchicalFacet(SearchFieldKeys.ANCESTOR_PATH, "1," + this.id + "," + this.title);
			} else {
				path = new HierarchicalFacet(ancestorPath);
				path.addTier(id, title);
			}
		}
		return path;
	}
	
	public String getAncestorNames() {
		return ancestorNames;
	}
	@Field
	public void setAncestorNames(String ancestorNames) {
		this.ancestorNames = ancestorNames;
	}
	
	public AccessGroupSet getFileAccess() {
		return fileAccess;
	}
	@Field
	public void setFileAccess(String[] fileAccess) {
		this.fileAccess = new AccessGroupSet(fileAccess);
	}
	
	public AccessGroupSet getRecordAccess() {
		return recordAccess;
	}
	@Field
	public void setRecordAccess(String[] recordAccess) {
		this.recordAccess = new AccessGroupSet(recordAccess);
	}
	
	public AccessGroupSet getSurrogateAccess() {
		return surrogateAccess;
	}
	@Field
	public void setSurrogateAccess(String[] surrogateAccess) {
		this.surrogateAccess = new AccessGroupSet(surrogateAccess);
	}
	
	public String getResourceType() {
		return resourceType;
	}
	@Field
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public Long getDisplayOrder() {
		return displayOrder;
	}
	@Field
	public void setDisplayOrder(long displayOrder) {
		this.displayOrder = displayOrder;
	}
	
	public HierarchicalFacet getContentType() {
		return contentType;
	}
	@Field
	public void setContentType(ArrayList<String> contentTypes) {
		ArrayList<HierarchicalFacet.HierarchicalFacetTier> tierList = HierarchicalFacet.createFacetTierList(contentTypes);
		
		if (tierList.size() > 0)
			this.contentType = new HierarchicalFacet(SearchFieldKeys.CONTENT_TYPE, tierList, 0);
	}
	
	public Set<String> getDatastream() {
		return datastream;
	}
	@Field
	public void setDatastream(String[] datastream) {
		HashSet<String> datastreams = new HashSet<String>();
		for (String value: datastream){
			datastreams.add(value);
		}
		this.datastream = datastreams;
	}
	
	public String getTitle() {
		return title;
	}
	@Field
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAbstract() {
		return abstractText;
	}
	@Field
	public void setAbstract(String abstractText) {
		this.abstractText = abstractText;
	}
	
	public List<String> getKeyword() {
		return keyword;
	}
	@Field
	public void setKeyword(List<String> keyword) {
		this.keyword = keyword;
	}
	
	public List<String> getSubject() {
		return subject;
	}
	@Field
	public void setSubject(List<String> subject) {
		this.subject = subject;
	}
	
	public String getLanguage() {
		return language;
	}
	@Field
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public List<String> getCreator() {
		return creator;
	}
	@Field
	public void setCreator(List<String> creator) {
		this.creator = creator;
	}
	
	public List<String> getName() {
		return name;
	}
	@Field
	public void setName(List<String> name) {
		this.name = name;
	}
	
	public List<String> getDepartment() {
		return department;
	}
	@Field
	public void setDepartment(List<String> department) {
		this.department = department;
	}
	
	public String getCreatorType() {
		return creatorType;
	}
	@Field
	public void setCreatorType(String creatorType) {
		this.creatorType = creatorType;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	@Field
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Date getDateAdded() {
		return dateAdded;
	}
	@Field
	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}
	
	public Date getDateUpdated() {
		return dateUpdated;
	}
	@Field
	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	@Field
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getFilesize() {
		return filesize;
	}
	@Field
	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("id: " + id + "\n");
		sb.append("ancestorPath: " + ancestorPath + "\n");
		sb.append("ancestorNames: " + ancestorNames + "\n");
		sb.append("fileAccess: " + fileAccess + "\n");
		sb.append("recordAccess: " + recordAccess + "\n");
		sb.append("resourceType: " + resourceType + "\n");
		sb.append("displayOrder: " + displayOrder + "\n");
		sb.append("contentType: " + contentType + "\n");
		sb.append("datastream: " + datastream + "\n");
		sb.append("title: " + title + "\n");
		sb.append("abstractText: " + abstractText + "\n");
		sb.append("keyword: " + keyword + "\n");
		sb.append("subject: " + subject + "\n");
		sb.append("language: " + language + "\n");
		sb.append("creator: " + creator + "\n");
		sb.append("name: " + name + "\n");
		sb.append("department: " + department + "\n");
		sb.append("creatorType: " + creatorType + "\n");
		sb.append("dateCreated: " + dateCreated + "\n");
		sb.append("dateAdded: " + dateAdded + "\n");
		sb.append("dateUpdated: " + dateUpdated + "\n");
		sb.append("timestamp: " + timestamp + "\n");
		sb.append("filesize: " + filesize + "\n");
		return sb.toString();
	}

	public String getParentCollection() {
		return parentCollection;
	}
	@Field
	public void setParentCollection(String parentCollection) {
		this.parentCollection = parentCollection;
	}
	
	public String getParentCollectionName(){
		if (ancestorPath == null || parentCollection == null)
			return null;
		return this.ancestorPath.getDisplayValue(parentCollection);
	}
	
	public String getParentCollectionSearchValue(){
		if (ancestorPath == null || parentCollection == null)
			return null;
		return this.ancestorPath.getSearchValue(parentCollection);
	}

	public long getChildCount() {
		return childCount;
	}

	public void setChildCount(long childCount) {
		this.childCount = childCount;
	}
}