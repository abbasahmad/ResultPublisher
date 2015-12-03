package com.smartesting.publisher.html;

import com.google.common.collect.Iterables;
import com.smartesting.publisher.api.entities.Project;
import com.smartesting.publisher.api.entities.TestSuite;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;
import java.util.Collection;

import static com.smartesting.publisher.api.entities.TagUtils.REQ;
import static com.smartesting.publisher.api.toolbox.TagAttributeFactory.*;
import static com.smartesting.publisher.api.toolbox.action.ActionUtils.process;
import static com.smartesting.publisher.html.CommonHtmlActions.titleRow;
import static com.smartesting.publisher.html.Context.key;
import static com.smartesting.publisher.html.HtmlDocument.*;

final class ProjectPublisher {
    private ProjectPublisher() {
    }

    static void adaptProject(final Project project, final Context context, final Collection<String> tagTypes) {
        final IndentedDocument document = context.createHtmlDocument(
                "resources/smartesting.css", key("project.mainTitle"), project.getName());
        process(
                document, br(), br(), table(
                attributes(
                        width("85%"),
                        height("100%"),
                        align("center"),
                        cellpadding("0"),
                        cellspacing("0"),
                        klass("title")),
                titleRow(context.getI18N(key("project.title"), project.getName()), "resources/"),
                row(
                        cell(
                                attributes(colspan("3"), align("center"), valign("top")), br(), table(
                                attributes(width("85%"), align("center"), valign("top")), row(
                                cell(
                                        attributes(valign("top")), table(
                                        attributes(width("100%"), align("center")), row(
                                        cell(
                                                br(), createCoverageTable(tagTypes, context))), row(
                                        cell(
                                                br(), table(
                                                attributes(width("90%"), align("center")),
                                                row(
                                                        cell(
                                                                attributes(klass("titles")),
                                                                text(context.getI18N(key("project.testsuiteList"))))),
                                                row(cell(displayTestSuitesAsListItems(context, project)))))), row(
                                        cell(
                                                br(), text(
                                                context.getI18N(
                                                        key("project.content"), project.getTests().size())))))), cell(
                                attributes(valign("top")), image("./resources/PictoCenterSmall.jpg"))))))));
        context.writeDocumentToFile(document, context.getProjectPath());
    }

    private static HtmlAction createCoverageTable(final Collection<String> tagTypes, final Context context) {
        if (tagTypes.isEmpty()) {
            return br();
        }
        final HtmlAction[] actions = new HtmlAction[tagTypes.size() + 1];
        actions[0] = row(cell(attributes(klass("titles")), text(context.getI18N(key("project.analysisTitle")))));
        for (int i = 0; i < tagTypes.size(); i++) {
            final String tagType = Iterables.get(tagTypes, i);
            actions[i + 1] = row(
                    cell(
                            link(
                                    context.getCoveragePath(tagType), context.getI18N(
                                    key("project.tagCoverageMatrix"), coverageTitle(tagType, context)))));
        }
        return table(attributes(width("90%"), align("center")), actions);
    }

    private static Object coverageTitle(final String tagType, final Context context) {
        return REQ.equals(tagType) ? context.getI18N("project.tagCoverageMatrix.req") : tagType;
    }

    private static HtmlAction displayTestSuitesAsListItems(final Context context, final Project project) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                for (final TestSuite suite : project.getTestSuites()) {
                    process(
                            document, listItem(
                            attributes(klass("defaultArrowListItem")),
                            link(context.getTestSuiteIndexPath(suite), suite.getName()),
                            spaces(1),
                            text(context.getI18N(key("project.testsuiteSummary"), suite.getTests().size()))));
                }
            }
        };
    }
}
