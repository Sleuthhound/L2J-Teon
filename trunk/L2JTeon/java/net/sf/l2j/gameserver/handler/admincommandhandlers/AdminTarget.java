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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - target name = sets player with respective name as target
 *
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminTarget implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_target" };
	private static final int REQUIRED_LEVEL = Config.GM_MIN;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
				return false;
		if (command.startsWith("admin_target"))
			handleTarget(command, activeChar);
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return level >= REQUIRED_LEVEL;
	}

	private void handleTarget(String command, L2PcInstance activeChar)
	{
		try
		{
			String targetName = command.substring(13);
			L2Object obj = L2World.getInstance().getPlayer(targetName);
			if (obj != null && obj instanceof L2PcInstance)
			{
				obj.onAction(activeChar);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Player " + targetName + " not found");
				activeChar.sendPacket(sm);
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Please specify correct name.");
			activeChar.sendPacket(sm);
		}
	}
}
