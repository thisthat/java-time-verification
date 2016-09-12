library(plyr)

csv <- read.csv("result_0_EvalTopFile.csv", sep = ";", header = T)
n_lines <- nrow(csv)
csv_only_meaningfuldata <- csv[csv$total > 0,]
csv_only_sync <- csv_only_meaningfuldata[csv_only_meaningfuldata$number.sync > 0,]
csv_only_time <- csv_only_meaningfuldata[csv_only_meaningfuldata$time.constraint > 0,]
n_elements_with_time_and_sync <- nrow( csv[ csv$time.constraint>0 & csv$number.sync > 0 ,] )

tmp <- within(csv_only_sync, class_out <- paste(package.out, class.name.out, sep='.'))
tmp <- within(tmp, class_in <- paste(package.in, class.name.in, sep='.'))
list_classes_sync <- subset( tmp, select = -c( package.out, class.name.out, method.name.out, package.in, class.name.in, method.name.in, total, time.constraint, number.sync, X ) )
count_list_classes_sync <- count(list_classes_sync)

tmp <- within(csv_only_time, class_out <- paste(package.out, class.name.out, sep='.'))
tmp <- within(tmp, class_in <- paste(package.in, class.name.in, sep='.'))
list_classes_time <- subset( tmp, select = -c( package.out, class.name.out, method.name.out, package.in, class.name.in, method.name.in, total, time.constraint, number.sync, X ) )
count_list_classes_time <- count(list_classes_time)
