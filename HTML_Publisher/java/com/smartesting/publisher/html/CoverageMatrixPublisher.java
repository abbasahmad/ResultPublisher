package com.smartesting.publisher.html;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.smartesting.publisher.api.entities.Tag;
import com.smartesting.publisher.api.entities.TestContext;
import com.smartesting.publisher.api.toolbox.action.ActionUtils;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;
import java.util.Collection;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.smartesting.publisher.api.toolbox.TagAttributeFactory.*;
import static com.smartesting.publisher.html.CommonHtmlActions.titleRow;
import static com.smartesting.publisher.html.Context.key;
import static com.smartesting.publisher.html.HtmlDocument.*;
import static java.lang.String.format;

final class CoverageMatrixPublisher {

    private static boolean isOdd = false;

    private CoverageMatrixPublisher() {
    }

    static void createCoverageTableFile(
            final Iterable<String> tags,
            final Iterable<TestContext> testContextList,
            final Context context,
            final String tagType) {
        final IndentedDocument document = context.createHtmlDocument(
                "./resources/smartesting.css",context.getI18N(key("tagCoverage.mainTitle")));
        ActionUtils.process(
                document,
                br(), br(),
                table(
                    attributes(
                        width("80%"),
                        height("100%"),
                        align("center"),
                        cellpadding("0"),
                        cellspacing("0"),
                        klass("title")),
                titleRow(context.getI18N(key("tagCoverageMatrix.mainTitle")), "./resources/"),
                row(
                        cell(
                                attributes(colspan("3"), valign("top")), br(), table(
                                attributes(border("1"), width("100%"), height("100%")), linkAsRow(
                                context.getProjectPath(), context.getI18N(key("testsuite.backToProject"))), row(
                                cell(
                                        table(
                                                attributes(
                                                        width("100%"), valign("top"), klass("tagCoverageMatrix")), row(
                                                cell(
                                                        attributes(
                                                                align("center"),
                                                                colspan("2"),
                                                                klass("tagCoverageMatrixTableTitle")),
                                                        br(),
                                                        bold(
                                                                text(
                                                                        context.getI18N(
                                                                                key("tagCoverageMatrix.synthesisTitle")))),
                                                        br(),
                                                        br())), row(
                                                cell(
                                                        attributes(
                                                                klass("tagCoverageMatrixCellTitle"),
                                                                align("center"),
                                                                colspan("2")), text(
                                                        context.getI18N(
                                                                key("tagCoverageMatrix.Test"))))), row(
                                                cell(
                                                        attributes(
                                                                klass("tagCoverageMatrixCellTitle"),
                                                                align("center"),
                                                                width("10px")), text(
                                                        tagType)), cell(
                                                createCoverageSynthesisTable(
                                                        tags, testContextList, context))), row(
                                                cell(
                                                        attributes(colspan("2")), spaces(1)))))))))));
        context.writeDocumentToFile(document, context.getCoveragePath(tagType));
    }

    private static HtmlAction createCoverageSynthesisTable(
            final Iterable<String> allTags, final Iterable<TestContext> testContextList, final Context context) {
        final Collection<HtmlAction> cells = newArrayList();
        cells.add(cell(attributes(klass("tagCoverageMatrixCellTitle")), spaces(1)));
        cells.add(cell(attributes(klass("tagCoverageMatrixCellTitle")), spaces(1)));
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                setOdd(false);
                ActionUtils.process(
                        document, table(
                        attributes(width("100%"),align("center")), row(
                        toArray(
                                Iterables.concat(
                                        cells, transform(
                                        testContextList, intoCell(context))),
                                HtmlAction.class)), createRowsForReqs(allTags, testContextList)));
            }
        };
    }

    private static HtmlAction createRowsForReqs(final Iterable<String> allTags, final Iterable<TestContext> testContextList) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                ActionUtils.process(
                        document, toArray(
                        transform(allTags, intoRowCells(testContextList)),
                        HtmlAction.class));
            }
        };
    }

    private static Function<String, HtmlAction> intoRowCells(final Iterable<TestContext> testContextList) {
        return new Function<String, HtmlAction>() {
            public HtmlAction apply(final String tagStr) {
                final Collection<HtmlAction> cells = newArrayList();
                setOdd(!isOdd());
                final int counter = getCoveredTestCountForTag(tagStr, testContextList);

                cells.add(cell(attributes(valign("top"), renderingClass(counter, isOdd())), text(tagStr)));
                cells.add(
                        cell(
                                attributes(
                                        renderingClass(counter, isOdd()),
                                        align("center")), text(format("%d", counter))));

                for (final TestContext testContext : testContextList) {
                    cells.add(
                            cell(
                                    attributes(align("center"), klass("tagCoverageResult")),
                                    testContainsTagName(tagStr, testContext) ? image("resources/covered.png") : spaces(1)));
                }
                return row(toArray(cells, HtmlAction.class));
            }

            private int getCoveredTestCountForTag(final String tagStr, final Iterable<TestContext> testContextList) {
                int counter = 0;
                for (final TestContext testContext : testContextList) {
                    if (testContainsTagName(tagStr, testContext)){
                        counter++;
                    }
                }
                return counter;
            }

            private TagAttribute renderingClass(final int counter, final boolean isOdd) {
                return klass("tagCoverageRow" + (counter == 0 ? "Error" : isOdd ? "Odd" : "Even"));
            }

        };
    }

    private static boolean testContainsTagName(final String tagStr, final TestContext testContext) {
        for (final Tag tag : testContext.getTest().getAllActivatedTags()) {
            if (tag.getName().equals(tagStr)){
                return true ;
            }
        }
        return false;
    }

    private static Function<TestContext, HtmlAction> intoCell(final Context context) {
        return new Function<TestContext, HtmlAction>() {
            public HtmlAction apply(final TestContext tc) {
                setOdd(!isOdd());
                return cell(
                        attributes(valign("top"), klass("tagCoverageMatrixTest" + (isOdd() ? "Odd" : "Even"))),
                        link(
                                attributes(klass("tagCoverageLink")),
                                context.getTestPath(tc.getSuite(), tc.getTest()),
                                tc.getTest().getName()));
            }
        };
    }

    public static boolean isOdd() {
        return isOdd;
    }

    public static void setOdd(final boolean odd) {
        isOdd = odd;
    }
}


