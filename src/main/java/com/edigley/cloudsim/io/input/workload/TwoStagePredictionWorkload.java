package com.edigley.cloudsim.io.input.workload;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Scanner;

import com.edigley.oursim.dispatchableevents.Event;
import com.edigley.oursim.dispatchableevents.EventListener;
import com.edigley.oursim.dispatchableevents.jobevents.JobEventListener;
import com.edigley.oursim.dispatchableevents.taskevents.TaskEventListener;
import com.edigley.oursim.entities.Job;
import com.edigley.oursim.entities.Peer;
import com.edigley.oursim.entities.Task;
import com.edigley.oursim.io.input.workload.Workload;

/**
 * 
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 2019/07/14
 * 
 */
public class TwoStagePredictionWorkload implements Workload, JobEventListener, TaskEventListener {

	private Map<String, Peer> peers;
	private Job nextJob = null;
	private Job nextNextJob = null;
	private Job polledJob = null;
	private Long currentTime = null;
	private Scanner scanner;
	
	private double bidValue;
	
	public TwoStagePredictionWorkload(String workloadFilePath, Map<String, Peer> peers, long startingTime, double bidValue)
			throws FileNotFoundException {
		this.bidValue = bidValue;
		this.peers = peers;
		this.scanner = new Scanner(new BufferedReader(new FileReader(workloadFilePath)));
		
		//skip the first line
		String firstLine = scanner.nextLine();
		String expectedHeader = "taskId time jobId jobSize runtime user peer TraceID Cluster.IAT Cluster.RT";
		if (!firstLine.trim().replaceAll(" +", " ").equalsIgnoreCase(expectedHeader)) {
			throw new RuntimeException("Header incompatible for this Workload. Expected Header: " + expectedHeader);
		}
		
	}

	public boolean merge(Workload other) {
		return false;
	}

	public void close() {
		this.scanner.close();
	}

	public void stop() {
		this.nextJob = null;
		this.scanner.close();
		this.scanner = new Scanner("");
	}

	private Job createJob(String line) {
		// taskId time jobId jobSize runtime user peer
		Scanner scLine = new Scanner(line);

		try {
			long taskID = scLine.nextLong();
			long submissionTime = scLine.nextLong();
			long jobID = scLine.nextLong();
			long jobSize = scLine.nextLong();
			long runTime = scLine.nextLong();
			String userID = scLine.next();
			String siteID = scLine.next();
			
			assert peers.containsKey(siteID) : siteID + " -> " + line;
			assert jobSize > 0;
			
			submissionTime = (currentTime != null) ? currentTime : submissionTime;
			Task task = new Task(taskID, "", runTime, submissionTime, null);
			task.setBidValue(bidValue);

			Job job = new Job(jobID, submissionTime, peers.get(siteID));
			job.addTask(task);
			job.setUserId(userID);

			return job;
		} catch (RuntimeException e) {
			System.err.println("line: " + line);
			throw e;
		} finally {
			scLine.close();			
		}
	}

	public Job poll() {
		this.polledJob = this.peek();
		this.nextJob = null;
		return this.polledJob;
	}
	
	private boolean shouldPrepareNextJob() {
		return (this.polledJob == null || (this.polledJob.isFinished() && currentTime != null && currentTime > this.polledJob.getFinishTime()));
	}
	
	public Job peek() {
		
		if (this.shouldPrepareNextJob() && (this.nextJob == null && scanner.hasNextLine())) {
			Job firstJob = (nextNextJob != null) ? nextNextJob : createJob(scanner.nextLine());
			while (scanner.hasNextLine() && (nextNextJob = createJob(scanner.nextLine())).getId() == firstJob.getId()) {
				Task nextTask = nextNextJob.getTasks().get(0);
				Task taskToNextJob = new Task(nextTask.getId(), "", nextTask.getDuration(), nextTask.getSubmissionTime(), null);
				taskToNextJob.setBidValue(bidValue);
				firstJob.addTask(taskToNextJob);
			}
			this.nextJob = firstJob;
		}
		
		return this.shouldPrepareNextJob() ? this.nextJob : null;
	}
	
	// Job Event Listener methods
	
	@Override
	public void jobFinished(Event<Job> jobEvent) {
		Job job = jobEvent.getSource();
		currentTime = job.getFinishTime() + 1;
		
		//String template = "---> jobFinished.[ jobId, subTime, startTime, makespan ]: [ %s, %s, %s, %s]";
		//System.out.println(String.format(template, job.getId(), job.getSubmissionTime(), job.getStartTime() ,job.getMakeSpan()));
		
		if (nextNextJob != null) {
			Task theOriginalTask = nextNextJob.getTasks().get(0);
			Task task = new Task(theOriginalTask.getId(), "", theOriginalTask.getDuration(), currentTime, null);
			task.setBidValue(bidValue);

			nextNextJob = new Job(nextNextJob.getId(), currentTime, nextNextJob.getSourcePeer());
			nextNextJob.addTask(task);
			nextNextJob.setUserId(nextNextJob.getUserId());
		}

	}
	
	@Override
	public int compareTo(EventListener o) {
		return this.hashCode() - o.hashCode();
	}

	@Override
	public void jobSubmitted(Event<Job> jobEvent) {
		//Job job = jobEvent.getSource();
		//String template = "---> jobSubmitted.[ jobId, subTime, startTime, makespan ]: [ %s, %s, %s, %s]";
		//System.out.println(String.format(template, job.getId(), job.getSubmissionTime(), job.getStartTime() ,job.getMakeSpan()));
	}

	@Override
	public void jobStarted(Event<Job> jobEvent) {
		//Job job = jobEvent.getSource();
		//String template = "---> jobStarted.[ jobId, subTime, startTime, makespan ]: [ %s, %s, %s, %s]";
		//System.out.println(String.format(template, job.getId(), job.getSubmissionTime(), job.getStartTime() ,job.getMakeSpan()));
	}

	@Override
	public void jobPreempted(Event<Job> jobEvent) {
		//System.out.println("---> TwoStagePredictionWorkload.jobPreempted.jobEvent: " + jobEvent);
	}

	// Task Event Listener methods
	
	@Override
	public void taskSubmitted(Event<Task> taskEvent) {
		//long taskId = taskEvent.getSource() != null ? taskEvent.getSource().getId() : -1;
		//long time = /*taskEvent != null ? taskEvent.getTime() :*/ -1;
		//System.out.println("taskSubmitted.[time, id]: [" + time + ", "+ taskId + "]");
	}

	@Override
	public void taskStarted(Event<Task> taskEvent) {
		//Task task = taskEvent.getSource();
		//String template = "---> taskStarted.[ taskId, subTime, startTime, makespan ]: [ %s, %s, %s, %s]";
		//System.out.println(String.format(template, task.getId(), task.getSubmissionTime(), task.getStartTime() ,task.getMakeSpan()));
	}

	@Override
	public void taskFinished(Event<Task> taskEvent) {
		//Task task = taskEvent.getSource();
		//String template = "---> taskFinished.[ taskId, subTime, startTime, makespan ]: [ %s, %s, %s, %s]";
		//System.out.println(String.format(template, task.getId(), task.getSubmissionTime(), task.getStartTime() ,task.getMakeSpan()));
	}

	@Override
	public void taskPreempted(Event<Task> taskEvent) {
	}

	@Override
	public void taskCancelled(Event<Task> taskEvent) {
	}

}
