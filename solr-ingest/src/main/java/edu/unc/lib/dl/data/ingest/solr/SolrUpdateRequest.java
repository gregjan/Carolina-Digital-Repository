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
package edu.unc.lib.dl.data.ingest.solr;

/**
 * Represents a request to update an object identified by pid.
 * @author bbpennel
 */
public class SolrUpdateRequest {
	protected String pid;
	protected SolrUpdateAction action; 
	protected SolrUpdateRequest linkedRequest;
	
	public SolrUpdateRequest(){
		pid = null;
		action = null;
		linkedRequest = null;
	}
	
	public SolrUpdateRequest(String pid, SolrUpdateAction action){
		this.setPid(pid);
		this.action = action;
		linkedRequest = null;
	}
	
	public SolrUpdateRequest(String pid, SolrUpdateAction action, SolrUpdateRequest linkedRequest){
		this.setPid(pid);
		this.action = action;
		this.setLinkedRequest(linkedRequest);
	}
	
	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		if (pid.indexOf("info:fedora/") == 0)
			pid = pid.substring(pid.indexOf("/") + 1);
		this.pid = pid;
	}

	public SolrUpdateAction getAction() {
		return action;
	}

	public void setAction(SolrUpdateAction action) {
		this.action = action;
	}
	
	public boolean isBlocked(){
		return false;
	}
	
	public void linkedRequestEstablished(SolrUpdateRequest linkerRequest){
	}
	
	public void setLinkedRequest(SolrUpdateRequest linkedRequest){
		this.linkedRequest = linkedRequest;
		if (linkedRequest != null)
			linkedRequest.linkedRequestEstablished(this);
	}
	
	public SolrUpdateRequest getLinkedRequest(){
		return this.linkedRequest;
	}
	
	public void linkedRequestCompleted(SolrUpdateRequest completedRequest){
	}
	
	public void requestCompleted(){
		if (linkedRequest != null){
			linkedRequest.linkedRequestCompleted(this);
		}
	}
}