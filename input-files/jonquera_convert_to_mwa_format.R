library(plyr)

inputFile="/home/edigley/eclipse-workspace/cloudsim/input-files/jonquera_single_core_8_workers_trace.txt"
outputFIle="/home/edigley/eclipse-workspace/cloudsim/input-files/jonquera_single_core_8_workers_trace_mwa.txt"

inputFile="/home/edigley/eclipse-workspace/cloudsim/input-files/jonquera_tac_16_workers_trace.txt"
outputFile="/home/edigley/eclipse-workspace/cloudsim/input-files/jonquera_tac_16_workers_trace_mwa.txt"

ds <- read.table(inputFile, header=T)
ds$taskId <- ds$generation*1000 + ds$individual
ds$time <- 10 * (ds$start)
ds$jobId <- ds$generation

nJobs <- aggregate(individual ~ generation, data=ds, FUN=length)
names(nJobs) <- c("jobId","jobSize")
ds <- merge(ds,nJobs)

ds$runtime <- 10 * (ds$end - ds$start)
ds$user <- 1
ds$peer <- 1
ds$TraceID <- "jonquera-ga-8-cores"
ds$Cluster.IAT <- "C5" 
ds$Cluster.RT <- "C4"

ds <- subset(merge(ds,nJobs),!is.na(time), select=c("taskId","time","jobId", "jobSize", "nCores", "runtime", "user", "peer", "TraceID", "Cluster.IAT", "Cluster.RT"))
ds <- ds[order(ds$taskId),]

write.table(ds, outputFile, col.names=T, row.name=F, quote=F)

getNTasks <- function(nJobs, generation) { 
	nJobs[nJobs$generation==generation,]$individual 
};

#library(dplyr)
#subset(ds,select=c("generation","individual")) %>% group_by(generation, individual) %>% mutate(count=n())
