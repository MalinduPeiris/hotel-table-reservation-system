package com.tablebooknow.util;

import com.tablebooknow.model.reservation.Reservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Implements a reservation queue management system with support for
 * priority, filtering, and sorting operations.
 */
public class ReservationQueue {
    private List<Reservation> reservations;
    private Queue<String> pendingQueue;

    /**
     * Default constructor - creates an empty queue
     */
    public ReservationQueue() {
        this.reservations = new ArrayList<>();
        this.pendingQueue = new LinkedList<>();
    }

    /**
     * Clears all reservations from the queue.
     * This method empties both the main reservations list and the pending queue.
     */
    public void clear() {
        this.reservations.clear();
        this.pendingQueue.clear();
    }

    /**
     * Constructor with initial reservations
     * @param reservations List of reservations to initialize the queue
     */
    public ReservationQueue(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
        this.pendingQueue = new LinkedList<>();

        // Initialize the queue with pending reservation IDs
        for (Reservation reservation : reservations) {
            if ("pending".equals(reservation.getStatus())) {
                pendingQueue.add(reservation.getId());
            }
        }
    }

    /**
     * Add a reservation to the queue
     * @param reservation The reservation to add
     */
    public void enqueue(Reservation reservation) {
        reservations.add(reservation);
        if ("pending".equals(reservation.getStatus())) {
            pendingQueue.add(reservation.getId());
        }
    }

    /**
     * Remove the next reservation from the queue
     * @return The next reservation in the queue
     */
    public Reservation dequeue() {
        if (pendingQueue.isEmpty()) {
            return null;
        }

        String nextId = pendingQueue.poll();
        return findReservationById(nextId);
    }

    /**
     * Check if the queue is empty
     * @return true if the queue is empty
     */
    public boolean isEmpty() {
        return pendingQueue.isEmpty();
    }

    /**
     * Get the number of reservations in the queue
     * @return The queue size
     */
    public int size() {
        return pendingQueue.size();
    }

    /**
     * Get all reservations
     * @return List of all reservations
     */
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    /**
     * Find all pending reservations
     * @return List of pending reservations
     */
    public List<Reservation> findPendingReservations() {
        return reservations.stream()
                .filter(r -> "pending".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Peek at the next pending reservation without removing it
     * @return The next pending reservation or null if none
     */
    public Reservation peekNextPending() {
        if (pendingQueue.isEmpty()) {
            return null;
        }

        String nextId = pendingQueue.peek();
        return findReservationById(nextId);
    }

    /**
     * Process the next reservation in the queue (dequeue and mark as confirmed)
     * @return The processed reservation or null if queue is empty
     */
    public Reservation processNextReservation() {
        if (pendingQueue.isEmpty()) {
            return null;
        }

        String nextId = pendingQueue.poll();
        Reservation nextReservation = findReservationById(nextId);

        if (nextReservation != null) {
            nextReservation.setStatus("confirmed");
        }

        return nextReservation;
    }

    /**
     * Remove a reservation from the queue
     * @param reservationId The ID of the reservation to remove
     * @return true if removed, false if not found
     */
    public boolean removeReservation(String reservationId) {
        boolean removed = false;

        // Remove from main list
        removed = reservations.removeIf(r -> r.getId().equals(reservationId));

        // Remove from pending queue if present
        pendingQueue.remove(reservationId);

        return removed;
    }

    /**
     * Find a reservation by ID
     * @param id The reservation ID
     * @return The reservation or null if not found
     */
    public Reservation findReservationById(String id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Filter reservations by status
     * @param status The status to filter by
     * @return List of reservations with the specified status
     */
    public List<Reservation> filterByStatus(String status) {
        return reservations.stream()
                .filter(r -> status.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Prioritize a reservation in the pending queue
     * @param reservationId The ID of the reservation to prioritize
     * @return true if prioritized, false if not found
     */
    public boolean prioritize(String reservationId) {
        // Check if the reservation exists and is pending
        Reservation reservation = findReservationById(reservationId);
        if (reservation == null || !"pending".equals(reservation.getStatus())) {
            return false;
        }

        // Remove from current position in queue if present
        if (pendingQueue.remove(reservationId)) {
            // Add to front of queue
            Queue<String> newQueue = new LinkedList<>();
            newQueue.add(reservationId);
            newQueue.addAll(pendingQueue);
            pendingQueue = newQueue;
            return true;
        }

        return false;
    }

    /**
     * Sort reservations by time (implements merge sort)
     * @return A new ReservationQueue with sorted reservations
     */
    public ReservationQueue sortByTime() {
        List<Reservation> sortedList = mergeSort(reservations, Comparator
                .comparing(Reservation::getReservationDate)
                .thenComparing(Reservation::getReservationTime));

        return new ReservationQueue(sortedList);
    }

    /**
     * Implementation of merge sort algorithm for sorting reservations
     * @param list The list to sort
     * @param comparator The comparator to use for sorting
     * @return A new sorted list
     */
    private <T> List<T> mergeSort(List<T> list, Comparator<T> comparator) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        List<T> left = mergeSort(list.subList(0, mid), comparator);
        List<T> right = mergeSort(list.subList(mid, list.size()), comparator);

        return merge(left, right, comparator);
    }

    /**
     * Merge two sorted lists
     * @param left The left list
     * @param right The right list
     * @param comparator The comparator to use for merging
     * @return A new merged list
     */
    private <T> List<T> merge(List<T> left, List<T> right, Comparator<T> comparator) {
        List<T> result = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (comparator.compare(left.get(leftIndex), right.get(rightIndex)) <= 0) {
                result.add(left.get(leftIndex));
                leftIndex++;
            } else {
                result.add(right.get(rightIndex));
                rightIndex++;
            }
        }

        // Add remaining elements
        while (leftIndex < left.size()) {
            result.add(left.get(leftIndex));
            leftIndex++;
        }

        while (rightIndex < right.size()) {
            result.add(right.get(rightIndex));
            rightIndex++;
        }

        return result;
    }
}