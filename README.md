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

### Pilztaker Script
The folder `pilztaker` contains the start script for
[pilztaker](https://github.com/SSE-LinuxAnalysis/pilztaker) (a wrapper for
[Undertaker v1.6.1](http://vamos.informatik.uni-erlangen.de/trac/undertaker/)),
which was used to generate the list of conditional code blocks. Please note that
we excluded 7 files from the analysis, which could not be handled by the parser:
* `include/linux/skbuff.h`
* `arch/(sh | sparc | tile | x86 | mips)/include/asm/pgtable-64.h`
* `arch/x86/kernel/espfix_64.c`

## Results
The results of our manual analysis of the output from the
ConfigurationMismatchDetector tool can be found in the `results/` folder. This
folder contains the following results:
* `Configuration Mismatch Analysis.xlsx` is the main sheet that contains the results of our manual analysis.
* `Involved Spaces.txt` is the result of `SpaceLocator` and lists how many Kconfig variables are also used in code and Kbuild files.
* `Conditional Blocks.csv` contains a list of all cpp dependent conditional code blocks.
* The sub folders contain additional raw data.

## Kernel
For the analysis, we used version [4.4.1 of the Linux kernel](https://cdn.kernel.org/pub/linux/kernel/v4.x/linux-4.4.1.tar.xz).

