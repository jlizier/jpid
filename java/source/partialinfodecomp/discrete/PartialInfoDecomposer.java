package partialinfodecomp.discrete;

import java.util.Arrays;
import java.util.Set;

/**
 * <p>
 * Implements the Partial Information Decomposition.
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
 * 		<li>Construct: {@link #PartialInfoDecomposer(int, int[])}</li>
 * 		<li>Provide observations (can be accumulated):
 * 			<ol>
 * 				<li>{@link #addObservations(int[], int[][])}</li>
 * 				<li>{@link #addObservation(int, int[])}</li>
 * 			</ol>
 * 	    </li>
 * 		<li>Compute required entities (all returned in bits):
 * 			<ol>
 * 				<li>{@link #Imin(NodeOfElements)}</li>
 * 				<li>{@link #PI(NodeOfElements)}</li>
 * 			</ol>
 * 		</li>
 * 		<li>Re-initialise (then start adding observations again):
 * 	{@link #initialise()}</li>
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
 * TODO Pull all methods computing "local" values into a child class.
 *
 */
public class PartialInfoDecomposer {

	/**
	 * Base of the target (ie how many different values can the target have)
	 */
	protected int targetBase;
	/**
	 * Number of source variables we are dealing with
	 */
	protected int numSources;
	/**
	 * Integer array of the bases of the sources
	 */
	protected int[] sourcesBases;
	/**
	 * Number of observations we have been supplied
	 */
	protected int numObservations;
	/**
	 * Count of the number of times each target state (the index to the array)
	 *  is observed
	 */
	protected int[] targetCount;
	/**
	 * Joint count of the number of times each target state (the second index to
	 *  the array) is observed, with a particular nodes of elements
	 *  which is identified by the first index of the array
	 *  and whose specific value is given in the third element of the array.
	 *  There will be 2^numSources elements - 1 (removing the empty set)
	 *   since this is the sum of numSources-choose-k for k=0 to numSources.
	 *  Keeping PDFs for all of them (instead of recomputing as required)
	 *   will cost us more space (I think on order numSources - TODO check this)
	 *   but less time in recomputing them, and I don't think the space cost
	 *   will prove too big a problem.
	 *  TODO Work out if the array index ordering is the most efficient
	 */
	protected int[][][] jointCountTargetAndSourceElement;
	/**
	 * Counts of observations, for each source element.
	 * The first index is the source element id, the second index
	 *  is the specific value of that source element.
	 */
	protected int[][] sourceElementCounts;
	/**
	 * The redundancy lattice generated for the given number of sources.
	 * Can be accessed by the user via {@link #getRedundancyLattice()}
	 * so that they can lookup relevant nodes and elements of sources.
	 */
	protected RedundancyLattice redundancyLattice;
	/**
	 * Keep log(2) as a constant for use in normalising to bits.
	 */
	protected double LOG_2 = Math.log(2.0);
	
	/**
	 * Whether we have stored/cached any PID computations here to speed up
	 *  later function calls.
	 */
	protected boolean storedAnyComptuations = false;
	/**
	 * Stored/cached values of the MinimisingElement for the partial infos for
	 *  each target value for each node
	 */
	protected MinimisingElement[][] storedMinsOfIs;
	/**
	 * Stored/cached values of the maximisation across children of the 
	 *  MinimisingElement for the partial infos for each target value
	 *  for each node
	 */
	protected MinimisingElement[][] storedMaxMinsOfChildrensIs;
	/**
	 * Stored/cached values of the specific infos for each target value for each
	 *  Element of Sources
	 */
	protected double[][] storedSpecificInfos;
	/**
	 * Stored/cached values of the local specific infos for each target value
	 *  for each Element of Sources
	 */
	protected double[][][] storedLocalSpecificInfos;
	
	/**
	 * Whether to make debug prints or not
	 */
	protected boolean debug = false;
	
	/**
	 * Returns a new PartialInfoDecomposer, configured for the given
	 *  base and number of sources.
	 * 
	 * @param base base for all variables
	 * @param numSources number of sources
	 * @throws Exception where the number of sources is not supported.
	 */
	public PartialInfoDecomposer(int base, int numSources) throws Exception {
		// 1. Store targetBase, and copy of sourceBases, set numSources
		targetBase = base;
		this.numSources = numSources;
		sourcesBases = new int[numSources];
		java.util.Arrays.fill(sourcesBases, base);
		constructorCommon();
	}

