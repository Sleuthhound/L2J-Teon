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
package net.sf.l2j.gameserver.instancemanager.clanhallsiege;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.ExclusiveTask;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.serverpackets.CreatureSay2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Author: Maxi
 * 
 */

public class FortressofTheDeadManager extends ClanHallSiege
{
	protected static Log					_log				= LogFactory.getLog(FortressofTheDeadManager.class.getName());
	private static FortressofTheDeadManager _instance;
	private boolean	_registrationPeriod	= false;
	private int _clanCounter		= 0;
	private Map<Integer, clanPlayersInfo> _clansInfo = new HashMap<Integer, clanPlayersInfo>();
	private L2ClanHallZone zone;/* = ZoneManager.getInstance().getZone(L2Zone.ZoneType.Clanhall, "Bandits Stronghold");*/
	public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(64);
	private clanPlayersInfo	_ownerClanInfo = new clanPlayersInfo();
	private boolean	_finalStage = false;
	private ScheduledFuture<?> _midTimer;
	
	public static final FortressofTheDeadManager getInstance()
	{
		if (_instance == null)
			_instance = new FortressofTheDeadManager();
		return _instance;
	}
	private FortressofTheDeadManager()
	{
		_log.info("Fortress of The Dead");
		long siegeDate = restoreSiegeDate(64);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate,64,22);
		// Schedule siege auto start
		_startSiegeTask.schedule(1000);
	}
	public void startSiege()
	{
		if (GameServer._instanceOk)
		{
			setRegistrationPeriod(false);
			if(_clansInfo.size() == 0)
			{
				endSiege(false);
				return;
			}
			if((_clansInfo.size() == 1) && (clanhall.getOwnerClan() == null))
			{
				endSiege(false);
				return;
			}
			if((_clansInfo.size() == 1) && (clanhall.getOwnerClan()!=null))
			{
				L2Clan clan = null;
				for (clanPlayersInfo a : _clansInfo.values())
					clan = ClanTable.getInstance().getClanByName(a._clanName);
				setIsInProgress(true);
				/*((L2ClanhallZone)zone).updateSiegeStatus();*/
				startSecondStep(clan);
				anonce("Take place at the siege of his headquarters.",1);
				_siegeEndDate = Calendar.getInstance();
				_siegeEndDate.add(Calendar.MINUTE, 30);
				_endSiegeTask.schedule(1000);
				return;
			}
			setIsInProgress(true);
			/*((L2ClanhallZone)zone).updateSiegeStatus();*/
			//spawnFlags();
			//gateControl(1);
			anonce("Take place at the siege of his headquarters.",1);
			ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5*60000);
			_midTimer=ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 25*60000);
			
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 60);
			_endSiegeTask.schedule(1000);
		}
	}
	public void startSecondStep(L2Clan winner)
	{
		FastList<String> winPlayers = FortressofTheDeadManager.getInstance().getRegisteredPlayers(winner);
		//unSpawnAll();
		_clansInfo.clear();
		clanPlayersInfo regPlayers = new clanPlayersInfo();
		regPlayers._clanName=winner.getName();
		regPlayers._players=winPlayers;
		_clansInfo.put(winner.getClanId(), regPlayers);
		_clansInfo.put(clanhall.getOwnerClan().getClanId(),_ownerClanInfo);
		//spawnFlags();
		//gateControl(1);
		_finalStage = true;
		anonce("Take place at the siege of his headquarters.",1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5*60000);
	}
	public void endSiege(boolean par)
	{
		_mobControlTask.cancel();
		_finalStage = false;
		if (par)
		{
			L2Clan winner=checkHaveWinner();
			if(winner!=null)
			{
				ClanHallManager.getInstance().setOwner(clanhall.getId(),winner);
				anonce("Attention! Clan hall, castle was conquered by the clan of robbers "+winner.getName(),2);
			}
			else
				anonce("Attention! Clan hall, Fortress robbers did not get a new owner",2);
		}
		setIsInProgress(false);
		/*((L2ClanhallZone)zone).updateSiegeStatus();*/
		//unSpawnAll();
		_clansInfo.clear();
		_clanCounter = 0;
		teleportPlayers();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(),64,22);
		_startSiegeTask.schedule(1000);
	}

	public void unSpawnAll()
	{
		for(String clanName:getRegisteredClans())
		{
			L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
			L2MonsterInstance mob=getQuestMob(clan);
			L2SiegeFlagInstance flag=getSiegeFlag(clan);
			if (mob!=null)
				mob.deleteMe();
			if (flag!=null)
				flag.deleteMe();
		}
	}

	public void gateControl(int val)
	{
		if (val==1)
		{
			DoorTable.getInstance().getDoor(22170001).openMe();
			DoorTable.getInstance().getDoor(22170002).openMe();
			DoorTable.getInstance().getDoor(22170003).closeMe();
			DoorTable.getInstance().getDoor(22170004).closeMe();
		}
		else if (val==2)
		{
			DoorTable.getInstance().getDoor(22170001).closeMe();
			DoorTable.getInstance().getDoor(22170002).closeMe();
			DoorTable.getInstance().getDoor(22170003).closeMe();
			DoorTable.getInstance().getDoor(22170004).closeMe();
		}
	}

	public void teleportPlayers()
	{
		for(L2Character cha : zone.getCharactersInside().values())
			if (cha instanceof L2PcInstance)
			{
				L2Clan clan=((L2PcInstance)cha).getClan();
				if (!isPlayerRegister(clan,cha.getName()))
					cha.teleToLocation(88404, -21821, -2276);
			}
	}

	public L2Clan checkHaveWinner()
	{
		L2Clan res=null;
		int questMobCount=0;
		for(String clanName:getRegisteredClans())
		{
			L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
			if (getQuestMob(clan)!=null)
			{
				res=clan;
				questMobCount++;
			}
		}
		if (questMobCount>1)
			return null;
		return res;
	}

	private class midSiegeStep implements Runnable 
	{
		public void run()
		{
			_mobControlTask.cancel();
			L2Clan winner=checkHaveWinner();
			if(winner!=null)
			{
				if (clanhall.getOwnerClan()==null)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(),winner);
					anonce("Attention! Clan hall, castle was conquered by the clan of robbers "+winner.getName(),2);
					endSiege(false);
				}
				else
					startSecondStep(winner);
			}
			else
			{
				endSiege(true);
			}
		}
	}

	private class startFirstStep implements Runnable 
	{
		public void run()
		{
			teleportPlayers();
			gateControl(2);
			int mobCounter=1;
			for(String clanName:getRegisteredClans())
			{
				L2NpcTemplate template;
				L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
				template = NpcTable.getInstance().getTemplate(35427+mobCounter);
				/*template.setServerSideTitle(true);
				template.setTitle(clan.getName());*/
				L2MonsterInstance questMob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
				questMob.setHeading(100);
				questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
				if (mobCounter==1)
					questMob.spawnMe(83752,-17354,-1828);
				else if (mobCounter==2)
					questMob.spawnMe(82018,-15126,-1829);
				else if (mobCounter==3)
					questMob.spawnMe(85320,-16191,-1823);
				else if (mobCounter==4)
					questMob.spawnMe(81522,-16503,-1829);
				else if (mobCounter==5)
					questMob.spawnMe(83786,-15369,-1828);
				clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._mob=questMob;
				mobCounter++;
			}
			_mobControlTask.schedule(3000);
			anonce("The battle began. Kill the enemy NPC",1);
		}
	}

	public void spawnFlags()
	{/*
		int flagCounter=1;
		for(String clanName:getRegisteredClans())
		{
			L2NpcTemplate template;
			L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
			if (clan==clanhall.getOwnerClan())
				template = NpcTable.getInstance().getTemplate(35422);
			else
				template = NpcTable.getInstance().getTemplate(35422+flagCounter);
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(null, IdFactory.getInstance().getNextId(), template, false,true,clan);
			flag.setTitle(clan.getName());
			flag.setHeading(100);
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			if (clan==clanhall.getOwnerClan())
				flag.spawnMe(81700,-16300,-1828);
			else
			{
				if (flagCounter==1)
					flag.spawnMe(83452,-17654,-1828);
				else if (flagCounter==2)
					flag.spawnMe(81718,-14826,-1829);
				else if (flagCounter==3)
					flag.spawnMe(85020,-15891,-1823);
				else if (flagCounter==4)
					flag.spawnMe(81222,-16803,-1829);
				else if (flagCounter==5)
					flag.spawnMe(83486,-15069,-1828);
			}
			clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
			regPlayers._flag=flag;
			flagCounter++;
		}*/
	}

	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}

	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}

	public boolean isPlayerRegister(L2Clan playerClan,String playerName)
	{
		if (playerClan==null)
			return false;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
				return true;
		return false;
	}

	public boolean isClanOnSiege(L2Clan playerClan)
	{
		if (playerClan==clanhall.getOwnerClan())
			return true;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers == null)
		{
			return false;
		}
		return true;
	}

	public synchronized int registerClanOnSiege(L2PcInstance player,L2Clan playerClan)
	{
		if (_clanCounter==5)
			return 2;
		{
			_clanCounter++;
			clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
			if (regPlayers == null)
			//{
				regPlayers = new clanPlayersInfo();
				regPlayers._clanName=playerClan.getName();
				_clansInfo.put(playerClan.getClanId(), regPlayers);
			//}
		}
		//else
			return 1;
		//return 0;
	}

	public boolean unRegisterClan(L2Clan playerClan)
	{
		if(_clansInfo.remove(playerClan.getClanId())!=null)
		{
			_clanCounter--;
			return true;
		}
		return false;
	}

	public FastList<String> getRegisteredClans()
	{
		FastList<String> clans=new FastList<String>();
		for (clanPlayersInfo a : _clansInfo.values())
		{
			clans.add(a._clanName);
		}
		return clans;
	}

	public FastList<String> getRegisteredPlayers(L2Clan playerClan)
	{
		if (playerClan==clanhall.getOwnerClan())
			return _ownerClanInfo._players;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			return regPlayers._players;
		return null;
	}

	public L2SiegeFlagInstance getSiegeFlag(L2Clan playerClan)
	{
		clanPlayersInfo clanInfo=_clansInfo.get(playerClan.getClanId());
		if(clanInfo!=null)
			return clanInfo._flag;
		return null;
	}

	public L2MonsterInstance getQuestMob(L2Clan clan)
	{
		clanPlayersInfo clanInfo=_clansInfo.get(clan.getClanId());
		if(clanInfo!=null)
			return clanInfo._mob;
		return null;
	}

	public int getPlayersCount(String playerClan)
	{
		for (clanPlayersInfo a : _clansInfo.values())
			if(a._clanName==playerClan)
				return a._players.size();
		return 0;
	}

	public void addPlayer(L2Clan playerClan,String playerName)
	{
		if (playerClan==clanhall.getOwnerClan())
			if (_ownerClanInfo._players.size()<18)
				if (!_ownerClanInfo._players.contains(playerName))
				{
					_ownerClanInfo._players.add(playerName);
					return;
				}
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.size()<18)
				if (!regPlayers._players.contains(playerName))
					regPlayers._players.add(playerName);
	}

	public void removePlayer(L2Clan playerClan,String playerName)
	{
		if (playerClan==clanhall.getOwnerClan())
			if (_ownerClanInfo._players.contains(playerName))
			{
				_ownerClanInfo._players.remove(playerName);
				return;
			}
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
				regPlayers._players.remove(playerName);
	}

	private final ExclusiveTask _startSiegeTask = new ExclusiveTask(){
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}
			Calendar siegeStart=Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			siegeStart.add(Calendar.HOUR, 1);
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			long remaining=registerTimeRemaining;
			if (registerTimeRemaining <= 0)
			{
				if (!isRegistrationPeriod())
				{
					if (clanhall.getOwnerClan()!=null)
						_ownerClanInfo._clanName=clanhall.getOwnerClan().getName();
					else
						_ownerClanInfo._clanName="";
					setRegistrationPeriod(true);
					anonce("Attention! The period of registration at the siege clan hall, castle robbers.",2);
					remaining=siegeTimeRemaining;
				}
			}
			if (siegeTimeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);
		}
	};

	public void anonce(String text,int type)
	{
		if (type==1)
		{
			CreatureSay2 cs = new CreatureSay2(0, SystemChatChannelId.Chat_Shout, "Journal", text);
			for(String clanName:getRegisteredClans())
			{
				L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
				for(String playerName:getRegisteredPlayers(clan))
				{
					L2PcInstance cha = L2World.getInstance().getPlayer(playerName);
					if (cha!=null)
						cha.sendPacket(cs);
				}
			}
		}
		else
		{
			CreatureSay2 cs = new CreatureSay2(0, SystemChatChannelId.Chat_Shout, "Journal", text);
			//L2MapRegion region = MapRegionManager.getInstance().getRegion(88404, -21821, -2276);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if /*(region == MapRegionManager.getInstance().getRegion(player.getX(), player.getY(), player.getZ())
						&& */(player.getInstanceId() == 0/*)*/)
				{
					player.sendPacket(cs);
				}
			}
		}
	}

	private final ExclusiveTask _endSiegeTask = new ExclusiveTask() {
		@Override
		protected void onElapsed()
		{
			if (!getIsInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(true);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};

	private final ExclusiveTask _mobControlTask = new ExclusiveTask() {
		@Override
		protected void onElapsed()
		{
			int mobCount=0;
			for(clanPlayersInfo cl:_clansInfo.values())
				if (cl._mob.isDead())
				{
					L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
					unRegisterClan(clan);
				}
				else
					mobCount++;
			teleportPlayers();
			if (mobCount<2)
				if (_finalStage)
				{
					_siegeEndDate = Calendar.getInstance();
					_endSiegeTask.cancel();
					_endSiegeTask.schedule(5000);
				}
				else
				{
					_midTimer.cancel(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 5000);
				}
			else
				schedule(3000);
		}
	};

	private class clanPlayersInfo
	{
		public String _clanName;
		public L2SiegeFlagInstance _flag = null;
		public L2MonsterInstance _mob = null;
		public FastList<String> _players	= new FastList<String>();
	}
}