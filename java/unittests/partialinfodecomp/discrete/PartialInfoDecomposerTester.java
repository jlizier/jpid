package partialinfodecomp.discrete;

import junit.framework.TestCase;

/**
 * Test cases for the PartialInfoDecomposer class.
 * 
 * TODO Add plenty more.
 * 
 * @author Joseph Lizier joseph.lizier at gmail.com
 *
 */
public class PartialInfoDecomposerTester extends TestCase {

	/**
	 * Test the PID elements of a two source OR function
	 *  (with equal probability inputs).
	 * Results are computed from the spreadsheet distribution with
	 *  this code.
	 */
	public void testTwoSourceOr() throws Exception {
		
		simpleTwoVariableFunctionTest(
				new int[]    {0, 1, 1, 1},
				new double[] {0.311278, 0.311278, 0.311278, 0.811278},
				new double[] {0.311278, 0.000000, 0.000000, 0.500000},
				0.0000005);
	}

	/**
	 * Test the PID elements of a two source AND function.
	 *  (with equal probability inputs).
	 * Results are computed from the spreadsheet distribution with
	 *  this code.
	 */
	public void testTwoSourceAnd() throws Exception {
		
		simpleTwoVariableFunctionTest(
				new int[]    {0, 0, 0, 1},
				new double[] {0.311278, 0.311278, 0.311278, 0.811278},
				new double[] {0.311278, 0.000000, 0.000000, 0.500000},
				0.0000005);
	}

	/**
	 * Test the PID elements of a two source XOR function.
	 *  (with equal probability inputs).
	 * Results are computed from the spreadsheet distribution with
	 *  this code.
	 */
	public void testTwoSourceXor() throws Exception {
		
		simpleTwoVariableFunctionTest(
				new int[]    {0, 1, 1, 0},
				new double[] {0, 0, 0, 1},
				new double[] {0, 0, 0, 1},
				0.0000005);
	}

	/**
	 * Test the PID elements of a two source single copy function.
	 *  (with equal probability inputs).
	 * Results are computed from the spreadsheet distribution with
	 *  this code.
	 */
	public void testTwoSourceCopyX1() throws Exception {
		
		simpleTwoVariableFunctionTest(
				new int[]    {0, 0, 1, 1},
				new double[] {0, 1, 0, 1},
				new double[] {0, 1, 0, 0},
				0.0000005);
	}

	/**
	 * Utility function to test for expected Imin and PI results
	 *  for a function of two binary variables, with equal probabilities
	 *  for each input.
	 * 
	 * @param functionOutputs outputs of the binary function for the inputs
	 *   (0,0), (0,1), (1,0), (1,1).
	 * @param expectedImins expected Imin values, for nodes (in order) 
	 *   {0}{1}, {0}, {1}, {0,1}
	 * @param expectedPIs expected PI values, for nodes in the order listed above
	 * @param precision precision of the expected result
	 * @throws Exception
	 */
	public void simpleTwoVariableFunctionTest(
			int[] functionOutputs,
			double[] expectedImins,
			double[] expectedPIs, double precision) throws Exception {
		PartialInfoDecomposer pid = new PartialInfoDecomposer(2, 2);
		
		// Set up the basic observations for equal distributions:
		pid.addObservation(functionOutputs[0], new int[] {0, 0});
		pid.addObservation(functionOutputs[1], new int[] {0, 1});
		pid.addObservation(functionOutputs[2], new int[] {1, 0});
		pid.addObservation(functionOutputs[3], new int[] {1, 1});

		// Check the Imin results:
		RedundancyLattice rl = pid.getRedundancyLattice();
		assertEquals(expectedImins[0],
			pid.Imin(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0}{1}"))),
			precision);
		assertEquals(expectedImins[1],
			pid.Imin(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0}"))),
			precision);
		assertEquals(expectedImins[2],
			pid.Imin(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{1}"))),
			precision);
		assertEquals(expectedImins[3],
			pid.Imin(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1}"))),
			precision);

		// Check the PI results:
		assertEquals(expectedPIs[0],
			pid.PI(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0}{1}"))),
			precision);
		assertEquals(expectedPIs[1],
			pid.PI(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0}"))),
			precision);
		assertEquals(expectedPIs[2],
			pid.PI(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{1}"))),
			precision);
		assertEquals(expectedPIs[3],
			pid.PI(rl.getNodeOfElements(
				RedundancyLattice.nodeSpecFromString("{0,1}"))),
			precision);
	}
}
