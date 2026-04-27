package com.thirdspare.tpa;

/**
 * Enum representing the type of teleport request
 */
public enum TeleportRequestType {
    /**
     * Requester wants to teleport TO the target
     */
    TPA,

    /**
     * Requester wants the target to teleport TO them
     */
    TPAHERE
}
