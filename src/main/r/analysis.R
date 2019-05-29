print("Loading/installing libraries")
is_installed <- function(mypkg) is.element(mypkg, installed.packages()[,1])

load_or_install<-function(package_names)
{
  for(package_name in package_names)
  {
    if(!is_installed(package_name))
    {
      install.packages(package_name,repos="http://lib.stat.cmu.edu/R/CRAN")
    }
    library(package_name,character.only=TRUE,quietly=TRUE,verbose=FALSE)
  }
}

load_or_install(c("tidyverse",
                  "Hmisc",
                  "reshape2",
                  "extrafont",
                  "plyr",
                  "scales",
                  "RColorBrewer",
                  "grid",
                  "gtable",
                  "plotly",
                  "utils",
                  "data.table"))
loadfonts()

tre.12 <- element_text(size = 10, family = "LM Roman 10")
tre.14 <- element_text(size = 12, family = "LM Roman 10")
tre.16 <- element_text(size = 14, family = "LM Roman 10", hjust = 0.5)
purpleColors <- c("#261724","#463147","#664F6F","#85709B","#A193C9","#B9BAFA")
greenColors <- c("#1E3140","#145861","#0F8174","#47AA75","#92D069","#EDEF5E")
printsafeColors <- c("#e41a1c","#377eb8","#4daf4a","#984ea3","#ff7f00")

ggplot_theme <- theme(panel.background = element_blank(), axis.line = element_line(colour = "black"),
                      panel.grid.major = element_line(colour = "grey"), panel.grid.minor = element_line(colour = "grey"),
                      axis.text = tre.14, axis.title = tre.16, legend.text = tre.12, legend.title = tre.14,
                      plot.title = tre.16)

dataFolder = "/Users/Mark/Google Drive/Big Question 3/Development/Data/35rounds/uniform2000"
outputFolder = paste0(dataFolder, "/figs/")
outputPrefix = paste0(outputFolder, "uniform2000_")
dir.create(file.path(outputFolder))

csvFiles = list.files(path=dataFolder, pattern="*.csv")

data <- do.call("rbind", lapply(csvFiles, function(fn)
  data.frame(Filename=fn, read.csv(paste0(dataFolder, "/" ,fn))
  )))

data <- data[, !(names(data) %in% c("Filename"))]

data$agent1AmbiguityMean = floor(data$agent1AmbiguityMean)
data$agent2AmbiguityMean = floor(data$agent2AmbiguityMean)
df_binarysub <- data[complete.cases(data) & data$agent1AmbiguityMean>0 & data$agent2AmbiguityMean>0, ]

#
# SANITY CHECK PLOTS
#

# Parameter space log-scale
df_binarysubsum <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry"), summarise, N = length(asymmetry))

