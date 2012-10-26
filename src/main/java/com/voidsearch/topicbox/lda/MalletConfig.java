package com.voidsearch.topicbox.lda;

import cc.mallet.classify.tui.Csv2Vectors;
import cc.mallet.topics.tui.Vectors2Topics;
import cc.mallet.util.CharSequenceLexer;
import cc.mallet.util.CommandOption;

import java.io.File;
import java.nio.charset.Charset;

/**
 * fork of set of private mallet defaults (from Csv2Vectors.java)
 * TODO : temporary only / replace with set of app-specific configurations
 */

public class MalletConfig {

    static CommandOption.File inputFile = new CommandOption.File
            (Csv2Vectors.class, "input", "FILE", true, null,
                    "The file containing data to be classified, one instance per line", null);

    static CommandOption.File outputFile = new CommandOption.File
            (Csv2Vectors.class, "output", "FILE", true, new File("text.vectors"),
                    "Write the instance list to this file; Using - indicates stdout.", null);

    static CommandOption.String lineRegex = new CommandOption.String
            (Csv2Vectors.class, "line-regex", "REGEX", true, "^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$",
                    "Regular expression containing regex-groups for label, name and data.", null);

    static CommandOption.Integer labelOption = new CommandOption.Integer
            (Csv2Vectors.class, "label", "INTEGER", true, 2,
                    "The index of the group containing the label string.\n" +
                            "   Use 0 to indicate that the label field is not used.", null);

    static CommandOption.Integer nameOption = new CommandOption.Integer
            (Csv2Vectors.class, "name", "INTEGER", true, 1,
                    "The index of the group containing the instance name.\n" +
                            "   Use 0 to indicate that the name field is not used.", null);

    static CommandOption.Integer dataOption = new CommandOption.Integer
            (Csv2Vectors.class, "data", "INTEGER", true, 3,
                    "The index of the group containing the data.", null);

    static CommandOption.File usePipeFromVectorsFile = new CommandOption.File
            (Csv2Vectors.class, "use-pipe-from", "FILE", true, new File("text.vectors"),
                    "Use the pipe and alphabets from a previously created vectors file.\n" +
                            "   Allows the creation, for example, of a test set of vectors that are\n" +
                            "   compatible with a previously created set of training vectors", null);

    static CommandOption.Boolean keepSequence = new CommandOption.Boolean
            (Csv2Vectors.class, "keep-sequence", "[TRUE|FALSE]", false, false,
                    "If true, final data will be a FeatureSequence rather than a FeatureVector.", null);

    static CommandOption.Boolean keepSequenceBigrams = new CommandOption.Boolean
            (Csv2Vectors.class, "keep-sequence-bigrams", "[TRUE|FALSE]", false, false,
                    "If true, final data will be a FeatureSequenceWithBigrams rather than a FeatureVector.", null);

    static CommandOption.Boolean removeStopWords = new CommandOption.Boolean
            (Csv2Vectors.class, "remove-stopwords", "[TRUE|FALSE]", false, false,
                    "If true, remove a default list of common English \"stop words\" from the text.", null);

    static CommandOption.File stoplistFile = new CommandOption.File
            (Csv2Vectors.class, "stoplist-file", "FILE", true, null,
                    "Instead of the default list, read stop words from a file, one per line. Implies --remove-stopwords", null);

    static CommandOption.File extraStopwordsFile = new CommandOption.File
            (Csv2Vectors.class, "extra-stopwords", "FILE", true, null,
                    "Read whitespace-separated words from this file, and add them to either \n" +
                            "   the default English stoplist or the list specified by --stoplist-file.", null);

    static CommandOption.Boolean preserveCase = new CommandOption.Boolean
            (Csv2Vectors.class, "preserve-case", "[TRUE|FALSE]", false, false,
                    "If true, do not force all strings to lowercase.", null);

    static CommandOption.String encoding = new CommandOption.String
            (Csv2Vectors.class, "encoding", "STRING", true, Charset.defaultCharset().displayName(),
                    "Character encoding for input file", null);

    static CommandOption.String tokenRegex = new CommandOption.String
            (Csv2Vectors.class, "token-regex", "REGEX", true, CharSequenceLexer.LEX_ALPHA.toString(),
                    "Regular expression used for tokenization.\n" +
                            "   Example: \"[\\p{L}\\p{N}_]+|[\\p{P}]+\" (unicode letters, numbers and underscore OR all punctuation) ", null);

