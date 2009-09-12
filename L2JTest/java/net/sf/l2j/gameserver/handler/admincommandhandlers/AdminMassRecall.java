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

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.entity.GmAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Party;


/**
 * This class handles following admin commands:
 * - recallparty
 * - recallclan
 * - recallally
 * 
 * @author  Yamaneko
 */
public class AdminMassRecall implements IAdminCommandHandler 
{
    private static String[] _adminCommands = {
        "admin_recallclan",
        "admin_recallparty",
        "admin_recallally"
    };
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (command.startsWith("admin_recallclan"))
        {
        	try
        	{
            	String val = command.substring(17).trim();
            	
	            L2Clan clan = ClanTable.getInstance().getClanByName(val);
	            if(clan == null)
	            {
	            	activeChar.sendMessage("This clan doesn't exists.");
	            	return true;
	            }
	            

	            L2PcInstance[] m = clan.getOnlineMembers("");
	            for(int i = 0; i < m.length; i++)
	            {
	            	Teleport(m[i], activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
	            }
        	}
        	catch(Exception e)
        	{
        		activeChar.sendMessage("Error in recallclan command.");
        	}
        }
        else if (command.startsWith("admin_recallally"))
        {
        	try
        	{
        		String val = command.substring(17).trim();
        		

	            L2Clan clan = ClanTable.getInstance().getClanByName(val);
	            if(clan == null) {
	            	activeChar.sendMessage("This clan doesn't exists.");
	            	return true;
	            }
	            
	            int ally = clan.getAllyId();
	            
	            if(ally == 0)
	            {

		            L2PcInstance[] m = clan.getOnlineMembers("");
		            for(int i = 0; i < m.length; i++) {
		            	Teleport(m[i], activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
		            }
	            }
	            else
	            {
	            	for(L2Clan aclan : ClanTable.getInstance().getClans())
	            	{
	            		if(aclan.getAllyId() == ally)
	            		{
	            			L2PcInstance[] m = aclan.getOnlineMembers("");
	            			for(int i = 0; i < m.length; i++)
	            			{
	            				Teleport(m[i], activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
	            			}
	            		}
	            	}
	            }
        	}
        	catch(Exception e)
        	{
        		activeChar.sendMessage("Error in recallally command.");
        	}
        }
        else if (command.startsWith("admin_recallparty"))
        {
        	try
        	{
        		String val = command.substring(18).trim();
        		

        		L2PcInstance player = L2World.getInstance().getPlayer(val);
        		if(player == null)
        		{
        			activeChar.sendMessage("Target error.");
        			return true;
        		}
        		if(!player.isInParty())
        		{
        			activeChar.sendMessage("Player is not in party.");
        			return true;
        		}
        		L2Party p = player.getParty();
        		

        		for(L2PcInstance ppl : p.getPartyMembers()) {
	    			Teleport(ppl, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
        		}
        	}
        	catch(Exception e)
        	{
        		activeChar.sendMessage("Error in recallparty command.");
        	}
        }
        return true;
    }
    
    private void Teleport(L2PcInstance player, int X, int Y, int Z, String Message) {
    	player.sendMessage(Message);
    	player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    	player.teleToLocation(X, Y, Z, true);
    }
    
    public String[] getAdminCommandList() 
    {
        return _adminCommands;
    }
}