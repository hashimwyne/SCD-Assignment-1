import java.io.*;
import java.util.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;


class Task {
    int id;
    String title;
    LocalDateTime startTime;
    LocalDateTime endTime;
    List<Integer> dependencies;

    public Task(int id, String title, String startTime, String endTime, List<Integer> dependencies) {
        this.id = id;
        this.title = title;
        this.startTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyyMMdd+HHmm"));
        this.endTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyyMMdd+HHmm"));
        this.dependencies = dependencies;
    }

    public boolean isOverlapping(Task other) {
        return !this.endTime.isBefore(other.startTime) && !this.startTime.isAfter(other.endTime);
    }
}



class Resource {
    String name;
    Map<Integer, Integer> taskAllocations;

    public Resource(String name) {
        this.name = name;
        this.taskAllocations = new HashMap<>();
    }

    public void addAllocation(int taskId, int percentage) {
        taskAllocations.put(taskId, percentage);
    }

    public int getTotalEffort(Map<Integer, Task> tasks) {
        int totalEffort = 0;
        for (Map.Entry<Integer, Integer> entry : taskAllocations.entrySet()) {
            Task task = tasks.get(entry.getKey());
            if (task != null) {
                long durationInHours = Duration.between(task.startTime, task.endTime).toHours();
                totalEffort += (durationInHours * entry.getValue()) / 100;
            }
        }
        return totalEffort;
    }
}


class Project {
    Map<Integer, Task> tasks;
    Map<String, Resource> resources;

    public Project() {
        tasks = new HashMap<>();
        resources = new HashMap<>();
    }

    public void addTask(Task task) {
        tasks.put(task.id, task);
    }

    public void addResource(Resource resource) {
        resources.put(resource.name, resource);
    }

    public LocalDateTime getProjectCompletionTime() {
        LocalDateTime latestEndTime = LocalDateTime.MIN;
        for (Task task : tasks.values()) {
            if (task.endTime.isAfter(latestEndTime)) {
                latestEndTime = task.endTime;
            }
        }
        return latestEndTime;
    }

    public void highlightOverlappingTasks() {
        for (Task task1 : tasks.values()) {
            for (Task task2 : tasks.values()) {
                if (task1 != task2 && task1.isOverlapping(task2)) {
                    System.out.println("Overlapping tasks: " + task1.title + " and " + task2.title);
                }
            }
        }
    }

    public void findTeamForTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            System.out.println("Resources for task " + task.title + ":");
            for (Resource resource : resources.values()) {
                if (resource.taskAllocations.containsKey(taskId)) {
                    System.out.println(resource.name);
                }
            }
        }
    }

    public void calculateEffortPerResource() {
        for (Resource resource : resources.values()) {
            int totalEffort = resource.getTotalEffort(tasks);
            System.out.println("Total effort required by " + resource.name + ": " + totalEffort + " hours");
        }
    }
}


class FileReaderUtil {

    public static List<Task> readTasksFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<Task> tasks = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(", ");
            int id = Integer.parseInt(parts[0]);
            String title = parts[1];
            String startTime = parts[2];
            String endTime = parts[3];
            List<Integer> dependencies = new ArrayList<>();
            for (int i = 4; i < parts.length; i++) {
                dependencies.add(Integer.parseInt(parts[i]));
            }
            tasks.add(new Task(id, title, startTime, endTime, dependencies));
        }
        reader.close();
        return tasks;
    }

    public static Map<String, Resource> readResourcesFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Map<String, Resource> resources = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(", ");
            String name = parts[0];
            Resource resource = new Resource(name);
            for (int i = 1; i < parts.length; i++) {
                String[] allocation = parts[i].split(":");
                int taskId = Integer.parseInt(allocation[0]);
                int percentage = Integer.parseInt(allocation[1]);
                resource.addAllocation(taskId, percentage);
            }
            resources.put(name, resource);
        }
        reader.close();
        return resources;
    }
}


public class ProjectPlanner {
    public static void main(String[] args) {
        try {

            List<Task> tasks = FileReaderUtil.readTasksFromFile("tasks.txt");
            Map<String, Resource> resources = FileReaderUtil.readResourcesFromFile("resources.txt");


            Project project = new Project();


            for (Task task : tasks) {
                project.addTask(task);
            }


            for (Resource resource : resources.values()) {
                project.addResource(resource);
            }


            System.out.println("Project Completion Time: " + project.getProjectCompletionTime());
            project.highlightOverlappingTasks();
            project.findTeamForTask(5);  // Example: Find team for task 5
            project.calculateEffortPerResource();

        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
        }
    }
}
