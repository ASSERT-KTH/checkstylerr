package io.openems.edge.common.taskmanager;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Manages a number of {@link TasksManager}s.
 * 
 * <p>
 * A useful application for MetaTasksManager is to provide a list of Tasks that
 * need to be handled on an OpenEMS Cycle run.
 * 
 * @param <T>
 */
public class MetaTasksManager<T extends ManagedTask> {

	private final Multimap<String, TasksManager<T>> tasksManagers = Multimaps
			.synchronizedListMultimap(ArrayListMultimap.create());
	private Map<Priority, Queue<T>> nextTasks;

	public MetaTasksManager() {
		// initialize Queues for next tasks
		EnumMap<Priority, Queue<T>> nextTasks = new EnumMap<>(Priority.class);
		for (Priority priority : Priority.values()) {
			nextTasks.put(priority, new LinkedList<>());
		}
		this.nextTasks = nextTasks;
	}

	/**
	 * Adds a TasksManager.
	 * 
	 * @param sourceId a source identifier
	 * @param task     the TasksManager
	 */
	public synchronized void addTasksManager(String sourceId, TasksManager<T> tasksManager) {
		this.tasksManagers.put(sourceId, tasksManager);
	}

	/**
	 * Removes a TasksManager.
	 * 
	 * @param sourceId a source identifier
	 * @param task     the TasksManager
	 */
	public synchronized void removeTasksManager(String sourceId, TasksManager<T> tasksManager) {
		this.tasksManagers.remove(sourceId, tasksManager);
	}

	/**
	 * Removes all TasksManagers with the given Source-ID.
	 * 
	 * @param sourceId a source identifier
	 */
	public synchronized void removeTasksManager(String sourceId) {
		this.tasksManagers.removeAll(sourceId);
	}

	/**
	 * Gets one task that with the given Priority sequentially.
	 * 
	 * @return the next task; null if there are no tasks with the given Priority
	 */
	public synchronized T getOneTask(Priority priority) {
		Queue<T> tasks = this.nextTasks.get(priority);
		if (tasks.isEmpty()) {
			// refill the queue
			for (TasksManager<T> tasksManager : this.tasksManagers.values()) {
				tasks.addAll(tasksManager.getAllTasks(priority));
			}
		}

		// returns the head or 'null' if the queue is still empty after refilling it
		return tasks.poll();
	}

	/**
	 * Gets all Tasks with the given Priority by their Source-ID.
	 * 
	 * @param priority the priority
	 * @return a list of tasks
	 */
	public Multimap<String, T> getAllTasksBySourceId(Priority priority) {
		Multimap<String, T> result = ArrayListMultimap.create();
		for (Entry<String, TasksManager<T>> entry : this.tasksManagers.entries()) {
			result.putAll(entry.getKey(), entry.getValue().getAllTasks(priority));
		}
		return result;
	}

	/**
	 * Gets all Tasks with by their Source-ID.
	 * 
	 * @param priority the priority
	 * @return a list of tasks
	 */
	public Multimap<String, T> getAllTasksBySourceId() {
		Multimap<String, T> result = ArrayListMultimap.create();
		for (Entry<String, TasksManager<T>> entry : this.tasksManagers.entries()) {
			result.putAll(entry.getKey(), entry.getValue().getAllTasks());
		}
		return result;
	}

	/**
	 * Does this {@link TasksManager} have any Tasks?.
	 * 
	 * @return true if there are Tasks
	 */
	public boolean hasTasks() {
		return !this.tasksManagers.isEmpty();
	}

}