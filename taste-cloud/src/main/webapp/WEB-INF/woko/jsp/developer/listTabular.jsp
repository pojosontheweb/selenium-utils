<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<%@ page import="woko.persistence.ResultIterator" %>
<%@ page import="woko.Woko" %>
<%@ page import="woko.util.Util" %>
<%@ page import="woko.facets.builtin.*" %>
<%@ page import="woko.facets.builtin.all.Link" %>
<%@ page import="woko.util.LinkUtil" %>
<%@ page import="java.util.*" %>

<w:facet facetName="<%=Layout.FACET_NAME%>"/>

<fmt:message bundle="${wokoBundle}" var="pageTitle" key="woko.devel.list.pageTitle"/>
<s:layout-render name="${layout.layoutPath}" layout="${layout}" pageTitle="${pageTitle}">
    <s:layout-component name="body">
        <%
            Woko<?,?,?,?> woko = Woko.getWoko(application);
            ListObjects list = (ListObjects)request.getAttribute(WokoFacets.list);
            String className = list.getClassName();
            ResultIterator results = list.getResults();
            int totalSize = results.getTotalSize();
            int p = list.getPage();
            int resultsPerPage = list.getResultsPerPage();
            int nbPages = totalSize / resultsPerPage;
            if (totalSize % resultsPerPage != 0) {
              nbPages++;
            }
            String listWrapperClass = list.getListWrapperCssClass();
            if (listWrapperClass==null) {
                listWrapperClass = "table " + className;
            }
            String overridenH1 = list.getPageHeaderTitle();
        %>

        <div class="container-fluid w-list-results <%=className%>">

            <c:choose>
                <c:when test="<%=overridenH1==null%>">
                    <w:includeFacet facetName="<%=WokoFacets.renderListTitle%>" targetObjectClass="<%=woko.getObjectStore().getMappedClass(className)%>"/>
                </c:when>
                <c:otherwise>
                    <h1 class="page-header"><%=overridenH1%></h1>
                </c:otherwise>
            </c:choose>

            <c:if test="<%=nbPages>1%>">
                <div class="row w-list-form hidden-xs">
                    <div class="col-lg-4 col-sm-4">
                        <s:form action="/list" class="form-inline">
                            <s:hidden name="className"/>
                            <input type="hidden"name="facet.page" value="1"/>
                            <s:select name="facet.resultsPerPage" onchange="this.form.submit()" class="form-control">
                                <s:option value="10">10</s:option>
                                <s:option value="25">25</s:option>
                                <s:option value="50">50</s:option>
                                <s:option value="100">100</s:option>
                                <s:option value="500">500</s:option>
                                <s:option value="1000">1000</s:option>
                            </s:select>
                            <fmt:message bundle="${wokoBundle}" key="woko.devel.list.objectPerPage"/>
                        </s:form>
                    </div>
                </div>
            </c:if>

                <%
                    List<Map<String,Object>> propertyValues = new ArrayList<Map<String, Object>>();
                    List<String> rowCssClasses = new ArrayList<String>();
                    List<Object> resultsList = new ArrayList<Object>();
                    List<String> propertyNames = null;
                    boolean computePropUnion = false;
                    if (list instanceof TabularResultFacet) {
                        TabularResultFacet trf = (TabularResultFacet)list;
                        propertyNames = trf.getPropertyNames();
                    }
                    if (propertyNames==null) {
                        propertyNames = new ArrayList<String>();
                        computePropUnion = true;
                    }

                    // show the "actions" column only of there is at least one possible
                    // action
                    boolean hasAtLeastOneAction = false;

                    while (results.hasNext()) {
                        Object result = results.next();
                        resultsList.add(result);
                        RenderProperties rpResult = woko.getFacet(RenderProperties.FACET_NAME, request, result, result.getClass());
                        Map<String,Object> resultPropValues = rpResult.getPropertyValues();
                        propertyValues.add(resultPropValues);
                        RenderListItem renderListItem = woko.getFacet(WokoFacets.renderListItem, request, result, result.getClass(),true );
                        String liWrapperClass = renderListItem.getItemWrapperCssClass();
                        if (liWrapperClass==null) {
                            liWrapperClass = "";
                        }
                        rowCssClasses.add(liWrapperClass);

                        if (computePropUnion) {
                            List<String> resultPropNames = rpResult.getPropertyNames();
                            // union of all props (TODO intersect is safer and accurate !)
                            for (String resultPropName : resultPropNames) {
                                if (!propertyNames.contains(resultPropName)) {
                                    propertyNames.add(resultPropName);
                                }
                            }
                        }

                        // check if at least one action is available (once only)
                        if (!hasAtLeastOneAction) {
                            RenderTabularListItemLinks rl = woko.getFacet(WokoFacets.renderTabularListItemLinks, request, result);
                            if (rl!=null) {
                                List<Link> links = rl.getLinks();
                                hasAtLeastOneAction = links!=null && links.size()>0;
                            }
                        }
                    }

                    if (results.getTotalSize()>0) {
                        // display table only if we have at least one result
                %>
                <table class="<%=listWrapperClass%>">
                    <thead>
                    <tr>
                        <%
                            for (String propName : propertyNames) {
                                String labelMsgKey = className + "." + propName;
                                String msg = woko.getLocalizedMessage(request, labelMsgKey);
                        %>
                        <th>
                            <c:out value="<%=msg%>"/>
                        </th>
                        <%
                            }
                        %>
                        <c:if test="<%=hasAtLeastOneAction%>">
                            <th><fmt:message bundle="${wokoBundle}" key="woko.devel.list.actions.column.label"/></th>
                        </c:if>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (int i=0; i<resultsList.size(); i++) {
                            Object result = resultsList.get(i);
                            String liWrapperClass = rowCssClasses.get(i);
                            if (listWrapperClass==null) {
                              listWrapperClass = "";
                            }
                    %>
                            <tr class="<%=liWrapperClass%>">
                                <%
                                    for (String propName : propertyNames) {
                                        Object propVal = propertyValues.get(i).get(propName);
                                        RenderPropertyValue rpv = Util.getRenderPropValueFacet(
                                                woko, request, result, propName, propVal);
                                        String pValFragmentPath = rpv.getFragmentPath(request);
                                %>
                                <td>
                                    <jsp:include page="<%=pValFragmentPath%>"/>
                                </td>
                                <%
                                    }
                                %>
                                <c:if test="<%=hasAtLeastOneAction%>">
                                    <td>
                                        <w:includeFacet facetName="<%=WokoFacets.renderTabularListItemLinks%>" targetObject="<%=result%>"/>
                                    </td>
                                </c:if>

                            </tr>
                    <%
                      }
                    %>
                    </tbody>
                </table>

            <%
                }

                int nbPagesClickable = nbPages < 10 ? nbPages : 10;
                if (nbPages>1) {
                    int pagerStart = p > nbPagesClickable ? p - (nbPagesClickable-1) : 1;

                    // Catch arguments from ResultFacet
                    String args = "";
                    if (list.getArgs() != null)  {
                        for(Object key : list.getArgs().keySet()){
                            args += "&" + key + "=" + list.getArgs().get(key);
                        }
                    }

                    String leftMoveCss = p <= 1 ? "disabled" : "";
                    String leftMoveHref = leftMoveCss.equals("disabled") ? "" : request.getContextPath() + "/list/" + className +
                            "?facet.page=" + (p - 1) + "&facet.resultsPerPage=" + resultsPerPage + args;

                    String rightMoveCss = p == nbPages ? "disabled" : "";
                    String rightMoveHref = rightMoveCss.equals("disabled") ? "" : request.getContextPath() + "/list/" + className +
                            "?facet.page=" + (p + 1) + "&facet.resultsPerPage=" + resultsPerPage + args;
            %>

                <ul class="pagination">
                    <li class="<%=leftMoveCss%>">
                        <a href="<%=leftMoveHref%>">«</a>
                    </li>
                <%
                    for (int i=pagerStart;i<=pagerStart+nbPagesClickable-1;i++) {
                        String css = "";
                        String currentPageHref = request.getContextPath() + "/list/" + className +
                                "?facet.page=" + i  + "&facet.resultsPerPage=" + resultsPerPage + args;

                        if (i==p) {
                            css = "active";
                        }

                %>
                    <li class="<%=css%>">
                        <a href="<%=currentPageHref%>"><%=i%></a>
                    </li>
                <%
                    }
                %>

                    <li class="<%=rightMoveCss%>">
                        <a href="<%=rightMoveHref%>">»</a>
                    </li>
                </ul>

            <%
                }
            %>

        </div>

    </s:layout-component>
</s:layout-render>

