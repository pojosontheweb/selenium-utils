package com.pojosontheweb.taste

/**
 * Created by vankeisb on 22/12/14.
 */
enum OutputFormat {

    text(new FormatterText()),
    json(new FormatterJson())

    private final ResultFormatter formatter

    OutputFormat(ResultFormatter formatter) {
        this.formatter = formatter
    }

    ResultFormatter getFormatter() {
        return formatter
    }
}