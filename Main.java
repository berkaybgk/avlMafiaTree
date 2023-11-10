/**
 * author: Berkay Bugra Gok
 * This class implements an AVL tree and several methods specific to a problem.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        File inputFile = new File(args[0]);
        AvlTree mainTree = new AvlTree(args[1]);

        // excessive codes to run the code from IDE
//        File inputFile = new File("small2.txt");
//        String outputFile = "out.txt";
//        AvlTree mainTree = new AvlTree(outputFile);

        Scanner scanner = new Scanner(inputFile);

        String bossName = scanner.next();
        String bossGMS = scanner.next();

        mainTree.add(bossName,Double.parseDouble(bossGMS));

        try {
            while (scanner.hasNextLine()) {
                String operation;
                operation = scanner.next();
                // if the operation is adding a new member
                if (operation.equals("MEMBER_IN")) {
                    String memberName = scanner.next();
                    String memberGMS = scanner.next();

                    mainTree.add(memberName, Double.parseDouble(memberGMS));


                } else if (operation.equals("MEMBER_OUT")) {
                    String memberName = scanner.next();
                    String memberGMS = scanner.next();
                    // find the member's node and perform a deletion
                    mainTree.remove(Double.parseDouble(memberGMS));


                } else if (operation.equals("INTEL_TARGET")) {
                    // find the lowest superior of the targeted two members
                    String member1Name = scanner.next();
                    String member1GMS = scanner.next();
                    double GMS1double = Double.parseDouble(member1GMS);

                    String member2Name = scanner.next();
                    String member2GMS = scanner.next();
                    double GMS2double = Double.parseDouble(member2GMS);

                    AvlNode resultNode = mainTree.targetNode(mainTree.root, Math.max(GMS1double, GMS2double), Math.min(GMS1double, GMS2double));

                    String stringGms = String.format("%,.3f", resultNode.gmsValue).replace(",", ".");
                    mainTree.writer.println("Target Analysis Result: " + resultNode.surnameName + " " + stringGms);


                } else if (operation.equals("INTEL_DIVIDE")) {
                    // we must log the max number of independent members(?)
                    int divisionResult = mainTree.divide(mainTree.root);
                    mainTree.writer.println("Division Analysis Result: " + divisionResult);


                } else if (operation.equals("INTEL_RANK")) {
                    // we must output the members that have the same rank sorted with increasing GMS.
                    String memberName = scanner.next();
                    String memberGMS = scanner.next();

                    int targetDepth = mainTree.findDepth(mainTree.root, Double.parseDouble(memberGMS),0);

                    mainTree.writer.print("Rank Analysis Result:");
                    mainTree.rank(mainTree.root, targetDepth, 0);
                    mainTree.writer.println();
                }

            }

        } catch (NoSuchElementException ignored) {

        }
        mainTree.writer.close();
    }

    static class AvlTree {
        // instance variables
        AvlNode root;
        File outputFile;
        PrintWriter writer;

        // constructors
        AvlTree( ) throws FileNotFoundException {
            root = null;
        }
        AvlTree(String outputName) throws FileNotFoundException {
            root = null;
            outputFile = new File(outputName);
            writer = new PrintWriter(outputFile);
        }

        // ------------------------------------------------ AVL METHODS ------------------------------------------------
        void add(String surnameName, double gmsValue) {
            root = insert(surnameName,gmsValue, root);
        }
        private AvlNode insert(String surnameName, double gmsValue, AvlNode currentNode) {

            if(currentNode == null)
                return new AvlNode(surnameName,gmsValue, null, null );

            writer.println(currentNode.surnameName + " welcomed " + surnameName);

            if(gmsValue < currentNode.gmsValue)
                currentNode.leftInferior = insert(surnameName, gmsValue, currentNode.leftInferior);
            else
                currentNode.rightInferior = insert(surnameName, gmsValue, currentNode.rightInferior);
            return balance(currentNode);
        }


        void remove (double gmsValue) {
            root = remove(gmsValue, root, true);
        }

        // removes the wanted element and writes the necessary output
        private AvlNode remove(double gmsValue, AvlNode currentNode, boolean print)
        {
            if(currentNode == null)
                return currentNode;   // Item not found; do nothing

            double compareResult = gmsValue - currentNode.gmsValue;

            if( compareResult < 0 ) // if in the left subtree
                currentNode.leftInferior = remove(gmsValue, currentNode.leftInferior, print);
            else if( compareResult > 0 ) // if in the right subtree
                currentNode.rightInferior = remove(gmsValue, currentNode.rightInferior, print);

            // found and two children
            else if(currentNode.leftInferior != null && currentNode.rightInferior != null)
            {
                AvlNode replacingNode = findMin(currentNode.rightInferior);

                if (print)
                    writer.println(currentNode.surnameName + " left the family, replaced by " + replacingNode.surnameName);
                currentNode.gmsValue = replacingNode.gmsValue;
                currentNode.surnameName = replacingNode.surnameName;

                currentNode.rightInferior = remove(currentNode.gmsValue, currentNode.rightInferior, false);
            }

            else // found and single child or no child
                if (currentNode.leftInferior != null) { // has only a left child
                    if (print)
                        writer.println(currentNode.surnameName + " left the family, replaced by " + currentNode.leftInferior.surnameName);
                    currentNode = currentNode.leftInferior;
                }
                else if (currentNode.rightInferior != null) { // has only a right child
                    if (print)
                        writer.println(currentNode.surnameName + " left the family, replaced by " + currentNode.rightInferior.surnameName);
                    currentNode = currentNode.rightInferior;
                }
                else { // has no child
                    if (print)
                        writer.println(currentNode.surnameName + " left the family, replaced by nobody");
                    currentNode = null;
                }

            return balance(currentNode);
        }

        // search for the node until we reach a node which satisfies the following
        // lowerGMS < currentNode.gmsValue < higherGMS where current node is the smallest common superior of the
        // other two nodes with values lowerGMS and higherGMS
        AvlNode targetNode(AvlNode currentNode, double higherGMS, double lowerGMS) {

            if (higherGMS < currentNode.gmsValue) { // the nodes we are looking for are both in the left subtree
                return targetNode(currentNode.leftInferior,higherGMS,lowerGMS);
            }

            else if (lowerGMS > currentNode.gmsValue) { // the nodes we are looking for are both in the right subtree
                return targetNode(currentNode.rightInferior,higherGMS,lowerGMS);
            }

            else { // currentGMS < higher and currentGMS > lower
                return currentNode;
            }
        }

        // finds and prints the nodes that are on the same level
        // using breadth first search
        void rank(AvlNode currentNode, int targetDepth, int depth) {
            if (currentNode == null) {
                return;
            }

            if (depth == targetDepth) {
                // At the desired depth, print the node's information.
                String gms = String.format("%.3f", currentNode.gmsValue).replace(",", ".");
                writer.print(" " + currentNode.surnameName + " " + gms);
                return;
            }

            rank(currentNode.leftInferior, targetDepth, depth + 1);
            rank(currentNode.rightInferior, targetDepth, depth + 1);

        }

        // find the maximum number of nodes that are not in a parent-child relationship
        // make sure to update the divided variables correctly
        int divide(AvlNode currentNode) {
            currentNode.divided = false;
            int rightDivide = 0;
            int leftDivide = 0;

            if ((currentNode.rightInferior == null) && (currentNode.leftInferior == null)) {
                currentNode.divided = true;
                return 1;
            }

            else {
                if (currentNode.rightInferior != null) // has right child
                    rightDivide = divide(currentNode.rightInferior);
                if (currentNode.leftInferior != null) // has left child
                    leftDivide = divide(currentNode.leftInferior);

                if ((currentNode.leftInferior == null || !currentNode.leftInferior.divided) &&
                        (currentNode.rightInferior == null || !currentNode.rightInferior.divided)) { // if left and right children are not divided
                    currentNode.divided = true;
                    return 1+leftDivide+rightDivide;
                }
            }

            return leftDivide+rightDivide;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        int findDepth(AvlNode currentNode, double gmsValue, int depth) {
            if (currentNode.gmsValue == gmsValue)
                return depth;
            else if (gmsValue > currentNode.gmsValue)
                return findDepth(currentNode.rightInferior, gmsValue,depth+1);
            else
                return findDepth(currentNode.leftInferior, gmsValue,depth+1);
        }

        private int height( AvlNode currentNode) {
            if (currentNode == null)
                return -1;
            else return currentNode.height;
        }

        // internal balance method to perform the necessary rotations on the nodes
        private AvlNode balance(AvlNode currentNode) {
            // if we reach an end of a branch
            if (currentNode == null)
                return currentNode;

            // if the currentNode is left-heavy
            if (height(currentNode.leftInferior) - height(currentNode.rightInferior) > 1) {
                if (height(currentNode.leftInferior.leftInferior) >= height(currentNode.leftInferior.rightInferior))
                    currentNode = rotateWithLeftChild(currentNode);
                else
                    currentNode = doubleWithLeftChild(currentNode);
            }
            // if the currentNode is right-heavy
            else {
                if (height(currentNode.rightInferior) - height(currentNode.leftInferior) > 1) {
                    if (height(currentNode.rightInferior.rightInferior) >= height(currentNode.rightInferior.leftInferior))
                        currentNode = rotateWithRightChild(currentNode);
                    else
                        currentNode = doubleWithRightChild(currentNode);
                }
            }
            // update the current nodes height
            currentNode.height = 1 + Math.max(height(currentNode.leftInferior), height(currentNode.rightInferior));
            // return the current node
            return currentNode;
        }


        AvlNode rotateWithLeftChild(AvlNode k2) { // right rotation
            AvlNode k1 = k2.leftInferior;
            k2.leftInferior = k1.rightInferior;
            k1.rightInferior = k2;
            k2.height = Math.max(height(k2.leftInferior), height(k2.rightInferior)) + 1;
            k1.height = Math.max(height(k1.leftInferior), k2.height ) + 1;
            return k1;
        }

        AvlNode rotateWithRightChild(AvlNode k1) { // left rotation
            AvlNode k2 = k1.rightInferior;
            k1.rightInferior = k2.leftInferior;
            k2.leftInferior = k1;
            k1.height = 1 + Math.max(height(k1.leftInferior), height(k1.rightInferior));
            k2.height = 1 + Math.max(height(k2.rightInferior), k1.height);
            return k2;
        }

        AvlNode doubleWithLeftChild(AvlNode k3) { // left-right rotation
            k3.leftInferior = rotateWithRightChild(k3.leftInferior);
            return rotateWithLeftChild(k3);
        }

        AvlNode doubleWithRightChild(AvlNode k1) { // right-left rotation
            k1.rightInferior = rotateWithLeftChild(k1.rightInferior);
            return rotateWithRightChild(k1);
        }

        AvlNode findMin(AvlNode currentNode) {
            if(currentNode == null)
                return currentNode;
            while(currentNode.leftInferior != null )
                currentNode = currentNode.leftInferior;
            return currentNode;
        }

        // to check the tree
        void printTree(AvlNode startingNode) {
            if( startingNode != null ) {
                printTree( startingNode.leftInferior );
                System.out.println("Current Node: " +startingNode.gmsValue);
                printTree(startingNode.rightInferior);
            }
        }
    }

    static class AvlNode {
        // instance variables
        String surnameName;
        double gmsValue;
        AvlNode leftInferior;
        AvlNode rightInferior;
        int height;
        boolean divided;

        // constructors
        AvlNode(String surnameName, double gmsValue) {
            this.surnameName = surnameName;
            this.gmsValue = gmsValue;
            this.leftInferior = null;
            this.rightInferior = null;
            divided = false;
        }

        AvlNode(String surnameName, double gmsValue, AvlNode leftChild, AvlNode rightChild) {
            this.surnameName = surnameName;
            this.gmsValue = gmsValue;
            this.leftInferior = leftChild;
            this.rightInferior = rightChild;
            height = 0;
            divided = false;
        }
    }
}
