package ru.productstar.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.productstar.hw.configuration.TaskConfig;
import ru.productstar.hw.entity.Task;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {"jdbcUrl=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"}
)
@ContextConfiguration(classes = TaskConfig.class)
@ExtendWith(SpringExtension.class)
class TaskDaoTest {
    @Autowired
    private TaskDao taskDao;

    @BeforeEach
    public void beforeEach() {
        taskDao.deleteAll();
    }

    @Test
    public void testSaveSetsId() {
        Task task = new Task("test task", false, LocalDateTime.now());
        taskDao.save(task);

        assertThat(task.getId()).isNotNull();
    }

    @Test
    public void testFindAllReturnsAllTasks() {
        Task firstTask = new Task("first task", false, LocalDateTime.now());
        taskDao.save(firstTask);

        Task secondTask = new Task("second task", false, LocalDateTime.now());
        taskDao.save(secondTask);

        assertThat(taskDao.findAll())
                .hasSize(2)
                .extracting("id")
                .contains(firstTask.getId(), secondTask.getId());
    }

    @Test
    public void testDeleteAllDeletesAllRowsInTasks() {
        Task firstTask = new Task("any task", false, LocalDateTime.now());
        taskDao.save(firstTask);

        int rowsDeleted = taskDao.deleteAll();
        assertThat(rowsDeleted).isEqualTo(1);

        assertThat(taskDao.findAll()).isEmpty();
    }

    @Test
    public void testGetByIdReturnsCorrectTask() {
        Task task = new Task("test task", false, LocalDateTime.now());
        taskDao.save(task);

        assertThat(taskDao.getById(task.getId()))
                .isNotNull()
                .extracting("id", "title", "finished", "createdDate")
                .containsExactly(task.getId(), task.getTitle(), task.getFinished(), task.getCreatedDate());
    }

    @Test
    public void testFindNotFinishedReturnsCorrectTasks() {
        Task unfinishedTask = new Task("test task", false, LocalDateTime.now());
        taskDao.save(unfinishedTask);

        Task finishedTask = new Task("test task", true, LocalDateTime.now());
        taskDao.save(finishedTask);

        assertThat(taskDao.findAllNotFinished())
                .singleElement()
                .extracting("id", "title", "finished", "createdDate")
                .containsExactly(unfinishedTask.getId(), unfinishedTask.getTitle(), unfinishedTask.getFinished(), unfinishedTask.getCreatedDate());
    }

    @Test
    public void testFindNewestTasksReturnsCorrectTasks() {
        Task firstTask = new Task("first task", false, LocalDateTime.now());
        taskDao.save(firstTask);

        Task secondTask = new Task("second task", false, LocalDateTime.now());
        taskDao.save(secondTask);

        Task thirdTask = new Task("third task", false, LocalDateTime.now());
        taskDao.save(thirdTask);

        assertThat(taskDao.findNewestTasks(2))
                .hasSize(2)
                .extracting("id")
                .containsExactlyInAnyOrder(secondTask.getId(), thirdTask.getId());
    }

    @Test
    public void testFinishSetsCorrectFlagInDb() {
        Task task = new Task("test task", false, LocalDateTime.now());
        taskDao.save(task);

        assertThat(taskDao.finishTask(task).getFinished()).isTrue();
        assertThat(taskDao.getById(task.getId()).getFinished()).isTrue();
    }

    @Test
    public void deleteByIdDeletesOnlyNecessaryData() {
        Task taskToDelete = new Task("first task", false, LocalDateTime.now());
        taskDao.save(taskToDelete);

        Task taskToPreserve = new Task("second task", false, LocalDateTime.now());
        taskDao.save(taskToPreserve);

        taskDao.deleteById(taskToDelete.getId());
        assertThat(taskDao.getById(taskToDelete.getId())).isNull();
        assertThat(taskDao.getById(taskToPreserve.getId())).isNotNull();
    }
}