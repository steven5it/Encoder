import java.io.*;
import java.util.*;

public class Encoder 
{
	final static int NUMBER_OF_CHARS = 26;
	private static int numberCharsToGenerate;
	private static String frequenciesFileName;



	public static void main(String[] args) throws IOException 
	{
		
		validateArgs(args);
		frequenciesFileName = args[0];
		numberCharsToGenerate = Integer.parseInt(args[1]);
		
		int[] charFrequencies1 = new int[NUMBER_OF_CHARS];
		int[] charFrequencies2 = new int[NUMBER_OF_CHARS * NUMBER_OF_CHARS];
		String[] oneCharCombinations = new String[NUMBER_OF_CHARS];
		String[] twoCharCombinations = new String[NUMBER_OF_CHARS * NUMBER_OF_CHARS];
		fillCharArrays(oneCharCombinations, twoCharCombinations);
		
		Map<String, Integer> oneCharFrequenciesMap = new HashMap<String, Integer>();
		Map<String, Integer> twoCharFrequenciesMap = new HashMap<String, Integer>();

		
		Map<String, String> oneCharEncodingsMap = new HashMap<String, String>();
		Map<String, String> twoCharEncodingsMap = new HashMap<String, String>();

		BufferedWriter bw = new BufferedWriter(new FileWriter("testText")); 
		BufferedWriter bwEncoded1 = new BufferedWriter(new FileWriter("testText.enc1"));
		BufferedWriter bwDecoded1 = new BufferedWriter(new FileWriter("testText.dec1"));
		BufferedWriter bwEncoded2 = new BufferedWriter(new FileWriter("testText.enc2"));
		BufferedWriter bwDecoded2 = new BufferedWriter(new FileWriter("testText.dec2"));

		// read in the file of frequencies and populate the frequency array
		try 
		{
			int oneCharSum = 0;
			int twoCharSum = 0;
			double entropySum = 0;
			fillFrequencies(charFrequencies1);
			fillTwoCharFrequencies(charFrequencies1, charFrequencies2);
		    oneCharSum = sumOfArray(charFrequencies1);
		    twoCharSum = sumOfArray(charFrequencies2);
			entropySum = entropySumOfArray(charFrequencies1, oneCharSum);
			
			HuffmanTree oneCharTree = buildTree(charFrequencies1, oneCharFrequenciesMap, 1);
			HuffmanTree twoCharTree = buildTree(charFrequencies2, twoCharFrequenciesMap, 2);

			System.out.println("SYMBOL\tWEIGHT\tHUFFMAN CODE");
			calculateAndPrintEncodings(oneCharTree, new StringBuilder(), oneCharEncodingsMap);
			generateVolume(charFrequencies1, oneCharSum, bw, numberCharsToGenerate, 1);
			
			int[] count1 = {0};
			encodeVolume(oneCharTree, bwEncoded1, oneCharEncodingsMap, 1);
			decodeVolume(oneCharTree, bwDecoded1, oneCharEncodingsMap, 1, count1);

			System.out.println("SYMBOL\tWEIGHT\tHUFFMAN CODE");
			calculateAndPrintEncodings(twoCharTree, new StringBuilder(), twoCharEncodingsMap);
			
			int[] count2 = {0};
			encodeVolume(twoCharTree, bwEncoded2, twoCharEncodingsMap, 2);
			decodeVolume(twoCharTree, bwDecoded2, twoCharEncodingsMap, 2, count2);
			
			System.out.println("\nEntropy of the language is: " + entropySum);
			double average1 = count1[0] / Double.parseDouble(args[1]);
			double average2 = count2[0] / Double.parseDouble(args[1]);
			System.out.println("1 symbol encoding file length is: " + count1[0]
					+ ", average bits per symbol: " + average1);
			System.out.println("Percentage increase from entropy = %"
					+ (100 * (average1 - entropySum) / (entropySum)));
			System.out.println("\n2 symbol encoding file length is: " + count2[0]
					+ ", average bits per symbol: " + average2);
			System.out.println("Percentage increase from entropy = %"
					+ (100 * (average2 - entropySum) / (entropySum)));

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}

	public static void validateArgs(String[] args)
	{
		if (args.length != 2) 
		{
			System.err.println("Improper number of arguments. Usage: java Encoder frequenciesFile k\n"
					+ "frequenciesFile: text file with integer weights of characters\n"
					+ "k: number of characters to generate in random test text file");
			System.exit(0);

		}
		if (!isInteger(args[1])) 
		{
			System.err.println("Second argument must be integer value.");
			System.exit(0);
		}
	}
	
	public static boolean isInteger(String str) 
	{
		try 
		{
			Integer.parseInt(str);
			return true;
		} 
		catch (NumberFormatException nfe) 
		{
			return false;
		}
	}
	
	private static void fillCharArrays(String[] chars1, String[] chars2) 
	{
		for (int i = 0; i < chars1.length; i++) {
			chars1[i] = Character.toString((char) ('A' + i));
		}
		for (int i = 0; i < chars2.length; i++) {
			chars2[i] = "";
		}
		for (int i = 0; i < chars2.length; i++) 
		{
			String s2 = Character.toString((char) ('A' + (i / 26)))
					+ Character.toString((char) ('A' + (i % 26)));
			chars2[i] = s2;
		}
	}
	
	private static void fillFrequencies(int[] charFrequencies1) throws IOException 
	{
		BufferedReader br = new BufferedReader(new FileReader(frequenciesFileName));
		System.out.println("Reading from file: " + frequenciesFileName);

		int i = 0;
		int sum = 0;
		String line;
		while ((line = br.readLine()) != null) 
		{
			if (line.equals("") || !isInteger(line)) 
			{
				charFrequencies1[i++] = 0;
				continue;
			}
			charFrequencies1[i++] = Integer.parseInt(line);
		}
		br.close();
	}

	private static void fillTwoCharFrequencies(int[] charFrequencies1, int[] charFrequencies2) 
	{
		for (int j = 0; j < charFrequencies1.length; j++) 
		{
			for (int k = 0; k < charFrequencies1.length; k++) 
			{
				charFrequencies2[(j * 26) + k] = charFrequencies1[j] * charFrequencies1[k];
			}
		}
	}
	
	private static int sumOfArray(int[] array) 
	{
		int sum = 0;
		for (int i = 0; i < array.length; i++)
		{
			sum += array[i];
		}
		return sum;
	}
	
	private static double entropySumOfArray(int[] charFrequencies1, int oneCharSum) 
	{
		// Entropy = log x(base n) = log x(base e)/log n(base e)
		double entropySum = 0;
		for (int e = 0; e < charFrequencies1.length; e++) 
		{
			if (charFrequencies1[e] == 0)
				continue;
			entropySum -= ((double) charFrequencies1[e] / (double) oneCharSum)
					* (log2((double) charFrequencies1[e] / (double) oneCharSum));
		}
		return entropySum;
	}

	// input is an array of frequencies, indexed by character code
	public static HuffmanTree buildTree(int[] charFrequencies,
			Map<String, Integer> charFrequenciesMap, int j) 
	{
		PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
		if (j == 1) 
		{
			for (int i = 0; i < charFrequencies.length; i++) 
			{
				if (charFrequencies[i] > 0) 
				{
					String string = Character.toString((char) ('A' + i));
					trees.offer(new HuffmanLeaf(charFrequencies[i], string));
					charFrequenciesMap.put(string, i);
				}
			}
		}

		else if (j == 2)
		{
			for (int i = 0; i < charFrequencies.length; i++) 
			{
				if (charFrequencies[i] > 0) 
				{
					String string = Character.toString((char) ('A' + (i / 26)))
							+ Character.toString((char) ('A' + (i % 26)));
					trees.offer(new HuffmanLeaf(charFrequencies[i], string));
					charFrequenciesMap.put(string, i);
				}
			}
		}

		assert trees.size() > 0;
		while (trees.size() > 1) 
		{
			HuffmanTree a = trees.poll();
			HuffmanTree b = trees.poll();

			trees.offer(new HuffmanNode(a, b));
		}
		return trees.poll();
	}

	public static void calculateAndPrintEncodings(HuffmanTree tree, StringBuilder prefix,
			Map<String, String> charEncodingsMap) 
	{
		assert tree != null;
		if (tree instanceof HuffmanLeaf) 
		{
			HuffmanLeaf leaf = (HuffmanLeaf) tree;
			System.out.println(leaf.value + "\t" + leaf.frequency + "\t" + prefix);
			charEncodingsMap.put(leaf.value, prefix.toString());
		} 
		else if (tree instanceof HuffmanNode) 
		{
			HuffmanNode node = (HuffmanNode) tree;

			prefix.append('0');
			calculateAndPrintEncodings(node.left, prefix, charEncodingsMap);
			prefix.deleteCharAt(prefix.length() - 1);

			prefix.append('1');
			calculateAndPrintEncodings(node.right, prefix, charEncodingsMap);
			prefix.deleteCharAt(prefix.length() - 1);
		}
	}

	private static void generateVolume(int[] charFrequencies, int sum,
			BufferedWriter bw, int k, int numberSymbols) throws IOException 
	{
		System.out.println("\nGenerating random volume of text of " + k + " chars based on probabilities from part 1...\n");
		Random rand = new Random();

		for (int i = 0; i < k; i++) 
		{
			int randomInt = rand.nextInt(sum);
			int indexOfChar = getIndexOfChar(charFrequencies, sum, randomInt);
			bw.write((char) ('A' + indexOfChar));
		}
		bw.close();
	}

	private static int getIndexOfChar(int[] charFrequencies, int sum, int randomInt) 
	{
		int indexOfChar = 0;
		int sumcount = 0;
		while (sumcount <= randomInt) 
		{
			sumcount += charFrequencies[indexOfChar++];
		}
		indexOfChar--;
		return indexOfChar;
	}

	public static void encodeVolume(HuffmanTree tree, BufferedWriter bwEncoded,
			Map<String, String> encodingsMap, int j) throws IOException 
	{
		assert tree != null;
		BufferedReader brEncoded = new BufferedReader(new InputStreamReader(new FileInputStream("testText")));
		int c;
		int count = 1;
		String s = "";
		while ((c = brEncoded.read()) != -1) 
		{
			char character = (char) c;
			s += character;

			if (count++ < j) 
				continue;
			else 
			{
				bwEncoded.write(encodingsMap.get(s));
				count = 1;
				s = "";
			}
		}
		bwEncoded.close();
		brEncoded.close();
	}

	public static void decodeVolume(HuffmanTree tree, BufferedWriter bwDecoded,
			Map<String, String> encodingsMap, int iteration, int binaryCount[])
			throws IOException 
	{
		assert tree != null;
		BufferedReader brDecoded = new BufferedReader(new InputStreamReader(
				new FileInputStream("testText.enc" + iteration)));
		int c;
		StringBuilder encoding = new StringBuilder();
		while ((c = brDecoded.read()) != -1) 
		{
			char character = (char) c;
			encoding.append(character);
			for (Map.Entry<String, String> entry : encodingsMap.entrySet()) 
			{
				if (entry.getValue().equals(encoding.toString())) 
				{
					bwDecoded.write(entry.getKey());
					encoding.setLength(0);
					break;
				}
			}
			binaryCount[0]++;
		}
		bwDecoded.close();
		brDecoded.close();
	}

	static double log2(double x) 
	{
		return Math.log(x) / Math.log(2.0d);
	}
}
