package com.simgraph.model;

import java.util.*;

/**
 * Binomial min-heap supporting insert, extract-min, and decrease-key.
 * Used by Dijkstra to give step-by-step heap state snapshots.
 */
public class BinomialQueue {

    private static final int MAX_DEG       = 32;
    private static final int INF_THRESHOLD = Integer.MAX_VALUE / 4;

    private final Node[]             roots   = new Node[MAX_DEG];
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    // ── Inner node (package-private intentional) ───────────────────────────────

    static class Node {
        int  vertex, key, degree;
        Node parent, child, sibling;

        Node(int v, int k) { vertex = v; key = k; }
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public boolean isEmpty() {
        for (Node r : roots) if (r != null) return false;
        return true;
    }

    public void insert(int vertex, int key) {
        Node n = new Node(vertex, key);
        nodeMap.put(vertex, n);
        mergeUp(n);
    }

    /** Returns {vertex, key} of the minimum element and removes it from the heap. */
    public int[] extractMin() {
        int minD = minDegree();
        if (minD < 0) return null;

        Node minRoot = roots[minD];
        roots[minD] = null;
        nodeMap.remove(minRoot.vertex);

        // Re-insert all children of the removed root
        Node child = minRoot.child;
        while (child != null) {
            Node next   = child.sibling;
            child.sibling = null;
            child.parent  = null;
            mergeUp(child);
            child = next;
        }
        return new int[]{ minRoot.vertex, minRoot.key };
    }

    public void decreaseKey(int vertex, int newKey) {
        Node n = nodeMap.get(vertex);
        if (n == null) return;
        n.key = newKey;
        bubbleUp(n);
    }

    /** JSON-serialisable snapshot of the current heap state. */
    public Map<String, Object> snapshot(String lastOp, int lastVertex) {
        List<Map<String, Object>> treeList = new ArrayList<>();
        for (Node r : roots) {
            if (r != null) treeList.add(toMap(r));
        }
        int minV = -1, minK = Integer.MAX_VALUE;
        for (Node r : roots) {
            if (r != null && r.key < minK) { minK = r.key; minV = r.vertex; }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("trees",      treeList);
        m.put("lastOp",     lastOp != null ? lastOp : "");
        m.put("lastVertex", lastVertex);
        m.put("minVertex",  minV);
        return m;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /** Makes `child` the new leftmost child of `root` (both must have equal degree). */
    private static void link(Node root, Node child) {
        child.parent  = root;
        child.sibling = root.child;
        root.child    = child;
        root.degree++;
    }

    private void mergeUp(Node n) {
        int d = n.degree;
        while (d < MAX_DEG && roots[d] != null) {
            Node other = roots[d];
            roots[d] = null;
            if (n.key <= other.key) link(n, other);
            else                   { link(other, n); n = other; }
            d = n.degree;
        }
        if (d < MAX_DEG) roots[d] = n;
    }

    private void bubbleUp(Node n) {
        while (n.parent != null && n.key < n.parent.key) {
            Node p  = n.parent;
            int  tv = n.vertex, tk = n.key;
            n.vertex = p.vertex; n.key = p.key;
            p.vertex = tv;       p.key = tk;
            nodeMap.put(p.vertex, p);
            nodeMap.put(n.vertex, n);
            n = p;
        }
    }

    private int minDegree() {
        int minD = -1, minK = Integer.MAX_VALUE;
        for (int i = 0; i < MAX_DEG; i++) {
            if (roots[i] != null && roots[i].key < minK) {
                minK = roots[i].key; minD = i;
            }
        }
        return minD;
    }

    private Map<String, Object> toMap(Node n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("v",   n.vertex);
        m.put("k",   n.key >= INF_THRESHOLD ? -1 : n.key); // -1 means ∞ in the frontend
        m.put("deg", n.degree);
        List<Map<String, Object>> ch = new ArrayList<>();
        Node c = n.child;
        while (c != null) { ch.add(toMap(c)); c = c.sibling; }
        m.put("ch", ch);
        return m;
    }
}

