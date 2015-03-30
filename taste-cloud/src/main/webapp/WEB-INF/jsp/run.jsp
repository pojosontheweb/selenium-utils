<%@ page import="woko.facets.builtin.WokoFacets" %>
<%@ page import="com.pojosontheweb.selenium.Browsr" %>
<%@ page import="com.pojosontheweb.tastecloud.actions.RunAction" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>

<w:facet facetName="<%=WokoFacets.layout%>" />
<s:layout-render name="${layout.layoutPath}" layout="${layout}" pageTitle="New run">
  <s:layout-component name="body">

    <div class="container">

      <s:form beanclass="<%=RunAction.class%>">

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
          <s:textarea name="taste" rows="10" class="form-control">/*

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

          }
</s:textarea>
        </div>

        <s:submit class="btn btn-primary" name="run" value="Run"/>
      </s:form>

    </div>

  </s:layout-component>
</s:layout-render>