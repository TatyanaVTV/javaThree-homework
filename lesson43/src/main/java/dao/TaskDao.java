package dao;

import entity.Task;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private final DataSource dataSource;

    public TaskDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Task parseTaskFromResultSet(ResultSet resultSet) {
        try {
            Task task = new Task(
                    resultSet.getString("title"),
                    resultSet.getBoolean("finished"),
                    resultSet.getTimestamp("created_date").toLocalDateTime()
            );
            task.setId(resultSet.getInt("task_id"));
            return task;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Task save(Task task) {
        // get connection
        // create statement
        // set params
        // execute
        // get id
        // set id
        String sql = "INSERT INTO task (title, finished, created_date) VALUES (?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {

            statement.setString(1, task.getTitle());
            statement.setBoolean(2, task.getFinished());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(task.getCreatedDate()));
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    task.setId(resultSet.getInt(1));
                }
            }

        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }

        return task;
    }

    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT task_id, title, finished, created_date FROM task ORDER BY task_id";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)
        ) {

            while (resultSet.next()) {
                final Task task = new Task(
                        resultSet.getString(2),
                        resultSet.getBoolean(3),
                        resultSet.getTimestamp(4).toLocalDateTime()
                );
                task.setId(resultSet.getInt(1));
                tasks.add(task);
            }

        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }

        return tasks;
    }

    public int deleteAll() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            String sql = "DELETE FROM task";
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Task getById(Integer id) {
        String sql = "SELECT task_id, title, finished, created_date FROM task WHERE task_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return parseTaskFromResultSet(rs);
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> findAllNotFinished() {
        List<Task> notFinishedTasks = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            String sql = "SELECT task_id, title, finished, created_date FROM task WHERE finished = false";
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                notFinishedTasks.add(parseTaskFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return notFinishedTasks;
    }

    public List<Task> findNewestTasks(Integer numberOfNewestTasks) {
        List<Task> newestTasks = new ArrayList<>();

        String sql = "SELECT task_id, title, finished, created_date FROM task ORDER BY created_date DESC LIMIT ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, numberOfNewestTasks);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                newestTasks.add(parseTaskFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newestTasks;
    }

    public Task finishTask(Task task) {
        String sql = "UPDATE task SET finished = true WHERE task_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, task.getId());
            statement.executeUpdate();
            task.setFinished(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    public void deleteById(Integer id) {
        String sql = "DELETE FROM task WHERE task_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
