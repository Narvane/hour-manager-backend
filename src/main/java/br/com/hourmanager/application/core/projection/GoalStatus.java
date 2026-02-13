package br.com.hourmanager.application.core.projection;

/**
 * Status da meta em relação à projeção até o final do período.
 */
public enum GoalStatus {

    /** Projeção atinge ou supera a meta. */
    ATINGIVEL,

    /** Projeção abaixo da meta mas ainda com margem (em risco). */
    EM_RISCO,

    /** Projeção muito abaixo da meta; praticamente impossível atingir. */
    IMPOSSIVEL
}
