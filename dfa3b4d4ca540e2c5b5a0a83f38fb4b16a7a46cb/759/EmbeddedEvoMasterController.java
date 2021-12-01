package em.embedded.org.devgateway.ocvn;


import com.mongodb.MongoClient;
import com.p6spy.engine.spy.P6SpyDriver;
import org.devgateway.toolkit.web.spring.WebApplication;
import org.evomaster.client.java.controller.AuthUtils;
import org.evomaster.client.java.controller.EmbeddedSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.db.DbCleaner;
import org.evomaster.client.java.controller.db.SqlScriptRunnerCached;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.evomaster.client.java.controller.api.dto.AuthenticationDto;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class used to start/stop the SUT. This will be controller by the EvoMaster process
 */
public class EmbeddedEvoMasterController extends EmbeddedSutController {

    public static void main(String[] args) {

        int port = 40100;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EmbeddedEvoMasterController controller = new EmbeddedEvoMasterController(port);
        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }


    private ConfigurableApplicationContext ctx;
    private Connection connection;
    private MongoClient mongoClient;


    private static final GenericContainer mongodb = new GenericContainer("mongo:3.2")
            .withExposedPorts(27017);


    public EmbeddedEvoMasterController() {
        this(0);
    }

    public EmbeddedEvoMasterController(int port) {
        setControllerPort(port);
    }


    @Override
    public String startSut() {

        mongodb.start();

        mongoClient = new MongoClient(mongodb.getContainerIpAddress(),
                mongodb.getMappedPort(27017));

        ctx = SpringApplication.run(WebApplication.class,
                new String[]{"--server.port=0",
                        "--liquibase.enabled=false",
                        "--spring.data.mongodb.uri=mongodb://"+mongodb.getContainerIpAddress()+":"+mongodb.getMappedPort(27017)+"/ocvn",
                        "--spring.datasource.url=jdbc:p6spy:h2:mem:testdb;DB_CLOSE_DELAY=-1;",
                        "--spring.datasource.driver-class-name=" + P6SpyDriver.class.getName(),
                        "--spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                        "--spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true",
                        "--spring.datasource.username=sa",
                        "--spring.datasource.password",
                        "--dg-toolkit.derby.port=0",
                        "--spring.cache.type=NONE"
                });



        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        JdbcTemplate jdbc = ctx.getBean(JdbcTemplate.class);

        try {
            connection = jdbc.getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "http://localhost:" + getSutPort();
    }

    protected int getSutPort() {
        return (Integer) ((Map) ctx.getEnvironment()
                .getPropertySources().get("server.ports").getSource())
                .get("local.server.port");
    }


    @Override
    public boolean isSutRunning() {
        return ctx != null && ctx.isRunning();
    }

    @Override
    public void stopSut() {
        ctx.stop();
        ctx.close();

        mongodb.stop();
    }

    @Override
    public String getPackagePrefixesToCover() {
        return "org.devgateway.";
    }

    @Override
    public void resetStateOfSUT() {
        mongoClient.getDatabase("ocvn").drop();
        mongoClient.getDatabase("ocvn-shadow").drop();

        DbCleaner.clearDatabase_H2(connection);
        SqlScriptRunnerCached.runScriptFromResourceFile(connection,"/init_db.sql");
    }


    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {
        return Arrays.asList(AuthUtils.getForDefaultSpringFormLogin("ADMIN", "admin", "admin"));
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getDatabaseDriverName() {
        return "org.h2.Driver";
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RestProblem(
                "http://localhost:" + getSutPort() + "/v2/api-docs?group=1ocDashboardsApi",
                null
        );
    }

    @Override
    public SutInfoDto.OutputFormat getPreferredOutputFormat() {
        return SutInfoDto.OutputFormat.JAVA_JUNIT_4;
    }
}
