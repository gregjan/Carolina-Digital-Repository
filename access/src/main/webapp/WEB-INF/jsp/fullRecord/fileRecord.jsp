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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 
<%@ taglib prefix="cdr" uri="http://cdr.lib.unc.edu/cdrUI"%>
<script src="/static/js/fullRecord.js"></script>
<div class="onecol">
	<div class="contentarea">
		<c:set var="thumbUrl">
			<c:choose>
				<c:when test="${cdr:contains(briefObject.datastream, 'IMAGE_JP2000')}">
				</c:when>
				<c:when test="${cdr:contains(briefObject.datastream, 'DATA_FILE')}">
					<c:choose>
						<c:when test="${briefObject.contentType.searchKey == 'pdf'}">
							${cdr:getDatastreamUrl(briefObject.id, 'DATA_FILE', fedoraUtil)}
						</c:when>
						<c:when test="${briefObject.contentType.highestTierDisplayValue == 'mp4'}">
						</c:when>
						<c:when test="${briefObject.contentType.highestTierDisplayValue == 'mp3'}">
						</c:when>
						<c:otherwise>
							${cdr:getDatastreamUrl(briefObject.id, 'DATA_FILE', fedoraUtil)}
						</c:otherwise>
					</c:choose>
				</c:when>
			</c:choose>
		</c:set>
		
		<a href="${thumbUrl}" class="thumb_link">
			<c:choose>
				<c:when test="${cdr:contains(briefObject.datastream, 'THUMB_LARGE')}">
					<div class="largethumb_container">
						<img id="thumb_main" class="largethumb ph_large_${briefObject.contentType.searchKey}" 
								src="${cdr:getDatastreamUrl(briefObject.id, 'THUMB_LARGE', fedoraUtil)}"/>
					</div>
				</c:when>
				<c:otherwise>
					<div class="largethumb_container">
						<img id="thumb_main" class="largethumb ph_large_default" src="/static/images/placeholder/large/${briefObject.contentType.searchKey}.png"/>
					</div>
				</c:otherwise>
			</c:choose>
		</a>
		<div class="collinfo">
			<h2><c:out value="${briefObject.title}" /></h2>
			<c:if test="${not empty briefObject.creator}">
				<p class="smaller"><span class="bold">Creator<c:if test="${fn:length(briefObject.creator) > 1}">s</c:if>:</span> 
					<c:forEach var="creatorObject" items="${briefObject.creator}" varStatus="creatorStatus">
						<c:out value="${creatorObject}"/><c:if test="${!creatorStatus.last}">, </c:if>
					</c:forEach>
				</p>
			</c:if>
			<p class="smaller">
				<span class="bold">File Type:</span> <c:out value="${briefObject.contentType.highestTierDisplayValue}" />
				<c:if test="${briefObject.filesize != -1}">  | <span class="bold">${searchSettings.searchFieldLabels[searchFieldKeys.FILESIZE]}:</span> <c:out value="${cdr:formatFilesize(briefObject.filesize, 1)}"/></c:if>
				<c:if test="${not empty briefObject.dateAdded}">  | <span>${searchSettings.searchFieldLabels[searchFieldKeys.DATE_ADDED]}:</span> <fmt:formatDate pattern="yyyy-MM-dd" value="${briefObject.dateAdded}" /></c:if>
				<c:if test="${not empty briefObject.dateCreated}">  | <span>${searchSettings.searchFieldLabels[searchFieldKeys.DATE_CREATED]}:</span> <fmt:formatDate pattern="yyyy-MM-dd" value="${briefObject.dateCreated}" /></c:if>
			</p>
			<c:choose>
				<c:when test="${cdr:contains(briefObject.datastream, 'DATA_FILE')}">
					<div class="actionlink left download">
						<c:set var="indexableContent" scope="page">
							<c:choose>
								<c:when test="${briefObject.contentType.highestTierDisplayValue == 'doc'
									|| briefObject.contentType.highestTierDisplayValue == 'docx'
									|| briefObject.contentType.highestTierDisplayValue == 'txt'
									|| briefObject.contentType.highestTierDisplayValue == 'rtf'
									|| briefObject.contentType.highestTierDisplayValue == 'pdf'
									|| briefObject.contentType.highestTierDisplayValue == 'htm'
									|| briefObject.contentType.highestTierDisplayValue == 'html'
									|| briefObject.contentType.highestTierDisplayValue == 'xml'
									|| briefObject.contentType.highestTierDisplayValue == 'xls'
									|| briefObject.contentType.highestTierDisplayValue == 'ppt'
									|| briefObject.contentType.highestTierDisplayValue == 'xlsx'
									|| briefObject.contentType.highestTierDisplayValue == 'pptx'}">
									indexable
								</c:when>
							</c:choose>
						</c:set>
						<a href="${indexableContent}${cdr:getDatastreamUrl(briefObject.id, 'DATA_FILE', fedoraUtil)}&dl=true">Download</a>
					</div>
				</c:when>
			</c:choose>
			
			<c:choose>
				<c:when test="${cdr:contains(briefObject.datastream, 'IMAGE_JP2000')}">
					<div class="actionlink left">
						<a href="" class="inline_viewer_link jp2_viewer_link">View</a>
					</div>
					<div class="clear_space"></div>
					<script src="/static/plugins/OpenLayers/OpenLayers.js"></script>
					
					<script type="text/javascript">
						$(function() {
							$(".inline_viewer_link").bind("click", {id: '${briefObject.id}', viewerId:'jp2_imageviewer_window',
								viewerContext: "${pageContext.request.contextPath}"}, displayJP2Viewer);
							if (window.location.hash.replace("#", "") == "showJP2"){
								$(".inline_viewer_link").trigger("click");
							}
						});
					  </script>
					  <div id="jp2_imageviewer_window" class="djatokalayers_window not_loaded">&nbsp;</div>
					
				</c:when>
				<c:when test="${cdr:contains(briefObject.datastream, 'DATA_FILE')}">
					<c:choose>
						<c:when test="${briefObject.contentType.searchKey == 'pdf'}">
							<div class="actionlink left">
								<a href="${cdr:getDatastreamUrl(briefObject.id, 'DATA_FILE', fedoraUtil)}">View</a>
							</div>
						</c:when>
						<c:when test="${briefObject.contentType.highestTierDisplayValue == 'mp3'}">
							<script src="/static/plugins/flowplayer/flowplayer-3.2.6.min.js"></script>
							<div class="actionlink left">
								<a href="" class="inline_viewer_link audio_player_link">Listen</a>
							</div>
							<div class="clear_space"></div>
							<div id="audio_player"></div>
							<c:set var="dataFileUrl">${cdr:getDatastreamUrl(briefObject.id, 'DATA_FILE', fedoraUtil)}&ext=.${briefObject.contentType.searchKey}</c:set>
							<script language="JavaScript">
								$(function() {
									$(".inline_viewer_link").bind("click", {viewerId:'audio_player',
										url: "${dataFileUrl}"}, displayAudioPlayer);
									if (window.location.hash.replace("#", "") == "showAudio"){
										$(".inline_viewer_link").trigger("click");
									}
								});
							</script>
						</c:when>
						<c:when test="${briefObject.contentType.highestTierDisplayValue == 'mp4'}">
							<div class="actionlink left">
								<a href="" class="inline_viewer_link video_viewer_link">View</a>
							</div>
							<div class="clear_space"></div>
							<script src="/static/plugins/flowplayer/flowplayer-3.2.6.min.js"></script>
							<div id="video_player"></div>
							<c:set var="dataFileUrl">${cdr:getDatastreamUrl(briefObject.id, 'DATA_FILE', fedoraUtil)}&ext=.${briefObject.contentType.searchKey}</c:set>
							<script language="JavaScript">
								$(function() {
									$(".inline_viewer_link").bind("click", {viewerId:'video_player',
										url: "${dataFileUrl}"}, displayVideoViewer);
									if (window.location.hash.replace("#", "") == "showVideo"){
										$(".inline_viewer_link").trigger("click");
									}
								});
							</script>
							<div class="clear"></div>
						</c:when>
					</c:choose>
				</c:when>
			</c:choose>
		</div>
	</div>
</div>
<div class="onecol shadowtop">
	<div class="contentarea">
		<c:if test="${briefObject.abstract != null}">
			<div class="description">
				<p>
					<c:out value="${briefObject.abstract}" />
				</p>
			</div>
		</c:if>
		<c:import url="WEB-INF/jsp/fullRecord/metadataBody.jsp" />
		<c:import url="WEB-INF/jsp/fullRecord/exports.jsp" />
	</div>
</div>
<c:import url="WEB-INF/jsp/fullRecord/neighborList.jsp" />