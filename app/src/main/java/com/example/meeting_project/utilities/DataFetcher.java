package com.example.meeting_project.utilities;

import com.example.meeting_project.interfaces.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class DataFetcher implements Runnable {

    private static DataFetcher instance;
    private final List<Subscriber> subscribers = new ArrayList<>();
    private final Thread thread;
    private boolean running = false;

    // Private constructor for Singleton
    private DataFetcher() {
        thread = new Thread(this);
        thread.setDaemon(true); // Optional: allows JVM to exit if only this thread is left
    }

    // Get the singleton instance
    public static synchronized DataFetcher getInstance() {
        if (instance == null) {
            instance = new DataFetcher();
        }
        return instance;
    }

    // Add a subscriber
    public synchronized void subscribe(Subscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    // Remove a subscriber
    public synchronized void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    // Notify all subscribers
    private synchronized void notifySubscribers() {
        for (Subscriber subscriber : subscribers) {
            subscriber.actOnUpdate();
        }
    }

    // Start the background thread
    public void start() {
        if (!running) {
            running = true;
            thread.start();
        }
    }

    // Thread logic
    @Override
    public void run() {
        while (running) {
            notifySubscribers();
            try {
                Thread.sleep(2000); // Wait 2 seconds between updates
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("DataFetcher interrupted");
            }
        }
    }

    // Stop the thread if needed
    public void stop() {
        running = false;
        thread.interrupt();
    }
}

