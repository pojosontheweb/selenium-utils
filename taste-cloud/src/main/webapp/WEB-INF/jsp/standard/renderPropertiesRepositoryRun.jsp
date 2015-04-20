<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp" %>
<%@taglib prefix="taste" tagdir="/WEB-INF/tags" %>
<c:set var="rr" value="${renderProperties.facetContext.targetObject}"/>
<div class="container-fluid">

    <div id="summary">
        <w:includeFacet facetName="summaryFragment" targetObject="${rr}"/>
    </div>

    <h2>Runs</h2>
    <div id="runs">
        <w:includeFacet facetName="runsFragment" targetObject="${rr}"/>
    </div>

</div>
<c:if test="${rr.finishedOn==null}">
    <script type="text/javascript">
        <w:url var="runsFrag" facetName="runsFragment" object="${rr}"/>
        <w:url var="summaryFragment" facetName="summaryFragment" object="${rr}"/>
        $(function () {
            var finished = false;
            function updateRuns() {
                $.get('${runsFrag}', function (frag) {
                    $('#runs').html(frag);
                    if (!finished) {
                        setTimeout(updateRuns, 2000);
                    }
                });
            }
            updateRuns();

            function updateSummary() {
                $.get('${summaryFragment}', function (frag) {
                    var rtInfo = $('#summary').html(frag).find('.rtinfo');
                    finished = rtInfo.data('finished');
                    if (finished) {
                        window.location.reload();
                    } else {
                        setTimeout(updateSummary, 2000);
                    }
                });
            }
            updateSummary();
        });
    </script>
</c:if>

