package com.tablebooknow.util;

import com.tablebooknow.model.payment.Payment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Queue implementation for managing payment processing.
 * This implementation uses the FIFO (First In, First Out) principle.
 */
public class PaymentQueue {
    private List<Payment> queue;

    /**
     * Constructor to initialize an empty queue.
     */
    public PaymentQueue() {
        this.queue = new ArrayList<>();
    }

    /**
     * Adds a payment to the end of the queue.
     *
     * @param payment The payment to add
     */
    public void enqueue(Payment payment) {
        queue.add(payment);
    }

    /**
     * Removes and returns the payment at the front of the queue.
     *
     * @return The payment at the front of the queue, or null if the queue is empty
     */
    public Payment dequeue() {
        if (isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }

    /**
     * Returns the payment at the front of the queue without removing it.
     *
     * @return The payment at the front of the queue, or null if the queue is empty
     */
    public Payment peek() {
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
     * Returns the number of payments in the queue.
     *
     * @return The number of payments in the queue
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns a list of all payments in the queue.
     *
     * @return A list of all payments in the queue
     */
    public List<Payment> getAllPayments() {
        return new ArrayList<>(queue);
    }

    /**
     * Clears all payments from the queue.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * Gets all pending payments (status = "PENDING").
     *
     * @return A list of pending payments
     */
    public List<Payment> getPendingPayments() {
        List<Payment> pendingPayments = new ArrayList<>();

        for (Payment payment : queue) {
            if ("PENDING".equals(payment.getStatus())) {
                pendingPayments.add(payment);
            }
        }

        return pendingPayments;
    }

    /**
     * Gets all completed payments (status = "COMPLETED").
     *
     * @return A list of completed payments
     */
    public List<Payment> getCompletedPayments() {
        List<Payment> completedPayments = new ArrayList<>();

        for (Payment payment : queue) {
            if ("COMPLETED".equals(payment.getStatus())) {
                completedPayments.add(payment);
            }
        }

        return completedPayments;
    }

    /**
     * Finds payments for a specific reservation ID.
     *
     * @param reservationId The reservation ID to search for
     * @return A list of payments for the specified reservation
     */
    public List<Payment> findByReservationId(String reservationId) {
        List<Payment> result = new ArrayList<>();

        for (Payment payment : queue) {
            if (payment.getReservationId() != null &&
                    payment.getReservationId().equals(reservationId)) {
                result.add(payment);
            }
        }

        return result;
    }

    /**
     * Finds payments for a specific user ID.
     *
     * @param userId The user ID to search for
     * @return A list of payments for the specified user
     */
    public List<Payment> findByUserId(String userId) {
        List<Payment> result = new ArrayList<>();

        for (Payment payment : queue) {
            if (payment.getUserId() != null &&
                    payment.getUserId().equals(userId)) {
                result.add(payment);
            }
        }

        return result;
    }

    /**
     * Sort payments by creation date.
     */
    public void sortByDate() {
        Collections.sort(queue, Comparator.comparing(Payment::getCreatedAt));
    }

    /**
     * Process pending payments based on a merge sort algorithm.
     * This method can be extended to handle actual payment processing logic.
     * @return The number of processed payments
     */
    public int processPayments() {
        // Get all pending payments
        List<Payment> pendingPayments = getPendingPayments();

        if (pendingPayments.isEmpty()) {
            return 0;
        }

        // Sort by creation date using merge sort
        List<Payment> sortedPayments = mergeSort(pendingPayments);

        int processedCount = 0;

        // Process each payment (in a real system, this would connect to a payment gateway)
        for (Payment payment : sortedPayments) {
            try {
                // Simulated processing logic
                // In a real system, this would make API calls to payment processor
                System.out.println("Processing payment: " + payment.getId());

                // Update payment status
                payment.setStatus("PROCESSING");

                // Simulate success (in reality, this would come from payment gateway)
                boolean success = true;

                if (success) {
                    payment.setStatus("COMPLETED");
                    payment.setCompletedAt(LocalDateTime.now());
                    processedCount++;
                } else {
                    payment.setStatus("FAILED");
                }

                // Update the payment in the queue
                updatePaymentInQueue(payment);

            } catch (Exception e) {
                System.err.println("Error processing payment " + payment.getId() + ": " + e.getMessage());
                payment.setStatus("FAILED");
                updatePaymentInQueue(payment);
            }
        }

        return processedCount;
    }

    /**
     * Updates a payment in the queue.
     * @param payment The payment to update
     */
    private void updatePaymentInQueue(Payment payment) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(payment.getId())) {
                queue.set(i, payment);
                break;
            }
        }
    }

    /**
     * Implementation of merge sort algorithm for sorting payments by creation date.
     * @param payments The list of payments to sort
     * @return A new sorted list of payments
     */
    private List<Payment> mergeSort(List<Payment> payments) {
        if (payments.size() <= 1) {
            return payments;
        }

        int mid = payments.size() / 2;
        List<Payment> left = new ArrayList<>(payments.subList(0, mid));
        List<Payment> right = new ArrayList<>(payments.subList(mid, payments.size()));

        left = mergeSort(left);
        right = mergeSort(right);

        return merge(left, right);
    }

    /**
     * Merges two sorted lists of payments.
     * @param left The left sorted list
     * @param right The right sorted list
     * @return A merged sorted list
     */
    private List<Payment> merge(List<Payment> left, List<Payment> right) {
        List<Payment> result = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            LocalDateTime leftDate = left.get(leftIndex).getCreatedAt();
            LocalDateTime rightDate = right.get(rightIndex).getCreatedAt();

            if (leftDate == null || (rightDate != null && leftDate.isAfter(rightDate))) {
                result.add(right.get(rightIndex));
                rightIndex++;
            } else {
                result.add(left.get(leftIndex));
                leftIndex++;
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
}