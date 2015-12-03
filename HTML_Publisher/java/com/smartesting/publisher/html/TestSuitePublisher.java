package com.smartesting.publisher.html;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.smartesting.publisher.api.entities.Operation;
import com.smartesting.publisher.api.entities.Test;
import com.smartesting.publisher.api.entities.TestContainer;
import com.smartesting.publisher.api.entities.TestSuite;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import static com.smartesting.publisher.api.entities.ModelEntityUtils.pathAsString;
import static com.smartesting.publisher.api.entities.TestUtils.*;
import static com.smartesting.publisher.api.toolbox.TagAttributeFactory.*;
import static com.smartesting.publisher.api.toolbox.action.ActionUtils.process;
import static com.smartesting.publisher.html.CommonHtmlActions.titleRow;
import static com.smartesting.publisher.html.Context.key;
import static com.smartesting.publisher.html.HtmlDocument.*;
import static java.lang.String.CASE_INSENSITIVE_ORDER;

final class TestSuitePublisher {
    private static final String TESTS_BY_REQUIREMENT = "testsByRequirement";
    private static final String TESTS_BY_OPERATION = "testsByOperation";

    private TestSuitePublisher() {
    }

    static void adaptTestSuite(final TestSuite suite, final Context context) {
        createIndex(context, suite);
        createTestsBy(context, suite, TESTS_BY_REQUIREMENT, testsByRequirementAsRows(suite, context));
        createTestsBy(context, suite, TESTS_BY_OPERATION, testsByOperationAsRows(suite, context));
    }

    @SuppressWarnings("unchecked")
	private static void createIndex(final Context context, final TestSuite suite) {
        final IndentedDocument document = context.createHtmlDocument(
                "../resources/smartesting.css", key("testsuite.mainTitle"), suite.getName());
        process(
                document, br(), br(), table(
                attributes(
                        width("80%"),
                        height("100%"),
                        align("center"),
                        cellpadding("0"),
                        cellspacing("0"),
                        klass("title")),
                titleRow(context.getI18N(key("testsuite.title"), suite.getName()), "../resources/"),
                row(
                        cell(
                                attributes(colspan("3"), valign("top")), br(), table(
                                attributes(width("100%"), align("center")),

                                linkAsRow(
                                        "../" + context.getProjectPath(),
                                        context.getI18N(key("testsuite.backToProject"))), row(
                                cell(
                                        br(), table(
                                        attributes(width("95%"), align("center")), row(
                                        cell(
                                                attributes(klass("titleTests")),
                                                text(context.getI18N(key("testsuite.testList"))),
                                                text(" (" + suite.getTests().size() + ')'),
                                                br())), testsAsRows(suite.getTests(), context)))))))));
        context.writeDocumentToFile(document, context.getTestSuiteIndexPath(suite));
    }

    @SuppressWarnings("unchecked")
	private static void createTestsBy(
            final Context context, final TestSuite suite, final String groupName, final HtmlAction groupAction) {
        final IndentedDocument document = context.createHtmlDocument(
                "../resources/smartesting.css", key("testsuite." + groupName + ".title"));
        process(
                document, br(), br(), table(
                attributes(
                        width("80%"),
                        height("100%"),
                        align("center"),
                        cellpadding("0"),
                        cellspacing("0"),
                        klass("title")), titleRow(
                context.getI18N(key("testsuite." + groupName + ".title"), suite.getName()), "../resources/"), row(
                cell(
                        attributes(colspan("3"), valign("top")), br(), table(
                        attributes(width("100%"), align("center")), linkAsRow(
                        "index.html",
                        context.getI18N(key("testsuite." + groupName + ".toTestsuiteIndex"), suite.getName())), row(
                        cell(
                                hr(attributes(width("500"))), br(), table(
                                attributes(
                                        width("98%"),
                                        align("center"),
                                        valign("top"),
                                        klass("groupedTests"),
                                        cellspacing("0")), groupAction))))))

        ));
        context.writeDocumentToFile(document, context.buildPathFromTestSuite(suite, groupName + ".html"));
    }

    private static HtmlAction testsByOperationAsRows(final TestContainer suite, final Context context) {
        return new HtmlAction() {
            @SuppressWarnings("unchecked")
			public void perform(final IndentedDocument document) {
                for (final Map.Entry<Operation, Collection<Test>> entry : groupTests(
                        suite.getTests(), byOperation(), createByOperationMapping()).asMap().entrySet()) {
                    process(
                            document, row(
                            cell(
                                    attributes(klass("groupedTests"), align("left"), valign("top"), colspan("1")),
                                    text(pathAsString(entry.getKey(), "::"))),
                            testsAsRows(entry.getValue(), context),
                            row(cell(spaces(1)))));
                }
            }
        };
    }

    private static HtmlAction testsByRequirementAsRows(final TestContainer suite, final Context context) {
        return new HtmlAction() {
            @SuppressWarnings("unchecked")
			public void perform(final IndentedDocument document) {
                for (final Map.Entry<String, Collection<Test>> entry : groupTests(
                        suite.getTests(),
                        byRequirement(),
                        TreeMultimap.<String, Test>create(CASE_INSENSITIVE_ORDER, byName())).asMap().entrySet()) {
                    process(
                            document, row(
                            cell(
                                    attributes(klass("groupedTests"), align("left"), valign("top"), colspan("1")),
                                    text(entry.getKey())),
                            testsAsRows(entry.getValue(), context),
                            row(cell(spaces(1)))));
                }
            }
        };
    }

    private static HtmlAction testsAsRows(final Iterable<Test> tests, final Context context) {
        return new HtmlAction() {
            @SuppressWarnings("unchecked")
			public void perform(final IndentedDocument document) {
            	for (final Test test : Ordering.from(byName()).sortedCopy(tests)) {
            		process(
                            document, row(
                            cell(
                                    listItem(
                                            attributes(klass("testsRendering")),
                                            link(context.getTestFilename(test), test.getName())))));
                }
            }
        };
    }

    private static Multimap<Operation, Test> createByOperationMapping() {
        return TreeMultimap.create(
                new Comparator<Operation>() {
                    public int compare(final Operation operation1, final Operation operation2) {
                        return CASE_INSENSITIVE_ORDER.compare(
                                pathAsString(operation1, "::"), pathAsString(operation2, "::"));
                    }
                }, byName());
    }
}
