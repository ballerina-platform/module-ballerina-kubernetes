package org.ballerinax.kubernetes.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class checks for cyclic dependencies between Deployments.
 */
public class DependencyValidator {

    private Map<String, Node> nodesMap = new HashMap<>();

    /**
     * Get a node from the Map if it already exists, else create it and put it in the map.
     */
    private Node getOrCreateNode(String nodeName) {
        Node currentNode;
        if (!nodesMap.containsKey(nodeName)) {
            currentNode = new Node(nodeName);
            nodesMap.put(nodeName, currentNode);
        } else {
            currentNode = nodesMap.get(nodeName);
        }
        return currentNode;
    }

    public boolean validateDependency(String... dependencyChain) {

        String nodeName = dependencyChain[0];

        Node node = getOrCreateNode(nodeName);

        for (int i = 1; i < dependencyChain.length; i++) {
            if (!node.addChild(getOrCreateNode(dependencyChain[i]))) {
                return false;
            }
        }
        return true;
    }

    private static class Node implements Comparable<Node> {
        Set<Node> children = new HashSet<>();
        String dependencies;

        Node(String dependencies) {
            this.dependencies = dependencies;
        }

        boolean addChild(Node n) {
            //if we add a child, all the children of the that node cannot be this node, otherwise we create a cycle.
            if (n.allDescendants().contains(this)) {
                return false;
            } else {
                this.children.add(n);
                return true;
            }
        }

        Set<Node> allDescendants() {
            Set<Node> all = new TreeSet<>();

            for (Node c : children) {
                all.add(c);
                all.addAll(c.allDescendants());
            }
            return all;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node) o;
            return dependencies != null ? dependencies.equals(node.dependencies) : node.dependencies == null;

        }

        @Override
        public int hashCode() {
            return dependencies != null ? dependencies.hashCode() : 0;
        }


        @Override
        public int compareTo(Node o) {
            return dependencies.compareTo(o.dependencies);
        }
    }
}
