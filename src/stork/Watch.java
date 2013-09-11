package stork;

// A class for keeping track of time in a way that it is guaranteed to
// be monotonically increasing even in the face of system time changes
// and also provide a meaningful time. Also provides a stopwatch
// mechanism to measure elapsed time.

public class Watch {
  public long start_time = -1, end_time = -1;

  private static long abs_base = System.currentTimeMillis();
  private static long rel_base = System.nanoTime();

  // Create an unstarted watch.
  public Watch() {
    this(-1, -1);
  }

  // Create a watch, and start it now if started is true.
  public Watch(boolean start) {
    this(start ? now() : -1);
  }

  // Create a watch with a given start time.
  public Watch(long start_time) {
    this(start_time, -1);
  }

  // Create a watch with both a given start and end time.
  public Watch(long start_time, long end_time) {
    this.start_time = start_time;
    this.end_time = end_time;
  }

  // Get the current time since epoch in milliseconds.
  public static long now() {
    return abs_base + (System.nanoTime() - rel_base);
  }

  // Get the elapsed time since a given time.
  public static long since(long t) {
    return (t < 0) ? 0 : now()-t;
  }

  // Given a duration in ms, return a pretty string representation.
  public static String pretty(long t) {
    if (t < 0) return null;

    long n = t % (long) 1E6,
         i = (t/=(long) 1E6) % 1000,
         s = (t/=1000) % 60,
         m = (t/=60) % 60,
         h = (t/=60) % 24,
         d = t / 24;

    return (d > 0) ? String.format("%dd%02dh%02dm%02ds", d, h, m, s) :
           (h > 0) ? String.format("%dh%02dm%02ds", h, m, s) :
           (m > 0) ? String.format("%dm%02ds", m, s) :
           (s > 0) ? String.format("%d.%02ds", s, i/10) :
           (i > 0) ? String.format("%d.%04ds", s, i/10) :
        	         String.format("%dns", n);
  }

  // Start (or restart) the timer.
  public synchronized Watch start() {
    start_time = now();
    end_time = -1;
    return this;
  }

  // Get either the current time or the total time if ended. Returns 0
  // if not started.
  public synchronized long elapsed() {
    return (end_time >= 0) ? end_time-start_time : since(start_time);
  }
  
  public synchronized double seconds() {
	return (double)elapsed()/1E9;
  }

  // Stop the timer.
  public synchronized long stop() {
    end_time = now();
    return end_time-start_time;
  }

  // Get start and end times. Returns -1 if not started/ended.
  public long startTime() { return start_time; }
  public long endTime()   { return end_time; }

  // Check if the timer is started/stopped.
  public synchronized boolean isStarted() { return start_time != -1; }
  public synchronized boolean isStopped() { return end_time != -1; }

  // Display the elapsed time as a pretty string.
  public String toString() {
    //return pretty(elapsed());
	return String.format("%fs", seconds());
  }
}
