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

dataFolder = "/Users/Mark/Google Drive/Big Question 3/Development/Data/consistent200"
outputFolder = paste0(dataFolder, "/figs/")
dir.create(file.path(outputFolder))

csvFiles = list.files(path=dataFolder, pattern="*.csv")

data <- do.call("rbind", lapply(csvFiles, function(fn)
  data.frame(Filename=fn, read.csv(paste0(dataFolder, "/" ,fn))
  )))

data <- data[, !(names(data) %in% c("Filename"))]

df_binarysub <- data[complete.cases(data), ]

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


pl1 <- ggplot(df_binary_diff_sum, aes(x=asymmetry, y=mean)) +
  ggplot_theme + theme(panel.grid.major = element_line(colour="gray"), panel.grid.minor = element_blank(), axis.text.x = element_text(angle=-90, vjust=0.5, color = "black"), axis.text.y = element_text(color="black"), legend.position="bottom") +
  scale_y_continuous("diff mean success order 1 - order 0") +
  scale_x_continuous("asymmetry", limits = c(0,1)) +
  geom_point() + geom_smooth(data = df_binary_diff, aes(y=diff_success), method = "lm") + #geom_ribbon(aes(ymin=lower_ci, ymax=upper_ci), alpha=0.2) +
  facet_grid(ambiguity2 ~ ambiguity1, labeller=label_bquote(rows=alpha[L] ~ .(ambiguity2)/.(8), cols=alpha[C] ~ .(ambiguity1)/.(8)))
ggsave(plot=pl1, width=20, height=15, units = "cm", paste0(outputFolder, "performance-diff.png"))
