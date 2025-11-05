package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Enemy;
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.physics.box2d.World;

/**
 * IudexGundyr (adaptado): jefe gigante con lanza — 4 ataques y 2 fases.
 *
 * Integrar con el sistema de colisiones / hitboxes / animaciones del proyecto.
 * Este archivo reemplaza o adapta la clase existente IudexGundyr para incluir:
 * - Attack1: carga (delay) + estocada en la dirección que mira al iniciar el ataque.
 * - Attack2: patada hacia atrás (solo cuando el jugador está detrás).
 * - Attack3: giro con lanza (cubre retaguardia primero y termina hacia la dirección inicial).
 * - Attack4: pisotón / empujón sin daño — se ejecuta al pasar a fase 2.
 * - Cambio de fase a mitad de vida: ejecuta Attack4, aumenta velocidad y reduce delays.
 *
 * NOTA: Métodos createXXXHitbox(...) y applyPushToPlayer(...) son puntos de integración;
 * hay que sustituirlos por las llamadas reales a tu sistema de colisiones/daño/knockback.
 */
public class IudexGundyr extends Enemy {
    public enum State { IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, STAGGER, DEAD }

    private State state = State.IDLE;
    private boolean facingRight = true;
    private boolean phase2 = false;

    private float maxHealth = 300f;
    private float health = maxHealth;

    // Movimiento
    private float baseSpeed = 48f;
    private float speed;
    private float phase2SpeedMultiplier = 1.5f;

    // Timers
    private float stateTimer = 0f;
    private float cooldownTimer = 0f;
    private float baseAttackCooldown = 1.2f;

    // Attack1: carga + estocada
    private float a1_chargeDelay = 0.7f;
    private float a1_lungeDuration = 0.28f;
    private float a1_lungeSpeed = 260f;
    private float a1_damage = 26f;
    private boolean a1_charging = false;
    private boolean a1_lungeStarted = false;

    // Attack2: patada hacia atrás
    private float a2_duration = 0.22f;
    private float a2_range = 54f;
    private float a2_damage = 14f;

    // Attack3: giro con lanza
    private float a3_duration = 0.9f;
    private float a3_damage = 18f;
    private float a3_radius = 140f;

    // Attack4: pisotón / empuje (sin daño)
    private float a4_duration = 0.5f;
    private float a4_pushForce = 300f;
    private float a4_cooldown_after_phase = 1.0f;

    public IudexGundyr(World world, float x, float y, Player player) {
        super(world, x, y, player);
        this.width = 96;   // ajustar collider según sprites
        this.height = 170;
        this.speed = baseSpeed;
        this.health = maxHealth;
    }

    /**
     * update: llama periódicamente desde el juego.
     * Firma propuesta: update(float dt, Player player)
     * Si tu proyecto usa otra firma, adapta en consecuencia.
     */
    public void update(float dt, Player player) {
        if (state == State.DEAD) return;

        // timers
        stateTimer += dt;
        cooldownTimer = Math.max(0f, cooldownTimer - dt);

        // detectar cambio de fase al llegar a la mitad de vida
        if (!phase2 && health <= maxHealth / 2f) {
            enterPhase2();
            // ejecutar pisotón inmediatamente como efecto de cambio de fase
            startAttack4();
            // dejamos que update de ATTACK4 se encargue de la lógica de empuje
            return;
        }

        // máquina de estados
        switch (state) {
            case IDLE:
                decideNextAction(dt, player);
                break;
            case ATTACK1:
                updateAttack1(dt, player);
                break;
            case ATTACK2:
                updateAttack2(dt, player);
                break;
            case ATTACK3:
                updateAttack3(dt, player);
                break;
            case ATTACK4:
                updateAttack4(dt, player);
                break;
            case STAGGER:
                if (stateTimer > 0.45f) setState(State.IDLE);
                break;
            default:
                break;
        }

        // orientar al jugador (útil para decidir "detrás")
        if (player.getPosition().x > this.position.x) facingRight = true;
        else facingRight = false;
    }

    private void setState(State s) {
        state = s;
        stateTimer = 0f;
        // TODO: disparar animación según state
    }

    private void decideNextAction(float dt, Player player) {
        if (cooldownTimer > 0f) {
            // acercarse o patrullar
            moveTowards(player, dt);
            return;
        }

        float dx = player.getPosition().x - this.position.x;
        float dist = Math.abs(dx);

        if (isPlayerBehind(player) && dist < 90f) {
            startAttack2();
        } else if (dist < 160f) {
            // elegir entre estocada o giro (aleatorio o basado en patrón)
            if (Math.random() < 0.55) startAttack1();
            else startAttack3();
        } else {
            // si está lejos, acercarse
            moveTowards(player, dt);
        }
    }

    private void moveTowards(Player player, float dt) {
        float dir = Math.signum(player.getPosition().x - this.position.x);
        this.position.x += dir * this.speed * (phase2 ? phase2SpeedMultiplier : 1.0f) * dt;
        // TODO: reemplazar por movimiento con física del proyecto si aplica
    }

    // --- ATTACK 1: CARGA + ESTOCADA ---
    private void startAttack1() {
        if (cooldownTimer > 0f) return;
        setState(State.ATTACK1);
        a1_charging = true;
        a1_lungeStarted = false;
        stateTimer = 0f;
        // TODO: animación de carga (preparar estocada)
    }