	/**
	 * Returns a new PartialInfoDecomposer, configured for the given
	 *  targetBase, and sourcesBases.length sources, with each source i
	 *  with base sourcesBases[i].
	 * 
	 * @param targetBase
	 * @param sourcesBases
	 * @throws Exception where the number of sources is not supported.
	 */
	public PartialInfoDecomposer(int targetBase, int[] sourcesBases) 
		throws Exception {
		
		// 1. Store targetBase, and copy of sourceBases, set numSources
		this.targetBase = targetBase;
		numSources = sourcesBases.length;
		this.sourcesBases = java.util.Arrays.copyOf(sourcesBases, sourcesBases.length);
		constructorCommon();
	}
	
	/**
	 * Perform common functions regardless of which constructor was called
	 * 
	 * @throws Exception where the number of sources is not supported.
	 */
	protected void constructorCommon() throws Exception {
		// 2. Construct the redundancy lattice by creating a RedundancyLattice
		//     object
		redundancyLattice = new RedundancyLattice(numSources);
		// 3. Create space for the target counts. Check that we have
		//   enough space to do this.
		targetCount = new int[targetBase];
		// 4. create enough space for each the counts for each element here
		jointCountTargetAndSourceElement =
			new int[redundancyLattice.getNumberOfElements()][][];
		sourceElementCounts =
			new int[redundancyLattice.getNumberOfElements()][];
		// We'll finish initialising them shortly ...

		// 5. Create storage for our precomputed values:
		storedMinsOfIs = new MinimisingElement[targetBase][redundancyLattice.getNumberOfNodes()];
		storedMaxMinsOfChildrensIs = new MinimisingElement[targetBase][redundancyLattice.getNumberOfNodes()];
		storedSpecificInfos = new double[targetBase][redundancyLattice.getNumberOfElements()];
		storedLocalSpecificInfos = new double[targetBase][redundancyLattice.getNumberOfElements()][];
		
		// 6. And finish initialising space for the counts and precomputed values where we need to know
		//  how many possible joint states there are for each element:
		for (ElementOfSources elOfSources : redundancyLattice.getElementsOfSources()) {
			// Compute the number of possible joint states of the sources
			//  in this element
			int numJointStates = 1;
			for (int s = 0; s < elOfSources.orderedSourceList.length; s++) {
				numJointStates *= sourcesBases[elOfSources.orderedSourceList[s]];
			}
			// Initialise the storage for counts:
			jointCountTargetAndSourceElement[elOfSources.id] = new int[targetBase][numJointStates];
			sourceElementCounts[elOfSources.id] = new int[numJointStates];
			
			// And initialise the storage for precomputed values:
			for (int t = 0; t < targetBase; t++) {
				storedLocalSpecificInfos[t][elOfSources.id] = new double[numJointStates];
			}
		}
		
		// 7. And initialise the precomputed values:
		for (int t = 0; t < targetBase; t++) {
			// No need to initialise the null values.
			// Since the specific info may be negative, we store NaN
			//  to indicate that we have not computed it yet.
			Arrays.fill(storedSpecificInfos[t], Double.NaN);
			for (int e = 0; e < redundancyLattice.getNumberOfElements(); e++) {
				Arrays.fill(storedLocalSpecificInfos[t][e], Double.NaN);				
			}
		}
	}
	
	/**
	 * Clears the supplied observations, so the decomposer can be used
	 *  again with a new set of observations.
	 * Does not need to specifically be called after constructing.
	 */
	public void initialise() {
		// Clear all current observations in targetCount and targetCountGivenSourceElement
		Arrays.fill(targetCount, 0);
		for (ElementOfSources elOfSources : redundancyLattice.getElementsOfSources()) {
			for (int t = 0; t < targetBase; t++) {
				Arrays.fill(jointCountTargetAndSourceElement[elOfSources.id][t], 0);
			}
			Arrays.fill(sourceElementCounts[elOfSources.id], 0);
		}
		clearAllStoredValues();
	}
	
