package com.smartesting.publisher.html;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Ordering.natural;
import static com.smartesting.publisher.api.description.DescriptionUtils.extractGroupOfSteps;
import static com.smartesting.publisher.api.description.DescriptionUtils.extractPrerequisites;
import static com.smartesting.publisher.api.entities.TagUtils.defaultTagTypeComparator;
import static com.smartesting.publisher.api.entities.TagUtils.groupTags;
import static com.smartesting.publisher.api.toolbox.TagAttributeFactory.*;
import static com.smartesting.publisher.api.toolbox.action.ActionUtils.process;
import static com.smartesting.publisher.html.CommonHtmlActions.titleRow;
import static com.smartesting.publisher.html.Context.key;
import static com.smartesting.publisher.html.HtmlDocument.*;
import static com.smartesting.toolbox.text.HtmlConverter.toHtml;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.smartesting.publisher.api.description.Prerequisite;
import com.smartesting.publisher.api.description.StepDescription;
import com.smartesting.publisher.api.entities.Step;
import com.smartesting.publisher.api.entities.Tag;
import com.smartesting.publisher.api.entities.Test;
import com.smartesting.publisher.api.entities.TestProjection;
import com.smartesting.publisher.api.entities.TestSuite;
import com.smartesting.publisher.api.toolbox.TagAttributeFactory.TagAttribute;
import com.smartesting.publisher.api.toolbox.document.IndentedDocument;
import com.smartesting.publisher.html.HtmlDocument.HtmlAction;

// TODO: Auto-generated Javadoc
/**
 * The Class TestPublisher.
 */
final class TestPublisher {
	
	private final static Logger LOGGER = Logger.getLogger(TestPublisher.class.getName());
	
	/** The result file path. */
	private static String resultFilePath;
	
	/** The result. */
	private static String result;
	
	/** The response. */
	private static String response;
	
	/** The test case tag name. */
	private static String testCaseTagName="testCase";
	
	/** The test step tag name. */
	private static String testStepTagName="testStep";
	
	/** The test case attribute name. */
	private static String testCaseAttributeName="report.xml";
	
	/** The test step attribute name. */
	private static String testStepAttributeName="report.xml";
	
	/** The result tag name. */
	private static String resultTagName="result";
	
	/** The response tag name. */
	private static String responseTagName="response";
	

    /**
     * Instantiates a new test publisher.
     */
    private TestPublisher() {
    }

    /**
     * Adapt test.
     *
     * @param test the test
     * @param suite the suite
     * @param context the context
     * @param projection the projection
     */
    static void adaptTest(
            final Test test, final TestSuite suite, final Context context, final TestProjection projection) {
    	final IndentedDocument document = context.createHtmlDocument(
                "../resources/smartesting.css", key("test.mainTitle"), test.getName());
        process(
                document, br(), br(), table(
                        attributes(
                                width("80%"),
                                height("100%"),
                                align("center"),
                                cellpadding("0"),
                                cellspacing("0"),
                                klass("title")),
                        titleRow(context.getI18N(key("test.title"), test.getName()), "../resources/"),
                        row(
                                cell(
                                        attributes(colspan("3"), valign("top")), br(), table(
                                                attributes(width("100%"), align("center")),
                                                row(cell(br(), createBackToTestSuiteTable(context, suite.getName()))),
                                                row(cell(br(), createPrerequisiteTable(test, context, projection))), 
                                                row(cell(br(), createSynthesisTable(test, context, projection))),
                                                row(cell(br(), createTagCoverageTable(test, context)))
                                                )))));
        context.writeDocumentToFile(document, context.getTestPath(suite, test));
    }

    /**
     * Creates the back to test suite table.
     *
     * @param context the context
     * @param suiteName the suite name
     * @return the html action
     */
    private static HtmlAction createBackToTestSuiteTable(final Context context, final String suiteName) {
        return table(
                attributes(width("95%"), align("center")),
                row(cell(link("index.html", context.getI18N(key("test.backToTestsuite"), suiteName)))));
    }

    /**
     * Creates the prerequisite table.
     *
     * @param test the test
     * @param context the context
     * @param projection the projection
     * @return the html action
     */
    private static HtmlAction createPrerequisiteTable(
            final Test test, final Context context, final TestProjection projection) {
        final Iterable<Prerequisite> prerequisites = extractPrerequisites(test.getSteps(), projection, true);
        if (isEmpty(prerequisites)) {
            return br();
        }
        return table(
                attributes(width("95%"), align("center"), klass("synthesis")),
                row(
                        cell(attributes(klass("titleSynthesis")))),
                processPrerequisitesAsRow(prerequisites));
        
    }

