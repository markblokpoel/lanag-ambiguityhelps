#!/bin/sh

spark-submit \
  --conf spark.driver.extraJavaOptions=-Dconfig.file=./application.conf \
  --conf spark.executor.extraJavaOptions=-Dconfig.file=./application.conf \
  --class com.markblokpoel.lanag.ambiguityhelps.experiments.structured.StructuredExperiment \
  --master local[*] \
  com.markblokpoel.lanag-ambiguityhelps-assembly-0.1.jar