	/**
	 * Clear all of the precomputed values that we have stored 
	 *  here to speed up later computation.
	 */
	protected void clearAllStoredValues() {
		storedAnyComptuations = false;
		for (int t = 0; t < targetBase; t++) {
			Arrays.fill(storedMinsOfIs[t], null);
			Arrays.fill(storedMaxMinsOfChildrensIs[t], null);
			// Since the specific info may be negative, we store NaN
			//  to indicate that we have not computed it yet.
			Arrays.fill(storedSpecificInfos[t], Double.NaN);
			for (int e = 0; e < redundancyLattice.getNumberOfElements(); e++) {
				Arrays.fill(storedLocalSpecificInfos[t][e], Double.NaN);				
			}
		}
	}
	
	/**
	 * Update the probability distribution functions with the given
	 *  time series of observations.
	 *  
	 * @param targetValues time series of target values
	 * @param sourceValues time series of the set of source values
	 *   (first index is time, second is source index - sources must
	 *   be ordered as per the call to the constructor.
	 */
	public void addObservations(int[] targetValues, int[][] sourceValues) {
		// For each time step call addObservation.
		// We should perhaps inline this later, but for the moment assume
		//  that the compiler will do this for us
		for (int n = 0; n < targetValues.length; n++) {
			addObservation(targetValues[n], sourceValues[n]);
		}
	}
	
	/**
	 * Update the probability distribution functions with the given
	 *  observation
	 *  
	 * @param targetValue target value
	 * @param sourceValue the set of source values
	 *   (source index - sources must be ordered as per the call to the constructor.
	 */
	public void addObservation(int targetValue, int[] sourceValue) {
		// Update our counts for this observation of the target
		//  for every element of sources we are tracking.
		targetCount[targetValue]++;
		for (ElementOfSources elOfSources : redundancyLattice.getElementsOfSources()) {
			int jointSourceValue = jointValueForElement(sourceValue, elOfSources);
			jointCountTargetAndSourceElement[elOfSources.id][targetValue][jointSourceValue]++;
			sourceElementCounts[elOfSources.id][jointSourceValue]++;
		}
		// Increment numObservations we have received
		numObservations++;
		
		// And check whether we need to clear any stored precomputed values:
		if (storedAnyComptuations) {
			clearAllStoredValues();
		}
	}
	
	/**
	 * Compute the joint value of the given set of sources in the given
	 * instantiation of source values.
	 * 
	 * @param sourceValue instantiation of the source values
	 * @param sourceElementId which set of sources to compute the joint value from.
	 * @return the joint value of the selected source set in the current instantiation
	 */
	public int jointValueForElement(int[] sourceValue, int sourceElementId) {
		return jointValueForElement(sourceValue,
				redundancyLattice.lookupSourceElement(sourceElementId));
	}
	
	/**
	 * Compute the joint value of the given set of sources in the given
	 * instantiation of source values.
	 * 
	 * @param sourceValue instantiation of the source values
	 * @param element which set of sources to compute the joint value from.
	 * 			To find the ElementOfSources for a given Set<Integer>, retrieve the id
	 * 			from getRedundancyLattice().lookupSourceElement(Set<Integer>)
	 * @return the joint value of the selected source set in the current instantiation
	 */
	public int jointValueForElement(int[] sourceValue, ElementOfSources element) {
		int jointSourceValue = 0;
		for (int s = 0; s < element.orderedSourceList.length; s++) {
			// For each source in the element:
			int sourceId = element.orderedSourceList[s];
			// Shift the current joint value up to provide enough room for
			//  the value of this source.
			jointSourceValue *= sourcesBases[sourceId];
			// Now add this source value in:
			jointSourceValue += sourceValue[sourceId];
		}
		return jointSourceValue;
	}

	/**
	 * Computes Imin for the given node of elements of sources.
	 * Imin is defined as per equation (3) of Williams and Beer.
	 * 
	 * @param node a set of source elements (where each source
	 *    element is a set of sources).
	 *    This must have been generated from the redundancy lattice in use
	 *    within this PartialInfoDecomposer object.
	 * @return Imin in bits
	 */
	public double Imin(NodeOfElements node) {
		double imin = 0.0;
		for (int t = 0; t < targetBase; t++) {
			// For each target value, add in the contribution from
			//  the minimum specific information from each element in the node, 
			//  weighted by the probability of observing that target value
			imin += ((double) targetCount[t] / (double) numObservations)
					* minOfIs(t, node);
		}
		return imin;
	}
	