    /**
     * Creates the synthesis table.
     *
     * @param test the test
     * @param context the context
     * @param projection the projection
     * @return the html action
     */
    private static HtmlAction createSynthesisTable(
            final Test test, final Context context, final TestProjection projection) {
    	LOGGER.info("createSynthesisTable method Execution");
        return table(
                attributes(width("95%"), align("center"), klass("synthesis")),
                row(
                        cell(attributes(klass("titleSynthesis")), text(context.getI18N(key("test.steps")))),
                        cell(attributes(klass("titleSynthesis")), text(context.getI18N(key("test.actions")))),
                        cell(attributes(klass("titleSynthesis")), text(context.getI18N(key("test.tags")))),
                        /** Adding Result and Response Columns to the table */
                        cell(attributes(klass("titleSynthesis")), text("Result")),
                        cell(attributes(klass("titleSynthesis")), text("Response"))),
                 /** Adding test rows*/   
                processTestAsRows(test, context, projection));
    }

    /**
     * Creates the tag coverage table.
     *
     * @param test the test
     * @param context the context
     * @return the html action
     */
    private static HtmlAction createTagCoverageTable(final Test test, final Context context) {
        final Multimap<String, String> tags = computeReachedTagsNotInSteps(test);
        if (tags.isEmpty()) {
            return br();
        }
        return table(
                attributes(width("95%"), align("center"), klass("synthesis")),
                row(
                        cell(
                                attributes(klass("titleSynthesis"), colspan("2")),
                                text(context.getI18N(key("test.additionalTags"), tags.size())))), 
                                
                             
                processTagCoverage(tags));
    }

    /**
     * Compute reached tags not in steps.
     *
     * @param test the test
     * @return the multimap
     */
    private static Multimap<String, String> computeReachedTagsNotInSteps(final Test test) {
        final Multimap<String, String> tags = TreeMultimap.create(defaultTagTypeComparator(), natural());
        for (final Tag tag : test.getTags()) {
            tags.put(tag.getType(), tag.getName());
        }
        for (final Step step : test.getSteps()) {
            for (final Tag tag : step.getAction().getTags()) {
                tags.remove(tag.getType(), tag.getName());
            }
        }
        return tags;
    }

