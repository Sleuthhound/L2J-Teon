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
package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_REST;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class L2PlayerAI extends L2CharacterAI
{
	private boolean _thinking; // to prevent recursive thinking

    //private Stack<IntentionCommand> _interruptedIntentions = new Stack<IntentionCommand>();
    IntentionCommand _nextIntention = null;

	public L2PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}

    void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
    {
        /*
        if (Config.DEBUG)
        _log.warning("L2PlayerAI: changeIntention -> Saving next intention: " + _intention + " " + _intention_arg0 + " " + _intention_arg1);
        */

    	_nextIntention = new IntentionCommand(intention, arg0, arg1);
    }

    @Override
    public IntentionCommand getNextIntention()
    {
    	return _nextIntention;
    }

	/**
	 * Saves the current Intention for this L2PlayerAI if necessary and calls changeIntention in AbstractAI.<BR>
	 * <BR>
	 *
	 * @param intention
	 *            The new Intention to set to the AI
	 * @param arg0
	 *            The first parameter of the Intention
	 * @param arg1
	 *            The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		/*
		 * if (Config.DEBUG) _log.warning("L2PlayerAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */
        // do nothing unless CAST intention
    	// however, forget interrupted actions when starting to use an offensive skill
        if (intention != AI_INTENTION_CAST || arg0 != null && ((L2Skill)arg0).isOffensive())
        {
        	_nextIntention = null;
        	super.changeIntention(intention, arg0, arg1);
            return;
        }
		// do nothing if next intention is same as current one.
		if (intention == _intention && arg0 == _intentionArg0 && arg1 == _intentionArg1)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
        // save current intention so it can be used after cast
        saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		super.changeIntention(intention, arg0, arg1);
	}

    /**
     * Launch actions corresponding to the Event ReadyToAct.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Launch actions corresponding to the Event Think</li><BR><BR>
     *
     */
    @Override
	protected void onEvtReadyToAct()
    {
        // Launch actions corresponding to the Event Think
    	if (_nextIntention != null)
    	{
    		setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
    		_nextIntention = null;
    	}
    	super.onEvtReadyToAct();
    }

    /**
     * Launch actions corresponding to the Event Cancel.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR>
     * <li>Stop an AI Follow Task</li>
     * <li>Launch actions corresponding to the Event Think</li><BR><BR>
     *
     */
    @Override
	protected void onEvtCancel()
    {
    	_nextIntention = null;
    	super.onEvtCancel();
    }

    /**
     * Finalize the casting of a skill. This method overrides L2CharacterAI method.<BR><BR>
     *
     * <B>What it does:</B>
     * Check if actual intention is set to CAST and, if so, retrieves latest intention
     * before the actual CAST and set it as the current intention for the player
     */
    @Override
	protected void onEvtFinishCasting()
    {
        if (getIntention() == AI_INTENTION_CAST)
        {
            // run interrupted or next intention
            if (_nextIntention != null)
            {
                if (_nextIntention._crtlIntention != AI_INTENTION_CAST) // previous state shouldn't be casting
                {
                	setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
                } else {
					setIntention(AI_INTENTION_IDLE);
				}
            }
            else
            {
                /*
                 if (Config.DEBUG)
                 _log.warning("L2PlayerAI: no previous intention set... Setting it to IDLE");
                 */
                // set intention to idle if skill doesn't change intention.
                setIntention(AI_INTENTION_IDLE);
            }
        }
    }

	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			clientStopMoving(null);
		}
	}

	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}

    /**
     * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR><BR>
     *
     * <B><U> Actions</U> : </B><BR><BR>
     * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
     * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO </li>
     * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast) </li><BR><BR>
     *
     */
    @Override
	protected void onIntentionMoveTo(L2CharPosition pos)
    {
    	if (getIntention() == AI_INTENTION_REST)
        {
            // Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
            clientActionFailed();
            return;
        }

        if (_actor.isAllSkillsDisabled() || _actor.isAttackingNow())
        {
        	clientActionFailed();
        	saveNextIntention(AI_INTENTION_MOVE_TO, pos, null);
        	return;
        }

        // Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
        changeIntention(AI_INTENTION_MOVE_TO, pos, null);

        // Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
        clientStopAutoAttack();

        // Abort the attack of the L2Character and send Server->Client ActionFailed packet
        _actor.abortAttack();

        // Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
        moveTo(pos.x, pos.y, pos.z);
    }

	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		super.clientNotifyDead();
	}

	private void actionFailed()
	{
		_actor.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void thinkAttack()
	{
		L2Character target = getAttackTarget();
		if (target == null)
		{
			actionFailed();
			return;
		}
		if (checkTargetLostOrDead(target))
		{
			if (target != null)
			{
				// Notify the target
				setAttackTarget(null);
			}
			actionFailed();
			return;
		}
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			actionFailed();
			return;
		}
		_accessor.doAttack(target);
		return;
	}

	private void thinkCast()
	{
		L2Character target = getCastTarget();
		// if (Config.DEBUG) _log.warning("L2PlayerAI: thinkCast -> Start");
		if (_skill.getTargetType() == SkillTargetType.TARGET_GROUND && _actor instanceof L2PcInstance)
		{
			if (maybeMoveToPosition(((L2PcInstance) _actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill))) {
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				if (_skill.isOffensive() && getAttackTarget() != null)
				{
					// Notify the target
					setCastTarget(null);
				}
				return;
			}
			if (target != null && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill))) {
				return;
			}
		}
		if (_skill.getHitTime() > 50) {
			clientStopMoving(null);
		}
		L2Object oldTarget = _actor.getTarget();
		if (oldTarget != null)
		{
			// Replace the current target by the cast target
			if (target != null && oldTarget != target) {
				_actor.setTarget(getCastTarget());
			}
			// Launch the Cast of the skill
			_accessor.doCast(_skill);
			// Restore the initial target
			if (target != null && oldTarget != target) {
				_actor.setTarget(oldTarget);
			}
		} else {
			_accessor.doCast(_skill);
		}
		return;
	}

	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled())
		{
			actionFailed();
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			actionFailed();
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			actionFailed();
			return;
		}
		setIntention(AI_INTENTION_IDLE);
		((L2PcInstance.AIAccessor) _accessor).doPickupItem(target);
		return;
	}

	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled()) {
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target)) {
			return;
		}
		if (maybeMoveToPawn(target, 36)) {
			return;
		}
		if (!(target instanceof L2StaticObjectInstance)) {
			((L2PcInstance.AIAccessor) _accessor).doInteract((L2Character) target);
		}
		setIntention(AI_INTENTION_IDLE);
		return;
	}

	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isAllSkillsDisabled()) {
			return;
		}
		/*
		 * if (Config.DEBUG) _log.warning("L2PlayerAI: onEvtThink -> Check intention");
		 */
		_thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK) {
				thinkAttack();
			} else if (getIntention() == AI_INTENTION_CAST) {
				thinkCast();
			} else if (getIntention() == AI_INTENTION_PICK_UP) {
				thinkPickUp();
			} else if (getIntention() == AI_INTENTION_INTERACT) {
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
}