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
package quests.Q272_WrathOfAncestors;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q272_WrathOfAncestors extends Quest
{
	private static final String qn = "Q272_WrathOfAncestors";
	
	// NPCs
	private static final int LIVINA = 30572;
	
	// Monsters
	private static final int GOBLIN_GRAVE_ROBBER = 20319;
	private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
	
	// Item
	private static final int GRAVE_ROBBERS_HEAD = 1474;
	
	// Reward
	private static final int ADENA = 57;
	
	public Q272_WrathOfAncestors(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			GRAVE_ROBBERS_HEAD
		};
		
		addStartNpc(LIVINA);
		addTalkId(LIVINA);
		
		addKillId(GOBLIN_GRAVE_ROBBER, GOBLIN_TOMB_RAIDER_LEADER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30572-03.htm"))
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
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 3)
				{
					if (player.getLevel() >= 5 && player.getLevel() <= 16)
						htmltext = "30572-02.htm";
					else
					{
						htmltext = "30572-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30572-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				if (st.getQuestItemsCount(GRAVE_ROBBERS_HEAD) < 50)
					htmltext = "30572-04.htm";
				else
				{
					htmltext = "30572-05.htm";
					st.takeItems(GRAVE_ROBBERS_HEAD, -1);
					st.rewardItems(ADENA, 1500);
					st.exitQuest(true);
					st.playSound(QuestState.SOUND_FINISH);
				}
				break;
			
			case State.COMPLETED:
				htmltext = Quest.getAlreadyCompletedMsg();
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
			if (st.dropAlwaysQuestItems(GRAVE_ROBBERS_HEAD, 1, 50))
				st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q272_WrathOfAncestors(272, qn, "Wrath of Ancestors");
	}
}