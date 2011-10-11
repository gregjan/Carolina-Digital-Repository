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
package edu.unc.lib.dl.search.solr.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * Base class for settings configuration objects.
 * @author bbpennel
 *
 */
public abstract class AbstractSettings {
	
	/**
	 * Creates a hashmap with the key and values flipped
	 * Note: This method is only safe for 1:1 maps.  If there are collisions, the 
	 * last one will win.
	 * @param map
	 * @return
	 */
	protected HashMap<String,String> getInvertedHashMap(Map<String,String> map){
		HashMap<String,String> inverted = new HashMap<String,String>();
		Iterator<Map.Entry<String,String>> pairIt = map.entrySet().iterator();
		while (pairIt.hasNext()){
			Map.Entry<String,String> pair = pairIt.next();
			inverted.put(pair.getValue(), pair.getKey());
		}
		return inverted;
	}
	
	protected String getKey(Map<String,String> map, String value){
		Iterator<Map.Entry<String,String>> pairIt = map.entrySet().iterator();
		while (pairIt.hasNext()){
			Map.Entry<String,String> pair = pairIt.next();
			if (pair.getValue().equals(value))
				return pair.getKey();
		}
		return null;
	}
	
	protected void populateMapFromProperty(String propertyPrefix, Map<String,String> map, Properties properties){
		Iterator<Map.Entry<Object,Object>> propIt = properties.entrySet().iterator();
		while (propIt.hasNext()){
			Map.Entry<Object,Object> propEntry = propIt.next();
			String propertyKey = (String)propEntry.getKey();
			
			//Field name mappings
			if (propertyKey.indexOf(propertyPrefix) == 0){
				map.put(propertyKey.substring(propertyKey.lastIndexOf(".")+1), (String)propEntry.getValue());
			}
		}
	}
	
	/**
	 * Populates a collection object with all entries in the string array generated by 
	 * splitting the value of the property provided, if it matches the property name
	 * provided.
	 * @param propertyName
	 * @param c
	 * @param propEntry
	 * @param delimiter
	 */
	protected void populateCollectionFromProperty(String propertyName, Collection<String> c, 
			Properties properties, String delimiter){
		String value = properties.getProperty(propertyName, null);
		if (value != null){
			String searchable[] = value.split(delimiter);
			for (String field: searchable){
				c.add(field);
			}
		}
	}
	
	protected Collection<String> getUnmodifiableCollectionFromProperty(String propertyName, Collection<String> c, Properties properties, String delimiter){
		populateCollectionFromProperty(propertyName, c, properties, delimiter);
		return Collections.unmodifiableCollection(c);
	}
	
	protected Set<String> getUnmodifiableSetFromProperty(String propertyName, Set<String> c, Properties properties, String delimiter){
		populateCollectionFromProperty(propertyName, c, properties, delimiter);
		return Collections.unmodifiableSet(c);
	}
	
	protected Map<String,String> getUnmodifiableMapFromProperty(String propertyPrefix, Map<String,String> map, Properties properties){
		populateMapFromProperty(propertyPrefix, map, properties);
		return Collections.unmodifiableMap(map);
	}
}