<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.tastecloud.actions.InitialConfigAction" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<w:facet facetName="<%=WokoFacets.layout%>"/>
<s:layout-render name="${layout.layoutPath}" layout="${layout}"
                 pageTitle="Initial configuration" skipLoginLink="true">
  <s:layout-component name="body">

    <div class="container-fluid">

      <h1>Initial configuration</h1>

      <p>
        This is the first time you access this taste instance, it
        has not been configured yet.
      </p>

      <s:form class="form-horizontal" beanclass="<%=InitialConfigAction.class%>">

        <w:b3-form-group-css fieldName="config.webappDir" var="wadCss"/>
        <div class="form-group ${wadCss}">
          <label for="config.webappDir" class="col-sm-2 control-label">Webapp dir</label>
          <div class="col-sm-10">
            <s:text name="config.webappDir" class="form-control" placeholder="base directory"/>
            <span id="helpBlock" class="help-block">
              The directory the webapp will read/write to. Must be available to the webapp process.
            </span>
          </div>
        </div>

        <w:b3-form-group-css fieldName="config.dockerUrl" var="duCss"/>
        <div class="form-group ${duCss}">
          <label for="config.dockerUrl" class="col-sm-2 control-label">Docker url</label>
          <div class="col-sm-10">
            <s:text name="config.dockerUrl" class="form-control" placeholder="docker url"/>
            <span id="helpBlock" class="help-block">
              Url to the docker engine.
            </span>
          </div>
        </div>

        <w:b3-form-group-css fieldName="config.dockerDir" var="ddCss"/>
        <div class="form-group ${ddCss}">
          <label for="config.dockerDir" class="col-sm-2 control-label">Docker dir</label>
          <div class="col-sm-10">
            <s:text name="config.dockerDir" class="form-control" placeholder="base directory"/>
            <span id="helpBlock" class="help-block">
              The directory that taste dockers will read/write to. Must be available to the taste docker.
            </span>
          </div>
        </div>

        <div class="form-group">
          <div class="col-sm-offset-2 col-sm-10">
            <s:submit name="configure" class="btn btn-default" value="Configure"/>
          </div>
        </div>
      </s:form>

    </div>

  </s:layout-component>
</s:layout-render>