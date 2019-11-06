# Language Agents - Rational Speech Act simulations

This simulation software was designed for Blokpoel, Dingemanse, Kachergis, Bögels, Toni and van Rooij, (under review).
It builds on the Rational Speech Act (RSA) theory by Frank & Goodman (2012). The simulations and
analyses are intended to investigate the effects of lexicon ambiguity, order of pragmatic reasoning and asymmetry
between agents on the agent's ability to successfully communicate. The software can also be used to create your own
simulations based on RSA.

There are several use-cases to explore. While the documentation has been written to be self-contained, it is
recommended to read the main paper and its supplementary information as well.

1. Tutorial 1 and 2: The RSA implementation and the simulation experiments
1. Running the simulations
1. Performing data analysis
1. Extending the simulation 

 ## Tutorial 1 and 2: The RSA implementation and the simulation experiments 
 These tutorials are recommended to be followed in order. They explain how we have implemented RSA theory and
 the experiments reported in the main paper in Scala. The easiest way to use these Jupyter notebooks is via Docker.
 
 Visit the [Docker website](https://www.docker.com/get-started), register for a free account and download the Docker
 Desktop client. After installation, login to Docker Desktop using your account. If you have already installed Docker
 you can skip the installation. You can now start an instance of Jupyter Notebooks with support for the Scala
 programming language. Open a terminal or Cmd-prompt and execute the following command:
 
 ```$ docker run -it --rm -p 8888:8888 almondsh/almond:0.8.2```
  
_The notebooks should work with any version later than 0.8.2, but note that using ```:latest``` grabs the 0.5 version._

After Docker has finished downloading the necessary files, copy-paste the URL it reports into a browser. You will be
greeted with a file explorer and now need to upload the ```.ipynb``` notebook files. Download the tutorials here:

1. [Tutorial 1: The RSA implementation](./notebooks/rsa-tutorial-part1.ipynb)
1. [Tutorial 2: The simulation experiments](./notebooks/rsa-tutorial-part2.ipynb)

You can then open and use the tutorials. For more information on using Jupyter notebooks see
[here](https://jupyter-notebook.readthedocs.io/en/stable/).

## Running the simulations
The easiest way to run the simulations would be to use the provided Jupyter notebooks and the installation instructions
from the tutorials. This way, you do not need to install the three necessary components (Scala, Java and Apache Spark)
manually on your system. Follow the installation instructions and grab the following notebooks:

1. [Uniform Simulation Experiment](./notebooks/uniform-experiment.ipynb) (from the main paper)
1. [Random Simulation Experiment](./notebooks/random-experiment.ipynb) (from the supplementary information)
1. [Structured Simulation Experiment](./notebooks/structured-experiment.ipynb) (from the supplementary information)

Alternatively, you can install Scala, Java and Apache Spark on your own system (or compute cluster) and run the
software either using the binary from the command line or from the IntelliJ IDE. You can find instructions for this
advanced method [here](./documentation/ADVANCED.md).

## Performing data analysis
The R-notebook provides detailed description of the data analysis procedures. You can view the pre-compiled notebook
[here](https://htmlpreview.github.io/?https://github.com/markblokpoel/lanag-ambiguityhelps/blob/master/src/main/r/analysis-rnotebook-final.html)
or you can [download the notebook](./src/main/r/analysis-rnotebook.Rmd) for use in R-studio. For the analysis you can
either download the datasets used in Blokpoel et al.(under review) [here](./datasets) or generate your own data by
running a simulation.

## Extending the simulation
To extend the simulation it is recommended first to follow the tutorials to get basic understanding of the
implementation. Then follow the instructions for installing Java, Scala, Apache Spark and the IntelliJ IDE in the
advanced readme [here](./documentation/ADVANCED.md). You can checkout our source code and expand on it (instructions
under advanced) or you can import the project as a library using the Scala Build Tool (sbt). Include the following
lines to your```build.sbt`` file:

```
libraryDependencies += "com.markblokpoel" %% "lanag-core" % "0.3.6"

libraryDependencies += "com.markblokpoel" %% "lanag-ambiguityhelps" % "0.X.X"

```