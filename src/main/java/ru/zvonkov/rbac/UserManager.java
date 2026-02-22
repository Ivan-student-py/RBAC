package ru.zvonkov.rbac;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("User with username '" + user.username() + "' already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
        if (user == null) return false;
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(username.trim()));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String cleanEmail = email.trim();
        return users.values().stream()
                .filter(user -> cleanEmail.equals(user.email()))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        if (sorter == null) {
            throw new IllegalArgumentException("Sorter must not be null");
        }
        return findByFilter(filter).stream()
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return users.containsKey(username.trim());
    }

    public void update(String username, String newFullName, String newEmail) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }
        String cleanUsername = username.trim();
        if (!users.containsKey(cleanUsername)) {
            throw new IllegalArgumentException("User with username '" + cleanUsername + "' does not exist");
        }

        User updatedUser = User.validate(cleanUsername, newFullName, newEmail);
        users.put(cleanUsername, updatedUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}