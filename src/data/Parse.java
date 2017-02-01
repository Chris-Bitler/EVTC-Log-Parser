package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import statistics.TableBuilder;

public class Parse {

	// Fields
	private FileInputStream stream = null;
	private MappedByteBuffer f = null;

	// Constructor
	public Parse(File f) throws IOException {
		try {
			this.stream = new FileInputStream(f);
			this.f = stream.getChannel().map(MapMode.READ_ONLY, 0, f.length());
			this.f.order(ByteOrder.LITTLE_ENDIAN);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Public Methods
	public bossData get_boss_data() throws IOException {

		// 12 bytes: version of build
		byte[] version_buffer = new byte[12];
		f.get(version_buffer);

		// 1 byte: skip
		f.position(f.position() + 1);

		// 2 bytes: Boss CID
		int cid = Short.toUnsignedInt(f.getShort());

		// 1 byte: position
		f.position(f.position() + 1);

		return new bossData(0, cid, get_boss_name(cid), get_boss_HP(cid), 0, get_String(version_buffer));
	}

	public List<playerData> get_player_data() throws IOException {

		// playerData array
		List<playerData> p_data = new ArrayList<playerData>();

		// 4 bytes: player count
		int player_count = f.getInt();
		if (!((player_count >= 1) && (player_count <= 10))) {
			System.out.println("Invalid .evtc file.");
			System.exit(0);
		}

		// 96 bytes: each player
		for (int i = 0; i < player_count; i++) {
			// 8 bytes: agent
			long agent = f.getLong();

			// 4 bytes: profession
			int prof_id = f.getInt();

			// 4 bytes: is_elite
			boolean is_elite = get_bool(f.getInt());

			// 4 bytes: toughness
			int toughness = f.getInt() + 1000;

			// 4 bytes: healing
			int healing = f.getInt();

			// 4 bytes: condition
			int condition = f.getInt();

			// 68 bytes: name
			byte[] name_buffer = new byte[68];
			f.get(name_buffer);

			// add player
			p_data.add(new playerData(agent, 0, get_String(name_buffer), get_prof(prof_id, is_elite), toughness,
					healing, condition));
		}
		return p_data;
	}

	public List<skillData> get_skill_data() throws IOException {

		// skillData array
		List<skillData> s_data = new ArrayList<skillData>();

		// 4 bytes: player count
		int skill_count = f.getInt();

		// 68 bytes: each skill
		for (int i = 0; i < skill_count; i++) {
			// 4 bytes: id
			int id = f.getInt();

			// 64 bytes: name
			byte[] name_buffer = new byte[64];
			f.get(name_buffer);

			// add skill
			s_data.add(new skillData(id, get_String(name_buffer)));
		}
		return s_data;
	}

	public List<combatData> get_combat_data() throws IOException {

		// combatData array
		List<combatData> c_data = new ArrayList<combatData>();

		// 64 bytes: each combat
		while (f.remaining() >= 64) {
			// 8 bytes: time
			long time = f.getLong();

			// 8 bytes: src_agent
			long src_agent = f.getLong();

			// 8 bytes: dst_agent
			long dst_agent = f.getLong();

			// 4 bytes: value
			int value = f.getInt();

			// 4 bytes: buff_dmg
			int buff_dmg = f.getInt();

			// 2 bytes: overstack_value
			int overstack_value = f.getShort();

			// 2 bytes: skill_id
			int skill_id = Short.toUnsignedInt(f.getShort());

			// 2 bytes: src_cid
			int src_cid = Short.toUnsignedInt(f.getShort());

			// 2 bytes: dst_cid
			int dst_cid = Short.toUnsignedInt(f.getShort());

			// 2 bytes: src_master_cid
			int src_master_cid = Short.toUnsignedInt(f.getShort());

			// 9 bytes: garbage
			f.position(f.position() + 9);

			// 1 byte: iff
			boolean iff = get_bool(f.get());

			// 1 byte: is_buff
			boolean is_buff = get_bool(f.get());

			// 1 byte: is_crit
			boolean is_crit = get_bool(f.get());

			// 1 byte: is_activation
			int is_activation = f.get();

			// 1 byte: is_buffremove
			boolean is_buffremove = get_bool(f.get());

			// 1 byte: is_ninety
			boolean is_ninety = get_bool(f.get());

			// 1 byte: is_fifty
			boolean is_fifty = get_bool(f.get());

			// 1 byte: is_moving
			boolean is_moving = get_bool(f.get());

			// 1 byte: is_statechange
			int is_statechange = f.get();

			// 4 bytes: garbage + compiler
			f.position(f.position() + 4);

			// add combat
			c_data.add(new combatData(time, src_agent, dst_agent, value, buff_dmg, overstack_value, skill_id, src_cid,
					dst_cid, src_master_cid, iff, is_buff, is_crit, is_activation, is_buffremove, is_ninety, is_fifty,
					is_moving, is_statechange));
		}

		// Close stream
		try {
			this.stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return c_data;
	}

	public void fill_missing_data(bossData b_data, List<playerData> p_data, List<skillData> s_data,
			List<combatData> c_data) {

		// Update boss agent
		for (combatData c : c_data) {
			if (c.get_src_cid() == b_data.getCID()) {
				b_data.setAgent(c.get_src_agent());
				break;
			}
		}

		// Update boss fight duration
		b_data.setFightDuration(c_data.get(c_data.size() - 1).get_time() - c_data.get(0).get_time());

		// Update player CIDs
		for (playerData p : p_data) {
			for (combatData c : c_data) {
				if (p.getAgent() == c.get_src_agent()) {
					if (c.get_src_master_cid() == 0) {
						p.setCID(c.get_src_cid());
					} else {
						p.setCID(c.get_src_master_cid());
					}
					break;
				}
			}
		}

		// Delete players with no CID
		Iterator<playerData> iter = p_data.iterator();
		while (iter.hasNext()) {
			playerData p = iter.next();
			if (p.getCID() == 0) {
				iter.remove();
			}
		}

		// Update combat for Xera logs
		if (b_data.getName().equals("Xera")) {
			long xera_50 = 16286;
			for (combatData c : c_data) {
				if (c.get_src_cid() == xera_50) {
					c.set_src_agent(b_data.getAgent());
					c.set_src_cid(b_data.getCID());
				} else if (c.get_dst_cid() == xera_50) {
					c.set_dst_agent(b_data.getAgent());
					c.set_dst_cid(b_data.getCID());
				}
			}
		}
	}

	public String toString(bossData b_data, List<playerData> p_data, List<skillData> s_data, List<combatData> c_data) {

		// Build tables
		String all_tables = "";
		TableBuilder table = new TableBuilder();

		// Boss Data Table
		table.addTitle("BOSS DATA");
		table.addRow("agent", "CID", "name", "HP", "fight_duration", "version");
		table.addRow(b_data.toStringArray());
		all_tables += table.toString() + "\n";
		table.clear();

		// Player Data
		table.addTitle("PLAYER DATA");
		table.addRow("agent", "CID", "name", "prof", "toughness", "healing", "condition");
		for (playerData p : p_data) {
			table.addRow(p.toStringArray());
		}
		all_tables += table.toString() + "\n";
		table.clear();

		// Skill Data
		table.addTitle("SKILL DATA");
		table.addRow("ID", "name");
		for (skillData s : s_data) {
			table.addRow(s.toStringArray());
		}
		all_tables += table.toString() + "\n";
		table.clear();

		// Combat Data Table
		table.addTitle("COMBAT DATA");
		table.addRow("time", "src_agent", "dst_agent", "value", "buff_dmg", "overstack_value", "skill_id", "src_cid",
				"dst_cid", "src_master_cid", "iff", "is_buff", "is_crit", "is_activation", "is_buffremove",
				"boolean is_ninety", "is_fifty", "is_moving", "is_statechange");
		for (combatData c : c_data) {
			table.addRow(c.toStringArray());
		}
		all_tables += table.toString();

		return all_tables;
	}

	// Private Methods
	private String get_boss_name(int cid) {
		if (cid == 15438) {
			return "Vale Guardian";
		} else if (cid == 15429) {
			return "Gorseval";
		} else if (cid == 15375) {
			return "Sabetha";
		} else if (cid == 16123) {
			return "Slothasor";
		} else if (cid == 16088) {
			return "Berg";
		} else if (cid == 16137) {
			return "Zane";
		} else if (cid == 16125) {
			return "Narella";
		} else if (cid == 16115) {
			return "Matthias";
		} else if (cid == 16235) {
			return "Keep Construct";
		} else if (cid == 16246) {
			return "Xera";
		} else {
			return "UNKNOWN";
		}
	}

	private int get_boss_HP(int cid) {
		if (cid == 15438) {
			return 22400000;
		} else if (cid == 15429) {
			return 21600000;
		} else if (cid == 15375) {
			return 34000000;
		} else if (cid == 16123) {
			return 19000000;
		} else if (cid == 16088) {
			return 6900000;
		} else if (cid == 16137) {
			return 5900000;
		} else if (cid == 16125) {
			return 4900000;
		} else if (cid == 16115) {
			return 25900000;
		} else if (cid == 16235) {
			return 55053600;
		} else if (cid == 16246) {
			return 22611300;
		} else {
			return 0;
		}
	}

	private String get_prof(int prof_id, boolean is_elite) {
		if (prof_id == 1) {
			if (is_elite) {
				return "Dragonhunter";
			} else {
				return "Guardian";
			}
		} else if (prof_id == 2) {
			if (is_elite) {
				return "Berserker";
			} else {
				return "Warrior";
			}
		} else if (prof_id == 3) {
			if (is_elite) {
				return "Scrapper";
			} else {
				return "Engineer";
			}
		} else if (prof_id == 4) {
			if (is_elite) {
				return "Druid";
			} else {
				return "Ranger";
			}
		} else if (prof_id == 5) {
			if (is_elite) {
				return "Daredevil";
			} else {
				return "Thief";
			}
		} else if (prof_id == 6) {
			if (is_elite) {
				return "Tempest";
			} else {
				return "Elementalist";
			}
		} else if (prof_id == 7) {
			if (is_elite) {
				return "Chronomancer";
			} else {
				return "Mesmer";
			}
		} else if (prof_id == 8) {
			if (is_elite) {
				return "Reaper";
			} else {
				return "Necromancer";
			}
		} else if (prof_id == 9) {
			if (is_elite) {
				return "Herald";
			} else {
				return "Revenant";
			}
		} else {
			return "UNKNOWN";
		}
	}

	private String get_String(byte[] bytes) {
		String str;
		try {
			str = new String(bytes, "UTF-8");
			int i = str.indexOf(0);
			return i == -1 ? str : str.substring(0, i);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "UNKNOWN";
	}

	private boolean get_bool(int i) {
		return (i != 0);
	}

}