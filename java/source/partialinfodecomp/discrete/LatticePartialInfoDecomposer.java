package partialinfodecomp.discrete;

import java.util.Arrays;

/**
 * <p>
 * Implements the Partial Information Decomposition on a lattice system,
 * with memory as a source, all elements having same base and
 * offsets of sources, and all elements considered to be homogeneous such
 * that observations are pooled between them.
 * The k-history of the destination element is considered to be source 0,
 * all other sources lie in the order supplied by sourceOffsets, and are
 * indexed from source 1 upwards.
 * </p>
 * 
 * <p>
 * See: "Nonnegative Decomposition of Multivariate Information",
 * Paul L. Williams and Randall D. Beer, 2010
 * <a href="http://arxiv.org/abs/1004.2515">arXiv:1004.2515</a>
 * <p>
 * 
 * <p>
 * Usage:
 * <ol>
 * 		<li>Construct: {@link #LatticePartialInfoDecomposer(int, int, int[], int)}</li>
 * 		<li>Provide observations (can be accumulated):
 * 			<ol>
 * 				<li>{@link #addObservations(int[][])}</li>
 * 			</ol>
 * 	    </li>
 * 		<li>Compute required entities:
 * 			<ol>
 * 				<li>{@link super#Imin(NodeOfElements)}</li>
 * 				<li>{@link super#PI(NodeOfElements)}</li>
 * 				<li>{@link #localPI(int[][], NodeOfElements)}</li>
 * 				<li>{@link #localPIAtInteractionOrder(int[][], int)}</li>
 * 				<li>{@link #localPIAtEachInteractionOrder(int[][])}</li>
 * 			</ol>
 * 		</li>
 * 		<li>Re-initialise (then start adding observations again):
 * 	{@link super#initialise()}</li>
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
 * Ben enter email and web site here if you want?<br/>
 * <a href="mailto:joseph.lizier at gmail.com">joseph.lizier at gmail.com</a><br/>
 * <a href="http://lizier.me/joseph/">http://lizier.me/joseph/</a>
 *
 */
public class LatticePartialInfoDecomposer extends PartialInfoDecomposer {

	/**
	 * Track what the generic offsets are of the destination *from* each source.
	 *  (I.e. in ECAs this will be an array including -1 and 1).
	 * The past state of the destination is assumed to be a source
	 *  (offset 0) and is not included in this array.
	 */
	protected int[] destOffsetsFromSources;
	/**
	 * Number of sources to the destination, not including the past value of
	 *  the destination itself.
	 */
	protected int numNonMemorySources;
	/**
	 * History length of the destination to consider as one source
	 */
	protected int k;
	/**
	 * Base for all variables
	 */
	protected int base;
	/**
	 * maxShiftedValue[variableState] holds the maximum value that
	 *  variableState is shifted to while being stored in the joint value
	 *  of the past of the destination, before falling off the end of the
	 *  past k states. We store these values to save recomputing them
	 *  every time they're needed.
	 */
	protected int[] maxShiftedValue;
	
	/**
	 * Returns a new PartialInfoDecomposer, configured for the given
	 *  base, and (sourcesBases.length + 1) sources, with each source i
	 *  with the given base.
	 * 
	 * @param base base of all variables
	 * @param destOffsetsFromSources array of offsets of the destination
	 *  *from* each causal information source.
	 *  (i.e. an offset of 1 means the destination is one index larger,
	 *  or one to the right, than the source). 
	 *  destOffsetsFromSources must not include 0 (the destination itself),
	 *  as this has been assumed to be a source and will be dealt with
	 *  separately.
	 * @param k history length to include as the memory source (must be >= 1)
	 * @throws Exception if k < 1 (will be thrown from
	 * 	{@link #createSourceBases(int, int, int)})
	 */
	public LatticePartialInfoDecomposer(int base, int numNonMemorySources,
			int[] destOffsetsFromSources, int k) throws Exception {
		// 1. Construct super class
		super(base, createSourceBases(base, destOffsetsFromSources.length, k));
		// 2. Store sourceOffsets and k
		this.destOffsetsFromSources = Arrays.copyOf(destOffsetsFromSources, destOffsetsFromSources.length);
		this.numNonMemorySources = destOffsetsFromSources.length;
		this.k = k;
		this.base = base;
		// And finally, create constants for tracking the k 
		//  joint states of the past of the destination:
		maxShiftedValue = new int[base];
		int baseToKMinus1 = power(base, k-1);
		for (int v = 0; v < base; v++) {
			maxShiftedValue[v] = v * baseToKMinus1;
		}
	}

