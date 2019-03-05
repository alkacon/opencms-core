<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<cms:formatter var="content" val="value">

  <c:set var="title" value="${value.Title}"/>
  <c:set var="text" value="${value.Text}"/>
  <c:set var="image" value="${value.Image}"/>
  <c:set var="linkuri" value="${value.LinkURI}"/>
  <c:set var="imageBean" value="${image.toImage}" />

  <div>
    <div style="width:200px;float:right">
      <img style="width:100%" src="${imageBean.vfsUri}"/>
    </div>
    <div>
      <h2>${title}</h2>
      <p>${text}</p>
    </div>
    <div>
      <a href="${linkuri}">Link</a>
    </div>
    <div style="clear:right;"></div>
  </div>
</cms:formatter>
