<%@page session="true" import="java.util.*, org.opencms.i18n.*, org.opencms.jsp.*, org.opencms.widgets.*" %>
<%@ page taglibs="c,cms,fmt,fn" %>
<cms:formatter var="content" val="value">
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com.alkacon.opencms.v8.subscription.frontend">
<c:if test="${not empty param.action}">
	<c:choose>
		<c:when test="${param.action == 'subscribe'}">
			<cms:usertracking action="subscribe" file="${param.file}" />
		</c:when>
		<c:when test="${param.action == 'unsubscribe'}">
			<cms:usertracking action="unsubscribe" file="${param.file}" />
		</c:when>
	</c:choose>
</c:if>
<c:set var="boxschema"><cms:elementsetting name="boxschema" default="box_schema1" /></c:set>
<c:set var="collector"><c:choose><c:when test="${content.value.Collector.exists}">${content.value.Collector}</c:when><c:otherwise>allSubscribed</c:otherwise></c:choose></c:set>
<c:set var="collectorparams"><c:choose><c:when test="${content.value.Params.exists}">${content.value.Params}</c:when><c:otherwise>resource=/|currentuser=true|includesubfolders=true|mode=all</c:otherwise></c:choose></c:set>
<div class="box ${boxschema}">
	<h4><c:choose>
	<c:when test="${content.value.Title.isSet}">
	${content.value.Title}
	</c:when>
	<c:otherwise>
	<fmt:message key="subscriptioneditor.title" /> 
	</c:otherwise>
	</c:choose></h4>
	<div class="boxbody">
		<c:set var="locale"><cms:info property="opencms.request.locale" /></c:set>
		<c:set var="currenturi"><cms:info property="opencms.request.uri"/></c:set>
		<c:set var="linktarget"><c:choose><c:when test="${cms.detailContentId != null}"><cms:link>${cms.detailContentSitePath}</cms:link></c:when><c:otherwise><cms:link>${cms.requestContext.uri}</cms:link></c:otherwise></c:choose></c:set>
		<div id="documentslist">
			<table cellpadding="3" cellspacing="3" style="margin-top: 5px"><thead>
			<tr>
				<th width="600px"><fmt:message key="titlecolumn" /></th>
				<th width="100px"><fmt:message key="actioncolumn" /></th>
			</tr></thead>
			<c:set var="docspresent" value="true" />
			<%-- load the resources --%>
			
			<cms:resourceload collector="${collector}" param="${collectorparams}">
			<cms:contentinfo var="info" />
			<c:choose>
				<c:when test="${info.resultSize == 0}">
					<c:set var="docspresent" value="false" />
				</c:when>
			</c:choose>
			<c:choose>
				<c:when test="${docspresent == true}">
					<cms:resourceaccess var="item"/>
					<c:set var="linktext"><c:choose><c:when test="${!empty item.property['Title']}">${item.property['Title']}</c:when><c:otherwise>${item.filename}</c:otherwise></c:choose></c:set>
						<tr>
							<td><a href="<cms:link>${item.filename}</cms:link>">${linktext}</a></td>
							<td>
								<%-- determine the user subscription --%>
								<c:set var="inputattributes" value="" />
								<c:set var="subscribedtouser"><cms:usertracking action="checksubscribed" file="${item.filename}" currentuser="true" /></c:set>
								<c:choose>
								    <c:when test="${subscribedtouser == true}">
								    	<a href="${linktarget}?action=unsubscribe&file=${item.filename}"><img src="<cms:link>/system/modules/com.alkacon.opencms.v8.subscription/resources/unsubscribe.png</cms:link>" title="<fmt:message key="button.unsubscribe"/>"></a>
								    </c:when>					    
								    <c:otherwise>
		    					    	<a href="${linktarget}?action=subscribe&file=${item.filename}"><img src="<cms:link>/system/modules/com.alkacon.opencms.v8.subscription/resources/subscribe.png</cms:link>" title="<fmt:message key="button.subscribe"/>"></a>
								    </c:otherwise>
								</c:choose>
							</td>
						</tr>
				</c:when>
				<c:otherwise>
				    <fmt:message key="nodocsfound" />
				</c:otherwise>
			</c:choose>
			</cms:resourceload>
			</table>
		</div><!-- documentslist -->
	</div><!-- boxbody -->
</div><!-- box -->
</fmt:bundle>
</cms:formatter>
