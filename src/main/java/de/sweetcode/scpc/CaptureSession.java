package de.sweetcode.scpc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CaptureSession {

    private List<DataPoint> dataPoints = new LinkedList<>();
    private List<CaptureSession.Listener> listeners = new ArrayList<>();

    private long sessionId;

    public CaptureSession() {
        this(-1L);
    }

    public CaptureSession(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public List<DataPoint> getDataPoints() {
        return this.dataPoints;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void add(DataPoint dataPoint) {
        this.dataPoints.add(dataPoint);
        this.listeners.forEach(e -> e.captured(dataPoint));
    }

    public interface Listener {
        void captured(DataPoint dataPoint);
    }

}
