package de.sweetcode.scpc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The CaptureSession represents one session worth of data.
 */
public class CaptureSession {

    private long sessionId;

    private List<DataPoint> dataPoints = new LinkedList<>();
    private List<CaptureSession.Listener> listeners = new ArrayList<>();

    /**
     * Creates a CaptureSession with the default id (-1).
     */
    public CaptureSession() {
        this(-1L);
    }

    /**
     * Creates a CaptureSession with the provided sessionId.
     * @param sessionId The session id the captured data belongs to.
     */
    public CaptureSession(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * The associated session id.
     * @return -1, default value, otherwise > 0.
     */
    public long getSessionId() {
        return this.sessionId;
    }

    /**
     * All data points.
     * @return A LinkedList, never null, but can be empty.
     */
    public List<DataPoint> getDataPoints() {
        return this.dataPoints;
    }

    /**
     * Sets the session id of the session.
     * @param sessionId The session id.
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Adds a listener, called when a new DataPoint gets added to the session.
     * @param listener
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * Adds a new data point and calls all associated listeners.
     * @param dataPoint The data point.
     */
    public void add(DataPoint dataPoint) {
        this.dataPoints.add(dataPoint);
        this.listeners.forEach(e -> e.captured(dataPoint));
    }

    public interface Listener {
        /**
         * Called when a new data point got added.
         * @param dataPoint The new data point.
         */
        void captured(DataPoint dataPoint);
    }

}
