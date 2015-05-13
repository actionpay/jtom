/**
 * Created by Temp on 12.05.2015.
 */

import org.adonweb.odm.exception.InvalidArgumentException;
import org.adonweb.odm.DAO;
import org.adonweb.odm.ConnectionPool;
import org.adonweb.odm.ConnectionInfo;
import org.adonweb.odm.tarantool.*;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

public class EntityDaoTest {
    private String host = "192.168.13.180";
    private Integer port = 3302;
    private String login = "remarketeer";
    private String password = "2kTNifmAIswtXkr";
    String entity = "test_entity";
    String luaCreateTesterScript = "box.schema.space.create('test_entity')\n" +
            "      box.space." + entity + ":create_index('primary', {type = 'hash', parts = {1, 'NUM'}})\n";
    String luaDropTesterScript = "box.space." + entity + ":drop()";

    public List createTesterSpace() throws Exception{
        return ((TarantoolConnection) ConnectionPool.connection("keeper")).eval(luaCreateTesterScript);
    }

    public List dropTesterSpace() throws Exception {
        return ((TarantoolConnection) ConnectionPool.connection("keeper")).eval(luaDropTesterScript);
    }

    @Test
    public void test() throws Exception {
        ConnectionPool.init(Arrays.asList(new ConnectionInfo(TarantoolConnection.class, "keeper", host, port, login, password)));
        try {
            dropTesterSpace();
        }catch (Exception ignored){}
        createTesterSpace();
        DAO<MockEntity> dao = TarantoolDAOImpl.getByClass(MockEntity.class);

        Long startInsert = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            MockEntity entityEntity = new MockEntity();
            entityEntity.setId(i)
                    .setF1(i * 10000 + 1)
                    .setF2(i * 10000 + 2)
                    .setF3(i * 10000 + 3)
                    .setF4(i * 10000 + 4)
                    .setF5( i * 10000 + 5)
                    .setF6( i * 10000 + 6)
                    .setF7(i * 10000 + 7);
            dao.insert(entityEntity);
            //entityEntity.setF1(i * 1000 + 1);
            //dao.save(entityEntity);
            //if (i%2==0)
//                dao.delete(entityEntity);
        }
        Long startSelect = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            //if (i%2!=0)
                dao.select(0, Arrays.asList(i/*,i * 10000 + 3*/));
        }
        /*for (MockEntity entity: dao.all().getAsObjectList())
            System.out.println(entity+" "+entity.getClass());*/
        Long endSelect = System.nanoTime();
        System.out.println("Insert Time: " + String.format("%.6f",(startSelect - startInsert) / 1000000000.));
        System.out.println("Select Time: " + String.format("%.6f", (endSelect - startSelect) / 1000000000.));
    }
}