	/**
	 * Creates an array of the source bases - all set to base except for the
	 *  first source which is the memory of length k (i.e. base of base^k)
	 *
	 * @param base base of all variables (i.e. variables can take values 0..base-1)
	 * @param numNonMemorySources number of sources in addition to the previous
	 * 		state of the destination
	 * @return array of source bases
	 * @throws Exception If k < 1
	 */
	protected static int[] createSourceBases(int base, int numNonMemorySources, int k) throws Exception {
		if (k < 1) {
			throw new Exception("history length k must be 1 or greater");
		}
		int[] bases = new int[numNonMemorySources + 1];
		// Initialise all to base
		Arrays.fill(bases, base);
		// Then make bases[0] equal to base raised to the power k
		bases[0] = power(base, k);
		return bases;
	}
	
	/**
	 * Add observations from the given lattice values. 
	 * 
	 * @param latticeValues observations of the lattice (first index is time,
	 *  second is spatial index. Assumed to be periodic boundary conditions)
	 */
	public void addObservations(int[][] latticeValues) {
		int timeSteps = latticeValues.length;
		int numVariables = latticeValues[0].length;
		
		// Initialise and store the current previous value for each column
		int[] pastKVal = new int[numVariables];  // initialised to all 0's
		for (int n = 0; n < k; n++) {
			for (int c = 0; c < numVariables; c++) {
				pastKVal[c] *= base;
				pastKVal[c] += latticeValues[n][c];
			}
		}
		// Now add all observations
		int[] sourceValues = new int[numNonMemorySources + 1];
		for (int n = k; n < timeSteps; n++) {
			// For each time step:
			for (int c = 0; c < numVariables; c++) {
				// For each variable in the lattice:
				// Assign memory source first
				sourceValues[0] = pastKVal[c];
				// Now insert the other source values
				for (int s = 0; s < numNonMemorySources; s++) {
					sourceValues[s+1] =
						latticeValues[n-1]
						    [(c-destOffsetsFromSources[s]+numVariables) % numVariables];
				}
				
				// KEY OPERATION:
				// Then add the observation in:
				addObservation(latticeValues[n][c], sourceValues);

				// And finally, update joint memory values for this variable:
				//  subtract out the oldest value ...
				pastKVal[c] -= maxShiftedValue[latticeValues[n-k][c]];
				//  shift the remaining joint values up ...
				pastKVal[c] *= base;
				//  and add in the most recent past value
				pastKVal[c] += latticeValues[n][c];
			}
		}
	}
	
