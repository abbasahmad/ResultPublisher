package com.smartesting.publisher.html;

import com.smartesting.publisher.api.toolbox.action.ActionUtils;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;

import static com.smartesting.publisher.api.toolbox.TagAttributeFactory.*;

final class CommonHtmlActions {
    private CommonHtmlActions() {
    }

    static HtmlDocument.HtmlAction titleRow(final CharSequence title, final String imageDirectory) {
        return new HtmlDocument.HtmlAction() {
            public void perform(final IndentedDocument document) {
                ActionUtils.process(
                        document, HtmlDocument.row(
                        HtmlDocument.cell(
                        attributes(height("100"), valign("top"), align("left")),
                        HtmlDocument.image(imageDirectory + "logo.png")),
                        HtmlDocument.cell(
                        attributes(valign("top"), width("100%")), HtmlDocument.table(
                        attributes(border("0"), height("100"), width("100%"), cellspacing("0")), HtmlDocument.row(
                        HtmlDocument.cell(
                                attributes(klass("title"), valign("top")),
                                HtmlDocument.h1(HtmlDocument.text(title))))))),
                        HtmlDocument.row(
                                HtmlDocument.cell(
                                        attributes(height("5"), align("left"), attribute("bgcolor","#9B998C")),
                                        HtmlDocument.image(imageDirectory + "underline.png")),
                                HtmlDocument.cell(
                                        attributes(height("5"), width("100%"), attribute("bgcolor","#9B998C")))

                        ));
            }
        };
    }
}
