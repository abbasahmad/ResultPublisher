package com.smartesting.publisher.html;

import com.smartesting.publisher.api.entities.Project;
import com.smartesting.publisher.api.entities.Test;
import com.smartesting.publisher.api.entities.TestSuite;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.google.common.base.Throwables.propagate;
import static com.smartesting.publisher.api.toolbox.FileUtils.ensureDirectory;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.apache.commons.lang.StringUtils.replaceChars;

@SuppressWarnings({"MethodMayBeStatic"})
final class Context {
    private final ResourceBundle bundle = ResourceBundle.getBundle(
            getClass().getName() + "Messages", Locale.getDefault(), getClass().getClassLoader());
    private final Project project;
    private final File outputDirectory;

    Context(final Project project, final File outputDirectory) {
        this.project = project;
        this.outputDirectory = outputDirectory;
    }

    IndentedDocument createHtmlDocument(final CharSequence stylesheet, final String key, final String... names) {
        return HtmlDocument.createHtmlDocument(stylesheet, getI18N(key, (Object[]) names));
    }

    void writeDocumentToFile(final IndentedDocument document, final String path) {
        try {
            final File file = new File(outputDirectory, path);
            ensureDirectory(file.getParentFile());
            writeStringToFile(file, document.getContent(), UTF_8);
        } catch (final IOException e) {
            throw propagate(e);
        }
    }

    File getOutputDirectory() {
        return outputDirectory;
    }

    String getProjectPath() {
        return project.getName() + ".html";
    }

    String getTestSuiteIndexPath(final TestSuite suite) {
        return buildPathFromTestSuite(suite, "index.html");
    }

    String getCoveragePath(final String kindCoverage) {
        return kindCoverage + "_coverage.html";
    }

    String buildPathFromTestSuite(final TestSuite suite, final String filename) {
        return suite.getName() + '/' + filename;
    }

    String getTestPath(final TestSuite suite, final Test test) {
        return suite.getName() + '/' + getTestFilename(test);
    }

    String getTestFilename(final Test test) {
        return replaceChars(test.getName(), "\"\\/#?:<>|*%'", "_____________") + ".html";
    }

    String getI18N(final String key, final Object... parameters) {
        try {
            return MessageFormat.format(bundle.getString(key), parameters);
        } catch (final MissingResourceException e) {
            return key;
        }
    }

    static String key(final String key) {
        return key;
    }
}
