package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ElephantRepository {

    @Autowired
    private NamedParameterJdbcTemplate template;

    public Elephant getElephant(int id) {
        String sql = "SELECT * FROM Elephants WHERE ElephantID = :id";
        return template.queryForObject(sql, new MapSqlParameterSource("id", id), (rs, rowNum) -> {
            Elephant elephant = new Elephant();
            elephant.setId(rs.getInt("ElephantID"));
            elephant.setName(rs.getString("Name"));
            elephant.setAge(rs.getInt("Age"));
            elephant.setSpecies(rs.getString("Species"));
            elephant.setLocation(rs.getString("Location"));
            elephant.setWeight(rs.getDouble("Weight"));
            elephant.setHeight(rs.getDouble("Height"));
            elephant.setHealthStatus(rs.getString("HealthStatus"));
            elephant.setLastHealthCheckDate(rs.getDate("LastHealthCheckDate"));
            elephant.setBirthday(rs.getDate("Birthday"));
            return elephant;
        });
    }

    public void addElephant(Elephant elephant) {
        String sql = "INSERT INTO Elephants (Name, Age, Species, Location, Weight, Height, HealthStatus, LastHealthCheckDate, Birthday) " +
                "VALUES (:name, :age, :species, :location, :weight, :height, :healthStatus, :lastHealthCheckDate, :birthday)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", elephant.getName())
                .addValue("age", elephant.getAge())
                .addValue("species", elephant.getSpecies())
                .addValue("location", elephant.getLocation())
                .addValue("weight", elephant.getWeight())
                .addValue("height", elephant.getHeight())
                .addValue("healthStatus", elephant.getHealthStatus())
                .addValue("lastHealthCheckDate", elephant.getLastHealthCheckDate())
                .addValue("birthday", elephant.getBirthday());

        template.update(sql, params);
    }

    public List<Map<String, Object>> getAllElephants(int start, int length, String searchValue, String orderColumn, String orderDir) {
        String sql = "SELECT * FROM Elephants";
        MapSqlParameterSource params = new MapSqlParameterSource();

        // Filtering
        if (searchValue != null && !searchValue.isEmpty()) {
            sql += " WHERE lower(Name) LIKE :searchValue OR lower(Species) LIKE :searchValue";
            params.addValue("searchValue", "%" + searchValue.toLowerCase() + "%");
        }

        // Ordering
        sql += " ORDER BY " + orderColumn + " " + orderDir;

        // Pagination
        sql += " OFFSET :start ROWS FETCH NEXT :length ROWS ONLY";
        params.addValue("start", start);
        params.addValue("length", length);

        return template.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> elephant = new HashMap<>();
            elephant.put("ElephantID", rs.getInt("ElephantID"));
            elephant.put("Name", rs.getString("Name"));
            elephant.put("Age", rs.getInt("Age"));
            elephant.put("Species", rs.getString("Species"));
            elephant.put("Location", rs.getString("Location"));
            elephant.put("Weight", rs.getDouble("Weight"));
            elephant.put("Height", rs.getDouble("Height"));
            elephant.put("HealthStatus", rs.getString("HealthStatus"));
            elephant.put("LastHealthCheckDate", rs.getDate("LastHealthCheckDate"));
            elephant.put("Birthday", rs.getDate("Birthday"));
            return elephant;
        });
    }

    public long getTotalCount() {
        String sql = "SELECT COUNT(*) FROM Elephants";
        return template.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    public long getFilteredCount(String searchValue) {
        String sql = "SELECT COUNT(*) FROM Elephants";
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (searchValue != null && !searchValue.isEmpty()) {
            sql += " WHERE lower(Name) LIKE :searchValue OR lower(Species) LIKE :searchValue";
            params.addValue("searchValue", "%" + searchValue.toLowerCase() + "%");
        }

        return template.queryForObject(sql, params, Long.class);
    }

    public List<Elephant> getElephantsForParams(Map<String, Object> params) {
        String searchValue = (String) params.get("searchValue");
        String orderColumn = (String) params.get("orderColumn");
        String orderDir = (String) params.get("orderDir");
        int start = (int) params.get("start");
        int length = (int) params.get("length");

        String sql = "SELECT * FROM Elephants";
        MapSqlParameterSource paramSource = new MapSqlParameterSource();

        // Filtering
        if (searchValue != null && !searchValue.isEmpty()) {
            sql += " WHERE lower(Name) LIKE :searchValue OR lower(Species) LIKE :searchValue";
            paramSource.addValue("searchValue", "%" + searchValue.toLowerCase() + "%");
        }

        // Ordering
        sql += " ORDER BY " + orderColumn + " " + orderDir;

        // Pagination
        sql += " OFFSET :start ROWS FETCH NEXT :length ROWS ONLY";
        paramSource.addValue("start", start);
        paramSource.addValue("length", length);

        return template.query(sql, paramSource, (rs, rowNum) -> {
            Elephant elephant = new Elephant();
            elephant.setId(rs.getInt("ElephantID"));
            elephant.setName(rs.getString("Name"));
            elephant.setAge(rs.getInt("Age"));
            elephant.setSpecies(rs.getString("Species"));
            elephant.setLocation(rs.getString("Location"));
            elephant.setWeight(rs.getDouble("Weight"));
            elephant.setHeight(rs.getDouble("Height"));
            elephant.setHealthStatus(rs.getString("HealthStatus"));
            elephant.setLastHealthCheckDate(rs.getDate("LastHealthCheckDate"));
            elephant.setBirthday(rs.getDate("Birthday"));
            return elephant;
        });
    }
}
