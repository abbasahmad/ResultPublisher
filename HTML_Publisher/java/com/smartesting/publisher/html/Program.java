package com.smartesting.publisher.html;

import com.google.common.collect.Ordering;
import com.smartesting.publisher.api.entities.Project;
import com.smartesting.publisher.api.entities.Tag;
import com.smartesting.publisher.api.entities.TestContext;
import com.smartesting.publisher.api.entities.TestProjection;
import com.smartesting.publisher.api.entities.TestSuite;
import com.smartesting.publisher.api.startup.HelpDefinition;
import com.smartesting.publisher.api.startup.InitialisationException;
import com.smartesting.publisher.api.startup.PublisherProgram;
import com.smartesting.publisher.api.tagrepository.TagRepository;
import com.smartesting.publisher.api.toolbox.ExceptionUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Category;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newTreeSet;
import static com.smartesting.publisher.api.entities.TagUtils.defaultTagTypeComparator;
import static com.smartesting.publisher.api.entities.TagUtils.getTags;
import static com.smartesting.publisher.api.startup.HelpRenderer.newline;
import static com.smartesting.publisher.api.startup.HelpRenderer.text;
import static com.smartesting.publisher.api.startup.UsualOptions.*;
import static com.smartesting.publisher.api.toolbox.FileUtils.cleanDirectory;
import static com.smartesting.publisher.api.toolbox.ResourceUtils.copy;
import static com.smartesting.publisher.api.toolbox.ResourceUtils.loadResource;
import static com.smartesting.publisher.api.toolbox.VersionUtils.getPublisherVersion;
import static java.awt.Desktop.*;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Arrays.asList;
import static org.apache.log4j.Logger.getLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class Program.
 */
public final class Program extends PublisherProgram<Context> {
    
    /** The Constant OUTPUT. */
    private static final Option OUTPUT = output();
    
    /** The Constant PROJECTION. */
    private static final Option PROJECTION = projection();
    
    /** The Constant TAG_REPOSITORY. */
    private static final Option TAG_REPOSITORY = tags();
    
    /** The Constant DONT_OPEN_RESULT. */
    private static final Option DONT_OPEN_RESULT = new Option(
            null, "dontOpenResult", false, "dont open html result in the default browser");
    
    /** The Constant LOCALE. */
    private static final ChoiceOption LOCALE = locale(Locale.FRENCH, Locale.ENGLISH);
    
    /** The Constant LOGGER. */
    private static final Category LOGGER = getLogger(Program.class);
    
    /** The can open. */
    private boolean canOpen = true;
    
    /** The tag repository. */
    private TagRepository tagRepository;

    /* (non-Javadoc)
     * @see com.smartesting.publisher.api.startup.AbstractPublisherProgram#addOptions(org.apache.commons.cli.Options)
     */
    public void addOptions(final Options options) {
        options.addOption(OUTPUT);
        options.addOption(TAG_REPOSITORY);
        options.addOption(LOCALE);
        options.addOption(DONT_OPEN_RESULT);
        options.addOption(PROJECTION);
    }

    /* (non-Javadoc)
     * @see com.smartesting.publisher.api.startup.AbstractPublisherProgram#defineHelp(com.smartesting.publisher.api.startup.HelpDefinition)
     */
    protected void defineHelp(final HelpDefinition definition) {
        definition.addHeaderDefinition(text("HTML publisher. This is the standard HTML publisher."), newline());
        definition.addHeaderDefinition(text("Mandatory option is: output"), newline());
        definition.addPropertiesDefinition(
                text("Version: " + getPublisherVersion(Program.class)), text("Author: Smartesting"));
    }

    /* (non-Javadoc)
     * @see com.smartesting.publisher.api.startup.PublisherProgram#createContext(org.apache.commons.cli.CommandLine, com.smartesting.publisher.api.entities.Project)
     */
    public Context createContext(final CommandLine line, final Project project) throws InitialisationException {
        Locale.setDefault(getLocale(LOCALE, Locale.ENGLISH));
        final File outputDirectory = getOutputDirectory(OUTPUT, "html");
        cleanDirectory(outputDirectory);
        if (line.hasOption(DONT_OPEN_RESULT.getLongOpt())) {
            canOpen = false;
        }
        tagRepository = getTagRepository(TAG_REPOSITORY);
        return new Context(project, outputDirectory);
    }

