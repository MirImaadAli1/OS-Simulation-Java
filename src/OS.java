import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OS implements OS_sim_interface {
    private int nextPid = 0;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final ArrayList<Queue<Integer>> processQueues = new ArrayList<>();
    private final ArrayList<Condition> waitConditions = new ArrayList<>();
    private final ArrayList<Boolean> processorAvailability = new ArrayList<>();
    private final ArrayList<Integer> processAssignment = new ArrayList<>();
    private final ArrayList<Integer> processPriorities = new ArrayList<>();

    public OS() {
        initializeReadyQueue();
    }

    private void initializeReadyQueue() {
        processorAvailability.add(true);
        for (int i = 0; i < 10; i++) {
            processQueues.add(new LinkedList<>());
        }
    }

    @Override
    public void set_number_of_processors(int nProcessors) {
        lock.lock();
        try {
            processorAvailability.clear();
            for (int i = 0; i < nProcessors; i++) {
                processorAvailability.add(true);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int reg(int priority) {
        lock.lock();
        try {
            waitConditions.add(lock.newCondition());
            processPriorities.add(priority);
            processAssignment.add(-1);
            // return nextPid++;
        } finally {
            lock.unlock();
        }
        // System.out.println("Registered process " + ID + " with priority " + priority);
        return nextPid++;
    }

    @Override
    public void start(int ID) {
        lock.lock();
        try {
            // Check if there are available processors
            if (isProcessorAvailable()) {
                if (!assignProcessor(ID)) {
                    System.out.println("Failed to assign processor for process " + ID + ", enqueuing...");
                    awaitAssignment(ID);
                }
            } else {
                // If no processors are available, enqueue the process
                System.out.println("No available processors, enqueuing process " + ID);
                addingProcessToQueue(ID);
                awaitAssignment(ID);
            }
        } finally {
            lock.unlock();
        }
    }
    
    // Helper method to check if there are available processors
    private boolean isProcessorAvailable() {
        for (boolean status : processorAvailability) {
            if (status) {
                return true;
            }
        }
        return false;
    }
    

    @Override
    public void schedule(int ID) {
        lock.lock();
        try {
            System.out.println("Scheduling process of ID: " + ID);
            releaseProcessor(ID);
            signalProcess();
            start(ID);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void terminate(int ID) {
        lock.lock();
        try {
            System.out.println("Terminating process of ID: " + ID);
            releaseProcessor(ID);
            signalProcess();
        } finally {
            lock.unlock();
        }
    }

    private boolean assignProcessor(int ID) {
        lock.lock();
        try {
            for (int i = 0; i < processorAvailability.size(); i++) {
                if (processorAvailability.get(i)) {
                    processorAvailability.set(i, false);
                    processAssignment.set(ID, i);
                    System.out.println("Allocated processor " + i + " to process " + ID);
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void releaseProcessor(int ID) {
        lock.lock();
        try {
            Integer processorIndex = processAssignment.get(ID);
            if (processorIndex != null && processorIndex != -1) {
                processorAvailability.set(processorIndex, true);
                processAssignment.set(ID, -1);
                System.out.println("Freed processor " + processorIndex + " from process " + ID);
            }
        } finally {
            lock.unlock();
        }
    }

    private void addingProcessToQueue(int ID) {
        lock.lock();
        try {
            Integer priority = processPriorities.get(ID);
            if (priority >= processQueues.size()) {
                while (processQueues.size() <= priority) {
                    processQueues.add(new LinkedList<>());
                }
            }
            Queue<Integer> queue = processQueues.get(priority);
            if (queue != null) {
                queue.offer(ID);
                System.out.println("Enqueued process " + ID + " with priority " + priority);
            }
        } finally {
            lock.unlock();
        }
    }

    private void awaitAssignment(int ID) {
        lock.lock();
        try {
            Condition condition = waitConditions.get(ID);
            if (condition != null) {
                while (processAssignment.get(ID) == -1) {
                    condition.await();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    private void signalProcess() {
        lock.lock();
        try {
            for (int priority = 9; priority >= 0; priority--) {
                Queue<Integer> currentQueue = processQueues.get(priority);
                if (currentQueue != null && !currentQueue.isEmpty()) {
                    Integer nextProcessID = currentQueue.poll();
                    if (assignProcessor(nextProcessID)) {
                        Condition processCondition = waitConditions.get(nextProcessID);
                        if (processCondition != null) {
                            processCondition.signal();
                            System.out.println("Signaled process: " + nextProcessID);
                            return;
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