    static CommandOption.Boolean printOutput = new CommandOption.Boolean
            (Csv2Vectors.class, "print-output", "[TRUE|FALSE]", false, false,
                    "If true, print a representation of the processed data\n" +
                            "   to standard output. This option is intended for debugging.", null);

    static CommandOption.SpacedStrings languageInputFiles = new CommandOption.SpacedStrings
            (Vectors2Topics.class, "language-inputs", "FILENAME [FILENAME ...]", true, null,
                    "Filenames for polylingual topic model. Each language should have its own file, " +
                            "with the same number of instances in each file. If a document is missing in " +
                            "one language, there should be an empty instance.", null);

    static CommandOption.String testingFile = new CommandOption.String
            (Vectors2Topics.class, "testing", "FILENAME", false, null,
                    "The filename from which to read the list of instances for empirical likelihood calculation.  Use - for stdin.  " +
                            "The instances must be FeatureSequence or FeatureSequenceWithBigrams, not FeatureVector", null);

    static CommandOption.String outputModelFilename = new CommandOption.String
            (Vectors2Topics.class, "output-model", "FILENAME", true, null,
                    "The filename in which to write the binary topic model at the end of the iterations.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String inputModelFilename = new CommandOption.String
            (Vectors2Topics.class, "input-model", "FILENAME", true, null,
                    "The filename from which to read the binary topic model to which the --input will be appended, " +
                            "allowing incremental training.  " +
                            "By default this is null, indicating that no file will be read.", null);

    static CommandOption.String inferencerFilename = new CommandOption.String
            (Vectors2Topics.class, "inferencer-filename", "FILENAME", true, null,
                    "A topic inferencer applies a previously trained topic model to new documents.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String evaluatorFilename = new CommandOption.String
            (Vectors2Topics.class, "evaluator-filename", "FILENAME", true, null,
                    "A held-out likelihood evaluator for new documents.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String stateFile = new CommandOption.String
            (Vectors2Topics.class, "output-state", "FILENAME", true, null,
                    "The filename in which to write the Gibbs sampling state after at the end of the iterations.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String topicKeysFile = new CommandOption.String
            (Vectors2Topics.class, "output-topic-keys", "FILENAME", true, null,
                    "The filename in which to write the top words for each topic and any Dirichlet parameters.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String topicWordWeightsFile = new CommandOption.String
            (Vectors2Topics.class, "topic-word-weights-file", "FILENAME", true, null,
                    "The filename in which to write unnormalized weights for every topic and word type.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String wordTopicCountsFile = new CommandOption.String
            (Vectors2Topics.class, "word-topic-counts-file", "FILENAME", true, null,
                    "The filename in which to write a sparse representation of topic-word assignments.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String topicReportXMLFile = new CommandOption.String
            (Vectors2Topics.class, "xml-topic-report", "FILENAME", true, null,
                    "The filename in which to write the top words for each topic and any Dirichlet parameters in XML format.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String topicPhraseReportXMLFile = new CommandOption.String
            (Vectors2Topics.class, "xml-topic-phrase-report", "FILENAME", true, null,
                    "The filename in which to write the top words and phrases for each topic and any Dirichlet parameters in XML format.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.String docTopicsFile = new CommandOption.String
            (Vectors2Topics.class, "output-doc-topics", "FILENAME", true, null,
                    "The filename in which to write the topic proportions per document, at the end of the iterations.  " +
                            "By default this is null, indicating that no file will be written.", null);

    static CommandOption.Double docTopicsThreshold = new CommandOption.Double
            (Vectors2Topics.class, "doc-topics-threshold", "DECIMAL", true, 0.0,
                    "When writing topic proportions per document with --output-doc-topics, " +
                            "do not print topics with proportions less than this threshold value.", null);

    static CommandOption.Integer docTopicsMax = new CommandOption.Integer
            (Vectors2Topics.class, "doc-topics-max", "INTEGER", true, -1,
                    "When writing topic proportions per document with --output-doc-topics, " +
                            "do not print more than INTEGER number of topics.  " +
                            "A negative value indicates that all topics should be printed.", null);

    static CommandOption.Integer numTopics = new CommandOption.Integer
            (Vectors2Topics.class, "num-topics", "INTEGER", true, 10,
                    "The number of topics to fit.", null);

    static CommandOption.Integer numThreads = new CommandOption.Integer
            (Vectors2Topics.class, "num-threads", "INTEGER", true, 1,
                    "The number of threads for parallel training.", null);

    static CommandOption.Integer numIterations = new CommandOption.Integer
            (Vectors2Topics.class, "num-iterations", "INTEGER", true, 1000,
                    "The number of iterations of Gibbs sampling.", null);

    static CommandOption.Integer randomSeed = new CommandOption.Integer
            (Vectors2Topics.class, "random-seed", "INTEGER", true, 0,
                    "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.", null);

    static CommandOption.Integer topWords = new CommandOption.Integer
            (Vectors2Topics.class, "num-top-words", "INTEGER", true, 20,
                    "The number of most probable words to print for each topic after model estimation.", null);

    static CommandOption.Integer showTopicsInterval = new CommandOption.Integer
            (Vectors2Topics.class, "show-topics-interval", "INTEGER", true, 50,
                    "The number of iterations between printing a brief summary of the topics so far.", null);

    static CommandOption.Integer outputModelInterval = new CommandOption.Integer
            (Vectors2Topics.class, "output-model-interval", "INTEGER", true, 0,
                    "The number of iterations between writing the model (and its Gibbs sampling state) to a binary file.  " +
                            "You must also set the --output-model to use this option, whose argument will be the prefix of the filenames.", null);

    static CommandOption.Integer outputStateInterval = new CommandOption.Integer
            (Vectors2Topics.class, "output-state-interval", "INTEGER", true, 0,
                    "The number of iterations between writing the sampling state to a text file.  " +
                            "You must also set the --output-state to use this option, whose argument will be the prefix of the filenames.", null);

    static CommandOption.Integer optimizeInterval = new CommandOption.Integer
            (Vectors2Topics.class, "optimize-interval", "INTEGER", true, 0,
                    "The number of iterations between reestimating dirichlet hyperparameters.", null);

    static CommandOption.Integer optimizeBurnIn = new CommandOption.Integer
            (Vectors2Topics.class, "optimize-burn-in", "INTEGER", true, 200,
                    "The number of iterations to run before first estimating dirichlet hyperparameters.", null);

    static CommandOption.Boolean useSymmetricAlpha = new CommandOption.Boolean
            (Vectors2Topics.class, "use-symmetric-alpha", "true|false", false, false,
                    "Only optimize the concentration parameter of the prior over document-topic distributions. This may reduce the number of very small, poorly estimated topics, but may disperse common words over several topics.", null);

    static CommandOption.Boolean useNgrams = new CommandOption.Boolean
            (Vectors2Topics.class, "use-ngrams", "true|false", false, false,
                    "Rather than using LDA, use Topical-N-Grams, which models phrases.", null);

    static CommandOption.Boolean usePAM = new CommandOption.Boolean
            (Vectors2Topics.class, "use-pam", "true|false", false, false,
                    "Rather than using LDA, use Pachinko Allocation Model, which models topical correlations." +
                            "You cannot do this and also --use-ngrams.", null);

    static CommandOption.Double alpha = new CommandOption.Double
            (Vectors2Topics.class, "alpha", "DECIMAL", true, 50.0,
                    "Alpha parameter: smoothing over topic distribution.", null);

    static CommandOption.Double beta = new CommandOption.Double
            (Vectors2Topics.class, "beta", "DECIMAL", true, 0.01,
                    "Beta parameter: smoothing over unigram distribution.", null);

    static CommandOption.Double gamma = new CommandOption.Double
            (Vectors2Topics.class, "gamma", "DECIMAL", true, 0.01,
                    "Gamma parameter: smoothing over bigram distribution", null);

    static CommandOption.Double delta = new CommandOption.Double
            (Vectors2Topics.class, "delta", "DECIMAL", true, 0.03,
                    "Delta parameter: smoothing over choice of unigram/bigram", null);

    static CommandOption.Double delta1 = new CommandOption.Double
            (Vectors2Topics.class, "delta1", "DECIMAL", true, 0.2,
                    "Topic N-gram smoothing parameter", null);

    static CommandOption.Double delta2 = new CommandOption.Double
            (Vectors2Topics.class, "delta2", "DECIMAL", true, 1000.0,
                    "Topic N-gram smoothing parameter", null);

    static CommandOption.Integer pamNumSupertopics = new CommandOption.Integer
            (Vectors2Topics.class, "pam-num-supertopics", "INTEGER", true, 10,
                    "When using the Pachinko Allocation Model (PAM) set the number of supertopics.  " +
                            "Typically this is about half the number of subtopics, although more may help.", null);

    static CommandOption.Integer pamNumSubtopics = new CommandOption.Integer
            (Vectors2Topics.class, "pam-num-subtopics", "INTEGER", true, 20,
                    "When using the Pachinko Allocation Model (PAM) set the number of subtopics.", null);

}