    /* (non-Javadoc)
     * @see com.smartesting.publisher.api.startup.PublisherProgram#postPublish(java.lang.Object)
     */
    public void postPublish(final Context context) {
        try {
            copyResources(context.getOutputDirectory());
        } catch (final IOException e) {
            LOGGER.debug(e.getMessage());
            throw propagate(e);
        }
        if (canOpen) {
            if (!isDesktopSupported() || !getDesktop().isSupported(Action.OPEN)) {
                LOGGER.warn(context.getI18N("publishing.autoOpenNotAvailable"));
                return;
            }
            try {
                getDesktop().open(new File(context.getOutputDirectory(), context.getProjectPath()));
            } catch (final IOException e) {
                LOGGER.debug(e.getMessage());
                ExceptionUtils.ignoreButTrace(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.smartesting.publisher.api.startup.PublisherProgram#publish(com.smartesting.publisher.api.entities.Project, java.lang.Object)
     */
    protected void publish(final Project project, final Context context) {
        LOGGER.info(context.getI18N("publishing.outputDirectory", context.getOutputDirectory()));
        final TestProjection currentProjection;
        try {
            currentProjection = getProjection(PROJECTION);
        } catch (final InitialisationException e) {
            LOGGER.error(e.getMessage());
            return;
        }

        for (final TestSuite suite : project.getTestSuites()) {
            TestSuitePublisher.adaptTestSuite(suite, context);
        }

        final Iterable<TestContext> projectTestContexts = TestContext.fromProject(project, currentProjection);
        final Iterable<TestContext> testContextsSorted = byTestName().sortedCopy(projectTestContexts);

        for (final TestContext testContext : projectTestContexts) {
            TestPublisher.adaptTest(testContext.getTest(), testContext.getSuite(), context, currentProjection);
        }

        final Collection<String> allTagsType = getAllTypesFromTestAndRepository(projectTestContexts, tagRepository);

        for (final String tagType : allTagsType) {
            final Iterable<String> allTags = getAllTags(projectTestContexts, tagRepository, tagType);
            CoverageMatrixPublisher.createCoverageTableFile(allTags, testContextsSorted, context, tagType);
        }

        ProjectPublisher.adaptProject(project, context, allTagsType);
    }

    /**
     * By test name.
     *
     * @return the ordering
     */
    private static Ordering<TestContext> byTestName() {
        return new Ordering<TestContext>() {
            public int compare(final TestContext context1, final TestContext context2) {
                return CASE_INSENSITIVE_ORDER.compare(context1.getTest().getName(), context2.getTest().getName());
            }
        };
    }

    /**
     * Gets the all types from test and repository.
     *
     * @param projectTestContexts the project test contexts
     * @param tagRepository the tag repository
     * @return the all types from test and repository
     */
    private static Collection<String> getAllTypesFromTestAndRepository(
            final Iterable<TestContext> projectTestContexts, final TagRepository tagRepository) {
        final Collection<String> result = newTreeSet(defaultTagTypeComparator());
        for (final TestContext testContext : projectTestContexts) {
            for (final Tag tag : testContext.getTest().getTags()) {
                result.add(tag.getType());
            }
        }
        addAll(result, tagRepository.getAllTypes());
        return result;
    }

    /**
     * Gets the all tags.
     *
     * @param testContexts the test contexts
     * @param tagRepository the tag repository
     * @param tagType the tag type
     * @return the all tags
     */
    private static Iterable<String> getAllTags(
            final Iterable<TestContext> testContexts, final TagRepository tagRepository, final String tagType) {
        final Collection<String> coverageTagsSet = newTreeSet(CASE_INSENSITIVE_ORDER);

        for (final com.smartesting.publisher.api.tagrepository.Tag tag : tagRepository.getTagsWithType(tagType)) {
            coverageTagsSet.add(tag.getName());
        }
        for (final TestContext testContext : testContexts) {
            coverageTagsSet.addAll(getTags(testContext.getTest().getAllActivatedTags(), tagType));
        }
        return coverageTagsSet;
    }

    /**
     * Copy resources.
     *
     * @param output the output
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void copyResources(final File output) throws IOException {
        for (final String name : asList(
                "resources/arrow.gif",
                "resources/covered.png",
                "resources/logo.png",
                "resources/arrow-operation.gif",
                "resources/top.png",
                "resources/border.png",
                "resources/PictoCenterSmall.jpg",
                "resources/eye.png",
                "resources/underline.png",
                "resources/smartesting.css")) {
            final InputStream resource = loadResource(Program.class, name);
            try {
                copy(resource, new File(output, name));
            } finally {
                resource.close();
            }
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
    	TestPublisher.setResultFilePath(args[5]);
    	for(int i =0; i< args.length; i++){
    		LOGGER.info("Arguments of the program \n args" +i +" = "+args[i]);
    	}
    	
        new Program().run(args);
    }
}
