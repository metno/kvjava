package metno.kvalobs.kl2kvDbCopy;

public interface IProcessQuery {
	
	
	void atFirst( java.sql.ResultSet res)throws java.sql.SQLException;
	void atEach( java.sql.ResultSet res)throws java.sql.SQLException;
	void atLast()throws java.sql.SQLException;
}
