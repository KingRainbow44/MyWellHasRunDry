package moe.seikimo.mwhrd.script;

public interface Scriptable {
    /**
     * Converts this object into a script-serializable object.
     *
     * @return An immutable value which represents this object at the current state.
     */
    ScriptObject intoScript();
}
