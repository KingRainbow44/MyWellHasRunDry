package moe.seikimo.mwhrd.game.guilds;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GuildPermission {
    RECRUIT(null, "Recruit"), // Assigned when a member joins the guild.
    MEMBER(RECRUIT, "Member"), // Assigned via a command; first tier of permission.
    OFFICER(MEMBER, "Officer"), // Assigned via a command; second tier of permission.
    OWNER(OFFICER, "Owner"); // Assigned when being the first to join a guild. Cannot be changed.

    final GuildPermission previousPermission;
    final String displayName;

    /**
     * @return The next permission in the hierarchy.
     */
    public GuildPermission getNext() {
        return switch (this) {
            case RECRUIT -> MEMBER;
            case MEMBER -> OFFICER;
            case OFFICER -> OWNER;
            case OWNER -> null;
        };
    }

    /**
     * @return The previous permission in the hierarchy.
     */
    public GuildPermission getPrevious() {
        return this.previousPermission;
    }

    /**
     * Returns true if this permission is greater than or equal to the permission.
     *
     * @param checkAgainst The permission to check against.
     * @return {@code true} if this permission is greater than or equal to the permission, {@code false} otherwise.
     */
    public boolean hasPermission(GuildPermission checkAgainst) {
        return this.ordinal() >= checkAgainst.ordinal();
    }

    /**
     * Returns true if the new rank can be promoted to.
     *
     * @param newRank The new rank to promote to.
     * @return {@code true} if the new rank can be promoted to, {@code false} otherwise.
     */
    public boolean canPromote(GuildPermission newRank) {
        return newRank != OWNER &&
            this != newRank &&
            this.ordinal() < newRank.ordinal();
    }

    /**
     * Returns true if the new rank can be demoted to.
     *
     * @param newRank The new rank to demote to.
     * @return {@code true} if the new rank can be demoted to, {@code false} otherwise.
     */
    public boolean canDemote(GuildPermission newRank) {
        return newRank != OWNER &&
            this != newRank &&
            this.ordinal() > newRank.ordinal();
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
