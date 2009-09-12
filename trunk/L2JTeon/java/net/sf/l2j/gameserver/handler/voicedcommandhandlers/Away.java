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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.AwayManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Siege;

/** 
 * @author Michiru
 * 
 */
public class Away implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "away", "back" };

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(String, net.sf.l2j.gameserver.model.L2PcInstance), String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String text)
	{
		if (command.startsWith("away"))
			return away(activeChar, text);
		else if (command.startsWith("back"))
			return back(activeChar);
		return false;
	}
	public static final int ZONE_PEACE = 2;

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */

	private boolean away(L2PcInstance activeChar, String text)
	{
		Siege siege = SiegeManager.getInstance().getSiege(activeChar);
		//check char is all ready in away mode
		if (activeChar.isAway())
		{
			activeChar.sendMessage("You are allready Away");
			return false;
		}
		
		if (!activeChar.isInsideZone(ZONE_PEACE) && Config.AWAY_PEACE_ZONE)
		{
			activeChar.sendMessage("You can only Away in Peace Zone");
			return false;
		}
		//check player is death/fake death and movement disable
		if (activeChar.isMovementDisabled() || activeChar.isAlikeDead())
			return false;
		// Check if player is in Siege
		if (siege != null && siege.getIsInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go Afk.");
			return false;
		}
		// Check if player is a Cursed Weapon owner
		if (activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("You can't go Afk! You are currently holding a cursed weapon.");
			return false;
		}
		// Check if player is in Duel
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You can't go Afk! You are in a duel!");
			return false;
		}
		//check is in DimensionsRift
		if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You can't go Afk! You are in the dimensional rift.");
			return false;
		}
		/**
		// Check to see if the player is in an event
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You can't go Afk! You are in event now.");
			return false;
		}
		*/
		//check player is in Olympiade
		if (activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() != -1)
		{
			activeChar.sendMessage("You can't go Afk! Your are fighting in Olympiad!");
			return false;
		}
		// Check player is in observer mode
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can't go Afk in Observer mode!");
			return false;
		}
		//check player have karma/pk/pvp status
		if (activeChar.getKarma() > 0 || activeChar.getPvpFlag() > 0)
		{
			activeChar.sendMessage("Player in PVP or with Karma can't use the Away command!");
			return false;
		}
		if (activeChar.isImmobilized())
			return false;
		//check away text have not more then 10 letter
		if (text.length() > 10)
		{
			activeChar.sendMessage("You can't set your status Away with more then 10 letters");
			return false;
		}
		// check if player have no one in target
		if (activeChar.getTarget() == null && text.length() <= 1 || text.length() <= 10)

			//set this Player status away in AwayManager
			AwayManager.getInstance().setAway(activeChar, text);
		return true;
	}

	private boolean back(L2PcInstance activeChar)
	{
		if (!activeChar.isAway())
		{
			activeChar.sendMessage("You are not Away!");
			return false;
		}
		AwayManager.getInstance().setBack(activeChar);
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}