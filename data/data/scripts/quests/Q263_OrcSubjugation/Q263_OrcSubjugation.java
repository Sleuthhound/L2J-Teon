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
package quests.Q263_OrcSubjugation;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;
import net.sf.l2j.util.Rnd;

public class Q263_OrcSubjugation extends Quest
{
	private final static String qn = "Q263_OrcSubjugation";
	
	// Items
	private static final int ORC_AMULET = 1116;
	private static final int ORC_NECKLACE = 1117;
	
	public Q263_OrcSubjugation(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			ORC_AMULET,
			ORC_NECKLACE
		};
		
		addStartNpc(30346);
		addTalkId(30346);
		
		addKillId(20385, 20386, 20387, 20388);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30346-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30346-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 2)
				{
					if (player.getLevel() >= 8 && player.getLevel() <= 16)
						htmltext = "30346-02.htm";
					else
					{
						htmltext = "30346-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30346-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				int amulet = st.getQuestItemsCount(ORC_AMULET);
				int necklace = st.getQuestItemsCount(ORC_NECKLACE);
				
				if (amulet == 0 && necklace == 0)
					htmltext = "30346-04.htm";
				else
				{
					htmltext = "30346-05.htm";
					st.rewardItems(57, amulet * 20 + necklace * 30);
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
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
		
		if (st.isStarted() && Rnd.get(10) > 4)
		{
			st.giveItems((npc.getNpcId() == 20385) ? ORC_AMULET : ORC_NECKLACE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q263_OrcSubjugation(263, qn, "Orc Subjugation");
	}
}