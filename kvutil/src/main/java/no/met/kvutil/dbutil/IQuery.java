package no.met.kvutil.dbutil;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IQuery extends ISqlBase {
    java.sql.ResultSet exec(PreparedStatement statement) throws SQLException;  // Execute select queries .
}