	/**
	 * Compute the local PI values for the given node, at every space-time
	 *  point in the lattice
	 * 
	 * @param latticeValues values for which to compute the local PI
	 * @param nodeOfElements which node in the redundancy lattice to compute for
	 * @return local PI values 
	 */
	public double[][] localPI(int[][] latticeValues, NodeOfElements nodeOfElements) {
		int timeSteps = latticeValues.length;
		int numVariables = latticeValues[0].length;
		double[][] localPiValues = new double[timeSteps][numVariables];
		
		// Initialise and store the current previous value for each column
		int[] pastKVal = new int[numVariables];  // initialised to all 0's
		for (int n = 0; n < k; n++) {
			for (int c = 0; c < numVariables; c++) {
				pastKVal[c] *= base;
				pastKVal[c] += latticeValues[n][c];
			}
		}
		// Now add all observations
		int[] sourceValues = new int[numNonMemorySources + 1];
		for (int n = k; n < timeSteps; n++) {
			// For each time step:
			for (int c = 0; c < numVariables; c++) {
				// For each variable in the lattice:
				// Assign memory source first
				sourceValues[0] = pastKVal[c];
				// Now insert the other source values
				for (int s = 0; s < numNonMemorySources; s++) {
					sourceValues[s+1] =
						latticeValues[n-1]
						    [(c-destOffsetsFromSources[s]+numVariables) % numVariables];
				}
				
				// KEY OPERATION:
				// Then compute the local PI from these values:
				localPiValues[n][c] = localPI(latticeValues[n][c], sourceValues, nodeOfElements);

				// And finally, update joint memory values for this variable:
				//  subtract out the oldest value ...
				pastKVal[c] -= maxShiftedValue[latticeValues[n-k][c]];
				//  shift the remaining joint values up ...
				pastKVal[c] *= base;
				//  and add in the most recent past value
				pastKVal[c] += latticeValues[n][c];
			}
		}
		return localPiValues;
	}
	
	/**
	 * Compute the local PI values at the given interaction order,
	 * at every space-time point in the lattice.
	 * This uses the PDFs built up from the earlier {@link #addObservations(int[][])}
	 * calls.
	 * 
	 * @param latticeValues values for which to compute the local PI
	 * @param order order we are interested in
	 * @return local PI values at the given interaction order.
	 */
	public double[][] localPIAtInteractionOrder(int[][] latticeValues, int order) {
		int timeSteps = latticeValues.length;
		int numVariables = latticeValues[0].length;
		double[][] localPiAtOrder = new double[timeSteps][numVariables];
		
		// Initialise and store the current previous value for each column
		int[] pastKVal = new int[numVariables];  // initialised to all 0's
		for (int n = 0; n < k; n++) {
			for (int c = 0; c < numVariables; c++) {
				pastKVal[c] *= base;
				pastKVal[c] += latticeValues[n][c];
			}
		}
		// Now compute local value at each observation
		int[] sourceValues = new int[numNonMemorySources + 1];
		for (int n = k; n < timeSteps; n++) {
			// For each time step:
			for (int c = 0; c < numVariables; c++) {
				// For each variable in the lattice:
				// Assign memory source first
				sourceValues[0] = pastKVal[c];
				// Now insert the other source values
				for (int s = 0; s < numNonMemorySources; s++) {
					sourceValues[s+1] =
						latticeValues[n-1]
						    [(c-destOffsetsFromSources[s]+numVariables) % numVariables];
				}
				
				// KEY OPERATION:
				// Then compute the local PI from these values:
				localPiAtOrder[n][c] = 
					localPIAtInteractionOrder(latticeValues[n][c],
							sourceValues, order);

				// And finally, update joint memory values for this variable:
				//  subtract out the oldest value ...
				pastKVal[c] -= maxShiftedValue[latticeValues[n-k][c]];
				//  shift the remaining joint values up ...
				pastKVal[c] *= base;
				//  and add in the most recent past value
				pastKVal[c] += latticeValues[n][c];
			}
		}
		return localPiAtOrder;
	}

	/**
	 * Compute the local PI values at each interaction order,
	 * at every space-time point in the lattice.
	 * 
	 * @param latticeValues values for which to compute the local PI
	 * @param order order we are interested in
	 * @return an array of local PI values at each interaction order
	 *  (first index is interaction order (where index value is one
	 *    less than the order), second is time, third is space)
	 */
	public double[][][] localPIAtEachInteractionOrder(int[][] latticeValues) {
		double[][][] localPisAtEachOrder = new double[numSources][][];
		for (int o = 0; o < numSources; o++) {
			localPisAtEachOrder[o] = localPIAtInteractionOrder(latticeValues, o+1);
		}
		return localPisAtEachOrder;
	}