    /**
     * Process tag coverage.
     *
     * @param tags the tags
     * @return the html action
     */
    private static HtmlAction processTagCoverage(final Multimap<String, String> tags) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                int line = 0;
                for (final String tagType : tags.keySet()) {
                    process(
                            document, row(
                                    cell(operationKlass(line), bold(text(tagType))),
                                    cell(operationKlass(line), processNames(tags.get(tagType))))
                                   );
                    line++;
                }
            }
            

            private HtmlAction processNames(final Iterable<String> names) {
                return new HtmlAction() {
                    public void perform(final IndentedDocument document) {
                        for (final CharSequence tagName : names) {
                        	process(document, text(tagName), br());
                        }
                    }
                };
            }
        };
    }

    /**
     * Process prerequisites as row.
     *
     * @param prerequisites the prerequisites
     * @return the html action
     */
    private static HtmlAction processPrerequisitesAsRow(
            final Iterable<Prerequisite> prerequisites) {
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                for (final Prerequisite prerequisite : prerequisites) {
                	process(
                            document, row(
                            		cell(klass("stepDetailsActions"), text(prerequisite.getKey())),
                                    cell(klass("stepDetailsActions"), text(prerequisite.getValue()))
                                  
                            		));
                }
            }
        };
    }

    /**
     * Process test as rows.
     *
     * @param test the test
     * @param context the context
     * @param projection the projection
     * @return the html action
     */
    private static HtmlAction processTestAsRows(
            final Test test, final Context context, final TestProjection projection) {
    	LOGGER.info("Execution et affichage des test comme des lignes (TestAsRows)");
        return new HtmlAction() {
            public void perform(final IndentedDocument document) {
                int indexStep = 0;
                for (final Step step : test.getSteps()) {
                	for (final Iterable<StepDescription> group : extractGroupOfSteps(step, projection)) {
                    	final int nbObs = getNumberOfObservations(group);
                        try {
							processAction(document, indexStep, step, Iterables.get(group, 0).getAction(), nbObs);
						} catch (ParserConfigurationException e) {
						} catch (SAXException e) {
						} catch (IOException e) {
						}
                        int indexObs = 1;
                        for (final StepDescription stepDescription : group) {
                            if (!stepDescription.getObservation().isEmpty()) {
                                processObservation(document, indexStep, indexObs, stepDescription.getObservation());
                                indexObs++;
                            }
                        }
                        indexStep++;
                    }
                }
                
            }

            private void processAction(
                    final IndentedDocument document,
                    final int indexStep,
                    final Step step, final String description,
                    final int nbObs) throws ParserConfigurationException, SAXException, IOException {
            	
            	int length = test.getName().length();
            	/** Gets testCase Id which is the 4 last chars of the test case name*/
            	String testCaseID = test.getName().substring(length-4,length);
            	LOGGER.info("Test Case Id = " + testCaseID);
            	String testStepID="";
            	for (Tag tag : step.getAction().getTags()){
            		/** Gets test step Id which is part of the value of the context of tag "<tag>" for every step*/
            		testStepID = tag.getContext().substring(tag.getContext().lastIndexOf(':')+1);
            		LOGGER.info("Test Step name = " + testStepID);
            		/** Gets response and result with printBetweenTags method*/
            		result= ResultsFileParser.printBetweenTags(resultFilePath, testCaseTagName,testStepTagName,resultTagName,testCaseAttributeName,testStepAttributeName,testCaseID, testStepID);
            		response = ResultsFileParser.printBetweenTags(resultFilePath, testCaseTagName,testStepTagName,responseTagName,testCaseAttributeName,testStepAttributeName,testCaseID, testStepID);
                	LOGGER.info("Retrieved values of result and response");
        		}
            	
            	process(
                        document, row(
                                cell(
                                        klass("stepDetailsStep"),
                                        text(context.getI18N(key("test.step"), indexStep + 1)),
                                        br(),
                                        italic('(' + step.getAction().getSelf().getName() + ')')), 
                             cell(
                                        operationKlass(indexStep), table(
                                                attributes(width("100%"), border("0"), klass("stepDetails")), row(
                                                        cell(
                                                                klass("stepDetailsOperation"),
                                                                italic(step.getAction().getOperation().getName()),
                                                                br(), br())), row(
                                                        cell(
                                                                klass("stepDetailsActions"), text(toHtml(description)),
                                                                br(), br())))), 
                                 cell(
                                        attributes(operationKlass(indexStep), rowspan("" + (nbObs + 1))),
                                        renderAssociatedTags(step), spaces(1)),
                                /** Adds a cell with a value of the result variable retrieved before*/        
                        		cell(
                                        attributes(operationKlass(indexStep+100000), rowspan("" + (nbObs+1))),
                                        text(toHtml(result)), spaces(1)),
                               /** Adds a cell with a value of the response variable retrieved before*/  
                                cell(
                                                attributes(operationKlass(indexStep+100000), rowspan("" + (nbObs+1))),
                                                text(toHtml(response)), spaces(1))
                                                
                                         
                        		));
                LOGGER.info("Added Result and Response values to the Publisher Table");
                
            }

            private void processObservation(
                    final IndentedDocument document,
                    final int indexStep,
                    final int indexObs,
                    final String observationDescription) {
                process(
                        document, row(
                                cell(
                                        klass("stepDetailsObs"), listItem(
                                                klass("observationOperation"),
                                                text("" + (indexStep + 1) + '.' + indexObs))),
                                cell(observationKlass(indexStep), text(toHtml(observationDescription)))));
               
            }

            private HtmlAction renderAssociatedTags(final Step step) {
            	
                return new HtmlAction() {
                	 public void perform(final IndentedDocument document) {
                		final Multimap<String, Tag> tags = groupTags(step.getAction().getTags());
                        for (final String tagType : tags.keySet()) {
                        	process(document, text(tagType));
                            boolean first = true;
                            for (final Tag tag : tags.get(tagType)) {
                                if (first) {
                                	process(document, spaces(4), text(tag.getName()), br());
                                	first = false;
                                } else {
                                	process(document, spaces(6 + tagType.length()), text(tag.getName()), br());
                                	
                                }
                            }
                            process(document, br());
                        }
                    }
                };
            }
            
            
        };
    }

    /**
     * Gets the number of observations.
     *
     * @param stepDescriptions the step descriptions
     * @return the number of observations
     */
    private static int getNumberOfObservations(
            final Iterable<StepDescription> stepDescriptions) {
        int nbObs = 0;
        for (final StepDescription stepDescription : stepDescriptions) {
            if (!stepDescription.getObservation().isEmpty()) {
                nbObs++;
            }
        }
        return nbObs;
    }

    /**
     * Operation klass.
     *
     * @param index the index
     * @return the tag attribute
     */
    private static TagAttribute operationKlass(final int index) {
        return klass((index % 2 == 0 ? "even" : "odd") + "Call");
    }

    /**
     * Observation klass.
     *
     * @param index the index
     * @return the tag attribute
     */
    private static TagAttribute observationKlass(final int index) {
        return klass((index % 2 == 0 ? "even" : "odd") + "ObservationOperation");
    }

	/**
	 * Gets the result file path.
	 *
	 * @return the result file path
	 */
	public String getResultFilePath() {
		return resultFilePath;
	}

	/**
	 * Sets the result file path.
	 *
	 * @param resultFilePath the new result file path
	 */
	public static void setResultFilePath(String resultFilePath) {
		TestPublisher.resultFilePath = resultFilePath;
	}
  
}
