library(plyr)

csv <- read.csv("vuze_second.csv", sep = ";")
package <- csv$package
type <- csv$type
correct <- csv$correct

top5package <- count(package)
top5package <- top5package[order(top5package$freq, decreasing = T),]
top5 <- top5package[1:5,]

par(mai=c(4,1,1,1))
barplot(top5$freq, main = "Top 5 packages", xlab = "", ylab = "freq", names.arg = top5$x, las=2, cex.names= 0.85)


par(mai=c(1,1,1,1))
typeCount <- count(type)
barplot(typeCount$freq, main = "Heuristic Frequency", ylab = "freq", names.arg = typeCount$x)
