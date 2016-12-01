import java.util.*;

class ProcessState  {
	
	private Integer [] S;
	private Integer [] N;
	private int length;

	public ProcessState (int size, int name){
		int l = size*size;
		this.length = size;
		S = new Integer [l]; 
		for(int i=0; i<S.length; i++){
			S[i] = EnumState.O.ordinal();
		}

		if (name == 1){
			S[0] = EnumState.H.ordinal();
		}
/*
		int i = l;
		while (i > size){
			for (i; i>size; i--)
			{
				S[i] = EnumState.R.ordinal();
			}
		}
*/
	}

	// Print Array.
	public void Print () {
		System.out.println("ProcessState print:");
			for (int i=0; i<S.length; i++){
				String print;
				int c = S[i];
				switch (c){
					case 0: print = "R";
							break;
					case 1: print = "E";
							break;
					case 2: print = "H";
							break;
					case 3: print = "O";
							break;
					default: print = "default";
							 break;	 
				}
				if (i !=  0  && i % this.length == 0){
					System.out.println("");
				}
				System.out.printf("%s \t", print);
			}
			System.out.printf("\n");
	}

	public int getLength()
	{
		return this.length;
	}
}