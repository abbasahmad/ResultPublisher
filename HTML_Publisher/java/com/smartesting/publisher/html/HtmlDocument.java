package com.smartesting.publisher.html;

import com.smartesting.publisher.api.toolbox.action.Action;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;

import static com.smartesting.publisher.api.toolbox.TagAttributeFactory.*;
import static com.smartesting.publisher.api.toolbox.action.ActionUtils.process;

final class HtmlDocument {
    private HtmlDocument() {
    }

    static IndentedDocument createHtmlDocument(final CharSequence stylesheet, final CharSequence title) {
        final IndentedDocument document = new IndentedDocument("\t", "</body>\n</html>\n");
        openTag(document, "html");
        document.newline();
        openTag(
                document,
                "meta",
                attribute("http-equiv", "Content-Type"),
                attribute("content", "text/html; charset=UTF-8"));
        openIndentedTag(document, "head");
        openTag(document, "link", attribute("rel", "stylesheet"), attribute("type", "text/css"), href(stylesheet));
        closeIndentedTag(document, "head");
        openIndentedTag(document, "title");
        document.append(title);
        closeIndentedTag(document, "title");
        openIndentedTag(document, "body");
        return document;
    }

    static HtmlAction linkAsRow(
            final CharSequence path, final CharSequence underlinedText, final TagAttribute... attributes) {
        return row(
                cell(
                        br(), table(
                        attributes(width("95%"), align("center"), cellspacing("0")),
                        row(cell(link(attributes(attributes), path, underlinedText))))));
    }

    interface HtmlAction extends Action<IndentedDocument> {
    }

    static HtmlAction table(final TagAttribute attributes, final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openIndentedTag(document, "table", attributes);
                process(document, (HtmlAction[]) children);
                closeIndentedTag(document, "table");
            }
        };
    }

    static HtmlAction row(final TagAttribute attributes, final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openIndentedTag(document, "tr", attributes);
                process(document, (HtmlAction[]) children);
                closeIndentedTag(document, "tr");
            }
        };
    }

    static HtmlAction row(final HtmlAction... children) {
        return row(attributes(), children);
    }
   

    static HtmlAction cell(final TagAttribute attributes, final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openIndentedTag(document, "td", attributes);
                process(document, (HtmlAction[]) children);
                closeIndentedTag(document, "td");
            }
        };
    }

    static HtmlAction cell(final HtmlAction... children) {
        return cell(attributes(), children);
    }

    static HtmlAction br() {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "br");
            }
        };
    }

    static HtmlAction hr(final TagAttribute attributes) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "hr", attributes);
            }
        };
    }

    static HtmlAction link(final TagAttribute attributes, final CharSequence path, final CharSequence underlinedText) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "a", href(path), compose(attributes));
                document.append(underlinedText);
                closeTag(document, "a");
            }
        };
    }

    static HtmlAction anchor(
            final TagAttribute attributes, final CharSequence path, final CharSequence underlinedText) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "a", attribute("name", path), compose(attributes));
                document.append(underlinedText);
                closeTag(document, "a");
            }
        };
    }

    static HtmlAction anchor(final CharSequence path, final CharSequence underlinedText) {
        return anchor(attributes(), path, underlinedText);
    }

    static HtmlAction listItem(final TagAttribute attributes, final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openIndentedTag(document, "li", attributes);
                process(document, (HtmlAction[]) children);
                closeIndentedTag(document, "li");
            }
        };
    }

    static HtmlAction link(final CharSequence path, final CharSequence underlinedText) {
        return link(attributes(), path, underlinedText);
    }

    static HtmlAction text(final CharSequence text) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                document.append(text);
            }
        };
    }

    static HtmlAction spaces(final int count) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                for (int i = 0; i < count; ++i) {
                    document.append("&nbsp;");
                }
            }
        };
    }

    static HtmlAction bold(final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "B");
                process(document, (HtmlAction[]) children);
                closeTag(document, "B");
            }
        };
    }

    static HtmlAction italic(final CharSequence text) {
        return italic(text(text));
    }

    static HtmlAction italic(final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "I");
                process(document, (HtmlAction[]) children);
                closeTag(document, "I");
            }
        };
    }

    static HtmlAction h1(final HtmlAction... children) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "h1");
                process(document, (HtmlAction[]) children);
                closeTag(document, "h1");
                document.newline();
            }
        };
    }

    static HtmlAction image(final TagAttribute attributes, final CharSequence path) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                openTag(document, "img", src(path), compose(attributes));
            }
        };
    }

    static HtmlAction image(final CharSequence path) {
        return image(attributes(), path);
    }

    static void openTag(final IndentedDocument document, final CharSequence name, final TagAttribute... attributes) {
        document.append('<');
        document.append(name);
        for (final Action<IndentedDocument> attribute : attributes) {
            attribute.perform(document);
        }
        document.append('>');
    }

    static void closeTag(final IndentedDocument document, final CharSequence name) {
        document.append("</");
        document.append(name);
        document.append('>');
    }

    static void openIndentedTag(
            final IndentedDocument document, final CharSequence name, final TagAttribute... attributes) {
        document.newline();
        openTag(document, name, attributes);
        document.incrementIndentation();
        document.newline();
    }

    static void closeIndentedTag(final IndentedDocument document, final CharSequence name) {
        document.decrementIndentation();
        document.newline();
        closeTag(document, name);
        document.newline();
    }
}
