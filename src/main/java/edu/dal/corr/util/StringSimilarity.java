package edu.dal.corr.util;

/**
 * @since 2016.07.24
 */
public class StringSimilarity
{
	public static double lcs(String word1,String word2)
	{	
		int ret_value = lcsImpl(word1, word2);
		double value_NLCS= (ret_value*1.0)/Math.max(word1.length(),word2.length());	
		double value_MCLCS_0 = mclcs0(word1,word2);
		double value_MCLCS_n = mclcsN(word1,word2);
		double value_MCLCS_z = mclcsZ(word1,word2);
		return 0.25*value_NLCS + 0.25*value_MCLCS_0 + 0.25*value_MCLCS_n + 0.25*value_MCLCS_z;
	}

	private static int lcsImpl(String word1,String word2){
		int lenE = word1.length();
		int lenF = word2.length();
		int[] kk = new int[lenE];
		int return_value = lenE;
		
		
		for(int i=0;i<lenE;i++) {
			kk[i]=lenF;
		}
		
		for(int i=0;i<lenE;i++) {
			for(int j=lenF-1;j>=0;j--) {
				if(j==(lenF-1)) {
					if(word1.substring(i, i+1).equals(word2.substring(j))) {
						int l=0;
						while(j>kk[l]) {
							l++;
						}
						kk[l]=j;
					}
				} else {
					if(word1.substring(i, i+1).equals(word2.substring(j, j+1))) {
						int l=0;
						while(j>kk[l]) {
							l++;
						}
						kk[l]=j;
					}
				}
			}
		}
		for(int i=0;i<lenE;i++) {
			if(kk[i]==lenF) {
				return_value = i;
				break;
			}
		}
		return return_value;
	}
	
	public static double mclcs0(String word1,String word2){
		int min_char_match=1;
		double val =0.0;
		String tmp="";
		if(word1.length()>word2.length()) {
			tmp = word1;
			word1 = word2;
			word2= tmp;
		}
		while(word1.length()>=min_char_match) {
			if(word2.indexOf(word1)==0) {
				val = (word1.length()*1.0)/Math.max(word1.length(),word2.length());
			}
			if(val>0) {
				return val;
			} else {
			  word1=word1.substring(0,word1.length()-1);		
			}
		}
		return 0;
	}
	
	public static double mclcsN(String word1,String word2)
	{
		double val =0.0;
		String tmp="";
		int tmpLen =0;
		if(word1.length()>word2.length()) {
			tmp = word1;
			word1 = word2;
			word2= tmp;
		}
		tmpLen = word1.length();
		while(tmpLen>0)
		{
			int i=0;
			while(i<=(word1.length()-tmpLen)) {
				if(i<=tmpLen) {
					String temp_str= word1.substring(i, tmpLen);
					if(word2.indexOf(temp_str)!=-1) {
						val = (temp_str.length()*1.0)/Math.max(word1.length(),word2.length());
					}
					if(val>0) {
						return val;
					}
				}
				i++;	
			}
			tmpLen--;
		}
		return 0.0;
	}
	
	public static double mclcsZ(String word1,String word2)
	{
		double val =0.0;
		String tmp="";
		if(word1.length()>word2.length()) {
			tmp = word1;
			word1 = word2;
			word2= tmp;
		}
		int i=1;
		while(i<=word1.length()) {
			String temp_str1 = word1.substring(word1.length()-i,word1.length());
			String temp_str2 = word2.substring(word2.length()-i,word2.length());
			if(temp_str1.equals(temp_str2)) {
				val = (temp_str1.length()*1.0)/Math.max(word1.length(),word2.length());
			}
			i++;
		}
		return val;
	}
}
