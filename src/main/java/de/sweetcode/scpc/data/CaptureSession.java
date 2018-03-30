package de.sweetcode.scpc.data;

import java.util.*;

/**
 * The CaptureSession represents one session worth of data.
 */
public class CaptureSession {

    private long sessionId;

    private GPUInformation gpuInformation = new GPUInformation();
    private GameInformation gameInformation = new GameInformation("", "");

    private List<DataPoint> dataPoints = new LinkedList<>();

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

    public GameInformation getGameInformation() {
        return this.gameInformation;
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
        this.notifyListeners(gpuInformation);
    }

    public void setGameInformation(GameInformation gameInformation) {
        this.gameInformation = gameInformation;
        this.notifyListeners(gameInformation);
    }

    /**
     * Adds a new data point and calls all associated listeners.
     * @param dataPoint The data point.
     */
    public void add(DataPoint dataPoint) {
        this.dataPoints.add(dataPoint);
        this.notifyListeners(dataPoint);
        this.notifyListeners(dataPoint.getGameState());
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

    private <T> void notifyListeners(T data) {

        Class clazz = data.getClass();

        //@HACKY: This is for enums that implement an interface like (GameStates).
        if(data instanceof Enum<?>) {
            clazz = data.getClass().getSuperclass().getInterfaces()[0];
        }

        if(this.listeners.containsKey(clazz)) {
            this.listeners.get(clazz).forEach(e -> e.captured(data));
        }
    }

    public interface Listener<T> {
        void captured(T data );
    }

}