	/**
	 * Computes local value of Imin for the given collection of sources,
	 *  with the given target value and source values for this observation.
	 * Imin is defined as per equation (3) of Williams and Beer.
	 * 
	 * @param targetValue the target value at this observation
	 * @param sourceValue the instantiated value of all sources at this observation
	 * @param node a set of source elements (where each source
	 *    element is a set of sources).
	 *    This must have been generated from the redundancy lattice in use
	 *    within this PartialInfoDecomposer object.
	 * @return local value of Imin in bits
	 */
	public double localImin(int targetValue, int[] sourceValue,
			NodeOfElements node) {
		// Find which source element in the collection gave the minimum
		//  specific information for this target value
		ElementOfSources elementGivingMinOfIs =
				whichElementGivesMinOfIs(targetValue, node).element;
		// Then return the local specific information component for the
		//  given source values of this element
		return localSpecificInformation(targetValue, sourceValue, elementGivingMinOfIs);
	}
	
	/**
	 * Compute the PI-function for a given collection of sources,
	 *  following the closed form description in equation (8) of 
	 *  Williams and Beer.
	 *  
	 * @param node a set of source elements (where each source
	 *    element is a set of sources).
	 *    This must have been generated from the redundancy lattice in use
	 *    within this PartialInfoDecomposer object, since its specific
	 *    children are used here.
	 * @return PI in bits
	 */
	public double PI(NodeOfElements node) {
		// 1. Start with Imin(node)
		double pi = Imin(node);
		for (int t = 0; t < targetBase; t++) {
			// For each target value, retrieve or compute the max over each direct
			//  child of minOfIs ...
			double maxOverChildren = 0.0;
			MinimisingElement maxMinElement = null;
			if (storedMaxMinsOfChildrensIs[t][node.id] != null) {
				// Retrieve it from an earlier computation
				maxOverChildren = storedMaxMinsOfChildrensIs[t][node.id].minValue;
			} else {
				// Compute the max over direct children of minOfIs:
				// (This branch will execute if there are no direct children,
				//  since the storedMaxMin value will be null, but this is safe.)
				for (NodeOfElements childNode : node.childNodes) {
					MinimisingElement minElement =
							whichElementGivesMinOfIs(t, childNode);
					if (maxMinElement == null) {
						// Then this first child provides the max so far
						maxOverChildren = minElement.minValue;
						maxMinElement = minElement;
					} else {
						if (minElement.minValue > maxOverChildren) {
							maxOverChildren = minElement.minValue;
							maxMinElement = minElement;
						}
					}
				}
				// And store the maxMinElement for later use, even it was null
				//  (in the case that there are no children)
				storedMaxMinsOfChildrensIs[t][node.id] = maxMinElement;
				storedAnyComptuations = true;
			}
			// And subtract this out, weighted by the probability of this
			//  target value:
			// (this still works even if there were no children, when
			//  there is nothing to subtract)
			pi -= ((double) targetCount[t] / (double) numObservations)
					* maxOverChildren;
		}
		return pi;
	}
	
	/**
	 * Compute the PI-function for a given collection of sources,
	 *  partitioning it into how much information exists at each
	 *  interaction order.
	 *  
	 * @return an array of PIs (in bits) at each interaction order 1..numSources
	 *  (index to the array is interaction order - 1, i.e. for
	 *  order 1, inspect index 0 of the returned array).
	 */
	public double[] PIatEachInteractionOrder() {
		double[] piAtEachInteractionOrder = new double[numSources];
		for (int o = 0; o < numSources; o++) {
			// For order = 1 to numSources
			//      call PIatInteractionOrder(order) to work out how much
			//      info was at this interaction order
			piAtEachInteractionOrder[o] = PIatInteractionOrder(o+1);
		}
		return piAtEachInteractionOrder;
	}

	/**
	 * Compute the PI-function for a given collection of sources,
	 *  returning how much information was at the given order
	 *  
	 * @param order interaction order we are interested in (1..numSources)
	 * @return sum of PIs at this interaction order (in bits)
	 */
	public double PIatInteractionOrder(int order) {
		double piAtThisInteractionOrder = 0.0;
		for (NodeOfElements node :
			redundancyLattice.getNodesForGivenInteractionOrder(order)) {
			// Add the PI for this node to the PI for this interaction order
			double piForThisNode = PI(node);
			piAtThisInteractionOrder += piForThisNode;
			if (debug) {
				System.out.println("PI for node ");
				node.printNode(0, false);
				System.out.println(piForThisNode);
			}
		}
		return piAtThisInteractionOrder;
	}

