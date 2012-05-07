package partialinfodecomp.discrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Implements the redundancy lattice for the Partial Information Decomposition.
 * </p>
 * 
 * <p>
 * See: "Nonnegative Decomposition of Multivariate Information",
 * Paul L. Williams and Randall D. Beer, 2010
 * <a href="http://arxiv.org/abs/1004.2515">arXiv:1004.2515</a>
 * <p>
 * 
 * <p>
 * Nomenclature:
 * 	<ul>
 * 		<li>The redundancy lattice (this class)
 * 			is a directed network of nodes.</li>
 * 		<li>Each node {@link #NodeOfElements} is a set of source elements.</li>
 * 		<li>Each source element {@link #ElementOfSources} is a set of single sources.</li>
 * 		<li>Each node has descendant nodes, who do not provide any 
 * 			redundant information that this ancestor node provides 
 * 			(i.e. the descendent nodes are lower in the redundancy lattice).</li>
 * 		<li>Each node has direct children in the redundancy lattice.</li>
 *  </ul>
 * </p>
 * 
 * <p>
 * Usage:
 * <ol>
 * 		<li>Construct for the given number of sources: {@link #RedundancyLattice(int)}</li>
 * 		<li>Lookup {@link NodeOfElements} and {@link ElementOfNodes} in the 
 * 			lattice based on their specification via Sets.</li>
 * 		<li>Return iterators over all possible elements of nodes, and
 * 			return elements based on their ids.</li>
 * 		<li>Return sets of nodes corresponding to each interaction structure</li>
 * </ol>
 * </p>
 * 
 * <p>
 * Mention whatever licensing terms we want to release it under here,
 *  e.g. GNU GPL 3
 *  Also request citation to our paper.
 * </p>
 *  
 * @author Ben Flecker and Joseph Lizier<br/>
 * <a href="mailto:btflecker at gmail.com">btflecker at gmail.com</a><br/>
 * <a href="mailto:joseph.lizier at gmail.com">joseph.lizier at gmail.com</a><br/>
 * <a href="http://lizier.me/joseph/">http://lizier.me/joseph/</a>
 *
 */
public class RedundancyLattice {

	/**
	 * Stores all possible elements of sources, allowing the user
	 * to draw an interator over them, and directly lookup
	 * a set of integers to the ElementOfSources that represents it.
	 */
	protected Hashtable<Set<Integer>,ElementOfSources> allPossibleElements;
	/**
	 * Array of all elements of sources, so we can translate from an element
	 *  id to the element object itself.
	 */
	protected ElementOfSources[] allElementsById;
	
	/**
	 * Stores all the nodes in the redundancy lattice, allow the user
	 * to directly lookup a set of sets of integers to the NodeOfElements
	 * that represents it.
	 */
	protected Hashtable<Set<Set<Integer>>,NodeOfElements> allNodes;
	
	/**
	 * The top node in the redundancy lattice.
	 */
	protected NodeOfElements topNode;
	
	/**
	 * Track which interaction order each node belongs to.
	 */
	protected List<Set<NodeOfElements>> nodesOfEachInteractionOrder;
	
	/**
	 * Construct the redundancy lattice from the given number of 
	 * sources.
	 * The method will:
	 * <ul>
	 * 	<li>Create every possible element of sources; store them
	 *      all in the allPossibleElements hashtable, and in the
	 *      allElementsById array indexed by their elementId</li>
	 *  <li>Create each required node, using these elements of sources
	 *      to build up the nodes, adding required child nodes to
	 *      each node, and storing all nodes in the allNodes hashtable</li>
	 *  <li>Keep a reference to the top node in the tree</li>
	 *  <li>Have nodesOfEachInteractionOrder track a set of nodes
	 *      at each interaction order.</li>
	 * </ul>
	 * 
	 * @param numSources
	 * @throws Exception where the number of sources is not supported.
	 */
	public RedundancyLattice(int numSources) throws Exception {
		if (numSources == 1) {
			buildRedundancyLatticeSize1();
		} else if (numSources == 2) {
			buildRedundancyLatticeSize2();
		} else if (numSources == 3) {
			buildRedundancyLatticeSize3();
		} else {
			throw new Exception("Not implemented for numSources == " + numSources);
			//TODO Implement: buildGenericRedundancyLattice();
		}
	}
	
	/**
	 * Specialist method to build up a redundancy lattice with 1 source nodes
	 */
	protected void buildRedundancyLatticeSize1() {
		// First build the source element:
		int currentElementId = 0;
		int[] singleSource = new int[1];
		singleSource[0] = 0;
		ElementOfSources element_0 =
			new ElementOfSources(singleSource, currentElementId++);
		// Create allElementsById array to track them by id
		allElementsById = new ElementOfSources[1];
		allElementsById[currentElementId - 1] = element_0;
		// And store them for later lookup:
		allPossibleElements = new Hashtable<Set<Integer>,ElementOfSources>(1);
		allPossibleElements.put(element_0.sources, element_0);
		
		// Now start building the redundancy lattice
		int currentNodeId = 0;
		allNodes = new Hashtable<Set<Set<Integer>>,NodeOfElements>(1);
		NodeOfElements node_0 = new NodeOfElements(currentNodeId++);
		node_0.addElementOfSources(element_0);
		allNodes.put(node_0.generateSetOfSetsOfSourceIds(), node_0);

		topNode = node_0;
		nodesOfEachInteractionOrder = new ArrayList<Set<NodeOfElements>>(1);
		Set<NodeOfElements> setOfOrder1 = new HashSet<NodeOfElements>();
		setOfOrder1.add(node_0);
		nodesOfEachInteractionOrder.add(setOfOrder1); // Add in first place
	}
	
	/**
	 * Specialist method to build up a redundancy lattice with 2 source nodes
	 */
	protected void buildRedundancyLatticeSize2() {
		// First build the source elements:
		int currentElementId = 0;
		int[] singleSource = new int[1];
		singleSource[0] = 0;
		ElementOfSources element_0 =
			new ElementOfSources(singleSource, currentElementId++);
		// Create allElementsById array to track them by id
		allElementsById = new ElementOfSources[3];
		allElementsById[currentElementId - 1] = element_0;
		singleSource[0] = 1;
		ElementOfSources element_1 =
			new ElementOfSources(singleSource, currentElementId++);
		allElementsById[currentElementId - 1] = element_1;
		int[] pairSources = new int[2];
		pairSources[0] = 0;
		pairSources[1] = 1;
		ElementOfSources pairElement =
			new ElementOfSources(pairSources, currentElementId++);
		allElementsById[currentElementId - 1] = pairElement;
		// And store them for later lookup:
		allPossibleElements = new Hashtable<Set<Integer>,ElementOfSources>(3);
		allPossibleElements.put(element_0.sources, element_0);
		allPossibleElements.put(element_1.sources, element_1);
		allPossibleElements.put(pairElement.sources, pairElement);
		
		// Now start building the redundancy lattice
		int currentNodeId = 0;
		allNodes = new Hashtable<Set<Set<Integer>>,NodeOfElements>(4);
		NodeOfElements node_0_1 = new NodeOfElements(currentNodeId++);
		node_0_1.addElementOfSources(element_0);
		node_0_1.addElementOfSources(element_1);
		allNodes.put(node_0_1.generateSetOfSetsOfSourceIds(), node_0_1);
		NodeOfElements node_0 = new NodeOfElements(currentNodeId++);
		node_0.addElementOfSources(element_0);
		node_0.addChildNode(node_0_1);
		allNodes.put(node_0.generateSetOfSetsOfSourceIds(), node_0);
		NodeOfElements node_1 = new NodeOfElements(currentNodeId++);
		node_1.addElementOfSources(element_1);
		node_1.addChildNode(node_0_1);
		allNodes.put(node_1.generateSetOfSetsOfSourceIds(), node_1);
		NodeOfElements node_01 = new NodeOfElements(currentNodeId++);
		node_01.addElementOfSources(pairElement);
		node_01.addChildNode(node_0);
		node_01.addChildNode(node_1);
		allNodes.put(node_01.generateSetOfSetsOfSourceIds(), node_01);
		topNode = node_01;
		nodesOfEachInteractionOrder = new ArrayList<Set<NodeOfElements>>(2);
		Set<NodeOfElements> setOfOrder1 = new HashSet<NodeOfElements>();
		setOfOrder1.add(node_0_1);
		setOfOrder1.add(node_0);
		setOfOrder1.add(node_1);
		nodesOfEachInteractionOrder.add(setOfOrder1); // Add in first place
		Set<NodeOfElements> setOfOrder2 = new HashSet<NodeOfElements>();
		setOfOrder2.add(node_01);
		nodesOfEachInteractionOrder.add(setOfOrder2); // Add in second place
	}
	
	/**
	 * Specialist method to build up a redundancy lattice with 3 source nodes
	 */
	protected void buildRedundancyLatticeSize3() {
		// 1. First build the source elements:
	    int currentElementId = 0;
    
	    // singleSource array
	    int [] singleSource = new int [1];
    
	    // single source 0 -EOS1-
	    singleSource[0] = 0;
	    ElementOfSources element_0 =
	        new ElementOfSources(singleSource, currentElementId++);
		// Create allElementsById array to track them by id
	    allElementsById = new ElementOfSources[7];
	    allElementsById[currentElementId - 1] = element_0;
	    
	    // single source 1 -EOS2-
	    singleSource[0] = 1;
	    ElementOfSources element_1 = 
	        new ElementOfSources(singleSource, currentElementId++);
	    allElementsById[currentElementId - 1] = element_1;
	    
	    // single source 2 -EOS3-
	    singleSource[0] = 2;
	    ElementOfSources element_2 = 
	        new ElementOfSources(singleSource, currentElementId++);
	    allElementsById[currentElementId - 1] = element_2;
	    
	    // pair source 01 -EOS4-
	    int[] pairSources_01 = new int[2];
	    pairSources_01[0] = 0;
	    pairSources_01[1] = 1;
	    ElementOfSources pairElement_01 =
	        new ElementOfSources(pairSources_01, currentElementId++);
	    allElementsById[currentElementId - 1] = pairElement_01;
	    
	    // pair source 02 -EOS5-
	    int[] pairSources_02 = new int[2];
	    pairSources_02[0] = 0;
	    pairSources_02[1] = 2;
	    ElementOfSources pairElement_02 =
	        new ElementOfSources(pairSources_02, currentElementId++);
	    allElementsById[currentElementId - 1] = pairElement_02;
	    
	    // pair source 12 -EOS6-
	    int[] pairSources_12 = new int[2];
	    pairSources_12[0] = 1;
	    pairSources_12[1] = 2;
	    ElementOfSources pairElement_12 =
	        new ElementOfSources(pairSources_12, currentElementId++);
	    allElementsById[currentElementId - 1] = pairElement_12;
	    
	    // triplet source 012 -EOS7-
	    int[] tripletSources_012 = new int[3];
	    tripletSources_012[0] = 0;
	    tripletSources_012[1] = 1;
	    tripletSources_012[2] = 2;
	    ElementOfSources tripletElement_012 =
	        new ElementOfSources(tripletSources_012, currentElementId++);
	    allElementsById[currentElementId - 1] = tripletElement_012;
	
	    //2. And store them for later lookup:
	    allPossibleElements = new Hashtable<Set<Integer>,ElementOfSources>(7);
	    allPossibleElements.put(element_0.sources, element_0);
	    allPossibleElements.put(element_1.sources, element_1);
	    allPossibleElements.put(element_2.sources, element_2);
	    allPossibleElements.put(pairElement_01.sources, pairElement_01);
	    allPossibleElements.put(pairElement_02.sources, pairElement_02);
	    allPossibleElements.put(pairElement_12.sources, pairElement_12);
	    allPossibleElements.put(tripletElement_012.sources, tripletElement_012);
	    
	    // 3. Now start building the redundancy lattice
	    int currentNodeId = 0;
	    
	    // Initialize template lattice
	    allNodes = new Hashtable<Set<Set<Integer>>,NodeOfElements>(18);
	    
	    // Node {0}{1}{2} -NID1-
	    NodeOfElements node_0_1_2 = new NodeOfElements(currentNodeId++);
	    node_0_1_2.addElementOfSources(element_0);
	    node_0_1_2.addElementOfSources(element_1);
	    node_0_1_2.addElementOfSources(element_2);
	    // Put in lattice
	    allNodes.put(node_0_1_2.generateSetOfSetsOfSourceIds(), node_0_1_2);
	    
	    // Node {0}{1} -NID2-
	    NodeOfElements node_0_1 = new NodeOfElements(currentNodeId++);
	    node_0_1.addElementOfSources(element_0);
	    node_0_1.addElementOfSources(element_1);
	    // Child nodes
	    node_0_1.addChildNode(node_0_1_2);
	    // Put in lattice
	    allNodes.put(node_0_1.generateSetOfSetsOfSourceIds(), node_0_1);
	    
	    // Node {0}{2} -NID3-
	    NodeOfElements node_0_2 = new NodeOfElements(currentNodeId++);
	    node_0_2.addElementOfSources(element_0);
	    node_0_2.addElementOfSources(element_2);
	    // Child nodes
	    node_0_2.addChildNode(node_0_1_2);
	    // Put in lattice
	    allNodes.put(node_0_2.generateSetOfSetsOfSourceIds(), node_0_2);
	
	    // Node {1}{2} -NID4-
	    NodeOfElements node_1_2 = new NodeOfElements(currentNodeId++);
	    node_1_2.addElementOfSources(element_1);
	    node_1_2.addElementOfSources(element_2);
	    // Child nodes
	    node_1_2.addChildNode(node_0_1_2);
	    // Put in lattice
	    allNodes.put(node_1_2.generateSetOfSetsOfSourceIds(), node_1_2);
	    
	    // Node {0}{1,2} -NID5-
	    NodeOfElements node_0_12 = new NodeOfElements(currentNodeId++);
	    node_0_12.addElementOfSources(element_0);
	    node_0_12.addElementOfSources(pairElement_12);
	    // Child nodes
	    node_0_12.addChildNode(node_0_1);
	    node_0_12.addChildNode(node_0_2);
	    // Put in lattice
	    allNodes.put(node_0_12.generateSetOfSetsOfSourceIds(), node_0_12);
	    
	    // Node {1}{0,2} -NID6-
	    NodeOfElements node_1_02 = new NodeOfElements(currentNodeId++);
	    node_1_02.addElementOfSources(element_1);
	    node_1_02.addElementOfSources(pairElement_02);
	    // Child nodes
	    node_1_02.addChildNode(node_0_1);
	    node_1_02.addChildNode(node_1_2);
	    // Put in lattice
	    allNodes.put(node_1_02.generateSetOfSetsOfSourceIds(), node_1_02);
	
	    // Node {2}{0,1} -NID7-
	    NodeOfElements node_2_01 = new NodeOfElements(currentNodeId++);
	    node_2_01.addElementOfSources(element_2);
	    node_2_01.addElementOfSources(pairElement_01);
	    // Child nodes
	    node_2_01.addChildNode(node_0_2);
	    node_2_01.addChildNode(node_1_2);
	    // Put in lattice
	    allNodes.put(node_2_01.generateSetOfSetsOfSourceIds(), node_2_01);
	
	    // Node {0} -NID8-
	    NodeOfElements node_0 = new NodeOfElements(currentNodeId++);
	    node_0.addElementOfSources(element_0);
	    // Child nodes
	    node_0.addChildNode(node_0_12);
	    // Put in lattice
	    allNodes.put(node_0.generateSetOfSetsOfSourceIds(), node_0);
	
	    // Node {1} -NID9-
	    NodeOfElements node_1 = new NodeOfElements(currentNodeId++);
	    node_1.addElementOfSources(element_1);
	    // Child nodes
	    node_1.addChildNode(node_1_02);
	    // Put in lattice
	    allNodes.put(node_1.generateSetOfSetsOfSourceIds(), node_1);
	
	    // Node {2} -NID10-
	    NodeOfElements node_2 = new NodeOfElements(currentNodeId++);
	    node_2.addElementOfSources(element_2);
	    // Child nodes
	    node_2.addChildNode(node_2_01);
	    // Put in lattice
	    allNodes.put(node_2.generateSetOfSetsOfSourceIds(), node_2);
	
	    // Node {0,1}{0,2}{1,2} -NID11-
	    NodeOfElements node_01_02_12 = new NodeOfElements(currentNodeId++);
	    node_01_02_12.addElementOfSources(pairElement_01);
	    node_01_02_12.addElementOfSources(pairElement_02);
	    node_01_02_12.addElementOfSources(pairElement_12);
	    // Child nodes
	    node_01_02_12.addChildNode(node_0_12);
	    node_01_02_12.addChildNode(node_1_02);
	    node_01_02_12.addChildNode(node_2_01);
	    // Put in lattice
	    allNodes.put(node_01_02_12.generateSetOfSetsOfSourceIds(), node_01_02_12);
	
	    // Node {0,1}{0,2} -NID12-
	    NodeOfElements node_01_02 = new NodeOfElements(currentNodeId++);
	    node_01_02.addElementOfSources(pairElement_01);
	    node_01_02.addElementOfSources(pairElement_02);
	    // Child nodes
	    node_01_02.addChildNode(node_0);
	    node_01_02.addChildNode(node_01_02_12);
	    // Put in lattice
	    allNodes.put(node_01_02.generateSetOfSetsOfSourceIds(), node_01_02);
	
	    // Node {0,1}{1,2} -NID13-
	    NodeOfElements node_01_12 = new NodeOfElements(currentNodeId++);
	    node_01_12.addElementOfSources(pairElement_01);
	    node_01_12.addElementOfSources(pairElement_12);
	    // Child nodes
	    node_01_12.addChildNode(node_1);
	    node_01_12.addChildNode(node_01_02_12);
	    // Put in lattice
	    allNodes.put(node_01_12.generateSetOfSetsOfSourceIds(), node_01_12);
	
	    // Node {0,2}{1,2} -NID14-
	    NodeOfElements node_02_12 = new NodeOfElements(currentNodeId++);
	    node_02_12.addElementOfSources(pairElement_02);
	    node_02_12.addElementOfSources(pairElement_12);
	    // Child nodes
	    node_02_12.addChildNode(node_2);
	    node_02_12.addChildNode(node_01_02_12);
	    // Put in lattice
	    allNodes.put(node_02_12.generateSetOfSetsOfSourceIds(), node_02_12);
	
	    // Node {0,1} -NID15-
	    NodeOfElements node_01 = new NodeOfElements(currentNodeId++);
	    node_01.addElementOfSources(pairElement_01);
	    // Child nodes
	    node_01.addChildNode(node_01_02);
	    node_01.addChildNode(node_01_12);
	    // Put in lattice
	    allNodes.put(node_01.generateSetOfSetsOfSourceIds(), node_01);
	
	    // Node {0,2} -NID16-
	    NodeOfElements node_02 = new NodeOfElements(currentNodeId++);
	    node_02.addElementOfSources(pairElement_02);
	    // Child nodes
	    node_02.addChildNode(node_01_02);
	    node_02.addChildNode(node_02_12);
	    // Put in lattice
	    allNodes.put(node_02.generateSetOfSetsOfSourceIds(), node_02);
	
	    // Node {1,2} -NID17-
	    NodeOfElements node_12 = new NodeOfElements(currentNodeId++);
	    node_12.addElementOfSources(pairElement_12);
	    // Child nodes
	    node_12.addChildNode(node_01_12);
	    node_12.addChildNode(node_02_12);
	    // Put in lattice
	    allNodes.put(node_12.generateSetOfSetsOfSourceIds(), node_12);
	
	    // Node {0,1,2} -NID18-
	    NodeOfElements node_012 = new NodeOfElements(currentNodeId++);
	    node_012.addElementOfSources(tripletElement_012);
	    // Child nodes
	    node_012.addChildNode(node_01);
	    node_012.addChildNode(node_02);
	    node_012.addChildNode(node_12);
	    // Put in lattice
	    allNodes.put(node_012.generateSetOfSetsOfSourceIds(), node_012);
	
	    // Top node
	    topNode = node_012;
	
	    // Set up interaction orders of nodes
	    nodesOfEachInteractionOrder = new ArrayList<Set<NodeOfElements>>(3);
	
	    // Define nodes of order 1
	    Set<NodeOfElements> setOfOrder1 = new HashSet<NodeOfElements>();
	    setOfOrder1.add(node_0_1_2);
	    setOfOrder1.add(node_0_1);
	    setOfOrder1.add(node_0_2);
	    setOfOrder1.add(node_1_2);
	    setOfOrder1.add(node_0);
	    setOfOrder1.add(node_1);
	    setOfOrder1.add(node_2);
	    setOfOrder1.add(node_0_12);
	    setOfOrder1.add(node_1_02);
	    setOfOrder1.add(node_2_01);
	    nodesOfEachInteractionOrder.add(setOfOrder1); // Must be added first
	
	    // Define nodes of order 2
	    Set<NodeOfElements> setOfOrder2 = new HashSet<NodeOfElements>();
	    setOfOrder2.add(node_01_02_12);
	    setOfOrder2.add(node_01_02);
	    setOfOrder2.add(node_01_12);
	    setOfOrder2.add(node_02_12);
	    setOfOrder2.add(node_01);
	    setOfOrder2.add(node_02);
	    setOfOrder2.add(node_12);
	    nodesOfEachInteractionOrder.add(setOfOrder2); // Added 2nd
	
	    // Define nodes of order 3
	    Set<NodeOfElements> setOfOrder3 = new HashSet<NodeOfElements>();
	    setOfOrder3.add(node_012);
	    nodesOfEachInteractionOrder.add(setOfOrder3); // Added 3rd
	}

	
	/**
	 * Return the node of elements for the given node specification
	 * 
	 * @param node Set of Sets of Integers specifying the node.
	 * @return the NodeOfElements corresponding to the specification
	 * 		or null if there is no such node.
	 */
	public NodeOfElements getNodeOfElements(Set<Set<Integer>> node) {
		return allNodes.get(node);
	}
	
	/**
	 * Return the child nodes of the source node specified by the
	 *  given set of source element ids
	 * 
	 * @param node Set of Sets of Integers specifying the node.
	 * @return a Set of child nodes from the node specified
	 * 		(which may be an empty set), or null if there is no
	 *      node matching that specified.
	 */
	public Set<NodeOfElements> getChildrenOfNode(Set<Set<Integer>> node) {
		NodeOfElements nodeOfElements = allNodes.get(node);
		if (nodeOfElements == null) {
			return null;
		}
		return nodeOfElements.childNodes;
	}
	
	/**
	 * Returns the number of Nodes of Elements represented in this
	 *  redundancy lattice
	 * 
	 * @return
	 */
	public int getNumberOfNodes() {
		return allNodes.size();
	}
	
	/**
	 * Converts a specification of a source element (a set of sources)
	 *  into the integer id used for this source element internally.
	 * 
	 * @param sourceElement Set of Integers specifying the element
	 * @return integer id of the corresponding source element, or -1
	 *   if there is no source element corresponding to this specification.
	 */
	public int sourceElementToId(Set<Integer> sourceElement) {
		ElementOfSources element = allPossibleElements.get(sourceElement);
		if (element == null) {
			// There is no element corresponding to this specification
			return -1;
		} else {
			return element.id;			
		}
	}

	/**
	 * Look up and return the SourceElement object described
	 *  by the given sourceElement set.
	 * 
	 * @param sourceElement Set of Integers specifying the element
	 * @return ElementOfSources corresponding to that specification, or
	 *   null if there is no element corresponding to it.
	 */
	public ElementOfSources lookupSourceElement(Set<Integer> sourceElement) {
		return allPossibleElements.get(sourceElement);
	}
	
	/**
	 * Look up and return the ElementOfSources object with the given id,
	 * or null if there is no such ElementOfSources
	 * 
	 * @param sourceElementId
	 * @return ElementOfSources corresponding to that id, or null if 
	 *   there is no such ElementOfSources
	 */
	public ElementOfSources lookupSourceElement(int sourceElementId) {
		if ((sourceElementId < 0) || (sourceElementId >= allElementsById.length)) {
			return null;
		}
		return allElementsById[sourceElementId];
	}
	
	/**
	 * Return an iterator over all possible source elements.
	 * 
	 * @return
	 */
	public Collection<ElementOfSources> getElementsOfSources() {
		return allPossibleElements.values();
	}
	
	/**
	 * Returns the number of Elements of Sources represented in this
	 *  RedundancyLattice
	 * 
	 * @return the number of elements.
	 */
	public int getNumberOfElements() {
		return allPossibleElements.size();
	}
	
	/**
	 * Return the set of NodeOfElements, which are all nodes in the lattice
	 *  which each have the minimum size of an
	 *  element being "order".
	 * 
	 * @param order
	 * @return the set of NodeOfElements satisfying this criteria (an empty set
	 *  is returned if there are no nodes at this order)
	 */
	public Set<NodeOfElements> getNodesForGivenInteractionOrder(int order) {
		if ((order < 1) || (order > nodesOfEachInteractionOrder.size())) {
			return new HashSet<NodeOfElements>();
		}
		// Else we have nodes at this order:
		return nodesOfEachInteractionOrder.get(order - 1);
	}
	
	/**
	 * Simple method to print out representations of each node.
	 * This will print twice any node which is a child of multiple other nodes.
	 * To print such nodes just once, we would need to mark that they have been visited
	 *  (but this has not yet been implemented)
	 */
	public void print(){
		topNode.printNode(0);
	}
	
	/**
	 * Getter for the top node in the redundancy lattice
	 * 
	 * @return the top node in the redundancy lattice
	 */
	public NodeOfElements getTopNode() {
		return topNode;
	}

	/**
	 * Convert a string specification of an element of sources
	 * to a set of Integers (which can be used in lookups such as 
	 * {@link #sourceElementToId(Set)}).
	 * 
	 * @param spec a string containing a comma-delimited list of integers,
	 *  which may be contained within curly braces, e.g. "{0,1,2}" or
	 *  "0,1,2" are both accepted. We are not checking correctness of the
	 *  use of { or }, merely ignoring them.
	 * @return a set of Integers specification
	 */
	public static Set<Integer> elementSpecFromString(String spec) {
		// Split the specification on curly braces or commas
		String[] sourceIdStrings = spec.split("[,\\{\\}]");
		Set<Integer> setSpec = new HashSet<Integer>();
		for (int i = 0; i < sourceIdStrings.length; i++) {
			if (sourceIdStrings[i].length() == 0) {
				// Ignore empty strings (can result from the braces
				//  being counted as delimiters
				continue;
			}
			Integer thisInt = Integer.parseInt(sourceIdStrings[i]);
			setSpec.add(thisInt);
		}
		return setSpec;
	}
	
	/**
	 * Convert a string specification of a node of elements of sources
	 * into a set of sets of Integers (which may be used in lookups such
	 * as {@link #getNodeOfElements(Set)})
	 * 
	 * @param spec a string containing a set of elements, where
	 *  each element is contained in curly braces, and each element is
	 *  a comma-delimited list of integers. E.g. this could be "{1}" or
	 *  "{0,1}{2}". Braces must be used here.
	 * @return a set of set of Integers specification
	 * @throws Exception for poorly formatted String specifications
	 */
	public static Set<Set<Integer>> nodeSpecFromString(String spec) throws Exception {
		// Check that the first and last characters are the
		//  required braces:
		if ((spec.charAt(0) != '{') || (spec.charAt(spec.length() - 1) != '}')) {
			throw new Exception("Invalid Node specification (does not start and end with curly braces)");
		}
		// Remove the first and last braces
		spec = spec.substring(1, spec.length() - 1);
		// Set up our set specification
		Set<Set<Integer>> setSpec = new HashSet<Set<Integer>>();
		// Then split on "}{" combinations
		String[] elementStrings = spec.split("\\}\\{");
		for (int i = 0; i < elementStrings.length; i++) {
			if (elementStrings[i].length() == 0) {
				throw new Exception("Empty elements are not allowed");
			}
			// Convert this element string spec to a set spec
			Set<Integer> elementSpec = elementSpecFromString(elementStrings[i]);
			// And add it to our spec of the set.
			setSpec.add(elementSpec);
		}
		return setSpec;
	}
	
}
