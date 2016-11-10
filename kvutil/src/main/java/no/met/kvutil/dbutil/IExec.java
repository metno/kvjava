package no.met.kvutil.dbutil;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IExec extends ISqlBase {
    int exec(PreparedStatement statement)throws SQLException;  // Execute insert, update, delete queries .
}
