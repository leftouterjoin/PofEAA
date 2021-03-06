package asakichy.object_relational.structural.concrete_table_inheritance;

/**
 * FootBallerドメインオブジェクト.
 */

public class FootBaller extends Player {
	private String club;

	public FootBaller(long id) {
		super(id, TYPE.FOOTBALL);
	}

	public FootBaller() {
		this(-1);
	}

	public String getClub() {
		return club;
	}

	public void setClub(String club) {
		this.club = club;
	}

}
