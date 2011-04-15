<%@page session="false" taglibs="c,cms,fn"%>
<cms:formatter var="content">
	<div class="box box_schema<cms:elementsetting name="color" default="3"/>">
		<h4>${content.value.Title}</h4>
		<div class="boxbody">
			${content.value.Content}

			<c:if test="${content.hasValue.JspFile}">
				<c:set var="path" value="${content.value.JspFile.stringValue}" />
				<c:choose>
					<c:when test="${!fn:contains(path, '?')}">
						<cms:include file="${path}">
							<cms:param name="box.uri" value="${content.filename}" />
						</cms:include>
					</c:when>
					<c:otherwise>
						<c:set var="uriParams" value="${fn:split(path, '?')[1]}" />
						<cms:include file="${fn:split(path, '?')[0]}">
							<cms:param name="box.uri" value="${content.filename}" />
							<c:forTokens items="${uriParams}" delims="&" var="uriParam">
								<cms:param name="${fn:split(uriParam, '=')[0]}" value="${fn:split(uriParam, '=')[1]}" />
							</c:forTokens>
						</cms:include>
					</c:otherwise>
				</c:choose>
			</c:if>	
		</div>
	</div>
</cms:formatter>