	/**
	 * <p>Utility method to return the active information storage.
	 *  This is not the most efficient way to compute it,
	 *  but is provided for those using this toolkit (since we already
	 *  have the relevant PDFs computed).</p>
	 *  
	 * <p>See: "Local measures of information storage in complex distributed computation",
	 * Joseph T. Lizier, Mikhail Prokopenko, Albert Y. Zomaya,
	 * accepted by Information Sciences, 2012.</p>
	 * 
	 * @return active information storage
	 * @throws Exception for an internal error in the toolkit
	 */
	public double averageActiveInfoStorage() throws Exception {
		NodeOfElements memNode;
		try {
			// Pull out the node corresponding to the memory:
			memNode = redundancyLattice.getNodeOfElements(
					RedundancyLattice.nodeSpecFromString("{0}"));
		} catch (Exception e) {
			// This should not happen but we throw it to detect any internal errors
			throw new RuntimeException("Internal error with redundancy lattice", e);
		}
		// Now make the computation
		double active = Imin(memNode);
		if (debug) {
			System.out.println("Active = " + active);
		}
		return active;
	}
	
	/**
	 * <p>Utility method to return the transfer entropy from one of the named
	 *  sources. This is not the most efficient way to compute the TE
	 *  but is provided for those using this toolkit (since we already
	 *  have the relevant PDFs computed).</p>
	 * 
	 * <p>See: "Measuring Information Transfer",
	 * Thomas Schreiber,
	 * Physical Review Letters, 85 (2), 461-464.</p>
	 * 
	 * @param sourceIndex which source to consider, numbered 0 .. numSources - 1
	 *   in the order that their offsets were passed to the constructor
	 *   {@link #LatticePartialInfoDecomposer(int, int, int[], int)}.
	 * @return returns the transfer entropy
	 * @throws Exception when the sourceIndex is not valid here
	 */
	public double averageApparentTransferEntropy(int sourceIndex) throws Exception {
		if ((sourceIndex >= destOffsetsFromSources.length) ||
			(sourceIndex < 0)) {
			throw new RuntimeException("sourceIndex is out of range");
		}
		NodeOfElements memNode;
		NodeOfElements sourceAndMemNode;
		try {
			// Pull out the node corresponding to the memory:
			memNode = redundancyLattice.getNodeOfElements(
					RedundancyLattice.nodeSpecFromString("{0}"));
			// Pull out the node corresponding to the memory and source together:
			sourceAndMemNode = redundancyLattice.getNodeOfElements(
					RedundancyLattice.nodeSpecFromString("{0," + (sourceIndex+1)
							+ "}"));
		} catch (Exception e) {
			// This should not happen since we've checked the source range 
			//  already, but we throw it to detect any internal errors
			throw new RuntimeException("Internal error with redundancy lattice", e);
		}
		// Now make the computation: it is MI of source and memory minus
		//  MI of just the memory. (Since we use node of single elements,
		//  Imin == MI.)
		double jointMI = Imin(sourceAndMemNode);
		double memMI = Imin(memNode);
		if (debug) {
			System.out.println("TE = jointMI(" + jointMI + ") - memMI(" +
					memMI + ") = " + (jointMI - memMI));
		}
		return jointMI - memMI;
	}
	
	/**
	 * Returns the integer result of base^power
	 * 
	 * @param base base integer of the operation
	 * @param power power that base is raised to
	 * @return base raised to exponent power (rounded by integer operations)
	 */
	protected static int power(int base, int power) {
		int result = 1;
		int absPower = Math.abs(power);
		for (int p = 0; p < absPower; p++) {
			result *= base;
		}
		if (power < 0) {
			// This will be zero for any base except 1 or -1
			result = 1 / result;
		}
		return result;
	}
}
