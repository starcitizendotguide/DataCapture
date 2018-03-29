package de.sweetcode.scpc.data;

import java.util.*;

/**
 * The CaptureSession represents one session worth of data.
 */
public class CaptureSession {

    private long sessionId;

    private List<DataPoint> dataPoints = new LinkedList<>();
    private GPUInformation gpuInformation;

    private Map<Class, List<Listener>> listeners = new HashMap<>();

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

    public DataPoint get(int index) {
        return this.dataPoints.get(index);
    }

    /**
     * All data points.
     * @return A LinkedList, never null, but can be empty.
     */
    public List<DataPoint> getDataPoints() {
        return this.dataPoints;
    }

    public GPUInformation getGPUInformation() {
        return this.gpuInformation;
    }

    /**
     * Sets the session id of the session.
     * @param sessionId The session id.
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void setGPUInformation(GPUInformation gpuInformation) {
        this.gpuInformation = gpuInformation;

        if(this.listeners.containsKey(GPUInformation.class)) {
            this.listeners.get(GPUInformation.class).forEach(e -> e.captured(gpuInformation));
        }
    }

    /**
     * Adds a listener, called when a new DataPoint gets added to the session.
     * @param listener
     */
    public <T> void addListener(Class<T> type, Listener<T> listener) {
        if(!(this.listeners.containsKey(type))) {
            this.listeners.put(type, new LinkedList<>());
        }
        this.listeners.get(type).add(listener);
    }

    /**
     * Adds a new data point and calls all associated listeners.
     * @param dataPoint The data point.
     */
    public void add(DataPoint dataPoint) {
        this.dataPoints.add(dataPoint);
        if(this.listeners.containsKey(DataPoint.class)) {
            this.listeners.get(DataPoint.class).forEach(e -> e.captured(dataPoint));
        }
        if(this.listeners.containsKey(GameState.class)) {
            this.listeners.get(GameState.class).forEach(e -> e.captured(dataPoint.getGameState()));
        }
    }

    public interface Listener<T> {
        void captured(T data );
    }

}
