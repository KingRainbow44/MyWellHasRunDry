package moe.seikimo.mwhrd.interfaces.player;

import moe.seikimo.mwhrd.interfaces.*;
import moe.seikimo.mwhrd.models.PlayerModel;
import moe.seikimo.mwhrd.script.ScriptObject;
import moe.seikimo.mwhrd.script.Scriptable;

/**
 * This interface acts as an underlying wrapper to all new player methods.
 */
public interface IPlayer extends
    IPlayerConditions,
    IDBObject<PlayerModel>,
    ISelectionPlayer,
    ITrialPlayer,
    ITimeTraveler,
    ICallbackPlayer,
    IGunWielder,
    IHardcorePlayer,
    Scriptable
{
    /**
     * Override for {@link IPlayer} to use the mixin-compliant name.
     */
    @Override
    default ScriptObject intoScript() {
        return this.mwhrd$intoScript();
    }

    /**
     * Converts this object into a script-serializable object.
     *
     * @return An immutable value which represents this object at the current state.
     */
    ScriptObject mwhrd$intoScript();
}
