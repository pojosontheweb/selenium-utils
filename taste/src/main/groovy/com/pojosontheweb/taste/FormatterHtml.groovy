package com.pojosontheweb.taste

import groovy.xml.MarkupBuilder

class FormatterHtml implements ResultFormatter {

    private static String getReportCss() {
        FormatterHtml.class.getResourceAsStream("/taste-report.css").text
    }

    private static def buildPage(
        MarkupBuilder builder,
        String pageTitle,
        Closure content) {

        builder.html {
            head {
                title {
                    mkp.yield(pageTitle)
                }
                style('type':'text/css') {
                    mkp.yieldUnescaped reportCss
                }
            }
            body {
                content()
            }
        }
    }

    private static def buildTableRow(MarkupBuilder b, String thContent, def tdContent) {
        b.tr {
            th {
                mkp.yield thContent
            }
            td {
                mkp.yield tdContent
            }
        }
    }

    private static def buildCfgSection(MarkupBuilder b, Cfg cfg) {
        b.h2 {
            mkp.yield 'Configuration'
        }
        if (cfg?.sysProps) {
            b.table('cellspacing':0, 'cellpadding':0) {
                cfg.sysProps.each { k,v ->
                    buildTableRow(b, k, v)
                }
            }
        } else {
            b.p {
                mkp.yield 'No configuration provided'
            }
        }
    }

    private static def buildTestResult(MarkupBuilder b, TestResult testResult) {
        String tblClass = testResult instanceof ResultSuccess ? 'success' : 'failure'
        b.table('class':"test-result $tblClass", 'cellspacing':0, 'cellpadding':0) {
            tbody {
                tr {
                    th(colspan:"2", 'class':'title') {
                        mkp.yield(testResult.testName)
                    }
                }
                buildTableRow(b, 'Started', testResult.startedOn)
                if (testResult.finishedOn) {
                    buildTableRow(b, 'Finished', testResult.finishedOn)
                    buildTableRow(b, 'Elapsed', ((testResult.finishedOn.time - testResult.startedOn.time) / 1000) + 's')
                }
                if (testResult instanceof ResultFailure) {
                    ResultFailure failure = (ResultFailure)testResult
                    b.tr {
                        th(valign:'top') {
                            mkp.yield 'Error'
                        }
                        td('class':'stack-trace') {
                            pre {
                                mkp.yield failure.stackTrace
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    void format(Cfg cfg, String fileName, TestResult testResult, Writer out) {
        MarkupBuilder b = new MarkupBuilder(out)
        buildPage(b, "Taste - $fileName") {
            b.h1 {
                mkp.yield "Taste - $fileName"
            }

            buildCfgSection(b, cfg)

            b.h2 {
                mkp.yield 'Test Result'
            }

            buildTestResult(b, testResult)
        }
    }

    @Override
    void format(Cfg cfg, String fileName, SuiteResult suiteResult, Writer out) {
        MarkupBuilder b = new MarkupBuilder(out)
        buildPage(b, "Taste - $fileName") {
            b.h1 {
                mkp.yield "Taste - $fileName"
            }

            b.h2 {
                mkp.yield "Summary"
            }
            String suiteClass = suiteResult.success ? 'success' : 'failure'
            b.table('class': suiteClass, 'cellspacing':0, 'cellpadding':0) {
                tbody {
                    tr {
                        th(colspan: 2, 'class':'title') {
                            mkp.yield(suiteResult.name)
                        }
                        int success = 0
                        int failed = 0
                        suiteResult?.testResults?.each { TestResult r ->
                            if (r instanceof ResultSuccess) {
                                success++
                            } else {
                                failed++
                            }
                        }
                        int total = success+failed
                        def percent = total>0 ? success * 100 / total : 0
                        buildTableRow(b, 'Total', total)
                        buildTableRow(b, 'Success', success)
                        buildTableRow(b, 'Failed', failed)
                        buildTableRow(b, 'Pass ratio', "$percent %")
                    }
                }
            }

            buildCfgSection(b, cfg)

            b.h2 {
                mkp.yield 'Test Results'
            }

            suiteResult?.testResults?.each { TestResult r ->
                buildTestResult(b, r)
            }

        }
    }

}
