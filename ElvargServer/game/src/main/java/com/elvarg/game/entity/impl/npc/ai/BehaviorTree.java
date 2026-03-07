package com.elvarg.game.entity.impl.npc.ai;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.ai.BehaviorNode.Status;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A lightweight behavior tree for NPC combat AI. Contains the root node
 * and factory methods for building trees declaratively.
 * <p>
 * Example usage for a boss:
 * <pre>
 * BehaviorTree tree = new BehaviorTree(
 *     BehaviorTree.selector(
 *         BehaviorTree.sequence(
 *             BehaviorTree.condition(npc -> npc.getHitpoints() < 50),
 *             BehaviorTree.action(npc -> { flee(npc); return Status.SUCCESS; })
 *         ),
 *         BehaviorTree.action(npc -> { attack(npc); return Status.SUCCESS; })
 *     )
 * );
 * </pre>
 */
public class BehaviorTree {

    private final BehaviorNode root;

    /**
     * Creates a behavior tree with the given root node.
     *
     * @param root the top-level node of the tree
     */
    public BehaviorTree(BehaviorNode root) {
        this.root = root;
    }

    /**
     * Evaluates the tree for one tick. Call this from the NPC's
     * combat method's {@code onTick()} or from {@code process()}.
     *
     * @param npc the NPC to evaluate
     * @return the result of this tick's evaluation
     */
    public Status tick(NPC npc) {
        return root.tick(npc);
    }

    // ── Factory methods for building trees ──

    /**
     * Runs children in order. Fails immediately if any child fails.
     * Succeeds only when all children succeed.
     */
    public static BehaviorNode sequence(BehaviorNode... children) {
        return new SequenceNode(Arrays.asList(children));
    }

    /**
     * Tries children in order. Succeeds immediately on the first
     * child that succeeds. Fails only when all children fail.
     */
    public static BehaviorNode selector(BehaviorNode... children) {
        return new SelectorNode(Arrays.asList(children));
    }

    /**
     * Checks a boolean condition. Returns SUCCESS if true, FAILURE if false.
     *
     * @param predicate the condition to check against the NPC
     */
    public static BehaviorNode condition(Predicate<NPC> predicate) {
        return npc -> predicate.test(npc) ? Status.SUCCESS : Status.FAILURE;
    }

    /**
     * Executes a game action. The function returns the action's status.
     *
     * @param action the action to perform on the NPC
     */
    public static BehaviorNode action(Function<NPC, Status> action) {
        return action::apply;
    }

    /**
     * Inverts a child's result: SUCCESS becomes FAILURE and vice versa.
     * RUNNING is passed through unchanged.
     */
    public static BehaviorNode inverter(BehaviorNode child) {
        return npc -> {
            Status result = child.tick(npc);
            if (result == Status.SUCCESS) return Status.FAILURE;
            if (result == Status.FAILURE) return Status.SUCCESS;
            return Status.RUNNING;
        };
    }

    /**
     * Repeats a child node up to {@code times} iterations in a single tick.
     * Stops early if the child fails.
     *
     * @param times the number of repetitions
     * @param child the node to repeat
     */
    public static BehaviorNode repeater(int times, BehaviorNode child) {
        return npc -> {
            for (int i = 0; i < times; i++) {
                Status result = child.tick(npc);
                if (result == Status.FAILURE) return Status.FAILURE;
                if (result == Status.RUNNING) return Status.RUNNING;
            }
            return Status.SUCCESS;
        };
    }

    // ── Composite node implementations ──

    /**
     * Sequence: runs children left-to-right, fails fast on first failure.
     */
    private static class SequenceNode implements BehaviorNode {
        private final List<BehaviorNode> children;

        SequenceNode(List<BehaviorNode> children) {
            this.children = children;
        }

        @Override
        public Status tick(NPC npc) {
            for (BehaviorNode child : children) {
                Status result = child.tick(npc);
                if (result != Status.SUCCESS) {
                    return result;
                }
            }
            return Status.SUCCESS;
        }
    }

    /**
     * Selector: tries children left-to-right, succeeds on first success.
     */
    private static class SelectorNode implements BehaviorNode {
        private final List<BehaviorNode> children;

        SelectorNode(List<BehaviorNode> children) {
            this.children = children;
        }

        @Override
        public Status tick(NPC npc) {
            for (BehaviorNode child : children) {
                Status result = child.tick(npc);
                if (result != Status.FAILURE) {
                    return result;
                }
            }
            return Status.FAILURE;
        }
    }
}
