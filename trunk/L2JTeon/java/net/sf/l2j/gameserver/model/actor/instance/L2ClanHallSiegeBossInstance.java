/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.FortResistSiegeManager;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

/**
 * This class manages all RaidBoss.
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public final class L2ClanHallSiegeBossInstance extends L2MonsterInstance
{
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000; // 30 sec

	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2Npc constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2ClanHallSiegeBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    @Override
	public void onSpawn()
    {
    	super.onSpawn();
    }

    @Override
    protected int getMaintenanceInterval() { return RAIDBOSS_MAINTENANCE_INTERVAL; }

    @Override
    public boolean doDie(L2Character killer)
    {
    	if (!super.doDie(killer))
    		return false;

    	if (getNpcId() == 35368 || (getNpcId() == 35368))
    		FortResistSiegeManager.getInstance().endSiege(true);

    	if (getNpcId() == 35410)
    		DevastatedCastleManager.getInstance().endSiege(true);

        return true;
    }

    /**
     * Spawn all minions at a regular interval
     * Also if boss is too far from home location at the time of this check, teleport it home
     *
     */
    @Override
    protected void manageMinions()
    {
        _minionList.spawnMinions();
        _minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
        {
        	public void run()
            {
                // teleport raid boss home if it's too far from home location
                L2Spawn bossSpawn = getSpawn();
                if(!isInsideRadius(bossSpawn.getLocx(),bossSpawn.getLocy(),bossSpawn.getLocz(), 5000, true, false))
                {
                    teleToLocation(bossSpawn.getLocx(),bossSpawn.getLocy(),bossSpawn.getLocz(), true);
                    healFull(); // prevents minor exploiting with it
                }
                _minionList.maintainMinions();
            }
        }, 60000, getMaintenanceInterval()+Rnd.get(5000));
    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
     */
    @Override
    public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
        super.reduceCurrentHp(damage, attacker, awake);

        if (this.getNpcId() == 35368 || (this.getNpcId() == 35375))
		{
			if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker).getClan()!= null)
				FortResistSiegeManager.getInstance().addSiegeDamage(((L2PcInstance)attacker).getClan(), damage);
			} else
		{
        if (this.getNpcId() == 35410)
		{
			if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker).getClan()!= null)
				DevastatedCastleManager.getInstance().addSiegeDamage(((L2PcInstance)attacker).getClan(), damage);
			}
		}
    }

    public void healFull()
    {
        super.setCurrentHp(super.getMaxHp());
        super.setCurrentMp(super.getMaxMp());
    }
}