/**
 * 
 */
package partialinfodecomp.discrete;

import java.util.Set;

import junit.framework.TestCase;

/**
 * Test cases for the redundancy lattice class
 * 
 * TODO Add plenty more
 * 
 * @author Joseph Lizier joseph.lizier at gmail.com
 *
 */
public class RedundancyLatticeTester extends TestCase {

	/**
	 * 
	 */
	public RedundancyLatticeTester() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public RedundancyLatticeTester(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	String lattice1 = "{0}\n";
	
	public void testStructure1SourceLattice() throws Exception {
		assertEquals(lattice1, generateAndPrintLattice(1));		
	}
	
	public void testOrdersFor1SourceLattice() throws Exception {
		// Test order 1 contains our 1 node:
		RedundancyLattice rl = new RedundancyLattice(1);
		
		// First check there's nothing at order 0:
		Set<NodeOfElements> setOfNodes = rl.getNodesForGivenInteractionOrder(0);
		assertEquals(0, setOfNodes.size());
		
		setOfNodes = rl.getNodesForGivenInteractionOrder(1);
		assertEquals(1, setOfNodes.size());
		
		// Test that there are no nodes at other orders
		for (int o = 2; o < 10; o++) {
			setOfNodes = rl.getNodesForGivenInteractionOrder(o);
			assertTrue(setOfNodes.isEmpty());
		}
	}

	public void testStructure2SourceLattice() throws Exception {
		RedundancyLattice rl = new RedundancyLattice(2);
		rl.topNode.printNode(0, true);
		
		// Check top node
		NodeOfElements topNode = rl.getTopNode();
		NodeOfElements node_01 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1}"));
		assertNotNull(node_01);
		assertEquals(topNode, node_01);
		
		// Check children of top node:
		NodeOfElements node_0 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0}"));
		assertNotNull(node_0);
		NodeOfElements node_1 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{1}"));
		assertNotNull(node_1);
		Set<NodeOfElements> childrenOfTopNode = topNode.getChildNodes();
		assertEquals(2, childrenOfTopNode.size());
		assertTrue(childrenOfTopNode.contains(node_0));
		assertTrue(childrenOfTopNode.contains(node_1));
		
		// Check the bottom node is a child of the two intermediate nodes:
		NodeOfElements node_0_1 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0}{1}"));
		assertNotNull(node_0_1);
		Set<NodeOfElements> childrenOfNode_0 = node_0.getChildNodes();
		assertEquals(1, childrenOfNode_0.size());
		childrenOfNode_0.iterator().next().printNode(0);
		node_0_1.printNode(0);
		assertTrue(childrenOfNode_0.contains(node_0_1));
		Set<NodeOfElements> childrenOfNode_1 = node_1.getChildNodes();
		assertTrue(childrenOfNode_1.contains(node_0_1));
		
	}


	// It's too difficult to test a string expression for redundancy
	//  lattice of size 3, since the sets are subject to reordering
	//  etc.
	public void testStructure3SourceLattice() throws Exception {
		RedundancyLattice rl = new RedundancyLattice(3);
		
		// Check top node
		NodeOfElements topNode = rl.getTopNode();
		NodeOfElements node_012 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1,2}"));
		assertNotNull(node_012);
		assertEquals(topNode, node_012);
		
		// Check children of top node:
		NodeOfElements node_01 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1}"));
		assertNotNull(node_01);
		NodeOfElements node_02 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,2}"));
		assertNotNull(node_02);
		NodeOfElements node_12 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{1,2}"));
		assertNotNull(node_12);
		Set<NodeOfElements> childrenOfTopNode = topNode.getChildNodes();
		assertEquals(3, childrenOfTopNode.size());
		assertTrue(childrenOfTopNode.contains(node_01));
		assertTrue(childrenOfTopNode.contains(node_02));
		assertTrue(childrenOfTopNode.contains(node_12));
		
		// Now go through and check children of the other nodes:
		NodeOfElements node_01_02 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1}{0,2}"));
		assertNotNull(node_01_02);
		NodeOfElements node_01_12 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1}{1,2}"));
		assertNotNull(node_01_12);
		NodeOfElements node_02_12 = rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,2}{1,2}"));
		assertNotNull(node_02_12);
		Set<NodeOfElements> childrenOfNode_01 = node_01.getChildNodes();
		assertEquals(2, childrenOfNode_01.size());
		assertTrue(childrenOfNode_01.contains(node_01_02));
		assertTrue(childrenOfNode_01.contains(node_01_12));
		Set<NodeOfElements> childrenOfNode_02 = node_02.getChildNodes();
		assertEquals(2, childrenOfNode_02.size());
		assertTrue(childrenOfNode_02.contains(node_01_02));
		assertTrue(childrenOfNode_02.contains(node_02_12));
		Set<NodeOfElements> childrenOfNode_12 = node_12.getChildNodes();
		assertEquals(2, childrenOfNode_12.size());
		assertTrue(childrenOfNode_12.contains(node_01_12));
		assertTrue(childrenOfNode_12.contains(node_02_12));

		// TODO Need to code up the rest of the structure tests here
	}

	protected String generateAndPrintLattice(int numSources) throws Exception {
		RedundancyLattice rl = new RedundancyLattice(numSources);
		StringBuffer buffer = new StringBuffer();
		rl.topNode.printNode(0, true, buffer);
		return buffer.toString();
	}

	/**
	 * Main routine used for simple temporary debugging.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int numSources = 3;
		
		RedundancyLattice rl = new RedundancyLattice(numSources);
		rl.print();
		
		// Now print nodes at each interaction order
		for (int o = 1; o <= numSources; o++) {
			System.out.println("Nodes at order " + o + ": ");
			for (NodeOfElements node : rl.getNodesForGivenInteractionOrder(o)) {
				node.printNode(0, false);
			}
			System.out.println();
		}
	}
}
