/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q277_GatekeepersOffering;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q277_GatekeepersOffering extends Quest
{
	private static final String qn = "Q277_GatekeepersOffering";
	
	// NPC
	private static final int TAMIL = 30576;
	
	// Item
	private static final int STARSTONE = 1572;
	
	// Reward
	private static final int GATEKEEPER_CHARM = 1658;
	
	// Monster
	private static final int GRAYSTONE_GOLEM = 20333;
	
	public Q277_GatekeepersOffering(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TAMIL);
		addTalkId(TAMIL);
		
		addKillId(GRAYSTONE_GOLEM);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30576-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 15 && player.getLevel() <= 21)
					htmltext = "30576-02.htm";
				else
				{
					htmltext = "30576-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1 && st.getQuestItemsCount(STARSTONE) < 20)
					htmltext = "30576-04.htm";
				else if (cond == 2 && st.getQuestItemsCount(STARSTONE) >= 20)
				{
					htmltext = "30576-05.htm";
					st.takeItems(STARSTONE, -1);
					st.rewardItems(GATEKEEPER_CHARM, 2);
					st.exitQuest(true);
					st.playSound(QuestState.SOUND_FINISH);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.getInt("cond") == 1)
			if (st.dropQuestItems(STARSTONE, 1, 20, 200000))
				st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q277_GatekeepersOffering(277, qn, "Gatekeeper's Offering");
	}
}