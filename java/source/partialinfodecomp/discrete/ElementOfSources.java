/**
 * 
 */
package partialinfodecomp.discrete;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Implements the element of individual sources for the Partial Information Decomposition.
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
 * 		<li>Each node {@link #NodeOfElements} is a set of source elements.</li>
 * 		<li>Each source element (this class) is a set of single sources.</li>
 * 		<li>Each node has descendant nodes, who do not provide any 
 * 			redundant information that this ancestor node provides 
 * 			(i.e. the descendant nodes are lower in the redundancy lattice).</li>
 * 		<li>Each node has direct children in the redundancy lattice.</li>
 *  </ul>
 * </p>
 * 
 * <p>
 * TODO Finish comments here and for other method headers.
 * Usage:
 * <ol>
 * 		<li>Construct: {@link ...}</li>
 * 		<li>Fill me out ...</li>
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
public class ElementOfSources {
	
	/**
	 * Set of source identifiers in this element
	 */
	protected Set<Integer> sources;
	/**
	 * List of sources in order that they be considered for
	 *  computing joint values of these sources.
	 */
	protected int[] orderedSourceList;
	/**
	 * Identifier of this source element
	 */
	protected int id;
	
	/**
	 * Constructor for an Element of sources.
	 * Constructor is protected, as it should only be created in the
	 *  instantiation of a redundancy lattice.
	 * 
	 * @param sourceList array of source ids (does not have to be sorted)
	 * @param id
	 */
	protected ElementOfSources(int[] sourceList, int id) {
		
		this.id = id;
		orderedSourceList = Arrays.copyOf(sourceList, sourceList.length);
		java.util.Arrays.sort(orderedSourceList, 0, orderedSourceList.length);
		// Implementation choice for the set of individual sources
		//  shouldn't really matter since it will not be large.
		sources = new HashSet<Integer>(orderedSourceList.length);
		for (int i = 0; i < orderedSourceList.length; i++) {
			Integer sourceInt = new Integer(orderedSourceList[i]);
			sources.add(sourceInt);
		}
	}
	
	/**
	 * 
	 * Constructor is protected, as it should only be created in the
	 *  instantiation of a redundancy lattice.
	 *
	 * @param sources
	 * @param id
	 */
	protected ElementOfSources(Set<Integer> sources, int id) {
		this.id = id;
		this.sources = sources;
		orderedSourceList = new int[sources.size()];
		int i = 0;
		for (Integer source : sources) {
			orderedSourceList[i++] = source.intValue();
		}
		// And then sort the list
		java.util.Arrays.sort(orderedSourceList, 0, orderedSourceList.length);
	}
	
	/**
	 * Write out the nodes in this element
	 */
	public String toString() {
		return toString(false);
	}
	
	/**
	 * Write out the nodes in this element
	 */
	public String toString(boolean writeId) {
		StringBuffer buffer = new StringBuffer();
		if (writeId) {
			buffer.append("id:" + id);
		}
		buffer.append("{");
		for (int i = 0; i < orderedSourceList.length; i++) {
			if (i > 0) {
				buffer.append(",");
			}
			buffer.append(orderedSourceList[i]);
		}
		buffer.append("}");
		return buffer.toString();
	}
}
