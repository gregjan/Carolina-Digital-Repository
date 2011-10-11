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
package edu.unc.lib.dl.ui.validator;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import edu.unc.lib.dl.security.access.AccessGroupSet;
import edu.unc.lib.dl.search.solr.model.BriefObjectMetadataBean;
import edu.unc.lib.dl.search.solr.model.SearchResultResponse;
import edu.unc.lib.dl.ui.util.AccessControlSettings;

/**
 * Validates and filters datastream lists to ensure that they only contain entries which the user is allowed to access.
 * @author bbpennel
 *
 */
public class DatastreamAccessValidator {
	private static AccessControlSettings accessSettings;
	
	public DatastreamAccessValidator(){
	}
	
	public static void filterSearchResult(SearchResultResponse response, AccessGroupSet userAccess){
		for (BriefObjectMetadataBean metadata: response.getResultList()){
			filterBriefObject(metadata, userAccess);
		}
	}
	
	public static void filterBriefObject(BriefObjectMetadataBean metadata, AccessGroupSet userAccess){
		filterDatastreams(metadata.getDatastream(), metadata.getSurrogateAccess(), metadata.getFileAccess(), userAccess);
	}
	
	public static void filterDatastreams(Collection<String> datastreams, AccessGroupSet surrogateAccess, 
			AccessGroupSet fileAccess, AccessGroupSet userAccess){
		
		if (userAccess.contains(accessSettings.getAdminGroup()))
			return;
		
		//Remove surrogate datastreams if no matching groups
		if (!surrogateAccess.containsAny(userAccess)){
			for (String accessDS: accessSettings.getSurrogateDatastreams()){
				datastreams.remove(accessDS);
			}
		}
		
		//Remove file datastreams if no matching groups
		if (!fileAccess.containsAny(userAccess)){
			for (String accessDS: accessSettings.getFileDatastreams()){
				datastreams.remove(accessDS);
			}
		}
		
		//Remove admin datastreams if not an admin
		if (!userAccess.contains(accessSettings.getAdminGroup())){
			for (String accessDS: accessSettings.getAdminDatastreams()){
				datastreams.remove(accessDS);
			}
		}
	}

	public AccessControlSettings getAccessSettings() {
		return accessSettings;
	}

	public void setAccessSettings(AccessControlSettings accessSettings) {
		DatastreamAccessValidator.accessSettings = accessSettings;
	}
}