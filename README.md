# Detection of Configuration Mismatches


## Tools
### ConfigurationMismatchDetector
The tool we used to detect configuration mismatches is supplied as an Eclipse
project in the `ConfigurationMismatchDetector` folder. The
`de.uni_hildesheim.sse.smell.Main` class contains the main method that launches
the analysis.
The data to run on Linux 4.4.1 x86 is already present in the
`ConfigurationMismatchDetector/input/` folder.
The raw output of a run on Linux 4.4.1 x86 is present in the
`ConfigurationMismatchDetector/output/` folder.

### SpaceLocator
This tool was used to calculate the distribution of spaces in which the Kconfig
variables have been used. The `net.ssehub.configmismatches.spaces.Main` class
contains the main method that launches the analysis.

## Results
The results of our manual analysis of the output from the
ConfigurationMismatchDetector tool can be found in the `results/` folder.
The file `Configuration Mismatch Analysis.xlsx` is the main sheet that
contains our results.

