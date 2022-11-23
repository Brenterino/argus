package dev.zygon.argus.group;

/**
 * Enumeration of available permissions to a group that may be assigned to
 * members of a group.
 */
public enum Permission {

    /**
     * Permission which grants the ability to read information from/about the
     * group.
     */
    READ,
    /**
     * Permission which grants the ability to write information to/about the
     * group.
     */
    WRITE,
    /**
     * Permission which grants the ability to read and write information
     * to/about the group.
     */
    READWRITE,
    /**
     * Permission which grants the ability to control access levels of other
     * group members.
     */
    ADMIN;

    /**
     * @return if this permission level grants read access to a group.
     */
    public boolean canRead() {
        return this == READWRITE ||
                this == READ ||
                this == ADMIN;
    }

    /**
     * @return if this permission level grants write access to a group.
     */
    public boolean canWrite() {
        return this == READWRITE ||
                this == WRITE ||
                this == ADMIN;
    }
}
