package moe.seikimo.mwhrd.interfaces.player;

import moe.seikimo.mwhrd.interfaces.*;
import moe.seikimo.mwhrd.models.PlayerModel;

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
    IHardcorePlayer
{

}