pl <- ggplot(df_binarysubsum, aes(y=N, x=asymmetry)) +
  ggplot_theme + theme(panel.grid.major = element_line(colour="gray"), panel.grid.minor = element_blank(), axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
    scale_x_continuous("asymmetry", limits = c(0,1), breaks=pretty_breaks(n=3)) +
    scale_y_log10("N", labels = scales::comma) +
    ggtitle("Parameter space") + geom_bar(stat="identity",fill=printsafeColors[2]) +
    facet_grid(agent2AmbiguityMean ~ agent1AmbiguityMean, labeller=label_bquote(rows=alpha[2] == ~ .(agent2AmbiguityMean)/.(8), cols=alpha[1] == ~ .(agent1AmbiguityMean)/.(8)))
ggsave(plot=pl, width=20, height=20, units = "cm", paste0(outputPrefix, "parameterspace-log.pdf"))

# Consistent lexicon generation analysis
if(grepl("cons", outputPrefix)) {
  df_binarysubsum <- ddply(df_binarysub, c("changeRate", "changeMethod", "asymmetry"), summarise,
                           N = length(asymmetry),
                           meanAmbiguity1 = mean(agent1AmbiguityMean),
                           meanAmbiguity2 = mean(agent2AmbiguityMean))
  df_binarysubsum$meanAmbiguity1 = floor(df_binarysubsum$meanAmbiguity1)
  df_binarysubsum$meanAmbiguity2 = floor(df_binarysubsum$meanAmbiguity2)
  df_binarysubsum <- df_binarysubsum[complete.cases(df_binarysubsum) & df_binarysubsum$meanAmbiguity1>0 & df_binarysubsum$meanAmbiguity2>0, ]
  pl <- ggplot(df_binarysubsum, aes(y=asymmetry, x=changeRate, color=changeMethod, size=N)) +
    ggplot_theme + theme(axis.text.x = element_text(angle=0, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="right") +
    ggtitle("Lexicon generation") +
    scale_x_continuous("change rate", breaks = pretty_breaks(3), limits = c(0,1)) +
    scale_y_continuous("asymmetry", breaks = pretty_breaks(3)) +
    scale_color_discrete("change method") +
    scale_size("number of samples") +
    geom_point(position = position_dodge(width = 0.1)) +
    facet_grid(meanAmbiguity2 ~ meanAmbiguity1, labeller=label_bquote(rows=alpha[2] == ~ .(meanAmbiguity2)/.(8), cols=alpha[1] == ~ .(meanAmbiguity1)/.(8)))
  ggsave(plot=pl, width=20, height=20, units = "cm", paste0(outputPrefix, "lexicon-generation.pdf"))
}

# Structured lexicon generation analysis
if(grepl("struc", outputPrefix)) {
  df_binarysubsum <- ddply(df_binarysub, c("threshold", "representationalChangeRate", "asymmetry"), summarise,
                           N = length(asymmetry),
                           meanAmbiguity1 = mean(agent1AmbiguityMean),
                           meanAmbiguity2 = mean(agent2AmbiguityMean))
  df_binarysubsum$meanAmbiguity1 = floor(df_binarysubsum$meanAmbiguity1)
  df_binarysubsum$meanAmbiguity2 = floor(df_binarysubsum$meanAmbiguity2)
  df_binarysubsum <- df_binarysubsum[complete.cases(df_binarysubsum) & df_binarysubsum$meanAmbiguity1>0 & df_binarysubsum$meanAmbiguity2>0, ]
  pl <- ggplot(df_binarysubsum, aes(y=asymmetry, x=representationalChangeRate, color=factor(threshold), size=N)) +
    ggplot_theme + theme(axis.text.x = element_text(angle=90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="right") +
    ggtitle("Lexicon generation") +
    scale_x_continuous("representational change rate", breaks = pretty_breaks(3), limits = c(0,1)) +
    scale_y_continuous("asymmetry", breaks = pretty_breaks(3)) +
    scale_color_discrete("similarity threshold") +
    scale_size("number of samples") +
    geom_point(alpha=0.5, shape=1) +
    facet_grid(meanAmbiguity2 ~ meanAmbiguity1, labeller=label_bquote(rows=alpha[2] == ~ .(meanAmbiguity2)/.(8), cols=alpha[1] == ~ .(meanAmbiguity1)/.(8)))
  ggsave(plot=pl, width=20, height=20, units = "cm", paste0(outputPrefix, "lexicon-generation.pdf"))
}

# Variance of ambiguity
if(grepl("struc", outputPrefix) || grepl("rand", outputPrefix)) {
  df_binarysubsum <- rbind(data.frame(ag = 1,
                                      amb = df_binarysub$agent1AmbiguityMean,
                                      var = df_binarysub$agent1AmbiguityVar),
                           data.frame(ag = 2,
                                      amb = df_binarysub$agent1AmbiguityMean,
                                      var = df_binarysub$agent2AmbiguityVar))
  df_binarysubsum <- ddply(df_binarysubsum, c("ag", "amb", "var"), summarise,
                           N = length(var))
  df_binarysubsum$varBin <- cut(df_binarysubsum$var,
                                breaks=seq(min(df_binarysubsum$var),max(df_binarysubsum$var),0.125),
                                labels=seq(min(df_binarysubsum$var),max(df_binarysubsum$var)-0.125,0.125),
                                include.lowest = TRUE, right = TRUE)

  pl <- ggplot(df_binarysubsum, aes(x=varBin, y=N, fill=factor(ag))) + ggplot_theme +
    ggtitle("Variance of ambiguity") +
    scale_y_log10("number of samples", labels = scales::comma) +
    scale_x_discrete("ambiguity variance", breaks=pretty_breaks(6)) +
    scale_fill_manual("agent", values = printsafeColors[2:3]) +
    geom_bar(position="dodge", stat="identity") +
    facet_wrap(amb ~., labeller=label_bquote(cols= (alpha[1]==alpha[2]) == ~ .(amb)/.(8)))
  ggsave(plot=pl, width=20, height=20, units = "cm", paste0(outputPrefix, "ambiguity-variance.pdf"))
}

#
# ANALYSIS PLOTS
#

# Communicative success per order of pragmatic inference
df_binarysubsum2 <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry", "agent1Order"), summarise,
                          N    = length(averageSuccess),
                          mean = mean(averageSuccess),
                          sd   = sd(averageSuccess),
                          se   = sd / sqrt(N),
                          lower_ci = mean - qt(1-(0.05/2), N-1) * se,
                          upper_ci = mean + qt(1-(0.05/2), N-1) * se
)
pl1 <- ggplot(df_binarysubsum2, aes(x=asymmetry, y=mean, color=factor(agent1Order))) +
  ggplot_theme + theme(axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  ggtitle("Communicative success") +
  scale_y_continuous("mean success rate", breaks=pretty_breaks(n=3), limits=c(0,1)) +
  scale_x_continuous("asymmetry", breaks = pretty_breaks(3), minor_breaks=NULL, limits=c(0,1)) +
  scale_color_discrete("order") +
  geom_line() +
  facet_grid(agent2AmbiguityMean ~ agent1AmbiguityMean, labeller=label_bquote(rows=alpha[2] == ~ .(agent2AmbiguityMean)/.(8), cols=alpha[1] == ~ .(agent1AmbiguityMean)/.(8)))
ggsave(plot=pl1, width=20, height=20, units = "cm", paste0(outputPrefix, "performance.pdf"))

# Zoomed in results, 3-way interaction
df_binarysubsum3 <- df_binarysubsum2[df_binarysubsum2$agent1AmbiguityMean<=max(df_binarysubsum2$agent1AmbiguityMean)/2 & df_binarysubsum2$agent2AmbiguityMean<=max(df_binarysubsum2$agent2AmbiguityMean)/2, ]
df_binarysubsum3maxima <- ddply(df_binarysubsum3[df_binarysubsum3$agent1Order==0,], c("agent1AmbiguityMean", "agent2AmbiguityMean", "agent1Order"), summarise,
                                maxSuccess = max(mean))

pl1 <- ggplot(df_binarysubsum3, aes(x=asymmetry, y=mean, color=factor(agent1Order))) +
  ggplot_theme + theme(axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  ggtitle("Communicative success") +
  scale_y_continuous("mean success rate", breaks=pretty_breaks(n=3), limits=c(0,1)) +
  scale_x_continuous("asymmetry", breaks = pretty_breaks(3), minor_breaks=NULL, limits=c(0,1)) +
  scale_color_discrete("order") +
  geom_line() +
  geom_hline(data=df_binarysubsum3maxima, aes(yintercept=df_binarysubsum3maxima$maxSuccess), linetype="dashed", color = rep(c(printsafeColors[1]), each = length(df_binarysubsum3maxima$agent1AmbiguityMean))) +
  facet_grid(agent2AmbiguityMean ~ agent1AmbiguityMean, labeller=label_bquote(rows=alpha[2] == ~ .(agent2AmbiguityMean)/.(8), cols=alpha[1] == ~ .(agent1AmbiguityMean)/.(8)))
ggsave(plot=pl1, width=20, height=20, units = "cm", paste0(outputPrefix, "performance-zoom.pdf"))


# Difference in communicative success between order 1 and order 0
df_binary_subsample <- df_binarysub
df_binarysubo0 <- subset(df_binary_subsample, agent1Order==0, select=c(pairId, agent1AmbiguityMean, agent2AmbiguityMean, asymmetry, averageSuccess))
df_binarysubo1 <- subset(df_binary_subsample, agent1Order==1, select=c(pairId, agent1AmbiguityMean, agent2AmbiguityMean, asymmetry, averageSuccess))

df_binary_diff <- within(merge(df_binarysubo1,df_binarysubo0, by="pairId"), {
  diff_success <- averageSuccess.x - averageSuccess.y
  ambiguity1 <- agent1AmbiguityMean.x
  ambiguity2 <- agent2AmbiguityMean.x
  asymmetry <- asymmetry.x
})[, c("pairId","ambiguity1","ambiguity2","asymmetry","diff_success")]

df_binary_diff_sum <- ddply(df_binary_diff, c("ambiguity1", "ambiguity2", "asymmetry"), summarise,
                            N    = length(diff_success),
                            mean = mean(diff_success),
                            sd   = sd(diff_success),
                            se   = sd / sqrt(N),
                            lower_ci = mean - qt(1-(0.05/2), N-1) * se,
                            upper_ci = mean + qt(1-(0.05/2), N-1) * se
)
pl1 <- ggplot(df_binary_diff_sum, aes(x=asymmetry, y=mean)) +
  ggplot_theme + theme(panel.grid.major = element_line(colour="gray"), panel.grid.minor = element_blank(), axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  ggtitle("First order agents's communicative success beyond zero order") +
  scale_y_continuous("mean success rate (order 1 - order 0)", breaks=pretty_breaks(n=3)) +
  scale_x_continuous("asymmetry", breaks = pretty_breaks(3), minor_breaks=NULL, limits = c(0,1)) +
  geom_point(size=0.25, color=printsafeColors[4]) +
  facet_grid(ambiguity2 ~ ambiguity1, labeller=label_bquote(rows=alpha[2] == ~ .(ambiguity2)/.(8), cols=alpha[1] == ~ .(ambiguity1)/.(8)))
ggsave(plot=pl1, width=20, height=20, units = "cm", paste0(outputPrefix, "performance-diff.pdf"))


# Ambiguity and asymmetry constraints
m = 8
a1s <- 1:m
a2s <- 1:m
minmaxdf <- data.frame(matrix(ncol = 5, nrow = 0))
x <- c("ambiguity1", "ambiguity2", "mina", "meana", "maxa")
colnames(minmaxdf) <- x

for(a1 in a1s) {
  for(a2 in a2s) {
    maxAsymmetry <- (min(a1,m-a2)+min(a2,m-a1))/m
    meanAsymmetry <- a1/m + a2/m - 2*a1*a2/(m*m)
    minAsymmetry <- 1-(min(a1,a2) + min(m-a1,m-a2))/m
    
    newRow <- data.frame(agent1AmbiguityMean=a1,agent2AmbiguityMean=a2,mina=minAsymmetry,meana=meanAsymmetry,maxa=maxAsymmetry)
    minmaxdf <- rbind(minmaxdf, newRow)
  }
}

df_binarysubsum <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry"), summarise, N = log(length(asymmetry)))

pl1 <- ggplot(minmaxdf, aes(x=factor(agent2AmbiguityMean), y=meana)) +
  ggplot_theme + theme(panel.grid.major = element_line(colour="gray"), panel.grid.minor = element_blank(), axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  ggtitle("Distribution of ambiguity and asymmetry in Experiment 1") +
  scale_y_continuous("asymmetry", breaks=pretty_breaks(n=3)) +
  scale_x_discrete(expression(alpha[2]), breaks = c(2,4,6,8), labels=c("2" = expression(2/8), "4" = expression(4/8),"6" = expression(6/8), "8" = expression(8/8))) +
  scale_fill_manual(name="", guide="legend", values=c(printsafeColors[3],printsafeColors[1]), labels=c("theoretical range", "population in Experiment 1")) +
  geom_violin(data = df_binarysubsum, aes(x=factor(df_binarysubsum$agent2AmbiguityMean), y=df_binarysubsum$asymmetry, weight=df_binarysubsum$N, fill=printsafeColors[1]), color=NA) +
  facet_grid(. ~ agent1AmbiguityMean, labeller=label_bquote(cols=alpha[1] == ~ .(agent1AmbiguityMean)/.(8)))

  if(grepl("consistent", dataFolder)) {
    pl1 <- pl1 + geom_crossbar(aes(y=meana, ymin=mina,ymax=maxa, fill=printsafeColors[3]))
  }

ggsave(plot=pl1, width=20, height=10, units = "cm", paste0(outputPrefix, "ambiguity-asymmetry.pdf"))


# Descriptive statistics
df_binarysubsum <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry"), summarise, N = length(asymmetry))
parameterNames <- c("$\\sum n$", "$\\min n$", "$\\max n$", "$\\mean n$", "$\\median n$", "$\\Var n$")
parameterDesc <- c("total population size",
                   "minimum pairs within condition",
                   "maximum pairs within condition",
                   "mean pairs in conditions",
                   "median of number of pairs in conditions",
                   "variance of number of pairs in condition")
parameterValues <- c(sum(df_binarysubsum$N),
                     nmin <- min(df_binarysubsum$N),
                     nmax <- max(df_binarysubsum$N),
                     nmean <- mean(df_binarysubsum$N),
                     nmed <- median(df_binarysubsum$N),
                     nvar <- var(df_binarysubsum$N))
descStats <- data.frame(parameterNames, parameterDesc, parameterValues)
write.table(descStats, paste0(outputPrefix, "descStats.snippet.tex"), quote = FALSE, eol = "\\\\\n", sep = " & ")

