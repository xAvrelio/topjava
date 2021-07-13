package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;
import ru.javawebinar.topjava.util.BeanValidate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);
        /*Validate user*/
        BeanValidate.getInstance().validate(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
        } else {
            /*
            User getUser = get(user.getId());
            if (getUser != null ) {
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE users SET ");
                if (getUser.getName().equals(user.getName())) {
                    sb.append("name=:name ");
                } else if (!getUser.getEmail().equals(user.getEmail())) {
                    sb.append("email=:email ");
                } else if (!getUser.getPassword().equals(user.getPassword())){
                    sb.append("password=:password ");
                } else if (!getUser.getRegistered().equals(user.getRegistered())){
                    sb.append("registered=:registered");
                } else if (getUser.isEnabled() != user.isEnabled()){
                    sb.append("enabled=:enabled ");
                } else if (getUser.getCaloriesPerDay() != user.getCaloriesPerDay()){
                    sb.append("calories_per_day=:caloriesPerDay ");
                } else if (!getUser.getRoles().equals(user.getRoles())){
                    addRoles(user);
                }
                sb.append("WHERE id:=id");
                System.out.println(sb);
                namedParameterJdbcTemplate.update(sb.toString(),parameterSource);
            }
             */

            if (namedParameterJdbcTemplate.update("""
                       UPDATE users SET name=:name, email=:email, password=:password, 
                       registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                    """, parameterSource) == 0) {
                                return null;
           }

        }
        removeRoles(user);
        addRoles(user);
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users LEFT OUTER JOIN user_roles on users.id = user_roles.user_id WHERE id=?", new UserMapper(), id);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public User getByEmail(String email) {
        String sql = "SELECT * FROM users LEFT OUTER JOIN user_roles on users.id = user_roles.user_id WHERE email=?";
        List<User> users = jdbcTemplate.query(sql, new UserMapper(), email);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("SELECT * FROM users LEFT OUTER JOIN user_roles on users.id = user_roles.user_id ORDER BY name, email", new UserMapper());
    }


    private void addRoles(User user) {
        Set<Role> roles = user.getRoles();
        if (!CollectionUtils.isEmpty(roles)) {
            jdbcTemplate.batchUpdate("INSERT INTO user_roles (user_id, role) VALUES (?, ?)", roles, roles.size(), (ps, argument) -> {
                ps.setInt(1, user.id());
                ps.setString(2, argument.name());
            });

        }
    }

    private void removeRoles(User user) {
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id=?", user.getId());
    }

    private Set<Role> getRoles(User user) {
        return jdbcTemplate.query("SELECT * FROM user_roles WHERE user_roles.user_id =?", new RoleMapper(), user.getId());
    }

    private static class RoleMapper implements ResultSetExtractor<Set<Role>> {
        Set<Role> roles = new HashSet<>();
        @Override
        public Set<Role> extractData(ResultSet rs) throws SQLException, DataAccessException {
            while (rs.next()) {
                roles.add(Enum.valueOf(Role.class, rs.getString("role")));
            }
            return roles;
        }
    }

    private static class UserMapper implements ResultSetExtractor<List<User>> {
        List<User> list = new ArrayList<>();
        Map<Integer, User> userMap = new HashMap<>();
        Map<Integer, Set<Role>> roleMap = new HashMap<>();

        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            while (rs.next()) {
                Integer userId = rs.getInt("id");
                Set<Role> roles = roleMap.get(userId);
                if (roles == null) {
                    roles = new HashSet<>();
                    if (rs.getString("role") != null) {
                        roles.add(Enum.valueOf(Role.class, rs.getString("role")));
                    }
                } else {
                    if (rs.getString("role") != null) {
                        roles.add(Enum.valueOf(Role.class, rs.getString("role")));
                    }
                }
                roleMap.put(userId, roles);
                User user = userMap.get(userId);
                if (user == null) {
                    user = new User();
                    user.setId(userId);
                    user.setEmail(rs.getString("email"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setPassword(rs.getString("password"));
                    user.setCaloriesPerDay(rs.getInt("calories_per_day"));
                    user.setName(rs.getString("name"));
                    user.setRoles(roleMap.get(userId));
                    list.add(user);
                    userMap.put(userId, user);
                } else {
                    user.setRoles(roleMap.get(userId));
                }
            }
            return list;
        }
    }


}
