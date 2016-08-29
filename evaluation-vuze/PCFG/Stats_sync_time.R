library(plyr)

csv <- read.csv("resultIndexSyncnumber.csv", sep = ";", header = T)
csv_sum_greater_zero <- csv[csv$sum>0,]
csv_importing_greater_zero_and_sum_zero <- csv[csv$sum==0 & csv$num.imported > 0,]

aggregate_sum <- ddply(csv_sum_greater_zero, .(package), summarize, Sum=sum(sum))
aggregate_import <- ddply(csv_sum_greater_zero, .(package), summarize, imported=sum(num.imported))

t <- cbind(aggregate_sum[ order( aggregate_sum$package ),] , aggregate_import[order(aggregate_import$package), ])
t <- t[,c(1,2,4)]

top5sum <- t[order( t$Sum , decreasing = T ),][1:5,]
top5import <- t[order( t$imported , decreasing = T ),][1:5,]


