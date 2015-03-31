<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.selenium.Browsr" %>
<%@ page import="com.pojosontheweb.tastecloud.actions.RunAction" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<w:facet facetName="<%=WokoFacets.layout%>" />
<s:layout-render name="${layout.layoutPath}" layout="${layout}" pageTitle="New run">

  <s:layout-component name="customJs">

    <link rel="stylesheet" type="text/css" href="http://eclipse.org/orion/editor/releases/current/built-editor.css"/>
    <script src="http://eclipse.org/orion/editor/releases/current/built-editor.min.js"></script>
    <script>
      require(["orion/editor/edit"], function(edit) {
        var editor = edit({className: "editor"})[0];
        $('#runForm').submit(function() {
          $('#tasteText').val(editor.getText());
        });

        editor.setText($('#tasteText').val());
      });
    </script>

    <style type="text/css">
      .editor .textview {
        min-height: 400px;
      }
    </style>

  </s:layout-component>

  <s:layout-component name="body">

    <div class="container">

      <s:form beanclass="<%=RunAction.class%>" id="runForm">

        <w:b3-form-group-css fieldName="browsr" var="bCss"/>
        <div class="form-group ${bCss}">
          <label for="browsr">Browser</label>
          <s:select name="browsr" class="form-control">
            <s:options-enumeration enum="<%=Browsr.class.getName()%>"/>
          </s:select>
        </div>

        <w:b3-form-group-css fieldName="taste" var="bCss"/>
        <div class="form-group ${bCss}">
          <label for="taste">Taste</label>

          <pre class="editor" data-editor-lang="js" data-editor-show-annotation-ruler="false"
                     data-editor-show-overview-ruler="false" data-editor-show-folding-ruler="false">
          </pre>

          <s:textarea name="taste" id="tasteText" style="display:none;">/*

Taste Examples on google.com.

Basic Search/Images tests, written in two
different styles (plain findr or "dsl").

*/

import com.google.common.base.Predicate
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement

import static com.pojosontheweb.taste.Taste.*

suite('Google Tests') {

  add test('search') {

    webDriver.get 'http://www.google.com'

    findr()
      .elem(By.id('gbqfq'))
      .sendKeys('pojos on the web')

    findr()
      .elem(By.cssSelector('button.gbqfb'))
      .click()

    findr()
      .elem(By.id('search'))
      .elemList(By.cssSelector('h3.r'))
      .at(0)
      .elem(By.tagName('a'))
      .where(textContains('POJOs on the Web'))
      .eval()

  }

}</s:textarea>

          </div>

        <s:submit class="btn btn-primary" name="run" value="Run"/>
      </s:form>

    </div>

  </s:layout-component>
</s:layout-render>