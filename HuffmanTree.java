abstract class HuffmanTree implements Comparable<HuffmanTree> 
{
    public final int frequency;
    public HuffmanTree ()
    {
    	frequency = 0;
    }
    public HuffmanTree(int freq) 
    { 
    	frequency = freq; 
    }
 
    public int compareTo(HuffmanTree tree) 
    {
        return frequency - tree.frequency;
    }
}