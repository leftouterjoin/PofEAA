package asakichy.object_relational.metadata_mapping.repository;

import static asakichy.object_relational.metadata_mapping.DB.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import asakichy.object_relational.structural.AppRuntimeException;

public abstract class Mapper<E extends DomainObject> {
	private static Map<Class<?>, Mapper<?>> mappers = new HashMap<Class<?>, Mapper<?>>();
	static {
		mappers.put(Person.class, new PersonMapper());
	}

	public static Mapper<?> getMapper(Class<?> klass) {
		return mappers.get(klass);
	}

	protected MetaDataMap dataMap;

	public Mapper() {
		loadDataMap();
	}

	protected abstract void loadDataMap();

	public E findObject(long id) {
		String sql = "SELECT" + dataMap.columnList() + " FROM " + dataMap.getTableName() + " WHERE id = ?";
		try {
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setLong(1, id);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			return load(rs);
		} catch (Exception e) {
			throw new AppRuntimeException(e);
		}
	}

	public Set<E> findObjectWhere(String whereClause) {
		String sql = "SELECT" + dataMap.columnList() + " FROM " + dataMap.getTableName() + " WHERE " + whereClause;
		try {
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			return loadAll(rs);
		} catch (Exception e) {
			throw new AppRuntimeException(e);
		}
	}

	private Set<E> loadAll(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
		Set<E> results = new HashSet<E>();
		while (rs.next()) {
			results.add(load(rs));
		}
		return results;
	}

	private E load(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
		@SuppressWarnings("unchecked")
		E result = (E) dataMap.getDomainClass().newInstance();
		result.setId(rs.getLong("id"));
		loadFields(rs, result);
		return result;
	}

	private void loadFields(ResultSet rs, E result) throws SQLException {
		for (ColumnMap column : dataMap.getColumns()) {
			Object columnValue = rs.getObject(column.getColumnName());
			column.setField(result, columnValue);
		}
	}

	public void update(E domainObject) {
		String sql = "UPDATE " + dataMap.getTableName() + dataMap.getUpdateList() + " WHERE id = ?";
		try {
			PreparedStatement stmt = prepareStatement(sql);
			int index = 0;
			for (ColumnMap column : dataMap.getColumns()) {
				stmt.setObject(++index, column.getValue(domainObject));
			}
			stmt.setLong(++index, domainObject.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new AppRuntimeException(e);
		}
	}

	public void insert(E domainObject) {
		String sql = "INSERT INTO " + dataMap.getTableName() + " VALUES (?" + dataMap.getInsertList() + ")";
		try {
			PreparedStatement stmt = prepareStatement(sql);
			int index = 0;
			stmt.setLong(++index, domainObject.getId());
			for (ColumnMap column : dataMap.getColumns()) {
				stmt.setObject(++index, column.getValue(domainObject));
			}
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new AppRuntimeException(e);
		}
	}

	public void delete(E domainObject) {
		String sql = "DELETE FROM " + dataMap.getTableName() + " WHERE id=?";
		try {
			PreparedStatement stmt = prepareStatement(sql);
			stmt.setLong(1, domainObject.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new AppRuntimeException(e);
		}
	}

	public MetaDataMap getDataMap() {
		return dataMap;
	}

}