	/**
	 * Compute the local PI-function for the given value of 
	 *  the target and a given node of source elements,
	 *  following the closed form description in equation (8) of 
	 *  Williams and Beer.
	 *  
	 * @param targetValue the target value at this observation
	 * @param sourceValue the instantiated value of all sources at this observation
	 * @param nodeOfElements a set of source elements (where each source
	 *    element is a set of sources).
	 *    This must have been generated from the redundancy lattice in use
	 *    within this PartialInfoDecomposer object, since its specific
	 *    children are used here.
	 * @return local PI in bits
	 */
	public double localPI(int targetValue, int[] sourceValue,
			NodeOfElements nodeOfElements) {
		// 1. Compute local Imin
		double localPi = localImin(targetValue, sourceValue, nodeOfElements);
		
		// 2. For this target value, retrieve or compute the max over each direct
		//  child of minOfIs, and save the element that gave this
		MinimisingElement maxMinElement = null;
		if (storedMaxMinsOfChildrensIs[targetValue][nodeOfElements.id] != null) {
			maxMinElement = storedMaxMinsOfChildrensIs[targetValue][nodeOfElements.id];
		} else {
			// Compute the max over direct children of minOfIs:
			// (This branch will execute if there are no direct children,
			//  since the storedMaxMin value will be null, but this is safe.)
			double maxOverChildren = 0.0;
			for (NodeOfElements childNode : nodeOfElements.childNodes) {
				MinimisingElement minElement =
						whichElementGivesMinOfIs(targetValue, childNode);
				if (maxMinElement == null) {
					// Then this first child provides the max so far
					maxOverChildren = minElement.minValue;
					maxMinElement = minElement;
				} else {
					if (minElement.minValue > maxOverChildren) {
						maxOverChildren = minElement.minValue;
						maxMinElement = minElement;
					}
				}
			}
			// And store the maxMinElement for later use, even it was null
			//  (in the case that there are no children)
			storedMaxMinsOfChildrensIs[targetValue][nodeOfElements.id] = maxMinElement;
			storedAnyComptuations = true;
		}
		
		if (maxMinElement != null) {
			// 3. Then compute the local specific information for
			//  the maxMin element.
			double localOfMaxOverChildren = 
				localSpecificInformation(targetValue, sourceValue, maxMinElement.element);

			// 4. And subtract this out (not weighted by the probability of this
			//  target value since it's the local value)
			localPi -= localOfMaxOverChildren;
		}
		// Else, this node had no children, so there is nothing to subtract out
			
		return localPi;
	}
	
	/**
	 * Compute the local PI-function for the given value of 
	 *  the target and a given value of source elements,
	 *  at the given interaction order.
	 *  
	 * @param targetValue the target value at this observation
	 * @param sourceValue the instantiated value of all sources at this observation
	 * @param order order we are interested in
	 * @return local PI sum (in bits) at a given interaction order
	 */
	public double localPIAtInteractionOrder(int targetValue, int[] sourceValue,
			int order) {
		double localPiAtThisInteractionOrder = 0.0;
		for (NodeOfElements node :
			redundancyLattice.getNodesForGivenInteractionOrder(order)) {
			// Add the local PI for this node to the local PI
			//  for this interaction order
			localPiAtThisInteractionOrder += localPI(targetValue, sourceValue, node);
		}
		return localPiAtThisInteractionOrder;
	}

	/**
	 * Compute the local PI-function for the given value of 
	 *  the target and a given value of source elements,
	 *  at all interaction orders.
	 *  
	 * @param targetValue the target value at this observation
	 * @param sourceValue the instantiated value of all sources at this observation
	 * @return local PI sum (in bits) at a given interaction order 1..numSources
	 *  (index to the array is interaction order - 1, i.e. for
	 *  order 1, inspect index 0 of the returned array).
	 */
	public double[] localPIAtEachInteractionOrder(int targetValue, int[] sourceValue) {
		double[] localPiAtEachInteractionOrder = new double[numSources];
		for (int o = 0; o < numSources; o++) {
			// call PIatInteractionOrder(order) to work out how much
			//   info was at this interaction order locally
			localPiAtEachInteractionOrder[o] =
				localPIAtInteractionOrder(targetValue, sourceValue, o+1);
		}
		return localPiAtEachInteractionOrder;
	}

