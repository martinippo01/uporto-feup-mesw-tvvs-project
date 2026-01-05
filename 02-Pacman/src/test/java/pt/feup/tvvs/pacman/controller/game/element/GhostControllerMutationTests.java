package pt.feup.tvvs.pacman.controller.game.element;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.controller.game.element.GhostController;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GhostControllerMutationTests {

    @Test
    void moveGhost_calls_setDirection_and_incrementCounter_for_counter_zero() throws Exception {
        Arena arena = new Arena(20,20);
        GhostController controller = new GhostController(arena);

        // create blinky at (2,2) with LEFT direction so RIGHT is opposite
        Blinky blinky = new Blinky(new Position(2,2));
        blinky.setOutsideGate();
        blinky.setDirection(Direction.LEFT);
        blinky.setCounter(0);

        arena.addGhost(blinky);
        // pacman target to the right at (5,2)
        arena.addPacman(new Pacman(new Position(5,2)));

        Blinky spy = spy(blinky);
        // replace the ghost in arena's set with spy
        arena.getGhosts().clear();
        arena.addGhost(spy);

        // call step with time that will invoke moveGhost (time % speed != 1). speed default 3 so use time=0
        controller.step(null, List.of(), 0L);

        // setDirection should have been called (choose UP as per logic in white-box test)
        verify(spy, atLeastOnce()).setDirection(any(Direction.class));
        // incrementCounter should have been called -> counter becomes 1
        assertThat(spy.getCounter()).isEqualTo(1);
        // and the actual direction should have changed from LEFT -> this detects mutants that remove setDirection
        assertThat(spy.getDirection()).isNotEqualTo(Direction.LEFT);
    }

    @Test
    void time_mod_speed_behavior_for_various_speeds() throws Exception {
        Arena arena = new Arena(20,20);
        GhostController controller = new GhostController(arena);

        // helper to test a given speed and time: when time % speed == 1 -> moveGhost skipped
        Blinky b = new Blinky(new Position(2,2));
        b.setOutsideGate();
        b.setCounter(0);
        arena.getGhosts().clear();
        arena.addPacman(new Pacman(new Position(5,2)));

        for (int speed : new int[]{2,3,4,5}) {
            Blinky ghost = new Blinky(new Position(2,2));
            ghost.setOutsideGate();
            ghost.setCounter(0);
            ghost.setSpeed(speed);
            Blinky spy = spy(ghost);
            arena.getGhosts().clear();
            arena.addGhost(spy);

            long tSkip = speed + 1; // tSkip % speed == 1
            controller.step(null, List.of(), tSkip);
            // skipped -> no direction change and counter remains 0
            verify(spy, never()).setDirection(any(Direction.class));
            assertThat(spy.getCounter()).isEqualTo(0);
            clearInvocations(spy);

            long tInvoke = speed; // speed % speed == 0 -> invoke
            controller.step(null, List.of(), tInvoke);
            verify(spy, atLeastOnce()).setDirection(any(Direction.class));
            assertThat(spy.getCounter()).isEqualTo(1);
            clearInvocations(spy);
        }
    }

    @Test
    void moveGhost_skipped_when_time_mod_speed_equals_one() throws Exception {
        Arena arena = new Arena(20,20);
        GhostController controller = new GhostController(arena);

        Blinky blinky = new Blinky(new Position(2,2));
        blinky.setOutsideGate();
        blinky.setCounter(0);
        arena.addGhost(blinky);
        arena.addPacman(new Pacman(new Position(5,2)));

        Blinky spy = spy(blinky);
        arena.getGhosts().clear();
        arena.addGhost(spy);

        // choose time such that time % speed == 1. speed default 3 -> pick time=4 (4%3==1)
        controller.step(null, List.of(), 4L);

        // moveGhost should NOT be invoked; counter remains 0 and setDirection not called
        verify(spy, never()).setDirection(any(Direction.class));
        assertThat(spy.getCounter()).isEqualTo(0);
    }

    @Test
    void dead_ghost_at_gate_triggers_setState_setInsideGate_and_setSpeed() throws Exception {
        Arena arena = new Arena(10,10);
        GhostController controller = new GhostController(arena);

        Blinky blinky = new Blinky(arena.getGhostGate().getPosition());
        blinky.setOutsideGate();
        blinky.setState(GhostState.DEAD);

        Blinky spy = spy(blinky);
        arena.addGhost(spy);
        arena.addPacman(new Pacman(new Position(1,1)));

        controller.step(null, List.of(), 0L);

        // verify state changed to ALIVE and inside gate and speed set
        verify(spy, atLeastOnce()).setState(GhostState.ALIVE);
        verify(spy, atLeastOnce()).setInsideGate();
        verify(spy, atLeastOnce()).setSpeed(Arena.GHOST_NORMAL_SPEED);
        // also assert the observable speed value changed (detects mutants that removed setSpeed)
        assertThat(spy.getSpeed()).isEqualTo(Arena.GHOST_NORMAL_SPEED);
    }

    @Test
    void frameCount_increments_after_step_and_reflects_in_isChaseMode() throws Exception {
        Arena arena = new Arena(5,5);
        GhostController controller = new GhostController(arena);

        Field frameCount = GhostController.class.getDeclaredField("frameCount");
        frameCount.setAccessible(true);
        // set to 0 then step and verify increments to 1
        frameCount.setInt(controller, 0);
        controller.step(null, List.of(), 0L);
        assertThat(frameCount.getInt(controller)).isEqualTo(1);

        // set to a value within chase window and verify isChaseMode true after step increments
        frameCount.setInt(controller, 449);
        Method isChase = GhostController.class.getDeclaredMethod("isChaseMode");
        isChase.setAccessible(true);
        // before step, frameCount==449 -> isChase false
        assertThat((Boolean) isChase.invoke(controller)).isFalse();
        controller.step(null, List.of(), 0L);
        // now frameCount==450 -> isChase true
        assertThat((Boolean) isChase.invoke(controller)).isTrue();
    }
}
