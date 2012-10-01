/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Micht
 */
public class CursedWeaponsManager
{
	private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());
	
	public static final CursedWeaponsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<Integer, CursedWeapon> _cursedWeapons;
	
	public CursedWeaponsManager()
	{
		_cursedWeapons = new FastMap<>();
		init();
	}
	
	public final void reload()
	{
		_cursedWeapons.clear();
		init();
	}
	
	private void init()
	{
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			_log.info("CursedWeaponsManager: Skipping loading.");
			return;
		}
		
		load();
		restore();
	}
	
	private final void load()
	{
		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/data/xml/cursed_weapons.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(file);
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("item".equalsIgnoreCase(d.getNodeName()))
				{
					NamedNodeMap attrs = d.getAttributes();
					int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
					String name = attrs.getNamedItem("name").getNodeValue();
					
					CursedWeapon cw = new CursedWeapon(id, skillId, name);
					
					int val;
					for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
					{
						if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
							cw.setDropRate(val);
						}
						else if ("duration".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
							cw.setDuration(val);
						}
						else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
							cw.setDurationLost(val);
						}
						else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
							cw.setDisapearChance(val);
						}
						else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
							cw.setStageKills(val);
						}
					}
					
					// Store cursed weapon
					_cursedWeapons.put(id, cw);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error parsing cursed_weapons.xml: ", e);
		}
		_log.info("CursedWeaponsManager: Loaded " + _cursedWeapons.size() + " cursed weapons.");
	}
	
	private final void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int itemId = rset.getInt("itemId");
				int playerId = rset.getInt("playerId");
				int playerKarma = rset.getInt("playerKarma");
				int playerPkKills = rset.getInt("playerPkKills");
				int nbKills = rset.getInt("nbKills");
				long endTime = rset.getLong("endTime");
				
				CursedWeapon cw = _cursedWeapons.get(itemId);
				cw.setPlayerId(playerId);
				cw.setPlayerKarma(playerKarma);
				cw.setPlayerPkKills(playerPkKills);
				cw.setNbKills(nbKills);
				cw.setEndTime(endTime);
				cw.reActivate(true);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore CursedWeapons data: " + e.getMessage(), e);
		}
	}
	
	public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (attackable instanceof L2SiegeGuardInstance || attackable instanceof L2RiftInvaderInstance || attackable instanceof L2FestivalMonsterInstance || attackable instanceof L2GrandBossInstance || attackable instanceof L2FeedableBeastInstance)
			return;
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActive())
				continue;
			
			if (cw.checkDrop(attackable, player))
				break;
		}
	}
	
	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if (player.isCursedWeaponEquipped()) // cannot own 2 cursed swords
		{
			CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();
			
			// erase the newly obtained cursed weapon
			cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
			cw.endOfLife(); // expire the weapon and clean up.
		}
		else
			cw.activate(player, item);
	}
	
	public void drop(int itemId, L2Character killer)
	{
		_cursedWeapons.get(itemId).dropIt(killer);
	}
	
	public void increaseKills(int itemId)
	{
		_cursedWeapons.get(itemId).increaseKills();
	}
	
	public int getLevel(int itemId)
	{
		return _cursedWeapons.get(itemId).getLevel();
	}
	
	public void checkPlayer(L2PcInstance player)
	{
		if (player == null)
			return;
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquippedId(cw.getItemId());
			}
		}
	}
	
	public static void removeFromDb(int itemId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete datas
			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "CursedWeaponsManager: Failed to remove data: " + e.getMessage(), e);
		}
	}
	
	public void saveData()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
			cw.saveData();
	}
	
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}
	
	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}
	
	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}
	
	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}
	
	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager _instance = new CursedWeaponsManager();
	}
}