    private void updateAttack1(float dt, Player player) {
        if (a1_charging) {
            if (stateTimer >= a1_chargeDelay) {
                a1_charging = false;
                a1_lungeStarted = true;
                stateTimer = 0f;
                // TODO: spawn hitbox inicial de estocada / marcar que la estocada empezó
                // Ejemplo conceptual: createSpearHitbox(a1_damage, offset, dir)
            } else {
                // mientras carga, se queda en sitio o retrocede ligeramente según animación
            }
            return;
        }

        if (a1_lungeStarted) {
            if (stateTimer <= a1_lungeDuration) {
                float dir = facingRight ? 1f : -1f;
                this.position.x += dir * a1_lungeSpeed * dt;
                // generar (o mantener) hitbox frontal de lanza durante la lunge
                createSpearHitbox(a1_damage, 80f, dir);
            } else {
                endAttack();
            }
        }
    }

    // --- ATTACK 2: PATADA HACIA ATRÁS ---
    private void startAttack2() {
        if (cooldownTimer > 0f) return;
        setState(State.ATTACK2);
        stateTimer = 0f;
        // TODO: animación de patada
    }

    private void updateAttack2(float dt, Player player) {
        if (stateTimer <= a2_duration) {
            float dir = facingRight ? -1f : 1f; // golpea hacia atrás
            createKickHitbox(a2_damage, a2_range, dir);
        } else {
            endAttack();
        }
    }

    // --- ATTACK 3: GIRO CON LANZA ---
    private void startAttack3() {
        if (cooldownTimer > 0f) return;
        setState(State.ATTACK3);
        stateTimer = 0f;
        // TODO: animación de giro
    }

    private void updateAttack3(float dt, Player player) {
        if (stateTimer <= a3_duration) {
            // aproximación simple: durante el giro, crear un área alrededor que hace daño
            // Para respetar "retaguardia primero", podrías dividir la duración en dos partes:
            float half = a3_duration * 0.5f;
            if (stateTimer <= half) {
                // primera mitad: hitbox posterior ampliado
                createSpinRearHitbox(a3_damage, a3_radius);
            } else {
                // segunda mitad: hitbox frontal / general que termina mirando a la dirección inicial
                createSpinFrontHitbox(a3_damage, a3_radius);
            }
        } else {
            endAttack();
        }
    }

    // --- ATTACK 4: PISOTÓN / EMPUJE (sin daño) ---
    private void startAttack4() {
        setState(State.ATTACK4);
        stateTimer = 0f;
        // TODO: animación de pisotón
    }

    private void updateAttack4(float dt, Player player) {
        if (stateTimer <= a4_duration) {
            // aplica empuje hacia fuera (alejar al player). El empuje se aplica una vez o durante un corto intervalo.
            float dx = player.getPosition().x - this.position.x;
            float dir = Math.signum(dx);
            applyPushToPlayer(player, dir * a4_pushForce);
            // No inflige daño.
        } else {
            cooldownTimer = a4_cooldown_after_phase;
            endAttack();
        }
    }

    private void endAttack() {
        setState(State.IDLE);
        cooldownTimer = baseAttackCooldown * (phase2 ? 0.75f : 1.0f);
    }

    private boolean isPlayerBehind(Player player) {
        boolean playerOnRight = player.getPosition().x > this.position.x;
        return (facingRight && !playerOnRight) || (!facingRight && playerOnRight);
    }

    private void enterPhase2() {
        phase2 = true;
        this.speed = baseSpeed * phase2SpeedMultiplier;
        a1_chargeDelay *= 0.75f;
        a1_lungeSpeed *= 1.25f;
        a3_duration *= 0.82f;
        baseAttackCooldown *= 0.8f;
        // TODO: play phase2 animation / VFX / sound
    }

    // --- Hooks para integración con el motor / sistema de colisiones ---
    private void createSpearHitbox(float damage, float range, float dir) {
        // TODO: crear hitbox rectangular delante del jefe en dirección dir.
        // Llamar a la API de colisiones/daño del proyecto.
    }

    private void createKickHitbox(float damage, float range, float dir) {
        // TODO: crear hitbox corto detrás del boss; usar owner=this para evitar dañar doble vez
    }

    private void createSpinRearHitbox(float damage, float radius) {
        // TODO: hitbox que cubra la retaguardia de forma prioritaria
    }

    private void createSpinFrontHitbox(float damage, float radius) {
        // TODO: hitbox que cubra frontal/total al terminar el giro
    }

    private void applyPushToPlayer(Player player, float force) {
        // TODO: llamar al método de Player para aplicar knockback / setVelocity
        // ejemplo conceptual: player.applyKnockback(force, 0f);
    }

    // --- Recepción de daño ---
    public void receiveDamage(float dmg) {
        this.health -= dmg;
        if (this.health <= 0f) {
            this.health = 0f;
            die();
            return;
        }
        // reacción opcional al recibir daño
        setState(State.STAGGER);
        // TODO: tocar animación de impacto
    }

    private void die() {
        setState(State.DEAD);
        // TODO: animación de muerte, drops, remover de mundo
    }
}
