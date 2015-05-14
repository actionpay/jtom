import net.actionpay.jtom.DAO;
import net.actionpay.jtom.ConnectionPool;
import net.actionpay.jtom.ConnectionInfo;
import net.actionpay.jtom.tarantool.TarantoolConnection;
import net.actionpay.jtom.tarantool.TarantoolDAOImpl;
import org.junit.*;

import java.util.*;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class EntityDaoTest {
    static private String host = "localhost";
    static private Integer port = 3302;
    static private String login = "login";
    static private String password = "password";

    @BeforeClass
    static public void prepare() throws Exception {
        ConnectionPool.init(Collections.singletonList(
                new ConnectionInfo(TarantoolConnection.class, "keeper", host, port, login, password)));
        TarantoolDAOImpl.getByClass(MockEntity.class).createSpace();
    }

    @AfterClass
    static public void done() throws Exception {
        TarantoolDAOImpl.getByClass(MockEntity.class).dropSpace();
        ConnectionPool.done();
    }

    @Test
    public void testInsertSelectTarantool() throws Exception {
        System.out.println("insert/select test");
        DAO<MockEntity> dao = TarantoolDAOImpl.getByClass(MockEntity.class);
        MockEntity entity = new MockEntity();
        Map f3 = new HashMap<>();
        f3.put("key", "value");
        entity.setId(0)
                .setF1("1")//integer string test
                .setF2(2)//integer test
                .setF3(f3)//map test
                .setF7(Arrays.asList("1", 1, "a"));//array test
        dao.save(entity);
        entity = dao.get(0, Collections.singletonList(0)).getObjectIterator().next();
        Assert.assertEquals(entity.getF1(), "1");
        Assert.assertEquals(entity.getF2(), Integer.valueOf(2));
        Assert.assertEquals(entity.getF3().get("key"), "value");
        Assert.assertEquals(entity.getF7().get(0), "1");
        Assert.assertEquals(entity.getF7().get(1), 1);
        Assert.assertEquals(entity.getF7().get(2), "a");
        System.out.println("end insert/select test");
    }

    @Test
    public void test2() {

    }

    @Test
    public void test() throws Exception {
        System.out.println("test");
        DAO<MockEntity> dao = TarantoolDAOImpl.getByClass(MockEntity.class);
        Integer count = 10;
        Long startInsert = System.nanoTime();
        for (int i = 0; i < count; i++) {
            MockEntity entityEntity = new MockEntity();
            entityEntity.setId(i)
                    .setF1(String.valueOf(i * 10000 + 1))
                    .setF2(i * 10000 + 2)
                    .setF4(i * 10000 + 4)
                    .setF5(i * 10000 + 5)
                    .setF6(i * 10000 + 6)
            ;
            dao.add(entityEntity);
        }
        Long startSelect = System.nanoTime();
        for (int i = 0; i < count; i++) {

            MockEntity entity = dao.get(0, Collections.singletonList(i)).getObjectIterator().next();
            Assert.assertEquals(entity.getF1(), String.valueOf(i * 10000 + 1));
            Assert.assertEquals(entity.getF2(), (Integer) (i * 10000 + 2));
            Assert.assertEquals(entity.getF4(), (Integer) (i * 10000 + 4));
            Assert.assertEquals(entity.getF5(), (Integer) (i * 10000 + 5));
            Assert.assertEquals(entity.getF6(), (Integer) (i * 10000 + 6));
            dao.drop(entity);
        }
        Long endSelect = System.nanoTime();


        System.out.println("Insert Time: " + String.format("%.6f", (startSelect - startInsert) / 1000000000.));
        System.out.println("Select Time: " + String.format("%.6f", (endSelect - startSelect) / 1000000000.));
        System.out.println("end test");
    }
}
