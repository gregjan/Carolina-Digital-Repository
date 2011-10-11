<%--

    Copyright 2008 The University of North Carolina at Chapel Hill

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cdr" uri="http://cdr.lib.unc.edu/cdrUI"%>
<script type="text/javascript" src="/static/js/jquery-ui-datepicker.min.js"></script>
<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<script type="text/javascript" src="/static/js/advancedSearch.js"></script>

<div class="contentarea">
	<h2>Advanced Search</h2>
</div>
<form>
	<div class="lightest">
		<div class="twocol lightest shadowtop">
			<div class="contentarea">
				<h3>Search for</h3>
				<p class="clear bold">
					<label>Keyword</label> <input name="${searchSettings.searchFieldParams[searchFieldKeys.DEFAULT_INDEX]}" class="advsearch_text" type="text" />
				</p>
				<p class="clear bold">
					<label>Title</label> <input name="${searchSettings.searchFieldParams[searchFieldKeys.TITLE_INDEX]}" class="advsearch_text" type="text" />
				</p>
				<p class="clear bold">
					<label>Contributor</label> <input name="${searchSettings.searchFieldParams[searchFieldKeys.CONTRIBUTOR_INDEX]}" class="advsearch_text" type="text" />
				</p>
				<p class="clear bold">
					<label>Subject</label> <input name="${searchSettings.searchFieldParams[searchFieldKeys.SUBJECT_INDEX]}" class="advsearch_text" type="text" />
				</p>
			</div>
		</div>
		<div class="twocol light shadowtop">
			<div class="contentarea">
				<h3>Limit By</h3>
				<select name="${searchSettings.searchFieldParams[searchFieldKeys.ANCESTOR_PATH]}" class="advsearch_select">
					<option value="">Collection</option>
					<c:forEach items="${collectionList}" var="collectionRecord">
						<option value="<c:out value='${collectionRecord.path.searchValue}' />"><c:out value="${collectionRecord.title}" /></option>
					</c:forEach>
				</select>
				<select name="${searchSettings.searchFieldParams[searchFieldKeys.CONTENT_TYPE]}" class="advsearch_select">
					<option value="">Format</option>
					<c:forEach items="${formatMap}" var="formatEntry">
						<option value="${formatEntry.key}"><c:out value="${formatEntry.value}"/></option>
					</c:forEach>
				</select> 
				<select name="${searchSettings.searchStateParams['ACCESS_FILTER_TYPE']}" class="advsearch_select">
					<option value="">Access</option>
					<c:forEach items="${accessMap}" var="accessEntry">
						<option value="${accessEntry.key}"><c:out value="${accessEntry.value}"/></option>
					</c:forEach>
				</select>
				
				<p class="clear"><br /></p>
				<p class="clear bold">
					<label>Date Added</label> from <input name="${searchSettings.searchFieldParams[searchFieldKeys.DATE_ADDED]}Start" class="advsearch_date" type="text" />
						 to <input name="${searchSettings.searchFieldParams[searchFieldKeys.DATE_ADDED]}End" class="advsearch_date" type="text" />
						<a class="date_field_tooltip" title="Enter dates in YYYY-MM-DD format.  Month and day may be omitted.  Leave the right hand date empty to search for items with dates starting at the left hand date, and vice versa.">?</a>
				</p>
				<p class="clear bold">
					<label>Date Created</label> from <input name="${searchSettings.searchFieldParams[searchFieldKeys.DATE_CREATED]}Start" class="advsearch_date" type="text" />
						 to <input name="${searchSettings.searchFieldParams[searchFieldKeys.DATE_CREATED]}End" class="advsearch_date" type="text" />
					<a class="date_field_tooltip" title="Enter dates in YYYY-MM-DD format.  Month and day may be omitted.  Leave the right hand date empty to search for items with dates starting at the left hand date, and vice versa.">?</a>
				</p>
			</div>
		</div>
	
		<div class="onecol white">
			<div class="contentarea">
				<input type="submit" class="right" id="advsearch_submit" value="Search"></input>
			</div>
		</div>
	
	</div>
</form>