	/**
	 * Compute the minimum specific information (see Williams and Beer's
	 *  equation (2)) provided by any element of the sourceCollection
	 *  about the given target outcome targetValue.
	 * 
	 * @param targetValue outcome of the target in question
	 * @param nodeOfElements a set of source elements (where each source
	 *    element is a set of sources).
	 *    This must have been generated from the redundancy lattice in use
	 *    within this PartialInfoDecomposer object, since the id of the nodeOfElements
	 *    is used to identify it.
	 * @return the minimum specific information in bits
	 */
	public double minOfIs(int targetValue, NodeOfElements nodeOfElements) {
		return whichElementGivesMinOfIs(targetValue, nodeOfElements).minValue;
	}
	
	/**
	 * Data structure to represent which element in a node
	 *  had the minimum specific information for a given target value,
	 *  and the value of that minimum specific information.
	 * Used in return values from
	 *  {@link PartialInfoDecomposer#whichElementGivesMinOfIs(int, NodeOfElements)}
	 * 
	 * @author Ben Flecker and Joseph Lizier
	 *
	 */
	public class MinimisingElement {
		/**
		 * Element of sources which minimised the specific information
		 */
		ElementOfSources element;
		/**
		 * The value of specific information for this element (and the
		 *  given target value)
		 */
		double minValue;
		
		public MinimisingElement(ElementOfSources element, double minValue) {
			this.element = element;
			this.minValue = minValue;
		}
	}
	
	/**
	 * Find which source element in the nodeOfElements provides the
	 *  minimum specific information (see Williams and Beer's equation (2))
	 *  about the given target outcome targetValue.
	 * 
	 * @param targetValue outcome of the target in question
	 * @param nodeOfElements a set of source elements (where each source
	 *    element is a set of sources).
	 *    This must have been generated from the redundancy lattice in use
	 *    within this PartialInfoDecomposer object, since the id of the nodeOfElements
	 *    is used to identify it.
	 * @return a structure containing the minimising source element, 
	 *    and what the minimum specific information was
	 */
	public MinimisingElement whichElementGivesMinOfIs(
			int targetValue, NodeOfElements nodeOfElements) {
		
		if (storedMinsOfIs[targetValue][nodeOfElements.id] != null) {
			// Retrieve the cached value from an earlier computation
			return storedMinsOfIs[targetValue][nodeOfElements.id];
		}
		// Else we need to compute the minimising element here:
		double minValue = 0.0;
		ElementOfSources minimisingElement = null;
		boolean examinedFirst = false;
		for (ElementOfSources element : nodeOfElements.elements) {
			// For each source element in this node
			double specificInfo = specificInformation(targetValue, element);
			// Check if the specific info it had about this target value
			//  is the minimum over all elements considered so far
			if (!examinedFirst) {
				minValue = specificInfo;
				minimisingElement = element;
				examinedFirst = true;
			} else {
				if (specificInfo < minValue) {
					minValue = specificInfo;
					minimisingElement = element;
				}
			}
		}
		// Create the MinimisingElement data structure and cache it for later:
		storedMinsOfIs[targetValue][nodeOfElements.id] = 
			new MinimisingElement(minimisingElement, minValue);
		storedAnyComptuations = true;
		return storedMinsOfIs[targetValue][nodeOfElements.id];
	}

