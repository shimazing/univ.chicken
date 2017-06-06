package ab.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class test {
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		String state1 = "12312434125123124215";
		String state2= "12312434125123124215";
		String state3= "12312512512411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412411241251243125412412412412512431254124124112412512431254124124112412512431254124124112412512431254124124112412512431254124124112412512431254124124112412512431254124124112412512431254124124144125123124216";
		
		System.out.println(state1.hashCode());
		System.out.println(state2.hashCode());
		System.out.println(state3.hashCode());
		/*FileInputStream fis2 = new FileInputStream("str2arrayDic_50.data");
        ObjectInputStream ois2 = new ObjectInputStream(fis2);
        HashMap<String, int[][]> strToarrayStateDic = (HashMap<String,int[][]>) ois2.readObject();
        
        for (String key: strToarrayStateDic.keySet()){
        	int count = 0;
        	for (String key2: strToarrayStateDic.keySet()){
        		System.out.print(Math.round(getStateDist(strToarrayStateDic.get(key),strToarrayStateDic.get(key2)))+ " ");
        	}
        	System.out.println();
        }*/
	}

	public static double getStateDist(int[][] state1, int[][] state2){
		double sum = 0;
		for(int i = 0 ; i < state1.length;i++){
			for(int j = 0; j < state1[i].length; j++){
				sum += Math.pow((state1[i][j]-state2[i][j]),2);
			}
		}
		if(sum ==0){
			return 0.0;
		}else{
			double dist = Math.sqrt(sum);
			return dist;
		}
	}
	
}

