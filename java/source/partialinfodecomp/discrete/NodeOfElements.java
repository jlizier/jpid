package partialinfodecomp.discrete;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO Make this an abstract class or interface, and pull the functionality into
 *  a specific PROTECTED child class. Same for ElementOfSources.
 * 
 * <p>
 * Implements the node of source elements in the redundancy lattice
 * for the Partial Information Decomposition.
 * </p>
 * 
 * <p>
 * See: "Nonnegative Decomposition of Multivariate Information",
 * Paul L. Williams and Randall D. Beer, 2010
 * <a href="http://arxiv.org/abs/1004.2515">arXiv:1004.2515</a>
 * <p>
 * 
 * <p>
 * Nomencalture:
 * 	<ul>
 * 		<li>The redundancy lattice {@link #RedundancyLattice}
 * 			is a directed network of nodes.</li>
 * 		<li>Each node (this class) is a set of source elements.</li>
 * 		<li>Each source element {@link #ElementOfSources} is a set of single sources.</li>
 * 		<li>Each node has descendant nodes, who do not provide any 
 * 			redundant information that this ancestor node provides 
 * 			(i.e. the descendant nodes are lower in the redundancy lattice).</li>
 * 		<li>Each node has direct children in the redundancy lattice.</li>
 *  </ul>
 * </p>
 * 
 * <p>
 * Usage:
 * <ol>
 * 		<li>Construct: {@link this#NodeOfElements(int)} or
 * 			{@link this#NodeOfElements(Set, int)} or
 * 			{@link this#NodeOfElements(Set, Set, int)}</li>
 * 		<li>Complete specification of the node using:
 * 			{@link #addElementOfSources(ElementOfSources)},
 * 			{@link #addChildNode(NodeOfElements)}</li>
 * 		<li>Store it in a lookup table, using 
 * 			{@link #generateSetOfSetsOfSourceIds()} to provide a key</li>
 * 		<li>Use information from the node, e.g. 
 * 			{@link #getChildNodes()}, {@link #printNode(int)},
 * 			{@link #printNode(int, boolean)}</li>
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
public class NodeOfElements {
	
	/**
	 * Set of source identifiers in this element
	 */
	protected Set<ElementOfSources> elements;
	/**
	 * Set of direct children of this node
	 */
	protected Set<NodeOfElements> childNodes;
	/**
	 * Identifier of this node of sources
	 */
	protected int id;
	
	/**
	 * 
	 * Constructor is protected, as it should only be created in the
	 *  instantiation of a redundancy lattice.
	 *  
	 * @param elements
	 * @param childNodes
	 * @param id
	 */
	protected NodeOfElements(Set<ElementOfSources> elements,
						Set<NodeOfElements> childNodes, int id) {
		this.elements = elements;
		if (childNodes == null) {
			// Initialise with an empty set of child nodes
			this.childNodes = new HashSet<NodeOfElements>(1);
		} else {
			this.childNodes = childNodes;
		}
		this.id = id;
	}

	/**
	 * 
	 * Constructor is protected, as it should only be created in the
	 *  instantiation of a redundancy lattice.
	 *  
	 * @param elements
	 * @param id
	 */
	protected NodeOfElements(Set<ElementOfSources> elements, int id) {
		this.elements = elements;
		this.childNodes = new HashSet<NodeOfElements>(1);
		this.id = id;
	}

	/**
	 * 
	 * Constructor is protected, as it should only be created in the
	 *  instantiation of a redundancy lattice.
	 * The elements must be explicitly added after this constructor is called
	 *  
	 * @param id
	 */
	protected NodeOfElements(int id) {
		this.elements = new HashSet<ElementOfSources>(1);
		this.childNodes = new HashSet<NodeOfElements>(1);
		this.id = id;
	}

	/**
	 * Adds a new child node to this node.
	 * Should only be called during the construction process by
	 *  a RedundancyLattice object. 
	 * 
	 * @param newChild new child node
	 */
	protected void addChildNode(NodeOfElements newChild) {
		childNodes.add(newChild);
	}
	
	/**
	 * Adds a new element of sources to this node.
	 * Should only be called during the construction process by
	 *  a RedundancyLattice object. 
	 * 
	 * @param element new element of sources in this node
	 */
	protected void addElementOfSources(ElementOfSources element) {
		elements.add(element);
	}
	
	/**
	 * Return the child nodes of this source node
	 * 
	 * @return set of child nodes. If there are no child nodes, this
	 *   is not null but is an empty set.
	 */
	public Set<NodeOfElements> getChildNodes() {
		return childNodes;
	}
	
	/**
	 * Generate a Set of Sets of Integers corresponding to the
	 * set of elements of sources which this node represents.
	 * This method is useful for inserting the node into a hashtable
	 * which is keyed by the specification of the node.
	 * 
	 * @return Set of Sets of Integers specifying this node.
	 */
	public Set<Set<Integer>> generateSetOfSetsOfSourceIds() {
		Set<Set<Integer>> setOfElements = new HashSet<Set<Integer>>(elements.size());
		for (ElementOfSources element : elements) {
			setOfElements.add(element.sources);
		}
		return setOfElements;
	}
	
	/**
	 * Print out the elements of this node,
	 *  then print out the child nodes on new lines
	 *  (if indicated by printChildren) with
	 *  an increase in the tab level.
	 * 
	 * @param node
	 * @param printChildren
	 * @param tabs
	 */
	public void printNode(int tabs, boolean printChildren) {
		for (int i = 0; i < tabs; i++) {
			System.out.print("\t");
		}
		for (ElementOfSources element : elements) {
			System.out.print(element);
		}
		System.out.println();
		if (printChildren) {
			for (NodeOfElements child: childNodes) {
				child.printNode(tabs+1, true);
			}
		}
	}
	
	/**
	 * Print out the elements of this node,
	 *  then print out the child nodes on new lines
	 *  (if indicated by printChildren) with
	 *  an increase in the tab level.
	 *  All output is directed to a StringBuffer.
	 * 
	 * @param tabs
	 * @param printChildren
	 * @param buffer
	 */
	public void printNode(int tabs, boolean printChildren, StringBuffer buffer) {
		for (int i = 0; i < tabs; i++) {
			buffer.append("\t");
		}
		for (ElementOfSources element : elements) {
			buffer.append(element);
		}
		buffer.append("\n");
		if (printChildren) {
			for (NodeOfElements child: childNodes) {
				child.printNode(tabs+1, true, buffer);
			}
		}
	}

	/**
	 * Print out the elements of this node,
	 *  then print out the child nodes on new lines, with
	 *  an increase in the tab level.
	 * 
	 * @param node
	 * @param tabs
	 */
	public void printNode(int tabs) {
		printNode(tabs, true);
	}
}
