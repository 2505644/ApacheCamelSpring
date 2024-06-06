package project.db;

import org.apache.camel.Handler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class H2Repo {

    private static Logger LOGGER = LogManager.getLogger(H2Repo.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Handler
    public void writeMessageToDb(InputStream inputStream) {
        String sql = "INSERT into msg (message) values (?)";
        KeyHolder holder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS);
                    try {
                        ps.setBlob(1, inputStream);
                    } catch (Exception e) {
                        LOGGER.error("Insert message body in H2Repository class!", e);
                    }
                    return ps;
                }, holder);

        insertHeader(holder.getKey().intValue(), "customType");
    }

    private void insertHeader(int recordId, String value) {
        String sql = "INSERT into headers (headers_id, head) values (?,?)";
        jdbcTemplate.update(sql, recordId, value);
    }

    public void showDataInH2Db() {
        LOGGER.info("Show result in database");
        String sql = "SELECT headers.head, msg.message " +
                " FROM msg" +
                " JOIN headers" +
                " ON msg.msg_id = headers.headers_id";

        List<Map<String, byte[]>> list = jdbcTemplate.query(
                sql, new ResultSetExtractor<List<Map<String, byte[]>>>() {
                    @Override
                    public List<Map<String, byte[]>> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                        List<Map<String, byte[]>> list = new ArrayList<>();

                        while (resultSet.next()) {
                            Map<String, byte[]> map = new HashMap<String, byte[]>();
                            map.put(
                                    resultSet.getString(1),
                                    resultSet.getBytes(2));
                            list.add(map);
                        }
                        return list;
                    }
                });

        for (Map<String, byte[]> map : list) {
            for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                String str = new String(entry.getValue());
                LOGGER.info(
                        String.format(
                                "type: %s  body: %s", entry.getKey(), str));
            }
        }
    }

    public void clearH2Db() {
        try {
            String deleteTable1 = "DROP TABLE msg";
            String deleteTable2 = "DROP TABLE headers";
            jdbcTemplate.execute(deleteTable1);
            jdbcTemplate.execute(deleteTable2);
            LOGGER.info("Successful table drop");
        } catch (Exception e) {
            LOGGER.error("Can`t drop tables!", e);
        }
    }
}
