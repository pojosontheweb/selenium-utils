<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp" %>
<c:set var="taste" value="${renderPropertiesEdit.facetContext.targetObject}"/>
<s:form action="/save/Taste" class="form-horizontal" id="saveTaste">

    <c:choose>
        <c:when test="${taste.id==null}">
            <s:hidden name="createTransient" value="true"/>
        </c:when>
        <c:otherwise>
            <s:hidden name="key" value="${taste.id}"/>
        </c:otherwise>
    </c:choose>

    <div class="container-fluid w-properties">

        <c:if test="${taste.id==null}">
            <div class="alert alert-info alert-dismissible" role="alert">
                <b>You are creating a simple Taste script !</b> This is good for
                basic tests, learning the APIs, or just to try out stuff.
                For more involved scenarios, you can bind a Git repository, and
                have your Tastes automatically triggered for you on commit.
                <br/>
                <br/>
                <a class="btn btn-primary" href="${cp}/edit/Repository?createTransient=true">Bind a Git repo</a>
                <button type="button" class="btn btn-default dismiss-alert" data-dismiss="alert" aria-label="Close">Not now, thanks</button>
            </div>
        </c:if>

        <w:b3-form-group-css fieldName="object.name" var="nameCss"/>
        <div class="${nameCss}">
            <div class="col-sm-12">
                <table width="100%">
                    <tr>
                        <td>
                            <s:text id="tasteName"
                                    name="object.name"
                                    class="form-control"
                                    placeholder="Give a name to your taste"/>
                        </td>
                        <td>
                            &nbsp;
                            <s:submit name="save" class="btn btn-primary"/>
                            <s:submit name="saveAndRun" class="btn btn-default" value="Save and run"/>
                        </td>
                    </tr>
                </table>
            </div>
        </div>

        <w:b3-form-group-css fieldName="object.taste" var="tasteCss"/>
        <div class="${tasteCss}">
            <div class="col-sm-12">
        <pre class="editor"
             data-editor-lang="js"
             data-editor-show-annotation-ruler="false"
             data-editor-show-overview-ruler="false"
             data-editor-show-folding-ruler="false"></pre>
            </div>
        </div>
    </div>
    <s:textarea name="object.taste" id="tasteText" style="display: none;"/>
</s:form>
<script>
    $(function() {
        var editor;
        require(["orion/editor/edit"], function (edit) {
            editor = edit({className: "editor"})[0];
            $('#saveTaste').submit(function () {
                $('#tasteText').val(editor.getText());
            });

            <c:choose>
            <c:when test="${taste.id==null}">
            editor.setText($('#initTaste').text());
            </c:when>
            <c:otherwise>
            editor.setText($('#tasteText').text());
            </c:otherwise>
            </c:choose>
            // ugly but better UX
            <c:if test="${taste.id==null}">
                setTimeout(function() {
                    $('#tasteName').trigger('focus');
                }, 500);
            </c:if>

            // initial resize
            resizeEditor(false);
        });

        function resizeEditor(animate) {
            // redraw the editor to accomodate vert space
            var ed = $('.editor'),
                    tasteName = $('#tasteName')[0],
                    top = tasteName.getBoundingClientRect().bottom,
                    winH = $(window).height(),
                    h = winH - top - 45;

            if (!animate) {
                ed.height(h);
                editor.resize();
            } else {
                ed.animate({height:h + 'px'}, 500, function() {
                    editor.resize();
                });
            }
        }

        $( window ).resize(function() {
            resizeEditor();
        });

        $('.dismiss-alert, .close').click(function() {
            setTimeout(function() {
                resizeEditor(true);
                $('#tasteName').trigger('focus');
            }, 200);
        });

    });
</script>
<script type="text/taste" id="initTaste">/*

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

    add test('searchF') {

        webDriver.get 'http://www.google.com'

        findr()
            .elem(By.id('lst-ib'))
            .sendKeys('pojos on the web')

        findr()
            .elem(By.cssSelector('button.lsb'))
            .where(attrEquals('value', 'Search'))
            .click()

        findr()
            .elem(By.id('search'))
            .elemList(By.cssSelector('h3.r'))
            .at(0)
            .elem(By.tagName('a'))
            .where(textContains('POJOs on the Web'))
            .eval()

    }

    add test('search') {

        webDriver.get 'http://www.google.com'

        $('#lst-ib') >> sendKeys('pojos on the web')

        $('button.lsb') + attrEquals('value', 'Search') >> click()

        $$('#search h3.r') +
            textContains('POJOs on the Web') +
            at(0) +
            $('a') +
            textContains('POJOs on the Web') >> eval()

    }

    add test('imagesF') {

        webDriver.get 'http://www.google.com'

        findr()
            .elemList(By.tagName('a'))
            .where(textEquals('Images'))
            .at(0)
            .click()

        findr()
            .elem(By.id('lst-ib'))
            .sendKeys('pojos on the web', Keys.ENTER)

        findr()
            .elemList(By.cssSelector('a.rg_l'))
            .where({ WebElement e ->
            e.getAttribute('href').contains 'www.pojosontheweb.com'
        } as Predicate)
            .at(0)
            .click()

        findr()
            .elemList(By.cssSelector('a.irc_vpl.irc_but'))
            .where(textEquals('Visit page'))
            .at(0)
            .click()

        findr()
            .elem(By.cssSelector('div.jumbotron h1'))
            .where(textEquals('POJOs on the Web!'))
            .eval()
    }

    add test('images') {

        webDriver.get 'http://www.google.com'
        $$('a') + textEquals('Images') + at(0) >> click()

        $('#lst-ib') >> sendKeys('pojos on the web', Keys.ENTER)

        $$('a.rg_l') + { WebElement e ->
            e.getAttribute('href').contains 'www.pojosontheweb.com'
        } + at(0) >> click()

        $$('a.irc_vpl.irc_but') +
            textEquals('Visit page') +
            at(0) >> click()

        $('div.jumbotron h1') +
            textEquals('POJOs on the Web!') >> eval()

    }

}
</script>

