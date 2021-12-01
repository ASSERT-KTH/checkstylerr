package emp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.sql.json.OracleJsonObject;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Select employees from the emp table where the salary is greater than 30,000.
 * 
 * <p>
 * Run first: {@link CreateTable}, {@link Insert}
 * </p>
 */
public class Filter {

    public static void main(String[] args) throws SQLException {
        PoolDataSource pool = PoolDataSourceFactory.getPoolDataSource();
        pool.setURL(args[0]);
        pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        
        try (Connection con = pool.getConnection()) {
            // Filter by salary
            PreparedStatement stmt = con.prepareStatement(
                "SELECT e.data FROM emp e WHERE e.data.salary.number() > :1");
    
            stmt.setInt(1, 30000);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OracleJsonObject obj = rs.getObject(1, OracleJsonObject.class);
                String name = obj.getString("name");
                String job  = obj.getString("job");
                System.out.println(name + " - " + job);
            }
            rs.close();
            stmt.close();
        }
        
    }

}
