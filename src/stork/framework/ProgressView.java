package stork.framework;

import android.graphics.Color;
import android.util.Log;

public class ProgressView {
	public Long job_id;
	public Progress progress; // if progress < 0, stork isn't transferring .
	public EndPoint src, dest;
	public String message;
	public String status;
	
	public static class ByteProgress {
		double total;
		double done;
		double inst;
		double avg;

		// Parse progress.
		public int getProgress() {
			if (total == 0)
				return -1;
			int d = (int) (100 * done / total);
			if (d > 100)
				d = 100;
			if (d < 0)
				d = -1;
			return d;
		}
	}

	public static class Progress {
		public ByteProgress bytes;
	}

	public static class EndPoint {
		public String[] uri;
		public String cred;
	}

	public int getColor() {
		if (this.status.equalsIgnoreCase("Complete"))
			return Color.GREEN;
		if (this.status.equalsIgnoreCase("failed"))
			return Color.RED;
		if (this.status.equalsIgnoreCase("removed"))
			return Color.RED;
		else
			return Color.YELLOW;
	}

	public int getProgress() {
		if (progress != null && progress.bytes != null)
			return progress.bytes.getProgress();
		else
			return -1;
	}
}