	/**
	 * Compute the specific information (see Williams and Beer's
	 *  equation (2)) provided by the given source element (i.e. set of sources)
	 *  about the given target outcome targetValue.
	 * 
	 * @param targetValue outcome of the target in question
	 * @param sourceElement a set of sources
	 * @return specific information in bits.
	 */
	public double specificInformation(int targetValue, ElementOfSources sourceElement) {
		
		if (!Double.isNaN(storedSpecificInfos[targetValue][sourceElement.id])) {
			// We have a cached value for the specific info here
			return storedSpecificInfos[targetValue][sourceElement.id];
		}
		// Else we need to compute it here:
		
		// As per Williams and Beer equation 2:
		int maxJointSourceValue =
			sourceElementCounts[sourceElement.id].length;
		double specificInfo = 0.0;
		for (int jointSourceValue = 0;
			jointSourceValue < maxJointSourceValue; jointSourceValue++) {
			//  Loop over each joint value of the sourceElement, 
			if (jointCountTargetAndSourceElement[sourceElement.id][targetValue][jointSourceValue]
			        == 0) {
				// All probabilities here will be zero, so skip this source value
				continue;
			}
			double probSourceGivenTarget =
				(double) jointCountTargetAndSourceElement[sourceElement.id][targetValue][jointSourceValue] /
				(double) targetCount[targetValue];
			// Formally one would now calculate:
			//double probTargetGivenSourceDivProbTarget = 
			//	(double) (numObservations *
			//	jointCountTargetAndSourceElement[sourceElement.id][targetValue][jointSourceValue] ) /
			//	((double) targetCount[targetValue] * (double) sourceElementCounts[sourceElement.id][jointSourceValue]);
			// However a more efficient method is to use the calculation we've already made here:
			double probTargetGivenSourceDivProbTarget = 
				probSourceGivenTarget * (double) (numObservations) /
					(double) sourceElementCounts[sourceElement.id][jointSourceValue];
			double logETerm = Math.log(probTargetGivenSourceDivProbTarget);
			// Add this contribution into our running total for the partial
			//  info. Note it is in base e at this point.
			specificInfo += probSourceGivenTarget * logETerm;
		}
		specificInfo /= LOG_2;
		// Store the computed value for later use
		storedSpecificInfos[targetValue][sourceElement.id] = specificInfo;
		storedAnyComptuations = true;		
		return specificInfo;
	}
	
	/**
	 * Compute the local specific information (see Williams and Beer's
	 *  equation (2)) provided by the current values of the given source
	 *  element (i.e. set of sources)
	 *  about the given target outcome targetValue.
	 * 
	 * @param targetValue outcome of the target in question
	 * @param sourceValue the instantiated value of all sources at this observation
	 * @param sourceElement a set of sources
	 * @return specific information
	 */
	public double localSpecificInformation(int targetValue, int[] sourceValues,
			ElementOfSources sourceElement) {
		
		int jointSourceValue = jointValueForElement(sourceValues, sourceElement);
		
		if (!Double.isNaN(storedLocalSpecificInfos[targetValue][sourceElement.id][jointSourceValue])) {
			// We have a precomputed cached value here:
			return storedLocalSpecificInfos[targetValue][sourceElement.id][jointSourceValue];
		}
		// Else we need to compute the local specific information value
			
		// Compute as per Williams and Beer equation 2:
		
		// Then utilising targetCount and targetCountGivenSourceElement.
		//   (converting the sourceElement to it's id with
		//   RedundancyLattice#sourceElementToId)
		//   compute the inner part of the specific information

		if (jointCountTargetAndSourceElement[sourceElement.id][targetValue][jointSourceValue]
		        == 0) {
			// All probabilities here will be zero, so by convention
			//  the local value will be zero. We could think about
			//  throwing an exception here since we've been asked to compute 
			//  on a situation that we have not observed.
			return 0;
		}
		// Could compute probTargetGivenSourceDivProbTarget directly,
		//  but we'll do it the same way as in specificInformation():
		double probSourceGivenTarget =
			(double) jointCountTargetAndSourceElement[sourceElement.id][targetValue][jointSourceValue] /
			(double) targetCount[targetValue];
		double probTargetGivenSourceDivProbTarget = 
			probSourceGivenTarget * (double) (numObservations) /
				(double) sourceElementCounts[sourceElement.id][jointSourceValue];
		double localPartialInfo = Math.log(probTargetGivenSourceDivProbTarget) /
						LOG_2;

		// Store the computed value for later use:
		storedLocalSpecificInfos[targetValue][sourceElement.id][jointSourceValue] =
			localPartialInfo;
		storedAnyComptuations = true;		
		return localPartialInfo;
	}
	
	/**
	 * Allow the end user access to the redundancy lattice, in particular
	 * to allow them access to {@link RedundancyLattice#getNodeOfElements(Set)}.
	 * This allows translation of a specification of a node into the object
	 * we use to represent this node, which can then be called in 
	 * the method here, e.g. {@link #Imin(NodeOfElements)} or
	 * {@link #specificInformation(int, ElementOfSources)}
	 * 
	 * @return the reundancy lattice in use here
	 */
	public RedundancyLattice getRedundancyLattice() {
		return redundancyLattice;
	}

	/**
	 * Whether debug prints will be made
	 * 
	 * @return debug status
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Set whether debug prints are to be made
	 * 
	 * @param debug whether debug prints are to be made
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
