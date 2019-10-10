<%@page taglibs="c,cms"%>
<cms:secureparams />
<div>
    <!-- Example function JSP for user data requests -->

    <jsp:useBean id="bean" class="org.opencms.jsp.userdata.CmsJspUserDataRequestBean" scope="page" />
    <c:set var="status" value="${bean.action(cms.vfs.cmsObject, pageContext.request.parameterMap)}" />

    <div class="oc-userdatarequest">
        <style type="text/css">
.oc-userdatarequest button {
    margin-top: 5px;
    margin-bottom: 5px;
}

.oc-userdatarequest form {
    margin-top: 5px;
    margin-bottom: 5px;
}

.oc-udr-rows {
    display: table;
}

.oc-udr-row {
    display: table-row;
}

.oc-udr-row>* {
    display: table-cell;
}

.oc-udr-row label {
    padding-right: 10px;
}
</style>
        <c:choose>
            <c:when test="${status == 'error'}">${bean.errorHtml}</c:when>
            <c:when test="${status == 'form'}">
                <c:if test="${bean.error}">
                    <div class="oc-udr-error">${bean.texts.Failure}</div>
                </c:if>

                <div class="oc-udr-before-form">${bean.texts.BeforeForm}</div>
                <form method="post">
                    <div class="oc-udr-rows">
                        <input type="hidden" name="action" value="request">
                        <div class="oc-udr-row">
                            <label for="udr_user"><c:out value="${bean.texts.LabelUser}" /></label> <input type="text" id="udr_user" name="user">
                        </div>
                        <div class="oc-udr-row">
                            <label for="udr_password"><c:out value="${bean.texts.LabelPassword}" /></label> <input type="password" id="udr_password" name="password">
                        </div>
                        <div class="oc-udr-row">
                            <label for="udr_email"><c:out value="${bean.texts.LabelEmail}" /></label> <input type="text" id="udr_email" name="email">
                        </div>
                    </div>
                    <button type="submit">
                        <c:out value="${bean.texts.LabelSubmit}" />
                    </button>
                </form>
                <div class="oc-udr-after-form">${bean.texts.AfterForm}</div>
            </c:when>
            <c:when test="${status == 'formOk'}">${bean.texts.Success}</c:when>
            <c:when test="${status == 'view'}">
                <c:if test="${bean.error}">
                    <div class="oc-udr-error">${bean.texts.ViewFailure}</div>
                </c:if>

                <div class="oc-udr-view-text">${bean.texts.ViewText}</div>
                <form method="post" action="?">
                    <input type="hidden" name="action" value="viewauth"> 
                    <input type="hidden" name="udrid" value="${param.udrid}" />
                    <div class="oc-udr-rows">
                        <c:choose>
                            <c:when test="${bean.onlyEmailRequired}">
                                <div class="oc-udr-row">
                                    <label for="udr_email"><c:out value="${bean.texts.LabelEmail}" /></label> <input type="text" id="udr_email" name="email">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="oc-udr-row">
                                    <label for="udr_user"><c:out value="${bean.texts.LabelUser}" /></label> <input type="text" id="udr_user" name="user">
                                </div>
                                <div class="oc-udr-row">
                                    <label for="udr_password"><c:out value="${bean.texts.LabelPassword}" /></label> <input type="password" id="udr_password" name="password">
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <button type="submit">
                        <c:out value="${bean.texts.LabelSubmit}" />
                    </button>
                </form>
            </c:when>
            <c:when test="${status == 'viewOk'}">
                <div class="oc-udr-before-data">${bean.texts.BeforeData}</div>
                <div class="oc-udr-userdata">${bean.infoHtml}</div>
                <c:set var="downloadLink" value="${bean.downloadLink}" />
                <a href="${downloadLink}"><c:out value="${bean.texts.DownloadLinkText}" /></a>
            </c:when>
        </c:choose>
    </div>

</div>