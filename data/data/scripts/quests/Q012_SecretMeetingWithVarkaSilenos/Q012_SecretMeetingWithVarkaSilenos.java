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
package quests.Q012_SecretMeetingWithVarkaSilenos;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;

public class Q012_SecretMeetingWithVarkaSilenos extends Quest
{
	private static final String qn = "Q012_SecretMeetingWithVarkaSilenos";
	
	// NPCs
	private final static int CADMON = 31296;
	private final static int HELMUT = 31258;
	private final static int NARAN_ASHANUK = 31378;
	
	// Items
	private final static int MUNITIONS_BOX = 7232;
	
	public Q012_SecretMeetingWithVarkaSilenos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			MUNITIONS_BOX
		};
		
		addStartNpc(CADMON);
		addTalkId(CADMON, HELMUT, NARAN_ASHANUK);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31296-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31258-02.htm"))
		{
			st.giveItems(MUNITIONS_BOX, 1);
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31378-02.htm"))
		{
			st.takeItems(MUNITIONS_BOX, 1);
			st.addExpAndSp(79761, 0);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				if (player.getLevel() >= 74)
					htmltext = "31296-01.htm";
				else
				{
					htmltext = "31296-02.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case CADMON:
						if (cond == 1)
							htmltext = "31296-04.htm";
						break;
					
					case HELMUT:
						if (cond == 1)
							htmltext = "31258-01.htm";
						else if (cond == 2)
							htmltext = "31258-03.htm";
						break;
					
					case NARAN_ASHANUK:
						if (cond == 2 && st.getQuestItemsCount(MUNITIONS_BOX) > 0)
							htmltext = "31378-01.htm";
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = Quest.getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q012_SecretMeetingWithVarkaSilenos(12, qn, "Secret Meeting With Varka Silenos");
	}
}