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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 52 };
	private static final int REQUIRED_LEVEL = Config.GM_ESCAPE;

	/**
	 * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode())
		{
			return false;
		}
		int unstuckTimer = activeChar.getAccessLevel() >= REQUIRED_LEVEL ? 1000 : Config.UNSTUCK_INTERVAL * 1000;
		// int unstuckTimer = (activeChar.getAccessLevel() ? 1000 : Config.UNSTUCK_INTERVAL * 1000);
		// int unstuckTimer = activeChar.getAccessLevel() >= REQUIRED_LEVEL ? 5000 : Config.UNSTUCK_INTERVAL * 1000;
		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape command in a festival.");
			return false;
		}
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You may not use an escape skill in a Event.");
			return false;
		}
		// Check to see if the player is in faction.
		if (Config.ENABLE_FACTION_KOOFS_NOOBS)
		{
			if (activeChar.isNoob() || activeChar.isKoof())
			{
				activeChar.sendMessage("You may not use an escape command in Faction mode.");
				return false;
			}
		}
		// Check to see if player is in jail
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("You can not escape from jail.");
			return false;
		}
		if (activeChar.inClanEvent || activeChar.inPartyEvent || activeChar.inSoloEvent)
		{
			activeChar.sendPacket(SystemMessage.sendString("You can't escape while in Event."));
			return false;
		}
		if (GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
		{
			activeChar.sendMessage("You may not use an escape command in a Boss Zone.");
			return false;
		}
		if (activeChar.getAccessLevel() >= REQUIRED_LEVEL)
		{
			L2Skill GM_escape = SkillTable.getInstance().getInfo(2100, 1); // 1 second escape
			if (GM_escape != null)
			{
				activeChar.doCast(GM_escape);
				activeChar.sendMessage("You use Escape: 1 second.");
				return true;
			}
		}
		else if (Config.UNSTUCK_INTERVAL == 300)
		{
			L2Skill escape = SkillTable.getInstance().getInfo(2099, 1); // 5 minutes escape
			if (escape != null)
			{
				activeChar.doCast(escape);
				return true;
			}
		}
		else
		{
			if (Config.UNSTUCK_INTERVAL > 100)
			{
				activeChar.sendMessage("You use Escape: " + unstuckTimer / 60000 + " minutes.");
			} else {
				activeChar.sendMessage("You use Escape: " + unstuckTimer / 1000 + " seconds.");
			}
		}
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/* 900 */);
		SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
		return true;
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;

		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}

		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			_activeChar.setIsIn7sDungeon(false);
			_activeChar.enableAllSkills();
			try
			{
				if (_activeChar.isKoof()) {
					_activeChar.teleToLocation(146334, 25767, -2013);
				} else if (_activeChar.isNoob()) {
					_activeChar.teleToLocation(59669, -42221, -2992);
				} else {
					_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
			catch (Throwable e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}