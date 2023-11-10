package com.company.service;

import com.company.dao.entity.Task;
import com.company.dao.entity.User;
import com.company.dao.reposiroty.TaskRepository;
import com.company.dao.reposiroty.UserRepository;
import com.company.dto.request.TaskRequest;
import com.company.dto.request.UserLoginRequest;
import com.company.dto.request.UserRequest;
import com.company.dto.response.TaskRespons;
import com.company.dto.response.UserRespons;
import com.company.exceptions.MyExceptionHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final UserLoginRequest userLoginRequest;
    private final TaskRepository taskRepository;

    public UserServiceImpl(UserRepository userRepository, UserLoginRequest userLoginRequest, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.userLoginRequest = userLoginRequest;
        this.taskRepository = taskRepository;
    }

    public User saveUser(User newUser) {
        return userRepository.save(newUser);
    }


    public List<UserRespons> getAllUser() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream().map(user -> {
            List<TaskRespons> tasks = user.getTaskList().stream()
                    .map(TaskRespons::new)
                    .collect(Collectors.toList());

            return new UserRespons(user, tasks);
        }).collect(Collectors.toList());
    }

    public UserRespons updateUser(Long userId, UserRequest userRequest) {
        return userRepository.findById(userId)
                .map(foundedUser -> {
                    if (userRequest.getName() != null && !userRequest.getName().isEmpty())
                        foundedUser.setFirstName(userRequest.getName());
                    if (userRequest.getSurname() != null && !userRequest.getSurname().isEmpty())
                        foundedUser.setLastName(userRequest.getSurname());
                    foundedUser.setId(userId);
                    User updateUser = userRepository.save(foundedUser);
                    List<TaskRespons> tasks = foundedUser.getTaskList().stream()
                            .map(TaskRespons::new)
                            .collect(Collectors.toList());
                    return new UserRespons(updateUser, tasks);
                })
                .orElseThrow(MyExceptionHandler::new);
    }

    public boolean deleteUser(Long userId) {
        Optional<User> optionalUser = Optional.of(userRepository.findById(userId).orElseThrow());
        userRepository.delete(optionalUser.get());
        return true;
    }

    public List<TaskRespons> getUserTasks(Long userId) {
        Optional<User> foundedUser = Optional.of(userRepository.findById(userId).orElseThrow());
        List<Task> taskList = foundedUser.get().getTaskList();
        List<TaskRespons> tasks = taskList.stream().map(TaskRespons::new).collect(Collectors.toList());
        return tasks;
    }

    public TaskRespons updateUserTasks(Long userId, Long taskId, TaskRequest taskRequest) {
        return userRepository.findById(userId)
                .map(user -> {
                    Optional<Task> optionalTask = taskRepository.findById(taskId);
                    if (optionalTask.isPresent()) {
                        Task task = optionalTask.get();
                        if (taskRequest.getTaskName() != null && !taskRequest.getTaskName().isEmpty())
                            task.setTaskName(taskRequest.getTaskName());
                        if (taskRequest.getDeadline() != null)
                            task.setDedline(taskRequest.getDeadline());
                        if (taskRequest.getText() != null && !taskRequest.getText().isEmpty())
                            task.setText(taskRequest.getText());
                        task.setUser(user);
                        Task updatedTask = taskRepository.save(task);
                        return new TaskRespons(updatedTask);
                    }
                    return null;
                })
                .orElseThrow(MyExceptionHandler::new);
    }

    public User getUserByEmail(String userName) {
        Optional<User> optionalUser = userRepository.findByEmail(userName);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        return null;
    }

    public boolean createUser(UserRequest userRequest) {
        Optional<User> foundedUser = userRepository.findByEmail(userRequest.getEmail());
        if (foundedUser != null)
            return false;
        User user = userRequest.userRequestConvertToUser(userRequest);
        userRepository.save(user);
        return true;
    }

    public UserRespons loginUser(UserLoginRequest request) {
        User userRequest = userLoginRequest.userLoginRequestConvertToUser(request);
        Optional<User> optionalUser = userRepository.findByEmail(userRequest.getEmail());
        if (optionalUser.isPresent()) {
            User foundedUser = optionalUser.get();
            if (foundedUser != null
                    && foundedUser.getEmail().equals(userRequest.getEmail())
                    && foundedUser.getPassword().equals(userRequest.getPassword())) {
                List<Task> userTasks = foundedUser.getTaskList();
                List<TaskRespons> tasks = userTasks.stream()
                        .map(TaskRespons::new)
                        .collect(Collectors.toList());
                return new UserRespons(foundedUser, tasks);
            }
            return null;
        }
        return null;
    }

}