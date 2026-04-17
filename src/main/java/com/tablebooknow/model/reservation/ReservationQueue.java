package com.tablebooknow.model.reservation;

import com.tablebooknow.model.reservation.Reservation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Queue implementation for managing reservation requests.
 * This implementation uses the FIFO (First In, First Out) principle.
 */
public class ReservationQueue {
    private List<Reservation> queue;

    /**
     * Constructor to initialize an empty queue.
     */
    public ReservationQueue() {
        this.queue = new ArrayList<>();
    }

    /**
     * Adds a reservation to the end of the queue.
     *
     * @param reservation The reservation to add
     */
    public void enqueue(Reservation reservation) {
        queue.add(reservation);
    }

    /**
     * Removes and returns the reservation at the front of the queue.
     *
     * @return The reservation at the front of the queue, or null if the queue is empty
     */
    public Reservation dequeue() {
        if (isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }

    /**
     * Returns the reservation at the front of the queue without removing it.
     *
     * @return The reservation at the front of the queue, or null if the queue is empty
     */
    public Reservation peek() {
        if (isEmpty()) {
            return null;
        }
        return queue.get(0);
    }

    /**
     * Checks if the queue is empty.
     *
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns the number of reservations in the queue.
     *
     * @return The number of reservations in the queue
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns a list of all reservations in the queue.
     *
     * @return A list of all reservations in the queue
     */
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(queue);
    }

    /**
     * Clears all reservations from the queue.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * Sorts the reservations in the queue by their reservation time.
     * Uses merge sort algorithm for sorting.
     */
    public void sortByTime() {
        if (queue.size() <= 1) {
            return;
        }

        List<Reservation> sorted = mergeSort(queue);
        queue.clear();
        queue.addAll(sorted);
    }

    /**
     * Implementation of merge sort algorithm for sorting reservations by time.
     *
     * @param reservations The list of reservations to sort
     * @return A new sorted list of reservations
     */
    private List<Reservation> mergeSort(List<Reservation> reservations) {
        if (reservations.size() <= 1) {
            return reservations;
        }

        int mid = reservations.size() / 2;
        List<Reservation> left = new ArrayList<>(reservations.subList(0, mid));
        List<Reservation> right = new ArrayList<>(reservations.subList(mid, reservations.size()));

        left = mergeSort(left);
        right = mergeSort(right);

        return merge(left, right);
    }

    /**
     * Merges two sorted lists of reservations.
     *
     * @param left The left sorted list
     * @param right The right sorted list
     * @return A merged sorted list
     */
    private List<Reservation> merge(List<Reservation> left, List<Reservation> right) {
        List<Reservation> result = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            try {
                Reservation leftRes = left.get(leftIndex);
                Reservation rightRes = right.get(rightIndex);

                // Compare reservation times
                LocalTime leftTime = LocalTime.parse(leftRes.getReservationTime());
                LocalTime rightTime = LocalTime.parse(rightRes.getReservationTime());

                if (leftTime.isBefore(rightTime) || leftTime.equals(rightTime)) {
                    result.add(leftRes);
                    leftIndex++;
                } else {
                    result.add(rightRes);
                    rightIndex++;
                }
            } catch (Exception e) {
                // In case of parsing error, add both reservations and continue
                if (leftIndex < left.size()) {
                    result.add(left.get(leftIndex));
                    leftIndex++;
                }
                if (rightIndex < right.size()) {
                    result.add(right.get(rightIndex));
                    rightIndex++;
                }
            }
        }

        // Add any remaining elements
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

    /**
     * Find reservations for a specific table and date.
     *
     * @param tableId The table ID to search for
     * @param date The date in YYYY-MM-DD format
     * @return A list of reservations for the specified table and date
     */
    public List<Reservation> findByTableAndDate(String tableId, String date) {
        List<Reservation> result = new ArrayList<>();

        for (Reservation reservation : queue) {
            if (reservation.getTableId() != null &&
                    reservation.getTableId().equals(tableId) &&
                    reservation.getReservationDate() != null &&
                    reservation.getReservationDate().equals(date) &&
                    !reservation.getStatus().equals("cancelled")) {

                result.add(reservation);
            }
        }

        return result;
    }

    /**
     * Check if a table is available at a specific date and time.
     *
     * @param tableId The table ID to check
     * @param date The date in YYYY-MM-DD format
     * @param time The time in HH:MM format
     * @param duration The duration in hours
     * @return true if the table is available, false otherwise
     */
    public boolean isTableAvailable(String tableId, String date, String time, int duration) {
        List<Reservation> tableReservations = findByTableAndDate(tableId, date);

        if (tableReservations.isEmpty()) {
            return true;
        }

        try {
            LocalTime requestedTime = LocalTime.parse(time);
            LocalTime requestedEndTime = requestedTime.plusHours(duration);

            for (Reservation reservation : tableReservations) {
                LocalTime reservationTime = LocalTime.parse(reservation.getReservationTime());
                LocalTime reservationEndTime = reservationTime.plusHours(reservation.getDuration());

                // Check for overlap
                if (requestedTime.isBefore(reservationEndTime) &&
                        reservationTime.isBefore(requestedEndTime)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            // If there's any error, assume the table is not available to be safe
            return false;
        }
    }
}