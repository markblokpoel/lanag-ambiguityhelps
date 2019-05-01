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

# dataFolder = "/Users/Mark/Google Drive/Big Question 3/Development/Data/random2000"
dataFolder = "/Users/Mark/Documents/Development/Scala/lanag-ambiguityhelps/output/rsa1shot_consistent-1556717157906/csv"
outputFolder = paste0(dataFolder, "/figs/")
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
# Parameter space
df_binarysubsum <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry"), summarise,
                        N = length(asymmetry))
ggplot(df_binarysubsum, aes(y=N, x=asymmetry)) +
 ggplot_theme + theme(axis.text.x = element_text(angle=90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
 scale_x_continuous("Asymmetry", breaks=pretty_breaks(2)) +
 scale_y_continuous("N") + geom_bar(stat="identity") +
 facet_grid(agent2AmbiguityMean ~ agent1AmbiguityMean, labeller=label_bquote(rows=alpha[L] ~ .(agent2AmbiguityMean)/.(8), cols=alpha[C] ~ .(agent1AmbiguityMean)/.(8)))

# Parameter space log-scale
df_binarysubsum <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry"), summarise, N = length(asymmetry))

ggplot(df_binarysubsum, aes(y=N, x=asymmetry)) +
  ggplot_theme + theme(axis.text.x = element_text(angle=0, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  scale_x_continuous("Asymmetry") +
 scale_y_log10("N") +
 ggtitle("Speaker vs listener ambiguity") + geom_bar(stat="identity",fill=printsafeColors[1]) +
 facet_grid(agent2AmbiguityMean ~ agent1AmbiguityMean, labeller=label_bquote(rows=alpha[L] ~ .(agent2AmbiguityMean)/.(8), cols=alpha[C] ~ .(agent1AmbiguityMean)/.(8)))

# Consistent lexicon generation analysis
df_binarysubsum <- ddply(df_binarysub, c("changeRate", "changeMethod", "asymmetry"), summarise,
                         N = length(asymmetry),
                         meanAmbiguity1 = mean(agent1AmbiguityMean),
                         meanAmbiguity2 = mean(agent2AmbiguityMean))

ggplot(df_binarysubsum, aes(y=asymmetry, x=changeRate, color=changeMethod)) +
  ggplot_theme + theme(axis.text.x = element_text(angle=0, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  geom_point(size=0.5, position = position_dodge(width = 0.1))

# Structured lexicon generation analysis
df_binarysubsum <- ddply(df_binarysub, c("threshold", "representationalChangeRate", "asymmetry"), summarise,
                         N = length(asymmetry),
                         meanAmbiguity1 = mean(agent1AmbiguityMean),
                         meanAmbiguity2 = mean(agent2AmbiguityMean))
ggplot(df_binarysubsum, aes(y=asymmetry, x=representationalChangeRate, color=threshold)) +
  ggplot_theme + theme(axis.text.x = element_text(angle=0, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  geom_point(size=0.5, position = position_dodge(width = 0.1))

df_binarysubsum <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent1AmbiguityVar"), summarise,
                         N = length(agent1AmbiguityVar))
ggplot(df_binarysubsum, aes(x=agent1AmbiguityVar, y=N)) +
  geom_point() + scale_y_log10() +
  facet_wrap(agent1AmbiguityMean ~.)


# Success rates
df_binarysubsum <- ddply(df_binarysub, c("averageSuccess", "agent1Order", "asymmetry"), summarise, N = length(averageSuccess))
ggplot(df_binarysubsum, aes(y=averageSuccess, x=asymmetry, color=factor(agent1Order))) +
  ggplot_theme + theme(axis.text.x = element_text(angle=0, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  geom_point(aes(size=0.5+N/max(df_binarysubsum$N))) +
  facet_wrap(agent1Order ~ .)

df_binarysubsum <- ddply(data, c("averageSuccess", "agent1Order"), summarise, N = length(averageSuccess))
ggplot(df_binarysubsum, aes(x=averageSuccess, y=N, fill=factor(agent1Order))) +
geom_bar(stat="identity", position=position_dodge(width=.1)) +
scale_y_log10()

#
# Analysis
#

print("Summarizing data again...")
df_binarysubsum2 <- ddply(df_binarysub, c("agent1AmbiguityMean", "agent2AmbiguityMean", "asymmetry", "agent1Order"), summarise,
                          N    = length(averageSuccess),
                          mean = mean(averageSuccess),
                          sd   = sd(averageSuccess),
                          se   = sd / sqrt(N),
                          lower_ci = mean - qt(1-(0.05/2), N-1) * se,
                          upper_ci = mean + qt(1-(0.05/2), N-1) * se
)
print("Done.")
pl1 <- ggplot(df_binarysubsum2, aes(x=asymmetry, y=mean, color=factor(agent1Order))) +
  ggplot_theme + theme(axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  scale_y_continuous(breaks=pretty_breaks(n=4), limits=c(0,1)) +
  scale_x_continuous("asymmetry",breaks = pretty_breaks(3), minor_breaks=NULL) +
  geom_line() + facet_grid(agent2AmbiguityMean ~ agent1AmbiguityMean)
ggsave(plot=pl1, width=20, height=15, units = "cm", paste0(outputFolder, "performance.png"))
print("Figure written to file.")

print("Summarizing data again...")
df_binary_subsample <- df_binarysub# %>% group_by(ambiguity1, ambiguity2, asymmetry) %>% sample_n(5000)
df_binarysubo0 <- subset(df_binary_subsample, agent1Order==0, select=c(pairId, agent1AmbiguityMean, agent2AmbiguityMean, asymmetry, success))
df_binarysubo1 <- subset(df_binary_subsample, agent1Order==1, select=c(pairId, agent1AmbiguityMean, agent2AmbiguityMean, asymmetry, success))

df_binary_diff <- within(merge(df_binarysubo1,df_binarysubo0, by="pairId"), {
  diff_success <- success.x - success.y
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
print("Done.")


pl1 <- ggplot(df_binary_diff_sum[df_binarysubsum2$agent1AmbiguityMean == df_binarysubsum2$agent2AmbiguityMean,], aes(x=asymmetry, y=mean)) +
  ggplot_theme + theme(panel.grid.major = element_line(colour="gray"), panel.grid.minor = element_blank(), axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  scale_y_continuous("diff mean success order 1 - order 0") +
  scale_x_continuous("asymmetry", limits = c(0,1)) +
  geom_point(size=0.5) + geom_smooth(data = df_binary_diff, aes(y=diff_success), method = "lm") + #geom_ribbon(aes(ymin=lower_ci, ymax=upper_ci), alpha=0.2) +
  facet_grid(. ~ ambiguity1, labeller=label_bquote(rows=alpha[L] ~ .(ambiguity1)/.(8)))
ggsave(plot=pl1, width=20, height=15, units = "cm", paste0(outputFolder, "performance-diff.png"))
print("Figure written to file.")
