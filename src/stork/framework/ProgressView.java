package stork.framework;

public class ProgressView {
	
	private Long jobID;
	private int progress; // if progress < 0, stork isn't transferring .
	private final String server_one;
	private final String server_two;
	private final String message;

	/**
	 * Constructor when there's numerical progress going on
	 * 
	 * @param jobId
	 * @param one
	 * @param progress
	 * @param two
	 */
	public ProgressView(Long jobId, String one, int progress, String two) {
		this.setJobID(jobId);
		this.server_one = one;
		this.server_two = two;
		this.progress = progress;
		this.message = null;
	}

	/**
	 * Consructor when there's a message instead of progress
	 * 
	 * @param jobId
	 * @param one
	 * @param message
	 * @param two
	 */
	public ProgressView(Long jobId, String one, String message, String two) {
		this.setJobID(jobId);
		this.server_one = one;
		this.server_two = two;
		this.progress = -1;
		this.message = message;
	}

	// GETTERS AND SETTERS
	
	public int getProgress() {
		return progress;
	}
	
	public void setProgress(int progress) {
		this.progress= progress;
	}

	public String getServer_one() {
		return server_one;
	}

	public String getServer_two() {
		return server_two;
	}

	public Long getJobID() {
		return jobID;
	}

	public void setJobID(Long jobID) {
		this.jobID = jobID;
	}

	public String getMessage() {
		return message;
	}

}
