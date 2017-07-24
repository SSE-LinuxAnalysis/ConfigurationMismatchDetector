package de.uni_hildesheim.sse.smell;

import de.uni_hildesheim.sse.smell.filter.configuration_mismatches.NoDominatingFilter;
import de.uni_hildesheim.sse.smell.filter.configuration_mismatches.PcSmellDetector;
import de.uni_hildesheim.sse.smell.filter.configuration_mismatches.PresenceConditionFinder;
import de.uni_hildesheim.sse.smell.filter.input.ConditionBlockReader;
import de.uni_hildesheim.sse.smell.filter.input.MakeModelExtender;
import de.uni_hildesheim.sse.smell.filter.input.VariableWithSolutionsReader;
import de.uni_hildesheim.sse.smell.filter.output.CsvPrinter;
import de.uni_hildesheim.sse.smell.filter.util.ConfigurationMismatchSolutionAnnotator;
import de.uni_hildesheim.sse.smell.filter.util.RedundantSolutionFilter;

public class Main {

    private static final String INPUT_FOLDER = "input/";
    private static final String OUTPUT_FOLDER = "output/";

    public static void main(String[] args) throws Exception {
        if (null == args || args.length < 3) {
            System.err.println("Useage: Main <path to linux> <linux-version> <arch> [<No. of Threads>]");
            System.err.println("E.g.: Main /data/linux-4.4.1 linux-4.4.1 x86");
        } else {
            int nThreads = 1;
            if (args.length > 3) {
                try {
                    int value = Integer.valueOf(args[3]);
                    if (value > 0) {
                        nThreads = value;
                    } else {
                        System.err.println("Please specify a positive inter value as 4. value, received: " + value);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Please specify a positive inter value as 4. value. Could not parse: "
                        + args[3]);
                }
            }
            configMismatchRun(args[1], args[2], nThreads);
            configMismatchResultPresentation(args[1], args[2], args[0]);
        }
    }

    private static void configMismatchRun(String version, String arch, int nThreads) throws Exception {
        String structure = INPUT_FOLDER + version + "/structure.csv";
        String makemodel = INPUT_FOLDER + version + "/" + arch + ".makemodel.csv";
        String kconfig = INPUT_FOLDER + version + "/" + arch + ".dimacs";
        
        String pcs = OUTPUT_FOLDER + version + "/" + arch + ".pcs.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".config_mismatch.result.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new ConditionBlockReader(structure, true));
        pipeline.addFilter(new MakeModelExtender(makemodel, false));
        
        /* 
         * PresenceConditionFinder: Creates list in form of:
         * A -> List of PCs
         */
        pipeline.addFilter(new PresenceConditionFinder());
        
        pipeline.addFilter(new CsvPrinter(pcs));
        
        /* 
         * NoDominatingFilter: Small filter, checks whether a variable
         * has non dependent PC, which can be toggled directly by this variable
         * in any configuration (or more precisely has no "Feature Effect")
         */
        pipeline.addFilter(new NoDominatingFilter());
        // For the paper, we run this with 32 threads
        pipeline.addFilter(new PcSmellDetector(kconfig, result, nThreads));
        
        // no output needed, because PcSmellDetector already writes it as it finds them
//        pipeline.addFilter(new CsvPrinter(result));
        
        System.out.println("Starting to run for " + version + ", arch " + arch);
        pipeline.run();
    }
    
    /**
     * Improves the result of a configMismatchRun() by adding source locations, prompts, etc. 
     */
    private static void configMismatchResultPresentation(String version, String arch, String linuxTree) throws Exception {
        String rsfFile = INPUT_FOLDER + version + "/" + arch + ".rsf";
        
        String configMismatchResult = OUTPUT_FOLDER + version + "/" + arch + ".config_mismatch.result.csv";
        String result = OUTPUT_FOLDER + version + "/" + arch + ".config_mismatch.analysis_template.csv";
        
        Pipeline pipeline = new Pipeline(new StreamProgressPrinter());
        
        pipeline.addFilter(new VariableWithSolutionsReader(configMismatchResult, true));
        pipeline.addFilter(new RedundantSolutionFilter());
        pipeline.addFilter(new ConfigurationMismatchSolutionAnnotator(linuxTree, rsfFile));
        pipeline.addFilter(new CsvPrinter(result));
        
        pipeline.run();
    }
    
}
