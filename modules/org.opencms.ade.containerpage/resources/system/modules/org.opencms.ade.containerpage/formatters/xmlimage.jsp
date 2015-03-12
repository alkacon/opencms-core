<%@page buffer="none" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setLocale value="${cms.locale}" />
<cms:bundle basename="org.opencms.xml.containerpage.messages">
<cms:formatter var="content" val="value" rdfa="rdfa">
	<c:choose>
		<c:when test="${cms.element.inMemoryOnly}">
			<div class="alert"><fmt:message key="xmlimage.message.new" /></div>
		</c:when>
		<c:otherwise>
			<c:set var="cssShadow"></c:set>
			<c:if test="${cms.element.setting.shadow.value == 'true'}">
				<c:set var="cssShadow">box-shadow shadow-effect-1</c:set>
			</c:if>
            <c:set var="cssBorder"></c:set>
			<c:if test="${cms.element.setting.border.value == 'true'}">
				<c:set var="cssBorder">img-bordered</c:set>
			</c:if>
            <c:set var="cssShape"></c:set>
			<c:if test="${cms.element.setting.shape.isSet and cms.element.setting.shape.value != 'none'}">
                <c:set var="cssShape">${cms.element.setting.shape.value}</c:set>
			</c:if>
            <c:set var="copyright"></c:set>
 			<c:if test="${value.Copyright.isSet}">
                <c:set var="copyright">&#13;&copy; ${value.Copyright}</c:set>
			</c:if>

            <div class="margin-bottom-20">

            <c:if test="${cms.element.setting.showheadline.value == 'top'}">
                <div class="headline"><h2 ${rdfa.Headline}>${value.Headline}</h2></div>
            </c:if>

			<div class="${cssShadow}">

            <c:if test="${value.Link.isSet and cms.element.setting.showlink.value == 'image'}">
              <a href="<cms:link>${value.Link}</cms:link>">
            </c:if>

			<span ${rdfa.Image}>
                <img 
                    src="<cms:link>${value.Image}</cms:link>" 
                    class="${cssShape} img-responsive ${cssBorder}" 
                    alt="${value.Headline}" 
                    title="<c:out value='${value.Headline} ${copyright}' escapeXml='false' />" />
            </span>

            <c:if test="${value.Link.isSet and cms.element.setting.showlink.value == 'image'}">
              </a>
            </c:if>
            
			</div>
       
            <c:choose>
                <c:when test="${cms.element.setting.showheadline.value == 'bottom'}">
                    <h2 ${rdfa.Headline}>${value.Headline}</h2>
                </c:when>
                <c:when test="${cms.element.setting.showheadline.value == 'bottomcenter'}">
                    <div class="center"><div class="margin-bottom-20"></div><p><strong ${rdfa.Headline}>${value.Headline}</strong></p></div>
                </c:when>
            </c:choose>
            <c:if test="${value.Text.isSet and cms.element.setting.showtext.value == 'true'}">
                <p class="margin-top-10" ${rdfa.Text}>${value.Text}</p>
            </c:if>        

            <c:if test="${value.Link.isSet and  cms.element.setting.showlink.value == 'button'}">
                <p><a class="btn-u btn-u-small" href="<cms:link>${value.Link}</cms:link>"><fmt:message key="xmlimage.frontend.readmore" /></a></p>
            </c:if>

            </div>               
		</c:otherwise>
	</c:choose>

</cms:formatter>
</cms:bundle>