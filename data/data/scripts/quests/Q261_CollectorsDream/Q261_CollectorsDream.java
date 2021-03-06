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
package quests.Q261_CollectorsDream;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q261_CollectorsDream extends Quest
{
	private static final String qn = "Q261_CollectorsDream";
	
	// NPC
	private final static int ALSHUPES = 30222;
	
	// Items
	private final static int GIANT_SPIDER_LEG = 1087;
	
	// Reward
	private final static int ADENA = 57;
	
	public Q261_CollectorsDream(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			GIANT_SPIDER_LEG
		};
		
		addStartNpc(ALSHUPES);
		addTalkId(ALSHUPES);
		
		addKillId(20308, 20460, 20466);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30222-03.htm"))
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
					htmltext = "30222-02.htm";
				else
				{
					htmltext = "30222-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				if (st.getInt("cond") == 2)
				{
					htmltext = "30222-05.htm";
					st.takeItems(GIANT_SPIDER_LEG, -1);
					st.rewardItems(ADENA, 1000);
					st.addExpAndSp(2000, 0);
					st.exitQuest(true);
					st.playSound(QuestState.SOUND_FINISH);
				}
				else
					htmltext = "30222-04.htm";
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
			if (st.dropAlwaysQuestItems(GIANT_SPIDER_LEG, 1, 8))
				st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q261_CollectorsDream(261, qn, "Collector's Dream");
	}
}