package net.actionpay.jtom;

import net.actionpay.jtom.annotations.*;
import net.actionpay.jtom.tarantool.IndexType;

/**
 * Created by Khakimov Artur on 27.05.2015.
 */
@Entity(space = "test_many_entity"
		, connection = "keeper")
@Indexes(value = {@Index(indexType = IndexType.INDEX_TYPE_HASH, unique = true, name = "primary")
		, @Index(indexType = IndexType.INDEX_TYPE_TREE, unique = false, name = "mock_entity_id")})
public class MockManyEntity {

	@Key(index="primary", position = 1)
	@Field(position = 0)
	private Long id;

	@Key(index = "mock_entity_id", position = 1)
	@Field(position = 1)
	private Long mockEntityId;

	public Long getMockEntityId() {
		return mockEntityId;
	}

	public MockManyEntity setMockEntityId(Long mockEntityId) {
		this.mockEntityId = mockEntityId;
		return this;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setParent(MockEntity entity){
		mockEntityId = entity.getId();
	}

	public MockEntity getParent() throws Exception {
		return DAOPool.by(MockEntity.class).one(mockEntityId);
	}

	@Override
	public String toString() {
		return "MockManyEntity{" +
				"id=" + id +
				", mockEntityId=" + mockEntityId +
				'}';
	}
}
