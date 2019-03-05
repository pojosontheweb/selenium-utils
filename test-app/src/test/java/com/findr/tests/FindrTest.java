package com.findr.tests;

import com.pojosontheweb.selenium.AbstractPageObject;
import com.pojosontheweb.selenium.Findr;
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase;
import com.pojosontheweb.selenium.Retry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.pojosontheweb.selenium.Findrs.*;

public class FindrTest extends ManagedDriverJunit4TestBase {

    private final Server server = new Server(8080);


    @Before
    public void openPage() throws Exception {
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        File warFile = new File("./src/main/webapp");
        webapp.setWar(warFile.getAbsolutePath());
        server.setHandler(webapp);
        server.start();
        getWebDriver().get("http://localhost:8080");
    }

    @After
    public void stopServer() throws Exception {
        server.stop();
    }


    @Test
    public void simple() {
        Findr res = $$(".simple-result")
                .where(hasClass("yalla"))
                .where(attrEquals("my-attr", "foo"))
                .count(1)
                .at(0);

        res.where(textEquals("not yet clicked")).eval();

        $("#simple-sync").click();
        res.where(textEquals("sync clicked")).eval();

        $("#simple-timeout").click();
        res.where(textEquals("timeout clicked")).eval();
    }

    @Test
    public void listOfThings() {
        ListOfThings lot = new ListOfThings($("#list-of-things"));

        lot.assertValues(
            "value 1",
            "value 2",
            "value 3",
            "value 4",
            "value 5"
        );

        lot.assertValueClass("bar", "value 5");

        lot.clickChangeList();

        Retry.retry(2)
            .add(() -> {
                lot.assertValues(
                    "value 10",
                    "value 20",
                    "value 30",
                    "value 40",
                    "value 50"
                );
            })
            .add(() -> {
                lot.assertValueClass("bar", "value 50");
            })
            .eval();
    }

    private static class ListOfThings extends AbstractPageObject {

        private ListOfThings(Findr findr) {
            super(findr);
        }

        private Findr ulElem = $$("ul")
                .where(hasClass("shoo"))
                .where(hasClass("be"))
                .where(hasClass("doo"))
                .where(hasClass("wah"))
                .where(not(hasClass("wtf")))
                .count(1)
                .at(0);

        public ListOfThings assertValues(final String... values) {
            return ulElem.$$("li.foo")
                    .whereAll(hasClass("foo"))
                    .whereAny(hasClass("bar"))
                    .eval(elems -> {
                if (elems.size() != values.length) {
                    return null;
                }

                for (int i = 0 ; i < values.length ; i++) {
                    if (!elems.get(i).getText().equals(values[i])) {
                        return null;
                    }
                }

                return this;
            });
        }

        public ListOfThings assertValueClass(String clazz, String value) {
            ulElem.$$("li.foo")
                    .where(hasClass(clazz))
                    .where(textEquals(value))
                    .count(1)
                    .eval("didn't find class " + clazz + " and text " + value);
            return this;
        }

        public ListOfThings clickChangeList() {
            $("#change-list").click();
            return this;
        }


    }
}
