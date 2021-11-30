package org.crazycake.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.crazycake.shiro.common.SessionInMemory;
import org.crazycake.shiro.exception.SerializationException;
import org.crazycake.shiro.integration.fixture.model.FakeSession;
import org.crazycake.shiro.serializer.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static org.crazycake.shiro.integration.fixture.TestFixture.*;

/**
 * RedisSessionDAO integration test was put under org.crazycake.shiro
 * is because I want to test protected method `doReadSession`
 */
public class RedisSessionDAOIntegrationTest {

    private RedisSessionDAO redisSessionDAO;
    private FakeSession session1;
    private FakeSession session2;
    private FakeSession emptySession;
    private String name1;
    private String prefix;
    private void blast() {
        blastRedis();
    }

    private void scaffold() {
        prefix = scaffoldPrefix();
        RedisManager redisManager = scaffoldStandaloneRedisManager();
        redisSessionDAO = scaffoldRedisSessionDAO(redisManager, prefix);
        session1 = scaffoldSession();
        session2 = scaffoldSession();
        emptySession = scaffoldEmptySession();
        name1 = scaffoldUsername();
    }

    @BeforeEach
    public void setUp() {
        blast();
        scaffold();
    }

    @AfterEach
    public void tearDown() {
        blast();
    }

    @Test
    public void testDoCreateNull() {
        Assertions.assertThrows(UnknownSessionException.class, () -> {
            redisSessionDAO.doCreate(null);
        });
    }

    @Test
    public void testDoCreate() {
        redisSessionDAO.doCreate(session1);
        Session actualSession = redisSessionDAO.doReadSession(session1.getId());
        assertSessionEquals(actualSession, session1);
    }

    @Test
    public void testDoCreateWithSessionTimeout() {
        doSetSessionDAOExpire(redisSessionDAO, -2);
        redisSessionDAO.doCreate(session2);
        assertEquals(getRedisTTL(prefix + session2.getId(), new StringSerializer()), 1800L);
    }

    @Test
    public void testUpdateNull() {
        Assertions.assertThrows(UnknownSessionException.class, () -> {
            redisSessionDAO.update(null);
        });
    }

    @Test
    public void testUpdateEmptySession() {
        Assertions.assertThrows(UnknownSessionException.class, () -> {
            redisSessionDAO.update(emptySession);
        });
    }

    @Test
    public void testUpdate() {
        redisSessionDAO.doCreate(session1);
        redisSessionDAO.doReadSession(session1.getId());
        doChangeSessionName(session1, name1);
        redisSessionDAO.update(session1);
        FakeSession actualSession = (FakeSession)redisSessionDAO.doReadSession(session1.getId());
        assertEquals(actualSession.getName(), name1);
    }

    @Test
    public void testUpdateWithoutSessionInMemory() {
        redisSessionDAO.setSessionInMemoryEnabled(false);
        redisSessionDAO.doCreate(session1);
        redisSessionDAO.doReadSession(session1.getId());
        doChangeSessionName(session1, name1);
        redisSessionDAO.update(session1);
        FakeSession actualSession = (FakeSession)redisSessionDAO.doReadSession(session1.getId());
        assertEquals(actualSession.getName(), name1);
    }

    @Test
    public void testDelete() {
        redisSessionDAO.doCreate(session1);
        redisSessionDAO.delete(session1);
        assertRedisEmpty();
    }

    @Test
    public void testGetActiveSessions() {
        redisSessionDAO.doCreate(session1);
        redisSessionDAO.doCreate(session2);
        Collection<Session> activeSessions = redisSessionDAO.getActiveSessions();
        assertEquals(activeSessions.size(), 2);
    }

    @Test
    public void testRemoveExpiredSessionInMemory() throws InterruptedException, SerializationException {
        redisSessionDAO.setSessionInMemoryTimeout(500L);
        redisSessionDAO.doCreate(session1);
        redisSessionDAO.doReadSession(session1.getId());
        Thread.sleep(1000);
        redisSessionDAO.doCreate(session2);
        redisSessionDAO.doReadSession(session2.getId());
        Map<Serializable, SessionInMemory> sessionMap = (Map<Serializable, SessionInMemory>) redisSessionDAO.getSessionsInThread().get();
        assertEquals(sessionMap.size(), 1);
    }
}
