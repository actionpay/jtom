package net.actionpay.jtom;

import net.actionpay.jtom.tarantool.TarantoolConnection;
import net.actionpay.jtom.tarantool.TarantoolImpl;
import org.junit.*;

import java.util.*;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class EntityDaoTest {
	static private String host = "192.168.13.180";
	static private Integer port = 3302;
	static private String login = "remarketeer";
	static private String password = "2kTNifmAIswtXkr";

	@BeforeClass
	static public void prepare() throws Exception {
		ConnectionPool.init(Arrays.asList(
				new ConnectionInfo(TarantoolConnection.class, "keeper", host, 3301, login, password),
				new ConnectionInfo(TarantoolConnection.class, "system", host, port, login, password)));
		TarantoolImpl.getByClass(MockManyEntity.class).createSpace();
		TarantoolImpl.getByClass(MockEntity.class).createSpace();
	}

	@AfterClass
	static public void done() throws Exception {
		TarantoolImpl.getByClass(MockManyEntity.class).dropSpace();
		TarantoolImpl.getByClass(MockEntity.class).dropSpace();

		ConnectionPool.done();
	}

	@Test
	public void testInsertSelectTarantool() throws Exception {
		System.out.println("insert/select test");
		DAO<MockEntity> dao = TarantoolImpl.getByClass(MockEntity.class);
		MockEntity entity = new MockEntity();
		Map f3 = new HashMap<>();
		f3.put("key", "value");
		entity.setId(0L)
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
		System.out.println("end insert/select test\n");
	}

	@Test
	public void testUpdate() throws Exception {
		System.out.println("update test");
		DAO<MockEntity> dao = TarantoolImpl.getByClass(MockEntity.class);
		MockEntity entity = new MockEntity();
		Map f3 = new HashMap<>();
		f3.put("key", "value");
		entity.setId(0L)
				.setF1("1")//integer string test
				.setF2(2)//integer test
				.setF3(f3)//map test
				.setF7(Arrays.asList("1", 1, "a"));//array test
		dao.save(entity);
		entity.setF1("1m");
		dao.save(entity);
		entity = dao.get(0, Collections.singletonList(0)).getObjectIterator().next();
		Assert.assertEquals(entity.getF1(), "1m");
		Assert.assertEquals(entity.getF2(), Integer.valueOf(2));
		Assert.assertEquals(entity.getF3().get("key"), "value");
		Assert.assertEquals(entity.getF7().get(0), "1");
		Assert.assertEquals(entity.getF7().get(1), 1);
		Assert.assertEquals(entity.getF7().get(2), "a");
		System.out.println("end update test\n");
	}

	@Test
	public void testRemove() throws Exception {
		System.out.println("remove test");
		DAO<MockEntity> dao = TarantoolImpl.getByClass(MockEntity.class);
		MockEntity entity = new MockEntity();
		Map f3 = new HashMap<>();
		f3.put("key", "value");
		entity.setId(0L)
				.setF1("1")//integer string test
				.setF2(2)//integer test
				.setF3(f3)//map test
				.setF7(Arrays.asList("1", 1, "a"));//array test
		dao.save(entity);
		dao.drop(entity);
		try {
			dao.get(0, Collections.singletonList(0)).getObjectIterator().next();
			Assert.assertEquals("Should not get here", true, false);
		} catch (NoSuchElementException ex) {
			System.out.println("Inserted element removed successful");
		}
		dao.save(entity);
		dao.dropById(0l);
		try {
			dao.get(0, Collections.singletonList(0)).getObjectIterator().next();
			Assert.assertEquals("Should not get here", true, false);
		} catch (NoSuchElementException ex) {
			System.out.println("Inserted element removed successful");
		}
		System.out.println("end remove test\n");
	}

	@Test
	public void test() throws Exception {
		System.out.println("test");
		DAO<MockEntity> dao = TarantoolImpl.getByClass(MockEntity.class);
		Integer count = 10;
		Long startInsert = System.nanoTime();
		for (int i = 0; i < count; i++) {
			MockEntity entityEntity = new MockEntity();
			entityEntity.setId((long) i)
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
		System.out.println("end test\n");
	}

	@Test
	public void testMock() throws Exception {
		Long mockId = 46L;
		int linkedToTestMockCount = 0;
		DAO<MockEntity> daoMock = DAOPool.by(MockEntity.class);
		DAO<MockManyEntity> daoManyMock = DAOPool.by(MockManyEntity.class);
		MockEntity mock = new MockEntity();
		mock.setId(mockId);
		mock.setF2(0);
		daoMock.save(mock);

		for (int i = 0; i < 100; i++) {
			MockManyEntity mockMany = new MockManyEntity();
			mockMany.setId((long) i);
			if (i%2==0) {
				linkedToTestMockCount++;
				mockMany.setParent(mock);
			}
			else
				mockMany.setMockEntityId(10L);
			daoManyMock.save(mockMany);
		}
		Assert.assertEquals(linkedToTestMockCount, mock.getEntities().size());
		daoMock.drop(mock);
	}

}
