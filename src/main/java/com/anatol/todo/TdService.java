package com.anatol.todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


//Сервис работающий с бд через JdbcTemplate. TodoRowMapper формирует объект t0d0 из полученных значений бд
@Component
public class TdService {

    @Autowired
    private JdbcTemplate jtmplt;

    private static class TodoRowMapper implements RowMapper<Todo>{
        @Override
        public Todo mapRow(ResultSet resultSet, int i) throws SQLException {
            Todo td = new Todo();
            td.setId(resultSet.getInt("id"));
            td.setDescription(resultSet.getString("description"));
            td.setProgress(resultSet.getDouble("progress"));
            return td;
        }

    }

    public List<Todo> getAllTodo() {
        return jtmplt.query(
                "SELECT id, description,progress  FROM todos",new TodoRowMapper());

    }

    public void updateTodo(Todo td) {
        final String sql = "UPDATE todos SET description=?, progress=? WHERE id=?";
        jtmplt.update(sql, td.getDescription(),td.getProgress(),td.getId());
    }

    public void insertTodo(Todo td) {
        final String sql = "INSERT INTO todos (description,progress) VALUES (?,?)";
        final String descr = td.getDescription();
        final double progr = td.getProgress();
        jtmplt.update(sql,new Object[]{descr, progr});

    }

    public Todo getTodoById(int id) {
        //SELECT id,descr,progress FROM todos WHERE id = {value}
        final String sql = "SELECT * FROM todos WHERE id = ?";
        Todo td = jtmplt.queryForObject(sql, new TodoRowMapper(), id);
        return td;
    }

    public void removeTodoById(int id) {
        //DELETE FROM todos WHERE id = {value}
        String sql = "DELETE FROM todos WHERE id = ?";
        jtmplt.update(sql, id);
    }
}
