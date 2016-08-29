library(plyr)

csv <- read.csv("result_0_EvalTop5packages.csv", sep = ";", header = T)
x <- max(csv$number.sync)
y <- max(csv$time.constraint)
r <- nrow(csv)
