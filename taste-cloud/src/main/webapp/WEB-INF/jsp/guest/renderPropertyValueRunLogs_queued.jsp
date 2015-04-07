<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="run" value="${renderPropertyValue.owningObject}"/>
<w:objectKey var="runId" object="${run}"/>
<div class="run-queued">
    <h2>Run in queue</h2>
    <p>
        The run is in the queue. It will start automatically...
    </p>
</div>
<script type="text/javascript">
    $(function() {
        var poll = function() {
            setTimeout(function() {
                wokoClient.loadObject('Run', '${runId}', {
                    onSuccess: function(run) {
                        if (run.summary && run.summary.startedOn) {
                            window.location.reload();
                        } else {
                            poll();
                        }
                    }
                })
            }, 1000);
        };
        poll();
    });
</script>
